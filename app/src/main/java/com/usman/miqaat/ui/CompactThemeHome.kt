package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import java.time.Duration

/** The approved short landscape board. Neither Home column is a scroll container. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactThemeHome(state: PrayerState, settings: AppSettings, actions: HomeActions) {
    val tk = ThemeTokenSets.of(settings.theme)
    val gallery = settings.theme == AppTheme.PRAYER_GALLERY
    val urdu = L10n.isUrdu(settings)
    val font = if (urdu) tk.fontArabic else tk.fontDisplay
    val ink = if (gallery) Color(0xFF072D40) else tk.contentPrimary
    val hero = heroInfo(state, settings, kicker(state, settings))
    var why by remember { mutableStateOf<Prayer?>(null) }
    var more by remember { mutableStateOf(false) }
    why?.let { WhyDialog(settings, state.today, it) { why = null } }
    Box(Modifier.fillMaxSize().background(if (gallery) tk.background else Color(0xFF03182D))) {
        if (!gallery) Image(painterResource(R.drawable.celestial_landscape_approved), null,
            Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.35f)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().displayCutoutPadding()
            .padding(horizontal = 12.dp).testTag("landscape-fit-board")) {
            Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f).fillMaxHeight().clickable(role = Role.Button, onClick = actions.onOpenLocation),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, null, Modifier.size(24.dp), tint = if (gallery) ink else tk.primary)
                    Text(settings.locationName, Modifier.weight(1f).padding(start = 6.dp).testTag("landscape-location"),
                        fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, maxLines = 1, color = ink)
                }
                Text(L10n.dateShort(settings, state.now), Modifier.width(if (urdu) 120.dp else 96.dp).padding(horizontal = 8.dp).testTag("landscape-date"),
                    fontFamily = font, fontSize = 17.sp, maxLines = 1, textAlign = if (gallery) TextAlign.End else TextAlign.Center, color = ink)
                Row(Modifier.then(if (gallery) Modifier else Modifier.weight(1f)), horizontalArrangement = Arrangement.End) {
                if (settings.kidsMode) IconButton(actions.onOpenLearn, Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.MenuBook, Str[R.string.s_learn_salah], Modifier.size(26.dp), tint = if (gallery) ink else tk.primary)
                }
                IconButton(actions.onOpenTimetable, Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.CalendarMonth, Str[R.string.s_monthly_timetable], Modifier.size(26.dp), tint = if (gallery) ink else tk.primary)
                }
                IconButton(actions.onOpenSettings, Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Settings, Str[R.string.s_settings], Modifier.size(26.dp), tint = if (gallery) ink else tk.primary)
                }
                Box {
                    IconButton({ more = true }, Modifier.size(48.dp).testTag("landscape-more-actions")) {
                        Icon(Icons.Outlined.MoreHoriz, L10n.word(settings, "More actions"), tint = if (gallery) ink else tk.primary)
                    }
                    DropdownMenu(more, { more = false }) {
                        adhkarModes(state, settings).forEach { mode ->
                            val label = L10n.word(settings, when (mode) {
                                AdhkarMode.MORNING -> "Morning adhkār"
                                AdhkarMode.EVENING -> "Evening adhkār"
                                AdhkarMode.POST -> "After-prayer adhkār"
                            })
                            DropdownMenuItem(text = { Text(label) }, onClick = { more = false; actions.onOpenAdhkar(mode) })
                        }
                        doors(state, settings, actions).forEach { link ->
                            DropdownMenuItem(text = { Text(link.label, color = if (link.warn) tk.warning else tk.contentPrimary) },
                                onClick = { more = false; link.onClick?.invoke() })
                        }
                        listOfNotNull(hero.special, fastProgress(state, settings)?.second, tarawihLine(state, settings)).forEach { line ->
                            DropdownMenuItem(text = { Text(line) }, onClick = {}, enabled = false)
                        }
                    }
                }
                }
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val short = maxHeight < 300.dp
                val gap = ((maxHeight - 288.dp) / 5).coerceIn(0.dp, 4.dp)
                val prayers = listedPrayers(settings)
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(0.54f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ApprovedLandscapeHero(state, settings, actions, hero, tk, ink, short,
                            Modifier.weight(1f).fillMaxWidth()) { why = hero.prayer }
                        Column(Modifier.fillMaxWidth().height(if (settings.showDisliked) 76.dp else 24.dp)
                            .testTag("landscape-timeline-panel")
                            .then(if (gallery) Modifier else Modifier.clip(RoundedCornerShape(12.dp))
                                .background(Color(0xCC03182D)).border(1.dp, tk.divider, RoundedCornerShape(12.dp)))
                            .padding(horizontal = 12.dp), verticalArrangement = Arrangement.Center) {
                            if (settings.showHijri) Text(L10n.hijri(settings, PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)),
                                Modifier.fillMaxWidth().testTag("landscape-hijri"), fontFamily = font, fontSize = 14.sp, maxLines = 1, color = ink)
                            if (settings.showDisliked) ApprovedLandscapeTimeline(state, settings, tk, ink, Modifier.fillMaxWidth().height(48.dp))
                        }
                    }
                    Column(Modifier.weight(0.46f).fillMaxHeight().testTag("landscape-prayer-list"),
                        verticalArrangement = Arrangement.spacedBy(gap)) {
                        prayers.forEach { prayer ->
                            val row = rowInfo(prayer, state, settings)
                            val selected = row.isNow || row.isNext
                            val shape = RoundedCornerShape(if (gallery) 8.dp else 12.dp)
                            val surface = if (gallery) {
                                if (selected) Color(0xFFFFEEDD) else tk.surface
                            } else if (selected) Color(0xFF23323D) else Color(0xE60A2238)
                            Row(Modifier.weight(1f).fillMaxWidth().testTag("compact-prayer-${prayer.name}").clip(shape)
                                .background(surface).border(if (selected) 1.5.dp else 1.dp,
                                    if (selected) (if (gallery) tk.accent else tk.primary) else tk.divider, shape)
                                .combinedClickable(onClick = actions.onToggleRelative, onLongClick = { why = prayer }, role = Role.Button,
                                    onClickLabel = Str[R.string.s_switch_clock_time_until], onLongClickLabel = Str[R.string.s_why_this_time])
                                .semantics(mergeDescendants = true) { contentDescription = row.spoken }
                                .padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                val icon = when (prayer) {
                                    Prayer.FAJR, Prayer.SUNRISE, Prayer.MAGHRIB -> Icons.Outlined.WbTwilight
                                    Prayer.ISHA -> Icons.Outlined.DarkMode
                                    Prayer.ASR -> Icons.Outlined.WbCloudy
                                    else -> Icons.Outlined.LightMode
                                }
                                Box(Modifier.size(if (short) 28.dp else 34.dp)
                                    .then(if (gallery) Modifier else Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.08f))),
                                    contentAlignment = Alignment.Center) {
                                    Icon(icon, null, Modifier.size(if (short) 22.dp else 26.dp), tint = if (gallery) {
                                        when (prayer) { Prayer.DHUHR, Prayer.ISHA -> tk.primary; Prayer.SUNRISE, Prayer.ASR -> Color(0xFFB26900); else -> tk.accent }
                                    } else tk.primary)
                                }
                                Text(buildAnnotatedString {
                                    append(row.label)
                                    if (selected) withStyle(SpanStyle(color = if (gallery) tk.accent else tk.primary, fontWeight = FontWeight.Bold, fontSize = if (short) 12.sp else TextUnit.Unspecified)) {
                                        append((if (short) "\n" else " · ") + L10n.word(settings, if (row.isNow) "NOW" else "NEXT"))
                                    }
                                },
                                    Modifier.weight(1f).padding(horizontal = 8.dp).testTag("landscape-row-name-${prayer.name}"),
                                    fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = if (urdu) 17.sp else if (short) 18.sp else 21.sp,
                                    color = ink, maxLines = if (short && selected) 2 else 1, lineHeight = if (short && selected) 20.sp else TextUnit.Unspecified)
                                Text(L10n.iso(if (settings.showRelative) row.relative else "${row.clock} ${row.suffix}".trim()),
                                    Modifier.width(if (urdu && settings.showRelative) 140.dp else if (urdu) 104.dp else if (settings.showRelative) 112.dp else 104.dp).testTag("landscape-row-time-${prayer.name}"), fontFamily = font,
                                    fontWeight = FontWeight.Medium, fontSize = if (urdu && settings.showRelative) 14.sp else if (urdu) 15.sp else if (settings.showRelative) 17.sp else 20.sp,
                                    maxLines = if (urdu && settings.showRelative) 2 else 1,
                                    lineHeight = if (urdu && settings.showRelative) 17.sp else TextUnit.Unspecified,
                                    textAlign = TextAlign.End, color = ink)
                                Icon(Icons.Outlined.ChevronRight, null, Modifier.padding(start = 6.dp).size(16.dp), tint = tk.contentSecondary)
                            }
                        }
                    }
                }
            }
        }
        if (settings.nightDim && state.period == Prayer.ISHA && !state.justPassed) Box(Modifier.fillMaxSize().background(tk.background.copy(alpha = 0.3f)))
    }
}

@Composable
private fun ApprovedLandscapeHero(state: PrayerState, s: AppSettings, a: HomeActions, hero: HeroInfo, tk: ThemeTokens,
    ink: Color, short: Boolean, modifier: Modifier, onDetails: () -> Unit) {
    val gallery = s.theme == AppTheme.PRAYER_GALLERY
    val urdu = L10n.isUrdu(s)
    val font = if (urdu) tk.fontArabic else tk.fontDisplay
    val shape = RoundedCornerShape(if (gallery) 0.dp else 12.dp)
    Box(modifier.clip(shape).testTag("landscape-authored-hero")
        .then(if (gallery) Modifier else Modifier.border(1.dp, tk.divider, shape))) {
        Image(painterResource(if (gallery) R.drawable.gallery_landscape_approved else R.drawable.celestial_landscape_approved), null,
            Modifier.fillMaxSize().testTag(if (gallery) "landscape-gallery-art" else "landscape-celestial-art"), contentScale = ContentScale.Crop)
        if (gallery && urdu) Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, tk.background.copy(alpha = 0.95f)))))
        if (!gallery) Box(Modifier.fillMaxSize().background(Color(0xFF03182D).copy(alpha = 0.20f)))
        val pad = if (short) 10.dp else 16.dp
        Row(Modifier.fillMaxSize().padding(pad), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(0.53f).fillMaxHeight().semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() },
                verticalArrangement = Arrangement.SpaceBetween) {
                Text(hero.kickerLabel, Modifier.testTag("landscape-hero-kicker"), fontFamily = if (urdu) font else tk.fontUi,
                    fontWeight = FontWeight.SemiBold, fontSize = if (short) 10.sp else 12.sp, maxLines = 1,
                    letterSpacing = if (urdu) 0.sp else 1.5.sp, color = if (gallery) ink else tk.primary)
                Text(hero.label, Modifier.fillMaxWidth().testTag("landscape-hero-name"), fontFamily = font,
                    fontWeight = FontWeight.SemiBold, fontSize = if (short) 32.sp else 40.sp, maxLines = 1, lineHeight = if (short) 34.sp else 42.sp, color = ink)
                if (!urdu) Text(hero.arabic, fontFamily = tk.fontArabic, fontSize = if (short) 19.sp else 23.sp,
                    lineHeight = if (short) 22.sp else 27.sp, color = if (gallery) ink else tk.primary)
                Text(L10n.iso("${hero.clock} ${hero.suffix}".trim()), Modifier.fillMaxWidth().testTag("landscape-hero-clock"),
                    fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = if (short) 29.sp else 34.sp,
                    maxLines = 1, lineHeight = if (short) 32.sp else 38.sp, color = ink)
                Text(hero.status, Modifier.fillMaxWidth().testTag("landscape-hero-status"), fontFamily = font,
                    fontSize = if (short) 12.sp else 15.sp, maxLines = 2, lineHeight = if (short) 14.sp else 18.sp,
                    color = if (gallery) tk.contentSecondary else tk.contentPrimary)
                Row(Modifier.fillMaxWidth().height(48.dp).testTag("landscape-hero-details")
                    .clip(RoundedCornerShape(24.dp)).background(if (gallery) tk.primary else Color(0xCC03182D))
                    .then(if (gallery) Modifier else Modifier.border(1.dp, tk.primary, RoundedCornerShape(24.dp)))
                    .clickable(role = Role.Button, onClick = onDetails).padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(Str[R.string.s_view_prayer_details], Modifier.weight(1f), fontFamily = font, maxLines = 1,
                        fontSize = if (short) 12.sp else 14.sp, color = if (gallery) tk.onPrimary else tk.contentPrimary)
                    Icon(Icons.Outlined.ChevronRight, null, Modifier.size(18.dp), tint = if (gallery) tk.onPrimary else tk.contentPrimary)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(0.47f).fillMaxHeight(), contentAlignment = if (gallery) Alignment.BottomCenter else Alignment.Center) {
                if (gallery) {
                    if (s.kidsMode) Row(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF0DD)).testTag("landscape-gallery-learn")
                        .clickable(role = Role.Button, onClick = a.onOpenLearn).padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.WbTwilight, null, Modifier.size(20.dp), tint = tk.accent)
                        Text(if (urdu) Str[R.string.s_learn_salah] else "Prepare for ${hero.label}", Modifier.weight(1f).padding(horizontal = 6.dp),
                            fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = if (short) 12.sp else 14.sp, maxLines = 2, color = ink)
                        Icon(Icons.Outlined.ChevronRight, null, Modifier.size(16.dp), tint = tk.accent)
                    }
                } else {
                    Box(Modifier.fillMaxWidth().aspectRatio(1f).testTag("landscape-countdown-dial"), contentAlignment = Alignment.Center) {
                        val remaining = landscapeCountdownFraction(state)
                        Canvas(Modifier.fillMaxSize()) {
                            val inset = 8.dp.toPx(); val d = size.minDimension - 2 * inset
                            val top = Offset((size.width - d) / 2, (size.height - d) / 2)
                            drawArc(tk.contentSecondary.copy(alpha = 0.45f), 0f, 360f, false, top, Size(d, d), style = Stroke(3.dp.toPx()))
                            drawArc(tk.primary, -90f, 360f * remaining, false, top, Size(d, d), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                            drawCircle(tk.primary, 5.dp.toPx(), Offset(size.width / 2, top.y))
                        }
                        Icon(Icons.Outlined.LightMode, null, Modifier.align(Alignment.TopCenter).size(18.dp).background(Color(0xFF03182D), RoundedCornerShape(50)), tint = tk.primary)
                        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(L10n.duration(s, state.delta), Modifier.fillMaxWidth().testTag("landscape-countdown-value"),
                                fontFamily = font, fontSize = if (urdu || short) 17.sp else 22.sp,
                                maxLines = 2, lineHeight = 23.sp, textAlign = TextAlign.Center, color = tk.contentPrimary)
                            Text(if (urdu) hero.kickerLabel else "${if (hero.justPassed) "Since" else "Until"} ${hero.label}",
                                fontFamily = font, fontSize = if (short) 13.sp else 16.sp, textAlign = TextAlign.Center, color = tk.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovedLandscapeTimeline(state: PrayerState, s: AppSettings, tk: ThemeTokens, ink: Color, modifier: Modifier) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val gallery = s.theme == AppTheme.PRAYER_GALLERY
    val periods = listedPrayers(s)
    val active = if (state.heroTime.toLocalDate() == state.now.toLocalDate()) state.hero else state.current
    val font = if (L10n.isUrdu(s)) tk.fontArabic else tk.fontDisplay
    Box(modifier.testTag("landscape-circular-timeline")) {
        Canvas(Modifier.fillMaxWidth().height(20.dp)) {
            val x0 = size.width / (periods.size * 2)
            drawLine(tk.contentSecondary.copy(alpha = 0.7f), Offset(x0, size.height / 2), Offset(size.width - x0, size.height / 2), 1.dp.toPx())
            periods.forEachIndexed { i, p ->
                val slot = if (rtl) periods.lastIndex - i else i
                val pt = Offset((slot + 0.5f) * size.width / periods.size, size.height / 2)
                val selected = p == active
                drawCircle(if (selected) (if (gallery) tk.accent else tk.primary) else tk.background, if (selected) 5.dp.toPx() else 3.5.dp.toPx(), pt)
                drawCircle(if (selected) (if (gallery) tk.accent else tk.primary) else tk.contentSecondary,
                    if (selected) 5.dp.toPx() else 3.5.dp.toPx(), pt, style = Stroke(1.dp.toPx()))
            }
        }
        Row(Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
            periods.forEach { p -> Text(L10n.prayer(s, p), Modifier.weight(1f).testTag("landscape-timeline-${p.name}"),
                fontFamily = font, fontSize = if (L10n.isUrdu(s)) 11.sp else 12.sp, maxLines = 1,
                textAlign = TextAlign.Center, color = if (p == active) (if (gallery) tk.accent else tk.primary) else ink) }
        }
    }
}

/** Remaining share of the previous-to-next listed period, including yesterday before Fajr. */
internal fun landscapeCountdownFraction(state: PrayerState): Float {
    if (state.justPassed) return 1f
    val previous = Prayer.entries.flatMap { p -> listOf(state.today[p], state.today[p].minusDays(1)) }
        .filter { it.isBefore(state.heroTime) }.maxOrNull() ?: return 1f
    val total = Duration.between(previous, state.heroTime).seconds.coerceAtLeast(1)
    return (state.delta.seconds.toFloat() / total).coerceIn(0f, 1f)
}
