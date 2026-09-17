#!/usr/bin/env bash
# JZS Brawl - build pipeline
#
# Produces a signed APK of Brawl Stars with the JZS V1 overlay injected.
#
# Requirements (install these before running):
#   - JDK 17+                    (javac, jar)
#   - Android SDK build-tools    (d8, zipalign, apksigner)
#   - Android NDK                (ndk-build) for libjz.so
#   - apktool 2.9+               (https://apktool.org)
#   - baksmali/smali 3+          (https://github.com/JesusFreke/smali)
#   - python3
#
# Usage:
#     export ANDROID_SDK=/path/to/Android/Sdk
#     export ANDROID_NDK=/path/to/Android/Sdk/ndk/26.x
#     ./build/build_mod.sh path/to/com.supercell.brawlstars.apk

set -euo pipefail

INPUT_APK="${1:-}"
if [[ -z "$INPUT_APK" || ! -f "$INPUT_APK" ]]; then
    echo "usage: $0 <path/to/com.supercell.brawlstars.apk>" >&2
    exit 1
fi

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/out"
WORK="$OUT/work"
CLASSES_OUT="$OUT/classes"
SMALI_OUT="$OUT/mod-smali"

rm -rf "$OUT"
mkdir -p "$OUT" "$CLASSES_OUT" "$SMALI_OUT"

echo "[1/7] Decoding APK with apktool ..."
apktool d --no-res "$INPUT_APK" -o "$WORK" -f

echo "[2/7] Compiling Java overlay classes ..."
find "$ROOT/mod/src/java" -name "*.java" > "$OUT/srcs.txt"
javac -source 8 -target 8 -d "$CLASSES_OUT" @"$OUT/srcs.txt"

echo "[3/7] Converting classes to DEX and baksmali'ing ..."
d8 --output "$OUT/jzs.dex.zip" $(find "$CLASSES_OUT" -name "*.class")
unzip -o "$OUT/jzs.dex.zip" classes.dex -d "$OUT" >/dev/null
baksmali d "$OUT/classes.dex" -o "$SMALI_OUT"

echo "[4/7] Copying mod smali into APK work tree ..."
mkdir -p "$WORK/smali/com/jzs/brawl"
cp -R "$SMALI_OUT/com/jzs/brawl/." "$WORK/smali/com/jzs/brawl/"

echo "[5/7] Injecting invoke-static in GameApp.onCreate ..."
python3 "$ROOT/build/inject_overlay.py" "$WORK/smali/com/supercell/titan/GameApp.smali"

echo "[6/7] Building libjz.so for both ABIs and dropping into lib/ ..."
pushd "$ROOT/mod/native" >/dev/null
ndk-build NDK_PROJECT_PATH=. APP_BUILD_SCRIPT=jni/Android.mk NDK_APPLICATION_MK=jni/Application.mk
popd >/dev/null
for abi in armeabi-v7a arm64-v8a; do
    if [[ -f "$ROOT/mod/native/libs/$abi/libjz.so" ]]; then
        mkdir -p "$WORK/lib/$abi"
        cp "$ROOT/mod/native/libs/$abi/libjz.so" "$WORK/lib/$abi/libjz.so"
    fi
done

echo "[7/7] Rebuilding, aligning and signing APK ..."
apktool b "$WORK" -o "$OUT/jzs-brawl-unsigned.apk"
zipalign -p -f 4 "$OUT/jzs-brawl-unsigned.apk" "$OUT/jzs-brawl-aligned.apk"

if [[ ! -f "$OUT/jzs.keystore" ]]; then
    keytool -genkey -v -keystore "$OUT/jzs.keystore" -alias jzs \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass jzsjzs -keypass jzsjzs \
        -dname "CN=JZS Brawl, O=JZS, C=FR"
fi

apksigner sign --ks "$OUT/jzs.keystore" --ks-pass pass:jzsjzs \
    --key-pass pass:jzsjzs --v1-signing-enabled true --v2-signing-enabled true \
    --out "$OUT/jzs-brawl-v1.apk" "$OUT/jzs-brawl-aligned.apk"

echo
echo "OK - built: $OUT/jzs-brawl-v1.apk"
