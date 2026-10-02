#!/usr/bin/env bash
# DARK AI - installation Linux / macOS
set -e
cd "$(dirname "$0")"
RED='\033[0;31m'; NC='\033[0m'
echo -e "${RED}\n  ==========  D A R K   A I  ==========\n${NC}"

if ! command -v ollama >/dev/null; then
  echo "[*] Installation d'Ollama..."
  if [[ "$(uname)" == "Darwin" ]] && command -v brew >/dev/null; then
    brew install ollama
  else
    curl -fsSL https://ollama.com/install.sh | sh
  fi
fi
command -v python3 >/dev/null || { echo "[!] Installe python3 puis relance."; exit 1; }

curl -s http://127.0.0.1:11434/api/tags >/dev/null || { (ollama serve >/dev/null 2>&1 &); sleep 3; }

cat <<MENU
  Choisis le cerveau de DARK selon ton PC :
  1) qwen3:4b      leger     - 8 Go RAM, pas de GPU
  2) qwen3:8b      equilibre - 16 Go RAM ou GPU 8 Go   [conseille]
  3) qwen3:14b     fort      - GPU 12 Go
  4) gpt-oss:20b   tres fort - GPU 16 Go
  5) qwen3:32b     brutal    - GPU 24 Go
  6) gpt-oss:120b  le max    - 64 Go+ de RAM/VRAM
MENU
read -rp "Ton choix [2] : " CHOICE
case "${CHOICE:-2}" in
  1) BASE=qwen3:4b ;; 3) BASE=qwen3:14b ;; 4) BASE=gpt-oss:20b ;;
  5) BASE=qwen3:32b ;; 6) BASE=gpt-oss:120b ;; *) BASE=qwen3:8b ;;
esac

echo "[*] Telechargement de $BASE ..."
ollama pull "$BASE"
sed "s|^FROM .*|FROM $BASE|" Modelfile > Modelfile.tmp && mv Modelfile.tmp Modelfile
ollama create dark -f Modelfile
echo -e "${RED}\n  Termine. Lance ./start.sh pour reveiller DARK.\n${NC}"
