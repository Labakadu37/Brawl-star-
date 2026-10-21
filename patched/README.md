# JZS Brawl - Key System Removed

## What was changed

The key verification system in `classes4.dex` has been bypassed:

- **`isUnlocked()`** now always returns `true`
- **`promptGate()`** now directly sets `unlocked = true` and opens the mod menu, without calling the API at `api.jzbrawl.fr`

No other files were modified.

## How to use

### Option 1: Replace classes4.dex in the APK manually

1. Rename `JZS_Glass_patched.apk` to `JZS_Glass_patched.zip`
2. Open with any ZIP tool (7-Zip, WinRAR, etc.)
3. Replace `classes4.dex` inside the ZIP with `classes4_patched.dex` (rename it to `classes4.dex`)
4. Rename back to `.apk`
5. Re-sign the APK with `apksigner` or `uber-apk-signer`

### Option 2: Use the patch script

```bash
python3 patch_apk.py JZS_Glass_patched.apk
```

This produces `JZS_Glass_nokey.apk` with the patched DEX.

## Re-signing

After patching, the APK signature is broken. Re-sign with:

```bash
# Using uber-apk-signer (easiest)
java -jar uber-apk-signer.jar --apks JZS_Glass_nokey.apk

# Or using apksigner
apksigner sign --ks my.keystore JZS_Glass_nokey.apk
```
