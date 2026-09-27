"""
Build script to create DiscordManager.exe

Usage:
    pip install -r requirements.txt
    python build_exe.py

The .exe will be in the dist/ folder.
"""
import subprocess
import sys
import importlib.util
import os


def build():
    # Find customtkinter's actual install path
    spec = importlib.util.find_spec("customtkinter")
    if spec is None or spec.origin is None:
        print("ERROR: customtkinter not installed. Run: pip install customtkinter")
        sys.exit(1)
    ctk_path = os.path.dirname(spec.origin)

    sep = ";" if sys.platform == "win32" else ":"
    add_data = f"{ctk_path}{sep}customtkinter"

    cmd = [
        sys.executable,
        "-m",
        "PyInstaller",
        "--onefile",
        "--windowed",
        "--name", "DiscordManager",
        "--add-data", add_data,
        "--collect-all", "customtkinter",
        "discord_manager.py",
    ]
    print(f"customtkinter found at: {ctk_path}")
    print(f"Running: {' '.join(cmd)}")
    subprocess.run(cmd, check=True)
    print("\nBuild done! .exe is in dist/DiscordManager.exe")


if __name__ == "__main__":
    build()
