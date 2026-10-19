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
import os
import queue
import socket
import sys
import threading
import tkinter as tk
from tkinter import messagebox, ttk

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))
from common import protocol

from PIL import Image, ImageTk


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

        manual = tk.Frame(f, bg="#7a0b0b")
        manual.pack(pady=(6, 0))
        tk.Label(manual, text="ou adresse manuelle :", bg="#7a0b0b",
                 fg="white").grid(row=0, column=0, padx=4)
        self.ip_entry = tk.Entry(manual, width=16)
        self.ip_entry.grid(row=0, column=1)
        self.ip_entry.insert(0, "")
        tk.Label(manual, text="port", bg="#7a0b0b", fg="white").grid(
            row=0, column=2, padx=(8, 2))
        self.port_entry = tk.Entry(manual, width=7)
        self.port_entry.insert(0, str(protocol.CONTROL_PORT))
        self.port_entry.grid(row=0, column=3)
        tk.Button(manual, text="Connexion", command=self._connect_manual,
                  bg="white", fg="#7a0b0b", relief="flat").grid(
            row=0, column=4, padx=8)

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
        self.scan()

    def scan(self):
        self.scan_btn.config(state="disabled")
        self.status.config(text="Recherche en cours…")

        def work():
            clients = protocol.discover_clients(timeout=1.5)
            self._events.put(("scan_done", clients))

        threading.Thread(target=work, daemon=True).start()

    def _render_clients(self, clients):
        self._clients = clients
        self.listbox.delete(0, tk.END)
        for c in clients:
            self.listbox.insert(tk.END, "  %s        %s:%d"
                                % (c["name"], c["ip"], c["port"]))
        self.scan_btn.config(state="normal")
        self.status.config(text="%d PC trouvé(s)." % len(clients)
                           if clients else "Aucun PC trouvé. Lance l'app "
                           "client sur l'autre PC, puis réessaie.")

    def _connect_selected(self):
        sel = self.listbox.curselection()
        if not sel:
            self.status.config(text="Sélectionne un PC dans la liste.")
            return
        c = self._clients[sel[0]]
        self._connect(c["ip"], c["port"])

    def _connect_manual(self):
        ip = self.ip_entry.get().strip()
        if not ip:
            self.status.config(text="Entre une adresse IP.")
            return
        try:
            port = int(self.port_entry.get().strip())
        except ValueError:
            self.status.config(text="Port invalide.")
            return
        self._connect(ip, port)

    def _connect(self, ip, port):
        self.token = self.token_entry.get()
        self.status.config(text="Connexion à %s:%d…" % (ip, port))

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
