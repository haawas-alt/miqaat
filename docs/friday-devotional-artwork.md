# Friday devotional artwork provenance

The approved handoff ZIP contains flattened `views/` mockups and no separate background layers. These four MA landscape assets are reference-guided reconstructions, not extracted original art. Live clock, prayer data, Arabic and English text remain Android text layers.

| Asset | Handoff reference | Intended composition |
| --- | --- | --- |
| `friday_devotional_ma_1_landscape_refined.jpg` | `views/MA_phone_landscape_01.png` | Gold arcade, green dome, minarets and reflecting courtyard on the left; textured emerald right field. |
| `friday_devotional_ma_2_landscape_clean_pages.jpg` | `views/MA_phone_landscape_02.png` | Lattice windows, warm sunbeams and an open Quran on a rehal on the left; emerald right field. The page detail is deliberately out of focus so generated lettering cannot be mistaken for Quranic text. |
| `friday_devotional_ma_3_landscape_refined.jpg` | `views/MA_phone_landscape_03.png` | Ornate arch, green dome and lit minarets on the left; emerald right field. |
| `friday_devotional_ma_4_landscape_refined.jpg` | `views/MA_phone_landscape_04.png` | Sunset courtyard, gold arcade, lantern and reflected light on the left; emerald right field. |

All four images were created by editing the named visual references to remove the device frame and baked-in UI. They are bundled locally and require no network access at runtime. The mockups' artwork cannot be reproduced pixel for pixel from these flattened references. Native screenshot review remains required before accepting them.

## Portrait artwork candidates (2026-11-26)
The KA1, KA2, CA1, and CA2 portrait assets were reconstructed from the approved `views/*_phone_portrait_0[12].png` references with the built-in image editor. They are background-only raster assets; live text, dates, countdowns, and prayer data remain Compose content. KA3/KA4/CA3/CA4 stay on the previous assets because the new candidates left larger blank regions and failed visual selection. New assets are confined to portrait devotional views; regular homepages and landscape assets are unchanged. Exact source artwork was not included in the handoff, so these are reconstructions and must be compared with native captures before acceptance.
