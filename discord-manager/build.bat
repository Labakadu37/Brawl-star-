@echo off
REM Construit DiscordManager.exe dans le dossier dist\
python -m pip install --upgrade pyinstaller
python -m PyInstaller --onefile --windowed --name DiscordManager app.py
echo.
echo Fini : dist\DiscordManager.exe
pause
