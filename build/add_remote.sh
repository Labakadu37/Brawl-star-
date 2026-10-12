#!/usr/bin/env bash
# Injecte le système de contrôle à distance (JZSRemote) dans un APK JZS Brawl,
# puis ré-aligne et signe. Nécessite : java, apktool/baksmali+smali, d8,
# zipalign/apksigner (ou uber-apk-signer). Voir docs/REMOTE.md.
#
# Usage: ./add_remote.sh <input.apk> <output.apk>
#
# Ce script documente les étapes ; adapte les chemins d'outils à ta machine.
set -euo pipefail
IN="${1:?apk d'entrée}"; OUT="${2:?apk de sortie}"
echo "1) Extraire classes4.dex depuis $IN"
echo "2) baksmali d classes4.dex -o smali4"
echo "3) Copier mod/src/smali/com/jzs/brawl/JZSRemote*.smali dans smali4/com/jzs/brawl/"
echo "4) Ajouter dans smali4/com/jzs/brawl/JZSInit.smali, après l'appel JZSModMenu->attach :"
echo "      invoke-static {p0}, Lcom/jzs/brawl/JZSRemote;->init(Landroid/app/Activity;)V"
echo "5) smali a smali4 -o classes4.dex"
echo "6) Remplacer classes4.dex dans l'APK (zip), puis zipalign -p 4 et signer."
echo
echo "Un APK déjà patché et signé est fourni (lien Gofile)."
