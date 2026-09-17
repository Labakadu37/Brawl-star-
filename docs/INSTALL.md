# Install the modded APK

Brawl Stars ships as a **split APK bundle** (`.xapk`): a base APK plus
config splits (`config.armeabi_v7a`, `config.mdpi`, `config.en`) plus a
1.7 GB asset pack (`install_time_asset_pack`). All of them must be
installed together for the game to run.

Our build modifies only the base APK. The other four are re-used
unchanged.

## Option A — SAI (Split APKs Installer, phone)

1. Copy the whole XAPK folder onto your device (or repack it as a `.zip`
   and rename to `.xapk`).
2. Replace `com.supercell.brawlstars.apk` inside the zip with the freshly
   built `out/jzs-brawl-v1.apk` (keep the file name unchanged so SAI
   still recognizes it as the base).
3. Open the `.xapk` with SAI and hit install.

## Option B — adb install-multiple (PC)

```bash
adb install-multiple \
    out/jzs-brawl-v1.apk \
    xapk/config.armeabi_v7a.apk \
    xapk/config.en.apk \
    xapk/config.mdpi.apk \
    xapk/install_time_asset_pack.apk
```

Uninstall any existing Brawl Stars first — you can't upgrade over a
Play-signed install because our debug keystore signature won't match.

## Sanity check

```bash
adb logcat -s JZS
```

You should see the three-line boot log documented at the end of
`BUILD.md`, and the badge `JZS V1 (lastest)` in the top-left of the game
screen.

## Troubleshooting

- **App crashes at boot with `ClassNotFoundException: com.jzs.brawl.JZSInit`**
  → your extra smali got dropped. Re-run the build and grep for
  `com/jzs/brawl` in the reassembled APK: `unzip -l out/jzs-brawl-v1.apk |
  grep classes`. You should see at least `classes3.dex`.
- **Install fails with `INSTALL_FAILED_UPDATE_INCOMPATIBLE`** → you have
  the Play Store version installed. Uninstall it first.
- **`libjz.so` warning in logcat** → non-fatal. The overlay is pure Java;
  it will still show up. Native lib only supplies a version string over
  JNI.
