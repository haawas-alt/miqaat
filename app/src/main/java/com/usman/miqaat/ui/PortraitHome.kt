package com.usman.miqaat.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WbTwilight
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Phone (portrait) home. Same information as the tablet, stacked: hero on top, prayers as rows. */
@Composable
fun PortraitHome(
    state: PrayerState, settings: AppSettings,
    onOpenTimetable: () -> Unit, onOpenSettings: () -> Unit, onOpenLocation: () -> Unit,
    onOpenQibla: () -> Unit, onOpenAdhkar: (Boolean) -> Unit, updateAvailable: Boolean, onOpenAbout: () -> Unit,
    onToggleRelative: () -> Unit = {}
) {
    val kiswah = settings.theme == AppTheme.KISWAH
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val u: Dp = minOf(maxWidth / 100, maxHeight / 205)
        fun fs(x: Float) = (u.value * x).sp
        val sky = skyFor(state.period)
        val top by animateColorAsState(if (kiswah) Color(0xFF0B0B0B) else sky.top, tween(1500), label = "t")
        val bottom by animateColorAsState(if (kiswah) Kiswah.silk else sky.bottom, tween(1500), label = "b")
        val gold = if (kiswah) Kiswah.threadSoft else Palette.goldSoft
        val ivory = if (kiswah) Kiswah.ivory else Palette.ivory
        val arabicFont: FontFamily = if (kiswah) ReemKufi else Amiri
        val numFont: FontFamily = if (kiswah) Cinzel else Cormorant
        val ramadan = PrayerEngine.isRamadan(settings, state.now.toLocalDate())
        val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && settings.jumuahEnabled

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
            if (kiswah) Weave(Modifier.fillMaxSize()) else { Glow(Modifier.fillMaxSize(), sky.glow); Stars(Modifier.fillMaxSize(), sky.stars); GirihLattice(Modifier.fillMaxSize(), tile = u.value * 22f) }

            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = u * 5, vertical = u * 2)) {
                // top bar
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onOpenLocation), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 4), tint = ivory)
                        Spacer(Modifier.width(u * 1.2f))
                        Text(settings.locationName, fontSize = fs(3.6f), fontWeight = FontWeight.SemiBold, color = ivory, fontFamily = Nunito)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(u * 2)) {
                        RoundIcon(Icons.Outlined.CalendarMonth, u, ivory, onOpenTimetable)
                        RoundIcon(Icons.Outlined.Settings, u, ivory, onOpenSettings)
                    }
                }
                Text(state.now.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH)), fontSize = fs(3.2f), color = ivory.copy(alpha = 0.85f), fontFamily = Nunito, modifier = Modifier.padding(top = u * 2))
                if (settings.showHijri) {
                    val h = PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
                    Text(h.english + "  ·  " + h.arabic, fontSize = fs(3.8f), color = gold, fontFamily = arabicFont)
                }

                // chips
                Row(Modifier.padding(top = u * 2.5f), horizontalArrangement = Arrangement.spacedBy(u * 2)) {
                    if (settings.showQibla) { val q = PrayerEngine.qibla(settings); SmallChip(Icons.Outlined.Explore, "Qibla ${q.toInt()}° ${PrayerEngine.compass(q)}", u, ivory, gold, onClick = onOpenQibla) }
                    if (settings.adhkarEnabled && state.current == Prayer.FAJR) SmallChip(Icons.Outlined.WbTwilight, "Morning adhkār", u, ivory, gold, gold = true) { onOpenAdhkar(true) }
                    if (settings.adhkarEnabled && (state.current == Prayer.ASR || state.current == Prayer.MAGHRIB)) SmallChip(Icons.Outlined.WbTwilight, "Evening adhkār", u, ivory, gold, gold = true) { onOpenAdhkar(false) }
                    if (updateAvailable) SmallChip(Icons.Outlined.Settings, "Update", u, ivory, gold, gold = true, onClick = onOpenAbout)
                }

                // hero takes whatever height is left between the header and the list
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    val kicker = when {
                        ramadan && state.current == null -> "Ramaḍān · Suhoor ends"
                        ramadan && state.hero == Prayer.MAGHRIB -> "Ramaḍān · Iftar"
                        isFri && state.hero == Prayer.DHUHR -> "Jumuʿah"
                        else -> null
                    }
                    if (kicker != null) Text(kicker.uppercase(), fontFamily = Nunito, fontSize = fs(2.8f), letterSpacing = fs(0.6f), fontWeight = FontWeight.Bold, color = gold)
                    Text(state.hero.arabic, fontFamily = arabicFont, fontSize = fs(if (kiswah) 10f else 13f), lineHeight = fs(15f), color = Color(0xFFF6E7B8))
                    Text(state.hero.english.uppercase() + if (state.justPassed) "  ·  NOW" else "", fontFamily = numFont, fontSize = fs(5f), letterSpacing = fs(1.2f), color = ivory.copy(alpha = 0.9f))
                    Row(verticalAlignment = Alignment.Top) {
                        Text(PrayerEngine.clock(state.heroTime, settings.use24h), style = TextStyle(fontFamily = numFont, fontSize = fs(if (kiswah) 19f else 21f), lineHeight = fs(21f), brush = if (kiswah) Kiswah.goldText else Brush.verticalGradient(listOf(ivory, ivory))))
                        val suf = PrayerEngine.suffix(state.heroTime, settings.use24h)
                        if (suf.isNotEmpty()) Text(" $suf", fontFamily = numFont, fontSize = fs(6f), color = ivory, modifier = Modifier.padding(top = u * 3))
                    }
                    val pill = if (state.justPassed) "azaan was ${PrayerEngine.humanDuration(state.delta)} ago" else "in ${PrayerEngine.humanDuration(state.delta)}"
                    Row(
                        Modifier.padding(top = u * 2).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.28f)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50)).padding(horizontal = u * 4, vertical = u * 1.6f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(u * 2).clip(CircleShape).background(if (state.justPassed) Palette.mint else Palette.gold))
                        Spacer(Modifier.width(u * 2))
                        Text(pill, fontSize = fs(3.8f), fontWeight = FontWeight.SemiBold, color = ivory, fontFamily = Nunito)
                    }
                    state.current?.let { cur ->
                        PrayerEngine.iqamah(settings, state.today, cur)?.takeIf { it.isAfter(state.now) }?.let { iq ->
                            Text("Iqamah in ${PrayerEngine.humanDuration(Duration.between(state.now, iq))}  ·  ${PrayerEngine.clock(iq, settings.use24h)} ${PrayerEngine.suffix(iq, settings.use24h)}", fontFamily = Nunito, fontSize = fs(3.2f), fontWeight = FontWeight.SemiBold, color = gold, modifier = Modifier.padding(top = u * 2))
                        }
                    }
                }
                }

                // prayer rows
                val shown = if (settings.showSunrise) Prayer.entries else Prayer.prayersOnly
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(u * 4)).background(Color.White.copy(alpha = if (kiswah) 0.04f else 0.07f)).border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(u * 4))) {
                    shown.forEachIndexed { i, p ->
                        val t = PrayerEngine.rowTime(state, p)
                        val done = !t.isAfter(state.now) && !(state.justPassed && p == state.hero)
                        val isNow = state.justPassed && p == state.hero
                        val isNext = !state.justPassed && p == state.hero && state.nextTime.toLocalDate() == state.now.toLocalDate()
                        val label = when { p == Prayer.DHUHR && isFri -> "Jumuʿah"; ramadan && p == Prayer.FAJR -> "Fajr · Suhoor"; ramadan && p == Prayer.MAGHRIB -> "Maghrib · Iftar"; else -> p.english }
                        val iq = PrayerEngine.iqamah(settings, state.today, p)
                        Row(
                            Modifier.fillMaxWidth().background(if (isNow || isNext) Palette.gold.copy(alpha = 0.14f) else Color.Transparent).alpha(if (done) 0.5f else 1f)
                                .clickable { onToggleRelative() }.padding(horizontal = u * 4, vertical = u * 1.9f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(label.uppercase(), fontSize = fs(3f), letterSpacing = fs(0.4f), fontWeight = FontWeight.Bold, color = ivory.copy(alpha = 0.85f), fontFamily = Nunito)
                                    if (isNext) Box(Modifier.padding(start = u * 2).clip(RoundedCornerShape(4.dp)).background(Palette.gold).padding(horizontal = u * 1.2f, vertical = u * 0.3f)) { Text("NEXT", fontSize = fs(2.4f), fontWeight = FontWeight.Bold, color = Palette.night, fontFamily = Nunito) }
                                    if (done && p.isPrayer) Icon(Icons.Outlined.Check, null, Modifier.padding(start = u * 2).size(u * 3.6f), tint = Palette.mint)
                                }
                                Text(p.arabic, fontFamily = arabicFont, fontSize = fs(4f), lineHeight = fs(4.6f), color = gold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                if (settings.showRelative) {
                                    Text(PrayerEngine.relative(t, state.now), fontFamily = Nunito, fontSize = fs(4.6f), fontWeight = FontWeight.SemiBold, color = ivory)
                                    Text(PrayerEngine.clock(t, settings.use24h) + " " + PrayerEngine.suffix(t, settings.use24h), fontFamily = Nunito, fontSize = fs(2.8f), color = ivory.copy(alpha = 0.6f))
                                } else Row(verticalAlignment = Alignment.Bottom) {
                                    Text(PrayerEngine.clock(t, settings.use24h), fontFamily = numFont, fontSize = fs(6.2f), lineHeight = fs(6.6f), color = ivory)
                                    val s2 = PrayerEngine.suffix(t, settings.use24h)
                                    if (s2.isNotEmpty()) Text(" $s2", fontFamily = numFont, fontSize = fs(3.4f), color = ivory, modifier = Modifier.padding(bottom = u * 0.8f))
                                }
                                if (iq != null) Text("Iqamah ${PrayerEngine.clock(iq, settings.use24h)}", fontFamily = Nunito, fontSize = fs(2.8f), fontWeight = FontWeight.Bold, color = gold.copy(alpha = 0.8f))
                            }
                        }
                        if (i < shown.lastIndex) Box(Modifier.fillMaxWidth().padding(horizontal = u * 4).height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = u * 1.6f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Designed by UZR · Make duʿā for me", fontFamily = numFont, fontSize = fs(2.8f), color = gold.copy(alpha = 0.55f))
                    Text(if (settings.showRelative) "tap: clock times" else "tap: time until / since", fontFamily = Nunito, fontSize = fs(2.4f), color = ivory.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, u: Dp, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(u * 9).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)).border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(u * 4.6f), tint = tint)
    }
}

@Composable
private fun SmallChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, u: Dp, ivory: Color, goldC: Color, gold: Boolean = false, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.clip(shape).background(if (gold) Palette.gold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)).border(1.dp, if (gold) Palette.gold.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.16f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = u * 2.6f, vertical = u * 1.2f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(u * 3.4f), tint = if (gold) goldC else ivory)
        Spacer(Modifier.width(u * 1.2f))
        Text(label, fontSize = (u.value * 3f).sp, fontWeight = FontWeight.SemiBold, color = if (gold) goldC else ivory, fontFamily = Nunito)
    }
}
