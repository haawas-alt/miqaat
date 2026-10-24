"""Require unchanged landscape pixels, with a bounded native Canvas edge allowance.

Robolectric may vary 4-6 antialiased edge pixels around the timeline marker.
Only that non-text Canvas may vary: at most 16 pixels, at most 16/255 per
channel. All other pixels and the Canvas bounds must match exactly.
"""
from pathlib import Path
import hashlib
import json
import sys
import re
from PIL import Image, ImageChops

before, after, output = map(Path, sys.argv[1:])
baseline = sorted(before.rglob('*.png'))
assert baseline, 'Missing approved landscape baseline'
results = []
def timeline_canvas(png):
    snapshot = png.with_suffix('.txt')
    if not snapshot.exists():
        return None
    lines = snapshot.read_text().splitlines()
    for i, line in enumerate(lines):
        if "Tag: 'approved-day-timeline'" in line:
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
        result['accepted'] = inside_canvas and result['changed_pixels'] <= 16 and result['max_channel_difference'] <= 16
        result['timeline_antialiasing_only'] = bool(result['accepted'])
        print(json.dumps(result))
    results.append(result)
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps({'baseline_commit': 'bc2e2290cf8a8eac160b0d1e0f58d157d4729be0',
                              'count': len(results), 'captures': results}, indent=2))
failed = [r['capture'] for r in results if not r['accepted']]
assert not failed, f'Landscape changed: {failed}'
print(f"{len(results)} unchanged landscape renders: {sum(r['identical'] for r in results)} pixel-exact; "
      f"{sum(not r['identical'] for r in results)} with bounded non-text Canvas edge variance only.")
