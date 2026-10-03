# Detailed Learn Salah artwork rebuild — 2026-10-03

This candidate supersedes the 1.85 handmade outline figures, which the owner rejected. Backup branch: `backup/pre-detailed-salah-rebuild-20261003`, source checkpoint `36048e0eeedb9990b5cc3958f1b0c98c33d8db43`. The production/design branch is not modified.

## Reference and scope
Owner supplied `06-1000162258.jpg` as an anatomical reference and five app screenshots as defect evidence. Rebuild covers EVERY `Learn.Posture` value, every lesson and posture library that calls `Figure`, and both salam head turns. The reference's stock illustration and watermark are not reproduced. New artwork is original, generated with ImageGen, using a consistent adult male character, white kufi, white kurta/trousers, bare feet and natural restrained facial detail. Raster artwork replaces the previous geometric outline figures; existing theme scenes and the approved lesson structure remain.

| Pose | Intent checked visually before integration | Remaining qualified review |
| --- | --- | --- |
| Takbir | Both palms raised near shoulders/ears, fingers distinct, balanced standing feet | School variants and hand height |
| Folded standing | Right hand over left, upright body, gaze lowered | School-specific hand placement |
| Ruku | Near-horizontal back, hands on knees, feet grounded; rounded-back draft rejected | Joint/back/neck correctness |
| Standing after ruku | Upright recovery, arms at sides, same character | Sequence guidance |
| Sujud | Forehead/nose near ground, palms grounded, forearms raised, bent knees/shins, raised heels/toes | All contact points and toe direction |
| Sitting between prostrations | Kneeling seat, left foot beneath seat, upright right foot, hands on thighs | Iftirash depiction and variants |
| Tashahhud | Kneeling seat, right index raised, left palm on thigh | Finger timing/motion and final sitting variants |
| Salam | Same seated body, opposite head turns; right points viewer-left, left viewer-right | Turn extent and school variants |

Artwork is a teaching candidate, NOT scholarly certification. The left foot can be partially obscured in frontal seated views; the side-view sitting illustration shows more of the lower-leg relationship. Any disputed detail must be reviewed against a chosen instructional school before production.

## Asset packaging
Nine alpha-transparent WebP assets in `app/src/main/res/drawable-nodpi/learn_figure_*.webp`. Generated masters were inspected, including a cream-background alpha composite. Packaging crops the transparent border, fits within 800×1000 pixels, and encodes WebP at quality 90. Figures are never auto-mirrored in Urdu RTL; left/right salam use separate resources. Old `learn_pose_*.xml` resources are superseded and no longer used by `Figure`; the old vector generator must not overwrite the detailed raster set.

## Gallery mobile Home
At normal system text on phones at least 340dp wide, prayer name and clock share a baseline, with reduced hero height, 80dp minimum prayer rows, 24sp row labels, 32sp row clocks and 26dp navigation symbols. Ramadan progress and tarawih information are retained. Larger text and smaller widths retain the flowing layout rather than squeezing the hero into a row.

## Verification limits
The cloud acceptance suite renders actual Compose UI using native Robolectric graphics. It checks text overflow, reachable lesson content, navigation bounds, and the compact Gallery hero's separate name/time bounds. It captures all eight postures across all four themes and all four device/orientation targets, including top and cue scroll states. It does NOT assert anatomical validity or 99% fidelity.

Final raw screenshot review and run IDs are recorded in the follow-up evidence document after cloud validation. No new paid emulator matrix is requested. Physical-device accessibility, signed upgrade on the owner's device, qualified Urdu/Islamic sign-off and Play Console account/pre-launch acceptance remain external gates.

## Final layout corrections
The mat now spans scene x8..592, keeping wide sujud and paired salam inside its borders. Figure ground positioning compensates for cropped scene backgrounds rather than using a constant inset. At normal phone landscape text scale, the whole posture panel remains fixed while the words column scrolls independently; the previous shared scrolling could cut off the head/feet.

Normal compact Gallery phone Home no longer scales the entire page down. It keeps 48dp navigation targets, enlarged rows and a scrolling footer. The clock uses a fixed 120dp paragraph column, 44sp type and 56sp leading. EN/UR and four next-prayer states are checked separately; no overflow assertion was removed.

## Review boundaries
The supplied stock posture reference guides anatomy, while the newly generated adult figures remain a review candidate. They are not the earlier approved line-art mockup character. Owner approval of this new treatment and qualified review of seated-foot geometry/finger variants remain necessary. Urdu explanatory notes retain an explicitly labelled English fallback; complete Urdu notes translation is not claimed.

All 128 normal-scale posture top captures (8 poses x4 themes x4 targets) were screened in matrices, with individual raw inspection of all eight Gallery phone-landscape poses and selected phone/tablet/theme views. Screening sheets do not count as full-resolution approval of every capture. Final provenance and technical test counts are recorded in docs/DETAILED_SALAH_RELEASE_CHECKPOINT.md.
