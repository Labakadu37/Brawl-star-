"""Discord Manager - base de l'application.

Fenetre avec un carre central ou l'utilisateur rentre son token Discord.
Le token est verifie aupres de l'API Discord (GET /users/@me) puis garde
uniquement en memoire : il n'est jamais ecrit sur le disque.
"""

import json
import threading
import tkinter as tk
import urllib.error
import urllib.request

APP_TITLE = "Discord Manager"
API_ME = "https://discord.com/api/v10/users/@me"

BG = "#1e1f22"
CARD = "#2b2d31"
INPUT = "#1e1f22"
TEXT = "#f2f3f5"
MUTED = "#b5bac1"
ACCENT = "#5865f2"
ACCENT_HOVER = "#4752c4"
ERROR = "#f23f43"
SUCCESS = "#23a55a"


def fetch_me(token):
    """Retourne le profil du compte associe au token, ou leve une erreur."""
    req = urllib.request.Request(
        API_ME,
        headers={"Authorization": token, "User-Agent": "DiscordManager/0.1"},
    )
    with urllib.request.urlopen(req, timeout=10) as resp:
        return json.loads(resp.read().decode("utf-8"))


class App(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(APP_TITLE)
        self.geometry("520x520")
        self.minsize(420, 420)
        self.configure(bg=BG)

        self.token = None
        self.user = None

        self.card = tk.Frame(self, bg=CARD, width=360, height=360)
        self.card.place(relx=0.5, rely=0.5, anchor="center")
        self.card.pack_propagate(False)

        self._build_login()

    def _clear_card(self):
        for child in self.card.winfo_children():
            child.destroy()

    def _build_login(self):
        self._clear_card()

        tk.Label(
            self.card, text="Rentre ton token", bg=CARD, fg=TEXT,
            font=("Segoe UI", 18, "bold"),
        ).pack(pady=(50, 6))
        tk.Label(
            self.card, text="Il reste uniquement en memoire.", bg=CARD,
            fg=MUTED, font=("Segoe UI", 10),
        ).pack(pady=(0, 24))

        self.show_var = tk.BooleanVar(value=False)
        self.entry = tk.Entry(
            self.card, show="*", bg=INPUT, fg=TEXT, insertbackground=TEXT,
            relief="flat", font=("Consolas", 11), width=32,
        )
        self.entry.pack(ipady=8, padx=24)
        self.entry.bind("<Return>", lambda _e: self._submit())
        self.entry.focus_set()

        tk.Checkbutton(
            self.card, text="Afficher", variable=self.show_var,
            command=self._toggle_show, bg=CARD, fg=MUTED,
            selectcolor=INPUT, activebackground=CARD,
            activeforeground=TEXT, font=("Segoe UI", 9),
        ).pack(anchor="w", padx=24, pady=(6, 0))

        self.button = tk.Button(
            self.card, text="Connexion", command=self._submit, bg=ACCENT,
            fg="white", activebackground=ACCENT_HOVER,
            activeforeground="white", relief="flat",
            font=("Segoe UI", 11, "bold"), cursor="hand2",
        )
        self.button.pack(fill="x", padx=24, pady=(16, 0), ipady=6)
        self.button.bind("<Enter>", lambda _e: self.button.config(bg=ACCENT_HOVER))
        self.button.bind("<Leave>", lambda _e: self.button.config(bg=ACCENT))

        self.status = tk.Label(
            self.card, text="", bg=CARD, fg=ERROR, font=("Segoe UI", 10),
            wraplength=300,
        )
        self.status.pack(pady=(14, 0))

    def _toggle_show(self):
        self.entry.config(show="" if self.show_var.get() else "*")

    def _submit(self):
        token = self.entry.get().strip().strip('"')
        if not token:
            self.status.config(text="Le token est vide.", fg=ERROR)
            return
        self.button.config(state="disabled", text="Verification...")
        self.status.config(text="")
        threading.Thread(target=self._check, args=(token,), daemon=True).start()

    def _check(self, token):
        try:
            user = fetch_me(token)
            self.after(0, self._on_success, token, user)
        except urllib.error.HTTPError as e:
            msg = "Token invalide." if e.code == 401 else f"Erreur HTTP {e.code}."
            self.after(0, self._on_error, msg)
        except Exception as e:  # reseau, timeout, etc.
            self.after(0, self._on_error, f"Connexion impossible : {e}")

    def _on_error(self, msg):
        self.button.config(state="normal", text="Connexion")
        self.status.config(text=msg, fg=ERROR)

    def _on_success(self, token, user):
        self.token = token
        self.user = user
        self._build_home()

    def _build_home(self):
        self._clear_card()
        name = self.user.get("global_name") or self.user.get("username", "?")
        tk.Label(
            self.card, text="Connecte", bg=CARD, fg=SUCCESS,
            font=("Segoe UI", 12, "bold"),
        ).pack(pady=(70, 6))
        tk.Label(
            self.card, text=name, bg=CARD, fg=TEXT,
            font=("Segoe UI", 20, "bold"),
        ).pack()
        tk.Label(
            self.card, text=f"@{self.user.get('username', '')}", bg=CARD,
            fg=MUTED, font=("Segoe UI", 10),
        ).pack(pady=(2, 30))
        tk.Button(
            self.card, text="Deconnexion", command=self._logout, bg=INPUT,
            fg=TEXT, activebackground=BG, activeforeground=TEXT,
            relief="flat", font=("Segoe UI", 10), cursor="hand2",
        ).pack(ipadx=12, ipady=4)

    def _logout(self):
        self.token = None
        self.user = None
        self._build_login()


if __name__ == "__main__":
    App().mainloop()
