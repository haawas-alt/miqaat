# 1.86-detail signed testing checkpoint - 3 October 2026

## Verdict
**GO for owner/developer APK evaluation with the limitations below. Production Play Store remains NO-GO.** Play internal/closed testing is conditional on actual Console upload, signing/version/account checks and the applicable testing requirements. No Console upload, production merge or promotion is claimed.

This checkpoint replaces the rejected 1.85 outline character with detailed original adult-male illustrations for every shared Learn.Posture value. It does not certify the new treatment as identical to the earlier approved line-art mockups, religiously approved, or 99% faithful.

## Exact candidate
- Source: `3e6ae9236f0b9425b757d55f23109b3b5b6ea85e`
- Version: `1.86-detail`; versionCode `1791015453`; package `com.usman.miqaat`; minSdk26, targetSdk36.
- Signed validation: [37109187582](https://github.com/haawas-alt/miqaat/actions/runs/37109187582), artifact11269037125.
- Debug/layout/lint validation: [37109187387](https://github.com/haawas-alt/miqaat/actions/runs/37109187387), artifact11269151930.
- Publication: [37109978637](https://github.com/haawas-alt/miqaat/actions/runs/37109978637).
- Both validation runs succeed at the exact source; 83 JUnit tests =82 passed,0 failed,0 errors,1 skipped.
- The skipped ScreenshotTest is an opt-in legacy capture collector without MIQAAT_SCREENSHOTS. ApprovedSalahRebuildTest (3), DetailedGalleryAcceptanceTest (1), SettingsBackAcceptanceTest (2) and DailyDhikrProgressTest (3) execute without skips.

The release hosting tag predates the candidate. Use source, provenance and asset hashes rather than the tag commit. This is a testing download, not an update to the stable `latest` release tag; install from the direct URL.

## Downloads
- [Miqaat-1.86-detail.apk](https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.86-detail.apk)
- [APK SHA-256](https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.86-detail.apk.sha256)
- [Release-signed Play AAB](https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.86-detail-play.aab)
- [Provenance](https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.86-detail-PROVENANCE.txt)
- [40 raw review images](https://github.com/haawas-alt/miqaat/releases/download/audit-fixes-3895c823/Miqaat-1.86-detail-review.zip)

APK:43,553,835 bytes; SHA-256 `06eecd91e988918a47bb768eaa8ed53e649f9a504191594edd3f779fd19b5b96`.
AAB:43,719,807 bytes; SHA-256 `b0fdef54896ca016ec299b2c510c7ee77fc4589d078e90fd1a703299ed5917ad`.
Certificate SHA-256: `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`, matching prior release-signed1.83/1.84/1.85.
The earlier debug certificate is different: an installed debug copy cannot receive this signed update in place. Preserve user data before a signing-key transition. The backup branch protects repository source, not app data.
Actual owner-device upgrade remains unverified.

## Implemented changes
All eight poses: takbir, folded standing, ruku, standing after ruku, sujud, sitting between prostrations, right-index tashahhud, and salam. Salam has two separate head turns with unchanged body/feet; no whole-body RTL mirroring. Nine transparent WebP resources are included in the minified signed APK and match source byte hashes. Fajr, Maghrib and four-rakah lessons share these resources; the posture library also uses them.

Mat width and ground offsets keep wide sujud and paired salam supported. Normal phone landscape retains the full posture panel while the words column scrolls. Larger scales use flowing content.

Prayer Gallery phone Home: prayer name/time share a baseline (mirrored layout in Urdu); fixed clock column; enlarged80dp rows/24sp labels/32sp row clocks/26dp navigation icons;48dp navigation targets; whole-page phone scaling removed. Footers scroll, and Urdu's larger Nastaliq metrics require more scrolling. Night-dim captures are intentionally darker. Ramadan lines remain.

## Final capture coverage
640 PNGs, all decoded/inventoried. This is native Robolectric-rendered Compose evidence, not fresh physical-device or emulator acceptance.

| Target | Dimensions px | PNGs |
|---|---|---:|
| Phone portrait |822x1782|160|
| Phone landscape |1782x822|156|
| Tablet portrait |1600x2560|184|
| Tablet landscape |2560x1600|140|

| Evidence | Themes / targets / language / scale | Captures |
|---|---|---:|
| All8postures,top+cue |4themes x4targets, EN100%|256|
| Takbir precheck/base/notes |4themes x4targets xEN/UR x100/130/200%|288|
| Shared daily counter |Morning/evening selected progress|2|
| Gallery mobile Home |EN/UR,100%,hours2/12/18/22|8|
| Other layout regressions |Settings,Home,Adhkar,Azaan,Hadith,Qibla targeted cases|86|

Paths: `audit-screens/approved-salah/posture__[pose]__[theme]__[device]__100__[top|cue].png`; `takbir__[theme]__[device]__[scale][__ur][__precheck|__notes].png`; `detailed-gallery/home__phone-portrait__100__hour-[2|12|18|22][__ur].png`.

All546Salah images match the reviewed4d7a55f artifact byte-for-byte; only the Gallery clock paragraph changed afterward. All128normal-scale posture top captures were screened in matrices; individual raw inspection covered all8Gallery phone-landscape poses, the8EN/UR Gallery Home states, and selected phone/tablet/theme/200%views. No claim that all640 images were manually accepted at full resolution. The review ZIP contains32raw Gallery posture-top captures across4targets and8Home states; the full640remain in Actions.

The suite asserts text overflow, reachable content, actual transformed bounds and touch-target sizes. It does not compare approved-reference pixels, judge anatomy or validate Islamic pedagogy. Complete Maghrib/four-rakah action sequences are not newly captured per lesson at all scales/languages.

## Binary checks
Both distribution APKs pass signing, zipalign16KB and integrated native ELF LOAD/RELRO/ZIP alignment across four ABIs. Bundletool validation and PAGE_ALIGNMENT_16K pass for the signed Play AAB. Play flavor excludes REQUEST_INSTALL_PACKAGES and uses the intended exact-alarm permission policy. These checks do not establish Console eligibility or reliable OEM alarm delivery.

Earlier native runtime evidence is scoped in CLOUD_ACCEPTANCE_FOLLOWUP.md: two native tests passed on API33/4KB and API35/16KB; that earlier overall run failed other assertions. No new paid emulator run occurred.

## Residual acceptance gates
| Priority | Gate / confirmed limit | Exact action |
|---|---|---|
| P1 | No named qualified approval of new art/Urdu | Scholar and fluent Urdu proofreader review exact24dhikr IDs,12lesson groups,8pose classes and other religious pages; retain signed scope/version/hashes |
| P1 | Urdu explanatory notes remain English fallback | Qualified translation of every note and school explanation; check rendered RTL/large-text/assistive reading order |
| P1 | Physical accessibility/OEM/signature upgrade not tested | Real devices: TalkBack/switch/focus,200%,alarm/reboot/DST,audio,Qibla,in-place same-key upgrade/data retention |
| P1 | Actual Play Console absent | Upload under correct app/signing identity; confirm versionCode,Data Safety/privacy/listing,prelaunch and account/testing eligibility |
| P2 | Frontal seated art partly obscures left foot; phone landscape details are small | Approve school-compatible anatomy; add approved feet/finger inset or detail view if needed for teaching |
| P2 | Complete approved mockup ZIP unrecovered; new realistic treatment unapproved | Recover verified boards, compare every required page/state/target and obtain owner acceptance; do not claim99% |
| P2 | Final-source coverage is targeted | Capture/review missing actual lesson/state/scale/language combinations before blanket release acceptance |

Qualified review PDF prepared separately:18pages, source comparisons, exact Urdu drafts, all8pose questions and sign-off forms. It explicitly says it is not scholarly certification. Source queue: QUALIFIED_CONTENT_REVIEW_HANDOFF.md.

## Legitimate 9.9/10 acceptance
- Verified approved boards and per-page/target comparisons with documented tolerances.
- Every pose anatomically/pedagogically approved, including right/left hand/foot and school variants.
- All required EN/UR states at100/130/200%reachable with no unintended clip/overlap; all touch targets/focus/contrast accepted on devices.
- Tablet Home at normal scale fits without scrolling/overlap; phone layout independently accepted.
- Full final-source screenshots inventoried, raw reviewed; tests assert real layout regressions and fidelity where reference baselines exist.
- Exact signed APK/AAB pass technical checks and physical upgrade/runtime acceptance.
- Named content approval and actual Play Console gates closed.
Green CI alone does not justify9.9/10.
