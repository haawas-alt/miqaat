# Option A — Garden Procession | Friday homepage design contract

> Historical A-1.1 proposal. Several coordinates and font values below differ from the current app. Use `OPTION-A-IMPLEMENTATION.md` for the current Friday-only implementation and unresolved visual audit.

Version `A-1.1`, 18 November 2026. Scope: Prayer Gallery's Friday **main homepage**. The four pictures in `A-garden-procession.png` are the artistic reference. Version A-1.0 contained layout coordinates that contradicted those pictures. The four native captures at `ci-option-a-shots-d4044a3e1101` exposed that error. This revision records the composition visible in the approved board; the values remain implementation targets rather than measurements extracted from AI-generated pixels. New native captures are required before approval.

Coordinate system: logical dp inside Android's safe drawing area, origin top-left, x rightward, y downward. Viewport dimensions are design targets. Each box is `[x,y,w,h]`. Background and decorative layers can overlap foreground boxes intentionally. Child text coordinates are relative to the parent box. All touch areas at least 48×48 dp, even when the drawn icon is smaller.

## Visual source, assets and tokens

- Art source: `../artwork/option-a-friday-garden.png` (the garden and reflecting pool). Retain its blue sky, floral foreground, minarets, central dome and pool. `cover` scaling, no stretch.
- Prayer miniatures: use the six illustrated prayer-period assets already used by the standard Prayer Gallery home, in order Fajr, Sunrise, Jumu'ah, Asr, Maghrib, Isha. Do not substitute circles or repeated images.
- Main ink `#073B34`; secondary ink `#41625A`; paper `#FFF9EE`; background `#F8F3E8`; terracotta `#A9472D`; selected sage `#DCE8D8`; antique gold `#C99845`; divider `#D9C9AA`; muted `#766E63`.
- Gradients: art scrim, transparent→`#173C34` at 12% only behind overlaid type; paper plaques `#FFF9EE` 96% opacity. Do not darken the whole artwork.
- Typography (normative, from app bundled fonts): English display `sanctuary_display.ttf`; time `sanctuary_clock.ttf`; Arabic `amiri_regular.ttf`; UI body `nunito_sans.ttf`. English title 26sp / 29 line height, 0.02em tracking; Arabic title 23sp / 30 line height; hero time 49sp / 52; AM/PM 17sp; prayer name 13sp semibold; Arabic row 12sp; row time 15sp; secondary/status 10sp; action title 13sp; action subtitle 10sp; navigation 11sp. Phone landscape compact title 18sp, hero time 34sp, row name 11sp, row time 12sp. Tablet hero time 58sp; tablet title 31sp; tablet rows 15sp/17sp. No text rasterized into artwork.
- Minimum readable contrast: 4.5:1 for body text. Decorative gold is not used as small body text. Border 1dp, plaque radius 12dp, action radius 12dp, selected row radius 10dp. Icons use the app's existing vector set; unique book, tasbih, reminder, Qibla, Adhkar, Learn and Timetable symbols.

## Viewport boxes (dp)

| Region | Phone portrait 390×844 | Phone landscape 844×390 | Tablet portrait 768×1024 | Tablet landscape 1280×800 |
|---|---:|---:|---:|---:|
| Header | `[0,0,390,80]` | `[0,0,844,44]` | `[0,0,768,70]` | `[0,0,1280,80]` |
| Garden artwork | `[10,80,370,180]` | `[300,0,544,390]` | `[18,70,732,292]` | `[480,0,800,640]` |
| Friday title plaque | `[30,245,330,62]` | absent | `[90,320,588,75]` | `[620,520,600,110]` |
| Next prayer/clock | `[18,310,354,92]` | `[598,137,232,226]` | `[24,415,340,220]` | absent |
| Six-prayer area | `[14,420,362,234]` | `[14,46,300,288]` | `[20,665,728,238]` | `[20,140,500,490]` |
| Friday actions | `[14,665,362,98]` | absent | `[380,415,360,220]` | `[20,632,1240,110]` |
| Navigation | `[0,775,390,69]` | `[0,340,590,50]` | `[0,922,768,102]` | `[0,744,1280,56]` |

Phone landscape has no separate Friday title or action cards: its clock card overlays the garden at the lower right, while navigation sits below the prayer rail. Tablet landscape has no large clock card: the artwork, ornamental Friday plaque and three illustrated action cards carry that composition. These differences are visible in the approved board and were missed in A-1.0.

## Responsive geometry and content

### Phone portrait

Header: MIQAAT mosque mark and serif wordmark at left; location and date beside it; live clock and settings at right. Artwork uses focus `(0.50,0.49)` in normalized source coordinates. Ornamental plaque overlaps artwork bottom by 15dp. The clock card sits directly below, with a large numeric time and smaller AM/PM, countdown pill and true configured Iqamah. Six compact rows follow a gold vertical timeline; each shows a circular distinct prayer image, English/Arabic labels, live time, end/Iqamah time and passed/next/future state mark. Selected Jumu'ah row has sage fill. The three action cards use a gold-backed book, beads and reminder icon, each with title and subtitle. Navigation has four destinations.

### Phone landscape

Header spans the top with MIQAAT and location/date at left and live clock/settings at right. Prayer rail x=14..314 has six compact rows with distinct round thumbnails and status marks. Artwork begins x=300 at the top and preserves central dome and pool. One tall clock card overlays its lower right. There is no separate Friday title plaque or three-card action row in this view. Navigation spans the lower left. All six prayer rows remain visible at 115% text; secondary descriptions may shorten, prayer names and times must remain readable.

### Tablet portrait

Art is a top panorama. An ornamental title plaque crosses its lower boundary. Below it the clock card occupies the left half while Al-Kahf, Salawat and Friday reminders form a vertical stack on the right. Six prayers form a 2-column × 3-row ledger beneath a gold divider. Reading order is Fajr/Sunrise, Jumu'ah/Asr, Maghrib/Isha in English; RTL reverses visual columns while retaining semantic order. Navigation uses four destinations as shown.

### Tablet landscape

Left six-prayer rail has roomy rows and circular distinct thumbnails. Right garden artwork retains mosque and reflecting pool; the ornamental Friday title crosses its lower field. There is no large clock card in this view; the live time remains in the header. Three illustrated Friday action cards span the full width beneath the art and prayer rail, with separate Qur'an, beads and lantern scenes. Navigation spans the bottom. No stretching of the source art: cover crop focus `(0.52,0.51)` and show both minarets where the viewport allows.

## States and interactions

Friday activates using the app's local calendar rule; preview mode remains selectable. Non-Friday Prayer Gallery home uses its existing layout. Render actual prayer data and Iqamah from the app; never hard-code the illustrative board time. Support 12/24-hour clock and English/Urdu RTL, dynamic text at 100% and 115%. If six rows cannot fit, scroll the prayer content while keeping the clock and navigation accessible. Selected row follows actual next prayer, so the Jumu'ah emphasis is conditional. Al-Kahf, Salawat, reminders, prayer rows, location, settings and navigation keep existing actions. Use content descriptions on icons and selected state announcements.

## Acceptance evidence before implementation sign-off

Capture native screenshots at all four viewports and compare them side by side against four deterministic mockups made from this spec. Inspect artwork crop, alignment, text baselines, Arabic shaping, icon identity, button dimensions and selected states at 100% and 115%, English and Urdu, 12/24-hour. Verify Friday entry/exit and weekday screenshot regression. The generated concept board is a visual art direction, not a pixel-perfect golden image. Record any deviation against this contract before claiming fidelity.
