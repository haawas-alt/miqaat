# -*- coding: utf-8 -*-
"""English review pack of the OPEN items only (sections already verified are listed, not repeated).
Reads REVIEW.md and ISLAMIC_REVIEW_PACK.md; writes Miqaat-Islamic-Review-Pack-open-items.pdf in the cwd.
Run from any directory:  python3 tools/review_pack_en.py"""
import re, base64, asyncio, os
import markdown
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
rev = open(f'{ROOT}/REVIEW.md', encoding='utf-8').read()
pack = open(f'{ROOT}/ISLAMIC_REVIEW_PACK.md', encoding='utf-8').read()
FONT = f'{ROOT}/app/src/main/res/font/amiri_regular.ttf'; FONTB = f'{ROOT}/app/src/main/res/font/amiri_bold.ttf'
f64 = base64.b64encode(open(FONT, 'rb').read()).decode(); fb64 = base64.b64encode(open(FONTB, 'rb').read()).decode()

def section(text, title, level='## '):
    a = text.index(level + title); m = re.search(r'\n' + level, text[a + len(level):]); b = a + len(level) + m.start() if m else len(text)
    return text[a:b].rstrip() + '\n'
CLOSED_J = ('J7', 'J10')
md = lambda t: markdown.markdown(t, extensions=['tables'])

parts = []
parts.append(md("""# Miqaat · Islamic content review pack — open items

**Content version 2026.09-b · issued 29 September 2026 · Status: PARTLY SIGNED**

### Closed — no action needed
Verified by **Sheikh Abdel Razek Mahmoud Ramadan (Egypt)** on 29 Sep 2026, as reported by the app owner, on content version 2026.09-b:

| Section | Items |
|---|---|
| Hadith after the azaan | 36 texts (Arabic, translation, source) — **closed** |
| Learn Ṣalāh | 12 texts and their notes, plus statements **J7** and **J10** — **closed** |

These are not repeated in this pack. Any later edit to their text voids the approval and reopens them.

### Still for review
| Section | Items |
|---|---|
| Duas (after the azaan; iftār) | 2 |
| Adhkār (morning, evening, after prayer) | 24 |
| Other claims made in the app | 10 |
| Jurisprudential statements and calculation assumptions | J1–J6, J8, J9, J11 |

For every item tick one: **☐ approved as shipped · ☐ approved with change (write it) · ☐ rejected**. Then sign the last page.
The app will continue to say the remaining content is unsigned; it must not say more than this pack records.
Grading policy: hadith come only from Ṣaḥīḥ al-Bukhārī and Ṣaḥīḥ Muslim; other collections appear only in the adhkār, with their grading and the grader named. Numbers follow sunnah.com.
"""))
parts.append('<div class="pb"></div>' + md('# Part 1 · Texts still for review'))
for t in ('Duas', 'Adhkār', 'Other claims made in the app'):
    parts.append(md(section(rev, t).replace('## ', '## ', 1)))
parts.append('<div class="pb"></div>' + md('# Part 2 · Statements requiring qualified review\n\nLegend — **Engine:** what the code computes. **Was:** earlier wording. **Now:** wording shipped. **Approve:** ☐ as shipped · ☐ with change · ☐ reject.'))
a = pack.index('### J1'); b = pack.index('\n---\n\n## Sign-off')
js = re.split(r'\n(?=### J)', pack[a:b])
for j in js:
    if j.startswith('### ') and any(j.startswith('### ' + c + ' ') for c in CLOSED_J): continue
    parts.append(md(j))
parts.append('<div class="pb"></div>' + md(section(pack, 'Sign-off', '## ')))
parts.append(md('### Already recorded\n- 29 Sep 2026 — hadith after the azaan (36) and Learn Ṣalāh (12), with J7 and J10: verified by Sheikh Abdel Razek Mahmoud Ramadan (Egypt) on 2026.09-b, as reported by the app owner.\n\nReviewed by: ______________________   Date: __________   Signature: ______________________\n\nCorrections (id → change):\n\n\n\n'))

css = f"""
@font-face{{font-family:Amiri;src:url(data:font/ttf;base64,{f64}) format("truetype");font-weight:400}}
@font-face{{font-family:Amiri;src:url(data:font/ttf;base64,{fb64}) format("truetype");font-weight:700}}
body{{font-family:'Helvetica Neue',Arial,'Amiri',sans-serif;font-size:10.5pt;line-height:1.45;color:#1a1a1a}}
h1{{font-size:22pt;color:#0B1033;margin:0 0 6pt}} h2{{font-size:15pt;color:#0B1033;border-bottom:1.5pt solid #B8912F;padding-bottom:2pt;margin:16pt 0 6pt;page-break-after:avoid}}
h3{{font-size:12.5pt;color:#0B1033;margin:12pt 0 3pt;page-break-after:avoid}}
table{{width:100%;border-collapse:collapse;margin:6pt 0 10pt;font-size:9.5pt}} th,td{{border:0.6pt solid #999;padding:3pt 5pt;vertical-align:top}}
th{{background:#EFE7D2}} tr{{page-break-inside:avoid}} .pb{{page-break-before:always}}
td:has(> *), td{{font-family:'Helvetica Neue',Arial,'Amiri',sans-serif}}
ul{{margin:2pt 0 4pt 16pt;padding:0}} li{{margin:1.5pt 0}}
"""
html = f'<!doctype html><html><head><meta charset="utf-8"><title>Miqaat review pack — open items</title><style>{css}</style></head><body>' + '\n'.join(parts) + '</body></html>'
out = os.getcwd()
open(f'{out}/review-open.html', 'w', encoding='utf-8').write(html)
from playwright.async_api import async_playwright
async def main():
    async with async_playwright() as p:
        b = await p.chromium.launch(); pg = await b.new_page()
        await pg.goto(f'file://{out}/review-open.html'); await pg.wait_for_timeout(800)
        await pg.pdf(path=f'{out}/Miqaat-Islamic-Review-Pack-open-items.pdf', format='A4', print_background=True, display_header_footer=True, header_template='<div></div>',
                     footer_template='<div style="font-size:8px;width:100%;text-align:center;color:#666">Miqaat · review pack, open items · 2026.09-b — <span class="pageNumber"></span> / <span class="totalPages"></span></div>',
                     margin={'top': '16mm', 'bottom': '18mm', 'left': '14mm', 'right': '14mm'})
        await b.close()
asyncio.run(main()); print('ok')
