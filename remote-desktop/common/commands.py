"""Run a small set of standard remote-admin actions on the client PC.

These are the ordinary actions a remote-desktop tool offers for a machine
you own (lock, sleep, restart, shut down). Nothing here is hidden or
destructive to data; power actions are confirmed on the admin side before
being sent.
"""

import subprocess
import sys

# The commands this client is willing to run. "message" is handled by the
# GUI (it shows a popup), not here.
POWER_COMMANDS = ("lock", "sleep", "restart", "shutdown")


def run_power_command(kind):
    """Execute a power/session action. Returns True if a command was run."""
    win = sys.platform.startswith("win")
    mac = sys.platform == "darwin"

    if kind == "lock":
        if win:
            import ctypes
            ctypes.windll.user32.LockWorkStation()
        elif mac:
            subprocess.Popen(["pmset", "displaysleepnow"])
        else:
            subprocess.Popen(["loginctl", "lock-session"])
        return True

    if kind == "sleep":
        if win:
            subprocess.Popen(["rundll32.exe", "powrprof.dll,SetSuspendState",
                              "0,1,0"])
        elif mac:
            subprocess.Popen(["pmset", "sleepnow"])
        else:
            subprocess.Popen(["systemctl", "suspend"])
        return True

    if kind == "restart":
        if win:
            subprocess.Popen(["shutdown", "/r", "/t", "0"])
        elif mac:
            subprocess.Popen(["osascript", "-e", 'tell app "System Events" '
                              'to restart'])
        else:
            subprocess.Popen(["systemctl", "reboot"])
        return True

    if kind == "shutdown":
        if win:
            subprocess.Popen(["shutdown", "/s", "/t", "0"])
        elif mac:
            subprocess.Popen(["osascript", "-e", 'tell app "System Events" '
                              'to shut down'])
        else:
            subprocess.Popen(["systemctl", "poweroff"])
        return True

    return False
