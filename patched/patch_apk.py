#!/usr/bin/env python3
"""Replace classes4.dex inside the APK with the patched version (no key check)."""
import zipfile, sys, os

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 patch_apk.py <original.apk> [output.apk]")
        sys.exit(1)

    apk_in = sys.argv[1]
    apk_out = sys.argv[2] if len(sys.argv) > 2 else apk_in.replace('.apk', '_nokey.apk')
    dex_path = os.path.join(os.path.dirname(__file__), 'classes4_patched.dex')

    if not os.path.exists(dex_path):
        print(f"ERROR: {dex_path} not found")
        sys.exit(1)

    patched_dex = open(dex_path, 'rb').read()
    print(f"Input:  {apk_in}")
    print(f"Output: {apk_out}")
    print(f"Patched DEX: {len(patched_dex)} bytes")

    with zipfile.ZipFile(apk_in, 'r') as zin:
        with zipfile.ZipFile(apk_out, 'w') as zout:
            for item in zin.infolist():
                if item.filename == 'classes4.dex':
                    zout.writestr(item, patched_dex, compress_type=zipfile.ZIP_DEFLATED)
                    print(f"  Replaced: {item.filename}")
                else:
                    zout.writestr(item, zin.read(item.filename), compress_type=item.compress_type)

    print(f"\nDone! Output: {apk_out} ({os.path.getsize(apk_out)} bytes)")
    print("Note: You need to re-sign the APK before installing.")

if __name__ == '__main__':
    main()
