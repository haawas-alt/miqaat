package com.usman.miqaat.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.ArtTheme
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    state: PrayerState,
    settings: AppSettings,
    onOpenTimetable: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLocation: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val u: Dp = maxWidth / 100          // 1 "unit" = 1% of screen width, like the mockup's cqw
        fun fs(x: Float): TextUnit = (u.value * x).sp
        val sky = skyFor(state.period)
        val top by animateColorAsState(sky.top, tween(1500), label = "top")
        val bottom by animateColorAsState(sky.bottom, tween(1500), label = "bottom")
        val glow by animateColorAsState(sky.glow, tween(1500), label = "glow")
        val starAlpha by animateFloatAsState(sky.stars, tween(1500), label = "stars")

        // Night dimming after Isha until Fajr
        val dim = settings.nightDim && (state.period == Prayer.ISHA) && !state.justPassed

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
            Glow(Modifier.fillMaxSize(), glow)
            Stars(Modifier.fillMaxSize(), starAlpha)
            if (settings.artTheme == ArtTheme.GEOMETRIC) GirihLattice(Modifier.fillMaxSize(), tile = u.value * 11f)
            if (settings.artTheme != ArtTheme.MINIMAL) {
                MihrabArch(
                    Modifier.align(Alignment.TopCenter).offset(y = u * 9).width(u * 44).height(u * 44)
                )
            }

            // ---- top bar
            Row(
                Modifier.fillMaxWidth().padding(horizontal = u * 3.6f, vertical = u * 3f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onOpenLocation).padding(u * 0.6f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 2.2f), tint = Palette.ivory)
                    Spacer(Modifier.width(u * 0.8f))
                    Text(settings.locationName, fontSize = fs(1.7f), fontWeight = FontWeight.SemiBold, color = Palette.ivory, fontFamily = Nunito)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        state.now.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH)),
                        fontSize = fs(1.6f), color = Palette.ivory, fontFamily = Nunito
                    )
                    if (settings.showHijri) {
                        val h = PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
                        Text(h.english + "  ·  " + h.arabic, fontSize = fs(1.9f), color = Palette.goldSoft, fontFamily = Amiri)
                    }
                    Row(Modifier.padding(top = u * 1.2f), horizontalArrangement = Arrangement.spacedBy(u * 1.2f)) {
                        IconChip(Icons.Outlined.CalendarMonth, u, onOpenTimetable)
                        IconChip(Icons.Outlined.Settings, u, onOpenSettings)
                    }
                }
            }

            // ---- hero
            Column(
                Modifier.align(Alignment.TopCenter).offset(y = u * 15.5f).width(u * 56),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.hero.arabic, fontFamily = Amiri, fontSize = fs(8.5f), lineHeight = fs(9.5f), color = Color(0xFFF6E7B8))
                Text(
                    state.hero.english.uppercase() + if (state.justPassed) "  ·  NOW" else "",
                    fontFamily = Cormorant, fontSize = fs(2.8f), letterSpacing = fs(0.7f), color = Palette.ivory.copy(alpha = 0.9f)
                )
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = u * 0.6f)) {
                    Text(
                        PrayerEngine.clock(state.heroTime, settings.use24h),
                        fontFamily = Cormorant, fontSize = fs(9.5f), lineHeight = fs(9.5f), color = Palette.ivory
                    )
                    val suf = PrayerEngine.suffix(state.heroTime, settings.use24h)
                    if (suf.isNotEmpty()) Text(" $suf", fontFamily = Cormorant, fontSize = fs(3.2f), letterSpacing = fs(0.3f), color = Palette.ivory, modifier = Modifier.padding(top = u * 1.4f))
                }
                StatePill(state, u)
                Text(
                    state.now.format(DateTimeFormatter.ofPattern(if (settings.use24h) "HH:mm" else "h:mm a", Locale.ENGLISH)),
                    fontSize = fs(1.5f), letterSpacing = fs(0.3f), color = Palette.ivory.copy(alpha = 0.7f), fontFamily = Nunito,
                    modifier = Modifier.padding(top = u * 1f)
                )
            }

            // ---- rail
            val shown = if (settings.showSunrise) Prayer.entries else Prayer.prayersOnly
            Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = u * 3.6f, vertical = u * 3f),
                horizontalArrangement = Arrangement.spacedBy(u * 1.2f)
            ) {
                shown.forEach { p ->
                    val t = state.today[p]
                    val done = !t.isAfter(state.now) && !(state.justPassed && p == state.hero)
                    val isNow = state.justPassed && p == state.hero
                    val isNext = !state.justPassed && p == state.hero && state.nextTime.toLocalDate() == state.now.toLocalDate()
                    PrayerCard(p, PrayerEngine.clock(t, settings.use24h), PrayerEngine.suffix(t, settings.use24h),
                        done = done, isNow = isNow, isNext = isNext, azaanOn = settings.azaanEnabled[p] == true, u = u,
                        modifier = Modifier.weight(1f))
                }
            }

            if (dim) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
        }
    }
}

@Composable
private fun IconChip(icon: androidx.compose.ui.graphics.vector.ImageVector, u: Dp, onClick: () -> Unit) {
    Box(
        Modifier.size(u * 3.6f).clip(CircleShape).background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, Modifier.size(u * 2f), tint = Palette.ivory) }
}

@Composable
private fun StatePill(state: PrayerState, u: Dp) {
    val dot = if (state.justPassed) Palette.mint else Palette.gold
    val text = if (state.justPassed) "azaan was ${PrayerEngine.humanDuration(state.delta)} ago"
    else "in ${PrayerEngine.humanDuration(state.delta)}"
    Row(
        Modifier.padding(top = u * 1.2f).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = u * 1.8f, vertical = u * 0.7f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(u * 0.9f).clip(CircleShape).background(dot))
        Spacer(Modifier.width(u * 0.8f))
        Text(text, fontSize = (u.value * 1.9f).sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory, fontFamily = Nunito)
    }
}

@Composable
fun PrayerCard(
    p: Prayer, time: String, suffix: String,
    done: Boolean, isNow: Boolean, isNext: Boolean, azaanOn: Boolean, u: Dp, modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(u * 1.6f)
    val bg = when { isNow -> Palette.gold.copy(alpha = 0.18f); p.isPrayer -> Color.White.copy(alpha = 0.07f); else -> Color.Transparent }
    val border = when { isNow -> Palette.gold; isNext -> Palette.gold.copy(alpha = 0.6f); else -> Color.White.copy(alpha = 0.12f) }
    Column(
        modifier.alpha(if (done) 0.55f else 1f).clip(shape).background(bg).border(1.dp, border, shape)
            .padding(horizontal = u * 1.5f, vertical = u * 1.5f),
        verticalArrangement = Arrangement.spacedBy(u * 0.35f)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(p.english.uppercase(), fontSize = (u.value * 1.35f).sp, letterSpacing = (u.value * 0.18f).sp, fontWeight = FontWeight.Bold, color = Palette.ivory.copy(alpha = 0.85f), fontFamily = Nunito)
            when {
                isNext -> Box(Modifier.clip(RoundedCornerShape(4.dp)).background(Palette.gold).padding(horizontal = u * 0.5f, vertical = u * 0.1f)) {
                    Text("NEXT", fontSize = (u.value * 1.05f).sp, fontWeight = FontWeight.Bold, color = Palette.night, fontFamily = Nunito)
                }
                done && p.isPrayer -> Icon(Icons.Outlined.Check, null, Modifier.size(u * 1.6f), tint = Palette.mint)
                p.isPrayer -> Icon(if (azaanOn) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null, Modifier.size(u * 1.6f), tint = Palette.ivory.copy(alpha = 0.7f))
            }
        }
        Text(p.arabic, fontFamily = Amiri, fontSize = (u.value * 2.2f).sp, lineHeight = (u.value * 2.4f).sp, color = Palette.goldSoft)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(time, fontFamily = Cormorant, fontSize = (u.value * 3f).sp, lineHeight = (u.value * 3.2f).sp, color = Palette.ivory)
            if (suffix.isNotEmpty()) Text(" $suffix", fontFamily = Cormorant, fontSize = (u.value * 1.5f).sp, color = Palette.ivory, modifier = Modifier.padding(bottom = u * 0.4f))
        }
    }
}
