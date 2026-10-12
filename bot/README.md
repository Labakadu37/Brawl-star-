# Bot Discord JZS Brawl (Termux)

Le bot modifie `remote/status.json` dans ce repo GitHub. Chaque copie du mod
télécharge ce fichier au lancement du jeu (`JZSRemote.java`), donc une commande
Discord touche **tous les joueurs** (délai max ~5 min, cache GitHub).

```
Toi ──/miseajour──► Bot (Termux) ──API GitHub──► remote/status.json
                                                       │
                     Joueurs (mod) ◄── téléchargé au lancement du jeu
```

## Commandes

| Commande | Effet en jeu |
|---|---|
| `/miseajour [build] [message] [lien]` | Tous les mods avec `MOD_BUILD < build` sont bloqués : popup « Mise à jour disponible » + bouton vers `discord.gg/jzbrawl`. Sans `build` = build actuel + 1 (bloque tout le monde). |
| `/annulermaj [build]` | Remet le build minimum (1 = tout le monde rejoue). |
| `/maintenance actif:True/False [message]` | Coupe/rallume le mod pour tout le monde. |
| `/annonce [message]` | Popup affichée une seule fois à chaque joueur (vide = supprimer). |
| `/badge [texte]` | Change le texte du badge en haut à gauche (vide = défaut). |
| `/lien url` | Change le lien affiché. |
| `/statut` | Affiche l'état actuel. |
| `/aide` | Liste des commandes. |

Seuls les IDs dans `OWNER_IDS` peuvent les utiliser.

## Workflow d'une mise à jour

1. Dans `mod/src/java/com/jzs/brawl/JZSRemote.java`, passe `MOD_BUILD` de `1` à `2`.
2. Rebuild l'APK (`build/build_mod.sh`) et poste-le sur ton Discord.
3. `/miseajour build:2` → tous ceux qui ont encore le build 1 sont bloqués et redirigés vers le Discord.

## Installation sur Termux

1. Crée le bot : <https://discord.com/developers/applications> → *New Application* → *Bot* → *Reset Token* (copie le token).
   Invite-le avec *OAuth2 → URL Generator* → coche `bot` + `applications.commands`.
2. Crée un token GitHub fine-grained : <https://github.com/settings/personal-access-tokens>
   → *Repository access* : seulement `Labakadu37/Brawl-star-` → *Permissions* : **Contents : Read and write**.
3. Dans Termux :

```bash
pkg update && pkg install -y python git
git clone https://github.com/Labakadu37/Brawl-star-.git
cd Brawl-star-/bot
pip install -r requirements.txt
cp .env.example .env
nano .env            # remplis DISCORD_TOKEN, GITHUB_TOKEN, OWNER_IDS, GUILD_ID
./termux_start.sh
```

`termux_start.sh` prend un wake-lock (Termux ne s'endort pas) et relance le bot s'il crash.
Désactive l'optimisation de batterie pour Termux dans les paramètres Android, sinon
Android peut le tuer en arrière-plan.

> Ne partage jamais ton `.env` : il contient tes tokens.
