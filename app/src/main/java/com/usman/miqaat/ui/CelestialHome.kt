package com.usman.miqaat.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Celestial Meridian home. One page, two arrangements chosen from the space it is given:
 *   landscape → hero and solar arc on the left, the day's prayers as a panel on the right, a bottom rail of doors;
 *   portrait (phones and portrait tablets) → hero over the arc, prayers as a list, doors underneath.
 * Everything shown is live: times, the hero, the arc's nodes, the doors, Ramaḍān lines. Nothing is typed in.
 */

internal fun prayerIcon(p: Prayer): ImageVector = when (p) {
    Prayer.FAJR -> Icons.Outlined.NightsStay
    Prayer.SUNRISE -> Icons.Outlined.WbTwilight
    Prayer.DHUHR -> Icons.Outlined.WbSunny
    Prayer.ASR -> Icons.Outlined.LightMode
    Prayer.MAGHRIB -> Icons.Outlined.WbTwilight
    Prayer.ISHA -> Icons.Outlined.DarkMode
}

@Composable
fun CelestialHome(state: PrayerState, settings: AppSettings, a: HomeActions) {
    val tk = ThemeTokenSets.celestial
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    val hero = heroInfo(state, settings, kicker(state, settings))
    val doorList = doors(state, settings, a)
    val dim = settings.nightDim && state.period == Prayer.ISHA && !state.justPassed
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val bw = maxWidth; val bh = maxHeight
        val wide = bw > bh * 1.15f
        Box(Modifier.fillMaxSize().background(tk.background)) {
            if (!wide) ThemedArtwork(portrait = true, modifier = Modifier.fillMaxSize(), scrim = 0.3f)
            if (wide) {
                CelestialWide(state, settings, a, tk, hero, doorList, bw, bh) { why = it }
            } else {
                CelestialStacked(state, settings, a, tk, hero, doorList, bw, bh) { why = it }
            }
            if (dim) Box(Modifier.fillMaxSize().background(Color(0xFF05070F).copy(alpha = 0.35f)))
        }
    }
}

/* ───────────────────────────── landscape ───────────────────────────── */

@Composable
private fun CelestialWide(
    state: PrayerState, s: AppSettings, a: HomeActions, tk: ThemeTokens, hero: HeroInfo, doorList: List<Door>, bw: Dp, bh: Dp, onWhy: (Prayer) -> Unit
) {
    val F = uiFont(s); val urdu = L10n.isUrdu(s)
    // Placed in the approved artwork's coordinate space (1585 x 992): horizon, sun and the right-hand panel line up on every tablet.
    val sc = minOf(bw / 1585f, bh / 992f)
    val ox = (bw - sc * 1585f) / 2; val oy = (bh - sc * 992f) / 2
    fun Modifier.at(x: Float, y: Float, w: Float, h: Float) = this.absoluteOffset(ox + sc * x, oy + sc * y).size(sc * w, sc * h)
    val u = sc * 13.8f
    Box(Modifier.fillMaxSize()) {
        ThemedArtwork(portrait = false, modifier = Modifier.at(0f, 0f, 1585f, 992f), scrim = 0f)
        Box(Modifier.at(0f, 0f, 1585f, 122f).background(tk.background.copy(alpha = 0.55f))) {
            CelestialTopBar(state, s, a, tk, u * 0.95f, F, urdu, Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding().padding(horizontal = sc * 60f))
        }
        Box(Modifier.at(0f, 121f, 1585f, 1.5f).background(tk.divider))
        SolarArc(Modifier.at(240f, 500f, 880f, 250f), state, s, tk)
        Box(Modifier.at(130f, 150f, 500f, 410f)) {
            FitHeight(Modifier.fillMaxSize()) { CelestialHero(hero, state, s, tk, u, F, urdu, Alignment.CenterHorizontally) }
        }
        if (s.showDisliked) DayThread(s, state.today, state.now, modifier = Modifier.at(140f, 780f, 900f, 90f), labelSize = (u.value * 1.05f).sp, fullNames = true, gnomon = true)
        CelestialPanel(state, s, tk, u, F, urdu, onWhy, a, Modifier.at(1093f, 153f, 458f, 674f))
        CelestialRail(doorList, tk, u, F, urdu, Modifier.at(0f, 892f, 1585f, 100f).background(tk.background.copy(alpha = 0.92f)).navigationBarsPadding().displayCutoutPadding().padding(horizontal = sc * 60f), spread = true)
    }
}

@Composable
private fun CelestialTopBar(state: PrayerState, s: AppSettings, a: HomeActions, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, modifier: Modifier) {
    fun fs(x: Float) = (u.value * x).sp
    val hij = PrayerEngine.hijri(state.now.toLocalDate(), s.hijriOffsetDays)
    val narrow = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 600
    val locationRow: @Composable (Modifier) -> Unit = { m ->
        Row(
            m.clip(RoundedCornerShape(50)).heightIn(min = 48.dp).clickable(onClick = a.onOpenLocation, role = Role.Button)
                .semantics(mergeDescendants = true) { contentDescription = Str.get(R.string.s_a11y_location, L10n.iso(s.locationName)) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 2.6f), tint = tk.primary)
            Spacer(Modifier.width(u * 1f))
            Text(s.locationName, fontFamily = F, fontSize = fs(2.2f), fontWeight = FontWeight.Medium, color = tk.contentPrimary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
    val dateColumn: @Composable (Modifier) -> Unit = { m ->
        Column(m, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(L10n.date(s, state.now), fontFamily = F, fontSize = fs(1.9f), letterSpacing = if (urdu) 0.sp else fs(0.08f), color = tk.contentPrimary, maxLines = 1, textAlign = TextAlign.Center)
            if (s.showHijri) Text(L10n.hijri(s, hij), fontFamily = if (urdu) F else Cormorant, fontSize = fs(1.9f), color = tk.primary, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
    val buttons: @Composable (Modifier) -> Unit = { m ->
        Row(m, horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.Top) {
            if (s.kidsMode) NavButton(Icons.Outlined.MenuBook, Str[R.string.s_theme_home_nav_learn], Str[R.string.s_learn_salah], tk, u, F, a.onOpenLearn, showLabel = !narrow)
            NavButton(Icons.Outlined.CalendarMonth, Str[R.string.s_theme_home_nav_timetable], Str[R.string.s_monthly_timetable], tk, u, F, a.onOpenTimetable, showLabel = !narrow)
            NavButton(Icons.Outlined.Settings, Str[R.string.s_theme_home_nav_settings], Str[R.string.s_settings], tk, u, F, a.onOpenSettings, showLabel = !narrow)
        }
    }
    if (narrow) {
        // Phone: location and the three buttons share the first line (so Settings is never pushed off screen); the date sits below.
        Column(modifier) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                locationRow(Modifier.weight(1f))
                buttons(Modifier)
            }
            dateColumn(Modifier.fillMaxWidth())
        }
    } else {
        Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            locationRow(Modifier.weight(1f))
            dateColumn(Modifier.weight(1.4f))
            buttons(Modifier.weight(1f))
        }
    }
}

@Composable
private fun NavButton(icon: ImageVector, label: String, description: String, tk: ThemeTokens, u: Dp, F: FontFamily, onClick: () -> Unit, showLabel: Boolean = true) {
    Column(
        Modifier.padding(start = u * 0.6f).widthIn(min = 48.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick, role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = description }.padding(horizontal = u * 0.8f, vertical = u * 0.3f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(maxOf(u * 4.6f, 44.dp)).border(1.dp, tk.outline, CircleShape).clip(CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(u * 2.1f), tint = tk.primary)
        }
        if (showLabel) Text(label, fontFamily = F, fontSize = (u.value * 1.35f).sp, color = tk.contentSecondary, maxLines = 1, modifier = Modifier.padding(top = u * 0.2f))
    }
}

@Composable
private fun CelestialHero(hero: HeroInfo, state: PrayerState, s: AppSettings, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, align: Alignment.Horizontal, scale: Float = 1f) {
    val density = LocalDensity.current
    fun fs(x: Float) = (u.value * x * scale).sp
    fun fd(x: Float) = with(density) { (u * x * scale).toSp() }
    Column(Modifier.semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() }, horizontalAlignment = align) {
        hero.special?.let { Text(it, fontFamily = Nunito, fontSize = fs(1.5f), fontWeight = FontWeight.Bold, color = tk.primary, modifier = Modifier.padding(bottom = u * 0.6f)) }
        Text(hero.kickerLabel, fontFamily = F, fontSize = fs(1.35f), letterSpacing = if (urdu) 0.sp else fs(0.42f), color = tk.contentSecondary)
        Box(Modifier.padding(vertical = u * 0.8f).width(u * 3.2f * scale).height(2.dp).background(tk.primary))
        Text(if (urdu) hero.label else hero.label.uppercase(), fontFamily = F, fontSize = fd(if (urdu) 4.4f else 4.6f), letterSpacing = if (urdu) 0.sp else fs(0.5f), fontWeight = FontWeight.Medium, color = tk.contentPrimary, maxLines = 1)
        Text(hero.arabic, fontFamily = tk.fontArabic, fontSize = fd(6.4f), lineHeight = fd(7.4f), color = tk.primary)
        Row(verticalAlignment = Alignment.Top) {
            Text(hero.clock, fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = fd(11f), lineHeight = fd(10.6f), color = tk.contentPrimary, maxLines = 1)
            if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = tk.fontDisplay, fontSize = fd(3.6f), color = tk.contentPrimary, modifier = Modifier.padding(top = u * 2.0f * scale))
        }
        Row(Modifier.padding(top = u * 0.8f), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (hero.justPassed) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule, null, Modifier.size(u * 2.3f * scale), tint = tk.accent)
            Spacer(Modifier.width(u * 0.8f))
            Text(hero.status, fontFamily = F, fontSize = fs(if (urdu) 2.3f else 2.1f), color = tk.accent, maxLines = 2)
        }
        fastProgress(state, s)?.let { (frac, label) ->
            Column(Modifier.padding(top = u * 1.2f).width(u * 30 * scale)) {
                Box(Modifier.fillMaxWidth().height(u * 0.5f).clip(RoundedCornerShape(50)).background(tk.divider)) { Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(tk.primary)) }
                Text(label, fontFamily = Nunito, fontSize = fs(1.3f), color = tk.primary, modifier = Modifier.padding(top = u * 0.4f))
            }
        }
        tarawihLine(state, s)?.let { Text(it, fontFamily = Nunito, fontSize = fs(1.3f), color = tk.primary, modifier = Modifier.padding(top = u * 0.7f), maxLines = 3) }
    }
}

@Composable
private fun CelestialPanel(
    state: PrayerState, s: AppSettings, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, onWhy: (Prayer) -> Unit, a: HomeActions, modifier: Modifier
) {
    Column(modifier.clip(RoundedCornerShape(tk.cornerLarge)).background(tk.surface.copy(alpha = 0.94f)).border(1.dp, tk.divider, RoundedCornerShape(tk.cornerLarge))) {
        val shown = listedPrayers(s)
        shown.forEachIndexed { i, p ->
            Box(Modifier.weight(1f).fillMaxWidth()) {
                FitHeight(Modifier.fillMaxSize()) { CelestialRow(rowInfo(p, state, s), s, tk, u, F, urdu, a.onToggleRelative) { onWhy(p) } }
            }
            if (i < shown.lastIndex) Box(Modifier.padding(horizontal = u * 1.6f).fillMaxWidth().height(1.dp).background(tk.divider))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CelestialRow(r: RowInfo, s: AppSettings, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, onToggle: () -> Unit, onWhy: () -> Unit) {
    fun fs(x: Float) = (u.value * x).sp
    val selected = r.isNow || r.isNext
    val ink = if (selected) tk.accent else tk.contentPrimary
    val shape = RoundedCornerShape(tk.cornerMedium)
    val tight = u < 6.dp   // phone landscape: the panel is narrow, so the secondary line and the info button give up width
    Row(
        Modifier.fillMaxWidth().padding(horizontal = u * 0.6f, vertical = u * 0.25f).clip(shape)
            .then(if (selected) Modifier.background(tk.selectedSurface).border(1.dp, tk.accent.copy(alpha = 0.5f), shape) else Modifier)
            .combinedClickable(onClick = onToggle, onLongClick = onWhy, onClickLabel = "Switch between clock time and time until", onLongClickLabel = "Why this time?", role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = r.spoken },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) Box(Modifier.width(u * 0.5f).height(u * 5f).background(tk.accent)) else Spacer(Modifier.width(u * 0.5f))
        Spacer(Modifier.width(u * 1.2f))
        Box(Modifier.size(u * 4.6f).clip(CircleShape).background(tk.surfaceRaised), contentAlignment = Alignment.Center) {
            Icon(prayerIcon(r.prayer), null, Modifier.size(u * 2.4f), tint = if (selected) tk.accent else tk.primary)
        }
        Spacer(Modifier.width(u * 1.4f))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (urdu) r.label else r.label.uppercase(), fontFamily = F, fontSize = fs(if (urdu) 1.9f else 1.5f), letterSpacing = if (urdu) 0.sp else fs(0.25f), fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = ink, maxLines = 1)
                Spacer(Modifier.width(u * 0.7f))
                when {
                    r.isNow -> Text("· " + L10n.word(s, "NOW").lowercase(), fontFamily = Nunito, fontSize = fs(1.2f), fontWeight = FontWeight.Bold, color = tk.accent)
                    r.isNext -> Text("· " + L10n.word(s, "NEXT").lowercase(), fontFamily = Nunito, fontSize = fs(1.2f), fontWeight = FontWeight.Bold, color = tk.accent)
                    r.done -> Icon(Icons.Outlined.Check, null, Modifier.size(u * 1.5f), tint = tk.success)
                    r.prayer.isPrayer -> Icon(if (r.azaanOn) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null, Modifier.size(u * 1.5f), tint = tk.contentMuted)
                }
            }
            if (r.small.isNotEmpty() && !tight) Text(r.small, fontFamily = F, fontSize = maxOf(fs(1.15f).value, 11f).sp, color = tk.contentSecondary, maxLines = 2)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(if (s.showRelative) r.relative else r.clock, fontFamily = if (s.showRelative) F else tk.fontDisplay, fontWeight = if (s.showRelative) FontWeight.SemiBold else FontWeight.Medium, fontSize = fs(if (s.showRelative) 2.2f else 4.2f), lineHeight = fs(4.4f), color = ink, maxLines = 1, modifier = Modifier.alpha(if (r.done) 0.7f else 1f))
            if (!s.showRelative && r.suffix.isNotEmpty()) Text(" ${r.suffix}", fontFamily = tk.fontDisplay, fontSize = fs(1.5f), color = tk.contentSecondary, modifier = Modifier.padding(bottom = u * 0.5f))
        }
        Box(Modifier.padding(start = u * 0.4f, end = u * 0.4f).then(if (tight) Modifier.size(width = 28.dp, height = 48.dp) else Modifier.size(48.dp)).clip(CircleShape).clickable(onClick = onWhy, role = Role.Button).semantics { contentDescription = Str[R.string.s_why_this_time] }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Info, null, Modifier.size(u * 1.6f), tint = tk.contentMuted)
        }
    }
}

@Composable
private fun CelestialRail(doorList: List<Door>, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, modifier: Modifier, spread: Boolean = false) {
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        Row(Modifier.fillMaxWidth().padding(top = u * 0.4f), verticalAlignment = Alignment.CenterVertically) {
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(Modifier.weight(1f), horizontalArrangement = if (spread) Arrangement.SpaceEvenly else Arrangement.spacedBy(u * 2.4f), verticalArrangement = Arrangement.spacedBy(u * 0.2f)) {
                doorList.forEach { d ->
                    val warn = d.warn
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).then(if (d.onClick != null) Modifier.clickable(onClick = d.onClick, role = Role.Button) else Modifier).heightIn(min = 48.dp).padding(horizontal = u * 0.6f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (warn) Icons.Outlined.WarningAmber else Icons.Outlined.ChevronRight, null, Modifier.size(u * 2.0f), tint = if (warn) tk.warning else tk.primary)
                        Spacer(Modifier.width(u * 0.6f))
                        Text(d.label, fontFamily = F, fontSize = (u.value * 1.55f).sp, fontWeight = FontWeight.Medium, color = if (warn) tk.warning else tk.contentPrimary, maxLines = 2)
                    }
                }
            }
            if (!spread) Text(Str[R.string.s_theme_signature], fontFamily = if (urdu) F else Cormorant, fontSize = (u.value * 1.4f).sp, color = tk.contentSecondary, maxLines = 1)
        }
    }
}

/* ───────────────────────────── portrait / phone ───────────────────────────── */

@Composable
private fun CelestialStacked(
    state: PrayerState, s: AppSettings, a: HomeActions, tk: ThemeTokens, hero: HeroInfo, doorList: List<Door>, w: Dp, h: Dp, onWhy: (Prayer) -> Unit
) {
    val F = uiFont(s); val urdu = L10n.isUrdu(s)
    val roomy = w >= 600.dp && h >= 780.dp              // portrait tablet: fit the page, no scrolling. Phones scroll.
    val u: Dp = if (roomy) minOf(w / 60, h / 100) else w / 60
    // On a phone w/60 is only ~6dp, which made list and top-bar text 4-9sp. These floors keep every label at a readable size.
    val rowU: Dp = if (roomy) u * 0.8f else maxOf(u * 0.5f, 8.dp)
    val barU: Dp = if (roomy) u * 0.85f else maxOf(u * 0.62f, 7.5.dp)
    val body: @Composable ColumnScope.() -> Unit = {
        CelestialTopBar(state, s, a, tk, barU, F, urdu, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        Box(Modifier.fillMaxWidth().padding(top = u * 1f)) {
            SolarArc(Modifier.fillMaxWidth().height(u * 15).padding(horizontal = 12.dp).align(Alignment.BottomCenter), state, s, tk)
            Box(Modifier.fillMaxWidth().padding(bottom = u * 8), contentAlignment = Alignment.Center) {
                CelestialHero(hero, state, s, tk, if (roomy) u * 0.8f else u * 0.62f, F, urdu, Alignment.CenterHorizontally, 1f)
            }
        }
        Spacer(Modifier.height(u * 2f))
        Column(Modifier.padding(horizontal = 12.dp).clip(RoundedCornerShape(tk.cornerLarge)).background(tk.surface.copy(alpha = 0.94f)).border(1.dp, tk.divider, RoundedCornerShape(tk.cornerLarge))) {
            val shown = listedPrayers(s)
            shown.forEachIndexed { i, p ->
                CelestialRow(rowInfo(p, state, s), s, tk, rowU, F, urdu, a.onToggleRelative) { onWhy(p) }
                if (i < shown.lastIndex) Box(Modifier.padding(horizontal = 14.dp).fillMaxWidth().height(1.dp).background(tk.divider))
            }
        }
        CelestialRail(doorList, tk, rowU, F, urdu, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp))
        if (s.showDisliked) DayThread(s, state.today, state.now, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), labelSize = if (roomy) 13.sp else 9.sp, fullNames = false, gnomon = true)
    }
    val page = Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding().navigationBarsPadding()
    // One screen, no scrolling, on every phone and tablet: the page is scaled down just enough to fit the height it is given.
    // (Large system text is handled by AccessibleHome, which is the one layout that scrolls.)
    FitHeight(page, verticalBias = 0f) { Column(Modifier.fillMaxWidth(), content = body) }
}
