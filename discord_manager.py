import customtkinter as ctk
import requests
import threading
import io
from urllib.request import urlopen
from PIL import Image, ImageDraw, ImageFilter

ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")

DISCORD_API = "https://discord.com/api/v10"

# --- Nitro Black/Orange Theme ---
BG = "#0a0a0a"
BG2 = "#111111"
CARD = "#1a1a1a"
CARD2 = "#141414"
CARD_BORDER = "#2a2a2a"
ORANGE = "#FF6B00"
ORANGE_DARK = "#CC5500"
ORANGE_LIGHT = "#FF8C33"
ORANGE_GLOW = "#FF6B0022"
PURPLE = "#9B59B6"
PURPLE_DARK = "#7D3C98"
GREEN = "#2ECC71"
YELLOW = "#F39C12"
RED = "#E74C3C"
RED_DARK = "#C0392B"
GRAY = "#666666"
GRAY_LIGHT = "#888888"
TEXT = "#FFFFFF"
TEXT_DIM = "#AAAAAA"
TEXT_DARK = "#777777"
INPUT_BG = "#0d0d0d"
INPUT_BORDER = "#333333"


def fetch_user(token):
    h = {"Authorization": token, "Content-Type": "application/json"}
    r = requests.get(f"{DISCORD_API}/users/@me", headers=h, timeout=10)
    r.raise_for_status()
    return r.json()


def fetch_settings(token):
    h = {"Authorization": token, "Content-Type": "application/json"}
    try:
        r = requests.get(f"{DISCORD_API}/users/@me/settings", headers=h, timeout=10)
        r.raise_for_status()
        return r.json()
    except Exception:
        return {}


def fetch_avatar(user_data):
    av = user_data.get("avatar")
    uid = user_data["id"]
    if av:
        ext = "gif" if av.startswith("a_") else "png"
        url = f"https://cdn.discordapp.com/avatars/{uid}/{av}.{ext}?size=256"
    else:
        idx = (int(uid) >> 22) % 6
        url = f"https://cdn.discordapp.com/embed/avatars/{idx}.png"
    data = urlopen(url).read()
    return Image.open(io.BytesIO(data)).resize((120, 120), Image.LANCZOS)


def circle_crop(img, size=120):
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size, size), fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(img.convert("RGBA"), (0, 0), mask)
    return out


def api_set_status(token, status):
    h = {"Authorization": token, "Content-Type": "application/json"}
    r = requests.patch(f"{DISCORD_API}/users/@me/settings", headers=h, json={"status": status}, timeout=10)
    r.raise_for_status()


def api_custom_status(token, text=None, emoji=None):
    h = {"Authorization": token, "Content-Type": "application/json"}
    cs = {}
    if text:
        cs["text"] = text
    if emoji:
        cs["emoji_name"] = emoji
    r = requests.patch(f"{DISCORD_API}/users/@me/settings", headers=h, json={"custom_status": cs or None}, timeout=10)
    r.raise_for_status()


def api_change_password(token, old_pw, new_pw):
    h = {"Authorization": token, "Content-Type": "application/json"}
    r = requests.patch(f"{DISCORD_API}/users/@me", headers=h, json={"password": old_pw, "new_password": new_pw}, timeout=10)
    r.raise_for_status()
    return r.json()


class GlowButton(ctk.CTkButton):
    pass


class LoginFrame(ctk.CTkFrame):
    def __init__(self, master, on_login):
        super().__init__(master, fg_color=BG)
        self.on_login = on_login
        self.grid_rowconfigure((0, 1, 2, 3, 4, 5), weight=1)
        self.grid_columnconfigure(0, weight=1)

        # Top accent line
        accent_line = ctk.CTkFrame(self, height=3, fg_color=ORANGE, corner_radius=0)
        accent_line.grid(row=0, column=0, sticky="new", pady=0)

        # Title
        ctk.CTkLabel(
            self, text="DISCORD", font=ctk.CTkFont(size=42, weight="bold"), text_color=ORANGE
        ).grid(row=1, column=0, pady=(0, 0), sticky="s")

        ctk.CTkLabel(
            self, text="MANAGER", font=ctk.CTkFont(size=42, weight="bold"), text_color=TEXT
        ).grid(row=2, column=0, pady=(0, 0), sticky="n")

        ctk.CTkLabel(
            self, text="━━━━━━━━━━━━━━━━━━━", font=ctk.CTkFont(size=14), text_color=ORANGE_DARK
        ).grid(row=2, column=0, pady=(45, 0), sticky="n")

        # Card
        card = ctk.CTkFrame(self, fg_color=CARD, corner_radius=20, border_width=1, border_color=CARD_BORDER)
        card.grid(row=3, column=0, padx=60)
        card.grid_columnconfigure(0, weight=1)

        inner = ctk.CTkFrame(card, fg_color="transparent")
        inner.pack(padx=40, pady=35)

        ctk.CTkLabel(
            inner, text="Connexion", font=ctk.CTkFont(size=22, weight="bold"), text_color=TEXT
        ).pack(pady=(0, 5))

        ctk.CTkLabel(
            inner, text="Entre ton token Discord pour commencer", font=ctk.CTkFont(size=12), text_color=TEXT_DIM
        ).pack(pady=(0, 20))

        self.token_entry = ctk.CTkEntry(
            inner, width=380, height=50, placeholder_text="Token Discord...",
            show="*", font=ctk.CTkFont(size=14), corner_radius=12,
            fg_color=INPUT_BG, border_color=INPUT_BORDER, border_width=1, text_color=TEXT,
        )
        self.token_entry.pack(pady=(0, 8))

        self.show_var = ctk.BooleanVar(value=False)
        ctk.CTkCheckBox(
            inner, text="Afficher le token", variable=self.show_var,
            command=lambda: self.token_entry.configure(show="" if self.show_var.get() else "*"),
            font=ctk.CTkFont(size=11), text_color=TEXT_DARK,
            fg_color=ORANGE, hover_color=ORANGE_DARK, border_color=GRAY,
        ).pack(pady=(0, 18))

        self.login_btn = ctk.CTkButton(
            inner, text="SE CONNECTER", width=380, height=48,
            font=ctk.CTkFont(size=15, weight="bold"),
            fg_color=ORANGE, hover_color=ORANGE_DARK, corner_radius=12,
            command=self.do_login,
        )
        self.login_btn.pack()

        self.status = ctk.CTkLabel(self, text="", font=ctk.CTkFont(size=13), text_color=RED)
        self.status.grid(row=4, column=0, pady=(10, 0))

        # Bottom
        ctk.CTkLabel(
            self, text="v1.0  ●  by JZS", font=ctk.CTkFont(size=11), text_color=TEXT_DARK
        ).grid(row=5, column=0, pady=(0, 15), sticky="s")

        self.token_entry.bind("<Return>", lambda e: self.do_login())

    def do_login(self):
        token = self.token_entry.get().strip()
        if not token:
            self.status.configure(text="⚠ Entre un token !", text_color=ORANGE)
            return
        self.login_btn.configure(state="disabled", text="Connexion...")
        self.status.configure(text="Verification en cours...", text_color=YELLOW)

        def task():
            try:
                user = fetch_user(token)
                settings = fetch_settings(token)
                avatar = None
                try:
                    avatar = circle_crop(fetch_avatar(user))
                except Exception:
                    pass
                self.after(0, lambda: self.on_login(token, user, settings, avatar))
            except requests.exceptions.HTTPError as e:
                msg = "Token invalide !" if e.response and e.response.status_code == 401 else f"Erreur: {e}"
                self.after(0, lambda: (
                    self.status.configure(text=msg, text_color=RED),
                    self.login_btn.configure(state="normal", text="SE CONNECTER"),
                ))
            except Exception as e:
                self.after(0, lambda: (
                    self.status.configure(text=f"Erreur: {e}", text_color=RED),
                    self.login_btn.configure(state="normal", text="SE CONNECTER"),
                ))

        threading.Thread(target=task, daemon=True).start()


class MainFrame(ctk.CTkFrame):
    def __init__(self, master, token, user_data, settings, avatar_img, on_logout):
        super().__init__(master, fg_color=BG)
        self.token = token
        self.user = user_data
        self.settings = settings
        self.on_logout = on_logout

        # Top bar
        topbar = ctk.CTkFrame(self, fg_color=CARD, height=50, corner_radius=0)
        topbar.pack(fill="x", side="top")
        topbar.pack_propagate(False)

        ctk.CTkLabel(
            topbar, text="DISCORD", font=ctk.CTkFont(size=18, weight="bold"), text_color=ORANGE
        ).pack(side="left", padx=(20, 3), pady=10)
        ctk.CTkLabel(
            topbar, text="MANAGER", font=ctk.CTkFont(size=18, weight="bold"), text_color=TEXT
        ).pack(side="left", pady=10)

        ctk.CTkButton(
            topbar, text="Deconnexion", width=110, height=32,
            fg_color=RED, hover_color=RED_DARK, corner_radius=8,
            font=ctk.CTkFont(size=12, weight="bold"), command=on_logout,
        ).pack(side="right", padx=15, pady=10)

        # Orange accent under topbar
        ctk.CTkFrame(self, height=2, fg_color=ORANGE, corner_radius=0).pack(fill="x")

        # Main content
        content = ctk.CTkFrame(self, fg_color=BG)
        content.pack(fill="both", expand=True, padx=15, pady=15)
        content.grid_columnconfigure(0, weight=2)
        content.grid_columnconfigure(1, weight=5)
        content.grid_rowconfigure(0, weight=1)

        # ======= LEFT: Profile =======
        left = ctk.CTkFrame(content, fg_color=CARD, corner_radius=16, border_width=1, border_color=CARD_BORDER)
        left.grid(row=0, column=0, padx=(0, 8), sticky="nsew")

        # Orange top on profile card
        orange_top = ctk.CTkFrame(left, height=60, fg_color=ORANGE, corner_radius=0)
        orange_top.pack(fill="x")
        # Rounded top corners hack
        corner_fix = ctk.CTkFrame(left, height=16, fg_color=CARD, corner_radius=16)
        corner_fix.place(relx=0, rely=0, y=50, relwidth=1, height=20)

        profile_inner = ctk.CTkFrame(left, fg_color="transparent")
        profile_inner.pack(fill="both", expand=True, padx=15, pady=(20, 15))

        # Avatar
        if avatar_img:
            # Add orange ring around avatar
            ring_size = 130
            ring = Image.new("RGBA", (ring_size, ring_size), (0, 0, 0, 0))
            d = ImageDraw.Draw(ring)
            d.ellipse((0, 0, ring_size, ring_size), fill=(255, 107, 0, 255))
            d.ellipse((4, 4, ring_size - 4, ring_size - 4), fill=(0, 0, 0, 0))
            avatar_with_ring = Image.new("RGBA", (ring_size, ring_size), (0, 0, 0, 0))
            avatar_with_ring.paste(ring, (0, 0), ring)
            avatar_with_ring.paste(avatar_img, (5, 5), avatar_img)
            ctk_img = ctk.CTkImage(light_image=avatar_with_ring, dark_image=avatar_with_ring, size=(130, 130))
            ctk.CTkLabel(profile_inner, image=ctk_img, text="").pack(pady=(0, 10))
        else:
            ctk.CTkLabel(profile_inner, text="👤", font=ctk.CTkFont(size=50), text_color=ORANGE).pack(pady=(0, 10))

        display = user_data.get("global_name") or user_data.get("username", "???")
        ctk.CTkLabel(
            profile_inner, text=display, font=ctk.CTkFont(size=20, weight="bold"), text_color=TEXT
        ).pack(pady=(0, 2))

        uname = user_data.get("username", "???")
        disc = user_data.get("discriminator", "0")
        tag = f"{uname}#{disc}" if disc != "0" else f"@{uname}"
        ctk.CTkLabel(profile_inner, text=tag, font=ctk.CTkFont(size=13), text_color=TEXT_DIM).pack(pady=(0, 4))

        # Current status indicator
        cur_status = settings.get("status", "online")
        status_colors = {"online": GREEN, "idle": YELLOW, "dnd": RED, "invisible": GRAY, "streaming": PURPLE}
        status_names = {"online": "En ligne", "idle": "Inactif", "dnd": "Ne pas deranger", "invisible": "Invisible"}
        sc = status_colors.get(cur_status, GREEN)
        sn = status_names.get(cur_status, cur_status)
        status_frame = ctk.CTkFrame(profile_inner, fg_color=sc, corner_radius=10, height=28)
        status_frame.pack(pady=(5, 8))
        ctk.CTkLabel(
            status_frame, text=f"  ● {sn}  ", font=ctk.CTkFont(size=11, weight="bold"),
            text_color="#000000" if sc in (GREEN, YELLOW) else TEXT
        ).pack(padx=8, pady=4)

        # Separator
        ctk.CTkFrame(profile_inner, height=1, fg_color=CARD_BORDER).pack(fill="x", pady=8)

        # Info
        info = ctk.CTkFrame(profile_inner, fg_color="transparent")
        info.pack(fill="x", padx=5)

        def info_row(parent, label, value, row, color=TEXT_DIM):
            ctk.CTkLabel(parent, text=label, font=ctk.CTkFont(size=11, weight="bold"), text_color=ORANGE, anchor="w").grid(row=row, column=0, sticky="w", pady=2)
            ctk.CTkLabel(parent, text=value, font=ctk.CTkFont(size=11), text_color=color, anchor="w", wraplength=170).grid(row=row, column=1, sticky="w", padx=(8, 0), pady=2)

        info.grid_columnconfigure(1, weight=1)
        info_row(info, "ID", user_data.get("id", "???"), 0)

        email = user_data.get("email")
        if email:
            info_row(info, "Email", email, 1)

        phone = user_data.get("phone")
        if phone:
            info_row(info, "Tel", phone, 2)

        # Nitro
        premium = user_data.get("premium_type", 0)
        nitro_map = {0: "Aucun", 1: "Classic", 2: "Nitro", 3: "Basic"}
        nitro_text = nitro_map.get(premium, "?")
        info_row(info, "Nitro", nitro_text, 3, ORANGE if premium else TEXT_DARK)

        # Badges
        flags = user_data.get("public_flags", 0)
        badge_map = {
            1: "Staff", 2: "Partner", 4: "HypeSquad", 8: "Bug Hunter",
            64: "Bravery", 128: "Brilliance", 256: "Balance",
            512: "Early Supporter", 16384: "Bug Hunter 2",
            131072: "Dev Bot", 4194304: "Active Dev",
        }
        badges = [n for v, n in badge_map.items() if flags & v]
        info_row(info, "Badges", ", ".join(badges) if badges else "Aucun", 4)

        # ======= RIGHT: Management =======
        right_scroll = ctk.CTkScrollableFrame(content, fg_color=BG, corner_radius=0, scrollbar_button_color=CARD_BORDER, scrollbar_button_hover_color=ORANGE_DARK)
        right_scroll.grid(row=0, column=1, padx=(8, 0), sticky="nsew")
        right_scroll.grid_columnconfigure(0, weight=1)

        self.feedback = ctk.CTkLabel(right_scroll, text="", font=ctk.CTkFont(size=13), text_color=GREEN)

        # --- STATUS SECTION ---
        self._section_title(right_scroll, "Statut", 0)

        status_card = ctk.CTkFrame(right_scroll, fg_color=CARD, corner_radius=14, border_width=1, border_color=CARD_BORDER)
        status_card.grid(row=1, column=0, sticky="ew", pady=(0, 12))

        status_inner = ctk.CTkFrame(status_card, fg_color="transparent")
        status_inner.pack(padx=20, pady=18)

        ctk.CTkLabel(
            status_inner, text="Change ton statut de presence",
            font=ctk.CTkFont(size=12), text_color=TEXT_DIM
        ).pack(pady=(0, 12))

        btn_frame = ctk.CTkFrame(status_inner, fg_color="transparent")
        btn_frame.pack()

        statuses = [
            ("En ligne", "online", GREEN, "#000"),
            ("Inactif", "idle", YELLOW, "#000"),
            ("Ne pas deranger", "dnd", RED, TEXT),
            ("Invisible", "invisible", "#555555", TEXT),
            ("Streaming", "streaming", PURPLE, TEXT),
        ]

        for i, (label, val, bg, fg) in enumerate(statuses):
            ctk.CTkButton(
                btn_frame, text=f"● {label}", width=130, height=40,
                fg_color=bg, hover_color=ORANGE_DARK,
                text_color=fg, font=ctk.CTkFont(size=12, weight="bold"),
                corner_radius=10, command=lambda v=val: self.set_status(v),
            ).grid(row=0 if i < 3 else 1, column=i % 3, padx=5, pady=4)

        # Streaming details (shown for streaming)
        self.stream_frame = ctk.CTkFrame(status_inner, fg_color=CARD2, corner_radius=10)

        ctk.CTkLabel(
            self.stream_frame, text="Details du stream", font=ctk.CTkFont(size=12, weight="bold"), text_color=PURPLE
        ).pack(pady=(10, 5))

        self.stream_name = ctk.CTkEntry(
            self.stream_frame, placeholder_text="Nom du jeu/stream...",
            height=38, font=ctk.CTkFont(size=12), fg_color=INPUT_BG,
            border_color=PURPLE, border_width=1, text_color=TEXT, corner_radius=8, width=350
        )
        self.stream_name.pack(padx=15, pady=(0, 5))

        self.stream_url = ctk.CTkEntry(
            self.stream_frame, placeholder_text="URL Twitch (https://twitch.tv/...)...",
            height=38, font=ctk.CTkFont(size=12), fg_color=INPUT_BG,
            border_color=PURPLE, border_width=1, text_color=TEXT, corner_radius=8, width=350
        )
        self.stream_url.pack(padx=15, pady=(0, 10))

        # --- CUSTOM STATUS ---
        self._section_title(right_scroll, "Statut Personnalise", 2)

        custom_card = ctk.CTkFrame(right_scroll, fg_color=CARD, corner_radius=14, border_width=1, border_color=CARD_BORDER)
        custom_card.grid(row=3, column=0, sticky="ew", pady=(0, 12))
        custom_inner = ctk.CTkFrame(custom_card, fg_color="transparent")
        custom_inner.pack(padx=20, pady=18, fill="x")
        custom_inner.grid_columnconfigure(0, weight=1)

        self.custom_emoji = ctk.CTkEntry(
            custom_inner, placeholder_text="Emoji (ex: 🎮)...",
            height=40, width=100, font=ctk.CTkFont(size=14), fg_color=INPUT_BG,
            border_color=INPUT_BORDER, border_width=1, text_color=TEXT, corner_radius=10,
        )
        self.custom_emoji.grid(row=0, column=0, sticky="w", padx=(0, 8), pady=(0, 8))

        self.custom_text = ctk.CTkEntry(
            custom_inner, placeholder_text="Ton texte de statut...",
            height=40, font=ctk.CTkFont(size=13), fg_color=INPUT_BG,
            border_color=INPUT_BORDER, border_width=1, text_color=TEXT, corner_radius=10,
        )
        self.custom_text.grid(row=0, column=1, sticky="ew", pady=(0, 8))

        btn_row = ctk.CTkFrame(custom_inner, fg_color="transparent")
        btn_row.grid(row=1, column=0, columnspan=2, sticky="ew")

        ctk.CTkButton(
            btn_row, text="Appliquer", width=150, height=40,
            fg_color=ORANGE, hover_color=ORANGE_DARK,
            font=ctk.CTkFont(size=13, weight="bold"), corner_radius=10,
            command=self.apply_custom,
        ).pack(side="left", padx=(0, 8))

        ctk.CTkButton(
            btn_row, text="Effacer", width=120, height=40,
            fg_color="#333333", hover_color="#444444",
            font=ctk.CTkFont(size=13, weight="bold"), corner_radius=10,
            command=self.clear_custom,
        ).pack(side="left")

        # --- CHANGE PASSWORD ---
        self._section_title(right_scroll, "Changer le Mot de Passe", 4)

        pw_card = ctk.CTkFrame(right_scroll, fg_color=CARD, corner_radius=14, border_width=1, border_color=CARD_BORDER)
        pw_card.grid(row=5, column=0, sticky="ew", pady=(0, 12))
        pw_inner = ctk.CTkFrame(pw_card, fg_color="transparent")
        pw_inner.pack(padx=20, pady=18, fill="x")

        ctk.CTkLabel(
            pw_inner, text="⚠ Necessite ton mot de passe actuel",
            font=ctk.CTkFont(size=11), text_color=ORANGE
        ).pack(anchor="w", pady=(0, 10))

        pw_fields = ctk.CTkFrame(pw_inner, fg_color="transparent")
        pw_fields.pack(fill="x")
        pw_fields.grid_columnconfigure(0, weight=1)
        pw_fields.grid_columnconfigure(1, weight=1)

        ctk.CTkLabel(pw_fields, text="Mot de passe actuel", font=ctk.CTkFont(size=11, weight="bold"), text_color=TEXT_DIM).grid(row=0, column=0, sticky="w", pady=(0, 4))
        self.old_pw = ctk.CTkEntry(
            pw_fields, placeholder_text="Mot de passe actuel...", show="*",
            height=42, font=ctk.CTkFont(size=13), fg_color=INPUT_BG,
            border_color=INPUT_BORDER, border_width=1, text_color=TEXT, corner_radius=10,
        )
        self.old_pw.grid(row=1, column=0, sticky="ew", padx=(0, 8), pady=(0, 10))

        ctk.CTkLabel(pw_fields, text="Nouveau mot de passe", font=ctk.CTkFont(size=11, weight="bold"), text_color=TEXT_DIM).grid(row=0, column=1, sticky="w", pady=(0, 4))
        self.new_pw = ctk.CTkEntry(
            pw_fields, placeholder_text="Nouveau mot de passe...", show="*",
            height=42, font=ctk.CTkFont(size=13), fg_color=INPUT_BG,
            border_color=INPUT_BORDER, border_width=1, text_color=TEXT, corner_radius=10,
        )
        self.new_pw.grid(row=1, column=1, sticky="ew", pady=(0, 10))

        ctk.CTkLabel(pw_fields, text="Confirmer", font=ctk.CTkFont(size=11, weight="bold"), text_color=TEXT_DIM).grid(row=2, column=0, sticky="w", pady=(0, 4))
        self.confirm_pw = ctk.CTkEntry(
            pw_fields, placeholder_text="Confirmer le nouveau...", show="*",
            height=42, font=ctk.CTkFont(size=13), fg_color=INPUT_BG,
            border_color=INPUT_BORDER, border_width=1, text_color=TEXT, corner_radius=10,
        )
        self.confirm_pw.grid(row=3, column=0, sticky="ew", padx=(0, 8), pady=(0, 10))

        ctk.CTkButton(
            pw_inner, text="Changer le mot de passe", width=250, height=42,
            fg_color=RED, hover_color=RED_DARK,
            font=ctk.CTkFont(size=13, weight="bold"), corner_radius=10,
            command=self.change_password,
        ).pack(pady=(5, 0))

        # --- FEEDBACK ---
        self.feedback.grid(row=6, column=0, pady=(8, 5))

    def _section_title(self, parent, text, row):
        frame = ctk.CTkFrame(parent, fg_color="transparent")
        frame.grid(row=row, column=0, sticky="ew", pady=(8, 6))
        ctk.CTkFrame(frame, height=1, fg_color=ORANGE_DARK, width=30).pack(side="left", padx=(0, 10), pady=1)
        ctk.CTkLabel(
            frame, text=text, font=ctk.CTkFont(size=16, weight="bold"), text_color=ORANGE
        ).pack(side="left")
        ctk.CTkFrame(frame, height=1, fg_color=ORANGE_DARK).pack(side="left", fill="x", expand=True, padx=(10, 0), pady=1)

    def show_feedback(self, text, color=GREEN):
        self.feedback.configure(text=text, text_color=color)
        self.after(4000, lambda: self.feedback.configure(text=""))

    def set_status(self, status):
        if status == "streaming":
            self.stream_frame.pack(pady=(12, 0), fill="x")
            name = self.stream_name.get().strip() or "Stream"
            url = self.stream_url.get().strip()

            def task():
                try:
                    api_set_status(self.token, "online")
                    api_custom_status(self.token, text=f"🔴 En live: {name}")
                    self.after(0, lambda: self.show_feedback(f"Statut Streaming active: {name}", PURPLE))
                except Exception as e:
                    self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

            threading.Thread(target=task, daemon=True).start()
            return

        self.stream_frame.pack_forget()

        def task():
            try:
                api_set_status(self.token, status)
                names = {"online": "En ligne", "idle": "Inactif", "dnd": "Ne pas deranger", "invisible": "Invisible"}
                self.after(0, lambda: self.show_feedback(f"Statut: {names.get(status, status)}"))
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()

    def apply_custom(self):
        text = self.custom_text.get().strip()
        emoji = self.custom_emoji.get().strip()
        if not text and not emoji:
            self.show_feedback("Ecris un texte ou un emoji !", ORANGE)
            return

        def task():
            try:
                api_custom_status(self.token, text=text or None, emoji=emoji or None)
                self.after(0, lambda: self.show_feedback("Statut personnalise applique !"))
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()

    def clear_custom(self):
        def task():
            try:
                api_custom_status(self.token)
                self.after(0, lambda: self.show_feedback("Statut personnalise efface !"))
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()

    def change_password(self):
        old = self.old_pw.get()
        new = self.new_pw.get()
        confirm = self.confirm_pw.get()

        if not old:
            self.show_feedback("Entre ton mot de passe actuel !", ORANGE)
            return
        if not new:
            self.show_feedback("Entre un nouveau mot de passe !", ORANGE)
            return
        if len(new) < 8:
            self.show_feedback("Le mot de passe doit faire au moins 8 caracteres !", RED)
            return
        if new != confirm:
            self.show_feedback("Les mots de passe ne correspondent pas !", RED)
            return

        def task():
            try:
                result = api_change_password(self.token, old, new)
                new_token = result.get("token")
                if new_token:
                    self.token = new_token
                self.after(0, lambda: self.show_feedback("Mot de passe change avec succes !", GREEN))
                self.after(0, lambda: (
                    self.old_pw.delete(0, "end"),
                    self.new_pw.delete(0, "end"),
                    self.confirm_pw.delete(0, "end"),
                ))
            except requests.exceptions.HTTPError as e:
                if e.response is not None and e.response.status_code == 401:
                    msg = "Mot de passe actuel incorrect !"
                elif e.response is not None and e.response.status_code == 400:
                    msg = "Mot de passe invalide (min 8 car.)"
                else:
                    msg = f"Erreur: {e}"
                self.after(0, lambda: self.show_feedback(msg, RED))
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()


class App(ctk.CTk):
    def __init__(self):
        super().__init__()
        self.title("Discord Manager")
        self.geometry("950x650")
        self.minsize(850, 580)
        self.configure(fg_color=BG)

        self.current = None
        self.show_login()

    def show_login(self):
        if self.current:
            self.current.destroy()
        self.current = LoginFrame(self, on_login=self.login_ok)
        self.current.pack(fill="both", expand=True)

    def login_ok(self, token, user, settings, avatar):
        if self.current:
            self.current.destroy()
        self.current = MainFrame(self, token, user, settings, avatar, on_logout=self.show_login)
        self.current.pack(fill="both", expand=True)


if __name__ == "__main__":
    App().mainloop()
