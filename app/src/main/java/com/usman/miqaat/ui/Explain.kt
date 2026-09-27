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
                SectionLabel("Calculated from")
                if (day.fromMasjid) Line("Source", "${settings.masjidName.ifBlank { "Masjid timetable" }} for ${day.date}", "Calculated would be ${c(calc[p])}")
                else Line("Source", "Calculated on this device", "Adhan library · Meeus astronomical algorithms")
                Line("Method", settings.method.label, settings.method.detail)
                when (p) {
                    Prayer.FAJR -> Line("Rule", "Sun ${settings.method.parameters().fajrAngle}° below the horizon before sunrise", "True dawn (al-fajr aṣ-ṣādiq)")
                    Prayer.SUNRISE -> Line("Rule", "Upper edge of the sun on the horizon", "Ends Fajr; not a prayer time")
                    Prayer.DHUHR -> Line("Rule", "Sun passes the meridian (zawāl), plus a minute", if (settings.jumuahEnabled && day.date.dayOfWeek == java.time.DayOfWeek.FRIDAY) "Friday: your Jumuʿah time is used instead" else "")
                    Prayer.ASR -> Line("Rule", if (settings.asrMethod == AsrMethod.HANAFI) "Shadow = 2 × object + noon shadow (Hanafi)" else "Shadow = object + noon shadow (Shafiʿi, Maliki, Hanbali)",
                        "The other view would give ${c(PrayerEngine.asrOther(settings, day.date))}")
                    Prayer.MAGHRIB -> Line("Rule", "Sunset: the sun's disc fully below the horizon", "")
                    Prayer.ISHA -> Line("Rule", settings.method.parameters().let { if (it.ishaInterval > 0) "${it.ishaInterval} min after Maghrib" else "Sun ${it.ishaAngle}° below the horizon after sunset" }, "Disappearance of the red twilight")
                }
                Line("Location", settings.locationName, "%.4f, %.4f · ${settings.zone().id}".format(settings.latitude, settings.longitude))
                SectionLabel("Your settings")
                val adj = settings.adjustments[p] ?: 0
                Line("Your adjustment", if (adj == 0) "None" else (if (adj > 0) "+$adj min" else "$adj min"), "Settings › Prayer times › Minute adjustments")
                SectionLabel("Islamic guidance · scholarly views, not calculation")
                if (end != null) Line("Ends", c(end), when (p) {
                    Prayer.ISHA -> "Shown at sharʿī midnight (halfway from sunset to dawn), the end of the preferred time in many views. Other scholars hold Isha valid until Fajr ${c(PrayerEngine.times(settings, day.date.plusDays(1))[Prayer.FAJR])}. Ask your imam."
                    Prayer.FAJR -> "At sunrise (agreed)"
                    Prayer.ASR -> "At sunset. Many scholars call the time after the sun yellows the time of necessity; ʿAsr already prayed is not affected."
                    Prayer.DHUHR -> "When ʿAsr begins — which itself depends on the ʿAsr view chosen above"
                    else -> "When the next prayer begins"
                })
                Spacer(Modifier.height(8.dp))
                Text("Disliked for voluntary prayer today · approximate", fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold, color = Palette.goldSoft)
                PrayerEngine.dislikedWindows(day, settings).forEach { w -> Text("${c(w.start)} – ${c(w.end)}  ·  ${w.label}", fontFamily = Nunito, fontSize = 13.sp, color = Palette.ivory, modifier = Modifier.padding(top = 4.dp)) }
                Text("These windows are conservative estimates (≈15 min after sunrise, ≈10 min around zawāl, after ʿAsr until sunset); the texts describe events, not minute counts, and schools differ on the details. Obligatory and missed prayers are not restricted by them — Ṣaḥīḥ Muslim 831.", fontFamily = Nunito, fontSize = 12.sp, color = Palette.textMuted, lineHeight = 17.sp, modifier = Modifier.padding(top = 6.dp))
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Palette.goldSoft) } }
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
@Composable
fun DayTimeline(settings: AppSettings, day: DayTimes, now: ZonedDateTime, height: Dp, modifier: Modifier = Modifier, dim: Boolean = false) {
    val start = day[Prayer.FAJR]; val end = day[Prayer.ISHA].plusMinutes(30)
    val total = Duration.between(start, end).toMillis().toFloat().coerceAtLeast(1f)
    fun f(z: ZonedDateTime) = (Duration.between(start, z).toMillis() / total).coerceIn(0f, 1f)
    val windows = PrayerEngine.dislikedWindows(day, settings)
    val marks = Prayer.entries.map { f(day[it]) }
    Canvas(modifier.height(height)) {
        val r = size.height / 2
        drawRoundRect(Color.White.copy(alpha = if (dim) 0.08f else 0.14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(r))
        drawRoundRect(Color(0xFF8FD3A7).copy(alpha = 0.28f), topLeft = Offset(0f, 0f), size = Size(size.width * f(end), size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(r))
        windows.forEach { w ->
            val x0 = size.width * f(w.start); val x1 = size.width * f(w.end)
            drawRect(Color(0xFFF08C8C).copy(alpha = 0.55f), topLeft = Offset(x0, 0f), size = Size((x1 - x0).coerceAtLeast(2f), size.height))
            var x = x0; val step = size.height * 0.9f
            while (x < x1) { drawLine(Color.Black.copy(alpha = 0.25f), Offset(x, size.height), Offset(x + size.height, 0f), 1.5f); x += step }
        }
        marks.forEach { m -> drawLine(Color.White.copy(alpha = 0.6f), Offset(size.width * m, 0f), Offset(size.width * m, size.height), 1.5f) }
        val nx = size.width * f(now)
        if (now.isAfter(start) && now.isBefore(end)) drawLine(Color(0xFFF6E7B8), Offset(nx, -r), Offset(nx, size.height + r), size.height * 0.35f, cap = StrokeCap.Round)
    }
}

@Composable
fun TimelineLegend(fs: androidx.compose.ui.unit.TextUnit, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Swatch(Color(0xFF8FD3A7).copy(alpha = 0.6f)); Text(" prayer may be offered", fontFamily = Nunito, fontSize = fs, color = color) }
        Row(verticalAlignment = Alignment.CenterVertically) { Swatch(Color(0xFFF08C8C).copy(alpha = 0.7f)); Text(" disliked for nawāfil: after sunrise · zawāl · after ʿAsr", fontFamily = Nunito, fontSize = fs, color = color) }
    }
}
@Composable private fun Swatch(c: Color) { androidx.compose.foundation.layout.Box(Modifier.size(9.dp).clip(CircleShape).background(c)) }
