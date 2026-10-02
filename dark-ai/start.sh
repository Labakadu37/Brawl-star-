#!/usr/bin/env bash
cd "$(dirname "$0")"
curl -s http://127.0.0.1:11434/api/tags >/dev/null || { (ollama serve >/dev/null 2>&1 &); sleep 3; }
exec python3 server.py
