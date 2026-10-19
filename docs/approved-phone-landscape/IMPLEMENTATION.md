# Approved phone landscape implementation

Approved 19 October 2026: dedicated short phone layouts for Home, Iqamah countdown and Quiet Mode in Miqaat, Kiswah, Celestial Meridian and Prayer Gallery. Based on verified Home/Quiet/Iqamah source f0d581d54471851b8229b500e61bbe31fe7f406e.

Home uses a two-column, three-row grid of all configured prayers beside the hero, plus the day timeline and context actions. Borders are restrained and Gallery art is a narrow thumbnail strip. The timeline has a translucent theme-coloured backing so bright horizons and architectural detail cannot sit directly behind its prayer labels. Compact cards pair Arabic names with secondary details, preserving their native glyph height even with the Kiswah band and system-bar reserve. Existing row tap-to-relative, long-press/info, location, Learn, timetable, settings, Qibla, adhkar, alert fixes, extra actions, preparation and seasonal information remain available. Bell icons retain their existing informational role; notification settings remain in settings/alert actions. Current and next selections use actual prayer windows, including tomorrow Fajr. The full original Kiswah band remains native text.

Iqamah places the live counter beside the prayer/deadline/reminder and both actions; normal phone landscape no longer relegates details to a scrolling panel. Quiet uses three horizontal information clusters: prayer, current Azaan, next Azaan; expiry and return actions remain visible. The native service remains responsible for scheduling, sounds, countdown completion and phase transitions.

A short landscape viewport (configuration height below 500dp) selects these layouts. Portrait and tablet routes retain their verified implementations. Normal default phone layouts have no scroll container. Expanded per-prayer Iqamah or relative-time preferences use a deliberate scrolling reflow to preserve full secondary details. System text above 115% retains the existing accessible reflow; large-text Iqamah keeps its counter and both actions pinned. No text is painted into background images. Artwork, complete Arabic, English/Urdu, RTL, Minimal mode, 12/24-hour times, night dim and display preferences remain supported.

Native acceptance covers normal and 740x360 phones with inset padding, both languages, all themes, text bounds, minimum touch targets, values, highlights, original Arabic band, actions and preferences. Retained portrait/tablet acceptance, regression, lint, established signing and build integrity checks must pass before delivery. A numerical design target does not imply a measured UX score; physical-device acceptance is separate.

## Final native acceptance

Tested application source: `4dbfb1fc02b4804083e49d0c4d714cb8f1a5c13e`. Verification run: https://github.com/haawas-alt/miqaat/actions/runs/39224859615.

All four native acceptance jobs passed their Home, Quiet, Iqamah and counter checks (19 test methods per theme). The new landscape suite renders 18 cases per theme: English/Urdu at normal and 740x360 phone sizes, with system-bar reserve; relative, Iqamah and five-prayer/24-hour preferences. Current/next states, clock values, text bounds, minimum button targets, the original Kiswah band and existing interactions are asserted. Native captures were reviewed across all 12 layouts, with additional small-phone and Urdu inspection.

## Release verification result

All four jobs in run `39224859615` succeeded. The main regression suite has 153 methods: 148 passed and five optional screenshot/audit methods were skipped. The three other theme jobs passed 19 methods each (57 additional passes). The new native suite produced 72 landscape captures. Debug and Play release lint, debug/release APK assembly and the Play bundle build passed.

Candidate version: `1.168-landscape`. The release APK uses the established signing certificate, SHA-256 `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`. Release-asset checks and native 16KB alignment checks passed, and the Play APK does not request package installation permission. Candidate hashes and source/run provenance accompany the delivery. These results use native Robolectric SDK34 rendering; physical-device acceptance remains outstanding.
