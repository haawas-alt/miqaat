> Superseded testing-download checkpoint: 1.85-salah (source 3903c60b5ffcab8645fcb176282c962cfda71bc6) is now published. See docs/APPROVED_SALAH_REBUILD.md for final validation, download links and remaining external acceptance gates. The older evidence below retains its original source scope.

# Miqaat cloud acceptance follow-up — 2 October 2026

## Executive verdict

The cloud fixes and signed testing checkpoint are verified: revised artwork, compatibility fixes, checked release-signed
testing APK and Play AAB are published. **Production Play Store remains NO-GO.** Approved-mockup fidelity,
qualified content approval, physical accessibility/OEM testing and actual Play Console acceptance are incomplete.

Developer/internal APK testing: **GO with the documented content/design limitations**.
Play internal/closed testing: **conditional** on Console upload, signing/version/account checks and the
applicable testing requirements. This report does not claim a Console upload or production promotion.

Provisional assessment, not a measured pixel-similarity percentage:

| Dimension | Score /10 | Interpretation |
|---|---:|---|
| Overall |6.5| Technical confidence improved; major design/content/release acceptance remains open |
| Functional confidence |7.8| Build, unit, native loader and targeted navigation evidence; actual alarms/audio/OEM behavior incomplete |
| Responsive quality |7.2| Four-target baseline and fresh phone evidence; large-text/state evidence remains uneven |
| Visual fidelity |6.0 proxy| Workmanship proxy only. Approved-reference fidelity is not numerically measurable without the actual archive |
| Accessibility readiness |5.6| Some semantics, contrast and large-text assertions; physical assistive-technology acceptance absent |
| Release readiness |6.0| Signed APK/AAB, correct key and binary checks pass; Store/Console and content/design gates remain |

**9.9/10 /99% fidelity is not justified.** Green execution, unique artwork hashes and contact sheets do not
prove approved-mockup identity or pedagogical correctness. See the original detailed
[run158 independent audit](RUN158_INDEPENDENT_AUDIT.md) for the baseline page-by-page findings.

## Correct downloads and provenance

- [Miqaat-1.84-audit.apk](https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.84-audit.apk)
- [Release-signed Play AAB](https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.84-audit-play.aab)
- [Build/evidence provenance](https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.84-audit-PROVENANCE.txt)

APK42,877,138 bytes; SHA-256 **30a80e89dd129a8337ba6c291bf7ab4356e19298ce73c4a0b208bdfade0055f1**.
AAB43,026,810 bytes; SHA-256 **ed239ac50f8b478ab0595671e1e798baeae5500efe5373685e03cfef049a5e33**.

Exact source: **9c7fb156bcfcaac489d96089a293b09209ae3638**.
Version1.84-audit; versionCode1790930817; minSdk26; targetSdk36.
Signed run [36985972238](https://github.com/haawas-alt/miqaat/actions/runs/36985972238):
artifact11218080995. Published by
[36987054069](https://github.com/haawas-alt/miqaat/actions/runs/36987054069).
GitHub release asset sizes and server SHA-256 digests match the downloaded signed artifact.

The hosting tag audit-fixes-3895c823 predates this source and its older release description may still refer
to a debug checkpoint. **Use the asset hash and provenance, not the hosting tag commit, to identify this build.**
The previous APK at this URL was replaced; an already-downloaded old file does not become the new build.

Signing certificate SHA-256:
6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01.
It matches the existing release-signed1.83. The earlier debug APK used
bbae28093f432726f9980b7124f874726a9b2b4a711b6db9675dafe656d84fa0;
a release-signed APK cannot update that installation in place. Preserve the user's data/settings before
a signing-key transition. Background app visibility was not evidence of the installation failure's cause.
The updater now detects incompatible updates; it cannot change an installed package's signing identity.

## Scope, backup and source identity

Original design: b5562a073f5555b647943f6cde4597a8805151bf, branch four-themes,
[run158 /36816998537](https://github.com/haawas-alt/miqaat/actions/runs/36816998537).
Later30122fb updated acceptance documentation; it is not a new design implementation.

Backup branch backup/pre-codex-audit-fixes-20261001 remains at
30122fbfbd4d2e347a28f7f81f0c444f1aaacd57, verified again before this checkpoint.
All fixes remain on fix/run158-audit-remediation in [draft PR#1](https://github.com/haawas-alt/miqaat/pull/1).
No merge or production release occurred.

The approved A2/D ZIP and full reference boards have not been recovered. The two recovered Miqaat ZIPs
contain recording scripts. Repository/artifact/history searches did not recover the specific design
archive previously supplied to the user. This does not establish that the user never received it.

## Verified cloud results

| Evidence | Conclusion | Practical limit |
|---|---|---|
| Original run158,16 screenshot ZIPs |976 PNGs inventoried, decoded and checksummed;244/device and244/theme | Integrity and visual screening are not976 individual full-resolution approvals |
| Android16 baseline36951482898 at50438ad |992 PNGs across16 groups;248/device/theme; EN800/UR192 |2 phone jobs failed; baseline predates later artwork and native fixes |
| Final signed36985972238 at9c7fb156 |76 tests:75 executed,1 expected opt-in collector skip,0 errors/failures; lint, APK/AAB signing, forbidden-asset, ELF/ZIP and bundle checks pass | Not Play Console or qualified design/content approval |
| Final general36985972216 at9c7fb156 | Build/unit/lint and both-flavor instrumented-test compilation pass; both new SettingsBackAcceptanceTest cases pass | Native JVM geometry differs from some emulator insets |
| Native rebuild36981684990 | Four ABIs pass16KB LOAD and RELRO-boundary checks; managed AAR entries remain byte-identical | Standalone rebuild evidence, supplemented by integrated signed checks |
| Native runtime36982213209 at68ef915 | Two native tests pass on each API33/4KB and API35/16KB target: actual page-size check, JNI load, path iteration and conic conversion | Overall run failed other assertions; these were instrumented debug-harness tests |
| Phone captures36984122910 atb4737afa | All10 capture test methods pass;108 images/device,216 total; strict inventories and every PNG decode/hash verified | Overall jobs fail the old case-sensitive Settings heading assertion, not a capture failure |
| Publication36987054069 | Exact checked APK/AAB and provenance uploaded; server digests match | Testing release only |

The final Android assertion correction was **compiled and verified through both native JVM phone
navigation tests; it was not rerun on an emulator**. The real Android back checks had already passed
scroll position zero, detail disappearance and visible accessible search; their remaining mismatch
was “Prayer setup” versus the UI's “PRAYER SETUP.” No app behavior changed in that correction.
This is deliberately documented rather than relabelling a failed Android run green.

The rebuilt native libraries in the release-signed68ef915 and b4737afa candidates were compared
byte-for-byte across all four ABIs and match. Subsequent changes concern test assertions/fixtures,
not native source or runtime app behavior. Physical ARM device and final minified-release device
acceptance remain separate gates.

## Coverage by page/device/theme/scale/language

All rows cover all four themes where screenshots exist. “Baseline4” means tablet landscape,
tablet portrait, phone portrait and phone landscape in the full Android16 baseline, not final-source
recapture of every page. Original run158 coverage remains in [its inventory](audit/run158-inventory.csv).

| Page/state | Device coverage | Scale/language | Final-source evidence limit |
|---|---|---|---|
| Home | Baseline4; fresh two phones | EN/UR100/130/200 | Fresh top-of-scroll images do not fully show large-text content below the fold |
| Settings landing | Baseline4; new native JVM back tests on both phone layouts | Baseline EN100/130/200, UR100; back tests EN100/Gallery | All-theme/large-text final Settings capture not repeated |
| Settings10 categories | Baseline4 | EN100; Display/Try it now EN200; Location/Display UR100 | No complete final-source category/language/scale recapture |
| Readiness ready/warning | Baseline4; fresh two phones | EN100/200 | Isolated component fixtures; some200% landscape crops clip lower status text |
| Try it now | Baseline4 | EN100/200 | Individual preview/action behavior and physical output need review |
| Timetable | Baseline4 | EN100/200, UR100 | Final-source tablet/phone full-page review not repeated |
| Learn overview | Baseline4 | EN100/200, UR100 | Final overview/progress/empty-state combinations incomplete |
| Eight lesson postures | Baseline4; fresh two phones; native tablet portrait | Fresh phones EN100; native tablet portrait EN200 | No Urdu lesson capture matrix, all lesson steps, expanded notes or separate salam turns |
| Azaan | Baseline4; fresh two phones | EN100/200 | Urdu Azaan/permission/audio behavior not fully evidenced |
| Dua after Azaan | Baseline4; fresh two phones | EN100/200, UR100 | Complete Arabic/translation/content approval remains open |
| Hadith | Baseline4; fresh two phones | EN100/200, first item | Entire library, progress timing and Urdu coverage incomplete |
| Morning Adhkar | Baseline4; targeted native large-text meanings | EN100/200, UR100 baseline; selected native EN/UR200 | Not all progress/completion/translation states |
| Evening Adhkar | Baseline4; targeted native meanings | EN100/200 | No complete Urdu evening screenshot evidence |
| Friday | Baseline4 | EN100/200, UR100 | No final-source expanded content/state review |
| Qibla | Baseline4; targeted native large text | EN100/200, UR100 | Fixed-heading fixtures do not prove physical compass accuracy |
| Iqamah | Baseline4; fresh two phones | EN100/200 | Actual countdown/alarm/audio/OEM behavior remains a device gate |

Fresh phone inventory: [216-image CSV](audit/final-phone-inventory.csv), including immutable source/run,
artifact ID, exact screenshot path, scale, language, dimensions and SHA-256.
EN184/UR32;54 images/theme;108/device.
PP92 full images1080×2209 plus16 readiness crops; PL100 full images2400×1080 plus8 readiness crops.
Component dimensions must not be treated as full-screen evidence.

## Page-by-page residual assessment

- **Home:** Compact landscape Adhkar entry is pinned and its behavioral checks pass in earlier targeted
  device evidence. Large-text Home is scrollable; a200% Gallery Urdu landscape capture shows only
  navigation/date/art above the fold. It does not visually certify the prayer list or Adhkar below.
  Tablet normal-size no-scroll/overlap remains supported by unchanged baseline layout evidence,
  not a new final-source approved-board comparison.

- **Settings:** Category tapping now scrolls to its destination in the test. Back restores landing scroll0.
  Accessible search is queried by content description because decorative placeholder semantics are
  intentionally cleared. Uppercase headings require case-insensitive matching. These test defects
  were not evidence of failed app navigation. Large-text/category/state combinations still require visual review.

- **Readiness:**200% isolated landscape warning captures truncate the last status subtitle.
  This is a fixture/crop evidence gap; the real Settings landing scrolls. Do not use the crop to claim
  complete component visibility or treat it as proof that the embedded screen is broken.
  Fix evidence by capturing the actual scrollable screen and a lower-content state.

- **Timetable:** Readability/contrast fixes and source checks exist. Urdu headers/date/dynamic copy
  and final device captures remain incompletely reviewed. Approved typography/spacing cannot be measured.

- **Learn overview/lessons:** Eight distinct vectors now replace the old geometric figures. Bowing/sujud
  frame sizes are improved; sujud forearm/palm separation was clarified. Full original mockup identity,
  posture pedagogy, every lesson action, progress/completed/empty states and expanded school notes remain open.

- **Azaan/Dua/Hadith/Iqamah:** Fresh phone100/200 captures exist and pass capture/inventory checks.
  Scrolling/measured headers address earlier clipping. Snapshot success does not certify all Hadith items,
  audio transitions, permissions, auto-scroll timing or actual background service delivery.

- **Morning/evening Adhkar:** Large-text meaning/progress fixes have targeted assertions. Daily versus
  per-session counts, source precision, Urdu completeness and completed/reopened states need review.
  A session counter is not a verified shared daily tally.

- **Friday:** Baseline snapshots and source are available; comprehensive final expanded-state/content
  review and approved-board comparison remain absent.

- **Qibla:** Labels, declination copy and scrolling were fixed in source and native tests.
  A fixed-heading capture is not a physical sensor/location accuracy test.

- **Theme authorship:** Home treatments differ; secondary pages and the same pedagogical vectors reuse
  layouts with theme colors/backgrounds. This can provide consistency, but does not demonstrate four
  completely authored page systems or99% adherence to approved boards.

## Eight-posture review

| Posture | Observed improvement | Remaining acceptance |
|---|---|---|
| Takbir | Both hands visibly raised | Palm/finger detail and shoulder/ear height need approved-reference and qualified review |
| Folded standing | Visibly distinct folded arms | Hand stacking/placement is stylized and not unambiguous |
| Ruku | Bent torso/level back, hands at knee area | Confirm precise hand/knee/head teaching geometry |
| Standing after ruku | Upright, arms lowered; distinct from folded standing | Match the adopted teaching variant and cue |
| Sujud | Ground contact, larger viewport; compact palm and raised forearm clearer | Forehead/nose, knees/toes and exact posture still require qualified approval |
| Sitting between prostrations | Separate seated silhouette | Foot/knee placement and teaching detail remain stylized |
| Tashahhud | Raised finger distinct from ordinary sitting | Gesture appears on viewer-right of a frontal-looking figure; anatomical right-hand orientation must be confirmed, not assumed from comments |
| Salam | Turned profile and arrow differ from seated/tashahhud | One profile is reused for the action “Right, then left”; a separate left-turn depiction is not evidenced |

Unique asset hashes prove different pixels, not correct anatomy or pedagogy.
See [content review handoff](QUALIFIED_CONTENT_REVIEW_HANDOFF.md) for all24 dhikr entries,12 lesson groups and8 postures.

## Severity-ranked findings and exact closure

No confirmed P0 was found. Absence of a P0 is not release approval.

| Severity | Finding / evidence | Exact remediation |
|---|---|---|
| P1 | Approved A2/D/design archive unavailable; original audit and search record | Recover the actual approved ZIP/boards, pin filenames/hashes and compare matching viewports page-by-page |
| P1 | Qualified approval absent for new Urdu/artwork; UrduContent.kt and new vectors | Named qualified Sunni reviewer and Urdu proofreader sign off exact content/artwork revision and exclusions |
| P1 | Tashahhud hand orientation ambiguous; posture-art__tashahhud.png and learn_pose_tashahhud.xml | Confirm viewpoint and anatomical right hand; correct gesture/approved art where needed; retain reviewer evidence |
| P1 | Physical accessibility/OEM/alarms/audio/sensors not accepted | Execute real TalkBack/switch/focus/200%/alarm/reboot/DST/OEM/audio/Qibla checks on representative devices |
| P1 | Play Console/store acceptance absent | Verify key/version history/account eligibility, upload final Play AAB for internal validation, obtain pre-launch results and complete store/policy declarations |
| P2 | Salam has one profile for two turns; Learn.actions and learn_pose_salam.xml | Provide approved distinct right/left teaching depictions or a clearly approved sequenced illustration |
| P2 | pp_ikhlas Urdu covers only Ikhlas while Arabic combines three Quls; UrduContent.kt/Adhkar.kt | Align title and full meaning/explicit summary with all three, with Urdu reviewer approval |
| P2 | bika_e/radeetu reference precision, shortened asbahna/amsayna and meanings | Adopt exact edition-specific citations/grade attribution and labelled excerpts or approved complete translations; see handoff |
| P2 | Daily tahlil source versus independent session counters | Implement/review an accurate daily tally or explicitly scoped counting model without misrepresenting prescribed timing |
| P2 |200% readiness crop truncates status; phone-landscape-a/state-warning__prayer_gallery__phone-landscape__200.png | Capture actual scrollable Settings and lower-content state; do not certify completeness from the crop |
| P2 | Large-text Home capture is header-only; phone-landscape-a/home__prayer_gallery__phone-landscape__200__ur.png | Add scrolled prayer/entry evidence at200% and verify reachability/targets, not just top-of-page appearance |
| P2 | Urdu/page/state/scale combinations missing; coverage table/inventory | Fill named gaps or explicitly exclude them from acceptance; key parity is not complete localization |
| P2 | Tests do not assert approved mockup fidelity | Add reference-matched comparison/reviewer thresholds; preserve functional layout assertions separately |
| P3 | Existing release tag/body predates signed APK | Keep exact asset/provenance hashes prominent; correct release metadata when permitted; never infer source from hosting tag |

Closed technical blockers: source-built16KB native alignment, correct release signing/provenance,
sequential capture contamination, orientation drift in captures, combined workflow exit status,
Settings accessible/case-aware regression queries, and sujud forearm visual ambiguity.
They do not close the open design/content/device/store findings above.

## Unverifiable items

- Exact approved mockup percentage and missing/changed elements relative to that archive.
- Qualified review identity/credentials/written scope for the new content and illustrations.
- Every image/state at full resolution: all image integrity was checked, but visual review used
  native/raw spot checks and selected sets; it was not complete individual review of all historical captures.
- Full final-source Android instrumented assertion execution after the case-only correction.
- Every page/theme/language/scale/progress/expanded-state combination, including separate salam turns.
- Real ARM/OEM and minified-release behavior, assistive technology, live sensors/background delivery.
- Play Console upload, pre-launch outcomes, production signing/version history, account testing eligibility
  and final listing/privacy/Data safety declarations.
- Store-readiness/legal/content certification cannot be inferred from local bundletool or lint success.

## Acceptance checklist for a legitimate9.9/10 claim

1. Exact approved references recovered, identified and compared at matched viewports with declared tolerances.
2. Four themes × four device/orientation targets visually reviewed at normal size; tablet Home has no scroll/overlap.
3. Every page, every lesson action and all eight postures/turns match approved art and pass qualified teaching review.
4. English/Urdu100/130/200 and all relevant selected/progress/empty/completed/scrolled/expanded states covered.
5. No clipped/overlapping text, inaccessible targets, wrong orientation or hidden content certified from partial crops.
6. Final-source behavioral layout assertions pass; skips are justified; capture success is not labelled fidelity success.
7. Qualified content/Urdu approval and real accessibility/device checks retained with exact revision evidence.
8. Signed APK/AAB source/key/version/hashes, native/ZIP alignment and runtime compatibility verified.
9. Actual Play Console internal/pre-launch/store/account requirements pass before production approval.

Until these pass, keep the testing label and production NO-GO.
