# Miqaat — Google Play submission pack

Everything you paste into Play Console. Store text is in `play/listing_en.txt` (final) and `play/listing_ur_DRAFT.txt` (needs Urdu proofreading). Images are in `art/play/`.

## 0. Before you start (5 minutes)
1. GitHub → repo **haawas-alt/miqaat** → Settings → Pages → Source "Deploy from a branch", Branch `main`, folder `/docs` → Save.
2. After ~1 minute open https://haawas-alt.github.io/miqaat/privacy.html — this is your **Privacy policy URL**.

## 1. Create the developer account
- play.google.com/console → sign up, **Personal** account, $25 one-time fee, identity verification (you do this yourself).
- Personal accounts created after 13 Nov 2023 must run a **closed test with at least 12 testers for 14 continuous days** before Production is unlocked.

## 2. Create the app
Name: `Miqaat: Prayer Times & Azaan` · Default language English (United Kingdom or US) · App · Free · accept declarations.

## 3. Store listing
- Short description, full description: from `play/listing_en.txt`.
- App icon: `art/play/play-icon-512.png` · Feature graphic: `art/play/feature-graphic-1024x500.png`
- Screenshots: at least 2 phone + tablet 7"/10" recommended (see `art/play/screens/` when present).
- Category: **Lifestyle** (or Books & Reference). Tags: prayer, Islam.
- Contact: website https://github.com/haawas-alt/miqaat/issues (do not publish your email if you'd rather not; Play requires an email in the console but it can be different from your personal one — use a dedicated address).

## 4. App content (Policy → App content)
| Item | Answer |
|---|---|
| Privacy policy | https://haawas-alt.github.io/miqaat/privacy.html |
| Ads | No ads |
| App access | All functionality available without login |
| Target audience | 13 and over only. Not designed for children. |
| Content rating (IARC) | Category: Reference/Utility. Violence, sex, language, drugs, gambling: No. User-generated content: No. Location shared with others: No. Expect rating 3+/Everyone. Religious content is not a rating trigger. |
| News app | No |
| Government app | No |
| Financial features | None |
| Health features | None |
| EU DSA trader | Non-trader (free, non-commercial hobby app) — choose honestly if you later monetise |

### Data safety
- Does the app collect or share user data? **Yes (collect), No (share)**
- Data type: **Location → Approximate location and Precise location**
  - Collected: yes · Shared: no · Processed ephemerally: yes
  - Purpose: App functionality
  - Optional: yes (user can enter a city manually)
- No other data types. No accounts, analytics, ads, crash reporting.
- Encrypted in transit: yes (only the optional place-name lookup uses the network).
- Deletion request: not applicable, nothing stored on our servers.
- Note: the Android geocoder may send coordinates/search text to Google; this is why location is declared conservatively.

## 5. Declarations for sensitive permissions
### Exact alarms (USE_EXACT_ALARM)
Use case: **Alarm clock**. Text: "Miqaat is an azaan (call to prayer) alarm app. Users set five daily prayer alarms whose whole function is to sound at the precise minute the prayer begins. Alarms that fire minutes late defeat the purpose. The app has an in-app Reliability screen that reports any late alarm."
Fallback if rejected: switch to SCHEDULE_EXACT_ALARM and guide the user to the "Alarms & reminders" toggle. Tell me and I will change it.

### Full-screen intent (USE_FULL_SCREEN_INTENT)
Text: "When an azaan alarm fires with the screen off or locked, the app shows a full-screen alarm screen with a clear Stop button, like a clock app's alarm."

### Foreground service — mediaPlayback
Text: "The azaan plays audio for up to ~4 minutes while the screen may be off. A foreground media service keeps playback from being killed. The notification has a Stop action."
Video (30–60 s, screen recording, upload to YouTube unlisted): set an azaan for one minute ahead → lock the phone → azaan sounds with the notification and full-screen Stop → press Stop. **You must record this on a real phone.**

## 6. Release
1. Testing → **Closed testing** → create track → upload the signed **.aab** (from the CI release; Play App Signing will re-sign, keep your key safe) → Testers: an email list or Google Group.
2. Release notes (first release):
   "First public test. Prayer times, azaan alarms, Qibla, Hijri calendar, Learn Ṣalāh, English and Urdu. Content is not yet fully scholar-reviewed — see Settings › About › Content sources."
3. Recruit **12+ testers**, ask them to opt in through the test link and keep the app installed 14 days.

### Tester message (paste to WhatsApp/family)
"Assalamu alaikum, I'm testing my azaan app before it goes on Google Play. Please (1) send me your Gmail address, (2) open the link I send and tap 'Become a tester', (3) install from Play, (4) keep it installed for 14 days and open it now and then. JazakAllahu khayran."

## 7. After 14 days: apply for Production
Play asks 3 parts. Draft answers:
1. **How did you recruit testers?** "Family, friends and local masjid community, invited by message."
2. **What feedback did you receive and how did you use it?** (fill in truthfully from actual feedback)
3. **Why is the app ready for production?** "Prayer-time calculation is tested against published reference tables; the app has been used daily on tablets and phones; a reliability screen tracks late alarms; content sources are shown in-app; scholarly review is in progress and disclosed."

## 8. Honest state at submission
- Content review: partial (hadith after azaan and Learn Ṣalāh reported reviewed by Sheikh Abdel Razek Mahmoud Ramadan; signed copy pending). About still says **Unsigned** until that arrives.
- Play edition cannot self-update (Play handles updates); the About crash was fixed by guarding the install-permission API.
