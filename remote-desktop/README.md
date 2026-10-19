# Remote Desktop (local)

Un petit outil « à la AnyDesk » pour contrôler **tes propres PC** sur ton
**réseau local**, sans taper de code : l'app admin détecte automatiquement
les PC qui ont lancé l'app client.

Deux applications :

| App        | Où l'installer                     | Rôle |
|------------|------------------------------------|------|
| **client** | le PC que tu veux **contrôler**    | affiche l'écran partagé, exécute souris + clavier reçus |
| **admin**  | le PC depuis lequel tu **contrôles** | détecte les clients, affiche l'écran distant, envoie souris + clavier |

```
remote-desktop/
├── common/      protocole réseau + table clavier (partagés)
├── client/      app à installer sur le PC à contrôler
└── admin/       app à installer sur le PC qui contrôle
```

## Utilisation consentie uniquement

Cet outil est fait pour **tes machines** ou des machines pour lesquelles tu
as l'**autorisation explicite** du propriétaire. Il est **volontairement
visible** : l'app client affiche en permanence une fenêtre « Partage
d'écran » avec un bouton pour couper, et une **clé partagée** empêche
n'importe qui sur le réseau de se connecter. Ne le transforme pas en outil
caché : contrôler l'ordinateur de quelqu'un à son insu est illégal dans la
plupart des pays.

## Installation

Sur **chaque** PC, installe Python 3.9+ puis les dépendances de l'app
concernée.

### PC à contrôler (client)

```bash
cd remote-desktop/client
pip install -r requirements.txt
python client_app.py
```

> Linux : `pynput`/`mss` utilisent X11. Sous Wayland, lance une session
> Xorg pour que le contrôle du curseur fonctionne.
> macOS : autorise l'app dans *Réglages → Confidentialité → Enregistrement
> de l'écran* **et** *Accessibilité*.

### PC qui contrôle (admin)

```bash
cd remote-desktop/admin
pip install -r requirements.txt
python admin_app.py
```

## Créer des exécutables `.exe` (Windows)

Pour obtenir `RemoteClient.exe` et `RemoteAdmin.exe` autonomes (aucun
Python requis pour les lancer ensuite) :

1. Sur un PC **Windows** avec Python 3.9+ installé
   ([python.org](https://www.python.org/downloads/), coche *Add Python to
   PATH*).
2. Ouvre le dossier `remote-desktop\packaging` et **double-clique sur
   `build_windows.bat`**.
3. Récupère les deux fichiers dans `remote-desktop\packaging\dist\` :
   - `RemoteClient.exe` → à copier sur le PC à contrôler
   - `RemoteAdmin.exe`  → à lancer sur le PC qui contrôle

> PyInstaller ne fait pas de compilation croisée : un `.exe` Windows doit
> être construit **sur Windows**. Sur Linux/macOS, le même script
> (`python build.py`) produit l'exécutable natif de cet OS.

Les `.exe` acceptent les mêmes options que les scripts, par ex. :
`RemoteClient.exe --name "PC-Salon" --token ma-cle --ask`.

## Déroulé

1. Sur le PC à contrôler : lance `client_app.py`. Une fenêtre rouge
   « Partage d'écran » s'ouvre et le PC devient détectable.
2. Sur l'autre PC : lance `admin_app.py`. Clique **🔍 Rechercher** : le PC
   client apparaît dans la liste.
3. Double-clique dessus (ou sélectionne puis **Se connecter**).
4. L'écran distant s'affiche. Bouge la souris, clique, tape : tout est
   renvoyé au client. Décoche **Contrôle actif** pour juste regarder.

## Sécurité / configuration

- **Clé partagée (token).** Par défaut `change-moi`. Mets la même valeur
  des deux côtés, par exemple :
  ```bash
  # client ET admin
  export RDESK_TOKEN="ma-cle-secrete"
  ```
  ou `--token ma-cle-secrete`. Sans la bonne clé, l'admin est refusé.
- **Confirmation manuelle.** Lance le client avec `--ask` pour qu'il
  demande « Autoriser ? » à chaque connexion au lieu d'accepter
  automatiquement.
- **Pare-feu.** Autorise les ports **UDP 47899** (découverte) et
  **TCP 47900** (contrôle) sur le PC client. Change-les avec `--port`.
- **Même réseau.** La découverte automatique marche sur un même LAN/Wi-Fi.
  D'un réseau à l'autre, utilise l'« adresse manuelle » dans l'admin (et
  ouvre/redirige le port TCP toi-même).

## Options

```bash
# Client
python client_app.py --name "PC-Salon" --port 47900 --token ma-cle --ask \
    --fps 20 --quality 85 --max-width 0

# Admin
python admin_app.py --token ma-cle --name "Mon-Portable"
```

- `--fps` : fluidité (plus haut = plus fluide, plus de bande passante).
- `--quality` : netteté JPEG 1–95 (défaut 85).
- `--max-width` : `0` = résolution native (défaut). Mets p. ex. `1280`
  pour réduire la bande passante sur un réseau lent.

## Contrôle (souris / clavier)

Le contrôle total est actif : clic, double-clic (ouvrir une page/appli),
clic droit, **glisser-déposer**, molette et clavier (y compris les
raccourcis comme Ctrl+C). Décoche **Contrôle actif** dans l'admin pour
juste regarder sans toucher.

> Le client se met automatiquement en mode *DPI-aware* sous Windows : sans
> ça, avec une mise à l'échelle d'affichage à 125 %/150 %, les clics
> tombaient à côté des icônes. Si une fenêtre lancée **en administrateur**
> ne réagit pas aux clics, lance aussi le client *en administrateur* (clic
> droit → Exécuter en tant qu'administrateur) — Windows interdit d'injecter
> des entrées dans une fenêtre plus privilégiée.

## Limites connues

- Partage le **moniteur principal** uniquement.
- JPEG ~20 i/s en résolution native, pensé pour un réseau local (pas de
  son, pas de transfert de fichiers).
- Trafic **non chiffré** : à n'utiliser que sur un réseau de confiance.
  Pour traverser Internet, fais-le passer dans un **tunnel SSH** ou un VPN.
