# Gallery audit reconciliation and acceptance gate

Baseline: 6ab3a6f139a346ad5a976e2669cc3565d26e1867 (Claude candidate 1.162-13).
Backup: backup/gallery-before-audit-remediation-6ab3a6f.

The independent audit evaluated 1.162-12; Claude's attached audit evaluates 1.162-13. The latter adds captures, not fixes to the display logic.

| Claude finding | Independent audit coverage | Resolution / verification required |
| --- | --- | --- |
| F1 simultaneous NOW/NEXT missing | G01 | Independent Gallery current-window and next-event markers; model regression test |
| F2 overnight next Fajr absent | G01; overnight evidence previously unavailable | Tomorrow's timetable feeds the Fajr card, end time and spoken description; never marked completed; explicit regression test |
| F3 large text uses accessible list | Accessibility coverage gap | Retain intentional reflow above 115%; capture 130% and 200% in both languages and orientations; do not shrink text into the grid |
| F4 alarm warning in captures | Physical-device / alarm reliability unverified | Preserve actual readiness warning. Robolectric cannot certify device alarm permissions or delivery |
| F5 Urdu Hijri era absent | G12 | Append Urdu era marker ھ; preserve full month and year |
| F6 Hijri user setting hides date | Settings preservation requirement | Respect explicit user setting; show both dates when enabled; do not silently override preference |
| F7 explicit information control absent | G06 | Restore separate 48dp information control on each landscape card; dialog interaction test |

Additional independent findings addressed in source: explicit variable-font weights and stronger time hierarchy (G02), Ramadan context restored (G03), layout-overflow checks plus retained raw artifacts (G04), evergreen NOW / terracotta NEXT (G05), supporting-time dayparts (G07), mirrored details chevron (G08), bounded footer with overflow menu (G09), proportional art and compact tablet treatment (G10), date strip above architecture (G11).

Do not delete previous prereleases: they remain rollback evidence. Do not merge to main, alter latest/updater, or submit to Play as part of this candidate workflow.

## 9.5/10 target: evidence required before acceptance

- Build and lint pass with the existing signing certificate; versionCode exceeds the prior candidate.
- All targeted tests pass; no assertions removed or weakened to turn CI green.
- Review raw English and Urdu captures for four device/orientation profiles, normal/130%/200% text, 12h/24h, current plus next, and overnight.
- Normal tablet landscape shows all cards and utility rail without scroll or overlap; large text may reflow and scroll.
- Full Gregorian and Hijri dates remain legible without obscuring architecture; mockup geometry, warm accent and bold readable clocks are checked visually.
- NOW, NEXT, completed and tomorrow states agree with actual event dates and prayer windows.
- Every explicit card details action and footer destination remains reachable; RTL direction is correct.
- No fresh production-readiness, qualified scholarly/Urdu review, physical-device TalkBack, or Play Console certification can be claimed from JVM captures. These require their own evidence.

Implementation alone is not a 9.5 score. Final visual review and residual findings determine the actual score.
