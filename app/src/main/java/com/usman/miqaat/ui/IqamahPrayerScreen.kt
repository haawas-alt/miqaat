package com.usman.miqaat.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale

internal data class IqamahCounter(val seconds: Long, val fraction: Float) {
    val label: String get() = String.format(Locale.ROOT, "%02d:%02d", seconds / 60, seconds % 60)
}

/** Round up partial seconds, clamp stale/future phases, and retain the service deadline as the sole timer source. */
internal fun iqamahCounter(p: Phase.IqamahCountdown, now: Long): IqamahCounter {
    val left = (p.endsAt - now).coerceAtLeast(0)
    return IqamahCounter((left + 999) / 1000,
        (left.toDouble() / (p.endsAt - p.startedAt).coerceAtLeast(1)).coerceIn(0.0, 1.0).toFloat())
}

@Composable
internal fun IqamahPrayerScreen(p: Phase.IqamahCountdown, onStop: () -> Unit, onStart: () -> Unit) {
    val s = MiqaatApp.instance.settings.value
    var now by remember(p.endsAt, s.zoneId) { mutableStateOf(ZonedDateTime.now(s.zone())) }
    LaunchedEffect(p.endsAt, s.zoneId) {
        while (true) { now = ZonedDateTime.now(s.zone()); delay(200) }
    }
    IqamahPrayerContent(p, s, now, onStop, onStart)
}

/** Native text and a native progress arc; background artwork is shared with the approved Quiet screens. */
@Composable
internal fun IqamahPrayerContent(p: Phase.IqamahCountdown, s: AppSettings, now: ZonedDateTime,
                                onStop: () -> Unit, onStart: () -> Unit) {
    if (isPhoneLandscape() && LocalDensity.current.fontScale <= 1.15f) {
        PhoneLandscapeIqamahContent(p, s, now, onStop, onStart); return
    }
    val colors = quietPalette(s.theme)
    val urdu = L10n.isUrdu(s)
    val scale = LocalDensity.current.fontScale
    val accessible = scale > 1.15f
    val target = remember(p.endsAt, s.zoneId) { Instant.ofEpochMilli(p.endsAt).atZone(s.zone()) }
    val counter = iqamahCounter(p, now.toInstant().toEpochMilli())
    BoxWithConstraints(Modifier.fillMaxSize().background(colors.background).testTag("iqamah-board")) {
        val landscape = maxWidth > maxHeight
        PrayerFlowBackdrop(s, landscape, Modifier.matchParentSize())
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = if (landscape) 28.dp else 20.dp, vertical = 10.dp)) {
            val compact = landscape && (maxHeight < 480.dp || accessible)
            val wide = landscape && !compact
            val roomy = wide && maxHeight >= 650.dp
            val tablet = maxWidth >= 600.dp
            val body = if (urdu) Amiri else QuietBody
            val text: @Composable (String, String, Float, Color) -> Unit = { value, tag, size, color ->
                Text(value, Modifier.fillMaxWidth().testTag(tag), fontFamily = body, fontSize = size.sp,
                    lineHeight = (size * if (urdu) 1.9f else 1.5f).sp, color = color, textAlign = TextAlign.Center)
            }
            val prayer: @Composable () -> Unit = {
                Text(p.prayer.arabic, Modifier.fillMaxWidth().testTag("iqamah-prayer-arabic").semantics { heading() },
                    fontFamily = if (s.theme == AppTheme.KISWAH) ReemKufi else Amiri,
                    fontSize = (if (roomy) 84 else if (wide) 64 else if (tablet) 68 else 42).sp,
                    lineHeight = (if (roomy) 124 else if (wide) 92 else if (tablet) 100 else 64).sp, color = colors.accent, textAlign = TextAlign.Center)
                Text(if (urdu) L10n.prayer(s, p.prayer) else L10n.prayer(s, p.prayer).uppercase(Locale.ROOT),
                    Modifier.fillMaxWidth().testTag("iqamah-prayer-name"), fontFamily = if (urdu) Amiri else Cinzel,
                    fontSize = (if (roomy) 22 else if (wide || tablet) 19 else 15).sp, lineHeight = if (roomy) (if (urdu) 42.sp else 32.sp) else if (urdu) 30.sp else 24.sp,
                    letterSpacing = if (urdu) 0.sp else 2.sp, color = colors.ink, textAlign = TextAlign.Center)
            }
            val schedule: @Composable () -> Unit = {
                QuietLabel(Str[R.string.s_iqamah_at], urdu, colors.accent, Modifier.testTag("iqamah-at-label"), if (roomy) 14f else if (wide) 12f else 10f)
                QuietTime(target, s, if (urdu) (if (roomy) 40f else if (wide || tablet) 32f else 26f) else if (roomy) 72f else if (wide) 56f else if (tablet) 58f else 38f,
                    colors.ink, Modifier.fillMaxWidth().testTag("iqamah-at-time"))
                if (target.toLocalDate().isAfter(now.withZoneSameInstant(s.zone()).toLocalDate()))
                    text(Str[R.string.s_quiet_tomorrow], "iqamah-tomorrow", 11f, colors.secondary)
            }
            val reminder: @Composable () -> Unit = {
                Text(Str[R.string.s_straighten_your_rows], Modifier.fillMaxWidth().padding(top = 5.dp).testTag("iqamah-reminder"),
                    fontFamily = Amiri, fontSize = if (roomy) 22.sp else if (wide) 19.sp else 15.sp, lineHeight = if (roomy) 34.sp else if (wide) 29.sp else 25.sp,
                    color = colors.ink, textAlign = TextAlign.Center)
                text(Str[R.string.s_a_al_bukh_r_723], "iqamah-source", if (wide) 12f else 10f, colors.secondary)
            }
            val actions: @Composable (Boolean) -> Unit = { inline ->
                text(Str[R.string.s_tap_skip_if_the_imam_is], "iqamah-hint", if (roomy) 14f else if (wide) 12f else 11f, colors.secondary)
                Spacer(Modifier.height(6.dp))
                if (inline) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IqamahAction(true, s, Modifier.weight(.65f), onStart, roomy)
                    IqamahAction(false, s, Modifier.weight(.35f), onStop, roomy)
                } else Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    IqamahAction(true, s, Modifier.fillMaxWidth(), onStart)
                    IqamahAction(false, s, Modifier.fillMaxWidth(), onStop)
                }
            }
            if (compact) {
                // Keep the counter and both actions pinned in short/large-text landscape. Supporting details scroll independently.
                Column(Modifier.fillMaxSize().testTag("iqamah-compact"), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth().weight(.72f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        IqamahDial(counter, s, Modifier.weight(.43f).fillMaxHeight(), compact = true)
                        Column(Modifier.weight(.57f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            IqamahAction(true, s, Modifier.fillMaxWidth(), onStart)
                            IqamahAction(false, s, Modifier.fillMaxWidth(), onStop)
                        }
                    }
                    Column(Modifier.fillMaxWidth().weight(.28f).verticalScroll(rememberScrollState()).testTag("iqamah-compact-details")) {
                        IqamahHeader(s, now, true)
                        if (s.theme == AppTheme.KISWAH) KiswahBand(false)
                        IqamahStages(s)
                        prayer(); schedule(); reminder()
                        text(Str[R.string.s_tap_skip_if_the_imam_is], "iqamah-hint", 12f, colors.secondary)
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().then(if (accessible) Modifier.verticalScroll(rememberScrollState()).testTag("iqamah-accessible") else Modifier)) {
                    IqamahHeader(s, now, accessible, roomy)
                    if (s.theme == AppTheme.KISWAH) KiswahBand(wide)
                    IqamahStages(s, roomy)
                    if (wide) {
                        Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            IqamahDial(counter, s, Modifier.weight(.52f).fillMaxHeight().testTag("iqamah-counter-panel"))
                            Box(Modifier.width(1.dp).fillMaxHeight(.80f).background(colors.accent.copy(alpha = .28f)))
                            Column(Modifier.weight(.48f).fillMaxHeight().padding(start = 28.dp, end = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
                                Column(Modifier.fillMaxWidth().testTag("iqamah-details-panel")) { prayer(); Spacer(Modifier.height(12.dp)); schedule(); reminder() }
                                Column(Modifier.fillMaxWidth()) { actions(true) }
                            }
                        }
                    } else {
                        if (accessible) {
                            prayer()
                            IqamahDial(counter, s, Modifier.fillMaxWidth().height(if (tablet) 400.dp else 280.dp).testTag("iqamah-counter-panel"))
                        } else Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            prayer()
                            IqamahDial(counter, s, Modifier.fillMaxWidth().weight(1f).testTag("iqamah-counter-panel"))
                        }
                        Column(Modifier.fillMaxWidth().testTag("iqamah-details-panel"), horizontalAlignment = Alignment.CenterHorizontally) {
                            schedule(); reminder()
                        }
                        Spacer(Modifier.height(8.dp)); actions(false)
                    }
                }
            }
        }
        if (s.nightDim && p.prayer == Prayer.ISHA) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = .22f)))
    }
}

@Composable
private fun IqamahHeader(s: AppSettings, now: ZonedDateTime, stacked: Boolean, roomy: Boolean = false) {
    val colors = quietPalette(s.theme)
    val urdu = L10n.isUrdu(s)
    val brand: @Composable (Modifier) -> Unit = { m ->
        Text("Miqaat", m.testTag("iqamah-brand"), fontFamily = QuietDisplay, fontSize = if (roomy) 40.sp else 27.sp, lineHeight = if (roomy) 48.sp else 34.sp, color = colors.ink)
    }
    val label: @Composable (Modifier) -> Unit = { m -> QuietLabel(Str[R.string.s_iqamah], urdu, colors.accent, m.testTag("iqamah-mode"), if (roomy) 14f else 11f) }
    val clock: @Composable (Modifier) -> Unit = { m ->
        Column(m, horizontalAlignment = Alignment.End) {
            Text(L10n.word(s, "NOW"), fontFamily = if (urdu) Amiri else QuietBody, fontSize = if (roomy) 11.sp else 9.sp,
                lineHeight = if (urdu) 17.sp else 12.sp, color = colors.secondary)
            Text(L10n.iso(L10n.at(now, s.use24h)), Modifier.testTag("iqamah-live-clock"),
                fontFamily = if (urdu) Amiri else QuietDisplay, fontSize = if (roomy) 20.sp else 14.sp, lineHeight = if (roomy) (if (urdu) 38.sp else 28.sp) else if (urdu) 26.sp else 21.sp, color = colors.ink)
        }
    }
    if (stacked) Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { brand(Modifier); label(Modifier); clock(Modifier.fillMaxWidth()) }
    else Box(Modifier.fillMaxWidth().heightIn(min = if (roomy) 64.dp else 48.dp)) {
        brand(Modifier.fillMaxWidth(.25f).align(Alignment.CenterStart))
        label(Modifier.fillMaxWidth(.40f).align(Alignment.Center))
        clock(Modifier.fillMaxWidth(.28f).align(Alignment.CenterEnd))
    }
    if (s.locationName.isNotBlank()) Text(L10n.iso(L10n.place(s.locationName)),
        Modifier.fillMaxWidth().testTag("iqamah-location"), fontFamily = if (urdu) Amiri else QuietBody,
        fontSize = if (roomy) 12.sp else 10.sp, lineHeight = if (roomy) (if (urdu) 23.sp else 18.sp) else if (urdu) 19.sp else 15.sp, color = colors.secondary, textAlign = TextAlign.End)
}

@Composable
private fun IqamahStages(s: AppSettings, roomy: Boolean = false) {
    val colors = quietPalette(s.theme)
    val urdu = L10n.isUrdu(s)
    val labels = listOf(Str[R.string.s_azaan], Str[R.string.s_iqamah_countdown], Str[R.string.s_iqamah], Str[R.string.s_step_prayer])
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).testTag("iqamah-stages"), verticalAlignment = Alignment.Top) {
        labels.forEachIndexed { index, label ->
            Column(Modifier.weight(1f).semantics { if (index == 1) selected = true }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(18.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.accent.copy(alpha = .30f)))
                    Box(Modifier.size(16.dp).clip(RoundedCornerShape(8.dp)).background(if (index <= 1) colors.accent else colors.background)
                        .border(1.dp, colors.secondary.copy(alpha = .7f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        if (index == 0) Text("✓", fontSize = 10.sp, lineHeight = 14.sp, color = colors.background)
                    }
                }
                Text(if (urdu) label else label.uppercase(Locale.ROOT), Modifier.fillMaxWidth().testTag("iqamah-stage-$index"),
                    fontFamily = if (urdu) Amiri else QuietBody, fontSize = if (roomy) 12.sp else 9.sp, lineHeight = if (roomy) (if (urdu) 25.sp else 18.sp) else if (urdu) 20.sp else 13.sp,
                    color = if (index == 1) colors.accent else colors.secondary, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
internal fun IqamahDial(counter: IqamahCounter, s: AppSettings, modifier: Modifier, compact: Boolean = false) {
    val colors = quietPalette(s.theme)
    val urdu = L10n.isUrdu(s)
    val scale = LocalDensity.current.fontScale
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val diameter = minOf(maxWidth * if (compact) .95f else .88f, maxHeight - 8.dp, 540.dp).coerceAtLeast(96.dp)
        Box(Modifier.size(diameter).testTag("iqamah-dial"), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(5.dp)) {
                val stroke = Stroke(width = (size.minDimension * .023f).coerceAtLeast(2.dp.toPx()), cap = StrokeCap.Round)
                drawArc(colors.ink.copy(alpha = .24f), -90f, 360f, false, style = stroke)
                if (counter.fraction > 0f) drawArc(colors.accent, -90f, 360f * counter.fraction, false, style = stroke)
            }
            // Counter glyphs stay within their dial at every font scale; surrounding reading text reflows.
            Column(Modifier.fillMaxWidth(.87f), horizontalAlignment = Alignment.CenterHorizontally) {
                QuietLabel(Str[R.string.s_iqamah_until], urdu, colors.ink, Modifier.testTag("iqamah-until"),
                    (if (compact) 9f else if (diameter > 300.dp) 12f else 9f) / if (compact) scale else 1f)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(counter.label, Modifier.fillMaxWidth().testTag("iqamah-seconds").semantics {
                        contentDescription = Str.get(R.string.s_iqamah_remaining_accessibility, (counter.seconds / 60).toInt(), (counter.seconds % 60).toInt())
                    }, style = TextStyle(fontFeatureSettings = "lnum, tnum"),
                        fontFamily = QuietDisplay, fontWeight = FontWeight.Medium, fontSize = (diameter.value * .285f / scale).sp,
                        lineHeight = (diameter.value * .32f / scale).sp, color = colors.ink, textAlign = TextAlign.Center)
                }
                QuietLabel(Str[R.string.s_iqamah_min_sec], urdu, colors.secondary, Modifier.testTag("iqamah-unit"),
                    (if (compact) 9f else 10f) / if (compact) scale else 1f)
            }
        }
    }
}

@Composable
internal fun IqamahAction(primary: Boolean, s: AppSettings, modifier: Modifier, onClick: () -> Unit, roomy: Boolean = false) {
    val colors = quietPalette(s.theme)
    val gallery = s.theme == AppTheme.PRAYER_GALLERY
    val fill = if (primary) (if (gallery) colors.ink else colors.accent) else colors.panel
    val ink = if (primary) (if (gallery) Color(0xFFFFF8E9) else Color(0xFF101819)) else colors.ink
    Box(modifier.heightIn(min = if (roomy) 56.dp else 48.dp).clip(RoundedCornerShape(28.dp)).background(fill)
        .border(1.dp, colors.accent.copy(alpha = .8f), RoundedCornerShape(28.dp))
        .testTag(if (primary) "iqamah-start" else "iqamah-dismiss").clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
        Text(Str[if (primary) R.string.s_start_iqamah_now else R.string.s_dismiss],
            Modifier.fillMaxWidth().testTag(if (primary) "iqamah-start-label" else "iqamah-dismiss-label"),
            fontFamily = if (L10n.isUrdu(s)) Amiri else QuietBody, fontWeight = FontWeight.SemiBold, fontSize = if (roomy) 18.sp else 14.sp,
            lineHeight = if (roomy) (if (L10n.isUrdu(s)) 34.sp else 27.sp) else if (L10n.isUrdu(s)) 27.sp else 21.sp, color = ink, textAlign = TextAlign.Center)
    }
}
