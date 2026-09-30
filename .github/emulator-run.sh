#!/usr/bin/env bash
# Runs on the CI emulator: fixes the rotation, runs the instrumented screenshot matrix, and pulls the PNGs out of the app.
set -uo pipefail
ROT="$1"; NAME="$2"
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation "$ROT"
sleep 3
adb shell wm size; adb shell wm density
mkdir -p shots/"$NAME"
./gradlew --no-daemon -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true connectedGithubDebugAndroidTest 2>&1 | tee shots/"$NAME"/gradle.log | tail -40
adb exec-out run-as com.usman.miqaat tar c -C files screens 2>/dev/null > shots/"$NAME"/screens.tar || true
( cd shots/"$NAME" && tar xf screens.tar 2>/dev/null && rm -f screens.tar && mv screens/* . 2>/dev/null; rmdir screens 2>/dev/null ) || true
ls shots/"$NAME" | wc -l
