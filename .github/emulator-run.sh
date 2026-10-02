#!/usr/bin/env bash
# Runs on the CI emulator: fixes the rotation, runs the Settings regression tests first (fast, the acceptance gate for
# Settings), then the screenshot matrix under a time box, and pulls the PNGs out of the app either way.
set -euo pipefail
status=0
ROT="$1"; NAME="$2"; TESTS="$3"
case "$NAME" in *landscape*) ORIENT=landscape;; *) ORIENT=portrait;; esac
# Lock the rotation the reliable way (wm user-rotation), re-applied before every gradle run: the plain settings keys were
# silently ignored on some jobs, which left "portrait" tablet and "landscape" phone screenshots in the natural orientation.
setrot() {
  adb shell settings put system accelerometer_rotation 0
  adb shell settings put system user_rotation "$ROT"
  adb shell wm user-rotation lock "$ROT" || adb shell cmd window user-rotation lock "$ROT" || true
  sleep 3
  mkdir -p shots/"$NAME"; echo "rotation requested $ROT; now: $(adb shell dumpsys window displays | grep -m1 -o 'mCurrentRotation=[A-Za-z0-9_]*' )" | tee -a shots/"$NAME"/rotation.txt
}
setrot
adb shell wm size; adb shell wm density
OUT=shots/"$NAME"; mkdir -p "$OUT"
G="./gradlew --no-daemon -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true -Pandroid.testInstrumentationRunnerArguments.orientation=$ORIENT connectedGithubDebugAndroidTest"

if [ "${NAME##*-}" = "a" ]; then
  # One gradle run per class so a slow or crashed emulator in one test cannot hide the others' results.
  for C in SettingsStateTest HomeLargeTextTest AdhkarEntryTest; do
    setrot
    timeout 900 $G -Pandroid.testInstrumentationRunnerArguments.class=com.usman.miqaat.ui.$C 2>&1 | tee "$OUT"/settings-tests-$C.log | tail -12 || status=1
    mkdir -p "$OUT"/results/$C && cp -r app/build/outputs/androidTest-results/connected/. "$OUT"/results/$C/ 2>/dev/null || true
  done
fi

setrot
# Sequential groups reuse an installed app; fresh isolated groups may not have one yet.
# Clear only stale captures, preserving app settings and already-pulled evidence.
if adb shell pm path com.usman.miqaat | grep -q '^package:'; then
  adb shell run-as com.usman.miqaat rm -rf files/screens
fi
CLASSES=$(for t in ${TESTS//,/ }; do printf "com.usman.miqaat.ui.ScreenshotMatrixTest#%s," "$t"; done)
timeout 1700 $G -Pandroid.testInstrumentationRunnerArguments.class="${CLASSES%,}" 2>&1 | tee "$OUT"/matrix.log | tail -15 || status=1
adb exec-out run-as com.usman.miqaat tar c -C files screens 2>/dev/null > "$OUT"/screens.tar || true
( cd "$OUT" && tar xf screens.tar 2>/dev/null && rm -f screens.tar && mv screens/* . 2>/dev/null; rmdir screens 2>/dev/null ) || true
python3 tools/check_screenshot_inventory.py "$OUT" "${NAME%-*}" "${NAME##*-}" || status=1
exit "$status"
