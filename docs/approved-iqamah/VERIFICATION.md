# Iqamah verification status — 19 October 2026

Status: implementation prepared locally; Android compilation and execution are not yet verified.

Passed local checks:
- `git diff --check`.
- All English and Urdu resource XML parses; no duplicate string identifiers.
- Every string resource referenced by the new screen exists; four new labels have Urdu translations.
- Repository destination checked: existing public `haawas-alt/miqaat`, connected account has write/admin permission; source base is the approved Quiet commit ca9b215aaaa13533236a9d6cdd6b9b6eee5449de.

Prepared but NOT RUN:
- `IqamahCounterTest`: rounding at partial seconds, 00:01/00:00 deadline boundaries, progress interval/clamping, long durations.
- `ApprovedIqamahAcceptanceTest`: four themes, EN/UR, phone/tablet portrait and landscape, compact screens, 200% text, midnight/24-hour/Minimal preferences, native text bounds, full Kiswah band and both 48dp action callbacks.
- Existing regression suite, debug/release lint, signed APK/Play APK/AAB, established signing/native-integrity/Play-permission checks.

Attempted local command:
`./gradlew --no-daemon testGithubDebugUnitTest --tests '*IqamahCounterTest' --tests '*ApprovedIqamahAcceptanceTest'`
It failed before compilation because the Gradle 8.14.3 download was unreachable (`java.net.SocketException: Network is unreachable`). No Android SDK/toolchain cache is available locally.

Cloud verification workflow is prepared at `.github/workflows/approved-iqamah-designs.yml`. Automatic approval review rejected source upload to GitHub, requiring explicit user authorization for this destination. No implementation commit has been uploaded and no new PR or candidate APK is available. The remote Iqamah branch exists at the unchanged Quiet base.

Native captures and visual comparison against the approved boards remain pending. No numerical design score, successful Android test result or physical-device acceptance is claimed.
