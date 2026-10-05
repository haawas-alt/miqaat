#!/usr/bin/env bash
# usage: run.sh <device-name> <rotation> <variant>
set -uo pipefail
NAME="$1"; ROT="$2"; VAR="$3"
ALL="Miqaat,Kiswah,Celestial,Gallery"
case "$VAR" in
  themes)  MODE=crawl;   THEMES=$ALL; LANG_=en; FONT=1.0; DEPTH=2; MAXA=70; BUD=100;;
  urdu)    MODE=crawl;   THEMES=$ALL; LANG_=ur; FONT=1.0; DEPTH=2; MAXA=40; BUD=100;;
  font13)  MODE=crawl;   THEMES=$ALL; LANG_=en; FONT=1.3; DEPTH=1; MAXA=14; BUD=60;;
  font20)  MODE=crawl;   THEMES=$ALL; LANG_=en; FONT=2.0; DEPTH=1; MAXA=14; BUD=60;;
  lessons) MODE=lessons; THEMES=$ALL; LANG_=en; FONT=1.0; DEPTH=1; MAXA=1;  BUD=110;;
  azaan)   MODE=azaan;   THEMES=$ALL; LANG_=en; FONT=1.0; DEPTH=1; MAXA=1;  BUD=110;;
  times)   MODE=times;   THEMES=$ALL; LANG_=en; FONT=1.0; DEPTH=1; MAXA=1;  BUD=110;;
esac
OUT="out/$NAME-$VAR"; mkdir -p "$OUT"
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation "$ROT"
adb shell wm user-rotation lock "$ROT" || adb shell cmd window user-rotation lock "$ROT" || true
sleep 3
{ adb shell wm size; adb shell wm density; adb shell getprop ro.build.version.release; adb shell dumpsys window displays | grep -m2 -E "mCurrentRotation|cur="; } | tee "$OUT/device.txt"
adb install -r -g apk/Miqaat.apk 2>&1 | tee "$OUT/install.txt"
for p in ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION POST_NOTIFICATIONS; do adb shell pm grant com.usman.miqaat android.permission.$p || true; done
adb emu geo fix 150.80 -33.99 || true   # Gledswood Hills NSW
adb shell settings put global window_animation_scale 0; adb shell settings put global transition_animation_scale 0; adb shell settings put global animator_duration_scale 0
adb shell settings put secure immersive_mode_confirmations confirmed || true
adb logcat -c
python3 audit/crawl.py --out "$OUT" --tag "$NAME-$VAR" --themes "$THEMES" --lang "$LANG_" --font "$FONT" --depth "$DEPTH" --max-actions "$MAXA" --budget-min "$BUD" --mode "$MODE" 2>&1 | tee "$OUT/crawl.log" | tail -5
adb shell dumpsys package com.usman.miqaat | grep -E "versionName|versionCode|targetSdk" | tee -a "$OUT/device.txt"
ls "$OUT" | wc -l
