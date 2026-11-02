# Friday option 2 implementation — testing only

## Current KA2 corrected testing delivery - 3 November 2026, Sydney

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
