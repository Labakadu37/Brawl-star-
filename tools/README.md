# JZS Brawl - Outils de patch

## patch_jzs.py

Script qui transforme l'APK HernBrawl en JZS Brawl :
- Remplace le branding `HB1.` par `JZS.` dans le ModMenu (classes4.dex)
- Patch les references "hern" dans la lib native
- Recalcule les checksums DEX (SHA-1 + Adler32)
- Repack l'APK

### Usage

```bash
# 1. Lancer le patch
python3 tools/patch_jzs.py HernBrawl.apk

# 2. Generer une cle de signature (une seule fois)
keytool -genkey -v -keystore jzs.keystore -alias jzs \
  -keyalg RSA -keysize 2048 -validity 10000

# 3. Signer l'APK
apksigner sign --ks jzs.keystore --ks-key-alias jzs jzs-brawl-v1.apk

# 4. Aligner
zipalign -f 4 jzs-brawl-v1.apk jzs-brawl-v1-aligned.apk

# 5. Installer
adb install jzs-brawl-v1-aligned.apk
```

### Ce qui est modifie

| Fichier | Avant | Apres |
|---|---|---|
| classes4.dex | `HB1.` | `JZS.` |
| libhernbrawlv2.so | `hern_hazard_render` | `jzsb_hazard_render` |

### Prochaines etapes

- [ ] Modifier les couleurs du ModMenu (bleu ciel + jaune + multicolor)
- [ ] Ajouter des animations
- [ ] Renommer libhernbrawlv2.so -> libjzs.so
- [ ] Decoder les strings encodees en hex dans classes4.dex
