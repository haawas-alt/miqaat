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
import androidx.compose.ui.semantics.clearAndSetSemantics
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

/*
 * Prayer Gallery home: warm ivory, editorial. Prayer periods are small abstract landscapes; the current one is marked
 * with a cobalt rule, a bold name and the words NOW / NEXT (never colour alone).
 *   landscape → header, hero band (name · art · time and details button), six period cards, bottom rail;
 *   portrait (phones, portrait tablets) → header, hero, one row per prayer, doors.
 * Every value is live; the same doors, Ramaḍān lines and gestures as the other homes.
 */

@Composable
fun GalleryHome(state: PrayerState, settings: AppSettings, a: HomeActions) {
    val tk = ThemeTokenSets.gallery
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    val hero = heroInfo(state, settings, kicker(state, settings))
    val doorList = doors(state, settings, a)
    val dim = settings.nightDim && state.period == Prayer.ISHA && !state.justPassed
    BoxWithConstraints(Modifier.fillMaxSize().background(tk.backgroundBrush)) {
        val bw = maxWidth; val bh = maxHeight
        val wide = bw > bh * 1.15f
        if (wide) {
            val u: Dp = minOf(bw / 100, bh / 60)
            GalleryWide(state, settings, a, tk, hero, doorList, u) { why = it }
        } else {
            GalleryStacked(state, settings, a, tk, hero, doorList, bw, bh) { why = it }
        }
        // a night-time dim is applied as a warm scrim, not a colour swap, so text contrast is unchanged in kind
        if (dim) Box(Modifier.fillMaxSize().background(Color(0xFF1A1208).copy(alpha = 0.22f)))
    }
}

/* ───────────────────────────── landscape ───────────────────────────── */

@Composable
private fun GalleryWide(state: PrayerState, s: AppSettings, a: HomeActions, tk: ThemeTokens, hero: HeroInfo, doorList: List<Door>, u: Dp, onWhy: (Prayer) -> Unit) {
    val F = uiFont(s); val urdu = L10n.isUrdu(s)
    Column(Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding()) {
        GalleryHeader(s, a, tk, u, F, urdu, Modifier.fillMaxWidth().padding(horizontal = u * 3.4f, vertical = u * 0.8f))
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        GalleryDate(state, s, tk, u, F, urdu, Modifier.fillMaxWidth().padding(top = u * 0.9f))
        // hero band
        Row(Modifier.weight(0.92f).fillMaxWidth().padding(horizontal = u * 3.4f, vertical = u * 0.8f), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).fillMaxHeight()) { FitHeight(Modifier.fillMaxSize()) { GalleryHeroLeft(hero, state, s, tk, u, F, urdu) } }
            ThemedArtwork(portrait = false, modifier = Modifier.weight(0.75f).fillMaxHeight().padding(horizontal = u * 1.5f).clip(RoundedCornerShape(tk.cornerLarge)), scrim = 0.12f)
            Box(Modifier.weight(1f).fillMaxHeight()) { FitHeight(Modifier.fillMaxSize()) { GalleryHeroRight(hero, tk, u, F, urdu) { onWhy(hero.prayer) } } }
        }
        // period cards
        Row(Modifier.weight(1.3f).fillMaxWidth()) {
            listedPrayers(s).forEach { p -> GalleryCard(rowInfo(p, state, s), s, tk, u, F, urdu, a.onToggleRelative, Modifier.weight(1f).fillMaxHeight()) { onWhy(p) } }
        }
        GalleryRail(doorList, tk, u, F, urdu, Modifier.fillMaxWidth().padding(horizontal = u * 3.4f, vertical = u * 0.5f))
    }
}

@Composable
private fun GalleryHeader(s: AppSettings, a: HomeActions, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, modifier: Modifier) {
    fun fs(x: Float) = (u.value * x).sp
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        GalleryMark(Modifier.size(u * 3.6f).clearAndSetSemantics { }, tk.contentPrimary)
        Spacer(Modifier.width(u * 1f))
        Text("Miqaat", fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontSize = fs(3.6f), color = tk.contentPrimary, maxLines = 1, modifier = Modifier.semantics { heading() })
        Box(Modifier.padding(horizontal = u * 1.6f).width(1.dp).height(u * 3.2f).background(tk.divider))
        Row(
            Modifier.clip(RoundedCornerShape(50)).heightIn(min = 48.dp).clickable(onClick = a.onOpenLocation, role = Role.Button).padding(end = u * 1.2f)
                .semantics(mergeDescendants = true) { contentDescription = "Location: ${s.locationName}. Opens location settings" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 2.4f), tint = tk.contentPrimary)
            Spacer(Modifier.width(u * 0.7f))
            Text(s.locationName, fontFamily = F, fontSize = fs(2.0f), color = tk.contentPrimary, maxLines = 1)
        }
        Spacer(Modifier.weight(1f))
        if (s.kidsMode) GalleryNav(Icons.Outlined.MenuBook, Str[R.string.s_theme_home_nav_learn], Str[R.string.s_learn_salah], tk, u, F, a.onOpenLearn)
        GalleryNav(Icons.Outlined.CalendarMonth, Str[R.string.s_theme_home_nav_timetable], Str[R.string.s_monthly_timetable], tk, u, F, a.onOpenTimetable)
        GalleryNav(Icons.Outlined.Settings, Str[R.string.s_theme_home_nav_settings], Str[R.string.s_settings], tk, u, F, a.onOpenSettings)
    }
}

@Composable
private fun GalleryNav(icon: ImageVector, label: String, description: String, tk: ThemeTokens, u: Dp, F: FontFamily, onClick: () -> Unit) {
    Row(
        Modifier.padding(start = u * 0.8f).heightIn(min = 48.dp).clip(RoundedCornerShape(50)).clickable(onClick = onClick, role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = description }.padding(horizontal = u * 1.2f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(u * 2.4f), tint = tk.contentPrimary)
        Spacer(Modifier.width(u * 0.7f))
        Text(label, fontFamily = F, fontSize = (u.value * 1.7f).sp, color = tk.contentPrimary, maxLines = 1)
    }
}

@Composable
private fun GalleryDate(state: PrayerState, s: AppSettings, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, modifier: Modifier) {
    fun fs(x: Float) = (u.value * x).sp
    val hij = PrayerEngine.hijri(state.now.toLocalDate(), s.hijriOffsetDays)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(L10n.date(s, state.now), fontFamily = if (urdu) F else Cormorant, fontWeight = FontWeight.Medium, fontSize = fs(2.6f), color = tk.contentPrimary, maxLines = 1, textAlign = TextAlign.Center)
        if (s.showHijri) Text(L10n.hijri(s, hij), fontFamily = F, fontSize = fs(1.6f), letterSpacing = if (urdu) 0.sp else fs(0.1f), color = tk.contentSecondary, maxLines = 1, textAlign = TextAlign.Center)
    }
}

@Composable
private fun GalleryHeroLeft(hero: HeroInfo, state: PrayerState, s: AppSettings, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, scale: Float = 1f) {
    val density = LocalDensity.current
    fun fs(x: Float) = (u.value * x * scale).sp
    fun fd(x: Float) = with(density) { (u * x * scale).toSp() }
    Column(Modifier.semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() }) {
        hero.special?.let { Text(it, fontFamily = Nunito, fontSize = fs(1.5f), fontWeight = FontWeight.Bold, color = tk.accent, modifier = Modifier.padding(bottom = u * 0.5f)) }
        Text(hero.kickerLabel, fontFamily = F, fontSize = fs(1.35f), letterSpacing = if (urdu) 0.sp else fs(0.4f), color = tk.contentSecondary)
        Text(hero.label, fontFamily = if (urdu) F else Cormorant, fontWeight = FontWeight.Medium, fontSize = fd(if (urdu) 5.6f else 7.6f), lineHeight = fd(8.2f), color = tk.contentPrimary, maxLines = 1)
        Text(hero.arabic, fontFamily = tk.fontArabic, fontSize = fd(5.2f), lineHeight = fd(6.4f), color = tk.accent)
        Row(Modifier.padding(top = u * 0.8f), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (hero.justPassed) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule, null, Modifier.size(u * 2.4f * scale), tint = tk.accent)
            Spacer(Modifier.width(u * 0.8f))
            Text(hero.status, fontFamily = F, fontSize = fs(if (urdu) 2.3f else 2.1f), color = tk.contentPrimary, maxLines = 2)
        }
        fastProgress(state, s)?.let { (frac, label) ->
            Column(Modifier.padding(top = u * 1f).width(u * 26 * scale)) {
                Box(Modifier.fillMaxWidth().height(u * 0.5f).clip(RoundedCornerShape(50)).background(tk.divider)) { Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(tk.primary)) }
                Text(label, fontFamily = Nunito, fontSize = fs(1.3f), color = tk.primary, modifier = Modifier.padding(top = u * 0.4f))
            }
        }
        tarawihLine(state, s)?.let { Text(it, fontFamily = Nunito, fontSize = fs(1.3f), color = tk.primary, modifier = Modifier.padding(top = u * 0.6f), maxLines = 3) }
    }
}

@Composable
private fun GalleryHeroRight(hero: HeroInfo, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, scale: Float = 1f, onDetails: () -> Unit) {
    val density = LocalDensity.current
    fun fd(x: Float) = with(density) { (u * x * scale).toSp() }
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.Top) {
            Text(hero.clock, fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = fd(12.5f), lineHeight = fd(12f), color = tk.contentPrimary, maxLines = 1)
            if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = tk.fontDisplay, fontSize = fd(3.4f), color = tk.contentSecondary, modifier = Modifier.padding(top = u * 2.4f * scale))
        }
        Spacer(Modifier.height(u * 1.4f))
        Row(
            Modifier.clip(RoundedCornerShape(50)).background(tk.primary).heightIn(min = 48.dp).clickable(onClick = onDetails, role = Role.Button).padding(horizontal = u * 2.2f, vertical = u * 0.8f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.MenuBook, null, Modifier.size(u * 2.4f * scale), tint = tk.onPrimary)
            Spacer(Modifier.width(u * 1.2f))
            Text(Str[R.string.s_view_prayer_details], fontFamily = F, fontWeight = FontWeight.Medium, fontSize = (u.value * 2.0f * scale).sp, color = tk.onPrimary, maxLines = 1)
            Spacer(Modifier.width(u * 1.2f))
            Icon(Icons.Outlined.ChevronRight, null, Modifier.size(u * 2.2f * scale), tint = tk.onPrimary)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryCard(r: RowInfo, s: AppSettings, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, onToggle: () -> Unit, modifier: Modifier, onWhy: () -> Unit) {
    fun fs(x: Float) = (u.value * x).sp
    val selected = r.isNow || r.isNext
    val ink = if (selected) tk.primary else tk.contentPrimary
    Box(
        modifier.background(if (selected) tk.surface else Color.Transparent)
            .combinedClickable(onClick = onToggle, onLongClick = onWhy, onClickLabel = "Switch between clock time and time until", onLongClickLabel = "Why this time?", role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = r.spoken }
    ) {
        Column(Modifier.fillMaxSize()) {
            PrayerCardArt(r.prayer, Modifier.weight(0.5f).fillMaxWidth().clearAndSetSemantics { })
            Column(Modifier.weight(0.5f).fillMaxWidth().padding(horizontal = u * 0.6f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                FitHeight(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(r.label, fontFamily = if (urdu) F else Cormorant, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = fs(if (urdu) 2.3f else 2.7f), color = ink, maxLines = 1)
                            when {
                                r.isNow || r.isNext -> Text("  · " + L10n.word(s, if (r.isNow) "NOW" else "NEXT").lowercase(), fontFamily = Nunito, fontSize = fs(1.15f), fontWeight = FontWeight.Bold, color = tk.primary)
                                r.done -> Icon(Icons.Outlined.Check, null, Modifier.padding(start = u * 0.6f).size(u * 1.5f), tint = tk.success)
                                r.prayer.isPrayer -> Icon(if (r.azaanOn) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null, Modifier.padding(start = u * 0.6f).size(u * 1.5f), tint = tk.contentMuted)
                            }
                        }
                        if (!urdu) Text(r.arabic, fontFamily = tk.fontArabic, fontSize = fs(2.0f), lineHeight = fs(2.5f), color = tk.accent, maxLines = 1)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(if (s.showRelative) r.relative else r.clock, fontFamily = if (s.showRelative) F else tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = fs(if (s.showRelative) 2.2f else 4.2f), lineHeight = fs(4.6f), color = ink, maxLines = 1, modifier = Modifier.alpha(if (r.done) 0.75f else 1f))
                            if (!s.showRelative && r.suffix.isNotEmpty()) Text(" ${r.suffix}", fontFamily = tk.fontDisplay, fontSize = fs(1.6f), color = tk.contentSecondary, modifier = Modifier.padding(bottom = u * 0.5f))
                        }
                        if (r.small.isNotEmpty()) Text(r.small, fontFamily = F, fontSize = fs(1.1f), color = tk.contentSecondary, maxLines = 2, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        if (selected) {
            Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(3.dp).background(tk.primary))
            Box(Modifier.align(Alignment.TopCenter).offset(y = (-4).dp).size(9.dp).clip(CircleShape).background(tk.primary))
        }
        Box(
            Modifier.align(Alignment.TopEnd).padding(u * 0.3f).size(48.dp).clip(CircleShape).clickable(onClick = onWhy, role = Role.Button).semantics { contentDescription = Str[R.string.s_why_this_time] },
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.size(u * 3.2f).clip(CircleShape).background(tk.surface.copy(alpha = 0.78f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Info, null, Modifier.size(u * 1.9f), tint = tk.contentPrimary) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GalleryRail(doorList: List<Door>, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, modifier: Modifier) {
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        Row(Modifier.fillMaxWidth().padding(top = u * 0.3f), verticalAlignment = Alignment.CenterVertically) {
            FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(u * 2.2f), verticalArrangement = Arrangement.spacedBy(u * 0.2f)) {
                doorList.forEach { d ->
                    val warn = d.warn
                    Row(
                        Modifier.clip(RoundedCornerShape(50)).then(if (d.onClick != null) Modifier.clickable(onClick = d.onClick, role = Role.Button) else Modifier).heightIn(min = 48.dp).padding(horizontal = u * 0.4f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(u * 3.6f).clip(CircleShape).background(if (warn) tk.warning else tk.accent), contentAlignment = Alignment.Center) {
                            Icon(if (warn) Icons.Outlined.NotificationsActive else Icons.Outlined.ChevronRight, null, Modifier.size(u * 2.0f), tint = Color.White)
                        }
                        Spacer(Modifier.width(u * 0.9f))
                        Text(d.label, fontFamily = if (urdu) F else Cormorant, fontWeight = FontWeight.Medium, fontSize = (u.value * 1.9f).sp, color = if (warn) tk.warning else tk.contentPrimary, maxLines = 2)
                    }
                }
            }
            Text(Str[R.string.s_theme_signature], fontFamily = if (urdu) F else Cormorant, fontSize = (u.value * 1.4f).sp, color = tk.contentSecondary, maxLines = 1)
        }
    }
}

/* ───────────────────────────── portrait / phone ───────────────────────────── */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryStackedRow(r: RowInfo, s: AppSettings, tk: ThemeTokens, F: FontFamily, urdu: Boolean, onToggle: () -> Unit, onWhy: () -> Unit) {
    val selected = r.isNow || r.isNext
    val ink = if (selected) tk.primary else tk.contentPrimary
    Row(
        Modifier.fillMaxWidth().heightIn(min = 72.dp).background(if (selected) tk.surface else Color.Transparent)
            .combinedClickable(onClick = onToggle, onLongClick = onWhy, onClickLabel = "Switch between clock time and time until", onLongClickLabel = "Why this time?", role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = r.spoken },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(56.dp).background(if (selected) tk.primary else Color.Transparent))
        PrayerCardArt(r.prayer, Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp).size(width = 64.dp, height = 56.dp).clip(RoundedCornerShape(10.dp)).clearAndSetSemantics { })
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.label, fontFamily = if (urdu) F else Cormorant, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = 22.sp, color = ink, maxLines = 1)
                when {
                    r.isNow || r.isNext -> Text("  · " + L10n.word(s, if (r.isNow) "NOW" else "NEXT").lowercase(), fontFamily = Nunito, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tk.primary)
                    r.done -> Icon(Icons.Outlined.Check, null, Modifier.padding(start = 6.dp).size(16.dp), tint = tk.success)
                    r.prayer.isPrayer -> Icon(if (r.azaanOn) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null, Modifier.padding(start = 6.dp).size(16.dp), tint = tk.contentMuted)
                }
            }
            if (!urdu) Text(r.arabic, fontFamily = tk.fontArabic, fontSize = 17.sp, color = tk.accent, maxLines = 1)
            if (r.small.isNotEmpty()) Text(r.small, fontFamily = F, fontSize = 12.sp, color = tk.contentSecondary, maxLines = 2)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(if (s.showRelative) r.relative else r.clock, fontFamily = if (s.showRelative) F else tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = if (s.showRelative) 17.sp else 30.sp, color = ink, maxLines = 1, modifier = Modifier.alpha(if (r.done) 0.75f else 1f))
            if (!s.showRelative && r.suffix.isNotEmpty()) Text(" ${r.suffix}", fontFamily = tk.fontDisplay, fontSize = 13.sp, color = tk.contentSecondary, modifier = Modifier.padding(bottom = 4.dp))
        }
        Box(Modifier.padding(horizontal = 4.dp).size(48.dp).clip(CircleShape).clickable(onClick = onWhy, role = Role.Button).semantics { contentDescription = Str[R.string.s_why_this_time] }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Info, null, Modifier.size(20.dp), tint = tk.contentMuted)
        }
    }
}

@Composable
private fun GalleryStacked(state: PrayerState, s: AppSettings, a: HomeActions, tk: ThemeTokens, hero: HeroInfo, doorList: List<Door>, w: Dp, h: Dp, onWhy: (Prayer) -> Unit) {
    val F = uiFont(s); val urdu = L10n.isUrdu(s)
    val roomy = w >= 600.dp && h >= 780.dp
    val u: Dp = if (roomy) minOf(w / 60, h / 100) else w / 60
    // On a phone w/60 is only ~6dp, which made the header, date and links 5-9sp. These floors keep them readable.
    val barU: Dp = if (roomy) u * 0.62f else maxOf(u * 0.62f, 7.5.dp)
    val railU: Dp = if (roomy) u * 0.5f else maxOf(u * 0.5f, 8.dp)
    val body: @Composable ColumnScope.() -> Unit = {
        GalleryHeader(s, a, tk, barU, F, urdu, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        GalleryDate(state, s, tk, barU, F, urdu, Modifier.fillMaxWidth().padding(top = 10.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                GalleryHeroLeft(hero, state, s, tk, u * 0.62f, F, urdu)
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(hero.clock, fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = 56.sp, color = tk.contentPrimary, maxLines = 1)
            if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = tk.fontDisplay, fontSize = 20.sp, color = tk.contentSecondary, modifier = Modifier.padding(top = 18.dp))
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(tk.primary).heightIn(min = 48.dp).clickable(onClick = { onWhy(hero.prayer) }, role = Role.Button).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Str[R.string.s_view_prayer_details], fontFamily = F, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = tk.onPrimary, maxLines = 2)
                Icon(Icons.Outlined.ChevronRight, null, Modifier.size(20.dp), tint = tk.onPrimary)
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        val shown = listedPrayers(s)
        shown.forEachIndexed { i, p ->
            GalleryStackedRow(rowInfo(p, state, s), s, tk, F, urdu, a.onToggleRelative) { onWhy(p) }
            if (i < shown.lastIndex) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(tk.divider))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        GalleryRail(doorList, tk, railU, F, urdu, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp))
        if (s.showDisliked) DayThread(s, state.today, state.now, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), labelSize = 9.sp, fullNames = false, gnomon = true)
    }
    val page = Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding().navigationBarsPadding()
    if (roomy) FitHeight(page) { Column(Modifier.fillMaxWidth(), content = body) }
    else Column(page.verticalScroll(rememberScrollState()), content = body)
}
