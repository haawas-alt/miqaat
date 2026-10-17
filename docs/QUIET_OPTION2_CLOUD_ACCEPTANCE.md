# Quiet screen Option 2 — cloud acceptance checkpoint

Verified 2026-10-13. Scope: the quiet screen shown after Iqamah, replacing its old plain clock layout.

## Exact build and rollback

- Baseline: `bb3bbbc15826054548a43660aeba68c26029c0d0`, release `audit-fix-1.90-15`.
- Backup created before edits: [backup/pre-quiet-option2-20261013](https://github.com/haawas-alt/miqaat/tree/backup/pre-quiet-option2-20261013), pointing at the exact baseline.
- Built implementation: `04a65cc5a97044540f51fbbb97d2db0ecb67a9a2`.
- Workflow: [run 38384863769](https://github.com/haawas-alt/miqaat/actions/runs/38384863769), Quiet Option 2 signed candidate, run number 10, success.
- [Testing release](https://github.com/haawas-alt/miqaat/releases/tag/quiet-option2-1.90-10).
- [Direct APK](https://github.com/haawas-alt/miqaat/releases/download/quiet-option2-1.90-10/Miqaat-1.90-quiet-option2.apk).
- Actual APK manifest: package `com.usman.miqaat`, versionName `1.90-quiet-option2`, versionCode `1791887993`.
- APK size: 43,858,430 bytes.
- Independently calculated APK SHA-256 matches both artifact checksum and release asset digest: `ad60a22152a1b432ac6b0b840bc136cef04de90ebd3e11f5c26844d78e9c7c0f`.
- APK v2 signature verified by apksigner in CI. Certificate SHA-256: `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`, matching the existing release key.
- Play AAB built: SHA-256 `6d1e45cbc1c1171d90561de390ed72c932bf7c9a233499753e37ff1ca3c6130f`. Not submitted to Play Console.
- Publication is a prerelease, make_latest false. The in-app updater's latest tag was not moved.

## Implementation and mockup comparison

Option 2 is a dark green/gold mihrab composition with a dominant current prayer and current Azaan time, a compact next-Azaan strip on portrait screens, and separate current/next panels on wide screens. The supplied Option 2 image was used as visual direction; a new text-free architectural backdrop is bundled as WebP. All captions, times and actions are real Compose text and controls.

Current identity comes from Phase.Quiet.prayer; its Azaan time comes from the prayer engine, including yesterday's Isha after midnight. Next Azaan comes from the engine's next actual prayer, excluding Sunrise, respecting timetable overrides. Existing quiet expiry and tap-to-stop service behavior remain connected through the original callback.

English uses existing Cormorant/Nunito fonts; Urdu uses existing Nastaliq, RTL ordering, localized times and explicit line heights. No Quran/Hadith or prayer rulings were added.

This is not a claim of pixel-identical 99% fidelity. The artwork was reconstructed from the mockup; live clock, expiry hint and explicit exit action are implementation additions. RTL mirrors the two panels. Full-width exit action differs from the mockup's smaller action.

## Validation evidence

Downloaded and inspected the final run's artifact `quiet-option2-build-38384863769`, ID `11807235415`, 150,555,650 bytes. Raw screenshots expire 2027-01-11; preserve the archive before expiration if long-term evidence is needed.

The full JUnit XML set has 91 tests: 90 passed, 0 failures, 0 errors, 1 skipped. All three QuietOptionAcceptanceTest methods passed. The skipped test is the pre-existing opt-in ScreenshotTest.render; it is not any quiet-screen acceptance case.

| Device | Raw dimensions | Language | Scale | Clock | Cases |
|---|---|---|---|---|---:|
| Phone portrait | 822 × 1782 | EN, Urdu RTL | 100%, 130%, 200% | 12h, 24h | 12 |
| Phone landscape | 1782 × 822 | EN, Urdu RTL | 100%, 130%, 200% | 12h, 24h | 12 |
| Tablet portrait | 1600 × 2560 | EN, Urdu RTL | 100%, 130%, 200% | 12h, 24h | 12 |
| Tablet landscape | 2560 × 1600 | EN, Urdu RTL | 100%, 130%, 200% | 12h, 24h | 12 |

All 48 raw PNGs were individually reviewed. Paths inside the artifact:
`app/build/audit-screens/quiet-option2/quiet__{device-orientation}__{en|ur}__{100|130|200}__{12h|24h}.png`.

Tests assert actual localized time/name values, no text layout overflow, displayed text and screen bounds, panel nonoverlap, hero containment, absence of scroll containers at normal scale, exit target at least 48dp, and working explicit/tap-anywhere exit callbacks. Model cases cover all five prayers, midnight Isha and imported timetable overrides. Isha-to-tomorrow-Fajr is also exercised.

At 100% all four orientations fit without scrolling. At 130%, phone landscape intentionally uses a scrollable reading layout. At 200%, all orientations switch to the accessible single-column layout. Tests scroll each field into view and verify it separately. Those screenshots are captured at the final exit position, so upper fields can be offscreen; they do not demonstrate simultaneous visibility of every field.

Release lint, GitHub APK, Play APK/AAB and forbidden asset checks succeeded. The Play APK permissions check confirmed absence of REQUEST_INSTALL_PACKAGES.

Earlier candidate runs exposed real intrinsic-width clipping in captions/header clock and an overly strict nanosecond comparison against the engine. Those failures were repaired; tests were retained. Final header clock uses an exact responsive width to prevent end-aligned paragraph overflow. Earlier failed artifacts remain in Actions.

## Explicit limits and remaining acceptance

These are native Robolectric SDK34 screenshots and assertions, not physical-device or emulator instrumentation. No paid emulator run was started. This checkpoint does not certify OEM lifecycle behavior, full-screen/lock-screen transitions, actual upgrade installation, audio-to-Iqamah-to-quiet timing on hardware, TalkBack reading order or OLED brightness. The current/next computation is tested; wall-clock refresh and service expiry are not end-to-end verified on hardware here.

No qualified human Islamic/Urdu reviewer has signed off the new interface captions. They are operational UI wording, not added religious instruction. No Play Console policy review, upload acceptance or production rollout was performed.

Before production promotion: install over 1.90-15 on a real phone and tablet without data loss; run actual Azaan→Iqamah→quiet→expiry; verify midnight Isha→Fajr; rotate and inspect system insets; check TalkBack, large-text scrolling and exit; obtain Urdu caption approval. The testing APK is published now; production readiness remains a separate gate.

## 2026-10-14 Sydney — Settings Show launch fix (latest candidate)

The previous build rendered the quiet screen correctly in isolated layout tests but Settings → Try it now → Quiet screen → Show did not open it. ACTION_QUIET initialized the service phase without calling showScreen. This confirmed gap was not covered by the original layout tests.

The action now sets Phase.Quiet before opening AzaanActivity, whose null-phase handling would otherwise close the screen. The existing quiet-duration override still offers a one-minute preview when quiet mode is disabled, without changing settings. No Option 2 layout/artwork changes were made.

- Pre-fix backup: [backup/pre-quiet-show-fix-20261014](https://github.com/haawas-alt/miqaat/tree/backup/pre-quiet-show-fix-20261014), exact commit 49aa895894193b9c33920225968d7fbfcd4f26e4.
- Built source: c319ccb2088864beed93c56bfafc7cf48c24597a.
- [Successful run 38430103332](https://github.com/haawas-alt/miqaat/actions/runs/38430103332), workflow run number 12.
- [Latest direct APK](https://github.com/haawas-alt/miqaat/releases/download/quiet-option2-1.90-12/Miqaat-1.90-quiet-option2.apk).
- VersionName 1.90-quiet-option2, actual manifest versionCode 1791908634, package com.usman.miqaat. Previous candidate code was 1791887993.
- APK SHA-256 b56a7137753bd0409bb52b7f331f61f2747d7e408b5e5194072cb047c0447b76, independently computed from downloaded artifact and matched to release asset digest; 43,858,430 bytes.
- Signature verification passed; certificate SHA-256 remains 6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01.
- Full suite: 93 tests, 92 passed, 0 failures/errors, 1 pre-existing opt-in screenshot test skipped.
- QuietPreviewLaunchTest: both tests passed. Actual Settings Show CTA is tapped on phone portrait and tablet landscape; the requested service intent/action/prayer is asserted. The real service command is dispatched with quietMinutes 0 and 3; tests assert a Quiet phase, expected preview expiry, launched AzaanActivity intent/prayer/new-task flag, unchanged saved settings and Stop cleanup.
- Existing QuietOptionAcceptanceTest: all three tests passed again, including the 48 layout/value/exit cases. 48 PNGs were captured in the final artifact. The current fix changes the launch path only; the original visual-review scope and limitations above still apply.
- Lint, GitHub APK, Play APK/AAB and forbidden-asset checks passed. Artifact ID 11832025832, quiet-option2-build-38430103332.

The testing prerelease supersedes build 10 for this Show-button fix. No production/updater latest tag was moved. These are Robolectric interaction/service assertions; a real Samsung device installation and Settings-to-screen transition remain hardware confirmation, not claimed as performed.
