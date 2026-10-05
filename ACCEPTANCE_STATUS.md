> **Independent acceptance notice (1 October 2026):** The PASS labels below are historical claims from the four-themes work, not independent acceptance. Run159 only changes this document. The current ci-screenshots head identifies run159 and is not an immutable run158 archive. See [RUN158_INDEPENDENT_AUDIT.md](RUN158_INDEPENDENT_AUDIT.md) and [RUN158_FIX_STATUS.md](RUN158_FIX_STATUS.md) for verified evidence, isolated fixes and open gates. No9.9/10,99% fidelity or production Play-ready claim is accepted.

# Miqaat — Tablet Audit v3 acceptance status (CI run 158, commit b5562a0, branch `four-themes`)

Evidence: 16 emulator jobs (4 devices x 4 groups) green; 28 gate tests, 0 failures (8 skipped by design); 976 screenshots on branch `ci-screenshots`, 244 per device, every one in the orientation its device name claims (earlier runs had tablet-portrait and phone-landscape silently rendering in the wrong orientation; fixed in runs 151-155, so earlier portrait/landscape claims were re-verified here). Sheets delivered in chat.
Legend: PASS = verified in CI/screenshots. PARTIAL = built, visible difference remains (listed under Residual differences). OPEN = not done.

## Safety and functionality
- PASS Backup: ZIP `/home/claude/backups/miqaat-pre-design-system-20260930-091726.zip` sha256 `ba7226990e2f9f9f50753a79b480de724601bf765c2fb0ce626d1af97967fb61` (HEAD ffd81a8). Note: this is a ZIP, not a dated git branch; a backup branch was not separately created.
- PARTIAL Baseline: earlier-run screenshots exist, but a formal pre-change baseline set was not recorded.
- PASS Prayer engine, scheduler, audio, alarms untouched; unit tests pass (build job gates).
- PASS Four themes selectable/persistent (tests); "Try it now" label kept.
- PASS Adhkar entries reachable in their windows in all four themes, phone and tablet (`AdhkarEntryTest`, run 143).

## Visual source of truth
- PASS Rejected moon/night and empty-mosque art absent from source, both APKs and the AAB (`tools/check_forbidden_assets.py`, build job, every run incl. 143).
- PASS (with residuals) A2/D Homes: landscape tablet Homes rebuilt in the approved art's coordinates and match closely (sheets `*__home.png`); tablet-portrait Celestial/Gallery now larger type and top-aligned (no blank band); Gallery keeps a small blank band at the bottom of tablet-portrait.
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
- PARTIAL Urdu RTL: mirroring verified on Home, Settings, Adhkar, Friday, Qibla, Timetable, Learn, Dua screenshots; Learn lesson content and Hadith text are English data; Urdu strings are unproofread drafts.

## Evidence
- PASS Screenshot matrix: 4 themes x 4 devices x pages (Home, Settings, states, Azaan flow, Adhkar, Friday, Qibla, Timetable, Learn).
- PASS Side-by-side sheets (tablet landscape, A2 and D, 10 pages each).
- PASS Instrumented tests: Settings state, large text, Adhkar entry, plus screenshot matrix.
- OPEN Pixel-diff (visual-regression) tests: screenshots are captured, not diffed automatically.
- OPEN TalkBack on device, imam content review, Urdu proofreading, Play steps.

## Learn lesson illustrations (added after review)
- PASS Eight distinct positions, each with its own drawing in a faceless robed-figure style: takbir (hands raised), standing hands folded, rukuʿ, standing after rukuʿ, sujud, sitting between prostrations, tashahhud (index finger raised), salam (head turned). Previously only six existed; tashahhud reused sitting and folded standing reused standing. A per-posture screenshot test now captures every position in every theme (`learnLessons`, group d); checked on tablet and phone.
- PARTIAL Composition: Celestial lesson card sits the figure on the approved sunrise scene with the details panel at right, as in A2. The figures are vector line art, simpler than the painted robed figure in the mockup. Only the first lesson step exists as a mockup, so the other seven were judged for distinctness and correctness, not pixel match.

## Residual differences (explicit)
1. "Differences between schools" card/screen from the Learn mockups is NOT built, deliberately: it is fiqh content that needs imam review. Lesson notes still flag where schools differ.
2. Gallery Azaan/Dua/Hadith/Adhkar/Qibla/Friday backdrop is a code-drawn soft landscape (warm sun, cool hills), because the approved Gallery art has the Home tile strip baked in. It matches the mockup's feel, not its painted detail (no moon, no painted hills).
3. Learn illustrations are line art (see above).
4. Phone-landscape Gallery Home uses the contained art layout and its text is small; Celestial phone-landscape panel now readable.
5. Miqaat and Kiswah were compared with their design-system boards at colour, button, chip and typography level only, not page by page.
6. Azaan flow top bar: step line and clock follow the mockup; the location line shows the app's saved place (blank in test fixtures).
7. Learn overview: Continue card appears once there is progress; before that a Start card is shown.
8. No automated pixel-diff tests; screenshots are captured and reviewed by eye.
9. Learn posture labels, cues and notes are English data strings (not translated to Urdu); Urdu proofreading, TalkBack, imam review and Play steps are yours.

## Builds
GitHub Actions artifacts `Miqaat-build-158` (github APK, play APK, play AAB) and prerelease `four-themes-preview` (Miqaat.apk). Do not treat as Play-ready until residual items 1–7 are accepted or fixed.
