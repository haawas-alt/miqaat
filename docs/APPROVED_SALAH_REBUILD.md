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
