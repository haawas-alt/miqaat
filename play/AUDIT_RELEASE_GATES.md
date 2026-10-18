# Play release gates for the audit remediation candidate

Do not submit or promote solely because CI is green. Console access and physical-device acceptance are not available in this cloud checkout.

## Artifact identity
- Use Miqaat-1.164-play.aab from the successful exact remediation workflow. Never upload the GitHub APK or GitHub updater flavor.
- Record SHA-256, exact source commit, version code and CI run. Confirm version code exceeds every active Console track.
- Compare the existing release-key fingerprint with the Console upload certificate. A matching direct-download certificate does not establish that it is the registered Play upload key.
- Preserve Play App Signing identity and test an update delivered by the internal track.

## Declarations needing owner verification
- Core purpose: prayer-time alarms and reminders. Check actual USE_EXACT_ALARM eligibility against current policy; do not claim an approved declaration without Console evidence.
- Full-screen intent and mediaPlayback foreground service: describe the user-triggered/permission-based behavior and provide the requested video. Test denial/fallback.
- Target audience: existing policy specifies 13+. Descriptive copy no longer explicitly targets children. Owner must confirm the actual audience; changing copy alone does not determine Families obligations.
- Data Safety: no developer-operated accounts, analytics or advertising in inspected source. Android geocoder may transmit location/search to its provider. Reconcile Google's current collection/sharing definitions and every included SDK; do not automatically answer No data collected based only on absence of a developer server.
- Privacy URL: publish reviewed privacy.html from the accepted branch and verify public reachability without login before entering it in Console.
- Content rating, permissions declaration, store contact details, screenshot accuracy and content/artwork/audio rights: retain actual completed Console responses.
- Personal developer account closed-testing requirements are conditional on account creation date and type. No tester count/duration is claimed by CI.

## Runtime evidence
- API 36 physical or approved virtual device, plus a 16 KB runtime, running the exact candidate. Static native alignment is necessary but not sufficient.
- Phone/tablet orientation, EN/UR 100/130/200, TalkBack/focus/targets/contrast, install/upgrade preservation, alarms in Doze/boot, audio interruptions and sensor behavior.
- Play pre-launch report triaged; no unresolved P0/P1.
- Qualified religious/Urdu content approval tied to docs/audit-content-manifest.json; outstanding content cannot be labeled scholar-approved.

## Release decision
Internal exploratory testing only until these gates close. No Play production publication, main-branch merge, or updater/latest-tag change is performed by the remediation workflow.
