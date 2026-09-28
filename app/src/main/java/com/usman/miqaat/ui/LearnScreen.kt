package com.usman.miqaat.ui

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.usman.miqaat.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.Adhkar
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.Learn
import java.util.Locale

/** Semantic surfaces for secondary screens, derived from the chosen theme so they are one product with the home screen. */
data class LearnColors(
    val background: Brush, val surface: Color, val surfaceRaised: Color, val primary: Color, val onPrimary: Color,
    val text: Color, val textSecondary: Color, val divider: Color, val success: Color, val display: FontFamily, val arabic: FontFamily, val kiswah: Boolean
)

@Composable
fun learnColors(settings: AppSettings): LearnColors = if (settings.theme == AppTheme.KISWAH) LearnColors(
    background = Brush.verticalGradient(listOf(Color(0xFF0B0B0B), Kiswah.silk)), surface = Color(0xFF121212), surfaceRaised = Color(0xFF1A1814),
    primary = Kiswah.thread, onPrimary = Color(0xFF0B0B0B), text = Kiswah.ivory, textSecondary = Kiswah.threadSoft.copy(alpha = 0.85f), divider = Kiswah.thread.copy(alpha = 0.35f),
    success = Palette.mint, display = Cinzel, arabic = ReemKufi, kiswah = true
) else LearnColors(
    background = Brush.verticalGradient(listOf(Palette.night, Palette.panel)), surface = Color(0xFF141C3D), surfaceRaised = Palette.panelRaised,
    primary = Palette.gold, onPrimary = Palette.night, text = Palette.ivory, textSecondary = Palette.textSecondary, divider = Palette.line,
    success = Palette.mint, display = Cormorant, arabic = Amiri, kiswah = false
)

private enum class Mode { LIBRARY, LESSON, WORDS, MOVES }

/** Learn Salah — for beginners of any age. Library → guided prayer (rakʿah by rakʿah), words practice, or movement review. */
@Composable
fun LearnScreen(settings: AppSettings, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val c = learnColors(settings)
    var mode by rememberSaveable { mutableStateOf(Mode.LIBRARY) }
    var lesson by rememberSaveable { mutableStateOf(Learn.Lesson.FAJR) }
    var progressTick by remember { mutableIntStateOf(0) }
    val progress = remember(progressTick, mode) { Learn.progress(ctx) }

    Box(Modifier.fillMaxSize().background(c.background)) {
        if (c.kiswah) Weave(Modifier.fillMaxSize()) else GirihLattice(Modifier.fillMaxSize(), alpha = 0.035f, tile = 120f)
        when (mode) {
            Mode.LIBRARY -> Library(c, progress,
                onLesson = { l -> lesson = l; mode = Mode.LESSON },
                onWords = { mode = Mode.WORDS }, onMoves = { mode = Mode.MOVES }, onBack = onBack, onReset = { Learn.clear(ctx); progressTick++ })
            Mode.LESSON -> LessonView(c, lesson, startAt = if (progress.lesson == lesson) progress.index else 0,
                onExit = { progressTick++; mode = Mode.LIBRARY }, onDone = { Learn.complete(ctx, lesson); progressTick++; mode = Mode.LIBRARY })
            Mode.WORDS -> WordsView(c) { mode = Mode.LIBRARY }
            Mode.MOVES -> MovesView(c) { mode = Mode.LIBRARY }
        }
    }
}

// ---------------------------------------------------------------- library

@Composable
private fun TopBar(c: LearnColors, title: String, onBack: () -> Unit, trailing: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 4.dp, end = 12.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = c.text) }
        Text(if (c.kiswah) title.uppercase() else title, fontFamily = c.display, fontSize = if (c.kiswah) 18.sp else 28.sp, letterSpacing = if (c.kiswah) 3.sp else 0.sp, color = c.text, modifier = Modifier.weight(1f).semantics { heading() })
        trailing()
    }
}

@Composable
private fun Library(c: LearnColors, p: Learn.Progress, onLesson: (Learn.Lesson) -> Unit, onWords: () -> Unit, onMoves: () -> Unit, onBack: () -> Unit, onReset: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopBar(c, Str[R.string.s_learn_salah], onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("الصَّلَاة", fontFamily = c.arabic, fontSize = 44.sp, color = c.primary, modifier = Modifier.semantics { contentDescription = Str[R.string.s_as_salah_the_prayer] })
            Text(Str[R.string.s_one_common_form_of_the_prayer], fontFamily = Nunito, fontSize = 14.sp, lineHeight = 20.sp, color = c.textSecondary)
            if (p.lesson != null) LibraryCard(c, Str.get(R.string.s_continue_x, Str[p.lesson.titleRes]), Str.get(R.string.s_resume_at_step, p.index + 1, Learn.actions(p.lesson).size), primary = true) { onLesson(p.lesson) }
            Text(Str[R.string.s_learn_a_complete_prayer], fontFamily = c.display, fontSize = if (c.kiswah) 13.sp else 22.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            Learn.Lesson.entries.forEach { l ->
                LibraryCard(c, Str[l.titleRes], Str[l.subtitleRes] + " · " + Str.get(R.string.s_n_steps, Learn.actions(l).size), done = l in p.completed) { onLesson(l) }
            }
            Text(Str[R.string.s_practise], fontFamily = c.display, fontSize = if (c.kiswah) 13.sp else 22.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            LibraryCard(c, Str[R.string.s_the_words], Str[R.string.s_the_twelve_texts_of_the_prayer], onClick = onWords)
            LibraryCard(c, Str[R.string.s_the_movements], Str[R.string.s_six_positions_what_each_looks_like], onClick = onMoves)
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val allRecorded = remember { (1..12).all { ctx.resources.getIdentifier("learn_%02d".format(it), "raw", ctx.packageName) != 0 } }
            Text(Str[if (allRecorded) R.string.s_recordings_bundled else R.string.s_audio_is_the_device_s_own], fontFamily = Nunito, fontSize = 12.sp, lineHeight = 17.sp, color = c.textSecondary, modifier = Modifier.padding(top = 10.dp))
            if (p.completed.isNotEmpty() || p.lesson != null) Text(Str[R.string.s_reset_progress], fontFamily = Nunito, fontSize = 13.sp, color = c.textSecondary, modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onReset).padding(vertical = 14.dp))
        }
    }
}

@Composable
private fun LibraryCard(c: LearnColors, title: String, subtitle: String, primary: Boolean = false, done: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).clip(RoundedCornerShape(16.dp)).background(if (primary) c.primary.copy(alpha = 0.16f) else c.surface)
            .border(1.dp, if (primary) c.primary.copy(alpha = 0.6f) else c.divider, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp).semantics(mergeDescendants = true) { if (done) stateDescription = "Completed" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = Nunito, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = if (primary) c.primary else c.text)
            Text(subtitle, fontFamily = Nunito, fontSize = 13.sp, color = c.textSecondary, lineHeight = 18.sp)
        }
        if (done) Icon(Icons.Outlined.Check, null, Modifier.size(20.dp), tint = c.success) else Text("›", fontSize = 22.sp, color = c.textSecondary)
    }
}

// ---------------------------------------------------------------- guided lesson

@Composable
private fun LessonView(c: LearnColors, lesson: Learn.Lesson, startAt: Int, onExit: () -> Unit, onDone: () -> Unit) {
    val ctx = LocalContext.current
    val actions = remember(lesson) { Learn.actions(lesson) }
    var i by rememberSaveable(lesson) { mutableIntStateOf(startAt.coerceIn(0, actions.lastIndex)) }
    val a = actions[i]
    LaunchedEffect(i) { Learn.save(ctx, lesson, i) }
    val audio = rememberSpeaker()
    val rakahSteps = actions.count { it.rakah == a.rakah }; val rakahPos = actions.take(i + 1).count { it.rakah == a.rakah }
    val where = "${lesson.title} · rakʿah ${a.rakah} of ${lesson.rakat} · step $rakahPos of $rakahSteps · ${a.step.position}"

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth > 720.dp
        Column(Modifier.fillMaxSize()) {
            TopBar(c, Str[lesson.titleRes], onExit) { Text("${i + 1} / ${actions.size}", fontFamily = Nunito, fontSize = 13.sp, color = c.textSecondary) }
            RakahMap(c, lesson, actions, i, Modifier.padding(horizontal = 20.dp).semantics { contentDescription = where; liveRegion = LiveRegionMode.Polite })
            val figure: @Composable (Modifier) -> Unit = { m -> PostureCard(c, a.posture, a.cue, m) }
            val words: @Composable (Modifier) -> Unit = { m -> WordsCard(c, a.step, audio, m) }
            if (wide) Row(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                figure(Modifier.weight(0.42f).fillMaxHeight()); words(Modifier.weight(0.58f).fillMaxHeight().verticalScroll(rememberScrollState()))
            } else Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                figure(Modifier.fillMaxWidth().height(180.dp * androidx.compose.ui.platform.LocalDensity.current.fontScale.coerceIn(1f, 1.5f))); words(Modifier.fillMaxWidth())
            }
            BottomBar(c, canBack = i > 0, last = i == actions.lastIndex, nextLabel = if (i < actions.lastIndex) "Continue · ${actions[i + 1].step.position.let { if (it.length > 22) actions[i + 1].posture.label.substringBefore(" ·") else it }}" else Str[R.string.s_finish_well_done],
                onBack = { audio.stop(); i-- }, onNext = { audio.stop(); if (i < actions.lastIndex) i++ else onDone() })
        }
    }
}

/** Segmented progress: one segment per rakʿah, filled by steps; the current one is bright. */
@Composable
private fun RakahMap(c: LearnColors, lesson: Learn.Lesson, actions: List<Learn.Action>, i: Int, modifier: Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (r in 1..lesson.rakat) {
                val steps = actions.withIndex().filter { it.value.rakah == r }
                val done = steps.count { it.index <= i }.toFloat() / steps.size
                Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.divider)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(done).background(if (actions[i].rakah == r) c.primary else c.primary.copy(alpha = 0.6f)))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (r in 1..lesson.rakat) Text("Rakʿah $r".let { if (c.kiswah) it.uppercase() else it }, fontFamily = if (c.kiswah) Cinzel else Nunito, fontSize = 11.sp, letterSpacing = if (c.kiswah) 1.5.sp else 0.5.sp,
                color = if (actions[i].rakah == r) c.text else c.textSecondary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PostureCard(c: LearnColors, posture: Learn.Posture, cue: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(c.surface).border(1.dp, c.divider, RoundedCornerShape(20.dp)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (!c.kiswah) MihrabArch(Modifier.fillMaxHeight().aspectRatio(0.9f), color = c.primary.copy(alpha = 0.35f))
            Figure(posture, c.primary, Modifier.fillMaxHeight(0.8f).aspectRatio(1f).semantics { contentDescription = "${posture.label}: ${posture.describe}" })
        }
        Text(if (c.kiswah) posture.label.uppercase() else posture.label, fontFamily = c.display, fontSize = if (c.kiswah) 12.sp else 20.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text, modifier = Modifier.padding(top = 8.dp))
        Text(cue, fontFamily = Nunito, fontSize = 13.sp, lineHeight = 18.sp, color = c.textSecondary, textAlign = TextAlign.Center)
    }
}

/** A calm, gender-neutral silhouette for each position, drawn with round strokes; no emoji. */
@Composable
fun Figure(p: Learn.Posture, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val sw = w * 0.06f
        val stroke = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun P(x: Float, y: Float) = Offset(w * x, h * y)
        fun line(pts: List<Offset>) { val path = Path(); pts.forEachIndexed { k, o -> if (k == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y) }; drawPath(path, color, style = stroke) }
        val headR = w * 0.075f
        // ground line
        drawLine(color.copy(alpha = 0.35f), P(0.08f, 0.92f), P(0.92f, 0.92f), sw * 0.5f, cap = StrokeCap.Round)
        when (p) {
            Learn.Posture.STANDING -> {
                drawCircle(color, headR, P(0.5f, 0.16f))
                line(listOf(P(0.5f, 0.24f), P(0.5f, 0.58f)))                       // torso
                line(listOf(P(0.5f, 0.58f), P(0.44f, 0.9f))); line(listOf(P(0.5f, 0.58f), P(0.56f, 0.9f)))
                line(listOf(P(0.5f, 0.30f), P(0.40f, 0.42f), P(0.52f, 0.44f)))      // folded arms
                line(listOf(P(0.5f, 0.30f), P(0.60f, 0.42f), P(0.48f, 0.44f)))
            }
            Learn.Posture.RISING -> {
                drawCircle(color, headR, P(0.5f, 0.16f))
                line(listOf(P(0.5f, 0.24f), P(0.5f, 0.58f)))
                line(listOf(P(0.5f, 0.58f), P(0.44f, 0.9f))); line(listOf(P(0.5f, 0.58f), P(0.56f, 0.9f)))
                line(listOf(P(0.5f, 0.30f), P(0.42f, 0.56f))); line(listOf(P(0.5f, 0.30f), P(0.58f, 0.56f)))   // arms at the sides
            }
            Learn.Posture.BOWING -> {
                drawCircle(color, headR, P(0.24f, 0.46f))
                line(listOf(P(0.32f, 0.47f), P(0.62f, 0.47f)))                      // level back
                line(listOf(P(0.62f, 0.47f), P(0.60f, 0.9f)))                       // legs
                line(listOf(P(0.36f, 0.48f), P(0.56f, 0.68f)))                      // arm to knee
                drawCircle(color, sw * 0.6f, P(0.57f, 0.69f))
            }
            Learn.Posture.PROSTRATING -> {
                drawCircle(color, headR, P(0.2f, 0.84f))
                line(listOf(P(0.28f, 0.80f), P(0.5f, 0.62f), P(0.62f, 0.66f)))      // back rising to the hips
                line(listOf(P(0.62f, 0.66f), P(0.66f, 0.88f), P(0.82f, 0.88f)))     // shins and feet
                line(listOf(P(0.32f, 0.78f), P(0.34f, 0.9f)))                       // arm down to the palm
                line(listOf(P(0.28f, 0.9f), P(0.4f, 0.9f)))                         // palm
            }
            Learn.Posture.SITTING -> {
                drawCircle(color, headR, P(0.46f, 0.36f))
                line(listOf(P(0.46f, 0.44f), P(0.46f, 0.72f)))                      // torso
                line(listOf(P(0.46f, 0.72f), P(0.72f, 0.72f), P(0.76f, 0.88f)))     // thigh and foot
                line(listOf(P(0.3f, 0.88f), P(0.76f, 0.88f)))                       // folded legs
                line(listOf(P(0.46f, 0.50f), P(0.6f, 0.66f)))                       // hand on thigh
            }
            Learn.Posture.SALAM -> {
                drawCircle(color, headR, P(0.5f, 0.36f))
                drawArc(color, -40f, 80f, false, topLeft = P(0.58f, 0.28f), size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.16f), style = Stroke(sw * 0.6f, cap = StrokeCap.Round))  // turn cue
                line(listOf(P(0.5f, 0.44f), P(0.5f, 0.72f)))
                line(listOf(P(0.5f, 0.72f), P(0.76f, 0.72f), P(0.8f, 0.88f)))
                line(listOf(P(0.34f, 0.88f), P(0.8f, 0.88f)))
                line(listOf(P(0.5f, 0.50f), P(0.64f, 0.66f)))
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
        Text(if (c.kiswah) step.position.uppercase() else step.position, fontFamily = c.display, fontSize = if (c.kiswah) 12.sp else 20.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.primary, modifier = Modifier.semantics { heading() })
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(step.arabic, fontFamily = c.arabic, fontSize = if (step.arabic.length > 160) 24.sp else 32.sp, lineHeight = if (step.arabic.length > 160) 42.sp else 54.sp, color = Color(0xFFF6E7B8), textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
        }
        // audio: play / stop, slow toggle, disclosed source
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val usable = audio.usable(index)
            Box(Modifier.size(48.dp).clip(CircleShape).background(if (usable) c.primary else c.primary.copy(alpha = 0.35f)).clickable(enabled = usable, role = Role.Button) { if (audio.playing) audio.stop() else audio.speak(step, index) }
                .semantics { contentDescription = if (audio.playing) Str[R.string.s_stop] else Str[R.string.s_hear_it]; stateDescription = if (!usable) audio.source(index) else if (audio.playing) "Playing" else Str[R.string.s_not_playing] }, contentAlignment = Alignment.Center) {
                Icon(if (audio.playing) Icons.Outlined.Stop else Icons.AutoMirrored.Outlined.VolumeUp, null, Modifier.size(24.dp), tint = c.onPrimary)
            }
            Box(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).border(1.dp, if (audio.slow) c.primary else c.divider, RoundedCornerShape(50))
                .selectable(selected = audio.slow, role = Role.Checkbox) { audio.slow = !audio.slow }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                Text(Str[R.string.s_slow], fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (audio.slow) c.primary else c.textSecondary)
            }
            Text(if (audio.playing) Str[R.string.s_playing] else audio.source(index), fontFamily = Nunito, fontSize = 11.sp, lineHeight = 15.sp, color = c.textSecondary, modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite })
        }
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Switch) { showTranslit = !showTranslit }.semantics { stateDescription = if (showTranslit) "Shown" else "Hidden" }, verticalAlignment = Alignment.CenterVertically) {
            Text(Str[R.string.s_how_to_say_it], fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = c.textSecondary, modifier = Modifier.weight(1f))
            Text(Str[if (showTranslit) R.string.s_hide else R.string.s_show], fontFamily = Nunito, fontSize = 12.sp, color = c.textSecondary)
        }
        AnimatedVisibility(showTranslit) { Text(step.transliteration, fontFamily = Cormorant, fontSize = 20.sp, lineHeight = 27.sp, color = c.text) }
        Text(Str[R.string.s_meaning], fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = c.textSecondary)
        Text(step.meaning, fontFamily = Nunito, fontSize = 15.sp, lineHeight = 22.sp, color = c.text)
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) { showNote = !showNote }.semantics { stateDescription = if (showNote) "Expanded" else "Collapsed" }, verticalAlignment = Alignment.CenterVertically) {
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
