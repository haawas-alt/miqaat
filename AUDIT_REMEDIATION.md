# Independent audit remediation

Baseline: design commit b5562a073f5555b647943f6cde4597a8805151bf, Actions run 158 / 36816998537. Commit 30122fb only changed acceptance documentation.

Backup before edits: `backup/pre-codex-audit-fixes-20261001` at 30122fbfbd4d2e347a28f7f81f0c444f1aaacd57. Work is isolated on `fix/run158-audit-remediation`. No production release or merge is authorized by this checkpoint.

## Implemented, verification pending

- Celestial and Gallery tablet boards anchor physical coordinates at the top left while their text remains RTL. Absolute offsets alone did not correct the RTL parent origin.
- Azaan sequence status, location/clock and steps reserve measured space. Body content scrolls and the prayer title and Arabic heading occupy separate lines.
- Qibla portrait and landscape text sections scroll; titles wrap separately from the Arabic heading.
- Short landscape Settings uses landing/detail navigation so the readiness panel does not crowd the selected category.
- Decorative theme thumbnails use a stable internal font scale while names, descriptions and actual application content retain the requested scale.
- Celestial/Gallery short landscape Home uses readable scrollable columns rather than a miniature tablet board.
- Learn Salah tablet portrait uses the stacked lesson arrangement and larger figures; narrow posture cards grow with text scale. The tashahhud finger is attached more clearly to the hand.
- Urdu rendering added for posture descriptions, action cues, recitation meanings and adhkar. These are draft translations requiring qualified proofread/religious approval; the Arabic originals and prayer order are unchanged.
- Capture exceptions now fail tests. Shell failures propagate; inventories assert exact names, counts, orientation, PNG integrity and SHA-256 manifests. Urdu Home at 200% is added, making the planned matrix 992 captures.
- Evidence publication uses a run/commit-specific branch. The four-themes preview publication waits for successful emulator validation rather than publishing before it.
- Targeted native Robolectric assertions check bounds, text overflow and reachability. These are regression checks, not golden-image fidelity assertions.

## Cloud validation

`Audit fix validation` compiles GitHub/Play debug variants and instrumented tests, runs JVM/unit/layout regressions and lint, and uploads reports and raw regression images. It launches no emulator and publishes no release. Python inventory checker negative tests run locally and in this workflow.

## Remaining acceptance requirements

- Inspect the new raw regression images and resolve every failing assertion, compiler error and lint blocker.
- Validate changed Android layouts on all four device/orientation targets. Existing run-158 captures cannot prove new-code behavior. Use a targeted emulator run only where JVM evidence is insufficient, then a final matrix when justified.
- Obtain original approved mockups with definitive approval/version provenance. Compare each available approved page at matching dimensions; contact sheets and source comments cannot establish 99% fidelity.
- Full Urdu editorial review, including remaining English content in expanded source/school notes and Hadith. Draft translations are not approved content.
- Qualified review of all eight prayer postures, school differences, recitations and narration/source handling. Distinct drawings do not constitute pedagogical sign-off.
- TalkBack reading order, contrast, touch targets, live announcements and complete 200% navigation on real devices.
- Real-device alarm, reboot, timezone/DST, background restrictions, audio and sensor validation. Screenshot CI cannot establish these.
- Play signing, store metadata, privacy/Data safety, policy compliance and Play Console prelaunch/device reports require release-specific evidence.

No 9.9/10, 99% fidelity or production-ready claim is made. `ACCEPTANCE_STATUS.md` and previous PASS labels remain historical claims, not independent acceptance results.
