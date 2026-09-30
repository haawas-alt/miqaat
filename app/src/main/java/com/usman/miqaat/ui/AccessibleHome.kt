package com.usman.miqaat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState

/**
 * The Home a person gets when they ask Android for larger text (font scale above ~115%). The dense one-screen boards
 * cannot honour a 130% or 200% request without clipping, so at those sizes Home reflows into a scrolling page that uses
 * the real font scale for every label: hero, prayer list and every door grow with the setting and nothing is truncated.
 * Wide screens split it in two columns (hero and doors | prayers). It is themed through the same tokens as every screen.
 * Every fact comes from the same models the boards use (heroInfo, rowInfo, doors), so nothing can disagree with them.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AccessibleHome(state: PrayerState, settings: AppSettings, a: HomeActions) {
    val tk = ThemeTokenSets.of(settings.theme)
    val F = uiFont(settings); val urdu = L10n.isUrdu(settings)
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    val hero = heroInfo(state, settings, kicker(state, settings))
    val doorList = doors(state, settings, a)
    val hij = PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
    val wide = LocalConfiguration.current.screenWidthDp >= 840
    val shape = RoundedCornerShape(tk.cornerLarge)

    Box(Modifier.fillMaxSize().background(tk.backgroundBrush)) {
        if (tk.art == ArtStyle.CELESTIAL || tk.art == ArtStyle.GALLERY) CompositionLocalProvider(LocalThemeTokens provides tk) { ThemedArtwork(portrait = !wide, modifier = Modifier.fillMaxSize(), scrim = 0.55f) }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().displayCutoutPadding().navigationBarsPadding()
                .padding(horizontal = if (wide) 32.dp else 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // header: place and the three destinations, each a full-size target with a visible label
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.weight(1f).clip(RoundedCornerShape(50)).heightIn(min = 48.dp).clickable(onClick = a.onOpenLocation, role = Role.Button)
                        .semantics(mergeDescendants = true) { contentDescription = "Location: ${settings.locationName}. Opens location settings" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.LocationOn, null, Modifier.size(24.dp), tint = tk.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(settings.locationName, fontFamily = F, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = tk.contentPrimary)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (settings.kidsMode) Nav(Icons.Outlined.MenuBook, Str[R.string.s_learn_salah], tk, F, a.onOpenLearn)
                Nav(Icons.Outlined.CalendarMonth, Str[R.string.s_monthly_timetable], tk, F, a.onOpenTimetable)
                Nav(Icons.Outlined.Settings, Str[R.string.s_settings], tk, F, a.onOpenSettings)
            }
            Text(L10n.date(settings, state.now), fontFamily = F, fontSize = 18.sp, color = tk.contentSecondary)
            if (settings.showHijri) Text(L10n.hijri(settings, hij), fontFamily = if (urdu) F else tk.fontArabic, fontSize = 18.sp, color = tk.accent)

            val heroBlock: @Composable ColumnScope.() -> Unit = {
                Column(
                    Modifier.fillMaxWidth().clip(shape).background(tk.surface.copy(alpha = 0.94f)).border(1.dp, tk.divider, shape).padding(20.dp)
                        .semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() },
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hero.special?.let { Text(it, fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = tk.primary) }
                    Text(hero.kickerLabel, fontFamily = F, fontSize = 15.sp, color = tk.contentSecondary)
                    Text(hero.label, fontFamily = if (urdu) F else tk.fontDisplay, fontSize = 34.sp, fontWeight = FontWeight.Medium, color = tk.contentPrimary)
                    Text(hero.arabic, fontFamily = tk.fontArabic, fontSize = 34.sp, color = tk.primary)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(hero.clock, fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = 52.sp, color = tk.contentPrimary)
                        if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = tk.fontDisplay, fontSize = 22.sp, color = tk.contentSecondary, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Text(hero.status, fontFamily = F, fontSize = 18.sp, color = tk.accent)
                    Spacer(Modifier.height(6.dp))
                    MiqButton(Str[R.string.s_view_prayer_details], { why = hero.prayer }, Modifier.fillMaxWidth())
                }
                doorList.forEach { d -> DoorRow(d, tk, F) }
            }
            val listBlock: @Composable ColumnScope.() -> Unit = {
                Column(Modifier.fillMaxWidth().clip(shape).background(tk.surface.copy(alpha = 0.94f)).border(1.dp, tk.divider, shape)) {
                    val shown = listedPrayers(settings)
                    shown.forEachIndexed { i, p ->
                        PrayerLine(rowInfo(p, state, settings), settings, tk, F, urdu, a.onToggleRelative) { why = p }
                        if (i < shown.lastIndex) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(tk.divider))
                    }
                }
            }
            if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), content = heroBlock)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), content = listBlock)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = heroBlock)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = listBlock)
            }
            Text(L10n.word(settings, "Designed by UZR · Make duʿā for me"), fontFamily = if (urdu) F else tk.fontDisplay, fontSize = 15.sp, color = tk.contentSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
        }
    }
}

@Composable
private fun Nav(icon: ImageVector, label: String, tk: ThemeTokens, F: androidx.compose.ui.text.font.FontFamily, onClick: () -> Unit) {
    Row(
        Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).border(1.dp, tk.outline, RoundedCornerShape(50)).clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(22.dp), tint = tk.primary)
        Spacer(Modifier.width(8.dp))
        Text(label, fontFamily = F, fontSize = 16.sp, color = tk.contentPrimary)
    }
}

@Composable
private fun DoorRow(d: Door, tk: ThemeTokens, F: androidx.compose.ui.text.font.FontFamily) {
    val shape = RoundedCornerShape(tk.cornerMedium)
    val ink = if (d.warn) tk.warning else tk.contentPrimary
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(shape).background(tk.surface.copy(alpha = 0.94f)).border(1.dp, if (d.warn) tk.warning else tk.divider, shape)
            .then(if (d.onClick != null) Modifier.clickable(role = Role.Button, onClick = d.onClick) else Modifier).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (d.warn) Icons.Outlined.WarningAmber else Icons.Outlined.ChevronRight, null, Modifier.size(24.dp), tint = if (d.warn) tk.warning else tk.primary)
        Spacer(Modifier.width(12.dp))
        Text(d.label, fontFamily = F, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = ink)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PrayerLine(r: RowInfo, s: AppSettings, tk: ThemeTokens, F: androidx.compose.ui.text.font.FontFamily, urdu: Boolean, onToggle: () -> Unit, onWhy: () -> Unit) {
    val selected = r.isNow || r.isNext
    val ink = if (selected) tk.accent else tk.contentPrimary
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).then(if (selected) Modifier.background(tk.selectedSurface) else Modifier)
            .combinedClickable(onClick = onToggle, onLongClick = onWhy, onClickLabel = Str[R.string.s_switch_clock_time_until], onLongClickLabel = Str[R.string.s_why_this_time], role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = r.spoken }.padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(tk.surfaceRaised), contentAlignment = Alignment.Center) {
            Icon(prayerIcon(r.prayer), null, Modifier.size(24.dp), tint = if (selected) tk.accent else tk.primary)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(r.label, fontFamily = if (urdu) F else tk.fontDisplay, fontSize = 22.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = ink)
            if (r.small.isNotEmpty()) Text(r.small, fontFamily = F, fontSize = 14.sp, color = tk.contentSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(if (s.showRelative) r.relative else r.clock, fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = 24.sp, color = ink)
                if (!s.showRelative && r.suffix.isNotEmpty()) Text(" ${r.suffix}", fontFamily = tk.fontDisplay, fontSize = 14.sp, color = tk.contentSecondary, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
        Box(Modifier.padding(horizontal = 4.dp).size(48.dp).clip(CircleShape).clickable(onClick = onWhy, role = Role.Button).semantics { contentDescription = Str[R.string.s_why_this_time] }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Info, null, Modifier.size(22.dp), tint = tk.contentSecondary)
        }
    }
}
