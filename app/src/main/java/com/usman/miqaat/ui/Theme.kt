package com.usman.miqaat.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.Prayer

object Palette {
    val ivory = Color(0xFFF3E9D2)
    val gold = Color(0xFFE3C36A)
    val goldSoft = Color(0xFFE9CF88)
    val goldDeep = Color(0xFFB8912F)
    val night = Color(0xFF0A1030)
    val panel = Color(0xFF0D1533)
    val panelRaised = Color(0xFF1B2340)
    val mint = Color(0xFF8FD3A7)
    val line = Color(0x1FFFFFFF)
    val lineStrong = Color(0x66FFFFFF)
    val glass = Color(0x12FFFFFF)

    // Semantic text tokens (solid colours, so contrast is predictable on the dark surfaces):
    // ivory 15.4:1, textSecondary ≈ 10:1, textMuted ≈ 7:1 on night/panel; textDisabled is for disabled controls only.
    val textSecondary = Color(0xFFCFC7B4)
    val textMuted = Color(0xFFAEA792)
    val textDisabled = Color(0xFF7F7A6C)
    /** Laid over the lower part of daytime skies so ivory text keeps ≥4.5:1 at the bright end of the gradient. */
    val scrim = Color(0xFF05070F)
}

/** Sky palette per prayer period: top, bottom, glow, star opacity. */
data class Sky(val top: Color, val bottom: Color, val glow: Color, val stars: Float)

fun skyFor(period: Prayer): Sky = when (period) {
    Prayer.FAJR -> Sky(Color(0xFF0B1B4A), Color(0xFF1B2E6B), Color(0xFF3F5FB8), 0.8f)
    Prayer.SUNRISE -> Sky(Color(0xFF1E3A6E), Color(0xFFC97B3A), Color(0xFFF3B65A), 0.15f)
    Prayer.DHUHR -> Sky(Color(0xFF7E5A17), Color(0xFFC9993A), Color(0xFFFFE08A), 0f)
    Prayer.ASR -> Sky(Color(0xFF6E3414), Color(0xFFB8702E), Color(0xFFF0A050), 0f)
    Prayer.MAGHRIB -> Sky(Color(0xFF3A1230), Color(0xFF7A2E4A), Color(0xFFD8607A), 0.35f)
    Prayer.ISHA -> Sky(Color(0xFF050A1E), Color(0xFF111A44), Color(0xFF2B3F8C), 1f)
}

val Amiri = FontFamily(Font(R.font.amiri_regular, FontWeight.Normal), Font(R.font.amiri_bold, FontWeight.Bold))
val Cormorant = FontFamily(Font(R.font.cormorant_garamond_medium, FontWeight.Medium))
val Nunito = FontFamily(Font(R.font.nunito_sans, FontWeight.Normal))
val Nastaliq = FontFamily(Font(R.font.noto_nastaliq_urdu, FontWeight.Normal))

/** True when the user has turned animations off in Android accessibility / developer settings. */
@androidx.compose.runtime.Composable
fun reduceMotion(): Boolean {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return androidx.compose.runtime.remember {
        runCatching { android.provider.Settings.Global.getFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }.getOrDefault(false)
    }
}

/** Darkens the lower part of a bright daytime sky so ivory text keeps ≥4.5:1 (WCAG AA) at the gradient's bright end. */
@androidx.compose.runtime.Composable
fun DaySkyScrim(period: Prayer, modifier: Modifier = Modifier.fillMaxSize()) {
    val day = period == Prayer.SUNRISE || period == Prayer.DHUHR || period == Prayer.ASR
    val k = if (day) 1f else 0.35f
    Box(modifier.background(androidx.compose.ui.graphics.Brush.verticalGradient(0f to Palette.scrim.copy(alpha = 0.10f * k), 0.30f to Palette.scrim.copy(alpha = 0.34f * k), 1f to Palette.scrim.copy(alpha = 0.62f * k))))
}

/** Body face for the chosen language: Nastaʿlīq for Urdu, Nunito otherwise. */
fun uiFont(s: com.usman.miqaat.data.AppSettings): FontFamily = if (s.language == com.usman.miqaat.data.Language.UR) Nastaliq else Nunito

private val scheme: ColorScheme = darkColorScheme(
    primary = Palette.gold,
    onPrimary = Palette.night,
    secondary = Palette.goldSoft,
    background = Palette.panel,
    onBackground = Palette.ivory,
    surface = Palette.panel,
    onSurface = Palette.ivory,
    surfaceVariant = Palette.panelRaised,
    onSurfaceVariant = Palette.ivory.copy(alpha = 0.7f),
    outline = Color(0x33FFFFFF)
)

private val type = Typography(
    displayLarge = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontSize = 96.sp),
    headlineMedium = TextStyle(fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontSize = 30.sp),
    titleLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = Nunito, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Nunito, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 1.5.sp),
    labelSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp)
)

@Composable
fun MiqaatTheme(theme: com.usman.miqaat.data.AppTheme = com.usman.miqaat.data.AppTheme.MIQAAT, content: @Composable () -> Unit) {
    // Material's own scheme stays the app-wide dark scheme for now (existing screens); the new tokens are provided alongside it.
    MaterialTheme(colorScheme = scheme, typography = type) { ProvideThemeTokens(theme, content) }
}
