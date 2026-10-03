# Prayer Gallery phone-fit testing checkpoint

Normal-size Gallery phone Home now uses the usable viewport instead of a vertical scroll container. Prayer name and hero time share a baseline; six rows take the remaining height. Header and information controls retain 48dp targets. Compact Urdu row text uses Amiri to avoid the much taller Nastaliq line metrics; the hero/date retain the Urdu display font. Row details remain visible. Arabic names and clocks have a distinct gap. Primary footer labels wrap; additional contextual actions are available through the 48dp More actions menu. Background artwork, signature and day thread remain visible.

Source: 7f25a976c94f937e130f6cc48831e0947001a391
Backup: backup/pre-gallery-phone-fit-20261003 at 3520effd8c0b7c13b40312543cfee8a5ee610c89.
Production four-themes remains at 30122fbfbd4d2e347a28f7f81f0c444f1aaacd57; PR #1 stays draft.

Validation:
- Signed candidate run 37137386752; audit run 37137386623.
- Native JVM rendered evidence: EN and Urdu, hours 2/12/18/22, 411x891dp and 360x800dp. The smaller phone reserves 24dp at the top and 48dp at the bottom to model system-bar space.
- Six rows and footer inside viewport, no normal-size home scroll action, header and row targets at least 48dp, hero name/clock on the same line without overlap, text overflow/row clipping and Arabic/clock spacing assertions.
- 648 raw PNGs in audit artifact, including 16 focused Home captures (822x1782 and 720x1600 pixels).
- Each validation run: 83 tests, 82 passed, 0 failed/errors; 1 expected opt-in legacy ScreenshotTest skipped.
- Manually reviewed all 16 focused raw Home screenshots before publication.
- Larger text continues through HomeRouter's AccessibleHome. This change does not cap the system font size.

Published testing APK: https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.87-fit.apk
APK SHA-256: 52265822e547cfe4ad134aaa77b965398582d65257375be64168cd4f830c2423
Certificate SHA-256: 6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01 (existing release key).
Source and checksums, not the pre-existing testing release tag commit, identify this candidate.

Limits: native JVM rendering and simulated inset space are not physical-device acceptance. Very short/narrow phones, all custom prayer/iqamah combinations and seasonal extras are not newly certified by these focused captures. Qualified Urdu/Islamic sign-off, physical accessibility/OEM testing and Play Console approval remain pending. No new paid emulator run or production merge/upload occurred. The 18-page religious/Urdu PDF remains the source-grounded 1.86 content review; this layout fix does not change its religious wording or artwork. It is not a scholarly certificate.
