package com.usman.miqaat.ui

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.roundToInt

/** Approved Friday and quiet compositions. Only native text/actions carry prayer facts. */
private fun worshipFont(s: AppSettings) = if (L10n.isUrdu(s)) Amiri else FontFamily.Serif
private fun words(s: AppSettings, en: String, ur: String) = if (L10n.isUrdu(s)) ur else en

@Composable private fun WorshipText(value: String, s: AppSettings, size: Float, color: Color,
    modifier: Modifier = Modifier, center: Boolean = false, lines: Int = 2) {
    Text(value, modifier, fontFamily = worshipFont(s), fontSize = size.sp,
        lineHeight = (size * if (L10n.isUrdu(s)) 1.8f else 1.25f).sp, color = color,
        textAlign = if (center) TextAlign.Center else TextAlign.Start, maxLines = lines)
}

@Composable private fun WorshipSurface(s: AppSettings, modifier: Modifier = Modifier, selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    val c = quietPalette(s.theme)
    val shape = RoundedCornerShape(14.dp)
    Column(modifier.clip(shape).background(if (selected) c.accent.copy(alpha = .22f) else c.panel)
        .border(if (selected) 1.dp else .6.dp, c.accent.copy(alpha = if (selected) .85f else .32f), shape), content = content)
}

/** Gallery uses the top panorama of the existing atlas, never the atlas grid. */
@Composable internal fun WorshipBackdrop(s: AppSettings, wide: Boolean, modifier: Modifier) {
    val c = quietPalette(s.theme)
    Box(modifier.background(c.background).testTag("quiet-art-${s.theme.name}")) {
        if (s.artTheme != ArtTheme.MINIMAL) {
            if (s.theme == AppTheme.PRAYER_GALLERY) {
                val atlas = ImageBitmap.imageResource(R.drawable.tablet_gallery_atlas_181)
                Canvas(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(if (wide) .43f else .40f)) {
                    drawImage(atlas, srcOffset = IntOffset.Zero, srcSize = IntSize(atlas.width, (atlas.height * .456f).roundToInt()),
                        dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), filterQuality = FilterQuality.High)
                }
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to c.background,
                    .45f to c.background, .62f to c.background.copy(alpha = .18f), 1f to Color.Transparent)))
            } else {
                val id = when (s.theme) {
                    AppTheme.MIQAAT -> if (wide) R.drawable.quiet_miqaat_landscape else R.drawable.approved_miqaat_arch
                    AppTheme.KISWAH -> if (wide) R.drawable.kiswah_tablet_approved else R.drawable.kiswah_tablet_portrait_approved
                    else -> if (wide) R.drawable.celestial_tablet_constellation else R.drawable.celestial_tablet_constellation_portrait
                }
                Image(painterResource(id), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
                Box(Modifier.matchParentSize().background(c.background.copy(alpha = if (s.theme == AppTheme.KISWAH) .16f else .24f)))
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to c.background.copy(alpha = .65f),
                    .18f to Color.Transparent, .8f to Color.Transparent, 1f to c.background.copy(alpha = .7f))))
            }
        }
    }
}

@Composable internal fun WorshipKiswahBand(s: AppSettings, wide: Boolean, tablet: Boolean) {
    val c = quietPalette(s.theme)
    val full = if (!wide && !tablet) Kiswah.BAND.substringBefore("   ✦   ") else Kiswah.BAND
    val text = if (!wide && tablet) full.replaceFirst("   ✦   ", "\n") else full
    Box(Modifier.fillMaxWidth().height(if (tablet && !wide) 88.dp else if (tablet) 68.dp else 54.dp)
        .background(Color.Black.copy(alpha = .9f)), contentAlignment = Alignment.Center) {
        KiswahOrnamentalFrame(if (tablet) .75f else .45f, Modifier.matchParentSize())
        Text(text, Modifier.fillMaxWidth().padding(horizontal = if (tablet) 28.dp else 18.dp, vertical = 12.dp)
            .testTag("kiswah-calligraphy-band").semantics { contentDescription = full },
            fontFamily = KiswahLandscapeArabic, fontSize = (if (tablet) 19 else 14).sp,
            lineHeight = (if (tablet) 30 else 22).sp, color = c.accent, textAlign = TextAlign.Center,
            maxLines = if (!wide && tablet) 2 else 1)
    }
}

@Composable internal fun FridayApprovedHome(state: PrayerState, s: AppSettings, a: HomeActions) {
    val c = quietPalette(s.theme)
    var why by remember { mutableStateOf<Prayer?>(null) }
    var more by remember { mutableStateOf(false) }
    val contextLinks = doors(state, s, a)
    why?.let { p ->
        val day = if (p == state.next && state.nextTime.toLocalDate().isAfter(state.today.date))
            PrayerEngine.times(s, state.nextTime.toLocalDate()) else state.today
        WhyDialog(s, day, p) { why = null }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(c.background).testTag("approved-home")) {
        val wide = maxWidth > maxHeight
        val tablet = minOf(maxWidth, maxHeight) >= 600.dp
        val short = wide && !tablet
        val compact = !tablet || maxHeight < 700.dp
        WorshipBackdrop(s, wide, Modifier.matchParentSize())
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = if (tablet) 22.dp else 10.dp)
            .testTag("friday-home")) {
            Row(Modifier.fillMaxWidth().height(if (tablet) 62.dp else 48.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).clickable(onClick = a.onOpenLocation)) {
                    WorshipText(s.locationName, s, if (tablet) 21f else 14f, c.ink, lines = 1)
                    WorshipText(if (L10n.isUrdu(s)) L10n.fridayDate(state.now.toLocalDate()) else state.now.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)), s, if (tablet) 14f else 11f, c.secondary, lines = 1)
                }
                WorshipText(L10n.at(state.now, s.use24h), s, if (tablet) 22f else 16f, c.ink, Modifier.padding(horizontal = 8.dp), lines = 1)
                Box {
                    IconButton(onClick = { more = true }, modifier = Modifier.size(48.dp)) {
                        Icon(if (contextLinks.any { it.warn }) Icons.Outlined.WarningAmber else Icons.Outlined.MoreVert,
                            words(s, "More actions and alerts", "مزید اختیارات اور انتباہات"), tint = c.accent)
                    }
                    DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                        contextLinks.forEach { link -> DropdownMenuItem(text = { Text(link.label) }, onClick = {
                            more = false; link.onClick?.invoke()
                        }) }
                    }
                }
                IconButton(onClick = a.onOpenSettings, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Settings, Str[R.string.s_settings], tint = c.accent)
                }
            }
            if (s.theme == AppTheme.KISWAH && (tablet || short)) WorshipKiswahBand(s, wide, tablet)
            val banner: @Composable () -> Unit = {
                WorshipSurface(s, Modifier.fillMaxWidth().heightIn(min = if (tablet) 68.dp else 44.dp)
                    .testTag("friday-banner").clickable(onClick = a.onOpenFriday)) {
                    WorshipText(words(s, "JUMUʿAH · الجمعة", "جمعہ · الجمعة"), s, if (tablet) 25f else 18f, c.accent,
                        Modifier.fillMaxWidth().padding(top = 5.dp), true, 1)
                    if (tablet) WorshipText(words(s, "A day for prayer, remembrance and reflection", "نماز، ذکر اور غور و فکر کا دن"),
                        s, 13f, c.ink, Modifier.fillMaxWidth().padding(bottom = 6.dp), true, 1)
                }
            }
            val hero: @Composable (Modifier) -> Unit = { m ->
                val h = heroInfo(state, s, null)
                val jum = s.jumuahEnabled && h.prayer == Prayer.DHUHR
                Column(m.testTag("home-hero").padding(horizontal = 10.dp, vertical = if (short) 0.dp else 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    WorshipText(h.kickerLabel, s, if (tablet) 16f else 11f, c.secondary, center = true, lines = 1)
                    Text(if (jum) "الجمعة" else h.arabic, Modifier.testTag("friday-hero-arabic"), fontFamily = if (s.theme == AppTheme.KISWAH) KiswahLandscapeArabic else Amiri,
                        fontSize = (if (tablet) 54 else if (short) 29 else 34).sp,
                        lineHeight = (if (tablet) 80 else if (short) 43 else 49).sp, color = c.accent)
                    if (!L10n.isUrdu(s)) WorshipText(h.label.uppercase(Locale.ROOT), s, if (tablet) 23f else 15f, c.ink, center = true, lines = 1)
                    WorshipText(if (s.showRelative) L10n.relative(s, state.heroTime, state.now) else "${h.clock} ${h.suffix}".trim(), s,
                        if (tablet) 66f else if (short) 36f else 43f, c.ink,
                        Modifier.testTag("friday-hero-time").clickable(onClick = a.onToggleRelative), true, 1)
                    WorshipSurface(s, Modifier.padding(vertical = 3.dp)) {
                        WorshipText(h.status, s, if (tablet) 17f else 12f, c.accent, Modifier.padding(horizontal = 14.dp, vertical = 3.dp), true, 1)
                    }
                    val iq = PrayerEngine.iqamah(s, state.today, h.prayer)
                    if (jum || iq != null) WorshipText(listOfNotNull(if (jum) words(s, "Masjid time", "مسجد کا وقت") else null,
                        iq?.let { "${L10n.word(s, "Iqamah")} ${L10n.at(it, s.use24h)}" }).joinToString(" · "), s,
                        if (tablet) 15f else 11f, c.secondary, center = true, lines = 1)
                    if (tablet) TextButton(onClick = { why = h.prayer }) {
                        WorshipText(words(s, "View prayer details →", "نماز کی تفصیل ←"), s, 16f, c.ink, lines = 1)
                    }
                }
            }
            val prayers: @Composable (Modifier) -> Unit = { m ->
                WorshipSurface(s, m.testTag("home-prayers")) {
                    listedPrayers(s).forEach { p ->
                        val r = galleryRowInfo(p, state, s)
                        val selected = r.isNow || r.isNext && listedPrayers(s).none { galleryRowInfo(it, state, s).isNow }
                        Row(Modifier.fillMaxWidth().weight(1f).heightIn(min = 48.dp)
                            .background(if (selected) c.accent.copy(alpha = .21f) else Color.Transparent)
                            .testTag("approved-row-${p.name}").semantics(mergeDescendants = true) { contentDescription = r.spoken }
                            .clickable { why = p }.padding(horizontal = if (tablet) 14.dp else 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (tablet) 10.dp else 5.dp)) {
                            if (s.theme == AppTheme.CELESTIAL_MERIDIAN) Canvas(Modifier.width(16.dp).fillMaxHeight()) {
                                drawLine(c.accent.copy(alpha = .6f), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx())
                                drawCircle(if (selected || r.done) c.accent else c.background, if (selected) 5.dp.toPx() else 3.dp.toPx())
                            }
                            Column(Modifier.weight(1f)) {
                                WorshipText(r.label, s, if (tablet) 22f else 15f, c.ink, lines = 1)
                                if (tablet && !L10n.isUrdu(s)) Text(if (s.jumuahEnabled && p == Prayer.DHUHR) "الجمعة" else r.arabic,
                                    fontFamily = Amiri, fontSize = 20.sp, lineHeight = 30.sp, color = c.accent)
                            }
                            Column(Modifier.width(if (tablet) 195.dp else 105.dp), horizontalAlignment = Alignment.End) {
                                WorshipText(if (s.showRelative) r.relative else "${r.clock} ${r.suffix}".trim(), s,
                                    if (tablet) 26f else 17f, c.ink, Modifier.testTag("friday-time-${p.name}"), lines = 1)
                                if (tablet && s.showEndTimes) WorshipText(r.small, s, 11f, c.secondary, lines = 2)
                            }
                            Box(Modifier.width(if (tablet) 48.dp else 35.dp), contentAlignment = Alignment.Center) {
                            if (r.isNow || r.isNext) Text(L10n.word(s, if (r.isNow) "NOW" else "NEXT"),
                                Modifier.testTag(if (r.isNow) "home-status-now" else "home-status-next")
                                    .clip(RoundedCornerShape(8.dp)).background(c.accent).padding(horizontal = 5.dp, vertical = 3.dp),
                                fontFamily = if (L10n.isUrdu(s)) Amiri else QuietBody, fontSize = 9.sp, color = c.background)
                            }
                            if (tablet && p.isPrayer) IconButton(onClick = { why = p }, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Outlined.Info, words(s, "Prayer details", "نماز کی تفصیل"), tint = c.secondary, modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                }
            }
            if (wide) Row(Modifier.weight(1f).fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(if (tablet) 20.dp else 10.dp)) {
                prayers(Modifier.weight(if (short) .48f else .47f).fillMaxHeight())
                Column(Modifier.weight(if (short) .52f else .53f).fillMaxHeight()) {
                    banner()
                    hero(Modifier.weight(1f).fillMaxWidth())
                    FridayActions(s, state.now, a.onOpenFriday, short, tablet)
                }
            } else {
                banner()
                hero(Modifier.fillMaxWidth().weight(if (tablet) .43f else .35f))
                FridayActions(s, state.now, a.onOpenFriday, false, tablet)
                Spacer(Modifier.height(6.dp))
                prayers(Modifier.fillMaxWidth().weight(if (tablet) .57f else .65f))
            }
            FridayDock(s, state, a, tablet)
        }
        if (s.nightDim && state.period == Prayer.ISHA) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = .22f)))
    }
}

@Composable private fun FridayActions(s: AppSettings, now: ZonedDateTime, onOpen: () -> Unit, short: Boolean, tablet: Boolean) {
    val c = quietPalette(s.theme)
    val prefs = LocalContext.current.getSharedPreferences("miqaat_friday", Context.MODE_PRIVATE)
    val key = now.toLocalDate().toString()
    val kahf = prefs.getBoolean("kahf_$key", false)
    val salawat = prefs.getInt("salawat_$key", 0)
    Row(Modifier.fillMaxWidth().padding(top = if (short) 0.dp else 5.dp).height(if (tablet) 78.dp else 52.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(Triple(Icons.Outlined.MenuBook, words(s, "Al-Kahf", "سورۃ الکہف"), words(s, if (kahf) "Read this Friday ✓" else "Mark as read", if (kahf) "پڑھ لی ✓" else "پڑھنے کا اندراج")),
            Triple(Icons.Outlined.FavoriteBorder, words(s, "Salawat", "درود شریف"), words(s, "$salawat this Friday", "اس جمعہ: $salawat")),
            Triple(Icons.Outlined.NotificationsNone, words(s, "Friday", "جمعہ"), words(s, "Reminders", "یاد دہانیاں"))).forEachIndexed { i, item ->
            WorshipSurface(s, Modifier.weight(1f).fillMaxHeight().testTag("friday-action-$i")
                .clickable(role = Role.Button, onClick = onOpen)) {
                Row(Modifier.fillMaxSize().padding(horizontal = if (tablet) 10.dp else 5.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    if (tablet) Icon(item.first, null, Modifier.size(25.dp), tint = c.accent)
                    Column {
                        WorshipText(item.second, s, if (tablet) 17f else 12f, c.ink, lines = 1)
                        WorshipText(item.third, s, if (tablet) 11f else 9f, c.secondary, lines = 1)
                    }
                }
            }
        }
    }
}

@Composable private fun FridayDock(s: AppSettings, state: PrayerState, a: HomeActions, tablet: Boolean) {
    val c = quietPalette(s.theme)
    val mode = adhkarModes(state, s).firstOrNull() ?: AdhkarMode.MORNING
    Row(Modifier.fillMaxWidth().height(if (tablet) 66.dp else 50.dp).padding(vertical = 3.dp).background(c.panel),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        val items: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, () -> Unit>> = listOf(Triple(Icons.Outlined.Explore, L10n.word(s, "Qibla"), a.onOpenQibla),
            Triple(Icons.Outlined.MenuBook, words(s, "Adhkar", "اذکار"), { a.onOpenAdhkar(mode) }),
            Triple(Icons.Outlined.School, words(s, "Learn", "سیکھیں"), a.onOpenLearn),
            Triple(Icons.Outlined.CalendarMonth, words(s, "Timetable", "اوقات"), a.onOpenTimetable))
        items.forEach { (icon, label, action) ->
            Column(Modifier.weight(1f).fillMaxHeight().clickable(role = Role.Button, onClick = action),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(icon, label, Modifier.size(if (tablet) 25.dp else 20.dp), tint = c.accent)
                WorshipText(label, s, if (tablet) 15f else 10f, c.ink, center = true, lines = 1)
            }
        }
    }
}

@Composable internal fun ApprovedQuietContent(p: Phase.Quiet, s: AppSettings, now: ZonedDateTime, onStop: () -> Unit) {
    val c = quietPalette(s.theme)
    val times = quietPrayerTimes(s, p.prayer, now)
    val ur = L10n.isUrdu(s)
    val remaining = ((p.endsAt - now.toInstant().toEpochMilli()).coerceAtLeast(0) + 59_999) / 60_000
    BoxWithConstraints(Modifier.fillMaxSize().background(c.background).testTag("quiet-board")
        .clickable(role = Role.Button, onClickLabel = Str[R.string.s_quiet_exit], onClick = onStop)) {
        val wide = maxWidth > maxHeight
        val tablet = minOf(maxWidth, maxHeight) >= 600.dp
        val short = wide && maxHeight < 500.dp
        val k = if (tablet) (if (wide) maxHeight.value / 800f else maxWidth.value / 800f).coerceIn(.72f, 1.2f) else .70f
        WorshipBackdrop(s, wide, Modifier.matchParentSize())
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = if (tablet) 28.dp else 14.dp, vertical = if (short) 4.dp else 10.dp)) {
            Row(Modifier.fillMaxWidth().height(if (short) 42.dp else 52.dp), verticalAlignment = Alignment.CenterVertically) {
                WorshipText("Miqaat", s, if (tablet) 28f else 23f, c.ink, Modifier.weight(1f).testTag("quiet-brand"), lines = 1)
                WorshipSurface(s, Modifier.weight(1.2f)) {
                    QuietLabel(Str[R.string.s_quiet_mode_title], ur, c.accent, Modifier.padding(vertical = 5.dp), if (tablet) 11f else 9f)
                }
                WorshipText(L10n.iso(L10n.at(now, s.use24h)), s, if (tablet) 22f else 16f, c.ink,
                    Modifier.weight(1f).testTag("quiet-live-clock"), true, 1)
            }
            if (s.theme == AppTheme.KISWAH) WorshipKiswahBand(s, wide, tablet)
            val hero: @Composable (Modifier) -> Unit = { m ->
                Column(m.testTag("quiet-current-panel").padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    if (s.theme == AppTheme.CELESTIAL_MERIDIAN && !short) Canvas(Modifier.fillMaxWidth(.70f).height(24.dp)) {
                        drawLine(c.accent.copy(alpha = .6f), Offset(0f, size.height * .75f), Offset(size.width, size.height * .25f), 1.dp.toPx())
                        for (i in 0..4) drawCircle(c.accent, if (i == 2) 4.dp.toPx() else 2.dp.toPx(),
                            Offset(size.width * i / 4, size.height * (.75f - i * .125f)))
                    }
                    Text(if (s.jumuahEnabled && p.prayer == Prayer.DHUHR && times.current.dayOfWeek == java.time.DayOfWeek.FRIDAY) "الجمعة" else p.prayer.arabic,
                        Modifier.fillMaxWidth().testTag("quiet-current-arabic").semantics { heading() },
                        fontFamily = if (s.theme == AppTheme.KISWAH) KiswahLandscapeArabic else Amiri,
                        fontSize = (if (short) 30f else 66f * k).sp, lineHeight = (if (short) 44f else 100f * k).sp,
                        color = c.accent, textAlign = TextAlign.Center)
                    WorshipText(if (ur) L10n.prayer(s, p.prayer) else L10n.prayer(s, p.prayer).uppercase(Locale.ROOT), s,
                        if (short) 12f else 23f * k, c.ink, Modifier.fillMaxWidth().testTag("quiet-current-name"), true, 1)
                    QuietLabel(Str[R.string.s_quiet_prayer_progress], ur, c.accent,
                        Modifier.padding(vertical = if (short) 2.dp else 8.dp).testTag("quiet-progress"), if (short) 9f else 12f)
                    WorshipSurface(s, Modifier.fillMaxWidth(if (tablet) .80f else .90f).padding(vertical = if (short) 0.dp else 8.dp)) {
                        QuietLabel(words(s, "Azaan at", "اذان کا وقت"), ur, c.accent,
                            Modifier.padding(top = 5.dp).testTag("quiet-current-label"), if (short) 9f else 11f)
                        QuietTime(times.current, s, if (ur) (if (short) 24f else 45f * k) else if (short) 42f else 96f * k,
                            if (s.theme == AppTheme.PRAYER_GALLERY) c.accent else c.ink,
                            Modifier.fillMaxWidth().testTag("quiet-current-time"))
                        WorshipText(Str[R.string.s_quiet_iqamah_complete], s, if (short) 10f else 13f, c.secondary,
                            Modifier.fillMaxWidth().padding(bottom = 7.dp).testTag("quiet-iqamah-complete"), true, 1)
                    }
                }
            }
            val next: @Composable (Modifier) -> Unit = { m ->
                Box(m.testTag("quiet-next-panel"), contentAlignment = Alignment.Center) {
                    WorshipSurface(s, Modifier.fillMaxWidth(if (wide) .95f else 1f)) {
                        QuietLabel(Str[R.string.s_quiet_next_azaan], ur, c.secondary,
                            Modifier.padding(top = if (short) 6.dp else 14.dp).testTag("quiet-next-label"), if (short) 9f else 11f)
                        WorshipText(if (ur) L10n.prayer(s, times.next) else L10n.prayer(s, times.next).uppercase(Locale.ROOT), s,
                            if (short) 16f else 24f * k, c.ink, Modifier.fillMaxWidth().testTag("quiet-next-name"), true, 1)
                        QuietTime(times.nextTime, s, if (ur) (if (short) 21f else 30f * k) else if (short) 34f else 64f * k,
                            c.ink, Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag("quiet-next-time"))
                        if (times.nextTime.toLocalDate().isAfter(now.withZoneSameInstant(s.zone()).toLocalDate()))
                            WorshipText(Str[R.string.s_quiet_tomorrow], s, 11f, c.secondary,
                                Modifier.fillMaxWidth().testTag("quiet-tomorrow"), true, 1)
                    }
                }
            }
            if (wide) Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (tablet) 32.dp else 10.dp)) {
                hero(Modifier.weight(.60f).fillMaxHeight())
                Box(Modifier.width(1.dp).fillMaxHeight(.55f).background(c.accent.copy(alpha = .4f)))
                next(Modifier.weight(.40f).fillMaxHeight())
            } else {
                hero(Modifier.weight(1f).fillMaxWidth())
                next(Modifier.fillMaxWidth().padding(bottom = if (tablet) 12.dp else 4.dp))
            }
            val progress = (1f - (p.endsAt - now.toInstant().toEpochMilli()).coerceAtLeast(0) /
                (s.quietMinutes.coerceAtLeast(1) * 60_000f)).coerceIn(0f, 1f)
            Canvas(Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(if (tablet) .42f else .65f).height(8.dp)) {
                drawRoundRect(c.accent.copy(alpha = .22f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
                drawRoundRect(c.accent, size = androidx.compose.ui.geometry.Size(size.width * progress, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f))
            }
            WorshipText(Str.get(R.string.s_quiet_ends_hint, remaining.toInt()), s, if (short) 9f else 11f, c.secondary,
                Modifier.fillMaxWidth().padding(vertical = if (short) 2.dp else 5.dp).testTag("quiet-expiry-hint"), true, 2)
            Row(Modifier.align(Alignment.CenterHorizontally).widthIn(max = 430.dp).fillMaxWidth().heightIn(min = 48.dp)
                .clip(RoundedCornerShape(28.dp)).background(c.panel).border(1.dp, c.accent, RoundedCornerShape(28.dp))
                .testTag("quiet-return").clickable(role = Role.Button, onClick = onStop).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, Modifier.size(18.dp), tint = c.accent)
                WorshipText(Str[R.string.s_quiet_exit], s, if (tablet) 18f else 14f, c.ink,
                    Modifier.padding(start = 10.dp).testTag("quiet-exit-label"), true, 1)
            }
        }
        if (s.nightDim && p.prayer == Prayer.ISHA) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = .22f)))
    }
}
