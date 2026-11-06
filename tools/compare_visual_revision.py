"""Create measurable before/after evidence without pretending it is a fidelity score."""
from pathlib import Path
import json
import sys
from PIL import Image, ImageChops, ImageDraw, ImageOps

before_root, after_root, output = map(Path, sys.argv[1:4])
common = sorted(p.relative_to(before_root) for p in before_root.rglob("*.png")
                if (after_root / p.relative_to(before_root)).exists())
if not common:
    raise SystemExit("No common native captures")

rows = []
changed_total = 0
for relative in common:
    with Image.open(before_root / relative) as old_image, Image.open(after_root / relative) as new_image:
        old = old_image.convert("RGB")
        new = new_image.convert("RGB")
        if old.size != new.size:
            rows.append({"path": str(relative), "before": old.size, "after": new.size, "comparable": False})
            changed_total += 1
            continue
        difference = ImageChops.difference(old, new)
        changed = sum(1 for pixel in difference.getdata() if pixel != (0, 0, 0))
        changed_total += changed
        absolute = sum(value * count for value, count in enumerate(difference.histogram()))
        rows.append({
            "path": str(relative), "width": old.width, "height": old.height,
            "changedPixels": changed, "changedFraction": changed / (old.width * old.height),
            "meanAbsoluteChannelDifference": absolute / (old.width * old.height * 3),
        })

output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps({"captures": len(rows), "changedPixels": changed_total, "files": rows}, indent=2))
if changed_total == 0:
    raise SystemExit("Candidate captures are byte-identical to the pre-fix baseline")

key = [p for p in common if p.name == "home-normal-default-EN.png"]
tiles = []
for relative in key:
    with Image.open(before_root / relative) as old_image, Image.open(after_root / relative) as new_image:
        old = ImageOps.contain(old_image.convert("RGB"), (700, 360))
        new = ImageOps.contain(new_image.convert("RGB"), (700, 360))
    tile = Image.new("RGB", (1440, 405), "#f4f1ea")
    tile.paste(old, (10, 34)); tile.paste(new, (730, 34))
    draw = ImageDraw.Draw(tile)
    draw.text((10, 8), f"BEFORE  {relative.parent}", fill="#162433")
    draw.text((730, 8), f"AFTER  {relative.parent}", fill="#162433")
    tiles.append(tile)
if tiles:
    board = Image.new("RGB", (1440, 405 * len(tiles)), "white")
    for index, tile in enumerate(tiles):
        board.paste(tile, (0, index * 405))
    board.save(output.with_suffix(".jpg"), quality=92)
