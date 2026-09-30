#!/usr/bin/env python3
"""Fails if either rejected legacy background (Celestial moon/night, Prayer Gallery empty mosque) is present.

Usage: check_forbidden_assets.py <path> [<path> ...]   (a directory, an .apk or an .aab)
The rejected files are recognised by SHA-256 of their bytes, including the portrait siblings that shipped with them.
"""
import hashlib, os, sys, zipfile

FORBIDDEN = {
    "408b5a312705940d702767a1257bf727d0836b6f2f864fe596662d023dc4f4bd": "celestial moon/night landscape (rejected)",
    "0a8eb471c3b164e99d274b7b7931d93194bd098c49bc870c48029b656a1de757": "celestial moon/night portrait (rejected)",
    "784f9727d43934ae73d9b4ab5d6768f48102ad1e0a3d212c25ee393d2d3733b5": "prayer gallery empty-mosque landscape (rejected)",
    "682a01e9044694be98680620497eeccb1fc877909a5beebb63e95ef4cc9262d4": "prayer gallery empty-mosque portrait (rejected)",
}
LEGACY_NAMES = ("art_celestial_landscape.webp", "art_celestial_portrait.webp", "art_gallery_landscape.webp", "art_gallery_portrait.webp")

def scan_bytes(label, data, hits):
    h = hashlib.sha256(data).hexdigest()
    if h in FORBIDDEN: hits.append(f"{label}: {FORBIDDEN[h]}")

def scan(path, hits):
    if os.path.isdir(path):
        for root, _, files in os.walk(path):
            if "/.git" in root or "/build" in root: continue
            for f in files:
                p = os.path.join(root, f)
                if f in LEGACY_NAMES: hits.append(f"{p}: legacy file name present")
                if os.path.getsize(p) > 100 * 1024 * 1024: continue
                if f.lower().endswith((".webp", ".png", ".jpg")): scan_bytes(p, open(p, "rb").read(), hits)
    else:
        with zipfile.ZipFile(path) as z:
            n = 0
            for i in z.infolist():
                base = i.filename.rsplit("/", 1)[-1]
                if base in LEGACY_NAMES: hits.append(f"{path}!{i.filename}: legacy file name present")
                if i.filename.lower().endswith((".webp", ".png", ".jpg")):
                    n += 1; scan_bytes(f"{path}!{i.filename}", z.read(i), hits)
            print(f"scanned {n} images in {path}")

if __name__ == "__main__":
    hits = []
    for a in sys.argv[1:]: scan(a, hits)
    if hits:
        print("FORBIDDEN LEGACY ASSETS FOUND:"); [print(" -", h) for h in hits]; sys.exit(1)
    print("OK: no rejected legacy assets in", ", ".join(sys.argv[1:]))
