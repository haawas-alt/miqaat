package com.usman.miqaat.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.*

/** Short landscape is a reading layout, not a scaled-down tablet artwork board. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactThemeHome(state: PrayerState, settings: AppSettings, actions: HomeActions) {
    val tk = ThemeTokenSets.of(settings.theme)
    val gallery = settings.theme == AppTheme.PRAYER_GALLERY
    val shape = RoundedCornerShape(if (gallery) 8.dp else 20.dp)
    val font = uiFont(settings)
    val hero = heroInfo(state, settings, kicker(state, settings))
    val links = doors(state, settings, actions)
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    Box(Modifier.fillMaxSize().background(tk.backgroundBrush)) {
        ThemedArtwork(portrait = false, modifier = Modifier.fillMaxSize(), scrim = if (gallery) 0.65f else 0.4f)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().displayCutoutPadding().padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).heightIn(min = 48.dp).clickable(role = Role.Button, onClick = actions.onOpenLocation).padding(vertical = 6.dp)) {
                    Text(settings.locationName, fontFamily = font, fontSize = 16.sp, color = tk.contentPrimary)
                    Text(L10n.dateShort(settings, state.now), fontFamily = font, fontSize = 13.sp, color = tk.contentSecondary)
                }
                if (settings.kidsMode) IconButton(onClick = actions.onOpenLearn, modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.MenuBook, Str[R.string.s_learn_salah], tint = tk.primary) }
                IconButton(onClick = actions.onOpenTimetable, modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.CalendarMonth, Str[R.string.s_monthly_timetable], tint = tk.primary) }
                IconButton(onClick = actions.onOpenSettings, modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.Settings, Str[R.string.s_settings], tint = tk.primary) }
            }
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(0.46f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.fillMaxWidth().clip(shape).background(tk.surface.copy(alpha = 0.94f)).border(1.dp, tk.divider, shape).padding(16.dp)
                        .semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() }) {
                        Text(hero.kickerLabel, fontFamily = font, fontSize = 14.sp, color = tk.contentSecondary)
                        hero.special?.let { Text(it, fontFamily = font, fontSize = 14.sp, color = tk.accent) }
                        Text(hero.label, fontFamily = tk.fontDisplay, fontSize = 28.sp, color = tk.contentPrimary)
                        if (!L10n.isUrdu(settings)) Text(hero.arabic, fontFamily = tk.fontArabic, fontSize = 24.sp, color = tk.accent)
                        Text(L10n.iso("${hero.clock} ${hero.suffix}"), fontFamily = tk.fontDisplay, fontSize = 38.sp, color = tk.contentPrimary)
                        Text(hero.status, fontFamily = font, fontSize = 14.sp, color = tk.accent)
                        Text(Str[R.string.s_view_prayer_details], fontFamily = font, fontSize = 16.sp, color = tk.primary,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) { why = hero.prayer }.padding(vertical = 12.dp))
                    }
                    if (settings.showHijri) Text(L10n.hijri(settings, PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)), fontFamily = font, fontSize = 14.sp, color = tk.accent)
                    if (settings.showDisliked) DayThread(settings, state.today, state.now, modifier = Modifier.fillMaxWidth().height(70.dp), labelSize = 13.sp, fullNames = true, gnomon = true)
                    fastProgress(state, settings)?.let { (_, text) -> Text(text, fontFamily = font, fontSize = 14.sp, color = tk.accent) }
                    tarawihLine(state, settings)?.let { Text(it, fontFamily = font, fontSize = 14.sp, color = tk.accent) }
                    links.forEach { link ->
                        Text(link.label, fontFamily = font, fontSize = 16.sp, color = if (link.warn) tk.warning else tk.contentPrimary,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(shape).background(tk.surface.copy(alpha = 0.94f))
                                .then(if (link.onClick != null) Modifier.clickable(role = Role.Button, onClick = link.onClick) else Modifier).padding(12.dp))
                    }
                }
                Column(Modifier.weight(0.54f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(if (gallery) 4.dp else 8.dp)) {
                    listedPrayers(settings).forEach { prayer ->
                        val row = rowInfo(prayer, state, settings)
                        val selected = row.isNow || row.isNext
                        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(shape).background(if (selected) tk.selectedSurface else tk.surface.copy(alpha = 0.94f))
                            .border(if (selected) 2.dp else 1.dp, if (selected) tk.primary else tk.divider, shape)
                            .combinedClickable(onClick = actions.onToggleRelative, onLongClick = { why = prayer }, role = Role.Button,
                                onClickLabel = Str[R.string.s_switch_clock_time_until], onLongClickLabel = Str[R.string.s_why_this_time])
                            .semantics(mergeDescendants = true) { contentDescription = row.spoken }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (gallery) Icon(when (prayer) { Prayer.FAJR, Prayer.ISHA -> Icons.Outlined.DarkMode; Prayer.SUNRISE, Prayer.MAGHRIB -> Icons.Outlined.WbTwilight; else -> Icons.Outlined.LightMode }, null, Modifier.size(28.dp), tint = tk.accent)
                            Column(Modifier.weight(1f).padding(start = if (gallery) 10.dp else 0.dp)) {
                                Text(row.label + if (selected) " · " + L10n.word(settings, if (row.isNow) "NOW" else "NEXT") else "", fontFamily = font, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 16.sp, color = tk.contentPrimary)
                                if (row.small.isNotBlank()) Text(row.small, fontFamily = font, fontSize = 13.sp, color = tk.contentSecondary)
                            }
                            Text(L10n.iso(if (settings.showRelative) row.relative else "${row.clock} ${row.suffix}"), fontFamily = tk.fontDisplay, fontSize = 22.sp, color = if (selected) tk.primary else tk.contentPrimary)
                        }
                    }
                }
            }
        }
        if (settings.nightDim && state.period == Prayer.ISHA && !state.justPassed) Box(Modifier.fillMaxSize().background(tk.background.copy(alpha = 0.3f)))
    }
}
