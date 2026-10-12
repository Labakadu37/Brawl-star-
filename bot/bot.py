#!/usr/bin/env python3
"""JZS Brawl - Discord control bot.

Slash commands that rewrite remote/status.json in the GitHub repo. Every
copy of the mod downloads that file on launch (see JZSRemote.java), so a
command here reaches all players within a few minutes.

Runs anywhere Python 3.10+ runs, including Termux. See bot/README.md.
"""

from __future__ import annotations

import asyncio
import base64
import json
import os
from pathlib import Path

import aiohttp
import discord
from discord import app_commands

HERE = Path(__file__).resolve().parent

DEFAULT_STATUS = {
    "min_build": 1,
    "update_message": "Une nouvelle version de JZS Brawl est disponible ! "
                      "Rejoins le Discord pour la telecharger.",
    "update_link": "https://discord.gg/jzbrawl",
    "maintenance": False,
    "maintenance_message": "Le mod est en maintenance, reviens plus tard.",
    "announcement": "",
    "announcement_id": 0,
    "badge_text": "",
}


def load_env(path: Path) -> None:
    if not path.is_file():
        return
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip().strip('"').strip("'"))


load_env(HERE / ".env")

DISCORD_TOKEN = os.environ.get("DISCORD_TOKEN", "")
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")
GITHUB_REPO = os.environ.get("GITHUB_REPO", "Labakadu37/Brawl-star-")
STATUS_PATH = os.environ.get("STATUS_PATH", "remote/status.json")
OWNER_IDS = {int(x) for x in os.environ.get("OWNER_IDS", "").replace(" ", "").split(",") if x}
GUILD_ID = int(os.environ.get("GUILD_ID", "0") or 0)

COLOR_OK = 0x2ECC71
COLOR_WARN = 0xF1C40F
COLOR_BAD = 0xE74C3C
COLOR_INFO = 0x5865F2


class StatusStore:
    """Read-modify-write of the status file through the GitHub contents API."""

    def __init__(self) -> None:
        self._lock = asyncio.Lock()
        self._session: aiohttp.ClientSession | None = None
        self._url = f"https://api.github.com/repos/{GITHUB_REPO}/contents/{STATUS_PATH}"

    async def _http(self) -> aiohttp.ClientSession:
        if self._session is None or self._session.closed:
            self._session = aiohttp.ClientSession(headers={
                "Authorization": f"Bearer {GITHUB_TOKEN}",
                "Accept": "application/vnd.github+json",
                "X-GitHub-Api-Version": "2022-11-28",
                "User-Agent": "jzs-brawl-bot",
            })
        return self._session

    async def _get(self) -> tuple[dict, str | None]:
        http = await self._http()
        async with http.get(self._url) as r:
            if r.status == 404:
                return dict(DEFAULT_STATUS), None
            if r.status != 200:
                raise RuntimeError(f"GitHub GET {r.status}: {await r.text()}")
            data = await r.json()
        status = dict(DEFAULT_STATUS)
        status.update(json.loads(base64.b64decode(data["content"]).decode("utf-8")))
        return status, data["sha"]

    async def read(self) -> dict:
        async with self._lock:
            status, _ = await self._get()
            return status

    async def update(self, changes: dict, reason: str) -> dict:
        async with self._lock:
            status, sha = await self._get()
            status.update(changes)
            body = {
                "message": f"status: {reason}",
                "content": base64.b64encode(
                    (json.dumps(status, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
                ).decode("ascii"),
            }
            if sha:
                body["sha"] = sha
            http = await self._http()
            async with http.put(self._url, json=body) as r:
                if r.status not in (200, 201):
                    raise RuntimeError(f"GitHub PUT {r.status}: {await r.text()}")
            return status

    async def close(self) -> None:
        if self._session is not None:
            await self._session.close()


store = StatusStore()


class JZSBot(discord.Client):
    def __init__(self) -> None:
        super().__init__(intents=discord.Intents.default())
        self.tree = app_commands.CommandTree(self)

    async def setup_hook(self) -> None:
        if GUILD_ID:
            guild = discord.Object(id=GUILD_ID)
            self.tree.copy_global_to(guild=guild)
            await self.tree.sync(guild=guild)
        else:
            await self.tree.sync()

    async def close(self) -> None:
        await store.close()
        await super().close()


bot = JZSBot()


def owner_only():
    async def predicate(interaction: discord.Interaction) -> bool:
        if interaction.user.id in OWNER_IDS:
            return True
        await interaction.response.send_message(
            embed=discord.Embed(description="⛔ Tu n'as pas la permission.", color=COLOR_BAD),
            ephemeral=True,
        )
        return False
    return app_commands.check(predicate)


def status_embed(status: dict, title: str, color: int) -> discord.Embed:
    blocked = status["maintenance"]
    e = discord.Embed(title=title, color=color)
    e.add_field(name="🔢 Build minimum", value=f"`{status['min_build']}`")
    e.add_field(name="🛠️ Maintenance", value="🔴 ON" if blocked else "🟢 OFF")
    e.add_field(name="🔗 Lien", value=status["update_link"], inline=False)
    e.add_field(name="📦 Message MAJ", value=status["update_message"][:1024], inline=False)
    if status["announcement"]:
        e.add_field(name=f"📢 Annonce #{status['announcement_id']}",
                    value=status["announcement"][:1024], inline=False)
    if status["badge_text"]:
        e.add_field(name="🏷️ Badge", value=status["badge_text"], inline=False)
    e.set_footer(text="JZS Brawl • les joueurs recoivent le changement en ~5 min max")
    return e


async def apply(interaction: discord.Interaction, changes: dict, reason: str,
                title: str, color: int) -> None:
    await interaction.response.defer(ephemeral=True, thinking=True)
    try:
        status = await store.update(changes, reason)
    except Exception as exc:  # noqa: BLE001 - surface any GitHub error to the owner
        await interaction.followup.send(
            embed=discord.Embed(title="❌ Erreur GitHub", description=f"```{exc}```"[:4000],
                                color=COLOR_BAD))
        return
    await interaction.followup.send(embed=status_embed(status, title, color))


@bot.tree.command(name="miseajour", description="Bloque les anciennes versions du mod et affiche le lien de MAJ")
@app_commands.describe(
    build="Build minimum autorise (laisser vide = build actuel + 1, bloque tout le monde)",
    message="Message affiche aux joueurs",
    lien="Lien affiche (defaut: discord.gg/jzbrawl)",
)
@owner_only()
async def miseajour(interaction: discord.Interaction, build: int | None = None,
                    message: str | None = None, lien: str | None = None) -> None:
    if build is None:
        build = (await store.read())["min_build"] + 1
    changes: dict = {"min_build": build}
    if message:
        changes["update_message"] = message
    if lien:
        changes["update_link"] = lien
    await apply(interaction, changes, f"miseajour -> build {build}",
                f"📦 Mise a jour forcee : build < {build} bloque", COLOR_WARN)


@bot.tree.command(name="annulermaj", description="Re-autorise un ancien build")
@app_commands.describe(build="Nouveau build minimum (1 = tout le monde peut jouer)")
@owner_only()
async def annulermaj(interaction: discord.Interaction, build: int = 1) -> None:
    await apply(interaction, {"min_build": build}, f"annulermaj -> build {build}",
                f"↩️ Build minimum remis a {build}", COLOR_OK)


@bot.tree.command(name="maintenance", description="Active/desactive le mod pour tout le monde")
@app_commands.describe(actif="True = mod bloque", message="Message de maintenance")
@owner_only()
async def maintenance(interaction: discord.Interaction, actif: bool,
                      message: str | None = None) -> None:
    changes: dict = {"maintenance": actif}
    if message:
        changes["maintenance_message"] = message
    await apply(interaction, changes, f"maintenance {'on' if actif else 'off'}",
                "🛠️ Maintenance activee" if actif else "✅ Maintenance terminee",
                COLOR_BAD if actif else COLOR_OK)


@bot.tree.command(name="annonce", description="Popup affichee une fois a chaque joueur")
@app_commands.describe(message="Texte de l'annonce (vide = supprimer)")
@owner_only()
async def annonce(interaction: discord.Interaction, message: str = "") -> None:
    current = await store.read()
    changes = {"announcement": message}
    if message:
        changes["announcement_id"] = current["announcement_id"] + 1
    await apply(interaction, changes, "annonce",
                "📢 Annonce envoyee" if message else "🗑️ Annonce supprimee", COLOR_INFO)


@bot.tree.command(name="badge", description="Change le texte du badge en jeu")
@app_commands.describe(texte="Nouveau texte (vide = texte par defaut)")
@owner_only()
async def badge(interaction: discord.Interaction, texte: str = "") -> None:
    await apply(interaction, {"badge_text": texte}, "badge",
                "🏷️ Badge modifie", COLOR_INFO)


@bot.tree.command(name="lien", description="Change le lien affiche aux joueurs")
@owner_only()
async def lien(interaction: discord.Interaction, url: str) -> None:
    await apply(interaction, {"update_link": url}, "lien", "🔗 Lien modifie", COLOR_INFO)


@bot.tree.command(name="statut", description="Affiche l'etat actuel du mod")
@owner_only()
async def statut(interaction: discord.Interaction) -> None:
    await interaction.response.defer(ephemeral=True, thinking=True)
    status = await store.read()
    blocked = status["maintenance"]
    await interaction.followup.send(embed=status_embed(
        status, "📊 Statut JZS Brawl", COLOR_BAD if blocked else COLOR_OK))


@bot.tree.command(name="aide", description="Liste des commandes")
async def aide(interaction: discord.Interaction) -> None:
    e = discord.Embed(title="🤖 JZS Brawl - Commandes", color=COLOR_INFO, description=(
        "`/miseajour [build] [message] [lien]` – bloque les vieux builds\n"
        "`/annulermaj [build]` – re-autorise un build\n"
        "`/maintenance actif [message]` – coupe le mod pour tout le monde\n"
        "`/annonce [message]` – popup unique en jeu\n"
        "`/badge [texte]` – change le texte du badge\n"
        "`/lien url` – change le lien affiche\n"
        "`/statut` – etat actuel"
    ))
    await interaction.response.send_message(embed=e, ephemeral=True)


@bot.event
async def on_ready() -> None:
    print(f"Connecte en tant que {bot.user} - repo {GITHUB_REPO}:{STATUS_PATH}")
    await bot.change_presence(activity=discord.Game(name="JZS Brawl • /aide"))


def main() -> None:
    missing = [k for k, v in (("DISCORD_TOKEN", DISCORD_TOKEN),
                              ("GITHUB_TOKEN", GITHUB_TOKEN),
                              ("OWNER_IDS", OWNER_IDS)) if not v]
    if missing:
        raise SystemExit(f"Variables manquantes dans bot/.env : {', '.join(missing)}")
    bot.run(DISCORD_TOKEN)


if __name__ == "__main__":
    main()
