# JZS Brawl V2

Pure JZS mod for **Brawl Stars v69.252** — zero HernBrawl base code.
Full-screen floating orb opens a glass-morphism cyan neon mod menu with
**18 toggles across 3 tabs** (Combat, Visual, Misc), a draggable launcher,
and a native engine (`libjzs_brawlv2.so`) that drives aimbot, ESP, dodge
and all the gameplay hacks via memory offsets into `libg.so`.

## Features

### Combat
- Aimbot
- Spinner / Spin bot
- Dodge bullets
- Trigger bot
- Gadget hack
- Super ready alert

### Visual
- Aura wall (ESP)
- Hazard ESP (mines, gas, bombs)
- Box ESP
- Tracers
- HP bars
- Name tags
- Range circle

### Misc
- Stealth mode
- FPS unlock
- Watermark
- Debug diag
- Fast reload
- Spoof name

## Layout

```
mod/
├── src/java/com/jzs/brawl/
│   ├── JZSInit.java        Application lifecycle + loadLibrary
│   ├── JzsConfig.java      flags.txt / name.txt / fps.txt I/O
│   └── JzsModMenu.java     Draggable orb + tabbed glass panel
└── native/jni/
    ├── jzbrawlv2.c         Full engine (aimbot, ESP, hooks)
    ├── Android.mk          builds libjzs_brawlv2.so
    └── Application.mk      arm64-v8a only

build/
├── build_mod.sh            full pipeline (decode → compile → inject → sign)
└── inject_overlay.py       smali injector for GameApp.onCreate
```

## Quick build

```bash
export ANDROID_SDK=$HOME/Android/Sdk
export ANDROID_NDK=$ANDROID_SDK/ndk/26.1.10909125
export PATH="$ANDROID_SDK/build-tools/34.0.0:$ANDROID_NDK:$PATH"

./build/build_mod.sh path/to/com.supercell.brawlstars.apk
# → out/jzs-brawl-v2.apk
```

## Mod menu UI

Draggable **JZS orb** on the HUD (top-left by default). Tap to open the
glass-morphism panel; tap outside to close. Three tabs, one neon cyan
accent, animated toggles with glow.

Config persists to `/data/data/com.supercell.brawlstars/files/revenge/flags.txt`
(one flag name per line) and is reloaded live by `libjzs_brawlv2.so`.

## Legal

Client-side mod for personal use. Does not touch network protocol code,
does not confer gameplay advantage in competitive matchmaking, and does
not redistribute Supercell's assets. Build it yourself.
