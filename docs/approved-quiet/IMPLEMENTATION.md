# Approved Quiet Screen implementation

The user approved the four-theme phone/tablet direction on 19 October 2026. This work is based on the verified Home implementation at `ca6e35473a70218cc4df837c8814aab8111c7d70`.

## Native content and behavior

- The active Quiet phase identifies the current prayer; its Azaan time comes from `quietPrayerTimes`, including overrides and date rollover.
- The actual next prayer and time, including tomorrow Fajr, use the live prayer engine.
- The small header clock updates every 15 seconds. The current prayer and current Azaan time dominate the screen.
- Iqamah completion, remaining Quiet duration, the next-day label, a 48 dp return control and tap-anywhere return remain available.
- English and Urdu, 12/24-hour time, Minimal artwork and Isha night dim are retained. Text scales above 115% use scrolling reflow.
- Scripture, prayer names, clocks and status labels are native Compose text; decorative images contain no text.

## Theme direction

| Theme | Quiet treatment |
|---|---|
| Miqaat | Navy, restrained antique gold, illuminated arch and courtyard; native ivory clock |
| Kiswah | Existing black woven silk/right-edge gold embroidery; original complete native Arabic band |
| Celestial Meridian | Existing indigo mountain-lake landscape with a contrast scrim and ivory/gold text |
| Prayer Gallery | Warm parchment, faded architectural watercolour, olive foliage, deep-green text and terracotta current Azaan |

Phone uses a vertical focal area and secondary next-prayer card. Landscape tablet uses a 60/40 current/next composition. Tablet portrait retains the spacious vertical composition; compact landscape reduces the base composition while keeping all information visible. Background art adapts to the viewport; it is not a rasterised mockup interface.

## Decorative asset provenance

The built-in image-generation tool created three text-free assets from the approved final Quiet boards, then they were encoded as WebP. Prompt intent:

- `quiet_miqaat_landscape.webp`: wide navy mihrab arch, antique-gold filigree at edges, mosque/palms/courtyard in the lower quarter, quiet middle for native text; no writing, clocks or UI.
- `quiet_gallery_portrait.webp`: tall warm-ivory watercolour arch, teal domes/minarets and olives at the edges, broad quiet parchment centre; no writing, clocks or UI.
- `quiet_gallery_landscape.webp`: corresponding wide watercolour landscape with quiet central space and architecture/foliage at the corners; no writing, clocks or UI.

Miqaat phone, Kiswah and Celestial reuse the Home decorative assets. Kiswah calligraphy always uses the original native `Kiswah.BAND`.

## Verification

`ApprovedQuietAcceptanceTest` captures all themes in English/Urdu on phone and tablet, and checks compact layouts, large text, Minimal/24-hour overnight preferences, glyph bounds, panel separation, full Arabic content and return actions. The retained `QuietOptionAcceptanceTest` exercises prayer-engine date/override correctness and its device/language/font/time-format matrix.

The verification workflow runs native Robolectric SDK34 tests, the retained regression suite, lint, release builds and established signing/integrity checks. Native rendering does not constitute physical-device acceptance or a measured numerical UX score. Final results are recorded after completion.
