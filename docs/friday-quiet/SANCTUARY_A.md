# Approved A quiet sanctuary implementation

Selected targets: MA, KA, CA and GA, phone/tablet portrait and landscape. Artwork was derived from the user's approved image mockups through text removal and portrait extraction. Native prayer names, actual current/next times, relative time, progress and return action overlay the text-free scene. No mockup demo time is baked into the art. Existing phase/scheduler, preferences and accessible reflow route are retained. Minimal art remains respected.

The generated reference scenes and native font rasterization are not pixel-identical. Review native screenshots against the selected source mockups before acceptance; no percentage or 100% match claim is justified solely by tests.

Artwork: sanctuary_{miqaat,kiswah,celestial,gallery}_{land,port}.webp, generated text-free edits of selected A mockups, 27 October 2026. No verse or invented religious text in artwork. English and Urdu text remains native.

Validation includes exact demo-clock fixtures, English/Urdu, 12/24h, current/next panel separation, native text fit, return target/action, existing compact/minimal/overnight/accessibility suites, Friday and weekday protection, release regression/lint, signing identity and native integrity. Testing candidate only until user acceptance.

## Arabic font metrics

`sanctuary_arabic.ttf` derives from the bundled Amiri Bold, with original glyph outlines, shaping and license metadata retained. Its family name is Miqaat Quiet Arabic. Vertical metrics are 820/-380 units rather than the broad Quranic mark envelope of the source font. This font is used only for the seven unvocalised Arabic prayer headings; Urdu body text and arbitrary content retain Amiri. The compact envelope includes the actual heading ascenders/descenders and prevents large invisible font leading from shrinking the selected headings.

## Reference correction pass

The sanctuary now uses isolated static Cormorant instances: Medium for native body/header text and Bold lining/tabular numerals for clocks. The clock subset retains numeral shaping and original font licensing metadata; its renamed family and compact vertical metrics are restricted to Latin time strings. Urdu retains Amiri. Fonts are isolated from home-screen variable-font resolution.

Native dividers, header ornaments, crescent/arch motifs, Gallery's terracotta progress ribbon, and theme-specific footers follow the A compositions. Portrait artwork scales in its drawing layer, anchored at the top, rather than requesting a layout height that its parent constrains. The progress bar represents configured quiet time remaining. Native text size is searched against the actual measured width and height instead of applying a single proportional estimate.
