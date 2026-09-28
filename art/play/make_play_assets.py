# -*- coding: utf-8 -*-
"""Google Play graphics: 512x512 store icon (full-bleed square, no transparency) and the 1024x500 feature graphic.
Run:  cd art && python3 play/make_play_assets.py"""
import asyncio, base64, os, re
from playwright.async_api import async_playwright
HERE = os.path.dirname(os.path.abspath(__file__)); ART = os.path.dirname(HERE); RES = os.path.join(os.path.dirname(ART), 'app/src/main/res/font')
svg = open(f'{ART}/miqaat-icon.svg').read()
sq = svg.replace('rx="228"', 'rx="0"')                    # Play applies its own mask: the icon must be a full square
b64 = lambda p: base64.b64encode(open(p, 'rb').read()).decode()
fonts = f"""
@font-face{{font-family:Cormorant;src:url(data:font/ttf;base64,{b64(RES+'/cormorant_garamond_medium.ttf')});}}
@font-face{{font-family:Nunito;src:url(data:font/ttf;base64,{b64(RES+'/nunito_sans.ttf')});}}
@font-face{{font-family:Amiri;src:url(data:font/ttf;base64,{b64(RES+'/amiri_regular.ttf')});}}"""
icon_uri = 'data:image/svg+xml;base64,' + base64.b64encode(sq.encode()).decode()
feature = f"""<html><head><style>{fonts}
body{{margin:0;width:1024px;height:500px;overflow:hidden;background:radial-gradient(120% 140% at 30% 40%,#2C2566 0%,#191B48 45%,#0A0C28 100%);position:relative;color:#F3ECD8}}
.glow{{position:absolute;left:-60px;top:20px;width:560px;height:560px;background:radial-gradient(closest-side,rgba(255,196,120,.32),rgba(255,196,120,0));}}
.icon{{position:absolute;left:78px;top:66px;width:368px;height:368px;border-radius:84px;box-shadow:0 18px 60px rgba(0,0,0,.45);overflow:hidden}}
.icon img{{width:100%;height:100%;display:block}}
.t{{position:absolute;left:520px;top:0;height:500px;width:460px;display:flex;flex-direction:column;justify-content:center}}
.ar{{font-family:Amiri;font-size:64px;color:#E9CF88;line-height:1.1}}
.name{{font-family:Cormorant;font-size:116px;letter-spacing:2px;line-height:1;margin-top:2px}}
.rule{{width:120px;height:3px;background:linear-gradient(90deg,#E9CF88,rgba(233,207,136,0));margin:18px 0 20px}}
.tag{{font-family:Nunito;font-size:28px;letter-spacing:.5px;color:#D9D3C2;line-height:1.35}}
.sm{{font-family:Nunito;font-size:20px;color:#A9A596;margin-top:18px}}
</style></head><body><div class="glow"></div><div class="icon"><img src="{icon_uri}"></div>
<div class="t"><div class="ar">ميقات</div><div class="name">Miqaat</div><div class="rule"></div>
<div class="tag">Prayer times · Azaan · Qibla</div><div class="sm">On your device. No ads. No tracking.</div></div></body></html>"""
async def main():
    async with async_playwright() as p:
        b = await p.chromium.launch()
        pg = await b.new_page(viewport={'width': 512, 'height': 512}, device_scale_factor=1)
        await pg.set_content("<html><body style='margin:0'><img id='i' width='512' height='512' style='display:block' src='" + icon_uri + "'></body></html>")
        await pg.wait_for_timeout(300)
        await (await pg.query_selector('#i')).screenshot(path=f'{HERE}/play-icon-512.png')
        pg2 = await b.new_page(viewport={'width': 1024, 'height': 500}, device_scale_factor=1)
        await pg2.set_content(feature); await pg2.wait_for_timeout(600)
        await pg2.screenshot(path=f'{HERE}/feature-graphic-1024x500.png')
        await b.close()
asyncio.run(main()); print('ok')
