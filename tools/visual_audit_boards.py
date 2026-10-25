"""Index actual native captures; contact sheets supplement, never replace, full-size PNGs."""
from pathlib import Path
import json,hashlib
from PIL import Image,ImageOps,ImageDraw
root=Path('app/build/visual-audit');out=Path('app/build/visual-boards');out.mkdir(parents=True,exist_ok=True)
rows=[]
for directory in sorted({p.parent for p in root.rglob('*.png')}):
    for language in ['EN','UR']:
        files=sorted(directory.glob(language+'-*.png'))
        for start in range(0,len(files),12):
            subset=files[start:start+12];thumbs=[]
            for p in subset:
                with Image.open(p) as im:
                    rows.append({'path':str(p),'width':im.width,'height':im.height,'bytes':p.stat().st_size,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
                    image=ImageOps.contain(im.convert('RGB'),(380,340))
                tile=Image.new('RGB',(400,380),'#eeeeee');tile.paste(image,((400-image.width)//2,25));ImageDraw.Draw(tile).text((8,355),p.stem,fill='black');thumbs.append(tile)
            board=Image.new('RGB',(1600,380*((len(thumbs)+3)//4)),'white')
            for i,tile in enumerate(thumbs):board.paste(tile,((i%4)*400,(i//4)*380))
            name='-'.join(directory.relative_to(root).parts)+f'-{language}-{start//12+1:02d}.jpg'
            board.save(out/name,quality=90)
(out/'inventory.json').write_text(json.dumps(rows,indent=2))
