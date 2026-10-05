# Miqaat: independent run-158 audit and remediation checkpoint

Audit date: 1 October 2026. Repository: haawas-alt/miqaat. This report separates original design-build evidence, implemented fixes, and unresolved acceptance work. Earlier PASS labels and ACCEPTANCE_STATUS.md are claims, not acceptance results.

## Executive verdict

**The 9.9/10 / 99% fidelity claim is not substantiated. Production Play Store: NO-GO. Isolated developer/internal APK testing: GO. Closed Play testing: conditional, pending a validated signed Play bundle and release-specific checks.**

The original evidence genuinely covers phones and tablets in both orientations. It is not a complete Cartesian product of pages, languages, scales, interaction states, and prayer lessons. Run 158 is green despite visible RTL displacement, miniature landscape Home boards, crowded large-text headers, cramped previews, and sparse portrait lesson layouts. A substantial set of these defects has been repaired on an isolated branch with targeted regression assertions. That does not turn historical screenshots into evidence for the changed code.

Scores below are conservative audit judgments, not measured pixel percentages. Original baseline overall assessment was approximately 5.5/10. The fix branch's provisional assessment is **6.0/10 overall**, using the simple mean of the five provisional categories below. These values reflect evidence limitations as well as defects; they are not a final all-device visual acceptance result. With approved references unresolved, the visual score is a qualitative implementation-quality proxy, not a measured mockup-fidelity percentage.

| Category | Provisional fix-branch score | Basis and limit |
|---|---:|---|
| Functional confidence | 7.4/10 | Cloud build, unit and targeted layout checks; alarm/audio/sensor/OEM behavior unverified |
| Responsive quality | 7.0/10 | Confirmed high-risk layouts repaired; fresh full Android matrix absent |
| Visual fidelity | 6.0/10 | Coherent Home identities, but approved board provenance and complete matching comparisons absent |
| Accessibility readiness | 5.6/10 | Targeted large-text reachability and touch-target improvements; TalkBack and full contrast/device acceptance absent |
| Release readiness | 4.0/10 | Developer builds available; content sign-off, release bundle and store acceptance remain |
| Overall | 6.0/10 | Rounded arithmetic mean; explicitly provisional |

Work-completion estimates such as “75%” describe an engineering checkpoint, not app quality or 75% release certification. Unknown issues can expand the remaining scope.

## Exact provenance and saved work

- Original branch: four-themes.
- Original design commit: **b5562a073f5555b647943f6cde4597a8805151bf**.
- Original [Build run 158](https://github.com/haawas-alt/miqaat/actions/runs/36816998537): ID **36816998537**, workflow **.github/workflows/build.yml**, success.
- Commit **30122fbfbd4d2e347a28f7f81f0c444f1aaacd57** is the later acceptance-documentation change; it is not a new design implementation.
- Backup made before edits: [backup/pre-codex-audit-fixes-20261001](https://github.com/haawas-alt/miqaat/tree/backup/pre-codex-audit-fixes-20261001), at 30122fbfbd4d2e347a28f7f81f0c444f1aaacd57.
- Fix branch: **fix/run158-audit-remediation**, [draft PR #1](https://github.com/haawas-alt/miqaat/pull/1), targeting four-themes. No merge or production release.
- Validated layout implementation checkpoint: **bab9c35c3f37dc78265eb6a474255dae82b97a37**, [validation run 8](https://github.com/haawas-alt/miqaat/actions/runs/36849969802).
- Theme-neutral Qibla instruction correction: **eca68763dd13e2d00f1963b88a4b7b07120cf4ec**, [validation run 9](https://github.com/haawas-alt/miqaat/actions/runs/36858888324), success.
- Adhkar application-source change: **2327de211f375f72917e393452fee2fd47cda312**, localized Adhkar progress and minimum navigation targets, [validation run 10](https://github.com/haawas-alt/miqaat/actions/runs/36860082951). Run10 failed on test compilation; run11 failed because merged semantics hid the scroll parent. Both test defects were corrected without removing assertions. Final [run12 /36862177246](https://github.com/haawas-alt/miqaat/actions/runs/36862177246) at **2bae2a5d7ccd2cbd964aac3b1b4bab63321a2ab1** passed.
- Documentation-only follow-up commits do not change the APK source checkpoint. No paid emulator run has been started for these fixes.

### Important ci-screenshots provenance correction

At inspection, ci-screenshots resolves to **86cd9fd69773b847555142ef1b518cfbf50ea773**, an orphan commit with no parent. Its shots/RUN.txt explicitly says **run 159 commit 30122fb...**. Comparing the Git blob hashes of all 976 PNGs against downloaded run-158 artifact bytes gives **657 identical and 319 different**. Timestamp/recapture changes can explain differences; this is not evidence of a new design build. It does prove the current branch head is not an immutable run-158 archive.

Accordingly, use **run-158 Actions artifacts** and the accompanying [CSV inventory](audit/run158-inventory.csv), not an unpinned ci-screenshots head, for this audit. The inventory records artifact name, exact path inside its ZIP, dimensions and SHA-256 for all 976 original PNGs. Artifact links below lead to ZIPs; the named path is the exact evidence within each ZIP.

No separately identifiable approved-mockup or side-by-side sheet files were found among the 976 original PNGs or in the inspected ci-screenshots tree. The user reports such sheets existed; their availability, exact approved version and provenance remain unresolved. This report does not silently treat implementation captures as approved mockups.

## CI evidence and matrix

Run158 has **17 artifacts in total:16 screenshot ZIPs plus Miqaat-build-158**. The original build ZIP was also downloaded:59 reported JVM tests,0 failures/errors,1 opt-in ScreenshotTest.render skipped (58 executed). It contains GitHub/Play APKs and a Play AAB, with no image or side-by-side sheet files. The GitHub APK and Play AAB match their supplied SHA-256 checksums. The original AAB's SHA-256 is **a4c5d535498f3ce5b10cf1f58bb0a57075fd5d925ec182ac13c5f3a56b8a09d1**; it contains a META-INF/MIQAAT.RSA signing block with certificate fingerprint **6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01**. Signing-block presence is not a full JAR-signature/Play Console acceptance verification. This original bundle does not contain the subsequent fixes. All16 screenshot artifacts were unexpired when checked. All16 ZIP entry lists were inspected; there are no JPEG/WebP/PDF/SVG/HTML sheets or filenames identifying mockups in these archives. The inspected ci-screenshots tree likewise has no identifiable mockup/sheet candidates. The screenshot ZIPs total 976 PNGs, **244 per target**, and every PNG was decoded and checksummed. No orientation mismatch was found among full-screen PNG dimensions.

Workflow jobs: build; 16 emulator jobs; publish-screens. All report success. Android emulator profile/API: pixel_tablet and pixel_6, API 34, google_apis, x86_64. Group assignments are identical for each target:

| Target | Profile / requested rotation | A | B | C | D | PNGs |
|---|---|---|---|---|---|---:|
| Tablet landscape | pixel_tablet / 0 | Home, RTL Home, readiness states | Settings landing/categories | Secondary pages | Azaan flow/eight postures | 244 |
| Tablet portrait | pixel_tablet / 1 | Same | Same | Same | Same | 244 |
| Phone portrait | pixel_6 / 0 | Same | Same | Same | Same | 244 |
| Phone landscape | pixel_6 / 1 | Same | Same | Same | Same | 244 |

Full-screen capture dimensions:

| Target | Dimensions | Full-screen PNGs | Component-only PNGs |
|---|---:|---:|---:|
| Tablet landscape | 2560 × 1432 | 228 | 16 |
| Tablet portrait | 1600 × 2392 | 228 | 16 |
| Phone portrait | 1080 × 2209 | 228 | 16 |
| Phone landscape | 2272 × 954 | 236 | 8 |

Readiness captures measure their component roots, not full app screens: phone portrait 1080×575 / 1080×1291; tablet landscape 2560×262 / 2560×914; tablet portrait 1600×262, 1600×300 or 1600×914; phone landscape 2272×344 / 2272×394 at 100%, and full-size roots at 200%. A wide crop can look landscape even when its device is portrait. That is not necessarily a wrong emulator orientation, but it cannot prove integration with the surrounding Settings page.

### Original screenshot artifact register

| Target / group | Artifact ID | Bytes |
|---|---:|---:|
| Tablet landscape A | [11141953137](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11141953137) | 29,520,955 |
| Tablet landscape B | [11141703990](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11141703990) | 20,747,237 |
| Tablet landscape C | [11142208552](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142208552) | 71,388,490 |
| Tablet landscape D | [11142007916](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142007916) | 57,555,058 |
| Tablet portrait A | [11141908557](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11141908557) | 22,176,589 |
| Tablet portrait B | [11142512217](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142512217) | 24,835,752 |
| Tablet portrait C | [11142397689](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142397689) | 65,716,434 |
| Tablet portrait D | [11142273077](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142273077) | 61,332,498 |
| Phone portrait A | [11142451727](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142451727) | 18,012,800 |
| Phone portrait B | [11142097616](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142097616) | 17,845,568 |
| Phone portrait C | [11142412333](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142412333) | 52,055,382 |
| Phone portrait D | [11142895181](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142895181) | 47,058,487 |
| Phone landscape A | [11142622127](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142622127) | 18,881,937 |
| Phone landscape B | [11142632351](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142632351) | 13,535,280 |
| Phone landscape C | [11142448095](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142448095) | 45,467,800 |
| Phone landscape D | [11142366911](https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/11142366911) | 41,710,642 |

For any row, the artifact URL is https://github.com/haawas-alt/miqaat/actions/runs/36816998537/artifacts/ARTIFACT_ID.

## Evidence coverage by page, theme, device, language and scale

Every row below applies to **all four themes and all four device/orientation targets**. Scale/language cells describe actual filename inventory, not a promise that every state on that page was captured. EN = English; UR = Urdu RTL.

| Page/state | EN scales | UR scales | Important gaps |
|---|---|---|---|
| Home | 100, 130, 200 | 100, 130 | UR 200 absent in original run |
| Settings landing | 100, 130, 200 | 100 | UR 130/200 absent |
| Settings location | 100 | 100 | 130/200 absent |
| Settings display | 100, 200 | 100 | 130 and UR 200 absent |
| Settings Try it now | 100, 200 | — | 130/UR and all preview interactions absent |
| Other Settings categories: about, azaan, health, hijri, iqamah, privacy, times | 100 | — | Larger scales, UR and scrolled ends absent |
| Ready/warning summary | 100, 200 | — | Mostly isolated components; no full-page UR |
| Timetable | 100, 200 | 100 | 130/UR 200; both scroll axes and complete month not demonstrated |
| Learn Salah overview | 100, 200 | 100 | Progress/empty/completed/resume/reset variants not comprehensively captured |
| Eight distinct lesson postures | 100 | — | 130/200 and UR; every step/lesson/school-note variant absent |
| Azaan | 100, 200 | — | 130/UR; real service/audio states absent |
| Dua after Azaan | 100, 200 | 100 | 130/UR 200; long-content end states absent |
| Hadith | 100, 200 | — | 130/UR; entire narration library absent |
| Iqamah countdown | 100, 200 | — | 130/UR; elapsed/live transition states absent |
| Morning Adhkar | 100, 200 | 100 | 130/UR 200; most selected entries, counts/completion/end states absent |
| Evening Adhkar | 100, 200 | — | 130/UR; most selected/progress states absent |
| Friday | 100, 200 | 100 | 130/UR 200; completed/persisted counter states absent |
| Qibla | 100, 200 | 100 | 130/UR 200; live heading/calibration/no-sensor states absent |

The native renderer uses different viewport dimensions from run158: phone portrait822×1782, phone landscape1782×822, tablet portrait1600×2560 and landscape2560×1600. These are test-content captures without the same Android window/inset geometry. They are not pixel-aligned reference comparisons.

Run-8 native regression evidence adds **70 PNGs**: eight isolated postures; 32 tablet-portrait English 200% lesson captures; eight Azaan portrait captures at 200%; four phone Hadith active-step captures; four Qibla instruction captures; four landscape Settings display captures; eight short-landscape Home initial/scrolled captures EN/UR; two RTL tablet Home captures. These are targeted Robolectric/native graphics, not a new Android emulator matrix.

Run12 added a targeted Adhkar regression across four themes × two phone orientations × two languages at200%: **16 additional PNGs**, bringing its downloaded artifact to **86 PNGs**. Final XML: **70 tests,0 failures/errors,1 skipped** (69 executed). All86 PNGs were decoded; the16 Adhkar captures were screened and raw EN/UR high-risk samples checked. [Final artifact11162701147](https://github.com/haawas-alt/miqaat/actions/runs/36862177246/artifacts/11162701147),178,122,434 bytes. These captures show the translation after scrolling, not an initial-screen comparison.

### Visual review scope

All 976 original PNGs were inventoried, decoded and checksummed. Original page/theme/device variations were screened using review sheets generated from those raw bytes; high-risk samples and source were inspected in detail. The 70 run-8 captures were screened across their groups, with raw spot checks for detail and regression assertions reviewed. This is **not a claim of a full-resolution individual human visual inspection of every one of the 976 images**, nor a complete approved-mockup comparison. Contact sheets were used for triage, never as sole proof of fine text legibility, contrast or fidelity.

## Page-by-page visual and responsive review

Approved-reference comparison status is explicitly **unverified** on every row until the exact approved board is identifiable and matched. The observations below concern visible implementation behavior and the intended Celestial/Gallery identity, not invented pixel differences.

| Page | Run-158 observation | Remediation and residual acceptance |
|---|---|---|
| Home: Celestial Meridian | Strong sunset/meridian identity; tablet EN normal-size composition fits a single viewport. RTL landscape places hero/time/prayers beyond the physical board; phone landscape miniaturizes the tablet board with unused side space. | Physical top-left board anchor repaired; dedicated short-landscape columns added; native RTL tablet bounds and EN/UR prayer reachability checks pass. Fresh Android four-target acceptance, normal-size non-scroll checks and approved A2 comparison remain. |
| Home: Prayer Gallery | Authored architectural hero and prayer cards; normal tablet EN composition fits. RTL board displacement and scaled miniature phone landscape analogous to Celestial. Fajr/Isha night motifs are similar, not proof of unique art for every prayer. | Anchor and short-landscape layout repaired. Preserve readable card art and spacing; compare actual approved D hero, crop, typography and timeline. Do not assert every illustration is unique. |
| Home: Miqaat | Recognizable illuminated arch on tablet landscape; portrait and large-text variants switch to common cards. | Identity is clearer on landscape Home than shared secondary screens. Verify ordinary-size Home and accessible fallback on actual devices; no fresh complete matrix. |
| Home: Kiswah | Black/gold, ornament and distinctive lettering on normal landscape Home; portrait and accessible pages largely share Miqaat structure. | Theme-specific Home identity exists; secondary-page authorship remains mainly token/font/surface variation. “Four fully authored designs” is too broad. |
| Settings landing/readiness | Phone uses grouped categories; tablet rail uses readiness above detail. Short landscape is treated as a wide rail and loses much of the category content below readiness, especially at 200%. | Short landscape now uses landing/detail routing. Ready/warning crops do not verify whole-page integration. Search, deep links, fixes, return position and category recreation require acceptance. |
| Settings categories/display | Normal screenshots cover categories. Tiny previews at 200% can have overlapping clock/labels; long categories have only initial viewport evidence. | Preview internals keep a stable decorative scale and have more height; actual user copy still scales. Selected/reselected states and end-of-category content remain to review. |
| Try it now | Theme selection and entry controls exist; many are below the first screen, particularly short landscape. | Reachability must be asserted for every control, and each preview must return correctly. A screenshot of the page title does not validate preview behavior or sound. |
| Timetable | Dense month table; phone appropriately requires horizontal scrolling; first capture shows only early columns and dates. At 200%, a short landscape viewport can show almost no data rows. | This can be intentional scrolling rather than clipping. Require both-axis reachability, sticky/clear headers, last date and Isha assertions at all scales. Full-month acceptance is open. |
| Learn overview | Lesson choice/progress affordances exist. Phone landscape first viewport is mostly introduction; tablet portrait leaves substantial unused space. | Scroll is acceptable if discoverable and all choices reachable. Native regression does not cover all progress, reset, completion and resume states. |
| Learn lessons | Tablet portrait uses tall narrow side-by-side panels with small central figures and large blank areas. Phone landscape illustrations are tiny. Captures only choose the first occurrence of each posture in FOUR; they do not cover every lesson page. | Portrait, short landscape and large text now stack naturally growing panels with explicit larger figures. 32 tablet-portrait 200% posture-cue checks pass. School notes, every lesson step, all languages/orientations and teaching sign-off remain. |
| Azaan | Strong identity on themed backdrops, but normal portrait has very small text and large empty regions. Large-text status, clock and step row can crowd. | Measured, nonoverlapping header; separate Arabic/prayer headings; body scrollable. Stop control retained. Native tests verify portrait title and Stop reachability. Audio, service lifecycle, live transitions and landscape variants remain. |
| Dua | Arabic, translation and source presented on the same background as Azaan. One original UR sample is not proof of complete translated content at all scales. | Draft Urdu dua added and shared flow layout improved. Recitation/editorial review, source correctness and full-content navigation remain. |
| Hadith | Repeated first narration across screenshot matrix; provenance visible but small. At 200% phone, active step may scroll off the step row. | Active step auto-scroll repaired. Urdu uses an explicitly labelled English translation where full Urdu is absent. Full narration localization and all-library readability are open. |
| Morning/Evening Adhkar | First Ayat al-Kursi is naturally shared between modes; this is not automatically repeated-art failure. Tablet portrait content is centered in a large area with small secondary text. At 200% translation/source can be below the viewport. | Reader is scrollable; new tests verify translation reachability. Progress labels localized and navigation targets enlarged. Counter changes, selected entry, completed list, scrolling and persistence need full acceptance. Sparse portrait typography requires design review against approved boards. |
| Friday | Useful reading/counting cards, hour-of-acceptance text and sunnah list. Phone/short landscape captures show only the first cards. | Require bottom-list access, correct local date/time, progress persistence and qualified wording review. A page capture does not establish calendar or religious-content correctness. |
| Qibla | Static fixed heading is used; sensor accuracy and declination are labels, not live sensor proof. Original 200% portrait instructions are outside/at the edge of the viewport. Gallery blue needle is incorrectly called gold. | Scrollable reading sections, separate titles and localized declination; theme-neutral needle wording fixed. Native instruction-reachability checks pass. Device orientation, calibration, no-sensor guidance and real bearing verification remain. |

### Authored themes and artwork

The four normal-size landscape Homes are structurally recognizable: Celestial sunset/meridian; Gallery architecture/card gallery; Miqaat illuminated arch; Kiswah black/gold ornament. They are not merely four colors of one Home.

Secondary pages overwhelmingly reuse one layout with token/font/backdrop changes. Celestial repeatedly uses the sunset landscape; Gallery repeatedly uses abstract mountains/cream surfaces outside Home. This creates consistency but does not establish individually authored page artwork. No evidence supports a requirement that each page needs new art, and inventing new art without the approved reference would be an uncontrolled redesign. Record it as a fidelity question, not an automatic mandate to replace every background.

## Eight Learn Salah postures

Native isolated captures contain no labels and are all pixel-distinct, so the difference is not only in captions. The diagrams remain schematic. Geometry, labels and recitations must be evaluated together by a qualified reviewer.

| Posture | Visually distinct cue | Limit / exact acceptance |
|---|---|---|
| Takbir | Both arms raised beside the head | Verify hand/palm height and school-specific wording; enlarged illustration and full cue visible at 200% |
| Folded standing | Hands meet/fold across torso | Distinct from lowered-arm standing; teach placement without implying one universally required position |
| Ruku | Horizontal bent torso, hands toward knees | Verify back/head alignment and knee/hand position; current schematic is recognizable |
| Standing after ruku | Upright torso with arms lowered | Distinct from folded standing; confirm full-upright cue and recitation are correct |
| Sujud | Head near mat, folded legs/raised back | Recognizable silhouette, but required contact points and toe/hand placement are not individually taught by the diagram |
| Sitting between prostrations | Seated figure with hands resting | Distinct from standing/sujud; feet/knee placement needs qualified review |
| Tashahhud | Seated figure with raised finger | Finger attachment improved; small original diagonal cue was ambiguous. Review finger timing/direction and school notes |
| Salam | Seated profile/head indication and turn arc | Distinct from sitting/tashahhud. A single static profile is not a full demonstration of both right and left turns; verify right/left instruction and any preview state |

The original eight-posture matrix captures EN 100% only and one selected action per posture. It does not validate all 21/29/36 lesson actions, repeated prostration transitions, expanded school/source notes, every recitation/audio file or learning progress.

## Severity-ranked findings and exact remediation

P0 = critical ship blocker demonstrated; P1 = material defect or acceptance blocker; P2 = substantial quality/coverage issue; P3 = minor issue. “Fixed” below means implemented with the stated targeted evidence, not independently accepted on every device.

### P0

**No P0 crash, destructive defect or security incident is established by the reviewed UI evidence.** This is not proof that no P0 exists outside the audited evidence.

### P1

| ID / status | Finding and evidence | Exact remediation / closure |
|---|---|---|
| P1-01 Fixed, device acceptance open | RTL Celestial/Gallery landscape board shifted off-screen. Run-158 A: home__celestial_meridian__tablet-landscape__100__ur.png and Gallery equivalent; phone landscape equivalents. | Use absolute physical origin with RTL text inside; retain all time/prayer bounds. Fixed in CelestialHome/GalleryHome; native RTL tablet bounds pass. Verify same screenshots on Android and no normal-size scrolling/overlap. |
| P1-02 Fixed, device acceptance open | 200% flow status/clock/steps crowded and Hadith active step unavailable. D: azaan/hadith + phone-portrait + 200 names. | Allocate measured header rows; separate Arabic title; scroll body and active step into view. Targeted native assertions/captures pass. Extend to UR/landscape and real transitions. |
| P1-03 Fixed, device acceptance open | Qibla 200% portrait instructions not properly accommodated. C: qibla__prayer_gallery__tablet-portrait__200.png, corresponding themes. | Scroll reading column with complete title and instruction; verify instruction and Back both reachable. Native instruction test passes; sensor acceptance pending. |
| P1-04 Fixed, broader acceptance open | Short landscape Settings readiness crowds category details; 200% previews overlap. B: settings-display/settings-test__THEME__phone-landscape__200.png. | Choose layout using height as well as width; detail/landing navigation; decorative thumbnails retain fixed internal scale; content scales normally. Validate all categories, selection and back behavior. |
| P1-05 Fixed, broader acceptance open | Portrait lesson panels sparse/small; high-scale posture cue clipping discovered during expanded regression. D: lesson-takbir__prayer_gallery__tablet-portrait__100.png; earlier failing run-7 large-text capture. | Stack portrait/short-height/large-text lesson panels, make figure/caption cards grow naturally. Native 32 English TP200 posture-cue checks pass. Full step/UR/device acceptance remains. |
| P1-06 Open acceptance blocker | No exact approved A2/D comparison can be traced in downloaded screenshot set; no 99% fidelity measurement exists. | Pin approved originals/version/approval provenance; compare matching dimensions with overlays and recorded layout/typography/art differences. Do not rename implementation shots “approved.” |
| P1-07 Open acceptance blocker | Urdu drafts and Islamic review not signed off; English Hadith/expanded source-school notes remain. Evidence: UrduContent.kt, Learn.kt review marker, ISLAMIC_REVIEW_PACK.md; Hadith translation label. | Qualified Urdu proofread and religious/editorial review; complete approved translations; record reviewer/version and resolve every pending content item before public release. Preserve Arabic originals and source IDs. |
| P1-08 Open acceptance blocker | Fresh full Android acceptance and release-critical device behavior missing after source changes. | Run justified targeted Android checks for changed layouts, then final four-target acceptance; real alarm/reboot/DST/audio/sensor/OEM and TalkBack checks. No matrix success can replace device-specific checks. |

### P2

| ID / status | Finding and evidence | Exact remediation / closure |
|---|---|---|
| P2-01 Fixed | Original screenshot capture catches Throwable and only writes an error file; workflow has continue-on-error and preview publishes before UI validation. ShotSupport.kt, build.yml, emulator-run.sh at b5562a. | Capture/rotation/inventory failures must fail tests and job; remove masking; publish preview only after build/emulator gates. Implemented. Validate through intentional negative checker cases and an actual final matrix. |
| P2-02 Fixed prospectively | ci-screenshots is force-replaced; current head identifies run159 and differs on319 PNGs. | Immutable run/attempt/commit evidence branches and SHA manifests implemented. Keep run158 artifact inventory; do not claim historical provenance is restored by new workflow. |
| P2-03 Fixed, acceptance open | Phone landscape Celestial/Gallery Home miniaturizes board. A: home__THEME__phone-landscape__100.png. | Dedicated readable columns with scrollable prayer list. Native all-prayer reachability EN/UR and initial/last-prayer images pass. Verify discoverable scrolling on Android. |
| P2-04 Open | Captures omit many scales/languages/states; most pages capture initial scroll only. ScreenshotMatrixTest.kt and inventory table above. | Add explicit reachability assertions and named initial/selected/completed/end captures for every required state; cover UR200 and lesson large text; make expectations match actual captured set. Planned full inventory is992 after added UR200 Home, but that matrix is not yet executed. |
| P2-05 Open design review | Small secondary text/sparse centered portrait Adhkar and mostly reused secondary-page geometry/art. C: adhkar-morning__THEME__tablet-portrait__100.png; shared screen source. | Establish minimum readable body/supporting type and bounded reader width/spacing against approved boards; check contrast at rendered background regions. Do not solve with arbitrary new art. |
| P2-06 Fixed; targeted validation passed | Adhkar Previous/Next have no 48dp minimum; visible progress labels hardcode done/once in English. AdhkarScreen.kt. | Use48dp minimum targets, existing localized Complete/Not yet read; new EN/UR200 translation-reachability/navigation regression. Run10 failed on test compilation; run11 failed because merged semantics hid the scroll parent. Both test defects were corrected without removing assertions. Final [run12 /36862177246](https://github.com/haawas-alt/miqaat/actions/runs/36862177246) at **2bae2a5d7ccd2cbd964aac3b1b4bab63321a2ab1** passed. |
| P2-07 Open release review | Screenshots use API34 while app compile/target SDK36. Source app/build.gradle.kts. | Validate target-API behavior and insets on supported current Android versions; no inference that API34 screenshots certify API36 behavior. |
| P2-08 Open evidence gap | Readiness component-only roots can hide overlap with rail/detail; test UI mocks all checks. | Full-page readiness-ready/warning/empty captures plus actual Android permission/action verification at all target sizes. Cropped component success is insufficient. |

### P3

| ID / status | Finding | Exact remediation |
|---|---|---|
| P3-01 Fixed | Gallery Qibla blue needle described as gold in EN/UR | Theme-neutral “Qibla needle” instruction; commit eca6876, validation run9 success |
| P3-02 Open review | Compact lesson/show-source/audio provenance copy can be very small; motif reuse and abbreviations can be obscured in sheets | Inspect full-resolution source/caption/expanded-note states, set readable minimums and wrap without reducing user font scale |
| P3-03 Corrected in this checkpoint | Historical acceptance text can be read as current independent approval | Prepend explicit supersession notice and link this report; preserve original claims as history |

## What the tests actually prove

Original ScreenshotMatrixTest mostly loops themes/scales and calls shot(). It does **not** compare golden pixels, measure mockup similarity, inspect every text layout, or assert every lesson posture's pedagogy. ShotSupport originally swallowed capture failures into an errors file. The emulator job originally had continue-on-error:true; build and branch preview were not dependent on UI acceptance.

There are separate behavior/layout tests (HomeLargeTextTest, SettingsStateTest, AdhkarEntryTest and JVM tests). They are useful, but not universal fidelity tests. Their presence does not mean each of the976 captures has an associated meaningful layout assertion.

The fix branch adds targeted **bounds, overlap, TextLayoutResult overflow, scroll reachability, distinct-art and inventory assertions**. Capture exceptions now fail, orientation is asserted, shell failures propagate, and inventory checks assert expected names/counts, PNG integrity/orientation and SHA-256. Five checker tests include deliberate bad inventories. These are stronger gates, not a 99% visual matching system.

Run8's final downloaded XML totals: **69 tests, zero failures/errors, one skipped**. The skip is the opt-in JVM screenshot collector when MIQAAT_SCREENSHOTS is not enabled; the dedicated native regression class still generated70 images. Builds/lint and instrumented-test compilation passed. Instrumented-test compilation is not execution.

The successful command was:
`./gradlew --no-daemon --continue testGithubDebugUnitTest lintGithubDebug assembleGithubDebug assemblePlayDebug assembleGithubDebugAndroidTest`

Debug signing fallback supports isolated developer installs without release secrets. It is not evidence that a new production-signed Play AAB exists, and debug APKs should not replace an existing release install with a different signing key.

### Skipped tests in run158

Eight skipped SettingsStateTest cases were found in original XML:
- Tablet portrait and landscape each skip phoneDetailStartsAtTopAndBackShowsLanding.
- Phone portrait and landscape each skip selectedCategoryAndItsOwnScrollSurviveRecreation, displayAndArtOpensAtTopWithContentImmediately, switchingFromScrolledCategoryShowsOnlyTheNewCategoryAtTop.

The intention—skip phone-only checks on tablets and rail-only checks on phones—is reasonable. It leaves orientation/layout-specific gaps and cannot be cited as evidence those behaviors passed on skipped targets. Fixes make wide-rail routing/test eligibility depend on both width and available height, preventing short phone landscape from being confused with tablet layouts.

Skipped build workflow steps (main/stable tag/releases and failure-log publication) are expected on a successful four-themes preview. No emulator job step was marked skipped. Expected branch-condition skips must not be confused with missing UI assertions.

## Final verified cloud checkpoint

[Run13 /36863571017](https://github.com/haawas-alt/miqaat/actions/runs/36863571017) succeeded at source **3895c823d2b5f3f33bb51c9bafd55046ec208c09**. Downloaded artifact [11162313402](https://github.com/haawas-alt/miqaat/actions/runs/36863571017/artifacts/11162313402),178,119,276 bytes: ZIP integrity passes; all86 PNGs decode; XML totals70 tests,0 failures/errors,1 expected opt-in collector skip (69 executed). Builds, lint and instrumented-test compilation passed. These are targeted native regressions, not a fresh full Android emulator acceptance matrix.

Raw run12 Urdu landscape Adhkar showed progress fractions visually reversed as `3 / 0` despite green CI. The final source isolates the numeric fraction left-to-right and asserts actual glyph bounding-box order. The final raw Prayer Gallery Urdu phone-landscape200% capture visibly reads `0 / 3`; the regression passes across all four themes. This confirmed P2 issue is fixed. Full progress-state/device/content acceptance remains open.

## Explicit unverifiable/open items

1. Exact approved A2/D originals, approval provenance, all-page matching design boards and quantified 99% fidelity.
2. Full fresh four-theme × four-target Android matrix for the fix source; original evidence cannot validate changed source.
3. Full-resolution individual review of every original image and every new state; review-sheet screening is explicitly distinguished above.
4. Every Learn step/lesson, expanded notes, both salam turns and qualified posture/recitation/source-school accuracy.
5. Qualified Urdu proofread; complete approved Hadith and expanded-note translations.
6. Real TalkBack traversal/live announcements, measured contrast across actual artwork, all interactive target bounds, and200% real-device navigation.
7. Actual alarm delivery after reboot, timezone/DST/location changes, background/battery constraints, media/audio interruption and OEM differences.
8. Real magnetic heading/calibration/no-sensor behavior, bearing accuracy and portrait/landscape sensor remapping.
9. Progress/empty/completed/resume/reset and selected/scrolled states across every page/theme/language.
10. New release-signed Play AAB, version/signature/device compatibility, store screenshots/listing, privacy/Data safety declarations, policy acceptance and Play Console prelaunch results.
11. Long-term artifact availability; the CSV preserves exact checksums/paths, but unexpired today does not guarantee indefinite retention.

These are not requests for permission and not silent acceptance. Qualified human sign-off, physical devices and approved-reference provenance cannot be manufactured from screenshots.

## Acceptance checklist for a legitimate 9.9/10 claim

- [ ] Pin exact approved A2/D and all required theme/page boards; record signed approval/version.
- [ ] Match every approved board at the same dimensions, language, scale/state; inspect overlays and raw images; resolve all material geometry/type/art/spacing differences.
- [ ] Four themes × tablet landscape/portrait × phone landscape/portrait; EN/UR;100/130/200 wherever required, with explicit gaps eliminated or approved.
- [ ] Tablet Home100%: all main information/controls inside one viewport, no scroll and no overlap in EN/UR.
- [ ] Phone Home: readable type and artwork, no miniature tablet board; every prayer/action reachable.
- [ ] Settings: landing, each category, ready/warning/empty summary, Try it now controls, selection, return/recreation and end scrolling verified.
- [ ] Timetable: both axes, last date and Isha reachable with clear headers at200%.
- [ ] All eight diagrams distinct without labels, all lesson steps readable, right/left salam represented correctly; qualified pedagogical/content approval.
- [ ] Azaan/Dua/Hadith/Iqamah: complete headers, body, sources and controls; active step visible; real transitions/audio verified.
- [ ] Adhkar/Friday: selected/progress/completed/end states and persistence, translated meaning/source navigation verified.
- [ ] Qibla: sensor/no-sensor/calibration/rotation and accurate instructional copy verified on devices.
- [ ] Large-text overflow/overlap/reachability assertions and at least 48dp interactive targets; TalkBack and measured contrast accepted.
- [ ] No swallowed captures, masked jobs, unexpected skips or incomplete inventories; immutable run/commit-specific raw evidence.
- [ ] Fresh release-specific Play bundle/store/content/privacy/device/prelaunch checks accepted.
- [ ] No open P0/P1, no material P2; numeric fidelity assessment backed by a documented comparison method, not a green CI badge.

A defensible fidelity score needs a fixed rubric before scoring: layout/spacing30%, typography20%, artwork/crop20%, color/material15%, interaction/state representation15%, judged per approved page and then across the required matrix. Any missing required reference/state is unscored, not an automatic100%. A9.9 aggregate requires no material individual-page mismatch and no open accessibility/release blocker. Pixel similarity can assist comparison, but blank regions, font antialiasing and a changing clock make it insufficient by itself.

## Focused follow-up prompt

“Continue haawas-alt/miqaat from fix/run158-audit-remediation and read RUN158_INDEPENDENT_AUDIT.md and RUN158_FIX_STATUS.md. Preserve backup/pre-codex-audit-fixes-20261001. Fix only reproducible residual issues; do not undo verified RTL origins, natural lesson-card growth, short-landscape routing or capture-failure gates. Use run158 artifact paths/checksums, not the current ci-screenshots head. Verify the latest cloud validation and inspect its raw Adhkar captures. Do not claim 99% fidelity or Play readiness without the checklist evidence. Do not invent approved mockups, translations/sign-off or physical-device results. Keep qualified Urdu/Islamic review and missing device/store evidence explicitly open. No merge, production release or new emulator expense unless essential evidence genuinely requires it.”
