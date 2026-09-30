# Approved artwork provenance

The only Celestial Meridian and Prayer Gallery artwork in the app. Each file is a lossless WebP re-encode of the approved-v2 PNG from the correction package (SHA-256 of the source PNG shown; verified against SOURCE_OF_TRUTH_MANIFEST.md).

The earlier moon/night and empty-mosque images are rejected and must not return. `tools/check_forbidden_assets.py` fails the build if their content hashes reappear.

| Resource | Source file | Source SHA-256 |
|---|---|---|
| `art_celestial_landscape_v2.webp` | `celestial-meridian-tablet-landscape-approved-v2.png` | `05C8E3CA9E139322A36363663282DA4AD8E8BC301E821F75734DADC09B7C02A0` |
| `art_celestial_portrait_v2.webp` | `celestial-meridian-phone-portrait-approved-v2.png` | `5CCE8D4A3100B853CB78E0272499F3E52FE972366F411F055EED3AF8EC0C96EE` |
| `art_gallery_landscape_v2.webp` | `prayer-gallery-tablet-landscape-approved-v2.png` | `C41E640F8AFE7323D0BD17CE20CCB654F2F0BAA15E02CACB4F4F85CE2D2D3643` |
| `art_gallery_portrait_v2.webp` | `prayer-gallery-phone-portrait-approved-v2.png` | `726A81DA1850262E7C73E93B52D5D00264B7BC4D168825D0899539078AB0E667` |
