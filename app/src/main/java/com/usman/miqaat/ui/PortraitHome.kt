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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.usman.miqaat.R
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
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
import com.usman.miqaat.data.L10n
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Phone (portrait) home. Same information as the tablet, stacked: hero on top, prayers as rows. */
@Composable
fun PortraitHome(
    state: PrayerState, settings: AppSettings,
    onOpenTimetable: () -> Unit, onOpenSettings: () -> Unit, onOpenLocation: () -> Unit,
    onOpenQibla: () -> Unit, onOpenAdhkar: (AdhkarMode) -> Unit, onOpenFriday: () -> Unit = {}, onOpenLearn: () -> Unit = {}, updateAvailable: Boolean, onOpenAbout: () -> Unit,
    onToggleRelative: () -> Unit = {}
) {
    val kiswah = settings.theme == AppTheme.KISWAH
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val u: Dp = minOf(maxWidth / 100, maxHeight / 235)
        val density = androidx.compose.ui.platform.LocalDensity.current
        val fontScale = density.fontScale
        // Labels and the countdown follow the Android font-size setting; the huge display numerals and Arabic
        // are already sized to the screen, so they stay put — otherwise they push everything else off the page.
        fun fs(x: Float) = (u.value * x).sp
        fun fd(x: Float) = with(density) { (u * x).toSp() }
        val roomy = fontScale > 1.15f   // large text: let the page scroll rather than clip
        val sky = skyFor(state.period)
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val alarmsOk = remember(state.now.toLocalDate(), state.hero) { com.usman.miqaat.data.Reliability.allGood(ctx) }
        val top by animateColorAsState(if (kiswah) Color(0xFF0B0B0B) else sky.top, tween(1500), label = "t")
        val bottom by animateColorAsState(if (kiswah) Kiswah.silk else sky.bottom, tween(1500), label = "b")
        val gold = if (kiswah) Kiswah.threadSoft else Palette.goldSoft
        val ivory = if (kiswah) Kiswah.ivory else Palette.ivory
        val arabicFont: FontFamily = if (kiswah) ReemKufi else Amiri
        val numFont: FontFamily = if (kiswah) Cinzel else Cormorant
        val ramadan = PrayerEngine.isRamadan(settings, state.now.toLocalDate())
        val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && settings.jumuahEnabled
        val F = uiFont(settings)
        val urdu = L10n.isUrdu(settings)

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
            if (kiswah) Weave(Modifier.fillMaxSize()) else { Glow(Modifier.fillMaxSize(), sky.glow); Stars(Modifier.fillMaxSize(), sky.stars); GirihLattice(Modifier.fillMaxSize(), tile = u.value * 22f); DaySkyScrim(state.period) }

            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = u * 5, vertical = u * 2).then(if (roomy) Modifier.verticalScroll(rememberScrollState()) else Modifier)) {
                // top bar
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onOpenLocation, role = androidx.compose.ui.semantics.Role.Button).heightIn(min = 48.dp).semantics(mergeDescendants = true) { contentDescription = "Location: ${settings.locationName}. Opens location settings" }, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 4), tint = ivory)
                        Spacer(Modifier.width(u * 1.2f))
                        Text(settings.locationName, fontSize = fs(3.6f), fontWeight = FontWeight.SemiBold, color = ivory, fontFamily = Nunito)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(u * 2)) {
                        if (settings.kidsMode) RoundIcon(Icons.Outlined.MenuBook, u, ivory, onOpenLearn, "Learn Salah")
                        RoundIcon(Icons.Outlined.CalendarMonth, u, ivory, onOpenTimetable, "Monthly timetable")
                        RoundIcon(Icons.Outlined.Settings, u, ivory, onOpenSettings, "Settings")
                    }
                }
                Text(L10n.date(settings, state.now), fontSize = fs(if (urdu) 3.8f else 3.2f), color = ivory.copy(alpha = 0.85f), fontFamily = F, modifier = Modifier.padding(top = u * 2))
                if (settings.showHijri) {
                    val h = PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(L10n.hijri(settings, h), fontSize = fs(3.8f), color = gold, fontFamily = if (urdu) F else arabicFont)
                        if (!urdu) { Text("  ·  ", fontSize = fs(3.8f), color = gold, fontFamily = arabicFont); Text(L10n.iso(h.arabic), fontSize = fs(3.8f), color = gold, fontFamily = arabicFont) }
                    }
                }

                // chips
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(Modifier.padding(top = u * 2.5f), horizontalArrangement = Arrangement.spacedBy(u * 2), verticalArrangement = Arrangement.spacedBy(u * 1.5f)) {
                    if (settings.showQibla) { val q = PrayerEngine.qibla(settings); SmallChip(Icons.Outlined.Explore, L10n.word(settings, "Qibla") + " " + L10n.iso("${q.toInt()}° ${PrayerEngine.compass(q)}"), u, ivory, gold, font = F, onClick = onOpenQibla) }
                    if (settings.adhkarEnabled && state.current == Prayer.FAJR) SmallChip(Icons.Outlined.WbTwilight, L10n.word(settings, "Morning adhkār"), u, ivory, gold, gold = true, font = F) { onOpenAdhkar(AdhkarMode.MORNING) }
                    if (settings.adhkarEnabled && (state.current == Prayer.ASR || state.current == Prayer.MAGHRIB)) SmallChip(Icons.Outlined.WbTwilight, L10n.word(settings, "Evening adhkār"), u, ivory, gold, gold = true, font = F) { onOpenAdhkar(AdhkarMode.EVENING) }
                    if (updateAvailable) SmallChip(Icons.Outlined.Settings, "Update", u, ivory, gold, gold = true, font = F, onClick = onOpenAbout)
                    if (!alarmsOk) SmallChip(Icons.Outlined.Info, Str[R.string.s_azaan_may_be_late_fix], u, ivory, gold, gold = true, font = F, onClick = onOpenSettings)
                    if (settings.zoneNeedsReview) SmallChip(Icons.Outlined.Info, Str[R.string.s_time_zone_needs_checking], u, ivory, gold, gold = true, font = F, onClick = onOpenLocation)
                    if (settings.fridayReminders && PrayerEngine.isJumuahWindow(settings, state.now)) SmallChip(Icons.Outlined.Check, L10n.word(settings, "Jumuʿah"), u, ivory, gold, font = F, onClick = onOpenFriday)
                    if (settings.postPrayerAdhkar && state.current != null && java.time.Duration.between(state.today[state.current], state.now).toMinutes() in 5..40) SmallChip(Icons.Outlined.WbTwilight, L10n.word(settings, "After-prayer adhkār"), u, ivory, gold, gold = true, font = F) { onOpenAdhkar(AdhkarMode.POST) }
                }

                // hero takes whatever height is left between the header and the list
                Box((if (roomy) Modifier.fillMaxWidth().padding(vertical = u * 3) else Modifier.weight(1f).fillMaxWidth()).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    val kicker = when {
                        ramadan && state.current == null -> "Ramaḍān · Suhoor ends"
                        ramadan && state.hero == Prayer.MAGHRIB -> "Ramaḍān · Iftar"
                        isFri && state.hero == Prayer.DHUHR -> "Jumuʿah"
                        else -> null
                    }
                    if (kicker != null) Text(kicker.uppercase(), fontFamily = Nunito, fontSize = fs(2.8f), letterSpacing = fs(0.6f), fontWeight = FontWeight.Bold, color = gold)
                    Text(state.hero.arabic, fontFamily = arabicFont, fontSize = fd(if (kiswah) 10f else 13f), lineHeight = fd(15f), color = Color(0xFFF6E7B8))
                    Text(L10n.prayer(settings, state.hero).let { if (urdu) it else it.uppercase() } + if (state.justPassed) "  ·  " + L10n.word(settings, "NOW") else "", fontFamily = if (urdu) F else numFont, fontSize = fs(if (urdu) 6f else 5f), letterSpacing = if (urdu) 0.sp else fs(1.2f), color = ivory.copy(alpha = 0.9f))
                    Row(verticalAlignment = Alignment.Top) {
                        Text(PrayerEngine.clock(state.heroTime, settings.use24h), style = TextStyle(fontFamily = numFont, fontSize = fd(if (kiswah) 19f else 21f), lineHeight = fd(21f), brush = if (kiswah) Kiswah.goldText else Brush.verticalGradient(listOf(ivory, ivory))))
                        val suf = PrayerEngine.suffix(state.heroTime, settings.use24h)
                        if (suf.isNotEmpty()) Text(" $suf", fontFamily = numFont, fontSize = fd(6f), color = ivory, modifier = Modifier.padding(top = u * 3))
                    }
                    val pill = if (state.justPassed) L10n.ago(settings, state.delta) else L10n.inFor(settings, state.delta)
                    Row(
                        Modifier.padding(top = u * 2).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.28f)).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50)).padding(horizontal = u * 4, vertical = u * 1.6f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(u * 2).clip(CircleShape).background(if (state.justPassed) Palette.mint else Palette.gold))
                        Spacer(Modifier.width(u * 2))
                        Text(pill, fontSize = fs(if (urdu) 4.4f else 3.8f), fontWeight = FontWeight.SemiBold, color = ivory, fontFamily = F)
                    }
                    state.current?.let { cur ->
                        PrayerEngine.iqamah(settings, state.today, cur)?.takeIf { it.isAfter(state.now) }?.let { iq ->
                            Text("${L10n.iqamahIn(settings, Duration.between(state.now, iq))}  ·  ${PrayerEngine.clock(iq, settings.use24h)} ${PrayerEngine.suffix(iq, settings.use24h)}", fontFamily = F, fontSize = fs(3.2f), fontWeight = FontWeight.SemiBold, color = gold, modifier = Modifier.padding(top = u * 2))
                        }
                    }
                }
                }

                if (settings.showDisliked) DayThread(settings, state.today, state.now, modifier = Modifier.fillMaxWidth().padding(start = u * 2, end = u * 2, bottom = u * 1.6f), kiswah = kiswah, labelSize = fs(2.4f))
                // prayer rows
                val shown = if (settings.showSunrise) Prayer.entries else Prayer.prayersOnly
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(u * 4)).background(Color.White.copy(alpha = if (kiswah) 0.04f else 0.07f)).border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(u * 4))) {
                    shown.forEachIndexed { i, p ->
                        val t = PrayerEngine.rowTime(state, p)
                        val done = !t.isAfter(state.now) && !(state.justPassed && p == state.hero)
                        val isNow = state.justPassed && p == state.hero
                        val isNext = !state.justPassed && p == state.hero && state.nextTime.toLocalDate() == state.now.toLocalDate()
                        val label = when { p == Prayer.DHUHR && isFri -> L10n.word(settings, "Jumuʿah"); ramadan && p == Prayer.FAJR -> L10n.word(settings, "Fajr · Suhoor"); ramadan && p == Prayer.MAGHRIB -> L10n.word(settings, "Maghrib · Iftar"); else -> L10n.prayer(settings, p) }
                        val iq = PrayerEngine.iqamah(settings, state.today, p)
                        val endT0 = if (settings.showEndTimes) PrayerEngine.endOf(settings, state.today, p) else null
                        val words = buildString {
                            append(label); append(", "); append(PrayerEngine.clock(t, settings.use24h)); append(' '); append(PrayerEngine.suffix(t, settings.use24h))
                            append(", "); append(L10n.relative(settings, t, state.now))
                            when { isNow -> append(", now"); isNext -> append(", next prayer"); done -> append(", passed") }
                            if (iq != null) { append(", iqamah "); append(PrayerEngine.clock(iq, settings.use24h)) }
                            if (endT0 != null) { append(", ends "); append(PrayerEngine.clock(endT0, settings.use24h)) }
                        }
                        Row(
                            Modifier.fillMaxWidth().background(if (isNow || isNext) Palette.gold.copy(alpha = 0.14f) else Color.Transparent).alpha(if (done) 0.65f else 1f)
                                .clickable(onClickLabel = "Switch between clock time and time until", role = androidx.compose.ui.semantics.Role.Button) { onToggleRelative() }.padding(horizontal = u * 4, vertical = u * 1.9f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f).semantics(mergeDescendants = true) { contentDescription = words }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (urdu) label else label.uppercase(), fontSize = fs(if (urdu) 4.2f else 3f), letterSpacing = if (urdu) 0.sp else fs(0.4f), fontWeight = FontWeight.Bold, color = ivory.copy(alpha = 0.85f), fontFamily = F)
                                    if (isNext) Box(Modifier.padding(start = u * 2).clip(RoundedCornerShape(4.dp)).background(Palette.gold).padding(horizontal = u * 1.2f, vertical = u * 0.3f)) { Text(Str[R.string.s_next_label], fontSize = fs(2.4f), fontWeight = FontWeight.Bold, color = Palette.night, fontFamily = Nunito) }
                                    if (done && p.isPrayer) Icon(Icons.Outlined.Check, null, Modifier.padding(start = u * 2).size(u * 3.6f), tint = Palette.mint)
                                }
                                Text(p.arabic, fontFamily = arabicFont, fontSize = fs(4f), lineHeight = fs(4.6f), color = gold)
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.clearAndSetSemantics { }) {
                                if (settings.showRelative) {
                                    Text(L10n.relative(settings, t, state.now), fontFamily = F, fontSize = fs(4.6f), fontWeight = FontWeight.SemiBold, color = ivory)
                                    Text(PrayerEngine.clock(t, settings.use24h) + " " + PrayerEngine.suffix(t, settings.use24h), fontFamily = Nunito, fontSize = fs(2.8f), color = Palette.textSecondary)
                                } else Row(verticalAlignment = Alignment.Bottom) {
                                    Text(PrayerEngine.clock(t, settings.use24h), fontFamily = numFont, fontSize = fd(6.2f), lineHeight = fd(6.6f), color = ivory)
                                    val s2 = PrayerEngine.suffix(t, settings.use24h)
                                    if (s2.isNotEmpty()) Text(" $s2", fontFamily = numFont, fontSize = fs(3.4f), color = ivory, modifier = Modifier.padding(bottom = u * 0.8f))
                                }
                                val endT = if (settings.showEndTimes) PrayerEngine.endOf(settings, state.today, p) else null
                                Row(horizontalArrangement = Arrangement.spacedBy(u * 2)) {
                                    if (endT != null) Text("${L10n.word(settings, "ends")} ${PrayerEngine.clock(endT, settings.use24h)}", fontFamily = F, fontSize = fs(2.6f), color = Palette.textMuted)
                                    if (iq != null) Text("${L10n.word(settings, "Iqamah")} ${PrayerEngine.clock(iq, settings.use24h)}", fontFamily = F, fontSize = fs(2.6f), fontWeight = FontWeight.Bold, color = gold.copy(alpha = 0.8f))
                                }
                            }
                            Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = androidx.compose.ui.semantics.Role.Button) { why = p }, contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Info, "Why this time?", Modifier.size(u * 4.2f), tint = Palette.textSecondary) }
                        }
                        if (i < shown.lastIndex) Box(Modifier.fillMaxWidth().padding(horizontal = u * 4).height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = u * 1.6f), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(L10n.word(settings, "Designed by UZR · Make duʿā for me"), fontFamily = if (urdu) F else numFont, fontSize = fs(2.8f), color = gold.copy(alpha = 0.9f))
                    Text(if (settings.showRelative) Str[R.string.s_tap_clock_times] else Str[R.string.s_tap_time_until_since], fontFamily = Nunito, fontSize = fs(2.4f), color = Palette.textMuted)
                }
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, u: Dp, tint: Color, onClick: () -> Unit, label: String) {
    Box(Modifier.size(maxOf(u * 9, 48.dp)).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)).border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape).clickable(onClick = onClick, role = androidx.compose.ui.semantics.Role.Button).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(u * 4.6f), tint = tint)
    }
}

@Composable
private fun SmallChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, u: Dp, ivory: Color, goldC: Color, gold: Boolean = false, font: FontFamily = Nunito, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier.clip(shape).background(if (gold) Palette.gold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)).border(1.dp, if (gold) Palette.gold.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.16f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = u * 2.6f, vertical = u * 1.2f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(u * 3.4f), tint = if (gold) goldC else ivory)
        Spacer(Modifier.width(u * 1.2f))
        Text(label, fontSize = (u.value * 3f).sp, fontWeight = FontWeight.SemiBold, color = if (gold) goldC else ivory, fontFamily = font, maxLines = 1)
    }
}
