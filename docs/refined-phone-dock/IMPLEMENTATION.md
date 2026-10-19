# Phone dock, prayer alignment and current theme previews

Approved 19 October 2026. Extends the verified 1.168 landscape implementation at 93cd28c26ba037a4189b8f905160a4dcde4f2a22.

Normal phone portrait uses one borderless action dock. Qibla includes its live bearing, and Adhkar includes the relevant Morning/Evening/After-prayer period and opens that collection. The general collection remains available outside a prompted period. Learn and Monthly Timetable remain direct actions, respecting their existing display preferences. Reliability warnings, Friday, post-prayer, travel, update and timetable-source actions remain available in a single borderless notice and its extra-actions menu. Tablet navigation and large-text accessible layouts remain in place.

The phone prayer-name column is measured independently of NOW/NEXT badges and the Sunrise bell slot. All six clock and secondary-detail origins therefore align in both LTR and RTL, including relative and 24-hour display preferences.

Kiswah's native calligraphy frame now reserves 12dp internal vertical padding around the original text. On short landscape phones under 390dp tall it keeps 6dp clearance and slightly reduces the space between prayer cards so all six 48dp targets remain visible. Regular phone landscape, portrait and tablet retain 12dp. The text is not downsized or replaced; original complete Arabic and diacritics remain native.

Display & Art theme cards now render a decorative, scaled version of the actual current landscape Home composable with current art and language preferences. They no longer use obsolete artwork or a separately drawn five-prayer mock layout. The card controls remain full-size radio buttons with selected/not-selected accessibility state; all taps on the miniature select that theme. The miniature uses sample prayer data and performs no navigation, audio or scheduling actions.

Verification will include native phone portrait and landscape checks, both languages, small/normal phones, clock-column geometry, calligraphy clearance, dock actions/preferences, theme selection, retained tablet and flow checks, full regression, lint and established-certificate signed release builds. No physical-device acceptance or measured numerical UX score is implied.
