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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.SystemUpdateAlt
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
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
import com.usman.miqaat.data.L10n
import androidx.compose.ui.text.font.FontFamily
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    state: PrayerState,
    settings: AppSettings,
    onOpenTimetable: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLocation: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenAdhkar: (AdhkarMode) -> Unit, onOpenFriday: () -> Unit = {}, onOpenLearn: () -> Unit = {},
    updateAvailable: Boolean = false,
    onOpenAbout: () -> Unit = {},
    onToggleRelative: () -> Unit = {}
) {
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // 1 unit = 1% of width, capped by height so short/wide tablets scale down instead of overlapping
        val u: Dp = minOf(maxWidth / 100, maxHeight / 56)
        fun fs(x: Float): TextUnit = (u.value * x).sp
        val sky = skyFor(state.period)
        val top by animateColorAsState(sky.top, tween(1500), label = "top")
        val bottom by animateColorAsState(sky.bottom, tween(1500), label = "bottom")
        val glow by animateColorAsState(sky.glow, tween(1500), label = "glow")
        val starAlpha by animateFloatAsState(sky.stars, tween(1500), label = "stars")
        val dim = settings.nightDim && state.period == Prayer.ISHA && !state.justPassed
        val F = uiFont(settings)
        val urdu = L10n.isUrdu(settings)

        val ramadan = PrayerEngine.isRamadan(settings, state.now.toLocalDate())
        val hij = PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
        val eidMorning = ((hij.month == 10 && hij.day == 1) || (hij.month == 12 && hij.day == 10)) && state.current != Prayer.DHUHR && state.current != Prayer.ASR && state.current != Prayer.MAGHRIB && state.current != Prayer.ISHA && state.current != null
        val oddNight = ramadan && hij.day >= 20 && hij.day % 2 == 1 && (state.current == Prayer.MAGHRIB || state.current == Prayer.ISHA)
        val friday = settings.fridayReminders && PrayerEngine.isJumuahWindow(settings, state.now)
        val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && settings.jumuahEnabled
        // adhkār window: after Fajr until Dhuhr (morning), after ʿAsr until Isha (evening)
        val morningWindow = state.current == Prayer.FAJR
        val eveningWindow = state.current == Prayer.ASR || state.current == Prayer.MAGHRIB

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
            Glow(Modifier.fillMaxSize(), glow)
            Stars(Modifier.fillMaxSize(), starAlpha)
            if (settings.artTheme == ArtTheme.GEOMETRIC) GirihLattice(Modifier.fillMaxSize(), tile = u.value * 11f)

            Column(Modifier.fillMaxSize().padding(start = u * 3.6f, end = u * 3.6f, top = u * 2.6f, bottom = u * 3.4f)) {

                // ---------- top bar
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Row(Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onOpenLocation).padding(u * 0.5f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 2.2f), tint = Palette.ivory)
                            Spacer(Modifier.width(u * 0.7f))
                            Text(settings.locationName, fontSize = fs(1.7f), fontWeight = FontWeight.SemiBold, color = Palette.ivory, fontFamily = F)
                        }
                        Row(Modifier.padding(top = u * 0.8f), horizontalArrangement = Arrangement.spacedBy(u * 0.9f)) {
                            if (settings.showQibla) {
                                val q = PrayerEngine.qibla(settings)
                                Chip(Icons.Outlined.Explore, "${L10n.word(settings, "Qibla")} ${q.toInt()}° ${PrayerEngine.compass(q)}", u, onClick = onOpenQibla)
                            }
                            if (settings.adhkarEnabled && morningWindow) Chip(Icons.Outlined.WbTwilight, L10n.word(settings, "Morning adhkār"), u, gold = true) { onOpenAdhkar(AdhkarMode.MORNING) }
                            if (settings.adhkarEnabled && eveningWindow) Chip(Icons.Outlined.WbTwilight, L10n.word(settings, "Evening adhkār"), u, gold = true) { onOpenAdhkar(AdhkarMode.EVENING) }
                            if (friday) Chip(Icons.Outlined.MenuBook, "Jumuʿah · al-Kahf · ṣalawāt", u, onClick = onOpenFriday)
                            if (settings.postPrayerAdhkar && state.current != null && state.justPassed.not() && java.time.Duration.between(state.today[state.current], state.now).toMinutes() in 5..40)
                                Chip(Icons.Outlined.WbTwilight, L10n.word(settings, "After-prayer adhkār"), u, gold = true) { onOpenAdhkar(AdhkarMode.POST) }
                            if (settings.postPrayerAdhkar && state.current != null && state.justPassed && java.time.Duration.between(state.today[state.current], state.now).toMinutes() >= 5)
                                Chip(Icons.Outlined.WbTwilight, L10n.word(settings, "After-prayer adhkār"), u, gold = true) { onOpenAdhkar(AdhkarMode.POST) }
                            if (ramadan && hij.day >= 27) Chip(Icons.Outlined.Info, "Zakāt al-Fiṭr before Eid prayer", u)
                            if (updateAvailable) Chip(Icons.Outlined.SystemUpdateAlt, L10n.word(settings, "Update available"), u, gold = true, onClick = onOpenAbout)
                            if (settings.travellerMode && PrayerEngine.isTravelling(settings)) Chip(Icons.Outlined.Flight, "Travelling · %.0f km from home".format(PrayerEngine.distanceKm(settings.homeLat!!, settings.homeLng!!, settings.latitude, settings.longitude)), u, gold = true, onClick = onOpenLocation)
                            if (state.today.fromMasjid) Chip(Icons.Outlined.LocationOn, settings.masjidName.ifBlank { "Masjid timetable" }, u, onClick = onOpenLocation)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(L10n.date(settings, state.now), fontSize = fs(if (urdu) 1.9f else 1.6f), color = Palette.ivory, fontFamily = F)
                        if (settings.showHijri) {
                            val h = PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
                            Text(L10n.hijri(settings, h) + (if (urdu) "" else "  ·  " + h.arabic), fontSize = fs(1.9f), color = Palette.goldSoft, fontFamily = if (urdu) F else Amiri)
                        }
                        Row(Modifier.padding(top = u * 1f), horizontalArrangement = Arrangement.spacedBy(u * 1.1f)) {
                            if (settings.kidsMode) IconChip(Icons.Outlined.MenuBook, u, onOpenLearn)
                            IconChip(Icons.Outlined.CalendarMonth, u, onOpenTimetable)
                            IconChip(Icons.Outlined.Settings, u, onOpenSettings)
                        }
                    }
                }

                // ---------- hero (takes whatever height is left)
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (settings.artTheme != ArtTheme.MINIMAL) {
                        MihrabArch(Modifier.fillMaxHeight(0.98f).aspectRatio(0.96f, matchHeightConstraintsFirst = true))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val kicker = when {
                            eidMorning -> "ʿĪd mubārak · اللهُ أكبر اللهُ أكبر لا إله إلا الله"
                            oddNight -> "Ramaḍān ${hij.day} · an odd night · seek Laylat al-Qadr"
                            ramadan && state.current == null -> "Ramaḍān · Suhoor ends at Fajr"
                            ramadan && state.hero == Prayer.MAGHRIB && !state.justPassed -> "Ramaḍān · Iftar"
                            ramadan && state.hero == Prayer.MAGHRIB && state.justPassed -> "Ramaḍān · Iftar time"
                            isFri && state.hero == Prayer.DHUHR -> "Jumuʿah"
                            else -> null
                        }
                        if (kicker != null) Text(kicker.uppercase(), fontFamily = Nunito, fontSize = fs(1.35f), letterSpacing = fs(0.3f), fontWeight = FontWeight.Bold, color = Palette.goldSoft, modifier = Modifier.padding(bottom = u * 0.6f))
                        Text(state.hero.arabic, fontFamily = Amiri, fontSize = fs(7.2f), lineHeight = fs(8f), color = Color(0xFFF6E7B8))
                        Text(
                            (if (isFri && state.hero == Prayer.DHUHR) L10n.word(settings, "Jumuʿah") else L10n.prayer(settings, state.hero)).let { if (urdu) it else it.uppercase() } + if (state.justPassed) "  ·  " + L10n.word(settings, "NOW") else "",
                            fontFamily = if (urdu) F else Cormorant, fontSize = fs(if (urdu) 3.2f else 2.6f), letterSpacing = if (urdu) 0.sp else fs(0.6f), color = Palette.ivory.copy(alpha = 0.9f)
                        )
                        Row(verticalAlignment = Alignment.Top) {
                            Text(PrayerEngine.clock(state.heroTime, settings.use24h), fontFamily = Cormorant, fontSize = fs(8.6f), lineHeight = fs(8.6f), color = Palette.ivory)
                            val suf = PrayerEngine.suffix(state.heroTime, settings.use24h)
                            if (suf.isNotEmpty()) Text(" $suf", fontFamily = Cormorant, fontSize = fs(3f), letterSpacing = fs(0.3f), color = Palette.ivory, modifier = Modifier.padding(top = u * 1.3f))
                        }
                        StatePill(state, u, settings)
                        state.current?.let { cur ->
                            PrayerEngine.iqamah(settings, state.today, cur)?.takeIf { it.isAfter(state.now) }?.let { iq ->
                                Text(
                                    "${L10n.iqamahIn(settings, Duration.between(state.now, iq))}  ·  ${PrayerEngine.clock(iq, settings.use24h)} ${PrayerEngine.suffix(iq, settings.use24h)}",
                                    fontFamily = F, fontSize = fs(1.6f), fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.padding(top = u * 0.8f)
                                )
                            }
                        }
                        if (ramadan && state.current != null && state.current != Prayer.MAGHRIB && state.current != Prayer.ISHA) {
                            FastProgress(state, u)
                        }
                        if (ramadan && (state.current == Prayer.ISHA || state.current == Prayer.MAGHRIB || state.current == null)) {
                            val lt = PrayerEngine.lastThird(settings, state.today)
                            Text("Tarāwīḥ ${PrayerEngine.clock(state.today[Prayer.ISHA].plusMinutes(settings.tarawihMinutesAfterIsha.toLong()), settings.use24h)}  ·  last third of the night from ${PrayerEngine.clock(lt, settings.use24h)} ${PrayerEngine.suffix(lt, settings.use24h)}",
                                fontFamily = Nunito, fontSize = fs(1.3f), color = Palette.goldSoft.copy(alpha = 0.9f), modifier = Modifier.padding(top = u * 0.8f))
                        }
                        Text(
                            state.now.format(DateTimeFormatter.ofPattern(if (settings.use24h) "HH:mm" else "h:mm a", Locale.ENGLISH)),
                            fontSize = fs(1.5f), letterSpacing = fs(0.3f), color = Palette.ivory.copy(alpha = 0.7f), fontFamily = Nunito, modifier = Modifier.padding(top = u * 0.8f)
                        )
                    }
                }
                if (settings.showDisliked) {
                    DayTimeline(settings, state.today, state.now, height = u * 0.7f, modifier = Modifier.fillMaxWidth().padding(start = u * 1, end = u * 1, bottom = u * 0.5f))
                }

                // ---------- rail
                val shown = if (settings.showSunrise) Prayer.entries else Prayer.prayersOnly
                Row(Modifier.fillMaxWidth().padding(top = u * 1.2f), horizontalArrangement = Arrangement.spacedBy(u * 1.2f)) {
                    shown.forEach { p ->
                        val t = PrayerEngine.rowTime(state, p)
                        val done = !t.isAfter(state.now) && !(state.justPassed && p == state.hero)
                        val isNow = state.justPassed && p == state.hero
                        val isNext = !state.justPassed && p == state.hero && state.nextTime.toLocalDate() == state.now.toLocalDate()
                        val label = when {
                            p == Prayer.DHUHR && isFri -> L10n.word(settings, "Jumuʿah")
                            ramadan && p == Prayer.FAJR -> L10n.word(settings, "Fajr · Suhoor")
                            ramadan && p == Prayer.MAGHRIB -> L10n.word(settings, "Maghrib · Iftar")
                            else -> L10n.prayer(settings, p)
                        }
                        val iq = PrayerEngine.iqamah(settings, state.today, p)
                        val endT = if (settings.showEndTimes) PrayerEngine.endOf(settings, state.today, p) else null
                        PrayerCard(p, label,
                            if (settings.showRelative) L10n.relative(settings, t, state.now) else PrayerEngine.clock(t, settings.use24h),
                            if (settings.showRelative) "" else PrayerEngine.suffix(t, settings.use24h),
                            done = done, isNow = isNow, isNext = isNext, azaanOn = settings.azaanEnabled[p] == true, u = u,
                            iqamah = iq?.let { PrayerEngine.clock(it, settings.use24h) }, relative = settings.showRelative,
                            ends = endT?.let { L10n.word(settings, "ends") + " " + PrayerEngine.clock(it, settings.use24h) }, onWhy = { why = p }, font = F, urdu = urdu,
                            modifier = Modifier.weight(1f).clickable(onClick = onToggleRelative))
                    }
                }
            }
            // signature
            Column(Modifier.align(Alignment.BottomStart).padding(start = u * 1.2f, bottom = u * 0.5f)) {
                Text(if (urdu) "ڈیزائن: UZR" else "Designed by UZR", fontFamily = if (urdu) F else Cormorant, fontSize = fs(1.45f), letterSpacing = fs(0.12f), color = Palette.goldSoft.copy(alpha = 0.75f))
                Text(if (urdu) "میرے لیے دعا کیجیے" else "Make duʿā for me", fontFamily = if (urdu) F else Cormorant, fontSize = fs(1.25f), fontStyle = FontStyle.Italic, color = Palette.ivory.copy(alpha = 0.5f))
            }
            if (dim) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
        }
    }
}

@Composable
private fun Chip(icon: ImageVector, label: String, u: Dp, gold: Boolean = false, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.clip(shape).background(if (gold) Palette.gold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f))
            .border(1.dp, if (gold) Palette.gold.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.16f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = u * 1.1f, vertical = u * 0.45f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(u * 1.5f), tint = if (gold) Palette.goldSoft else Palette.ivory)
        Spacer(Modifier.width(u * 0.6f))
        Text(label, fontSize = (u.value * 1.3f).sp, fontWeight = FontWeight.SemiBold, color = if (gold) Palette.goldSoft else Palette.ivory, fontFamily = Nunito)
    }
}

@Composable
private fun IconChip(icon: ImageVector, u: Dp, onClick: () -> Unit) {
    Box(
        Modifier.size(u * 3.6f).clip(CircleShape).background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, Modifier.size(u * 2f), tint = Palette.ivory) }
}

@Composable
private fun StatePill(state: PrayerState, u: Dp, settings: AppSettings) {
    val dot = if (state.justPassed) Palette.mint else Palette.gold
    val text = if (state.justPassed) L10n.ago(settings, state.delta) else L10n.inFor(settings, state.delta)
    Row(
        Modifier.padding(top = u * 1f).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50)).padding(horizontal = u * 1.8f, vertical = u * 0.65f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(u * 0.9f).clip(CircleShape).background(dot))
        Spacer(Modifier.width(u * 0.8f))
        Text(text, fontSize = (u.value * (if (L10n.isUrdu(settings)) 2.1f else 1.8f)).sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory, fontFamily = uiFont(settings))
    }
}

/** Thin bar: how far through today's fast we are (Fajr → Maghrib). */
@Composable
private fun FastProgress(state: PrayerState, u: Dp) {
    val start = state.today[Prayer.FAJR]; val end = state.today[Prayer.MAGHRIB]
    val total = Duration.between(start, end).toMinutes().coerceAtLeast(1)
    val done = Duration.between(start, state.now).toMinutes().coerceIn(0, total)
    Column(Modifier.padding(top = u * 1f).width(u * 34), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(u * 0.45f).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f))) {
            Box(Modifier.fillMaxWidth(done / total.toFloat()).fillMaxHeight().background(Palette.gold))
        }
        Text("Iftar in ${PrayerEngine.humanDuration(Duration.between(state.now, end))}", fontFamily = Nunito, fontSize = (u.value * 1.25f).sp, color = Palette.goldSoft, modifier = Modifier.padding(top = u * 0.4f), textAlign = TextAlign.Center)
    }
}

@Composable
fun PrayerCard(
    p: Prayer, label: String, time: String, suffix: String,
    done: Boolean, isNow: Boolean, isNext: Boolean, azaanOn: Boolean, u: Dp, iqamah: String? = null, relative: Boolean = false,
    ends: String? = null, onWhy: (() -> Unit)? = null, font: FontFamily = Nunito, urdu: Boolean = false, modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(u * 1.6f)
    val bg = when { isNow -> Palette.gold.copy(alpha = 0.18f); p.isPrayer -> Color.White.copy(alpha = 0.07f); else -> Color.Transparent }
    val border = when { isNow -> Palette.gold; isNext -> Palette.gold.copy(alpha = 0.6f); else -> Color.White.copy(alpha = 0.12f) }
    Column(
        modifier.alpha(if (done) 0.55f else 1f).clip(shape).background(bg).border(1.dp, border, shape).padding(horizontal = u * 1.4f, vertical = u * 1.3f),
        verticalArrangement = Arrangement.spacedBy(u * 0.3f)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (urdu) label else label.uppercase(), fontSize = (u.value * (if (urdu) 1.6f else 1.2f)).sp, letterSpacing = if (urdu) 0.sp else (u.value * 0.14f).sp, fontWeight = FontWeight.Bold, color = Palette.ivory.copy(alpha = 0.85f), fontFamily = font, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
            when {
                isNext -> Box(Modifier.padding(start = u * 0.4f).clip(RoundedCornerShape(4.dp)).background(Palette.gold).padding(horizontal = u * 0.5f, vertical = u * 0.1f)) {
                    Text("NEXT", fontSize = (u.value * 1.0f).sp, fontWeight = FontWeight.Bold, color = Palette.night, fontFamily = Nunito)
                }
                done && p.isPrayer -> Icon(Icons.Outlined.Check, null, Modifier.size(u * 1.6f), tint = Palette.mint)
                p.isPrayer -> Icon(if (azaanOn) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null, Modifier.size(u * 1.6f), tint = Palette.ivory.copy(alpha = 0.7f))
            }
            if (onWhy != null) Icon(Icons.Outlined.Info, "Why this time?", Modifier.padding(start = u * 0.5f).size(u * 1.6f).clickable(onClick = onWhy), tint = Palette.ivory.copy(alpha = 0.55f))
        }
        Text(p.arabic, fontFamily = Amiri, fontSize = (u.value * 2.1f).sp, lineHeight = (u.value * 2.3f).sp, color = Palette.goldSoft)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(time, fontFamily = if (relative) font else Cormorant, fontWeight = if (relative) FontWeight.SemiBold else FontWeight.Normal, fontSize = (u.value * (if (relative) 1.7f else 2.9f)).sp, lineHeight = (u.value * 3f).sp, color = Palette.ivory, maxLines = 1)
            if (suffix.isNotEmpty()) Text(" $suffix", fontFamily = Cormorant, fontSize = (u.value * 1.4f).sp, color = Palette.ivory, modifier = Modifier.padding(bottom = u * 0.35f))
        }
        if (ends != null || iqamah != null) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(u * 0.8f)) {
            if (ends != null) Text(ends, fontFamily = font, fontSize = (u.value * 1.05f).sp, color = Palette.ivory.copy(alpha = 0.6f), maxLines = 1)
            if (iqamah != null) Row(verticalAlignment = Alignment.CenterVertically) {
                Text("IQ ", fontFamily = Nunito, fontSize = (u.value * 1.05f).sp, letterSpacing = (u.value * 0.1f).sp, fontWeight = FontWeight.Bold, color = Palette.ivory.copy(alpha = 0.6f))
                Text(iqamah, fontFamily = Nunito, fontSize = (u.value * 1.2f).sp, fontWeight = FontWeight.Bold, color = Palette.goldSoft, maxLines = 1)
            }
        }
    }
}
