# Cloud acceptance follow-up — 2 October 2026

This is a checkpoint, not acceptance approval. Original source audit: [RUN158_INDEPENDENT_AUDIT.md](RUN158_INDEPENDENT_AUDIT.md).
Changes remain on fix/run158-audit-remediation in [draft PR #1](https://github.com/haawas-alt/miqaat/pull/1).
Backup before remediation: backup/pre-codex-audit-fixes-20261001 at 30122fbfbd4d2e347a28f7f81f0c444f1aaacd57.
No merge or production release has occurred.

## Verdict

Production Play Store: **NO-GO**. Developer testing: conditional, with known outstanding gates.
No 9.9/10 or 99% fidelity claim is justified. The original audit's provisional score is not raised
because source changes and CI execution alone do not establish final visual quality.

The latest downloadable Miqaat-1.84-audit.apk is the signed af185b53529d311ebd9fde5b7a60b7244e9a0701
checkpoint. It does **not** include subsequent eight-posture artwork or the native rebuild.
Do not identify its source from the older hosting tag audit-fixes-3895c823.
SHA-256: 54491209ee9eb64fc26751f38e13ee0ba1b52bf0be6dcbefe681a377bd2f15be.
It remains a testing build; newer release checks exposed a native compatibility defect.

## Evidence and completed work

| Evidence | Source / run | What it establishes | Limit |
|---|---|---|---|
| Original design audit | b5562a073f5555b647943f6cde4597a8805151bf / 36816998537 | 976 original captures, four themes and four device/orientation targets; original audit and inventory saved | Full approved A2/D reference archive not recovered |
| Android 16 acceptance baseline | 50438ad30caf83fbbd9026622c2abcafa9d6e4eb / 36951482898 | All 16 screenshot groups collected; 992 PNGs decoded and checksummed, 248 per device and theme; EN800 / UR192 | Baseline predates later artwork, updater and native changes; 2 phone jobs failed |
| Initial posture rewrite | 96197f0e11887d22543f0f899ff7e26a6a1e8a9e / 36953996416 | Eight distinct assets; native render captures and build/unit/lint validation passed | Not an approved-mockup comparison |
| Latest artwork framing | ed115f15f35389174cc0e107fd16e37a4f88b675 / 36955123830 | Build/unit/lint validation passed | Signed validation 36955123880 failed the native RELRO gate |
| Phone targeted evidence | 96197f0e11887d22543f0f899ff7e26a6a1e8a9e / 36953996423 | Home/Adhkar and lesson evidence captured on both phone orientations | Settings visibility assertion failed; D inventory contained stale A captures |
| Native source rebuild | 0d1299939add9e5ba0f9abd609110afa2c73e160 / 36981684990 | All four ABIs pass LOAD alignment and GNU_RELRO end-boundary checks; managed AAR entries remain identical | Standalone build; app integration/runtime approval is separate |
| Integrated candidate | 68ef915cf155f5bde7c4a2f82f107d107a292ff1 / 36982213126, 36982213180, 36982213209 | Signed, general validation and targeted runtime/phone tests started | Results pending at this checkpoint |

Baseline full-screen dimensions were TL2560×1600, TP1600×2560, PP1080×2400 and PL2400×1080.
Readiness captures include component crops; they must not be presented as complete-screen coverage.
Integrity checks are not 992 individual visual approvals. English/Urdu and text-scale coverage
remain uneven by page as described in the original audit.

## Confirmed residual fixes made

1. All eight Learn Salah illustrations have separate authored assets: raised hands in takbir,
   folded standing, bowed ruku, arms-down standing after ruku, ground-contact sujud, seated pause,
   raised-finger tashahhud and turned-head salam. Bowing and sujud viewports and card fit were
   subsequently enlarged. These are candidates, not certified matches to the unavailable mockups.

2. Compact Home pins the Adhkar entry above scrolling columns. This addresses the confirmed
   landscape entry-access failure; reachability and layout still need the final source's runtime evidence.

3. Settings detail back returns to a saved landing scroll reset. The prior failing XML says
   the prayer-setup heading exists but is not displayed. It does not establish failed navigation.
   The new regression checks actual scroll position zero, visible search, absent detail content,
   and category reachability. It does not require a category heading below a tall readiness card
   to fit above the fold on every short screen.

4. Sequential screenshot groups now remove only prior files/screens captures before collecting
   a new group, and only when the app is installed. Already-pulled evidence and app settings remain.
   Missing/unexpected names still fail strict inventory checks.

5. Updating the published graphics-path dependency alone did not fix the release compatibility
   failure. The replacement is source-built from pinned AndroidX commit
   794e3806700833665f48f56f7dd3581642a6057f with NDK28.2.13676358 and explicit 16KB maximum/common page sizes.
   Source Git blob hashes and upstream AAR SHA-256 are checked. All non-JNI AAR entries remain
   byte-identical. See [native rebuild provenance](docs/GRAPHICS_PATH_REBUILD.md).
   No binary header patch or relaxed release gate was used.

6. Essential runtime validation uses two targeted phone jobs: API33/4KB to exercise the native
   fallback, and API35/16KB to verify actual loader/page-size compatibility. Native tests assert
   actual page size, library loading, path segment types, finite coordinates and iterator termination.
   Existing full Android16 baseline evidence remains separate; this is not a fresh 16-job rerun.

## Installation diagnosis

The release-signed 1.83 and signed 1.84 testing checkpoint share certificate SHA-256:
6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01.

The earlier debug APK certificate is:
bbae28093f432726f9980b7124f874726a9b2b4a711b6db9675dafe656d84fa0.

This confirms the package-signature incompatibility behind updating an installed debug build
with a release-signed build. Running the app in the background does not demonstrate the cause.
The updated self-updater detects compatibility problems before presenting the installer.
Do not recommend uninstalling without considering the user's settings/data.

## Severity-ranked remaining gates

| Severity | Gate | Required closure |
|---|---|---|
| P1 | Final native integration | Signed APKs retain all four rebuilt libraries; strict ELF/ZIP checks, bundletool validation and 16KB runtime tests pass |
| P1 | Final phone acceptance | Both targeted jobs pass corrected Settings/Adhkar/Home checks and independent screenshot inventories; inspect final raw captures |
| P1 | Approved visual comparison | Recover the actual approved ZIP/boards; compare all eight postures and A2/D page layouts at matching viewport sizes; document differences |
| P1 | Qualified Islamic/Urdu approval | Named qualified reviewer signs off the exact content revision, including new Urdu text; AI-assisted checks are not scholar approval |
| P1 | Accessibility and devices | Physical TalkBack/focus order, large text, switch access, audio, alarms/background/OEM behavior; automated lint does not substitute |
| P1 | Store release | Verify production versionCode/key in Play Console, upload final signed Play AAB for internal validation, obtain pre-launch report and complete policy/store declarations |
| P2 | Content source precision | Resolve source/translation excerpt issues through qualified review, including daily vs session dhikr counts, the evening bika citation variant, radeetu count/grade and combined Quls meaning |
| P2 | Urdu completion | Key parity does not cover dynamic text, dates, spoken labels or English lesson notes; review every remaining mixed-language state |
| P2 | Final report/artifact provenance | Update this checkpoint with final CI conclusions and immutable artifact hashes; publish latest testing APK only after those checks pass |

## Explicitly unverifiable

- Exact approved mockup fidelity: accessible ZIP searches recovered recording-script packs,
  not the specific design archive previously supplied to the user. This does not mean the user never received it.
- Qualified human approval for the newly added Urdu wording and posture pedagogy.
- Real Samsung/other OEM background reliability, TalkBack and assistive technology behavior.
- Play Console production signing/version history, account eligibility and store/pre-launch outcomes
  without actual console evidence.
- Final source runtime and signed release conclusions while the referenced jobs remain pending.
- Every page at every language/scale combination: existing evidence has gaps, especially 130%
  outside Home/readiness and Urdu secondary/deep content. Contact sheets do not close these gaps.

## Acceptance required before a 9.9/10 claim

- Approved references identified by exact archive/files and hashes; per-page comparisons reviewed
  at matched viewport sizes with declared tolerances.
- All four themes and four orientations inspected at normal size; tablet Home has no scroll/overlap.
- All eight postures are visually distinct and pedagogically approved, including hands, ground
  contacts, finger and head direction; illustrations match the approved artwork.
- All available English/Urdu100/130/200 evidence reviewed; missing combinations tested or explicitly excluded.
- No clipped text, overlap, inaccessible controls, incorrect orientation or hidden bottom content.
- Behavioral layout assertions and screenshot inventory pass on the final source, with skips justified.
- Qualified content review and physical accessibility/device checks completed.
- Final signed APK/AAB hashes, versionCode, signing certificate, ELF/ZIP alignment and runtime checks verified.
- Play Console internal/pre-launch and applicable store requirements pass before production approval.

CI execution success alone cannot establish this score.
