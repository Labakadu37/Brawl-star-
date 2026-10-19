@echo off
rem ============================================================
rem  Remote Desktop - construit les .exe Windows (double-clic)
rem  Produit: dist\RemoteClient.exe  et  dist\RemoteAdmin.exe
rem ============================================================
setlocal
cd /d "%~dp0"

where py >nul 2>nul && (set "PY=py") || (set "PY=python")

echo.
echo === Installation des dependances (PyInstaller + libs) ===
%PY% -m pip install --upgrade pip
%PY% -m pip install pyinstaller -r "..\client\requirements.txt"
if errorlevel 1 (
  echo.
  echo ERREUR: installation des dependances echouee. Python est-il installe ?
  echo Telecharge-le sur https://www.python.org/downloads/ ^(coche "Add to PATH"^).
  pause
  exit /b 1
)

echo.
echo === Construction des executables ===
%PY% build.py
if errorlevel 1 (
  echo.
  echo ERREUR pendant la construction. Lis les messages ci-dessus.
  pause
  exit /b 1
)

echo.
echo ============================================================
echo  Termine ! Dans le dossier "dist" :
echo    RemoteClient.exe  -^> a copier sur le PC a controler
echo    RemoteAdmin.exe   -^> a lancer sur le PC qui controle
echo ============================================================
pause
