# JZCord

> Anciennement « Vercors ». Le nom technique du module (`dev.vercors.xposed`)
> ne change pas, pour que les mises à jour s'installent par-dessus.

Un mod client pour **l'app Discord officielle sur Android**, dans l'esprit de
[Vencord](https://vencord.dev) (qui ne marche que sur PC) et des mods mobiles
comme Revenge / Bunny : on garde l'app Discord normale et on y ajoute des
plugins.

> ⚠️ Modifier le client Discord est contraire à ses conditions d'utilisation.
> Ça reste rare en pratique, mais ton compte peut être sanctionné : utilise-le
> à tes risques.

## Comment ça marche

```
┌────────────── app Discord (React Native + Hermes) ──────────────┐
│                                                                  │
│  1. preload.js      globalThis.__VERCORS_LOADER__ = {...}        │
│  2. vercors.js      ← le mod (ce dossier : mod/)                 │
│  3. bundle Discord  ← chargé normalement, mais déjà intercepté   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
        ▲ injectés par le module Xposed (xposed/) avant le bundle Discord
```

- **`xposed/`** : module Xposed en Kotlin (l'APK). Il s'accroche aux méthodes
  de React Native qui chargent le bundle JS (`CatalystInstanceImpl` en mode
  bridge et `ReactInstance` en mode bridgeless) et fait évaluer `vercors.js`
  juste avant Discord.
- **`mod/`** : le mod en TypeScript, compilé en un seul `vercors.js` :
  - `lib/metro.ts` intercepte `__d` (le système de modules Metro) pour voir
    chaque module Discord au moment où il se charge (`findByProps`,
    `waitFor`…).
  - `lib/patcher.ts` : patchs `before` / `after` / `instead`, annulables.
  - `lib/plugins.ts` : plugins activables, chacun nettoyé à l'arrêt.
  - `lib/commands.ts` : commandes slash locales, ajoutées aux commandes
    intégrées de Discord.

## Plugins inclus

| Plugin | Par défaut | Ce qu'il fait |
|---|---|---|
| MessageLogger | ❌ | Garde les messages supprimés (surlignés en rouge) et les anciennes versions des messages modifiés (✏️), jusqu'au redémarrage |
| ShowHiddenChannels | ❌ | Affiche les salons auxquels tu n'as pas accès ; les ouvrir affiche juste leur nom et leur sujet |
| PlatformIndicators | ❌ | Badges PC/téléphone/navigateur/console (couleur du statut) dans le chat, les profils, et à la place du rond de statut sur l'avatar (option) |
| FakeNitro | ❌ | Emojis animés et d'autres serveurs sans Nitro (envoyés en image) |
| ShowMeYourName | ❌ | « Surnom (pseudo) » dans le chat |
| NoReplyMention | ❌ | Les réponses ne mentionnent plus la personne par défaut |
| AlwaysAnimate | ❌ | Emojis, GIF et avatars animés toujours animés |
| SilentTyping | ❌ | Les autres ne voient plus « est en train d'écrire… » |
| NoTrack | ✅ | Bloque les requêtes de télémétrie (`/science`, `/metrics`…) |
| ClearURLs | ✅ | Retire `utm_*`, `fbclid`, `si`, les paramètres TikTok… des liens que tu envoies |
| MoreCommands | ❌ | `/lenny`, `/reverse`, `/mock`, `/owo`, `/upper`, `/lower`, `/flip`, `/roll`, `/timestamp` |
| Experiments | ❌ | Débloque le menu développeur / expériences de Discord |
| DevTools | ❌ | Ajoute `/eval` pour explorer Discord (`jzcord.metro.findByProps(...)`) |

**Réglages** : *Paramètres Discord → section **JZCord** → Plugins JZCord*.
Les interrupteurs s'appliquent tout de suite. La page a aussi un **journal**
(afficher / copier) pour diagnostiquer un plugin qui ne marche pas.

Les commandes `/jzcord` restent disponibles :

- `/jzcord action:liste` : liste des plugins (🟢 = actif)
- `/jzcord action:activer plugin:SilentTyping`
- `/jzcord action:chercher plugin:typing` : modules Discord dont une propriété contient ce mot
- `/jzcord action:infos` : version, loader, modules chargés

## Récupérer l'APK

**Sans ordinateur** : à chaque push, GitHub Actions compile tout
(workflow *JZCord (Discord mobile)*). Ouvre l'onglet **Actions** du dépôt,
le dernier run, puis télécharge l'artefact `jzcord` : il contient
`app-release.apk` (le module) et `vercors.js`.

**Sur PC** :

```bash
cd discord-client/mod && npm install && npm test   # → dist/jzcord.js
cd ../xposed && ./gradlew assembleRelease          # → app/build/outputs/apk/release/app-release.apk
```

(il faut le SDK Android : `ANDROID_HOME` ou `sdk.dir` dans `xposed/local.properties`)

## Installer sur le téléphone

### Sans root : LSPatch (le plus courant)

1. Installe l'APK du module (`app-release.apk`).
2. Installe un **LSPatch** à jour (le fork maintenu de JingMatrix, ou NPatch).
3. Dans LSPatch : *Gérer → + → Sélectionner une app installée → Discord*,
   mode **Intégré** (« Integrated »), *Embed modules* → coche **JZCord**,
   puis *Start Patch*.
4. Désinstalle Discord, puis installe l'APK patché que LSPatch a produit
   (la signature change, d'où la désinstallation). Reconnecte-toi.

### Avec root : LSPosed

Installe l'APK du module, active **JZCord** dans LSPosed avec la portée
`com.discord`, puis force l'arrêt de Discord et relance-le.

### L'icône JZCord sur l'écran d'accueil

Le module installe une icône **JZCord** (le logo du mod) qui ouvre Discord.
Android ne permet pas de changer le nom et l'icône de Discord lui-même sans
modifier son APK : masque l'icône Discord d'origine (Samsung : appui long sur
l'écran d'accueil → Paramètres → Masquer les applications) et garde JZCord.

### Vérifier

Tape `/jzcord` dans un salon : si la commande apparaît, le mod est chargé.
Sinon, regarde les logs :

```bash
adb logcat | grep -E "JZCord|LSPosed"
```

## Mettre à jour le mod sans réinstaller l'APK

Le module peut télécharger `vercors.js` depuis une URL au lieu d'utiliser la
copie embarquée. Crée
`/data/data/com.discord/files/jzcord/loader.json` (nécessite root) :

```json
{ "bundleUrl": "https://exemple.com/jzcord.js" }
```

Le téléchargement se fait en arrière-plan et sert au lancement suivant.

## Écrire un plugin

Crée `mod/src/plugins/monPlugin.ts` puis ajoute-le dans `plugins/index.ts` :

```ts
import { filters } from "../lib/metro";
import { definePlugin } from "../lib/plugins";

export default definePlugin({
    name: "MonPlugin",
    description: "Ajoute 👋 à la fin de chaque message",
    authors: ["moi"],
    start({ waitFor, before, registerCommand }) {
        // Attend le module Discord qui a ces propriétés, puis le patche.
        waitFor(filters.byProps("sendMessage", "editMessage"), messageActions => {
            before(messageActions, "sendMessage", args => {
                args[1].content += " 👋";
            });
        });
        registerCommand({
            name: "salut",
            description: "Envoie un salut",
            execute: () => "Salut tout le monde !", // une chaîne = message envoyé
        });
    },
});
```

Tout ce qu'un plugin fait via son contexte (patchs, `waitFor`, commandes) est
annulé automatiquement quand on le désactive. Pour trouver les modules
Discord, active DevTools et utilise par exemple
`/eval code:Object.keys(jzcord.metro.findByProps("sendMessage"))`.

## Limites connues

- Les noms de modules / propriétés Discord changent avec les mises à jour :
  un plugin peut cesser de marcher (il ne fait alors rien, sans planter l'app).
- Pas encore d'écran de réglages dans l'UI Discord : tout passe par `/jzcord`.
- Testé ici avec un faux runtime Metro (`npm test`) ; le chargement réel
  dépend de la version de Discord installée. Si `/jzcord` n'apparaît pas,
  envoie les logs `adb logcat`.
