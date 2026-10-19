# Approved Home verification — 19 October 2026

Tested implementation: `dfad16868c675b3eb655752dd72d6f66330aaeb4`.

[Passing verification run](https://github.com/haawas-alt/miqaat/actions/runs/39192712134).

## Results

- All four theme jobs passed.
- Main regression suite: 140 test methods, 135 passed, 5 opt-in visual audit methods skipped, no failures or errors. The three other theme jobs each passed their six Home acceptance methods.
- 24 standard native captures: four themes × English/Urdu × phone portrait/tablet portrait/tablet landscape.
- Phone geometry and actions also checked at 360 × 800 with reserved system-bar space. Large text, compact landscape, compact tablets, Ramadan, Iqamah, relative time, 24-hour time, five-prayer display and complete Hijri dates are covered by acceptance/regression checks.
- Debug and Play release lint passed. GitHub release APK, Play release APK and Play AAB built successfully.
- GitHub APK signature verified against the established certificate (`6285b269623127c3e2f0e87a5967f3fe143414c97371de3284d3489984541b01`). Native ELF/ZIP alignment and rejected-artwork checks passed. Play APK contains no `REQUEST_INSTALL_PACKAGES` permission.

Candidate version: `1.165-design`. Candidate checksums and exact build provenance are included with the deliverable.

## Visual and feature acceptance

Native captures were reviewed against the approved boards. Theme identity, layout hierarchy, persistent prayer timeline, independent NOW/NEXT highlights, Gallery illustrations and original Kiswah Arabic band are retained. Native scripture and live prayer values remain independent of decorative artwork. The original concept art is adapted for production rather than treated as a pixel-exact raster interface.

Short dark-theme phones, expanded Urdu relative-time/Iqamah settings and large text use scrolling reflow to keep their content readable. Compact Gallery phones retain all six rows and 48 dp controls on one screen. The preparation shortcut opens the appropriate existing lesson and preserves saved progress.

## Scope

Rendering and UI interaction verification uses Robolectric native graphics on Android SDK 34. These results are not emulator or physical-device acceptance. They do not establish a numerical UX score. Production merge and store publication are separate from this tested candidate.
