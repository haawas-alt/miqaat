# Quiet Screen verification — 19 October 2026

Validated implementation: `08b9499585a3837d6fca58b8a69e3313e5071a63`.

[Passing workflow](https://github.com/haawas-alt/miqaat/actions/runs/39206163969) · [Implementation PR](https://github.com/haawas-alt/miqaat/pull/7)

- All four theme jobs passed.
- Main regression suite: 143 methods, 138 passed, 5 opt-in audit methods skipped, zero failures/errors. Each of the three other theme jobs passed its three Quiet acceptance methods.
- 64 native Quiet captures cover all four themes in English/Urdu: standard phone portrait, tablet portrait/landscape; compact phone, compact tablet, short landscape; 200% text; and Minimal/24-hour overnight mode. Standard approved compositions account for 24 captures.
- Retained Quiet tests passed the current/next prayer engine, imported overrides, midnight/tomorrow Fajr, preview/service launch, tap-anywhere return and the device/language/font/time-format matrix.
- Debug and Play release lint passed. Signed GitHub APK, Play APK and Play AAB built successfully as version `1.166-quiet`.
- The GitHub APK verified against established signing certificate SHA-256 `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`. Native alignment and rejected-artwork scans passed. The Play APK contains no `REQUEST_INSTALL_PACKAGES` permission.

Native captures were visually reviewed against the approved direction. Normal landscape tablet typography uses the full tablet scale, Kiswah embroidery is kept at the edge, and header/footer washes protect readability. The original complete Kiswah band remains native text. Current prayer/Azaan time is visually distinct from the small live clock and secondary next Azaan.

The package includes the previously approved Home implementation. No timing, preferences, progress storage or return behavior was removed by the Quiet redesign.

Evidence uses Robolectric native graphics on Android SDK34. Physical-device acceptance remains outstanding; these automated and visual checks do not establish a measured numerical UX score. Exact candidate provenance and SHA-256 checksums accompany the delivered build.
