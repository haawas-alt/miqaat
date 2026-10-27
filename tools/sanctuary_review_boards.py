"""Export controlled demo views for comparison against approved A concepts."""
from pathlib import Path
from PIL import Image
root=Path('app/build/sanctuary-review');out=Path('public-review');out.mkdir(exist_ok=True)
for p in root.rglob('*-12h.png'):
    theme=p.parent.name;dev,lang=p.name.removesuffix('-12h.png').rsplit('-',1)
    if lang!='EN':dev+='-'+lang
    Image.open(p).convert('RGB').save(out/f'{theme}-{dev}-sanctuary.jpg',quality=97,subsampling=0)
