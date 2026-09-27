# Miqaat · release checklist

Two editions are built from every commit on `main` by `.github/workflows/build.yml`:

| Edition | Artifact | Updates | Install permission | Distribution |
|---|---|---|---|---|
| `github` | `Miqaat.apk` (+ `.sha256`) | in-app, SHA-256-verified | REQUEST_INSTALL_PACKAGES (flavour manifest only) | GitHub Releases |
| `play` | `Miqaat-play.aab` (+ `.sha256`) | Google Play | none | Play Console |

## Every build (automated, fails the build if not met)
- [x] Unit tests pass (`testGithubReleaseUnitTest`) — 11 engine tests + 9 trust tests
- [x] Lint passes with `ContentDescription` and `MissingPermission` as errors
- [x] R8 shrink + resource shrink, `-dontobfuscate`, raw audio/fonts kept via `res/raw/keep.xml`
- [x] Signed with the release key held only in GitHub secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`)
- [x] Immutable annotated tag `v1.<run>` at the built commit; `latest` tag moved to the same commit
- [x] Release body carries `build=` and `commit=`; SHA-256 published for every artifact
- [x] `BuildConfig.GIT_SHA` / `BUILD_TAG` shown in Settings › About

## Before any public release (manual)

### Policy & platform
- [x] `targetSdk = 36`, `compileSdk = 36` (Play requirement from 31 Aug 2026 for new apps and updates)
- [x] Exact-alarm permissions: `SCHEDULE_EXACT_ALARM maxSdkVersion=32` + `USE_EXACT_ALARM` — never both on one device
- [x] `USE_EXACT_ALARM` justification ready for the Play declaration: user-scheduled prayer-time alarms (alarm-clock use case)
- [x] Play edition has no `REQUEST_INSTALL_PACKAGES` and no self-install code path (`BuildConfig.SELF_UPDATE = false`)
- [x] `allowBackup=false` (coordinates never enter cloud backup; in-app settings file instead)
- [ ] Play Console data-safety form: location (on device only), no collection, no sharing; geocoder → Google disclosed
- [ ] Foreground-service (`mediaPlayback`) declaration text prepared
- [ ] Full-screen-intent use case declared (alarm)
- [ ] Android 16 large-screen note: orientation locks are ignored on ≥ 600 dp devices targeting 36; portrait layout exists and was checked (DEVICE_TEST_PLAN E/B)

### Trust
- [x] No prayer times shown until a place is chosen or detected (Setup gate, widget, scheduler) — TrustTest
- [x] Qibla corrected for magnetic declination; accuracy and declination displayed; sensor-less fallback — TrustTest + DEVICE_TEST_PLAN D
- [x] No advertised-but-unimplemented control (traveller qaṣr/jamʿ removed)
- [ ] DEVICE_TEST_PLAN A (14-day soak) signed
- [ ] DEVICE_TEST_PLAN B, C, D signed

### Islamic content
- [x] ISLAMIC_REVIEW_PACK.md lists every ruling-like statement with before/after wording and approval fields
- [x] App says "Unsigned" until a reviewer signs; correction channel linked from About
- [ ] Named reviewer(s) signed content version 2026.09-a (or later)
- [ ] After signing: About text updated in the same commit; content version bumped

### Accessibility
- [x] Interactive targets ≥ 48 dp in home (all three), settings controls, Learn, Qibla, azaan pills
- [x] Semantics: named icon buttons, merged prayer cards with spoken state, headings, radio/chip roles and selected state, live regions on steppers, compass and wave descriptions, widget content description
- [x] Contrast tokens (`textSecondary`, `textMuted`) replace 30–55 % alpha text; daytime sky scrim
- [x] Reduce-motion respected (animator scale 0 → static wave)
- [ ] DEVICE_TEST_PLAN E signed (TalkBack, Switch Access, 200 % font)
- [ ] Remaining: full string resourcing + complete Urdu (settings still English) — tracked in AUDIT_REMEDIATION.md

### Privacy & security
- [x] Updater verifies published SHA-256 before install; discards mismatches; no check before setup; check throttle persisted
- [x] Alarm-volume change persisted and restored after crash; audio focus requested/abandoned
- [x] Location executor leak fixed (main executor + CancellationSignal)
- [x] Privacy screen wording matches both editions
- [ ] Reviewed release APK's manifest with `aapt dump permissions` and recorded in the release notes

### Performance
- [x] R8 enabled; `@Immutable` on engine models so per-second ticks don't recompose the rail/cards
- [ ] Cold-start and frame timing measured on device 6 (wall tablet) and recorded
- [ ] Baseline profile — deferred (see AUDIT_REMEDIATION.md)

## Release steps
1. Merge to `main`; wait for the green build; note `v1.<run>`.
2. Install `Miqaat.apk` from the `v1.<run>` release on the wall tablet and a phone; run the smoke list (setup → home → Settings › Test › full sequence → Qibla → widget).
3. Upload `Miqaat-play.aab` to the Play internal testing track; wait for the pre-launch report.
4. When A–E in DEVICE_TEST_PLAN.md are signed and the review pack is signed, promote; otherwise the release stays "testing".
5. Move the `stable` branch to the promoted commit (`git push origin v1.<run>:stable`) so the `stable` release is the last known-good build.
