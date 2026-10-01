#!/usr/bin/env bash
# Runs on the CI emulator: fixes the rotation, runs the Settings regression tests first (fast, the acceptance gate for
# Settings), then the screenshot matrix under a time box, and pulls the PNGs out of the app either way.
set -uo pipefail
ROT="$1"; NAME="$2"; TESTS="$3"
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation "$ROT"
sleep 3
adb shell wm size; adb shell wm density
OUT=shots/"$NAME"; mkdir -p "$OUT"
G="./gradlew --no-daemon -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true connectedGithubDebugAndroidTest"

if [ "${NAME##*-}" = "a" ]; then
  # One gradle run per class so a slow or crashed emulator in one test cannot hide the others' results.
  for C in SettingsStateTest HomeLargeTextTest AdhkarEntryTest; do
    timeout 900 $G -Pandroid.testInstrumentationRunnerArguments.class=com.usman.miqaat.ui.$C 2>&1 | tee "$OUT"/settings-tests-$C.log | tail -12
    mkdir -p "$OUT"/results/$C && cp -r app/build/outputs/androidTest-results/connected/. "$OUT"/results/$C/ 2>/dev/null || true
  done
fi

CLASSES=$(for t in ${TESTS//,/ }; do printf "com.usman.miqaat.ui.ScreenshotMatrixTest#%s," "$t"; done)
timeout 1700 $G -Pandroid.testInstrumentationRunnerArguments.class="${CLASSES%,}" 2>&1 | tee "$OUT"/matrix.log | tail -15
adb exec-out run-as com.usman.miqaat tar c -C files screens 2>/dev/null > "$OUT"/screens.tar || true
( cd "$OUT" && tar xf screens.tar 2>/dev/null && rm -f screens.tar && mv screens/* . 2>/dev/null; rmdir screens 2>/dev/null ) || true
ls "$OUT" | wc -l
