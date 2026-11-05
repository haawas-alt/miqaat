# GA2 - verified testing review

The user requested the next theme after the CA2 review delivery, in the same
session without hourly scheduling. GA2 / Prayer Gallery reference:
libfile_fcd6ab14b7f48191a06319fad6bd9658. Base app 7dfdcab906886689e828d13a46574665c590a1f6;
branch documentation head 191ace962bc9f0db0a0bb92e188a36a58f77a75a.
MA2, KA2 and CA2 are frozen at their current review states. No numerical fidelity
claim or visual acceptance follows from the user's request to move on.

Baseline native review 41512793769 / source 4a584944a52fd72099a5248e6941bc0ceed5d19b:
wrong floral/Arabic banner; bold clock with tiny PM; no live seconds; redundant
English hero title; missing Home tab; stacked wide/tablet navigation; repeated
portrait artwork behind the table; dark cropped landscape architecture.

This batch implements a native geometric transliterated/Arabic dhikr band, live
seconds and regular clock, compact real-fact hierarchy, Home selection and
reference nav icons/dividers, fine shortcut borders, twilight icons and smaller
landscape table with retained engine/Iqamah/end metadata. Portrait uses one arch
and a floor-only backdrop; wide art retains the central mosque and right arch.

Verify all 32 GA2 profiles and live tick, inspect native renders, preserve 48dp
prayer/action targets and all prior-theme assertions, then run the signed gate.
The short landscape keeps its header over the scene to fit six 48dp rows; it
cannot reproduce the illustrative global header at the same time. Keep this
limitation explicit in the comparison report; do not invent a 99.90% score.

## First native review corrections

Source 15929d4534c823ea6e2647259e8602ecf7be4a39 / focused run 41575685838
caught four English landscape NEXT-chip clips and one Urdu 115% Sunrise-detail
clip. The subsequent batch widens the status chip and facts column and gives
the landscape table 33% rather than 30% of the row. No assertions are weakened.
Actual renders also exposed a repeated arch peak inside the patterned banner
and vertically stretched floor reflections. The banner now samples only the
side geometry and the portrait backdrop uses the blue central courtyard texture.

## Final testing delivery - 5 November 2026, Sydney

Application source: 53234ea3f7ff8602a4d9c1e66342477c1d28ea42.
Focused GA2 run 41578218125 passed all 32 layout/language/clock/text-scale profiles,
including the live-second tick and current-prayer transition. The main clock's
digit font must remain at least 1.5 times the countdown font. Taller Urdu labels
reflow with Friday and Iqamah on a shared line before the clock can shrink.
Normal and 115% Urdu phone/tablet screenshots were visually reviewed.

Full signed gate 41578218209 passed: 195 tests total, 190 passed, 5 expected
opt-in visual audits skipped, zero failures/errors. MA2 41578218110, KA2
41578218041 and CA2 41578218063 focused regressions also passed. Release lint,
certificate, 16KB alignment, asset and bundle checks passed.

Testing APK: Miqaat-GA2-Test-53234ea.apk (63027009 bytes).
SHA-256: 28a75c86194fccc8eb289b0913c46a7db147fe97adcf626fae34bbebf11e85f7.
Signing certificate SHA-256: 6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01.
Report: Miqaat-GA2-Current-Comparison.pdf, 9 pages, reference/native and before/after
comparisons plus enlarged Urdu, banner/navigation details and build evidence.

Visual limitations remain explicit: short-landscape header placement, extra
real-fact density, artwork crop and ornament details differ from the illustration.
No 99.90% similarity measurement or production promotion is claimed.
