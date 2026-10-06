package com.usman.miqaat.ui

import com.usman.miqaat.data.L10n
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.displayCutoutPadding
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
    val tk = screenTokens()
    var ym by remember { mutableStateOf(YearMonth.now()) }
    val days = remember(ym, settings) { PrayerEngine.month(settings, ym.year, ym.monthValue) }
    val today = LocalDate.now(settings.zone())
    val listState = rememberLazyListState()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    LaunchedEffect(ym) { if (ym == YearMonth.now()) listState.scrollToItem((today.dayOfMonth - 3).coerceAtLeast(0)) }

    val hStart = PrayerEngine.hijri(ym.atDay(1), settings.hijriOffsetDays)
    val hEnd = PrayerEngine.hijri(ym.atEndOfMonth(), settings.hijriOffsetDays)
    val hijriRange = L10n.hijriRange(settings, hStart, hEnd)

    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().background(tk.backgroundBrush)) {
    val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
    // Eight columns need roughly 90 dp each at normal text; as text grows the table switches to the frozen-Date, sideways-scrolling form early.
    val compact = maxWidth < 720.dp || maxWidth < 440.dp * fontScale
    val wide = maxWidth >= 1000.dp          // room for the Hijri range beside the title; phones in landscape are not
    val pad = if (compact) 14.dp else 28.dp
    val hScroll = rememberScrollState()          // one horizontal scroll shared by the header and every row
    Column(Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding().padding(horizontal = pad, vertical = if (compact) 8.dp else 20.dp)) {
        // Header: title + metadata on one row, month controls on their own row when narrow (never squeezed together).
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = tk.contentPrimary) }
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(L10n.monthYear(settings, ym, compact), fontFamily = Cormorant, fontSize = if (compact) 26.sp else 38.sp, color = tk.contentPrimary, lineHeight = 40.sp)
                    if (settings.showHijri && wide) Text("   $hijriRange", fontFamily = Amiri, fontSize = 22.sp, color = tk.accent, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false).padding(bottom = 6.dp))
                }
                Text(
                    (if (settings.showHijri && !wide) "$hijriRange · " else "") + "${L10n.iso(L10n.place(settings.locationName))} · ${settings.method.text} · ${if (L10n.isUrdu(settings)) "عصر" else "Asr"}: ${settings.asrMethod.text.substringBefore('،').substringBefore(',')}",
                    fontFamily = Nunito, fontSize = 13.sp, color = tk.contentSecondary, maxLines = if (fontScale > 1.3f) 4 else 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            if (!compact) MonthNav(settings, ym, today, listState, scope) { ym = it }
        }
        if (compact) Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End) { MonthNav(settings, ym, today, listState, scope) { ym = it } }
        Spacer(Modifier.padding(6.dp))

        val ramadanMonth = days.any { PrayerEngine.isRamadan(settings, it.date) }
        val ur = L10n.isUrdu(settings)
        val cols = if (ur) listOf("تاریخ", "ہجری", if (ramadanMonth) "فجر · سحری" else "فجر", "طلوعِ آفتاب", "ظہر", "عصر", if (ramadanMonth) "مغرب · افطار" else "مغرب", "عشاء")
                   else if (ramadanMonth) listOf("Date", "Hijri", "Fajr · Suhoor", "Sunrise", "Dhuhr", "Asr", "Maghrib · Iftar", "Isha")
                   else listOf("Date", "Hijri", "Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
        val weights = listOf(1.5f, 1.2f, 1f, 1f, 1f, 1f, 1f, 1f)
        val shape = RoundedCornerShape(14.dp)
        // Compact: the Date column is frozen; the rest scrolls sideways (width grows with the font setting).
        val dateW = if (compact) (74.dp * fontScale) else 0.dp
        val restW = if (compact) (560.dp * fontScale) else 0.dp
        val canScrollMore = compact && hScroll.value < hScroll.maxValue
        Column(Modifier.fillMaxWidth().weight(1f).clip(shape).border(1.dp, tk.divider, shape)) {
            @Composable fun cells(content: @Composable (Int, Modifier) -> Unit) {
                if (!compact) Row(Modifier.fillMaxWidth()) { cols.indices.forEach { i -> content(i, Modifier.weight(weights[i])) } }
                else Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.width(dateW)) { content(0, Modifier.fillMaxWidth()) }
                    Row(Modifier.weight(1f).horizontalScroll(hScroll)) { Row(Modifier.width(restW)) { (1 until cols.size).forEach { i -> content(i, Modifier.weight(weights[i])) } } }
                }
            }
            Box {
                Row(Modifier.fillMaxWidth().background(tk.surfaceRaised).padding(vertical = 10.dp, horizontal = 14.dp)) {
                    cells { i, m -> Text(cols[i].uppercase(), m, fontFamily = Nunito, fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold, color = tk.contentPrimary.copy(alpha = 0.9f), maxLines = 1, softWrap = false) }
                }
                if (canScrollMore) Text("›", Modifier.align(Alignment.CenterEnd).padding(end = 6.dp).semantics { contentDescription = if (L10n.isUrdu(settings)) "مزید کالم" else "More columns to the right" }, fontSize = 20.sp, color = tk.accent)
            }
            LazyColumn(state = listState) {
                items(days, key = { it.date.toEpochDay() }) { d ->
                    val isToday = d.date == today
                    val fri = d.date.dayOfWeek == DayOfWeek.FRIDAY
                    val color = when { isToday -> tk.todayText; fri -> tk.fridayText; else -> tk.contentPrimary }
                    val h = PrayerEngine.hijri(d.date, settings.hijriOffsetDays)
                    val spoken = L10n.spokenDay(settings, d.date, isToday, fri, h, Prayer.entries.map { it to d[it] })
                    Row(
                        Modifier.fillMaxWidth().background(tk.surface).background(when { isToday -> tk.primary.copy(alpha = 0.16f); fri -> tk.fridayText.copy(alpha = 0.07f); else -> Color.Transparent })
                            .padding(vertical = 8.dp, horizontal = 14.dp).semantics(mergeDescendants = true) { contentDescription = spoken },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        cells { i, m ->
                            when (i) {
                                0 -> Text(L10n.dayCell(settings, d.date), m, fontFamily = Nunito, fontSize = 15.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, color = color, maxLines = 1)
                                1 -> Text(L10n.hijriShort(settings, h) + (if (h.isRamadan) " ☾" else ""), m, fontFamily = Nunito, fontSize = 14.sp, color = color, maxLines = 1)
                                else -> { val p = Prayer.entries[i - 2]; Text(PrayerEngine.clock(d[p], settings.use24h), m, fontFamily = Nunito, fontSize = 15.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, color = color, maxLines = 1) }
                            }
                        }
                    }
                    Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).background(tk.divider).padding(top = 1.dp))
                }
            }
        }
        Text(
            (if (compact) Str[R.string.s_swipe_the_table_sideways_for_asr] else "") + Str[R.string.s_fridays_in_green_jumu_ah_at],
            fontFamily = Nunito, fontSize = 12.sp, color = tk.contentSecondary, modifier = Modifier.padding(top = 8.dp)
        )
    }
    }
}

@Composable
private fun MonthNav(settings: com.usman.miqaat.data.AppSettings, ym: YearMonth, today: LocalDate, listState: androidx.compose.foundation.lazy.LazyListState, scope: kotlinx.coroutines.CoroutineScope, set: (YearMonth) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NavChip("‹ " + L10n.monthShort(settings, ym.minusMonths(1)), label = if (L10n.isUrdu(settings)) "پچھلا مہینہ" else "Previous month") { set(ym.minusMonths(1)) }
        NavChip(Str[R.string.s_today], current = ym == YearMonth.now(), label = Str[R.string.s_go_to_today]) { scope.launch { set(YearMonth.now()); listState.animateScrollToItem((today.dayOfMonth - 3).coerceAtLeast(0)) } }
        NavChip(L10n.monthShort(settings, ym.plusMonths(1)) + " ›", label = if (L10n.isUrdu(settings)) "اگلا مہینہ" else "Next month") { set(ym.plusMonths(1)) }
    }
}

@Composable
private fun NavChip(text: String, current: Boolean = false, label: String = text, onClick: () -> Unit) {
    val tk = screenTokens()
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier.heightIn(min = 48.dp).semantics { contentDescription = label }.clip(shape).background(if (current) tk.primary.copy(alpha = 0.15f) else Color.Transparent)
            .border(1.dp, if (current) tk.primary else tk.neutralStroke, shape)
            .clickable(onClick = onClick, role = androidx.compose.ui.semantics.Role.Button).padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center
    ) { Text(text, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = tk.contentPrimary) }
}
