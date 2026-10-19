#!/usr/bin/env python3
"""Build standalone executables for the client and admin apps.

Run this ON THE OS YOU WANT TO TARGET:
  * Windows  -> dist/RemoteClient.exe  and  dist/RemoteAdmin.exe
  * Linux    -> dist/RemoteClient       and  dist/RemoteAdmin
  * macOS    -> dist/RemoteClient.app   (bundle)

    python build.py            # build both apps
    python build.py client     # build only the client
    python build.py admin      # build only the admin

PyInstaller cannot cross-compile: to get Windows .exe files you must run
this on a Windows machine.
"""

import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)              # the remote-desktop/ directory

# pynput and mss pick a platform backend at runtime via importlib, so
# PyInstaller needs to be told to bundle the right one for THIS OS.
if sys.platform.startswith("win"):
    CLIENT_HIDDEN = ["pynput.keyboard._win32", "pynput.mouse._win32",
                     "mss.windows", "pystray._win32"]
elif sys.platform == "darwin":
    CLIENT_HIDDEN = ["pynput.keyboard._darwin", "pynput.mouse._darwin",
                     "mss.darwin"]
else:
    CLIENT_HIDDEN = ["pynput.keyboard._xorg", "pynput.mouse._xorg",
                     "mss.linux"]

TARGETS = {
    "client": {"script": os.path.join(ROOT, "client", "client_app.py"),
               "name": "RemoteClient", "hidden": CLIENT_HIDDEN},
    "admin": {"script": os.path.join(ROOT, "admin", "admin_app.py"),
              "name": "RemoteAdmin", "hidden": []},
}


def ensure_pyinstaller():
    try:
        import PyInstaller  # noqa: F401
    except ImportError:
        print(">> installation de PyInstaller…")
        subprocess.check_call([sys.executable, "-m", "pip", "install",
                               "pyinstaller"])


def build(key):
    t = TARGETS[key]
    cmd = [sys.executable, "-m", "PyInstaller", "--noconfirm", "--clean",
           "--onefile", "--noconsole", "--name", t["name"],
           "--paths", ROOT,                       # so `common` is importable
           "--distpath", os.path.join(HERE, "dist"),
           "--workpath", os.path.join(HERE, "work"),
           "--specpath", os.path.join(HERE, "work")]
    for mod in t["hidden"]:
        cmd += ["--hidden-import", mod]
    cmd.append(t["script"])
    print(">>", " ".join(cmd))
    subprocess.check_call(cmd)


def main():
    which = sys.argv[1:] or ["client", "admin"]
    unknown = [w for w in which if w not in TARGETS]
    if unknown:
        sys.exit("Cible(s) inconnue(s): %s  (client|admin)" % ", ".join(unknown))
    ensure_pyinstaller()
    for key in which:
        build(key)
    print("\n=== Terminé. Exécutables dans: %s ===" % os.path.join(HERE, "dist"))


if __name__ == "__main__":
    main()
