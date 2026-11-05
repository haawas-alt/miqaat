# Reopened four-theme reference correction - 6 November 2026, Sydney

The user explicitly requested all remaining gaps in MA2, KA2, CA2 and GA2 be
fixed. This supersedes the earlier theme-freeze notes for this work.
Baseline app: 53234ea3f7ff8602a4d9c1e66342477c1d28ea42.
Documentation parent: a775e822d562233eaf65650a13add101bb2b4bf8.

## Concrete corrections

- A global landscape header and banner precede both the table and hero, in all
  four themes. The clock no longer shares its column with a second header.
- The landscape dock is inline and 48dp tall. On a short viewport, the timetable
  really scrolls; every prayer still has a non-overlapping 48dp row. This replaces
  the former layout compromise rather than faking or shrinking touch bounds.
- Portrait allocation uses actual shared header/band/dock dimensions once.
  Shortcut space is counted inside the hero, the timetable fills the remainder,
  and the dock is anchored at the bottom.
- CA2 adopts GA2's constrained Urdu reflow so the display clock remains dominant.
  The same minimum 1.5x digit/countdown font hierarchy is checked in every theme.
- MA2 has reference-traced text-free green/gold end panels rather than a generic
  diamond grid. KA2 interior stitch detail and live header seconds are restored.
  CA2 gets its fine comet rail/glow and rounded dock outline; its backdrop samples
  star-only strips so the arch is not repeated above the banner. GA2 uses small
  gold dhikr separators, correctly aligned architecture and full-width chrome.

## Verification

Run 32 profiles per theme (128 total): four layouts, two languages, both clock
formats, normal and 115% text. Assert real facts, clipped glyphs, live ticks,
current/next state, full-width banner position and clock dominance. Prayer checks
scroll the actual row fully into view before checking facts and 48dp bounds.
Preserve all unrelated weekday/quiet behavior and release integrity gates.
Inspect current native captures before final packaging and delivery.

No numerical fidelity percentage is inferred from passing tests. The approved
boards have illustrative data and inconsistent labelled aspect ratios. Match
the reference composition responsively and document any remaining differences.
No scheduling and no production promotion are requested by this correction.

## Final source review

Application source: `4f867dfd854cfb2c6168220879d0d6845c80a6cb`.
The final correction keeps CA2 comet highlights at the edge, outside live text.
The last visual check found dark live MA2 header facts over dark foliage in a
shallow landscape crop. That header now samples the ivory sky; the tall skyline
compositions retain their bottom alignment.

All four focused runs passed at `11c0179`, 32 profiles each. The final source
changes CA2 ornament only; its focused verification is `42077813831`.

| Theme | Focused run | Profiles |
| --- | --- | --- |
| MA2 | 42076601879 | 32 |
| KA2 | 42076601795 | 32 |
| CA2 | 42076601813 | 32 |
| GA2 | 42076601797 | 32 |

Full signed gate: `42077814071`; final package evidence is recorded below once
that exact-source gate has completed. The final full suite includes all four
32-profile matrices and the weekday/quiet tests. The superseded first batch was
`b769de71ee28cab22ed176eebf8a6041a0fbde51`; its four focused previews also
passed, but its full signed run was cancelled automatically by the final source.
Do not distribute the superseded source as the final all-theme package.


### Border detail review

Enlarged reference/native comparisons are included in the combined report.
A final inspection caught CA2 comet highlights crossing the Arabic glyph area;
source `4f867df` confines those paths to the top/bottom rails. Focused CA2 run
`42077813831` passed its 32 profiles and live tick checks. MA2, KA2 and GA2
captures remain from source `11c0179`, whose theme code is unchanged by the
CA2-only final commit. The combined report identifies both capture sources.

Reference boards contain illustrative locations, dates and prayer facts, and
some device labels do not match the image aspect ratio. The report preserves
source proportions instead of stretching the references. Low landscape uses
real table scrolling; every prayer remains available at its full 48dp height.
Differences in responsive crops, metadata density and ornament detail remain
visible in the report. No numerical visual similarity is asserted.


## Final signed testing delivery

Full signed run `42077814071` succeeded at application source
`4f867dfd854cfb2c6168220879d0d6845c80a6cb`.
The actual JUnit reports contain 195 tests: 190 passed, 5 expected opt-in audit
skips, zero failures and zero errors. All four 32-profile matrices and the quiet
screen regression were confirmed in the reports.

Release lint, signatures, Play permission policy, target SDK 36, APK alignment,
native page integrity, AAB signature/configuration and bundle validation passed.
Downloaded candidate provenance matches both the exact source and the run;
its APK SHA-256 matches `SHA256SUMS`. Both APKs retain the established signer.

Testing APK: `Miqaat-All-Themes-Test-4f867df.apk` (63040941 bytes).

APK SHA-256: `c247df967e5b70fc43aef78c2aba0af4a1f3ec070ae959634afe24fda344eb20`.

Signing certificate SHA-256:
`6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`.

The combined 22-page comparison preserves the existing latest-report identity.
It contains all four reference/native comparisons, enlarged Urdu, tablet
landscape, before/after landscape placement, enlarged border detail and this
validation. It distinguishes the unchanged MA2/KA2/GA2 capture source from the
CA2-only final correction. This is a testing delivery, not a numerical fidelity
claim or production promotion. No scheduled task was created.
