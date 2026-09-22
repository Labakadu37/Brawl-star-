#!/usr/bin/env python3
"""
JZS Brawl - DEX binary patcher
Replaces encoded library name in DEX files (same-length swap).
No Java/baksmali needed.
"""
import struct
import hashlib
import sys
import os

OLD_ENCODED = b"dcf098a54a7b0f38c0fbd0"   # hernbrawlv2
NEW_ENCODED = b"deef99944a7b0f38c0fbd0"   # jzs_brawlv2

BRANDING_OLD = [
    (b"HernBrawl", b"JZSxBrawl"),
    (b"Hern Brawl", b"JZS  Brawl"),
]

def adler32(data):
    a, b = 1, 0
    for byte in data:
        a = (a + byte) % 65521
        b = (b + a) % 65521
    return (b << 16) | a

def patch_dex(filepath):
    with open(filepath, "rb") as f:
        data = bytearray(f.read())

    if data[:4] != b"dex\n":
        return False, "not a DEX file"

    count = 0

    pos = 0
    while True:
        idx = data.find(OLD_ENCODED, pos)
        if idx < 0:
            break
        data[idx:idx + len(OLD_ENCODED)] = NEW_ENCODED
        count += 1
        pos = idx + len(NEW_ENCODED)

    for old_brand, new_brand in BRANDING_OLD:
        if len(old_brand) != len(new_brand):
            continue
        pos = 0
        while True:
            idx = data.find(old_brand, pos)
            if idx < 0:
                break
            data[idx:idx + len(old_brand)] = new_brand
            count += 1
            pos = idx + len(new_brand)

    if count == 0:
        return False, "no matches"

    sig = hashlib.sha1(bytes(data[32:])).digest()
    data[12:32] = sig

    checksum = adler32(bytes(data[12:]))
    struct.pack_into("<I", data, 8, checksum)

    with open(filepath, "wb") as f:
        f.write(data)

    return True, f"{count} replacements"

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 patch_dex.py <file.dex> [file2.dex ...]")
        sys.exit(1)

    for path in sys.argv[1:]:
        if not os.path.exists(path):
            print(f"  SKIP {path}: not found")
            continue
        ok, msg = patch_dex(path)
        if ok:
            print(f"  OK   {os.path.basename(path)}: {msg}")
        else:
            print(f"  SKIP {os.path.basename(path)}: {msg}")
