# Discord Manager

Application Windows (`.exe`) pour gérer son compte Discord à partir de son token.

**Étape actuelle : la base.** Une fenêtre avec une carte « Entre ton token »,
un champ masqué, une case « Afficher le token » et un bouton Connexion.
Le token reste uniquement en mémoire : il n'est ni sauvegardé ni envoyé.

## Récupérer le .exe

Chaque push qui modifie `discord-manager/` lance le workflow GitHub Actions
**Build Discord Manager (.exe)**. Dans l'onglet *Actions* du repo, ouvre le
dernier run et télécharge l'artifact `DiscordManager` (un zip qui contient
`DiscordManager.exe`).

## Construire le .exe soi-même (Windows)

Il faut Python 3.10+ installé (cocher « Add to PATH »), puis double-clic sur
`build.bat`. Le fichier sort dans `dist\DiscordManager.exe`.

## Lancer sans build

```bash
pip install -r requirements.txt
python app.py
```
