"""Six Play-icon concepts for Miqaat. Flat vector, full-bleed 1024 square (Play applies its own mask).
Run: python3 make_concepts.py  -> concept-N.svg, concept-N-512.png, sheet.png
"""
import asyncio, os, math
os.chdir(os.path.dirname(os.path.abspath(__file__)))

NAVY_A, NAVY_B, NAVY_C = "#2A2461", "#171A46", "#0B0D2A"
GOLD_L, GOLD, GOLD_D = "#FFE9B5", "#E8C77E", "#B98B3E"
GLOW_A, GLOW_B = "#FFD08A", "#F07F3A"

DEFS = f'''
  <radialGradient id="bg" cx="50%" cy="30%" r="80%"><stop offset="0" stop-color="{NAVY_A}"/><stop offset=".55" stop-color="{NAVY_B}"/><stop offset="1" stop-color="{NAVY_C}"/></radialGradient>
  <linearGradient id="gold" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{GOLD_L}"/><stop offset=".55" stop-color="{GOLD}"/><stop offset="1" stop-color="{GOLD_D}"/></linearGradient>
  <radialGradient id="dawn" gradientUnits="userSpaceOnUse" cx="512" cy="900" r="520"><stop offset="0" stop-color="#FFF4DC"/><stop offset=".22" stop-color="{GLOW_A}"/><stop offset=".5" stop-color="{GLOW_B}" stop-opacity=".55"/><stop offset="1" stop-color="{GLOW_B}" stop-opacity="0"/></radialGradient>
  <radialGradient id="sun" cx="50%" cy="50%" r="50%"><stop offset="0" stop-color="#FFF6E0"/><stop offset=".55" stop-color="{GLOW_A}"/><stop offset="1" stop-color="{GLOW_B}"/></radialGradient>
  <radialGradient id="halo" cx="50%" cy="50%" r="50%"><stop offset="0" stop-color="{GLOW_A}" stop-opacity=".55"/><stop offset="1" stop-color="{GLOW_A}" stop-opacity="0"/></radialGradient>
'''

def svg(body, extra_defs=""):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024" width="1024" height="1024">
  <defs>{DEFS}{extra_defs}</defs>
  <rect width="1024" height="1024" fill="url(#bg)"/>
  {body}
</svg>'''

def pointed_arch(x0, x1, apex, floor, k=0.78):
    """Two-centre pointed arch: each side is a circular arc of radius k*w centred on the opposite side.
    apex is derived from k; the given apex is ignored except to keep call sites unchanged."""
    w = x1 - x0; xm = (x0 + x1) / 2; r = k * w
    h = math.sqrt(r*r - (w/2)*(w/2))          # apex height above the springing line
    spring = apex + h
    return (f"M{x0} {floor} V{spring:.1f} A{r:.1f} {r:.1f} 0 0 1 {xm} {apex:.1f} "
            f"A{r:.1f} {r:.1f} 0 0 1 {x1} {spring:.1f} V{floor} Z")

def crescent(cx, cy, r, tilt=28, thick=0.62):
    """Crescent as a single filled path (outer circle minus offset circle), via evenodd mask."""
    a = math.radians(tilt)
    dx, dy = math.cos(a) * r * (1-thick) * 1.15, -math.sin(a) * r * (1-thick) * 1.15
    return (f'<mask id="cm{cx}{cy}"><rect width="1024" height="1024" fill="white"/>'
            f'<circle cx="{cx+dx:.1f}" cy="{cy+dy:.1f}" r="{r*0.88:.1f}" fill="black"/></mask>'
            f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="url(#gold)" mask="url(#cm{cx}{cy})"/>')

concepts = {}

# 1 · The "m" mihrab (owner's concept, rebuilt flat): one silhouette, dawn horizon inside, crescent in the valley.
A = pointed_arch(150, 590, 300, 880); B = pointed_arch(434, 874, 300, 880)
concepts[1] = ("m-mihrab", svg(f'''
  <clipPath id="mclip"><path d="{A}"/><path d="{B}"/></clipPath>
  <g clip-path="url(#mclip)"><rect width="1024" height="1024" fill="url(#dawn)"/></g>
  <path d="{A} {B}" fill="none" stroke="url(#gold)" stroke-width="46" stroke-linejoin="round"/>
  <rect x="120" y="880" width="784" height="26" rx="13" fill="url(#gold)"/>
  {crescent(512, 205, 74, tilt=20)}
'''))

# 2 · Keyhole: a gold field with one mihrab cut out, dawn behind it. Maximum simplicity.
K = pointed_arch(292, 732, 150, 1024)
concepts[2] = ("keyhole", svg(f'''
  <clipPath id="kc"><path d="{K}"/></clipPath>
  <rect x="96" y="96" width="832" height="928" rx="120" fill="url(#gold)"/>
  <g clip-path="url(#kc)"><rect width="1024" height="1024" fill="url(#bg)"/><rect width="1024" height="1024" fill="url(#dawn)"/>
    <circle cx="512" cy="820" r="130" fill="url(#sun)"/></g>
''', ''))

# 3 · Dawn arch: sun half-risen over a gold ground inside one pointed arch. Prayer time = the sun's position.
D = pointed_arch(196, 828, 250, 880)
concepts[3] = ("dawn-arch", svg(f'''
  <clipPath id="dc"><path d="{D}"/></clipPath>
  <g clip-path="url(#dc)">
    <rect width="1024" height="1024" fill="url(#dawn)"/>
    <circle cx="512" cy="690" r="260" fill="url(#halo)"/>
    <circle cx="512" cy="690" r="118" fill="url(#sun)"/>
    <rect x="0" y="690" width="1024" height="400" fill="#7A5A22"/>
    <rect x="0" y="690" width="1024" height="12" fill="url(#gold)"/>
  </g>
  <path d="{D}" fill="none" stroke="url(#gold)" stroke-width="46" stroke-linejoin="round"/>
'''))

# 4 · Five: one arch, five marks along its inner curve for the five prayers; the current one is lit.
F0, F1, FA, FF = 176, 848, 236, 880
Fp = pointed_arch(F0, F1, FA, FF)
fw = F1-F0; fr = 0.78*fw; fh = math.sqrt(fr*fr-(fw/2)**2); fspring = FA+fh; fxm=(F0+F1)/2
# points on the inner edge of the arch (offset 70 inside the stroke centre line), spaced by angle on each arc
def on_left(theta):   # theta from 0 (spring, at left jamb) to max (apex), angle measured at centre (F1, fspring)
    return F1 - (fr-70)*math.cos(theta), fspring - (fr-70)*math.sin(theta)
def on_right(theta):
    return F0 + (fr-70)*math.cos(theta), fspring - (fr-70)*math.sin(theta)
tmax = math.atan2(fh, fw/2)
pts = [on_left(tmax*0.18), on_left(tmax*0.62), (fxm, FA+70+8), on_right(tmax*0.62), on_right(tmax*0.18)]
dots = "".join(f'<circle cx="{x:.0f}" cy="{y:.0f}" r="34" fill="url(#gold)"/>' for i,(x,y) in enumerate(pts) if i != 1)
lx, ly = pts[1]
concepts[4] = ("five", svg(f'''
  <path d="{Fp}" fill="none" stroke="url(#gold)" stroke-width="46" stroke-linejoin="round"/>
  <rect x="130" y="880" width="764" height="26" rx="13" fill="url(#gold)"/>
  {dots}
  <circle cx="{lx:.0f}" cy="{ly:.0f}" r="120" fill="url(#halo)"/>
  <circle cx="{lx:.0f}" cy="{ly:.0f}" r="60" fill="url(#sun)"/>
'''))

# 5 · Moon arch: the arch's left curve IS the crescent — moon and mihrab in one continuous mark.
M0, M1, MA, MF = 226, 798, 230, 900
Mp = pointed_arch(M0, M1, MA, MF)
w = M1 - M0; r = 0.78 * w; hh = math.sqrt(r*r - (w/2)**2); spring = MA + hh; xm = (M0+M1)/2
# lune between the arch's left arc (circle r about (M1,spring)) and a flatter arc through the same two end points
ax, ay = xm, MA; bx, by = M0, spring
cxm, cym = (ax+bx)/2, (ay+by)/2                      # chord midpoint
L = math.hypot(ax-bx, ay-by)
R2 = L * 0.55                                         # tighter circle through the same two points: bulges outward
d2 = math.sqrt(R2*R2 - (L/2)**2)
ux, uy = (M1-cxm)/math.hypot(M1-cxm, spring-cym), (spring-cym)/math.hypot(M1-cxm, spring-cym)   # unit toward C1 centre
c2x, c2y = cxm + ux*d2, cym + uy*d2
concepts[5] = ("moon-arch", svg(f'''
  <mask id="lune"><rect width="1024" height="1024" fill="black"/>
    <circle cx="{c2x:.1f}" cy="{c2y:.1f}" r="{R2:.1f}" fill="white"/>
    <circle cx="{M1}" cy="{spring:.1f}" r="{r-20:.1f}" fill="black"/></mask>
  <clipPath id="mc5"><path d="{Mp}"/></clipPath>
  <g clip-path="url(#mc5)"><rect width="1024" height="1024" fill="url(#dawn)" opacity=".7"/></g>
  <path d="{Mp}" fill="none" stroke="url(#gold)" stroke-width="46" stroke-linejoin="round"/>
  <rect width="1024" height="1024" fill="url(#gold)" mask="url(#lune)"/>
  <rect x="180" y="900" width="664" height="26" rx="13" fill="url(#gold)"/>
'''))

# 6 · Crescent window: a solid gold arch tile with a crescent cut through it, dawn showing through the cut.
W = pointed_arch(236, 788, 150, 940)
concepts[6] = ("crescent-window", svg(f'''
  <clipPath id="wc"><path d="{W}"/></clipPath>
  <mask id="wm"><rect width="1024" height="1024" fill="white"/><circle cx="512" cy="470" r="200" fill="black"/><circle cx="592" cy="400" r="176" fill="white"/></mask>
  <g clip-path="url(#wc)"><rect width="1024" height="1024" fill="url(#dawn)"/></g>
  <path d="{W}" fill="url(#gold)" mask="url(#wm)"/>
'''))

for n, (name, s) in concepts.items():
    open(f"concept-{n}.svg", "w").write(s)

from playwright.async_api import async_playwright
async def main():
    async with async_playwright() as p:
        b = await p.chromium.launch()
        for n, (name, s) in concepts.items():
            for size in (512, 96, 48):
                pg = await b.new_page(viewport={'width': size, 'height': size})
                sized = s.replace('viewBox="0 0 1024 1024" width="1024" height="1024"', 'viewBox="0 0 1024 1024" width="%d" height="%d"' % (size, size), 1)
                await pg.set_content("<html><body style='margin:0'>" + sized + "</body></html>")
                await (await pg.query_selector('svg')).screenshot(path=f"concept-{n}-{size}.png")
                await pg.close()
        # contact sheet
        cells = "".join(f'''<div class=c><div class=big><img src="concept-{n}-512.png"></div>
            <div class=small><img src="concept-{n}-96.png" width=96><img src="concept-{n}-48.png" width=48></div><p>{n} · {name}</p></div>''' for n, (name, s) in concepts.items())
        html = f'''<html><body style="margin:0;background:#f3f1ec;font:14px system-ui;padding:24px">
        <style>.g{{display:grid;grid-template-columns:repeat(3,1fr);gap:28px}}.c{{text-align:center}}.big img{{width:300px;border-radius:66px;box-shadow:0 8px 24px rgba(0,0,0,.18)}}
        .small{{display:flex;gap:16px;justify-content:center;align-items:flex-end;margin-top:12px}}.small img{{border-radius:22%}}p{{margin:8px 0 0;color:#333}}</style>
        <div class=g>{cells}</div></body></html>'''
        open("sheet.html", "w").write(html)
        pg = await b.new_page(viewport={'width': 1100, 'height': 900})
        await pg.goto("file://" + os.path.abspath("sheet.html"))
        await pg.screenshot(path="sheet.png", full_page=True)
        await b.close()
asyncio.run(main())
print("ok")
