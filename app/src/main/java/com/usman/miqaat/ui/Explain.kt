package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.usman.miqaat.R
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AsrMethod
import com.usman.miqaat.data.DayTimes
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import java.time.Duration
import java.time.ZonedDateTime

/** "Why this time?" – everything that went into one prayer time, in plain words. */
@Composable
fun WhyDialog(settings: AppSettings, day: DayTimes, p: Prayer, onDismiss: () -> Unit) {
    val t = day[p]
    val calc = PrayerEngine.calculated(settings, day.date)
    val end = PrayerEngine.endOf(settings, day, p)
    val h24 = settings.use24h
    fun c(z: ZonedDateTime) = PrayerEngine.clock(z, h24) + " " + PrayerEngine.suffix(z, h24)
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Palette.panelRaised,
        title = { Text("Why ${PrayerEngine.clock(t, h24)}?  ·  ${p.english}  ${p.arabic}", fontFamily = Cormorant, fontSize = 26.sp, color = Palette.ivory) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                SectionLabel(Str[R.string.s_calculated_from])
                if (day.fromMasjid) Line(Str[R.string.s_source], "${settings.masjidName.ifBlank { "Masjid timetable" }} for ${day.date}", "Calculated would be ${c(calc[p])}")
                else Line(Str[R.string.s_source], Str[R.string.s_calculated_on_this_device], Str[R.string.s_adhan_library_meeus_astronomical_algorithms])
                Line(Str[R.string.s_method], settings.method.label, settings.method.detail)
                when (p) {
                    Prayer.FAJR -> Line(Str[R.string.s_rule], "Sun ${settings.method.parameters().fajrAngle}° below the horizon before sunrise", Str[R.string.s_true_dawn_al_fajr_a_diq])
                    Prayer.SUNRISE -> Line(Str[R.string.s_rule], Str[R.string.s_upper_edge_of_the_sun_on], Str[R.string.s_ends_fajr_not_a_prayer_time])
                    Prayer.DHUHR -> Line(Str[R.string.s_rule], Str[R.string.s_sun_passes_the_meridian_zaw_l], if (settings.jumuahEnabled && day.date.dayOfWeek == java.time.DayOfWeek.FRIDAY) Str[R.string.s_friday_your_jumu_ah_time_is] else "")
                    Prayer.ASR -> Line(Str[R.string.s_rule], if (settings.asrMethod == AsrMethod.HANAFI) Str[R.string.s_shadow_2_object_noon_shadow_hanafi] else Str[R.string.s_shadow_object_noon_shadow_shafi_i],
                        "The other view would give ${c(PrayerEngine.asrOther(settings, day.date))}")
                    Prayer.MAGHRIB -> Line(Str[R.string.s_rule], Str[R.string.s_sunset_the_sun_s_disc_fully], "")
                    Prayer.ISHA -> Line(Str[R.string.s_rule], settings.method.parameters().let { if (it.ishaInterval > 0) "${it.ishaInterval} min after Maghrib" else "Sun ${it.ishaAngle}° below the horizon after sunset" }, Str[R.string.s_disappearance_of_the_red_twilight])
                }
                Line("Location", settings.locationName, "%.4f, %.4f · ${settings.zone().id}".format(settings.latitude, settings.longitude))
                SectionLabel(Str[R.string.s_your_settings])
                val adj = settings.adjustments[p] ?: 0
                Line(Str[R.string.s_your_adjustment], if (adj == 0) Str[R.string.s_none] else (if (adj > 0) "+$adj min" else "$adj min"), Str[R.string.s_settings_prayer_times_minute_adjustments])
                SectionLabel(Str[R.string.s_islamic_guidance_scholarly_views_not_calculation])
                if (end != null) Line(Str[R.string.s_ends], c(end), when (p) {
                    Prayer.ISHA -> "Shown at sharʿī midnight (halfway from sunset to dawn), the end of the preferred time in many views. Other scholars hold Isha valid until Fajr ${c(PrayerEngine.times(settings, day.date.plusDays(1))[Prayer.FAJR])}. Ask your imam."
                    Prayer.FAJR -> Str[R.string.s_at_sunrise_agreed]
                    Prayer.ASR -> Str[R.string.s_at_sunset_many_scholars_call_the]
                    Prayer.DHUHR -> Str[R.string.s_when_asr_begins_which_itself_depends]
                    else -> Str[R.string.s_when_the_next_prayer_begins]
                })
                Spacer(Modifier.height(8.dp))
                Text(Str[R.string.s_disliked_for_voluntary_prayer_today_approximate], fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold, color = Palette.goldSoft)
                PrayerEngine.dislikedWindows(day, settings).forEach { w -> Text("${c(w.start)} – ${c(w.end)}  ·  ${w.label}", fontFamily = Nunito, fontSize = 13.sp, color = Palette.ivory, modifier = Modifier.padding(top = 4.dp)) }
                Text(Str[R.string.s_these_windows_are_conservative_estimates_15], fontFamily = Nunito, fontSize = 12.sp, color = Palette.textMuted, lineHeight = 17.sp, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(Str[R.string.s_close], color = Palette.goldSoft) } }
    )
}

@Composable
private fun SectionLabel(t: String) = Text(t.uppercase(), fontFamily = Nunito, fontSize = 11.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold, color = Palette.textMuted, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp).semantics { heading() })

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Line(k: String, v: String, sub: String) {
    Column(Modifier.padding(vertical = 6.dp).semantics(mergeDescendants = true) {}) {
        // Key and value wrap onto two lines on narrow screens instead of squeezing.
        androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(k, fontFamily = Nunito, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory, modifier = Modifier.padding(end = 12.dp))
            Text(v, fontFamily = Nunito, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft)
        }
        if (sub.isNotEmpty()) Text(sub, fontFamily = Nunito, fontSize = 12.sp, color = Palette.textSecondary)
    }
    HorizontalDivider(color = Palette.line)
}

/**
 * The day as a thin bar from Fajr to Isha: green where voluntary prayer is fine, hatched where it's disliked,
 * with a marker for now.
 */
/**
 * Option A · "Gold thread": the day as one hairline. Passed time is bright gold, the rest dim thread;
 * a diamond bead per prayer, one glowing bead for now; where voluntary prayer is disliked the thread goes dotted.
 * Used on phones (both themes).
 */
@Composable
fun DayThread(settings: AppSettings, day: DayTimes, now: ZonedDateTime, modifier: Modifier = Modifier, kiswah: Boolean = false, labelSize: androidx.compose.ui.unit.TextUnit = 9.sp) {
    val start = day[Prayer.FAJR].minusMinutes(20); val end = day[Prayer.ISHA].plusMinutes(40)
    val total = Duration.between(start, end).toMillis().toFloat().coerceAtLeast(1f)
    fun f(z: ZonedDateTime) = (Duration.between(start, z).toMillis() / total).coerceIn(0f, 1f)
    val windows = PrayerEngine.dislikedWindows(day, settings)
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val gold = Palette.gold; val thread = if (kiswah) Color(0xFF8A6D2F) else Palette.goldSoft.copy(alpha = 0.45f); val dimThread = if (kiswah) Color(0xFF3A3020) else Color.White.copy(alpha = 0.18f)
    val labelStyle = androidx.compose.ui.text.TextStyle(fontFamily = if (kiswah) Cinzel else Nunito, fontSize = labelSize, letterSpacing = if (kiswah) 1.5.sp else 0.8.sp, fontWeight = FontWeight.SemiBold)
    val shortName = mapOf(Prayer.FAJR to "F", Prayer.SUNRISE to "☼", Prayer.DHUHR to "D", Prayer.ASR to "A", Prayer.MAGHRIB to "M", Prayer.ISHA to "I")
    val words = Str[R.string.s_day_line] + windows.joinToString("; ") { w -> "avoid voluntary prayer ${PrayerEngine.clock(w.start, settings.use24h)} to ${PrayerEngine.clock(w.end, settings.use24h)}" }
    Canvas(modifier.height(with(androidx.compose.ui.platform.LocalDensity.current) { labelSize.toDp() } * 3.4f).semantics { contentDescription = words }) {
        val y = size.height * 0.36f
        val xNow = size.width * f(now)
        val hair = 1.dp.toPx()
        // thread: solid between the disliked windows, dotted inside them; gold behind now, dim ahead
        val cuts = windows.map { size.width * f(it.start) to size.width * f(it.end) }.sortedBy { it.first }
        val solid = mutableListOf<Pair<Float, Float>>(); var cur = 0f
        cuts.forEach { (a, b) -> if (a > cur) solid += cur to a; cur = maxOf(cur, b) }
        if (cur < size.width) solid += cur to size.width
        val elapsed = Brush.horizontalGradient(listOf(thread, gold), 0f, xNow.coerceAtLeast(1f))
        solid.forEach { (a, b) ->
            if (a < xNow) drawLine(elapsed, Offset(a, y), Offset(minOf(b, xNow), y), hair * 1.5f)
            if (b > xNow) drawLine(dimThread, Offset(maxOf(a, xNow), y), Offset(b, y), hair)
        }
        val dots = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(hair * 1.2f, hair * 2.2f))
        cuts.forEach { (a, b) -> drawLine(if (b <= xNow) gold.copy(alpha = 0.8f) else thread, Offset(a, y), Offset(b, y), hair, pathEffect = dots) }
        // beads
        val bead = size.height * 0.16f
        Prayer.entries.forEach { p ->
            val x = size.width * f(day[p]); val passed = !day[p].isAfter(now)
            val isNext = p == Prayer.entries.firstOrNull { day[it].isAfter(now) && it.isPrayer }
            val r = if (isNext) bead * 1.25f else if (p.isPrayer) bead else bead * 0.75f
            rotate(45f, Offset(x, y)) {
                if (passed) drawRect(gold, Offset(x - r, y - r), Size(2 * r, 2 * r))
                else drawRect(if (isNext) gold else thread, Offset(x - r, y - r), Size(2 * r, 2 * r), style = Stroke(hair * 1.2f))
            }
            val t = measurer.measure(shortName.getValue(p), labelStyle)
            drawText(t, color = if (passed || isNext) Palette.ivory else Palette.textMuted, topLeft = Offset(x - t.size.width / 2f, size.height - t.size.height))
        }
        // now: glowing bead
        if (now.isAfter(start) && now.isBefore(end)) {
            drawCircle(Brush.radialGradient(listOf(gold.copy(alpha = 0.55f), gold.copy(alpha = 0f)), Offset(xNow, y), bead * 3.2f), bead * 3.2f, Offset(xNow, y))
            drawCircle(Color(0xFFF6E7B8), bead * 0.9f, Offset(xNow, y))
        }
    }
}

/**
 * Option B · "Sun arc": the sun's path from sunrise to sunset with the sun where it is now; Fajr and Isha
 * as short night wings on the horizon. Disliked windows are faint ruby ticks along the arc. Used on tablets.
 */
@Composable
fun SunArc(settings: AppSettings, day: DayTimes, now: ZonedDateTime, height: Dp, modifier: Modifier = Modifier, kiswah: Boolean = false, labelSize: androidx.compose.ui.unit.TextUnit = 9.sp) {
    val rise = day[Prayer.SUNRISE]; val set = day[Prayer.MAGHRIB]
    val dayMs = Duration.between(rise, set).toMillis().toFloat().coerceAtLeast(1f)
    fun t(z: ZonedDateTime) = (Duration.between(rise, z).toMillis() / dayMs)
    val windows = PrayerEngine.dislikedWindows(day, settings)
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val gold = Palette.gold; val thread = if (kiswah) Color(0xFF8A6D2F) else Palette.goldSoft.copy(alpha = 0.5f); val dimThread = if (kiswah) Color(0xFF3A3020) else Color.White.copy(alpha = 0.2f)
    val night = if (kiswah) Color(0xFF2B3F8C) else Color(0xFF7F95E0)
    val ruby = Color(0xFFC9646F)
    val labelStyle = androidx.compose.ui.text.TextStyle(fontFamily = if (kiswah) Cinzel else Nunito, fontSize = labelSize, letterSpacing = if (kiswah) 1.5.sp else 1.sp, fontWeight = FontWeight.SemiBold)
    val words = "Sun arc: sunrise ${PrayerEngine.clock(rise, settings.use24h)}, sunset ${PrayerEngine.clock(set, settings.use24h)}. " + windows.joinToString("; ") { w -> "avoid voluntary prayer ${PrayerEngine.clock(w.start, settings.use24h)} to ${PrayerEngine.clock(w.end, settings.use24h)}" }
    Canvas(modifier.height(height).semantics { contentDescription = words }) {
        val hair = 1.dp.toPx()
        val labelH = labelSize.toPx() * 1.5f
        val horizon = size.height - labelH - hair * 3
        val wingW = size.width * 0.09f
        val x0 = wingW; val x1 = size.width - wingW
        val apex = hair * 6
        val p0 = Offset(x0, horizon); val p2 = Offset(x1, horizon); val c = Offset(size.width / 2, apex * 2 - horizon)   // control so the curve's top is at `apex`
        fun pt(u: Float): Offset { val k = u.coerceIn(0f, 1f); val a = (1 - k) * (1 - k); val b = 2 * (1 - k) * k; val d = k * k; return Offset(a * p0.x + b * c.x + d * p2.x, a * p0.y + b * c.y + d * p2.y) }
        fun poly(from: Float, to: Float): Path { val p = Path(); var first = true; var u = from; while (u <= to + 1e-4f) { val o = pt(u); if (first) p.moveTo(o.x, o.y) else p.lineTo(o.x, o.y); first = false; u += 0.01f }; return p }
        // horizon and night wings
        drawLine(dimThread, Offset(0f, horizon), Offset(size.width, horizon), hair)
        drawLine(night.copy(alpha = 0.9f), Offset(hair * 2, horizon), Offset(x0, horizon), hair * 1.6f, cap = StrokeCap.Round)
        drawLine(night.copy(alpha = 0.9f), Offset(x1, horizon), Offset(size.width - hair * 2, horizon), hair * 1.6f, cap = StrokeCap.Round)
        // the arc: dim whole, gold elapsed
        drawPath(poly(0f, 1f), dimThread, style = Stroke(hair * 1.2f))
        val tn = t(now)
        if (tn > 0f) drawPath(poly(0f, tn.coerceAtMost(1f)), gold, style = Stroke(hair * 1.8f, cap = StrokeCap.Round))
        // disliked: ruby ticks perpendicular-ish to the arc (drawn upward)
        windows.forEach { w ->
            var u = t(w.start); val uEnd = t(w.end)
            val step = if (uEnd - u < 0.06f) (uEnd - u).coerceAtLeast(0.005f) / 2f else 0.045f
            while (u <= uEnd + 1e-4f) { val o = pt(u); drawLine(ruby.copy(alpha = 0.85f), Offset(o.x, o.y - hair * 2), Offset(o.x, o.y - hair * 8), hair, cap = StrokeCap.Round); u += step }
        }
        // prayer marks
        val marks = listOf(Prayer.FAJR to Offset(x0 * 0.45f, horizon), Prayer.SUNRISE to p0, Prayer.DHUHR to pt(t(day[Prayer.DHUHR])), Prayer.ASR to pt(t(day[Prayer.ASR])), Prayer.MAGHRIB to p2, Prayer.ISHA to Offset(size.width - x0 * 0.45f, horizon))
        val r = hair * 2.6f
        marks.forEach { (p, o) ->
            val passed = !day[p].isAfter(now)
            val isNext = p == Prayer.entries.firstOrNull { day[it].isAfter(now) && it.isPrayer }
            if (passed) drawCircle(gold, r, o) else drawCircle(if (isNext) gold else thread, if (isNext) r * 1.2f else r, o, style = Stroke(hair * 1.2f))
            if (p.isPrayer) {
                val txt = measurer.measure(if (kiswah) p.english.uppercase() else p.english.uppercase(), labelStyle)
                val x = (o.x - txt.size.width / 2f).coerceIn(0f, size.width - txt.size.width)
                drawText(txt, color = if (passed || isNext) Palette.ivory else Palette.textMuted, topLeft = Offset(x, size.height - txt.size.height))
            }
        }
        // the sun (day) or a moon dot on the wing (night)
        if (tn in 0f..1f) {
            val o = pt(tn)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF1C2), gold, gold.copy(alpha = 0f)), o, hair * 12), hair * 12, o)
            drawCircle(Color(0xFFFFF1C2), hair * 3.4f, o)
        } else {
            val nightSpan = if (tn < 0f) Duration.between(day[Prayer.FAJR].minusMinutes(60), rise) else Duration.between(set, day[Prayer.ISHA].plusMinutes(90))
            val frac = (if (tn < 0f) Duration.between(day[Prayer.FAJR].minusMinutes(60), now) else Duration.between(set, now)).toMillis().toFloat() / nightSpan.toMillis().coerceAtLeast(1)
            val x = if (tn < 0f) hair * 2 + (x0 - hair * 2) * frac.coerceIn(0f, 1f) else x1 + (size.width - hair * 2 - x1) * frac.coerceIn(0f, 1f)
            drawCircle(Color(0xFFF6E7B8), hair * 2.6f, Offset(x, horizon))
        }
    }
}

/** Kept for callers that still use the old name: phones get the thread, tablets the arc. */
@Composable
fun DayTimeline(settings: AppSettings, day: DayTimes, now: ZonedDateTime, height: Dp, modifier: Modifier = Modifier, dim: Boolean = false, kiswah: Boolean = false, tablet: Boolean = false) {
    if (tablet) SunArc(settings, day, now, height, modifier, kiswah) else DayThread(settings, day, now, modifier, kiswah)
}

@Composable
fun TimelineLegend(fs: androidx.compose.ui.unit.TextUnit, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Swatch(Color(0xFF8FD3A7).copy(alpha = 0.6f)); Text(Str[R.string.s_prayer_may_be_offered], fontFamily = Nunito, fontSize = fs, color = color) }
        Row(verticalAlignment = Alignment.CenterVertically) { Swatch(Color(0xFFF08C8C).copy(alpha = 0.7f)); Text(Str[R.string.s_disliked_for_naw_fil_after_sunrise], fontFamily = Nunito, fontSize = fs, color = color) }
    }
}
@Composable private fun Swatch(c: Color) { androidx.compose.foundation.layout.Box(Modifier.size(9.dp).clip(CircleShape).background(c)) }
