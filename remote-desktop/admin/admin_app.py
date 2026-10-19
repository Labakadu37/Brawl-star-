"""Remote Desktop — ADMIN (the PC you control from).

Run this on your controlling PC. It scans the local network, lists the
clients that are running, and — once you pick one and it accepts the
shared token — shows that screen live and forwards your mouse and
keyboard to it.

    pip install customtkinter pillow
    python admin_app.py
"""

import argparse
import io
import json
import os
import queue
import socket
import sys
import threading
import tkinter as tk
from tkinter import messagebox

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from common import protocol

import customtkinter as ctk
from PIL import Image, ImageTk

# ---- Dark theme palette (red used only as an accent) ------------------
BG = "#0c0c0f"
CARD = "#17171c"
CARD_HOVER = "#20202a"
BORDER = "#2c2c34"
TEXT = "#eaeaef"
MUTED = "#8b8b95"
ACCENT = "#e0222f"
ACCENT_HOVER = "#b11b25"
GREEN = "#35c66b"

ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")


def _config_path():
    base = (os.environ.get("APPDATA")
            or os.path.join(os.path.expanduser("~"), ".config"))
    folder = os.path.join(base, "remote-desktop")
    os.makedirs(folder, exist_ok=True)
    return os.path.join(folder, "remote_hosts.json")


def load_remote_hosts():
    try:
        with open(_config_path(), "r", encoding="utf-8") as fh:
            data = json.load(fh)
        return [str(h) for h in data] if isinstance(data, list) else []
    except (OSError, ValueError):
        return []


def save_remote_hosts(hosts):
    try:
        with open(_config_path(), "w", encoding="utf-8") as fh:
            json.dump(hosts, fh)
    except OSError:
        pass


class AdminApp:
    def __init__(self, token, admin_name):
        self.token = token
        self.admin_name = admin_name
        self.root = ctk.CTk()
        self.root.title("Remote Desktop")
        self.root.geometry("1000x660")
        self.root.minsize(820, 560)
        self.root.configure(fg_color=BG)

        self.sock = None
        self.recv_thread = None
        self.stop_event = threading.Event()
        self.control_enabled = tk.BooleanVar(value=True)

        self._latest = None            # most recent PIL Image (frame)
        self._latest_lock = threading.Lock()
        self._photo = None             # keep a ref so Tk does not GC it
        self._draw_rect = (0, 0, 1, 1)  # ox, oy, dw, dh for coord mapping
        self._frame_size = (1, 1)
        self._send_lock = threading.Lock()
        self._events = queue.Queue()   # worker -> GUI notifications
        self.remote_hosts = load_remote_hosts()   # saved VPN/Tailscale peers
        self._card_widgets = []

        self._build_connect_view()
        self.root.after(80, self._pump_events)

    # ===================================================================
    # Connect view
    # ===================================================================

    def _build_connect_view(self):
        self.connect_frame = ctk.CTkFrame(self.root, fg_color=BG,
                                          corner_radius=0)
        self.connect_frame.pack(fill="both", expand=True, padx=24, pady=20)
        f = self.connect_frame

        # Header: logo badge + title
        header = ctk.CTkFrame(f, fg_color="transparent")
        header.pack(fill="x", pady=(4, 18))
        badge = ctk.CTkFrame(header, width=44, height=44, corner_radius=12,
                             fg_color=ACCENT)
        badge.pack(side="left")
        badge.pack_propagate(False)
        ctk.CTkLabel(badge, text="🖥", font=("Segoe UI Emoji", 20),
                     text_color="white").pack(expand=True)
        title_box = ctk.CTkFrame(header, fg_color="transparent")
        title_box.pack(side="left", padx=14)
        ctk.CTkLabel(title_box, text="Remote Desktop", text_color=TEXT,
                     font=("Segoe UI Semibold", 22)).pack(anchor="w")
        ctk.CTkLabel(title_box, text="Tes PC détectés sur le réseau",
                     text_color=MUTED, font=("Segoe UI", 13)).pack(anchor="w")

        self.scan_btn = ctk.CTkButton(header, text="⟳  Rechercher",
                                      width=130, height=38, corner_radius=10,
                                      fg_color=CARD, hover_color=CARD_HOVER,
                                      text_color=TEXT, command=self.scan)
        self.scan_btn.pack(side="right")

        # Scrollable list of device cards
        self.list_frame = ctk.CTkScrollableFrame(f, fg_color="transparent")
        self.list_frame.pack(fill="both", expand=True)

        # Bottom toolbar
        bottom = ctk.CTkFrame(f, fg_color="transparent")
        bottom.pack(fill="x", pady=(14, 0))
        ctk.CTkButton(bottom, text="＋  PC distant", width=130, height=38,
                      corner_radius=10, fg_color=CARD, hover_color=CARD_HOVER,
                      text_color=TEXT,
                      command=self._add_remote_host).pack(side="left")
        ctk.CTkLabel(bottom, text="Clé", text_color=MUTED,
                     font=("Segoe UI", 13)).pack(side="left", padx=(16, 6))
        self.token_entry = ctk.CTkEntry(bottom, width=180, height=38,
                                        corner_radius=10, show="•",
                                        fg_color=CARD, border_color=BORDER,
                                        text_color=TEXT)
        self.token_entry.insert(0, self.token)
        self.token_entry.pack(side="left")
        self.status = ctk.CTkLabel(bottom, text="", text_color=MUTED,
                                   font=("Segoe UI", 13))
        self.status.pack(side="right")

        self._clients = []
        self.on_connect_view = True
        self._scanning = False
        self.scan()
        self._auto_rescan()

    def _auto_rescan(self):
        """Keep the list fresh so PCs appear/disappear on their own."""
        if not getattr(self, "on_connect_view", False):
            return
        if not self._scanning:
            self.scan()
        self.root.after(3000, self._auto_rescan)

    def scan(self):
        if self._scanning:
            return
        self._scanning = True
        self.scan_btn.configure(state="disabled")
        if not self._clients:
            self.status.configure(text="Recherche…")

        hosts = list(self.remote_hosts)

        def work():
            clients = protocol.discover_clients(timeout=1.2, extra_hosts=hosts)
            self._events.put(("scan_done", clients))

        threading.Thread(target=work, daemon=True).start()

    def _add_remote_host(self):
        dialog = ctk.CTkInputDialog(
            title="Appairer un PC distant",
            text="Adresse du PC distant (nom ou IP Tailscale/VPN).\n"
                 "À saisir une seule fois :")
        host = dialog.get_input()
        if not host:
            return
        host = host.strip()
        if host and host not in self.remote_hosts:
            self.remote_hosts.append(host)
            save_remote_hosts(self.remote_hosts)
            self.status.configure(text="PC distant ajouté")
            self.scan()

    def _render_clients(self, clients):
        self._scanning = False
        if not getattr(self, "on_connect_view", False):
            return
        self._clients = clients
        for w in self._card_widgets:
            w.destroy()
        self._card_widgets = []

        if not clients:
            empty = ctk.CTkFrame(self.list_frame, fg_color="transparent")
            empty.pack(fill="both", expand=True, pady=40)
            self._card_widgets.append(empty)
            ctk.CTkLabel(empty, text="🖥", font=("Segoe UI Emoji", 40),
                         text_color=MUTED).pack()
            ctk.CTkLabel(empty, text="Aucun PC trouvé",
                         text_color=TEXT,
                         font=("Segoe UI Semibold", 15)).pack(pady=(8, 2))
            ctk.CTkLabel(empty, text="Lance l'app client sur l'autre PC "
                         "(même réseau).", text_color=MUTED,
                         font=("Segoe UI", 12)).pack()
        else:
            for c in clients:
                self._make_card(c)

        self.scan_btn.configure(state="normal")
        self.status.configure(text="%d PC disponible(s)" % len(clients)
                              if clients else "")

    def _make_card(self, client):
        card = ctk.CTkFrame(self.list_frame, fg_color=CARD, corner_radius=14,
                            border_width=1, border_color=BORDER, height=74)
        card.pack(fill="x", pady=6)
        card.pack_propagate(False)
        self._card_widgets.append(card)

        icon = ctk.CTkLabel(card, text="🖥", font=("Segoe UI Emoji", 24),
                            text_color=TEXT, width=56)
        icon.pack(side="left", padx=(16, 4))

        info = ctk.CTkFrame(card, fg_color="transparent")
        info.pack(side="left", fill="y", expand=True, pady=14)
        ctk.CTkLabel(info, text=client["name"], text_color=TEXT,
                     font=("Segoe UI Semibold", 16),
                     anchor="w").pack(anchor="w")
        dot = ctk.CTkFrame(info, fg_color="transparent")
        dot.pack(anchor="w", pady=(2, 0))
        ctk.CTkLabel(dot, text="●", text_color=GREEN,
                     font=("Segoe UI", 11)).pack(side="left")
        ctk.CTkLabel(dot, text=" En ligne", text_color=MUTED,
                     font=("Segoe UI", 12)).pack(side="left")

        ctk.CTkButton(card, text="Contrôler", width=118, height=40,
                      corner_radius=10, fg_color=ACCENT,
                      hover_color=ACCENT_HOVER, text_color="white",
                      font=("Segoe UI Semibold", 14),
                      command=lambda c=client: self._connect(
                          c["ip"], c["port"])).pack(side="right", padx=16)

        for w in (card, icon, info):
            w.bind("<Double-Button-1>",
                   lambda e, c=client: self._connect(c["ip"], c["port"]))

    def _connect(self, ip, port):
        self.token = self.token_entry.get()
        self.status.configure(text="Connexion…")

        def work():
            try:
                sock = socket.create_connection((ip, port), timeout=8)
                protocol.send_msg(sock, protocol.MSG_HELLO,
                                  {"token": self.token,
                                   "admin": self.admin_name})
                msg_type, payload = protocol.recv_msg(sock)
                if msg_type == protocol.MSG_DENIED:
                    reason = protocol.decode_json(payload).get("reason", "")
                    sock.close()
                    self._events.put(("connect_failed",
                                      "Refusé : %s" % reason))
                    return
                if msg_type != protocol.MSG_WELCOME:
                    sock.close()
                    self._events.put(("connect_failed", "Réponse inattendue"))
                    return
                info = protocol.decode_json(payload)
                self._events.put(("connected", (sock, info)))
            except OSError as exc:
                self._events.put(("connect_failed", "Échec : %s" % exc))

        threading.Thread(target=work, daemon=True).start()

    # ===================================================================
    # Viewer view
    # ===================================================================

    def _start_session(self, sock, info):
        self.sock = sock
        self.stop_event.clear()
        self.on_connect_view = False          # stop the auto-rescan loop
        self.connect_frame.destroy()
        self._frame_size = (info.get("width", 1), info.get("height", 1))

        self.view_frame = ctk.CTkFrame(self.root, fg_color="#000000",
                                       corner_radius=0)
        self.view_frame.pack(fill="both", expand=True)

        bar = ctk.CTkFrame(self.view_frame, fg_color="#141418",
                           corner_radius=0, height=48)
        bar.pack(fill="x")
        bar.pack_propagate(False)
        ctk.CTkLabel(bar, text="  🖥  %s   ·   %d×%d" % (info.get("name", "?"),
                     self._frame_size[0], self._frame_size[1]),
                     text_color=TEXT,
                     font=("Segoe UI Semibold", 13)).pack(side="left", padx=8)
        ctk.CTkButton(bar, text="Déconnecter", width=120, height=32,
                      corner_radius=8, fg_color=ACCENT,
                      hover_color=ACCENT_HOVER, text_color="white",
                      command=self._disconnect).pack(side="right", padx=10)
        self.control_switch = ctk.CTkSwitch(bar, text="Contrôle actif",
                                            variable=self.control_enabled,
                                            progress_color=ACCENT,
                                            text_color=TEXT)
        self.control_switch.pack(side="right", padx=12)

        self.canvas = tk.Canvas(self.view_frame, bg="black",
                                highlightthickness=0, cursor="tcross")
        self.canvas.pack(fill="both", expand=True)

        self.canvas.bind("<Motion>", self._on_move)
        # While a button is held Tk sends B?-Motion instead of Motion;
        # bind them too so dragging (windows, selections) works.
        self.canvas.bind("<B1-Motion>", self._on_move)
        self.canvas.bind("<B2-Motion>", self._on_move)
        self.canvas.bind("<B3-Motion>", self._on_move)
        self.canvas.bind("<ButtonPress>", self._on_button)
        self.canvas.bind("<ButtonRelease>", self._on_button)
        self.canvas.bind("<MouseWheel>", self._on_wheel)        # Win / mac
        self.canvas.bind("<Button-4>", self._on_wheel)          # Linux up
        self.canvas.bind("<Button-5>", self._on_wheel)          # Linux down
        self.root.bind("<KeyPress>", self._on_key)
        self.root.bind("<KeyRelease>", self._on_key)
        self.canvas.focus_set()

        self.recv_thread = threading.Thread(target=self._recv_loop, daemon=True)
        self.recv_thread.start()
        self.root.after(30, self._repaint)

    def _recv_loop(self):
        try:
            while not self.stop_event.is_set():
                msg_type, payload = protocol.recv_msg(self.sock)
                if msg_type == protocol.MSG_FRAME:
                    img = Image.open(io.BytesIO(payload))
                    img.load()
                    with self._latest_lock:
                        self._latest = img
                elif msg_type == protocol.MSG_BYE:
                    break
        except (OSError, ConnectionError):
            pass
        if not self.stop_event.is_set():
            self._events.put(("session_ended", None))

    def _repaint(self):
        if self.stop_event.is_set():
            return
        with self._latest_lock:
            img = self._latest
        if img is not None:
            cw = max(1, self.canvas.winfo_width())
            ch = max(1, self.canvas.winfo_height())
            iw, ih = img.size
            scale = min(cw / iw, ch / ih)
            dw, dh = max(1, int(iw * scale)), max(1, int(ih * scale))
            ox, oy = (cw - dw) // 2, (ch - dh) // 2
            self._draw_rect = (ox, oy, dw, dh)
            disp = img.resize((dw, dh), Image.BILINEAR)
            self._photo = ImageTk.PhotoImage(disp)
            self.canvas.delete("frame")
            self.canvas.create_image(ox, oy, anchor="nw", image=self._photo,
                                     tags="frame")
        self.root.after(30, self._repaint)

    # -- input forwarding ------------------------------------------------

    def _norm(self, ex, ey):
        ox, oy, dw, dh = self._draw_rect
        nx = (ex - ox) / dw
        ny = (ey - oy) / dh
        if nx < 0 or nx > 1 or ny < 0 or ny > 1:
            return None
        return min(max(nx, 0.0), 1.0), min(max(ny, 0.0), 1.0)

    def _send_input(self, event):
        if not self.control_enabled.get() or self.sock is None:
            return
        try:
            with self._send_lock:
                protocol.send_msg(self.sock, protocol.MSG_INPUT, event)
        except OSError:
            pass

    def _on_move(self, e):
        pos = self._norm(e.x, e.y)
        if pos:
            self._send_input({"kind": "move", "x": pos[0], "y": pos[1]})

    def _on_button(self, e):
        pos = self._norm(e.x, e.y)
        if not pos:
            return
        button = {1: "left", 2: "middle", 3: "right"}.get(e.num)
        if button is None:
            return
        pressed = e.type == tk.EventType.ButtonPress
        self._send_input({"kind": "click", "x": pos[0], "y": pos[1],
                          "button": button, "pressed": pressed})

    def _on_wheel(self, e):
        if getattr(e, "num", None) == 4:
            dy = 1
        elif getattr(e, "num", None) == 5:
            dy = -1
        else:
            dy = 1 if e.delta > 0 else -1
        self._send_input({"kind": "scroll", "dx": 0, "dy": dy})

    def _on_key(self, e):
        kind = "keydown" if e.type == tk.EventType.KeyPress else "keyup"
        self._send_input({"kind": kind, "keysym": e.keysym,
                          "char": e.char})

    # -- teardown --------------------------------------------------------

    def _disconnect(self):
        self.stop_event.set()
        if self.sock is not None:
            try:
                protocol.send_msg(self.sock, protocol.MSG_BYE)
            except OSError:
                pass
            try:
                self.sock.close()
            except OSError:
                pass
            self.sock = None
        if hasattr(self, "view_frame"):
            self.view_frame.destroy()
        self.root.unbind("<KeyPress>")
        self.root.unbind("<KeyRelease>")
        self._latest = None
        self._build_connect_view()

    # ===================================================================
    # Event pump (worker threads -> GUI thread)
    # ===================================================================

    def _pump_events(self):
        try:
            while True:
                tag, value = self._events.get_nowait()
                if tag == "scan_done":
                    self._render_clients(value)
                elif tag == "connect_failed":
                    self.status.configure(text=value)
                elif tag == "connected":
                    sock, info = value
                    self._start_session(sock, info)
                elif tag == "session_ended":
                    messagebox.showinfo("Session terminée",
                                        "La connexion avec le client a été "
                                        "fermée.")
                    self._disconnect()
        except queue.Empty:
            pass
        self.root.after(80, self._pump_events)

    def run(self):
        self.root.mainloop()


def main():
    ap = argparse.ArgumentParser(description="Remote Desktop — admin")
    ap.add_argument("--token", default=os.environ.get("RDESK_TOKEN",
                                                       protocol.DEFAULT_TOKEN))
    ap.add_argument("--name", default=socket.gethostname())
    args = ap.parse_args()
    AdminApp(args.token, args.name).run()


if __name__ == "__main__":
    main()
