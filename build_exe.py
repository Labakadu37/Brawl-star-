"""
Build script to create discord_manager.exe

Usage:
    pip install -r requirements.txt
    python build_exe.py

The .exe will be in the dist/ folder.
"""
import subprocess
import sys


def build():
    cmd = [
        sys.executable,
        "-m",
        "PyInstaller",
        "--onefile",
        "--windowed",
        "--name",
        "DiscordManager",
        "--add-data",
        "customtkinter;customtkinter" if sys.platform == "win32" else "customtkinter:customtkinter",
        "discord_manager.py",
    ]
    print(f"Running: {' '.join(cmd)}")
    subprocess.run(cmd, check=True)
    print("\nBuild done! .exe is in dist/DiscordManager.exe")


if __name__ == "__main__":
    build()
