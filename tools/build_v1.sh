#!/bin/bash
# ============================================
# JZS Brawl V1 - Build Script pour Termux
# ============================================
# Usage: bash build_v1.sh /chemin/vers/hernbrawl.apk
# Output: ~/storage/shared/JZS_Brawl/JZS_Brawl_V1.apk
# ============================================

set -e

APK_INPUT="$1"
if [ -z "$APK_INPUT" ]; then
    echo "Usage: bash build_v1.sh /chemin/vers/hernbrawl.apk"
    echo ""
    echo "Exemple:"
    echo "  bash build_v1.sh ~/storage/shared/Download/hernbrawl.apk"
    exit 1
fi

if [ ! -f "$APK_INPUT" ]; then
    echo "ERREUR: Fichier pas trouve: $APK_INPUT"
    exit 1
fi

echo "=========================================="
echo "  JZS Brawl V1 Builder"
echo "=========================================="

# Dossier de travail
WORK="$HOME/jzs_build"
OUT="$HOME/storage/shared/JZS_Brawl"
REPO="$HOME/Brawl-star-"
SMALI_VER="2.5.2"
SMALI_JAR="$WORK/smali.jar"
BAKSMALI_JAR="$WORK/baksmali.jar"

rm -rf "$WORK"
mkdir -p "$WORK" "$OUT"

# ==========================================
# Etape 1: Installer les dependances
# ==========================================
echo ""
echo "[1/7] Installation des dependances..."
pkg install -y openjdk-17 zip unzip clang 2>/dev/null || true

# ==========================================
# Etape 2: Telecharger baksmali/smali
# ==========================================
echo "[2/7] Telechargement baksmali/smali..."
if [ ! -f "$SMALI_JAR" ]; then
    curl -L -o "$SMALI_JAR" \
        "https://github.com/JesusFreke/smali/releases/download/v${SMALI_VER}/smali-${SMALI_VER}.jar" 2>/dev/null
fi
if [ ! -f "$BAKSMALI_JAR" ]; then
    curl -L -o "$BAKSMALI_JAR" \
        "https://github.com/JesusFreke/smali/releases/download/v${SMALI_VER}/baksmali-${SMALI_VER}.jar" 2>/dev/null
fi

if [ ! -f "$SMALI_JAR" ] || [ ! -f "$BAKSMALI_JAR" ]; then
    echo "ERREUR: Impossible de telecharger smali/baksmali"
    exit 1
fi

# ==========================================
# Etape 3: Extraire l'APK
# ==========================================
echo "[3/7] Extraction de l'APK..."
mkdir -p "$WORK/apk"
cd "$WORK/apk"
unzip -q -o "$APK_INPUT"

# Supprimer la signature originale
rm -rf META-INF

# ==========================================
# Etape 4: Compiler la lib native
# ==========================================
echo "[4/7] Compilation de libjzbrawlv2.so..."
cd "$REPO/mod/native/jni"

# Pull les derniers changements
cd "$REPO"
git pull origin claude/bsd-bsdbrawl-wie3if 2>/dev/null || true
cd "$REPO/mod/native/jni"

clang -shared -fPIC -O2 -fvisibility=hidden \
    -ffunction-sections -fdata-sections \
    -Wl,--gc-sections \
    -o "$WORK/libjzbrawlv2.so" \
    jzbrawlv2.c -lm -ldl

echo "   -> libjzbrawlv2.so compile ($(wc -c < "$WORK/libjzbrawlv2.so") bytes)"

# ==========================================
# Etape 5: Patcher les DEX (changer nom lib)
# ==========================================
echo "[5/7] Patch des fichiers DEX..."

# Ancien encodage (hernbrawlv2) et nouveau (jzbrawlv2)
OLD_ENC="dcf098a54a7b0f38c0fbd0"
NEW_ENC="deef88b9497e02399e"

# Patcher chaque DEX qui contient le nom de la lib
cd "$WORK/apk"
for DEX in classes*.dex; do
    echo "   Analyse $DEX..."
    mkdir -p "$WORK/smali_$DEX"

    # Desassembler
    java -jar "$BAKSMALI_JAR" d "$DEX" -o "$WORK/smali_$DEX" 2>/dev/null

    # Chercher et remplacer l'ancien encodage par le nouveau
    FOUND=$(grep -rl "$OLD_ENC" "$WORK/smali_$DEX" 2>/dev/null || true)
    if [ -n "$FOUND" ]; then
        echo "   -> Trouve dans $DEX, patch en cours..."
        for F in $FOUND; do
            sed -i "s/$OLD_ENC/$NEW_ENC/g" "$F"
            echo "      Patche: $(basename $F)"
        done

        # Reassembler le DEX
        java -jar "$SMALI_JAR" a "$WORK/smali_$DEX" -o "$WORK/apk/$DEX" 2>/dev/null
        echo "   -> $DEX reassemble"
    fi
done

# Patcher aussi le titre du mod menu (HernBrawl -> JZS Brawl)
# Encodage de "HernBrawl" et "JZS Brawl V1"
echo "   Patch branding JZS..."
for DEX in classes*.dex; do
    mkdir -p "$WORK/smali2_$DEX"
    java -jar "$BAKSMALI_JAR" d "$DEX" -o "$WORK/smali2_$DEX" 2>/dev/null

    # Chercher "Hern" dans les strings pour le branding
    BRAND=$(grep -rl "HernBrawl\|hernbrawl\|Hern Brawl\|hern_hazard" "$WORK/smali2_$DEX" 2>/dev/null || true)
    if [ -n "$BRAND" ]; then
        for F in $BRAND; do
            # Changer les noms visibles (pas les noms de fonctions natives)
            sed -i 's/HernBrawl/JZS Brawl/g' "$F"
            sed -i 's/Hern Brawl/JZS Brawl/g' "$F"
            sed -i 's/hernbrawl/jzsbrawl/g' "$F"
        done
        java -jar "$SMALI_JAR" a "$WORK/smali2_$DEX" -o "$WORK/apk/$DEX" 2>/dev/null
    fi
done

# ==========================================
# Etape 6: Remplacer la lib native
# ==========================================
echo "[6/7] Remplacement de la lib native..."
cd "$WORK/apk"

# Mettre notre lib avec le nom que le smali attend maintenant (jzbrawlv2)
cp "$WORK/libjzbrawlv2.so" "lib/arm64-v8a/libjzbrawlv2.so"

# Garder aussi l'ancien nom au cas ou d'autres endroits le chargent
cp "$WORK/libjzbrawlv2.so" "lib/arm64-v8a/libhernbrawlv2.so"

echo "   -> Libs copiees dans lib/arm64-v8a/"

# ==========================================
# Etape 7: Repackager et signer l'APK
# ==========================================
echo "[7/7] Creation de l'APK final..."
cd "$WORK/apk"

# Creer le ZIP/APK
zip -q -r "$WORK/jzs_unsigned.apk" . -x '*.DS_Store'

# Generer une cle de signature si pas deja faite
KEYSTORE="$HOME/.jzs_keystore.jks"
if [ ! -f "$KEYSTORE" ]; then
    echo "   Generation de la cle de signature..."
    keytool -genkey -v -keystore "$KEYSTORE" \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -alias jzs -storepass jzsbrawl -keypass jzsbrawl \
        -dname "CN=JZS,OU=Brawl,O=JZS,L=FR,S=FR,C=FR" 2>/dev/null
fi

# Signer avec jarsigner
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
    -keystore "$KEYSTORE" -storepass jzsbrawl -keypass jzsbrawl \
    "$WORK/jzs_unsigned.apk" jzs 2>/dev/null

# Copier le resultat
cp "$WORK/jzs_unsigned.apk" "$OUT/JZS_Brawl_V1.apk"

# Nettoyer
rm -rf "$WORK/smali_"* "$WORK/smali2_"* "$WORK/apk"

echo ""
echo "=========================================="
echo "  JZS Brawl V1 PRET !"
echo "=========================================="
echo ""
echo "  APK: $OUT/JZS_Brawl_V1.apk"
echo ""
echo "  -> Va dans Mes Fichiers > JZS_Brawl"
echo "  -> Installe JZS_Brawl_V1.apk"
echo "  -> Desinstalle l'ancien HernBrawl avant"
echo ""
echo "  Features V1:"
echo "    - ESP (box sur ennemis)"
echo "    - Aimbot auto-target"
echo "    - Entity tracking"
echo "    - Spinner detection"
echo "=========================================="
