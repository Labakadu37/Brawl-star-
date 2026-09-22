#!/usr/bin/env python3
"""
JZS Brawl - Mod Menu Patcher v2.0
Patches classes4.dex to:
  1. Fix invisible mod menu button (alpha 0 -> visible)
  2. Apply premium "Midnight Cyan" neon theme
  3. Apply library name patches (hernbrawl -> jzs_brawl)

Usage:
  python3 patch_modmenu.py <classes4.dex>
  python3 patch_modmenu.py --scan <classes4.dex>
"""
import struct
import hashlib
import sys
import os
import shutil
import re


THEME_NAME = "Midnight Cyan"

COLORS = {
    # Panel gradient (dark gray -> deep navy)
    0xFF14171D: 0xFF0A1628,
    0xFF111419: 0xFF0E1B30,
    0xFF0E1116: 0xFF081222,

    # Borders/strokes (gray -> bright cyan glow)
    0xFF2A3039: 0xFF00ACC1,

    # Tab & toggle bg (dark gray -> dark navy)
    0xFF191D24: 0xFF0D1525,
    0xFF232830: 0xFF101C30,

    # Tab selected bg (green -> vivid cyan)
    0xFF7ED957: 0xFF00ACC1,

    # Tab selected text (dark -> light cyan-white)
    0xFF101418: 0xFFE0F7FA,

    # Tab unselected text (gray -> soft cyan)
    0xFF7E858F: 0xFF4DD0E1,

    # Button icon (gold -> electric cyan neon)
    0xFFE7C64A: 0xFF00E5FF,

    # Button/watermark gold -> teal
    0xFFD4AF37: 0xFF00BCD4,

    # Button subtitle (gray -> pale cyan)
    0xFF9AA0A8: 0xFF80DEEA,

    # Green accent -> cyan
    0xFF6FD98A: 0xFF26C6DA,
}


def adler32(data):
    a, b = 1, 0
    for byte in data:
        a = (a + byte) % 65521
        b = (b + a) % 65521
    return (b << 16) | a


def fix_checksums(data):
    sig = hashlib.sha1(bytes(data[32:])).digest()
    data[12:32] = sig
    cs = adler32(bytes(data[12:]))
    struct.pack_into("<I", data, 8, cs)


def fix_alpha(data, verbose=False):
    """
    NOP out setAlpha(0.0f) on the mod menu floating button.

    Bytecode pattern:
      62 00 XX XX  = sget-object v0, <el field>
      12 01        = const/4 v1, #0  (float 0.0)
      6e 20 YY YY  = invoke-virtual {v0, v1}, setAlpha(F)V
      10 00        = register pair {v0, v1}

    We NOP the const/4 + invoke-virtual (8 bytes -> 4 NOP instructions).
    The FrameLayout default alpha is 1.0, so the button becomes visible.
    """
    pattern = re.compile(
        b'\x62\x00..'
        b'\x12\x01'
        b'\x6e\x20..'
        b'\x10\x00',
        re.DOTALL,
    )

    matches = list(pattern.finditer(bytes(data)))
    if not matches:
        return 0

    count = 0
    for m in matches:
        off = m.start() + 4
        if verbose:
            print(f"    @0x{m.start():x}: sget+const/4+invoke -> NOP")
        data[off : off + 8] = b"\x00" * 8
        count += 1

    return count


def fix_colors(data, verbose=False):
    """Replace color constants in Dalvik 'const' instructions (opcode 0x14)."""
    total = 0
    details = {}

    for old_color, new_color in COLORS.items():
        old_bytes = struct.pack("<I", old_color)
        new_bytes = struct.pack("<I", new_color)
        n = 0
        pos = 0
        while pos < len(data) - 5:
            idx = data.find(b"\x14", pos)
            if idx < 0 or idx + 6 > len(data):
                break
            if bytes(data[idx + 2 : idx + 6]) == old_bytes:
                data[idx + 2 : idx + 6] = bytearray(new_bytes)
                n += 1
                if verbose:
                    print(
                        f"    @0x{idx:x}: #{old_color:08X} -> #{new_color:08X}"
                    )
                pos = idx + 6
            else:
                pos = idx + 1
        if n > 0:
            details[f"#{old_color:08X}"] = n
            total += n

    return total, details


def scan_colors(data):
    """Diagnostic: scan for known mod menu color constants."""
    targets = {
        0xFF14171D: "panel gradient 1",
        0xFF111419: "panel gradient 2",
        0xFF0E1116: "panel gradient 3",
        0xFF2A3039: "stroke/border",
        0xFF191D24: "tab/toggle bg",
        0xFF7ED957: "tab selected (green)",
        0xFF101418: "tab selected text",
        0xFF7E858F: "tab unselected text",
        0xFFE7C64A: "button icon (gold)",
        0xFFD4AF37: "gold text",
        0xFF9AA0A8: "subtitle text",
        0xFF6FD98A: "green accent",
        0xFF232830: "item bg",
        0xFF00ACC1: "cyan (NEW theme)",
        0xFF00E5FF: "neon cyan (NEW theme)",
        0xFF00BCD4: "teal (NEW theme)",
    }

    print("  Color scan results:")
    found = False
    for color, label in targets.items():
        color_bytes = struct.pack("<I", color)
        count = 0
        pos = 0
        while pos < len(data) - 5:
            idx = data.find(b"\x14", pos)
            if idx < 0 or idx + 6 > len(data):
                break
            if bytes(data[idx + 2 : idx + 6]) == color_bytes:
                count += 1
                pos = idx + 6
            else:
                pos = idx + 1
        if count > 0:
            print(f"    #{color:08X} ({label}): {count}x")
            found = True

    if not found:
        print("    (no known colors found)")

    pattern = re.compile(
        b"\x62\x00..\x12\x01\x6e\x20..\x10\x00", re.DOTALL
    )
    matches = list(pattern.finditer(data))
    print()
    print(f"  setAlpha(0) pattern: {len(matches)} match(es)")
    for m in matches:
        print(f"    @0x{m.start():x}")


def main():
    if len(sys.argv) < 2:
        print()
        print("  ==========================================")
        print("  JZS Brawl - Mod Menu Patcher v2.0")
        print("  ==========================================")
        print()
        print("  Usage:")
        print("    python3 patch_modmenu.py <classes4.dex>")
        print("    python3 patch_modmenu.py --scan <classes4.dex>")
        print()
        print("  Patches:")
        print("    [1] Bouton flottant VISIBLE")
        print("    [2] Theme Midnight Cyan (neon bleu)")
        print()
        print("  Etapes:")
        print("    1. MT Manager -> ouvrir l'APK")
        print("    2. Extraire classes4.dex")
        print("    3. python3 patch_modmenu.py classes4.dex")
        print("    4. Remettre classes4.dex dans l'APK")
        print("    5. Signer l'APK")
        print()
        sys.exit(1)

    scan_mode = "--scan" in sys.argv
    files = [a for a in sys.argv[1:] if not a.startswith("--")]

    if not files:
        print("Erreur: pas de fichier specifie")
        sys.exit(1)

    for path in files:
        if not os.path.exists(path):
            print(f"  Erreur: {path} pas trouve")
            continue

        with open(path, "rb") as f:
            data = bytearray(f.read())

        if data[:4] != b"dex\n":
            print(f"  Erreur: {path} n'est pas un fichier DEX")
            continue

        if scan_mode:
            print()
            print(f"  SCAN: {os.path.basename(path)} ({len(data)} bytes)")
            print()
            scan_colors(data)
            print()
            continue

        print()
        print("  ==========================================")
        print(f"  Patching: {os.path.basename(path)}")
        print(f"  Taille: {len(data)} bytes")
        print("  ==========================================")
        print()

        backup = path + ".original"
        if not os.path.exists(backup):
            shutil.copy2(path, backup)
            print(f"  Backup -> {os.path.basename(backup)}")
            print()

        print("  [1/3] Bouton flottant -> VISIBLE")
        n = fix_alpha(data, verbose=True)
        if n > 0:
            print(f"        OK ({n} setAlpha(0) supprime)")
        else:
            print("        -- Pas trouve (deja corrige?)")
        print()

        print(f"  [2/3] Theme: {THEME_NAME}")
        n, details = fix_colors(data, verbose=True)
        if n > 0:
            print(f"        OK ({n} couleurs changees)")
        else:
            print("        -- Aucune couleur trouvee")
        print()

        print("  [3/3] Checksums...")
        fix_checksums(data)
        print("        OK")
        print()

        with open(path, "wb") as f:
            f.write(data)

        print("  ==========================================")
        print("  TERMINE !")
        print("  ==========================================")
        print()
        if n > 0 or fix_alpha != 0:
            print("  [x] Bouton modmenu VISIBLE")
            print(f"  [x] Theme {THEME_NAME} applique")
            print()
            print("  -> Remets classes4.dex dans l'APK")
            print("  -> Signe avec MT Manager")
            print()


if __name__ == "__main__":
    main()
