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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        val kiswah = com.usman.miqaat.MiqaatApp.instance.settings.value.theme == com.usman.miqaat.data.AppTheme.KISWAH
        val bg = when {
            kiswah -> listOf(Color(0xFF0B0B0B), Kiswah.silk)
            isAzaan -> listOf(Color(0xFF2A1440), Color(0xFF0A0716))
            isIq -> listOf(Color(0xFF163A3A), Color(0xFF0B1F24), Color(0xFF06131A))
            else -> listOf(Color(0xFF1E2A5C), Color(0xFF0D1533), Color(0xFF080D24))
        }
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(bg))) {
            if (kiswah) Weave(Modifier.fillMaxSize()) else GirihLattice(Modifier.fillMaxSize(), tile = u.value * 11f, alpha = 0.12f)
            // Option A: a quiet status tag, top-left, instead of any system pop-up
            Row(Modifier.statusBarsPadding().padding(start = u * 3.6f, top = u * 2.6f), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(u * 0.9f).clip(androidx.compose.foundation.shape.CircleShape).background(Palette.gold))
                Spacer(Modifier.width(u * 0.9f))
                Text(
                    when (phase) {
                        is Phase.Azaan -> "${phase.prayer.english} azaan · playing"
                        is Phase.IqamahCountdown, is Phase.IqamahNow -> "${phase.prayer.english} · iqamah"
                        else -> "${phase.prayer.english} · after the azaan"
                    }.uppercase(),
                    fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, letterSpacing = (u.value * 0.16f).sp, fontWeight = FontWeight.Bold, color = Palette.goldSoft.copy(alpha = 0.85f)
                )
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
    val iftar = phase is Phase.Iftar
    val iq = phase is Phase.IqamahCountdown || phase is Phase.IqamahNow
    val idx = when (phase) { is Phase.Azaan -> 0; is Phase.Iftar -> 1; is Phase.Dua -> if (iftar) 2 else 1; is Phase.HadithPhase -> if (iftar) 3 else 2; is Phase.IqamahCountdown -> 1; is Phase.IqamahNow -> 2; is Phase.Quiet -> 3 }
    val labels = when {
        iq -> listOf("Azaan", "Iqamah countdown", "Iqamah", "Prayer")
        iftar -> listOf("Azaan", "Iftar dua", "Dua after azaan", "Hadith", "Home")
        else -> listOf("Azaan", "Dua after azaan", "Hadith", "Home")
    }
    @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
    androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth().padding(top = u * 2.4f), horizontalArrangement = Arrangement.spacedBy(u * 1, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(u * 0.6f)) {
        labels.forEachIndexed { i, l ->
            val done = i < idx; val cur = i == idx
            val shape = RoundedCornerShape(50)
            Box(
                Modifier.clip(shape).background(if (cur) Palette.gold.copy(alpha = 0.12f) else Color.Transparent)
                    .border(1.dp, when { cur -> Palette.gold; done -> Palette.mint.copy(alpha = 0.4f); else -> Color.White.copy(alpha = 0.15f) }, shape)
                    .padding(horizontal = u * 1.4f, vertical = u * 0.6f)
            ) {
                Text((if (done) "✓ " else "") + l.uppercase(), fontFamily = Nunito, fontSize = (u.value * 1.25f).sp, letterSpacing = (u.value * 0.15f).sp, fontWeight = FontWeight.Bold,
                    color = when { cur -> Color(0xFFF6E7B8); done -> Palette.mint; else -> Palette.ivory.copy(alpha = 0.55f) })
            }
        }
    }
}

@Composable
private fun AzaanBody(phase: Phase, u: Dp) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("ٱللَّٰهُ أَكْبَرُ", fontFamily = Amiri, fontSize = (u.value * 12f).sp, lineHeight = (u.value * 14f).sp, color = Color(0xFFF6E7B8), textAlign = TextAlign.Center)
        Text("${phase.prayer.english.uppercase()} AZAAN  ·  ${phase.prayer.arabic}", fontFamily = Cormorant, fontSize = (u.value * 2.6f).sp, letterSpacing = (u.value * 0.6f).sp, color = Palette.ivory.copy(alpha = 0.85f), modifier = Modifier.padding(top = u * 1))
        Wave(Modifier.padding(top = u * 4).width(u * 22).height(u * 8))
        Text("Hayya ʿalaṣ-ṣalāh · Come to prayer", fontFamily = Nunito, fontSize = (u.value * 1.6f).sp, letterSpacing = (u.value * 0.1f).sp, color = Palette.ivory.copy(alpha = 0.7f), modifier = Modifier.padding(top = u * 3))
    }
}

@Composable
private fun DuaBody(u: Dp) {
    Column(Modifier.fillMaxSize().padding(horizontal = u * 9), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker("Dua after the azaan", u)
        Arabic(Duas.AFTER_AZAAN_AR, u, size = 4.4f)
        Translation(Duas.AFTER_AZAAN_EN, u)
        Source("${Duas.AFTER_AZAAN_SRC}  ·  ${Duas.AFTER_AZAAN_NOTE}", u)
    }
}

@Composable
private fun IftarBody(u: Dp) {
    Column(Modifier.fillMaxSize().padding(horizontal = u * 9), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker("Ramaḍān · dua at iftar", u)
        Arabic(com.usman.miqaat.data.Ramadan.IFTAR_AR, u, size = 4.8f)
        Translation(com.usman.miqaat.data.Ramadan.IFTAR_EN, u)
        Source(com.usman.miqaat.data.Ramadan.IFTAR_SRC, u)
    }
}

@Composable
private fun CountdownBody(p: Phase.IqamahCountdown, u: Dp) {
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
                drawArc(Color.White.copy(alpha = 0.12f), 0f, 360f, false, style = stroke)
                drawArc(Palette.gold, -90f, 360f * (leftMs / total.toFloat()), false, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$secs", fontFamily = Cormorant, fontSize = (u.value * 10.5f).sp, lineHeight = (u.value * 10.5f).sp, color = Palette.ivory)
                Text("SECONDS", fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, letterSpacing = (u.value * 0.35f).sp, fontWeight = FontWeight.Bold, color = Palette.ivory.copy(alpha = 0.7f), modifier = Modifier.padding(top = u * 0.6f))
            }
        }
        Text("سَوُّوا صُفُوفَكُمْ  ·  Straighten your rows", fontFamily = Cormorant, fontSize = (u.value * 2.4f).sp, color = Palette.ivory.copy(alpha = 0.9f))
        Text("Ṣaḥīḥ al-Bukhārī 723", fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, color = Palette.ivory.copy(alpha = 0.6f), modifier = Modifier.padding(top = u * 0.6f))
    }
}

@Composable
private fun IqamahNowBody(phase: Phase, u: Dp) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker(phase.prayer.english, u)
        Text("الإقامة", fontFamily = Amiri, fontSize = (u.value * 9f).sp, lineHeight = (u.value * 10f).sp, color = Color(0xFFF6E7B8))
        Text("قَدْ قَامَتِ الصَّلاَةُ", fontFamily = Amiri, fontSize = (u.value * 4.2f).sp, lineHeight = (u.value * 6f).sp, color = Palette.goldSoft)
        Text("The prayer has begun", fontFamily = Cormorant, fontSize = (u.value * 2.4f).sp, color = Palette.ivory.copy(alpha = 0.9f), modifier = Modifier.padding(top = u * 0.6f))
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
        Text("%d:%02d".format(if (t.hour % 12 == 0) 12 else t.hour % 12, t.minute), fontFamily = Cormorant, fontSize = (u.value * 12f).sp, lineHeight = (u.value * 12f).sp, color = Palette.ivory.copy(alpha = 0.55f))
        Text(p.prayer.arabic, fontFamily = Amiri, fontSize = (u.value * 3.4f).sp, color = Color(0xFFF6E7B8).copy(alpha = 0.5f))
        val left = ((p.endsAt - now).coerceAtLeast(0) / 60_000) + 1
        Text("in prayer · screen wakes in $left min · tap to wake now", fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, letterSpacing = (u.value * 0.08f).sp, color = Palette.ivory.copy(alpha = 0.3f), modifier = Modifier.padding(top = u * 2))
    }
}

@Composable
private fun HadithBody(p: Phase.HadithPhase, u: Dp) {
    Column(Modifier.fillMaxSize().padding(horizontal = u * 9), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Kicker("Hadith of the hour · ${p.prayer.english}", u)
        val long = p.hadith.arabic.length > 110
        Arabic("قَالَ رَسُولُ اللَّهِ ﷺ: " + p.hadith.arabic, u, size = if (long) 3.3f else 4f)
        Translation("The Messenger of Allah ﷺ said: “${p.hadith.english}”", u, size = if (p.hadith.english.length > 160) 2f else 2.35f)
        Source("Narrated by ${p.hadith.narrator}  ·  ${p.hadith.source}", u)
    }
}

@Composable private fun Kicker(t: String, u: Dp) = Text(t.uppercase(), fontFamily = Nunito, fontSize = (u.value * 1.35f).sp, letterSpacing = (u.value * 0.3f).sp, fontWeight = FontWeight.Bold, color = Palette.goldSoft)
@Composable private fun Arabic(t: String, u: Dp, size: Float) = CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Text(t, fontFamily = Amiri, fontSize = (u.value * size).sp, lineHeight = (u.value * size * 1.75f).sp, color = Color(0xFFF6E7B8), textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = u * 1.2f))
}
@Composable private fun Translation(t: String, u: Dp, size: Float = 2.35f) = Text(t, fontFamily = Cormorant, fontSize = (u.value * size).sp, lineHeight = (u.value * size * 1.45f).sp, color = Palette.ivory, textAlign = TextAlign.Center)
@Composable private fun Source(t: String, u: Dp) = Text(t, fontFamily = Nunito, fontSize = (u.value * 1.35f).sp, letterSpacing = (u.value * 0.08f).sp, color = Palette.ivory.copy(alpha = 0.75f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = u * 1.4f))

@Composable
private fun BottomBar(phase: Phase, u: Dp, onStop: () -> Unit, onSkip: () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }
    Row(Modifier.fillMaxWidth().padding(horizontal = u * 3.6f, vertical = u * 2.6f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        // left: narration state
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(u * 1.2f)) {
            val narrating = when (phase) { is Phase.Azaan -> true; is Phase.Dua, is Phase.Iftar -> true; is Phase.HadithPhase -> phase.narrating; is Phase.IqamahNow -> true; else -> false }
            if (narrating) Wave(Modifier.width(u * 6).height(u * 2.4f), bars = 5)
            Text(
                when (phase) { is Phase.Azaan -> "Azaan playing"; is Phase.Iftar -> "Reading the iftar dua"; is Phase.Dua -> "Reading the dua"; is Phase.HadithPhase -> if (phase.narrating) "Reading the hadith" else "Take a moment"; is Phase.IqamahCountdown -> "Tap Skip if the imam is ready"; is Phase.IqamahNow -> "Iqamah"; is Phase.Quiet -> "" },
                fontFamily = Nunito, fontSize = (u.value * 1.5f).sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory
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
                is Phase.Azaan -> { Pill("Stop azaan", true, u.value, onStop); Pill("Skip ›", false, u.value, onSkip) }
                is Phase.IqamahCountdown -> { Pill("Dismiss", false, u.value, onStop); Pill("Start iqamah now ›", true, u.value, onSkip) }
                is Phase.IqamahNow -> Pill("Dismiss", false, u.value, onStop)
                is Phase.HadithPhase -> Pill("Back to clock", false, u.value, onStop)
                else -> { Pill("Back to clock", false, u.value, onStop); Pill("Skip ›", false, u.value, onSkip) }
            }
        }
    }
}

@Composable
private fun Ring(fraction: Float, label: String, u: Dp) {
    Box(Modifier.size(u * 6.4f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 2.5f * (size.width / 40f), cap = StrokeCap.Round)
            drawArc(Color.White.copy(alpha = 0.15f), 0f, 360f, false, style = stroke)
            drawArc(Palette.gold, -90f, 360f * fraction.coerceIn(0f, 1f), false, style = stroke)
        }
        Text(label, fontFamily = Cormorant, fontSize = (u.value * 1.9f).sp, color = Palette.ivory)
    }
}

@Composable
private fun Pill(label: String, primary: Boolean, u: Float, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier.clip(shape).background(if (primary) Palette.gold else Color.Transparent)
            .border(1.dp, if (primary) Palette.gold else Color.White.copy(alpha = 0.25f), shape)
            .clickable(onClick = onClick).padding(horizontal = (u * 2.4f).dp, vertical = (u * 1.1f).dp)
    ) { Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = (u * 1.5f).sp, color = if (primary) Color(0xFF160C2A) else Palette.ivory) }
}

@Composable
private fun Wave(modifier: Modifier, bars: Int = 15) {
    val t by rememberInfiniteTransition(label = "wave").animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart), label = "t")
    Canvas(modifier) {
        val gap = size.width / bars
        for (i in 0 until bars) {
            val phase = (i / bars.toFloat()) * PI * 2
            val hgt = size.height * (0.2f + 0.8f * (0.5f + 0.5f * sin(t * 2 * PI + phase).toFloat()))
            drawRoundRect(Brush.verticalGradient(listOf(Palette.gold, Palette.goldDeep)), topLeft = Offset(i * gap + gap * 0.3f, (size.height - hgt) / 2), size = Size(gap * 0.4f, hgt), cornerRadius = CornerRadius(gap))
        }
    }
}
