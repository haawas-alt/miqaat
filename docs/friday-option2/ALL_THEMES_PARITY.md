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


## Follow-up mockup refinement - 6 November 2026, Sydney

The user requested quick corrections after the explicit 7.9/10 visual review.
This reopens the listed proportions, landscape and styling gaps together.

- Compact phone landscape retains the global header and banner, but places its
  navigation under the hero. The prayer column uses the freed height. Every
  approved 812x375 profile now asserts all six complete 48dp rows simultaneously,
  without scrolling, with no overlapping adjacent targets. Actual smaller or
  heavily inset viewports scroll only when their table is shorter than 288dp.
- MA2's shallow header restores its skyline and proportionate, softly blended
  palm details. Its Arabic/clock/PM proportions and shortcut styling are refined.
- KA2's main arch preserves source proportions rather than stretching wide.
  Its embroidery remains a separate strip; the Arabic heading is larger and the
  clock/countdown balance is reduced. Banner interior vines are more restrained.
- CA2/GA2 use a larger Arabic heading and smaller clock cap. The Urdu compact
  layout activates before those larger labels can crowd out the clock. Portrait
  actions move closer to the Friday facts with visible courtyard beneath them.
- Native shortcut gradients, stronger gold outlines, filled book icons, brighter
  selected prayer gradients and larger navigation glyphs are shared across themes.
  Enlarged Urdu tablet facts retain 64dp buttons; English tablet buttons use 56dp.
  Compact Urdu navigation reserves room for its actual native line heights.

All changes preserve engine facts, Iqamah/end data, live ticks, actions, 48dp
minimum prayer/shortcut targets and the existing weekday/quiet implementations.
Source and final verification evidence are recorded below after the gate.
The compact navigation placement is an explicit responsive adaptation, not an
assertion that the illustration and native implementation have identical pixels.


### Focused verification of the final refinement source

App source: `b34211cfd70e294611eb161db05eaba88158e4a2`.
All 128 final-source native profiles passed and their captures were inspected.

| Theme | Focused run | Profiles |
| --- | --- | --- |
| MA2 | 42138646469 | 32 |
| KA2 | 42138646402 | 32 |
| CA2 | 42138646475 | 32 |
| GA2 | 42138646340 | 32 |

The checks retain real prayer/Iqamah/end facts, live ticks, text overflow checks,
clock dominance, full-width banners, 48dp targets and the new simultaneous
six-row visibility/non-overlap assertions at the approved landscape viewport.
The combined report uses all four final-source capture sets, with the earlier
`4f867df` landscape shown only as the before image. Signed gate `42138646437`
is the final exact-source release verification; package results follow below.


### Final verified refinement delivery

App `b34211cfd70e294611eb161db05eaba88158e4a2` passed full signed gate `42138646437`.
Actual JUnit reports: 190 passed, 5 expected opt-in skips,
zero failures and zero errors. All four native profile matrices and the quiet
regression are present in those reports. Release lint, signer verification,
permissions/target SDK, forbidden-asset checks, bundle validation and APK/native
page alignment passed. Downloaded candidate provenance matches the source and
run; the APK hash matches `SHA256SUMS` and the established certificate.

APK: `Miqaat-All-Themes-Test-b34211c.apk` (63040941 bytes).

SHA-256: `42ae88be0125cb64b29620614a8bfce36b564b13c37eb1ff7992b5ffe06c0613`.

Certificate SHA-256: `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`.

The updated 22-page latest comparison contains current native captures for all
four themes, approved reference pairs, Urdu 115%, tablet landscape, before/after
landscape, banner details and exact-source validation. Compact phone landscape
navigation sits below the hero, while six full 48dp prayer rows fit the approved
375dp viewport. Below 288dp of actual prayer-table space, a real scroll fallback
remains. This adaptation is disclosed in the report. No numerical visual-match
claim, production promotion or scheduled task is part of this delivery.
