# Iqamah verification — 19 October 2026

Validated implementation: `d3cc3664eba6595c7ab75149b305db9cf228f64d`.
Passing workflow: https://github.com/haawas-alt/miqaat/actions/runs/39211354142
Implementation PR: https://github.com/haawas-alt/miqaat/pull/8

- All four theme jobs passed.
- Main regression suite: 150 methods, 145 passed, 5 opt-in audit methods skipped, zero failures/errors. Each other theme job passed its 7 Iqamah acceptance and counter methods.
- 64 native captures cover all four themes in English/Urdu: standard phone portrait and tablet portrait/landscape, compact phone/tablet/landscape, 200% text, and Minimal/24-hour overnight mode. The standard approved compositions account for 24 captures.
- Counter boundary tests passed partial-second rounding, the exact deadline, elapsed phases, remaining-ring fraction/clamping and long durations. The UI update test verifies the countdown changes while the displayed Iqamah deadline stays fixed.
- Native assertions passed text bounds, full Kiswah Arabic content, date rollover, clock formats and both 48dp action callbacks. Retained regression checks cover the live screen, existing Home/Quiet behaviour and other app features.
- Debug and Play release lint passed. Signed GitHub APK, Play APK and Play AAB built as version 1.167-iqamah.
- The GitHub APK verifies against established signing certificate SHA-256 `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`. Native alignment, rejected-artwork and Play-permission checks passed. Downloaded archives match GitHub digests; delivered build checksums match CI.

Native English and Urdu captures were visually reviewed. Refinements aligned countdown digits and enlarged normal-tablet typography. The original Kiswah band, prayer stages, live clock/location, scheduled Iqamah time, original row-straightening reminder/source, Start Iqamah now and Dismiss remain native UI. The candidate includes the approved Home and Quiet designs.

Evidence uses Robolectric native graphics on Android SDK34. Physical-device acceptance remains outstanding; these checks do not establish a measured numerical UX score. This is a testing candidate, not a published store release. The Iqamah playback state retains its existing implementation; the approved countdown state is the scope of this redesign.
