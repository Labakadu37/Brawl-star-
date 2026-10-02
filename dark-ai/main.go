// DARK AI - application locale.
//
// Un seul exécutable : il sert l'interface (dossier web/ embarqué), relaie le
// chat vers Ollama, installe Ollama et le modèle au premier lancement, et
// ouvre l'interface dans sa propre fenêtre (Edge/Chrome en mode application).
package main

import (
	"bufio"
	"bytes"
	"embed"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"io/fs"
	"log"
	"net"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"regexp"
	"runtime"
	"strconv"
	"strings"
	"sync"
	"time"
)

//go:embed web
var webFS embed.FS

//go:embed Modelfile
var modelfile string

const (
	addr      = "127.0.0.1:7666"
	ollamaURL = "http://127.0.0.1:11434"
	modelName = "dark"
	setupURL  = "https://ollama.com/download/OllamaSetup.exe"
)

// state est ce que l'interface affiche pendant l'installation.
type state struct {
	Ollama   string  `json:"ollama"` // missing | starting | running
	HasModel bool    `json:"hasModel"`
	Busy     bool    `json:"busy"`
	Step     string  `json:"step"`
	Progress float64 `json:"progress"` // 0..1, -1 = indéterminé
	Error    string  `json:"error"`
	RAMGB    int     `json:"ramGB"`
	OS       string  `json:"os"`
}

var (
	mu       sync.Mutex
	st       = state{Ollama: "starting", Progress: -1, OS: runtime.GOOS}
	ollamaPr *exec.Cmd // processus "ollama serve" lancé par nous
	lastPing = time.Now()
)

func setState(f func(s *state)) {
	mu.Lock()
	defer mu.Unlock()
	f(&st)
}

func main() {
	logDir, _ := os.UserCacheDir()
	logDir = filepath.Join(logDir, "DARK")
	os.MkdirAll(logDir, 0o755)
	if f, err := os.OpenFile(filepath.Join(logDir, "dark.log"), os.O_CREATE|os.O_WRONLY|os.O_TRUNC, 0o644); err == nil {
		log.SetOutput(f)
	}

	ln, err := net.Listen("tcp", addr)
	if err != nil {
		// Déjà lancé : on ouvre juste une nouvelle fenêtre.
		openWindow("http://"+addr, logDir)
		return
	}

	setState(func(s *state) { s.RAMGB = totalRAMGB() })
	go ensureOllama()

	web, _ := fs.Sub(webFS, "web")
	mux := http.NewServeMux()
	mux.Handle("/", http.FileServer(http.FS(web)))
	mux.HandleFunc("/api/ping", handlePing)
	mux.HandleFunc("/api/status", handleStatus)
	mux.HandleFunc("/api/install-ollama", handleInstallOllama)
	mux.HandleFunc("/api/retry", handleRetry)
	mux.HandleFunc("/api/setup", handleSetup)
	mux.HandleFunc("/api/models", handleModels)
	mux.HandleFunc("/api/chat", handleChat)
	go http.Serve(ln, mux)

	go openWindow("http://"+addr, logDir)

	// On s'arrête quand plus aucune fenêtre ne donne signe de vie
	// (marge large : une fenêtre réduite ralentit ses minuteurs).
	for {
		time.Sleep(5 * time.Second)
		mu.Lock()
		idle := time.Since(lastPing)
		busy := st.Busy
		mu.Unlock()
		if idle > 3*time.Minute && !busy {
			break
		}
	}
	if ollamaPr != nil && ollamaPr.Process != nil {
		ollamaPr.Process.Kill()
	}
}

// ---------- Ollama ----------

func ollamaUp() bool {
	c := http.Client{Timeout: 2 * time.Second}
	r, err := c.Get(ollamaURL + "/api/version")
	if err != nil {
		return false
	}
	r.Body.Close()
	return r.StatusCode == 200
}

func findOllama() string {
	if p, err := exec.LookPath("ollama"); err == nil {
		return p
	}
	for _, p := range []string{
		filepath.Join(os.Getenv("LOCALAPPDATA"), "Programs", "Ollama", "ollama.exe"),
		filepath.Join(os.Getenv("ProgramFiles"), "Ollama", "ollama.exe"),
		"/usr/local/bin/ollama",
		"/Applications/Ollama.app/Contents/Resources/ollama",
	} {
		if _, err := os.Stat(p); err == nil {
			return p
		}
	}
	return ""
}

// ensureOllama démarre Ollama si besoin et met à jour l'état.
func ensureOllama() bool {
	if !ollamaUp() {
		bin := findOllama()
		if bin == "" {
			setState(func(s *state) { s.Ollama = "missing" })
			return false
		}
		setState(func(s *state) { s.Ollama = "starting" })
		cmd := exec.Command(bin, "serve")
		hideWindow(cmd)
		if err := cmd.Start(); err != nil {
			log.Println("ollama serve:", err)
		} else {
			ollamaPr = cmd
		}
		for i := 0; i < 40 && !ollamaUp(); i++ {
			time.Sleep(500 * time.Millisecond)
		}
		if !ollamaUp() {
			setState(func(s *state) { s.Ollama = "missing"; s.Error = "Ollama ne démarre pas." })
			return false
		}
	}
	has := hasModel()
	setState(func(s *state) { s.Ollama = "running"; s.HasModel = has })
	return true
}

func listModels() ([]string, error) {
	c := http.Client{Timeout: 5 * time.Second}
	r, err := c.Get(ollamaURL + "/api/tags")
	if err != nil {
		return nil, err
	}
	defer r.Body.Close()
	var data struct {
		Models []struct {
			Name string `json:"name"`
		} `json:"models"`
	}
	if err := json.NewDecoder(r.Body).Decode(&data); err != nil {
		return nil, err
	}
	names := make([]string, 0, len(data.Models))
	for _, m := range data.Models {
		names = append(names, m.Name)
	}
	return names, nil
}

func hasModel() bool {
	names, _ := listModels()
	for _, n := range names {
		if n == modelName || n == modelName+":latest" {
			return true
		}
	}
	return false
}

// ---------- Installation ----------

func handleInstallOllama(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost || !startBusy("Téléchargement d'Ollama...") {
		http.Error(w, "occupé", http.StatusConflict)
		return
	}
	go func() {
		err := installOllama()
		if err == nil && !ensureOllama() {
			err = errors.New("Ollama est installé mais ne répond pas. Redémarre DARK.")
		}
		endBusy(err)
	}()
	w.WriteHeader(http.StatusAccepted)
}

func installOllama() error {
	if runtime.GOOS != "windows" {
		return errors.New("Installe Ollama avec : curl -fsSL https://ollama.com/install.sh | sh")
	}
	dst := filepath.Join(os.TempDir(), "OllamaSetup.exe")
	if err := download(setupURL, dst); err != nil {
		return fmt.Errorf("téléchargement d'Ollama : %w", err)
	}
	setState(func(s *state) { s.Step = "Installation d'Ollama..."; s.Progress = -1 })
	if err := exec.Command(dst, "/SILENT", "/NORESTART").Run(); err != nil {
		return fmt.Errorf("installation d'Ollama : %w", err)
	}
	return nil
}

func download(url, dst string) error {
	r, err := http.Get(url)
	if err != nil {
		return err
	}
	defer r.Body.Close()
	if r.StatusCode != 200 {
		return errors.New(r.Status)
	}
	f, err := os.Create(dst)
	if err != nil {
		return err
	}
	defer f.Close()
	var done int64
	buf := make([]byte, 256<<10)
	for {
		n, rerr := r.Body.Read(buf)
		if n > 0 {
			if _, err := f.Write(buf[:n]); err != nil {
				return err
			}
			done += int64(n)
			if r.ContentLength > 0 {
				p := float64(done) / float64(r.ContentLength)
				setState(func(s *state) { s.Progress = p })
			}
		}
		if rerr == io.EOF {
			return nil
		}
		if rerr != nil {
			return rerr
		}
	}
}

func handleRetry(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost || !startBusy("Recherche d'Ollama...") {
		http.Error(w, "occupé", http.StatusConflict)
		return
	}
	go func() {
		ensureOllama()
		endBusy(nil)
	}()
	w.WriteHeader(http.StatusAccepted)
}

var allowedBases = map[string]bool{
	"qwen3:4b": true, "qwen3:8b": true, "qwen3:14b": true,
	"gpt-oss:20b": true, "qwen3:32b": true, "gpt-oss:120b": true,
}

func handleSetup(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Base string `json:"base"`
	}
	json.NewDecoder(r.Body).Decode(&req)
	if r.Method != http.MethodPost || !allowedBases[req.Base] {
		http.Error(w, "modèle inconnu", http.StatusBadRequest)
		return
	}
	if !startBusy("Téléchargement de " + req.Base + "...") {
		http.Error(w, "occupé", http.StatusConflict)
		return
	}
	go func() {
		err := pullModel(req.Base)
		if err == nil {
			setState(func(s *state) { s.Step = "Création de DARK..."; s.Progress = -1 })
			err = createModel(req.Base)
		}
		if err == nil {
			setState(func(s *state) { s.HasModel = true })
		}
		endBusy(err)
	}()
	w.WriteHeader(http.StatusAccepted)
}

func pullModel(base string) error {
	body, _ := json.Marshal(map[string]any{"model": base, "stream": true})
	r, err := http.Post(ollamaURL+"/api/pull", "application/json", bytes.NewReader(body))
	if err != nil {
		return err
	}
	defer r.Body.Close()
	sc := bufio.NewScanner(r.Body)
	for sc.Scan() {
		var m struct {
			Status    string `json:"status"`
			Total     int64  `json:"total"`
			Completed int64  `json:"completed"`
			Error     string `json:"error"`
		}
		if json.Unmarshal(sc.Bytes(), &m) != nil {
			continue
		}
		if m.Error != "" {
			return errors.New(m.Error)
		}
		setState(func(s *state) {
			if m.Total > 0 {
				s.Progress = float64(m.Completed) / float64(m.Total)
				s.Step = fmt.Sprintf("Téléchargement de %s : %.1f / %.1f Go", base, gb(m.Completed), gb(m.Total))
			}
		})
	}
	return sc.Err()
}

func gb(n int64) float64 { return float64(n) / (1 << 30) }

// createModel fabrique "dark" à partir du modèle de base et du Modelfile embarqué.
func createModel(base string) error {
	system := ""
	if m := regexp.MustCompile(`(?s)SYSTEM\s+"""(.*?)"""`).FindStringSubmatch(modelfile); m != nil {
		system = strings.TrimSpace(m[1])
	}
	params := map[string]any{}
	for _, line := range strings.Split(modelfile, "\n") {
		f := strings.Fields(line)
		if len(f) == 3 && f[0] == "PARAMETER" {
			if v, err := strconv.ParseFloat(f[2], 64); err == nil {
				params[f[1]] = v
			} else {
				params[f[1]] = f[2]
			}
		}
	}
	body, _ := json.Marshal(map[string]any{
		"model": modelName, "from": base, "system": system,
		"parameters": params, "stream": false,
	})
	r, err := http.Post(ollamaURL+"/api/create", "application/json", bytes.NewReader(body))
	if err != nil {
		return err
	}
	defer r.Body.Close()
	if r.StatusCode != 200 {
		msg, _ := io.ReadAll(r.Body)
		return fmt.Errorf("création du modèle : %s", msg)
	}
	return nil
}

func startBusy(step string) bool {
	mu.Lock()
	defer mu.Unlock()
	if st.Busy {
		return false
	}
	st.Busy, st.Step, st.Progress, st.Error = true, step, -1, ""
	return true
}

func endBusy(err error) {
	if err != nil {
		log.Println(err)
	}
	setState(func(s *state) {
		s.Busy, s.Step, s.Progress = false, "", -1
		if err != nil {
			s.Error = err.Error()
		}
	})
}

// ---------- API pour l'interface ----------

func writeJSON(w http.ResponseWriter, code int, v any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(code)
	json.NewEncoder(w).Encode(v)
}

func handlePing(w http.ResponseWriter, r *http.Request) {
	mu.Lock()
	lastPing = time.Now()
	mu.Unlock()
	w.WriteHeader(http.StatusNoContent)
}

func handleStatus(w http.ResponseWriter, r *http.Request) {
	mu.Lock()
	s := st
	mu.Unlock()
	writeJSON(w, 200, s)
}

func handleModels(w http.ResponseWriter, r *http.Request) {
	names, err := listModels()
	if err != nil {
		writeJSON(w, 503, map[string]string{"error": "Ollama ne répond pas."})
		return
	}
	writeJSON(w, 200, map[string]any{"default": modelName, "models": names})
}

func handleChat(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "POST", http.StatusMethodNotAllowed)
		return
	}
	var req struct {
		Model    string            `json:"model"`
		Messages []json.RawMessage `json:"messages"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		writeJSON(w, 400, map[string]string{"error": err.Error()})
		return
	}
	if req.Model == "" {
		req.Model = modelName
	}
	body, _ := json.Marshal(map[string]any{"model": req.Model, "messages": req.Messages, "stream": true})
	up, err := http.NewRequestWithContext(r.Context(), http.MethodPost, ollamaURL+"/api/chat", bytes.NewReader(body))
	if err != nil {
		writeJSON(w, 500, map[string]string{"error": err.Error()})
		return
	}
	up.Header.Set("Content-Type", "application/json")
	resp, err := http.DefaultClient.Do(up)
	if err != nil {
		writeJSON(w, 503, map[string]string{"error": "Ollama ne répond pas."})
		return
	}
	defer resp.Body.Close()
	if resp.StatusCode != 200 {
		msg, _ := io.ReadAll(resp.Body)
		writeJSON(w, resp.StatusCode, map[string]string{"error": string(msg)})
		return
	}
	w.Header().Set("Content-Type", "application/x-ndjson")
	w.Header().Set("Cache-Control", "no-cache")
	fl, _ := w.(http.Flusher)
	buf := make([]byte, 4096)
	for {
		n, err := resp.Body.Read(buf)
		if n > 0 {
			if _, werr := w.Write(buf[:n]); werr != nil {
				return // fenêtre fermée ou "Stop"
			}
			if fl != nil {
				fl.Flush()
			}
		}
		if err != nil {
			return
		}
	}
}

// ---------- Fenêtre ----------

// openWindow ouvre l'interface en mode application (sans barre d'adresse).
// Edge est présent sur tous les Windows 10/11 ; sinon Chrome, sinon le navigateur par défaut.
func openWindow(url, dataDir string) {
	args := []string{
		"--app=" + url,
		"--user-data-dir=" + filepath.Join(dataDir, "window"),
		"--window-size=1100,800",
		"--no-first-run", "--no-default-browser-check",
	}
	for _, b := range browserCandidates() {
		if _, err := os.Stat(b); err == nil {
			if err := exec.Command(b, args...).Start(); err == nil {
				return
			}
		}
	}
	openDefault(url)
}
