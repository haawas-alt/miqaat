package com.usman.miqaat.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    BoxWithConstraints(modifier.testTag("approved-dark-hero")
        .semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() },
        contentAlignment = Alignment.Center) {
        val compact = maxHeight < 230.dp
        if (s.artTheme != ArtTheme.MINIMAL) {
            val art = when (s.theme) {
                AppTheme.KISWAH -> R.drawable.approved_kiswah_silk
                AppTheme.CELESTIAL_MERIDIAN -> R.drawable.celestial_landscape_approved
                else -> R.drawable.approved_miqaat_arch
            }
            Image(painterResource(art), null, Modifier.fillMaxSize(),
                contentScale = if (s.theme == AppTheme.CELESTIAL_MERIDIAN) ContentScale.Crop else ContentScale.FillBounds)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (kiswah) .26f else .38f)))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
                tk.background, Color.Transparent, Color.Black.copy(alpha = .24f), tk.background))))
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(
                tk.background.copy(alpha = .85f), Color.Transparent, Color.Transparent, tk.background.copy(alpha = .85f)))))
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(hero.special ?: hero.kickerLabel, fontFamily = body,
                fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = if (urdu) 0.sp else 1.6.sp,
                color = tk.primary, textAlign = TextAlign.Center, maxLines = 2)
            Text(hero.arabic, fontFamily = if (kiswah) ReemKufi else Amiri,
                fontSize = if (compact) 29.sp else 43.sp, lineHeight = if (compact) 38.sp else 57.sp,
                color = tk.arabicText, modifier = Modifier.testTag("approved-hero-arabic"))
            if (!urdu) Text(hero.label.uppercase(), fontFamily = Cinzel, fontSize = 10.sp,
                lineHeight = 14.sp, letterSpacing = 2.sp, color = tk.contentSecondary, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(hero.clock, fontFamily = ApprovedDisplay, fontWeight = FontWeight.Medium,
                    fontSize = if (compact) 62.sp else 80.sp, lineHeight = if (compact) 60.sp else 78.sp,
                    color = tk.contentPrimary, maxLines = 1, modifier = Modifier.testTag("approved-hero-clock"))
                if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = if (urdu) Amiri else ApprovedDisplay,
                    fontSize = 18.sp, color = tk.contentPrimary, maxLines = 1)
            }
            Row(Modifier.clip(RoundedCornerShape(12.dp)).background(tk.background.copy(alpha = .92f))
                .padding(horizontal = 9.dp, vertical = 3.dp).testTag("portrait-hero-status"),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.Schedule, null, Modifier.size(14.dp), tint = tk.primary)
                Text(hero.status, fontFamily = body, fontSize = 12.sp, lineHeight = if (urdu) 21.sp else 17.sp,
                    color = tk.contentPrimary, maxLines = 2, textAlign = TextAlign.Center)
            }
            Row(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(16.dp))
                .clickable(role = Role.Button, onClick = onDetails).testTag("portrait-hero-details")
                .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(Str[R.string.s_view_prayer_details], fontFamily = body, fontSize = 12.sp,
                    color = tk.contentPrimary, maxLines = 2)
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(16.dp), tint = tk.primary)
            }
            ApprovedSeasonal(state, s, tk)
        }
    }
}
