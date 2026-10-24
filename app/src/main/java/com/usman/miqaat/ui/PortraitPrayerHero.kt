package com.usman.miqaat.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.*

/** Explicit opt-in; landscape, tablets and Prayer Gallery retain their existing composition. */
@Composable
internal fun phonePortraitPolish(s: AppSettings): Boolean {
    val c = LocalConfiguration.current
    return c.orientation == Configuration.ORIENTATION_PORTRAIT &&
        minOf(c.screenWidthDp, c.screenHeightDp) < 600 && s.theme != AppTheme.PRAYER_GALLERY
}

internal fun portraitPrayerSurface(s: AppSettings) =
    if (s.theme == AppTheme.KISWAH) Color(0xFF101110) else Color(0xFF06172B)

@Composable
internal fun PortraitPrayerHero(state: PrayerState, s: AppSettings, tk: ThemeTokens, hero: HeroInfo,
    modifier: Modifier, onDetails: () -> Unit) {
    val kiswah = s.theme == AppTheme.KISWAH
    val urdu = L10n.isUrdu(s)
    val body = if (urdu) Amiri else ApprovedBody
    val nativeStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
    BoxWithConstraints(modifier.testTag("approved-dark-hero")
        .semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() },
        contentAlignment = Alignment.Center) {
        if (s.artTheme != ArtTheme.MINIMAL) {
            val art = when (s.theme) {
                AppTheme.KISWAH -> R.drawable.approved_kiswah_silk
                AppTheme.CELESTIAL_MERIDIAN -> R.drawable.celestial_landscape_approved
                else -> R.drawable.approved_miqaat_arch
            }
            Image(painterResource(art), null, Modifier.fillMaxSize(),
                contentScale = if (s.theme == AppTheme.CELESTIAL_MERIDIAN) ContentScale.Crop else ContentScale.FillBounds)
            // Preserve the scene's light and colour. Contrast is local to the central text.
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.radialGradient(
                    listOf(Color(0xFF020A15).copy(alpha = if (kiswah) .16f else .48f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width*.5f, size.height*.38f),
                    radius = size.width*.42f))
                drawRect(Brush.verticalGradient(0f to tk.background.copy(alpha = .24f),
                    .16f to Color.Transparent, .76f to Color.Transparent, 1f to tk.background))
                drawRect(Brush.horizontalGradient(0f to tk.background.copy(alpha = .32f),
                    .12f to Color.Transparent, .88f to Color.Transparent, 1f to tk.background.copy(alpha = .32f)))
            }
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 4.dp).testTag("portrait-hero-content"),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            BoxWithConstraints(Modifier.weight(1f, fill = false).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val compact = maxHeight < 176.dp
                val micro = maxHeight < (if (kiswah) 118.dp else 126.dp)
                val tiny = maxHeight < 72.dp
                val caption: @Composable () -> Unit = {
                    Column(horizontalAlignment = Alignment.Start) {
                        if (tiny) Text(if (urdu) hero.kickerLabel else "${hero.kickerLabel} · ${hero.label}",
                        fontFamily = body, fontSize = 8.sp, lineHeight = if (urdu) 16.sp else 14.sp,
                        color = tk.primary, maxLines = 1, style = nativeStyle)
                        else Text(hero.special ?: hero.kickerLabel, fontFamily = body, fontSize = if (micro) 8.sp else 9.sp,
                        lineHeight = if (urdu) 18.sp else 13.sp, letterSpacing = if (urdu) 0.sp else 1.1.sp,
                        color = tk.primary, maxLines = 1, style = nativeStyle)
                        if (!urdu && !tiny) Text(hero.label.uppercase(), fontFamily = Cinzel,
                        fontSize = if (micro) 10.sp else 11.sp, lineHeight = 16.sp, letterSpacing = 1.5.sp,
                        color = tk.contentPrimary, maxLines = 1, style = nativeStyle)
                    }
                }
                val clock: @Composable () -> Unit = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Text(hero.clock, fontFamily = ApprovedDisplay, fontWeight = FontWeight.Medium,
                        fontSize = if (tiny) 32.sp else if (micro) 42.sp else if (compact) 52.sp else 68.sp,
                        lineHeight = if (tiny) 44.sp else if (micro) 58.sp else if (compact) 70.sp else 92.sp,
                        color = tk.contentPrimary, maxLines = 1, style = nativeStyle,
                        modifier = Modifier.testTag("approved-hero-clock"))
                        if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = if (urdu) Amiri else ApprovedDisplay,
                        fontSize = if (tiny) 11.sp else if (micro) 13.sp else if (compact) 15.sp else 18.sp,
                        color = tk.contentPrimary, maxLines = 1, style = nativeStyle)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (micro) {
                        // A tightly grouped fallback for short screens with an active reliability notice.
                        Row(Modifier.widthIn(max = 248.dp).fillMaxWidth().testTag("portrait-prayer-lockup"),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                caption()
                                Text(hero.arabic, fontFamily = if (kiswah) ReemKufi else Amiri, fontSize = if (tiny) 20.sp else 24.sp,
                                lineHeight = if (tiny) (if (kiswah) 32.sp else 38.sp) else if (kiswah) 38.sp else 46.sp, color = tk.arabicText, style = nativeStyle,
                                modifier = Modifier.testTag("approved-hero-arabic"))
                            }
                            clock()
                        }
                    } else {
                        Row(Modifier.testTag("portrait-prayer-lockup"), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                            Text(hero.arabic, fontFamily = if (kiswah) ReemKufi else Amiri,
                            fontSize = if (compact) 26.sp else 38.sp,
                            lineHeight = if (compact) (if (kiswah) 44.sp else 50.sp) else 70.sp,
                            color = tk.arabicText, style = nativeStyle, modifier = Modifier.testTag("approved-hero-arabic"))
                            caption()
                        }
                        clock()
                    }
                }
            }
            Row(Modifier.padding(top = 2.dp).clip(RoundedCornerShape(14.dp))
                .background(tk.background.copy(alpha = .78f)).padding(horizontal = 10.dp, vertical = 2.dp)
                .testTag("portrait-hero-status"), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.Schedule, null, Modifier.size(14.dp), tint = tk.primary)
                Text(hero.status, fontFamily = body, fontSize = 12.sp, lineHeight = if (urdu) 22.sp else 18.sp,
                    color = tk.contentPrimary, maxLines = 2, textAlign = TextAlign.Center, style = nativeStyle)
            }
            Box(Modifier.padding(top = 2.dp).heightIn(min = 48.dp).widthIn(min = 194.dp, max = 240.dp)
                .clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClick = onDetails)
                .testTag("portrait-hero-details"), contentAlignment = Alignment.Center) {
                Row(Modifier.heightIn(min = 34.dp).clip(RoundedCornerShape(18.dp))
                    .background(tk.background.copy(alpha = .72f))
                    .border(.75.dp, tk.primary.copy(alpha = .72f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(Str[R.string.s_view_prayer_details], fontFamily = body, fontSize = 12.sp,
                        color = tk.contentPrimary, maxLines = 2, style = nativeStyle)
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(16.dp), tint = tk.primary)
                }
            }
            ApprovedSeasonal(state, s, tk)
        }
    }
}
