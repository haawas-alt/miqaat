#!/usr/bin/env python3
"""Check actual APK native-library ELF load alignment and ZIP placement for 16 KiB pages."""
import argparse, json, struct, zipfile
from pathlib import Path

def check(apk):
    data = apk.read_bytes()
    rows = []
    with zipfile.ZipFile(apk) as archive:
        for item in archive.infolist():
            if not item.filename.endswith(".so"):
                continue
            lib = archive.read(item)
            if lib[:4] != b"\x7fELF":
                raise ValueError("Not ELF: " + item.filename)
            endian = "<" if lib[5] == 1 else ">"
            if lib[4] == 2:
                phoff = struct.unpack_from(endian + "Q", lib, 32)[0]
                entsize, count = struct.unpack_from(endian + "HH", lib, 54)
                align_offset, align_format = 48, "Q"
            elif lib[4] == 1:
                phoff = struct.unpack_from(endian + "I", lib, 28)[0]
                entsize, count = struct.unpack_from(endian + "HH", lib, 42)
                align_offset, align_format = 28, "I"
            else:
                raise ValueError("Unsupported ELF class: " + item.filename)
            loads = []
            for index in range(count):
                offset = phoff + index * entsize
                if struct.unpack_from(endian + "I", lib, offset)[0] == 1:
                    loads.append(struct.unpack_from(endian + align_format, lib, offset + align_offset)[0])
            if not loads or min(loads) < 16384:
                raise ValueError("ELF load alignment <16 KiB: " + item.filename)
            filename_len, extra_len = struct.unpack_from("<HH", data, item.header_offset + 26)
            payload_offset = item.header_offset + 30 + filename_len + extra_len
            if item.compress_type == zipfile.ZIP_STORED and payload_offset % 16384:
                raise ValueError("Uncompressed library ZIP alignment <16 KiB: " + item.filename)
            rows.append({"library": item.filename, "elf_load_alignment": loads, "zip_offset": payload_offset,
                         "uncompressed": item.compress_type == zipfile.ZIP_STORED})
    return {"apk": str(apk), "native_libraries": rows, "result": "PASS"}

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("apk", type=Path, nargs="+")
    args = parser.parse_args()
    print(json.dumps([check(apk) for apk in args.apk], indent=2))
