# DARK AI

IA 100 % locale, style dark, en **un seul fichier : `DARK.exe`**.
Pas d'abonnement, pas de compte, rien ne quitte ton PC.

## Lancer (Windows 10 / 11)

1. Télécharge `dist/DARK.exe` et double-clique dessus.
2. Si Windows affiche « Windows a protégé votre ordinateur » :
   **Informations complémentaires → Exécuter quand même**
   (normal pour un .exe fait maison, non signé).
3. Au premier lancement, DARK s'occupe de tout dans sa fenêtre :
   - installe son moteur **Ollama** (un clic),
   - te fait choisir son cerveau selon ton PC et le télécharge,
   - crée le modèle `dark` avec sa personnalité.
4. Les fois suivantes, il s'ouvre direct sur le chat.
   Ferme la fenêtre et DARK s'arrête tout seul.

## Quel cerveau choisir ?

| Nom        | Modèle         | Il te faut                            | Taille |
|------------|----------------|---------------------------------------|--------|
| Léger      | `qwen3:4b`     | 8 Go de RAM, pas de carte graphique   | 2,5 Go |
| Équilibré  | `qwen3:8b`     | 16 Go de RAM ou GPU 8 Go (conseillé)  | 5 Go   |
| Fort       | `qwen3:14b`    | GPU 12 Go                             | 9 Go   |
| Très fort  | `gpt-oss:20b`  | GPU 16 Go                             | 14 Go  |
| Brutal     | `qwen3:32b`    | GPU 24 Go                             | 20 Go  |
| Le maximum | `gpt-oss:120b` | 64 Go+ de RAM/VRAM                    | 65 Go  |

Pour changer de cerveau : `ollama rm dark` dans un terminal, puis relance DARK.

## Personnaliser et recompiler

- **Personnalité** : bloc `SYSTEM` dans `Modelfile`.
- **Logo / couleurs / textes** : `web/logo.png`, `web/favicon.png`, `web/index.html`.

Tout est embarqué dans l'exe, donc recompile après une modif
(Go 1.22+, marche depuis Windows, Linux ou Mac) :

```bash
GOOS=windows GOARCH=amd64 go build -trimpath -ldflags "-s -w -H windowsgui" -o dist/DARK.exe .
```

Pour une nouvelle icône d'exe :
`go-winres simply --icon icone.png --manifest gui --arch amd64`
(outil : `go install github.com/tc-hib/go-winres@latest`).

Puis `ollama rm dark` et relance DARK pour appliquer une nouvelle personnalité.

## Comment ça marche

`DARK.exe` (Go, aucune dépendance) lance un mini-serveur sur `127.0.0.1:7666`,
démarre Ollama en arrière-plan, et ouvre l'interface dans une fenêtre
d'application Edge (sans barre d'adresse). Le journal est dans
`%LOCALAPPDATA%\DARK\dark.log`.

```
dark-ai/
├── dist/DARK.exe              l'application prête à lancer
├── main.go                    serveur, installation, fenêtre
├── sys_windows.go / sys_other.go
├── rsrc_windows_amd64.syso    icône + infos de l'exe
├── Modelfile                  personnalité de DARK
└── web/                       interface
```
