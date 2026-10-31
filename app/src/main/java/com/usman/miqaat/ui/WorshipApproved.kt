package com.usman.miqaat.ui

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.roundToInt

/** Approved Friday and quiet compositions. Only native text/actions carry prayer facts. */
internal fun fridayHomepageActive(s: AppSettings, now: ZonedDateTime): Boolean {
    if (!s.fridayReminders) return false
    val local = now.withZoneSameInstant(s.zone())
    if (local.dayOfWeek != java.time.DayOfWeek.FRIDAY) return false
    // UI day-boundaries use the engine's published second, never incidental subsecond Date precision.
    val maghrib = PrayerEngine.times(s, local.toLocalDate(), s.zone())[Prayer.MAGHRIB]
        .toInstant().truncatedTo(java.time.temporal.ChronoUnit.SECONDS)
    return local.toInstant().isBefore(maghrib)
}

private fun worshipFont(s: AppSettings) = if (L10n.isUrdu(s)) Amiri else FontFamily.Serif
private fun words(s: AppSettings, en: String, ur: String) = if (L10n.isUrdu(s)) ur else en

@Composable private fun WorshipText(value: String, s: AppSettings, size: Float, color: Color,
    modifier: Modifier = Modifier, center: Boolean = false, lines: Int = 2) {
    Text(value, modifier, fontFamily = worshipFont(s), fontSize = size.sp,
        lineHeight = (size * if (L10n.isUrdu(s)) 1.8f else 1.25f).sp, color = color,
        textAlign = if (center) TextAlign.Center else TextAlign.Start, maxLines = lines)
}

@Composable private fun WorshipSurface(s: AppSettings, modifier: Modifier = Modifier, selected: Boolean = false, ornate: Boolean = false, fill: Color? = null,
    content: @Composable ColumnScope.() -> Unit) {
    val c = fridayOption2Palette(s.theme)
    val shape = if (ornate) CutCornerShape(10.dp) else RoundedCornerShape(14.dp)
    Column(modifier.clip(shape).background(fill ?: if (selected) c.accent.copy(alpha = .22f) else c.panel)
        .border(if (selected) 1.dp else .6.dp, c.accent.copy(alpha = if (selected) .85f else .32f), shape), content = content)
}

/** Gallery uses the top panorama of the existing atlas, never the atlas grid. */
@Composable internal fun WorshipBackdrop(s: AppSettings, wide: Boolean, modifier: Modifier) {
    val c = fridayOption2Palette(s.theme)
    Box(modifier.background(c.background).testTag("quiet-art-${s.theme.name}")) {
        if (s.artTheme != ArtTheme.MINIMAL) {
            if (s.theme == AppTheme.PRAYER_GALLERY) {
                GalleryFridayVista(Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(if (wide) .43f else .40f), rounded = false)
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
    val c = fridayOption2Palette(s.theme)
    val full = if (!wide && !tablet) Kiswah.BAND.substringBefore("   ✦   ") else Kiswah.BAND
    val text = if (!wide && tablet) full.replaceFirst("   ✦   ", "\n") else full
    Box(Modifier.fillMaxWidth().height(if (tablet && !wide) 88.dp else if (tablet) 68.dp else if (wide) 48.dp else 54.dp)
        .background(Color.Black.copy(alpha = .9f)), contentAlignment = Alignment.Center) {
        KiswahOrnamentalFrame(if (tablet) .75f else .45f, Modifier.matchParentSize())
        Text(text, Modifier.fillMaxWidth().padding(horizontal = if (tablet) 28.dp else 18.dp, vertical = 12.dp)
            .testTag("kiswah-calligraphy-band").semantics { contentDescription = full },
            fontFamily = KiswahLandscapeArabic, fontSize = (if (tablet) 19 else 14).sp,
            lineHeight = (if (tablet) 30 else 22).sp, color = c.accent, textAlign = TextAlign.Center,
            maxLines = if (!wide && tablet) 2 else 1)
    }
}

@Composable internal fun FridayApprovedHome(state: PrayerState, s: AppSettings, a: HomeActions, onClosePreview: (() -> Unit)? = null) {
    val c = fridayOption2Palette(s.theme)
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
        val viewportHeight = maxHeight
        val density = LocalDensity.current
        val typeScale = density.fontScale
        val safeHeight = maxHeight - with(density) { (WindowInsets.safeDrawing.getTop(density) + WindowInsets.safeDrawing.getBottom(density)).toDp() }
        val smallPortrait = !tablet && !wide && maxHeight < 800.dp
        FridayOption2Backdrop(s, wide, Modifier.matchParentSize())
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = if (tablet) 22.dp else 10.dp)
            .testTag("friday-home")) {
            val header: @Composable () -> Unit = {
            Row(Modifier.fillMaxWidth().height(if (tablet) 62.dp else 48.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onClosePreview != null) IconButton(onClick = onClosePreview, modifier = Modifier.size(48.dp).testTag("jummah-preview-close")) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = c.accent)
                }
                Column(Modifier.weight(1f).clickable(onClick = a.onOpenLocation)) {
                    WorshipText(if (onClosePreview != null) words(s, "Jumuʿah preview", "جمعہ کا پیش منظر") else s.locationName, s, if (tablet) 21f else 14f, c.ink, lines = 1)
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
            }
            if (!short) header()
            val banner: @Composable () -> Unit = { FridayMasthead(s, tablet, wide, a.onOpenFriday) }
            val hero: @Composable (Modifier) -> Unit = { m ->
                FridayHero(state, s, a, tablet, short, smallPortrait, wide, m) { why = state.hero }
            }
            val prayers: @Composable (Modifier) -> Unit = { m ->
                WorshipSurface(s, m.background(c.background.copy(alpha = .72f), RoundedCornerShape(14.dp)).testTag("home-prayers")) {
                    listedPrayers(s).forEach { p ->
                        val r = fridayRowInfo(p, state, s)
                        val selected = r.isNow || r.isNext && listedPrayers(s).none { galleryRowInfo(it, state, s).isNow }
                        Row(Modifier.fillMaxWidth().weight(1f).heightIn(min = 48.dp)
                            .then(if (selected) Modifier.clip(RoundedCornerShape(12.dp)).background(Brush.horizontalGradient(
                                if (s.theme == AppTheme.MIQAAT) listOf(Color(0xFFD5E3CE), Color(0xFFE7EDDC))
                                else listOf(Color(0xFF735226).copy(alpha = .88f), Color(0xFF34291E).copy(alpha = .88f))))
                                .border(.8.dp, c.accent.copy(alpha = .8f), RoundedCornerShape(12.dp)) else Modifier)
                            .drawBehind { drawLine(c.accent.copy(alpha = .12f), Offset(8.dp.toPx(), size.height), Offset(size.width - 8.dp.toPx(), size.height), 1f) }
                            .testTag("approved-row-${p.name}").semantics(mergeDescendants = true) { contentDescription = r.spoken }
                            .clickable { why = p }.padding(horizontal = if (tablet) 14.dp else 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (tablet) 10.dp else 5.dp)) {
                            if (s.theme == AppTheme.CELESTIAL_MERIDIAN) Canvas(Modifier.width(16.dp).fillMaxHeight()) {
                                drawLine(c.accent.copy(alpha = .6f), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 1.dp.toPx())
                                drawCircle(if (selected || r.done || p == Prayer.SUNRISE && !state.today[p].isAfter(state.now)) c.accent else c.background, if (selected) 5.dp.toPx() else 3.dp.toPx())
                            }
                            Icon(if (p == Prayer.FAJR || p == Prayer.ISHA) Icons.Outlined.DarkMode else if (p == Prayer.ASR) Icons.Outlined.WbTwilight else Icons.Outlined.WbSunny, null, Modifier.size(if (tablet) 24.dp else 18.dp), tint=c.accent)
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                WorshipText(r.label, s, if (tablet) 20f else if (short) 15f else 17f, c.ink, lines = 1)
                                if (tablet && !L10n.isUrdu(s)) Text(if (s.jumuahEnabled && p == Prayer.DHUHR) "الجمعة" else r.arabic,
                                    fontFamily = Amiri, fontSize = 18.sp, lineHeight = 27.sp, color = c.accent)
                            }
                            Column(Modifier.width(if (tablet) 205.dp else if (short) 112.dp else 128.dp), horizontalAlignment = Alignment.End) {
                                FridayTime(state.today[p], s, if (tablet) 29f else 21f,
                                    Modifier.fillMaxWidth(), "friday-time-${p.name}", c.ink, TextAlign.End)
                                if (s.showRelative || s.showEndTimes) WorshipText(
                                    if (s.showRelative) L10n.relative(s, state.today[p], state.now) else fridayPrayerDetail(p, state, s, tablet),
                                    s, if (tablet) 12f else if (L10n.isUrdu(s)) 8.5f else 9f, c.secondary,
                                    Modifier.testTag("friday-detail-${p.name}"), lines = if (tablet) 2 else 1)
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
            if (wide && tablet) {
                if (s.theme == AppTheme.MIQAAT) FridayMiqaatHeader(Modifier.testTag("friday-gallery-vista").fillMaxWidth().height(72.dp))
                banner()
            }
            if (wide) Row(Modifier.weight(1f).fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(if (tablet) 20.dp else 10.dp)) {
                prayers(Modifier.weight(if (short) .40f else .40f).fillMaxHeight())
                if (s.theme == AppTheme.PRAYER_GALLERY) Spacer(Modifier.weight(.16f))
                Column(Modifier.weight(if (s.theme == AppTheme.PRAYER_GALLERY) .44f else .60f).fillMaxHeight()) {
                    if (short) header()
                    if (short && s.theme == AppTheme.MIQAAT) FridayMiqaatHeader(Modifier.testTag("friday-gallery-vista").fillMaxWidth().height(16.dp))
                    if (short) banner()
                    hero(Modifier.weight(1f).fillMaxWidth())
                    FridayActions(s, state.now, a, short, tablet)
                }
            } else {
                if (s.theme == AppTheme.MIQAAT) FridayMiqaatHeader(Modifier.testTag("friday-gallery-vista").fillMaxWidth().height(if (tablet) 80.dp else if (smallPortrait) 60.dp else 76.dp))
                banner()
                val idealHeroHeight = if (tablet) (viewportHeight * .44f).coerceIn(440.dp, 600.dp)
                    else if (s.theme == AppTheme.MIQAAT) (if (smallPortrait) 250.dp else 320.dp)
                    else if (s.theme == AppTheme.KISWAH) (if (smallPortrait) 250.dp else 320.dp)
                    else (if (smallPortrait) 250.dp else 320.dp)
                val mastheadHeight = if(tablet) 48.dp else 38.dp
                val artHeight = if (s.theme == AppTheme.MIQAAT) (if (tablet) 80.dp else if (smallPortrait) 60.dp else 76.dp) else 0.dp
                val chrome = (if (tablet) 62.dp else 48.dp) + mastheadHeight + artHeight +
                    (if (tablet) 87.dp else 63.dp) + 6.dp + (if (tablet) 66.dp else 56.dp)
                val heroHeight = (idealHeroHeight * typeScale).coerceAtMost((safeHeight - chrome - (if(tablet) (if (L10n.isUrdu(s)) 64.dp else 52.dp) * typeScale else 48.dp) * listedPrayers(s).size).coerceAtLeast(140.dp))
                hero(Modifier.fillMaxWidth().height(heroHeight).padding(vertical = if (tablet) 10.dp else 4.dp))
                FridayActions(s, state.now, a, false, tablet)
                Spacer(Modifier.height(6.dp))
                prayers(Modifier.fillMaxWidth().then(if(tablet) Modifier.height((if (L10n.isUrdu(s)) 64.dp else 52.dp) * typeScale * listedPrayers(s).size) else Modifier.weight(1f)))
            }
            FridayDock(s, state, a, tablet)
        }
        if (s.nightDim && state.period == Prayer.ISHA) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = .22f)))
    }
}

/** Keep spoken congregation facts aligned with the visible Friday row. */
internal fun fridayRowInfo(p: Prayer, state: PrayerState, s: AppSettings): RowInfo {
    val r = galleryRowInfo(p, state, s)
    if (p != Prayer.DHUHR || !s.jumuahEnabled) return r
    return r.copy(spoken = L10n.spokenRow(s, r.label, p, state.today[p], state.now,
        r.isNow, r.isNext, r.done, PrayerEngine.iqamah(s, state.today, p), null, !r.azaanOn))
}

/** A congregation has a configured start, not an end at Asr. Keep other prayer-window details. */
internal fun fridayPrayerDetail(p: Prayer, state: PrayerState, s: AppSettings, tablet: Boolean): String {
    if (p == Prayer.DHUHR && s.jumuahEnabled) {
        val iq = PrayerEngine.iqamah(s, state.today, p)
        return if (iq != null) "${L10n.word(s, "Iqamah")} ${L10n.at(iq, s.use24h)}"
            else words(s, "Masjid azaan time", "مسجد میں اذان کا وقت")
    }
    val small = galleryRowInfo(p, state, s).small
    return if (tablet) small else small.substringBefore(" · ")
}

@Composable private fun FridayActions(s: AppSettings, now: ZonedDateTime, a: HomeActions, short: Boolean, tablet: Boolean) {
    val c = fridayOption2Palette(s.theme)
    val prefs = LocalContext.current.getSharedPreferences("miqaat_friday", Context.MODE_PRIVATE)
    val key = now.toLocalDate().toString()
    val kahf = prefs.getBoolean("kahf_$key", false)
    val salawat = prefs.getInt("salawat_$key", 0)
    Row(Modifier.fillMaxWidth().padding(top = if (short) 0.dp else 5.dp).height(if (tablet) 64.dp else 52.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(Triple(Icons.Outlined.MenuBook, words(s, "Al-Kahf", "سورۃ الکہف"), words(s, if (kahf) "Read this Friday ✓" else "Mark as read", if (kahf) "پڑھ لی ✓" else "پڑھنے کا اندراج")),
            Triple(Icons.Outlined.FavoriteBorder, words(s, "Salawat", "درود شریف"), words(s, "$salawat this Friday", "اس جمعہ: $salawat"))).forEachIndexed { i, item ->
            WorshipSurface(s, Modifier.weight(1f).fillMaxHeight().background(if(s.theme == AppTheme.MIQAAT) (if(i==0) Color(0xFF145440) else Color(0xFFB24926)) else Color.Transparent, RoundedCornerShape(10.dp)).testTag("friday-action-$i")
                .border(if (s.theme == AppTheme.MIQAAT) 0.dp else 1.dp, c.accent.copy(alpha=.85f), RoundedCornerShape(10.dp))
                .clickable(role = Role.Button, onClick = if (i == 0) a.onOpenKahf else a.onOpenSalawat), fill=if(s.theme == AppTheme.MIQAAT) (if(i==0) Color(0xFF145440) else Color(0xFFB24926)) else c.background.copy(alpha=.8f)) {
                Row(Modifier.fillMaxSize().padding(horizontal = if (tablet) 10.dp else 5.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                    if (i == 1) SalawatBeads(if (s.theme == AppTheme.MIQAAT) Color.White else if (s.theme == AppTheme.PRAYER_GALLERY) c.ink else c.accent, Modifier.size(if (tablet) 27.dp else 18.dp))
                    else Icon(item.first, null, Modifier.size(if (tablet) 25.dp else 18.dp), tint = if(s.theme == AppTheme.MIQAAT) Color.White else c.accent)
                    Column {
                        WorshipText(item.second, s, if (tablet) 17f else 12f, if(s.theme == AppTheme.MIQAAT) Color.White else c.ink, Modifier.testTag("friday-action-title-$i"), lines = 1)
                        WorshipText(item.third, s, if (tablet) 11f else 9f, if(s.theme == AppTheme.MIQAAT) Color(0xFFF5EBD8) else c.secondary, Modifier.testTag("friday-action-detail-$i"), lines = 1)
                    }
                }
            }
        }
    }
}

@Composable private fun FridayDock(s: AppSettings, state: PrayerState, a: HomeActions, tablet: Boolean) {
    val c = fridayOption2Palette(s.theme)
    val mode = adhkarModes(state, s).firstOrNull() ?: AdhkarMode.MORNING
    Row(Modifier.fillMaxWidth().height(if (tablet) 66.dp else 56.dp).padding(vertical = 3.dp).background(c.background.copy(alpha = .94f), RoundedCornerShape(14.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        val items: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, () -> Unit>> = listOfNotNull(if (s.showQibla) Triple(Icons.Outlined.Explore, L10n.word(s, "Qibla"), a.onOpenQibla) else null,
            if (s.adhkarEnabled) Triple(Icons.Outlined.MenuBook, words(s, "Adhkar", "اذکار"), { a.onOpenAdhkar(mode) }) else null,
            Triple(Icons.Outlined.MenuBook, words(s, "Learn", "سیکھیں"), a.onOpenLearn),
            Triple(Icons.Outlined.CalendarMonth, words(s, "Timetable", "اوقات"), a.onOpenTimetable))
        items.forEach { (icon, label, action) ->
            Column(Modifier.weight(1f).fillMaxHeight().clickable(role = Role.Button, onClick = action),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (label == words(s, "Adhkar", "اذکار")) SalawatBeads(c.accent, Modifier.size(if (tablet) 25.dp else 20.dp).semantics { contentDescription = label })
                else Icon(icon, label, Modifier.size(if (tablet) 25.dp else 20.dp), tint = c.accent)
                WorshipText(label, s, if (tablet) 15f else 10f, c.ink, center = true, lines = 1)
            }
        }
    }
}

@Composable internal fun ApprovedQuietContent(p: Phase.Quiet, s: AppSettings, now: ZonedDateTime, onStop: () -> Unit) {
    SanctuaryQuietContent(p, s, now, onStop)
}

@Composable private fun SalawatBeads(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val radius = size.minDimension * .32f
        for (i in 0..11) {
            val angle = i * Math.PI / 6
            drawCircle(color, size.minDimension * .055f,
                Offset(size.width / 2 + radius * kotlin.math.cos(angle).toFloat(), size.height * .4f + radius * kotlin.math.sin(angle).toFloat()))
        }
        drawLine(color, Offset(size.width / 2, size.height * .73f), Offset(size.width / 2, size.height * .92f), size.minDimension * .055f)
        drawCircle(color, size.minDimension * .06f, Offset(size.width / 2, size.height * .94f))
    }
}

/** The same panorama used by the Gallery homepage, cropped to its frame rather than stretched. */
@Composable private fun GalleryFridayVista(modifier: Modifier, rounded: Boolean = true) {
    val atlas = ImageBitmap.imageResource(R.drawable.tablet_gallery_atlas_181)
    Canvas(if (rounded) modifier.clip(RoundedCornerShape(14.dp)) else modifier) {
        val sourceHeight = atlas.height * .456f
        val aspect = size.width / size.height
        val sh = minOf(sourceHeight, atlas.width / aspect)
        val sw = minOf(atlas.width.toFloat(), sh * aspect)
        drawImage(atlas, srcOffset = IntOffset(((atlas.width - sw) / 2).roundToInt(), ((sourceHeight - sh) / 2).roundToInt()),
            srcSize = IntSize(sw.roundToInt(), sh.roundToInt()), dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), filterQuality = FilterQuality.High)
    }
}


/** One compact Friday masthead. Kiswah combines its Friday label and dhikr in one embroidered frame. */
@Composable private fun FridayMasthead(s: AppSettings, tablet: Boolean, wide: Boolean, onOpen: () -> Unit) {
    val c=fridayOption2Palette(s.theme)
    Box(Modifier.fillMaxWidth().height(if(tablet) 48.dp else if(wide) 24.dp else 38.dp)
        .background(if(s.theme==AppTheme.MIQAAT) Color(0xFF194B3C) else c.background.copy(alpha=.92f))
        .border(.8.dp, Color(0xFFE7C77E), RoundedCornerShape(8.dp)).testTag("friday-banner")
        .clickable(role=Role.Button,onClick=onOpen),contentAlignment=Alignment.Center) {
        Text(if (s.theme == AppTheme.MIQAAT && !L10n.isUrdu(s)) "✦   SubhanAllah   ✦   Alhamdulillah   ✦   Allahu Akbar   ✦" else Kiswah.BAND,Modifier.padding(horizontal=10.dp).testTag("kiswah-calligraphy-band"),fontFamily=Amiri,
            fontSize=(if(tablet) 18 else 12).sp,lineHeight=(if(tablet) 30 else 24).sp,
            color=Color(0xFFF0D89D),textAlign=TextAlign.Center,maxLines=1)
    }
}

/** Native, lining numerals with smaller suffixes. Width is measured before drawing, including Urdu and 24-hour forms. */
@Composable private fun FridayTime(time: ZonedDateTime, s: AppSettings, numberSize: Float, modifier: Modifier,
    tag: String, color: Color, align: TextAlign = TextAlign.Center, suffixRatio: Float = .55f) {
    val text = L10n.at(time, s.use24h)
    val digits = PrayerEngine.clock(time, s.use24h)
    val start = text.indexOf(digits)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        fun styled(size: Float) = buildAnnotatedString {
            if (start < 0) append(text) else {
                append(text.substring(0, start))
                withStyle(SpanStyle(fontSize = size.sp, fontFamily = if (tag == "friday-hero-time") FontFamily.Serif else QuietDisplay,
                    fontWeight = if (tag == "friday-hero-time") FontWeight.Bold else FontWeight.Normal,
                    fontFeatureSettings = "'lnum' 1, 'tnum' 1")) { append(digits) }
                append(text.substring(start + digits.length))
            }
        }
        fun style(size: Float) = TextStyle(fontFamily = worshipFont(s), fontSize = (size * suffixRatio).sp,
            lineHeight = (size * if (tag == "friday-hero-time") 1.28f else 1.2f).sp, platformStyle = PlatformTextStyle(includeFontPadding = false),
            textAlign = align, color = color)
        val resolved = remember(text, numberSize, suffixRatio, maxWidth, density.fontScale, density.density) {
            val measured = measurer.measure(styled(numberSize), style(numberSize), softWrap = false, maxLines = 1).size.width
            val available = with(density) { maxWidth.toPx() }
            if (measured > available) numberSize * (available / measured).coerceIn(.5f, 1f) else numberSize
        }
        Text(styled(resolved), Modifier.fillMaxWidth().testTag(tag), style = style(resolved), maxLines = 1)
    }
}

@Composable private fun FridayClock(state: PrayerState, s: AppSettings, size: Float, modifier: Modifier = Modifier) {
    val c = fridayOption2Palette(s.theme)
    FridayTime(state.heroTime, s, size, modifier, "friday-hero-time",
        if (s.theme == AppTheme.KISWAH) c.accent else c.ink, suffixRatio = .30f)
}

@Composable private fun FridayHero(state: PrayerState, s: AppSettings, a: HomeActions,
    tablet: Boolean, short: Boolean, smallPortrait: Boolean, wide: Boolean, modifier: Modifier, onDetails: () -> Unit) {
    val c = fridayOption2Palette(s.theme)
    val h = heroInfo(state, s, null)
    val ur = L10n.isUrdu(s)
    val gallery = false // Native live facts over reference-derived hero art.
    val iq = PrayerEngine.iqamah(s, state.today, h.prayer)
    // The status line is the single countdown. Extra metadata only appears when there is real iqamah data.
    val iqLabel = iq?.let { "${L10n.word(s, "Iqamah")} ${L10n.at(it, s.use24h)}" }
    val status = (if (state.justPassed) L10n.ago(s, state.delta) else L10n.inForState(s, state)).replaceFirstChar { it.uppercase() }
    val detailLabel = words(s, "View prayer details", "نماز کی تفصیل")
    val detailAction = if (ur) "← $detailLabel" else "$detailLabel →"
    BoxWithConstraints(modifier.testTag("home-hero")) {
        val roomy = tablet && maxHeight > 480.dp * LocalDensity.current.fontScale
        val tight = !tablet && maxHeight < (if (gallery) 170.dp else 210.dp) * LocalDensity.current.fontScale
        val numberSize = if (gallery) {
            if (tablet) (if (ur) 80f else 115f) else if (short) (if (ur) 40f else 56f) else (if (ur) (if (tight) 38f else 44f) else if (tight) 56f else 65f)
        } else if (tablet) (if (ur) (if (roomy) 98f else 76f) else if (roomy) 154f else 122f)
        else if (short) (if (ur) (if (s.theme == AppTheme.KISWAH) 40f else 48f) else if (s.theme == AppTheme.KISWAH) 68f else 72f) else if (tight) (if (ur) 44f else 56f) else (if (ur) 64f else 82f)
        // Reserve full native label heights before growing a compact landscape clock.
        val scale = LocalDensity.current.fontScale
        // Budget every native text line before allocating the display clock. Garden art
        // makes MA2's available hero shorter, especially with Iqamah and Urdu enabled.
        val arabicLine = if (tablet) (if (roomy) 112f else 89.6f) else if (short || tight) 34f else 61.6f
        val nameLine = if (ur) 0f else if (tablet) 25f else if (short) 15f else 17.5f
        val kickerLine = if (short) (if (ur) 16.2f else 11.25f) else if (tablet) (if (ur) 23.4f else 16.25f) else if (ur) 18f else 12.5f
        val statusLine = if (tablet) (if (ur) 27f else 18.75f) + 6f else (if (ur) 20f else 14f) + 6f
        val iqamahLine = if (iqLabel == null) 0f else if (tablet) (if (ur) 21.6f else 15f) else if (ur) 18f else 12.5f
        val fridayLine = if (maxHeight > 250.dp * scale) (if (ur) 25.2f else 17.5f) else 0f
        val reserved = (if (fridayLine > 0f) 26f else 0f) + fridayLine + arabicLine + nameLine + kickerLine + statusLine + iqamahLine + (if (!tablet && !short) 44f else 16f)
        val fittedNumberSize = minOf(numberSize, ((maxHeight.value / scale - reserved) / 1.28f).coerceAtLeast(20f))
        val base = Modifier.fillMaxSize().clip(RoundedCornerShape(if (tablet) 24.dp else 18.dp))
            .then(if (gallery) Modifier.background(c.panel.copy(alpha = 1f)).border(.8.dp, c.accent.copy(alpha = .22f), RoundedCornerShape(if (tablet) 24.dp else 18.dp))
                else if (s.theme == AppTheme.CELESTIAL_MERIDIAN) Modifier.drawBehind {
                    // Fade to the original artwork in every direction; no rectangular panel edge.
                    drawRect(Brush.radialGradient(
                        listOf(c.background.copy(alpha = .72f), c.background.copy(alpha = .40f), Color.Transparent),
                        center = Offset(size.width * .5f, size.height * .48f),
                        radius = minOf(size.width, size.height) * .48f))
                } else Modifier)
            .clickable(role = Role.Button, onClickLabel = detailLabel, onClick = onDetails)
        if (!(wide && s.theme == AppTheme.PRAYER_GALLERY)) FridayMihrabReveal(s, Modifier.matchParentSize())
        Column(base.padding(horizontal = if (tablet) 18.dp else 8.dp, vertical = if (short) 2.dp else if (tight) 4.dp else 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (!short) WorshipText(h.kickerLabel, s, if (tablet) 13f else if (tight) 9f else 10f, c.secondary,
                Modifier.testTag("friday-hero-kicker"), center = true, lines = 1)
            val names: @Composable (Modifier) -> Unit = { m ->
                val arabicSize = if (gallery) (if (tablet) 38 else if (short) 20 else 30)
                    else if (tablet) (if (roomy) 80 else 64) else if (short) 24 else if (smallPortrait) 28 else 38
                Column(m, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (s.jumuahEnabled && h.prayer == Prayer.DHUHR) "الجمعة" else h.arabic,
                        Modifier.testTag("friday-hero-arabic"), fontFamily = if (s.theme == AppTheme.KISWAH) KiswahLandscapeArabic else Amiri,
                        fontSize = arabicSize.sp, lineHeight = (arabicSize * 1.4f).sp, color = if (s.theme == AppTheme.MIQAAT) c.ink else c.accent, maxLines = 1)
                    if (!ur) WorshipText(h.label.uppercase(Locale.ROOT), s,
                        if (tablet) 20f else if (short) 11f else 13f, c.ink,
                        Modifier.testTag("friday-hero-name"), center = true, lines = 1)
                }
            }
            // Gallery's editorial portrait composition places the clock beside the prayer name.
            // Landscape keeps a compact stack beneath a separate framed garden panorama.
            if (gallery && !short) Row(Modifier.fillMaxWidth().padding(horizontal = if (tablet) 28.dp else 16.dp), verticalAlignment = Alignment.CenterVertically) {
                names(Modifier.weight(.43f))
                FridayClock(state, s, fittedNumberSize, Modifier.weight(.57f).clickable(onClick = a.onToggleRelative))
            } else {
                if (!tablet) Column(horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(if (s.jumuahEnabled && h.prayer == Prayer.DHUHR) "الجمعة" else h.arabic,
                        Modifier.testTag("friday-hero-arabic"), fontFamily = if (s.theme == AppTheme.KISWAH) KiswahLandscapeArabic else Amiri,
                        fontSize = (if (short || tight) 24 else 44).sp, lineHeight = (if (short || tight) 34f else 61.6f).sp, color = if (s.theme == AppTheme.MIQAAT) c.ink else c.accent, maxLines = 1)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (short) WorshipText(h.kickerLabel, s, 9f, c.secondary, Modifier.testTag("friday-hero-kicker"), center = true, lines = 1)
                        if (!ur) WorshipText(h.label.uppercase(Locale.ROOT), s, if (short) 12f else 14f, c.ink, Modifier.testTag("friday-hero-name"), lines = 1)
                    }
                } else names(Modifier.fillMaxWidth())
                FridayClock(state, s, fittedNumberSize, Modifier.fillMaxWidth(if (tablet) .68f else .82f).clickable(onClick = a.onToggleRelative))
            }
            Row(Modifier.clip(RoundedCornerShape(24.dp)).background(c.accent.copy(alpha = if (gallery) .07f else .10f))
                .padding(horizontal = if (tablet) 18.dp else 12.dp, vertical = if (short) 0.dp else if (tight) 2.dp else 3.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.Schedule, null, Modifier.size(if (tablet) 17.dp else 12.dp), tint = c.accent)
                WorshipText(status, s, if (tablet) 15f else if (short) 9f else if (tight) 10f else 11f, c.accent,
                    Modifier.testTag("friday-hero-status"), center = true, lines = 1)
            }
            if (iqLabel != null) WorshipText(iqLabel, s, if (tablet) 12f else if (short) 8f else if (tight) 9f else 10f,
                c.secondary, Modifier.testTag("friday-hero-iqamah"), center = true, lines = 1)
            if (fridayLine > 0f) WorshipText(words(s, "◇ ── Friday ── ◇", "◇ ── جمعہ ── ◇"), s, 14f, c.accent,
                Modifier.testTag("friday-hero-day"), center = true, lines = 1)

        }
    }
}

@Composable private fun FridayMiqaatHeader(modifier: Modifier) {
    Image(painterResource(R.drawable.friday_miqaat_header), null, modifier.clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop, alignment = Alignment.BottomCenter)
}
