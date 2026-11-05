# Reopened four-theme reference correction - 6 November 2026, Sydney

The user explicitly requested all remaining gaps in MA2, KA2, CA2 and GA2 be
fixed. This supersedes the earlier theme-freeze notes for this work.
Baseline app: 53234ea3f7ff8602a4d9c1e66342477c1d28ea42.
Documentation parent: a775e822d562233eaf65650a13add101bb2b4bf8.

## Concrete corrections

- A global landscape header and banner precede both the table and hero, in all
  four themes. The clock no longer shares its column with a second header.
- The landscape dock is inline and 48dp tall. On a short viewport, the timetable
  really scrolls; every prayer still has a non-overlapping 48dp row. This replaces
  the former layout compromise rather than faking or shrinking touch bounds.
- Portrait allocation uses actual shared header/band/dock dimensions once.
  Shortcut space is counted inside the hero, the timetable fills the remainder,
  and the dock is anchored at the bottom.
- CA2 adopts GA2's constrained Urdu reflow so the display clock remains dominant.
  The same minimum 1.5x digit/countdown font hierarchy is checked in every theme.
- MA2 has reference-traced text-free green/gold end panels rather than a generic
  diamond grid. KA2 interior stitch detail and live header seconds are restored.
  CA2 gets its fine comet rail/glow and rounded dock outline; its backdrop samples
  star-only strips so the arch is not repeated above the banner. GA2 uses small
  gold dhikr separators, correctly aligned architecture and full-width chrome.

## Verification

Run 32 profiles per theme (128 total): four layouts, two languages, both clock
formats, normal and 115% text. Assert real facts, clipped glyphs, live ticks,
current/next state, full-width banner position and clock dominance. Prayer checks
scroll the actual row fully into view before checking facts and 48dp bounds.
Preserve all unrelated weekday/quiet behavior and release integrity gates.
Inspect current native captures before final packaging and delivery.

No numerical fidelity percentage is inferred from passing tests. The approved
boards have illustrative data and inconsistent labelled aspect ratios. Match
the reference composition responsively and document any remaining differences.
No scheduling and no production promotion are requested by this correction.
