# CA2 - active foreground implementation

The user accepted moving on from the current KA2 build and requested CA2 to match
its approved mockup at least 99.90%, in this session without scheduling.
Reference: libfile_df3909d2d094819193b975dbc393076d.
Baseline application: 4a584944a52fd72099a5248e6941bc0ceed5d19b; native review 41512793769.
MA2 and KA2 remain frozen. CA2 changes are theme-scoped; GA2 unchanged.

Baseline differences: wrong dhikr/floral band, excessive bold clock, missing
seconds, redundant prayer-name heading, missing compact Friday line, flat
header/table exterior, stacked landscape/tablet navigation and incorrect icons.

Implement a reference-traced curved gold band, live native dhikr and seconds,
Arabic/kicker/regular-clock hierarchy, celestial background continuity, compact
header, reference prayer icons, fine-bordered actions and inline navigation.
Preserve all engine/Iqamah/end facts, callbacks, 48dp rows/actions and Friday
lifecycle/reduced motion. Run all 32 CA2 profiles plus live tick; preserve all
MA2/KA2 assertions. Inspect actual native renders before signed testing delivery.
Do not invent a 99.90% similarity result; reference uses illustrative facts and
native app retains functional metadata/accessibility targets. No production push.

## First native review

Application source: 7dfdcab906886689e828d13a46574665c590a1f6.
Focused CA2 run 41554830390 passed both tests: the 32-profile readability matrix
and the live countdown tick/fact-preservation test. Every matrix PNG was reviewed.
The prior source 1d357ced00889f2d47befd9ff29faab4fd9f4398 also passed MA2 run
41554291055 and KA2 run 41554292900; the final change is CA2-only Amiri budgeting.

The first matrix caught clipping of the Friday label in normal Urdu tablet
portrait (12h and 24h). The fixed source reserves Amiri glyph extents before
fitting the main clock; the same assertions now pass without being weakened.

Visible limitations: landscape header still sits over the scene rather than
spanning the table; native rows retain additional metadata, the footer lacks the
reference outline, and the comet glow/art crop differ. The approved board uses
illustrative proportions and data. No 99.90% similarity result is established.
The comparison PDF preserves source/capture aspect ratios and discloses these
differences. This is a review candidate, not visual acceptance or production.

## Verified testing package

Full signed run 41554830363 succeeded for application source 7dfdcab906886689e828d13a46574665c590a1f6.
JUnit: 193 tests, 188 passed, 5 expected opt-in visual audits skipped, zero failures/errors.
Release lint, certificate checks, 16KB alignment, bundle validation and asset checks passed.
Testing APK: Miqaat-CA2-Test-7dfdcab.apk (62859047 bytes).
SHA-256: 5c29f1056d8340260c8681c0241d4a17cbbc8fb613ffad19a57bab68b24aae2f.
Signing certificate SHA-256: 6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01.
Comparison: Miqaat-CA2-Current-Comparison.pdf, 8 pages.
No numerical fidelity acceptance, production promotion or scheduling.
