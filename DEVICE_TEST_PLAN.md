# Miqaat · physical-device test plan

Nothing in this file has been executed yet. It is the gate between "builds and passes its unit tests" and
"launch-ready". Record results in the tables (device, Android version, build, date, pass/fail, notes) and
attach the Reliability & backup log export for every failure.

## Device matrix (minimum)

| # | Device class | Android | Why |
|---|---|---|---|
| 1 | Pixel (any) | 14 or 15 | Reference behaviour, API 34–35 |
| 2 | Pixel or emulator | 16 (API 36) | Target SDK; large-screen orientation rules |
| 3 | Samsung Galaxy phone | 13–15 (One UI) | Sleeping-apps / deep-sleep lists |
| 4 | Xiaomi / Redmi / POCO | MIUI or HyperOS | Aggressive autostart + battery restrictions |
| 5 | Oppo / Realme / OnePlus | ColorOS / OxygenOS | Background kill behaviour |
| 6 | Low-cost Android tablet | 11 (the wall tablet) | Primary wall-mode device, Go-edition-like memory |
| 7 | Any | 12 or 12L | SCHEDULE_EXACT_ALARM path (user-revocable) |

## A · Alarm reliability — 14-day soak

Run on devices 1, 3, 4 and 6 in parallel. Enable all five azaans, iqamah on two prayers, pre-reminder 10 min.
Each day, export Reliability & backup › log and check:

| Check | Pass criterion |
|---|---|
| Every azaan entry | "On time" (≤ 90 s of planned) on ≥ 99 % of events over 14 days |
| Screen off, device idle 4 h+ (Doze) | azaan fires, screen wakes, full-screen shows |
| Locked with PIN | lock-screen azaan visible, Stop works without unlocking |
| Battery saver on | azaan fires; if late, app showed "Azaan may be late · fix" chip beforehand |
| Reboot at night | first azaan after boot fires; log shows "Device started · alarms re-armed" |
| Force-stop then reopen | app re-arms on open (log entry) — note: after force-stop Android will not fire until reopened; the log must say so via checkMissed |
| Airplane mode | no change (all offline) |
| App updated via updater (device 6) and via `adb install -r` | "Miqaat updated · alarms re-armed" logged; next azaan fires |
| Exact-alarm permission revoked (device 7) | chip appears; Settings shows "Not granted · Fix"; log has "Exact alarms revoked"; azaan still fires (approximate); re-granting logs "granted" |
| Notifications disabled in system settings | Reliability row shows Not granted; azaan still plays audio |
| Alarm volume | after each sequence, system alarm volume is back to its prior value; kill the process mid-hadith (`adb shell am kill`) → on next app open the volume is restored (AlarmVolume.restoreIfStale) |
| Audio focus | music playing at azaan time pauses/ducks and resumes after; incoming phone call during azaan stops the sequence cleanly |
| Bluetooth headphones connected | azaan routes per system alarm routing; document behaviour |

## B · Time and zone

| Scenario | How | Pass |
|---|---|---|
| DST start (Sydney 4 Oct 2026 02:00→03:00) | set date to 3 Oct 23:50, watch overnight | Fajr time unchanged wall-clock; log has "Clock changed · Self-check passed"; azaan fires on time |
| DST end | same, first Sunday in April | as above |
| Manual clock change ±1 h | Settings › Date & time | log entry; next alarm re-armed |
| Zone change (fly Sydney→London simulated) | change device zone, keep place | home shows ⚠ zone mismatch on Settings › Location and setup Confirm; times shown in chosen zone |
| Place with fixed zone (preset) on device in another zone | pick "Karachi" from Sydney | times shown in Asia/Karachi; widget matches |
| Date boundary | at 23:59 → 00:00 | hero rolls to next day's Fajr; no crash |
| High latitude | set place to Oslo, Tromsø (lat 69.6) in June | no exception; times shown; Why-this-time names the high-latitude rule |
| Polar | Longyearbyen (78°) | app does not crash; times may be rule-derived — document |
| Manual adjustments ±15 | per prayer | card, timetable, widget and azaan all use the adjusted time |

## C · Location and first run

| Scenario | Pass |
|---|---|
| Fresh install, deny location, tap through | Cannot finish setup without choosing a place; Next disabled with "Choose or detect a place to continue." |
| Fresh install, location off in system | "Location is turned off…" message; search still works |
| Fresh install, airplane mode, detect | Cached fix used or timeout message; if fix but no geocoder, name shows coordinates (e.g. "33.87°S, 151.21°E"), never a stale city |
| Fresh install, London SIM/location | Setup Confirm shows Europe/London (device zone); if device zone is Sydney → ⚠ and "Fix the time zone first" |
| Existing install upgrading from build 33 that had detected location | no setup shown (legacy migration) |
| Existing install upgrading that never detected and kept the default | setup shown |
| Change place in Settings | widget, timetable, alarms recalc immediately (log shows re-arm) |
| Traveller: move ≥ 80 km with home set | chip appears; no qaṣr/jamʿ controls anywhere |

## D · Qibla (blocker 2)

Use a known reference: a masjid mihrab, or a map bearing from a GIS tool. Test in 4 orientations (portrait, landscape left/right, upside-down) on devices 1, 3 and 6.

| City (device location) | True bearing | Expected declination shown | Pass criterion |
|---|---|---|---|
| Sydney | 277.5° | ≈ +12.6° | needle within ±5° of the mihrab when accuracy "high" |
| London | 119.0° | ≈ +0.5° | as above |
| New York | 58.5° | ≈ −12.9° | as above |
| Karachi | 267.8° | ≈ +2.2° | as above |
| Anywhere | — | — | wave phone near a speaker → accuracy drops, text says calibrate; figure-of-eight → recovers |
| Tablet with no magnetometer | — | — | "No compass sensor" text; numeric bearing still shown |
| TalkBack | — | — | compass announces "Turn left/right N degrees…" or "You are facing the Qibla" |

## E · Accessibility

Run on device 1 with TalkBack, then with Switch Access, then font scale 200 % and display size largest.

| Screen | TalkBack pass criteria |
|---|---|
| Setup (4 steps) | step progress announced; every control has a name; Next disabled state announced |
| Home (tablet, Kiswah, portrait, large type) | location button announces place; each prayer card reads one sentence incl. state; hero reads "Next prayer X at …, in …"; icon buttons named; chips named |
| Timetable | rows readable; horizontal scroll cue present on phones |
| Settings | headings announced; every switch/chip/stepper has role + state; steppers ±48 dp |
| Qibla | see D |
| Azaan screen | Stop/Skip named; wave described as "Audio playing" |
| Adhkār | counter announces count; completion announced |
| Font scale 200 % | no clipped text, no lost actions; note overflow for fix |
| Reduce motion (Developer options › animator scale off) | wave stops animating; sky still updates |
| Contrast (Accessibility Scanner) | zero "text contrast" findings on Dhuhr, ʿAsr and Sunrise skies |

## F · Play edition (`Miqaat-play.aab`)

| Check | Pass |
|---|---|
| Install via internal testing track | installs on API 26–36 |
| Manifest | no REQUEST_INSTALL_PACKAGES; USE_EXACT_ALARM declared; SCHEDULE_EXACT_ALARM maxSdk 32 |
| About | "Google Play edition"; no update controls |
| Pre-launch report | no crashes; accessibility warnings triaged |

## G · Direct edition (`Miqaat.apk`)

| Check | Pass |
|---|---|
| Update from build 33 | updater downloads, verifies SHA-256, installer opens; About shows new commit |
| Tampered APK (edit one byte of the downloaded file before install) | "Checksum did not match" and the file is deleted |
| Release with no .sha256 asset | "Release has no checksum; not installing" |

## Sign-off

| Gate | Owner | Date | Result |
|---|---|---|---|
| A · 14-day soak | | | |
| B · time/zone | | | |
| C · location/first run | | | |
| D · Qibla | | | |
| E · accessibility | | | |
| F · Play | | | |
| G · direct | | | |
