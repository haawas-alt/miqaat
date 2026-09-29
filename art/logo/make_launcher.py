"""Cuts the launcher + Play assets from art/logo/miqaat-logo-source.png (owner-supplied artwork).

Outputs
  app/src/main/res/mipmap-*/ic_launcher_fg.png      adaptive-icon foreground (108dp canvas, art in the 66dp safe zone)
  app/src/main/res/mipmap-*/ic_launcher_legacy.png  square legacy icon (48dp) for pre-26 launchers / some pickers
  art/play/play-icon-512.png                         Play Store icon (full bleed, Play masks it)
  art/play/feature-graphic-1024x500.png              Play feature graphic
  art/logo/preview-strip.png                         48/96/192 preview
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter
os.chdir(os.path.dirname(os.path.abspath(__file__)))
ROOT = os.path.abspath("../..")
SRC = Image.open("miqaat-logo-source.png").convert("RGBA")
BG = (0, 38, 32)                     # deep green, matches the source's outer gradient
GOLD_BBOX = (201, 94, 1040, 1145)    # measured extents of the gold arch in the source

def arch_crop(pad_frac=0.06):
    """Square crop around the arch with a little breathing room; the arch fills ~88% of the square."""
    x0, y0, x1, y1 = GOLD_BBOX
    w, h = x1 - x0, y1 - y0
    side = int(max(w, h) * (1 + 2 * pad_frac))
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    box = (int(cx - side / 2), int(cy - side / 2), int(cx + side / 2), int(cy + side / 2))
    canvas = Image.new("RGBA", (side, side), BG + (255,))
    # paste the source (clamped) onto the green canvas so any overrun off the source edge is green
    sx0, sy0 = max(box[0], 0), max(box[1], 0)
    sx1, sy1 = min(box[2], SRC.width), min(box[3], SRC.height)
    canvas.paste(SRC.crop((sx0, sy0, sx1, sy1)), (sx0 - box[0], sy0 - box[1]))
    return canvas

ART = arch_crop()

# ---- Play icon: full-bleed 512, arch ~80% of height
play = Image.new("RGBA", (1024, 1024), BG + (255,))
a = ART.resize((int(1024 * 0.90), int(1024 * 0.90)), Image.LANCZOS)
play.alpha_composite(a, ((1024 - a.width) // 2, (1024 - a.height) // 2))
# soften the seam between crop and canvas with a wide vignette of the same green
play.convert("RGB").resize((512, 512), Image.LANCZOS).save(os.path.join(ROOT, "art/play/play-icon-512.png"), optimize=True)

# ---- Adaptive icon foreground: 108dp canvas, safe circle 66dp -> art occupies ~62% of the canvas
DENS = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
for d, s in DENS.items():
    size = int(108 * s)
    fg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    art_px = int(size * 0.54)
    aa = ART.resize((art_px, art_px), Image.LANCZOS)
    # feather the square edge of the crop into transparency so the background colour shows around it
    mask = Image.new("L", (art_px, art_px), 0)
    ImageDraw.Draw(mask).rounded_rectangle((0, 0, art_px - 1, art_px - 1), radius=int(art_px * 0.18), fill=255)
    mask = mask.filter(ImageFilter.GaussianBlur(art_px * 0.02))
    aa.putalpha(mask)
    fg.alpha_composite(aa, ((size - art_px) // 2, (size - art_px) // 2))
    ddir = os.path.join(ROOT, f"app/src/main/res/mipmap-{d}")
    os.makedirs(ddir, exist_ok=True)
    fg.save(os.path.join(ddir, "ic_launcher_fg.png"), optimize=True)
    # legacy square icon (48dp): full-bleed art, rounded a little
    lsz = int(48 * s)
    leg = play.resize((lsz, lsz), Image.LANCZOS)
    m = Image.new("L", (lsz, lsz), 0); ImageDraw.Draw(m).rounded_rectangle((0, 0, lsz - 1, lsz - 1), radius=int(lsz * 0.18), fill=255)
    leg.putalpha(m); leg.save(os.path.join(ddir, "ic_launcher.png"), optimize=True)

# ---- Feature graphic 1024x500: art left, wordmark right
fgph = Image.new("RGB", (1024, 500), BG)
# soft radial glow behind the art
glow = Image.new("RGBA", (1024, 500), (0, 0, 0, 0))
ImageDraw.Draw(glow).ellipse((-60, -140, 560, 640), fill=(255, 190, 80, 70))
glow = glow.filter(ImageFilter.GaussianBlur(90)); fgph.paste(glow, (0, 0), glow)
art = ART.resize((430, 430), Image.LANCZOS)
m = Image.new("L", art.size, 0); ImageDraw.Draw(m).rounded_rectangle((0, 0, 429, 429), radius=80, fill=255)
fgph.paste(art, (60, 35), m)
d = ImageDraw.Draw(fgph)
def font(path, size):
    try: return ImageFont.truetype(path, size)
    except Exception: return ImageFont.load_default()
serif = font(os.path.join(ROOT, "app/src/main/res/font/cormorant_garamond_medium.ttf"), 128)
sans = font(os.path.join(ROOT, "app/src/main/res/font/nunito_sans.ttf"), 30)
d.text((540, 130), "Miqaat", font=serif, fill=(236, 201, 120))
d.text((546, 285), "Prayer times · Azaan · Qibla", font=sans, fill=(220, 226, 220))
d.text((546, 330), "On your device · no ads · no tracking", font=sans, fill=(160, 175, 168))
fgph.save(os.path.join(ROOT, "art/play/feature-graphic-1024x500.png"), optimize=True)

# ---- Preview strip at launcher sizes with a circular mask (what most launchers show)
strip = Image.new("RGB", (560, 230), (243, 241, 236))
x = 20
for s in (48, 96, 192):
    icon = Image.new("RGBA", (s, s), BG + (255,))
    fgp = Image.open(os.path.join(ROOT, "app/src/main/res/mipmap-xxxhdpi/ic_launcher_fg.png")).resize((int(s * 108 / 72), int(s * 108 / 72)), Image.LANCZOS)
    off = (s - fgp.width) // 2
    icon.alpha_composite(fgp, (off, off))
    m = Image.new("L", (s, s), 0); ImageDraw.Draw(m).ellipse((0, 0, s - 1, s - 1), fill=255)
    strip.paste(icon.convert("RGB"), (x, 210 - s), m); x += s + 24
strip.save("preview-strip.png")
print("ok")
