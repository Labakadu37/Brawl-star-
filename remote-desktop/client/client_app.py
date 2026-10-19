"""Remote Desktop — CLIENT (the PC you want to control).

Run this on the machine you want to reach. It shows a visible window so
the person at this PC always knows the screen is being shared, and it can
be stopped with one click. An admin on the same local network discovers
it automatically (no code to type) and, after presenting the shared
token, can see this screen and move the mouse / type.

    pip install mss pillow pynput
    python client_app.py
"""

import argparse
import io
import os
import queue
import socket
import sys
import threading
import time
import tkinter as tk
from tkinter import messagebox

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from common import protocol
from common.keymap import build_special_keys

# Defaults tuned for a local network: native resolution, crisp JPEG.
FPS = 20
JPEG_QUALITY = 85
MAX_WIDTH = 0              # 0 = never downscale (send the real resolution)


def set_dpi_aware():
    """On Windows, make this process DPI-aware.

    Without this, display scaling (125%, 150%, ...) makes the captured
    pixels and the cursor coordinates disagree, so remote clicks land in
    the wrong place and nothing opens. This fixes that; it is a no-op on
    other systems.
    """
    if not sys.platform.startswith("win"):
        return
    import ctypes

    try:
        ctypes.windll.shcore.SetProcessDpiAwareness(2)   # per-monitor aware
    except Exception:
        try:
            ctypes.windll.user32.SetProcessDPIAware()
        except Exception:
            pass


class ScreenShareService:
    """Owns the TCP listener and one active admin session at a time."""

    def __init__(self, name, port, token, require_approval, ui_queue,
                 fps=FPS, quality=JPEG_QUALITY, max_width=MAX_WIDTH):
        self.name = name
        self.port = port
        self.token = token
        self.require_approval = require_approval
        self.ui = ui_queue            # (tag, value) messages for the GUI
        self.fps = fps
        self.quality = quality
        self.max_width = max_width
        self._input_error_reported = False
        self.stop_event = threading.Event()
        self.session_lock = threading.Lock()
        self.active_admin = None      # address tuple, or None
        self._srv = None
        self._mouse = None
        self._keyboard = None
        self._special = None

    # -- lifecycle -------------------------------------------------------

    def start(self):
        threading.Thread(target=self._accept_loop, daemon=True).start()
        threading.Thread(
            target=protocol.discovery_responder,
            args=(self.name, self.port, self.stop_event),
            daemon=True,
        ).start()

    def stop(self):
        self.stop_event.set()
        if self._srv is not None:
            try:
                self._srv.close()
            except OSError:
                pass

    def kick(self):
        """Drop the current admin session (called from the GUI)."""
        with self.session_lock:
            admin = self.active_admin
        if admin is not None:
            self._active_sock_shutdown()

    def _active_sock_shutdown(self):
        sock = getattr(self, "_active_sock", None)
        if sock is not None:
            try:
                sock.shutdown(socket.SHUT_RDWR)
            except OSError:
                pass

    # -- accept loop -----------------------------------------------------

    def _accept_loop(self):
        srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
            srv.bind(("", self.port))
        except OSError as exc:
            self.ui.put(("fatal", "Impossible d'écouter sur le port %d : %s"
                         % (self.port, exc)))
            return
        srv.listen(1)
        srv.settimeout(0.5)
        self._srv = srv
        self.ui.put(("listening", self.port))

        while not self.stop_event.is_set():
            try:
                conn, addr = srv.accept()
            except socket.timeout:
                continue
            except OSError:
                break
            with self.session_lock:
                busy = self.active_admin is not None
            if busy:
                self._reject(conn, "une autre session est déjà active")
                continue
            threading.Thread(target=self._handle, args=(conn, addr),
                              daemon=True).start()
        srv.close()

    def _reject(self, conn, reason):
        try:
            protocol.send_msg(conn, protocol.MSG_DENIED, {"reason": reason})
        except OSError:
            pass
        conn.close()

    # -- one admin session ----------------------------------------------

    def _handle(self, conn, addr):
        conn.settimeout(10)
        try:
            msg_type, payload = protocol.recv_msg(conn)
            if msg_type != protocol.MSG_HELLO:
                return self._reject(conn, "handshake invalide")
            hello = protocol.decode_json(payload)
            if hello.get("token") != self.token:
                self.ui.put(("status", "Connexion refusée (mauvaise clé) "
                             "depuis %s" % addr[0]))
                return self._reject(conn, "clé incorrecte")

            admin_name = str(hello.get("admin", addr[0]))[:64]
            if self.require_approval and not self._ask_approval(admin_name,
                                                                addr[0]):
                return self._reject(conn, "refusé par l'utilisateur")
        except (OSError, ValueError):
            conn.close()
            return

        with self.session_lock:
            if self.active_admin is not None:
                return self._reject(conn, "une autre session est déjà active")
            self.active_admin = addr
            self._active_sock = conn

        width, height, scale = self._screen_size()
        send_lock = threading.Lock()
        try:
            with send_lock:
                protocol.send_msg(conn, protocol.MSG_WELCOME,
                                  {"name": self.name, "width": width,
                                   "height": height})
            conn.settimeout(None)
            self.ui.put(("connected", "%s (%s)" % (admin_name, addr[0])))

            stop = threading.Event()
            sender = threading.Thread(
                target=self._send_frames, args=(conn, send_lock, scale, stop),
                daemon=True)
            sender.start()
            self._recv_input(conn, width, height, stop)   # blocks until end
            stop.set()
            sender.join(timeout=2)
        except OSError:
            pass
        finally:
            with self.session_lock:
                self.active_admin = None
                self._active_sock = None
            try:
                conn.close()
            except OSError:
                pass
            self.ui.put(("disconnected", None))

    # -- approval prompt (marshalled to the GUI thread) ------------------

    def _ask_approval(self, admin_name, ip):
        result = {}
        done = threading.Event()
        self.ui.put(("approval", (admin_name, ip, result, done)))
        done.wait(timeout=30)
        return bool(result.get("ok"))

    # -- screen capture --------------------------------------------------

    def _screen_size(self):
        import mss

        with mss.mss() as sct:
            mon = sct.monitors[1]
            w, h = mon["width"], mon["height"]
        scale = 1.0
        if self.max_width and w > self.max_width:
            scale = self.max_width / float(w)
        return w, h, scale

    def _send_frames(self, conn, send_lock, scale, stop):
        import mss
        from PIL import Image

        interval = 1.0 / self.fps
        with mss.mss() as sct:
            mon = sct.monitors[1]
            while not stop.is_set() and not self.stop_event.is_set():
                t0 = time.time()
                shot = sct.grab(mon)
                img = Image.frombytes("RGB", shot.size, shot.bgra,
                                      "raw", "BGRX")
                if scale < 1.0:
                    img = img.resize(
                        (max(1, int(img.width * scale)),
                         max(1, int(img.height * scale))),
                        Image.BILINEAR)
                buf = io.BytesIO()
                img.save(buf, "JPEG", quality=self.quality)
                try:
                    with send_lock:
                        protocol.send_msg(conn, protocol.MSG_FRAME,
                                          buf.getvalue())
                except OSError:
                    break
                dt = time.time() - t0
                if dt < interval:
                    time.sleep(interval - dt)

    # -- input replay ----------------------------------------------------

    def _ensure_controllers(self):
        if self._mouse is None:
            from pynput.mouse import Controller as MouseController
            from pynput.keyboard import Controller as KeyboardController
            self._mouse = MouseController()
            self._keyboard = KeyboardController()
            self._special = build_special_keys()

    def _recv_input(self, conn, width, height, stop):
        from pynput.mouse import Button
        from pynput.keyboard import KeyCode

        self._ensure_controllers()
        buttons = {"left": Button.left, "right": Button.right,
                   "middle": Button.middle}
        while not stop.is_set():
            try:
                msg_type, payload = protocol.recv_msg(conn)
            except (OSError, ConnectionError):
                break
            if msg_type == protocol.MSG_BYE:
                break
            if msg_type != protocol.MSG_INPUT:
                continue
            ev = protocol.decode_json(payload)
            kind = ev.get("kind")
            try:
                if kind == "move":
                    self._mouse.position = (int(ev["x"] * width),
                                            int(ev["y"] * height))
                elif kind == "click":
                    self._mouse.position = (int(ev["x"] * width),
                                            int(ev["y"] * height))
                    btn = buttons.get(ev.get("button"), Button.left)
                    if ev.get("pressed"):
                        self._mouse.press(btn)
                    else:
                        self._mouse.release(btn)
                elif kind == "scroll":
                    self._mouse.scroll(ev.get("dx", 0), ev.get("dy", 0))
                elif kind in ("keydown", "keyup"):
                    key = self._resolve_key(ev)
                    if key is None:
                        continue
                    if kind == "keydown":
                        self._keyboard.press(key)
                    else:
                        self._keyboard.release(key)
            except Exception as exc:
                # A single bad event must never kill the session, but the
                # first failure is worth surfacing so a blocked input
                # (e.g. an elevated target window) can be diagnosed.
                if not self._input_error_reported:
                    self._input_error_reported = True
                    self.ui.put(("status",
                                 "⚠ Entrée bloquée : %s" % exc))
                continue

    def _resolve_key(self, ev):
        from pynput.keyboard import KeyCode

        keysym = ev.get("keysym", "")
        if keysym in self._special:
            return self._special[keysym]
        char = ev.get("char") or ""
        if len(char) == 1 and char.isprintable():
            return KeyCode.from_char(char)
        if len(keysym) == 1 and keysym.isprintable():
            return KeyCode.from_char(keysym)
        return None


class ClientGUI:
    def __init__(self, service, info, tray=True):
        self.service = service
        self.ui = service.ui
        self.tray = None
        self._status_text = "En attente d'un admin"
        self.root = tk.Tk()
        self.root.title("Remote Desktop — Client (cet écran est partageable)")
        self.root.geometry("460x360")
        self.root.configure(bg="#7a0b0b")

        tk.Label(self.root, text="PARTAGE D'ÉCRAN", bg="#7a0b0b",
                 fg="white", font=("Helvetica", 20, "bold")).pack(pady=(18, 2))
        self.state_lbl = tk.Label(self.root, text="● En attente d'un admin",
                                  bg="#7a0b0b", fg="#ffd7d7",
                                  font=("Helvetica", 12, "bold"))
        self.state_lbl.pack(pady=(0, 10))

        card = tk.Frame(self.root, bg="#fdecec", bd=0)
        card.pack(fill="x", padx=18)
        for label, value in info:
            row = tk.Frame(card, bg="#fdecec")
            row.pack(fill="x", padx=12, pady=2)
            tk.Label(row, text=label, bg="#fdecec", fg="#7a0b0b",
                     width=16, anchor="w",
                     font=("Helvetica", 10, "bold")).pack(side="left")
            tk.Label(row, text=value, bg="#fdecec", fg="#333",
                     anchor="w", font=("Helvetica", 10)).pack(side="left")

        self.detail_lbl = tk.Label(self.root, text="", bg="#7a0b0b",
                                   fg="white", font=("Helvetica", 10))
        self.detail_lbl.pack(pady=8)

        if sys.platform.startswith("win"):
            self.autostart_var = tk.BooleanVar(value=self._autostart_enabled())
            tk.Checkbutton(self.root, text="Démarrer automatiquement avec "
                           "Windows", variable=self.autostart_var,
                           command=self._toggle_autostart, bg="#7a0b0b",
                           fg="white", selectcolor="#4a0606",
                           activebackground="#7a0b0b",
                           activeforeground="white").pack(pady=(0, 4))

        self.stop_btn = tk.Button(self.root, text="Déconnecter l'admin",
                                  command=self.service.kick, state="disabled",
                                  bg="white", fg="#7a0b0b",
                                  font=("Helvetica", 11, "bold"), relief="flat",
                                  padx=14, pady=6)
        self.stop_btn.pack(pady=(4, 2))
        tk.Button(self.root, text="Arrêter le partage et quitter",
                  command=self._on_close, bg="#4a0606", fg="white",
                  relief="flat", padx=10, pady=4).pack()

        tray_ok = self._build_tray() if tray else False
        if tray_ok:
            # Live in the tray: the window just hides when closed, and we
            # start hidden so nothing pops up on login.
            self.root.protocol("WM_DELETE_WINDOW", self._hide_window)
            self.root.withdraw()
        else:
            self.root.protocol("WM_DELETE_WINDOW", self._on_close)

        self.root.after(80, self._pump)

    # -- tray ------------------------------------------------------------

    def _build_tray(self):
        try:
            import pystray
            from PIL import Image, ImageDraw
        except Exception:
            return False

        icon_img = Image.new("RGB", (64, 64), "#7a0b0b")
        d = ImageDraw.Draw(icon_img)
        d.rectangle([12, 16, 52, 44], outline="white", width=4)
        d.rectangle([26, 46, 38, 52], fill="white")

        menu = pystray.Menu(
            pystray.MenuItem(lambda item: self._status_text, None,
                             enabled=False),
            pystray.MenuItem("Ouvrir la fenêtre", self._tray_open,
                             default=True),
            pystray.MenuItem("Démarrer avec Windows",
                             self._tray_toggle_autostart,
                             checked=lambda item: self._autostart_enabled()),
            pystray.MenuItem("Déconnecter l'admin",
                             lambda: self.service.kick()),
            pystray.MenuItem("Quitter le partage", self._tray_quit),
        )
        self.tray = pystray.Icon("remote-desktop", icon_img,
                                 "Remote Desktop — partage d'écran", menu)
        threading.Thread(target=self.tray.run, daemon=True).start()
        return True

    def _tray_open(self):
        self.root.after(0, self._show_window)

    def _tray_quit(self):
        self.root.after(0, self._on_close)

    def _tray_toggle_autostart(self):
        self.root.after(0, self._toggle_autostart)

    def _show_window(self):
        self.root.deiconify()
        self.root.lift()
        self.root.focus_force()

    def _hide_window(self):
        self.root.withdraw()

    # -- autostart -------------------------------------------------------

    def _autostart_enabled(self):
        if not sys.platform.startswith("win"):
            return False
        try:
            from common import autostart
            return autostart.is_enabled()
        except Exception:
            return False

    def _toggle_autostart(self):
        try:
            from common import autostart
            if autostart.is_enabled():
                autostart.disable()
            else:
                autostart.enable()
        except Exception as exc:
            self.detail_lbl.config(text="Démarrage auto: %s" % exc)
        if hasattr(self, "autostart_var"):
            self.autostart_var.set(self._autostart_enabled())
        if self.tray is not None:
            self.tray.update_menu()

    # -- event pump ------------------------------------------------------

    def _pump(self):
        try:
            while True:
                tag, value = self.ui.get_nowait()
                self._handle(tag, value)
        except queue.Empty:
            pass
        self.root.after(80, self._pump)

    def _handle(self, tag, value):
        if tag == "listening":
            self._set_status("En attente d'un admin", "#ffd7d7")
        elif tag == "connected":
            self.state_lbl.config(text="● CONTRÔLÉ À DISTANCE", fg="#fff2a8")
            self.detail_lbl.config(text="Admin connecté : %s" % value)
            self.stop_btn.config(state="normal")
            self._status_text = "Contrôlé par %s" % value
            self._notify("Contrôle à distance actif", str(value))
        elif tag == "disconnected":
            self._set_status("En attente d'un admin", "#ffd7d7")
            self.detail_lbl.config(text="")
            self.stop_btn.config(state="disabled")
        elif tag == "status":
            self.detail_lbl.config(text=value)
        elif tag == "approval":
            admin_name, ip, result, done = value
            self._show_window()
            ok = messagebox.askyesno(
                "Autoriser le contrôle à distance ?",
                "« %s » (%s) demande à contrôler cet ordinateur.\n\n"
                "Autoriser ?" % (admin_name, ip), parent=self.root)
            result["ok"] = ok
            done.set()
        elif tag == "fatal":
            messagebox.showerror("Erreur", value)
            self._on_close()

    def _set_status(self, text, color):
        self.state_lbl.config(text="● " + text, fg=color)
        self._status_text = text
        if self.tray is not None:
            self.tray.update_menu()

    def _notify(self, title, message):
        if self.tray is not None:
            try:
                self.tray.notify(message, title)
            except Exception:
                pass

    def _on_close(self):
        self.service.stop()
        if self.tray is not None:
            try:
                self.tray.stop()
            except Exception:
                pass
        self.root.destroy()

    def run(self):
        self.root.mainloop()


def main():
    ap = argparse.ArgumentParser(description="Remote Desktop — client")
    ap.add_argument("--name", default=socket.gethostname(),
                    help="nom affiché dans l'app admin")
    ap.add_argument("--port", type=int, default=protocol.CONTROL_PORT)
    ap.add_argument("--token", default=os.environ.get("RDESK_TOKEN",
                                                       protocol.DEFAULT_TOKEN),
                    help="clé partagée exigée de l'admin")
    ap.add_argument("--ask", action="store_true",
                    help="demander une confirmation à chaque connexion")
    ap.add_argument("--fps", type=int, default=FPS,
                    help="images par seconde (défaut %d)" % FPS)
    ap.add_argument("--quality", type=int, default=JPEG_QUALITY,
                    help="qualité JPEG 1-95 (défaut %d)" % JPEG_QUALITY)
    ap.add_argument("--max-width", type=int, default=MAX_WIDTH,
                    help="réduire au-delà de cette largeur (0 = jamais)")
    ap.add_argument("--tray", action="store_true",
                    help="démarrer réduit dans la zone de notification")
    ap.add_argument("--window", action="store_true",
                    help="forcer la grande fenêtre (pas de tray)")
    ap.add_argument("--install-autostart", action="store_true",
                    help="(Windows) lancer le client à l'ouverture de session")
    ap.add_argument("--remove-autostart", action="store_true",
                    help="(Windows) retirer le démarrage automatique")
    args = ap.parse_args()

    if args.install_autostart or args.remove_autostart:
        from common import autostart
        if args.install_autostart:
            autostart.enable()
            print("Démarrage automatique activé.")
        else:
            autostart.disable()
            print("Démarrage automatique désactivé.")
        return

    set_dpi_aware()

    ui_queue = queue.Queue()
    service = ScreenShareService(args.name, args.port, args.token,
                                 args.ask, ui_queue, fps=args.fps,
                                 quality=max(1, min(95, args.quality)),
                                 max_width=args.max_width)
    info = [
        ("Nom de ce PC", args.name),
        ("Adresse (IP/VPN)", protocol.local_ip()),
        ("Port", str(args.port)),
        ("Confirmation", "à chaque connexion" if args.ask else "automatique"),
    ]
    service.start()
    ClientGUI(service, info, tray=not args.window).run()


if __name__ == "__main__":
    main()
