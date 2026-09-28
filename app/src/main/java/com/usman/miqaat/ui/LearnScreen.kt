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
    background = Brush.verticalGradient(listOf(Palette.night, Palette.panel)), surface = Color.White.copy(alpha = 0.06f), surfaceRaised = Palette.panelRaised,
    primary = Palette.gold, onPrimary = Palette.night, text = Palette.ivory, textSecondary = Palette.textSecondary, divider = Palette.line,
    success = Palette.mint, display = Cormorant, arabic = Amiri, kiswah = false
)

private enum class Mode { LIBRARY, LESSON, WORDS, MOVES }

/** Learn Salah — for children and adult beginners. Library → guided prayer (rakʿah by rakʿah), words practice, or movement review. */
@Composable
fun LearnScreen(settings: AppSettings, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val c = learnColors(settings)
    var mode by rememberSaveable { mutableStateOf(Mode.LIBRARY) }
    var lesson by rememberSaveable { mutableStateOf(Learn.Lesson.FAJR) }
    var progressTick by remember { mutableIntStateOf(0) }
    val progress = remember(progressTick, mode) { Learn.progress(ctx) }

    Box(Modifier.fillMaxSize().background(c.background)) {
        if (c.kiswah) Weave(Modifier.fillMaxSize()) else GirihLattice(Modifier.fillMaxSize(), alpha = 0.07f, tile = 120f)
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
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = c.text) }
        Text(if (c.kiswah) title.uppercase() else title, fontFamily = c.display, fontSize = if (c.kiswah) 18.sp else 28.sp, letterSpacing = if (c.kiswah) 3.sp else 0.sp, color = c.text, modifier = Modifier.weight(1f).semantics { heading() })
        trailing()
    }
}

@Composable
private fun Library(c: LearnColors, p: Learn.Progress, onLesson: (Learn.Lesson) -> Unit, onWords: () -> Unit, onMoves: () -> Unit, onBack: () -> Unit, onReset: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopBar(c, "Learn Salah", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("الصَّلَاة", fontFamily = c.arabic, fontSize = 44.sp, color = c.primary, modifier = Modifier.semantics { contentDescription = "As-salah, the prayer" })
            Text("One common form of the prayer, one action at a time, with the words in Arabic, how to say them and what they mean. Where the schools differ, the note says so. For children and adult beginners alike.", fontFamily = Nunito, fontSize = 14.sp, lineHeight = 20.sp, color = c.textSecondary)
            if (p.lesson != null) LibraryCard(c, "Continue · ${p.lesson.title}", "Resume at step ${p.index + 1} of ${Learn.actions(p.lesson).size}", primary = true) { onLesson(p.lesson) }
            Text("Learn a complete prayer", fontFamily = c.display, fontSize = if (c.kiswah) 13.sp else 22.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            Learn.Lesson.entries.forEach { l ->
                LibraryCard(c, l.title, l.subtitle + " · ${Learn.actions(l).size} steps", done = l in p.completed) { onLesson(l) }
            }
            Text("Practise", fontFamily = c.display, fontSize = if (c.kiswah) 13.sp else 22.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            LibraryCard(c, "The words", "The twelve texts of the prayer on their own, with replay and slow speed", onClick = onWords)
            LibraryCard(c, "The movements", "Six positions, what each looks like and what is said in it", onClick = onMoves)
            Text("Audio is the device's own text-to-speech, offered as an aid — it is not verified recitation. Learn the words with a teacher or a reciter you trust. Content: ISLAMIC_REVIEW_PACK.md § J7 (unsigned).", fontFamily = Nunito, fontSize = 12.sp, lineHeight = 17.sp, color = c.textSecondary, modifier = Modifier.padding(top = 10.dp))
            if (p.completed.isNotEmpty() || p.lesson != null) Text("Reset progress", fontFamily = Nunito, fontSize = 13.sp, color = c.textSecondary, modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onReset).padding(vertical = 14.dp))
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
            TopBar(c, lesson.title, onExit) { Text("${i + 1} / ${actions.size}", fontFamily = Nunito, fontSize = 13.sp, color = c.textSecondary) }
            RakahMap(c, lesson, actions, i, Modifier.padding(horizontal = 20.dp).semantics { contentDescription = where; liveRegion = LiveRegionMode.Polite })
            val figure: @Composable (Modifier) -> Unit = { m -> PostureCard(c, a.posture, a.cue, m) }
            val words: @Composable (Modifier) -> Unit = { m -> WordsCard(c, a.step, audio, m) }
            if (wide) Row(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                figure(Modifier.weight(0.42f).fillMaxHeight()); words(Modifier.weight(0.58f).fillMaxHeight().verticalScroll(rememberScrollState()))
            } else Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                figure(Modifier.fillMaxWidth().height(180.dp)); words(Modifier.fillMaxWidth())
            }
            BottomBar(c, canBack = i > 0, last = i == actions.lastIndex, nextLabel = if (i < actions.lastIndex) "Continue · ${actions[i + 1].step.position}" else "Finish · well done",
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

/** Device text-to-speech with visible state (playing / slow) and a disclosed source. */
private class Speaker(ctx: android.content.Context) {
    var playing by mutableStateOf(false)
    var slow by mutableStateOf(false)
    var arabicOk by mutableStateOf(false)
    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(ctx) { status ->
        ready = status == TextToSpeech.SUCCESS
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
    fun speak(step: Adhkar.Step) {
        if (!ready) return
        tts.language = if (arabicOk) Locale("ar") else Locale.ENGLISH
        tts.setSpeechRate(if (slow) 0.6f else 0.85f)
        val text = if (arabicOk) step.arabic.replace("۝", "،").replace("·", "،") else step.transliteration
        playing = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "learn") == TextToSpeech.SUCCESS
    }
    fun stop() { tts.stop(); playing = false }
    fun release() { tts.stop(); tts.shutdown() }
    val source: String get() = if (arabicOk) "Device text-to-speech, Arabic voice · an aid, not verified recitation" else "Device text-to-speech (no Arabic voice installed): reads the transliteration"
}

@Composable
private fun rememberSpeaker(): Speaker {
    val ctx = LocalContext.current
    val sp = remember { Speaker(ctx.applicationContext) }
    DisposableEffect(Unit) { onDispose { sp.release() } }
    return sp
}

@Composable
private fun WordsCard(c: LearnColors, step: Adhkar.Step, audio: Speaker, modifier: Modifier) {
    var showTranslit by rememberSaveable { mutableStateOf(true) }
    var showNote by rememberSaveable(step.position) { mutableStateOf(false) }
    Column(modifier.clip(RoundedCornerShape(20.dp)).background(c.surface).border(1.dp, c.divider, RoundedCornerShape(20.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (c.kiswah) step.position.uppercase() else step.position, fontFamily = c.display, fontSize = if (c.kiswah) 12.sp else 20.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.primary, modifier = Modifier.semantics { heading() })
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(step.arabic, fontFamily = c.arabic, fontSize = if (step.arabic.length > 160) 24.sp else 32.sp, lineHeight = if (step.arabic.length > 160) 42.sp else 54.sp, color = Color(0xFFF6E7B8), textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
        }
        // audio: play / stop, slow toggle, disclosed source
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(c.primary).clickable(role = Role.Button) { if (audio.playing) audio.stop() else audio.speak(step) }
                .semantics { contentDescription = if (audio.playing) "Stop" else "Hear it"; stateDescription = if (audio.playing) "Playing" else "Not playing" }, contentAlignment = Alignment.Center) {
                Icon(if (audio.playing) Icons.Outlined.Stop else Icons.AutoMirrored.Outlined.VolumeUp, null, Modifier.size(24.dp), tint = c.onPrimary)
            }
            Box(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).border(1.dp, if (audio.slow) c.primary else c.divider, RoundedCornerShape(50))
                .selectable(selected = audio.slow, role = Role.Checkbox) { audio.slow = !audio.slow }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                Text("Slow", fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (audio.slow) c.primary else c.textSecondary)
            }
            Text(if (audio.playing) "Playing…" else audio.source, fontFamily = Nunito, fontSize = 11.sp, lineHeight = 15.sp, color = c.textSecondary, modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite })
        }
        Row(Modifier.fillMaxWidth().heightIn(min = 44.dp).clickable(role = Role.Switch) { showTranslit = !showTranslit }.semantics { stateDescription = if (showTranslit) "Shown" else "Hidden" }, verticalAlignment = Alignment.CenterVertically) {
            Text("How to say it", fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = c.textSecondary, modifier = Modifier.weight(1f))
            Text(if (showTranslit) "hide" else "show", fontFamily = Nunito, fontSize = 12.sp, color = c.textSecondary)
        }
        AnimatedVisibility(showTranslit) { Text(step.transliteration, fontFamily = Cormorant, fontSize = 20.sp, lineHeight = 27.sp, color = c.text) }
        Text("Meaning", fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = c.textSecondary)
        Text(step.meaning, fontFamily = Nunito, fontSize = 15.sp, lineHeight = 22.sp, color = c.text)
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) { showNote = !showNote }.semantics { stateDescription = if (showNote) "Expanded" else "Collapsed" }, verticalAlignment = Alignment.CenterVertically) {
            Text("Note, schools and source", fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ExpandMore, null, tint = c.textSecondary)
        }
        AnimatedVisibility(showNote) { Text(step.note, fontFamily = Nunito, fontSize = 13.sp, lineHeight = 19.sp, color = c.textSecondary) }
    }
}

@Composable
private fun BottomBar(c: LearnColors, canBack: Boolean, last: Boolean, nextLabel: String, onBack: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(c.surfaceRaised.copy(alpha = 0.92f)).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(CircleShape).border(1.dp, if (canBack) c.divider else c.divider.copy(alpha = 0.3f), CircleShape).clickable(enabled = canBack, role = Role.Button, onClick = onBack).semantics { contentDescription = "Previous step" }, contentAlignment = Alignment.Center) {
            Text("‹", fontSize = 26.sp, color = if (canBack) c.text else c.textSecondary.copy(alpha = 0.4f))
        }
        Box(Modifier.weight(1f).heightIn(min = 52.dp).clip(RoundedCornerShape(50)).background(if (last) c.success else c.primary).clickable(role = Role.Button, onClick = onNext).padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
            Text(nextLabel, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = c.onPrimary, maxLines = 1, textAlign = TextAlign.Center)
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
        TopBar(c, "The words", onBack) { Text("${i + 1} / ${words.size}", fontFamily = Nunito, fontSize = 13.sp, color = c.textSecondary) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) { WordsCard(c, words[i], audio, Modifier.fillMaxWidth()) }
        BottomBar(c, canBack = i > 0, last = i == words.lastIndex, nextLabel = if (i < words.lastIndex) "Next · ${words[i + 1].position}" else "Back to Learn Salah",
            onBack = { audio.stop(); i-- }, onNext = { audio.stop(); if (i < words.lastIndex) i++ else onBack() })
    }
}

@Composable
private fun MovesView(c: LearnColors, onBack: () -> Unit) {
    val said = mapOf(
        Learn.Posture.STANDING to "Takbīr, the opening, al-Fātiḥah and a sūrah", Learn.Posture.BOWING to "Subḥāna rabbiya l-ʿaẓīm ×3",
        Learn.Posture.RISING to "Samiʿa llāhu liman ḥamidah · Rabbanā wa laka l-ḥamd", Learn.Posture.PROSTRATING to "Subḥāna rabbiya l-aʿlā ×3",
        Learn.Posture.SITTING to "Rabbi ghfir lī between the prostrations · the tashahhud at the end", Learn.Posture.SALAM to "As-salāmu ʿalaykum wa raḥmatu llāh, right then left")
    Column(Modifier.fillMaxSize()) {
        TopBar(c, "The movements", onBack)
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
