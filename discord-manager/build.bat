@echo off
REM Construit DiscordManager.exe dans le dossier dist\
cd /d "%~dp0"
python -m pip install -r requirements.txt pyinstaller || goto :error
python -m PyInstaller --noconfirm --onefile --windowed --name DiscordManager --collect-data customtkinter app.py || goto :error
echo.
echo OK : dist\DiscordManager.exe
pause
exit /b 0
:error
echo.
echo Erreur pendant le build.
pause
exit /b 1
