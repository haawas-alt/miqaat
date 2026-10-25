# Approved tablet landscape implementation 1.181

Scope: Miqaat and Prayer Gallery tablet landscape only, minimum dimension 600dp. Phone portrait/landscape, tablet portrait and deferred Kiswah/Celestial landscape use their verified 1.180 renderers. Large system type retains AccessibleHome.

Miqaat: full-width scene with pointed gold arch in right half, calm six-row left panel, grouped next-prayer hero, full-width timeline and one four-action bottom dock. Prayer Gallery: botanical ivory paper, unified painting/name/clock/action hero and six illustrated cards in a 3x2 grid. All content is live native Compose. Source concept typography/art details are reproduced using clean artwork layers; generated concept text is never shipped as UI.

Native review covers 1280x800 and 960x600, English/Urdu, absolute/relative clocks, 24-hour/five-prayer settings, 110% font, overnight and iqamah/Ramadan states. All details, end times, NOW/NEXT states, destinations and contextual notices remain functional. Tests assert text ink, row/action bounds, grid order and full-month Hijri dates. Existing retained Gallery tests remain required.

Protection: 72 approved phone landscape captures plus 28 before/after captures of phone portrait, tablet portrait and deferred tablet landscape themes. The existing bounded non-text timeline antialiasing allowance applies; no other pixel changes are accepted.

Art assets prepared with the built-in image-generation skill from the two approved tablet concepts, with prompts limited to removing UI and reconstructing clean artwork. Names: tablet_miqaat_181.webp, tablet_gallery_atlas_181.webp and tablet_gallery_paper_181.webp. Original artwork and launcher resources remain intact.
