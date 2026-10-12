# Bot Discord JZS Brawl (hébergement Termux)

Le bot pilote le mod à distance en écrivant `status.json` sur GitHub.
Le mod lit ce fichier et se bloque / affiche des annonces.

## 1. Créer le bot Discord

1. https://discord.com/developers/applications → **New Application**.
2. Onglet **Bot** → **Add Bot** → copie le **Token**.
3. Onglet **OAuth2 → URL Generator** : coche `bot` + `applications.commands`,
   puis invite le bot sur ton serveur avec l'URL générée.
   (Aucun "privileged intent" nécessaire : on utilise les commandes slash.)

## 2. Créer un token GitHub

GitHub → Settings → Developer settings → **Fine-grained tokens** →
Generate new token :
- Repository access : **Labakadu37/Brawl-star-**
- Permissions → Repository → **Contents : Read and write**
Copie le token.

## 3. Installer sur Termux

```bash
pkg update -y && pkg install -y python git
pip install -U discord.py requests

git clone https://github.com/Labakadu37/Brawl-star-.git
cd Brawl-star-/discord_bot      # (depuis la branche qui contient ce dossier)

cp config.example.json config.json
nano config.json                # colle les 2 tokens + ton ID Discord + guild_id
```

Pour trouver ton **ID Discord** : Paramètres Discord → Avancés → active
"Mode développeur", puis clic droit sur ton profil → "Copier l'identifiant".
Même chose sur ton serveur pour le `guild_id` (clic droit sur le serveur).

## 4. Lancer

```bash
python bot.py
```

Tu dois voir `connecté comme ... - commandes synchronisées`.

## 5. Commandes (slash, dans Discord)

| Commande            | Effet                                                        |
|---------------------|--------------------------------------------------------------|
| `/maj [message]`    | 🔒 Bloque le mod pour tout le monde + écran "mise à jour".    |
| `/ouvrir`           | 🔓 Débloque le mod.                                           |
| `/annonce titre message` | 📢 Affiche un message une fois à chaque joueur.         |
| `/statut`           | Affiche l'état actuel.                                        |

> Propagation : jusqu'à ~5 min (cache GitHub + sondage du mod toutes les 60 s).

## Laisser tourner en continu sur Termux

```bash
pkg install -y tmux
tmux new -s jzs
python bot.py
# Ctrl+b puis d pour détacher ; "tmux attach -t jzs" pour revenir.
```
