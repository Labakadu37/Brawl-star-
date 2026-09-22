#!/bin/bash
# ============================================
# JZS Brawl V1 - Build Script pour Termux
# PAS BESOIN DE JAVA - utilise Python
# ============================================
# Usage: bash build_v1.sh /chemin/vers/hernbrawl.apk
# Output: ~/storage/shared/JZS_Brawl/JZS_Brawl_V1.apk
# ============================================

set -e

APK_INPUT="$1"
if [ -z "$APK_INPUT" ]; then
    echo ""
    echo "Usage: bash build_v1.sh /chemin/vers/hernbrawl.apk"
    echo ""
    echo "Exemple:"
    echo "  bash build_v1.sh ~/storage/shared/Download/hernbrawl.apk"
    echo ""
    exit 1
fi

if [ ! -f "$APK_INPUT" ]; then
    echo "ERREUR: Fichier pas trouve: $APK_INPUT"
    exit 1
fi

echo ""
echo "=========================================="
echo "  JZS Brawl V1 Builder"
echo "=========================================="
echo ""

WORK="$HOME/jzs_build"
OUT="$HOME/storage/shared/JZS_Brawl"
REPO="$HOME/Brawl-star-"

rm -rf "$WORK"
mkdir -p "$WORK" "$OUT"

# ==========================================
# Etape 1: Verifier les outils
# ==========================================
echo "[1/6] Verification des outils..."
for cmd in python3 clang zip unzip keytool; do
    if ! command -v $cmd &>/dev/null; then
        echo "  Installation de $cmd..."
        case $cmd in
            python3) pkg install -y python;;
            clang)   pkg install -y clang;;
            zip)     pkg install -y zip;;
            unzip)   pkg install -y unzip;;
            keytool) pkg install -y openjdk-17 2>/dev/null || true;;
        esac
    fi
done
echo "  OK"

# ==========================================
# Etape 2: Compiler la lib native
# ==========================================
echo "[2/6] Compilation de libjzs_brawlv2.so..."

cd "$REPO"
git pull origin claude/bsd-bsdbrawl-wie3if 2>/dev/null || true

cd "$REPO/mod/native/jni"
clang -shared -fPIC -O2 -fvisibility=hidden \
    -ffunction-sections -fdata-sections \
    -Wl,--gc-sections \
    -o "$WORK/libjzs_brawlv2.so" \
    jzbrawlv2.c -lm -ldl

SIZE=$(wc -c < "$WORK/libjzs_brawlv2.so")
echo "  -> libjzs_brawlv2.so compile ($SIZE bytes)"

# ==========================================
# Etape 3: Extraire l'APK
# ==========================================
echo "[3/6] Extraction de l'APK..."
mkdir -p "$WORK/apk"
cd "$WORK/apk"
unzip -q -o "$APK_INPUT"
rm -rf META-INF
echo "  OK"

# ==========================================
# Etape 4: Patcher les DEX (Python, pas Java)
# ==========================================
echo "[4/6] Patch des fichiers DEX..."

cd "$WORK/apk"
python3 "$REPO/tools/patch_dex.py" classes*.dex

echo "  Lib: hernbrawlv2 -> jzs_brawlv2"

# ==========================================
# Etape 5: Remplacer la lib native
# ==========================================
echo "[5/6] Remplacement de la lib native..."
cd "$WORK/apk"

mkdir -p lib/arm64-v8a

# Notre lib avec le nouveau nom
cp "$WORK/libjzs_brawlv2.so" "lib/arm64-v8a/libjzs_brawlv2.so"

# Copie aussi sous l'ancien nom (backup au cas ou)
cp "$WORK/libjzs_brawlv2.so" "lib/arm64-v8a/libhernbrawlv2.so"

# Supprimer armeabi-v7a si present (on est arm64 only)
rm -rf lib/armeabi-v7a 2>/dev/null || true

echo "  OK"

# ==========================================
# Etape 6: Repackager et signer
# ==========================================
echo "[6/6] Creation de l'APK..."
cd "$WORK/apk"
zip -q -r "$WORK/jzs_build.apk" .

# Signer l'APK
KEYSTORE="$HOME/.jzs_keystore.jks"
if command -v keytool &>/dev/null && command -v jarsigner &>/dev/null; then
    if [ ! -f "$KEYSTORE" ]; then
        echo "  Generation de la cle..."
        keytool -genkey -v -keystore "$KEYSTORE" \
            -keyalg RSA -keysize 2048 -validity 10000 \
            -alias jzs -storepass jzsbrawl -keypass jzsbrawl \
            -dname "CN=JZS,OU=Brawl,O=JZS,L=FR,S=FR,C=FR" 2>/dev/null
    fi
    jarsigner -sigalg SHA256withRSA -digestalg SHA-256 \
        -keystore "$KEYSTORE" -storepass jzsbrawl -keypass jzsbrawl \
        "$WORK/jzs_build.apk" jzs 2>/dev/null
    echo "  -> APK signe"
else
    echo "  -> APK NON signe (signe-le dans MT Manager)"
fi

cp "$WORK/jzs_build.apk" "$OUT/JZS_Brawl_V1.apk"

# Copier aussi la lib seule
cp "$WORK/libjzs_brawlv2.so" "$OUT/"

# Nettoyer
rm -rf "$WORK"

echo ""
echo "=========================================="
echo "  JZS Brawl V1 PRET !"
echo "=========================================="
echo ""
echo "  APK -> JZS_Brawl/JZS_Brawl_V1.apk"
echo "  LIB -> JZS_Brawl/libjzs_brawlv2.so"
echo ""
echo "  Si l'APK n'est PAS signe:"
echo "    -> Ouvre-le dans MT Manager"
echo "    -> Menu > Signer"
echo ""
echo "  IMPORTANT: Desinstalle HernBrawl avant !"
echo ""
echo "  Features V1:"
echo "    [x] ESP (box ennemis)"
echo "    [x] Aimbot auto-target"
echo "    [x] Entity tracking"
echo "    [x] Spinner detection"
echo "    [x] Trajectory prediction"
echo "=========================================="
