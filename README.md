# Miqaat · ميقات

A single-purpose azaan and prayer-times clock for an Android tablet (Android 8.0+, designed for landscape).

- Five daily prayer times plus sunrise, calculated on-device with the [Adhan](https://github.com/batoulapps/Adhan) library
- Home screen shows the next prayer with a countdown, or "azaan was N min ago" for a configurable window after each azaan
- Sky palette shifts through the day (Fajr indigo → Dhuhr gold → Asr amber → Maghrib rose → Isha midnight)
- Azaan plays at each prayer through the alarm channel; exact alarms survive Doze, reboots and DST changes
- Timetable for any month, with Hijri dates and Fridays highlighted
- Settings: location (auto or search), 11 calculation methods, Asr madhab, per-prayer minute offsets, Hijri ±day adjustment, azaan per prayer, custom azaan MP3, volume, pre-reminder, keep-screen-on, night dimming, art theme, launch on boot
- Offline after first location fix. No account, no ads, no analytics.

## Installing on the tablet

1. Open the repository's **Releases** page on the tablet and download `Miqaat.apk` (or pick it up from the latest **Actions** run's artifact).
2. Open the downloaded file. Allow "install from this source" if asked.
3. On first launch, tap **Use my location**. Then in Settings › Azaan & alerts tap **Battery optimisation › Fix** so Android never delays the azaan.

Every push to `main` rebuilds the APK and updates the `latest` release. The APK is signed with the key in `keystore/`, so updates install over the top without uninstalling.

## Bundling azaan recordings

The app plays, in order of preference: a file you choose in Settings → a recording bundled in the app → the tablet's default alarm tone.

To bundle recordings, drop MP3 files into `app/src/main/res/raw/`:

- `azaan.mp3` — used for all prayers
- `azaan_fajr.mp3` — optional, used for Fajr only

Then push; the next build includes them. Use recordings you have the right to distribute.

## Building locally

Open in Android Studio (Ladybug or newer) or run `./gradlew assembleRelease`.


## Production-readiness documents
- `AUDIT_REMEDIATION.md` — every audit finding mapped to code, tests and status
- `DEVICE_TEST_PLAN.md` — the physical-device matrix (alarm soak, DST, Qibla, TalkBack) that gates a public launch
- `ISLAMIC_REVIEW_PACK.md` — versioned register of every ruling-like statement, with approval fields (currently unsigned)
- `RELEASE_CHECKLIST.md` — Play policy, permissions, signing, immutable tags, checksums

Two editions are built from every commit: `Miqaat.apk` (direct download, SHA-256-verified in-app updater) and
`Miqaat-play.aab` (Google Play, no self-update, no install permission).
