"""Density exports from approved 03 Light Within. Requires Pillow.
Android masks the full-bleed artwork; portal stays inside the adaptive safe circle.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
ROOT=Path(__file__).resolve().parents[2]
ART=Image.open(ROOT/'art/logo/light-within-source.webp').convert('RGB')
BG=(5,14,25)
for density,scale in {'mdpi':1,'hdpi':1.5,'xhdpi':2,'xxhdpi':3,'xxxhdpi':4}.items():
    folder=ROOT/f'app/src/main/res/mipmap-{density}'
    size=int(108*scale);art_size=round(size*.60)
    fg=Image.new('RGBA',(size,size),BG+(255,))
    fg.alpha_composite(ART.resize((art_size,art_size),Image.Resampling.LANCZOS).convert('RGBA'),((size-art_size)//2,(size-art_size)//2))
    fg.save(folder/'ic_launcher_fg.png',optimize=True)
    legacy_size=int(48*scale)
    ART.resize((legacy_size,legacy_size),Image.Resampling.LANCZOS).save(folder/'ic_launcher.png',optimize=True)
ART.resize((512,512),Image.Resampling.LANCZOS).save(ROOT/'art/play/play-icon-512.png',optimize=True)
feature=Image.new('RGB',(1024,500),BG)
feature.paste(ART.resize((440,440),Image.Resampling.LANCZOS),(25,30))
draw=ImageDraw.Draw(feature)
def font(name,size):return ImageFont.truetype(str(ROOT/f'app/src/main/res/font/{name}.ttf'),size)
draw.text((505,115),'Miqaat',font=font('cormorant_garamond_medium',115),fill='#ECCA86')
draw.text((510,270),'Prayer times · Azaan · Qibla',font=font('nunito_sans',27),fill='#EBE6D7')
draw.text((510,315),'On your device · no ads · no tracking',font=font('nunito_sans',24),fill='#AEB8C6')
feature.save(ROOT/'art/play/feature-graphic-1024x500.png',optimize=True)
layer=Image.open(ROOT/'app/src/main/res/mipmap-xxxhdpi/ic_launcher_fg.png')
strip=Image.new('RGB',(600,250),'#EDEAE3');x=22
for size in (48,96,192):
    icon=Image.new('RGBA',(size,size),BG+(255,))
    f=layer.resize((round(size*108/72),round(size*108/72)),Image.Resampling.LANCZOS)
    icon.alpha_composite(f,((size-f.width)//2,(size-f.height)//2))
    mask=Image.new('L',(size,size));ImageDraw.Draw(mask).ellipse((0,0,size-1,size-1),fill=255)
    strip.paste(icon.convert('RGB'),(x,225-size),mask);x+=size+35
strip.save(ROOT/'art/logo/preview-strip.png')
