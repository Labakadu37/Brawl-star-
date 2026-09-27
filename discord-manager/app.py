"""Discord Manager - fenetre de base.

Pour l'instant : une carte au centre avec un champ "Entre ton token"
et un bouton Connexion. Le token reste uniquement en memoire, il n'est
ni sauvegarde sur le disque ni envoye ou que ce soit.
"""

import customtkinter as ctk

APP_NAME = "Discord Manager"

# Couleurs facon Discord
BG = "#313338"
CARD = "#2B2D31"
FIELD = "#1E1F22"
BLURPLE = "#5865F2"
BLURPLE_HOVER = "#4752C4"
TEXT = "#F2F3F5"
MUTED = "#B5BAC1"
RED = "#F23F43"
GREEN = "#23A55A"


class App(ctk.CTk):
    def __init__(self):
        super().__init__()
        self.title(APP_NAME)
        self.geometry("520x420")
        self.minsize(420, 380)
        self.configure(fg_color=BG)

        self.token = None  # garde en memoire seulement

        card = ctk.CTkFrame(self, fg_color=CARD, corner_radius=16, width=380, height=300)
        card.place(relx=0.5, rely=0.5, anchor="center")
        card.pack_propagate(False)

        ctk.CTkLabel(
            card, text=APP_NAME, text_color=TEXT,
            font=ctk.CTkFont(size=22, weight="bold"),
        ).pack(pady=(28, 4))

        ctk.CTkLabel(
            card, text="Entre ton token", text_color=MUTED,
            font=ctk.CTkFont(size=14),
        ).pack(pady=(0, 16))

        self.entry = ctk.CTkEntry(
            card, width=320, height=40, show="•",
            placeholder_text="Colle ton token ici",
            fg_color=FIELD, border_color=FIELD, text_color=TEXT,
        )
        self.entry.pack()
        self.entry.bind("<Return>", lambda _e: self.on_login())

        self.show_var = ctk.BooleanVar(value=False)
        ctk.CTkCheckBox(
            card, text="Afficher le token", variable=self.show_var,
            command=self.toggle_show, text_color=MUTED,
            fg_color=BLURPLE, hover_color=BLURPLE_HOVER,
            checkbox_width=18, checkbox_height=18,
        ).pack(anchor="w", padx=30, pady=(10, 0))

        ctk.CTkButton(
            card, text="Connexion", width=320, height=40,
            fg_color=BLURPLE, hover_color=BLURPLE_HOVER,
            font=ctk.CTkFont(size=14, weight="bold"),
            command=self.on_login,
        ).pack(pady=(16, 8))

        self.status = ctk.CTkLabel(card, text="", text_color=MUTED)
        self.status.pack()

        self.entry.focus_set()

    def toggle_show(self):
        self.entry.configure(show="" if self.show_var.get() else "•")

    def on_login(self):
        token = self.entry.get().strip().strip('"')
        if not token:
            self.status.configure(text="Le token est vide.", text_color=RED)
            return
        self.token = token
        # Prochaine etape : se connecter et afficher les infos du compte.
        self.status.configure(text="Token recu ✔ (la suite arrive)", text_color=GREEN)


if __name__ == "__main__":
    ctk.set_appearance_mode("dark")
    App().mainloop()
