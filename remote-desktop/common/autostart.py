"""Start the client automatically on Windows login (unattended access).

This writes a normal, visible entry under the current user's Run key — the
same mechanism ordinary apps use. It shows up in Task Manager > Startup and
can be removed from there or with the tray menu / --no-autostart.

It is intentionally NOT a hidden install: the client keeps a tray icon so
the person at the PC can always see it is running and stop it. This module
only handles "launch on login", never concealment.
"""

import os
import sys

RUN_KEY = r"Software\Microsoft\Windows\CurrentVersion\Run"
VALUE_NAME = "RemoteDesktopClient"


def _launch_command():
    """Command that relaunches this client, minimized to the tray."""
    if getattr(sys, "frozen", False):            # built .exe
        return '"%s" --tray' % sys.executable
    script = os.path.abspath(os.path.join(os.path.dirname(__file__), "..",
                                          "client", "client_app.py"))
    pyw = os.path.join(os.path.dirname(sys.executable), "pythonw.exe")
    exe = pyw if os.path.exists(pyw) else sys.executable
    return '"%s" "%s" --tray' % (exe, script)


def enable():
    import winreg

    with winreg.OpenKey(winreg.HKEY_CURRENT_USER, RUN_KEY, 0,
                        winreg.KEY_SET_VALUE) as key:
        winreg.SetValueEx(key, VALUE_NAME, 0, winreg.REG_SZ,
                          _launch_command())


def disable():
    import winreg

    try:
        with winreg.OpenKey(winreg.HKEY_CURRENT_USER, RUN_KEY, 0,
                            winreg.KEY_SET_VALUE) as key:
            winreg.DeleteValue(key, VALUE_NAME)
    except FileNotFoundError:
        pass


def is_enabled():
    import winreg

    try:
        with winreg.OpenKey(winreg.HKEY_CURRENT_USER, RUN_KEY, 0,
                            winreg.KEY_QUERY_VALUE) as key:
            winreg.QueryValueEx(key, VALUE_NAME)
        return True
    except FileNotFoundError:
        return False
