"""Remote Desktop — ADMIN (the PC you control from).

Run this on your controlling PC. It scans the local network, lists the
clients that are running, and — once you pick one and it accepts the
shared token — shows that screen live and forwards your mouse and
keyboard to it.

    pip install pillow
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
from tkinter import messagebox, simpledialog

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from common import protocol

from PIL import Image, ImageTk


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
        self.root = tk.Tk()
        self.root.title("Remote Desktop — Admin")
        self.root.geometry("980x640")
        self.root.configure(bg="#7a0b0b")

        self.sock = None
        self.recv_thread = None
        self.stop_event = threading.Event()
        self.control_enabled = tk.BooleanVar(value=True)

        self._latest = None            # most recent PIL Image (frame)
        self._latest_lock = threading.Lock()
        self._photo = None             # keep a ref so Tk does not GC it
        self._draw_rect = (0, 0, 1, 1) # ox, oy, dw, dh for coord mapping
        self._frame_size = (1, 1)
        self._send_lock = threading.Lock()
        self._events = queue.Queue()   # worker -> GUI notifications
        self.remote_hosts = load_remote_hosts()   # saved VPN/Tailscale peers

        self._build_connect_view()
        self.root.after(80, self._pump_events)

    # ===================================================================
    # Connect view
    # ===================================================================

    def _build_connect_view(self):
        self.connect_frame = tk.Frame(self.root, bg="#7a0b0b")
        self.connect_frame.pack(fill="both", expand=True)
        f = self.connect_frame

        tk.Label(f, text="Remote Desktop — Admin", bg="#7a0b0b", fg="white",
                 font=("Helvetica", 22, "bold")).pack(pady=(26, 2))
        tk.Label(f, text="PC détectés sur le réseau local", bg="#7a0b0b",
                 fg="#ffd7d7", font=("Helvetica", 12)).pack(pady=(0, 12))

        list_card = tk.Frame(f, bg="white")
        list_card.pack(padx=40, fill="x")
        self.listbox = tk.Listbox(list_card, height=8, activestyle="none",
                                  font=("Helvetica", 12), bd=0,
                                  highlightthickness=0)
        self.listbox.pack(side="left", fill="both", expand=True, padx=2, pady=2)
        self.listbox.bind("<Double-Button-1>", lambda e: self._connect_selected())
        sb = tk.Scrollbar(list_card, command=self.listbox.yview)
        sb.pack(side="right", fill="y")
        self.listbox.config(yscrollcommand=sb.set)

        btns = tk.Frame(f, bg="#7a0b0b")
        btns.pack(pady=12)
        self.scan_btn = tk.Button(btns, text="🔍 Rechercher", command=self.scan,
                                  bg="white", fg="#7a0b0b", relief="flat",
                                  font=("Helvetica", 11, "bold"),
                                  padx=14, pady=6)
        self.scan_btn.grid(row=0, column=0, padx=6)
        tk.Button(btns, text="Se connecter", command=self._connect_selected,
                  bg="#ffd7d7", fg="#7a0b0b", relief="flat",
                  font=("Helvetica", 11, "bold"),
                  padx=14, pady=6).grid(row=0, column=1, padx=6)
        tk.Button(btns, text="➕ PC distant", command=self._add_remote_host,
                  bg="#4a0606", fg="white", relief="flat",
                  font=("Helvetica", 10), padx=10,
                  pady=6).grid(row=0, column=2, padx=6)

        tok = tk.Frame(f, bg="#7a0b0b")
        tok.pack(pady=14)
        tk.Label(tok, text="Clé partagée :", bg="#7a0b0b", fg="white").pack(
            side="left")
        self.token_entry = tk.Entry(tok, width=24, show="•")
        self.token_entry.insert(0, self.token)
        self.token_entry.pack(side="left", padx=6)

        self.status = tk.Label(f, text="", bg="#7a0b0b", fg="#ffd7d7")
        self.status.pack()
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
        self.scan_btn.config(state="disabled")
        if not self._clients:
            self.status.config(text="Recherche en cours…")

        hosts = list(self.remote_hosts)

        def work():
            clients = protocol.discover_clients(timeout=1.2, extra_hosts=hosts)
            self._events.put(("scan_done", clients))

        threading.Thread(target=work, daemon=True).start()

    def _add_remote_host(self):
        host = simpledialog.askstring(
            "Appairer un PC distant",
            "Adresse du PC distant (nom ou IP Tailscale/VPN).\n"
            "À saisir une seule fois — il apparaîtra ensuite tout seul :",
            parent=self.root)
        if not host:
            return
        host = host.strip()
        if host and host not in self.remote_hosts:
            self.remote_hosts.append(host)
            save_remote_hosts(self.remote_hosts)
            self.status.config(text="PC distant ajouté : %s" % host)
            self.scan()

    def _render_clients(self, clients):
        self._scanning = False
        if not getattr(self, "on_connect_view", False):
            return
        # Preserve the current selection across auto-refreshes.
        sel = self.listbox.curselection()
        selected_name = self._clients[sel[0]]["name"] if sel else None
        self._clients = clients
        self.listbox.delete(0, tk.END)
        for c in clients:
            self.listbox.insert(tk.END, "   💻   %s" % c["name"])
        if selected_name:
            for i, c in enumerate(clients):
                if c["name"] == selected_name:
                    self.listbox.selection_set(i)
                    break
        self.scan_btn.config(state="normal")
        self.status.config(text="%d PC disponible(s) — double-clique pour "
                           "contrôler." % len(clients) if clients
                           else "Aucun PC trouvé. Lance l'app client sur "
                           "l'autre PC (même réseau Wi-Fi).")

    def _connect_selected(self):
        sel = self.listbox.curselection()
        if not sel:
            self.status.config(text="Sélectionne un PC dans la liste.")
            return
        c = self._clients[sel[0]]
        self._connect(c["ip"], c["port"])

    def _connect(self, ip, port):
        self.token = self.token_entry.get()
        self.status.config(text="Connexion…")

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

        self.view_frame = tk.Frame(self.root, bg="black")
        self.view_frame.pack(fill="both", expand=True)

        bar = tk.Frame(self.view_frame, bg="#2b2b2b")
        bar.pack(fill="x")
        tk.Label(bar, text="  %s  —  %dx%d" % (info.get("name", "?"),
                 self._frame_size[0], self._frame_size[1]),
                 bg="#2b2b2b", fg="white",
                 font=("Helvetica", 11, "bold")).pack(side="left", pady=4)
        tk.Checkbutton(bar, text="Contrôle actif", variable=self.control_enabled,
                       bg="#2b2b2b", fg="white", selectcolor="#2b2b2b",
                       activebackground="#2b2b2b", activeforeground="white").pack(
            side="left", padx=12)
        tk.Button(bar, text="Déconnecter", command=self._disconnect,
                  bg="#7a0b0b", fg="white", relief="flat",
                  padx=10).pack(side="right", padx=6, pady=3)

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
                    self.status.config(text=value)
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
