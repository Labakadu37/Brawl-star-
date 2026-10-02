@echo off
title DARK AI
color 0C
cd /d "%~dp0"
tasklist /fi "imagename eq ollama.exe" | find /i "ollama.exe" >nul || start "" /min ollama serve
python server.py
pause
