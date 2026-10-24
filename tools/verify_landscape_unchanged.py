"""Require every native phone landscape render to match the approved baseline byte for byte."""
from pathlib import Path
import hashlib
import json
import sys

before, after, output = map(Path, sys.argv[1:])
baseline = sorted(before.rglob('*.png'))
assert baseline, 'Missing approved landscape baseline'
results = []
for old in baseline:
    relative = old.relative_to(before)
    new = after / relative
    results.append({'capture': str(relative), 'identical': new.exists() and old.read_bytes() == new.read_bytes(),
                    'baseline_sha256': hashlib.sha256(old.read_bytes()).hexdigest()})
output.parent.mkdir(parents=True, exist_ok=True)
output.write_text(json.dumps({'baseline_commit': 'bc2e2290cf8a8eac160b0d1e0f58d157d4729be0',
                              'count': len(results), 'captures': results}, indent=2))
failed = [r['capture'] for r in results if not r['identical']]
assert not failed, f'Landscape changed: {failed}'
print(f'{len(results)} phone landscape renders identical to the approved baseline.')
