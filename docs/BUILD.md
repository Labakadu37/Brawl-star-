# Build pipeline

`build/build_mod.sh` runs seven steps in order. Each is trivially
inspectable; nothing is magic.

## 1. Decode the APK

```bash
apktool d --no-res com.supercell.brawlstars.apk -o out/work -f
```

Produces `out/work/smali/**` (including `com/supercell/titan/GameApp.smali`)
and `out/work/lib/**`. We pass `--no-res` because we don't need to touch
Supercell's resources, only their code.

## 2. Compile our Java

```bash
javac -source 8 -target 8 -d out/classes \
    mod/src/java/com/jzs/brawl/JZSOverlay.java \
    mod/src/java/com/jzs/brawl/JZSInit.java
```

Two classes, no dependencies beyond `android.jar` (which `javac` pulls in
via `--boot-class-path` if you export `ANDROID_JAR`; on desktop you can
skip that — `d8` will complain but succeed since we only reference public
framework symbols).

## 3. DEX + baksmali

```bash
d8 --output out/jzs.dex.zip out/classes/com/jzs/brawl/*.class
baksmali d out/classes.dex -o out/mod-smali
```

`d8` is deterministic; `baksmali` produces smali that apktool will
re-assemble into the final `classes3.dex`.

## 4. Merge our smali into the APK tree

```bash
cp -R out/mod-smali/com/jzs/brawl/. out/work/smali/com/jzs/brawl/
```

## 5. Inject the invoke-static

`build/inject_overlay.py` finds

```
.method public onCreate(Landroid/os/Bundle;)V
    ...
    return-void
.end method
```

in `out/work/smali/com/supercell/titan/GameApp.smali` and inserts

```
invoke-static {p0}, Lcom/jzs/brawl/JZSInit;->install(Landroid/app/Activity;)V
```

immediately before the final `return-void`. Idempotent.

## 6. Build libjz.so

```bash
ndk-build NDK_PROJECT_PATH=mod/native \
    APP_BUILD_SCRIPT=mod/native/jni/Android.mk \
    NDK_APPLICATION_MK=mod/native/jni/Application.mk
cp mod/native/libs/armeabi-v7a/libjz.so out/work/lib/armeabi-v7a/
cp mod/native/libs/arm64-v8a/libjz.so   out/work/lib/arm64-v8a/
```

Native lib is loaded on demand by `JZSInit`; if it is missing the mod
still works (overlay is pure Java), you just lose the `nativeVersion()`
JNI export.

## 7. Reassemble, align, sign

```bash
apktool b out/work -o out/jzs-brawl-unsigned.apk
zipalign -p -f 4 out/jzs-brawl-unsigned.apk out/jzs-brawl-aligned.apk
apksigner sign --ks out/jzs.keystore --ks-pass pass:jzsjzs \
    --out out/jzs-brawl-v1.apk out/jzs-brawl-aligned.apk
```

A fresh debug keystore is generated the first time. It stays under
`out/jzs.keystore` and is reused on subsequent builds so the signature
doesn't churn between installs.

## Verifying

After install, check `logcat`:

```
adb logcat -s JZS
I JZS: JZS Brawl init - JZS V1 (lastest)
I JZS: libjz loaded - JZS Brawl JZS V1 (lastest)
I JZS: JZS Brawl overlay attached: JZS V1 (lastest)
```

If you see the three lines, the badge is on screen.
