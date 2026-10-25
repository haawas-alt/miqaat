# Approved tablet landscape implementation 1.181

Scope: Miqaat and Prayer Gallery tablet landscape only, minimum dimension 600dp. Phone portrait/landscape, tablet portrait and deferred Kiswah/Celestial landscape use their verified 1.180 renderers. Large system type retains AccessibleHome.

Miqaat: full-width scene with pointed gold arch in right half, calm six-row left panel, grouped next-prayer hero, full-width timeline and one four-action bottom dock. Prayer Gallery: botanical ivory paper, unified painting/name/clock/action hero and six illustrated cards in a 3x2 grid. All content is live native Compose. Source concept typography/art details are reproduced using clean artwork layers; generated concept text is never shipped as UI.

Native review covers 1280x800 and 960x600, English/Urdu, absolute/relative clocks, 24-hour/five-prayer settings, 110% font, overnight and iqamah/Ramadan states. All details, end times, NOW/NEXT states, destinations and contextual notices remain functional. Tests assert text ink, row/action bounds, grid order and full-month Hijri dates. Existing retained Gallery tests remain required.

Protection: 72 approved phone landscape captures plus 28 before/after captures of phone portrait, tablet portrait and deferred tablet landscape themes. The existing bounded non-text timeline antialiasing allowance applies; no other pixel changes are accepted.

Art assets prepared with the built-in image-generation skill from the two approved tablet concepts, with prompts limited to removing UI and reconstructing clean artwork. Names: tablet_miqaat_181.webp, tablet_gallery_atlas_181.webp and tablet_gallery_paper_181.webp. Original artwork and launcher resources remain intact.

## Artwork prompts and saved source assets

Built-in image generation was used, then the PNGs were encoded as WebP for Android. Saved project paths:

- `app/src/main/res/drawable-nodpi/tablet_miqaat_181.webp`
- `app/src/main/res/drawable-nodpi/tablet_gallery_atlas_181.webp`
- `app/src/main/res/drawable-nodpi/tablet_gallery_paper_181.webp`

Reference concepts: conversation images `exec-7edf40ce-fcf1-4b4a-b897-6577f853abd6.png` (Miqaat) and `exec-47eebdd8-7d4a-4821-a09e-4916d0abae86.png` (Prayer Gallery). Generated artwork is an approximation of raster concept art; native screenshot comparisons disclose the resulting differences rather than asserting pixel identity.

### Miqaat artwork prompt

Use case: precise-object-edit. Production Android tablet artwork layer, not a mockup. Edit the attached approved Miqaat tablet concept. Return ONLY the full 16:10 background artwork with NO interface, NO text, NO Arabic, NO numerals, NO buttons, NO icons, NO prayer table, NO lines or timeline, NO bottom navigation, NO panel outlines. Preserve the exact approved scene: deep midnight navy starry sky, luminous gold pointed architectural arch occupying the RIGHT HALF, palm silhouettes left within the arch, a crescent on the RIGHT within the arch, mosque dome/minarets and warm golden lights in the lower right, reflections across the bottom. LEFT HALF remains dark navy quiet with very subtle stars and water near bottom, so a native prayer list can sit there. Remove all UI and reconstruct only the sky/architecture/water beneath it. Do not reposition the arch, do not introduce new motifs. Match the artwork in the reference as closely as possible. Entire output edge to edge landscape 16:10, no device, no labels.

### Prayer Gallery atlas prompt

Use case: precise-object-edit. Production art atlas extracted from approved tablet Prayer Gallery reference. Return ONLY the SEVEN paintings from the reference, no UI, text, numerals, labels, borders, buttons, leaves surrounding the page or paper page. Exact layout for output: landscape 3:2 atlas, top HALF one wide painting showing the reference's daytime grand mosque courtyard, green domes, symmetrical minarets, reflecting pool, flowers and palms. Bottom half divided into THREE EQUAL COLUMNS and TWO EQUAL ROWS, seven paintings total including hero. First row below hero: Fajr blue alpine lake/pine mountains, Sunrise orange rolling hills/palms sunrise, Dhuhr bright green dome mosque/palms. Last row: Asr warm sunlit mosque courtyard/trees, Maghrib purple mosque silhouettes and brilliant orange sunset, Isha blue night mosque/crescent/palms. Preserve each source painting's subject, colours and watercolor illustrated style; extract/reconstruct as faithfully as possible from the reference. Panels meet with NO gaps, NO rounded corners, NO separating lines. All panels are paintings only. No lettering of any kind.

### Prayer Gallery paper prompt

Use case: precise-object-edit. Extract ONLY the decorative page background from this approved tablet app mockup. Return a landscape 16:10 pale warm ivory watercolor parchment background, matching the subtle cream/gold paper texture in the reference. Preserve delicate realistic olive branches/leaves in the top left corner and top right corner and a few olive leaves down the upper right edge, as in the reference. Remove every other element: NO mosque paintings, NO landscape art, NO cards, NO dividers, NO outlines, NO icons, NO text, NO numerals, NO labels, NO UI. Centre and bottom must be almost plain ivory paper with a very subtle watercolor wash, quiet for native UI. Same restrained botanical style, exact light cream palette. Flat edge to edge art asset.
