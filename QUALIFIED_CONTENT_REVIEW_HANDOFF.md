# Content review handoff — rebuilt candidate 3903c60b5ffcab8645fcb176282c962cfda71bc6

Status: **not qualified scholarly approval**. This is source triage and a review queue.
A named qualified Sunni reviewer and fluent Urdu proofreader must review the actual candidate text
and posture images, record corrections, and sign off the content revision.
Earlier AI-assisted documents and owner-reported partial reviews do not certify the new Urdu wording.

Files: app/src/main/java/com/usman/miqaat/data/Adhkar.kt, UrduContent.kt, Learn.kt, DailyDhikrProgress.kt, values/learn_rebuild.xml, values-ur/learn_rebuild.xml and app/src/main/res/drawable/learn_pose_*.xml. All posture drawings are independently rebuilt; previous approvals do not cover them. Arabic recitation strings were preserved, but source attribution, Urdu guidance, summary labels and counter behavior changed.

The tashahhud index is now on the worshipper's anatomical right (viewer-left). Salam has separate right and left head-turn drawings, not an RTL-mirrored single view. Confirm those views, limb/contact geometry and valid school variations against the exact candidate.

## Supersession of the findings below

The original comparison table is retained as the reason for remediation, not a description of unfixed candidate behavior. In this candidate:
- bika_e cites Ibn Majah 3868 for its evening return wording.
- radeetu cites Hisn al-Muslim 87 for three repetitions, with related Abu Dawud/Tirmidhi grades separately attributed.
- asbahna/amsayna are explicitly marked excerpts; abbreviated meanings are labelled summaries.
- tahlil persists one shared civil-date total across morning/evening, reopening and a 100 cap.
- pp_ikhlas Urdu title/guidance identifies all three Quls.

These are implemented corrections requiring qualified approval of the exact adopted wording, references and illustrations. No external reviewer has signed this candidate.

## Confirmed comparisons and product issues

| Item | Evidence | Finding | Reviewer action |
|---|---|---|---|
| bika / bika_e | [Abu Dawud5068](https://sunnah.com/abudawud:5068), [Ibn Majah3868](https://sunnah.com/ibnmajah:3868), retrieved2Oct2026 | Abu Dawud's displayed edition uses a resurrection ending for its evening text; the candidate evening text ends with return and corresponds to Ibn Majah3868 | Identify and cite the exact adopted evening variant; review the Tirmidhi3391 variant separately |
| radeetu | [Abu Dawud5072](https://sunnah.com/abudawud:5072), retrieved2Oct2026 | This displayed text does not state three repetitions and displays Al-Albani's weak grade. Candidate source copy says three is in the narration and combines grades across references | Cite the specific three-times narration and attribute each grading to its exact chain/version; do not silently remove a legitimate variant |
| asbahna / amsayna | [Muslim2723b](https://sunnah.com/muslim:2723b), retrieved2Oct2026 | The candidate omits the source's concluding petitions | Decide full text versus clearly labelled excerpt; proofread matching English and Urdu |
| tahlil100 | Candidate source explicitly says100/day; UI progress is session-local | Opening morning and evening gives separate sessions rather than a shared daily total | Decide daily progress/count model and explain it accurately; do not present200/day as the cited prescription |
| pp_ikhlas Urdu | Arabic combines Ikhlas, Falaq and Nas; Urdu title/meaning only identify Ikhlas and provide its excerpt | Confirmed coverage mismatch | Use a title covering all three and matched full meanings or an explicitly labelled summary approved by the Urdu reviewer |
| kursi / pp_kursi and lesson meanings | Full Arabic paired with shortened English/Urdu text | Excerpts/summary meanings need explicit presentation | Approve full translations or label excerpts/meaning summaries accurately |
| salawat10 | App explicitly labels10 as a suggested count | Prescribed versus suggested distinction is present | Confirm wording remains clear in Urdu and all views |

These are textual/product findings, not a ruling on narration validity.

## Complete dhikr queue

“Source in app” below is a claim requiring verification where not explicitly compared above.
Every row requires Arabic diacritics, translation, count, timing, grading and Urdu approval.

| ID | Count in app | Source in app / required focus |
|---|---:|---|
| kursi |1| Qur'an2:255; Nasai al-Kubra10729/Hakim1:562/Targhib662; full verse versus excerpt meaning |
| ikhlas |3| Qur'an112; Abu Dawud5082/Tirmidhi3575; morning/evening count and exact grade attribution |
| falaq |3| Qur'an113; same morning/evening narration |
| nas |3| Qur'an114; same morning/evening narration |
| asbahna |1| Muslim2723; omission/excerpt label and matching Urdu |
| amsayna |1| Muslim2723; omission/excerpt label and matching Urdu |
| sayyid |1| Bukhari6306; complete Arabic and Urdu |
| bika |1| Abu Dawud5068/Tirmidhi3391; distinguish variants |
| bika_e |1| Exact evening text aligns with Ibn Majah3868; correct edition-specific citation |
| bismillah |3| Abu Dawud5088/Tirmidhi3388; exact wording/count |
| radeetu |3| Identify the exact three-times narration and separate its grading |
| afini |3| Abu Dawud5090; excerpt status and count |
| audhu |3| Muslim2709/Tirmidhi3604; evening-only count and exact variant |
| tahlil |100| Bukhari3293/Muslim2691; daily versus session tracking |
| tasbih |100| Muslim2692; morning/evening timing and count |
| salawat |10| Muslim408 supports reward;10 is labelled app suggestion |
| pp_istighfar |3| Muslim591 |
| pp_salam |1| Muslim591 |
| pp_tahlil |1| Bukhari844/Muslim593 |
| pp_tasbih |33| Muslim596; keep the selected combined count variant consistent |
| pp_hamd |33| Muslim596 |
| pp_takbir |34| Muslim596;33/33/34 variant |
| pp_kursi |1| Qur'an2:255/Nasai al-Kubra9848/Jami6464; excerpt meaning |
| pp_ikhlas |1| Abu Dawud1523/Tirmidhi2903 and morning/evening5082; distinguish post-prayer from time-of-day evidence; Urdu mismatch |

## Eight-posture teaching review

| Posture | Required visual and pedagogical review |
|---|---|
| Takbir | Both hands raised; correct palm direction/height; shoulders/ears variations explained |
| Folded standing | Hands visibly folded, appropriate side/order; selected placement does not imply other schools invalid |
| Ruku | Hands on knees, readable back/head alignment; physical movement differentiated from sujud |
| Standing after ruku | Upright torso, visibly different arm position from folded standing; cue/recitation appropriate to context |
| Sujud | Forehead/nose, hands, knees and toes shown clearly; avoid ambiguous floating/unsupported contact |
| Sitting between prostrations | Clear seated posture and brief-pause context; feet/body position approved |
| Tashahhud | Seated posture with unmistakable right-hand finger gesture; explain school differences in movement/timing |
| Salam | Visible head turn and direction; distinguish right/left steps, not merely a caption change |

Illustration review must include the approved mockups when recovered. Generic silhouette distinctions
do not constitute pedagogical approval.

## Sign-off record required

- Named reviewer, relevant qualification, review date and language competency.
- Exact source commit/content version and hashes of reviewed artwork.
- Per-item corrections, adopted narration/translation edition and variant/grade rationale.
- Approval of all24 dhikr entries,12 lesson text groups and8 posture illustrations.
- Explicit exclusions and any further reviewer required.
- Written approval retained with the release evidence; do not convert a partial review into complete approval.
