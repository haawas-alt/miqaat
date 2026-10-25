#!/usr/bin/env python3
"""Restore exactly the approved artwork bytes delivered with this commit."""
from pathlib import Path
import hashlib,json
root=Path('.artwork-transfer')
for item in json.loads((root/'manifest.json').read_text()):
    target=Path(item['path'])
    assert target.parent==Path('app/src/main/res/drawable-nodpi') and target.suffix=='.webp'
    chunks=[root/p for p in item['parts']]
    assert all(p.is_relative_to(root) and '..' not in p.parts for p in chunks)
    data=b''.join(p.read_bytes() for p in chunks)
    assert hashlib.sha256(data).hexdigest()==item['sha256'],target
    target.write_bytes(data)
    print('Restored checksum-verified',target)
