# Système de contrôle à distance (kill-switch + annonces)

## Idée

Le bot Discord (sur Termux) écrit un petit fichier `status.json` sur GitHub
(branche `status`). Le mod, dans l'APK, lit ce fichier toutes les ~60 s via
`raw.githubusercontent.com` et réagit :

- `locked: true`  → le mod menu est caché, un écran plein "Mise à jour
  disponible" s'affiche avec un bouton vers le Discord. L'état est mis en
  cache : **couper Internet ne débloque pas** (le dernier état reçu reste).
- `announce_*`    → un popup message s'affiche une seule fois par joueur.

## Fichier `status.json`

```json
{
  "locked": true,
  "title": "Mise à jour disponible",
  "body": "Texte affiché sous le titre.",
  "button": "Rejoindre le Discord",
  "url": "https://discord.gg/jzbrawl",
  "announce_id": "171...",
  "announce_title": "Titre",
  "announce_body": "Message",
  "ts": 171...
}
```

## URL lue par le mod (codée dans l'APK)

```
https://raw.githubusercontent.com/Labakadu37/Brawl-star-/status/status.json
```

Si tu changes de repo/branche, modifie `STATUS_URL` dans
`mod/src/java/com/jzs/brawl/JZSRemote.java` puis reconstruis.

## Limites (honnêtes)

- Délai de propagation : jusqu'à ~5 min (cache CDN de raw + sondage 60 s).
- Seuls les joueurs **sur le nouvel APK** sont concernés. L'ancien APK n'a
  pas ce système.
- Contournable par quelqu'un qui sait décompiler l'APK. C'est un contrôle
  côté client ; aucune version n'est "impossible à enlever".
