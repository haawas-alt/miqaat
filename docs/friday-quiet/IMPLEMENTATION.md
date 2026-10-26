# Approved Friday and quiet-screen designs

The requested layouts are implemented as native Compose facts and actions over existing text-free artwork. The generated mockups are design targets, not screenshots or numerical accuracy evidence.

## Scope and behaviour

- Friday homepages: Miqaat, Kiswah, Celestial Meridian and Prayer Gallery, English/Urdu, phone and tablet, portrait/landscape.
- The homepage is active on Friday before local Maghrib when Friday reminders are enabled. The existing Thursday-night Friday shortcut remains on the regular homepage.
- Configured Jumuah timing remains opt-in. This presentation never enables it, substitutes a mockup's sample time, or alters masjid imports, calculation settings, alert preferences or scheduler behaviour.
- Prayer rows, NOW/NEXT and hero values use the existing engine. Sunrise is not given an Azaan alarm control.
- Al-Kahf opens the existing Friday completion tracker; it does not promise an unimplemented Quran reader. Salawat shows the recorded local count, not an invented percentage. All Friday shortcuts open the existing Friday routine page.
- Location, settings, prayer explanations, relative-time toggle, Qibla, Adhkar, Learn and timetable remain functional. Contextual warnings/update/travel/import actions remain available from the header menu. Home Qibla/Adhkar visibility preferences are honoured.
- Native quiet composition: phase-backed current prayer/time, actual next prayer, live clock, expiry/progress and service Stop/return callback. Minimal art, Urdu/RTL, 24h and night dim remain supported.
- Accessibility text above 115% retains the established reflowing/scrolling home and quiet routes. Very short phone landscape home viewports below 405dp retain the established compact home so the new Friday composition does not squeeze six prayer targets below 48dp.

## Visual fidelity limits

Native fonts, icon drawing, artwork crops and rasterisation differ from the generated concepts. Native facts deliberately differ from the sample dates/times/end-times and invented progress indicators visible in some mockups. A pixel-identical or 10/10 claim is not justified by a passing test suite.

The UI uses the existing approved backgrounds: gold arch, Kiswah silk, alpine constellation lake, and the top garden panorama of the Gallery atlas. Generated UI text is never shipped as an image.

## Verification

The new `FridayQuietApprovedTest` captures the real routed UI for four themes × four device profiles × English/Urdu, for both Friday and quiet views. It checks native text bounds, current/next times, minimum prayer-row heights and actions. Existing quiet/short-landscape/accessibility and full release suites remain gates. Protection renders compare non-Friday homes and unchanged iqamah screens against production e7399d57a7e0; only intentionally redesigned quiet captures are excluded from pixel comparison. Existing pixel thresholds are not relaxed.

Screenshots are encrypted in CI with a task-specific public key; the private key is not in the repository. CI is automated runtime evidence, not a physical-device alarm or qualified content sign-off.

The workflow publishes a signed testing prerelease only after all gates succeed. It does not change the production updater or Play Console.
