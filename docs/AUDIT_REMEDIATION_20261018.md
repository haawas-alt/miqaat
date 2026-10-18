# Audit remediation status

Baseline: a5028c6424ab51f2186fc108ee71dfbe5ac29959, release gallery-tablet-1.163-25.
Backup: backup/gallery-1.163-25-before-audit-fixes-20261018.
Work branch: fix/audit-1.163-25-remediation.

This status records implementation, not independent acceptance. CI results, raw captures and artifact provenance determine verification. No automatic numerical quality score is awarded.

| Finding | Change | Remaining acceptance |
|---|---|---|
| F01 | Short or scaled landscape Iqamah uses a number-first composition without an obscuring fixed footer | Native layout tests and raw review; physical service flow |
| F02 | Settings rows account for font scale; wide theme preview always stacks | EN/UR native bounds and selection tests; full Settings interaction |
| F03 | Later-rakah Fatiha includes a separate basmala cue; existing audio identity, action counts and saved indices preserved | Exact Arabic/Urdu/English and school-specific qualified review; audio listening |
| F04 | Ruku/sujud notes distinguish weak-chain count reports from scholarly recommendation | Qualified scholar approval of new EN/UR note |
| F05 | Dedicated CI builds signed Play AAB and both APKs, verifies identity and manifests | Successful build; Play upload key/account verification; internal-track install and Console checks |
| F06 | Contextual Adhkar doors use semantic kind instead of English substring | Urdu competing-door fixture and callback verification |
| F07 | Numerical compass error claims replaced by qualitative labels | Real compass calibration and no-sensor testing |
| F08 | Portrait tablet uses single-column, wider, bounded reading layout | Long-entry/200% screenshots and physical reading review |
| F09 | Urdu UI corrections and runtime template; Iftar uses the exact attributed published Urdu translation from Mohaddis T2, Abu Dawud 2357 | Source checked; independent full-version scholarly approval and proofreading remain OPEN |
| F10 | Assertions for countdown/selection/basmala; retry diagnostics; unique JUnit publication paths | CI pass; signed binary and full interactive coverage; locked mockup golden review |
| F11 | Device checklist below | Physical execution remains OPEN |
| F12 | Four-theme descriptions, Urdu listing correction and geocoder privacy contradiction addressed | Target audience decision and Console declarations remain OPEN; policy branch must be deployed before store release |
| F13 | Privacy edition label follows distribution flavor rather than debug updater enablement | Release-equivalent fixture review |
| F14 | Both Asr short labels include noon shadow baseline | Qualified bilingual review |

## Physical and Console gates

Do not mark any of these passed without a named tester, device/OS, exact artifact hash, steps, actual outcome and evidence.

- Actual signed upgrade from supported old version; preserve settings and progress; diagnose installer result rather than recommending uninstall.
- Phone and tablet, both orientations, navigation modes/cutouts; EN/UR 100/130/200; all routes and error/empty/completed states.
- TalkBack traversal and roles, actual 48 dp targets, focus, contrast, magnification and Switch Access.
- Quiet preview Show CTA; current and next Azaan; Iqamah transitions, stop/skip and prayer-time/day rollover.
- Screen-off/Doze, reboot, timezone/DST, OEM battery restrictions, denied exact-alarm/notification/full-screen permissions and recovery.
- Actual audio for all recordings, interrupted audio/calls and Bluetooth; slow playback and Arabic correspondence.
- Qualified Arabic/Urdu/English and posture review with exact content version and asset hashes. AI/source checks are not scholar certification.
- Play App Signing/upload-key correspondence, version-code check, target 36 and 16 KB runtime, pre-launch report, Data Safety, full-screen/FGS/exact-alarm declarations, audience/rating and store rights.
- Account-specific closed-testing eligibility and required duration where applicable.

No production publication is authorized by this status file. Candidate artifacts do not prove Console acceptance.
