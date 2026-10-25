# Portrait visual correction · 1.178

The 1.177 layout spread the prayer name and time apart and suppressed too much
artwork colour. This correction keeps them in one centred composition, with a
bilingual prayer title above the clock, a closely attached countdown, and a
shallow outlined action inside its unchanged 48dp touch target. Only genuinely
short hero regions use a tightly grouped compact row. The upper group wraps its
content instead of consuming spare height between the title and countdown.

The original artwork assets are unchanged. Broad black overlays are removed;
a local radial scrim protects the central text while the original sunset,
mosque lighting and gold embroidery remain visible. Edge fades blend the scene
into its page. The portrait starfield and geometric pattern are quieter.

NEXT uses an outlined gold badge and a stronger tinted row; NOW retains a
filled gold badge. Selected rows have a taller accent, thin gold boundaries and
stronger type. Secondary end times are slightly larger.

Scope remains the three dark phone portrait themes. The approved Kiswah banner,
Prayer Gallery, tablet layouts, launcher identity and every phone landscape
composition are retained. Early native morning renders use relative-time mode
and reliable alerts, matching the supplied review screenshots; small-screen,
Urdu, night and warning-state tests remain in the suite. Early renders are
encrypted and available before the full regression/release stages complete.

The landscape comparison remains pixel-exact outside the non-text timeline
Canvas, whose bounds must also match. Native marker edges may vary by at most
16 connected pixels per cluster, 64 overall, at 16/255 per channel. This avoids
combining separate marker-edge variations into a false failure. Text, artwork,
controls, larger clusters and meaningful colour changes remain protected.

Passing tests establishes fit, actions and regressions. Visual quality requires
inspection of the native renders; it is not established by a numerical score.
