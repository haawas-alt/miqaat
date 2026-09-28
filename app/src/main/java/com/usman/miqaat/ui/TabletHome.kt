package com.usman.miqaat.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.NotificationsOff
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.ArtTheme
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Tablet homes (landscape, ≥ 500 dp tall).
 *
 *  MihrabHome  — Miqaat theme. A timetable board on the left, a recessed mihrab on the right holding the hour;
 *                one sundial line beneath the niche; doors and signature on the foot line.
 *  CourtyardHome — Kiswah theme. The list is the sundial: one gold line down the left with a bead at now; the
 *                whole right side is open court for the hour; the Kufic band at the head, as on the Kiswah.
 *
 * Both keep every behaviour of the earlier tablet page: location tap, doors for Qibla/adhkār/Jumuʿah/alerts,
 * kicker lines for Ramaḍān and ʿĪd, fast-progress and tarāwīḥ lines, tap to switch clock ↔ time-until,
 * long-press or ⓘ for "Why this time?", ✓ / bell states, iqamah and end times, night dim, art-theme setting.
 */

class HomeActions(
    val onOpenTimetable: () -> Unit, val onOpenSettings: () -> Unit, val onOpenLocation: () -> Unit,
    val onOpenQibla: () -> Unit, val onOpenAdhkar: (AdhkarMode) -> Unit, val onOpenFriday: () -> Unit, val onOpenLearn: () -> Unit,
    val updateAvailable: Boolean, val onOpenAbout: () -> Unit, val onToggleRelative: () -> Unit
)

private class Door(val label: String, val warn: Boolean = false, val onClick: (() -> Unit)? = null)

/** Everything that used to be a chip, in one place, so both tablet homes show exactly the same doors. */
@Composable
private fun doors(state: PrayerState, s: AppSettings, a: HomeActions): List<Door> {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val alarmsOk = remember(state.now.toLocalDate(), state.hero) { com.usman.miqaat.data.Reliability.allGood(ctx) }
    val ramadan = PrayerEngine.isRamadan(s, state.now.toLocalDate())
    val hij = PrayerEngine.hijri(state.now.toLocalDate(), s.hijriOffsetDays)
    val out = mutableListOf<Door>()
    if (s.showQibla) { val q = PrayerEngine.qibla(s); out += Door(L10n.word(s, "Qibla") + " " + L10n.iso("${q.toInt()}° ${PrayerEngine.compass(q)}"), onClick = a.onOpenQibla) }
    if (s.adhkarEnabled && state.current == Prayer.FAJR) out += Door(L10n.word(s, "Morning adhkār")) { a.onOpenAdhkar(AdhkarMode.MORNING) }
    if (s.adhkarEnabled && (state.current == Prayer.ASR || state.current == Prayer.MAGHRIB)) out += Door(L10n.word(s, "Evening adhkār")) { a.onOpenAdhkar(AdhkarMode.EVENING) }
    if (s.fridayReminders && PrayerEngine.isJumuahWindow(s, state.now)) out += Door("Jumuʿah · al-Kahf · ṣalawāt", onClick = a.onOpenFriday)
    if (s.postPrayerAdhkar && state.current != null && Duration.between(state.today[state.current], state.now).toMinutes().let { m -> if (state.justPassed) m >= 5 else m in 5..40 })
        out += Door(L10n.word(s, "After-prayer adhkār")) { a.onOpenAdhkar(AdhkarMode.POST) }
    if (ramadan && hij.day >= 27) out += Door("Zakāt al-Fiṭr before Eid prayer")
    if (a.updateAvailable) out += Door(L10n.word(s, "Update available"), onClick = a.onOpenAbout)
    if (!alarmsOk) out += Door(Str[R.string.s_azaan_may_be_late_fix], warn = true, onClick = a.onOpenSettings)
    if (s.zoneNeedsReview) out += Door(Str[R.string.s_time_zone_needs_checking], warn = true, onClick = a.onOpenLocation)
    if (s.travellerMode && PrayerEngine.isTravelling(s)) out += Door("Travelling · %.0f km from home".format(PrayerEngine.distanceKm(s.homeLat!!, s.homeLng!!, s.latitude, s.longitude)), onClick = a.onOpenLocation)
    if (state.today.fromMasjid) out += Door(s.masjidName.ifBlank { Str[R.string.s_masjid_timetable] }, onClick = a.onOpenLocation)
    return out
}

@Composable
private fun kicker(state: PrayerState, s: AppSettings): String? {
    val ramadan = PrayerEngine.isRamadan(s, state.now.toLocalDate())
    val hij = PrayerEngine.hijri(state.now.toLocalDate(), s.hijriOffsetDays)
    val eidMorning = ((hij.month == 10 && hij.day == 1) || (hij.month == 12 && hij.day == 10)) && (state.current == null || state.current == Prayer.FAJR || state.current == Prayer.SUNRISE)
    val oddNight = ramadan && hij.day >= 20 && hij.day % 2 == 1 && (state.current == Prayer.MAGHRIB || state.current == Prayer.ISHA)
    val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && s.jumuahEnabled
    return when {
        eidMorning -> "ʿĪd mubārak · اللهُ أكبر اللهُ أكبر لا إله إلا الله"
        oddNight -> "Ramaḍān ${hij.day} · an odd night · seek Laylat al-Qadr"
        ramadan && state.current == null -> "Ramaḍān · Suhoor ends at Fajr"
        ramadan && state.hero == Prayer.MAGHRIB && !state.justPassed -> "Ramaḍān · Iftar"
        ramadan && state.hero == Prayer.MAGHRIB && state.justPassed -> "Ramaḍān · Iftar time"
        isFri && state.hero == Prayer.DHUHR -> "Jumuʿah"
        else -> null
    }
}

/* ───────────────────────────── shared pieces ───────────────────────────── */

@Composable
private fun Header(state: PrayerState, s: AppSettings, a: HomeActions, u: Dp, gold: Color, ivory: Color, kiswah: Boolean, F: FontFamily, urdu: Boolean) {
    fun fs(x: Float) = (u.value * x).sp
    val hij = PrayerEngine.hijri(state.now.toLocalDate(), s.hijriOffsetDays)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.clip(RoundedCornerShape(50)).clickable(onClick = a.onOpenLocation, role = Role.Button).heightIn(min = 48.dp).padding(end = u * 1.5f)
                .semantics(mergeDescendants = true) { contentDescription = "Location: ${s.locationName}. Opens location settings" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 2.2f), tint = gold)
            Spacer(Modifier.width(u * 0.8f))
            if (kiswah) Text(s.locationName.uppercase(), fontFamily = Cinzel, fontSize = fs(1.3f), letterSpacing = fs(0.3f), color = gold, maxLines = 1)
            else Text(s.locationName, fontFamily = F, fontSize = fs(1.9f), fontWeight = FontWeight.SemiBold, color = ivory, maxLines = 1)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            if (kiswah) {
                Text((if (s.showHijri) L10n.hijri(s, hij) + "  ·  " else "") + state.now.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH)).uppercase() + "  ·  " + state.now.format(DateTimeFormatter.ofPattern(if (s.use24h) "HH:mm" else "h:mm a", Locale.ENGLISH)),
                    fontFamily = Cinzel, fontSize = fs(1.3f), letterSpacing = fs(0.25f), color = ivory.copy(alpha = 0.85f), maxLines = 1)
            } else {
                Text(L10n.date(s, state.now) + "  ·  " + state.now.format(DateTimeFormatter.ofPattern(if (s.use24h) "HH:mm" else "h:mm a", Locale.ENGLISH)), fontFamily = F, fontSize = fs(1.6f), color = ivory.copy(alpha = 0.85f), maxLines = 1)
                if (s.showHijri) Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(L10n.hijri(s, hij), fontFamily = if (urdu) F else Cormorant, fontSize = fs(1.9f), color = gold, maxLines = 1)
                    if (!urdu) { Text("  ·  ", fontFamily = Amiri, fontSize = fs(1.7f), color = gold); Text(L10n.iso(hij.arabic), fontFamily = Amiri, fontSize = fs(1.7f), color = gold) }
                }
            }
        }
        Spacer(Modifier.width(u * 2.5f))
        if (s.kidsMode) HdrIcon(Icons.Outlined.MenuBook, u, gold, a.onOpenLearn, Str[R.string.s_learn_salah])
        HdrIcon(Icons.Outlined.CalendarMonth, u, gold, a.onOpenTimetable, Str[R.string.s_monthly_timetable])
        HdrIcon(Icons.Outlined.Settings, u, gold, a.onOpenSettings, Str[R.string.s_settings])
    }
}

@Composable
private fun HdrIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, u: Dp, tint: Color, onClick: () -> Unit, label: String) {
    Box(
        Modifier.padding(start = u * 1.1f).size(maxOf(u * 4.4f, 48.dp)).clip(CircleShape).border(1.dp, tint.copy(alpha = 0.35f), CircleShape)
            .clickable(onClick = onClick, role = Role.Button).semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, Modifier.size(u * 1.9f), tint = tint) }
}

/** Arabic name · Latin name · clock · one status line, plus the Ramaḍān extras when they apply. */
@Composable
private fun Hero(state: PrayerState, s: AppSettings, u: Dp, kiswah: Boolean, arabicFont: FontFamily, numFont: FontFamily, F: FontFamily, urdu: Boolean, gold: Color, ivory: Color, scale: Float = 1f) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    fun fs(x: Float) = (u.value * x * scale).sp
    fun fd(x: Float) = with(density) { (u * x * scale).toSp() }
    val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && s.jumuahEnabled
    val ramadan = PrayerEngine.isRamadan(s, state.now.toLocalDate())
    val k = kicker(state, s)
    val name = (if (isFri && state.hero == Prayer.DHUHR) L10n.word(s, "Jumuʿah") else L10n.prayer(s, state.hero))
    val status = buildString {
        append(if (state.justPassed) L10n.ago(s, state.delta).replaceFirstChar { it.uppercase() } else L10n.inFor(s, state.delta).replaceFirstChar { it.uppercase() })
        state.current?.let { cur -> PrayerEngine.iqamah(s, state.today, cur)?.takeIf { it.isAfter(state.now) }?.let { iq -> append("  ·  ${L10n.word(s, "Iqamah")} ${PrayerEngine.clock(iq, s.use24h)}") } }
    }
    val words = "$name, ${PrayerEngine.clock(state.heroTime, s.use24h)} ${PrayerEngine.suffix(state.heroTime, s.use24h)}, $status"
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = words; heading() }) {
        if (k != null) Text(if (kiswah) k.uppercase() else k, fontFamily = if (kiswah) Cinzel else Nunito, fontSize = fs(1.3f), letterSpacing = fs(0.25f), fontWeight = FontWeight.Bold, color = gold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = u * 0.6f))
        Text(state.hero.arabic, fontFamily = arabicFont, fontSize = fd(if (kiswah) 6.4f else 7.4f), lineHeight = fd(8.4f), color = Color(0xFFF6E7B8))
        Text(if (urdu) name else name.uppercase(), fontFamily = if (urdu) F else (if (kiswah) Cinzel else Nunito), fontSize = fs(if (urdu) 2.6f else 1.35f), letterSpacing = if (urdu) 0.sp else fs(0.34f), fontWeight = FontWeight.Bold, color = ivory.copy(alpha = 0.85f), modifier = Modifier.padding(top = u * 0.6f))
        Row(verticalAlignment = Alignment.Top) {
            Text(PrayerEngine.clock(state.heroTime, s.use24h), style = TextStyle(fontFamily = numFont, fontSize = fd(if (kiswah) 15f else 15.5f), lineHeight = fd(15f), brush = if (kiswah) Kiswah.goldText else Brush.verticalGradient(listOf(ivory, ivory))))
            val suf = PrayerEngine.suffix(state.heroTime, s.use24h)
            if (suf.isNotEmpty()) Text(" $suf", fontFamily = numFont, fontSize = fd(3.4f), color = if (kiswah) gold else ivory, modifier = Modifier.padding(top = u * 2.2f * scale))
        }
        Row(Modifier.padding(top = u * 1.1f), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(u * 0.8f).clip(CircleShape).background(if (state.justPassed) Palette.mint else Palette.gold))
            Spacer(Modifier.width(u * 1f))
            Text(if (kiswah) status.uppercase() else status, fontFamily = if (kiswah) Cinzel else F, fontSize = fs(if (kiswah) 1.25f else if (urdu) 2.1f else 1.7f), letterSpacing = if (kiswah) fs(0.16f) else 0.sp, fontWeight = FontWeight.SemiBold, color = ivory.copy(alpha = 0.92f), textAlign = TextAlign.Center)
        }
        if (ramadan && state.current != null && state.current != Prayer.MAGHRIB && state.current != Prayer.ISHA) {
            val start = state.today[Prayer.FAJR]; val end = state.today[Prayer.MAGHRIB]
            val total = Duration.between(start, end).toMinutes().coerceAtLeast(1); val done = Duration.between(start, state.now).toMinutes().coerceIn(0, total)
            Column(Modifier.padding(top = u * 1f).width(u * 30), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(u * 0.4f).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.18f))) { Box(Modifier.fillMaxWidth(done / total.toFloat()).fillMaxHeight().background(Palette.gold)) }
                Text("Iftar in ${PrayerEngine.humanDuration(Duration.between(state.now, end))}", fontFamily = Nunito, fontSize = fs(1.2f), color = gold, modifier = Modifier.padding(top = u * 0.4f))
            }
        }
        if (ramadan && (state.current == Prayer.ISHA || state.current == Prayer.MAGHRIB || state.current == null)) {
            val lt = PrayerEngine.lastThird(s, state.today)
            Text("Tarāwīḥ ${PrayerEngine.clock(state.today[Prayer.ISHA].plusMinutes(s.tarawihMinutesAfterIsha.toLong()), s.use24h)}  ·  last third of the night from ${PrayerEngine.clock(lt, s.use24h)} ${PrayerEngine.suffix(lt, s.use24h)}",
                fontFamily = Nunito, fontSize = fs(1.2f), color = gold.copy(alpha = 0.9f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = u * 0.7f))
        }
    }
}

/** One prayer on the board: name and Arabic on the left, time on the right, the small line beneath. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeRow(
    p: Prayer, state: PrayerState, s: AppSettings, u: Dp, kiswah: Boolean, arabicFont: FontFamily, numFont: FontFamily, F: FontFamily, urdu: Boolean, gold: Color, ivory: Color,
    onToggle: () -> Unit, onWhy: () -> Unit, modifier: Modifier
) {
    fun fs(x: Float) = (u.value * x).sp
    val t = state.today[p]
    val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && s.jumuahEnabled
    val label = if (isFri && p == Prayer.DHUHR) L10n.word(s, "Jumuʿah") else L10n.prayer(s, p)
    val passed = !t.isAfter(state.now)
    val isNow = p == state.hero && state.justPassed
    val isNext = p == state.next && !state.justPassed
    val done = passed && p.isPrayer && !isNow
    val iq = PrayerEngine.iqamah(s, state.today, p)
    val endT = if (s.showEndTimes) PrayerEngine.endOf(s, state.today, p) else null
    val small = listOfNotNull(
        endT?.let { "${L10n.word(s, "ends")} ${PrayerEngine.clock(it, s.use24h)}" },
        iq?.let { "iq ${PrayerEngine.clock(it, s.use24h)}" },
        if (p == Prayer.SUNRISE) "ḍuḥā from ${PrayerEngine.clock(t.plusMinutes(15), s.use24h)}" else null
    ).joinToString("  ·  ")
    val spoken = buildString {
        append(label); append(", "); append(PrayerEngine.clock(t, s.use24h)); append(' '); append(PrayerEngine.suffix(t, s.use24h)); append(", "); append(L10n.relative(s, t, state.now))
        when { isNow -> append(", now"); isNext -> append(", next prayer"); done -> append(", passed") }
        if (iq != null) { append(", iqamah "); append(PrayerEngine.clock(iq, s.use24h)) }
        if (endT != null) { append(", ends "); append(PrayerEngine.clock(endT, s.use24h)) }
        if (s.azaanEnabled[p] != true && p.isPrayer) append(", azaan off")
    }
    Row(
        modifier.fillMaxWidth()
            .combinedClickable(onClick = onToggle, onLongClick = onWhy, onClickLabel = "Switch between clock time and time until", onLongClickLabel = "Why this time?", role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = spoken },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (urdu) label else label.uppercase(), fontFamily = if (urdu) F else (if (kiswah) Cinzel else Nunito), fontSize = fs(if (urdu) 1.9f else 1.15f), letterSpacing = if (urdu) 0.sp else fs(if (kiswah) 0.34f else 0.24f), fontWeight = FontWeight.Bold,
                    color = if (isNow || isNext) Color(0xFFFFF0BE) else ivory.copy(alpha = if (done) 0.55f else 0.75f), maxLines = 1)
                Spacer(Modifier.width(u * 0.8f))
                when {
                    isNow -> Text("· " + L10n.word(s, "NOW").lowercase(), fontFamily = Nunito, fontSize = fs(1.1f), fontWeight = FontWeight.Bold, color = Color(0xFFFFF0BE))
                    isNext -> Text("· " + L10n.word(s, "NEXT").lowercase(), fontFamily = Nunito, fontSize = fs(1.1f), fontWeight = FontWeight.Bold, color = gold)
                    done -> Icon(Icons.Outlined.Check, null, Modifier.size(u * 1.4f), tint = Palette.mint)
                    p.isPrayer -> Icon(if (s.azaanEnabled[p] == true) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null, Modifier.size(u * 1.4f), tint = ivory.copy(alpha = 0.6f))
                }
            }
            if (!urdu) Text(p.arabic, fontFamily = arabicFont, fontSize = fs(2.6f), lineHeight = fs(3f), color = gold.copy(alpha = if (done) 0.55f else 0.95f), maxLines = 1)
            if (small.isNotEmpty()) Text(small, fontFamily = F, fontSize = fs(1.05f), letterSpacing = if (kiswah) fs(0.12f) else 0.sp, color = ivory.copy(alpha = 0.5f), maxLines = 1)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                if (s.showRelative) L10n.relative(s, t, state.now) else PrayerEngine.clock(t, s.use24h),
                style = TextStyle(fontFamily = if (s.showRelative) F else numFont, fontSize = fs(if (s.showRelative) 2.2f else 4.4f), lineHeight = fs(4.4f), fontWeight = if (s.showRelative) FontWeight.SemiBold else FontWeight.Normal,
                    brush = if (kiswah && !s.showRelative) Kiswah.goldText else Brush.verticalGradient(listOf(ivory, ivory))),
                modifier = Modifier.alpha(if (done) 0.55f else 1f), maxLines = 1
            )
            if (!s.showRelative) { val suf = PrayerEngine.suffix(t, s.use24h); if (suf.isNotEmpty()) Text(" $suf", fontFamily = numFont, fontSize = fs(1.4f), color = ivory.copy(alpha = if (done) 0.5f else 0.85f), modifier = Modifier.padding(bottom = u * 0.5f)) }
        }
        Box(Modifier.padding(start = u * 0.6f).size(40.dp).clip(CircleShape).clickable(onClick = onWhy, role = Role.Button).semantics { contentDescription = Str[R.string.s_why_this_time] }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Info, null, Modifier.size(u * 1.4f), tint = ivory.copy(alpha = 0.45f))
        }
    }
}

@Composable
private fun DoorsRow(doors: List<Door>, u: Dp, gold: Color, kiswah: Boolean, F: FontFamily, modifier: Modifier = Modifier) {
    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
    androidx.compose.foundation.layout.FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(u * 2.2f), verticalArrangement = Arrangement.spacedBy(u * 0.3f)) {
        doors.forEach { d ->
            Row(Modifier.clip(RoundedCornerShape(50)).then(if (d.onClick != null) Modifier.clickable(onClick = d.onClick, role = Role.Button) else Modifier).heightIn(min = 40.dp).padding(horizontal = u * 0.4f), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(u * 0.55f).graphicsLayer { rotationZ = 45f }.background(gold))
                Spacer(Modifier.width(u * 0.7f))
                Text(if (kiswah) d.label.uppercase() else d.label, fontFamily = if (kiswah) Cinzel else F, fontSize = (u.value * (if (kiswah) 1.05f else 1.35f)).sp, letterSpacing = if (kiswah) (u.value * 0.22f).sp else 0.sp, fontWeight = FontWeight.SemiBold, color = if (d.warn) Color(0xFFFFD9A0) else gold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun Signature(u: Dp, gold: Color, kiswah: Boolean, F: FontFamily, urdu: Boolean) {
    Text(
        if (urdu) "ڈیزائن: UZR · میرے لیے دعا کیجیے" else if (kiswah) "DESIGNED BY UZR  ·  MAKE DUʿĀ FOR ME" else "Designed by UZR  ·  Make duʿā for me",
        fontFamily = if (urdu) F else if (kiswah) Cinzel else Cormorant, fontSize = (u.value * (if (kiswah) 1.0f else 1.45f)).sp, letterSpacing = (u.value * (if (kiswah) 0.3f else 0.06f)).sp, color = gold.copy(alpha = 0.7f), maxLines = 1
    )
}

/** The recess: frame band, inner light edge, muqarnas courses at the springing line, ember rising from the floor. */
@Composable
private fun MihrabNiche(modifier: Modifier, hood: Boolean, ember: Float) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val inset = w * 0.03f
        fun arch(x0: Float, x1: Float, top: Float, bottom: Float): Path = Path().apply {
            val xm = (x0 + x1) / 2f; val spring = top + (bottom - top) * 0.42f
            moveTo(x0, bottom); lineTo(x0, spring)
            cubicTo(x0, top + (spring - top) * 0.25f, xm - (x1 - x0) * 0.22f, top + (spring - top) * 0.05f, xm, top)
            cubicTo(xm + (x1 - x0) * 0.22f, top + (spring - top) * 0.05f, x1, top + (spring - top) * 0.25f, x1, spring)
            lineTo(x1, bottom); close()
        }
        val outer = arch(inset, w - inset, h * 0.03f, h)
        drawPath(outer, Brush.verticalGradient(listOf(Color(0xE60A0E2C), Color(0xD9141A45), Color(0xF24A2C2A)), 0f, h))
        drawPath(outer, Brush.radialGradient(listOf(Color(0xFFF0873A).copy(alpha = 0.55f * ember), Color(0xFFF0873A).copy(alpha = 0.12f * ember), Color.Transparent), Offset(w / 2, h), h * 0.75f))
        val band = Brush.verticalGradient(listOf(Color(0xFFFFF0BE), Color(0xFFE3C36A), Color(0xFF9E7A22)), 0f, h)
        drawPath(arch(inset * 0.7f, w - inset * 0.7f, h * 0.02f, h), band, style = Stroke(w * 0.012f))
        drawPath(arch(inset * 1.9f, w - inset * 1.9f, h * 0.075f, h), Brush.verticalGradient(listOf(Color(0xB3FFF7E3), Color(0x14FFF7E3)), 0f, h), style = Stroke(1.2.dp.toPx()))
        if (hood) {
            // two courses of small niches at the springing line
            val y1 = h * 0.24f; val y2 = y1 - h * 0.04f
            val n = 13; val x0 = w * 0.11f; val x1 = w * 0.89f; val step = (x1 - x0) / n; val r = step / 2f
            val hair = 1.dp.toPx()
            for (i in 0 until n) drawArc(Color(0x8CE9CF88), 180f, 180f, false, Offset(x0 + i * step, y1 - r), Size(step, 2 * r), style = Stroke(hair))
            for (i in 0 until n - 1) drawArc(Color(0x47E9CF88), 180f, 180f, false, Offset(x0 + step / 2 + i * step, y2 - r), Size(step, 2 * r), style = Stroke(hair))
            drawLine(Color(0x59E9CF88), Offset(x0, y1), Offset(x1, y1), hair)
        }
    }
}

/* ───────────────────────────── D · Mihrab and list (Miqaat) ───────────────────────────── */

@Composable
fun MihrabHome(state: PrayerState, settings: AppSettings, a: HomeActions) {
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val u: Dp = minOf(maxWidth / 100, maxHeight / 60)
        val sky = skyFor(state.period)
        val top by animateColorAsState(sky.top, tween(1500), label = "top")
        val bottom by animateColorAsState(sky.bottom, tween(1500), label = "bottom")
        val F = uiFont(settings); val urdu = L10n.isUrdu(settings)
        val gold = Palette.goldSoft; val ivory = Palette.ivory
        val dim = settings.nightDim && state.period == Prayer.ISHA && !state.justPassed
        val minsToAzaan = Duration.between(state.now, state.nextTime).toMinutes()
        val ember = if (!state.justPassed && minsToAzaan in 0..10) 1f else 0.7f   // the niche warms in the last ten minutes

        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
            Glow(Modifier.fillMaxSize(), sky.glow); Stars(Modifier.fillMaxSize(), sky.stars)
            if (settings.artTheme == ArtTheme.GEOMETRIC) GirihLattice(Modifier.fillMaxSize(), tile = u.value * 12f, alpha = 0.07f)
            DaySkyScrim(state.period)

            Column(Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding().padding(horizontal = u * 4, vertical = u * 2.4f)) {
                Header(state, settings, a, u, gold, ivory, false, F, urdu)
                Row(Modifier.weight(1f).fillMaxWidth().padding(top = u * 2f)) {
                    // the board
                    val shown = if (settings.showSunrise) Prayer.entries else Prayer.prayersOnly
                    Column(Modifier.width(u * 33).fillMaxHeight().padding(top = u * 1f)) {
                        shown.forEach { p ->
                            val isNow = p == state.hero && state.justPassed
                            Row(Modifier.weight(1f).fillMaxWidth()) {
                                Box(Modifier.width(u * 0.4f).fillMaxHeight(0.8f).align(Alignment.CenterVertically).clip(RoundedCornerShape(50)).background(if (isNow) Brush.verticalGradient(listOf(Color(0xFFFFF7E3), gold)) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))))
                                Spacer(Modifier.width(u * 1.8f))
                                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                                    TimeRow(p, state, settings, u, false, Amiri, Cormorant, F, urdu, gold, ivory, a.onToggleRelative, { why = p }, Modifier)
                                    Box(Modifier.fillMaxWidth().padding(top = u * 0.8f).height(1.dp).background(if (isNow) Palette.gold else gold.copy(alpha = 0.16f)))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(u * 5))
                    // the niche
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            MihrabNiche(Modifier.fillMaxSize(), hood = settings.artTheme != ArtTheme.MINIMAL, ember = ember)
                            FitHeight(Modifier.fillMaxSize().padding(top = u * 15, start = u * 3, end = u * 3, bottom = u * 2)) {
                                Hero(state, settings, u, false, Amiri, Cormorant, F, urdu, gold, ivory)
                            }
                        }
                        if (settings.showDisliked) DayThread(settings, state.today, state.now, modifier = Modifier.fillMaxWidth().padding(start = u * 4, end = u * 4, top = u * 1.2f), labelSize = (u.value * 1.05f).sp, fullNames = true, gnomon = true)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = u * 1.2f), verticalAlignment = Alignment.CenterVertically) {
                    DoorsRow(doors(state, settings, a), u, gold, false, F, Modifier.weight(1f))
                    Signature(u, gold, false, F, urdu)
                }
            }
            if (dim) Box(Modifier.fillMaxSize().background(Color(0xFF05070F).copy(alpha = 0.35f)))
        }
    }
}

/* ───────────────────────────── C · Courtyard (Kiswah) ───────────────────────────── */

@Composable
fun CourtyardHome(state: PrayerState, settings: AppSettings, a: HomeActions) {
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val u: Dp = minOf(maxWidth / 100, maxHeight / 60)
        fun fs(x: Float) = (u.value * x).sp
        val F = uiFont(settings); val urdu = L10n.isUrdu(settings)
        val gold = Kiswah.threadSoft; val ivory = Kiswah.ivory
        val dim = settings.nightDim && state.period == Prayer.ISHA && !state.justPassed
        val shown = if (settings.showSunrise) Prayer.entries else Prayer.prayersOnly

        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF151311), Kiswah.silk), radius = 1600f))) {
            Weave(Modifier.fillMaxSize())
            // ember of the sun that set, low on the court
            Box(Modifier.align(Alignment.BottomEnd).fillMaxWidth(0.6f).fillMaxHeight(0.35f).background(Brush.radialGradient(listOf(Color(0xFFF0873A).copy(alpha = 0.22f), Color.Transparent), radius = 900f)))

            Column(Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding().padding(horizontal = u * 4, vertical = u * 2.2f)) {
                Header(state, settings, a, u, gold, ivory, true, F, urdu)
                // the band, ruled above and below, as on the Kiswah
                Box(Modifier.fillMaxWidth().padding(top = u * 1.6f).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, gold, gold, Color.Transparent))))
                Text(Kiswah.BAND, fontFamily = ReemKufi, fontSize = fs(2.4f), color = Color(0xFFE3C36A).copy(alpha = 0.7f), maxLines = 1, softWrap = false, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = u * 0.8f).semantics { contentDescription = "Lā ilāha illa llāh, Muḥammadun rasūlu llāh. Subḥāna llāhi wa-bi-ḥamdih. Subḥāna llāhi l-ʿaẓīm." })
                Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, gold, gold, Color.Transparent))))

                Row(Modifier.weight(1f).fillMaxWidth().padding(top = u * 1.5f)) {
                    // the list is the sundial: one line with a diamond per prayer and a lit bead at now
                    Row(Modifier.width(u * 36).fillMaxHeight()) {
                        Canvas(Modifier.width(u * 2.4f).fillMaxHeight()) {
                            val x = size.width / 2f; val n = shown.size; val rowH = size.height / n
                            val nowIdx = shown.indexOfFirst { it == state.hero }.coerceAtLeast(0)
                            val yNow = rowH * nowIdx + rowH / 2f
                            drawLine(gold, Offset(x, rowH / 2f), Offset(x, yNow), 1.5.dp.toPx())
                            drawLine(gold.copy(alpha = 0.3f), Offset(x, yNow), Offset(x, size.height - rowH / 2f), 1.dp.toPx())
                            shown.forEachIndexed { i, p ->
                                val y = rowH * i + rowH / 2f; val passed = !state.today[p].isAfter(state.now); val r = u.toPx() * 0.55f
                                rotate(45f, Offset(x, y)) {
                                    if (passed) drawRect(gold, Offset(x - r, y - r), Size(2 * r, 2 * r)) else drawRect(gold.copy(alpha = 0.6f), Offset(x - r, y - r), Size(2 * r, 2 * r), style = Stroke(1.dp.toPx()))
                                }
                                if (i == nowIdx && state.justPassed) {
                                    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE1A0).copy(alpha = 0.6f), Color.Transparent), Offset(x, y), r * 4), r * 4, Offset(x, y))
                                    drawCircle(Color(0xFFFFF7E3), r * 0.9f, Offset(x, y))
                                }
                            }
                        }
                        Spacer(Modifier.width(u * 1.6f))
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            shown.forEach { p ->
                                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                                    TimeRow(p, state, settings, u, true, ReemKufi, Cinzel, F, urdu, gold, ivory, a.onToggleRelative, { why = p }, Modifier)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(u * 4))
                    // the court
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            FitHeight(Modifier.fillMaxSize().padding(horizontal = u * 2, vertical = u * 1)) {
                                Hero(state, settings, u, true, ReemKufi, Cinzel, F, urdu, gold, ivory, scale = 1.25f)
                            }
                        }
                        if (settings.showDisliked) DayThread(settings, state.today, state.now, modifier = Modifier.fillMaxWidth().padding(start = u * 6, end = u * 6, top = u * 0.5f), kiswah = true, labelSize = (u.value * 1.0f).sp, gnomon = true)
                        Row(Modifier.fillMaxWidth().padding(start = u * 6, end = u * 6, top = u * 1f), verticalAlignment = Alignment.CenterVertically) {
                            DoorsRow(doors(state, settings, a), u, gold, true, F, Modifier.weight(1f))
                            Signature(u, gold, true, F, urdu)
                        }
                    }
                }
            }
            if (dim) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
        }
    }
}
