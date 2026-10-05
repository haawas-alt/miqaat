package com.usman.miqaat.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Phone held sideways (roughly 900 × 410 dp). Neither the tablet page nor the portrait page fits that shape, so
 * this is its own composition, laid out like a prayer hall seen from the door:
 *
 *   ┌ location · date ─────────────────────────── ▢ ▢ ▢ ┐
 *   │                                 │  FAJR     ✓  4:13 │
 *   │      المغرب                     │  SUNRISE     5:37 │
 *   │      MAGHRIB · NOW              │  DHUHR    ✓ 11:47 │
 *   │      5:57 PM                    │  ASR      ✓  3:16 │
 *   │      ● azaan was 37 min ago     │ ▌MAGHRIB  ◆  5:55 │
 *   │  ─◆──◆──────◆───────◆─····◆──◇  │  ISHA        7:14 │
 *   └ qibla · adhkār · signature ───────────────────────┘
 *
 * The mihrab (left) holds the one thing the screen exists for — the current prayer, its time and how far away
 * it is — at a size that reads from across a room. The right column is the day's six times, each row given an
 * equal share of the height so nothing can ever be clipped. Every unit is a percentage of the screen *height*,
 * the scarce dimension here, so the page scales on any phone without scrolling.
 */
@OptIn(ExperimentalFoundationApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun LandscapeHome(
    state: PrayerState, settings: AppSettings,
    onOpenTimetable: () -> Unit, onOpenSettings: () -> Unit, onOpenLocation: () -> Unit,
    onOpenQibla: () -> Unit, onOpenAdhkar: (AdhkarMode) -> Unit, onOpenFriday: () -> Unit, onOpenLearn: () -> Unit,
    updateAvailable: Boolean, onOpenAbout: () -> Unit, onToggleRelative: () -> Unit
) {
    val kiswah = settings.theme == AppTheme.KISWAH
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val u: Dp = maxHeight / 100                       // 1 unit = 1 % of height
        val density = androidx.compose.ui.platform.LocalDensity.current
        fun fs(x: Float) = maxOf(u.value * x, MIN_SP).sp                 // text that follows the user's font size
        fun fd(x: Float) = with(density) { (u * x).toSp() } // display text: fixed to the screen, never overflows
        val sky = skyFor(state.period)
        val top by animateColorAsState(if (kiswah) Color(0xFF0B0B0B) else sky.top, tween(1500), label = "t")
        val bottom by animateColorAsState(if (kiswah) Kiswah.silk else sky.bottom, tween(1500), label = "b")
        val gold = if (kiswah) Kiswah.threadSoft else Palette.goldSoft
        val ivory = if (kiswah) Kiswah.ivory else Palette.ivory
        val arabicFont = if (kiswah) ReemKufi else Amiri
        val numFont = if (kiswah) Cinzel else Cormorant
        val F = uiFont(settings)
        val urdu = L10n.isUrdu(settings)
        val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && settings.jumuahEnabled
        val hij = PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
        val dim = settings.nightDim && state.period == Prayer.ISHA && !state.justPassed

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
            if (kiswah) Weave(Modifier.fillMaxSize())
            else { Glow(Modifier.fillMaxSize(), sky.glow); Stars(Modifier.fillMaxSize(), sky.stars); GirihLattice(Modifier.fillMaxSize(), tile = u.value * 30f); DaySkyScrim(state.period) }

            Column(Modifier.fillMaxSize().displayCutoutPadding().padding(horizontal = u * 5, vertical = u * 2.5f)) {

                // ── header: one quiet line ─────────────────────────────────────────────────────────────
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onOpenLocation, role = Role.Button).padding(top = u * 1, bottom = u * 1, end = u * 2)
                            .semantics(mergeDescendants = true) { contentDescription = Str.get(R.string.s_a11y_location, L10n.iso(settings.locationName)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 4.2f), tint = gold)
                        Spacer(Modifier.width(u * 1.2f))
                        Text(settings.locationName, fontFamily = F, fontSize = fs(3.6f), fontWeight = FontWeight.SemiBold, color = ivory, maxLines = 1)
                    }
                    Spacer(Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(L10n.date(settings, state.now), fontFamily = F, fontSize = fs(2.9f), color = ivory.copy(alpha = 0.85f), maxLines = 1)
                        if (settings.showHijri) Text(L10n.hijri(settings, hij), fontFamily = if (urdu) F else Cormorant, fontSize = fs(3.1f), color = gold, maxLines = 1)
                    }
                    Spacer(Modifier.width(u * 4))
                    LandIcon(Icons.Outlined.MenuBook, u, ivory, onOpenLearn, Str[R.string.s_learn_salah])
                    LandIcon(Icons.Outlined.CalendarMonth, u, ivory, onOpenTimetable, Str[R.string.s_monthly_timetable])
                    LandIcon(Icons.Outlined.Settings, u, ivory, onOpenSettings, Str[R.string.s_settings])
                }

                // ── body: mihrab on the left, the day's times on the right ─────────────────────────────
                Row(Modifier.weight(1f).fillMaxWidth().padding(top = u * 1.5f)) {

                    // left · the hero
                    Column(Modifier.weight(1.2f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (!kiswah && settings.artTheme != com.usman.miqaat.data.ArtTheme.MINIMAL) MihrabArch(Modifier.fillMaxHeight(0.98f).aspectRatio(0.96f, matchHeightConstraintsFirst = true))
                            val heroWords = (if (state.justPassed) "${L10n.prayer(settings, state.hero)} was at " else "Next prayer ${L10n.prayer(settings, state.hero)} at ") +
                                PrayerEngine.clock(state.heroTime, settings.use24h) + " " + PrayerEngine.suffix(state.heroTime, settings.use24h) + ", " +
                                (if (state.justPassed) L10n.ago(settings, state.delta) else L10n.inForState(settings, state))
                            FitHeight(Modifier.fillMaxSize()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = heroWords; heading() }) {
                                    Text(state.hero.arabic, fontFamily = arabicFont, fontSize = fd(if (kiswah) 11f else 14f), lineHeight = fd(15f), color = Color(0xFFF6E7B8))
                                    Text(
                                        (if (isFri && state.hero == Prayer.DHUHR) L10n.word(settings, "Jumuʿah") else L10n.prayer(settings, state.hero)).let { if (urdu) it else it.uppercase() } +
                                            if (state.justPassed) "  ·  " + L10n.word(settings, "NOW") else "",
                                        fontFamily = if (urdu) F else numFont, fontSize = fs(if (urdu) 4.6f else 3.8f), letterSpacing = if (urdu) 0.sp else fs(1f), color = ivory.copy(alpha = 0.9f)
                                    )
                                    Row(verticalAlignment = Alignment.Top) {
                                        Text(PrayerEngine.clock(state.heroTime, settings.use24h), style = TextStyle(fontFamily = numFont, fontSize = fd(if (kiswah) 21f else 23f), lineHeight = fd(23f), brush = if (kiswah) Kiswah.goldText else Brush.verticalGradient(listOf(ivory, ivory))))
                                        val suf = PrayerEngine.suffix(state.heroTime, settings.use24h)
                                        if (suf.isNotEmpty()) Text(" $suf", fontFamily = numFont, fontSize = fd(6f), color = ivory, modifier = Modifier.padding(top = u * 3))
                                    }
                                    val pill = if (state.justPassed) L10n.ago(settings, state.delta) else L10n.inForState(settings, state)
                                    Row(
                                        Modifier.padding(top = u * 1.2f).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = if (kiswah) 0.35f else 0.28f)).border(1.dp, gold.copy(alpha = 0.35f), RoundedCornerShape(50)).padding(horizontal = u * 4, vertical = u * 1.3f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.size(u * 1.8f).clip(CircleShape).background(if (state.justPassed) Palette.mint else Palette.gold))
                                        Spacer(Modifier.width(u * 1.8f))
                                        Text(pill, fontFamily = F, fontSize = fs(if (urdu) 4f else 3.4f), fontWeight = FontWeight.SemiBold, color = ivory)
                                    }
                                    state.current?.let { cur ->
                                        PrayerEngine.iqamah(settings, state.today, cur)?.takeIf { it.isAfter(state.now) }?.let { iq ->
                                            Text("${L10n.iqamahIn(settings, Duration.between(state.now, iq))}  ·  ${PrayerEngine.clock(iq, settings.use24h)} ${PrayerEngine.suffix(iq, settings.use24h)}",
                                                fontFamily = F, fontSize = fs(2.9f), fontWeight = FontWeight.SemiBold, color = gold, modifier = Modifier.padding(top = u * 1.5f))
                                        }
                                    }
                                }
                            }
                        }
                        if (settings.showDisliked) DayThread(settings, state.today, state.now, modifier = Modifier.fillMaxWidth().padding(horizontal = u * 2, vertical = u * 1f), kiswah = kiswah, labelSize = fs(2.4f))
                        // quiet footer: the small doors, then the signature
                        androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth().padding(top = u * 0.8f), horizontalArrangement = Arrangement.spacedBy(u * 2.5f, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(u * 0.5f)) {
                            if (settings.showQibla) { val q = PrayerEngine.qibla(settings); Door(L10n.word(settings, "Qibla") + " " + L10n.iso("${q.toInt()}° ${PrayerEngine.compass(q)}"), u, ivory, F, onOpenQibla) }
                            if (AdhkarMode.MORNING in adhkarModes(state, settings)) Door(L10n.word(settings, "Morning adhkār"), u, gold, F) { onOpenAdhkar(AdhkarMode.MORNING) }
                            if (AdhkarMode.EVENING in adhkarModes(state, settings)) Door(L10n.word(settings, "Evening adhkār"), u, gold, F) { onOpenAdhkar(AdhkarMode.EVENING) }
                            if (AdhkarMode.POST in adhkarModes(state, settings)) Door(L10n.word(settings, "After-prayer adhkār"), u, gold, F) { onOpenAdhkar(AdhkarMode.POST) }
                            if (isFri) Door(L10n.word(settings, "Jumuʿah"), u, gold, F, onOpenFriday)
                            if (updateAvailable) Door(L10n.word(settings, "Update available"), u, gold, F, onOpenAbout)
                            if (settings.zoneNeedsReview) Door(Str[R.string.s_time_zone_needs_checking], u, gold, F, onOpenLocation)
                        }
                        Text(if (urdu) "ڈیزائن: UZR · میرے لیے دعا کیجیے" else "Designed by UZR · Make duʿā for me", fontFamily = if (urdu) F else Cormorant, fontSize = fs(2.5f), color = gold.copy(alpha = 0.7f), modifier = Modifier.padding(top = u * 0.8f))
                    }

                    Spacer(Modifier.width(u * 5))
                    Box(Modifier.width(1.dp).fillMaxHeight(0.9f).align(Alignment.CenterVertically).background(gold.copy(alpha = 0.25f)))
                    Spacer(Modifier.width(u * 5))

                    // right · the day's times, one equal row each
                    val shown = if (settings.showSunrise) Prayer.entries else Prayer.prayersOnly
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        shown.forEach { p ->
                            val t = state.today[p]
                            val done = state.current != null && p.ordinal <= state.current.ordinal && p != Prayer.SUNRISE && !(state.justPassed && p == state.hero)
                            val isHero = p == state.hero
                            val endT = if (settings.showEndTimes) PrayerEngine.endOf(settings, state.today, p) else null
                            val label = if (isFri && p == Prayer.DHUHR) L10n.word(settings, "Jumuʿah") else L10n.prayer(settings, p)
                            Row(
                                Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(u * 2))
                                    .background(if (isHero) gold.copy(alpha = if (kiswah) 0.10f else 0.14f) else Color.Transparent)
                                    .combinedClickable(onClick = onToggleRelative, onLongClick = { why = p }, onClickLabel = "Switch between clock time and time until", onLongClickLabel = "Why this time?", role = Role.Button)
                                    .padding(horizontal = u * 2.5f)
                                    .semantics(mergeDescendants = true) {
                                        contentDescription = "$label, ${PrayerEngine.clock(t, settings.use24h)} ${PrayerEngine.suffix(t, settings.use24h)}, ${L10n.relative(settings, t, state.now)}" +
                                            (if (isHero) ", now" else if (done) ", passed" else "")
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.width(u * 0.8f).fillMaxHeight(0.55f).clip(RoundedCornerShape(50)).background(if (isHero) gold else Color.Transparent))
                                Spacer(Modifier.width(u * 2.5f))
                                Column(Modifier.weight(1f)) {
                                    Text(if (urdu) label else label.uppercase(), fontFamily = if (urdu) F else Nunito, fontSize = fs(if (urdu) 3.6f else 2.7f), letterSpacing = if (urdu) 0.sp else fs(0.5f), fontWeight = FontWeight.Bold,
                                        color = if (isHero) Color(0xFFF6E7B8) else if (done) ivory.copy(alpha = 0.55f) else ivory.copy(alpha = 0.85f), maxLines = 1)
                                    if (!urdu) Text(p.arabic, fontFamily = arabicFont, fontSize = fs(3.3f), lineHeight = fs(3.8f), color = gold.copy(alpha = if (done) 0.5f else 0.9f), maxLines = 1)
                                }
                                if (done) Text("✓", fontFamily = Nunito, fontSize = fs(3.2f), color = Palette.mint.copy(alpha = 0.8f), modifier = Modifier.padding(end = u * 2))
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(if (settings.showRelative) L10n.relative(settings, t, state.now) else PrayerEngine.clock(t, settings.use24h),
                                            fontFamily = if (settings.showRelative) F else numFont, fontSize = fs(if (settings.showRelative) 3.4f else 6f), lineHeight = fs(6f),
                                            fontWeight = if (isHero) FontWeight.Bold else FontWeight.Normal, color = if (done && !isHero) ivory.copy(alpha = 0.6f) else ivory, maxLines = 1)
                                        if (!settings.showRelative) { val s = PrayerEngine.suffix(t, settings.use24h); if (s.isNotEmpty()) Text(" $s", fontFamily = numFont, fontSize = fs(3f), color = ivory.copy(alpha = 0.8f), modifier = Modifier.padding(bottom = u * 0.6f)) }
                                    }
                                    if (endT != null && p.isPrayer) Text("${L10n.word(settings, "ends")} ${PrayerEngine.clock(endT, settings.use24h)}", fontFamily = F, fontSize = fs(2.3f), color = ivory.copy(alpha = 0.5f), maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
            if (dim) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
        }
    }
}

@Composable
private fun LandIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, u: Dp, tint: Color, onClick: () -> Unit, label: String) {
    Box(
        Modifier.padding(start = u * 1.5f).size(maxOf(u * 9, 44.dp)).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)).border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .clickable(onClick = onClick, role = Role.Button).semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, Modifier.size(u * 4.2f), tint = tint) }
}

/** A small text "door" to another screen: underlined in gold on touch targets no shorter than 40 dp. */
@Composable
private fun Door(label: String, u: Dp, color: Color, font: androidx.compose.ui.text.font.FontFamily, onClick: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick, role = Role.Button).heightIn(min = 40.dp).padding(horizontal = u * 1.5f), contentAlignment = Alignment.Center) {
        Text(label, fontFamily = font, fontSize = (u.value * 2.9f).sp, fontWeight = FontWeight.SemiBold, color = color, textAlign = TextAlign.Center, maxLines = 1)
    }
}
