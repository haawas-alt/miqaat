# Functionality inventory (traceability guardrail)

Written before the design-system/Settings migration. Pre-change backup: `/home/claude/backups/miqaat-pre-design-system-20260930-091726.zip` (sha256 ba7226990e2f9f9f50753a79b480de724601bf765c2fb0ce626d1af97967fb61), branch `four-themes`, HEAD `ffd81a8`, clean tree.

Rules for this migration: section composables (`LocationSection` … `AboutSection`) keep their bodies and every `store.update { … }` call untouched; only the shell around them (navigation, layout, search, readiness) and shared visual primitives change. `SettingsScreen` keeps the single `LaunchedEffect(timingKey) { AzaanScheduler.reschedule(ctx) }` that re-arms alarms when timing-relevant settings change.

## Settings (10 destinations)

| Destination | Controls (existing, all retained) | State read → mutated | Side effect / system intent |
|---|---|---|---|
| Location | Current location, use device location, Home, place search, traveller mode, time zone, masjid times on/off, import timetable file, imported days | `AppSettings.location*`, `zoneId`, `zoneManual`, `masjid*` via `SettingsStore.update` | reschedule via timingKey; location permission request; file picker (SAF) for timetable |
| Prayer times | Calculation method, Asr method, high-latitude rule, Ramaḍān mode, suhoor alarm, tarāwīḥ, Friday reminders, hour-of-acceptance reminder, Jumuʿah azaan/time, show end times / sunrise / disliked times / "azaan was … ago" | method, asr, highLat, ramadan*, suhoor*, tarawih*, friday*, show* | reschedule (timingKey) |
| Azaan & alerts | per-prayer azaan on/off, azaan file, Fajr file, volume, speaker boost, reminder before azaan, dua & hadith after azaan, narration, hadith source, hadith on-screen seconds | azaanEnabled, files, azaanVolume, azaanBoostDb, preReminderMinutes, narration*, hadith* | reschedule; audio file picker; AlarmVolume/LoudnessEnhancer at playback |
| Iqamah | iqamah times/offsets per prayer, Jumuʿah fixed time, countdown seconds, sound at iqamah, quiet screen after iqamah | iqamah*, quiet* | reschedule (iqamah events) |
| Hijri calendar | Show Hijri date, adjustment (−/+ days), moon sighted / complete 30 days | showHijri, hijriOffsetDays, monthLength override | affects Ramaḍān/Jumuʿah logic, reschedule |
| Display & art | Theme (now four), language, time format, keep screen on, dim after Isha, large type, Qibla on home, Learn Salah, adhkar toggles, widget, open on device start | theme, language, use24h, keepScreenOn, nightDim, largeType, showQibla, kidsMode, adhkarEnabled, postPrayerAdhkar, launchOnBoot | widget refresh; BootReceiver enablement |
| Try it now (was "Test & preview") | azaan recording play/stop, short countdown, quiet screen, iqamah sound, full sequence, Ramaḍān Maghrib sequence, one hadith, preview a theme/Show, Start, Stop | none persisted | starts `AzaanService`/activities with test extras |
| Reliability & backup | Exact alarms, notifications, battery optimisation, full-screen azaan (rows + remedies), next alarm armed, time-change self-check, log, launch on boot, save/restore settings file | `Health` log, `Reliability.checks` | opens system settings; SAF export/import |
| Privacy | static statements, permissions, source code link | – | opens links |
| About | version, check for updates (forced), download/install, allow installs, content sources, report correction, learn recitation | `Updater.State` | download + package installer (github flavour only) |

## Other screens (unchanged behaviour; only colours/tokens touched in the four-theme work)

Home (Miqaat/Kiswah/Celestial/Gallery + Large type), Timetable, Qibla (sensors, declination, calibration), Adhkar (morning/evening/after prayer counters), Dua/Hadith/Azaan/Iqamah/Quiet phases, Friday (al-Kahf persistence, salawat counter, hour of acceptance), Learn Salah, Setup/onboarding, Why-this-time dialog, widget, boot/reschedule receivers, updater. Engines (`PrayerEngine`, `AzaanScheduler`, `Health`, `Reliability`, `Updater`, `SettingsStore`) are not modified.

## Existing test coverage

`PrayerEngineTest` (times, windows, end times), `TrustTest` (content/trust checks), `ThemeTokensTest` (theme enum/persistence fallback/contrast). New tests added by this work are listed in `ACCEPTANCE_CHECKLIST.md`.
