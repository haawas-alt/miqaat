# Miqaat — Tablet Audit v3 acceptance status (CI run 143, commit 4d73030, branch `four-themes`)

Evidence: 16 emulator jobs (4 devices x 4 groups) green; 28 instrumented tests, 0 failures (8 skipped by design: tablet-only/phone-only cases); 848 screenshots on branch `ci-screenshots`; side-by-side + matrix sheets in `Miqaat-evidence-run143.zip` (delivered in chat).
Legend: PASS = verified in CI/screenshots. PARTIAL = built, visible difference remains (listed under Residual differences). OPEN = not done.

## Safety and functionality
- PASS Backup: ZIP `/home/claude/backups/miqaat-pre-design-system-20260930-091726.zip` sha256 `ba7226990e2f9f9f50753a79b480de724601bf765c2fb0ce626d1af97967fb61` (HEAD ffd81a8). Note: this is a ZIP, not a dated git branch; a backup branch was not separately created.
- PARTIAL Baseline: earlier-run screenshots exist, but a formal pre-change baseline set was not recorded.
- PASS Prayer engine, scheduler, audio, alarms untouched; unit tests pass (build job gates).
- PASS Four themes selectable/persistent (tests); "Try it now" label kept.
- PASS Adhkar entries reachable in their windows in all four themes, phone and tablet (`AdhkarEntryTest`, run 143).

## Visual source of truth
- PASS Rejected moon/night and empty-mosque art absent from source, both APKs and the AAB (`tools/check_forbidden_assets.py`, build job, every run incl. 143).
- PARTIAL A2/D Homes: landscape tablet Homes rebuilt in the approved art's coordinates and match closely (sheets `*__home.png`); Celestial portrait Home unchanged; Gallery tablet-portrait has extra space above header.
- PARTIAL Miqaat/Kiswah: Home and shared tokens now navy/gold and Kiswah-specific; not compared page-by-page against every design-system board.
- PASS Approved v2 art used with intentional fit (no stretch/crop on Home; backdrops on Azaan flow, Adhkar, Qibla, Friday, Learn).
- PASS Shared components use theme tokens; Material colour scheme derived from tokens (no default blue CTA outside Gallery's cobalt, which matches mockup D).

## Responsive quality
- PASS Tablet landscape Home no scroll/overlap; tablet portrait no mid-word wrap (screenshots, large-text test).
- PASS Settings readiness reflows in portrait; Display & art shows content at top (SettingsStateTest 5/5).
- PASS Category-state isolation / switching to Try it now (SettingsStateTest).
- PASS Phone Home follows phone layout; Celestial/Gallery fit without scrolling at 100%.
- PASS Home responds to 130%/200% (HomeLargeTextTest, all themes/devices); 200% uses the scrolling accessible layout by design.
- PARTIAL Settings/Home/secondary pages captured at 100% and 200% (130% for Home/Settings only).
- PARTIAL Urdu RTL: mirroring verified on Home, Settings and secondary screenshots; Azaan/Learn/Adhkar English leakage not fully audited; Urdu strings are unproofread drafts (yours to review).

## Evidence
- PASS Screenshot matrix: 4 themes x 4 devices x pages (Home, Settings, states, Azaan flow, Adhkar, Friday, Qibla, Timetable, Learn).
- PASS Side-by-side sheets (tablet landscape, A2 and D, 10 pages each).
- PASS Instrumented tests: Settings state, large text, Adhkar entry, plus screenshot matrix.
- OPEN Pixel-diff (visual-regression) tests: screenshots are captured, not diffed automatically.
- OPEN TalkBack on device, imam content review, Urdu proofreading, Play steps.

## Residual differences (explicit)
1. Gallery Azaan/Dua/Hadith: the v2 art has a baked tile strip and olive branch that show behind the text; mockup has a softer landscape. Needs a dedicated backdrop crop/asset.
2. Azaan flow: stage pills instead of the mockup's step-line; no location/clock top-right.
3. Learn overview: no "Differences between schools" card; no Continue card until progress exists.
4. Friday: type small and layout sparse versus mockup.
5. Celestial portrait Home and Gallery tablet-portrait spacing (above).
6. Phone-landscape Celestial/Gallery not reviewed in detail.
7. Miqaat vs Kiswah secondary pages not compared against design-system boards one by one.

## Builds
GitHub Actions artifacts `Miqaat-build-143` (github APK, play APK, play AAB) and prerelease `four-themes-preview` (Miqaat.apk). Do not treat as Play-ready until residual items 1–7 are accepted or fixed.
