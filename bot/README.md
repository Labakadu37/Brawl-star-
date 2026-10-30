# JZS key bot

Discord bot (`/genkey`, `/keys`, `/keyinfo`, `/revokekey`, `/unrevokekey`, `/resetkey`) plus the HTTP API the mod menu calls (`/api/activate`, `/api/check`). Every answer is signed with `private.pem`; the menu only trusts answers that verify against the public key built into the APK.

## Start

1. Install Node.js 20+.
2. Copy `.env.example` to `.env` and fill it in.
3. Windows: double-click `start.bat`. Linux/macOS: `./start.sh`.

## .env

| Name | Value |
|---|---|
| `DISCORD_TOKEN` | Bot token from the Discord Developer Portal (Bot tab, Reset Token). |
| `DISCORD_CLIENT_ID` | Application ID. |
| `DISCORD_GUILD_ID` | Your server ID. Commands then appear instantly on that server. |
| `ADMIN_ROLE_ID` | Role allowed to use the commands (admins always can). |
| `PORT` | API port. |
| `HOST` | `127.0.0.1` behind nginx (install.sh); `0.0.0.0` to expose the port directly. |

## Make the API reachable

The phone must reach the API over HTTPS. Either:

- VPS with a domain: `sudo bash install.sh your-domain.com` (nginx, Let's Encrypt, systemd service).
- Home PC: run the bot, then `cloudflared tunnel --url http://localhost:PORT` and use the `https://...trycloudflare.com` URL it prints. That URL changes on every restart; use a named tunnel for a fixed one.

Rebuild the APK with that URL: `JZ_URL=https://... python3 tools/tale_menu/tale_patch.py ...`.

## Keys

`private.pem` signs every answer. Keep it secret and never regenerate it: the APK contains the matching public key, so a new key pair means a new APK.
