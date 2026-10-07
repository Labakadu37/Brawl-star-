#!/usr/bin/env python3
"""Replace the launcher icon of a decoded APK with the JZS logo.

The APK is decoded with ``apktool d --no-res``, so resources stay compiled
and ``res/`` holds the original files at their original paths. We therefore
never touch resources.arsc or binary XML: we ask ``aapt2`` which files back
the icon and overwrite only those bitmaps, keeping each file's size and
format so the resource table stays valid.

Handled icon shapes:
  * legacy bitmap icons (``android:icon``)           -> full logo
  * legacy round icons (``android:roundIcon``)       -> logo cut to a circle
  * adaptive icons (``<adaptive-icon>`` foreground)  -> logo in the visible zone
  * adaptive icons (``<adaptive-icon>`` background)  -> blurred logo
  * monochrome layer (Android 13 themed icons)       -> left untouched

Anything unexpected (missing aapt2, vector layers, unreadable files) is
reported and skipped: the build keeps the original icon rather than fail.

Usage:
    replace_icon.py <original.apk> <apktool work dir> <logo.png>
"""

import os
import re
import shutil
import subprocess
import sys

try:
    from PIL import Image, ImageDraw, ImageFilter
except ImportError:  # pragma: no cover - depends on host
    Image = None

BITMAP_EXTS = (".png", ".webp", ".jpg", ".jpeg")
# Adaptive icons are 108dp; launchers show at most the centre 72dp.
ADAPTIVE_VISIBLE = 72.0 / 108.0

REF_RE = re.compile(r"=@(0x[0-9a-fA-F]{8})")
ICON_ATTR_RE = re.compile(r":(icon|roundIcon)\(0x[0-9a-fA-F]+\)=@(0x[0-9a-fA-F]{8})")
RES_HEADER_RE = re.compile(r"^\s*resource (0x[0-9a-fA-F]{8}) ")
RES_FILE_RE = re.compile(r"\(file\) (\S+)")
ELEMENT_RE = re.compile(r"^(\s*)E: (\S+)")
ADAPTIVE_LAYERS = {"foreground": "fg", "background": "bg", "monochrome": "mono"}
# When one bitmap is reached through several roles, the most visible wins
# (e.g. a foreground also reused as the monochrome layer).
ROLE_PRIORITY = {"fg": 0, "legacy": 1, "round": 2, "bg": 3, "mono": 4}


def warn(msg):
    print("    [icon] warning: " + msg, file=sys.stderr)


def find_aapt2():
    exe = shutil.which("aapt2")
    if exe:
        return exe
    sdk = os.environ.get("ANDROID_SDK") or os.environ.get("ANDROID_HOME")
    if sdk and os.path.isdir(os.path.join(sdk, "build-tools")):
        for ver in sorted(os.listdir(os.path.join(sdk, "build-tools")), reverse=True):
            cand = os.path.join(sdk, "build-tools", ver, "aapt2")
            if os.access(cand, os.X_OK):
                return cand
    return None


def run(cmd):
    return subprocess.run(cmd, check=True, capture_output=True, text=True).stdout


def resource_files(aapt2, apk):
    """Map resource id (int) -> list of file paths inside the APK."""
    out = run([aapt2, "dump", "resources", apk])
    files, current = {}, None
    for line in out.splitlines():
        m = RES_HEADER_RE.match(line)
        if m:
            current = int(m.group(1), 16)
            continue
        m = RES_FILE_RE.search(line)
        if m and current is not None:
            files.setdefault(current, []).append(m.group(1))
    return files


def xml_refs(aapt2, apk, path, role):
    """Yield (res id, role) for every reference in a compiled XML file.

    Inside an <adaptive-icon>, references below <foreground>/<background>/
    <monochrome> take that layer's role; everything else inherits ``role``.
    """
    out = run([aapt2, "dump", "xmltree", "--file", path, apk])
    stack = []  # (indent, role)
    for line in out.splitlines():
        m = ELEMENT_RE.match(line)
        if m:
            indent = len(m.group(1))
            while stack and stack[-1][0] >= indent:
                stack.pop()
            parent = stack[-1][1] if stack else role
            stack.append((indent, ADAPTIVE_LAYERS.get(m.group(2), parent)))
            continue
        for ref in REF_RE.findall(line):
            yield int(ref, 16), (stack[-1][1] if stack else role)


def collect_targets(aapt2, apk):
    """Return {apk path: role} for every bitmap that makes up the icon."""
    manifest = run([aapt2, "dump", "xmltree", "--file", "AndroidManifest.xml", apk])
    roots = []
    for attr, ref in ICON_ATTR_RE.findall(manifest):
        roots.append((int(ref, 16), "round" if attr == "roundIcon" else "legacy"))
    if not roots:
        warn("no android:icon found in the manifest")
        return {}

    res_files = resource_files(aapt2, apk)
    targets, seen = {}, set()
    todo = list(roots)
    while todo:
        res_id, role = todo.pop()
        if (res_id, role) in seen:
            continue
        seen.add((res_id, role))
        for path in res_files.get(res_id, []):
            low = path.lower()
            if low.endswith(".9.png"):
                warn("skipping nine-patch " + path)
            elif low.endswith(BITMAP_EXTS):
                if path not in targets or ROLE_PRIORITY[role] < ROLE_PRIORITY[targets[path]]:
                    targets[path] = role
            elif low.endswith(".xml"):
                for ref in xml_refs(aapt2, apk, path, role):
                    todo.append(ref)
            else:
                warn("skipping unknown icon file " + path)
    return targets


def cover(logo, size):
    """Scale + centre-crop ``logo`` to exactly ``size``."""
    w, h = size
    scale = max(w / logo.width, h / logo.height)
    img = logo.resize((max(1, round(logo.width * scale)), max(1, round(logo.height * scale))),
                      Image.LANCZOS)
    left = (img.width - w) // 2
    top = (img.height - h) // 2
    return img.crop((left, top, left + w, top + h))


def render(logo, size, role):
    w, h = size
    if role == "legacy":
        return cover(logo, size)
    if role == "round":
        img = cover(logo, size)
        mask = Image.new("L", size, 0)
        ImageDraw.Draw(mask).ellipse((0, 0, w - 1, h - 1), fill=255)
        img.putalpha(mask)
        return img
    if role == "fg":
        inner = (max(1, round(w * ADAPTIVE_VISIBLE)), max(1, round(h * ADAPTIVE_VISIBLE)))
        canvas = Image.new("RGBA", size, (0, 0, 0, 0))
        canvas.paste(cover(logo, inner), ((w - inner[0]) // 2, (h - inner[1]) // 2))
        return canvas
    if role == "bg":
        return cover(logo, size).filter(ImageFilter.GaussianBlur(max(w, h) / 20))
    return None


def save_like(img, original, dest):
    fmt = original.format or "PNG"
    if fmt == "JPEG":
        img = img.convert("RGB")
    kwargs = {}
    if fmt == "WEBP":
        kwargs["lossless"] = True
    img.save(dest, format=fmt, **kwargs)


def main():
    if len(sys.argv) != 4:
        print(__doc__, file=sys.stderr)
        return 1
    apk, work, logo_path = sys.argv[1:]

    if Image is None:
        warn("Pillow is not installed (pip install pillow) - keeping original icon")
        return 0
    aapt2 = find_aapt2()
    if not aapt2:
        warn("aapt2 not found in PATH or $ANDROID_SDK/build-tools - keeping original icon")
        return 0
    try:
        logo = Image.open(logo_path).convert("RGBA")
        targets = collect_targets(aapt2, apk)
    except (OSError, subprocess.CalledProcessError) as exc:
        warn("could not inspect APK icon (%s) - keeping original icon" % exc)
        return 0

    replaced = 0
    for path, role in sorted(targets.items()):
        if role == "mono":
            continue  # themed-icon silhouette: a full-colour logo would be a solid blob
        dest = os.path.join(work, path)
        if not os.path.isfile(dest):
            warn("%s not present in work tree - skipped" % path)
            continue
        try:
            with Image.open(dest) as original:
                original.load()
                img = render(logo, original.size, role)
                if img is None:
                    continue
                save_like(img, original, dest)
        except OSError as exc:
            warn("could not rewrite %s (%s) - skipped" % (path, exc))
            continue
        print("    [icon] %-6s %s" % (role, path))
        replaced += 1

    if not replaced:
        warn("no icon bitmap replaced - keeping original icon")
    return 0


if __name__ == "__main__":
    sys.exit(main())
