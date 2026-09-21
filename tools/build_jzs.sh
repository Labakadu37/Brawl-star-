#!/bin/bash
# Build JZS Brawl APK from HernBrawl
# Usage: ./tools/build_jzs.sh HernBrawl.apk

set -e

APK="$1"
if [ -z "$APK" ]; then
    echo "Usage: ./tools/build_jzs.sh <HernBrawl.apk>"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "[1/2] Patch de l'APK avec patch_jzs.py..."
python3 "$SCRIPT_DIR/patch_jzs.py" "$APK"

OUT="jzs-brawl-v1.apk"

echo "[2/2] Signature..."
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
    if command -v zipalign &> /dev/null; then
        zipalign -f 4 "$OUT" "${OUT%.apk}-aligned.apk"
        mv "${OUT%.apk}-aligned.apk" "$OUT"
        echo "    APK signe et aligne!"
    else
        echo "    APK signe! (zipalign non trouve, fais-le manuellement)"
    fi
else
    echo "    apksigner non trouve. Signe manuellement:"
    echo "    keytool -genkey -v -keystore jzs.keystore -alias jzs -keyalg RSA -keysize 2048 -validity 10000"
    echo "    apksigner sign --ks jzs.keystore --ks-key-alias jzs $OUT"
    echo "    zipalign -f 4 $OUT jzs-brawl-v1-aligned.apk"
fi

echo ""
echo "Done! -> $OUT"
echo "Installe avec: adb install $OUT"
