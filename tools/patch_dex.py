#!/usr/bin/env python3
"""
JZS Brawl - DEX binary patcher
Replaces library name and branding in DEX files.
Supports: XOR-encoded hex strings AND plain text strings.
Same-length swap only (no offset recalculation needed).
"""
import struct
import hashlib
import sys
import os

def xor_encode(text):
    out = ""
    for i, b in enumerate(text.encode("utf-8")):
        key = ((i * 0x1f) + 0x11) & 0xff
        key ^= 0xa5
        out += format(b ^ key, "02x")
    return out

OLD_LIB = "hernbrawlv2"
NEW_LIB = "jzs_brawlv2"

OLD_ENCODED = xor_encode(OLD_LIB).encode()
NEW_ENCODED = xor_encode(NEW_LIB).encode()

REPLACEMENTS = [
    # XOR-encoded lib name (same length: 22 hex chars each)
    (OLD_ENCODED, NEW_ENCODED),
    # Plain text lib name (same length: 11 chars each)
    (OLD_LIB.encode(), NEW_LIB.encode()),
    # Same-length branding swaps
    (b"HernBrawl", b"JZS Brawl"),
    (b"hernbrawl", b"jzsbrawl\x00"),
]

def adler32(data):
    a, b = 1, 0
    for byte in data:
        a = (a + byte) % 65521
        b = (b + a) % 65521
    return (b << 16) | a

def patch_dex(filepath, verbose=False):
    with open(filepath, "rb") as f:
        data = bytearray(f.read())

    if data[:4] != b"dex\n":
        return False, "not a DEX file"

    count = 0
    details = []

    for old, new in REPLACEMENTS:
        if len(old) != len(new):
            if verbose:
                details.append(f"  WARN: skip {old!r}->{new!r} (length mismatch {len(old)}!={len(new)})")
            continue
        pos = 0
        while True:
            idx = data.find(old, pos)
            if idx < 0:
                break
            data[idx:idx + len(old)] = new
            count += 1
            details.append(f"  @0x{idx:x}: {old!r} -> {new!r}")
            pos = idx + len(new)

    if count == 0:
        return False, "no matches"

    sig = hashlib.sha1(bytes(data[32:])).digest()
    data[12:32] = sig

    checksum = adler32(bytes(data[12:]))
    struct.pack_into("<I", data, 8, checksum)

    with open(filepath, "wb") as f:
        f.write(data)

    msg = f"{count} replacements"
    if verbose and details:
        msg += "\n" + "\n".join(details)
    return True, msg

def scan_dex(filepath):
    """Scan a DEX file for any hern-related strings (diagnostic)."""
    with open(filepath, "rb") as f:
        data = f.read()

    targets = [b"hern", b"Hern", b"HERN", b"brawlv2", b"brawlv", OLD_ENCODED]
    finds = []
    for t in targets:
        pos = 0
        while True:
            idx = data.find(t, pos)
            if idx < 0:
                break
            context = data[max(0,idx-4):idx+len(t)+8]
            finds.append(f"  @0x{idx:x}: found {t!r} ctx={context!r}")
            pos = idx + len(t)
    return finds

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 patch_dex.py [--scan] <file.dex> [file2.dex ...]")
        sys.exit(1)

    scan_mode = "--scan" in sys.argv
    files = [a for a in sys.argv[1:] if not a.startswith("--")]

    for path in files:
        if not os.path.exists(path):
            print(f"  SKIP {path}: not found")
            continue

        if scan_mode:
            print(f"  SCAN {os.path.basename(path)}:")
            finds = scan_dex(path)
            if finds:
                for f in finds:
                    print(f)
            else:
                print("    (nothing found)")
        else:
            ok, msg = patch_dex(path, verbose=True)
            if ok:
                print(f"  OK   {os.path.basename(path)}: {msg}")
            else:
                print(f"  SKIP {os.path.basename(path)}: {msg}")
