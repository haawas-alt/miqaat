package com.usman.miqaat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.usman.miqaat.data.AppTheme

/** Which artwork family a theme draws. Screens pick art through this, never by comparing themes. */
enum class ArtStyle { ILLUMINATED_SKY, KISWAH_SILK, CELESTIAL, GALLERY }

/**
 * Semantic design tokens for one theme. Screens read these instead of comparing [AppTheme] values.
 * Colours named `*Text`-safe are checked against their intended surfaces for WCAG AA (≥ 4.5:1) in ThemeTokensTest.
 *
 * MIQAAT and KISWAH map onto the palettes the app already used, so existing screens look identical while they
 * migrate; CELESTIAL_MERIDIAN and PRAYER_GALLERY are the two new systems.
 */
@Immutable
data class ThemeTokens(
    val theme: AppTheme,
    val dark: Boolean,
    val art: ArtStyle,

    // surfaces
    val background: Color,
    val backgroundBrush: Brush,
    val surface: Color,
    val surfaceRaised: Color,
    val selectedSurface: Color,
    val divider: Color,
    val outline: Color,
    val focusRing: Color,

    // actions and content
    val primary: Color,
    val onPrimary: Color,
    val contentPrimary: Color,
    val contentSecondary: Color,
    val contentMuted: Color,

    // status (all text-safe on `surface` and `background`)
    val info: Color,
    val success: Color,
    val warning: Color,
    /** Second accent, text-safe (Gallery: terracotta; Celestial: coral). */
    val accent: Color,
    /** Sun / time colour for artwork and non-text marks only; not for text on light surfaces. */
    val sun: Color,
    /** Failure colour, text-safe on `surface`; always paired with an icon and words. */
    val error: Color,

    // table / list emphasis
    val todayText: Color,
    val fridayText: Color,
    val neutralStroke: Color,
    /** Arabic scripture on reading surfaces. */
    val arabicText: Color,
    /** Side-panel / rail wash and subtle raised fill. */
    val scrim: Color,
    val softFill: Color,
    /** Full-screen Azaan sky per phase (top→bottom). */
    val skyAzaan: List<Color>,
    val skyIqamah: List<Color>,
    val skyDua: List<Color>,

    // shape and depth
    val cornerSmall: Dp,
    val cornerMedium: Dp,
    val cornerLarge: Dp,
    val elevation: Dp,

    // type roles (fonts are the app's bundled resources; Urdu switches to Nastaliq in uiFont())
    val fontDisplay: FontFamily,
    val fontUi: FontFamily,
    val fontArabic: FontFamily
)

object ThemeTokenSets {
    val miqaat = ThemeTokens(
        theme = AppTheme.MIQAAT, dark = true, art = ArtStyle.ILLUMINATED_SKY,
        background = Palette.night, backgroundBrush = Brush.verticalGradient(listOf(Palette.night, Palette.panel)),
        surface = Palette.panel, surfaceRaised = Palette.panelRaised, selectedSurface = Color(0xFF26304F),
        divider = Palette.line, outline = Palette.lineStrong, focusRing = Palette.gold,
        primary = Color(0xFFF0B84A), onPrimary = Palette.night,
        contentPrimary = Palette.ivory, contentSecondary = Palette.textSecondary, contentMuted = Palette.textMuted,
        info = Color(0xFF9CC3E8), success = Color(0xFF5FD38A), warning = Color(0xFFF2A07B), accent = Palette.goldSoft, sun = Palette.gold,
        error = Color(0xFFF08C8C),
        todayText = Color(0xFFF6E7B8), fridayText = Color(0xFFA6E3B8), neutralStroke = Color.White.copy(alpha = 0.2f),
        arabicText = Color(0xFFF6E7B8), scrim = Color.Black.copy(alpha = 0.18f), softFill = Color.White.copy(alpha = 0.06f),
        skyAzaan = listOf(Color(0xFF2A1440), Color(0xFF0A0716)), skyIqamah = listOf(Color(0xFF163A3A), Color(0xFF0B1F24), Color(0xFF06131A)), skyDua = listOf(Color(0xFF1E2A5C), Color(0xFF0D1533), Color(0xFF080D24)),
        cornerSmall = 10.dp, cornerMedium = 16.dp, cornerLarge = 24.dp, elevation = 0.dp,
        fontDisplay = Cormorant, fontUi = Nunito, fontArabic = Amiri
    )

    val kiswah = ThemeTokens(
        theme = AppTheme.KISWAH, dark = true, art = ArtStyle.KISWAH_SILK,
        background = Color(0xFF0B0B0B), backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF0B0B0B), Kiswah.silk)),
        surface = Color(0xFF121212), surfaceRaised = Color(0xFF1A1814), selectedSurface = Color(0xFF2A2415),
        divider = Kiswah.thread.copy(alpha = 0.35f), outline = Kiswah.thread.copy(alpha = 0.6f), focusRing = Kiswah.threadSoft,
        primary = Color(0xFFE6B450), onPrimary = Color(0xFF0B0B0B),
        contentPrimary = Kiswah.ivory, contentSecondary = Color(0xFFCFCABD), contentMuted = Palette.textMuted,
        info = Kiswah.threadSoft, success = Palette.mint, warning = Color(0xFFF2A07B), accent = Kiswah.threadSoft, sun = Kiswah.thread,
        error = Color(0xFFF08C8C),
        todayText = Color(0xFFF6E7B8), fridayText = Color(0xFFA6E3B8), neutralStroke = Color.White.copy(alpha = 0.2f),
        arabicText = Color(0xFFF6E7B8), scrim = Color.Black.copy(alpha = 0.18f), softFill = Color.White.copy(alpha = 0.06f),
        skyAzaan = listOf(Color(0xFF0B0B0B), Kiswah.silk), skyIqamah = listOf(Color(0xFF0B0B0B), Kiswah.silk), skyDua = listOf(Color(0xFF0B0B0B), Kiswah.silk),
        cornerSmall = 8.dp, cornerMedium = 12.dp, cornerLarge = 14.dp, elevation = 0.dp,
        fontDisplay = Cormorant, fontUi = Nunito, fontArabic = ReemKufi
    )

    /** Dark, midnight-navy, solar-arc system. Spec: docs in the four-theme handoff, DESIGN_TOKENS.md. */
    val celestial = ThemeTokens(
        theme = AppTheme.CELESTIAL_MERIDIAN, dark = true, art = ArtStyle.CELESTIAL,
        background = Color(0xFF061A36), backgroundBrush = Brush.verticalGradient(listOf(Color(0xFF061A36), Color(0xFF0B2C55))),
        surface = Color(0xFF0D2748), surfaceRaised = Color(0xFF133559), selectedSurface = Color(0xFF3A2B33),
        divider = Color(0x33B9C8DB), outline = Color(0x66B9C8DB), focusRing = Color(0xFF72D7E8),
        primary = Color(0xFFFFD166), onPrimary = Color(0xFF061A36),
        contentPrimary = Color(0xFFFFF6E5), contentSecondary = Color(0xFFB9C8DB), contentMuted = Color(0xFF8FA3BC),
        info = Color(0xFF72D7E8), success = Color(0xFF63D7BB), warning = Color(0xFFFF805C), accent = Color(0xFFFF805C), sun = Color(0xFFFFD166),
        error = Color(0xFFFF8A80),
        todayText = Color(0xFFFFD166), fridayText = Color(0xFF63D7BB), neutralStroke = Color(0x33B9C8DB),
        arabicText = Color(0xFFFFF6E5), scrim = Color(0x33061A36), softFill = Color(0x14FFFFFF),
        skyAzaan = listOf(Color(0xFF1B2450), Color(0xFF061A36)), skyIqamah = listOf(Color(0xFF0C4256), Color(0xFF06243F), Color(0xFF061A36)), skyDua = listOf(Color(0xFF0F2E5C), Color(0xFF082246), Color(0xFF061A36)),
        cornerSmall = 12.dp, cornerMedium = 18.dp, cornerLarge = 24.dp, elevation = 0.dp,
        fontDisplay = Cormorant, fontUi = Nunito, fontArabic = Amiri
    )

    /**
     * Warm-light editorial system. Deviation from the handoff's "around" values, recorded on purpose: the text-role
     * terracotta and green are darkened slightly (A94F31, 0E7A58) so they clear 4.5:1 on ivory; the original
     * B85A3A / 15936C are kept as [sun]-style fills where they are never text.
     */
    val gallery = ThemeTokens(
        theme = AppTheme.PRAYER_GALLERY, dark = false, art = ArtStyle.GALLERY,
        background = Color(0xFFFBF7EE), backgroundBrush = Brush.verticalGradient(listOf(Color(0xFFFBF7EE), Color(0xFFF5EFE2))),
        surface = Color(0xFFFFFDF7), surfaceRaised = Color(0xFFF3ECDD), selectedSurface = Color(0xFFE8EFFC),
        divider = Color(0xFFD8D0C3), outline = Color(0xFF8C8377), focusRing = Color(0xFF1559D6),
        primary = Color(0xFF1559D6), onPrimary = Color(0xFFFFFFFF),
        contentPrimary = Color(0xFF0B302D), contentSecondary = Color(0xFF665F59), contentMuted = Color(0xFF6B635C),
        info = Color(0xFF1559D6), success = Color(0xFF0E7A58), warning = Color(0xFFA94F31), accent = Color(0xFFA94F31), sun = Color(0xFFE7A94B),
        error = Color(0xFFB3261E),
        todayText = Color(0xFF1559D6), fridayText = Color(0xFF0E7A58), neutralStroke = Color(0xFF8C8377),
        arabicText = Color(0xFF0B302D), scrim = Color(0x0D0B302D), softFill = Color(0xFFF3ECDD),
        skyAzaan = listOf(Color(0xFFF6E7D0), Color(0xFFFBF7EE)), skyIqamah = listOf(Color(0xFFDCEBE4), Color(0xFFF3F4EA), Color(0xFFFBF7EE)), skyDua = listOf(Color(0xFFE3EAF8), Color(0xFFF3F0EA), Color(0xFFFBF7EE)),
        cornerSmall = 10.dp, cornerMedium = 16.dp, cornerLarge = 22.dp, elevation = 0.dp,
        fontDisplay = Cormorant, fontUi = Nunito, fontArabic = Amiri
    )

    fun of(theme: AppTheme): ThemeTokens = when (theme) {
        AppTheme.MIQAAT -> miqaat
        AppTheme.KISWAH -> kiswah
        AppTheme.CELESTIAL_MERIDIAN -> celestial
        AppTheme.PRAYER_GALLERY -> gallery
    }

    val all: List<ThemeTokens> get() = AppTheme.entries.map(::of)
}

val LocalThemeTokens = staticCompositionLocalOf { ThemeTokenSets.miqaat }

/** Read the active theme's tokens: `Tokens.current.primary`. */
object Tokens {
    val current: ThemeTokens @Composable @ReadOnlyComposable get() = LocalThemeTokens.current
}

@Composable
fun ProvideThemeTokens(theme: AppTheme, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalThemeTokens provides ThemeTokenSets.of(theme), content = content)
}

/**
 * Tokens for screens that have not been redesigned for every theme. Miqaat and Kiswah keep the palette those screens
 * have always used (so they look exactly as before); the two new themes supply their own. One choke point, no per-screen
 * theme comparisons.
 */
@Composable
@ReadOnlyComposable
fun screenTokens(): ThemeTokens {
    val c = LocalThemeTokens.current
    return c
}
