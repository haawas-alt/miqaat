# KA2 Kiswah - active reference review

MA2 was accepted as-is by the user on 3 November 2026 (Sydney). Do not revisit it.
KA2 starts from accepted application source `1d57ba517354e4f2b3b7bfd8cee02abdf2e6fe13`.
Reference: `libfile_3fdc1c26b65c819197810f2602147905`, approved responsive board.

## Review scope

- Phone portrait/landscape and tablet portrait/landscape.
- English/Urdu, 12/24-hour clocks, normal/115% type.
- Reference black/gold masthead, stitched banner, arch/calligraphy composition,
  display-clock/countdown hierarchy, dark timetable and fine-bordered actions.
- Preserve native engine facts, Iqamah/end metadata, callbacks and 48dp targets.
- Keep accepted MA2 and weekday/quiet layouts unchanged. CA2/GA2 remain frozen.

## Validation

Retrieve the exact accepted-source native Kiswah captures before batching
corrections. Run the dedicated KA2 native matrix and inspect its actual renders;
then use the complete signed candidate gate. No assertion weakening or promotion.
An as-built/reference report is a separate KA2 file, never a replacement for MA2.

## First correction batch

Baseline native captures from signed run 41430245963 confirmed the old floral
rectangular banner, heavy Arabic, wrong kicker order, missing seconds countdown,
missing compact Friday label and duplicate background ornament outside the hero.
This batch adds a KA2-only notched gold dhikr band, Amiri headline, kicker above
the display clock, real HH:MM:SS and until/since wording, visible Friday/Iqamah,
black cloth outside the hero, a darker selected table row and single-bordered
actions. All existing MA2 assertions remain in the reusable review matrix.
A separate KA2 matrix and live-tick test must pass before any completion claim.

## Foreground verification - 3 November 2026, Sydney

Implementation source: `77549fc66107c9548c86b3199110e72932697521`.
The user requested completion in the active session rather than hourly checks;
the Friday automation is paused. Do not restart it without a new instruction.

Completed changes include the live seconds countdown, four-phrase native banner,
cream display clock, black/gold timetable, distinct prayer/navigation icons,
inline landscape/tablet navigation, pin/date/Friday header and the complete
right-hand embroidery panel in every viewport. The new header is included in
the strict glyph-clipping matrix; its 115% Urdu tablet overflow was corrected
without removing or weakening assertions.

- KA2 focused run `41500671146`: passed, all 24 profiles and live-tick test.
- MA2 regression run `41500671211`: passed; accepted MA2 styling is unchanged.
- Full signed candidate run `41500671223`: passed; release lint/APK/AAB, bundle, signing, permissions and 16KB alignment checks passed.
- JUnit totals: 191 tests, 186 passed, 5 opt-in legacy export tests skipped, 0 failures/errors.
- Testing APK SHA-256: `60f961ea5fc437a8cf59a5e476f963f7e9698ab24ebd0407f77a518b1869bd5f`.
- Certificate verified against `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`.
- Current comparison identity: `libfile_08d7907f0f98819190fdb0d06ff35176`.

Native captures use controlled Mascot prayer settings, not the reference's
illustrative Makkah data. Functional Iqamah/end metadata and 48dp rows require
responsive spacing; the banner ornament is native geometry. These captures
support a reviewable implementation, not a numerical or exact-pixel fidelity
claim. No production promotion. Keep CA2 and GA2 unchanged until this batch is
validated and delivered. The current native implementation and signed testing
build are validated; the next implementation theme is CA2. User acceptance of
the reference comparison is not implied by passing tests.


## Reopened visual review - user marked PDF mismatch

The user rejected the simple star-ended banner as unlike the approved embroidery,
and marked the portrait/landscape hero composition. Passing layout/build tests
was insufficient. KA2 remains active; do not start CA2 yet.

Correction: replace simplified star caps with native vector traces retaining the original quantized gold/shadow palette of
the text-free embroidery from the approved board's tablet banner. Source bounds
are left (1003,164)-(1051,212), right (1468,164)-(1516,212), in the 1536x1024
reference. Caps stay square and physically left/right in both languages; native
Arabic remains live and independently readable. Restore faint interlaced banner
texture and cream lettering. Make the display clock regular serif with a smaller
nominal size; replace the weight-expanded clock with one centred content stack,
and stack the Arabic heading below the kicker in landscape too. Preserve engine
facts, countdown semantics, actions and all prior clipping/48dp assertions.

These corrections require fresh screenshots and validation before delivery.


### Current correction validation

Application source: `dfe5ddf25488f92193c554878ab3c42fec8ef4c6`.
Focused native run `41509396530` passed all 24 layout/language/font/clock
profiles and the live countdown tick. It also checks square embroidery caps and
keeps live Arabic glyphs between the physical caps in both EN/UR. Manual review
covered all 24 current-source renders, with enlarged approved/native border
comparison on page 7 of `libfile_08d7907f0f98819190fdb0d06ff35176` (version 3).
Full signed gate `41509396797` is in progress. Do not deliver the previous APK
as this corrected build. No user acceptance or 100% fidelity score is implied.


The enlarged PDF also exposed a portrait-specific art distortion: a complete
landscape painting had been squeezed into the portrait hero, leaving a narrow
arch and oversized right strip. Portrait now draws the existing arch crop and
complete embroidery strip independently (15% phone, 20% tablet); the hero text
centre follows the arch peak. Landscape keeps its existing artwork split.
The English PM suffix is restored to 60% of the clock size and phone shortcut
labels/icons enlarged while keeping secondary facts and 48dp targets. The same
8s/5min lifecycle/reduced-motion envelope controls both native image patches.
This additional composition batch needs fresh captures and signed validation.
