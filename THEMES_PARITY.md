# Four-theme feature parity checklist

Status key: ✅ verified · 🟡 built, needs on-device check · ⬜ not started

| Area | MIQAAT | KISWAH | CELESTIAL | GALLERY |
|---|---|---|---|---|
| Enum, persistence, unknown-value fallback (unit test) | ✅ | ✅ | ✅ | ✅ |
| Settings selector with selected-state semantics | ✅ | ✅ | ✅ | ✅ |
| Token set + AA text contrast (unit test) | ✅ | ✅ | ✅ | ✅ |
| Home: tablet landscape | unchanged | unchanged | 🟡 | 🟡 |
| Home: phone / portrait | unchanged | unchanged | 🟡 | 🟡 |
| Monthly timetable | unchanged | unchanged | 🟡 | 🟡 |
| Qibla | unchanged | unchanged | 🟡 | 🟡 |
| Adhkar (morning/evening/after prayer) | unchanged | unchanged | 🟡 | 🟡 |
| Learn Salah (overview + lesson) | unchanged | unchanged | 🟡 | 🟡 |
| Azaan sequence | unchanged | unchanged | 🟡 | 🟡 |
| Friday / Jumuʿah | unchanged | unchanged | 🟡 | 🟡 |
| Setup, dialogs | unchanged | unchanged | ⬜ | ⬜ |
| Font scale 100/130/200, Urdu/RTL, TalkBack | – | – | ⬜ | ⬜ |

Engines (prayer, alarm, scheduling, religious content, persistence) are not touched by theming.
Live data only: every home value comes from `HomeModel.kt`, which reuses the existing engine/L10n calls.

Screens other than the homes read `screenTokens()`: Miqaat/Kiswah get exactly the palette they always used; the two new themes get their own tokens. Behaviour code (sensor, counters, phases, audio) was not edited.
Quiet/in-prayer Azaan screen stays near-black in every theme on purpose (dark room, wake-on-tap).
