package com.usman.miqaat.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.platform.testTag
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
import com.usman.miqaat.data.UrduContent
import com.usman.miqaat.data.Duas
import com.usman.miqaat.data.L10n
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AzaanScreen(phase: Phase, onStop: () -> Unit, onSkip: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val portrait = maxHeight > maxWidth
        val u = if (portrait) maxWidth / 62 else minOf(maxWidth / 100, maxHeight / 56)
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
            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                FlowHeader(phase, u, stacked = portrait)
                StepsBar(phase, u)
                Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
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

/** One measured header: status and clock reserve their own space before the step line. */
@Composable
private fun FlowHeader(phase: Phase, u: Dp, stacked: Boolean) {
    val tk = screenTokens()
    val st = com.usman.miqaat.MiqaatApp.instance.settings.value
    var tm by remember { mutableStateOf(java.tim…25182 tokens truncated…      Learn.Posture.SITTING -> {
                robe(0.28f, 0.66f, 0.74f, 0.86f, 0.07f)                                                // folded legs
                poly(0.40f to 0.32f, 0.54f to 0.32f, 0.58f to 0.68f, 0.36f to 0.68f)                   // upright torso
                head(0.47f, 0.21f); foot(0.75f, 0.87f)
                limb(0.52f to 0.40f, 0.60f to 0.55f, 0.62f to 0.69f); hand(0.62f, 0.70f)             // hand resting on the thigh
            }
            Learn.Posture.TASHAHHUD -> {
                robe(0.28f, 0.66f, 0.74f, 0.86f, 0.07f)
                poly(0.40f to 0.32f, 0.54f to 0.32f, 0.58f to 0.68f, 0.36f to 0.68f)
                head(0.47f, 0.21f); foot(0.75f, 0.87f)
                limb(0.45f to 0.42f, 0.40f to 0.58f, 0.44f to 0.69f); hand(0.44f, 0.70f)             // left hand on the left thigh
                limb(0.54f to 0.40f, 0.64f to 0.52f, 0.66f to 0.62f); hand(0.66f, 0.635f)             // right hand on the right thigh…
                drawLine(color, P(0.67f, 0.61f), P(0.74f, 0.50f), sw * 1.8f, cap = StrokeCap.Round)     // …index finger raised
            }
            Learn.Posture.SALAM -> {
                robe(0.28f, 0.66f, 0.74f, 0.86f, 0.07f)
                poly(0.40f to 0.32f, 0.54f to 0.32f, 0.58f to 0.68f, 0.36f to 0.68f)
                head(0.50f, 0.21f); foot(0.75f, 0.87f)
                poly(0.585f to 0.20f, 0.625f to 0.225f, 0.585f to 0.245f, closed = true)                // nose: the face is turned
                limb(0.52f to 0.40f, 0.60f to 0.55f, 0.62f to 0.69f); hand(0.62f, 0.70f)
                drawArc(color, 200f, 120f, false, topLeft = P(0.26f, 0.04f), size = androidx.compose.ui.geometry.Size(w * 0.48f, h * 0.30f), style = Stroke(sw, cap = StrokeCap.Round))   // turn to the right, then the left
                drawLine(color, P(0.30f, 0.17f), P(0.32f, 0.12f), sw, cap = StrokeCap.Round); drawLine(color, P(0.70f, 0.17f), P(0.68f, 0.12f), sw, cap = StrokeCap.Round)
            }
        }
    }
}

// ---------------------------------------------------------------- words + audio

/**
 * Audio for a step. Prefers a bundled human recording res/raw/learn_NN.mp3 (learn_NN_slow.mp3 when Slow is on and it exists);
 * falls back to the device's text-to-speech only when no recording is bundled. State (playing / slow / source) is visible to the UI.
 */
private class Speaker(private val ctx: android.content.Context) {
    var playing by mutableStateOf(false)
    var slow by mutableStateOf(false)
    var arabicOk by mutableStateOf(false)
    var lastWasRecording by mutableStateOf(false)
    var ready by mutableStateOf(false)
    var failed by mutableStateOf(false)
    private var player: android.media.MediaPlayer? = null
    private val tts: TextToSpeech = TextToSpeech(ctx) { status ->
        ready = status == TextToSpeech.SUCCESS
        failed = !ready
        if (ready) runCatching { arabicOk = engine().isLanguageAvailable(Locale("ar")) >= TextToSpeech.LANG_AVAILABLE }
    }
    private fun engine() = tts
    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) { playing = true }
            override fun onDone(id: String?) { playing = false }
            @Deprecated("Deprecated in Java") override fun onError(id: String?) { playing = false }
        })
    }
    private fun recordingId(index: Int): Int {
        val base = "learn_%02d".format(index + 1)
        val slowId = if (slow) ctx.resources.getIdentifier(base + "_slow", "raw", ctx.packageName) else 0
        return if (slowId != 0) slowId else ctx.resources.getIdentifier(base, "raw", ctx.packageName)
    }
    fun hasRecording(index: Int) = recordingId(index) != 0

    fun speak(step: Adhkar.Step, index: Int) {
        stop()
        val id = recordingId(index)
        if (id != 0) {
            lastWasRecording = true
            player = android.media.MediaPlayer().apply {
                setAudioAttributes(android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_MEDIA).setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH).build())
                runCatching { setDataSource(ctx, android.net.Uri.parse("android.resource://${ctx.packageName}/$id")); prepare() }
                    .onFailure { release(); player = null; return }
                if (slow && ctx.resources.getIdentifier("learn_%02d_slow".format(index + 1), "raw", ctx.packageName) == 0)
                    runCatching { playbackParams = playbackParams.setSpeed(0.75f) }   // no slow take: slow the normal one
                setOnCompletionListener { playing = false; it.release(); if (player === it) player = null }
                setOnErrorListener { mp, _, _ -> playing = false; mp.release(); if (player === mp) player = null; true }
                start(); playing = true
            }
            return
        }
        lastWasRecording = false
        if (!ready) return
        tts.language = if (arabicOk) Locale("ar") else Locale.ENGLISH
        tts.setSpeechRate(if (slow) 0.6f else 0.85f)
        val text = if (arabicOk) step.arabic.replace("۝", "،").replace("·", "،") else step.transliteration
        playing = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "learn") == TextToSpeech.SUCCESS
    }
    fun stop() { tts.stop(); player?.runCatching { if (isPlaying) stop(); release() }; player = null; playing = false }
    fun release() { stop(); tts.shutdown() }
    /** Audio is usable when a recording exists or the voice engine is up. */
    fun usable(index: Int) = hasRecording(index) || ready
    fun source(index: Int): String = when {
        hasRecording(index) -> Str[R.string.s_recited_by_named]
        failed -> Str[R.string.s_voice_unavailable]
        !ready -> Str[R.string.s_preparing_voice]
        arabicOk -> Str[R.string.s_device_text_to_speech_arabic_voice]
        else -> Str[R.string.s_device_text_to_speech_no_arabic]
    }
}

@Composable
private fun rememberSpeaker(): Speaker {
    val ctx = LocalContext.current
    val sp = remember { Speaker(ctx.applicationContext) }
    DisposableEffect(Unit) { onDispose { sp.release() } }
    return sp
}

@Composable
private fun WordsCard(c: LearnColors, step: Adhkar.Step, audio: Speaker, modifier: Modifier, index: Int = Learn.words.indexOfFirst { it.arabic == step.arabic }.coerceAtLeast(0)) {
    var showTranslit by rememberSaveable { mutableStateOf(true) }
    var showNote by rememberSaveable(step.position) { mutableStateOf(false) }
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(c.surface).border(1.dp, c.divider, RoundedCornerShape(20.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (L10n.uiUrdu) UrduContent.position(step) else if (c.kiswah) step.position.uppercase() else step.position, fontFamily = c.display, fontSize = if (c.kiswah) 12.sp else 20.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.primary, modifier = Modifier.semantics { heading() })
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(step.arabic, fontFamily = c.arabic, fontSize = if (step.arabic.length > 160) 24.sp else 32.sp, lineHeight = if (step.arabic.length > 160) 42.sp else 54.sp, color = screenTokens().arabicText, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
        }
        // audio: play / stop, slow toggle, disclosed source
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val usable = audio.usable(index)
            Box(Modifier.size(48.dp).clip(CircleShape).background(if (usable) c.primary else c.primary.copy(alpha = 0.35f)).clickable(enabled = usable, role = Role.Button) { if (audio.playing) audio.stop() else audio.speak(step, index) }
                .semantics { contentDescription = if (audio.playing) Str[R.string.s_stop] else Str[R.string.s_hear_it]; stateDescription = if (!usable) audio.source(index) else if (audio.playing) Str[R.string.s_playing] else Str[R.string.s_not_playing] }, contentAlignment = Alignment.Center) {
                Icon(if (audio.playing) Icons.Outlined.Stop else Icons.AutoMirrored.Outlined.VolumeUp, null, Modifier.size(24.dp), tint = c.onPrimary)
            }
            Box(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).border(1.dp, if (audio.slow) c.primary else c.divider, RoundedCornerShape(50))
                .selectable(selected = audio.slow, role = Role.Checkbox) { audio.slow = !audio.slow }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                Text(Str[R.string.s_slow], fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (audio.slow) c.primary else c.textSecondary)
            }
            Text(if (audio.playing) Str[R.string.s_playing] else audio.source(index), fontFamily = Nunito, fontSize = 11.sp, lineHeight = 15.sp, color = c.textSecondary, modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite })
        }
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Switch) { showTranslit = !showTranslit }.semantics { stateDescription = Str[if (showTranslit) R.string.s_show else R.string.s_hide] }, verticalAlignment = Alignment.CenterVertically) {
            Text(Str[R.string.s_how_to_say_it], fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = c.textSecondary, modifier = Modifier.weight(1f))
            Text(Str[if (showTranslit) R.string.s_hide else R.string.s_show], fontFamily = Nunito, fontSize = 12.sp, color = c.textSecondary)
        }
        AnimatedVisibility(showTranslit) { Text(step.transliteration, fontFamily = Cormorant, fontSize = 20.sp, lineHeight = 27.sp, color = c.text) }
        Text(Str[R.string.s_meaning], fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = c.textSecondary)
        Text(if (L10n.uiUrdu) UrduContent.stepMeanings[Adhkar.salah.indexOfFirst { it.arabic == step.arabic }.coerceAtLeast(0)] else step.meaning, fontFamily = Nunito, fontSize = 15.sp, lineHeight = 22.sp, color = c.text)
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) { showNote = !showNote }.semantics { stateDescription = Str[if (showNote) R.string.s_expanded else R.string.s_collapsed] }, verticalAlignment = Alignment.CenterVertically) {
            Text(Str[R.string.s_note_schools_and_source], fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ExpandMore, null, tint = c.textSecondary)
        }
        AnimatedVisibility(showNote) { Text(step.note, fontFamily = Nunito, fontSize = 13.sp, lineHeight = 19.sp, color = c.textSecondary) }
    }
}

@Composable
private fun BottomBar(c: LearnColors, canBack: Boolean, last: Boolean, nextLabel: String, onBack: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(c.surfaceRaised.copy(alpha = 0.92f)).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(CircleShape).border(1.dp, if (canBack) c.divider else c.divider.copy(alpha = 0.3f), CircleShape).clickable(enabled = canBack, role = Role.Button, onClick = onBack).semantics { contentDescription = Str[R.string.s_previous_step] }, contentAlignment = Alignment.Center) {
            Text("‹", fontSize = 26.sp, color = if (canBack) c.text else c.textSecondary.copy(alpha = 0.4f))
        }
        Box(Modifier.weight(1f).heightIn(min = 52.dp).clip(RoundedCornerShape(50)).background(if (last) c.success else c.primary).clickable(role = Role.Button, onClick = onNext).padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
            Text(nextLabel, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = c.onPrimary, maxLines = 2, textAlign = TextAlign.Center)
        }
    }
}

// ---------------------------------------------------------------- practice modes

@Composable
private fun WordsView(c: LearnColors, onBack: () -> Unit) {
    val words = Learn.words
    var i by rememberSaveable { mutableIntStateOf(0) }
    val audio = rememberSpeaker()
    Column(Modifier.fillMaxSize()) {
        TopBar(c, Str[R.string.s_the_words], onBack) { Text("${i + 1} / ${words.size}", fontFamily = Nunito, fontSize = 13.sp, color = c.textSecondary) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) { WordsCard(c, words[i], audio, Modifier.fillMaxWidth()) }
        BottomBar(c, canBack = i > 0, last = i == words.lastIndex, nextLabel = if (i < words.lastIndex) "Next · ${words[i + 1].position}" else Str[R.string.s_back_to_learn_salah],
            onBack = { audio.stop(); i-- }, onNext = { audio.stop(); if (i < words.lastIndex) i++ else onBack() })
    }
}

@Composable
private fun MovesView(c: LearnColors, onBack: () -> Unit) {
    val said = mapOf(
        Learn.Posture.TAKBIR to "Allāhu akbar", Learn.Posture.TASHAHHUD to "At-taḥiyyātu lillāhi waṣ-ṣalawātu waṭ-ṭayyibāt…",
        Learn.Posture.STANDING to Str[R.string.s_takb_r_the_opening_al_f], Learn.Posture.BOWING to Str[R.string.s_sub_na_rabbiya_l_a_m],
        Learn.Posture.RISING to Str[R.string.s_sami_a_ll_hu_liman_amidah], Learn.Posture.PROSTRATING to Str[R.string.s_sub_na_rabbiya_l_a_l],
        Learn.Posture.SITTING to Str[R.string.s_rabbi_ghfir_l_between_the_prostrations], Learn.Posture.SALAM to Str[R.string.s_as_sal_mu_alaykum_wa_ra])
    Column(Modifier.fillMaxSize()) {
        TopBar(c, Str[R.string.s_the_movements], onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Learn.Posture.entries.forEach { p ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface).border(1.dp, c.divider, RoundedCornerShape(16.dp)).padding(14.dp).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
                    Figure(p, c.primary, Modifier.size(84.dp).semantics { contentDescription = p.describe })
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(if (c.kiswah) p.label.uppercase() else p.label, fontFamily = c.display, fontSize = if (c.kiswah) 12.sp else 19.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text)
                        Text(p.describe, fontFamily = Nunito, fontSize = 13.sp, lineHeight = 18.sp, color = c.textSecondary)
                        Text(said.getValue(p), fontFamily = Cormorant, fontSize = 16.sp, lineHeight = 21.sp, color = c.primary, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
