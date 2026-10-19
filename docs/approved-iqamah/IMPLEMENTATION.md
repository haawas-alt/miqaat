# Approved Iqamah countdown designs

Approved phone and tablet mockups: 19 October 2026. Based on Home + Quiet candidate ca9b215.

The live countdown is now minutes:seconds with an explicit unit label. The scheduled Iqamah time comes directly from the active service phase's end timestamp in the user's chosen timezone and clock format; it is not a newly calculated prayer schedule. The live clock remains secondary, with the existing location context retained below the header. Rounding up keeps 00:01 visible until the actual deadline. The ring shows the remaining fraction of the original service interval.

All four themes use their approved artwork and typography, with the original complete Kiswah Arabic band rendered as native text. Phone portrait uses a vertical hierarchy; landscape tablet places the counter beside the prayer, absolute Iqamah time, existing Arabic/English or Urdu row-straightening reminder and source, and both actions. Start Iqamah now and Dismiss retain their existing callbacks. Playback, scheduling, tick sounds, transition to Iqamah and Quiet, and settings are unchanged.

Large text on portrait reflows. Short/large-text landscape pins the counter and both actions while secondary information scrolls independently. English, Urdu/RTL, Minimal artwork, 12/24-hour time, midnight target date and Isha night dim are retained. No UI text or religious content is embedded in raster artwork. The approved countdown state is the scope of this change; the existing Iqamah playback state is retained.

Verification must include deadline/rounding/progress cases, native text geometry and callback tests across all themes, languages and device sizes, retained regression tests, lint and signed builds. Final evidence and limitations are recorded after checks complete. No numerical UX score or physical-device acceptance is inferred from automated rendering.
