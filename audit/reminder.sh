#!/usr/bin/env bash
# Runtime check of the pre-prayer reminder path: fire the alarm receiver the way AlarmManager would, then look for the
# Android 8+ foreground-service contract violation. Usage: reminder.sh <apk> <label>
set -uo pipefail
APK="$1"; LABEL="$2"; OUT="out/reminder-$LABEL"; mkdir -p "$OUT"
adb root >/dev/null 2>&1 || true; sleep 2
adb install -r -g "$APK" 2>&1 | tee "$OUT/install.txt"
adb shell dumpsys package com.usman.miqaat | grep -E "versionName|versionCode" | tee "$OUT/version.txt"
for p in POST_NOTIFICATIONS ACCESS_FINE_LOCATION; do adb shell pm grant com.usman.miqaat android.permission.$p || true; done
# open the app once so first-run defaults exist, then leave it
adb shell am start -n com.usman.miqaat/.MainActivity >/dev/null 2>&1; sleep 6
adb shell input keyevent KEYCODE_HOME; sleep 2
adb logcat -c
for n in 1 2 3; do
  adb shell am broadcast -n com.usman.miqaat/.azaan.AzaanAlarmReceiver --es prayer ASR --ez reminder true | tee -a "$OUT/broadcast.txt"
  sleep 14
done
adb logcat -d > "$OUT/logcat.txt"
adb shell dumpsys notification --noredact > "$OUT/notifications.txt" 2>&1 || true
{
  echo "label=$LABEL"
  echo "FGS_not_started_violations=$(grep -ciE 'did not then call Service.startForeground|ForegroundServiceDidNotStartInTimeException' "$OUT/logcat.txt")"
  echo "app_fatal_exceptions=$(grep -c 'FATAL EXCEPTION' "$OUT/logcat.txt")"
  echo "anr_mentions=$(grep -c 'ANR in com.usman.miqaat' "$OUT/logcat.txt")"
  echo "reminder_notification_present=$(grep -c -E 'Prepare for Asr prayer|Asr in a few minutes' "$OUT/notifications.txt")"
} | tee "$OUT/RESULT.txt"
grep -iE "startForeground|ForegroundService|FATAL|AndroidRuntime" "$OUT/logcat.txt" | head -40 > "$OUT/key-lines.txt"
