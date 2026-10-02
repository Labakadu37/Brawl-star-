@echo off
chcp 65001 >nul
title DARK AI - Installation
color 0C
cd /d "%~dp0"

echo.
echo   ==========================================
echo                D A R K   A I
echo        IA locale - installation Windows
echo   ==========================================
echo.

where ollama >nul 2>nul
if errorlevel 1 (
  echo [*] Installation d'Ollama...
  winget install -e --id Ollama.Ollama --accept-source-agreements --accept-package-agreements
  if errorlevel 1 (
    echo [!] Echec. Installe Ollama a la main : https://ollama.com/download
    echo     puis relance install.bat
    start https://ollama.com/download
    pause
    exit /b 1
  )
  set "PATH=%PATH%;%LOCALAPPDATA%\Programs\Ollama"
)

where python >nul 2>nul
if errorlevel 1 (
  echo [*] Installation de Python...
  winget install -e --id Python.Python.3.12 --accept-source-agreements --accept-package-agreements
  echo [!] Python installe. Ferme cette fenetre et relance install.bat.
  pause
  exit /b 0
)

echo.
echo   Choisis le cerveau de DARK selon ton PC :
echo.
echo   1^) qwen3:4b      leger     - 8 Go RAM, pas de carte graphique
echo   2^) qwen3:8b      equilibre - 16 Go RAM ou GPU 8 Go   [conseille]
echo   3^) qwen3:14b     fort      - GPU 12 Go
echo   4^) gpt-oss:20b   tres fort - GPU 16 Go
echo   5^) qwen3:32b     brutal    - GPU 24 Go
echo   6^) gpt-oss:120b  le max    - 64 Go+ de RAM/VRAM
echo.
set "CHOICE=2"
set /p "CHOICE=Ton choix [2] : "
set "BASE=qwen3:8b"
if "%CHOICE%"=="1" set "BASE=qwen3:4b"
if "%CHOICE%"=="3" set "BASE=qwen3:14b"
if "%CHOICE%"=="4" set "BASE=gpt-oss:20b"
if "%CHOICE%"=="5" set "BASE=qwen3:32b"
if "%CHOICE%"=="6" set "BASE=gpt-oss:120b"

echo.
echo [*] Telechargement de %BASE% (plusieurs Go, patience)...
ollama pull %BASE%
if errorlevel 1 (
  echo [!] Ollama ne repond pas. Lance l'application Ollama puis relance install.bat.
  pause
  exit /b 1
)

echo [*] Creation du modele "dark"...
powershell -NoProfile -Command "(Get-Content -Raw -Encoding UTF8 Modelfile) -replace '(?m)^FROM .*$', 'FROM %BASE%' | Set-Content -Encoding UTF8 Modelfile"
ollama create dark -f Modelfile
if errorlevel 1 ( pause & exit /b 1 )

echo.
echo   Installation terminee. Lance start.bat pour reveiller DARK.
echo.
pause
