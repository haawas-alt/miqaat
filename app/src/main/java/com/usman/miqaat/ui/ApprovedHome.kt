package com.usman.miqaat.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import java.time.Duration

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
internal val ApprovedBody = androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(
    R.font.nunito_sans, FontWeight.Normal, variationSettings = androidx.compose.ui.text.font.FontVariation.Settings(androidx.compose.ui.text.font.FontVariation.weight(500))))
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
internal val ApprovedDisplay = androidx.compose.ui.text.font.FontFamily(androidx.compose.ui.text.font.Font(
    R.font.cormorant_garamond_medium, FontWeight.Medium, variationSettings = androidx.compose.ui.text.font.FontVariation.Settings(androidx.compose.ui.text.font.FontVariation.weight(500))))
private fun approvedUi(s: AppSettings) = if (L10n.isUrdu(s)) Amiri else ApprovedBody

/** Approved October boards. Artwork is decorative; every fact and action is native Compose.
 * Large text is routed to AccessibleHome; short landscape retains its dedicated layout.
 */
@Composable
fun ApprovedHome(state: PrayerState, s: AppSettings, a: HomeActions) {
    val tk = ThemeTokenSets.of(s.theme)
    val gallery = s.theme == AppTheme.PRAYER_GALLERY
    val kiswah = s.theme == AppTheme.KISWAH
    val celestial = s.theme == AppTheme.CELESTIAL_MERIDIAN
    val portraitPolish = phonePortraitPolish(s)
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { p ->
        val day = if (p == state.next && state.nextTime.toLocalDate().isAfter(state.today.date))
            PrayerEngine.times(s, state.nextTime.toLocalDate()) else state.today
        WhyDialog(s, day, p) { why = null }
    }
    val hero = heroInfo(state, s, kicker(state, s))
    val links = doors(state, s, a)
    BoxWithConstraints(Modifier.fillMaxSize().clipToBounds().background(tk.background).testTag("approved-home")) {
        val wide = maxWidth > maxHeight && maxHeight >= 500.dp
        val tablet = maxWidth >= 600.dp
        val compactWide = wide && maxHeight < 700.dp
        val compactGalleryPhone = gallery && !tablet && maxHeight < 760.dp
        val reflow = !wide && (((s.iqamahEnabled || (!gallery && s.showRelative)) && L10n.isUrdu(s) && !tablet) || maxHeight < 700.dp || (!tablet && !gallery && maxHeight < 820.dp))
        Box(Modifier.fillMaxSize()) {
            if (kiswah) {
                Weave(Modifier.fillMaxSize())
                if (s.artTheme == ArtTheme.GEOMETRIC) GirihLattice(Modifier.fillMaxSize(), Kiswah.thread, .065f, 100f)
            } else if (celestial && s.artTheme != ArtTheme.MINIMAL && !portraitPolish) {
                Image(painterResource(R.drawable.celestial_landscape_approved), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Color(0xFF00152C).copy(alpha = .18f)))
            } else if (!gallery) {
                Stars(Modifier.fillMaxSize(), if (portraitPolish) .30f else .7f)
                if (s.artTheme == ArtTheme.GEOMETRIC) GirihLattice(Modifier.fillMaxSize(), alpha = if (portraitPolish) .025f else .06f)
            }
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().displayCutoutPadding()
                .padding(horizontal = if (wide) 24.dp else 12.dp).then(if (reflow) Modifier.verticalScroll(rememberScrollState()) else Modifier)) {
                ApprovedHeader(state, s, a, tk, wide, portraitPolish)
                if (kiswah) KiswahBand(wide)
                if (wide && gallery) {
                    Box(Modifier.fillMaxWidth().weight(if (compactWide) .40f else .34f).testTag("home-hero")) {
                        GalleryApprovedHero(state, s, a, tk, hero, Modifier.fillMaxSize(), true) { why = hero.prayer }
                    }
                    Column(Modifier.fillMaxWidth().weight(if (compactWide) .60f else .66f).testTag("home-prayers"), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        galleryGridRows(listedPrayers(s)).forEach { row ->
                            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { p -> ApprovedRow(galleryRowInfo(p, state, s), s, tk, a, Modifier.weight(1f).fillMaxHeight(), true, true) { why = p } }
                                repeat(3-row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                    ApprovedTimeline(state, s, tk, Modifier.fillMaxWidth().height(54.dp))
                } else if (wide) {
                    Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        val list: @Composable RowScope.() -> Unit = {
                            ApprovedList(state, s, tk, a, Modifier.weight(if (celestial) .44f else .46f).fillMaxHeight().padding(vertical = 12.dp)) { why = it }
                        }
                        val focus: @Composable RowScope.() -> Unit = {
                            Column(Modifier.weight(if (celestial) .56f else .54f).fillMaxHeight()) {
                                Box(Modifier.then(if (reflow) Modifier.height(if (gallery) 260.dp else 290.dp) else Modifier.weight(1f)).fillMaxWidth().testTag("home-hero")) {
                                    DarkApprovedHero(state, s, tk, hero, Modifier.fillMaxSize(), true) { why = hero.prayer }
                                }
                                ApprovedTimeline(state, s, tk, Modifier.fillMaxWidth().height(60.dp))
                            }
                        }
                        if (celestial) { focus(); list() } else { list(); focus() }
                    }
                } else {
                    // Six rows retain a minimum touch height. Hero and spare row space share the remaining height.
                    // On 360x800 with system insets this yields a compact hero, rather than shrinking the entire screen.
                    Box(Modifier.then(if (reflow) Modifier.height(if (gallery) 260.dp else 290.dp) else Modifier.weight(1f)).fillMaxWidth().testTag("home-hero")) {
                        if (gallery) GalleryApprovedHero(state, s, a, tk, hero, Modifier.fillMaxSize(), false) { why = hero.prayer }
                        else if (portraitPolish) PortraitPrayerHero(state, s, tk, hero, Modifier.fillMaxSize()) { why = hero.prayer }
                        else DarkApprovedHero(state, s, tk, hero, Modifier.fillMaxSize(), tablet) { why = hero.prayer }
                    }
                    ApprovedTimeline(state, s, tk, Modifier.fillMaxWidth().height(if (tablet) 56.dp else 48.dp))
                    ApprovedList(state, s, tk, a, Modifier.height(listedPrayers(s).size * (if (tablet) 100.dp else if (reflow) 82.dp else if (gallery && L10n.isUrdu(s) && !s.showRelative && !compactGalleryPhone) (64f * androidx.compose.ui.platform.LocalDensity.current.fontScale).dp else if (compactGalleryPhone) 56.dp else 60.dp)).fillMaxWidth()
                        .then(if (gallery) Modifier.testTag("gallery-phone-fit") else Modifier)) { why = it }
                }
                if (!wide && !tablet) ApprovedPhoneDock(state, s, a, links, tk)
                else {
                    ApprovedContext(links, s, tk, wide, Modifier.fillMaxWidth().testTag("gallery-phone-footer"))
                    if (!wide) ApprovedNavigation(s, a, tk)
                }
                if (!phonePortraitLayout()) Text(Str[R.string.s_theme_signature], Modifier.fillMaxWidth().padding(bottom = 2.dp),
                    fontFamily = approvedUi(s), fontSize = 9.sp, color = tk.contentSecondary, textAlign = TextAlign.End, maxLines = 1)
            }
            if (s.nightDim && state.period == Prayer.ISHA && !state.justPassed)
                Box(Modifier.fillMaxSize().background(Color(0xFF100A04).copy(alpha = .22f)))
        }
    }
}

@Composable
private fun ApprovedHeader(state: PrayerState, s: AppSettings, a: HomeActions, tk: ThemeTokens, wide: Boolean, portraitPolish: Boolean = false) {
    val F = approvedUi(s)
    val dates: @Composable () -> Unit = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(L10n.date(s, state.now), fontFamily = F, fontSize = if (wide) 15.sp else 12.sp, color = tk.contentPrimary, maxLines = 1)
            if (s.showHijri) Text(galleryHijriLine(s, PrayerEngine.hijri(state.now.toLocalDate(), s.hijriOffsetDays)),
                fontFamily = if (L10n.isUrdu(s)) Amiri else F, fontSize = if (wide) 15.sp else 12.sp,
                lineHeight = 18.sp, color = tk.contentSecondary, maxLines = 1, modifier = Modifier.testTag("approved-hijri"))
        }
    }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(if (wide) 64.dp else 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp)).clickable(role = Role.Button, onClick = a.onOpenLocation)
                .semantics(mergeDescendants = true) { contentDescription = Str.get(R.string.s_a11y_location, L10n.iso(L10n.place(s.locationName))) }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, Modifier.size(22.dp), tint = tk.primary)
                Text(L10n.place(s.locationName), Modifier.padding(start = 5.dp), fontFamily = F, fontSize = if (wide) 19.sp else 15.sp, color = tk.contentPrimary, maxLines = 1)
            }
            if (wide) { dates(); Spacer(Modifier.weight(1f)) }
            if (s.kidsMode && !portraitPolish) ApprovedNav(Icons.Outlined.MenuBook, Str[R.string.s_learn_salah], tk, a.onOpenLearn, wide)
            // Timetable also remains in the header for discoverability and existing accessibility contracts.
            if (!portraitPolish) ApprovedNav(Icons.Outlined.CalendarMonth, Str[R.string.s_monthly_timetable], tk, a.onOpenTimetable, wide)
            ApprovedNav(Icons.Outlined.Settings, Str[R.string.s_settings], tk, a.onOpenSettings, wide)
        }
        if (!wide) Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { dates() }
    }
}

@Composable
private fun ApprovedNav(icon: ImageVector, label: String, tk: ThemeTokens, onClick: () -> Unit, showLabel: Boolean) {
    Column(Modifier.widthIn(min = 48.dp).heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp))
        .clickable(role = Role.Button, onClick = onClick).semantics(mergeDescendants = true) { contentDescription = label }
        .padding(horizontal = 6.dp, vertical = 3.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, null, Modifier.size(24.dp), tint = tk.contentPrimary)
        if (showLabel) Text(label, fontFamily = if (L10n.uiUrdu) Amiri else ApprovedBody, fontSize = 11.sp, color = tk.contentPrimary, maxLines = 1)
    }
}

@Composable
internal fun KiswahBand(wide: Boolean) {
    val cfg=androidx.compose.ui.platform.LocalConfiguration.current
    if(minOf(cfg.screenWidthDp,cfg.screenHeightDp)<600) {
        KiswahOptionOneBand(wide)
    } else {
        // This approval covers phone banners; retain the established tablet allocation.
        Box(Modifier.fillMaxWidth().padding(vertical = 5.dp).testTag("kiswah-calligraphy-frame").border(1.dp, Kiswah.thread)
            .background(Color.Black.copy(alpha = .65f)), contentAlignment = Alignment.Center) {
            Text(if (wide) Kiswah.BAND else Kiswah.BAND.replaceFirst("   ✦   ", "\n"), Modifier.padding(horizontal = 12.dp, vertical = 12.dp).testTag("kiswah-calligraphy-band").semantics { contentDescription = Kiswah.BAND },
                fontFamily = Amiri, fontSize = if (wide) 28.sp else 15.sp, lineHeight = if (wide) 48.sp else 23.sp,
                color = Kiswah.threadSoft, textAlign = TextAlign.Center, maxLines = if (wide) 1 else Int.MAX_VALUE)
        }
    }
}

@Composable
private fun DarkApprovedHero(state: PrayerState, s: AppSettings, tk: ThemeTokens, hero: HeroInfo, modifier: Modifier, roomy: Boolean, onDetails: () -> Unit) {
    BoxWithConstraints(modifier.testTag("approved-dark-hero").semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() }, contentAlignment = Alignment.Center) {
        val k = ((maxHeight.value - 52f) / 240f).coerceIn(.40f, if (roomy) 1.6f else 1.05f)
        val kiswah = s.theme == AppTheme.KISWAH
        if (s.theme == AppTheme.MIQAAT && s.artTheme != ArtTheme.MINIMAL) {
            Image(painterResource(R.drawable.approved_miqaat_arch), null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            Box(Modifier.fillMaxSize().background(Color(0xFF00152C).copy(alpha = .14f)))
        }
        if (kiswah && s.artTheme != ArtTheme.MINIMAL) Image(painterResource(R.drawable.approved_kiswah_silk), null, Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
        if (s.theme == AppTheme.CELESTIAL_MERIDIAN && s.artTheme != ArtTheme.MINIMAL)
            Box(Modifier.fillMaxSize()) {
                Image(painterResource(R.drawable.celestial_landscape_approved), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Color(0xFF00152C).copy(alpha = .22f)))
                ApprovedSolarArc(state, Modifier.fillMaxSize().padding(12.dp))
            }
        Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(hero.special ?: hero.kickerLabel, fontFamily = approvedUi(s), fontSize = (11*k).sp, lineHeight = (16*k).sp,
                letterSpacing = if (L10n.isUrdu(s)) 0.sp else 2.sp, color = tk.primary, textAlign = TextAlign.Center, maxLines = 2)
            Text(hero.arabic, fontFamily = if (kiswah) ReemKufi else Amiri, fontSize = (52*k).sp, lineHeight = (62*k).sp, color = if (kiswah) Kiswah.highlight else tk.arabicText,
                modifier = Modifier.testTag("approved-hero-arabic"))
            if (!L10n.isUrdu(s)) Text(hero.label.uppercase(), fontFamily = Cinzel, fontSize = (12*k).sp, letterSpacing = 3.sp, color = tk.contentPrimary, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(hero.clock, fontFamily = ApprovedDisplay, fontWeight = FontWeight.Medium, fontSize = (78*k).sp, lineHeight = (76*k).sp, color = tk.contentPrimary, maxLines = 1, modifier = Modifier.testTag("approved-hero-clock"))
                if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = if (L10n.isUrdu(s)) Amiri else ApprovedDisplay, fontSize = (20*k).sp, color = tk.contentPrimary, maxLines = 1)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.Schedule, null, Modifier.size(16.dp), tint = tk.primary)
                Text(hero.status, fontFamily = approvedUi(s), fontSize = (14*k).sp, lineHeight = (19*k).sp, color = tk.contentPrimary, maxLines = 2, textAlign = TextAlign.Center)
            }
            ApprovedDetails(Str[R.string.s_view_prayer_details], tk, onDetails, Modifier.padding(top = 3.dp), false)
            ApprovedSeasonal(state, s, tk)
        }
    }
}

@Composable
private fun ApprovedSolarArc(state: PrayerState, modifier: Modifier) {
    Canvas(modifier.clearAndSetSemantics { }) {
        val gold = Color(0xFFFFD071)
        val w=size.width;val h=size.height;val base=h*.88f;val rise=h*.8f
        fun point(f:Float)=Offset(w*(.05f+.9f*f),base-kotlin.math.sin(f*kotlin.math.PI.toFloat())*rise)
        val path=Path().apply { moveTo(point(0f).x,point(0f).y);for(i in 1..100) { val p=point(i/100f);lineTo(p.x,p.y) } }
        drawPath(path,gold.copy(alpha=.75f),style=Stroke(1.dp.toPx(),pathEffect=PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(),3.dp.toPx()))))
        val sunrise=state.today[Prayer.SUNRISE];val sunset=state.today[Prayer.MAGHRIB]
        val f=(Duration.between(sunrise,state.now).seconds/Duration.between(sunrise,sunset).seconds.coerceAtLeast(1).toFloat()).coerceIn(0f,1f)
        val p=point(f)
        drawCircle(Brush.radialGradient(listOf(gold.copy(alpha=.6f),Color.Transparent),p,18.dp.toPx()),18.dp.toPx(),p)
        drawCircle(gold,5.dp.toPx(),p)
    }
}

@Composable
internal fun ApprovedSeasonal(state: PrayerState, s: AppSettings, tk: ThemeTokens) {
    fastProgress(state, s)?.let { (progress, label) ->
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(.7f).height(3.dp), color = tk.primary, trackColor = tk.divider)
        Text(label, fontFamily = approvedUi(s), fontSize = 12.sp, color = tk.contentPrimary)
    }
    tarawihLine(state, s)?.let { Text(it, fontFamily = approvedUi(s), fontSize = 12.sp, color = tk.contentPrimary, maxLines = 2) }
}

@Composable
private fun ApprovedDetails(label: String, tk: ThemeTokens, onClick: () -> Unit, modifier: Modifier = Modifier, filled: Boolean = false) {
    Row(modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(24.dp)).background(if (filled) tk.accent else tk.background.copy(alpha = .72f))
        .border(1.dp, if (tk.dark) tk.primary else tk.accent.copy(alpha = .6f), RoundedCornerShape(24.dp))
        .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Text(label, fontFamily = if (L10n.uiUrdu) Amiri else ApprovedBody, fontSize = 13.sp, color = if (filled) tk.background else tk.contentPrimary, maxLines = 2)
        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.padding(start = 6.dp).size(17.dp), tint = if (filled) tk.background else tk.primary)
    }
}

/** Atlas contains only paintings: hero in top half; six equal period illustrations in bottom half. */
@Composable
internal fun ApprovedGalleryArt(index: Int?, modifier: Modifier) {
    val bmp = ImageBitmap.imageResource(R.drawable.approved_gallery_atlas)
    Canvas(modifier.clearAndSetSemantics { }) {
        val sw = if (index == null) bmp.width else bmp.width/6
        val heroH = (bmp.height * .45f).toInt()
        val sh = if (index == null) heroH else bmp.height-heroH
        val x = if (index == null) 0 else index*sw
        val y = if (index == null) 0 else heroH
        val cropW = minOf(sw, (sh*size.width/size.height).toInt()).coerceAtLeast(1)
        val cropH = minOf(sh, (sw*size.height/size.width).toInt()).coerceAtLeast(1)
        drawImage(bmp, srcOffset = IntOffset(x+(sw-cropW)/2,y+(sh-cropH)/2), srcSize = IntSize(cropW,cropH), dstSize = IntSize(size.width.toInt(),size.height.toInt()))
    }
}

@Composable
private fun GalleryApprovedHero(state: PrayerState, s: AppSettings, a: HomeActions, tk: ThemeTokens, hero: HeroInfo, modifier: Modifier, wide: Boolean, onDetails: () -> Unit) {
    BoxWithConstraints(modifier.testTag("gallery-mobile-hero").semantics(mergeDescendants = true) { contentDescription = hero.spoken; heading() }) {
        val compactHero = wide && maxHeight < 190.dp
        val compactPhoneHero = !wide && maxHeight < 170.dp
        Column(Modifier.fillMaxSize()) {
            if (!wide && s.artTheme != ArtTheme.MINIMAL)
                ApprovedGalleryArt(null, Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)))
            Row(Modifier.fillMaxWidth().then(if (wide) Modifier.weight(1f) else Modifier).padding(vertical = if (compactPhoneHero) 0.dp else 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(hero.special ?: hero.kickerLabel, fontFamily = approvedUi(s), fontSize = if (compactPhoneHero) 10.sp else 11.sp, color = tk.contentSecondary, maxLines = 1)
                    Text(if (L10n.isUrdu(s)) hero.label else hero.arabic, fontFamily = Amiri, fontSize = if (wide) (if (compactHero) 38.sp else 54.sp) else if (compactPhoneHero) 26.sp else 32.sp,
                        lineHeight = if (wide) (if (compactHero) 50.sp else 70.sp) else if (compactPhoneHero) 32.sp else 40.sp, color = tk.accent, maxLines = 1, modifier = Modifier.testTag("gallery-mobile-prayer-name"))
                    if (!L10n.isUrdu(s)) Text(hero.label.uppercase(), fontFamily = Cinzel, fontSize = 11.sp, letterSpacing = 2.sp, color = tk.accent, maxLines = 1)
                }
                if (wide && s.artTheme != ArtTheme.MINIMAL) ApprovedGalleryArt(null, Modifier.weight(1.5f).fillMaxHeight())
                Column(Modifier.weight(1.1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(hero.clock, fontFamily = ApprovedDisplay, fontWeight = FontWeight.Medium, fontSize = if (wide) (if (compactHero) 52.sp else 76.sp) else if (compactPhoneHero) 38.sp else 49.sp,
                            lineHeight = if (wide) (if (compactHero) 56.sp else 80.sp) else if (compactPhoneHero) 40.sp else 50.sp, color = tk.accent, maxLines = 1, modifier = Modifier.testTag("gallery-mobile-prayer-time"))
                        if (hero.suffix.isNotEmpty()) Text(" ${hero.suffix}", fontFamily = if (L10n.isUrdu(s)) Amiri else ApprovedDisplay, fontSize = if (wide) 23.sp else 14.sp, color = tk.contentPrimary, maxLines = 1)
                    }
                    Text(hero.status, fontFamily = approvedUi(s), fontSize = if (compactPhoneHero) 11.sp else 12.sp, lineHeight = 16.sp, color = tk.contentPrimary, maxLines = 1)
                    if (wide && !compactHero) {
                        ApprovedDetails(Str[R.string.s_view_prayer_details], tk, onDetails, Modifier.fillMaxWidth(.9f), true)
                        if (s.kidsMode) ApprovedDetails(if (L10n.isUrdu(s)) "نماز کی تیاری" else "Prepare for ${hero.label}", tk,
                            { a.onPreparePrayer(hero.prayer) }, Modifier.fillMaxWidth(.9f), false)
                    }
                }
            }
            if (!wide || compactHero) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                if (phonePortraitLayout()) {
                    ApprovedDetails(Str[R.string.s_view_prayer_details], tk, onDetails, Modifier.weight(1.2f), true)
                    if (s.kidsMode) Row(Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(24.dp))
                        .clickable(role = Role.Button, onClick = { a.onPreparePrayer(hero.prayer) })
                        .padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Text(if (L10n.isUrdu(s)) "نماز کی تیاری" else "Prepare for ${hero.label}",
                            fontFamily = approvedUi(s), fontSize = 12.sp, color = tk.accent, maxLines = 2)
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.padding(start = 4.dp).size(16.dp), tint = tk.accent)
                    }
                } else {
                    ApprovedDetails(Str[R.string.s_view_prayer_details], tk, onDetails, Modifier.weight(1f), wide)
                    if (s.kidsMode) ApprovedDetails(if (L10n.isUrdu(s)) "نماز کی تیاری" else "Prepare for ${hero.label}", tk, { a.onPreparePrayer(hero.prayer) }, Modifier.weight(1f), !wide)
                }
            }
            ApprovedSeasonal(state, s, tk)
        }
    }
}

@Composable
private fun ApprovedList(state: PrayerState, s: AppSettings, tk: ThemeTokens, a: HomeActions, modifier: Modifier, onWhy: (Prayer) -> Unit) {
    val polished = phonePortraitLayout()
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(if (polished && tk.dark) portraitPrayerSurface(s) else if (tk.dark) Color(0xFF00162D).copy(alpha = if (s.theme == AppTheme.KISWAH) .12f else .82f) else tk.background.copy(alpha = .8f))
        .border(if (polished) .5.dp else 1.dp, if (polished) tk.outline.copy(alpha = .45f) else tk.outline, RoundedCornerShape(12.dp)).testTag("home-prayers")) {
        listedPrayers(s).forEach { p ->
            Box(Modifier.weight(1f).fillMaxWidth().testTag("gallery-fit-row-${p.name}")) {
                ApprovedRow(galleryRowInfo(p, state, s), s, tk, a, Modifier.fillMaxSize(), s.theme == AppTheme.PRAYER_GALLERY, false) { onWhy(p) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApprovedRow(r: RowInfo, s: AppSettings, tk: ThemeTokens, a: HomeActions, modifier: Modifier, illustrated: Boolean, tile: Boolean, onWhy: () -> Unit) {
    val now = r.isNow
    val next = r.isNext && !now
    val selected = now || next
    val polished = phonePortraitLayout()
    val marker = if (polished) tk.primary else if (now) (if (tk.dark) Color(0xFF45DAE3) else Color(0xFF087C77)) else if (tk.dark) Color(0xFFFFCC63) else tk.accent
    val fill = if (polished) (if (now) tk.primary.copy(alpha = .20f) else if (next) tk.primary.copy(alpha = .13f) else Color.Transparent) else if (now) (if (tk.dark) Color(0xFF00414C) else Color(0xFFEAF7F1)) else if (next) (if (tk.dark) Color(0xFF4B381C) else Color(0xFFFFEBDD)) else Color.Transparent
    val urdu = L10n.isUrdu(s)
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val large = configuration.screenWidthDp >= 600
    val compactTablet = large && configuration.screenHeightDp < 700
    val smallPhone = configuration.screenWidthDp < 380
    val textStyle = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
    Column(modifier.then(if (polished) Modifier.padding(horizontal = 4.dp) else Modifier)
        .clip(RoundedCornerShape(if (tile) 12.dp else if (polished) 8.dp else 0.dp))
        .then(if (polished && selected) Modifier.background(Brush.horizontalGradient(listOf(
            tk.primary.copy(alpha = if (now) .16f else .10f), tk.primary.copy(alpha = if (now) .08f else .04f)))) else Modifier.background(if (polished) Color.Transparent else fill))
        .then(if (polished) Modifier.drawBehind {
            if (r.prayer != Prayer.FAJR && !selected) drawLine(tk.divider.copy(alpha = .45f),
                Offset(10.dp.toPx(), 0f), Offset(size.width - 10.dp.toPx(), 0f), .5.dp.toPx())
            if (selected) {
                drawRoundRect(tk.primary.copy(alpha = .30f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()), style = Stroke(.5.dp.toPx()))
            }
        } else Modifier.border(if (selected) 1.dp else .5.dp, if (selected) marker.copy(alpha = .75f) else tk.divider, RoundedCornerShape(if (tile) 12.dp else 0.dp)))
        .combinedClickable(onClick = a.onToggleRelative, onLongClick = onWhy, role = Role.Button)
        .semantics(mergeDescendants = true) { contentDescription = r.spoken }
        .testTag("approved-row-${r.prayer.name}")) {
        if (tile && s.artTheme != ArtTheme.MINIMAL) ApprovedGalleryArt(r.prayer.ordinal, Modifier.fillMaxWidth().weight(if (compactTablet) .36f else .44f))
        Row(Modifier.fillMaxWidth().then(if (tile) Modifier.weight(if (compactTablet) .64f else .56f) else Modifier.fillMaxHeight()), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(if (polished) (if (illustrated) 4.dp else 8.dp) else if (illustrated) 3.dp else 18.dp).fillMaxHeight(), contentAlignment = Alignment.Center) {
                if (polished) Box(Modifier.height(26.dp).width(2.dp).background(if (selected) tk.primary.copy(alpha = if (now) 1f else .8f) else Color.Transparent, RoundedCornerShape(1.dp)))
                else if (illustrated) Box(Modifier.fillMaxHeight().width(3.dp).background(if (selected) marker else Color.Transparent))
                else Canvas(Modifier.fillMaxSize()) {
                    val x = size.width/2; val y = size.height/2
                    drawLine(tk.primary.copy(alpha = .55f), Offset(x,0f), Offset(x,size.height), 1.dp.toPx())
                    rotate(45f, Offset(x,y)) {
                        drawRect(if (r.done || selected) tk.primary else tk.background, Offset(x-3.dp.toPx(),y-3.dp.toPx()), androidx.compose.ui.geometry.Size(6.dp.toPx(),6.dp.toPx()))
                        drawRect(tk.primary, Offset(x-3.dp.toPx(),y-3.dp.toPx()), androidx.compose.ui.geometry.Size(6.dp.toPx(),6.dp.toPx()), style = Stroke(1.dp.toPx()))
                    }
                }
            }
            if (illustrated && !tile && s.artTheme != ArtTheme.MINIMAL) ApprovedGalleryArt(r.prayer.ordinal, Modifier.padding(horizontal = 5.dp, vertical = 3.dp).width(if (smallPhone) 40.dp else if (polished) 56.dp else 64.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp)))
            // Measure the phone name column before status badges and bell slots. Clock origins
            // therefore stay identical for all six rows, including Sunrise and NOW/NEXT.
            Column(Modifier.then(if (!large && !tile) Modifier.fillMaxWidth(if (illustrated) (if (polished && !urdu) .24f else .285f) else .34f)
                else Modifier.weight(if (tile) 1.1f else 1f)).padding(start = if (illustrated) 5.dp else 8.dp)) {
                Text(if (urdu) r.label else r.label.uppercase(), Modifier.heightIn(min = if (large) 32.dp else 20.dp).testTag("gallery-fit-label-${r.prayer.name}"), fontFamily = if (urdu) Amiri else Cinzel,
                    fontSize = if (urdu) (if (large) (if (compactTablet) 22.sp else 26.sp) else 18.sp) else if (large) (if (compactTablet) 17.sp else 20.sp) else 12.sp, lineHeight = if (large) 32.sp else if (urdu) 25.sp else 20.sp,
                    fontWeight = FontWeight.Medium, color = tk.contentPrimary, maxLines = 1, style = textStyle)
                if (polished && illustrated && urdu && selected) Box(Modifier.testTag("gallery-fit-marker-${r.prayer.name}")) {
                    Text(L10n.word(s, if (now) "NOW" else "NEXT"),
                        Modifier.testTag(if (now) "gallery-status-now" else "gallery-status-next")
                            .clip(RoundedCornerShape(6.dp)).background(tk.accent).padding(horizontal = 5.dp, vertical = 1.dp),
                        fontFamily = Amiri, fontSize = 11.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold,
                        color = tk.background, maxLines = 1, style = textStyle)
                }
                if (!urdu) Text(r.arabic, Modifier.testTag("gallery-fit-arabic-${r.prayer.name}"), fontFamily = Amiri, fontSize = if (large) (if (compactTablet) 22.sp else 26.sp) else 17.sp,
                    lineHeight = if (large) 34.sp else 24.sp, color = if (polished && selected) tk.primary else if (tk.dark) tk.arabicText else tk.accent, maxLines = 1, style = textStyle)
            }
            Column(Modifier.weight(if (tile) 1.2f else 1.15f).padding(start = 8.dp), horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (s.showRelative) r.relative else r.clock, Modifier.testTag("gallery-fit-clock-${r.prayer.name}"), fontFamily = if (s.showRelative) approvedUi(s) else ApprovedDisplay,
                        fontSize = if (s.showRelative) (if (large) 20.sp else 14.sp) else if (large) (if (compactTablet) 30.sp else 38.sp) else if (polished && illustrated && urdu && smallPhone) 21.sp else 25.sp, lineHeight = if (s.showRelative) (if (large) (if (compactTablet) 22.sp else 26.sp) else 18.sp) else if (large) (if (compactTablet) 36.sp else 46.sp) else if (polished && illustrated && urdu && smallPhone) 30.sp else 32.sp,
                        fontWeight = if (polished && selected && s.showRelative) FontWeight.SemiBold else FontWeight.Medium, color = tk.contentPrimary, maxLines = if (s.showRelative) 2 else 1, style = if (polished && !s.showRelative) textStyle.copy(fontFeatureSettings = "'lnum' 1, 'tnum' 1") else textStyle)
                    if (!s.showRelative && r.suffix.isNotEmpty()) Text(" ${r.suffix}", fontFamily = if (urdu) Amiri else ApprovedDisplay, fontSize = if (urdu) 11.sp else 12.sp, color = tk.contentPrimary, maxLines = 1)
                }
                if (r.small.isNotEmpty()) Text(if (urdu) r.small.replace(" بجے", "") else r.small, Modifier.testTag("gallery-fit-detail-${r.prayer.name}"), fontFamily = if (urdu) Amiri else ApprovedBody,
                    fontSize = if (large) (if (compactTablet) 12.sp else 15.sp) else if (polished && tk.dark && !urdu && !s.iqamahEnabled) 12.sp else 11.sp, lineHeight = if (large) (if (compactTablet) 18.sp else 22.sp) else if (urdu) 18.sp else if (polished && tk.dark && !s.iqamahEnabled) 16.sp else 15.sp, color = tk.contentSecondary, maxLines = 2, style = textStyle)
            }
            if (selected && !(polished && illustrated && urdu)) Box(Modifier.testTag("gallery-fit-marker-${r.prayer.name}")) {
                if (s.theme == AppTheme.PRAYER_GALLERY) GalleryStatusBadge(now, s, compact = !large)
                else Text(L10n.word(s, if (now) "NOW" else "NEXT"), Modifier.testTag(if (now) "home-status-now" else "home-status-next").clip(RoundedCornerShape(9.dp)).background(if (polished && next) Color.Transparent else marker)
                    .then(if (polished && next) Modifier.border(.8.dp, tk.primary, RoundedCornerShape(9.dp)) else Modifier).padding(horizontal = 6.dp, vertical = 3.dp), fontFamily = if (urdu) Amiri else ApprovedBody,
                    fontSize = 10.sp, lineHeight = if (urdu) 20.sp else 14.sp, fontWeight = FontWeight.Bold, color = if (polished && next) tk.primary else Color(0xFF032126), maxLines = 1)
            }
            // Bell state is informational; notification settings remain in the header and alert action.
            if (r.prayer.isPrayer) Box(Modifier.width(28.dp).fillMaxHeight().semantics { contentDescription = if (r.azaanOn) L10n.t("Azaan enabled", "اذان فعال ہے") else L10n.t("Azaan off", "اذان بند ہے") }, contentAlignment = Alignment.Center) {
                Icon(if (r.azaanOn) Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff, null, Modifier.size(17.dp), tint = if (tk.dark) tk.primary else tk.accent)
            }
            Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onWhy)
                .testTag("approved-info-${r.prayer.name}").semantics { contentDescription = Str[R.string.s_why_this_time] }, contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Info, null, Modifier.size(19.dp), tint = tk.contentSecondary)
            }
        }
    }
}

@Composable
internal fun ApprovedTimeline(state: PrayerState, s: AppSettings, tk: ThemeTokens, modifier: Modifier, landscape: Boolean = false, compact: Boolean = false) {
    val quietPortrait = phonePortraitLayout()
    val prayers = listedPrayers(s)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val gold = if (tk.dark) Color(0xFFFFCC63) else tk.accent
    val active = if (state.heroTime.toLocalDate() == state.today.date) state.hero else null
    var explainDay by remember { mutableStateOf(false) }
    if (explainDay) AlertDialog(onDismissRequest = { explainDay = false }, confirmButton = { TextButton({ explainDay = false }) { Text(L10n.t("Close", "بند کریں")) } }, text = { Column {
        Text(Str[R.string.s_day_line])
        if (s.showDisliked) PrayerEngine.dislikedWindows(state.today, s).forEach { w -> Text(L10n.avoidWindow(w.start,w.end,s.use24h)) }
    } })
    Column(modifier.testTag("approved-day-timeline").clickable(role = Role.Button) { explainDay = true }.padding(horizontal = 5.dp), verticalArrangement = Arrangement.Center) {
        Canvas(Modifier.fillMaxWidth().height(17.dp).semantics { contentDescription = Str[R.string.s_day_line] }) {
            val step = size.width/prayers.size
            fun x(i: Int) = (if (rtl) prayers.lastIndex-i+.5f else i+.5f)*step
            val y = size.height/2
            drawLine(gold.copy(alpha = .65f), Offset(x(0),y), Offset(x(prayers.lastIndex),y), 1.dp.toPx())
            prayers.forEachIndexed { i,p ->
                val point = Offset(x(i),y); val sel = p == active
                if (sel) drawCircle(Brush.radialGradient(listOf(gold.copy(alpha = if (quietPortrait) .30f else .65f),Color.Transparent),point,(if (quietPortrait) 10.dp else 13.dp).toPx()),(if (quietPortrait) 10.dp else 13.dp).toPx(),point)
                rotate(45f, point) { drawRect(gold, Offset(point.x-3.dp.toPx(),point.y-3.dp.toPx()), androidx.compose.ui.geometry.Size(6.dp.toPx(),6.dp.toPx())) }
                if (sel) drawCircle(gold, (if (quietPortrait) 6.dp else 8.dp).toPx(), point, style = Stroke(1.dp.toPx()))
            }
            if (s.showDisliked) {
                fun at(t: java.time.ZonedDateTime): Float {
                    val i = prayers.indexOfLast { !state.today[it].isAfter(t) }
                    if (i < 0) return x(0)
                    if (i >= prayers.lastIndex) return x(prayers.lastIndex)
                    val whole = Duration.between(state.today[prayers[i]],state.today[prayers[i+1]]).seconds.coerceAtLeast(1)
                    val fraction = (Duration.between(state.today[prayers[i]],t).seconds/whole.toFloat()).coerceIn(0f,1f)
                    return x(i)+(x(i+1)-x(i))*fraction
                }
                PrayerEngine.dislikedWindows(state.today,s).forEach { w ->
                    drawLine(if (tk.dark) Color(0xFFF3A781) else tk.accent, Offset(at(w.start),y),Offset(at(w.end),y),2.dp.toPx(),pathEffect=PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(),2.dp.toPx())))
                }
            }
            // The moving bead uses actual elapsed time, independent of equally spaced label slots.
            val times = prayers.map { state.today[it] }
            val idx = times.indexOfLast { !it.isAfter(state.now) }
            if (idx >= 0 && idx < times.lastIndex) {
                val duration = Duration.between(times[idx], times[idx+1]).seconds.coerceAtLeast(1)
                val f = (Duration.between(times[idx],state.now).seconds/duration.toFloat()).coerceIn(0f,1f)
                val nx = x(idx)+(x(idx+1)-x(idx))*f
                drawCircle(tk.contentPrimary, 2.dp.toPx(), Offset(nx,y-5.dp.toPx()))
            }
        }
        Row(Modifier.fillMaxWidth()) { prayers.forEach { p ->
            Text(L10n.prayer(s,p), Modifier.weight(1f).testTag("approved-timeline-${p.name}"), fontFamily = if (L10n.isUrdu(s)) Amiri else if (landscape) androidx.compose.ui.text.font.FontFamily.Serif else ApprovedBody,
                fontSize = if (compact && !L10n.isUrdu(s)) 10.sp else if (landscape && !L10n.isUrdu(s)) 13.sp else 11.sp, lineHeight = 18.sp, textAlign = TextAlign.Center, color = if (p == active) gold else tk.contentPrimary, maxLines = 1)
        } }
    }
}

@Composable
private fun ApprovedContext(links: List<Door>, s: AppSettings, tk: ThemeTokens, wide: Boolean, modifier: Modifier) {
    val visible = if (wide) (links.filter { it.warn }.take(1) + links.filter { it.kind == DoorKind.ADHKAR }.take(1) + links.filter { !it.warn && it.kind != DoorKind.ADHKAR }.take(1)) else galleryVisibleDoors(links)
    val extra = links.filter { it !in visible }
    var more by remember { mutableStateOf(false) }
    Row(modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        visible.forEach { d ->
            Row(Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(24.dp))
                .background(tk.background.copy(alpha = .82f)).border(1.dp, if (d.warn) tk.warning else tk.outline, RoundedCornerShape(24.dp))
                .then(if (d.onClick != null) Modifier.clickable(role = Role.Button, onClick = d.onClick) else Modifier)
                .semantics(mergeDescendants = true) { contentDescription = d.label }.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (d.warn) Icons.Outlined.WarningAmber else if (d.kind == DoorKind.ADHKAR) Icons.Outlined.MenuBook else Icons.Outlined.Explore, null, Modifier.size(18.dp), tint = if (d.warn) tk.warning else tk.primary)
                Text(d.label, Modifier.weight(1f).padding(start = 5.dp), fontFamily = if (L10n.isUrdu(s)) Amiri else ApprovedBody, fontSize = if (wide) 15.sp else 12.sp, lineHeight = 17.sp, color = tk.contentPrimary, maxLines = 2)
            }
        }
        if (extra.isNotEmpty()) Box {
            IconButton({ more = true }, Modifier.size(48.dp)) { Icon(Icons.Outlined.MoreHoriz, L10n.word(s,"More actions"), tint = tk.contentPrimary) }
            DropdownMenu(more, { more = false }) { extra.forEach { d -> DropdownMenuItem(text = { Text(d.label) }, onClick = { more = false; d.onClick?.invoke() }) } }
        }
    }
}

@Composable
private fun ApprovedNavigation(s: AppSettings, a: HomeActions, tk: ThemeTokens) {
    Row(Modifier.fillMaxWidth().height(54.dp).border(.5.dp, tk.outline, RoundedCornerShape(16.dp)).background(tk.background.copy(alpha = .84f), RoundedCornerShape(16.dp)), horizontalArrangement = Arrangement.SpaceEvenly) {
        if (s.showQibla) Box(Modifier.weight(1f)) { ApprovedNav(Icons.Outlined.Explore, L10n.word(s,"Qibla"), tk, a.onOpenQibla, true) }
        // The general Adhkar library remains accessible even outside a prompted time window.
        Box(Modifier.weight(1f)) { ApprovedNav(Icons.Outlined.AutoStories, L10n.t("Adhkar", "اذکار"), tk, { a.onOpenAdhkar(AdhkarMode.MORNING) }, true) }
        if (s.kidsMode) Box(Modifier.weight(1f)) { ApprovedNav(Icons.Outlined.MenuBook, L10n.t("Learn", "سیکھیں"), tk, a.onOpenLearn, true) }
        Box(Modifier.weight(1f)) { ApprovedNav(Icons.Outlined.CalendarMonth, L10n.t("Timetable", "اوقات"), tk, a.onOpenTimetable, true) }
    }
}
