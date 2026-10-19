package com.usman.miqaat.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

internal data class QuietPrayerTimes(val current: ZonedDateTime, val next: Prayer, val nextTime: ZonedDateTime)

/** The quiet phase identifies the prayer; never infer it from Home's next-prayer hero. */
internal fun quietPrayerTimes(s: AppSettings, prayer: Prayer, now: ZonedDateTime): QuietPrayerTimes {
    val local = now.withZoneSameInstant(s.zone())
    val today = PrayerEngine.times(s, local.toLocalDate())
    val current = if (!today[prayer].isAfter(local)) today[prayer]
        else PrayerEngine.times(s, local.toLocalDate().minusDays(1))[prayer]
    val upcoming = PrayerEngine.state(s, local)
    return QuietPrayerTimes(current, upcoming.next, upcoming.nextTime)
}

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private val QuietBody = FontFamily(Font(R.font.nunito_sans, FontWeight.Normal,
    variationSettings = FontVariation.Settings(FontVariation.weight(500))))
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private val QuietDisplay = FontFamily(Font(R.font.cormorant_garamond_medium, FontWeight.Medium,
    variationSettings = FontVariation.Settings(FontVariation.weight(500))))

private data class QuietPalette(val background: Color, val ink: Color, val accent: Color, val secondary: Color, val panel: Color)
private fun quietPalette(theme: AppTheme) = if (theme == AppTheme.PRAYER_GALLERY)
    QuietPalette(Color(0xFFF6EFE2), Color(0xFF103E37), Color(0xFF9E4027), Color(0xFF3F584E), Color(0xFFF9F2E6).copy(alpha = .88f))
else QuietPalette(if (theme == AppTheme.KISWAH) Color(0xFF080909) else Color(0xFF03152D),
    Color(0xFFF5ECD8), Color(0xFFE7C77E), Color(0xFFD8CFBD),
    (if (theme == AppTheme.KISWAH) Color(0xFF10100F) else Color(0xFF061C35)).copy(alpha = .65f))

@Composable
internal fun QuietPrayerScreen(p: Phase.Quiet, onStop: () -> Unit) {
    val s = MiqaatApp.instance.settings.value
    var now by remember(s.zoneId) { mutableStateOf(ZonedDateTime.now(s.zone())) }
    LaunchedEffect(s.zoneId) { while (true) { now = ZonedDateTime.now(s.zone()); delay(15_000) } }
    QuietPrayerContent(p, s, now, onStop)
}

/** Native information and actions over text-free theme art. The phase remains the source of the current prayer. */
@Composable
internal fun QuietPrayerContent(p: Phase.Quiet, s: AppSettings, now: ZonedDateTime, onStop: () -> Unit) {
    val times = remember(s, p.prayer, now.toLocalDate(), now.hour, now.minute) { quietPrayerTimes(s, p.prayer, now) }
    val colors = quietPalette(s.theme)
    val urdu = L10n.isUrdu(s)
    val accessible = LocalDensity.current.fontScale > 1.15f
    val body = if (urdu) Amiri else QuietBody
    val remaining = ((p.endsAt - now.toInstant().toEpochMilli()).coerceAtLeast(0) + 59_999) / 60_000
    BoxWithConstraints(Modifier.fillMaxSize().background(colors.background).testTag("quiet-board")
        .clickable(role = Role.Button, onClickLabel = Str[R.string.s_quiet_exit], onClick = onStop)) {
        val landscapeArt = maxWidth > maxHeight
        QuietThemeBackdrop(s, landscapeArt, Modifier.matchParentSize())
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = if (landscapeArt) 28.dp else 20.dp, vertical = 12.dp)) {
            val short = maxHeight < 480.dp
            val wide = maxWidth > maxHeight && !accessible
            val tablet = maxWidth >= 600.dp
            val compact = maxHeight < 780.dp
            val headingSize = if (short) 32f else if (wide && compact) 44f else if (tablet) 72f else if (compact) 48f else 62f
            val clockSize = if (short) 40f else if (wide && compact) 54f else if (tablet) 78f else if (compact) 52f else 64f
            val header: @Composable () -> Unit = {
                val brand: @Composable (Modifier) -> Unit = { m ->
                    Text("Miqaat", m.testTag("quiet-brand"), fontFamily = QuietDisplay, fontWeight = FontWeight.Medium,
                        fontSize = if (short) 24.sp else 28.sp, lineHeight = 34.sp, color = colors.ink)
                }
                val mode: @Composable (Modifier) -> Unit = { m ->
                    QuietLabel(Str[R.string.s_quiet_mode_title], urdu, colors.accent, m, if (short) 10f else 11f)
                }
                val clock: @Composable (Modifier) -> Unit = { m ->
                    Column(m, horizontalAlignment = Alignment.End) {
                        Text(L10n.word(s, "NOW"), fontFamily = body, fontSize = 9.sp, lineHeight = if (urdu) 17.sp else 12.sp, color = colors.secondary)
                        Text(L10n.iso(L10n.at(now, s.use24h)), Modifier.testTag("quiet-live-clock"),
                            fontFamily = if (urdu) Amiri else QuietDisplay, fontSize = 14.sp, lineHeight = if (urdu) 26.sp else 21.sp,
                            color = colors.ink, textAlign = TextAlign.End)
                    }
                }
                if (accessible) Column(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    brand(Modifier); mode(Modifier.fillMaxWidth()); clock(Modifier.fillMaxWidth())
                } else Box(Modifier.fillMaxWidth().heightIn(min = if (short) 44.dp else 56.dp)) {
                    brand(Modifier.fillMaxWidth(.25f).align(Alignment.CenterStart))
                    mode(Modifier.fillMaxWidth(.40f).align(Alignment.Center))
                    clock(Modifier.fillMaxWidth(.28f).align(Alignment.CenterEnd))
                }
            }
            val hero: @Composable (Modifier) -> Unit = { m ->
                Column(m.testTag("quiet-current-panel").then(if (!accessible) Modifier.fillMaxHeight() else Modifier)
                    .padding(horizontal = 8.dp, vertical = if (short) 2.dp else 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    val status: @Composable () -> Unit = {
                        QuietLabel(Str[R.string.s_quiet_prayer_progress], urdu, colors.accent,
                            Modifier.fillMaxWidth().testTag("quiet-progress"), if (short) 10f else 12f)
                    }
                    if (wide) { status(); Spacer(Modifier.height(if (short) 2.dp else 8.dp)) }
                    Text(p.prayer.arabic, Modifier.fillMaxWidth().testTag("quiet-current-arabic").semantics { heading() },
                        fontFamily = if (s.theme == AppTheme.KISWAH) ReemKufi else Amiri, fontSize = headingSize.sp,
                        lineHeight = (headingSize * 1.65f).sp, color = colors.accent, textAlign = TextAlign.Center)
                    Text(if (urdu) L10n.prayer(s, p.prayer) else L10n.prayer(s, p.prayer).uppercase(java.util.Locale.ROOT),
                        Modifier.fillMaxWidth().testTag("quiet-current-name"), fontFamily = if (urdu) Amiri else Cinzel,
                        fontSize = (if (short) 12 else if (tablet) 20 else 16).sp, lineHeight = (if (urdu) 30 else if (short) 18 else 26).sp,
                        letterSpacing = if (urdu) 0.sp else 2.sp, color = colors.ink, textAlign = TextAlign.Center)
                    if (!wide) { Spacer(Modifier.height(if (compact) 8.dp else 18.dp)); status() }
                    Spacer(Modifier.height(if (short) 4.dp else if (compact) 12.dp else 22.dp))
                    QuietLabel(Str[R.string.s_quiet_current_azaan], urdu, colors.accent, Modifier.testTag("quiet-current-label"), if (short) 10f else 12f)
                    QuietTime(times.current, s, if (urdu) (if (short) 22f else if (tablet) 42f else 32f) else clockSize,
                        colors.ink, Modifier.fillMaxWidth().testTag("quiet-current-time"))
                    Text(Str[R.string.s_quiet_iqamah_complete], Modifier.fillMaxWidth().testTag("quiet-iqamah-complete"),
                        fontFamily = body, fontSize = if (short) 11.sp else 14.sp, lineHeight = if (urdu) 28.sp else 21.sp,
                        color = colors.secondary, textAlign = TextAlign.Center)
                }
            }
            val next: @Composable (Modifier) -> Unit = { m ->
                Box(m.testTag("quiet-next-panel"), contentAlignment = Alignment.Center) {
                    Column(Modifier.fillMaxWidth(if (wide) .86f else 1f)
                        .clip(RoundedCornerShape(16.dp)).background(colors.panel)
                        .border(1.dp, colors.accent.copy(alpha = .28f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = if (short) 8.dp else if (wide) 24.dp else 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        QuietLabel(Str[R.string.s_quiet_next_azaan], urdu, colors.secondary, Modifier.testTag("quiet-next-label"), if (short) 10f else 11f)
                        Text(if (urdu) L10n.prayer(s, times.next) else L10n.prayer(s, times.next).uppercase(java.util.Locale.ROOT),
                            Modifier.fillMaxWidth().testTag("quiet-next-name"), fontFamily = if (urdu) Amiri else Cinzel,
                            fontSize = (if (urdu) 24 else if (wide) 24 else 19).sp, lineHeight = if (urdu) 42.sp else 32.sp,
                            color = colors.ink, textAlign = TextAlign.Center)
                        QuietTime(times.nextTime, s, if (urdu) (if (short) 22f else 28f) else if (wide) (if (short) 36f else 52f) else 36f,
                            colors.ink, Modifier.fillMaxWidth().testTag("quiet-next-time"))
                        if (times.nextTime.toLocalDate().isAfter(now.withZoneSameInstant(s.zone()).toLocalDate()))
                            Text(Str[R.string.s_quiet_tomorrow], Modifier.fillMaxWidth().testTag("quiet-tomorrow"), fontFamily = body,
                                fontSize = 12.sp, lineHeight = if (urdu) 24.sp else 18.sp, color = colors.secondary, textAlign = TextAlign.Center)
                    }
                }
            }
            val footer: @Composable () -> Unit = {
                Column(Modifier.fillMaxWidth().padding(top = if (short) 4.dp else 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Str.get(R.string.s_quiet_ends_hint, remaining.toInt()), Modifier.fillMaxWidth().testTag("quiet-expiry-hint"),
                        fontFamily = body, fontSize = if (short) 10.sp else 11.sp, lineHeight = if (urdu) 24.sp else 17.sp,
                        color = colors.secondary, textAlign = TextAlign.Center)
                    Row(Modifier.padding(top = 5.dp).widthIn(min = 180.dp, max = 420.dp).heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(28.dp)).background(colors.panel).border(1.dp, colors.accent.copy(alpha = .75f), RoundedCornerShape(28.dp))
                        .testTag("quiet-return").clickable(role = Role.Button, onClick = onStop).padding(horizontal = 24.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, Modifier.size(18.dp), tint = colors.accent)
                        Text(Str[R.string.s_quiet_exit], Modifier.padding(start = 10.dp).testTag("quiet-exit-label"), fontFamily = body,
                            fontSize = 14.sp, lineHeight = if (urdu) 28.sp else 22.sp, color = colors.ink, textAlign = TextAlign.Center)
                    }
                }
            }
            Column(Modifier.fillMaxSize().then(if (accessible) Modifier.verticalScroll(rememberScrollState()).testTag("quiet-accessible") else Modifier)) {
                header()
                if (s.theme == AppTheme.KISWAH) KiswahBand(wide && !short)
                if (accessible) {
                    hero(Modifier.fillMaxWidth()); next(Modifier.fillMaxWidth()); footer()
                } else {
                    if (wide) Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        hero(Modifier.weight(.60f))
                        Box(Modifier.width(1.dp).fillMaxHeight(.65f).background(colors.accent.copy(alpha = .30f)))
                        next(Modifier.weight(.40f))
                    } else { hero(Modifier.weight(1f).fillMaxWidth()); next(Modifier.fillMaxWidth()) }
                    footer()
                }
            }
        }
        if (s.nightDim && p.prayer == Prayer.ISHA) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = .22f)))
    }
}

@Composable
private fun QuietTime(time: ZonedDateTime, s: AppSettings, size: Float, color: Color, modifier: Modifier) {
    val urdu = L10n.isUrdu(s)
    val value = L10n.iso(L10n.at(time, s.use24h))
    val suffix = PrayerEngine.suffix(time, s.use24h)
    val styled = buildAnnotatedString {
        append(value)
        if (!urdu && suffix.isNotBlank()) {
            val start = value.indexOf(suffix)
            if (start >= 0) addStyle(SpanStyle(fontSize = (size * .36f).sp), start, start + suffix.length)
        }
    }
    Text(styled, modifier, fontFamily = if (urdu) Amiri else QuietDisplay, fontWeight = FontWeight.Medium,
        fontSize = size.sp, lineHeight = (size * if (urdu) 1.9f else 1.1f).sp, color = color, textAlign = TextAlign.Center)
}

@Composable
private fun QuietLabel(text: String, urdu: Boolean, color: Color, modifier: Modifier = Modifier, size: Float = 12f) {
    Text(if (urdu) text else text.uppercase(java.util.Locale.ROOT), modifier.fillMaxWidth(), fontFamily = if (urdu) Amiri else QuietBody,
        fontSize = size.sp, lineHeight = (if (urdu) size * 2f else size * 1.5f).sp, letterSpacing = if (urdu) 0.sp else 1.4.sp,
        fontWeight = FontWeight.Medium, color = color, textAlign = TextAlign.Center)
}

@Composable
private fun QuietThemeBackdrop(s: AppSettings, wide: Boolean, modifier: Modifier) {
    val colors = quietPalette(s.theme)
    Box(modifier.background(colors.background).testTag("quiet-art-${s.theme.name}")) {
        if (s.artTheme != ArtTheme.MINIMAL) {
            val id = when (s.theme) {
                AppTheme.MIQAAT -> if (wide) R.drawable.quiet_miqaat_landscape else R.drawable.approved_miqaat_arch
                AppTheme.KISWAH -> R.drawable.approved_kiswah_silk
                AppTheme.CELESTIAL_MERIDIAN -> R.drawable.celestial_landscape_approved
                AppTheme.PRAYER_GALLERY -> if (wide) R.drawable.quiet_gallery_landscape else R.drawable.quiet_gallery_portrait
            }
            Image(painterResource(id), null, Modifier.matchParentSize(),
                contentScale = if (s.theme == AppTheme.KISWAH) ContentScale.FillBounds else ContentScale.Crop)
            Box(Modifier.matchParentSize().background(if (s.theme == AppTheme.PRAYER_GALLERY) colors.background.copy(alpha = .12f)
                else Color(0xFF010A18).copy(alpha = if (s.theme == AppTheme.KISWAH) .08f else .28f)))
        }
    }
}
