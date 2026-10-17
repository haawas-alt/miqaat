# Prayer Gallery remediation verification

## Scope and release identity

This follow-up addresses the independent audit of gallery-tablet-1.162-12 and Claude's audit of 1.162-13. It is not a new audit of all app features or Learn Salah artwork.

- Built source: a91ce7e0ccc880103ad7b51085de321d1bd6c7e5.
- Candidate workflow: https://github.com/haawas-alt/miqaat/actions/runs/39075136186 (run 23).
- Version: 1.163-gallery. Testing prerelease only; main, latest, updater and Play unchanged.
- Backup: backup/gallery-before-audit-remediation-6ab3a6f at 6ab3a6f139a346ad5a976e2669cc3565d26e1867.
- Expected release: gallery-tablet-1.163-23. Final binary verification is recorded below after completion.

## Changes and audit reconciliation

The tablet landscape composition retains Option 3's illustrated 3-by-2 cards, strengthens the clock hierarchy with actual variable-font weight settings, restores each end-time line and explicit information control, uses the warm accent for the details button, and places the complete Gregorian and Hijri dates above the architecture. Existing geometric illustrations remain; this is not an exact reproduction of the generated mockup's painterly scenes.

| Audit issue | Result |
| --- | --- |
| Claude F1 / independent G01: simultaneous current and next | Separate current-window and next-event facts; both markers can be displayed concurrently |
| Claude F2 / G01: next-day Fajr | Tomorrow timetable supplies clock, end, iqamah and explanation; tomorrow Fajr is not marked completed |
| Claude F3: large-font layout differs | Intentional accessible reflow retained above 115%; 130% and 200% can scroll |
| Claude F4: alarm readiness warning | Preserved truthful warning; physical alarm delivery remains unverified |
| Claude F5 / G12: Hijri era | Full month/year plus AH in English and ھ in Urdu; removed oversized tracking that clipped the small-phone date |
| Claude F6: Hijri off | Explicit user preference remains respected |
| Claude F7 / G06: card information control | Separate 48dp target restored; interaction test checks dialog existence |
| G02 typography / G05 palette | Explicit font axes, bold clocks, warm theme accent; tinted-row foreground contrast corrected |
| G03 Ramadan | Fasting progress and Tarawih context restored in the landscape hero; forced-on fixtures test layout, not calendar correctness |
| G07 supporting times | Dayparts included; mixed-direction times isolated |
| G08 RTL | Forward navigation mirrors in normal Gallery, compact landscape and accessible layouts |
| G09 footer | Bounded utility rail; overflow destinations remain actionable through More |
| G10 responsive / G11 dates | Proportional art, compact tablet budgets, separate date strip, inset handling |

## Evidence coverage

| Profile | Raw dimensions | Languages | Text sizes | Evidence |
| --- | --- | --- | --- | --- |
| Phone portrait | 822 x 1782 | English, Urdu | 100%, 130%, 200% | gallery-matrix-phone-portrait-* |
| Phone landscape | 1782 x 822 | English, Urdu | 100%, 130%, 200% | gallery-matrix-phone-landscape-* |
| Tablet portrait | 1600 x 2560 | English, Urdu | 100%, 130%, 200% | gallery-matrix-tablet-portrait-* |
| Tablet landscape | 2560 x 1600 | English, Urdu | 100%, 130%, 200% | gallery-matrix-tablet-landscape-* |
| Small phone / inset checks | 720 x 1600 | English, Urdu | 100% | detailed-gallery/*360-insets* and gallery-small-phone-full-hijri.png |
| Compact tablet | 1920 x 1200 | Urdu | 100% | gallery-small-tablet-ur-long-date.png; gallery-small-tablet-ur-ramadan-night.png |

The matrix contains 24 initial viewport captures and 16 scrolled NEXT-clock captures at enlarged text sizes. Additional evidence covers 12/24-hour clocks, simultaneous states, overnight Fajr, long Urdu dates, Ramadan context, and four phone times of day with/without insets. Seventy PNGs are retained. Some duplicates are deliberate state/profile evidence.

Raw screenshot review confirmed the small-phone full Hijri fix, readable landscape clocks, both current/next states and no normal-size tablet landscape card/footer overlap. Tablet portrait's generous hero spacing is retained from the existing composition; it is not the landscape grid. Enlarged text intentionally exceeds one viewport rather than shrinking text.

## What the tests prove

The tests exercise state logic, tomorrow event dates, RTL reading order, actual details/menu callbacks, text line extents, dropped-line detection, card containment and the small-phone full date. Native Robolectric uses SDK 34; these are not emulator or physical-device tests. A calibrated layout check uses shaped glyph extents rather than a known misleading cached paragraph-width flag, with one physical pixel of rounding tolerance. Screenshot capture alone is not a fidelity assertion.

The legacy opt-in ScreenshotTest remains skipped. New Gallery checks are not skipped. The Android content-view capture does not include separate popup/dialog windows: dialog existence and menu destination callbacks are asserted, but these PNGs cannot certify their visual appearance.

## Remaining acceptance limits

- Physical phone/tablet installation and upgrade, TalkBack reading order, contrast on real displays, system font/display-size combinations, device-specific insets, audio and alarm delivery have not been certified.
- No qualified Urdu editor or Islamic scholar has signed off through this work. No new religious ruling or prayer calculation was introduced.
- No Play Console checks, release AAB submission or production rollout occurred.
- The work is suitable for internal candidate testing after the final run passes. A 9.5/10 overall acceptance claim requires device/user acceptance and remaining visual checks; CI success does not establish that score or 99% mockup fidelity.
- Earlier audit PDFs remain baseline records; they should not be presented as a fresh qualified review of this build.

## Final verified result

Run 23 completed successfully on 17 October 2026 at 22:37 UTC. The exact built source is a91ce7e0ccc880103ad7b51085de321d1bd6c7e5; a later documentation-only commit does not create a new design build.

- 122 tests recorded: 121 passed, zero failures/errors, one legacy opt-in ScreenshotTest.render skipped.
- Gallery's 23 regression methods passed; lint and signed GitHub release assembly passed.
- 70 PNGs retained in artifact 12115984496, expiring 16 November 2026. The final enlarged-Urdu capture was checked for the corrected left-pointing Qibla navigation arrow.
- APK: https://github.com/haawas-alt/miqaat/releases/download/gallery-tablet-1.163-23/Miqaat-gallery.apk
- APK size: 43,876,110 bytes; versionCode 1792276064; versionName 1.163-gallery.
- Downloaded APK SHA-256 independently matched the published checksum: 52525e3184e32ed87c9cc68af324a5dcac9f8729dd1fcabb820a4440eaa180a5.
- APK v2 signing certificate extracted independently: 6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01, matching the existing certificate. CI apksigner verify passed. Certificate extraction alone is not a cryptographic signature check.

Cloud remediation and candidate publication are complete. This verifies a testing candidate, not production acceptance or a certified 9.5/10 rating.
