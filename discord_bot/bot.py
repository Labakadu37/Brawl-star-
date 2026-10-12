#!/usr/bin/env python3
"""
JZS Brawl - Discord control bot (host on Termux).

What it does
------------
Writes a small `status.json` to the GitHub repo (branch `status`). The mod
inside the APK polls that file (raw.githubusercontent) every ~60s and reacts:

  * /maj      -> locks the mod menu for everyone + shows an "update available"
                 screen with the Discord link.
  * /ouvrir   -> unlocks it again.
  * /annonce  -> shows a one-shot message/announcement popup to everyone.
  * /statut   -> prints the current state.

No database: the single source of truth is status.json on GitHub.

Setup: see README.md in this folder.
"""

import json
import os
import time
import base64

import requests
import discord
from discord import app_commands

# --------------------------------------------------------------------------- #
# Config: environment variables first, then config.json next to this file.
# --------------------------------------------------------------------------- #
HERE = os.path.dirname(os.path.abspath(__file__))


def _load_config():
    cfg = {}
    path = os.path.join(HERE, "config.json")
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            cfg = json.load(f)
    # env overrides
    for k in ("discord_token", "github_token", "github_repo",
              "status_branch", "guild_id", "discord_invite"):
        env = os.environ.get("JZS_" + k.upper())
        if env:
            cfg[k] = env
    cfg.setdefault("github_repo", "Labakadu37/Brawl-star-")
    cfg.setdefault("status_branch", "status")
    cfg.setdefault("discord_invite", "https://discord.gg/jzbrawl")
    cfg.setdefault("admins", [])          # list of Discord user IDs allowed
    cfg.setdefault("guild_id", "")        # optional: instant slash-command sync
    return cfg


CFG = _load_config()
GH_API = "https://api.github.com"


# --------------------------------------------------------------------------- #
# GitHub helpers (write status.json on branch `status`)
# --------------------------------------------------------------------------- #
def _gh_headers():
    return {
        "Authorization": "Bearer " + CFG["github_token"],
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    }


def _repo():
    return CFG["github_repo"]


def _default_branch():
    r = requests.get(f"{GH_API}/repos/{_repo()}", headers=_gh_headers(), timeout=20)
    r.raise_for_status()
    return r.json()["default_branch"]


def _ensure_status_branch():
    """Create the `status` branch off the default branch if it doesn't exist."""
    br = CFG["status_branch"]
    r = requests.get(f"{GH_API}/repos/{_repo()}/git/ref/heads/{br}",
                     headers=_gh_headers(), timeout=20)
    if r.status_code == 200:
        return
    base = _default_branch()
    r = requests.get(f"{GH_API}/repos/{_repo()}/git/ref/heads/{base}",
                     headers=_gh_headers(), timeout=20)
    r.raise_for_status()
    sha = r.json()["object"]["sha"]
    r = requests.post(f"{GH_API}/repos/{_repo()}/git/refs", headers=_gh_headers(),
                      json={"ref": f"refs/heads/{br}", "sha": sha}, timeout=20)
    if r.status_code not in (200, 201):
        raise RuntimeError(f"create branch failed: {r.status_code} {r.text}")


def read_status():
    """Return the current status dict (or defaults)."""
    br = CFG["status_branch"]
    r = requests.get(
        f"{GH_API}/repos/{_repo()}/contents/status.json",
        headers=_gh_headers(), params={"ref": br}, timeout=20)
    if r.status_code == 200:
        content = base64.b64decode(r.json()["content"]).decode("utf-8")
        try:
            return json.loads(content), r.json()["sha"]
        except Exception:
            return {}, r.json()["sha"]
    return {}, None


def write_status(status: dict):
    """Create/update status.json on the `status` branch."""
    _ensure_status_branch()
    _, sha = read_status()
    body = {
        "message": "update status",
        "content": base64.b64encode(
            json.dumps(status, ensure_ascii=False, indent=2).encode("utf-8")
        ).decode("ascii"),
        "branch": CFG["status_branch"],
    }
    if sha:
        body["sha"] = sha
    r = requests.put(f"{GH_API}/repos/{_repo()}/contents/status.json",
                     headers=_gh_headers(), json=body, timeout=30)
    if r.status_code not in (200, 201):
        raise RuntimeError(f"write status failed: {r.status_code} {r.text}")


# --------------------------------------------------------------------------- #
# Discord bot
# --------------------------------------------------------------------------- #
intents = discord.Intents.default()
client = discord.Client(intents=intents)
tree = app_commands.CommandTree(client)


def _is_admin(interaction: discord.Interaction) -> bool:
    admins = [str(a) for a in CFG.get("admins", [])]
    if admins and str(interaction.user.id) in admins:
        return True
    # fallback: anyone with "Manage Server" permission
    perms = getattr(interaction.user, "guild_permissions", None)
    return bool(perms and perms.manage_guild)


async def _deny(interaction):
    await interaction.response.send_message(
        "⛔ Tu n'as pas la permission d'utiliser cette commande.", ephemeral=True)


@client.event
async def on_ready():
    try:
        gid = CFG.get("guild_id")
        if gid:
            guild = discord.Object(id=int(gid))
            tree.copy_global_to(guild=guild)
            await tree.sync(guild=guild)
        else:
            await tree.sync()
        print(f"[JZS] connecté comme {client.user} - commandes synchronisées")
    except Exception as e:
        print("[JZS] sync error:", e)


@tree.command(name="maj", description="Bloquer le mod pour tout le monde (mise à jour).")
@app_commands.describe(message="Texte affiché (optionnel)")
async def maj(interaction: discord.Interaction, message: str = None):
    if not _is_admin(interaction):
        return await _deny(interaction)
    await interaction.response.defer(ephemeral=True, thinking=True)
    status = {
        "locked": True,
        "title": "Mise à jour disponible",
        "body": message or "Une nouvelle version du mod est disponible. "
                           "Rejoins le Discord pour la télécharger.",
        "button": "Rejoindre le Discord",
        "url": CFG["discord_invite"],
        "ts": int(time.time()),
    }
    try:
        write_status(status)
        await interaction.followup.send(
            "🔒 **Mod bloqué.** Tout le monde verra l'écran de mise à jour "
            "(jusqu'à ~5 min le temps de la propagation).", ephemeral=True)
    except Exception as e:
        await interaction.followup.send(f"❌ Erreur: {e}", ephemeral=True)


@tree.command(name="ouvrir", description="Débloquer le mod.")
async def ouvrir(interaction: discord.Interaction):
    if not _is_admin(interaction):
        return await _deny(interaction)
    await interaction.response.defer(ephemeral=True, thinking=True)
    status = {"locked": False, "ts": int(time.time())}
    try:
        write_status(status)
        await interaction.followup.send("🔓 **Mod débloqué.**", ephemeral=True)
    except Exception as e:
        await interaction.followup.send(f"❌ Erreur: {e}", ephemeral=True)


@tree.command(name="annonce", description="Afficher un message à tous les joueurs.")
@app_commands.describe(titre="Titre du message", message="Contenu du message")
async def annonce(interaction: discord.Interaction, titre: str, message: str):
    if not _is_admin(interaction):
        return await _deny(interaction)
    await interaction.response.defer(ephemeral=True, thinking=True)
    cur, _ = read_status()
    cur = cur or {}
    cur["announce_id"] = str(int(time.time()))      # new id => shown once
    cur["announce_title"] = titre
    cur["announce_body"] = message
    cur.setdefault("locked", bool(cur.get("locked", False)))
    cur["ts"] = int(time.time())
    try:
        write_status(cur)
        await interaction.followup.send(
            "📢 **Annonce envoyée** à tous les joueurs.", ephemeral=True)
    except Exception as e:
        await interaction.followup.send(f"❌ Erreur: {e}", ephemeral=True)


@tree.command(name="statut", description="Voir l'état actuel du mod.")
async def statut(interaction: discord.Interaction):
    await interaction.response.defer(ephemeral=True, thinking=True)
    try:
        cur, _ = read_status()
        cur = cur or {}
        locked = cur.get("locked", False)
        txt = ("🔒 BLOQUÉ" if locked else "🔓 ouvert")
        ann = cur.get("announce_title")
        extra = f"\nDernière annonce: **{ann}**" if ann else ""
        await interaction.followup.send(f"État du mod: {txt}{extra}", ephemeral=True)
    except Exception as e:
        await interaction.followup.send(f"❌ Erreur: {e}", ephemeral=True)


def main():
    token = CFG.get("discord_token")
    if not token:
        raise SystemExit("Manque discord_token (config.json ou JZS_DISCORD_TOKEN).")
    if not CFG.get("github_token"):
        raise SystemExit("Manque github_token (config.json ou JZS_GITHUB_TOKEN).")
    client.run(token)


if __name__ == "__main__":
    main()
