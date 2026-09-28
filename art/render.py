import asyncio
from playwright.async_api import async_playwright
svg=open('miqaat-icon.svg').read()
html=f"<html><body style='margin:0;background:transparent'>{svg}</body></html>"
async def main():
    async with async_playwright() as p:
        b=await p.chromium.launch(); pg=await b.new_page(viewport={'width':1024,'height':1024}, device_scale_factor=1)
        await pg.set_content(html); el=await pg.query_selector('svg')
        await el.screenshot(path='miqaat-icon-1024.png', omit_background=True)
        # small-size previews
        for s in (48,96,192):
            pg2=await b.new_page(viewport={'width':s,'height':s}); await pg2.set_content(html.replace('width="1024" height="1024"',f'width="{s}" height="{s}"'))
            await (await pg2.query_selector('svg')).screenshot(path=f'preview-{s}.png', omit_background=True)
        await b.close()
asyncio.run(main())
