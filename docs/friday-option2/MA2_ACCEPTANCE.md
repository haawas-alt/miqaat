# MA2 reference acceptance — accepted by user

**3 November 2026, Sydney:** user accepted MA2 as-is and requested the next
theme. Accepted source: `1d57ba517354e4f2b3b7bfd8cee02abdf2e6fe13`, focused run
`41430245994` and full signed gate `41430245963` both passed. Preserve MA2.
The historic gaps below are a record, not an active correction queue. KA2 is active.

Baseline: 2e7d92932b13b55d2ddc4c95a1c1866287f3333d; signed validation run 41409526373 succeeded.
Reviewed full native English captures: approved375x812, approved812x375, approved768x1024, tablet-landscape.
Reference: libfile_d5d9017892b08191a893fc42f04a1ee4 (complete original 1024x1536 PNG).
The reference contains phone portrait, phone landscape and tablet portrait. Tablet landscape adapts the phone landscape composition.

## One batch from the matched review

| Gap in baseline | Correction in this batch | Evidence still required |
| --- | --- | --- |
| Generic rounded enclosing timetable card; red border around selected row | MA2 uses unframed ivory rows with only the green selected fill and fine separators | Four native layouts and Urdu 115% |
| Terracotta navigation icons | Forest green navigation icons, preserving callbacks and target sizes | Four native layouts |
| Moon for Fajr and inconsistent daytime symbols | Orange sun/twilight sequence; moon retained for Isha | Four native layouts |
| Plain thick dhikr strip, lacking reference end geometry | Narrower MA2 strip, serif English labels, gold geometric ends | English/Urdu and 115% glyph fit |
| Urdu clock pre-shrunk despite available room; small PM suffix and uppercase Next Prayer | Same MA2 display hierarchy in EN/UR, revised suffix proportion and sentence-case kicker; width fitter retained | 12/24h, Urdu, 115% fit |
| Active prayer time has same weight as other times | Bold active MA2 time | Changing current/next state |
| Review captures only published after APK/AAB validation | Upload full native captures immediately after all unit tests; signing/lint/integrity gates remain | Workflow success |

## Material remaining differences — not accepted as finished

- Phone landscape header remains in the hero column. Full-width header, six 48dp rows and dock cannot share 375dp height without changing the composition. Do not fake or shrink touch bounds.
- Phone landscape's existing 48+32dp header budget is now one painted 80dp panorama. This removes the isolated 32dp crop; inspect native minaret fit before acceptance.
- Header location pin restored within a 48dp clickable location row; settings uses MA2 green. Real alert control remains, unlike the illustrative reference clock icon.
- Hero painting remains a reconstruction, with different courtyard/arch geometry from the approved artwork.
- Arabic/Latin/clock spacing and portrait proportions still need reference verification after this batch.
- Additional real Iqamah/end metadata and tablet bilingual names remain intentionally visible; their alignment must be reviewed.

KA2/CA2/GA2 are frozen until MA2 is visually accepted. No assertion changes, no production promotion.
No claim of completion or percentage follows from a passing build.

## Follow-up from actual d6d218f captures

Run 41419322663 failed two assertions: Urdu tablet hero clock was 983px wide inside a 979px node. Assertions remain unchanged. Fix fits the shaped string iteratively with a two-dp inset, and reduces only the Urdu translated suffix ratio while keeping the display number hierarchy. English MA2's PM remains reference-sized.
Native review also confirmed the new geometric band ends drew outside their bounds; the Canvas is now explicitly clipped.
Added a MA2-only 115% matrix covering all four layouts, EN/UR and 12/24h with the same fact, clipping and 48dp assertions. The focused preview runs this matrix for fast feedback; the signed-candidate workflow still runs the entire suite and all release gates.

The focused MA2 matrix also captures normal-font 12h in all four layouts and both languages (in addition to the complete 115% 12/24h matrix). Review can therefore use current normal-font native captures without waiting for signed packaging. Superseded focused preview jobs cancel automatically; the full signed gate remains separate.

## Current native review follow-up

The 579ae3d focused 115% matrix passed (run 41422657924). Native captures still showed
an undersized tablet portrait display clock, especially in Urdu: the table reserved
64dp per Urdu row and the hero reserved an additional 26sp without rendering that gap.
MA2 now uses 56dp Urdu tablet rows (still above the 48dp minimum), a smaller Arabic
heading in a constrained tablet hero, and reserves the actual Friday line height.
All text, real Iqamah/end metadata and existing clipping/touch assertions remain.
This needs new native confirmation before acceptance.

The afternoon glyph is distinct from the noon sun and the English countdown now
reads as one sentence. Signed validation cancels superseded branch runs going
forward; only the latest candidate is relevant for delivery.
