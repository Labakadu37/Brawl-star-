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

FPS = 15
JPEG_QUALITY = 60
MAX_WIDTH = 1280            # downscale wide screens to save bandwidth


class ScreenShareService:
    """Owns the TCP listener and one active admin session at a time."""

    def __init__(self, name, port, token, require_approval, ui_queue):
        self.name = name
        self.port = port
        self.token = token
        self.require_approval = require_approval
        self.ui = ui_queue            # (tag, value) messages for the GUI
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
        if w > MAX_WIDTH:
            scale = MAX_WIDTH / float(w)
        return w, h, scale

    def _send_frames(self, conn, send_lock, scale, stop):
        import mss
        from PIL import Image

        interval = 1.0 / FPS
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
                img.save(buf, "JPEG", quality=JPEG_QUALITY)
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
            except Exception:
                # A single bad event must never kill the session.
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
    def __init__(self, service, info):
        self.service = service
        self.ui = service.ui
        self.root = tk.Tk()
        self.root.title("Remote Desktop — Client (cet écran est partageable)")
        self.root.geometry("460x320")
        self.root.configure(bg="#7a0b0b")
        self.root.protocol("WM_DELETE_WINDOW", self._on_close)

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

        self.stop_btn = tk.Button(self.root, text="Déconnecter l'admin",
                                  command=self.service.kick, state="disabled",
                                  bg="white", fg="#7a0b0b",
                                  font=("Helvetica", 11, "bold"), relief="flat",
                                  padx=14, pady=6)
        self.stop_btn.pack(pady=(4, 2))
        tk.Button(self.root, text="Arrêter le partage et quitter",
                  command=self._on_close, bg="#4a0606", fg="white",
                  relief="flat", padx=10, pady=4).pack()

        self.root.after(80, self._pump)

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
            self.state_lbl.config(text="● En attente d'un admin")
        elif tag == "connected":
            self.state_lbl.config(text="● CONTRÔLÉ À DISTANCE",
                                  fg="#fff2a8")
            self.detail_lbl.config(text="Admin connecté : %s" % value)
            self.stop_btn.config(state="normal")
        elif tag == "disconnected":
            self.state_lbl.config(text="● En attente d'un admin", fg="#ffd7d7")
            self.detail_lbl.config(text="")
            self.stop_btn.config(state="disabled")
        elif tag == "status":
            self.detail_lbl.config(text=value)
        elif tag == "approval":
            admin_name, ip, result, done = value
            ok = messagebox.askyesno(
                "Autoriser le contrôle à distance ?",
                "« %s » (%s) demande à contrôler cet ordinateur.\n\n"
                "Autoriser ?" % (admin_name, ip))
            result["ok"] = ok
            done.set()
        elif tag == "fatal":
            messagebox.showerror("Erreur", value)
            self._on_close()

    def _on_close(self):
        self.service.stop()
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
    args = ap.parse_args()

    ui_queue = queue.Queue()
    service = ScreenShareService(args.name, args.port, args.token,
                                 args.ask, ui_queue)
    info = [
        ("Nom de ce PC", args.name),
        ("Adresse IP", protocol.local_ip()),
        ("Port", str(args.port)),
        ("Confirmation", "à chaque connexion" if args.ask else "automatique"),
    ]
    service.start()
    ClientGUI(service, info).run()


if __name__ == "__main__":
    main()
