# Discord Account Manager

Petite app de bureau (Electron → `.exe` Windows) pour gérer **ton propre**
compte Discord sans passer par le client officiel. Usage personnel.

> **Avertissement** — Piloter un compte via son token utilisateur est un
> « self-bot », contraire aux CGU de Discord, et peut entraîner le
> bannissement du compte. Utilise cet outil en connaissance de cause, sur
> ton compte uniquement. Le token n'est envoyé qu'à l'API officielle de
> Discord et, si tu coches « Se souvenir de moi », stocké **chiffré**
> localement (trousseau de l'OS via `safeStorage`).

## Fonctions

- **Login par token** — connexion avec le token du compte.
- **Aperçu du compte** — avatar, bannière, nom, ID, email, téléphone, 2FA, Nitro.
- **Statut** — en ligne / absent / ne pas déranger / invisible.
- **Statut personnalisé** — texte + emoji.
- **Activité « Streaming »** — badge violet (URL Twitch/YouTube requise).
- **Profil** — nom affiché global, bio, couleur d'accent.
- **Sécurité** — changement de mot de passe (exige le mot de passe actuel).

Le statut et les activités passent par le **gateway** WebSocket de Discord
(la présence ne peut pas être définie en REST) ; le profil et le mot de
passe passent par l'API REST v10.

## Lancer en dev

```bash
cd app
npm install
npm start
```

## Générer le `.exe` Windows

### Via GitHub Actions (recommandé ici)

Le workflow `.github/workflows/build.yml` compile sur `windows-latest` à
chaque push touchant `app/`. Récupère l'installeur et la version portable
dans l'artefact **discord-account-manager-windows** de l'exécution.

Déclenchement manuel : onglet **Actions → Build Windows EXE → Run workflow**.

### En local (sur Windows)

```bash
cd app
npm install
npm run dist   # → app/dist/*.exe (installeur NSIS + portable)
```

## Où est le token ?

Barre latérale Discord → **Se déconnecter n'est pas nécessaire**. Le token
se récupère depuis le client Discord ou le web (DevTools). Ne le partage
jamais : quiconque l'a a un accès complet au compte.
