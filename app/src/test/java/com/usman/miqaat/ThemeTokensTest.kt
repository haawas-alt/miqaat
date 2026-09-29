package com.usman.miqaat

import androidx.compose.ui.graphics.Color
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.ui.ThemeTokens
import com.usman.miqaat.ui.ThemeTokenSets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class ThemeTokensTest {
    private fun lin(c: Float) = if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    private fun lum(c: Color) = 0.2126f * lin(c.red) + 0.7152f * lin(c.green) + 0.0722f * lin(c.blue)
    /** WCAG contrast of [fg] over an opaque [bg]. */
    private fun over(fg: Color, bg: Color): Color = Color(fg.red * fg.alpha + bg.red * (1 - fg.alpha), fg.green * fg.alpha + bg.green * (1 - fg.alpha), fg.blue * fg.alpha + bg.blue * (1 - fg.alpha))
    private fun ratio(fg: Color, bg: Color): Float { val a = lum(over(fg, bg)); val b = lum(bg); return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f) }

    @Test fun fourThemesInOrderWithStableNames() {
        assertEquals(listOf("MIQAAT", "KISWAH", "CELESTIAL_MERIDIAN", "PRAYER_GALLERY"), AppTheme.entries.map { it.name })
        assertEquals("Celestial Meridian", AppTheme.CELESTIAL_MERIDIAN.label)
        assertEquals("Prayer Gallery", AppTheme.PRAYER_GALLERY.label)
        assertEquals(4, ThemeTokenSets.all.size)
    }

    @Test fun everyThemeHasItsOwnTokens() {
        AppTheme.entries.forEach { assertEquals(it, ThemeTokenSets.of(it).theme) }
    }

    /** Legacy or unknown stored values must never crash or reset to something surprising. */
    @Test fun unknownStoredThemeFallsBackToDefault() {
        fun parse(s: String?) = runCatching { AppTheme.valueOf(s ?: "") }.getOrDefault(AppTheme.MIQAAT)
        assertEquals(AppTheme.KISWAH, parse("KISWAH")); assertEquals(AppTheme.MIQAAT, parse("MIQAAT"))
        assertEquals(AppTheme.PRAYER_GALLERY, parse("PRAYER_GALLERY"))
        assertEquals(AppTheme.MIQAAT, parse("SOMETHING_FROM_A_FUTURE_BUILD")); assertEquals(AppTheme.MIQAAT, parse(null))
    }

    /** Body-text roles must clear WCAG AA (4.5:1) on the surfaces they sit on. */
    @Test fun textRolesMeetAA() {
        fun check(tk: ThemeTokens, name: String, fg: Color, bg: Color, min: Float = 4.5f) =
            assertTrue("${tk.theme} $name on ${bg} = ${ratio(fg, bg)}", ratio(fg, bg) >= min)
        ThemeTokenSets.all.forEach { tk ->
            listOf(tk.background, tk.surface, tk.surfaceRaised).forEach { bg ->
                check(tk, "contentPrimary", tk.contentPrimary, bg)
                check(tk, "contentSecondary", tk.contentSecondary, bg)
                check(tk, "contentMuted", tk.contentMuted, bg)
                check(tk, "success", tk.success, bg)
                check(tk, "warning", tk.warning, bg)
                check(tk, "info", tk.info, bg)
            }
            check(tk, "onPrimary/primary", tk.onPrimary, tk.primary)
            check(tk, "contentPrimary/selected", tk.contentPrimary, tk.selectedSurface)
            check(tk, "primary/selected", tk.primary, tk.selectedSurface, 3f)   // large/UI use
        }
    }

    @Test fun lightAndDarkAreWhatTheyClaim() {
        assertTrue(ThemeTokenSets.gallery.dark.not() && lum(ThemeTokenSets.gallery.background) > 0.8f)
        listOf(ThemeTokenSets.miqaat, ThemeTokenSets.kiswah, ThemeTokenSets.celestial).forEach { assertTrue(it.dark && lum(it.background) < 0.05f) }
    }

    @Test fun screenTextRolesMeetAAOnNewThemes() {
        for (tk in listOf(ThemeTokenSets.celestial, ThemeTokenSets.gallery)) {
            for ((name, fg) in listOf("arabicText" to tk.arabicText, "todayText" to tk.todayText, "fridayText" to tk.fridayText, "accent" to tk.accent)) {
                for ((bn, bg) in listOf("background" to tk.background, "surface" to tk.surface)) {
                    val r = ratio(fg, bg)
                    assertTrue("${tk.theme} $name on $bn = $r", r >= 4.5f)
                }
            }
        }
    }

    @Test fun everyThemeHasAzaanSkiesForEveryPhase() {
        for (tk in ThemeTokenSets.all) {
            assertTrue(tk.skyAzaan.size >= 2 && tk.skyIqamah.size >= 2 && tk.skyDua.size >= 2)
            // the sky must agree with the theme's light/dark claim so text tokens stay readable on it
            for (c in tk.skyAzaan + tk.skyIqamah + tk.skyDua) assertTrue("${tk.theme} sky vs dark=${tk.dark}", (lum(c) < 0.25f) == tk.dark)
        }
    }
}
