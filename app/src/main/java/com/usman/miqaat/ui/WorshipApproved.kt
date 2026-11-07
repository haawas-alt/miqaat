package com.usman.miqaat.ui

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.MenuBook
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

@Composable private fun WorshipSurface(s: AppSettings, modifier: Modifier = Modifier, selected: Boolean = false, ornate: Boolean = false, fill: Color? = null, framed: Boolean = true, cornerRadius: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit) {
    val c = fridayOption2Palette(s.theme)
    val shape = if (ornate) CutCornerShape(10.dp) else RoundedCornerShape(cornerRadius)
    Column(modifier.clip(shape).background(fill ?: if (selected) c.accent.copy(alpha = .22f) else c.panel)
        .then(if (framed) Modifier.border(if (selected) 1.dp else .6.dp, c.accent.copy(alpha = if (selected) .85f else .32f), shape) else Modifier), content = content)
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

/**
 * MA2 and GA2 were originally wired to each other's approved Friday artwork.
 * Translate only at the Friday composition boundary so ordinary-day screens,
 * persisted theme selection, and prayer data retain their established identity.
 */
internal fun fridayApprovedVisualTheme(theme: AppTheme): AppTheme = when (theme) {
    AppTheme.MIQAAT -> AppTheme.PRAYER_GALLERY
    AppTheme.PRAYER_GALLERY -> AppTheme.MIQAAT
    else -> theme
}

@Composable internal fun FridayApprovedHome(state: PrayerState, s: AppSettings, a: HomeActions, onClosePreview: (() -> Unit)? = null) {
    FridayApprovedVisualHome(state, s.copy(theme = fridayApprovedVisualTheme(s.theme)), a, onClosePreview)
}

@Composable private fun FridayApprovedVisualHome(state: PrayerState, s: AppSettings, a: HomeActions, onClosePreview: (() -> Unit)? = null) {
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
        // Urdu rows have two native lines too; 56dp fits their measured line
        // heights without taking the display clock's space at larger font scales.
        val tabletPrayerRowHeight = if (L10n.isUrdu(s)) {
            if (s.theme == AppTheme.MIQAAT) 56.dp else 64.dp
        } else 52.dp
        FridayOption2Backdrop(s, wide, Modifier.matchParentSize())
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = if (tablet) 22.dp else 10.dp)
            .testTag("friday-home")) {
            val headerTheme=when(s.theme){AppTheme.CELESTIAL_MERIDIAN->"celestial";AppTheme.PRAYER_GALLERY->"gallery";else->"kiswah"}
            val header: @Composable () -> Unit = {
            Row(Modifier.fillMaxWidth().height(if (tablet) 62.dp else 48.dp), verticalAlignment = Alignment.CenterVertically) {
                if (onClosePreview != null) IconButton(onClick = onClosePreview, modifier = Modifier.size(48.dp).testTag("jummah-preview-close")) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = c.accent)
                }
                if (s.theme == AppTheme.KISWAH || s.theme == AppTheme.CELESTIAL_MERIDIAN || s.theme == AppTheme.PRAYER_GALLERY) {
                    Row(Modifier.weight(1f).heightIn(min = 48.dp).clickable(onClick = a.onOpenLocation),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(if(tablet) 18.dp else if(s.theme==AppTheme.PRAYER_GALLERY)15.dp else 13.dp), tint = c.accent)
                        Spacer(Modifier.width(if(s.theme==AppTheme.PRAYER_GALLERY)5.dp else 3.dp))
                        Column(Modifier.weight(1f)) {
                            WorshipText(if(onClosePreview != null) words(s,"Jumuʿah preview","جمعہ کا پیش منظر") else s.locationName,
                                s,if(tablet) 16f else 11f,c.ink,Modifier.testTag("$headerTheme-header-location"),lines=1)
                            if(!wide && (s.theme != AppTheme.PRAYER_GALLERY || tablet)) {
                            WorshipText(if(L10n.isUrdu(s)) L10n.fridayDate(state.now.toLocalDate()) else
                                state.now.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy",Locale.ENGLISH)),
                                s,if(tablet) 11f else 9f,c.secondary,Modifier.testTag("$headerTheme-header-date"),lines=1)
                            }
                        }
                    }
                    if(wide)Column(Modifier.weight(.9f),horizontalAlignment=Alignment.CenterHorizontally) {
                        WorshipText(words(s,"Friday","جمعہ"),s,if(tablet)13f else 10f,c.accent,
                            Modifier.testTag("$headerTheme-header-day"),center=true,lines=1)
                        WorshipText(if(L10n.isUrdu(s))L10n.fridayDate(state.now.toLocalDate()) else
                            state.now.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy",Locale.ENGLISH)),s,
                            if(tablet)11f else 9f,c.secondary,Modifier.testTag("$headerTheme-header-date"),center=true,lines=1)
                    }
                    if (s.theme != AppTheme.PRAYER_GALLERY || wide || tablet) Column(Modifier.width(if(tablet) 160.dp else 100.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                        if(!wide)WorshipText(words(s,"Friday","جمعہ"),s,if(tablet) (if(L10n.isUrdu(s)) 11f else 13f) else 10f,c.accent,
                            Modifier.testTag("$headerTheme-header-day"),center=true,lines=1)
                        WorshipText(if(!L10n.isUrdu(s))
                            ((when(s.theme){AppTheme.KISWAH->LocalKiswahFridayNow.current;AppTheme.PRAYER_GALLERY->LocalGalleryFridayNow.current;else->LocalCelestialFridayNow.current})?:state.now).format(java.time.format.DateTimeFormatter.ofPattern(if(s.use24h)"HH:mm:ss" else "h:mm:ss a",Locale.ENGLISH))
                            else L10n.at(state.now,s.use24h),s,if(tablet) (if(L10n.isUrdu(s)) 17f else 19f) else 13f,c.ink,
                            Modifier.testTag("$headerTheme-header-clock"),center=true,lines=1)
                    }
                } else if (s.theme == AppTheme.MIQAAT) {
                    Row(Modifier.weight(.8f).heightIn(min = 48.dp).clickable(onClick = a.onOpenLocation),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(if (tablet) 18.dp else 14.dp), tint = c.ink)
                        Spacer(Modifier.width(3.dp))
                        WorshipText(if (onClosePreview != null) words(s, "Jumuʿah preview", "جمعہ کا پیش منظر") else s.locationName,
                            s, if (tablet) 18f else 12f, c.ink, Modifier.weight(1f), lines = 1)
                    }
                    Column(Modifier.weight(1.2f), horizontalAlignment = Alignment.CenterHorizontally) {
                        WorshipText(if (L10n.isUrdu(s)) L10n.fridayDate(state.now.toLocalDate()) else state.now.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
                            s, if (tablet) 13f else 10f, c.ink, center = true, lines = 1)
                        WorshipText(L10n.at(state.now, s.use24h), s, if (tablet) 22f else 16f, c.ink, center = true, lines = 1)
                    }
                } else {
                Column(Modifier.weight(1f).clickable(onClick = a.onOpenLocation)) {
                    WorshipText(if (onClosePreview != null) words(s, "Jumuʿah preview", "جمعہ کا پیش منظر") else s.locationName, s, if (tablet) 21f else 14f, c.ink, lines = 1)
                    WorshipText(if (L10n.isUrdu(s)) L10n.fridayDate(state.now.toLocalDate()) else state.now.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)), s, if (tablet) 14f else 11f, c.secondary, lines = 1)
                }
                WorshipText(L10n.at(state.now, s.use24h), s, if (tablet) 22f else 16f, c.ink, Modifier.padding(horizontal = 8.dp), lines = 1)
                }
                if(s.theme==AppTheme.PRAYER_GALLERY) IconButton(onClick=a.onOpenSettings,modifier=Modifier.size(if(tablet)48.dp else 44.dp)) {
                    Icon(Icons.Outlined.Settings,Str[R.string.s_settings],Modifier.size(if(tablet)24.dp else 22.dp),tint=c.accent)
                }
                if (s.theme == AppTheme.MIQAAT || s.theme == AppTheme.PRAYER_GALLERY) Box {
                    IconButton(onClick = { more = true }, modifier = Modifier.size(if(s.theme==AppTheme.PRAYER_GALLERY&&!tablet)44.dp else 48.dp)) {
                        if(s.theme==AppTheme.PRAYER_GALLERY) Ma2FridayGridIcon(c.accent,Modifier.size(if(tablet)22.dp else 20.dp)
                            .semantics { contentDescription=words(s,"More actions and alerts","مزید اختیارات اور انتباہات") })
                        else Icon(Icons.Outlined.AccessTime,words(s,"More actions and alerts","مزید اختیارات اور انتباہات"),Modifier.size(24.dp),tint=c.accent)
                    }
                    DropdownMenu(expanded = more, onDismissRequest = { more = false }) {
                        contextLinks.forEach { link -> DropdownMenuItem(text = { Text(link.label) }, onClick = {
                            more = false; link.onClick?.invoke()
                        }) }
                    }
                }
                if(s.theme!=AppTheme.PRAYER_GALLERY) IconButton(onClick = a.onOpenSettings, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Outlined.Settings, Str[R.string.s_settings],
                        Modifier.size(24.dp),
                        tint = if (s.theme == AppTheme.MIQAAT) c.ink else c.accent)
                }
            }
            }
            // MA2's location and clock sit in the ivory sky above its skyline, as on
            // the approved board; a separate solid header strip broke that composition.
            if (!short && s.theme == AppTheme.MIQAAT) {
                val skylineHeight = if (wide) 100.dp else if (tablet) 80.dp else if (smallPortrait) 44.dp else 48.dp
                Box(Modifier.fillMaxWidth().height((if (tablet) 62.dp else 48.dp) + skylineHeight)) {
                    FridayMiqaatHeader(Modifier.testTag("friday-gallery-vista").matchParentSize())
                    header()
                }
            } else if(short && s.theme==AppTheme.MIQAAT) {
                Box(Modifier.fillMaxWidth().height(58.dp)) {
                    // Fit the shallow panorama so the skyline stays below live header facts.
                    FridayMiqaatHeader(Modifier.testTag("friday-gallery-vista").matchParentSize(), Alignment.BottomCenter, ContentScale.FillBounds)
                    header()
                }
            } else header()
            val banner: @Composable () -> Unit = { FridayMasthead(s, tablet, wide, a.onOpenFriday) }
            val hero: @Composable (Modifier) -> Unit = { m ->
                // One scene contains the live hero and its two shortcuts, as on
                // the approved boards. Painting separate hero-only rectangles cut
                // the courtyard off immediately above the buttons.
                BoxWithConstraints(m) {
                    val artworkHeight=maxHeight
                    val artworkPanelInset = when {
                        s.theme == AppTheme.KISWAH -> maxWidth * (if(wide) .24f else if(tablet) .20f else .15f)
                        s.theme == AppTheme.MIQAAT && wide -> maxWidth * .30f
                        else -> 0.dp
                    }
                    if (s.theme == AppTheme.MIQAAT) {
                        // Recover the composition from each approved viewport rather
                        // than cropping a generic outdoor painting into the phone arch.
                        FridayMihrabReveal(s, Modifier.matchParentSize(),
                            when {
                                wide -> R.drawable.friday_miqaat_landscape_v2
                                !tablet -> R.drawable.friday_miqaat_phone_v2
                                else -> R.drawable.friday_miqaat_scene_v2
                            },
                            artworkContentScale = ContentScale.Crop)
                    } else if (!(wide && s.theme == AppTheme.PRAYER_GALLERY)) {
                        if (s.theme == AppTheme.PRAYER_GALLERY && !wide) Ma2FridayPortraitArtwork(Modifier.matchParentSize())
                        else FridayMihrabReveal(s, Modifier.matchParentSize(),
                            artworkContentScale = if(s.theme == AppTheme.KISWAH || (s.theme==AppTheme.CELESTIAL_MERIDIAN && wide)) ContentScale.FillBounds else ContentScale.Crop,
                            kiswahPortraitPanel = if(s.theme==AppTheme.KISWAH && !wide) { if(tablet).20f else .15f } else null)
                    }
                    Column(Modifier.fillMaxSize()) {
                        FridayHero(state, s, a, tablet, short, smallPortrait, wide,
                            Modifier.weight(1f).fillMaxWidth()) { why = state.hero }
                        FridayActions(s, state.now, a, short, tablet,
                            Modifier.absolutePadding(left=if(s.theme==AppTheme.KISWAH && !wide && !tablet)12.dp else 0.dp,right = artworkPanelInset))
                        if(!wide && s.artTheme!=ArtTheme.MINIMAL) {
                            // Keep the actions beside the Friday facts, with a visible
                            // courtyard below them rather than a large gap above.
                            val floor=when(s.theme) {
                                AppTheme.CELESTIAL_MERIDIAN -> if(tablet).08f else .025f
                                AppTheme.PRAYER_GALLERY -> if(tablet).06f else .025f
                                AppTheme.MIQAAT -> .02f
                                else -> 0f
                            }
                            Spacer(Modifier.height(artworkHeight*floor))
                        }
                    }
                }
            }
            val prayers: @Composable (Modifier) -> Unit = { m ->
                WorshipSurface(s, m.background(c.background.copy(alpha = .72f), RoundedCornerShape(6.dp)).testTag("home-prayers"),
                    fill = if (s.theme == AppTheme.MIQAAT) Color.Transparent else if(s.theme == AppTheme.KISWAH) Color(0xFF050707).copy(alpha=.9f) else null,
                    framed = s.theme != AppTheme.MIQAAT, cornerRadius = if (s.theme == AppTheme.MIQAAT) 0.dp else if(s.theme==AppTheme.KISWAH)12.dp else 14.dp) {
                    BoxWithConstraints(Modifier.fillMaxSize().testTag("friday-prayer-viewport")) {
                    val scrollPrayers = maxHeight < 48.dp * listedPrayers(s).size
                    Column(Modifier.fillMaxSize().then(if(scrollPrayers)Modifier.verticalScroll(rememberScrollState()).testTag("friday-prayer-scroll") else Modifier)) {
                    listedPrayers(s).forEach { p ->
                        val r = fridayRowInfo(p, state, s)
                        val selected = r.isNow || r.isNext && listedPrayers(s).none { galleryRowInfo(it, state, s).isNow }
                        Row(Modifier.fillMaxWidth().then(if(scrollPrayers)Modifier.height(48.dp) else Modifier.weight(1f)).heightIn(min = 48.dp)
                            .then(if (selected) Modifier.clip(RoundedCornerShape(if(s.theme==AppTheme.PRAYER_GALLERY)12.dp else 4.dp)).background(Brush.horizontalGradient(
                                if (s.theme == AppTheme.MIQAAT) listOf(Color(0xFFD5E3CE), Color(0xFFE7EDDC))
                                else if(s.theme == AppTheme.KISWAH) listOf(Color(0xFF98773C).copy(alpha=.88f),Color(0xFF3B2E1C).copy(alpha=.94f))
                                else if(s.theme==AppTheme.PRAYER_GALLERY) listOf(Color(0xFFC49A50).copy(alpha=.92f),Color(0xFF60411F).copy(alpha=.94f),Color(0xFF36291C).copy(alpha=.94f))
                                else listOf(Color(0xFFA67C3E).copy(alpha = .9f), Color(0xFF49301D).copy(alpha = .94f))))
                                .then(if (s.theme == AppTheme.MIQAAT) Modifier else Modifier.border(1.dp, c.accent.copy(alpha = .9f), RoundedCornerShape(if(s.theme==AppTheme.PRAYER_GALLERY)12.dp else 4.dp))) else Modifier)
                            .drawBehind { drawLine(c.accent.copy(alpha = .12f), Offset(8.dp.toPx(), size.height), Offset(size.width - 8.dp.toPx(), size.height), 1f) }
                            .testTag("approved-row-${p.name}").semantics(mergeDescendants = true) { contentDescription = r.spoken }
                            .clickable { why = p }.padding(horizontal = if (tablet) 14.dp else 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (tablet) 10.dp else 5.dp)) {
                            if (s.theme == AppTheme.PRAYER_GALLERY) {
                                Ma2FridayPrayerIcon(p, c.accent, Modifier.size(if(tablet)26.dp else 20.dp))
                            } else if (s.theme == AppTheme.MIQAAT && p == Prayer.ASR) {
                                MiqaatFridayAsrIcon(Color(0xFFE66C20), Modifier.size(if (tablet) 24.dp else 18.dp))
                            } else Icon(if (s.theme == AppTheme.MIQAAT) when (p) {
                                Prayer.ISHA -> Icons.Outlined.DarkMode
                                Prayer.SUNRISE, Prayer.MAGHRIB -> Icons.Outlined.WbTwilight
                                else -> Icons.Outlined.WbSunny
                            } else if (s.theme == AppTheme.KISWAH) when(p) {
                                Prayer.FAJR, Prayer.ISHA -> Icons.Outlined.DarkMode
                                Prayer.DHUHR -> Icons.Outlined.Mosque
                                Prayer.ASR -> Icons.Outlined.LocationCity
                                Prayer.MAGHRIB -> Icons.Outlined.WbTwilight
                                else -> Icons.Outlined.WbSunny
                            } else if(s.theme==AppTheme.CELESTIAL_MERIDIAN || s.theme==AppTheme.PRAYER_GALLERY) when(p) {
                                Prayer.FAJR,Prayer.ASR,Prayer.MAGHRIB -> Icons.Outlined.WbTwilight
                                Prayer.ISHA -> Icons.Outlined.DarkMode
                                else -> Icons.Outlined.WbSunny
                            } else if (p == Prayer.FAJR || p == Prayer.ISHA) Icons.Outlined.DarkMode else if (p == Prayer.ASR) Icons.Outlined.WbTwilight else Icons.Outlined.WbSunny,
                                null, Modifier.size(if (tablet) 24.dp else 18.dp), tint = if (s.theme == AppTheme.MIQAAT) Color(0xFFE66C20) else c.accent)
                            val rowInk = if(s.theme==AppTheme.PRAYER_GALLERY) Color(0xFFF9F0DC) else c.ink
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Center) {
                                WorshipText(r.label, s, if(s.theme==AppTheme.PRAYER_GALLERY) { if(tablet)18f else if(short)15f else 16f } else if (tablet) 17f else if (short) 14f else 15f, rowInk, lines = 1)
                            }
                            Column(Modifier.width(if (tablet && wide) 138.dp else if (tablet) 168.dp else if (short && s.theme==AppTheme.PRAYER_GALLERY) 92.dp else if (short) 102.dp else 116.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                FridayTime(state.today[p], s, if(s.theme==AppTheme.PRAYER_GALLERY) { if(tablet)23f else 19f } else if (tablet) 22f else 18f,
                                    Modifier.fillMaxWidth(), "friday-time-${p.name}", rowInk, TextAlign.Center, emphasized = selected && s.theme == AppTheme.MIQAAT)
                            }
                        }
                    }
                    }
                    }
                }
            }
            if (wide) banner()
            if (wide) Row(Modifier.weight(1f).fillMaxWidth().padding(vertical = if(tablet)6.dp else 0.dp), horizontalArrangement = Arrangement.spacedBy(if (tablet) 20.dp else 10.dp)) {
                val prayerWeight = when (s.theme) {
                    AppTheme.KISWAH -> .40f
                    AppTheme.PRAYER_GALLERY -> if (tablet) .35f else .31f
                    else -> .35f
                }
                val gallerySpacer = if (s.theme == AppTheme.PRAYER_GALLERY) if (tablet) .14f else .18f else 0f
                prayers(Modifier.weight(prayerWeight).fillMaxHeight())
                if (gallerySpacer > 0f) Spacer(Modifier.weight(gallerySpacer))
                Column(Modifier.weight(1f - prayerWeight - gallerySpacer).fillMaxHeight()) {
                    hero(Modifier.weight(1f).fillMaxWidth())
                    if(short) FridayDock(s, state, a, tablet, wide)
                }
            } else {
                banner()
                val idealHeroHeight = if (tablet) (viewportHeight * when (s.theme) {
                    AppTheme.MIQAAT, AppTheme.KISWAH -> .47f
                    AppTheme.CELESTIAL_MERIDIAN -> .48f
                    AppTheme.PRAYER_GALLERY -> .46f
                    else -> .46f
                }).coerceIn(450.dp, 620.dp)
                    else if (s.theme == AppTheme.MIQAAT) (if (smallPortrait) 264.dp else 342.dp)
                    else if (s.theme == AppTheme.KISWAH) (if (smallPortrait) 270.dp else 350.dp)
                    else if(s.theme == AppTheme.PRAYER_GALLERY) (if(smallPortrait)260.dp else 330.dp)
                    else (if (smallPortrait) 258.dp else 334.dp)
                val mastheadHeight = fridayBandHeight(s.theme,tablet,wide,typeScale)
                val headerHeight = (if(tablet)62.dp else 48.dp) + if(s.theme==AppTheme.MIQAAT) {
                    if(tablet)80.dp else if(smallPortrait)44.dp else 48.dp
                } else 0.dp
                val dockHeight = fridayDockHeight(tablet,wide)
                val shortcutsHeight = if(tablet){if(L10n.isUrdu(s))69.dp else 61.dp}else 57.dp
                val prayerMinimum = (if(tablet)tabletPrayerRowHeight*typeScale else 48.dp)*listedPrayers(s).size
                val availableHero = (safeHeight-headerHeight-mastheadHeight-dockHeight-6.dp-prayerMinimum)
                    .coerceAtLeast(140.dp+shortcutsHeight)
                val heroHeight = (idealHeroHeight*typeScale+shortcutsHeight).coerceAtMost(availableHero)
                hero(Modifier.fillMaxWidth().height(heroHeight))
                Spacer(Modifier.height(6.dp))
                prayers(Modifier.fillMaxWidth().then(if(s.theme==AppTheme.PRAYER_GALLERY)Modifier.padding(horizontal=if(tablet)36.dp else 12.dp) else Modifier).weight(1f))
            }
            if(!short) FridayDock(s, state, a, tablet, wide)
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

@Composable private fun FridayActions(s: AppSettings, now: ZonedDateTime, a: HomeActions, short: Boolean, tablet: Boolean, modifier: Modifier = Modifier) {
    val c = fridayOption2Palette(s.theme)
    val prefs = LocalContext.current.getSharedPreferences("miqaat_friday", Context.MODE_PRIVATE)
    val key = now.toLocalDate().toString()
    val kahf = prefs.getBoolean("kahf_$key", false)
    val salawat = prefs.getInt("salawat_$key", 0)
    val approvedWidth = if (tablet) when (s.theme) {
        AppTheme.MIQAAT, AppTheme.KISWAH -> 450.dp
        AppTheme.CELESTIAL_MERIDIAN -> 380.dp
        AppTheme.PRAYER_GALLERY -> 390.dp
    } else if (short) 420.dp else when (s.theme) {
        AppTheme.MIQAAT, AppTheme.PRAYER_GALLERY -> 330.dp
        AppTheme.KISWAH -> 320.dp
        AppTheme.CELESTIAL_MERIDIAN -> 300.dp
    }
    val approvedHeight = if (tablet) { if (L10n.isUrdu(s)) 64.dp else 60.dp }
        else if (short) 60.dp
        else when (s.theme) {
            AppTheme.MIQAAT -> 54.dp
            AppTheme.KISWAH, AppTheme.PRAYER_GALLERY -> 56.dp
            else -> 52.dp
        }
    Row(modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
        .widthIn(max = approvedWidth)
        .padding(top = if (short) 0.dp else 5.dp)
        .height(approvedHeight),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(Triple(Icons.Filled.MenuBook, words(s, "Al-Kahf", "سورۃ الکہف"), words(s, if (kahf) "Read this Friday ✓" else "Mark as read", if (kahf) "پڑھ لی ✓" else "پڑھنے کا اندراج")),
            Triple(Icons.Outlined.FavoriteBorder, words(s, "Salawat", "درود شریف"), words(s, "$salawat this Friday", "اس جمعہ: $salawat"))).forEachIndexed { i, item ->
            WorshipSurface(s, Modifier.weight(1f).fillMaxHeight().background(Brush.verticalGradient(
                if(s.theme==AppTheme.MIQAAT) { if(i==0)listOf(Color(0xFF246B50),Color(0xFF0D4133)) else listOf(Color(0xFFC86538),Color(0xFF9E3F24)) }
                else listOf(c.accent.copy(alpha=.18f),c.background.copy(alpha=.96f))),RoundedCornerShape(10.dp)).testTag("friday-action-$i")
                .border(if (s.theme == AppTheme.MIQAAT) 0.dp else 1.25.dp, c.accent.copy(alpha=.95f), RoundedCornerShape(10.dp))
                .clickable(role = Role.Button, onClick = a.onOpenFriday), fill=Color.Transparent,
                framed = s.theme != AppTheme.MIQAAT && s.theme != AppTheme.KISWAH && s.theme != AppTheme.CELESTIAL_MERIDIAN && s.theme != AppTheme.PRAYER_GALLERY, cornerRadius = if (s.theme == AppTheme.MIQAAT || s.theme == AppTheme.KISWAH || s.theme == AppTheme.CELESTIAL_MERIDIAN || s.theme == AppTheme.PRAYER_GALLERY) 10.dp else 14.dp) {
                Row(Modifier.fillMaxSize().padding(horizontal = if (tablet) 14.dp else 9.dp)
                    .semantics { contentDescription = "${item.second}. ${item.third}" },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if(tablet)11.dp else 9.dp, Alignment.CenterHorizontally)) {
                    if (i == 1 && s.theme == AppTheme.PRAYER_GALLERY) Ma2FridaySalawatIcon(c.accent, Modifier.size(if(tablet)33.dp else 27.dp))
                    else if (i == 1) SalawatBeads(if (s.theme == AppTheme.MIQAAT) Color.White else c.accent, Modifier.size(if (tablet) 29.dp else 23.dp))
                    else Icon(if(s.theme==AppTheme.PRAYER_GALLERY)Icons.Outlined.MenuBook else item.first, null,
                        Modifier.size(if (tablet) 28.dp else 23.dp), tint = if(s.theme == AppTheme.MIQAAT) Color.White else c.accent)
                    WorshipText(item.second, s,
                        if (tablet) 17f else if (s.theme == AppTheme.MIQAAT) 15f else 14f,
                        if(s.theme == AppTheme.MIQAAT) Color.White else c.ink,
                        Modifier.testTag("friday-action-title-$i"), lines = 1)
                }
            }
        }
    }
}

@Composable private fun FridayDock(s: AppSettings, state: PrayerState, a: HomeActions, tablet: Boolean, wide: Boolean) {
    if(s.theme==AppTheme.PRAYER_GALLERY) {
        GalleryFridayDock(s,state,a,tablet,wide)
        return
    }
    val c = fridayOption2Palette(s.theme)
    val mode = adhkarModes(state, s).firstOrNull() ?: AdhkarMode.MORNING
    val iconColor = if (s.theme == AppTheme.MIQAAT) c.ink else c.accent
    Row(Modifier.fillMaxWidth().height(fridayDockHeight(tablet,wide)).padding(vertical = 3.dp).background(c.background.copy(alpha = .94f), RoundedCornerShape(14.dp))
        .then(if(s.theme==AppTheme.CELESTIAL_MERIDIAN)Modifier.border(.7.dp,c.secondary.copy(alpha=.40f),RoundedCornerShape(14.dp)) else Modifier),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        val items: List<Triple<androidx.compose.ui.graphics.vector.ImageVector, String, () -> Unit>> = listOfNotNull(if (s.showQibla) Triple(Icons.Outlined.Explore, L10n.word(s, "Qibla"), a.onOpenQibla) else null,
            if (s.adhkarEnabled) Triple(Icons.Outlined.MenuBook, words(s, "Adhkar", "اذکار"), { a.onOpenAdhkar(mode) }) else null,
            Triple(if(s.theme==AppTheme.KISWAH) Icons.Outlined.School else Icons.Outlined.MenuBook, words(s, "Learn", "سیکھیں"), a.onOpenLearn),
            Triple(Icons.Outlined.CalendarMonth, words(s, "Timetable", "اوقات"), a.onOpenTimetable))
        items.forEachIndexed { index, (icon, label, action) ->
            if(s.theme!=AppTheme.MIQAAT && index>0) Spacer(Modifier.width(.5.dp).fillMaxHeight(.55f).background(c.secondary.copy(alpha=.5f)))
            val inline = tablet && s.theme != AppTheme.MIQAAT
            val itemModifier = Modifier.weight(1f).fillMaxHeight().clickable(role = Role.Button, onClick = action)
            val content: @Composable () -> Unit = {
                if (label == words(s, "Adhkar", "اذکار") && s.theme != AppTheme.KISWAH)
                    SalawatBeads(iconColor, Modifier.size(if(!tablet && wide && L10n.isUrdu(s))20.dp else if (tablet)30.dp else 24.dp).semantics { contentDescription = label })
                else Icon(icon, label, Modifier.size(if(!tablet && wide && L10n.isUrdu(s))20.dp else if (tablet)30.dp else 24.dp), tint = iconColor)
                WorshipText(label, s, if (tablet) 16f else if(wide && L10n.isUrdu(s))10f else 11f, c.ink, center = true, lines = 1)
            }
            if (inline) Row(itemModifier, verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) { content() }
            else Column(itemModifier, horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) { content() }
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
    if(s.theme==AppTheme.CELESTIAL_MERIDIAN) {
        CelestialFridayBanner(tablet,Modifier.fillMaxWidth().height(fridayBandHeight(s.theme,tablet,wide,LocalDensity.current.fontScale)).testTag("friday-banner").clickable(role=Role.Button,onClick=onOpen))
        return
    }
    if(s.theme == AppTheme.KISWAH) {
        KiswahFridayBanner(tablet,wide,Modifier.fillMaxWidth()
            .height(fridayBandHeight(s.theme,tablet,wide,LocalDensity.current.fontScale))
            .testTag("friday-banner").clickable(role=Role.Button,onClick=onOpen))
        return
    }
    if(s.theme==AppTheme.PRAYER_GALLERY) {
        GalleryFridayBanner(s,tablet,Modifier.fillMaxWidth().height(fridayBandHeight(s.theme,tablet,wide,LocalDensity.current.fontScale))
            .testTag("friday-banner").clickable(role=Role.Button,onClick=onOpen))
        return
    }
    val c=fridayOption2Palette(s.theme)
    val miqaat = s.theme == AppTheme.MIQAAT
    Box(Modifier.fillMaxWidth().height(fridayBandHeight(s.theme,tablet,wide,LocalDensity.current.fontScale))
        .background(if(s.theme==AppTheme.MIQAAT) Color(0xFF194B3C) else c.background.copy(alpha=.92f))
        .border(.8.dp, Color(0xFFE7C77E), RoundedCornerShape(if (miqaat) 0.dp else 8.dp)).testTag("friday-banner")
        .clickable(role=Role.Button,onClick=onOpen),contentAlignment=Alignment.Center) {
        if (miqaat) MiqaatDhikrEnds(tablet,wide,Modifier.matchParentSize())
        if (s.theme != AppTheme.MIQAAT) KiswahOrnamentalFrame(
            if (tablet) .60f else .35f, Modifier.matchParentSize())
        val dhikr = if (miqaat) {
            if (L10n.isUrdu(s)) "سُبْحَانَ اللَّهِ   ✦   الْحَمْدُ لِلَّهِ   ✦   اللَّهُ أَكْبَرُ"
            else if (!tablet && !wide) "✦ SubhanAllah ✦ Alhamdulillah ✦ Allahu Akbar ✦"
            else "✦   SubhanAllah   ✦   Alhamdulillah   ✦   Allahu Akbar   ✦"
        } else Kiswah.BAND
        if (miqaat) {
            val measurer = rememberTextMeasurer()
            val density = LocalDensity.current
            val nominal = (if (!L10n.isUrdu(s)) (if (tablet) 16 else 11) else if (tablet) 18 else 12).toFloat()
            val bandStyle = TextStyle(fontFamily = if (L10n.isUrdu(s)) Amiri else FontFamily.Serif,
                fontSize = nominal.sp, lineHeight = (if (tablet) 30 else if (wide) 20 else 24).sp,
                color = Color(0xFFF0D89D), textAlign = TextAlign.Center)
            BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                val fitted = remember(dhikr, nominal, maxWidth, density.fontScale, density.density) {
                    val available = with(density) { (maxWidth - 2.dp).toPx() }.coerceAtLeast(1f)
                    var size = nominal
                    repeat(4) {
                        val layout = measurer.measure(dhikr, bandStyle.copy(fontSize = size.sp), softWrap = false, maxLines = 1)
                        val width = maxOf(layout.size.width.toFloat(), layout.getLineRight(0) - layout.getLineLeft(0))
                        if (width > available) size *= (available / width) * .99f
                    }
                    size
                }
                Text(dhikr, Modifier.testTag("kiswah-calligraphy-band"),
                    style = bandStyle.copy(fontSize = fitted.sp), softWrap = false, maxLines = 1)
            }
        } else Text(dhikr, Modifier.padding(horizontal=10.dp).testTag("kiswah-calligraphy-band"),
            fontFamily=if (s.theme == AppTheme.KISWAH) KiswahLandscapeArabic else Amiri,
            fontSize=(if(tablet) 18 else 12).sp,lineHeight=(if(tablet) 30 else if(wide) 20 else 24).sp,
            color=Color(0xFFF0D89D),textAlign=TextAlign.Center,maxLines=1)
    }
}

/** Gold geometric end panels from MA2's narrow green dhikr strip. */
@Composable private fun MiqaatDhikrEnds(tablet:Boolean,wide:Boolean,modifier:Modifier) {
    BoxWithConstraints(modifier) {
        if(tablet||wide) {
            val leftWidth=minOf(maxWidth*.17f,maxHeight*(95f/30f))
            val rightWidth=minOf(maxWidth*.17f,maxHeight*(81f/30f))
            Image(painterResource(R.drawable.ma2_banner_left),null,Modifier.align(androidx.compose.ui.AbsoluteAlignment.CenterLeft).width(leftWidth).fillMaxHeight(),contentScale=ContentScale.FillBounds)
            Image(painterResource(R.drawable.ma2_banner_right),null,Modifier.align(androidx.compose.ui.AbsoluteAlignment.CenterRight).width(rightWidth).fillMaxHeight(),contentScale=ContentScale.FillBounds)
        } else Canvas(Modifier.matchParentSize()) {
            val d=3.dp.toPx()
            for(x in listOf(7.dp.toPx(),size.width-7.dp.toPx())) {
                val y=size.height/2
                val diamond=Path().apply{moveTo(x,y-d);lineTo(x+d,y);lineTo(x,y+d);lineTo(x-d,y);close()}
                drawPath(diamond,Color(0xFFE7C77E),style=androidx.compose.ui.graphics.drawscope.Stroke(.8.dp.toPx()))
            }
        }
    }
}

/** Native, lining numerals with smaller suffixes. Width is measured before drawing, including Urdu and 24-hour forms. */
@Composable internal fun FridayTime(time: ZonedDateTime, s: AppSettings, numberSize: Float, modifier: Modifier,
    tag: String, color: Color, align: TextAlign = TextAlign.Center, suffixRatio: Float = .55f, emphasized: Boolean = false) {
    val text = L10n.at(time, s.use24h)
    val digits = PrayerEngine.clock(time, s.use24h)
    val start = text.indexOf(digits)
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        fun styled(size: Float) = buildAnnotatedString {
            if (start < 0) append(text) else {
                append(text.substring(0, start))
                withStyle(SpanStyle(fontSize = size.sp, fontFamily = if (tag.startsWith("friday-")) FontFamily.Serif else QuietDisplay,
                    fontWeight = if ((tag == "friday-hero-time" && s.theme != AppTheme.KISWAH && s.theme != AppTheme.CELESTIAL_MERIDIAN && s.theme != AppTheme.PRAYER_GALLERY) || emphasized) FontWeight.Bold else FontWeight.Normal,
                    fontFeatureSettings = "'lnum' 1, 'tnum' 1")) { append(digits) }
                append(text.substring(start + digits.length))
            }
        }
        fun style(size: Float) = TextStyle(fontFamily = worshipFont(s), fontSize = (size * suffixRatio).sp,
            lineHeight = (size * if (tag == "friday-hero-time") 1.28f else 1.2f).sp, platformStyle = PlatformTextStyle(includeFontPadding = false),
            textAlign = align, color = color)
        val resolved = remember(text, numberSize, suffixRatio, emphasized, maxWidth, density.fontScale, density.density, s.theme) {
            val measured = measurer.measure(styled(numberSize), style(numberSize), softWrap = false, maxLines = 1).size.width
            // Urdu suffix shaping and pixel rounding are not exactly linear in sp.
            // Leave MA2 a two-dp inset and remeasure the fitted string, rather than
            // assuming that one proportional resize guarantees a visible glyph fit.
            val available = with(density) { maxWidth.toPx() - if (s.theme == AppTheme.MIQAAT || s.theme == AppTheme.KISWAH || s.theme == AppTheme.CELESTIAL_MERIDIAN || s.theme == AppTheme.PRAYER_GALLERY) 2.dp.toPx() else 0f }
            var fitted = if (measured > available) numberSize * (available / measured).coerceIn(.5f, 1f) else numberSize
            if (s.theme == AppTheme.MIQAAT || s.theme == AppTheme.KISWAH || s.theme == AppTheme.CELESTIAL_MERIDIAN || s.theme == AppTheme.PRAYER_GALLERY) repeat(4) {
                val width = measurer.measure(styled(fitted), style(fitted), softWrap = false, maxLines = 1).size.width
                if (width > available) fitted *= (available / width) * .995f
            }
            fitted
        }
        Text(styled(resolved), Modifier.fillMaxWidth().testTag(tag), style = style(resolved), softWrap = false, maxLines = 1)
    }
}

@Composable private fun FridayClock(state: PrayerState, s: AppSettings, size: Float, modifier: Modifier = Modifier) {
    val c = fridayOption2Palette(s.theme)
    FridayTime(state.heroTime, s, size, modifier, "friday-hero-time",
        if (s.theme == AppTheme.KISWAH) c.accent else c.ink, suffixRatio = if (s.theme == AppTheme.MIQAAT) (if (L10n.isUrdu(s)) .24f else .55f) else .30f)
}

@Composable private fun FridayHero(state: PrayerState, s: AppSettings, a: HomeActions,
    tablet: Boolean, short: Boolean, smallPortrait: Boolean, wide: Boolean, modifier: Modifier, onDetails: () -> Unit) {
    if(s.theme==AppTheme.PRAYER_GALLERY) {
        GalleryFridayHero(state,s,a,tablet,short,modifier,onDetails)
        return
    }
    if(s.theme==AppTheme.CELESTIAL_MERIDIAN) {
        CelestialFridayHero(state,s,a,tablet,short,modifier,onDetails)
        return
    }
    if(s.theme == AppTheme.KISWAH) {
        KiswahFridayHero(state,s,a,tablet,short,wide,modifier,onDetails)
        return
    }
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
        val compactMiqaatTablet = s.theme == AppTheme.MIQAAT && tablet && maxHeight < 360.dp * LocalDensity.current.fontScale
        val tabletArabicSize = if (compactMiqaatTablet) 48 else if (roomy) 80 else 72
        val numberSize = if (s.theme == AppTheme.MIQAAT) {
            // MA2 keeps the same display hierarchy in both languages. The clock
            // measurer fits the translated suffix instead of pre-shrinking Urdu.
            if (tablet) (if (roomy) 124f else 104f) else if (short) 64f else if (tight) 56f else 74f
        } else if (wide && tablet && s.theme == AppTheme.PRAYER_GALLERY) {
            if (ur) 62f else 80f
        } else if (gallery) {
            if (tablet) (if (ur) 80f else 115f) else if (short) (if (ur) 40f else 56f) else (if (ur) (if (tight) 38f else 44f) else if (tight) 56f else 65f)
        } else if (tablet) (if (ur) (if (roomy) 98f else 76f) else if (roomy) 154f else 122f)
        else if (short) (if (ur) (if (s.theme == AppTheme.KISWAH) 40f else 48f) else if (s.theme == AppTheme.KISWAH) 68f else 72f) else if (tight) (if (ur) 44f else 56f) else (if (ur) 64f else 82f)
        // Reserve full native label heights before growing a compact landscape clock.
        val scale = LocalDensity.current.fontScale
        // Budget every native text line before allocating the display clock. Garden art
        // makes MA2's available hero shorter, especially with Iqamah and Urdu enabled.
        val arabicLine = if (tablet) tabletArabicSize * 1.4f else if (short || tight) 34f else 67.2f
        val nameLine = if (ur) 0f else if (tablet) 25f else if (short) 15f else 17.5f
        val kickerLine = if (s.theme == AppTheme.MIQAAT) 0f else if (short) (if (ur) 16.2f else 11.25f) else if (tablet) (if (ur) 23.4f else 16.25f) else if (ur) 18f else 12.5f
        val statusLine = if (tablet) (if (ur) 27f else 18.75f) + 6f else (if (ur) 20f else 14f) + 6f
        val compactMiqaatFooter = s.theme == AppTheme.MIQAAT && maxHeight <= 250.dp * scale
        val iqamahLine = if (iqLabel == null || compactMiqaatFooter) 0f else if (tablet) (if (ur) 21.6f else 15f) else if (ur) 18f else 12.5f
        val fridayLine = if (compactMiqaatFooter) (if (ur) 18f else 12.5f)
            else if (maxHeight > 250.dp * scale) (if (ur) 25.2f else 17.5f) else 0f
        val reserved = (if (fridayLine > 0f && s.theme != AppTheme.MIQAAT) 26f else 0f) + fridayLine + arabicLine + nameLine + kickerLine + statusLine + iqamahLine + (if (!tablet && !short && s.theme != AppTheme.MIQAAT) 44f else 16f)
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
        // Kiswah's medallion panel occupies the right quarter of the painting.
        // Centre live facts in the actual mihrab opening, including in Urdu;
        // physical artwork placement is independent of text reading direction.
        Column(base.absolutePadding(right = when {
            s.theme == AppTheme.KISWAH -> maxWidth * (if(wide) .24f else if(tablet) .20f else .15f)
            s.theme == AppTheme.MIQAAT && wide -> maxWidth * .30f
            else -> 0.dp
        })
            .padding(horizontal = if (tablet) 18.dp else 8.dp, vertical = if (short) 2.dp else if (tight) 4.dp else 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val names: @Composable (Modifier) -> Unit = { m ->
                val arabicSize = if (gallery) (if (tablet) 38 else if (short) 20 else 30)
                    else if (tablet) tabletArabicSize else if (short) 24 else if (smallPortrait) 28 else 38
                Column(m, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (s.jumuahEnabled && h.prayer == Prayer.DHUHR) "الجمعة" else h.arabic,
                        Modifier.testTag("friday-hero-arabic"), fontFamily = if (s.theme == AppTheme.KISWAH) KiswahLandscapeArabic else Amiri,
                        fontSize = arabicSize.sp, lineHeight = (arabicSize * 1.4f).sp, color = if (s.theme == AppTheme.MIQAAT) c.ink else c.accent, maxLines = 1)
                    if (s.theme != AppTheme.MIQAAT) WorshipText(h.kickerLabel, s, if (tablet) 13f else 10f, c.secondary,
                        Modifier.testTag("friday-hero-kicker"), center = true, lines = 1)
                    if (!ur) WorshipText(h.label, s,
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
                        fontSize = (if (short || tight) 24 else 48).sp, lineHeight = (if (short || tight) 34f else 67.2f).sp, color = if (s.theme == AppTheme.MIQAAT) c.ink else c.accent, maxLines = 1)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (s.theme != AppTheme.MIQAAT) WorshipText(h.kickerLabel, s, if (short || tight) 9f else 10f, c.secondary, Modifier.testTag("friday-hero-kicker"), center = true, lines = 1)
                        if (!ur) WorshipText(h.label, s, if (short) 12f else 14f, c.ink, Modifier.testTag("friday-hero-name"), lines = 1)
                    }
                } else names(Modifier.fillMaxWidth())
                FridayClock(state, s, fittedNumberSize, Modifier.fillMaxWidth(if (tablet) .68f else .82f).clickable(onClick = a.onToggleRelative))
            }
            Row(Modifier
                .padding(horizontal = if (tablet) 18.dp else 12.dp, vertical = if (short) 0.dp else if (tight) 2.dp else 3.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (s.theme == AppTheme.MIQAAT) WorshipText(if (ur) h.kickerLabel else h.kickerLabel.lowercase(Locale.ENGLISH).replaceFirstChar { it.uppercase() }, s,
                    if (tablet) 15f else if (short) 9f else if (tight) 10f else 11f, c.ink,
                    Modifier.testTag("friday-hero-kicker"), center = true, lines = 1)
                WorshipText(if (s.theme == AppTheme.MIQAAT && !ur) status.replaceFirstChar { it.lowercase() } else status,
                    s, if (tablet) 15f else if (short) 9f else if (tight) 10f else 11f, c.accent,
                    Modifier.testTag("friday-hero-status"), center = true, lines = 1)
            }
            if (compactMiqaatFooter) {
                // Keep Friday visible without dropping a real congregation fact.
                // A shared line fits the compact mihrab instead of adding another row.
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WorshipText(words(s, "◇ Friday ◇", "◇ جمعہ ◇"), s, 10f, c.accent,
                        Modifier.testTag("friday-hero-day"), center = true, lines = 1)
                    if (iqLabel != null) WorshipText(iqLabel, s, if (short) 8f else 10f,
                        c.secondary, Modifier.testTag("friday-hero-iqamah"), center = true, lines = 1)
                }
            } else {
                if (iqLabel != null) WorshipText(iqLabel, s, if (tablet) 12f else if (short) 8f else if (tight) 9f else 10f,
                    c.secondary, Modifier.testTag("friday-hero-iqamah"), center = true, lines = 1)
                if (fridayLine > 0f) WorshipText(words(s, "◇ ── Friday ── ◇", "◇ ── جمعہ ── ◇"), s, 14f, c.accent,
                    Modifier.testTag("friday-hero-day"), center = true, lines = 1)
            }

        }
    }
}

@Composable private fun FridayMiqaatHeader(modifier: Modifier, alignment: Alignment = Alignment.BottomCenter, scale:ContentScale = ContentScale.Crop) {
    if(scale==ContentScale.FillBounds) {
        val art=ImageBitmap.imageResource(R.drawable.friday_miqaat_header)
        Canvas(modifier.clip(RoundedCornerShape(8.dp))) {
            val w=size.width.roundToInt();val h=size.height.roundToInt()
            drawImage(art,dstSize=IntSize(w,h),filterQuality=FilterQuality.High)
            // Preserve the palms' proportions in the shallow panorama. Their
            // positions avoid the live location, central clock and action icons.
            fun palm(x:Int,y:Int,sw:Int,sh:Int,at:Float) {
                val dh=(h*.88f).roundToInt();val dw=(dh*sw.toFloat()/sh).roundToInt()
                val left=(w*at).roundToInt();val top=h-dh
                val bounds=androidx.compose.ui.geometry.Rect(left.toFloat(),top.toFloat(),(left+dw).toFloat(),h.toFloat())
                drawContext.canvas.saveLayer(bounds,Paint())
                drawImage(art,IntOffset(x,y),IntSize(sw,sh),IntOffset(left,top),IntSize(dw,dh),filterQuality=FilterQuality.High)
                drawRect(Brush.horizontalGradient(0f to Color.Transparent,.18f to Color.Black,.82f to Color.Black,1f to Color.Transparent,
                    startX=left.toFloat(),endX=(left+dw).toFloat()),Offset(left.toFloat(),top.toFloat()),androidx.compose.ui.geometry.Size(dw.toFloat(),dh.toFloat()),blendMode=BlendMode.DstIn)
                drawRect(Brush.verticalGradient(0f to Color.Transparent,.25f to Color.Black,1f to Color.Black,
                    startY=top.toFloat(),endY=h.toFloat()),Offset(left.toFloat(),top.toFloat()),androidx.compose.ui.geometry.Size(dw.toFloat(),dh.toFloat()),blendMode=BlendMode.DstIn)
                drawContext.canvas.restore()
            }
            palm(270,410,280,314,.25f)
            palm(1670,370,390,354,.77f)
        }
    } else Image(painterResource(R.drawable.friday_miqaat_header), null, modifier.clip(RoundedCornerShape(8.dp)), contentScale = scale, alignment = alignment)
}



