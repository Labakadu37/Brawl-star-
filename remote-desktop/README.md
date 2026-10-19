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
as l'**autorisation explicite** du propriétaire. Le client reste
**découvrable** : même réduit, il garde une **icône dans la zone de
notification** (comme TeamViewer / RustDesk) pour que la personne devant le
PC puisse toujours le voir et l'arrêter, et une **clé partagée** empêche
n'importe qui de se connecter. C'est volontaire et à conserver : un outil
de contrôle **totalement invisible** que l'utilisateur ne peut ni voir ni
arrêter est un logiciel espion, illégal dans la plupart des pays.

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

## Accès sans surveillance (icône tray + démarrage auto)

Pour contrôler un PC **sans avoir à ouvrir l'app** à chaque fois :

1. Sur le PC à contrôler, lance le client une fois. Il démarre **réduit
   dans la zone de notification** (petite icône en bas à droite) — pas de
   grande fenêtre. Clic droit sur l'icône pour l'ouvrir, le déconnecter ou
   le quitter.
2. Active **« Démarrer automatiquement avec Windows »** (case dans la
   fenêtre, ou clic droit sur l'icône tray). Il se relancera tout seul à
   chaque ouverture de session, réduit dans le tray.

En ligne de commande :

```bash
RemoteClient.exe --install-autostart     # activer le démarrage auto
RemoteClient.exe --remove-autostart      # le retirer
RemoteClient.exe --window                # forcer la grande fenêtre
```

> L'entrée de démarrage est **visible** dans *Gestionnaire des tâches →
> Démarrage* et l'icône tray reste affichée : c'est un accès non
> surveillé, pas une installation cachée.

## Contrôler un PC hors du réseau (Internet)

La découverte automatique ne marche que sur un **même réseau**. Pour
atteindre un PC ailleurs (ex. depuis l'hôpital), le plus simple et le plus
sûr est **[Tailscale](https://tailscale.com)** (gratuit, chiffré, aucune
config de routeur) :

1. Installe Tailscale sur **les deux PC** et connecte-les au même compte.
   Chaque PC reçoit une adresse stable (ex. `100.x.y.z`).
2. Sur le PC client, note cette adresse (ou son nom Tailscale).
3. Dans l'app admin, clique **« ➕ PC distant »** et entre cette adresse
   **une seule fois**. Le PC apparaît alors dans la liste comme un PC local
   — double-clique pour le contrôler. L'adresse est mémorisée.

> Évite d'exposer directement le port sur Internet (redirection de port) :
> le flux n'est pas chiffré. Tailscale/VPN chiffre tout de bout en bout.

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
