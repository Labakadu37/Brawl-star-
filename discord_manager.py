import customtkinter as ctk
import requests
import threading
import io
import os
import sys
from urllib.request import urlopen
from PIL import Image, ImageDraw

# --- Theme ---
ctk.set_appearance_mode("dark")
ctk.set_default_color_theme("blue")

DISCORD_API = "https://discord.com/api/v10"
DARK_BG = "#1a1a2e"
CARD_BG = "#16213e"
ACCENT = "#5865F2"
ACCENT_HOVER = "#4752C4"
GREEN = "#57F287"
YELLOW = "#FEE75C"
RED = "#ED4245"
GRAY = "#99AAB5"
TEXT = "#FFFFFF"
SUBTEXT = "#B9BBBE"


def fetch_user(token):
    headers = {"Authorization": token, "Content-Type": "application/json"}
    r = requests.get(f"{DISCORD_API}/users/@me", headers=headers, timeout=10)
    r.raise_for_status()
    return r.json()


def fetch_avatar(user_data):
    avatar_hash = user_data.get("avatar")
    user_id = user_data["id"]
    if avatar_hash:
        ext = "gif" if avatar_hash.startswith("a_") else "png"
        url = f"https://cdn.discordapp.com/avatars/{user_id}/{avatar_hash}.{ext}?size=128"
    else:
        index = (int(user_id) >> 22) % 6
        url = f"https://cdn.discordapp.com/embed/avatars/{index}.png"
    data = urlopen(url).read()
    img = Image.open(io.BytesIO(data)).resize((128, 128), Image.LANCZOS)
    return img


def make_circle_avatar(img, size=128):
    mask = Image.new("L", (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size, size), fill=255)
    result = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    result.paste(img.convert("RGBA"), (0, 0), mask)
    return result


def update_status(token, status):
    headers = {"Authorization": token, "Content-Type": "application/json"}
    r = requests.patch(
        f"{DISCORD_API}/users/@me/settings",
        headers=headers,
        json={"status": status},
        timeout=10,
    )
    r.raise_for_status()
    return r.json()


def update_custom_status(token, text=None, emoji_name=None):
    headers = {"Authorization": token, "Content-Type": "application/json"}
    custom = {}
    if text:
        custom["text"] = text
    if emoji_name:
        custom["emoji_name"] = emoji_name
    payload = {"custom_status": custom if (text or emoji_name) else None}
    r = requests.patch(
        f"{DISCORD_API}/users/@me/settings",
        headers=headers,
        json=payload,
        timeout=10,
    )
    r.raise_for_status()
    return r.json()


class LoginFrame(ctk.CTkFrame):
    def __init__(self, master, on_login):
        super().__init__(master, fg_color=DARK_BG)
        self.on_login = on_login

        self.grid_rowconfigure((0, 1, 2, 3, 4, 5, 6), weight=1)
        self.grid_columnconfigure(0, weight=1)

        # Logo / Title
        title = ctk.CTkLabel(
            self,
            text="Discord Manager",
            font=ctk.CTkFont(size=32, weight="bold"),
            text_color=ACCENT,
        )
        title.grid(row=1, column=0, pady=(0, 5))

        subtitle = ctk.CTkLabel(
            self,
            text="Gere ton compte Discord facilement",
            font=ctk.CTkFont(size=14),
            text_color=SUBTEXT,
        )
        subtitle.grid(row=2, column=0, pady=(0, 30))

        # Token input card
        card = ctk.CTkFrame(self, fg_color=CARD_BG, corner_radius=15, width=420, height=200)
        card.grid(row=3, column=0, padx=40)
        card.grid_propagate(False)
        card.grid_rowconfigure((0, 1, 2, 3), weight=1)
        card.grid_columnconfigure(0, weight=1)

        token_label = ctk.CTkLabel(
            card,
            text="Rentre ton Token",
            font=ctk.CTkFont(size=18, weight="bold"),
            text_color=TEXT,
        )
        token_label.grid(row=0, column=0, pady=(20, 5))

        self.token_entry = ctk.CTkEntry(
            card,
            width=350,
            height=45,
            placeholder_text="Token Discord...",
            show="*",
            font=ctk.CTkFont(size=14),
            corner_radius=10,
            fg_color="#0f3460",
            border_color=ACCENT,
            text_color=TEXT,
        )
        self.token_entry.grid(row=1, column=0, pady=(5, 5))

        self.show_var = ctk.BooleanVar(value=False)
        show_check = ctk.CTkCheckBox(
            card,
            text="Afficher le token",
            variable=self.show_var,
            command=self.toggle_show,
            font=ctk.CTkFont(size=12),
            text_color=SUBTEXT,
            fg_color=ACCENT,
            hover_color=ACCENT_HOVER,
        )
        show_check.grid(row=2, column=0, pady=(0, 5))

        self.login_btn = ctk.CTkButton(
            card,
            text="Connexion",
            width=200,
            height=42,
            font=ctk.CTkFont(size=16, weight="bold"),
            fg_color=ACCENT,
            hover_color=ACCENT_HOVER,
            corner_radius=10,
            command=self.do_login,
        )
        self.login_btn.grid(row=3, column=0, pady=(5, 20))

        self.status_label = ctk.CTkLabel(
            self, text="", font=ctk.CTkFont(size=13), text_color=RED
        )
        self.status_label.grid(row=4, column=0, pady=(10, 0))

        # Bind Enter key
        self.token_entry.bind("<Return>", lambda e: self.do_login())

    def toggle_show(self):
        self.token_entry.configure(show="" if self.show_var.get() else "*")

    def do_login(self):
        token = self.token_entry.get().strip()
        if not token:
            self.status_label.configure(text="Entre un token !", text_color=RED)
            return
        self.login_btn.configure(state="disabled", text="Connexion...")
        self.status_label.configure(text="Verification...", text_color=YELLOW)

        def task():
            try:
                user = fetch_user(token)
                avatar_img = None
                try:
                    avatar_img = fetch_avatar(user)
                    avatar_img = make_circle_avatar(avatar_img)
                except Exception:
                    pass
                self.after(0, lambda: self.on_login(token, user, avatar_img))
            except requests.exceptions.HTTPError as e:
                if e.response is not None and e.response.status_code == 401:
                    msg = "Token invalide !"
                else:
                    msg = f"Erreur: {e}"
                self.after(
                    0,
                    lambda: (
                        self.status_label.configure(text=msg, text_color=RED),
                        self.login_btn.configure(state="normal", text="Connexion"),
                    ),
                )
            except Exception as e:
                self.after(
                    0,
                    lambda: (
                        self.status_label.configure(
                            text=f"Erreur: {e}", text_color=RED
                        ),
                        self.login_btn.configure(state="normal", text="Connexion"),
                    ),
                )

        threading.Thread(target=task, daemon=True).start()


class MainFrame(ctk.CTkFrame):
    def __init__(self, master, token, user_data, avatar_img, on_logout):
        super().__init__(master, fg_color=DARK_BG)
        self.token = token
        self.user_data = user_data
        self.on_logout = on_logout

        self.grid_columnconfigure(0, weight=1)
        self.grid_columnconfigure(1, weight=2)
        self.grid_rowconfigure(0, weight=1)

        # --- Left: Profile preview ---
        left = ctk.CTkFrame(self, fg_color=CARD_BG, corner_radius=15, width=300)
        left.grid(row=0, column=0, padx=(15, 8), pady=15, sticky="nsew")
        left.grid_propagate(False)
        left.grid_columnconfigure(0, weight=1)

        profile_title = ctk.CTkLabel(
            left,
            text="Profil",
            font=ctk.CTkFont(size=20, weight="bold"),
            text_color=ACCENT,
        )
        profile_title.grid(row=0, column=0, pady=(20, 15))

        # Avatar
        if avatar_img:
            ctk_img = ctk.CTkImage(light_image=avatar_img, dark_image=avatar_img, size=(100, 100))
            avatar_label = ctk.CTkLabel(left, image=ctk_img, text="")
            avatar_label.grid(row=1, column=0, pady=(5, 10))
        else:
            no_avatar = ctk.CTkLabel(
                left, text="👤", font=ctk.CTkFont(size=60), text_color=GRAY
            )
            no_avatar.grid(row=1, column=0, pady=(5, 10))

        username = user_data.get("username", "???")
        display_name = user_data.get("global_name") or username
        discriminator = user_data.get("discriminator", "0")

        name_label = ctk.CTkLabel(
            left,
            text=display_name,
            font=ctk.CTkFont(size=22, weight="bold"),
            text_color=TEXT,
        )
        name_label.grid(row=2, column=0, pady=(5, 2))

        if discriminator and discriminator != "0":
            tag = f"{username}#{discriminator}"
        else:
            tag = f"@{username}"
        tag_label = ctk.CTkLabel(
            left, text=tag, font=ctk.CTkFont(size=14), text_color=SUBTEXT
        )
        tag_label.grid(row=3, column=0, pady=(0, 5))

        user_id = user_data.get("id", "???")
        id_label = ctk.CTkLabel(
            left,
            text=f"ID: {user_id}",
            font=ctk.CTkFont(size=12),
            text_color=GRAY,
        )
        id_label.grid(row=4, column=0, pady=(0, 5))

        # Badges info
        flags = user_data.get("public_flags", 0)
        badges = []
        flag_map = {
            1: "Staff",
            2: "Partner",
            4: "HypeSquad Events",
            8: "Bug Hunter 1",
            64: "Bravery",
            128: "Brilliance",
            256: "Balance",
            512: "Early Supporter",
            16384: "Bug Hunter 2",
            131072: "Dev Bot",
            4194304: "Active Dev",
        }
        for val, name in flag_map.items():
            if flags & val:
                badges.append(name)

        if badges:
            badge_text = " | ".join(badges)
        else:
            badge_text = "Aucun badge"
        badge_label = ctk.CTkLabel(
            left,
            text=badge_text,
            font=ctk.CTkFont(size=11),
            text_color=SUBTEXT,
            wraplength=250,
        )
        badge_label.grid(row=5, column=0, pady=(5, 10))

        # Nitro
        premium = user_data.get("premium_type", 0)
        nitro_map = {0: "Pas de Nitro", 1: "Nitro Classic", 2: "Nitro", 3: "Nitro Basic"}
        nitro_label = ctk.CTkLabel(
            left,
            text=nitro_map.get(premium, "Inconnu"),
            font=ctk.CTkFont(size=13),
            text_color=YELLOW if premium else GRAY,
        )
        nitro_label.grid(row=6, column=0, pady=(0, 10))

        # Email / Phone
        email = user_data.get("email", "Non visible")
        phone = user_data.get("phone", "Non visible")
        info_frame = ctk.CTkFrame(left, fg_color="transparent")
        info_frame.grid(row=7, column=0, pady=(5, 10), padx=15, sticky="ew")
        info_frame.grid_columnconfigure(0, weight=1)

        if email and email != "Non visible":
            ctk.CTkLabel(
                info_frame,
                text=f"Email: {email}",
                font=ctk.CTkFont(size=11),
                text_color=SUBTEXT,
                anchor="w",
            ).grid(row=0, column=0, sticky="w", pady=2)

        if phone and phone != "Non visible":
            ctk.CTkLabel(
                info_frame,
                text=f"Tel: {phone}",
                font=ctk.CTkFont(size=11),
                text_color=SUBTEXT,
                anchor="w",
            ).grid(row=1, column=0, sticky="w", pady=2)

        # Logout button
        logout_btn = ctk.CTkButton(
            left,
            text="Deconnexion",
            fg_color=RED,
            hover_color="#a83232",
            font=ctk.CTkFont(size=13, weight="bold"),
            corner_radius=8,
            width=150,
            height=35,
            command=self.on_logout,
        )
        logout_btn.grid(row=8, column=0, pady=(10, 20))

        # --- Right: Account management ---
        right = ctk.CTkFrame(self, fg_color=CARD_BG, corner_radius=15)
        right.grid(row=0, column=1, padx=(8, 15), pady=15, sticky="nsew")
        right.grid_columnconfigure(0, weight=1)

        manage_title = ctk.CTkLabel(
            right,
            text="Gestion du compte",
            font=ctk.CTkFont(size=20, weight="bold"),
            text_color=ACCENT,
        )
        manage_title.grid(row=0, column=0, pady=(20, 20), padx=20, sticky="w")

        # --- Status section ---
        status_section = ctk.CTkFrame(right, fg_color="#0f3460", corner_radius=10)
        status_section.grid(row=1, column=0, padx=20, pady=(0, 15), sticky="ew")
        status_section.grid_columnconfigure(1, weight=1)

        ctk.CTkLabel(
            status_section,
            text="Statut en ligne",
            font=ctk.CTkFont(size=15, weight="bold"),
            text_color=TEXT,
        ).grid(row=0, column=0, columnspan=4, padx=15, pady=(12, 8), sticky="w")

        statuses = [
            ("En ligne", "online", GREEN),
            ("Inactif", "idle", YELLOW),
            ("Ne pas deranger", "dnd", RED),
            ("Invisible", "invisible", GRAY),
        ]

        for i, (label, value, color) in enumerate(statuses):
            btn = ctk.CTkButton(
                status_section,
                text=label,
                width=120,
                height=35,
                fg_color=color,
                hover_color=ACCENT_HOVER,
                text_color="#000000" if color in (GREEN, YELLOW) else TEXT,
                font=ctk.CTkFont(size=12, weight="bold"),
                corner_radius=8,
                command=lambda v=value: self.set_status(v),
            )
            btn.grid(row=1, column=i, padx=8, pady=(0, 12))

        # --- Custom status ---
        custom_section = ctk.CTkFrame(right, fg_color="#0f3460", corner_radius=10)
        custom_section.grid(row=2, column=0, padx=20, pady=(0, 15), sticky="ew")
        custom_section.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            custom_section,
            text="Statut personnalise",
            font=ctk.CTkFont(size=15, weight="bold"),
            text_color=TEXT,
        ).grid(row=0, column=0, columnspan=2, padx=15, pady=(12, 8), sticky="w")

        self.custom_text = ctk.CTkEntry(
            custom_section,
            placeholder_text="Ton texte de statut...",
            height=38,
            font=ctk.CTkFont(size=13),
            fg_color=DARK_BG,
            border_color=ACCENT,
            text_color=TEXT,
            corner_radius=8,
        )
        self.custom_text.grid(row=1, column=0, padx=(15, 8), pady=(0, 12), sticky="ew")

        apply_custom = ctk.CTkButton(
            custom_section,
            text="Appliquer",
            width=100,
            height=38,
            fg_color=ACCENT,
            hover_color=ACCENT_HOVER,
            font=ctk.CTkFont(size=13, weight="bold"),
            corner_radius=8,
            command=self.apply_custom_status,
        )
        apply_custom.grid(row=1, column=1, padx=(0, 15), pady=(0, 12))

        clear_custom = ctk.CTkButton(
            custom_section,
            text="Effacer le statut",
            width=130,
            height=32,
            fg_color=RED,
            hover_color="#a83232",
            font=ctk.CTkFont(size=12),
            corner_radius=8,
            command=self.clear_custom_status,
        )
        clear_custom.grid(row=2, column=0, columnspan=2, padx=15, pady=(0, 12))

        # --- Streaming status ---
        stream_section = ctk.CTkFrame(right, fg_color="#0f3460", corner_radius=10)
        stream_section.grid(row=3, column=0, padx=20, pady=(0, 15), sticky="ew")
        stream_section.grid_columnconfigure(0, weight=1)

        ctk.CTkLabel(
            stream_section,
            text="Statut Streaming",
            font=ctk.CTkFont(size=15, weight="bold"),
            text_color=TEXT,
        ).grid(row=0, column=0, columnspan=2, padx=15, pady=(12, 8), sticky="w")

        ctk.CTkLabel(
            stream_section,
            text="Simule un statut 'En stream' sur ton profil",
            font=ctk.CTkFont(size=11),
            text_color=SUBTEXT,
        ).grid(row=1, column=0, columnspan=2, padx=15, pady=(0, 8), sticky="w")

        self.stream_name = ctk.CTkEntry(
            stream_section,
            placeholder_text="Nom du stream (ex: Brawl Stars)...",
            height=38,
            font=ctk.CTkFont(size=13),
            fg_color=DARK_BG,
            border_color="#9B59B6",
            text_color=TEXT,
            corner_radius=8,
        )
        self.stream_name.grid(row=2, column=0, padx=(15, 8), pady=(0, 8), sticky="ew")

        self.stream_url = ctk.CTkEntry(
            stream_section,
            placeholder_text="URL Twitch (ex: https://twitch.tv/toi)...",
            height=38,
            font=ctk.CTkFont(size=13),
            fg_color=DARK_BG,
            border_color="#9B59B6",
            text_color=TEXT,
            corner_radius=8,
        )
        self.stream_url.grid(row=3, column=0, padx=(15, 8), pady=(0, 8), sticky="ew")

        stream_btn = ctk.CTkButton(
            stream_section,
            text="Lancer le Streaming",
            width=160,
            height=38,
            fg_color="#9B59B6",
            hover_color="#7D3C98",
            font=ctk.CTkFont(size=13, weight="bold"),
            corner_radius=8,
            command=self.start_streaming,
        )
        stream_btn.grid(row=2, column=1, padx=(0, 15), pady=(0, 8))

        stop_stream_btn = ctk.CTkButton(
            stream_section,
            text="Arreter",
            width=160,
            height=38,
            fg_color=RED,
            hover_color="#a83232",
            font=ctk.CTkFont(size=13, weight="bold"),
            corner_radius=8,
            command=self.stop_streaming,
        )
        stop_stream_btn.grid(row=3, column=1, padx=(0, 15), pady=(0, 8))

        # --- Feedback label ---
        self.feedback = ctk.CTkLabel(
            right, text="", font=ctk.CTkFont(size=13), text_color=GREEN
        )
        self.feedback.grid(row=4, column=0, pady=(5, 15))

    def show_feedback(self, text, color=GREEN):
        self.feedback.configure(text=text, text_color=color)
        self.after(3000, lambda: self.feedback.configure(text=""))

    def set_status(self, status):
        def task():
            try:
                update_status(self.token, status)
                self.after(0, lambda: self.show_feedback(f"Statut change: {status}"))
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()

    def apply_custom_status(self):
        text = self.custom_text.get().strip()
        if not text:
            self.show_feedback("Ecris un texte de statut !", RED)
            return

        def task():
            try:
                update_custom_status(self.token, text=text)
                self.after(0, lambda: self.show_feedback(f"Statut perso applique !"))
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()

    def clear_custom_status(self):
        def task():
            try:
                update_custom_status(self.token)
                self.after(0, lambda: self.show_feedback("Statut perso efface !"))
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()

    def start_streaming(self):
        name = self.stream_name.get().strip() or "Stream"
        url = self.stream_url.get().strip() or "https://twitch.tv/"

        def task():
            try:
                headers = {
                    "Authorization": self.token,
                    "Content-Type": "application/json",
                }
                payload = {
                    "activities": [
                        {
                            "name": name,
                            "type": 1,
                            "url": url,
                        }
                    ],
                    "status": "online",
                    "since": 0,
                    "afk": False,
                }
                # Note: activity streaming requires a gateway connection
                # This sets custom status as a workaround
                update_custom_status(self.token, text=f"🔴 En live: {name}")
                self.after(
                    0,
                    lambda: self.show_feedback(f"Streaming simule: {name}"),
                )
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()

    def stop_streaming(self):
        def task():
            try:
                update_custom_status(self.token)
                self.after(0, lambda: self.show_feedback("Streaming arrete !"))
            except Exception as e:
                self.after(0, lambda: self.show_feedback(f"Erreur: {e}", RED))

        threading.Thread(target=task, daemon=True).start()


class App(ctk.CTk):
    def __init__(self):
        super().__init__()
        self.title("Discord Manager")
        self.geometry("850x620")
        self.minsize(750, 550)
        self.configure(fg_color=DARK_BG)

        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=1)

        self.current_frame = None
        self.show_login()

    def show_login(self):
        if self.current_frame:
            self.current_frame.destroy()
        self.current_frame = LoginFrame(self, on_login=self.on_login_success)
        self.current_frame.grid(row=0, column=0, sticky="nsew")

    def on_login_success(self, token, user_data, avatar_img):
        if self.current_frame:
            self.current_frame.destroy()
        self.current_frame = MainFrame(
            self, token, user_data, avatar_img, on_logout=self.show_login
        )
        self.current_frame.grid(row=0, column=0, sticky="nsew")

    def show_main(self):
        pass


if __name__ == "__main__":
    app = App()
    app.mainloop()
