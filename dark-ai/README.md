# DARK AI

IA 100 % locale, style dark. Elle tourne sur ton PC via [Ollama](https://ollama.com) :
pas d'abonnement, pas d'internet après l'installation, rien ne sort de ta machine.

## Installation

**Windows** : double-clique sur `install.bat`, puis sur `start.bat`.

**Linux / macOS** :

```bash
./install.sh
./start.sh
```

L'interface s'ouvre toute seule sur <http://127.0.0.1:7666>.

L'installateur met en place Ollama (et Python sous Windows si besoin), te fait choisir
le « cerveau » de DARK selon ton PC, le télécharge, puis crée le modèle `dark`
avec sa personnalité (fichier `Modelfile`).

## Quel modèle choisir ?

| Choix | Modèle         | Il te faut                       |
|-------|----------------|----------------------------------|
| 1     | `qwen3:4b`     | 8 Go de RAM, pas de GPU          |
| 2     | `qwen3:8b`     | 16 Go de RAM ou GPU 8 Go (conseillé) |
| 3     | `qwen3:14b`    | GPU 12 Go                        |
| 4     | `gpt-oss:20b`  | GPU 16 Go                        |
| 5     | `qwen3:32b`    | GPU 24 Go                        |
| 6     | `gpt-oss:120b` | 64 Go+ de RAM/VRAM — le plus puissant |

Trop gros pour ton PC = très lent. Commence par 2 et monte si ça tourne bien.
Pour changer plus tard, relance l'installateur.

## Personnaliser

- **Personnalité** : modifie le bloc `SYSTEM` dans `Modelfile`, puis
  `ollama create dark -f Modelfile`.
- **Logo** : remplace `web/logo.png` (carré) et `web/favicon.png`.
- **Couleurs** : variables `--red`, `--bg`… en haut de `web/index.html`.
- **Port / modèle par défaut** : variables d'environnement `DARK_PORT`, `DARK_MODEL`.

## Fichiers

```
dark-ai/
├── install.bat / install.sh   installation
├── start.bat / start.sh       lancement
├── Modelfile                  personnalité de DARK
├── server.py                  petit serveur local (Python, sans dépendance)
└── web/                       interface (index.html, logo.png, favicon.png)
```
