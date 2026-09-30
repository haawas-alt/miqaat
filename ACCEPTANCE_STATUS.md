# Combined design-system + Settings: acceptance status

Legend: ✅ done and verified by CI tests · 🟡 built, needs on-device/emulator confirmation · ⬜ not done

## Backup and preservation
- ✅ Backup ZIP outside checkout: `/home/claude/backups/miqaat-pre-design-system-20260930-091726.zip`, sha256 `ba7226990e2f9f9f50753a79b480de724601bf765c2fb0ce626d1af97967fb61`, opened and enumerated (285 files, no errors). HEAD `ffd81a8`, branch `four-themes`, tree was clean (status/patch/untracked files recorded next to it).
- ✅ No user change reset, cleaned, stashed or overwritten.
- ✅ Functionality inventory written before migration: `FUNCTIONALITY_INVENTORY.md`.

## Shared system
- ✅ One component layer (`ui/DesignSystem.kt`) + tokens (`ThemeTokens.kt`, incl. new `error`, `arabicText`, sky roles); spacing scale, breakpoints, buttons, chips, search, rows, rail item, notices, loading/empty, themed artwork with scrim.
- ✅ Four persisted themes; legacy/unknown values fall back safely (tests).
- ✅ Miqaat and Kiswah use exactly the palettes they always had (`screenTokens()`).
- 🟡 Supplied artwork integrated as optimised WebP (12–70 KB each) behind the Celestial home and in the Gallery hero with token scrims; contrast against real crops still to be judged on device.
- ✅ No mockup screenshot is shipped as UI.

## Settings
- ✅ Ten destinations in spec order; every section body and control unchanged; alarm re-arm effect unchanged.
- ✅ "Try it now" in UI/search/tests; enum key `TEST` kept for compatibility.
- ✅ Tablet ≥720dp: fixed rail (back, title, search, 10 items with bar + tint + bold/icon cue + selected semantics), detail pane scrolls independently, max content width 920dp (tests).
- ✅ Phone <720dp: grouped landing (search, readiness, Prayer setup / Experience / System), focused detail screens, back returns to landing, deep links leave Settings on back; no chip carousel (tests).
- ✅ Readiness summary from live `AzaanScheduler.nextEvent` and `Reliability` checks; OK / attention / no-event / no-location states; icon + words; failing checks are Fix buttons (tests with fake and live states).
- ✅ Search: declarative index, synonyms, ranking, empty/no-result states, opens the destination and scrolls to + highlights the matching row (JVM + Compose tests).

## Functional regression
- ✅ No engine, scheduler, store, updater, audio or alarm code changed; existing Prayer/Trust/Theme tests pass.
- 🟡 Every setting mutation and scheduler side effect: code paths untouched; behaviour to be spot-checked on device (Location detect, azaan file picker, exact-alarm remedy, backup export/import).

## Phone / tablet verification
- ✅ Compose tests at phone portrait, tablet landscape, tablet portrait, 200% font, Urdu RTL, all four themes (Settings composes in each).
- ⬜ Human-eye screenshots for every screen × theme × orientation × scale × language: JVM screenshot job exists (`ScreenshotTest`, CI job `screens`, branch `ci-screenshots`) but first runs timed out in Robolectric; to be done on the emulator instead.
- ⬜ TalkBack pass on device; reduced-motion pass on device.

## Engineering
- ✅ Release builds (github APK, play APK + AAB) compile; unit + Compose tests pass in CI; lint passes.
- ⬜ Instrumented (emulator) tests: none written; Robolectric Compose tests stand in for them.

## Honest limitations
- Urdu strings added in this work are unproofread drafts; search synonyms are English only.
- Not every screen has bespoke phone-landscape tuning beyond what the earlier four-theme work did; emulator review will show gaps.

## Emulator pass (Medium_Tablet, landscape, 30 Sep 2026, build 104)
Verified visually: tablet Settings rail + independent detail scroll; readiness summary (next event, location, exact alarms, battery, notifications); search "battery" → Reliability, scrolls to and highlights the row; Try it now; theme switching on Celestial, Gallery, Kiswah (Settings and home).
Defects found and fixed: theme-picker labels invisible in Gallery (now follow active theme); Kiswah/Miqaat tablet home link row truncated by the signature (signature now on its own line). Both re-checked on build 104.
Still NOT verified: phone layouts (emulator was a tablet only), portrait, 130%/200% text, Urdu/RTL, TalkBack, reduced motion, Miqaat theme after the change, search results are broad for "battery" (all Reliability rows match).

## Phone emulator pass (Pixel 8, portrait, 30 Sep 2026, builds 104-110)
Verified visually: Settings landing (grouped rows, search, readiness), detail screens, theme cards, Celestial/Gallery/Kiswah/Miqaat phone homes, Urdu (Nastaliq) on home, Settings landing and detail, Timetable in Celestial.
Defects found and fixed: readiness checks broke mid-word; detail title shown twice; Celestial phone Settings button off-screen; Celestial/Gallery phone text 4-9sp (now floors of 11-13sp); Gallery phone header pushed Settings off-screen after the text-size change (regression, fixed); Settings icons blue on light themes (now gold-brown).
Observed, not fixed: changing language inside Settings returns to home; moon artwork sits behind date/kicker on Celestial phone; Urdu headline mixes bidi ("عشاء · 8:16 PM").
Still NOT verified: landscape phone, 130%/200% system text (needs device setting), TalkBack, reduced motion, remaining Settings detail screens on phone, dialogs/pickers, full screenshot matrix, Urdu proofreading.
