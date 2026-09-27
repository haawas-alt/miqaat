package com.usman.miqaat.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    val glass = Color(0x12FFFFFF)
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
fun MiqaatTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = type, content = content)
}
