package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import kotlinx.coroutines.delay
import java.time.Instant
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

private val QuietGold = Color(0xFFE7CB88)
private val QuietIvory = Color(0xFFF3EBDC)
private val QuietSecondary = Color(0xFFC3C6BC)

@Composable
internal fun QuietPrayerScreen(p: Phase.Quiet, onStop: () -> Unit) {
    val s = MiqaatApp.instance.settings.value
    var now by remember(s.zoneId) { mutableStateOf(ZonedDateTime.now(s.zone())) }
    LaunchedEffect(s.zoneId) { while (true) { now = ZonedDateTime.now(s.zone()); delay(15_000) } }
    QuietPrayerContent(p, s, now, onStop)
}

/** Deterministic content also used by real-layout acceptance tests. */
@Composable
internal fun QuietPrayerContent(p: Phase.Quiet, s: AppSettings, now: ZonedDateTime, onStop: () -> Unit) {
    val times = remember(s, p.prayer, now.toLocalDate(), now.hour, now.minute) { quietPrayerTimes(s, p.prayer, now) }
    val urdu = L10n.isUrdu(s)
    val displayFont = if (urdu) Nastaliq else Cormorant
    val fontScale = LocalDensity.current.fontScale
    val remaining = ((p.endsAt - now.toInstant().toEpochMilli()).coerceAtLeast(0) + 59_999) / 60_000
    BoxWithConstraints(Modifier.fillMaxSize().testTag("quiet-board")
        .background(Brush.verticalGradient(listOf(Color(0xFF0D211B), Color(0xFF050E0A))))
        .clickable(role = Role.Button, onClickLabel = Str[R.string.s_quiet_exit], onClick = onStop)
        .safeDrawingPadding().padding(20.dp)) {
        val short = maxHeight < 480.dp
        val large = maxWidth >= 600.dp && !short
        val accessible = fontScale > 1.35f || (short && fontScale > 1.15f)
        val wide = maxWidth > maxHeight && !accessible
        if (!wide) QuietBackdrop(Modifier.matchParentSize())
        val header: @Composable () -> Unit = {
            val brand: @Composable (Modifier) -> Unit = { m ->
                Column(m, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Miqaat", Modifier.testTag("quiet-brand"), fontFamily = Cormorant, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = QuietGold)
                    Text(Str[R.string.s_quiet_mode_title], fontFamily = if (urdu) Nastaliq else Nunito,
                        fontSize = if (urdu) 12.sp else 13.sp, color = QuietSecondary)
                }
            }
            val clock: @Composable (Modifier) -> Unit = { m ->
                Text(L10n.iso(L10n.at(now, s.use24h)), m.testTag("quiet-live-clock"),
                    fontFamily = displayFont, fontSize = if (urdu) 11.sp else 14.sp,
                    color = QuietSecondary, textAlign = TextAlign.End)
            }
            if (maxWidth < 600.dp) Column(Modifier.fillMaxWidth().padding(bottom = if (short) 6.dp else 16.dp)) {
                clock(Modifier.fillMaxWidth()); brand(Modifier.fillMaxWidth())
            } else Box(Modifier.fillMaxWidth().padding(bottom = if (short) 6.dp else 16.dp)) {
                brand(Modifier.align(Alignment.TopCenter)); clock(Modifier.align(Alignment.TopEnd))
            }
        }
        val hero: @Composable (Modifier) -> Unit = { modifier ->
            Box(modifier.testTag("quiet-current-panel")) {
                if (wide) QuietBackdrop(Modifier.matchParentSize())
            Column(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = if (short) 4.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                QuietLabel(Str[R.string.s_quiet_prayer_progress], urdu, Modifier.testTag("quiet-progress"))
                Spacer(Modifier.height(if (short) 6.dp else 16.dp))
                Text(L10n.prayer(s, p.prayer), Modifier.fillMaxWidth().testTag("quiet-current-name").semantics { heading() },
                    fontFamily = displayFont, fontWeight = FontWeight.Bold,
                    fontSize = (if (urdu) { if (short) 22 else if (large) 56 else 38 } else { if (short) 48 else if (large) 94 else 64 }).sp,
                    lineHeight = (if (urdu) { if (short) 42 else if (large) 108 else 72 } else { if (short) 52 else if (large) 100 else 70 }).sp,
                    textAlign = TextAlign.Center, color = QuietGold)
                Spacer(Modifier.height(if (short) 4.dp else 12.dp))
                QuietLabel(Str[R.string.s_quiet_current_azaan], urdu, Modifier.testTag("quiet-current-label"))
                Text(L10n.iso(L10n.at(times.current, s.use24h)), Modifier.fillMaxWidth().testTag("quiet-current-time"),
                    fontFamily = displayFont, fontSize = (if (urdu) { if (short) 18 else if (large) 44 else 30 } else { if (short) 38 else if (large) 66 else 48 }).sp,
                    lineHeight = (if (urdu) { if (short) 34 else if (large) 88 else 58 } else { if (short) 44 else if (large) 72 else 54 }).sp,
                    textAlign = TextAlign.Center, color = QuietIvory)
                Spacer(Modifier.height(if (short) 6.dp else 18.dp))
                Box(Modifier.width(32.dp).height(1.dp).background(QuietGold.copy(alpha = 0.65f)))
                Text(Str[R.string.s_quiet_iqamah_complete], Modifier.testTag("quiet-iqamah-complete").padding(top = if (short) 4.dp else 12.dp),
                    fontFamily = if (urdu) Nastaliq else Nunito, fontSize = (if (urdu && short) 10 else if (urdu) 12 else 14).sp,
                    lineHeight = (if (urdu && short) 24 else if (urdu) 30 else 20).sp,
                    color = QuietSecondary, textAlign = TextAlign.Center)
            }
        }
            }
        val next: @Composable (Modifier) -> Unit = { modifier ->
            Box(modifier.testTag("quiet-next-panel")
                .border(1.dp, if (wide) Color.Transparent else QuietGold.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
                .background(if (wide) Color.Transparent else Color(0xDD0B1B14), RoundedCornerShape(16.dp))) {
                if (wide) QuietBackdrop(Modifier.matchParentSize())
            Column(Modifier.fillMaxWidth().then(if (wide) Modifier.fillMaxHeight() else Modifier)
                .padding(horizontal = 16.dp, vertical = if (short) 10.dp else 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                QuietLabel(Str[R.string.s_quiet_next_azaan], urdu, Modifier.testTag("quiet-next-label"))
                Text(L10n.prayer(s, times.next), Modifier.fillMaxWidth().testTag("quiet-next-name"),
                    fontFamily = displayFont, fontSize = (if (urdu) { if (short) 22 else if (large) 40 else 28 } else { if (short) 40 else if (large) 64 else 30 }).sp,
                    lineHeight = (if (urdu) { if (short) 42 else if (large) 80 else 54 } else { if (short) 46 else if (large) 70 else 36 }).sp,
                    color = QuietGold, textAlign = TextAlign.Center)
                Text(L10n.iso(L10n.at(times.nextTime, s.use24h)), Modifier.fillMaxWidth().testTag("quiet-next-time"),
                    fontFamily = displayFont, fontSize = (if (urdu) { if (short) 20 else if (large) 38 else 26 } else { if (short) 34 else if (large) 48 else 32 }).sp,
                    lineHeight = (if (urdu) { if (short) 38 else if (large) 76 else 50 } else { if (short) 40 else if (large) 54 else 38 }).sp,
                    color = QuietIvory, textAlign = TextAlign.Center)
                if (times.nextTime.toLocalDate().isAfter(now.withZoneSameInstant(s.zone()).toLocalDate()))
                    Text(Str[R.string.s_quiet_tomorrow], fontFamily = if (urdu) Nastaliq else Nunito,
                        fontSize = (if (urdu) 12 else 14).sp, color = QuietSecondary, modifier = Modifier.testTag("quiet-tomorrow"))
            }
        }
            }
        val footer: @Composable () -> Unit = {
            Column(Modifier.fillMaxWidth().padding(top = if (short) 4.dp else 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Str.get(R.string.s_quiet_ends_hint, remaining.toInt()), fontFamily = if (urdu) Nastaliq else Nunito,
                    fontSize = (if (urdu) 11 else 12).sp, lineHeight = (if (urdu) 26 else 17).sp, color = QuietSecondary, textAlign = TextAlign.Center)
                Box(Modifier.heightIn(min = 48.dp).widthIn(min = 160.dp)
                    .border(1.dp, QuietGold.copy(alpha = 0.30f), RoundedCornerShape(24.dp))
                    .testTag("quiet-return").clickable(role = Role.Button, onClick = onStop).padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center) {
                    Text(Str[R.string.s_quiet_exit], Modifier.testTag("quiet-exit-label"), fontFamily = if (urdu) Nastaliq else Nunito, fontSize = (if (urdu) 13 else 14).sp,
                        color = QuietIvory, textAlign = TextAlign.Center)
                }
            }
        }
        if (accessible) Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).testTag("quiet-accessible"),
            horizontalAlignment = Alignment.CenterHorizontally) {
            header(); hero(Modifier.fillMaxWidth()); next(Modifier.fillMaxWidth()); footer()
        } else Column(Modifier.fillMaxSize()) {
            header()
            if (wide) Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                hero(Modifier.weight(0.60f).fillMaxHeight())
                Box(Modifier.width(1.dp).fillMaxHeight(0.70f).background(QuietGold.copy(alpha = 0.25f)))
                Spacer(Modifier.width(20.dp))
                next(Modifier.weight(0.40f).fillMaxHeight(0.85f))
            } else {
                hero(Modifier.weight(1f).fillMaxWidth())
                next(Modifier.fillMaxWidth())
            }
            footer()
        }
    }
}

@Composable
private fun QuietLabel(text: String, urdu: Boolean, modifier: Modifier = Modifier) {
    Text(text, modifier.fillMaxWidth(), fontFamily = if (urdu) Nastaliq else Nunito, fontWeight = FontWeight.SemiBold,
        fontSize = (if (urdu) 12 else 12).sp, lineHeight = (if (urdu) 26 else 17).sp,
        letterSpacing = (if (urdu) 0 else 1).sp, textAlign = TextAlign.Center, color = QuietIvory)
}

@Composable
private fun QuietBackdrop(modifier: Modifier) {
    Box(modifier) {
        Image(painterResource(R.drawable.quiet_option2_mihrab), null, Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds, alpha = 0.72f)
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.12f)))
    }
}
