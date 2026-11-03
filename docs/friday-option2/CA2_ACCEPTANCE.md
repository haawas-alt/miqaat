# CA2 - active foreground implementation

The user accepted moving on from the current KA2 build and requested CA2 to match
its approved mockup at least 99.90%, in this session without scheduling.
Reference: libfile_df3909d2d094819193b975dbc393076d.
Baseline application: 4a584944a52fd72099a5248e6941bc0ceed5d19b; native review 41512793769.
MA2 and KA2 remain frozen. CA2 changes are theme-scoped; GA2 unchanged.

Baseline differences: wrong dhikr/floral band, excessive bold clock, missing
seconds, redundant prayer-name heading, missing compact Friday line, flat
header/table exterior, stacked landscape/tablet navigation and incorrect icons.

Implement a reference-traced curved gold band, live native dhikr and seconds,
Arabic/kicker/regular-clock hierarchy, celestial background continuity, compact
header, reference prayer icons, fine-bordered actions and inline navigation.
Preserve all engine/Iqamah/end facts, callbacks, 48dp rows/actions and Friday
lifecycle/reduced motion. Run all 32 CA2 profiles plus live tick; preserve all
MA2/KA2 assertions. Inspect actual native renders before signed testing delivery.
Do not invent a 99.90% similarity result; reference uses illustrative facts and
native app retains functional metadata/accessibility targets. No production push.
