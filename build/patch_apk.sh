#!/bin/bash
# Patch JZS_Glass.apk with key-auth-enabled classes4.dex
# Usage: ./patch_apk.sh <path_to_original_apk> [output_apk]

set -e

APK_IN="${1:?Usage: $0 <original.apk> [output.apk]}"
APK_OUT="${2:-$(dirname "$APK_IN")/JZS_Glass_patched.apk}"
DEX="$(dirname "$0")/../mod/dex/classes4_patched.dex"

if [ ! -f "$APK_IN" ]; then
    echo "Error: APK not found: $APK_IN"
    exit 1
fi

if [ ! -f "$DEX" ]; then
    echo "Error: Patched DEX not found: $DEX"
    exit 1
fi

echo "[1/4] Copying APK..."
cp "$APK_IN" "$APK_OUT"

echo "[2/4] Removing old signature..."
zip -qd "$APK_OUT" "META-INF/*.SF" "META-INF/*.RSA" "META-INF/*.DSA" "META-INF/MANIFEST.MF" 2>/dev/null || true

echo "[3/4] Replacing classes4.dex..."
cd "$(dirname "$DEX")"
cp classes4_patched.dex classes4.dex
zip -j "$APK_OUT" classes4.dex
rm classes4.dex

echo "[4/4] Signing APK..."
KEYSTORE="/tmp/jzs-sign.keystore"
if [ ! -f "$KEYSTORE" ]; then
    keytool -genkeypair -v -keystore "$KEYSTORE" -alias jzs \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass jzsbrawl123 -keypass jzsbrawl123 \
        -dname "CN=JZS, OU=Brawl, O=JZS, L=FR, ST=FR, C=FR" 2>/dev/null
fi

jarsigner -sigalg SHA256withRSA -digestalg SHA-256 \
    -keystore "$KEYSTORE" -storepass jzsbrawl123 -keypass jzsbrawl123 \
    "$APK_OUT" jzs

echo ""
echo "Done! Patched APK: $APK_OUT"
echo "Install: adb install $APK_OUT"
