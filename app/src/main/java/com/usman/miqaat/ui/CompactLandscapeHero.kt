package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import kotlin.math.cos
import kotlin.math.sin

/** Approved landscape concepts, confined to the short-phone reading layout.
 * The ring is a decorative frame, never a claimed fraction of a prayer interval.
 * Both before/after status and iqamah remain the engine's formatted live facts.
 */
@Composable
internal fun CompactLandscapeHero(
    state: PrayerState, s: AppSettings, actions: HomeActions, tk: ThemeTokens,
    hero: HeroInfo, shape: RoundedCornerShape, onDetails: () -> Unit
) {
    val gallery = s.theme == AppTheme.PRAYER_GALLERY
    val urdu = L10n.isUrdu(s)
    val font = if (urdu) tk.fontArabic else uiFont(s)
    Row(
        Modifier.fillMaxWidth().testTag("landscape-authored-hero").clip(shape)
            .background(tk.surface.copy(alpha = 0.94f)).border(1.dp, tk.divider, shape).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(0.58f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Column(Modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() }) {
                Text(hero.kickerLabel, fontFamily = font, fontSize = 12.sp, color = tk.contentSecondary)
                hero.special?.let { Text(it, fontFamily = font, fontSize = 12.sp, color = tk.accent) }
                Text(hero.label, modifier = Modifier.fillMaxWidth().testTag("landscape-hero-name"), fontFamily = if (urdu) font else tk.fontDisplay,
                    fontSize = 28.sp, color = tk.contentPrimary)
                if (!urdu) Text(hero.arabic, fontFamily = tk.fontArabic, fontSize = 24.sp, color = tk.accent)
                Text(L10n.iso("${hero.clock} ${hero.suffix}"), modifier = Modifier.fillMaxWidth().testTag("landscape-hero-clock"),
                    fontFamily = tk.fontDisplay, fontSize = 32.sp, color = tk.contentPrimary)
                // Celestial places the status in its dial once; Gallery keeps it beside the clock.
                if (gallery) Text(hero.status, modifier = Modifier.testTag("landscape-hero-status"),
                    fontFamily = font, fontSize = 13.sp, color = tk.accent)
            }
            Text(Str[R.string.s_view_prayer_details], fontFamily = font, fontSize = 14.sp, color = tk.primary,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("landscape-hero-details")
                    .clickable(role = Role.Button, onClick = onDetails).padding(vertical = 8.dp))
        }
        if (gallery) Column(Modifier.weight(0.42f), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.gallery_dawn_landscape), contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(6.dp))
                    .testTag("landscape-gallery-art"))
            if (s.kidsMode) Text(Str[R.string.s_learn_salah], fontFamily = font, fontSize = 13.sp,
                color = tk.primary, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("landscape-gallery-learn")
                    .clickable(role = Role.Button, onClick = actions.onOpenLearn).padding(vertical = 8.dp))
        } else Column(Modifier.weight(0.42f), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).testTag("landscape-countdown-dial"), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val radius = size.minDimension * 0.46f
                    val c = center
                    drawCircle(tk.primary.copy(alpha = 0.65f), radius, c, style = Stroke(2.dp.toPx()))
                    for (i in 0 until 12) {
                        val angle = Math.toRadians(i * 30.0 - 90.0)
                        val outer = Offset(c.x + cos(angle).toFloat() * radius, c.y + sin(angle).toFloat() * radius)
                        val inner = Offset(c.x + cos(angle).toFloat() * radius * 0.90f, c.y + sin(angle).toFloat() * radius * 0.90f)
                        drawLine(tk.primary.copy(alpha = 0.55f), inner, outer, 1.dp.toPx())
                    }
                }
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(L10n.duration(s, state.delta), modifier = Modifier.testTag("landscape-countdown-value"),
                        fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = 23.sp,
                        textAlign = TextAlign.Center, color = tk.contentPrimary)
                    Text(if (urdu) hero.kickerLabel else
                        (if (hero.justPassed) "Since ${hero.label}" else "Until ${hero.label}"),
                        fontFamily = font, fontSize = 12.sp, textAlign = TextAlign.Center, color = tk.primary)
                }
            }
            // Preserve pending iqamah separately without repeating the countdown.
            hero.status.substringAfter("  ·  ", "").takeIf { it.isNotBlank() }?.let { detail ->
                Text(detail, modifier = Modifier.testTag("landscape-hero-status"), fontFamily = font,
                    fontSize = 12.sp, textAlign = TextAlign.Center, color = tk.accent)
            }
        }
    }
}
