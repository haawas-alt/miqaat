#!/usr/bin/env python3
"""Fail on capture errors, missing/extra PNGs, mislabeled orientation or malformed PNGs.

This validates capture integrity only. It deliberately does not claim visual fidelity.
"""
import argparse
import hashlib
import json
from pathlib import Path
import struct

THEMES = ("miqaat", "kiswah", "celestial_meridian", "prayer_gallery")
SECTIONS = ("location", "times", "azaan", "iqamah", "hijri", "display", "test", "health", "privacy", "about")
POSTURES = ("takbir", "standing", "bowing", "rising", "prostrating", "sitting", "tashahhud", "salam")

def expected_names(device, group):
    cases = []
    def add(page, scales=(100,), languages=("en",)):
        cases.extend((page, scale, lang) for scale in scales for lang in languages)
    if group == "a":
        add("home", (100, 130, 200), ("en", "ur"))
        for page in ("state-ready", "state-warning"):
            add(page, (100, 200))
    elif group == "b":
        add("settings", (100, 130, 200))
        add("settings", languages=("ur",))
        for section in SECTIONS:
            add("settings-" + section)
        for section in ("display", "test"):
            add("settings-" + section, (200,))
        for section in ("display", "location"):
            add("settings-" + section, languages=("ur",))
    elif group == "c":
        for page in ("timetable", "qibla", "adhkar-morning", "adhkar-evening", "friday", "learn"):
            add(page, (100, 200))
        for page in ("timetable", "qibla", "adhkar-morning", "friday", "learn"):
            add(page, languages=("ur",))
    elif group == "d":
        for page in ("azaan", "dua", "hadith", "iqamah"):
            add(page, (100, 200))
        add("dua", languages=("ur",))
        for posture in POSTURES:
            add("lesson-" + posture)
    else:
        raise ValueError("Unknown group: " + group)
    return {f"{page}__{theme}__{device}__{scale}{'__ur' if lang == 'ur' else ''}.png" for theme in THEMES for page, scale, lang in cases}

def validate(root, device, group):
    files = list(root.glob("*.png"))
    actual = {p.name for p in files}
    expected = expected_names(device, group)
    errors = []
    if list(root.glob("_errors*")):
        errors.append("Capture error file present")
    if actual != expected:
        errors.append(f"Missing: {sorted(expected - actual)}; unexpected: {sorted(actual - expected)}")
    rows = []
    for p in sorted(files):
        data = p.read_bytes()
        if len(data) < 33 or data[:8] != b"\x89PNG\r\n\x1a\n" or data[12:16] != b"IHDR" or b"IEND" not in data[-12:]:
            errors.append("Malformed PNG: " + p.name)
            continue
        w, h = struct.unpack(">II", data[16:24])
        if w == 0 or h == 0:
            errors.append("Empty PNG: " + p.name)
        if not p.name.startswith("state-") and (w > h) != device.endswith("landscape"):
            errors.append(f"Wrong orientation: {p.name} {w}x{h}")
        rows.append({"file": p.name, "width": w, "height": h, "sha256": hashlib.sha256(data).hexdigest()})
    manifest = {"device": device, "group": group, "count": len(files), "expected_count": len(expected), "files": rows, "errors": errors}
    (root / "MANIFEST.json").write_text(json.dumps(manifest, indent=2) + "\n")
    if errors:
        raise ValueError("\n".join(errors))
    return manifest

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    parser.add_argument("device")
    parser.add_argument("group", choices=list("abcd"))
    args = parser.parse_args()
    result = validate(args.directory, args.device, args.group)
    print(f"Verified {result['count']} captures for {args.device}/{args.group}; no visual-fidelity assertion")
