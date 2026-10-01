package com.usman.miqaat.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.usman.miqaat.R
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.Duas
import com.usman.miqaat.data.L10n
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AzaanScreen(phase: Phase, onStop: () -> Unit, onSkip: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val portrait = maxHeight > maxWidth
        val u = if (portrait) maxWidth / 62 else maxWidth / 100
        if (phase is Phase.Quiet) { QuietBody(phase, u, onStop); return@BoxWithConstraints }
        val isAzaan = phase is Phase.Azaan
        val isIq = phase is Phase.IqamahCountdown || phase is Phase.IqamahNow
        val tk = screenTokens()
        val kiswah = com.usman.miqaat.MiqaatApp.instance.settings.value.theme == com.usman.miqaat.data.AppTheme.KISWAH
        val bg = if (kiswah) ThemeTokenSets.kiswah.skyAzaan else when { isAzaan -> tk.skyAzaan; isIq -> tk.skyIqamah; else -> tk.skyDua }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(bg))) {
            val artTheme = tk.art == ArtStyle.CELESTIAL || tk.art == ArtStyle.GALLERY
            if (artTheme) ThemedBackdrop(portrait, Modifier.fillMaxSize(), scrim = 0.72f)
            else if (kiswah) Weave(Modifier.fillMaxSize()) else GirihLattice(Modifier.fillMaxSize(), tile = u.value * 11f, alpha = 0.12f)
            // Option A: a quiet status tag, top-left, instead of any system pop-up
            Row(Modifier.statusBarsPadding().padding(start = u * 3.6f, top = u * 2.6f), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(u * 0.9f).clip(androidx.compose.foundation.shape.CircleShape).background(tk.primary))
                Spacer(Modifier.width(u * 0.9f))
                Text(
                    when (phase) {
                        is Phase.Azaan -> Str.get(R.string.s_x_azaan_playing, L10n.prayer(com.usman.miqaat.MiqaatApp.instance.settings.value, phase.prayer))
                        is Phase.IqamahCountdown, is Phase.IqamahNow -> Str.get(R.string.s_prayer_iqamah, L10n.prayer(com.usman.miqaat.MiqaatApp.instance.settings.value, phase.prayer))
                        else -> Str.get(R.string.s_prayer_after_azaan, L10n.prayer(com.usman.miqaat.MiqaatApp.instance.settings.value, phase.prayer))
                    }.uppercase(),
                    fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, letterSpacing = (u.value * 0.16f).sp, fontWeight = FontWeight.Bold, color = tk.accent.copy(alpha = 0.85f)
                )
            }
            run {
                val st = com.usman.miqaat.MiqaatApp.instance.settings.value
                var tm by remember { mutableStateOf(java.time.ZonedDateTime.now(st.zone())) }
                LaunchedEffect(Unit) { while (true) { tm = java.time.ZonedDateTime.now(st.zone()); delay(15_000) } }
                Column(Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = u * 3.6f, top = u * 2.2f), horizontalAlignment = Alignment.End) {
                    Text(L10n.iso(st.locationName), fontFamily = Nunito, fontSize = (u.value * 1.4f).sp, color = tk.contentSecondary, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = u * 24))
                    Text(com.usman.miqaat.data.PrayerEngine.clock(tm, st.use24h) + " " + com.usman.miqaat.data.PrayerEngine.suffix(tm, st.use24h), fontFamily = Cormorant, fontSize = (u.value * 3.2f).sp, color = tk.contentPrimary)
                }
            }
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(top = if (portrait) u * 3 else 0.dp)) {
                StepsBar(phase, u)
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    Crossfade(targetState = phase::class, label = "phase") { cls ->
                        when (cls) {
                            Phase.Azaan::class -> AzaanBody(phase, u)
                            Phase.Dua::class -> DuaBody(u)
                            Phase.Iftar::class -> IftarBody(u)
                            Phase.IqamahCountdown::class -> (phase as? Phase.IqamahCountdown)?.let { CountdownBody(it, u) }
                            Phase.IqamahNow::class -> IqamahNowBody(phase, u)
                            else -> (phase as? Phase.HadithPhase)?.let { HadithBody(it, u) }
                        }
                    }
                }
                BottomBar(phase, u, onStop, onSkip)
            }
        }
    }
}

@Composable
private fun StepsBar(phase: Phase, u: Dp) {
    val tk = screenTokens()
    val iftar = phase is Phase.Iftar
    val iq = phase is Phase.IqamahCountdown || phase is Phase.IqamahNow
    val idx = when (phase) { is Phase.Azaan -> 0; is Phase.Iftar -> 1; is Phase.Dua -> if (iftar) 2 else 1; is Phase.HadithPhase -> if (iftar) 3 else 2; is Phase.IqamahCountdown -> 1; is Phase.IqamahNow -> 2; is Phase.Quiet -> 3 }
    // Only promise the steps that will actually run: with "after the azaan" off, the screen closes when the azaan ends.
    val after = com.usman.miqaat.MiqaatApp.instance.settings.value.afterAzaanEnabled || phase !is Phase.Azaan
    val labels = when {
        iq -> listOf(Str[R.string.s_step_azaan], Str[R.string.s_iqamah_countdown], Str[R.string.s_iqamah], Str[R.string.s_step_prayer])
        iftar -> listOf(Str[R.string.s_step_azaan], Str[R.string.s_iftar_dua], Str[R.string.s_dua_after_azaan], Str[R.string.s_step_hadith], Str[R.string.s_step_home])
        !after -> listOf(Str[R.string.s_step_azaan], Str[R.string.s_step_home])
        else -> listOf(Str[R.string.s_step_azaan], Str[R.string.s_dua_after_azaan], Str[R.string.s_step_hadith], Str[R.string.s_step_home])
    }
    // Step line like the approved mockups: a dot per step joined by a line, label underneath.
    Row(Modifier.fillMaxWidth().padding(top = u * 2.4f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
        labels.forEachIndexed { i, l ->
            val done = i < idx; val cur = i == idx
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = u * 15)) {
                Box(Modifier.size(u * 1.9f).clip(CircleShape).background(if (cur) tk.primary else if (done) tk.success else Color.Transparent)
                    .border(2.dp, if (cur) tk.primary else if (done) tk.success else tk.neutralStroke, CircleShape), contentAlignment = Alignment.Center) {
                    if (done) Text("✓", fontSize = (u.value * 1.2f).sp, color = tk.onPrimary, fontWeight = FontWeight.Bold)
                    else if (cur) Box(Modifier.size(u * 0.7f).clip(CircleShape).background(tk.onPrimary))
                }
                Text(l.uppercase(), fontFamily = Nunito, fontSize = (u.value * 1.2f).sp, letterSpacing = (u.value * 0.1f).sp, fontWeight = if (cur) FontWeight.Bold else FontWeight.SemiBold, textAlign = TextAlign.Center, maxLines = 2,
                    color = if (cur) tk.primary else tk.contentPrimary.copy(alpha = 0.6f), modifier = Modifier.padding(top = u * 0.5f, start = u * 0.3f, end = u * 0.3f))
            }
            if (i < labels.lastIndex) Box(Modifier.padding(top = u * 0.9f).width(u * 6).height(2.dp).background(if (i < idx) tk.success else tk.neutralStroke))
        }
    }
}

@Composable
private fun AzaanBody(phase: Phase, u: Dp) {
    val tk = screenTokens()
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("ٱللَّٰهُ أَكْبَرُ", fontFamily = Amiri, fontSize = (u.value * 8f).sp, lineHeight = (u.value * 12f).sp, color = tk.arabicText, textAlign = TextAlign.Center)
        Text("${phase.prayer.english.uppercase()} AZAAN  ·  ${phase.prayer.arabic}", fontFamily = Cormorant, fontSize = (u.value * 2.6f).sp, letterSpacing = (u.value * 0.6f).sp, color = tk.contentPrimary.copy(alpha = 0.85f), modifier = Modifier.padding(top = u * 1))
        Wave(Modifier.padding(top = u * 4).width(u * 22).height(u * 8))
        Text(Str[R.string.s_hayya_ala_al_h_come_to], fontFamily = Nunito, fontSize = (u.value * 1.6f).sp, letterSpacing = (u.value * 0.1f).sp, color = tk.contentSecondary, modifier = Modifier.padding(top = u * 3))
    }
}

@Composable
private fun DuaBody(u: Dp) {
    val tk = screenTokens()
    Column(Modifier.fillMaxSize().padding(horizontal = u * 9), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker(Str[R.string.s_dua_after_the_azaan], u)
        Arabic(Duas.AFTER_AZAAN_AR, u, size = 4.4f)
        Translation(Duas.AFTER_AZAAN_EN, u)
        Source("${Duas.AFTER_AZAAN_SRC}  ·  ${Duas.AFTER_AZAAN_NOTE}", u)
    }
}

@Composable
private fun IftarBody(u: Dp) {
    val tk = screenTokens()
    Column(Modifier.fillMaxSize().padding(horizontal = u * 9), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker(Str[R.string.s_rama_n_dua_at_iftar], u)
        Arabic(com.usman.miqaat.data.Ramadan.IFTAR_AR, u, size = 4.8f)
        Translation(com.usman.miqaat.data.Ramadan.IFTAR_EN, u)
        Source(com.usman.miqaat.data.Ramadan.IFTAR_SRC, u)
    }
}

@Composable
private fun CountdownBody(p: Phase.IqamahCountdown, u: Dp) {
    val tk = screenTokens()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(200) } }
    val total = (p.endsAt - p.startedAt).coerceAtLeast(1)
    val leftMs = (p.endsAt - now).coerceAtLeast(0)
    val secs = ((leftMs + 999) / 1000).toInt()
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker("${p.prayer.english} · iqamah in", u)
        Box(Modifier.padding(vertical = u * 1).size(u * 26), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(width = size.width * 0.045f, cap = StrokeCap.Round)
                drawArc(tk.neutralStroke, 0f, 360f, false, style = stroke)
                drawArc(tk.primary, -90f, 360f * (leftMs / total.toFloat()), false, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$secs", fontFamily = Cormorant, fontSize = (u.value * 10.5f).sp, lineHeight = (u.value * 10.5f).sp, color = tk.contentPrimary)
                Text(Str[R.string.s_seconds_caps], fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, letterSpacing = (u.value * 0.35f).sp, fontWeight = FontWeight.Bold, color = tk.contentSecondary, modifier = Modifier.padding(top = u * 0.6f))
            }
        }
        Text(Str[R.string.s_straighten_your_rows], fontFamily = Cormorant, fontSize = (u.value * 2.4f).sp, color = tk.contentPrimary.copy(alpha = 0.9f))
        Text(Str[R.string.s_a_al_bukh_r_723], fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, color = tk.contentSecondary, modifier = Modifier.padding(top = u * 0.6f))
    }
}

@Composable
private fun IqamahNowBody(phase: Phase, u: Dp) {
    val tk = screenTokens()
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker(phase.prayer.english, u)
        Text("الإقامة", fontFamily = Amiri, fontSize = (u.value * 9f).sp, lineHeight = (u.value * 10f).sp, color = tk.arabicText)
        Text("قَدْ قَامَتِ الصَّلاَةُ", fontFamily = Amiri, fontSize = (u.value * 4.2f).sp, lineHeight = (u.value * 6f).sp, color = tk.accent)
        Text(Str[R.string.s_the_prayer_has_begun], fontFamily = Cormorant, fontSize = (u.value * 2.4f).sp, color = tk.contentPrimary.copy(alpha = 0.9f), modifier = Modifier.padding(top = u * 0.6f))
    }
}

@Composable
private fun QuietBody(p: Phase.Quiet, u: Dp, onStop: () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    Column(
        Modifier.fillMaxSize().background(Color(0xFF05090F)).clickable(onClick = onStop),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        val t = java.time.LocalTime.now()
        Text("%d:%02d".format(if (t.hour % 12 == 0) 12 else t.hour % 12, t.minute), fontFamily = Cormorant, fontSize = (u.value * 12f).sp, lineHeight = (u.value * 12f).sp, color = Palette.textMuted)
        Text(p.prayer.arabic, fontFamily = Amiri, fontSize = (u.value * 3.4f).sp, color = Color(0xFFF6E7B8).copy(alpha = 0.5f))
        val left = ((p.endsAt - now).coerceAtLeast(0) / 60_000) + 1
        Text(Str.get(R.string.s_in_prayer_wake, left.toInt()), fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, letterSpacing = (u.value * 0.08f).sp, color = Palette.textSecondary, modifier = Modifier.padding(top = u * 2))
    }
}

@Composable
private fun HadithBody(p: Phase.HadithPhase, u: Dp) {
    val tk = screenTokens()
    Column(Modifier.fillMaxSize().padding(horizontal = u * 9), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker("A hadith after ${p.prayer.english} · ${p.hadith.source}", u)
        val long = p.hadith.arabic.length > 110
        Arabic("قَالَ رَسُولُ اللَّهِ ﷺ: " + p.hadith.arabic, u, size = if (long) 3.3f else 4f)
        Translation("The Messenger of Allah ﷺ said: “${p.hadith.english}”", u, size = if (p.hadith.english.length > 160) 2f else 2.35f)
        Source("Narrated by ${p.hadith.narrator}  ·  ${p.hadith.source}", u)
    }
}

@Composable private fun Kicker(t: String, u: Dp) = Text(t.uppercase(), fontFamily = Nunito, fontSize = (u.value * 1.35f).sp, letterSpacing = (u.value * 0.3f).sp, fontWeight = FontWeight.Bold, color = screenTokens().accent)
@Composable private fun Arabic(t: String, u: Dp, size: Float) = CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Text(t, fontFamily = Amiri, fontSize = (u.value * size).sp, lineHeight = (u.value * size * 1.75f).sp, color = screenTokens().arabicText, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = u * 1.2f))
}
@Composable private fun Translation(t: String, u: Dp, size: Float = 2.35f) = Text(t, fontFamily = Cormorant, fontSize = (u.value * size).sp, lineHeight = (u.value * size * 1.45f).sp, color = screenTokens().contentPrimary, textAlign = TextAlign.Center)
@Composable private fun Source(t: String, u: Dp) = Text(t, fontFamily = Nunito, fontSize = (u.value * 1.35f).sp, letterSpacing = (u.value * 0.08f).sp, color = screenTokens().contentPrimary.copy(alpha = 0.75f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = u * 1.4f))

@Composable
private fun BottomBar(phase: Phase, u: Dp, onStop: () -> Unit, onSkip: () -> Unit) {
    val tk = screenTokens()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
    androidx.compose.foundation.layout.FlowRow(
        Modifier.fillMaxWidth().padding(horizontal = u * 3.6f, vertical = u * 2.6f).clip(RoundedCornerShape(u * 2)).background(tk.surface.copy(alpha = 0.72f))
            .border(1.dp, tk.neutralStroke, RoundedCornerShape(u * 2)).padding(horizontal = u * 2.4f, vertical = u * 1.4f),
        verticalArrangement = Arrangement.spacedBy(u * 1f), horizontalArrangement = Arrangement.spacedBy(u * 1.2f)
    ) {
        // left: narration state
        Row(Modifier.weight(1f, fill = false), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(u * 1.2f)) {
            val narrating = when (phase) { is Phase.Azaan -> true; is Phase.Dua, is Phase.Iftar -> true; is Phase.HadithPhase -> phase.narrating; is Phase.IqamahNow -> true; else -> false }
            if (narrating) Wave(Modifier.width(u * 6).height(u * 2.4f), bars = 5)
            Text(
                when (phase) { is Phase.Azaan -> Str[R.string.s_azaan_playing]; is Phase.Iftar -> Str[R.string.s_reading_the_iftar_dua]; is Phase.Dua -> Str[R.string.s_reading_the_dua]; is Phase.HadithPhase -> if (phase.narrating) Str[R.string.s_reading_the_hadith] else Str[R.string.s_take_a_moment]; is Phase.IqamahCountdown -> Str[R.string.s_tap_skip_if_the_imam_is]; is Phase.IqamahNow -> Str[R.string.s_iqamah]; is Phase.Quiet -> "" },
                fontFamily = Nunito, fontSize = (u.value * 1.5f).sp, fontWeight = FontWeight.SemiBold, color = tk.contentPrimary, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        // middle: countdown ring for hadith
        if (phase is Phase.HadithPhase) {
            val total = (phase.endsAt - phase.startedAt).coerceAtLeast(1)
            val left = (phase.endsAt - now).coerceAtLeast(0)
            Ring(left / total.toFloat(), "%d:%02d".format(left / 60_000, (left / 1000) % 60), u)
        } else Spacer(Modifier.width(u * 6.4f))
        // right: actions
        Row(horizontalArrangement = Arrangement.spacedBy(u * 1.2f)) {
            when (phase) {
                is Phase.Azaan -> { Pill(Str[R.string.s_stop_azaan], true, u.value, onStop); Pill(Str[R.string.s_skip], false, u.value, onSkip) }
                is Phase.IqamahCountdown -> { Pill(Str[R.string.s_dismiss], false, u.value, onStop); Pill(Str[R.string.s_start_iqamah_now], true, u.value, onSkip) }
                is Phase.IqamahNow -> Pill(Str[R.string.s_dismiss], false, u.value, onStop)
                is Phase.HadithPhase -> Pill(Str[R.string.s_back_to_clock], false, u.value, onStop)
                else -> { Pill(Str[R.string.s_back_to_clock], false, u.value, onStop); Pill(Str[R.string.s_skip], false, u.value, onSkip) }
            }
        }
    }
}

@Composable
private fun Ring(fraction: Float, label: String, u: Dp) {
    val tk = screenTokens()
    Box(Modifier.size(u * 6.4f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 2.5f * (size.width / 40f), cap = StrokeCap.Round)
            drawArc(tk.neutralStroke, 0f, 360f, false, style = stroke)
            drawArc(tk.primary, -90f, 360f * fraction.coerceIn(0f, 1f), false, style = stroke)
        }
        Text(label, fontFamily = Cormorant, fontSize = (u.value * 1.9f).sp, color = tk.contentPrimary)
    }
}

@Composable
private fun Pill(label: String, primary: Boolean, u: Float, onClick: () -> Unit) {
    val tk = screenTokens()
    val shape = RoundedCornerShape(50)
    Box(
        Modifier.clip(shape).background(if (primary) tk.primary else Color.Transparent)
            .border(1.dp, if (primary) tk.primary else tk.neutralStroke, shape)
            .clickable(onClick = onClick, role = androidx.compose.ui.semantics.Role.Button).heightIn(min = 52.dp).padding(horizontal = (u * 3f).dp, vertical = (u * 1.3f).dp),
        contentAlignment = Alignment.Center
    ) { Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = (u * 1.75f).sp, color = if (primary) tk.onPrimary else tk.contentPrimary, maxLines = 1) }
}

@Composable
private fun Wave(modifier: Modifier, bars: Int = 15) {
    val tk = screenTokens()
    val still = reduceMotion()
    val anim by rememberInfiniteTransition(label = "wave").animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart), label = "t")
    val t = if (still) 0.25f else anim
    Canvas(modifier.semantics { contentDescription = Str[R.string.s_audio_playing] }) {
        val gap = size.width / bars
        for (i in 0 until bars) {
            val phase = (i / bars.toFloat()) * PI * 2
            val hgt = size.height * (0.2f + 0.8f * (0.5f + 0.5f * sin(t * 2 * PI + phase).toFloat()))
            drawRoundRect(Brush.verticalGradient(listOf(tk.primary, Palette.goldDeep)), topLeft = Offset(i * gap + gap * 0.3f, (size.height - hgt) / 2), size = Size(gap * 0.4f, hgt), cornerRadius = CornerRadius(gap))
        }
    }
}
