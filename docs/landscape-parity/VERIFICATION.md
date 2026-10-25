# Miqaat 1.170 landscape candidate verification

Application source: `36027df6d98fa4a31ee3119fa9fedde901031425`.
Cloud run: https://github.com/haawas-alt/miqaat/actions/runs/39275118414

The signed direct-download APK is `Miqaat-1.170-landscape.apk` (48,281,811 bytes). SHA-256: `b912f6d1e8a17b5186068af324dc71692861b900407b8d1b4227a576cfc7de9c`.
Established signing certificate SHA-256: `6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`.
The downloaded APK was checked against the candidate checksum, certificate record and source provenance.

All four native theme acceptance stages passed. Final collected XML reports contain 252 records: 247 passed, zero failures/errors and five optional capture skips. Miqaat, Kiswah and Celestial each contribute 31 passing records; the full Gallery regression report contributes 159 records (154 passed, five skipped). The earlier Gallery acceptance stage also passed before the full suite replaced its XML output.

Full regression, Github debug lint/assembly, signed Github/Play release builds and Play release lint passed. Candidate checks verify the established signing certificate, native 16KB ELF/ZIP alignment, absence of rejected assets and absence of the install-packages permission from the Play edition.

Native captures were visually inspected for the landscape composition, Kiswah calligraphy clearance, prayer/clock alignment, badges, hero controls, portrait action dock and current-rendered theme selector. Compact Urdu cards reflow to retain complete text and touch targets. Gallery's alert action uses dark amber text on a cream fill (nominal contrast 6.02:1).

Rendering/testing used Robolectric native graphics on SDK34; physical-device acceptance has not been performed. Exact 99.99% pixel identity to the generated reference has not been measured or established. No qualified religious sign-off or Play-store approval is implied by this UI candidate.
