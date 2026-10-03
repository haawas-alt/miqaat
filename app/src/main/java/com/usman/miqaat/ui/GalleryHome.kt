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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.draw.paint
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
            GalleryWide(state, settings, a, tk, hero, doorList, bw, bh) { why = it }
        } else {
            GalleryStacked(state, settings, a, tk, hero, doorList, bw, bh) { why = it }
        }
        // a night-time dim is applied as a warm scrim, not a colour swap, so text contrast is unchanged in kind
        if (dim) Box(Modifier.fillMaxSize().background(Color(0xFF1A1208).copy(alpha = 0.22f)))
    }
}

/* ───────────────────────────── landscape ───────────────────────────── */

@Composable
private fun GalleryWide(state: PrayerState, s: AppSettings, a: HomeActions, tk: ThemeTokens, hero: HeroInfo, doorList: List<Door>, bw: Dp, bh: Dp, onWhy: (Prayer) -> Unit) {
    val F = uiFont(s); val urdu = L10n.isUrdu(s)
    // Everything is placed in the approved artwork's own coordinate space (1586 x 992), so the baked-in period tiles and the
    // prayer text always line up. `sc` is dp per artwork pixel; the artwork is fitted, never stretched.
    val sc = minOf(bw / 1586f, bh / 992f)
    val ox = (bw - sc * 1586f) / 2; val oy = (bh - sc * 992f) / 2
    fun Modifier.at(x: Float, y: Float, w: Float, h: Float) = this.absoluteOffset(ox + sc * x, oy + sc * y).size(sc * w, sc * h)
    val u = sc * 13.8f
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.AbsoluteAlignment.TopLeft) {
        ThemedArtwork(portrait = false, modifier = Modifier.at(0f, 104f, 1586f, 734f), scrim = 0f, fill = true)
        Box(Modifier.at(0f, 0f, 1586f, 104f).background(tk.background.copy(alpha = 0.55f))) {
            GalleryHeader(s, a, tk, u * 0.95f, F, urdu, Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding().padding(horizontal = sc * 52f))
        }
        Box(Modifier.at(0f, 103f, 1586f, 1.5f).background(tk.divider))
        GalleryDate(state, s, tk, u * 0.92f, F, urdu, Modifier.at(400f, 104f, 786f, 84f).padding(top = sc * 4f))
        Box(Modifier.at(120f, 196f, 500f, 290f).testTag("home-hero")) { FitHeight(Modifier.fillMaxSize()) { GalleryHeroLeft(hero, state, s, tk, u, F, urdu) } }
        Box(Modifier.at(1090f, 216f, 450f, 262f).testTag("home-time")) { FitHeight(Modifier.fillMaxSize()) { GalleryHeroRight(hero, tk, u, F, urdu) { onWhy(hero.prayer) } } }
        Row(Modifier.at(0f, 503f, 1586f, 362f).testTag("home-prayers")) {
            listedPrayers(s).forEach { p -> GalleryCard(rowInfo(p, state, s), s, tk, u, F, urdu, a.onToggleRelative, Modifier.weight(1f).fillMaxHeight()) { onWhy(p) } }
        }
        Box(Modifier.at(0f, 868f, 1586f, 124f)) {
            GalleryRail(doorList, tk, u * 0.9f, F, urdu, Modifier.fillMaxSize().navigationBarsPadding().displayCutoutPadding().padding(horizontal = sc * 40f), spread = true)
        }
    }
}

@Composable
private fun GalleryHeader(s: AppSettings, a: HomeActions, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, modifier: Modifier) {
    fun fs(x: Float) = (u.value * x).sp
    // Phone: the three destinations become icon-only and the location takes the leftover width, so Settings is never pushed off screen.
    val narrow = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 600
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (narrow) {
            // The wordmark is dropped on phones so the location keeps enough room to read; the mark still announces "Miqaat".
            GalleryMark(Modifier.size(u * 3.6f).semantics { contentDescription = "Miqaat"; heading() }, tk.contentPrimary)
            Spacer(Modifier.width(u * 1.6f))
        } else {
            GalleryMark(Modifier.size(u * 3.6f).clearAndSetSemantics { }, tk.contentPrimary)
            Spacer(Modifier.width(u * 1f))
            Text("Miqaat", fontFamily = Cormorant, fontWeight = FontWeight.Medium, fontSize = fs(3.6f), color = tk.contentPrimary, maxLines = 1, modifier = Modifier.semantics { heading() })
            Box(Modifier.padding(horizontal = u * 1.6f).width(1.dp).height(u * 3.2f).background(tk.divider))
        }
        Row(
            (if (narrow) Modifier.weight(1f) else Modifier).clip(RoundedCornerShape(50)).heightIn(min = 48.dp).clickable(onClick = a.onOpenLocation, role = Role.Button).padding(end = u * 1.2f)
                .semantics(mergeDescendants = true) { contentDescription = Str.get(R.string.s_a11y_location, L10n.iso(s.locationName)) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.LocationOn, null, Modifier.size(u * 2.4f), tint = tk.contentPrimary)
            Spacer(Modifier.width(u * 0.7f))
            Text(s.locationName, fontFamily = F, fontSize = fs(2.0f), color = tk.contentPrimary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        if (!narrow) Spacer(Modifier.weight(1f))
        if (s.kidsMode) GalleryNav(Icons.Outlined.MenuBook, Str[R.string.s_theme_home_nav_learn], Str[R.string.s_learn_salah], tk, u, F, a.onOpenLearn, showLabel = !narrow)
        GalleryNav(Icons.Outlined.CalendarMonth, Str[R.string.s_theme_home_nav_timetable], Str[R.string.s_monthly_timetable], tk, u, F, a.onOpenTimetable, showLabel = !narrow)
        GalleryNav(Icons.Outlined.Settings, Str[R.string.s_theme_home_nav_settings], Str[R.string.s_settings], tk, u, F, a.onOpenSettings, showLabel = !narrow)
    }
}

@Composable
private fun GalleryNav(icon: ImageVector, label: String, description: String, tk: ThemeTokens, u: Dp, F: FontFamily, onClick: () -> Unit, showLabel: Boolean = true) {
    Row(
        Modifier.padding(start = u * 0.8f).heightIn(min = 48.dp).widthIn(min = 48.dp).clip(RoundedCornerShape(50)).clickable(onClick = onClick, role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = description }.padding(horizontal = u * 1.2f),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, Modifier.size(if (showLabel) u * 2.4f else 26.dp), tint = tk.contentPrimary)
        if (showLabel) {
            Spacer(Modifier.width(u * 0.7f))
            Text(label, fontFamily = F, fontSize = (u.value * 1.7f).sp, color = tk.contentPrimary, maxLines = 1)
        }
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
        modifier.background(if (selected) tk.surface.copy(alpha = 0.55f) else Color.Transparent)
            .combinedClickable(onClick = onToggle, onLongClick = onWhy, onClickLabel = "Switch between clock time and time until", onLongClickLabel = "Why this time?", role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = r.spoken }
    ) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.weight(0.5f).fillMaxWidth())
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
private fun GalleryRail(doorList: List<Door>, tk: ThemeTokens, u: Dp, F: FontFamily, urdu: Boolean, modifier: Modifier, spread: Boolean = false) {
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        Row(Modifier.fillMaxWidth().padding(top = u * 0.3f), verticalAlignment = Alignment.CenterVertically) {
            FlowRow(Modifier.weight(1f), horizontalArrangement = if (spread) Arrangement.SpaceEvenly else Arrangement.spacedBy(u * 2.2f), verticalArrangement = Arrangement.spacedBy(u * 0.2f)) {
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
            if (!spread) Text(Str[R.string.s_theme_signature], fontFamily = if (urdu) F else Cormorant, fontSize = (u.value * 1.4f).sp, color = tk.contentSecondary, maxLines = 1)
        }
    }
}

/* ───────────────────────────── portrait / phone ───────────────────────────── */

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryStackedRow(r: RowInfo, s: AppSettings, tk: ThemeTokens, F: FontFamily, urdu: Boolean, onToggle: () -> Unit, onWhy: () -> Unit, k: Float = 1f) {
    val selected = r.isNow || r.isNext
    val ink = if (selected) tk.primary else tk.contentPrimary
    Row(
        Modifier.fillMaxWidth().heightIn(min = (80 * k).dp).background(if (selected) tk.surface else Color.Transparent)
            .combinedClickable(onClick = onToggle, onLongClick = onWhy, onClickLabel = "Switch between clock time and time until", onLongClickLabel = "Why this time?", role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = r.spoken },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(56.dp).background(if (selected) tk.primary else Color.Transparent))
        PrayerCardArt(r.prayer, Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp).size(width = (64 * k).dp, height = (56 * k).dp).clip(RoundedCornerShape(10.dp)).clearAndSetSemantics { })
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(r.label, fontFamily = if (urdu) F else Cormorant, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, fontSize = (24 * k).sp, color = ink, maxLines = 1)
                when {
                    r.isNow || r.isNext -> Text("  · " + L10n.word(s, if (r.isNow) "NOW" else "NEXT").lowercase(), fontFamily = Nunito, fontSize = (12 * k).sp, fontWeight = FontWeight.Bold, color = tk.primary)
                    r.done -> Icon(Icons.Outlined.Check, null, Modifier.padding(start = 6.dp).size(16.dp), tint = tk.success)
                    r.prayer.isPrayer -> Icon(if (r.azaanOn) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null, Modifier.padding(start = 6.dp).size(16.dp), tint = tk.contentMuted)
                }
            }
            if (!urdu) Text(r.arabic, fontFamily = tk.fontArabic, fontSize = (17 * k).sp, color = tk.accent, maxLines = 1)
            if (r.small.isNotEmpty()) Text(r.small, fontFamily = F, fontSize = (12 * k).sp, color = tk.contentSecondary, maxLines = 2)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(if (s.showRelative) r.relative else r.clock, fontFamily = if (s.showRelative) F else tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = if (s.showRelative) (17 * k).sp else (32 * k).sp, color = ink, maxLines = 1, modifier = Modifier.alpha(if (r.done) 0.75f else 1f))
            if (!s.showRelative && r.suffix.isNotEmpty()) Text(" ${r.suffix}", fontFamily = tk.fontDisplay, fontSize = (13 * k).sp, color = tk.contentSecondary, modifier = Modifier.padding(bottom = 4.dp))
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
    val compactPhone = w < 600.dp && w >= 340.dp && LocalDensity.current.fontScale <= 1.15f
    val body: @Composable ColumnScope.() -> Unit = {
        GalleryHeader(s, a, tk, barU, F, urdu, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        // Portrait crop of the approved artwork: only its top band (the arch and olive branch). The period tiles baked into the lower
        // part of the picture are never shown here, because this layout lists the prayers itself.
        val artBand = androidx.compose.ui.res.painterResource(R.drawable.art_gallery_portrait_v2)
        Column(
            Modifier.fillMaxWidth().drawBehind {
                val bandH = minOf(size.height, size.width * 0.86f)
                clipRect(bottom = bandH) {
                    with(artBand) { draw(androidx.compose.ui.geometry.Size(size.width, size.width * 1672f / 941f), alpha = 0.92f) }
                }
                drawRect(Brush.verticalGradient(listOf(tk.background.copy(alpha = 0.2f), tk.background.copy(alpha = 0.5f), tk.background), endY = bandH))
            }
        ) {
        GalleryDate(state, s, tk, barU, F, urdu, Modifier.fillMaxWidth().padding(top = 10.dp))
        if (compactPhone) {
            GalleryCompactHero(hero, state, s, tk, F, urdu, Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp))
        } else {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                GalleryHeroLeft(hero, state, s, tk, u * 0.62f, F, urdu)
            }
        }
        }
        val detailsButton: @Composable () -> Unit = {
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(tk.primary).heightIn(min = 48.dp).clickable(onClick = { onWhy(hero.prayer) }, role = Role.Button).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(Str[R.string.s_view_prayer_details], fontFamily = F, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = tk.onPrimary, maxLines = 2)
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, Modifier.size(20.dp), tint = tk.onPrimary)
            }
        }
        // On a phone the clock and the button no longer compete for one line (Urdu day-part words made the clock overflow).
        if (compactPhone) {
            Box(Modifier.padding(horizontal = 18.dp, vertical = 2.dp)) { detailsButton() }
        } else Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(hero.clock, fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = 56.sp, color = tk.contentPrimary, maxLines = 1)
                if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = if (urdu) F else tk.fontDisplay, fontSize = 20.sp, color = tk.contentSecondary, maxLines = 1, modifier = Modifier.padding(top = 18.dp))
                if (w >= 600.dp) { Spacer(Modifier.weight(1f)); detailsButton() }
            }
            if (w < 600.dp) Box(Modifier.padding(top = 6.dp)) { detailsButton() }
        }
        Spacer(Modifier.height(12.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        val shown = listedPrayers(s)
        shown.forEachIndexed { i, p ->
            GalleryStackedRow(rowInfo(p, state, s), s, tk, F, urdu, a.onToggleRelative, onWhy = { onWhy(p) }, k = if (roomy) 1.25f else 1f)
            if (i < shown.lastIndex) Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(1.dp).background(tk.divider))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(tk.divider))
        GalleryRail(doorList, tk, railU, F, urdu, Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp))
        if (s.showDisliked) DayThread(s, state.today, state.now, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), labelSize = 9.sp, fullNames = false, gnomon = true)
    }
    val page = Modifier.fillMaxSize().statusBarsPadding().displayCutoutPadding().navigationBarsPadding()
    // One screen, no scrolling, on every phone and tablet: the page is scaled down just enough to fit the height it is given.
    // (Large system text is handled by AccessibleHome, which is the one layout that scrolls.)
    FitHeight(page, verticalBias = 0f) { Column(Modifier.fillMaxWidth(), content = body) }
}

/** Compact phone hero: prayer and clock share a baseline; scales above normal use the flowing fallback. */
@Composable
private fun GalleryCompactHero(hero: HeroInfo, state: PrayerState, s: AppSettings, tk: ThemeTokens, F: FontFamily, urdu: Boolean, modifier: Modifier) {
    Column(modifier.testTag("gallery-mobile-hero").semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() }) {
        hero.special?.let { Text(it, fontFamily = F, fontSize = 12.sp, color = tk.accent) }
        Text(hero.kickerLabel, fontFamily = F, fontSize = 11.sp, letterSpacing = if (urdu) 0.sp else 1.6.sp, color = tk.contentSecondary)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(hero.label, fontFamily = if (urdu) F else Cormorant, fontWeight = FontWeight.Medium,
                fontSize = if (urdu) 30.sp else 38.sp, color = tk.contentPrimary, maxLines = 1,
                modifier = Modifier.weight(1f).alignByBaseline().testTag("gallery-mobile-prayer-name"))
            Text(hero.clock, fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium,
                fontSize = 44.sp, color = tk.contentPrimary, maxLines = 1,
                modifier = Modifier.alignByBaseline().testTag("gallery-mobile-prayer-time"))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (!urdu) Text(hero.arabic, fontFamily = tk.fontArabic, fontSize = 24.sp, color = tk.accent)
            Spacer(Modifier.weight(1f))
            if (hero.suffix.isNotEmpty()) Text(hero.suffix, fontFamily = if (urdu) F else tk.fontDisplay, fontSize = 14.sp, color = tk.contentSecondary)
        }
        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (hero.justPassed) Icons.Outlined.CheckCircle else Icons.Outlined.Schedule, null, Modifier.size(16.dp), tint = tk.accent)
            Spacer(Modifier.width(6.dp))
            Text(hero.status, fontFamily = F, fontSize = 13.sp, color = tk.contentPrimary, maxLines = 2)
        }
        fastProgress(state, s)?.let { (frac, label) ->
            Column(Modifier.padding(top = 6.dp).fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)).background(tk.divider)) {
                    Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(tk.primary))
                }
                Text(label, fontFamily = F, fontSize = 12.sp, color = tk.primary, modifier = Modifier.padding(top = 3.dp))
            }
        }
        tarawihLine(state, s)?.let {
            Text(it, fontFamily = F, fontSize = 12.sp, color = tk.primary, modifier = Modifier.padding(top = 4.dp), maxLines = 3)
        }
    }
}
