# Phone portrait refinement and Light Within identity

Scope: Miqaat, Kiswah and Celestial Meridian at phone portrait only. The gate
requires portrait orientation and a smallest screen dimension below 600dp.
Prayer Gallery and tablet layouts retain their existing styling.

- Blend hero artwork into the page; remove the duplicate Celestial image behind the table.
- Keep the clock prominent and put the countdown on an opaque dark backing.
  On shorter hero regions, pair the prayer name with the clock to reserve
  countdown and 48dp details-action heights before allocating spare space.
- Replace the outlined hero capsule with a lighter action retaining a 48dp target.
- Simplify table dividers and rail; use restrained theme gold NOW/NEXT treatments.
- Keep Learn and Timetable in the phone dock; remove duplicate portrait header shortcuts.
- Remove the portrait designer credit to reduce visual clutter.
- Retain native text, prayer calculations, relative-time mode and all destinations.

The global launcher identity is approved option 03, Light Within. Production
source: `art/logo/light-within-source.webp`; exports: `art/logo/make_launcher.py`.
Artwork was prepared with the built-in image editor from the selected concept:
preserve the navy sculpted pointed doorway, warm gold inner arch and glowing
threshold; remove presentation text, duplicate thumbnail, tile edge and shadow;
continue navy to square edges. No green or additional symbol.
Density-specific adaptive and legacy icons, Android monochrome layer, Play icon
and feature graphic are updated together. The historic green source is retained
for provenance but is no longer used by the export script.

CI renders the approved baseline `bc2e2290cf8a8eac160b0d1e0f58d157d4729be0`
and the candidate on the same runner using the same isolated capture test,
then requires every existing phone
landscape PNG to be byte-identical. This covers all four themes, both languages,
normal/inset phone sizes and retained preference/quiet/iqamah cases. CI also runs
the existing portrait/tablet/accessibility checks and new day/night portrait
checks. Encrypted render artifacts permit private visual review. A successful
CI run does not claim physical-device acceptance.
