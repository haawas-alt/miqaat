# Approved-reference Salah rebuild — 2 October 2026

This supersedes the 1.84-audit Salah artwork. It does not supersede the original run-158 independent audit or claim production acceptance.

## Authorization and backup

The owner explicitly rejected the previous figures and authorized a fresh rebuild using the approved photograph. Before any changes, `backup/pre-approved-salah-rebuild-20261002` was created and read back at `2adf06aa4499cfb63d7af2a421954db27b07de53`. The prior design branch `four-themes` is not changed. Work remains in the draft remediation PR.

## Concrete reference

`image-1790943347105.jpg`, supplied by the owner on 2 October 2026, is the visual source for the Fajr takbir lesson. The local copy was readable despite the earlier missing-path message.

SHA-256: `f0c725ea6a0f388cb1fdc30860dcb362a17eb2a996bb0ad93e911d95f2253c47`.

Observed approval: a natural-proportioned, faceless person in a loose full-length robe; visible fingers and bare feet; fine dark outlines and ivory fill; a cream architectural arch, muted olive/terracotta terrain, sunrise and botanical detail. The tablet layout pairs a left illustration panel with a right reading column, dotted rakʿah progress, large Arabic, Listen/Slow controls, pronunciation, meaning, visible notes and a different-schools affordance.

The exact named archive `miqaat-corrected-approved-mockups-and-tablet-audit-claude-pafkage` (including package and partial-title spelling variants) has not been retrieved. Filename searches and prior-context retrieval returned no matching archive. The supplied photograph permits a concrete first-lesson comparison; it does not establish approved images for the other seven postures or all four theme boards. Do not claim 99% fidelity to an unseen ZIP.

## Rebuilt implementation

- All posture contours are newly authored in `tools/rebuild_salah_art.py`; none reuse the superseded figure paths. Editable SVGs are in `docs/salah-art`, Android vectors in `app/src/main/res/drawable`.
- Eight postures plus a separately drawn left salām view. On the frontal seated figure, anatomical right is viewer-left. Salām labels refer to the worshipper’s right and left, and the views are deliberately not mirrored by Urdu RTL.
- Fixed ivory robe fills and charcoal linework replace the theme-wide monochrome tint. Natural figures remain legible against each scene.
- Gallery uses the approved cream arch, earth tones, sun and olive sprig. Celestial uses a night arch and solar arc; Miqaat adds restrained geometric border detail; Kiswah uses a woven band and warm thread tones. These are newly authored variants, not verified approvals of unseen theme boards.
- Tablet landscape uses the editorial two-column composition, independently scrolling long recitations. Phone landscape uses a scrollable two-column body without miniaturizing the text; portrait and 200% text use a flowing column.
- Arabic has explicit RTL; transliteration is explicitly LTR. Urdu UI uses the bundled Urdu face. Detailed legacy step notes remain English and are disclosed as such.
- Continue grows with its label rather than truncating at two lines. Prayer order, Arabic recitations, recordings, persisted progress and signing identity are preserved.

## Pedagogical checks and limits

| Posture | Drawing distinction | Independent check still required |
|---|---|---|
| Takbir | Upright robe, both open palms raised, individual fingers | Height and school-specific placement |
| Folded standing | Right palm overlaps left wrist, upright body | Exact chest/navel placement and school conventions |
| Rukūʿ | Side view, level back, bent hips, hand cupping knee | Both knees/hands, posture comfort and proportions |
| Rising | Fully upright, open hands at sides | Valid school-specific alternatives |
| Sujūd | Side view, elevated hips, bent arms with forearms clear of floor, compact palm, head/knee/toe contacts | Seven contact points, robe-obscured limbs, exact forehead/nose and toe orientation |
| Between prostrations | Upright seated side view, hands on thighs, folded/upright feet | Sitting variants |
| Tashahhud | Seated frontal view, anatomical right finger at viewer-left | Gesture timing, seating/feet and school variants |
| Salām | Same seated body, two separately turned head profiles | Natural turn direction and shoulder relationship |

Primary text anchors checked: [Bukhari 828](https://sunnah.com/bukhari:828) (back, hands, feet and sitting); [Bukhari 822](https://sunnah.com/bukhari:822) (forearms off the ground); [Muslim 580a](https://sunnah.com/muslim:580a) (right index finger). These checks inform drafting and do not replace named qualified review. The assistant is not a credentialed scholar and cannot issue that sign-off.

## Validation contract

`ApprovedSalahRebuildTest` asserts actual Compose bounds, Arabic/pronunciation/meaning/note text overflow, scroll reachability and a minimum 48dp Continue target. It captures first and scrolled-note states across all four themes × four device/orientation targets × 100/130/200% × English/Urdu, plus all eight postures on tablet landscape. Existing regressions continue to assert distinct artwork and large-text cue reachability. Native JVM renderings are app UI evidence, not physical-device or approved-golden pixel assertions.

The first compile exposed a missing settings argument in the existing `uiFont` API. The new lesson now uses an explicit language-sensitive helper. Any failed run remains failed; only completed passing runs may substantiate build readiness.

Final run IDs, raw capture inventory, visual review and downloadable candidate will be recorded after validation. No fresh paid emulator run has been started for this rebuild.

## External gates that cannot be claimed complete from this cloud session

- Exact archive recovery and independent approval of every posture/theme against its actual board.
- Named qualified Sunni review and Urdu proofreading of the changed draft wording and new drawings.
- Physical TalkBack, switch access, focus, 200% text/touch targets and OEM prayer/alarm/audio/sensor acceptance.
- Play Console upload/prelaunch reports, account eligibility, installed-signature/version compatibility and listing/privacy/data safety approval.

A signed APK/AAB passing local technical checks is a testing candidate, not production acceptance.

## Confirmed content and counter residual fixes

Alongside the artwork, the draft now corrects the evening `bika_e` citation to Ibn Majah 3868 (displayed evening wording ends with al-maṣīr; grade attributed to Darussalam), marks `asbahna`/`amsayna` as excerpts rather than complete Muslim 2723b supplications, and labels abbreviated meanings as summaries. It corrects the Urdu post-prayer three-Quls title/guidance so it covers all three displayed surahs. Arabic recitation text is unchanged.

The tahlil target now has one saved daily counter shared between morning and evening instead of a separate 100 on every reopening/session. The date is the device civil date, not a new religious claim about day boundaries. Unit tests exercise reopening across sessions, day rollover and capping at 100. Other session-specific counts stay separate.

The radeetu source now identifies Hisn al-Muslim 87 and its count-specific references, separately attributing the grades attached to the related Abu Dawud/Tirmidhi texts. This corrects the previous conflation; chain-specific verification remains in the qualified review queue. New Urdu wording is still a proofreader/qualified-review draft.

## Final cloud checkpoint — 3 October 2026

Source candidate: `3903c60b5ffcab8645fcb176282c962cfda71bc6`. The final refinement commit independently repositions the lower postures to the common floor line, moves takbir palms outside the shoulders, redraws sleeve bends and makes the anatomical-right tashahhud index upright. Lesson scene cropping is bottom-aligned so the prayer mat remains visible in portrait aspect ratios.

The previous checkpoint 104b745 had 408 raw PNGs, with the complete 96-case takbir matrix captured in initial, navigation-checked and scrolled-note states (288), eight postures × four themes on tablet landscape (32), two shared-counter states and 86 existing regression captures. XML reports show 82 reported tests: 81 executed successfully and one opt-in full-page screenshot suite skipped. This is predecessor evidence, not proof of the final changes.

| Target | Raw PNG size | Lesson matrix |
|---|---|---|
| Phone portrait | 822 × 1782 | All four themes, English/Urdu, 100/130/200% |
| Phone landscape | 1782 × 822 | All four themes, English/Urdu, 100/130/200% |
| Tablet portrait | 1600 × 2560 | All four themes, English/Urdu, 100/130/200% |
| Tablet landscape | 2560 × 1600 | All four themes, English/Urdu, 100/130/200% |

These are Robolectric native-graphics renderings of the app, not emulator or physical-device screenshots. Matrices were inventoried programmatically and visually scanned; selected raw captures were inspected at readable resolution. Do not describe this as a pixel-by-pixel review of every screenshot.

### Honest mockup comparison

Gallery reproduces the approved editorial two-column lesson, restrained ivory/charcoal faceless figure, cream arch, earth-toned landscape, foliage and blue controls. It is not a verified 99% replica: the photo's rakah-count subtitle and translucent architectural pillars are absent; geometric proportions, illustration contours, terrain, typography spacing and some control styling differ. The other seven posture approvals and four full theme boards remain unavailable. Their newly drawn figures are review candidates, not independently verified approved golden images.

Portrait and 200% text legitimately scroll. In short phone landscape, the initial viewport shows only part of the body, but the lesson body is scrollable; the screenshot's initial crop does not itself demonstrate inaccessible content. Long recitations require separate scrolling/reachability checks. Detailed legacy notes remain English, with explicit disclosure in Urdu.

### Required acceptance gates

- [ ] Exact approved archive recovered and each actual board identified/versioned.
- [ ] Approved comparison of all eight posture drawings, including school variants, contact points and salām directions.
- [ ] Independent golden comparisons for each theme and device target; no overflow at supported scales/languages.
- [ ] Named qualified Sunni reviewer and fluent Urdu proofreader sign exact candidate/content/art hashes.
- [ ] Physical Android testing of TalkBack, focus order, switch access, touch targets, 200% text, audio, notifications, reboot/DST/OEM restrictions and Qibla sensors.
- [ ] Installed-app upgrade verified against the user's actual signing identity and versionCode, preserving data.
- [ ] Play Console accepts the exact signed bundle; prelaunch reports, signing/version policy, listing, privacy and Data Safety checks completed.

No credentialed reviewer, physical device or Play Console session is available in this task. These gates remain open. The cloud deliverable is an internal testing candidate, not a production release or a 9.9/10 certification.

### Verified final artifacts

- Audit validation: [run 37101494544](https://github.com/haawas-alt/miqaat/actions/runs/37101494544), SUCCESS; artifact 11265957583. Signed validation: [run 37101494556](https://github.com/haawas-alt/miqaat/actions/runs/37101494556), SUCCESS; artifact 11266117453. Both use source `3903c60b5ffcab8645fcb176282c962cfda71bc6`.
- Both final XML report sets contain 82 reported tests, 81 executed passes, zero failures/errors and one opt-in ScreenshotTest skip. ApprovedSalahRebuildTest has three passes; DailyDhikrProgressTest has three passes.
- Final audit archive: 408 PNGs. All 288 takbir matrix states, 32 posture/theme captures and two counter captures exist; no missing expected matrix filenames. Inventory and hashes: `audit/salah-1.85-inventory.csv`.
- Inspected final raw Gallery phone portrait takbir, tablet landscape sujud and paired salām confirm the mat/figure alignment correction. All 32 final posture/theme captures were also visually scanned. Readable raw predecessor language/scale samples informed the final crop correction. This is selected raw inspection plus inventory, not a claim that every final pixel was manually reviewed.
- Version name `1.85-salah`, versionCode `1791007113`, package `com.usman.miqaat`, min SDK 26, target SDK 36. Certificate SHA-256 `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01` matches the existing stable release signing identity.
- APK SHA-256 `e35f0c3bd02c81766e4b5433da3e26b5ebe25e7d07817f02122c5817f0d90bfa`; Play AAB SHA-256 `51caf86c26321c0f8a6cf4dc32a28ec91bbd73d5ce6013e4b134733b1c152a9d`. All three packaged artifact hashes independently matched their recorded sums.
- Build checks include signed APK verification, lint, forbidden assets, target SDK, Play permission inspection, bundletool validation, AAB signature verification and 16KB native/ZIP alignment. Play Console policy/account/listing acceptance is still unverified.

Download assets are published through the separately gated testing-download workflow; verify its conclusion and release asset digest before telling a user that publication succeeded.
