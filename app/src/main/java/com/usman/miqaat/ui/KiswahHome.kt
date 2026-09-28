package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState
import com.usman.miqaat.data.L10n
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

val ReemKufi = FontFamily(Font(R.font.reem_kufi, FontWeight.Normal))
val Cinzel = FontFamily(Font(R.font.cinzel, FontWeight.Normal))

object Kiswah {
    val silk = Color(0xFF070707)
    val thread = Color(0xFFC9A24B)
    val threadSoft = Color(0xFFE9CF88)
    val highlight = Color(0xFFFFF0BE)
    val ivory = Color(0xFFF3E9D2)
    val goldText = Brush.verticalGradient(listOf(Color(0xFFFFF0BE), Color(0xFFE3C36A), Color(0xFF9E7A22)))
    const val BAND = "لَا إِلَٰهَ إِلَّا ٱللَّٰهُ مُحَمَّدٌ رَسُولُ ٱللَّٰهِ   ✦   سُبْحَانَ ٱللَّٰهِ وَبِحَمْدِهِ   ✦   سُبْحَانَ ٱللَّٰهِ ٱلْعَظِيمِ"
}

/** Black silk, one gold Kufic band, an enormous time. Everything else whispers. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun KiswahHome(
    state: PrayerState, settings: AppSettings,
    onOpenTimetable: () -> Unit, onOpenSettings: () -> Unit, onOpenLocation: () -> Unit,
    onOpenQibla: () -> Unit, onOpenAdhkar: (AdhkarMode) -> Unit, onOpenFriday: () -> Unit = {}, onOpenLearn: () -> Unit = {}, updateAvailable: Boolean, onOpenAbout: () -> Unit,
    onToggleRelative: () -> Unit = {}
) {
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    BoxWithConstraints(Modifier.fillMaxSize().background(Kiswah.silk)) {
        val u: Dp = minOf(maxWidth / 100, maxHeight / 56)
        val short = maxHeight < 480.dp   // phone landscape: scroll, fixed-height hero, thread instead of arc
        fun fs(x: Float) = (u.value * x).sp
        val ramadan = PrayerEngine.isRamadan(settings, state.now.toLocalDate())
        val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && settings.jumuahEnabled
        val morningWindow = state.current == Prayer.FAJR
        val eveningWindow = state.current == Prayer.ASR || state.current == Prayer.MAGHRIB
        val minuteLeft = !state.justPassed && state.delta.toMinutes() < 1
        val urdu = L10n.isUrdu(settings)
        val F = if (urdu) Nastaliq else Cinzel

        Weave(Modifier.fillMaxSize())

        Column(Modifier.fillMaxSize().displayCutoutPadding().padding(horizontal = u * 4, vertical = if (short) u * 1.2f else u * 2)) {
            // top line
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(u * 2), verticalArrangement = Arrangement.spacedBy(u * 0.5f)) {
                    Caps(settings.locationName, fs(1.3f), Modifier.clickable(onClick = onOpenLocation, role = androidx.compose.ui.semantics.Role.Button).semantics { contentDescription = "Location: ${settings.locationName}. Opens location settings" }, alpha = 1f)
                    if (settings.showQibla) { val q = PrayerEngine.qibla(settings); Caps("· " + L10n.word(settings, "Qibla") + " " + L10n.iso("${q.toInt()}° ${PrayerEngine.compass(q)}"), fs(1.1f), Modifier.clickable(onClick = onOpenQibla, role = androidx.compose.ui.semantics.Role.Button), alpha = 0.85f) }
                    if (settings.adhkarEnabled && morningWindow) Caps("· " + L10n.word(settings, "Morning adhkār"), fs(1.1f), Modifier.clickable { onOpenAdhkar(AdhkarMode.MORNING) }, bright = true)
                    if (short) Caps(if (urdu) "· ڈیزائن: UZR · میرے لیے دعا کیجیے" else "· DESIGNED BY UZR · MAKE DUʿĀ FOR ME", fs(0.95f), Modifier.alpha(0.8f))
                    if (settings.adhkarEnabled && eveningWindow) Caps("· " + L10n.word(settings, "Evening adhkār"), fs(1.1f), Modifier.clickable { onOpenAdhkar(AdhkarMode.EVENING) }, bright = true)
                    if (updateAvailable) Caps("· " + L10n.word(settings, "Update available"), fs(1.1f), Modifier.clickable(onClick = onOpenAbout), bright = true)
                    if (settings.fridayReminders && PrayerEngine.isJumuahWindow(settings, state.now)) Caps("· " + L10n.word(settings, "Jumuʿah"), fs(1.1f), Modifier.clickable(onClick = onOpenFriday))
                    if (settings.postPrayerAdhkar && state.current != null && java.time.Duration.between(state.today[state.current], state.now).toMinutes() in 5..40) Caps("· " + L10n.word(settings, "After-prayer adhkār"), fs(1.1f), Modifier.clickable { onOpenAdhkar(AdhkarMode.POST) }, bright = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(u * 1.6f)) {
                    val h = PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
                    Caps((if (settings.showHijri) h.english + "  ·  " else "") + state.now.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)), fs(1.3f))
                    if (settings.kidsMode) Icon(androidx.compose.material.icons.Icons.Outlined.MenuBook, "Learn Salah", Modifier.size(maxOf(u * 2.6f, 48.dp)).clip(androidx.compose.foundation.shape.CircleShape).clickable(onClick = onOpenLearn, role = androidx.compose.ui.semantics.Role.Button).padding(maxOf(u * 0.5f, 10.dp)), tint = Kiswah.threadSoft)
                    Icon(Icons.Outlined.CalendarMonth, "Monthly timetable", Modifier.size(maxOf(u * 2.6f, 48.dp)).clip(androidx.compose.foundation.shape.CircleShape).clickable(onClick = onOpenTimetable, role = androidx.compose.ui.semantics.Role.Button).padding(maxOf(u * 0.5f, 10.dp)), tint = Kiswah.threadSoft)
                    Icon(Icons.Outlined.Settings, "Settings", Modifier.size(maxOf(u * 2.6f, 48.dp)).clip(androidx.compose.foundation.shape.CircleShape).clickable(onClick = onOpenSettings, role = androidx.compose.ui.semantics.Role.Button).padding(maxOf(u * 0.5f, 10.dp)), tint = Kiswah.threadSoft)
                }
            }

            // the band
            Box(Modifier.fillMaxWidth().padding(top = u * 2).height(u * 7f), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width * 0.0018f
                    drawLine(Kiswah.thread, Offset(0f, 0f), Offset(size.width, 0f), w)
                    drawLine(Kiswah.thread, Offset(0f, size.height), Offset(size.width, size.height), w)
                }
                Text(Kiswah.BAND, fontFamily = ReemKufi, fontSize = fs(2.9f), color = Color(0xFFE3C36A), maxLines = 1, softWrap = false, letterSpacing = fs(0.05f))
            }

            // hero
            Box(Modifier.weight(1f).fillMaxWidth().padding(vertical = if (short) u * 0.6f else 0.dp), contentAlignment = Alignment.Center) {
                FitHeight(Modifier.fillMaxSize()) { Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val kicker = when {
                        ramadan && state.current == null -> "Ramaḍān · Suhoor ends"
                        ramadan && state.hero == Prayer.MAGHRIB -> "Ramaḍān · Iftar"
                        isFri && state.hero == Prayer.DHUHR -> "Jumuʿah"
                        else -> L10n.prayer(settings, state.hero)
                    }
                    Text(if (urdu) kicker else kicker.uppercase(), fontFamily = F, fontSize = fs(2.1f), letterSpacing = fs(0.6f), color = Kiswah.threadSoft)
                    Text(state.hero.arabic, fontFamily = ReemKufi, fontSize = fs(5.4f), lineHeight = fs(7f), color = Color(0xFFF6E7B8))
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            PrayerEngine.clock(state.heroTime, settings.use24h),
                            style = TextStyle(fontFamily = Cinzel, fontSize = fs(13.5f), lineHeight = fs(13.5f), brush = Kiswah.goldText, letterSpacing = fs(-0.2f)),
                            modifier = Modifier.alpha(if (minuteLeft) 1f else 0.96f)
                        )
                        val suf = PrayerEngine.suffix(state.heroTime, settings.use24h)
                        if (suf.isNotEmpty()) Text(suf, fontFamily = Cinzel, fontSize = fs(2.8f), letterSpacing = fs(0.2f), color = Color(0xFFE3C36A), modifier = Modifier.padding(start = u * 0.8f, top = u * 2))
                    }
                    val pill = if (state.justPassed) L10n.ago(settings, state.delta) else L10n.inFor(settings, state.delta)
                    Box(Modifier.padding(top = u * 0.8f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            GoldRule(u * 26, u)
                            Text(if (urdu) pill else pill.uppercase(), fontFamily = F, fontSize = fs(if (urdu) 2f else 1.5f), letterSpacing = fs(0.3f), color = Kiswah.threadSoft, modifier = Modifier.padding(vertical = u * 0.6f))
                            GoldRule(u * 26, u)
                        }
                    }
                    state.current?.let { cur ->
                        PrayerEngine.iqamah(settings, state.today, cur)?.takeIf { it.isAfter(state.now) }?.let { iq ->
                            Text("IQAMAH IN ${PrayerEngine.humanDuration(Duration.between(state.now, iq)).uppercase()}  ·  ${PrayerEngine.clock(iq, settings.use24h)}", fontFamily = Cinzel, fontSize = fs(1.3f), letterSpacing = fs(0.25f), color = Kiswah.threadSoft.copy(alpha = 0.8f), modifier = Modifier.padding(top = u * 1))
                        }
                    }
                } }
            }

            if (settings.showDisliked) {
                DayThread(settings, state.today, state.now, modifier = Modifier.fillMaxWidth().padding(start = u * 2, end = u * 2, top = u * 0.6f, bottom = u * 1.2f), kiswah = true, labelSize = fs(1.0f))
            }
            // rail: a single gold line, then times
            GoldRule(null, u)
            val shown = if (settings.showSunrise) Prayer.entries else Prayer.prayersOnly
            Row(Modifier.fillMaxWidth().padding(top = u * 1.2f, bottom = u * 1.6f), horizontalArrangement = Arrangement.SpaceBetween) {
                shown.forEach { p ->
                    val t = PrayerEngine.rowTime(state, p)
                    val done = !t.isAfter(state.now) && !(state.justPassed && p == state.hero)
                    val next = p == state.hero && state.nextTime.toLocalDate() == state.now.toLocalDate()
                    val label = when { p == Prayer.DHUHR && isFri -> L10n.word(settings, "Jumuʿah"); ramadan && p == Prayer.FAJR -> L10n.word(settings, "Suhoor"); ramadan && p == Prayer.MAGHRIB -> L10n.word(settings, "Iftar"); else -> L10n.prayer(settings, p) }
                    val iq = PrayerEngine.iqamah(settings, state.today, p)
                    val words = buildString {
                        append(label); append(", "); append(PrayerEngine.clock(t, settings.use24h)); append(' '); append(PrayerEngine.suffix(t, settings.use24h))
                        append(", "); append(L10n.relative(settings, t, state.now)); if (next) append(", next prayer") else if (done) append(", passed")
                        if (iq != null) { append(", iqamah "); append(PrayerEngine.clock(iq, settings.use24h)) }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(if (done) 0.6f else 1f)
                        .combinedClickable(onClick = onToggleRelative, onLongClick = { why = p }, onClickLabel = "Switch between clock time and time until", onLongClickLabel = "Why this time?", role = androidx.compose.ui.semantics.Role.Button)
                        .semantics(mergeDescendants = true) { contentDescription = words }) {
                        Text(if (urdu) label else label.uppercase(), fontFamily = F, fontSize = fs(if (urdu) 1.6f else 1.15f), letterSpacing = fs(0.3f), color = Kiswah.threadSoft.copy(alpha = 0.75f))
                        if (settings.showRelative) Text(L10n.relative(settings, t, state.now).let { if (urdu) it else it.uppercase() }, fontFamily = F, fontSize = fs(1.5f), letterSpacing = fs(0.1f), color = if (next) Kiswah.highlight else Kiswah.ivory)
                        else Row(verticalAlignment = Alignment.Bottom) {
                            Text(PrayerEngine.clock(t, settings.use24h), fontFamily = Cinzel, fontSize = fs(2.4f), color = if (next) Kiswah.highlight else Kiswah.ivory)
                            val s = PrayerEngine.suffix(t, settings.use24h)
                            if (s.isNotEmpty()) Text(" $s", fontFamily = Cinzel, fontSize = fs(1.2f), color = if (next) Kiswah.highlight else Kiswah.ivory, modifier = Modifier.padding(bottom = u * 0.3f))
                        }
                        if (iq != null) Text("IQ ${PrayerEngine.clock(iq, settings.use24h)}", fontFamily = Cinzel, fontSize = fs(1.05f), letterSpacing = fs(0.15f), color = Kiswah.threadSoft.copy(alpha = 0.85f))
                    }
                }
            }
        }
        if (!short) Text(if (urdu) L10n.word(settings, "Designed by UZR · Make duʿā for me") else "DESIGNED BY UZR  ·  MAKE DUʿĀ FOR ME", fontFamily = if (urdu) Nastaliq else Cinzel, fontSize = fs(1.0f), letterSpacing = fs(0.3f), color = Kiswah.threadSoft.copy(alpha = 0.85f), modifier = Modifier.align(Alignment.BottomStart).padding(start = u * 4, bottom = u * 0.7f))
    }
}

@Composable
private fun Caps(t: String, size: androidx.compose.ui.unit.TextUnit, modifier: Modifier = Modifier, alpha: Float = 0.9f, bright: Boolean = false) {
    val urdu = t.any { it in '\u0600'..'\u06FF' }
    Text(if (urdu) t else t.uppercase(), fontFamily = if (urdu) Nastaliq else Nunito, fontSize = if (urdu) size * 1.25f else size, letterSpacing = if (urdu) 0.sp else size * 0.18f, fontWeight = FontWeight.Bold, maxLines = 1,
        color = if (bright) Kiswah.highlight else Kiswah.threadSoft.copy(alpha = alpha), modifier = modifier)
}

@Composable
private fun GoldRule(width: Dp?, u: Dp) {
    Box((if (width != null) Modifier.width(width) else Modifier.fillMaxWidth()).height(1.dp)
        .background(Brush.horizontalGradient(listOf(Color.Transparent, Kiswah.thread, Kiswah.thread, Color.Transparent))))
}

/** Very faint silk weave: two crossing hairline grids. */
@Composable
fun Weave(modifier: Modifier) {
    Canvas(modifier) {
        val step = 4f * density
        val c = Color.White.copy(alpha = 0.022f)
        var x = 0f; while (x < size.width) { drawLine(c, Offset(x, 0f), Offset(x, size.height), 1f); x += step }
        var y = 0f; while (y < size.height) { drawLine(c, Offset(0f, y), Offset(size.width, y), 1f); y += step }
    }
}
