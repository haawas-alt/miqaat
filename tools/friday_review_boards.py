"""Create contact sheets using only controlled public demo captures; no user device data."""
from pathlib import Path
import os
from PIL import Image, ImageDraw, ImageFont
root=Path('app/build/friday-quiet-review');out=Path('public-review');out.mkdir(exist_ok=True)
def font(n):
    try:return ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf',n)
    except OSError:return ImageFont.load_default(size=n)
for theme,title in [('MIQAAT','Miqaat'),('KISWAH','Kiswah'),('CELESTIAL_MERIDIAN','Celestial Meridian'),('PRAYER_GALLERY','Prayer Gallery')]:
    for kind in ['friday','quiet']:
        board=Image.new('RGB',(2400,1600),'#151b25');d=ImageDraw.Draw(board)
        d.text((25,15),title+' — '+kind.title()+' · actual app captures',font=font(38),fill='#f8f1df')
        for dev,x,y,w,h in [('tablet-landscape',25,115,1150,720),('phone-landscape',25,935,1150,535),('tablet-portrait',1230,115,640,1024),('phone-portrait',1900,115,465,1008)]:
            d.text((x,y-36),dev.replace('-',' ').title(),font=font(24),fill='#c8d3e0')
            im=Image.open(root/theme/(dev+'-EN-'+kind+'.png')).convert('RGB');im.thumbnail((w,h),Image.Resampling.LANCZOS)
            board.paste(im,(x+(w-im.width)//2,y))
        d.text((1240,1250),'Controlled demo settings · Mascot, NSW',font=font(24),fill='#e1c27a')
        d.text((1240,1290),'Engine-calculated times · English / Urdu verified',font=font(20),fill='#bdc9d8')
        d.text((1240,1330),'Source '+os.environ['GITHUB_SHA'][:12],font=font(20),fill='#bdc9d8')
        board.resize((1600,1067),Image.Resampling.LANCZOS).save(out/(theme+'-'+kind+'.jpg'),quality=89)
(out/'SOURCE.txt').write_text(os.environ['GITHUB_SHA']+'\nPublic app demo captures only; no live user data.\n')
