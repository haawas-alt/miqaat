# Option A — current Friday homepage implementation and audit

Version A-1.2, 18 November 2026. This is the current implementation record, **not** an approval of visual parity. Approved reference: `../prayer-gallery-friday-redesign/premium-concepts-v3/A-garden-procession.png`. Scope is the Prayer Gallery **Friday main homepage**. The ordinary weekday screen remains on its existing route. The board uses illustrative data and drawn device shells, so pixel measurements below are source-image coordinates or app dp coordinates, as labelled.

## Artwork and positions

Four approved-board rasters are bundled as `prayer_gallery_friday_approved_phone_portrait.png`, `...phone_landscape.png`, `...tablet_portrait.png` and `...tablet_landscape.png` in `drawable-nodpi`. `BitmapPainter` selects the garden regions at runtime. Prayer data and controls remain live. The phone-landscape source contains an illustrative clock; the live clock and footer cover it. Check the edges for an exposed duplicate ornament.

| View | Board image crop `[x,y,w,h]` px | Garden box `[x,y,w,h]` dp | Title box dp | Clock box dp | Prayer box dp | Actions box dp | Footer box dp |
|---|---:|---:|---:|---:|---:|---:|---:|
| Phone portrait 390×844 | `[22,170,616,320]` | `[10,80,370,190]` | `[27,228,336,66]` | `[18,294,354,92]` | `[14,386,362,300]` | `[14,686,362,87]` | `[0,775,390,69]` |
| Phone landscape 844×390 | `[550,20,770,455]` | `[330,0,514,390]` | absent | `[598,165,232,190]` | `[14,42,326,298]` | absent | `[0,340,844,50]` |
| Tablet portrait 768×1024 | `[22,125,626,220]` | `[18,70,732,292]` | `[90,305,588,100]` | `[24,415,340,220]` | `[20,665,728,238]` | `[380,415,360,220]` | `[0,922,768,102]` |
| Tablet landscape 1280×800 | `[580,80,700,430]` | `[530,50,750,460]` | `[570,400,660,100]` | absent | `[20,120,500,370]` | `[20,515,1240,205]` | `[0,720,1280,80]` |

Headers are `[0,0,390,80]`, `[0,0,844,44]`, `[0,0,768,70]` and `[0,0,1280,80]` dp in the same order. All positions are relative to `safeDrawingPadding()`. The four layouts are selected by shortest side ≥600dp and width greater than height, then scaled to the actual safe viewport. Artwork uses crop, not stretched pixels. At phone portrait font scale ≥1.1, art/title/clock change to `[10,80,370,180]`, `[27,218,336,64]`, `[18,282,354,104]`.

The phone and tablet title backgrounds are separate text-free ornamental WebP images; text overlays remain live. Six prayer thumbnails and three action icons are distinct images. Tablet-landscape cards use three distinct reference-derived scene images. The phone portrait gold timeline has six nodes. The tablet portrait has a gold center divider.

## Current colors, fonts and sizes

| Element | Value |
|---|---|
| Ink / paper / page | `#073B34` / `#FFF9EE` / `#F8F3E8` |
| Terracotta / gold / selected sage / border | `#A9472D` / `#C99845` / `#DCE8D8` / `#D9C9AA` |
| English body/wordmark | Android serif; wordmark 18sp bold |
| Numeric clock | bundled `sanctuary_clock` |
| Arabic | bundled `amiri_regular` and `amiri_bold` |
| Header location/date | portrait 14/11sp; landscape 13/10sp; header time 15sp portrait, 14sp landscape |
| Title main/subtitle | phone 26/11sp; tablet 32/15sp; Urdu title 24sp phone, 29sp tablet |
| Clock prayer name/numeral/suffix | phone portrait 17/50/18sp; phone landscape 19/53/18sp; tablet portrait 24/72/24sp |
| Clock countdown | 12sp phone portrait, 13sp phone landscape, 16sp tablet portrait |
| Prayer name/Arabic/time/end | phone portrait 15/12/15/10sp; phone landscape 14/12/14/10sp; tablets 20/16/20/12sp |
| Action title/subtitle | phone 13/9sp; tablet 20/13sp |
| Footer labels | phone landscape 11sp; others 12sp |

At ≥115% text, phone portrait clock numeral is 45sp and action title/subtitle 12/8sp. Urdu uses Amiri for Arabic labels and a compact header date. Times keep left-to-right reading order in Urdu. The app's clock setting controls 12/24-hour display. All six prayer rows, the selected state, end and Iqamah times come from `PrayerEngine`; footer and action handlers reuse existing navigation.

## Whole-screen discrepancy audit before A-1.2 captures

| View | Board → prior Compose capture → correction / remaining check | Severity |
|---|---|---|
| Phone portrait | Different mosque architecture, title too wide, compact clock, row icons/text too far left, missing gold nodes, different footer icons → board art crop, narrower title, row inset/miniature and timeline added. Clock and footer still need review. | P1/P2 |
| Phone landscape | Mosque too large, wrong pool crop, plain clock outline, crowded footer, different row typography → its own board art and scalloped clock outline. Check for exposed baked clock and footer collision. | P1/P2 |
| Tablet portrait | Mosque composition, wide title, small clock within its card, sparse prayer/action/footer typography → taller board crop and narrower title. Art sharpness, clock and typography still need review. | P1/P2 |
| Tablet landscape | Mosque/palms too large, wide title, soft scene-card art, undersized caption area, left rail/footer differences → art crop excluding baked gear, narrower title and taller action caption. Scene sharpness and rail/footer still need review. | P1/P2 |

The approved board's viewport ratios differ from the requested Android viewports, and source artwork must be enlarged on high-density Android screens. Registered overlays and color difference maps are therefore diagnostic, not a 99.9% score. Reinspect all four **new** captures, both languages, 115% text, 24-hour clock and weekday routing before changing this audit or claiming fidelity.
