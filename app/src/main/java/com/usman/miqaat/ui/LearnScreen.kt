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
import androidx.compose.material.icons.outlined.Balance
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
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
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.UrduContent
import com.usman.miqaat.data.Learn
import java.util.Locale

/** Semantic surfaces for secondary screens, derived from the chosen theme so they are one product with the home screen. */
data class LearnColors(
    val background: Brush, val surface: Color, val surfaceRaised: Color, val primary: Color, val onPrimary: Color,
    val text: Color, val textSecondary: Color, val divider: Color, val success: Color, val display: FontFamily, val arabic: FontFamily, val kiswah: Boolean
)

@Composable
fun learnColors(settings: AppSettings): LearnColors {
    val tk = screenTokens()
    val fresh = tk.art == ArtStyle.CELESTIAL || tk.art == ArtStyle.GALLERY
    return if (fresh) LearnColors(
        background = tk.backgroundBrush, surface = tk.surface, surfaceRaised = tk.surfaceRaised, primary = if (tk.art == ArtStyle.GALLERY) Color(0xFF233F9A) else tk.primary, onPrimary = tk.onPrimary,
        text = tk.contentPrimary, textSecondary = tk.contentSecondary, divider = tk.divider, success = tk.success, display = tk.fontDisplay, arabic = tk.fontArabic, kiswah = false
    ) else legacyLearnColors(settings)
}

@Composable
private fun legacyLearnColors(settings: AppSettings): LearnColors = if (settings.theme == AppTheme.KISWAH) LearnColors(
    background = Brush.verticalGradient(listOf(Color(0xFF0B0B0B), Kiswah.silk)), surface = Color(0xFF121212), surfaceRaised = Color(0xFF1A1814),
    primary = Kiswah.thread, onPrimary = Color(0xFF0B0B0B), text = Kiswah.ivory, textSecondary = Kiswah.threadSoft.copy(alpha = 0.85f), divider = Kiswah.thread.copy(alpha = 0.35f),
    success = screenTokens().success, display = Cinzel, arabic = ReemKufi, kiswah = true
) else LearnColors(
    background = Brush.verticalGradient(listOf(Palette.night, Palette.panel)), surface = Color(0xFF141C3D), surfaceRaised = Palette.panelRaised,
    primary = Palette.gold, onPrimary = Palette.night, text = Palette.ivory, textSecondary = Palette.textSecondary, divider = Palette.line,
    success = Palette.mint, display = Cormorant, arabic = Amiri, kiswah = false
)

private fun lessonUiFont(): FontFamily = if (L10n.uiUrdu) Nastaliq else Nunito

private enum class Mode { LIBRARY, LESSON, WORDS, MOVES }

/** Learn Salah — for beginners of any age. Library → guided prayer (rakʿah by rakʿah), words practice, or movement review. */
@Composable
fun LearnScreen(settings: AppSettings, previewLesson: Learn.Lesson? = null, previewStep: Int = 0, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val c = learnColors(settings)
    var mode by rememberSaveable { mutableStateOf(if (previewLesson != null) Mode.LESSON else Mode.LIBRARY) }
    var lesson by rememberSaveable { mutableStateOf(previewLesson ?: Learn.Lesson.FAJR) }
    var progressTick by remember { mutableIntStateOf(0) }
    val progress = remember(progressTick, mode) { Learn.progress(ctx) }

    Box(Modifier.fillMaxSize().background(c.background)) {
        if (c.kiswah) Weave(Modifier.fillMaxSize()) else GirihLattice(Modifier.fillMaxSize(), alpha = 0.035f, tile = 120f)
        when (mode) {
            Mode.LIBRARY -> Library(c, progress,
                onLesson = { l -> lesson = l; mode = Mode.LESSON },
                onWords = { mode = Mode.WORDS }, onMoves = { mode = Mode.MOVES }, onBack = onBack, onReset = { Learn.clear(ctx); progressTick++ })
            Mode.LESSON -> LessonView(c, lesson, startAt = if (previewLesson != null) previewStep else if (progress.lesson == lesson) progress.index else 0,
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
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 840.dp && maxHeight >= 600.dp && maxWidth > maxHeight && androidx.compose.ui.platform.LocalDensity.current.fontScale <= 1.15f) LibraryWide(c, p, onLesson, onWords, onMoves, onBack, onReset)
        else LibraryNarrow(c, p, onLesson, onWords, onMoves, onBack, onReset)
    }
}

/** Tablet landscape: hero + continue card on the left, prayer cards with progress on the right, practice row along the bottom. */
@Composable
private fun LibraryWide(c: LearnColors, p: Learn.Progress, onLesson: (Learn.Lesson) -> Unit, onWords: () -> Unit, onMoves: () -> Unit, onBack: () -> Unit, onReset: () -> Unit) {
    val tk = screenTokens()
    Box(Modifier.fillMaxSize()) {
        if (tk.art == ArtStyle.CELESTIAL || tk.art == ArtStyle.GALLERY) ThemedBackdrop(false, Modifier.fillMaxSize(), scrim = 0.55f)
        Column(Modifier.fillMaxSize()) {
            TopBar(c, Str[R.string.s_learn_salah], onBack)
            Row(Modifier.weight(1f).padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)) {
                    Text("الصَّلَاة", fontFamily = c.arabic, fontSize = 88.sp, color = c.primary, modifier = Modifier.semantics { contentDescription = Str[R.string.s_as_salah_the_prayer] })
                    Text(Str[R.string.s_one_common_form_of_the_prayer], fontFamily = Nunito, fontSize = 22.sp, lineHeight = 31.sp, color = c.text)
                    if (p.lesson != null) LibraryCard(c, Str.get(R.string.s_continue_x, Str[p.lesson.titleRes]), Str.get(R.string.s_resume_at_step, p.index + 1, Learn.actions(p.lesson).size), primary = true, big = true) { onLesson(p.lesson) }
                    else { val first = Learn.Lesson.entries.first(); LibraryCard(c, Str[first.titleRes], Str[first.subtitleRes] + " · " + Str.get(R.string.s_n_steps, Learn.actions(first).size), primary = true, big = true) { onLesson(first) } }
                }
                Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
                    Text(Str[R.string.s_learn_a_complete_prayer], fontFamily = c.display, fontSize = 28.sp, color = c.text, modifier = Modifier.semantics { heading() })
                    Learn.Lesson.entries.forEach { l ->
                        val n = Learn.actions(l).size
                        val frac = when { l in p.completed -> 1f; l == p.lesson -> (p.index / n.toFloat()).coerceIn(0f, 1f); else -> 0f }
                        LibraryCard(c, Str[l.titleRes], Str[l.subtitleRes] + " · " + Str.get(R.string.s_n_steps, n), done = l in p.completed, progress = frac, big = true) { onLesson(l) }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().background(c.surface.copy(alpha = 0.85f)).navigationBarsPadding().padding(horizontal = 32.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(Str[R.string.s_practise], fontFamily = c.display, fontSize = 24.sp, color = c.text, modifier = Modifier.semantics { heading() })
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.weight(1f)) { LibraryCard(c, Str[R.string.s_the_words], Str[R.string.s_the_twelve_texts_of_the_prayer], big = true, onClick = onWords) }
                    Box(Modifier.weight(1f)) { LibraryCard(c, Str[R.string.s_the_movements], Str[R.string.s_six_positions_what_each_looks_like], big = true, onClick = onMoves) }
                }
            }
        }
    }
}

@Composable
private fun LibraryNarrow(c: LearnColors, p: Learn.Progress, onLesson: (Learn.Lesson) -> Unit, onWords: () -> Unit, onMoves: () -> Unit, onBack: () -> Unit, onReset: () -> Unit) {
    val big = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    Column(Modifier.fillMaxSize()) {
        TopBar(c, Str[R.string.s_learn_salah], onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("الصَّلَاة", fontFamily = c.arabic, fontSize = if (big) 72.sp else 44.sp, color = c.primary, modifier = Modifier.semantics { contentDescription = Str[R.string.s_as_salah_the_prayer] })
            Text(Str[R.string.s_one_common_form_of_the_prayer], fontFamily = Nunito, fontSize = if (big) 20.sp else 14.sp, lineHeight = if (big) 28.sp else 20.sp, color = c.textSecondary)
            if (p.lesson != null) LibraryCard(c, Str.get(R.string.s_continue_x, Str[p.lesson.titleRes]), Str.get(R.string.s_resume_at_step, p.index + 1, Learn.actions(p.lesson).size), primary = true, big = big) { onLesson(p.lesson) }
            Text(Str[R.string.s_learn_a_complete_prayer], fontFamily = c.display, fontSize = if (c.kiswah) 13.sp else if (big) 28.sp else 22.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            Learn.Lesson.entries.forEach { l ->
                LibraryCard(c, Str[l.titleRes], Str[l.subtitleRes] + " · " + Str.get(R.string.s_n_steps, Learn.actions(l).size), done = l in p.completed, big = big) { onLesson(l) }
            }
            Text(Str[R.string.s_practise], fontFamily = c.display, fontSize = if (c.kiswah) 13.sp else if (big) 28.sp else 22.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            LibraryCard(c, Str[R.string.s_the_words], Str[R.string.s_the_twelve_texts_of_the_prayer], big = big, onClick = onWords)
            LibraryCard(c, Str[R.string.s_the_movements], Str[R.string.s_six_positions_what_each_looks_like], big = big, onClick = onMoves)
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val allRecorded = remember { (1..12).all { ctx.resources.getIdentifier("learn_%02d".format(it), "raw", ctx.packageName) != 0 } }
            Text(Str[if (allRecorded) R.string.s_recordings_bundled else R.string.s_audio_is_the_device_s_own], fontFamily = Nunito, fontSize = 12.sp, lineHeight = 17.sp, color = c.textSecondary, modifier = Modifier.padding(top = 10.dp))
            if (p.completed.isNotEmpty() || p.lesson != null) Text(Str[R.string.s_reset_progress], fontFamily = Nunito, fontSize = 13.sp, color = c.textSecondary, modifier = Modifier.heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onReset).padding(vertical = 14.dp))
        }
    }
}

@Composable
private fun LibraryCard(c: LearnColors, title: String, subtitle: String, primary: Boolean = false, done: Boolean = false, progress: Float? = null, big: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 64.dp).clip(RoundedCornerShape(16.dp)).background(if (primary) c.primary.copy(alpha = 0.16f) else c.surface)
            .border(1.dp, if (primary) c.primary.copy(alpha = 0.6f) else c.divider, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp).semantics(mergeDescendants = true) { if (done) stateDescription = Str[R.string.s_complete] },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = Nunito, fontSize = if (big) 24.sp else 17.sp, fontWeight = FontWeight.Bold, color = if (primary) c.primary else c.text)
            Text(subtitle, fontFamily = Nunito, fontSize = if (big) 17.sp else 13.sp, color = c.textSecondary, lineHeight = if (big) 23.sp else 18.sp)
            if (progress != null) Box(Modifier.padding(top = 8.dp).fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(c.divider)) {
                Box(Modifier.fillMaxWidth(progress.coerceIn(0.04f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(c.primary))
            }
        }
        if (done) Icon(Icons.Outlined.Check, null, Modifier.size(20.dp), tint = c.success) else Text("›", fontSize = 22.sp, color = c.textSecondary)
    }
}

// ---------------------------------------------------------------- guided lesson

/** Approved editorial lesson: illustration and recitation side by side on tablet, flowing on narrow/large-text screens. */
@Composable
private fun LessonView(c: LearnColors, lesson: Learn.Lesson, startAt: Int, onExit: () -> Unit, onDone: () -> Unit) {
    val ctx = LocalContext.current
    val actions = remember(lesson) { Learn.actions(lesson) }
    var i by rememberSaveable(lesson) { mutableIntStateOf(startAt.coerceIn(0, actions.lastIndex)) }
    val a = actions[i]
    LaunchedEffect(i) { Learn.save(ctx, lesson, i) }
    val audio = rememberSpeaker()
    val rakahSteps = actions.count { it.rakah == a.rakah }
    val rakahPos = actions.take(i + 1).count { it.rakah == a.rakah }
    val where = Str.get(R.string.s_lesson_where, Str[lesson.titleRes], a.rakah, lesson.rakat, rakahPos, rakahSteps, if (L10n.uiUrdu) UrduContent.position(a.step) else a.step.position)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scale = androidx.compose.ui.platform.LocalDensity.current.fontScale
        val wide = maxWidth >= 840.dp && maxWidth > maxHeight && maxHeight >= 600.dp && scale <= 1.3f
        // Phone landscape gets a two-column SCROLLABLE body, without compressing typography or artwork.
        val shortWide = maxWidth > maxHeight && maxWidth >= 600.dp && scale <= 1.3f
        if (maxHeight < 520.dp && scale > 1.3f) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                TopBar(c, Str[lesson.titleRes], onExit) {
                    Text(Str.get(R.string.learn_step_of, i + 1, actions.size), fontFamily = c.display, fontSize = 16.sp, color = c.textSecondary)
                }
                RakahMap(c, lesson, actions, i, Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    .semantics { contentDescription = where; liveRegion = LiveRegionMode.Polite })
                Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    PostureCard(c, a.posture, if (L10n.uiUrdu) UrduContent.cue(a) else a.cue, Modifier.fillMaxWidth(), reflow = true)
                    LessonWords(c, a.step, audio, Modifier.fillMaxWidth(), editorial = false)
                }
                BottomBar(c, canBack = i > 0, last = i == actions.lastIndex,
                    nextLabel = if (i < actions.lastIndex) Str.get(R.string.s_continue_x, if (L10n.uiUrdu) UrduContent.position(actions[i + 1].step) else actions[i + 1].step.position) else Str[R.string.s_finish_well_done],
                    onBack = { audio.stop(); i-- }, onNext = { audio.stop(); if (i < actions.lastIndex) i++ else onDone() })
            }
        } else Column(Modifier.fillMaxSize()) {
            TopBar(c, Str[lesson.titleRes], onExit) {
                Text(Str.get(R.string.learn_step_of, i + 1, actions.size), fontFamily = c.display, fontSize = 16.sp, color = c.textSecondary)
            }
            RakahMap(c, lesson, actions, i, Modifier.padding(horizontal = if (wide) 28.dp else 20.dp, vertical = 8.dp)
                .semantics { contentDescription = where; liveRegion = LiveRegionMode.Polite })
            if (wide) {
                Row(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    PostureCard(c, a.posture, if (L10n.uiUrdu) UrduContent.cue(a) else a.cue,
                        Modifier.weight(0.48f).fillMaxHeight())
                    LessonWords(c, a.step, audio, Modifier.weight(0.52f).fillMaxHeight().verticalScroll(rememberScrollState()), editorial = true)
                }
            } else if (shortWide) {
                Row(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    PostureCard(c, a.posture, if (L10n.uiUrdu) UrduContent.cue(a) else a.cue,
                        Modifier.weight(0.44f), reflow = true)
                    LessonWords(c, a.step, audio, Modifier.weight(0.56f), editorial = true)
                }
            } else {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    PostureCard(c, a.posture, if (L10n.uiUrdu) UrduContent.cue(a) else a.cue, Modifier.fillMaxWidth(), reflow = true)
                    LessonWords(c, a.step, audio, Modifier.fillMaxWidth(), editorial = false)
                }
            }
            BottomBar(c, canBack = i > 0, last = i == actions.lastIndex,
                nextLabel = if (i < actions.lastIndex) Str.get(R.string.s_continue_x, if (L10n.uiUrdu) UrduContent.position(actions[i + 1].step) else actions[i + 1].step.position) else Str[R.string.s_finish_well_done],
                onBack = { audio.stop(); i-- }, onNext = { audio.stop(); if (i < actions.lastIndex) i++ else onDone() })
        }
    }
}

/** One dot per actual action, preserving the 21-step Fajr sequence and rakʿah grouping. */
@Composable
private fun RakahMap(c: LearnColors, lesson: Learn.Lesson, actions: List<Learn.Action>, i: Int, modifier: Modifier) {
    val accent = screenTokens().accent
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (r in 1..lesson.rakat) {
                val steps = actions.withIndex().filter { it.value.rakah == r }
                Column(Modifier.weight(steps.size.toFloat()), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Str.get(R.string.s_rakah_n, r).let { if (L10n.uiUrdu) it else it.uppercase() },
                        fontFamily = lessonUiFont(), fontSize = 10.sp, letterSpacing = if (L10n.uiUrdu) 0.sp else 2.sp,
                        color = if (actions[i].rakah == r) screenTokens().accent else c.textSecondary)
                    Canvas(Modifier.fillMaxWidth().height(18.dp).clearAndSetSemantics { }) {
                        val rtl = layoutDirection == LayoutDirection.Rtl
                        val xs = steps.indices.map { j ->
                            val x = 4.dp.toPx() + j * (size.width - 8.dp.toPx()) / (steps.size - 1).coerceAtLeast(1)
                            if (rtl) size.width - x else x
                        }
                        val y = size.height / 2
                        drawLine(c.divider, Offset(xs.first(), y), Offset(xs.last(), y), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                        steps.forEachIndexed { j, step ->
                            if (j > 0 && step.index <= i) drawLine(accent, Offset(xs[j-1], y), Offset(xs[j], y), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                            drawCircle(if (step.index <= i) accent else c.divider, if (step.index == i) 3.5.dp.toPx() else 2.4.dp.toPx(), Offset(xs[j], y))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostureCard(c: LearnColors, posture: Learn.Posture, cue: String, modifier: Modifier, reflow: Boolean = false) {
    val art = screenTokens().art
    val scene = when (art) {
        ArtStyle.GALLERY -> R.drawable.learn_scene_gallery
        ArtStyle.CELESTIAL -> R.drawable.learn_scene_celestial
        ArtStyle.KISWAH_SILK -> R.drawable.learn_scene_kiswah
        ArtStyle.ILLUMINATED_SKY -> R.drawable.learn_scene_miqaat
    }
    val shape = RoundedCornerShape(if (c.kiswah) 12.dp else 18.dp)
    val figureHeight = if (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600) 360.dp else 300.dp
    Column(modifier.testTag("lesson-posture").clip(shape).background(c.surfaceRaised).border(1.dp, c.divider, shape), horizontalAlignment = Alignment.CenterHorizontally) {
        Box((if (reflow) Modifier.height(figureHeight) else Modifier.weight(1f)).fillMaxWidth()
            .semantics { contentDescription = if (L10n.uiUrdu) UrduContent.postureDescriptions[posture.ordinal] else posture.describe }, contentAlignment = Alignment.Center) {
            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(scene), null, Modifier.matchParentSize().clearAndSetSemantics { }, contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            if (posture == Learn.Posture.SALAM) {
                // Never mirror the anatomical right hand in RTL. Two separately drawn head turns.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Figure(posture, c.primary, Modifier.fillMaxWidth().weight(1f))
                            Text(Str[R.string.learn_salam_right], fontFamily = lessonUiFont(), fontSize = 12.sp, color = c.text, modifier = Modifier.background(c.surface.copy(alpha = 0.95f)).padding(6.dp))
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(R.drawable.learn_pose_salam_left), null,
                                Modifier.fillMaxWidth().weight(1f), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                            Text(Str[R.string.learn_salam_left], fontFamily = lessonUiFont(), fontSize = 12.sp, color = c.text, modifier = Modifier.background(c.surface.copy(alpha = 0.95f)).padding(6.dp))
                        }
                    }
                }
            } else Figure(posture, c.primary, Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp).fillMaxHeight(0.85f).fillMaxWidth(0.82f))
        }
        Text(if (L10n.uiUrdu) UrduContent.postureLabels[posture.ordinal] else posture.label.uppercase(),
            fontFamily = if (L10n.uiUrdu) lessonUiFont() else c.display, fontSize = 18.sp,
            letterSpacing = if (L10n.uiUrdu) 0.sp else 1.5.sp, color = c.text, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp).semantics { heading() })
        Text(cue, fontFamily = lessonUiFont(), fontSize = 14.sp, lineHeight = 21.sp, color = c.textSecondary, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 14.dp))
    }
}

/** Original robe line drawings from the supplied approved reference. Solid paper fill preserves legibility on all scenes. */
@Composable
@Suppress("UNUSED_PARAMETER")
fun Figure(p: Learn.Posture, color: Color, modifier: Modifier) {
    val art = when (p) {
        Learn.Posture.TAKBIR -> R.drawable.learn_pose_takbir
        Learn.Posture.STANDING -> R.drawable.learn_pose_standing
        Learn.Posture.BOWING -> R.drawable.learn_pose_bowing
        Learn.Posture.RISING -> R.drawable.learn_pose_rising
        Learn.Posture.PROSTRATING -> R.drawable.learn_pose_prostrating
        Learn.Posture.SITTING -> R.drawable.learn_pose_sitting
        Learn.Posture.TASHAHHUD -> R.drawable.learn_pose_tashahhud
        Learn.Posture.SALAM -> R.drawable.learn_pose_salam
    }
    androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(art), null, modifier,
        contentScale = androidx.compose.ui.layout.ContentScale.Fit)
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
        Text(Str[if ('…' in step.meaning) R.string.learn_meaning_summary else R.string.s_meaning], fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = c.textSecondary)
        Text(if (L10n.uiUrdu) UrduContent.stepMeanings[Adhkar.salah.indexOfFirst { it.arabic == step.arabic }.coerceAtLeast(0)] else step.meaning, fontFamily = Nunito, fontSize = 15.sp, lineHeight = 22.sp, color = c.text)
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) { showNote = !showNote }.semantics { stateDescription = Str[if (showNote) R.string.s_expanded else R.string.s_collapsed] }, verticalAlignment = Alignment.CenterVertically) {
            Text(Str[R.string.s_note_schools_and_source], fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = c.text, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ExpandMore, null, tint = c.textSecondary)
        }
        AnimatedVisibility(showNote) {
            Column {
                if (L10n.uiUrdu) Text(Str[R.string.s_english_text], fontFamily = Nunito, fontSize = 12.sp, color = c.textSecondary)
                Text(step.note, fontFamily = Nunito, fontSize = 13.sp, lineHeight = 19.sp, color = c.textSecondary)
            }
        }
    }
}

/** Reading composition from the approved board. Notes and source remain visible instead of hidden in a generic card. */
@Composable
private fun LessonWords(c: LearnColors, step: Adhkar.Step, audio: Speaker, modifier: Modifier, editorial: Boolean) {
    val index = Learn.words.indexOfFirst { it.arabic == step.arabic }.coerceAtLeast(0)
    var showTranslit by rememberSaveable { mutableStateOf(true) }
    var showSchools by rememberSaveable(step.position) { mutableStateOf(false) }
    val longArabic = step.arabic.length > 160
    val base = if (editorial) modifier.padding(vertical = 8.dp) else modifier
        .clip(RoundedCornerShape(18.dp)).background(c.surface).border(1.dp, c.divider, RoundedCornerShape(18.dp)).padding(18.dp)
    Column(base, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (L10n.uiUrdu) UrduContent.position(step) else step.position.uppercase(),
            fontFamily = lessonUiFont(), fontSize = 12.sp, letterSpacing = if (L10n.uiUrdu) 0.sp else 2.sp,
            color = screenTokens().accent, modifier = Modifier.semantics { heading() })
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(step.arabic, fontFamily = c.arabic, fontSize = if (longArabic) 27.sp else if (editorial) 64.sp else 36.sp,
                lineHeight = if (longArabic) 44.sp else if (editorial) 88.sp else 56.sp,
                color = screenTokens().arabicText, textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().testTag("lesson-arabic"))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            val usable = audio.usable(index)
            Row(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).background(if (usable) c.primary else c.primary.copy(alpha = 0.35f))
                .clickable(enabled = usable, role = Role.Button) { if (audio.playing) audio.stop() else audio.speak(step, index) }
                .semantics { contentDescription = Str[if (audio.playing) R.string.s_stop else R.string.s_hear_it]; stateDescription = if (!usable) audio.source(index) else Str[if (audio.playing) R.string.s_playing else R.string.s_not_playing] }
                .padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(if (audio.playing) Icons.Outlined.Stop else Icons.AutoMirrored.Outlined.VolumeUp, null, Modifier.size(22.dp), tint = c.onPrimary)
                Text(Str[if (audio.playing) R.string.s_stop else R.string.learn_listen], fontFamily = lessonUiFont(), fontSize = 16.sp, color = c.onPrimary)
            }
            Box(Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).border(1.dp, if (audio.slow) c.primary else c.divider, RoundedCornerShape(50))
                .selectable(selected = audio.slow, role = Role.Checkbox) { audio.slow = !audio.slow }
                .padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(Str[R.string.s_slow], fontFamily = lessonUiFont(), fontSize = 15.sp, color = if (audio.slow) c.primary else c.textSecondary)
            }
        }
        Text(if (audio.playing) Str[R.string.s_playing] else audio.source(index), fontFamily = lessonUiFont(), fontSize = 11.sp, lineHeight = 17.sp,
            color = c.textSecondary, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Switch) { showTranslit = !showTranslit }
            .semantics { stateDescription = Str[if (showTranslit) R.string.s_show else R.string.s_hide] }, verticalAlignment = Alignment.CenterVertically) {
            Text(Str[R.string.learn_say], fontFamily = lessonUiFont(), fontSize = 11.sp, letterSpacing = if (L10n.uiUrdu) 0.sp else 2.sp, color = c.textSecondary, modifier = Modifier.weight(1f))
            Text(Str[if (showTranslit) R.string.s_hide else R.string.s_show], fontFamily = lessonUiFont(), fontSize = 11.sp, color = c.textSecondary)
        }
        AnimatedVisibility(showTranslit) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(step.transliteration, fontFamily = Cormorant, fontSize = if (editorial) 26.sp else 22.sp, lineHeight = 32.sp, color = c.text,
                    modifier = Modifier.fillMaxWidth().testTag("lesson-transliteration"))
            }
        }
        Text(Str[if ('…' in step.meaning) R.string.learn_meaning_summary else R.string.s_meaning], fontFamily = lessonUiFont(), fontSize = 11.sp, letterSpacing = if (L10n.uiUrdu) 0.sp else 2.sp, color = c.textSecondary)
        Text(if (L10n.uiUrdu) UrduContent.stepMeanings[index] else step.meaning, fontFamily = if (L10n.uiUrdu) Nastaliq else Cormorant, fontSize = 22.sp, lineHeight = 32.sp, color = c.text,
            modifier = Modifier.fillMaxWidth().testTag("lesson-meaning"))
        Text(Str[R.string.learn_notes], fontFamily = lessonUiFont(), fontSize = 11.sp, letterSpacing = if (L10n.uiUrdu) 0.sp else 2.sp, color = c.textSecondary)
        // This source copy is unchanged; Urdu UI explicitly discloses that the detailed note is still English.
        if (L10n.uiUrdu) Text(Str[R.string.s_english_text], fontFamily = lessonUiFont(), fontSize = 11.sp, color = c.textSecondary)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(step.note, fontFamily = Cormorant, fontSize = 17.sp, lineHeight = 24.sp, color = c.textSecondary,
                modifier = Modifier.fillMaxWidth().testTag("lesson-note"))
        }
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(12.dp)).background(c.surfaceRaised)
            .clickable(role = Role.Button) { showSchools = !showSchools }
            .semantics { stateDescription = Str[if (showSchools) R.string.s_expanded else R.string.s_collapsed] }
            .padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Outlined.Balance, null, Modifier.size(28.dp), tint = screenTokens().accent)
            Column(Modifier.weight(1f)) {
                Text(Str[R.string.learn_schools], fontFamily = lessonUiFont(), fontSize = 15.sp, color = c.text)
                Text(Str[R.string.learn_schools_detail], fontFamily = lessonUiFont(), fontSize = 12.sp, lineHeight = 18.sp, color = c.textSecondary)
            }
            Icon(Icons.Outlined.ExpandMore, null, tint = c.textSecondary)
        }
        AnimatedVisibility(showSchools) {
            Text(Str[R.string.learn_school_guidance], fontFamily = lessonUiFont(), fontSize = 13.sp, lineHeight = 20.sp, color = c.textSecondary)
        }
    }
}

@Composable
private fun BottomBar(c: LearnColors, canBack: Boolean, last: Boolean, nextLabel: String, onBack: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(c.surfaceRaised.copy(alpha = 0.92f)).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(52.dp).heightIn(min = 52.dp).clip(RoundedCornerShape(26.dp)).border(1.dp, if (canBack) c.divider else c.divider.copy(alpha = 0.3f), CircleShape).clickable(enabled = canBack, role = Role.Button, onClick = onBack).semantics { contentDescription = Str[R.string.s_previous_step] }, contentAlignment = Alignment.Center) {
            Text("‹", fontSize = 26.sp, color = if (canBack) c.text else c.textSecondary.copy(alpha = 0.4f))
        }
        Column(Modifier.weight(1f).heightIn(min = 52.dp).testTag("lesson-next").clip(RoundedCornerShape(50)).background(if (last) c.success else c.primary).clickable(role = Role.Button, onClick = onNext).padding(horizontal = 18.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(nextLabel, fontFamily = lessonUiFont(), fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 24.sp, color = c.onPrimary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().testTag("lesson-next-label"))
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
        BottomBar(c, canBack = i > 0, last = i == words.lastIndex, nextLabel = if (i < words.lastIndex) Str.get(R.string.s_continue_x, if (L10n.uiUrdu) UrduContent.position(words[i + 1]) else words[i + 1].position) else Str[R.string.s_back_to_learn_salah],
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
                    Figure(p, c.primary, Modifier.size(84.dp).semantics { contentDescription = if (L10n.uiUrdu) UrduContent.postureDescriptions[p.ordinal] else p.describe })
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(if (L10n.uiUrdu) UrduContent.postureLabels[p.ordinal] else if (c.kiswah) p.label.uppercase() else p.label, fontFamily = c.display, fontSize = if (c.kiswah) 12.sp else 19.sp, letterSpacing = if (c.kiswah) 2.sp else 0.sp, color = c.text)
                        Text(if (L10n.uiUrdu) UrduContent.postureDescriptions[p.ordinal] else p.describe, fontFamily = Nunito, fontSize = 13.sp, lineHeight = 18.sp, color = c.textSecondary)
                        Text(said.getValue(p), fontFamily = Cormorant, fontSize = 16.sp, lineHeight = 21.sp, color = c.primary, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

