# Friday option 2 implementation — testing only

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
