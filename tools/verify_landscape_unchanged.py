"""Require unchanged landscape pixels, with a bounded native Canvas edge allowance.

Robolectric may vary antialiased edge pixels around multiple timeline markers.
Only that non-text Canvas may vary: at most 16 connected pixels per edge cluster,
64 pixels overall, and 16/255 per channel. All other pixels and the Canvas bounds
must match exactly. A count across the entire Canvas incorrectly combined
separate marker edges and made otherwise unchanged renders intermittently fail.
"""
from pathlib import Path
import hashlib
import json
import sys
import re
from PIL import Image, ImageChops

before, after, output = map(Path, sys.argv[1:4])
baseline_commit = sys.argv[4] if len(sys.argv)>4 else 'bc2e2290cf8a8eac160b0d1e0f58d157d4729be0'
baseline = sorted(before.rglob('*.png'))
assert baseline, 'Missing approved landscape baseline'
results = []
def timeline_canvas(png):
    snapshot = png.with_suffix('.txt')
    if not snapshot.exists():
        return None
    lines = snapshot.read_text().splitlines()
    tag = "approved-day-timeline" if any("Tag: 'approved-day-timeline'" in line for line in lines) else "landscape-timeline-panel"
    for i, line in enumerate(lines):
        if f"Tag: '{tag}'" in line:
            # The first child of this Column is its non-text native Canvas.
            for child in lines[i+1:]:
                m = re.search(r'at \(l=([\d.]+), t=([\d.]+), r=([\d.]+), b=([\d.]+)\)px', child)
                if m:
                    return tuple(map(float, m.groups()))
    return None
for old in baseline:
    relative = old.relative_to(before)
    new = after / relative
    a = Image.open(old).convert('RGBA')
    b = Image.open(new).convert('RGBA') if new.exists() else None
    same = b is not None and a.size == b.size and a.tobytes() == b.tobytes()
    result = {'capture': str(relative), 'identical': same, 'accepted': same,
              'baseline_sha256': hashlib.sha256(old.read_bytes()).hexdigest()}
    if not same and b is not None and a.size == b.size:
        difference = ImageChops.difference(a, b)
        result['changed_pixels'] = sum(any(pixel) for pixel in difference.getdata())
        result['max_channel_difference'] = max(high for low, high in difference.getextrema())
        result['channel_bounds'] = [band.getbbox() for band in difference.split()]
        box = timeline_canvas(old)
        boxes = [bounds for bounds in result['channel_bounds'] if bounds]
        inside_canvas = box is not None and box == timeline_canvas(new) and all(
            bounds[0] >= box[0] and bounds[1] >= box[1] and bounds[2] <= box[2] and bounds[3] <= box[3]
            for bounds in boxes)
        clusters = []
        if inside_canvas and result['changed_pixels'] <= 64 and result['max_channel_difference'] <= 16:
            union = (min(b[0] for b in boxes), min(b[1] for b in boxes),
                     max(b[2] for b in boxes), max(b[3] for b in boxes))
            crop = difference.crop(union)
            points = {(i % crop.width, i // crop.width) for i, pixel in enumerate(crop.getdata()) if any(pixel)}
            while points:
                pending = [points.pop()]
                count = 0
                while pending:
                    x, y = pending.pop()
                    count += 1
                    for dx in (-1, 0, 1):
                        for dy in (-1, 0, 1):
                            neighbour = (x+dx, y+dy)
                            if neighbour in points:
                                points.remove(neighbour)
                                pending.append(neighbour)
                clusters.append(count)
        result['edge_clusters'] = sorted(clusters, reverse=True)
        result['accepted'] = bool(clusters) and max(clusters) <= 16
        result['timeline_antialiasing_only'] = bool(result['accepted'])
        print(json.dumps(result))
    results.append(result)
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps({'baseline_commit': baseline_commit,
                              'count': len(results), 'captures': results}, indent=2))
failed = [r['capture'] for r in results if not r['accepted']]
assert not failed, f'Landscape changed: {failed}'
print(f"{len(results)} unchanged landscape renders: {sum(r['identical'] for r in results)} pixel-exact; "
      f"{sum(not r['identical'] for r in results)} with bounded non-text Canvas edge variance only.")
