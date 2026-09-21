#!/bin/bash
# Build JZS Brawl APK from HernBrawl
# Usage: ./tools/build_jzs.sh HernBrawl.apk

set -e

APK="$1"
if [ -z "$APK" ]; then
    echo "Usage: ./tools/build_jzs.sh <HernBrawl.apk>"
    exit 1
fi

OUT="jzs-brawl-v1.apk"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "[1/4] Copie de l'APK..."
cp "$APK" "$OUT"

echo "[2/4] Decodage du classes4.dex patche..."
base64 -d "$SCRIPT_DIR/classes4_jzs.b64" > /tmp/classes4.dex

echo "[3/4] Remplacement dans l'APK..."
cd /tmp && zip -j "$(cd - > /dev/null && pwd)/$OUT" classes4.dex
rm /tmp/classes4.dex

echo "[4/4] Signature..."
if command -v apksigner &> /dev/null; then
    if [ ! -f jzs.keystore ]; then
        echo "    Generation de la cle..."
        keytool -genkey -v -keystore jzs.keystore -alias jzs \
            -keyalg RSA -keysize 2048 -validity 10000 \
            -dname "CN=JZS, OU=Brawl, O=JZS, L=Paris, S=IDF, C=FR" \
            -storepass jzsbrawl -keypass jzsbrawl
    fi
    apksigner sign --ks jzs.keystore --ks-key-alias jzs \
        --ks-pass pass:jzsbrawl --key-pass pass:jzsbrawl "$OUT"
    echo "    APK signe!"
else
    echo "    apksigner non trouve. Signe manuellement:"
    echo "    apksigner sign --ks jzs.keystore --ks-key-alias jzs $OUT"
fi

echo ""
echo "Done! -> $OUT"
echo "Installe avec: adb install $OUT"
