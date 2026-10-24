# Kiswah Option 1: matching phone fonts and one-phrase portrait

The approved Traditional Silver Vine border uses extracted original artwork rather than the earlier generic vector vine. Phone portrait and landscape now share the existing Reem Kufi font. Portrait displays only the first phrase of `Kiswah.BAND`; landscape retains all three phrases. Tablet banners retain their previous text and allocation.

The portrait frame is shortened for one phrase. Its original top/bottom rails and four corners retain their scale; a middle section of the side rails is removed instead of flattening the ornament. Landscape retains the source frame's aspect ratio. The provenance JSON records the exact source crop, shortened rows and asset hashes.

Arabic remains native text. Screen-reader descriptions match the displayed phrase in portrait. Candidate font sizes are measured with the current density to avoid overflow at Android's enlarged font settings.

Short landscape layouts use available height after system insets and retain six prayer cards and minimum 48dp detail targets. The smallest phone puts the full timeline beside the cards; its labels are checked for overflow. Normal landscape retains the stacked hero; very short available spaces use a compact hero. Urdu timeline labels receive their full native line height.

Local native Android 34 verification: 38 selected CI acceptance methods pass with zero failures, errors or skips. These cover Home, Quiet, Iqamah, English/Urdu, normal/small phone layouts, larger fonts, content, font-family identity, navigation and target sizes. Native renders have been visually inspected. This is Robolectric evidence, not physical-device acceptance.

The user explicitly authorized uploading the code and artwork to the public GitHub repository haawas-alt/miqaat. CI builds candidate 1.176-kiswah with the established release key and preserves encrypted visual evidence. No stable-release promotion or Play publication is part of this change.
