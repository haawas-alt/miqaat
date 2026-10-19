# Approved Home design implementation

Design direction approved by the user on 19 October 2026. Native implementation targets phone portrait, tablet portrait, and tablet landscape. Dedicated compact phone landscape and large-text reflow remain available.

## Retained value

| Feature | Implementation |
|---|---|
| Prayer schedule | Live prayer engine; six entries including Sunrise unless disabled by the user |
| Current and upcoming prayers | Independent NOW/NEXT pills, full-row/tile fills and leading markers; elapsed time is not a record that someone prayed |
| Day progress | Always-visible six-period timeline, moving live-time bead, dotted disliked-time ranges when enabled; tap opens the guidance |
| Arabic identity | Native Arabic names; original Kiswah.BAND content, independent of bitmap artwork; complete Hijri month and year |
| Time details | Live clock/relative mode, end times, Duha guidance and configured Iqamah; per-prayer details button and long press |
| Context | Prioritized alarm warning and Adhkar prompt; every other contextual door remains reachable through More |
| Destinations | Location, Settings, Timetable, Qibla, Adhkar, Learn when enabled, Friday, update, traveller and masjid actions |
| Seasonal content | Existing Ramadan/Eid kicker, fasting progress and Tarawih guidance |
| Preferences | Sunrise, 24-hour, relative time, end times, artwork selection, night dim, language, and large text |

## Layouts

- Miqaat: navy field, illuminated golden arch, timetable left/hero right on tablets.
- Kiswah: woven black and gold, original Arabic band and prayer thread; table left/hero right on tablets.
- Celestial Meridian: mountain-lake scene and restrained gold solar arc; hero left/timetable right on tablets.
- Prayer Gallery: warm ivory, architectural hero, individual illustrated periods; three-column by two-row tablet grid and illustrated phone rows. Phone prayer name and clock occupy the same horizontal band. Prepare for the hero prayer opens the matching existing guided lesson (Fajr, Maghrib or four-rakah prayer), rather than an unrelated default.

Live UI is not a screenshot or a generated interface bitmap. AI-generated source concept art necessarily differs slightly in composition from the approved boards; native text uses the real app values rather than copying mockup typos or sample dates.

## Artwork provenance

Built-in image-generation tool used for three text-free decorative assets, followed by WebP encoding. Existing Celestial landscape asset reused. Original artwork and prompt intent are preserved here for reproduction; all visible scripture is native text from the existing source.

| Saved project path | Generation prompt |
|---|---|
| `app/src/main/res/drawable-nodpi/approved_miqaat_arch.webp` | Portrait production background based on approved Miqaat phone: deep navy and gold pointed mihrab arch, geometric filigree, illuminated pillars, quiet starry center, crescent, mosque and palms, lanterns and reflective courtyard. No UI, text, numbers, writing or prayer table. |
| `app/src/main/res/drawable-nodpi/approved_gallery_atlas.webp` | Landscape watercolor art atlas based on approved Gallery: upper architectural mosque/teal domes/palms/olive branches; six equal lower paintings of dawn lake, sunrise hills, midday mosque, afternoon palms, sunset mosque, and moonlit mosque. No UI, text, Arabic, clocks or labels. |
| `app/src/main/res/drawable-nodpi/approved_kiswah_silk.webp` | Portrait black silk background based on approved Kiswah phone: quiet woven geometric center; gold geometric/floral embroidery panels and medallions at right. No text, Arabic, pseudo-Arabic or UI. Native code supplies real Arabic. |

## Verification

`ApprovedHomeAcceptanceTest` captures the real native Compose screen and checks prayer/text bounds, minimum information-button sizes, all six timeline labels, original Kiswah text, relative toggle, navigation callbacks, and the prayer detail dialog. Existing Gallery status, phone fit, contextual-door, prayer engine and Learn regressions are retained.

Renderer: Robolectric native graphics, Android SDK 34. This is not physical-device or emulator evidence and is not by itself a defensible numerical UX score. A signed candidate and final test/capture provenance are recorded separately after validation.

Backup before changes: `backup/verified-option1-before-approved-designs-20261019` at `10525d07738e2905b9a552521880f6b8d9968993`.
