# Friday option 2 implementation — testing only

## Current: four-theme corrected testing delivery - 6 November 2026, Sydney

MA2, KA2, CA2 and GA2 are corrected together at app source
`4f867dfd854cfb2c6168220879d0d6845c80a6cb`. Full signed gate `42077814071`
passed: 190 tests passed, 5 expected opt-in skips, zero failures/errors, including
all four 32-profile matrices and the quiet regression. Release integrity checks
passed and the downloaded APK matches the source/run provenance, checksums and
established certificate. See `ALL_THEMES_PARITY.md` for fixes and package hashes.

The combined 22-page latest comparison and exact-source APK replace the earlier
per-theme testing delivery as the current review pair. The report shows remaining
responsive/illustration differences; no 99.90% numerical fidelity claim is made.
Earlier freeze notes below are historical and superseded by the user's request
to fix all four themes. No scheduling or production promotion.

## Historical GA2 testing delivery - 5 November 2026, Sydney

The user moved on from the CA2 review build and requested the next theme.
GA2 / Prayer Gallery is active in this session without scheduling.
MA2, KA2 and CA2 remain frozen; see `GA2_ACCEPTANCE.md`.

GA2 source `53234ea3f7ff8602a4d9c1e66342477c1d28ea42` passed focused run
`41578218125` and full signed gate `41578218209`: 190 tests passed, 5 expected
opt-in tests skipped. All three earlier-theme focused regressions passed.
The signed APK and 9-page comparison are the current review delivery.
The report discloses remaining illustration/layout differences; 99.90% visual
similarity has not been established. See `GA2_ACCEPTANCE.md` for package hashes.

## Previous theme: CA2 review delivery

User asked to move on from KA2 and complete CA2 in this session, with at least
99.90% mockup fidelity. No scheduling. Preserve MA2/KA2; see `CA2_ACCEPTANCE.md`.
A numerical fidelity claim requires evidence, not passing layout tests alone.

CA2 first review candidate is source `7dfdcab906886689e828d13a46574665c590a1f6`.
Focused 32-profile/live-tick run `41554830390` and full signed gate `41554830363`
passed (188 tests passed, 5 opt-in audits skipped). The 8-page comparison shows
remaining landscape-header, table-density, footer-outline and artwork differences.
99.90% similarity is not established; this is a testing review candidate.
See `CA2_ACCEPTANCE.md` for package hash and validation evidence.

## Historical KA2 corrected testing delivery - 3 November 2026, Sydney

Source `4a584944a52fd72099a5248e6941bc0ceed5d19b` passed focused KA2 run
`41512793270`, MA2 regression `41512793439`, and full signed gate `41512793769`.
The reference-traced embroidery, regular clock, stacked landscape heading,
portrait arch crop/clock alignment, PM ratio and phone shortcut sizing are in
the verified APK. The 8-page comparison and exact-source APK are delivered;
see the final delivery section of `KA2_ACCEPTANCE.md` for hashes and identities.
This is testing delivery, not user visual acceptance, a 100% fidelity score,
or production promotion. MA2 is frozen and CA2/GA2 unchanged. No hourly tasks.


## Previous visual correction request - 3 November 2026, Sydney

The user marked the delivered KA2 PDF and rejected the simplified Arabic banner
border and hero composition. KA2 is active again; CA2/GA2 must wait. The previous
source remains a tested baseline, not visual acceptance. See the reopened review
in `KA2_ACCEPTANCE.md`. New embroidery/typography corrections await validation.

## Earlier user decision - 3 November 2026, Sydney

The user accepted MA2 as-is after reviewing its comparison PDF and explicitly
asked to stop spending time on MA2 and start the next theme. Freeze MA2 at
`1d57ba517354e4f2b3b7bfd8cee02abdf2e6fe13`. Both focused native run
`41430245994` and full signed gate `41430245963` succeeded.

KA2 native implementation and signed testing validation are complete at
`77549fc66107c9548c86b3199110e72932697521`: focused run `41500671146`, MA2
regression `41500671211` and signed gate `41500671223` all passed. The seven-page
comparison and exact-source APK retain real facts and 48dp targets; this is not an
exact-pixel fidelity or production-promotion claim. See `KA2_ACCEPTANCE.md`.
Next theme: **CA2**, then GA2. The user requested foreground work; the Friday
hourly automation is paused. Historic MA2 residuals do not
block this transition and must not trigger more MA2 styling changes.

User approved MA2 / KA2 / CA2 / GA2 responsive mockups on 30 October 2026 (Sydney).
Phone portrait, phone landscape and tablet portrait; retain tablet landscape.

## References
- MA2: ChatGPT Library libfile_d5d9017892b08191a893fc42f04a1ee4
- KA2: libfile_3fdc1c26b65c819197810f2602147905
- CA2: libfile_df3909d2d094819193b975dbc393076d
- GA2: libfile_fcd6ab14b7f48191a06319fad6bd9658

## Implementation
- Isolated from production main; based on validated Kiswah tablet repair 68ab8e0.
- Friday routing also covers short landscape phones.
- Native mihrab light reveal: 1s fade in, 6s hold, 1s fade out, 292s rest.
- Lifecycle suspends the reveal offscreen; reduced motion uses a static outline.
- Native Al-Kahf and Salawat actions replace the old three Friday cards and duplicate details button.
- Prayer hero stays clickable for details; buttons open their corresponding Friday section.
- Friday theme palette follows approved reference boards; weekday/quiet palettes unchanged.
- Existing background paintings reused provisionally; exact artwork parity requires visual review.

## Required before delivery
1. Cloud unit tests/lint/signed build; retain all clipping/touch/language assertions.
2. Inspect actual Friday PNGs for each theme, both languages and all sizes, 12/24h.
3. Compare normal weekday screenshots to base; check Friday transition boundary and reduced motion.
4. Correct material differences, including retained legacy spacing/background crops.
5. Deliver actual/reference PDF and signed test APK with source and certificate provenance.

This commit starts validation. It does not assert exact visual parity or completion.
No production promotion is authorized by this implementation request.
