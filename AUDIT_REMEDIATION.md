# Miqaat · audit remediation log

Source: *Executive Product, UX, Islamic Content, Accessibility, and Engineering Audit* of build 33
(commit `adb454b`). This log maps every finding and every item of the audit's "Prioritized roadmap" (1–50) to
what changed, where, how it is evidenced, and what remains.

**Status key**
- **CODE** — fixed in code (file(s) named; test named where one exists)
- **COPY** — fixed through wording, design or configuration
- **DEVICE** — implemented, but only real-device testing can confirm it (see DEVICE_TEST_PLAN.md)
- **SCHOLAR** — requires qualified scholarly approval (see ISLAMIC_REVIEW_PACK.md)
- **DEFERRED** — intentionally not done in this pass, with the reason

Verification performed in this pass (GitHub Actions run 35, commit `dd15943`, release `v1.35`):
`./gradlew testGithubReleaseUnitTest lintGithubRelease assembleGithubRelease assemblePlayRelease bundlePlayRelease` — 20 unit
tests passed (11 engine + 9 trust), lint passed with `ContentDescription`/`MissingPermission` as errors (run 34 failed on four
pre-existing `NewApi` errors — `setShowWhenLocked`/`setTurnScreenOn` below API 27, `getCurrentLocation` below API 30, a
cutout attribute — all fixed with real guards, not suppressions). Shipped size: `Miqaat.apk` 44.7 MB → 35.5 MB after R8 +
resource shrinking (the remainder is bundled audio and fonts); `Miqaat-play.aab` 35.9 MB. `latest` tag now points at the
built commit; `v1.35` is the immutable tag.
**Not performed:** anything requiring an Android device or emulator — the workspace has none. No claim below
about on-device behaviour should be read as tested.

---

## The six release blockers

| # | Finding | Status | What changed | Evidence |
|---|---|---|---|---|
| 1 | First run can silently leave Gledswood Hills active | **CODE** | `AppSettings.locationSet` (new, default false; placeholder coordinates are the Kaʿbah, name empty). `Setup.ready()` gates the home screens, the scheduler (`AzaanScheduler.nextEvent` returns null) and the widget. New 4-step `SetupScreen` (welcome → place → confirm zone/method/today's times → alerts); "Next" is disabled until a place is chosen or detected; manual search/presets are offered *before* any permission request. `detect()` changes nothing on failure and reports the reason (`LocationRepo.Fix/Problem`); geocoder failure falls back to coordinates, never a stale name. Legacy migration: installs that had detected (homeLat present) or picked a non-default place are treated as configured; others see setup. | `TrustTest.freshInstallIsUnconfigured`, `finishingSetupWithoutAPlaceIsNotReady`, `readyOnlyWhenPlaceChosenAndSetupFinished`, `zoneMismatchIsNoticed`; files `data/Settings.kt`, `data/Setup.kt`, `data/LocationRepo.kt`, `ui/SetupScreen.kt`, `MainActivity.kt`, `MiqaatWidget.kt`, `azaan/AzaanScheduler.kt` |
| 2 | Qibla mixes true bearing with magnetic heading | **CODE + DEVICE** | `GeomagneticField(lat,lng,0,now).declination` added to the magnetic azimuth (`PrayerEngine.trueHeading`); accuracy taken from the magnetometer's own `onAccuracyChanged`; screen shows "Compass accuracy … · Magnetic declination ±x° applied"; unreliable state tells the user to treat the needle as approximate; sensor-less fallback keeps the numeric true bearing; spoken description for TalkBack (`qiblaWords`). | `TrustTest.qiblaBearingsForKnownCities` (8 cities), `magneticHeadingIsCorrectedByDeclination`, `bearingNormalisation`, `turnDirectionsAreShortestWay`; `ui/QiblaScreen.kt`; on-device matrix in DEVICE_TEST_PLAN § D |
| 3 | Traveller qaṣr/jamʿ toggles do nothing | **CODE + SCHOLAR** | Both toggles removed from Settings; description now states plainly that Miqaat does not shorten or combine prayers. Prefs kept for compatibility, never read. Re-introduction conditions recorded in ISLAMIC_REVIEW_PACK § J6. | `ui/SettingsScreen.kt` (LocationSection) |
| 4 | Accessibility fundamentally incomplete | **CODE + DEVICE** | See § E below: named icons, merged card semantics with spoken state, headings, roles, 48 dp targets, contrast tokens, sky scrim, reduce-motion, live regions, compass/wave/widget descriptions. TalkBack traversal and 200 % font remain device-verified. | lint `ContentDescription` = error; DEVICE_TEST_PLAN § E |
| 5 | Not publishable: API 34, REQUEST_INSTALL_PACKAGES, both exact-alarm permissions | **CODE** | `compileSdk/targetSdk 36` (AGP 8.10.1); product flavours `play` (no updater, no install permission) and `github` (updater, permission in `src/github/AndroidManifest.xml`); `SCHEDULE_EXACT_ALARM maxSdkVersion=32` + `USE_EXACT_ALARM`; CI builds APK + AAB for both. | `app/build.gradle.kts`, `AndroidManifest.xml`, `.github/workflows/build.yml` |
| 6 | Religious review prepared but not completed | **SCHOLAR** | `ISLAMIC_REVIEW_PACK.md`: versioned register (2026.09-a) of every ruling-like statement with before/after wording, engine behaviour and approval fields; app says "Unsigned" and links a correction channel. Disputed items reworded toward caution (J1–J7) without changing any calculation or Arabic text. | ISLAMIC_REVIEW_PACK.md; Settings › About |

## Screen-by-screen findings

| Screen | Finding | Status | Change |
|---|---|---|---|
| First run | chained permissions, no manual path, unsafe Continue | **CODE** | replaced by `SetupScreen` (above); notification permission asked in its own step with the reason shown |
| Home (landscape) | chip accumulation | **DEFERRED** | kept: chips already wrap and are contextual; the owner asked for these features. Priority/overflow rules are a design task for a later pass |
| Home | hidden tap-to-toggle on cards | **CODE** | cards expose `onClick(label = "Switch between clock time and time until")` and a `stateDescription`; Kiswah long-press exposes `onLongClickLabel = "Why this time?"`; the hint text under the portrait rail stays |
| Home | daytime contrast (ivory on Dhuhr ≈ 2.15:1) | **CODE** | `DaySkyScrim` over the lower 70 % of daytime skies (0.34–0.62 alpha near-black), all three home layouts; secondary text uses solid tokens instead of alpha |
| Kiswah | 40 dp unlabelled icons, undiscoverable long-press, faint caps | **CODE** | 48 dp named icons; long-press label; Caps default alpha 0.9; iqamah/signature alpha raised |
| Portrait | 40 dp targets, unlabelled icons | **CODE** | 48 dp; labels; merged row semantics |
| Large type | tap-anywhere not self-evident | **DEFERRED** | the tap surface is the whole screen (≥ 48 dp) and the mode is opt-in; configurable peek duration deferred |
| Why-this-time | mixes calculation with fiqh | **COPY** | split into "Calculated from", "Your settings", "Islamic guidance · scholarly views, not calculation"; key/value rows wrap (FlowRow) on narrow screens |
| Qibla | see blocker 2 | **CODE** | plus 48 dp back button |
| Timetable | 760 dp horizontal table, no scroll cue, no export | **DEFERRED** | not changed in this pass (usable, not misleading); export and phone-day cards are roadmap items 37/16 |
| Settings | density, 38 dp steppers | **CODE (partial)** | steppers 48 dp with names ("Increase", "One hour later"…) and live region on the value; chips 48 dp radio semantics; "Essential vs Advanced" split deferred |
| Azaan & alerts | exact-alarm status says "Open" | **CODE** | `ReliabilityRows`: Granted ✓ / Not granted · Fix › for exact alarms, notifications, battery, full-screen; shared with setup |
| Full-screen azaan | alarm-stream change not crash-safe, no audio focus | **CODE + DEVICE** | `AlarmVolume` persists the prior volume before changing it and restores on finish/destroy; `restoreIfStale` on app start undoes a change older than 30 min; `AudioFocusRequest(GAIN_TRANSIENT)` requested in `begin()` and abandoned in `finishAll()`; focus loss stops the sequence |
| Post-azaan | "Hadith of the hour" vague | **COPY** | kicker now "A hadith after Fajr · Ṣaḥīḥ al-Bukhārī 614"-style with the source |
| Post-azaan | default on for phones | already off on phones since build 20 (`MiqaatApp` pocket defaults) | — |
| Iqamah | quiet-screen text 30 % | **CODE** | token `textSecondary` |
| Adhkār | unlabelled count circle | **DEFERRED** | counter text uses tokens; role/state on the circle is in the next accessibility pass |
| Friday | hour-of-acceptance stated as certainty | **COPY + SCHOLAR** | states the view and names the other; J5 |
| Learn | children-only framing, one form as universal | **COPY + SCHOLAR** | renamed Learn Salah; notes name where schools differ; 48 dp controls; J7 |
| Widget | stale/no a11y | **CODE (partial)** | `contentDescription` on the root; unconfigured state; multi-size widgets deferred |
| Health/backup | ambiguous label, backup sensitivity, allowBackup, unverified update, mutable tag | **CODE + COPY** | renamed Reliability & backup; backup row warns that coordinates are included; `allowBackup=false`; updater SHA-256 verification; `v1.<run>` immutable tags and `latest` moved to the built commit; commit shown in About |

## Functional/technical findings

| Area | Finding | Status | Change |
|---|---|---|---|
| Calculation | "most Australian mosques use MWL" unsubstantiated | **COPY** | reworded (J1) |
| Calculation | presets Australia-centric | **CODE** | +15 presets across Asia, Europe, Africa, the Americas; developer's suburb removed from the top |
| Calculation | detected location uses device zone without confirmation | **CODE** | zone shown in setup Confirm; `Setup.zoneLooksWrong` (> 3 h from lng/15) blocks "These look right" and warns in Settings › Location |
| Calculation | permissive timetable import | **DEFERRED** | row-level review UI not built; existing 12-h monotonic check and the Fajr/Maghrib comparison line remain |
| Calculation | Jumuʿah replaces Dhuhr | **SCHOLAR** | J9 |
| Alarms | both exact permissions | **CODE** | maxSdkVersion 32 |
| Alarms | no permission-state listener | **CODE** | `BootReceiver` handles `SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`: logs and re-arms |
| Alarms | inexact fallback presented as exact | **CODE** | planned label "(approximate)", home chip "Azaan may be late · fix", reliability rows |
| Alarms | direct activity start from service + full-screen intent | **DEVICE** | unchanged; DEVICE_TEST_PLAN § A |
| Alarms | volume restore / audio focus | **CODE** | see above |
| Perf | executor leak in `LocationRepo.current` | **CODE** | main executor + `CancellationSignal` tied to coroutine cancellation |
| Perf | per-second whole-screen recomposition | **CODE (partial)** | `@Immutable` on `DayTimes`, `PrayerState`, `AppSettings` so rail/cards/timeline skip; `state` already rebuilt once per minute. Frame timing must be measured on device |
| Perf | unminified 43 MB APK | **CODE** | R8 + resource shrinking (`-dontobfuscate`); raw audio kept by `res/raw/keep.xml`. Size after shrink is reported by CI (`ls -la Miqaat*`) |
| Perf | no baseline profile / macrobenchmark | **DEFERRED** | needs a device to generate; tracked in RELEASE_CHECKLIST |
| Perf | update check each foreground, throttle in memory | **CODE** | throttle persisted in prefs; no check before setup; Play edition never checks |
| Privacy | `allowBackup=true` | **CODE** | false |
| Privacy | updater does not verify SHA-256 | **CODE** | verifies the published `.sha256`; refuses releases without one; deletes mismatches |
| Privacy | mutable `latest` tag pointing at an older commit | **CODE** | CI force-moves `latest` to the built commit and creates immutable `v1.<run>`; body carries `commit=` |
| Privacy | backup editable/unauthenticated | **COPY** | sensitivity note; type validation unchanged |
| Privacy | REQUEST_INSTALL_PACKAGES blocks Play | **CODE** | flavour manifest |

## Prioritized roadmap (audit items 1–50)

| # | Item | Status | Where |
|---|---|---|---|
| 1 | Remove live default; block output until place confirmed | CODE | blocker 1 |
| 2 | Manual city before permission requests | CODE | `SetupScreen.PlaceStep` |
| 3 | Qibla declination + known-bearing tests | CODE + DEVICE | blocker 2 |
| 4 | Remove/implement traveller features | CODE | blocker 3 |
| 5 | Signed scholarly review | SCHOLAR | pack written, unsigned |
| 6 | Screen-reader semantics + 48 dp | CODE + DEVICE | § E |
| 7 | Contrast failures | CODE | tokens + scrim |
| 8 | Target API 36 | CODE | gradle |
| 9 | Remove REQUEST_INSTALL_PACKAGES from Play | CODE | flavours |
| 10 | Exact-alarm manifest + state recovery | CODE | manifest, BootReceiver |
| 11 | Audio focus, route handling, crash-safe volume | CODE (focus + volume) · DEFERRED (per-route rules) | AzaanService, AlarmVolume |
| 12 | Location executor leak | CODE | LocationRepo |
| 13 | Locked/Doze/reboot/DST OEM tests | DEVICE | DEVICE_TEST_PLAN § A–B |
| 14 | Immutable releases + checksum verification | CODE | workflow, Updater |
| 15 | Wall Mode flagship, simplify phone | DEFERRED | product direction; owner decision |
| 16 | Home to one hero/one rail/two prompts | DEFERRED | owner requested these chips; wrap + contextual only |
| 17 | Advanced settings gate | DEFERRED | next UX pass |
| 18 | Reliability status card | CODE | `ReliabilityRows` in Settings and setup; home chip |
| 19 | Per-prayer alert modes, short azaan | DEFERRED | feature, not a blocker |
| 20 | Post-azaan optional per prayer, quiet default on phones | PARTIAL | already off by default on phones; per-prayer control deferred |
| 21 | Replace hidden gestures with labelled actions | CODE (semantics) | on-screen buttons unchanged; ⓘ exists on Miqaat/portrait themes |
| 22 | Settings search / undo | DEFERRED | |
| 23 | Phone navigation, foldables | DEFERRED | |
| 24 | Resourceize strings, complete Urdu | DEFERRED | large mechanical change; Settings › Language already says settings stay English |
| 25 | Arabic + five languages | DEFERRED | needs translators |
| 26 | Font-size and reduce-motion controls | PARTIAL | reduce motion respected from system; sizes are sp (scale with system) — overflow to be checked on device |
| 27 | Validate timetable imports before activation | DEFERRED | see Calculation row |
| 28 | Qibla accuracy/calibration/environment warnings | CODE | QiblaScreen |
| 29 | Child/adult Learn Salah with madhhab notes | COPY + SCHOLAR | J7 |
| 30 | Creator signature → About | **DEFERRED (owner decision)** | the owner asked for it on the home screen; contrast raised to token level instead |
| 31 | Rename Health → Reliability & backup | COPY | Settings |
| 32 | Exact-alarm Granted/Not granted | CODE | ReliabilityRows |
| 33 | Labels on Calendar/Settings/Qibla/Learn/location icons | CODE | all three homes |
| 34 | 36/38/40/44 dp → 48 | CODE | homes, settings controls, Learn, azaan pills, Qibla back |
| 35 | Scrim on Sunrise/Dhuhr/Asr | CODE | `DaySkyScrim` |
| 36 | Replace 30–45 % helper text | CODE | tokens |
| 37 | Timetable "Today" + scroll cue | DEFERRED | |
| 38 | Location/zone/method summary in details | CODE | Why-this-time "Calculated from" section already lists all three; setup Confirm too |
| 39 | "Test next azaan" after onboarding | CODE | setup Alerts step: Play a test azaan |
| 40 | Inexact-alarm warning | CODE | chip + rows + planned label |
| 41 | Backup privacy note | COPY | Reliability & backup |
| 42 | Checksum + source commit in About | CODE | BuildConfig GIT_SHA/BUILD_TAG |
| 43 | Correction/report link | CODE | About → GitHub issue |
| 44 | No update check before setup; persist last-check | CODE | Updater |
| 45 | R8 / resource shrinking | CODE | gradle + keep.xml |
| 46 | Baseline profile + cold-start benchmark | DEFERRED | needs device |
| 47 | Cache static drawing layers | PARTIAL | `@Immutable` prevents needless redraw; explicit `drawWithCache` deferred |
| 48 | Phone post-azaan hadith as dismissible card | DEFERRED | off by default on phones |
| 49 | "Mute today" / "skip this prayer" | DEFERRED | feature |
| 50 | Plain-language reliability & privacy page | PARTIAL | in-app Privacy page updated for both editions; a web page is a store-launch task |

## § E · Accessibility changes in detail
- **Names:** every icon-only control in the three home layouts, Learn, Qibla and the widget has a `contentDescription`; decorative icons inside labelled rows stay `null` (correct exclusion).
- **Roles/state:** chips are `selectable` radio buttons in a `selectableGroup`; settings rows that open something are `Role.Button`; steppers announce "Increase/Decrease/One hour later…" and their value is a polite live region; reliability values carry `stateDescription` Granted/Not granted; volume slider has a name and state.
- **Merged semantics:** each prayer card/row reads as one sentence (name, time, relative time, now/next/passed, iqamah, end, azaan off) and offers a custom action; the hero is a heading with a minute-rounded description (no per-second announcements).
- **Headings:** section titles in Settings, setup steps and the Why dialog.
- **Targets:** ≥ 48 dp on all changed controls (list in roadmap #34).
- **Contrast:** `Palette.textSecondary` (#CFC7B4, ≈10:1 on night) and `textMuted` (#AEA792, ≈7:1) replace alpha text; `DaySkyScrim` brings ivory over the Dhuhr bright end from ≈2.2:1 to > 4.5:1 in the text region (to be confirmed with Accessibility Scanner on device).
- **Motion:** `reduceMotion()` reads the animator duration scale; the azaan wave freezes when it is 0.
- **Canvas alternatives:** compass (`qiblaWords`), wave ("Audio playing"), widget root description.
- **Not done:** explicit traversal order, RTL layout verification, 200 % font-scale overflow audit, Adhkār counter role/state, timetable row semantics — all need a device and are in DEVICE_TEST_PLAN § E.

## Known risks after this pass
- Legacy migration heuristics may still show setup to an existing user who picked the old default deliberately (Gledswood Hills residents) — one-time, low cost.
- `USE_EXACT_ALARM` is reserved for alarm/clock apps; Play review may ask for justification (prepared in RELEASE_CHECKLIST).
- Android 16 ignores orientation locks on large screens; the tablet layout will rotate — portrait layout exists but has not been seen on a large portrait screen.
- Resource shrinking with `getIdentifier` lookups depends on `res/raw/keep.xml`; a missing clip would be caught by the fallback (TTS) and a log entry, not a crash, but must be confirmed on device (DEVICE_TEST_PLAN § G smoke).
- Alarm-volume manipulation remains a global side-effect by design (owner wants a guaranteed loudness); it is now crash-safe but still visible to the user.

## Files changed in this pass
`app/build.gradle.kts`, `build.gradle.kts`, `app/proguard-rules.pro`, `app/src/main/AndroidManifest.xml`, `app/src/github/AndroidManifest.xml`, `app/src/main/res/raw/keep.xml`, `app/src/main/res/values/strings.xml`, `.github/workflows/build.yml`,
`MainActivity.kt`, `MiqaatApp.kt`, `MiqaatWidget.kt`,
`data/Settings.kt`, `data/Setup.kt` (new), `data/Reliability.kt` (new), `data/LocationRepo.kt`, `data/PrayerEngine.kt`, `data/Updater.kt`, `data/Adhkar.kt`,
`azaan/AzaanService.kt`, `azaan/AlarmVolume.kt` (new), `azaan/AzaanScheduler.kt`, `azaan/BootReceiver.kt`,
`ui/SetupScreen.kt` (new), `ui/SettingsScreen.kt`, `ui/HomeScreen.kt`, `ui/PortraitHome.kt`, `ui/KiswahHome.kt`, `ui/QiblaScreen.kt`, `ui/Explain.kt`, `ui/Theme.kt`, `ui/AzaanScreen.kt`, `ui/LearnScreen.kt`, `ui/FridayScreen.kt`, plus token replacements in `TimetableScreen.kt`, `AdhkarScreen.kt`, `LargeHome.kt`,
`app/src/test/.../TrustTest.kt` (new),
`ISLAMIC_REVIEW_PACK.md`, `DEVICE_TEST_PLAN.md`, `RELEASE_CHECKLIST.md`, this file.

---

## Re-audit of v1.36 (28 Sep 2026) — response, shipped in v1.39

| Finding | Severity | Status | What changed | Evidence |
|---|---|---|---|---|
| Automatic location refresh writes `zoneId = null` and can undo a zone the user chose | **P0** | **CODE** | `AppSettings.zoneManual` (persisted). Zone picker sets it; `Setup.applyFix()` (pure) never touches a manual zone. For automatic zones it keeps the device zone only while plausible for the new longitude, otherwise switches to the zone of the nearest known place within 600 km, otherwise keeps the previous zone and asks the user (`needsZoneChoice`). Background refresh (`refreshIfDue`) runs at most every 30 min and never on an unconfigured app. No offline IANA boundary dataset is bundled (none is reachable from this workspace and it would add ~1 MB); the preset list now spans 35 cities on five continents, and the confirm step still shows the zone explicitly. | `ZoneRefreshTest` (5 tests: manual zone survives, device zone followed while plausible, switch to nearest known zone, ask when unknown, small moves ignored); `data/Setup.kt`, `data/Settings.kt`, `ui/SettingsScreen.kt` (`detect`, `refreshIfDue`), `MainActivity.kt` |
| Timetable import too trusting | P1 | **CODE** | Import no longer activates on read. A review dialog shows coverage, skipped lines, iqamah detection, the zone times are read in, first/last rows beside the calculated times, and `PrayerEngine.reviewTimetable()` anomalies (order, >20 min day-to-day jumps, Fajr/Maghrib >15 min from calculation, iqamah before azaan). Activation needs an explicit "Use these times" / "Use anyway"; "Discard" drops the file. | `PrayerEngineTest.timetableReviewFlagsOnlyRealProblems`; `ui/SettingsScreen.kt` |
| Learn Salah: ignores theme, emoji, flat carousel, no structure, no progress, opaque audio | P1 (design) | **CODE + SCHOLAR** | Rebuilt. `LearnScreen(settings, onBack)`; `LearnColors` derived from `AppTheme` (Miqaat: navy + lattice + Cormorant/Amiri; Kiswah: silk + weave + Cinzel/Reem Kufi) with identical layout. Library → *Learn a complete prayer* (Fajr 2, Maghrib 3, four-rakʿah), *The words*, *The movements*. Guided lesson generated from the reviewed texts with prayer → rakʿah → action levels (segmented rakʿah map; second prostration, first sitting, final sitting, salām placed correctly; sūrah only in the first two rakʿahs). Six vector posture silhouettes drawn in Canvas replace emoji, each with a text equivalent. Progress saved (`miqaat_learn`), Continue card, completion marks, reset. Audio: play/stop with announced state, Slow toggle, source disclosed as device TTS, not verified recitation. Transliteration is a toggle; note/schools/source expandable. All controls ≥ 48 dp, headings, live region on progress. Tablet layout: figure left, words right. | `data/Learn.kt`, `ui/LearnScreen.kt`; content unchanged → still ISLAMIC_REVIEW_PACK § J7 (structure added to that item) |
| Adhkār counter semantics | P1 (a11y) | **CODE** | list rows are tabs with state (Completed / n of m); counter button has role, label ("Count one recitation"), state and a polite live region; ≥ 56 dp | `ui/AdhkarScreen.kt` |
| Timetable semantics, "Today", scroll cue | P1 (a11y) / roadmap 37 | **CODE** | each row reads as one sentence; nav chips named and 48 dp; a **Today** chip scrolls to today; compact mode says "Swipe the table sideways for ʿAsr, Maghrib and Isha" | `ui/TimetableScreen.kt` |
| Back buttons 40 dp | a11y | **CODE** | all `IconButton(onBack)` are 48 dp | six screens |
| Alarm reliability unproven on hardware | P1 | **DEVICE** | unchanged: DEVICE_TEST_PLAN § A must be executed and signed |
| Religious content unsigned | P1 | **SCHOLAR** | unchanged: no approval claimed anywhere in the app |
| ~130 embedded `Text("…")`, no `stringResource`, partial Urdu | P1 | **CODE (v1.42) · translation awaiting proof-read** | 427 UI strings moved to `res/values/strings_ui.xml` with a complete `values-ur` translation (setup, settings, Learn, Qibla, Why-dialog, timetable, Friday, adhkār, azaan screens). Strings are read through `ui/Str.kt`, resources resolved for the in-app Language setting (not the system locale) and the screen tree re-keyed on change, so the whole app switches, not just the home screen. Bundle language splits disabled so Play installs carry both languages. Layout stays LTR by design pending an RTL pass on device. The Urdu was machine-written by Claude and is being proof-read by the owner (`Miqaat-Urdu-proofread.xlsx`); corrections will land as a values-ur edit only. Remaining English: data-layer labels (prayer names come from L10n already; posture/lesson names in `Learn.kt`, method descriptions in `Settings.kt`). |
| No Compose UI / screenshot / instrumentation tests, no baseline profile | P2 | **DEFERRED** | need an emulator or device in CI; the workspace has neither. Pure logic that used to live in composables (`Setup.applyFix`, `reviewTimetable`, `Learn.actions`) was extracted so it is unit-tested instead. |
| Per-second `now` state | P2 | **PARTIAL** | `@Immutable` models + strong-skipping; only tracing on device can prove cost |

Verification: GitHub Actions run 39 (`v1.39`): all unit tests passed (the count is in the CI test report artifact, not maintained here), lint passed, both editions built. Run 38 failed on a Kotlin rule (vararg of a value class) in the new posture drawing — fixed in the next commit.

---

## Live-device audit of v1.40 (Pixel 8 AVD, API 36) — response, shipped in v1.47

| # | Finding | Status | What changed |
|---|---|---|---|
| P1 | Landscape Settings rail hides four sections | **CODE** | rail is `verticalScroll`; all ten destinations reachable |
| P1 | 1.30× text clips Home countdown, breaks Timetable header | **CODE** | Home: display numerals/Arabic are screen-scaled (`fd`), labels and the countdown follow the font setting, portrait page scrolls above 1.15×, hero no longer clipped. Timetable: title/metadata and month controls on separate rows when narrow; header cells single-line; compact table has a **frozen Date column** and a shared horizontal scroll for the rest, width scaling with font size, with a › cue while more columns remain |
| P1 | Background refresh keeps an old zone silently | **CODE** | `zoneNeedsReview` persisted when no plausible zone is found; home chip "Time zone needs checking · fix", Settings › Location warning, planned alarm labelled "(zone unverified)"; cleared when the user picks a zone. Heuristic order changed: a known place within 150 km decides first (Brisbane on a Sydney-DST phone → Australia/Brisbane), then device-zone plausibility, then nearest place within 600 km, else ask. Alarms are **not** blocked while unresolved — a late azaan in the old zone was judged less harmful than a missing one; the label and chip make the state visible. Tests: `oneHourBorderIsCaughtByTheNearestKnownPlace`, `unresolvedZoneIsFlaggedForReviewUntilTheUserPicksOne`. No offline IANA dataset yet (nothing reachable from this workspace). |
| P1 | Import accepts 25:00 / 12:75 | **CODE** | hard-rejected per row with a named reason (minutes 00–59, 24-h hours 0–23, AM/PM hours 1–12, result within the day); `parserRejectsImpossibleClockValues` |
| P1 | External gates | **DEVICE / SCHOLAR** | unchanged; review PDF produced for the imam; recording sheet issued for Learn Salah audio |
| P2 | Urdu home leftovers, bidi | **CODE** | NEXT, reliability chip, hint, large-type instruction moved to resources; Qibla chip and Hijri line use bidi isolates / separate containers |
| P2 | Settings chip row discoverability | **CODE** | two-row wrapping grid with tab semantics |
| P2 | Timetable horizontal context | **CODE** | frozen Date column, › cue (above) |
| P2 | Iqamah guidance names a missing button | **COPY** | "Tap \"Start iqamah now\" if the imam is ready", wraps to two lines; Dismiss localised |
| P2 | Large-type instruction contrast | **CODE** | ivory on a 38 % black pill |
| P2 | Repository filenames in UI | **COPY** | "Content review pending" everywhere user-facing |
| P2 | Learn: 44 dp row, TTS before ready | **CODE** | 48 dp; play button disabled with "Preparing voice…" / "No voice available" until the engine reports, or immediately usable when a bundled recording exists |
| P2 | Learn at large scale / busy pattern | **CODE** | posture panel grows with font scale, Continue wraps, lattice at 0.035 behind a solid card surface |
| P3 | Why dialog scroll affordance | **DEFERRED** | already sectioned; fade deferred |
| P3 | Qibla portrait balance | **CODE** | `SpaceEvenly` |
| P3 | Test count in docs | **COPY** | counts no longer maintained by hand |

## Live emulator walkthrough of v1.48 (28 Sep 2026, Pixel 8 AVD, API 36) — response, shipped in v1.49

Verified working: home countdown in seconds under one minute; ʿAsr azaan fired on time with the azaan screen and "azaan was N s ago" state; settings chip grid; reliability rows (Granted / Not granted · Fix) and their refresh on return; timetable frozen Date column with horizontal swipe and Today chip; Learn library and lesson steps; Qibla with declination and accuracy states; evening-adhkār chip; Urdu home and settings render right-to-left with isolated numbers.

| # | Finding | Fix |
|---|---------|-----|
| L1 | Urdu: calculation method names/details, madhab chips, high-latitude rule, narration, Ramadan mode, iqamah sound, theme and art-theme chips stayed English (enum `label`s) | Every settings enum gained `labelRes`/`detailRes`; UI reads `Method.text/info`, `AsrMethod.text` … via `Str`; the English `label` is kept only for logs and the correction e-mail |
| L2 | Urdu: Prayer-times section heading and its intro were hard-coded | Resourced (`s_match_your_local_masjid`) |
| L3 | Urdu: Why-this-time dialog title, Fajr/Isha rule lines, Location label, Isha end note, "other view would give", masjid-source lines and the three disliked-window descriptions were English | Resourced with format args; `PrayerEngine.Window` carries `labelRes` (English `label` kept for tests) |
| L4 | Urdu: Hijri date rendered "ربیع الثانی 17 1448" — day and year collapsed because the string begins with a digit | Wrapped in a right-to-left isolate (U+2067…U+2069) |
| L5 | Azaan screen step bar promised "Duʿā after azaan · Hadith · Home" even when *After the azaan* is off (screen actually closes after the azaan) | Bar shows "Azaan · Home" when after-azaan is disabled; step names and the top status tag are resourced/localised |
| L6 | About › "Allow installs" button did not change to "Download & install" after granting the permission until leaving the section | `LifecycleResumeEffect` re-evaluates `canRequestPackageInstalls` on return |
| L7 | Learn bottom bar "Continue · Sitting · ṣalāh upon the Prophet ﷺ" wrapped with ﷺ alone on the second line | Long next-step names fall back to the posture name ("Continue · Sitting") |
| L8 | Compact timetable: prayer columns wider than needed (two swipes to reach Isha); Friday green too subtle | Scroll width 700 → 560 dp × font scale; Friday rows get a brighter green and a faint row wash |

Still English in Urdu mode by design for now: Learn Ṣalāh lesson content (postures, cues, notes — awaiting the reciter's recordings before that text is finalised and translated), timetable weekday/month abbreviations, and Latin day-bar letters (F · S · D · A · M · I). The Urdu strings added in this pass were proofread by the owner on 28 Sep 2026 (`Miqaat-Urdu-proofread-2.xlsx`, no changes).
| L9 | Urdu portrait home (both themes): the hero clock and countdown pill were clipped out of view — Urdu prayer rows are two lines tall, so the "hero takes the remaining height" layout ran out of room | Home now scrolls (the existing large-text path) whenever the language is Urdu or the screen is under 780 dp tall |
| L10 | "tablet" wording throughout copy on a phone app; Learn library titles, location-problem messages, About/Setup literals still English in Urdu | Copy says "device" (kiosk lines keep "wall tablet"); all of these resourced |
| L11 | Fresh-install walkthrough: each setup step opened already scrolled down (scroll position carried over); after "Use my location" the Next button sat below twelve presets; place card and confirm row repeated the coordinates when the geocoder returned no name; no way to pick Urdu before setup | Scroll resets per step; Next also appears on the chosen-place card; coordinates shown once; English/اردو chips on the welcome step |
| L12 | Urdu countdown pill read "گھنٹے 53 منٹ باقی 1" | Durations wrapped in a right-to-left isolate |
| L13 | Picker chevrons pointed the wrong way in right-to-left layout; "Report a content correction" and "Device · zone" still English | `Str.chev` mirrors per layout direction; both strings resourced |
| L14 | Phone in landscape (1.3× text): the tablet home arrangement could not fit in 412 dp — hero clock hidden behind the sun arc, "MAGHRIB/ISHA" labels colliding, signature over the Fajr card; Settings rail sat under the status bar; timetable title overlapped the Hijri range | Home (both themes) has a *short* mode under 480 dp tall: scrolls, fixed-height hero, day thread instead of arc, in-line signature; rail gets status-bar padding; Hijri range moves to the meta line unless ≥1000 dp wide |
| L15 | Owner's tablet (landscape, Miqaat theme) and phone landscape photos: the hero clock's lower half and the whole countdown were hidden behind the day arc/thread — the hero column is taller than the height left after the cards | New `FitHeight` layout scales the hero down to the height it actually has (never up) in all three home layouts; phone landscape no longer scrolls — thread instead of arc, arch off, signature in the header line |
| L16 | Tablet speaker too quiet | Speaker boost gains a +18 dB level (Settings › Azaan & alerts); default azaan volume 100 % for new installs |
| L17 | Phone landscape still poor after L15 — scaling the tablet page shrank the hero to a stamp while the chips and buttons stayed huge | New `LandscapeHome` composition for phones held sideways (height < 500 dp): hero (name, clock, countdown, iqamah) fills the left half at reading size; the six times sit in equal-height rows on the right so nothing can clip; one-line header; chips reduced to small text "doors" under the day thread; sized from screen height so it never scrolls. Both themes |
| L18 | Tablet: the sun arc took a band of height, its Maghrib/Isha labels collided, and the header chips stacked three-high — together they squeezed the hero | The tablet home now uses the same straight gold thread as the phone (arc retired from home); header chips flow in one line beside the location; hero regains the height |
| L19 | App icon | Replaced with the approved concept: night-purple ground, gold crescent, two frosted arches whose overlap is a lit mihrab door. Master `art/miqaat-icon.svg` (generated by `art/make_icon.py`), export `art/miqaat-icon-1024.png`, adaptive icon (`ic_launcher_background/foreground/monochrome`) sized to the 66 dp safe zone; themed (monochrome) icon included |
| L20 | Learn Ṣalāh used device text-to-speech | Owner-supplied recitations bundled as `learn_01…12.mp3` (normalised, trimmed); library note now states they are human recordings; Slow button plays the recording at 0.75× |
| L21 | Tablet (build 67): signature overlapped the Fajr card; chips still on their own line under the location | Chips now flow on the same line as the location (as on the phone landscape); signature sits under them at the top-left, off the cards |
| L22 | Reciter credit | About › "Learn Ṣalāh recitation · Abur Rahman Usman Zia"; each Learn step's audio line names him |
| L23 | Tablet home redesign (owner's brief: "world-class, Islamic architecture") | New `TabletHome.kt`: **MihrabHome** (Miqaat theme, design D — timetable board left, recessed mihrab with muqarnas hood right, sundial line with gnomon, doors + signature on the foot line) and **CourtyardHome** (Kiswah theme, design C — the list is the sundial, open court for the hour, Kufic band at the head). Every behaviour of the old tablet page is carried over (doors = old chips, kickers, Ramaḍān bar, tarāwīḥ, tap/long-press/ⓘ, ✓/bell, iqamah/ends, night dim, art-theme, TalkBack). `DayThread` gained full names, a gnomon and ember-coloured disliked windows. Phone layouts unchanged |
| T1 | Tablet audit v1.70 · TAB-01/04 portrait home scrolled at 200 % text and in Urdu | On tablets (≥600×780 dp) the portrait page no longer scrolls: the whole page is scaled to the height it has (`FitHeight`), and the dashboard's font scale is capped at 1.3× (`CapFontScale`; Large type mode remains for bigger text). Phones still scroll. |
| T2 | TAB-02 landscape timeline labels collide at 130/200 % | `DayThread` label size capped at 1.15×; labels chosen collision-aware (full names → initials → skip a label that would touch its neighbour); Urdu uses Urdu names; prayer-row small line may wrap to two lines; landscape dashboards capped at 1.3× font scale. |
| T3 | TAB-03 portrait timetable headings merge at 200 % | Table switches to the frozen-Date, sideways-scrolling form whenever eight columns will not fit at the current font scale (`maxWidth < 440 dp × fontScale`), not only under 720 dp; metadata line may wrap. |
| T4 | TAB-05 "Update available" on v1.70 | Not a defect: 1.71+ had already been published, so the chip was correct. The `play` flavour has no updater at all. Re-checked on the exact release artifact before Play submission (release checklist). |
| T5 | Urdu leftovers in tablet home ("iq") | Uses اقامت in Urdu. Still open for translator sign-off: AM/PM suffix, "ḍuḥā from", place-name script. |
| T6 | Owner decisions, 29 Sep 2026 | Urdu leftovers from T5 (AM/PM suffix, "ḍuḥā from", place-name script, Latin timeline initials in portrait Urdu) **accepted as-is by the owner**. Re-recorded hadith audio (Arabic 7/17/21/24/25/26/30/32; English 24/25/31) **verified by the owner** on device. Tablet fixes T1–T3 verified on the Medium Tablet emulator at 200 % text (Home landscape and portrait, Timetable portrait, Urdu Home portrait), build 1.78. |

## P · Play-store preparation (29 Sep 2026)

| # | Finding | Remediation |
|---|---|---|
| P1 | **Play edition crashed on Settings › About** (ChatGPT, on the v1.78 Play build): `canRequestPackageInstalls()` was called unconditionally, but the `play` flavour has no `REQUEST_INSTALL_PACKAGES`. | `Updater.canInstall` and `openInstallPermission` short-circuit when `Updater.enabled` is false, and the API call is wrapped in `runCatching`. Verified by code reading and a green CI build of both flavours (1.79); the Play AAB cannot be run on the emulator, so the exact Play artifact should be re-tested by the owner. |
| P2 | `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` is a permission Play questions and rarely allows. | Permission removed. "Battery settings" now opens the system battery-optimisation list, falling back to the app's own settings page. |
| P3 | Learn Ṣalāh described as "for children"; Play target audience is 13+. | Framing changed to "for beginners of any age" in English and Urdu; recorded in ISLAMIC_REVIEW_PACK History. No content change. |
| P4 | No privacy-policy URL. | `docs/privacy.html` (served by GitHub Pages once enabled) and `docs/index.html`. |
| P5 | Store assets and listing text. | Owner-supplied artwork adopted as the app icon (`art/logo/`, `make_launcher.py` cuts adaptive foreground PNGs at five densities, legacy square icons, Play 512 icon and feature graphic). Monochrome layer redrawn to match. Notification small icon now uses the monochrome vector (was the old colour foreground). Listing text in `play/`. Submission steps in `PLAY_SUBMISSION.md`. |
| P6 | Settings › "Test & preview" read as a developer leftover. | Renamed "Try it now" (Urdu: ابھی آزمائیں) and moved directly under Azaan. |
| P7 | About showed a stale update offer because of the 6-hour check throttle (adjourned earlier). | About now forces a check on open. |
| P8 | Tablet azaan quiet compared with media apps. | Not a defect: the app plays on the alarm channel, which some tablets cap lower than media. Owner resolved with the in-app +12 dB boost. Consider defaulting the boost to +6 dB on ≥600 dp screens in a later build. |
