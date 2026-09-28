package com.usman.miqaat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.usman.miqaat.R
import androidx.compose.foundation.layout.size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.heightIn
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TimetableScreen(settings: AppSettings, onBack: () -> Unit) {
    var ym by remember { mutableStateOf(YearMonth.now()) }
    val days = remember(ym, settings) { PrayerEngine.month(settings, ym.year, ym.monthValue) }
    val today = LocalDate.now(settings.zone())
    val listState = rememberLazyListState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    LaunchedEffect(ym) { if (ym == YearMonth.now()) listState.scrollToItem((today.dayOfMonth - 3).coerceAtLeast(0)) }

    val hStart = PrayerEngine.hijri(ym.atDay(1), settings.hijriOffsetDays)
    val hEnd = PrayerEngine.hijri(ym.atEndOfMonth(), settings.hijriOffsetDays)
    val hijriRange = if (hStart.month == hEnd.month) "${hStart.english.substringAfter(' ')}" else
        "${hStart.english.substringAfter(' ').substringBeforeLast(' ')} – ${hEnd.english.substringAfter(' ')}"

    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().background(Palette.panel)) {
    val compact = maxWidth < 720.dp
    val pad = if (compact) 14.dp else 28.dp
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = pad, vertical = if (compact) 8.dp else 20.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = Palette.ivory) }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(ym.format(DateTimeFormatter.ofPattern(if (compact) "MMM yyyy" else "MMMM yyyy", Locale.ENGLISH)), fontFamily = Cormorant, fontSize = if (compact) 26.sp else 38.sp, color = Palette.ivory, lineHeight = 40.sp)
                    if (settings.showHijri && !compact) Text("   $hijriRange", fontFamily = Amiri, fontSize = 22.sp, color = Palette.goldSoft, modifier = Modifier.padding(bottom = 6.dp))
                }
                Text(
                    "${settings.locationName} · ${settings.method.label} · Asr: ${settings.asrMethod.label.substringBefore(',')}",
                    fontFamily = Nunito, fontSize = 13.sp, color = Palette.textSecondary
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NavChip("‹ " + ym.minusMonths(1).format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)), label = "Previous month") { ym = ym.minusMonths(1) }
                NavChip(Str[R.string.s_today], current = ym == YearMonth.now(), label = Str[R.string.s_go_to_today]) { scope.launch { ym = YearMonth.now(); listState.animateScrollToItem((today.dayOfMonth - 3).coerceAtLeast(0)) } }
                NavChip(ym.plusMonths(1).format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)) + " ›", label = "Next month") { ym = ym.plusMonths(1) }
            }
        }
        Spacer(Modifier.padding(6.dp))

        val ramadanMonth = days.any { PrayerEngine.isRamadan(settings, it.date) }
        val cols = if (ramadanMonth) listOf("Date", "Hijri", Str[R.string.s_fajr_suhoor], "Sunrise", "Dhuhr", "Asr", Str[R.string.s_maghrib_iftar], "Isha")
                   else listOf("Date", "Hijri", "Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
        val weights = listOf(1.5f, 1.2f, 1f, 1f, 1f, 1f, 1f, 1f)
        val shape = RoundedCornerShape(14.dp)
        Column(Modifier.fillMaxWidth().weight(1f).clip(shape).border(1.dp, Palette.line, shape).then(if (compact) Modifier.horizontalScroll(rememberScrollState()) else Modifier)) {
            val rowMod = if (compact) Modifier.width(760.dp) else Modifier.fillMaxWidth()
            Row(rowMod.background(Palette.panelRaised).padding(vertical = 10.dp, horizontal = 14.dp)) {
                cols.forEachIndexed { i, c ->
                    Text(c.uppercase(), Modifier.weight(weights[i]), fontFamily = Nunito, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold, color = Palette.ivory.copy(alpha = 0.9f))
                }
            }
            LazyColumn(state = listState) {
                items(days, key = { it.date.toEpochDay() }) { d ->
                    val isToday = d.date == today
                    val fri = d.date.dayOfWeek == DayOfWeek.FRIDAY
                    val color = when { isToday -> Color(0xFFF6E7B8); fri -> Color(0xFFBFE3C9); else -> Palette.ivory }
                    val h = PrayerEngine.hijri(d.date, settings.hijriOffsetDays)
                    val spoken = d.date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ENGLISH)) + (if (isToday) ", today" else "") + (if (fri) ", Friday" else "") + ", " + h.short + ": " +
                        Prayer.entries.joinToString(", ") { "${it.english} ${PrayerEngine.clock(d[it], settings.use24h)} ${PrayerEngine.suffix(d[it], settings.use24h)}" }
                    Row(
                        rowMod.background(if (isToday) Palette.gold.copy(alpha = 0.16f) else Color.Transparent)
                            .padding(vertical = 8.dp, horizontal = 14.dp).semantics(mergeDescendants = true) { contentDescription = spoken },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(d.date.format(DateTimeFormatter.ofPattern("EEE d", Locale.ENGLISH)), Modifier.weight(weights[0]), fontFamily = Nunito, fontSize = 15.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, color = color)
                        Text(h.short + (if (h.isRamadan) " ☾" else ""), Modifier.weight(weights[1]), fontFamily = Nunito, fontSize = 14.sp, color = color.copy(alpha = 0.6f))
                        Prayer.entries.forEachIndexed { i, p ->
                            Text(
                                PrayerEngine.clock(d[p], settings.use24h), Modifier.weight(weights[i + 2]),
                                fontFamily = Nunito, fontSize = 15.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (p == Prayer.SUNRISE) color.copy(alpha = 0.55f) else color
                            )
                        }
                    }
                    Box(rowMod.padding(horizontal = 14.dp).background(Palette.line).padding(top = 1.dp))
                }
            }
        }
        Text(
            (if (compact) Str[R.string.s_swipe_the_table_sideways_for_asr] else "") + Str[R.string.s_fridays_in_green_jumu_ah_at],
            fontFamily = Nunito, fontSize = 12.sp, color = Palette.textSecondary, modifier = Modifier.padding(top = 8.dp)
        )
    }
    }
}

@Composable
private fun NavChip(text: String, current: Boolean = false, label: String = text, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier.heightIn(min = 48.dp).semantics { contentDescription = label }.clip(shape).background(if (current) Palette.gold.copy(alpha = 0.15f) else Color.Transparent)
            .border(1.dp, if (current) Palette.gold else Color.White.copy(alpha = 0.2f), shape)
            .clickable(onClick = onClick, role = androidx.compose.ui.semantics.Role.Button).padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center
    ) { Text(text, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Palette.ivory) }
}
