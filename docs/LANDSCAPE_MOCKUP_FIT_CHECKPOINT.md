# Landscape mockup fidelity and fit correction

Backup pre-landscape-fit-20261004 at 14a7f34a651f82839093c5ac290a2c79f133ea38 created before edits.

Verified references retrieved from the user's files: Celestial meridian prayer countdown.png (Countdown Meridian option 1) and Dawn prayer gallery.png (Dawn Landscape option 3), created 4 October 2026. These are distinct from the older complete approved archive which remains unavailable.

1.88 did not implement the approved composition faithfully: small isolated Gallery image, generic tinted surfaces, diamond timeline, missing Celestial row icons and scrollable columns. Its prior test accepted those scroll containers. This candidate reconstructs the full hero paintings with text-free image assets, authored row icons/type/selected surfaces, primary details button, Gallery prepare CTA and circular full-name timeline. Live labels/times/countdown remain engine-backed. Contextual Adhkar/Qibla/readiness/seasonal actions remain available through More actions rather than expanding Home vertically. Full row end-time/iqamah facts remain in spoken semantics and Why detail; main rows match the reference's single-line composition.

New native JVM acceptance exercises actual HomeRouter: 64 cases across both themes, EN/UR, 891x411dp and 740x360dp, four hours, clock/relative display, with 24dp top and side system-bar reserves. It rejects any scrolling Home node, requires all six rows and full timeline/hero/control bounds in viewport, checks row/hero/countdown text overflow and 48dp controls, and activates Gallery Prepare without scrolling. Qualified religious review and physical device acceptance are not claimed. Large system text continues AccessibleHome.

## Verified 1.89 testing candidate

Exact app source: cfc5b57b5cb0899ea51ef513ff097f4242bb3ff4.
Signed validation: https://github.com/haawas-alt/miqaat/actions/runs/37266216421
Audit validation: https://github.com/haawas-alt/miqaat/actions/runs/37266216424
Both succeeded: 84 tests, 83 passed, no failures/errors and one opt-in ScreenshotTest.render skipped per run. Lint, both flavor compilation, bundle validation, signature verification and native 16KB alignment checks pass.

712 raw PNG captures present; all 64 new landscape captures reviewed individually at 1480x720 and 1782x822 pixels. All six rows and full timeline remain visible in EN/UR, clock/relative display and all four time fixtures, with system-bar reserves. No scrolling container exists on normal-size Home. More actions and Prepare navigation pass without scrolling.

Composition follows the retrieved selected mockups: full scene paintings, serif hierarchy, theme-specific icon colours and selected surfaces, circular timeline and Celestial live countdown. This is not a pixel-identical or 99% certification: painting reconstruction and stock icon contours differ; compact selected English rows use a second status line, Urdu uses a compact Nastaliq size, and large-font AccessibleHome intentionally scrolls. Small Gallery Prepare labels may use two lines. End-time and iqamah facts remain in details and spoken semantics.

Independent APK manifest inspection: package com.usman.miqaat, versionName 1.89-mockup-fit, versionCode 1791176728.
APK size 43,780,001 bytes; SHA-256 734e121bf42d4f1e913068245da3fe43c5d64828144a3fe0f90347721ddc4ebb.
AAB size 44,023,739 bytes; SHA-256 573d19527e68ff98b79a413f9e0c3e8b71006fa0b36606051bb13a22339925a5.
Release certificate SHA-256 6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01 retained.

Publication is gated on those exact source runs and APK hash, targeting the existing testing release audit-fixes-3895c823. Earlier assets and backups remain intact. Raw screenshots remain in existing Actions evidence; no new screenshot review branch is published.
Direct APK after publication: https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.89-mockup-fit.apk

No paid emulator run requested or started. Qualified Urdu/Islamic sign-off, physical accessibility/OEM acceptance and actual Play Console acceptance remain unverified. Production promotion remains NO-GO; PR stays draft.

Publication run https://github.com/haawas-alt/miqaat/actions/runs/37267371486 succeeded on 5 October 2026. Release server size/digest verified against independently hashed APK and AAB. Version 1.88 and earlier release assets remain intact.
