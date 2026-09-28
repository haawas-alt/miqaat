# Miqaat · Islamic content review pack

**Content version:** 2026.09-a  **App build:** ≥ 1.34  **Status: UNSIGNED — no scholarly approval has been given for any version.**

The app says exactly that (Settings › About › Content sources: "Unsigned"). No release may describe its content as
"scholar reviewed", "verified" or "approved" until a named reviewer signs a version below, and the app text is
changed in the same commit that records the signature.

This pack has two parts:

1. **Texts** — every dua, dhikr, hadith and Learn-Salah step with Arabic, translation, source and grading.
   The full tables live in [REVIEW.md](REVIEW.md) (36 hadith · 24 adhkār · 12 salah steps · 2 duas). Nothing in
   this version changed the Arabic or the sources; four Learn-Salah *notes* were reworded (J7 below).
2. **Jurisprudential statements and calculation assumptions** — everything the app asserts that is a *ruling or
   a view*, not a text. Each item records the screen, the wording before this remediation, the wording now shipped,
   what the engine actually computes, and an approval field.

Engineering corrections (e.g. the Qibla north-reference fix, DST wall-clock arithmetic) are **not** in this pack:
they change nothing religious. Anything that *would* change a religious position is listed here and was **not**
changed silently — only the presentation was made more careful, always in the direction of stating the view and
naming that others exist.

---

## Part 2 · Statements requiring qualified review

Legend — **Engine:** what the code computes. **Was:** wording in build 33. **Now:** wording in this version.
**Approve:** ☐ approved as shipped · ☐ approved with change (write it) · ☐ reject.

### J1 · Default calculation convention and its description
- **Screens:** Setup › Check the times; Settings › Prayer times; Why-this-time dialog.
- **Engine:** Default `Method.MWL` (Fajr 18°, Isha 17°). Eleven conventions selectable. Default unchanged.
- **Was:** "Fajr 18°, Isha 17° · used by most Australian mosques" / "Most Australian mosques follow the Muslim World League convention".
- **Now:** "widely used in Europe, Australia and much of the world · confirm with your masjid" and "Conventions differ by country and community; the MWL angles are a common starting point — check your masjid's timetable and adjust, or import it."
- **Question for the reviewer:** is MWL an acceptable default for a global first run, given the confirm step now shows today's Fajr/Maghrib and asks the user to compare with the masjid?
- **Approve:** ☐ ☐ ☐  Reviewer notes: ________________________________

### J2 · Disliked (makrūh) times for voluntary prayer
- **Screens:** Home day bar (toggle "Show disliked times"); Why-this-time › Islamic guidance.
- **Engine:** Three windows: sunrise → sunrise + 15 min; true noon − 10 min → true noon; ʿAsr → sunset. Numbers unchanged.
- **Was:** presented as three timed windows with brief labels ("Sunrise · until the sun has risen a spear's length (~15 min)", "Zawāl · the sun at its zenith, just before Dhuhr", "After ʿAsr · until the sun has set").
- **Now:** heading "Disliked for voluntary prayer today · approximate"; each label states that the minutes are a *conservative estimate* of an event the texts describe, not a rule; the ʿAsr window notes "schools differ on whether this attaches to the time or to having prayed ʿAsr"; footer cites Ṣaḥīḥ Muslim 831 for the exemption of obligatory/missed prayers.
- **Questions:** (a) Are 15 and 10 minutes acceptable conservative approximations, or should the bar be removed entirely by default? (b) Should the post-ʿAsr window begin at the *time* of ʿAsr (as computed) or be described only as "after praying ʿAsr"? The engine cannot know when the user prayed.
- **Approve:** ☐ ☐ ☐  Notes: ________________________________

### J3 · End of Isha
- **Screens:** Prayer cards "ends …"; Why-this-time › Ends; Settings › Show end times.
- **Engine:** `PrayerEngine.midnight()` = midpoint of sunset and the next dawn (sharʿī midnight). Unchanged.
- **Was:** "Isha ends at sharʿī midnight" / "Sharʿī midnight; Isha stays valid until Fajr …, but praying before midnight is preferred".
- **Now:** "Shown at sharʿī midnight (halfway from sunset to dawn), the end of the preferred time in many views. Other scholars hold Isha valid until Fajr … Ask your imam." Settings row: "the time preferred by many scholars; others hold it valid until Fajr — ask your imam."
- **Approve:** ☐ ☐ ☐  Notes: ________________________________

### J4 · End of ʿAsr and Dhuhr
- **Engine:** ʿAsr ends at sunset; Dhuhr ends when ʿAsr begins (per the chosen ʿAsr view). Unchanged.
- **Was:** "At sunset; the preferred time ends when the sun yellows".
- **Now:** "At sunset. Many scholars call the time after the sun yellows the time of necessity; ʿAsr already prayed is not affected." Dhuhr: "When ʿAsr begins — which itself depends on the ʿAsr view chosen above."
- **Approve:** ☐ ☐ ☐  Notes: ________________________________

### J5 · Friday hour of acceptance
- **Screens:** Friday screen card; optional quiet reminder 60 min before Friday Maghrib.
- **Engine:** Reminder fires at Maghrib − 60 min on Fridays. Unchanged.
- **Was:** "The last hour before Maghrib on Friday, when duʿā is answered · Abū Dāwūd 1048 (ṣaḥīḥ), an-Nasāʾī 1389".
- **Now:** "An hour on Friday when duʿā is answered (Bukhārī 935). Shown here as the last hour before Maghrib — the view of many scholars, from Abū Dāwūd 1048 and an-Nasāʾī 1389; another well-known view places it between the imam sitting and the end of the prayer." Reminder text: "held by many scholars to be the hour…".
- **Approve:** ☐ ☐ ☐  Notes: ________________________________

### J6 · Traveller mode — qaṣr and jamʿ (REMOVED from the UI)
- **Was:** two toggles, "Shorten 4-rakʿah prayers (qaṣr)" and "Combining prayers (jamʿ)", described as annotating cards and playing one azaan per pair. **They did nothing** (audit blocker 3).
- **Now:** toggles removed. Traveller mode only shows a "Travelling · N km from home" chip; its description says: "Miqaat does not shorten or combine prayers for you — rulings on qaṣr and jamʿ depend on your journey and your school; ask your imam." The stored preferences are kept for compatibility but are never read.
- **Decision required before any re-introduction:** distance threshold (the 80 km chip trigger is a *display* heuristic, not a ruling), duration of stay, which pairs may be combined and when (jamʿ taqdīm/taʾkhīr), and per-school wording. Until a reviewer specifies this, the feature stays out.
- **Approve removal:** ☐  Notes: ________________________________

### J7 · Learn Salah (formerly "Learn to pray (children)")
- **Screens:** Learn screen; Settings › Display.
- **Engine:** 12 steps, Arabic/transliteration/meaning/note; one "hear it" TTS button. Arabic and sources unchanged.
- **Was:** framed for children; notes stated one form as *the* form ("Raise both hands to the ears, then fold them"; "Raise the index finger at the shahādah").
- **Now:** renamed "Learn Salah — for children and adult beginners. Shows one common form; some details differ between schools." Notes reworded where schools differ: hand height (shoulders or ears), hand placement (chest or below the navel), āmīn (aloud or quietly), the finger in tashahhud (how it is moved differs). Opening supplication described as "one of several reported".
- **Structure (v1.39):** the twelve texts are now arranged into complete prayers by `Learn.actions()`: Fajr (2), Maghrib (3), four-rakʿah. Per rakʿah: takbīr (opening duʿā and taʿawwudh in the first only) → al-Fātiḥah → a sūrah (first two rakʿahs only) → rukūʿ → rising → sujūd → sitting → second sujūd; tashahhud after the second rakʿah when more follow; final sitting = tashahhud + ṣalawāt + salām. Six posture drawings, each with a text description (`Learn.Posture.describe`). Please confirm this sequence and the posture descriptions, and whether a "pillars vs sunnah" marking should be added.
- **Approve:** ☐ ☐ ☐  Notes: ________________________________

### J8 · Hijri calendar and moon sighting
- **Engine:** java.time Umm al-Qura (HijrahChronology) + a user offset of −2…+2 days; "moon sighted / not sighted" buttons shift by one day on the 29th/30th. Ramaḍān mode and Eid features follow the offset.
- **Wording:** "Dates follow the Umm al-Qura calendar. If your local community's moon sighting differs, shift by a day." Unchanged.
- **Approve:** ☐ ☐ ☐  Notes: ________________________________

### J9 · Jumuʿah replaces Dhuhr on Fridays
- **Engine:** when enabled, the Friday Dhuhr slot shows the user's Jumuʿah time and the azaan fires at it; the calculated Dhuhr is still shown in Why-this-time.
- **Question:** should the calculated Dhuhr remain visible on the card for those who pray Dhuhr (women, travellers, the ill)?
- **Approve:** ☐ ☐ ☐  Notes: ________________________________

### J10 · After-azaan sequence and narration
- **Engine:** azaan → dua after azaan (Bukhārī 614) → one hadith (3 min, Arabic + English recordings) → done; iftar dua at Maghrib in Ramaḍān.
- **Question:** any objection to English narration of hadith at alarm volume in a home; any hadith in REVIEW.md whose translation should change.
- **Approve:** ☐ ☐ ☐  Notes: ________________________________

### J11 · Iqamah "never before azaan" guard
- **Engine:** a fixed iqamah time that falls before that day's azaan is replaced by azaan + 5 min. Purely protective; no ruling implied.
- **Approve:** ☐ ☐ ☐

---

## Sign-off

| Field | |
|---|---|
| Content version reviewed | 2026.09-a |
| Reviewer name and qualification | |
| School(s) represented | |
| Date | |
| Items approved as shipped | |
| Items approved with change (attach wording) | |
| Items rejected | |
| Signature | |

Once signed: copy this table into `CHANGELOG`-style history below, bump the content version, change
Settings › About › Content sources from "Unsigned" to "Reviewed by … on …", and open the correction channel
(Settings › About › Report a content correction → GitHub issues) to that reviewer.

## History
- 2026.09-a — first structured pack; wording changes J1–J7 made in the direction of *more* caution; no Arabic text, source or calculation changed. Unsigned.
