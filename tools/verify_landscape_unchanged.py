"""Require every native phone landscape render to match the approved baseline byte for byte."""
from pathlib import Path
import hashlib
import json
import sys
from PIL import Image, ImageChops

before, after, output = map(Path, sys.argv[1:])
baseline = sorted(before.rglob('*.png'))
assert baseline, 'Missing approved landscape baseline'
results = []
for old in baseline:
    relative = old.relative_to(before)
    new = after / relative
    a = Image.open(old).convert('RGBA')
    b = Image.open(new).convert('RGBA') if new.exists() else None
    same = b is not None and a.size == b.size and a.tobytes() == b.tobytes()
    result = {'capture': str(relative), 'identical': same,
              'baseline_sha256': hashlib.sha256(old.read_bytes()).hexdigest()}
    if not same and b is not None and a.size == b.size:
        difference = ImageChops.difference(a, b)
        result['changed_pixels'] = sum(any(pixel) for pixel in difference.getdata())
        result['max_channel_difference'] = max(high for low, high in difference.getextrema())
        result['channel_bounds'] = [band.getbbox() for band in difference.split()]
        print(json.dumps(result))
    results.append(result)
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps({'baseline_commit': 'bc2e2290cf8a8eac160b0d1e0f58d157d4729be0',
                              'count': len(results), 'captures': results}, indent=2))
failed = [r['capture'] for r in results if not r['identical']]
assert not failed, f'Landscape changed: {failed}'
print(f'{len(results)} phone landscape renders identical to the approved baseline.')
