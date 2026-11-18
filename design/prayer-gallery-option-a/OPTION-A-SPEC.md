# Option A — Garden Procession | Friday homepage design contract

Version `A-1.1`, 18 November 2026. Scope: Prayer Gallery's Friday **main homepage**. The four pictures in `A-garden-procession.png` are the artistic reference. Version A-1.0 contained layout coordinates that contradicted those pictures. The four native captures at `ci-option-a-shots-d4044a3e1101` exposed that error. This revision records the composition visible in the approved board; the values remain implementation targets rather than measurements extracted from AI-generated pixels. New native captures are required before approval.

Coordinate system: logical dp inside Android's safe drawing area, origin top-left, x rightward, y downward. Viewport dimensions are design targets. Each box is `[x,y,w,h]`. Background and decorative layers can overlap foreground boxes intentionally. Child text coordinates are relative to the parent box. All touch areas at least 48×48 dp, even when the drawn icon is smaller.

The approved AI concept board does not draw its four device frames at the aspect ratios printed in its captions. Approximate image-frame ratios are phone portrait 0.39 versus labelled 390/844 = 0.46, phone landscape 2.37 versus 844/390 = 2.16, tablet portrait 0.65 versus 768/1024 = 0.75, and tablet landscape 1.40 versus 1280/800 = 1.60. These are measurements of the concept-board pixels, not Android layout requirements. Direct pixel parity is therefore impossible without distorting the artwork or changing the requested device viewports. Preserve hierarchy and content while adapting geometry to the actual viewport; do not report a numerical parity percentage from unregistered screenshots.

The phone portrait concept visually allocates about 234dp to six compact prayer rows. Six independent 48dp-high rows require at least 288dp before gaps. The implementation now allocates 298dp and compresses the artwork/clock/shortcut area to retain all six visible rows. This is a documented visual tradeoff, subject to native capture and test verification; do not weaken the touch-target test to hide it.

## Visual source, assets and tokens

- Art sources: the original `../artwork/option-a-friday-garden.png` and a scene derived from the approved board. Four Friday-only crops are stored as `prayer_gallery_friday_garden_{phone_portrait,phone_landscape,tablet_portrait,tablet_landscape}.webp`. They retain the blue sky, floral foreground, minarets, central dome and pool. Each crop was selected with a bounded image-difference search, then must be visually reviewed; `cover` scaling, no stretch.
- Prayer miniatures: use the six illustrated prayer-period assets already used by the standard Prayer Gallery home, in order Fajr, Sunrise, Jumu'ah, Asr, Maghrib, Isha. Do not substitute circles or repeated images.
- Main ink `#073B34`; secondary ink `#41625A`; paper `#FFF9EE`; background `#F8F3E8`; terracotta `#A9472D`; selected sage `#DCE8D8`; antique gold `#C99845`; divider `#D9C9AA`; muted `#766E63`.
- Gradients: art scrim, transparent→`#173C34` at 12% only behind overlaid type; paper plaques `#FFF9EE` 96% opacity. Do not darken the whole artwork.
- Typography: English display, headers, labels, body and navigation use the Android serif family visible in the approved board; time uses bundled `sanctuary_clock.ttf`; Arabic uses bundled `amiri_regular.ttf` and `amiri_bold.ttf`. Current phone portrait title is 30sp / 33sp line height, hero time 55sp / 58sp, prayer name and row time 15sp, Arabic row 12sp, action title 14sp. Tablet title is 36sp, tablet portrait hero time 72sp, tablet prayer name 20sp. These are implementation values, not a measured 99.9% match; compare native captures before approval. No text is rasterized into artwork.
- Minimum readable contrast: 4.5:1 for body text. Decorative gold is not used as small body text. Border 1dp, plaque radius 12dp, action radius 12dp, selected row radius 10dp. Icons use the app's existing vector set; unique book, tasbih, reminder, Qibla, Adhkar, Learn and Timetable symbols.

## Viewport boxes (dp)

| Region | Phone portrait 390×844 | Phone landscape 844×390 | Tablet portrait 768×1024 | Tablet landscape 1280×800 |
|---|---:|---:|---:|---:|
| Header | `[0,0,390,80]` | `[0,0,844,44]` | `[0,0,768,70]` | `[0,0,1280,80]` |
| Garden artwork | `[10,80,370,170]` | `[330,0,514,390]` | `[18,70,732,292]` | `[480,50,800,460]` |
| Friday title plaque | `[30,235,330,60]` | absent | `[90,320,588,75]` | `[590,395,660,110]` |
| Next prayer/clock | `[18,296,354,88]` | `[598,165,232,190]` | `[24,415,340,220]` | absent |
| Six-prayer area | `[14,390,362,298]` | `[14,42,326,298]` | `[20,665,728,238]` | `[20,120,500,370]` |
| Friday actions | `[14,691,362,82]` | absent | `[380,415,360,220]` | `[20,515,1240,205]` |
| Navigation | `[0,775,390,69]` | `[0,340,844,50]` | `[0,922,768,102]` | `[0,720,1280,80]` |

Phone landscape has no separate Friday title or action cards: its clock card overlays the garden at the lower right, while navigation sits below the prayer rail. Tablet landscape has no large clock card: the artwork, ornamental Friday plaque and three illustrated action cards carry that composition. These differences are visible in the approved board and were missed in A-1.0.

## Responsive geometry and content

### Phone portrait

Header: MIQAAT mosque mark and serif wordmark at left; location and date beside it; live clock and settings at right. Artwork uses focus `(0.50,0.49)` in normalized source coordinates. Ornamental plaque overlaps artwork bottom by 15dp. The clock card sits directly below, with a large numeric time and smaller AM/PM, countdown pill and true configured Iqamah. Six compact rows follow a gold vertical timeline; each shows a circular distinct prayer image, English/Arabic labels, live time, end/Iqamah time and passed/next/future state mark. Selected Jumu'ah row has sage fill. The three action cards use a gold-backed book, beads and reminder icon, each with title and subtitle. Navigation has four destinations.

### Phone landscape

Header spans the top with MIQAAT and location/date at left and live clock/settings at right. Prayer rail x=14..340 has six compact rows with distinct round thumbnails and status marks. Artwork begins x=330 at the top and preserves central dome and pool. One tall clock card overlays its lower right. There is no separate Friday title plaque or three-card action row in this view. Navigation spans the bottom. All six prayer rows remain visible at 115% text; secondary descriptions may shorten, prayer names and times must remain readable.

### Tablet portrait

Art is a top panorama. An ornamental title plaque crosses its lower boundary. Below it the clock card occupies the left half while Al-Kahf, Salawat and Friday reminders form a vertical stack on the right. Six prayers form a 2-column × 3-row ledger beneath a gold divider. Reading order is Fajr/Sunrise, Jumu'ah/Asr, Maghrib/Isha in English; RTL reverses visual columns while retaining semantic order. Navigation uses four destinations as shown.

### Tablet landscape

Left six-prayer rail has roomy rows and circular distinct thumbnails. Right garden artwork retains mosque and reflecting pool; the ornamental Friday title crosses its lower field. There is no large clock card in this view; the live time remains in the header. Three illustrated Friday action cards span the full width beneath the art and prayer rail, with separate Qur'an, beads and lantern scenes. Navigation spans the bottom. No stretching of the source art: cover crop focus `(0.52,0.51)` and show both minarets where the viewport allows.

## States and interactions

Friday activates using the app's local calendar rule; preview mode remains selectable. Non-Friday Prayer Gallery home uses its existing layout. Render actual prayer data and Iqamah from the app; never hard-code the illustrative board time. Support 12/24-hour clock and English/Urdu RTL, dynamic text at 100% and 115%. If six rows cannot fit, scroll the prayer content while keeping the clock and navigation accessible. Selected row follows actual next prayer, so the Jumu'ah emphasis is conditional. Al-Kahf, Salawat, reminders, prayer rows, location, settings and navigation keep existing actions. Use content descriptions on icons and selected state announcements.

## Acceptance evidence before implementation sign-off

Capture native screenshots at all four viewports and compare them side by side against four deterministic mockups made from this spec. Inspect artwork crop, alignment, text baselines, Arabic shaping, icon identity, button dimensions and selected states at 100% and 115%, English and Urdu, 12/24-hour. Verify Friday entry/exit and weekday screenshot regression. The generated concept board is a visual art direction, not a pixel-perfect golden image. Record any deviation against this contract before claiming fidelity.
