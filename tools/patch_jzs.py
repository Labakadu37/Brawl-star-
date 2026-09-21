#!/usr/bin/env python3
"""Patch HernBrawl APK -> JZS Brawl

Usage:
    python3 patch_jzs.py HernBrawl.apk
    -> produces jzs-brawl-v1.apk
"""
import hashlib
import struct
import sys
import os
import shutil
import zipfile
import subprocess

def patch_dex(data: bytearray, old: bytes, new: bytes) -> int:
    assert len(old) == len(new), "old and new must be same length"
    count = 0
    pos = 0
    while True:
        pos = data.find(old, pos)
        if pos == -1:
            break
        data[pos:pos+len(old)] = new
        count += 1
        pos += len(new)
    return count

def fix_dex_checksums(data: bytearray):
    # SHA-1 signature (offset 12, 20 bytes) covers bytes 32+
    sha1 = hashlib.sha1(data[32:]).digest()
    data[12:32] = sha1
    # Adler32 checksum (offset 8, 4 bytes) covers bytes 12+
    a = 1
    b = 0
    for byte in data[12:]:
        a = (a + byte) % 65521
        b = (b + a) % 65521
    checksum = (b << 16) | a
    data[8:12] = struct.pack('<I', checksum)

def patch_native_lib(data: bytearray) -> int:
    # Replace "hern_hazard_render" -> "jzsb_hazard_render" (same length 18)
    old = b"hern_hazard_render"
    new = b"jzsb_hazard_render"
    count = 0
    pos = 0
    while True:
        pos = data.find(old, pos)
        if pos == -1:
            break
        data[pos:pos+len(old)] = new
        count += 1
        pos += len(new)
    return count

def main():
    if len(sys.argv) < 2:
        print("Usage: python3 patch_jzs.py <HernBrawl.apk>")
        sys.exit(1)

    apk_path = sys.argv[1]
    work_dir = "jzs_work"
    out_apk = "jzs-brawl-v1.apk"

    if os.path.exists(work_dir):
        shutil.rmtree(work_dir)
    os.makedirs(work_dir)

    print(f"[1/5] Extracting {apk_path}...")
    with zipfile.ZipFile(apk_path, 'r') as zin:
        zin.extractall(work_dir)

    # Patch classes4.dex (mod menu branding)
    dex4_path = os.path.join(work_dir, "classes4.dex")
    if os.path.exists(dex4_path):
        print("[2/5] Patching classes4.dex: HB1. -> JZS.")
        with open(dex4_path, "rb") as f:
            dex4 = bytearray(f.read())
        n = patch_dex(dex4, b"HB1.", b"JZS.")
        fix_dex_checksums(dex4)
        with open(dex4_path, "wb") as f:
            f.write(dex4)
        print(f"    {n} replacement(s) done, checksums fixed")
    else:
        print("[2/5] WARNING: classes4.dex not found, skipping")

    # Patch native lib
    so_path = os.path.join(work_dir, "lib/arm64-v8a/libhernbrawlv2.so")
    if os.path.exists(so_path):
        print("[3/5] Patching libhernbrawlv2.so branding...")
        with open(so_path, "rb") as f:
            so_data = bytearray(f.read())
        n = patch_native_lib(so_data)
        with open(so_path, "wb") as f:
            f.write(so_data)
        print(f"    {n} replacement(s) done")
    else:
        print("[3/5] WARNING: libhernbrawlv2.so not found, skipping")

    # Repack APK
    print(f"[4/5] Repacking -> {out_apk}...")
    if os.path.exists(out_apk):
        os.remove(out_apk)
    with zipfile.ZipFile(out_apk, 'w', zipfile.ZIP_DEFLATED) as zout:
        for root, dirs, files in os.walk(work_dir):
            for file in files:
                file_path = os.path.join(root, file)
                arcname = os.path.relpath(file_path, work_dir)
                zout.write(file_path, arcname)
    print(f"    Done: {os.path.getsize(out_apk) / 1024 / 1024:.1f} MB")

    # Sign APK
    print("[5/5] Signing APK...")
    print("    Run these commands to sign:")
    print(f"    keytool -genkey -v -keystore jzs.keystore -alias jzs -keyalg RSA -keysize 2048 -validity 10000")
    print(f"    apksigner sign --ks jzs.keystore --ks-key-alias jzs {out_apk}")
    print(f"    zipalign -f 4 {out_apk} {out_apk.replace('.apk', '-aligned.apk')}")

    # Cleanup
    shutil.rmtree(work_dir)
    print(f"\nDone! APK: {out_apk}")
    print("N'oublie pas de signer avant d'installer.")

if __name__ == "__main__":
    main()
