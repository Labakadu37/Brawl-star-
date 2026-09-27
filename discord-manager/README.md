# Discord Manager

Base de l'app : une fenetre avec un carre au centre ou on rentre son token
Discord. Le token est verifie (`GET /users/@me`) et, s'il est bon, l'app
affiche le nom du compte. Le token reste en memoire, rien n'est sauvegarde.

## Lancer sans .exe

Python 3.9+ (tkinter est inclus avec Python sur Windows) :

```
python app.py
```

## Creer le .exe (Windows)

Double-clic sur `build.bat`, ou :

```
pip install pyinstaller
pyinstaller --onefile --windowed --name DiscordManager app.py
```

Le fichier est dans `dist\DiscordManager.exe`.

Sinon, l'onglet **Actions** de GitHub construit le .exe automatiquement
(workflow `Build Discord Manager`) : ouvre le dernier run et telecharge
l'artifact `DiscordManager`.

## Attention

- Ne partage jamais ton token : il donne l'acces complet a ton compte.
- Automatiser un compte utilisateur (self-bot) est contraire aux conditions
  d'utilisation de Discord et peut entrainer un ban du compte.
