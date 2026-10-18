# Miqaat audit implementation and verification report

Prepared for Usman Rana on 18 October 2026. Baseline release gallery-tablet-1.163-25. Candidate 1.164-audit.

The confirmed code and copy changes are implemented, backed up and available in draft PR 4. Cloud regression checks and signed candidate preparation have completed. Production Play Store release remains NO GO: qualified content approval, physical-device acceptance and account-specific Play Console checks are still open. Internal exploratory testing is a conditional GO using the identified candidate and the limitations below.

This is a remediation verification report, supplementing the independent baseline audit. It does not certify a complete new device matrix or 9.5/10 visual fidelity. The baseline audit score of 6.6/10 is not automatically replaced by a higher score because CI passed.

Baseline category scores were functional confidence 7.0/10, responsive quality 7.2/10, visual fidelity 7.6/10, accessibility readiness 5.5/10 and release readiness 5.5/10. A new numerical score is withheld until the complete acceptance evidence exists.


## Priorities one to eight

| No | Implemented work | Verification and remaining gate |
| --- | --- | --- |
| 1 | Countdown and Settings accessibility layout | Targeted EN/UR layout and interaction tests pass; actual device accessibility remains open. |
| 2 | Later rakah basmala cue | All three prayer sequences preserve action counts and Fatiha audio lookup; exact content approval open. |
| 3 | Religious narration notes | Authentic wording distinguished from weak count reports; qualified approval open. |
| 4 | Urdu and source corrections | Published Iftar translation attributed; full bilingual and scholarly sign-off open. |
| 5 | Adhkar portrait reading, contextual doors and compass copy | Raw targeted captures and semantic/callback checks reviewed; physical sensor and full route matrix open. |
| 6 | Regression assertions and evidence integrity | Passing tests, unique XML paths and retry diagnostics; golden mockup and real accessibility acceptance open. |
| 7 | Physical reliability and upgrade acceptance plan | Prepared only. No physical device session was available; not marked passed. |
| 8 | Signed APKs and Play AAB, policy and store preparation | Binary checks pass; upload-key correspondence, Console declarations and internal-track installation open. |


## Backup and exact source identity

Verified rollback branch: backup/gallery-1.163-25-before-audit-fixes-20261018. It points to baseline a5028c6424ab51f2186fc108ee71dfbe5ac29959. Implementation branch: fix/audit-1.163-25-remediation. Draft PR: https://github.com/haawas-alt/miqaat/pull/4.

Tested and built application commit: 49427f05655e844bf501dc1c9c8233bbe14ee029. Workflow run: 39107606299. Later evidence packaging and documentation commits do not represent another design build. Artifact PROVENANCE and SHA256SUMS identify the actual binary.


## Confirmed findings and implementation

The severity below is release triage for this handover. Implemented means the identified change exists; it does not imply every external acceptance condition has passed. No P0 was established by these targeted checks.


### F01 P1 Iqamah countdown visibility

A number-first layout is used in short or enlarged landscape screens. Start and Dismiss are placed alongside the number, before optional guidance; a fixed footer no longer obscures the timer. Tests assert the complete number and unit bounds and execute both callbacks across 48 configurations. Raw review additionally found a normal-tablet phase label splitting inside COUNTDOWN; its width now accommodates whole words, with an explicit wrapping assertion. Service timing, system insets and actual TalkBack use remain open.

Implementation evidence: app/src/main/java/com/usman/miqaat/ui/AzaanScreen.kt.


### F02 P1 Settings label width

SettingRow accounts for font scale; the Try it now theme row explicitly stacks label and description above the options. Twelve EN/UR landscape configurations assert readable label bounds and actual option selection. This is a component fixture, not proof that every Settings route or Show CTA service interaction works on a device.

Implementation evidence: app/src/main/java/com/usman/miqaat/ui/SettingsScreen.kt.


### F03 P1 Later rakah basmala

Later Fatiha actions now show a separate localized basmala cue, its Arabic text and an honest note that Listen plays Fatiha only. Sequence tests retain 21, 29 and 36 actions and existing saved indices, audio index and Arabic lookup. No replacement religious audio was fabricated. School-specific wording and actual recording correspondence need qualified approval.

Implementation evidence: app/src/main/java/com/usman/miqaat/data/Learn.kt and ui/LearnScreen.kt.


### F04 P1 Ruku and sujud attribution

EN/UR notes distinguish the authentic wording in Muslim 772 from the weak or discontinuous chains attached to three repetitions in Abu Dawud 886 and Tirmidhi 261. The text presents repetition as a scholarly recommendation, not a condition invalidating prayer. This is a sourced correction awaiting qualified review.

Implementation evidence: app/src/main/java/com/usman/miqaat/data/Adhkar.kt and data/UrduContent.kt.


### F05 P1 Play release artifact gap

CI now builds and verifies both signed release APKs and a Play AAB from the exact tested source. This closes the missing build-output gap. It does not establish that the key is registered in Play Console, that generated Play splits install, or that production declarations are accepted.

Implementation evidence: .github/workflows/audit-remediation.yml and tools/check_release_integrity.py.


### F11 P1 Physical acceptance evidence

An explicit checklist requires device, OS, exact artifact hash, steps and actual outcome. No cloud screenshot or JVM callback test is substituted for Doze, boot, real audio, signed upgrade preservation or assistive technology acceptance. This finding remains open.

Implementation evidence: docs/AUDIT_REMEDIATION_20261018.md.


### F06 P2 Urdu contextual doors

Doors have a semantic ADHKAR kind. The visible pair prioritizes one warning and the Adhkar action independently of English or Urdu label spelling; callback fixtures cover competing warnings and short or empty lists. A new full Home device matrix has not been performed.

Implementation evidence: app/src/main/java/com/usman/miqaat/ui/GalleryHome.kt and ui/TabletHome.kt.


### F07 P2 Unsupported compass error claims

Numerical accuracy promises such as plus or minus five degrees are replaced by qualitative calibration guidance in EN/UR. Real magnetic interference, absent sensors and calibration behavior remain device checks.

Implementation evidence: app/src/main/res/values/strings_ui.xml and values-ur/strings_ui.xml.


### F08 P2 Portrait tablet Adhkar reading

Portrait tablets use a wider bounded reading column, larger Arabic and linear navigation. EN/UR 100% and 200% long-entry fixtures assert minimum Arabic size and no horizontal overflow. Portrait tablet counters now expand with text size; a separate tahlil fixture requires the count 100 to remain visible on one line with every glyph fitting, allowing one physical pixel for text rounding. The old sidebar is absent in portrait; direct list jumping is not preserved in that composition. Some normal-size vertical whitespace remains. Full collection and usability acceptance remain open.

Implementation evidence: app/src/main/java/com/usman/miqaat/ui/AdhkarScreen.kt.


### F09 P2 Urdu wording and Iftar source

Reliability, clock return, location refresh and geocoder copy are corrected; About uses localized runtime values. Iftar uses the attributed published Urdu wording for Abu Dawud 2357 rather than an unapproved invented translation. These targeted fixes do not certify the complete Urdu corpus.

Implementation evidence: app/src/main/res/values-ur/strings_ui.xml and data/Adhkar.kt.


### F10 P2 Tests that only capture images

New tests assert visibility, bounds, text wrapping, selection, callbacks, sequence invariants and audio identity. Screenshot retries record causes and retry only ComposeNotIdleException; XML publication preserves distinct paths. Existing image captures still are not pixel-fidelity or pedagogical posture assertions.

Implementation evidence: app/src/test/java/com/usman/miqaat/ui/AuditRemediationTest.kt, GalleryDoorPriorityTest.kt, data/LearnSequenceTest.kt and androidTest/ui/ShotSupport.kt.


### F12 P2 Store and privacy consistency

Descriptions list four themes; Urdu listing grammar is corrected; geocoder wording no longer contradicts possible provider network use. Existing 13+ policy is preserved without an unsupported audience reclassification. The accepted policy must be deployed and actual audience, SDK Data Safety, permissions and rights must be reconciled in Console.

Implementation evidence: docs/privacy.html, play/listing_en.txt and play/listing_ur_DRAFT.txt.


### F14 P2 Asr explanation

Both short Hanafi Asr labels now state that twice the object height is additional to the noon shadow. Exact bilingual fiqh wording remains in the qualified review queue.

Implementation evidence: app/src/main/res/values/strings_ui.xml and values-ur/strings_ui.xml.


### F13 P3 Distribution privacy label

The edition description uses the distribution flavor rather than debug updater availability. Play and direct-download copy are distinct. Source and release manifests support this change; no fresh complete About-screen device matrix is claimed.

Implementation evidence: app/src/main/java/com/usman/miqaat/ui/SettingsScreen.kt.


## Cloud verification results

Final run: https://github.com/haawas-alt/miqaat/actions/runs/39107606299. Both release flavors run the seven affected test methods after the phase-label and three digit counter refinements: 14 test executions passed, zero failures or skips. The earlier exact-source run 39093985409 passed 7 GitHub methods and 128 Play methods with one opt-in screenshot skip; it included the full Play unit suite. These are test executions, not 135 distinct behaviors or 135 devices.

The targeted UI tests use Robolectric SDK 34 with native graphics, not a running Android emulator or a physical device. Instrumentation-test compilation does not execute those tests. No fresh paid emulator matrix was started for this remediation.

The sole legacy skip is ScreenshotTest.render, gated by MIQAAT_SCREENSHOTS=1. It is reasonable for the ordinary unit gate to skip that opt-in broad capture job, but it leaves the full capture matrix unexecuted for this candidate. The new targeted captures run without that flag. Earlier failed capture attempts used unsupported PixelCopy on Robolectric; the harness was corrected to draw the actual composed view before the successful run.

Lint and compilation: release lint for both distributions and GitHub debug instrumentation-test Kotlin compilation pass. Lint reports 98 warnings and 6 hints per distribution, with no errors. They include 20 unused resources, 13 vector raster warnings, 12 discouraged API warnings and 7 plural candidates. A passing lint task does not mean zero warnings or complete localization quality.

Static localization checks find the same 683 English and Urdu resource keys, no duplicate string names and no format-placeholder mismatches. All 91 bundled MP3/WAV audio streams decode successfully. The Fajr MP3 emits an embedded-cover metadata warning because JPEG bytes are tagged as PNG; no audio stream failure or Android playback failure is established. Decoding does not verify pronunciation, meaning, recording identity or actual device playback.

| Evidence | Configuration coverage | Boundary |
| --- | --- | --- |
| 48 countdown captures | All four themes; phone 740 by 360 dp and tablet 1280 by 800 dp landscape; EN/UR; 100/130/200% | Bounds and callback tests; not actual service, insets or orientation change. |
| 12 Settings captures | Phone/tablet landscape; EN/UR; 100/130/200%; all four choices in each fixture | Isolated row; theme names are brand labels; no full page or service CTA acceptance. |
| 8 Adhkar captures | Prayer Gallery tablet portrait; EN/UR; 100/200%; long first entry and three digit counter | No fresh 130% or other-theme portrait Adhkar captures in this targeted set. |
| 2 basmala captures | Prayer Gallery phone portrait; EN/UR; 100%; later Fatiha | Full lesson sequence validated in data; only this route captured here. |

Both release flavors execute assertions, but the raw capture names are reused by the second Play execution; separate flavor PNG sets are not retained. This does not establish flavor-specific visual coverage for every page.

The 70 targeted PNGs have dimensions 1480 by 720 (24), 2560 by 1600 (30), 1782 by 822 (6), 1600 by 2560 (8), and 822 by 1782 (2). Contact boards were used for coverage screening, with raw captures opened for the critical enlarged text and content cases. Transparent backgrounds in isolated Settings PNGs are a fixture property; the raw row is not a full screen and must not be judged as a black production background.

Visual conclusions: the targeted off-screen countdown, squeezed label and phase-word wrapping defects are addressed. Portrait Adhkar is more readable, but some generous empty space remains. Enlarged countdown uses a simplified accessibility composition. No locked approved-mockup overlay, complete new phone/tablet page audit, posture-by-posture re-certification, contrast measurement or 9.5/10 fidelity claim is established by this run.


## Signed candidate verification

Test candidate APK: https://github.com/haawas-alt/miqaat/releases/download/audit-remediation-39107606299/Miqaat-1.164-audit.apk. This prerelease supports exploratory testing and does not replace the stable updater release.

Version 1.164-audit; version code 1792308350; package com.usman.miqaat; minimum SDK 26; target SDK 36. Version code exceeds the audited direct-download baseline 1792284867, but every active Play track still needs checking. Both APKs use the expected RSA 4096-bit release certificate and verify using APK signature scheme v2.

Certificate SHA256: 6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01. Matching the direct-download certificate is useful for upgrade compatibility; it is not proof that an installed debug build or a Play-signed build has the same identity.


### Miqaat-1.164-audit.apk

43894866 bytes. SHA256 d12f2b46bfeed18e57fb2e316c973180fec843c1a6af670653ea3f3e70b2a05b.


### Miqaat-1.164-play.apk

43875238 bytes. SHA256 b44162174bbfb05f0985054bf2507ea0ac4e9e714cd8457ea4bb121f241d0df1.


### Miqaat-1.164-play.aab

44201376 bytes. SHA256 1b9dcb4b2e1840149f7b57afa8c89c969351739e84d59f9eb08f43018d494e94.

Independent local hashes match the published SHA256SUMS. APK native library ELF load and ZIP alignment checks pass for all four ABIs. The forbidden asset scan passes. The Play manifest excludes REQUEST_INSTALL_PACKAGES; the GitHub flavor retains its updater permission. Static 16 KB alignment is not a successful runtime test on a 16 KB device.

The AAB passes JAR signature verification and official bundletool 1.18.2 validation. Jarsigner also reports a self-signed certificate, absent timestamp and ZIP stream-order warnings. These are retained in the evidence rather than suppressed; bundletool validation succeeds. Play upload, generated split installation and registered upload-certificate correspondence remain unverified.


## Content review and approval requirements

Source checks and language corrections were performed as implementation work. They are not a Quran hafiz or Sunni scholar certification. The handoff includes every one of the 216 files under app/src/main, with hashes covering inline text, resources, fonts, audio and artwork. The manifest is marked REVIEW_PENDING_NOT_SCHOLAR_APPROVED and identifies the tested source.

Qualified reviewer scope: verify Arabic letters and diacritics against authoritative editions; EN/UR meaning, spelling and grammar; all sources and grades; the basmala instruction with school differences; ruku/sujud count notes; every Learn Salah posture, foot placement, hand placement, raised finger and salam turn; and exact correspondence of all recordings. Each approval must name the reviewer, qualifications, date, content version, exceptions and approved hashes.

The historical owner-reported partial scholar review does not cover every new Urdu wording or new artwork. Automated transcription of the Fatiha recording supports the distinction between the separate basmala cue and Fatiha audio, but it is not a qualified listening approval. No religious sign-off has been invented or backdated.

Source handoff: docs/RELIGIOUS_SOURCE_CHANGES_20261018.md. Iftar Urdu reference: https://mohaddis.com/View/Abu-Daud/T2/2357. Prayer sequence and narration references are recorded beside the changed content. The remaining full-corpus rights and attribution review is part of release acceptance.


## Remaining acceptance in execution order

| Gate | Required outcome | Owner or environment |
| --- | --- | --- |
| Qualified content approval | Named Arabic, Urdu and fiqh reviewer approves exact text, audio and posture hashes or returns corrections. | Qualified human reviewer |
| Complete device matrix | Phone/tablet, both orientations, EN/UR 100/130/200; every route, empty/error/progress/selected state; normal Home fits without scrolling. | Physical device tester |
| Accessibility | TalkBack order and labels, focus, Switch Access, magnification, measured contrast, targets and cutouts. | Android devices with assistive services |
| Reliability and upgrade | Signed update preserves settings/progress; real audio, interruptions, Bluetooth, Doze, reboot, permission denial, DST/timezone and OEM battery behavior pass. | Phone and tablet with exact candidate |
| Preview and quiet flow | Try it now Show CTA; current/next Azaan; countdown, Iqamah, quiet, stop/skip and rollover verified end to end. | Instrumented or physical runtime |
| Store validation | Upload certificate and all active version codes checked; internal-track install; pre-launch and 16 KB runtime triaged. | Owner Play Console access |
| Declarations and publication | Audience, Data Safety, content rating, FGS/full-screen/exact alarm forms and rights complete; reviewed privacy URL deployed publicly. | Owner Play Console and policy deployment |
| Visual acceptance | Locked approved mockup references compared to full raw captures at matching geometry; no material P0/P1; agreed residual P2/P3 documented. | Designer and UAT reviewer |

Production remains NO GO until these gates close. Internal exploratory testing is appropriate for finding residual issues, with the exact candidate identity and known limitations attached. No production release, main-branch merge, updater latest-tag change or Play submission has been performed. A certified 9.5 or 9.9 score cannot be inferred from the evidence currently available.


## Evidence locations

Draft implementation PR: https://github.com/haawas-alt/miqaat/pull/4. Final signed build: https://github.com/haawas-alt/miqaat/actions/runs/39107606299. Downloadable evidence package: https://github.com/haawas-alt/miqaat/actions/runs/39107606299/artifacts/12129421502. GitHub may require sign-in to download Actions artifacts; retention is 30 days.

Within the evidence archive, candidate/ contains the APKs, AAB, SHA256SUMS, PROVENANCE, signature results, manifests and native integrity reports. app/build/test-results/ retains each distribution path. app/build/audit-screens/remediation/ holds the 70 raw regression captures. lint XML and build logs are preserved.

Critical raw names include countdown-PHONE_LANDSCAPE-PRAYER_GALLERY-true-2.0.png; countdown-PHONE_LANDSCAPE-CELESTIAL_MERIDIAN-false-2.0.png; countdown-TABLET_LANDSCAPE-PRAYER_GALLERY-false-1.0.png; settings-PHONE_LANDSCAPE-true-2.0.png; adhkar-tablet-portrait-true-2.0.png; and basmala-true.png. Filenames with true indicate Urdu; false indicates English.

Repository handoff files: docs/AUDIT_REMEDIATION_20261018.md, docs/AUDIT_VERIFICATION_20261018.md, docs/RELIGIOUS_SOURCE_CHANGES_20261018.md, docs/audit-content-manifest.json and play/AUDIT_RELEASE_GATES.md. Documentation-only commits after the tested source do not change the binary or constitute another design build.

