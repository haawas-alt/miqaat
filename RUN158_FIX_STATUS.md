> **Superseded checkpoint:** see [CLOUD_ACCEPTANCE_FOLLOWUP.md](CLOUD_ACCEPTANCE_FOLLOWUP.md) for the 2 October signed testing APK/AAB, fresh Android evidence, corrected test findings and remaining release gates. The historical statements below about no emulator execution and the old APK are no longer current.

# Independent audit remediation: saved cloud checkpoint

## Outcome

Confirmed technical fixes are isolated in [draft PR #1](https://github.com/haawas-alt/miqaat/pull/1), targeting four-themes. The original branch has not been merged or released. See [the detailed independent audit](RUN158_INDEPENDENT_AUDIT.md) for the verdict, scores, all-page coverage, eight-posture review, severity-ranked findings and acceptance checklist.

Production Play Store: **NO-GO**. Isolated developer/internal APK testing: **GO**. Closed Play testing: conditional on a validated release-signed Play bundle and release-specific checks. No99% fidelity or complete-repair claim.

## Backup and evidence

- Original design source: b5562a073f5555b647943f6cde4597a8805151bf; [run158 /36816998537](https://github.com/haawas-alt/miqaat/actions/runs/36816998537).
- Backup before any edits: backup/pre-codex-audit-fixes-20261001, at30122fbfbd4d2e347a28f7f81f0c444f1aaacd57.
- All16 run158 screenshot ZIPs downloaded;976 PNGs decoded and checksummed,244 per device/orientation. Exact paths, scales, language, dimensions and SHA-256: [inventory CSV](audit/run158-inventory.csv).
- Current ci-screenshots at86cd9fd identifies run159.657 PNGs equal originalrun158 and319 differ. Use run158 artifacts as the immutable source for this audit; do not confuse recapture differences with a new design implementation.
- No new emulator execution. All new validations use cloud build/JVM/native graphics and instrumented-test compilation.

## Implemented fixes

- Celestial/Gallery physical top-left tablet board origins with RTL text preserved.
- Dedicated readable short-landscape Home columns; every prayer reachable in EN/UR.
- Measured Azaan flow header, separate Arabic/title rows, scrollable body; active Hadith step auto-scroll.
- Scrollable Qibla reading sections, separate titles, localized declination and theme-neutral Qibla needle instructions in EN/UR.
- Short-height Settings landing/detail routing; decorative theme previews with stable internal scale and additional height, actual text still respects user scale.
- Learn portrait/short-landscape/large-text stacked cards, larger explicit illustrations, naturally growing captions/cards; attached tashahhud finger.
- Draft Urdu posture descriptions, lesson cues/meanings, Adhkar and dua; localized headers/copy. Arabic originals, prayer order, core engine/service and narration library are preserved. Qualified review remains mandatory.
- Adhkar progress labels localized and Previous/Next minimum48dp targets. New EN/UR200% meaning/navigation regression.
- Capture errors and orientation failures propagate; shell errors no longer masked; exact screenshot inventory/integrity/orientation/SHA checks with negative test cases.
- Emulator validation blocks four-themes preview publication; new evidence paths are run/attempt/commit-specific. The isolated validation workflow publishes no release and uses no release secrets.
- Historical acceptance claims are explicitly labelled as such; historical AUDIT_REMEDIATION.md remains preserved.

## Validation

Run8 /36849969802 atbab9c35c: success. Downloaded finalXML shows69 tests,0 failures/errors,1 opt-in collector skipped;70 targeted raw PNGs. Builds, lint and instrumented-test compilation pass.

Run9 /36858888324 ateca6876: success for the theme-neutral Qibla instruction change.

Run10 /36860082951 at2327de2: test compilation failed because the new bounds assertion used nonexistent DpRect width/height properties. Corrected by computing dimensions from its edges; application/debug builds and lint had completed, but this run is not a test pass.

Pre-final validation: Success: [run12 /36862177246](https://github.com/haawas-alt/miqaat/actions/runs/36862177246), code checkpoint **2bae2a5d7ccd2cbd964aac3b1b4bab63321a2ab1**.70 reported tests,0 failures/errors,1 opt-in collector skipped;69 executed.86 downloaded/decoded raw PNGs, including16 EN/UR200% Adhkar captures. Builds/lint and Android-test compilation pass. [Artifact11162701147](https://github.com/haawas-alt/miqaat/actions/runs/36862177246/artifacts/11162701147),178,122,434 bytes. Run11's merged-semantics lookup failure was fixed by selecting the actual unmerged reader text; assertions remain.

## Final verified cloud checkpoint

[Run13 /36863571017](https://github.com/haawas-alt/miqaat/actions/runs/36863571017) succeeded at source **3895c823d2b5f3f33bb51c9bafd55046ec208c09**. Downloaded artifact [11162313402](https://github.com/haawas-alt/miqaat/actions/runs/36863571017/artifacts/11162313402),178,119,276 bytes: ZIP integrity passes; all86 PNGs decode; XML totals70 tests,0 failures/errors,1 expected opt-in collector skip (69 executed). Builds, lint and instrumented-test compilation passed. These are targeted native regressions, not a fresh full Android emulator acceptance matrix.

Raw run12 Urdu landscape Adhkar showed progress fractions visually reversed as `3 / 0` despite green CI. The final source isolates the numeric fraction left-to-right and asserts actual glyph bounding-box order. The final raw Prayer Gallery Urdu phone-landscape200% capture visibly reads `0 / 3`; the regression passes across all four themes. This confirmed P2 issue is fixed. Full progress-state/device/content acceptance remains open.

## What remains open

- Pin exact approved A2/D and all-page board provenance; make matching raw comparisons. Approved-mockup identity is not established by source comments or implementation sheets.
- Fresh full Android acceptance after source changes; native regressions use different viewport/inset geometry from original emulators.
- Qualified Urdu/Islamic/pedagogical approval; complete approved Hadith and expanded-note translations.
- Every lesson step and school/source note, both salam turns, all-progress/empty/completed/selected/scrolled states.
- Real TalkBack/contrast/targets200% acceptance; actual alarm/reboot/DST/OEM/audio/sensor behavior.
- Release-signed Play AAB/store metadata/privacy/Data safety/current policy/prelaunch/device results.
- The original 976 captures were completely inventoried and integrity-checked, with visual screening and raw/source spot checks. Do not claim every image received an individual full-resolution manual inspection.

The report defines exact closure conditions. Missing device, approved-reference and qualified-review evidence is not a passing result and cannot be fabricated in cloud CI.
