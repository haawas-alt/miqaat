#!/usr/bin/env bash
# Keep the combined result in one shell: emulator-runner launches each script line separately.
set -euo pipefail
rotation="$1"; target="$2"; status=0
actual="$(adb shell getconf PAGE_SIZE | tr -d '\r')"
test "$actual" = "$EXPECTED_PAGE_SIZE"
bash .github/emulator-run.sh "$rotation" "$target-a" homes,homesUrdu,states || status=1
bash .github/emulator-run.sh "$rotation" "$target-d" azaanFlow,learnLessons || status=1
exit "$status"
