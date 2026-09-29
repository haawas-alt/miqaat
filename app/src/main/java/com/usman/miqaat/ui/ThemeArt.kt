package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerState
import java.time.Duration
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/*
 * Generative artwork for the two new themes. Everything is drawn from the space it is given: no bitmaps, no text, no
 * animation (so nothing depends on motion and reduced-motion needs no special case). All of it is decorative and
 * exposes nothing to accessibility services; the real information is always live text beside it.
 */

/* ───────────────────────────── shared helpers ───────────────────────────── */

/** Deterministic pseudo-random in 0..1 so stars sit in the same place on every frame and every rotation. */
private fun rnd(seed: Int): Float {
    var x = seed * 747796405 + 2891336453L.toInt()
    x = ((x shr ((x shr 28) + 4)) xor x) * 277803737
    x = (x shr 22) xor x
    return (x.toLong() and 0xFFFFFF).toFloat() / 16777216f
}

private fun DrawScope.stars(color: Color, count: Int, maxY: Float, seed: Int = 7) {
    for (i in 0 until count) {
        val x = rnd(seed + i * 2) * size.width
        val y = rnd(seed + i * 2 + 1) * maxY
        val fade = 1f - (y / max(maxY, 1f)) * 0.7f                       // thinner near the horizon
        val r = (0.6f + rnd(seed + i * 7) * 1.4f) * (size.minDimension / 700f).coerceIn(0.6f, 2f)
        drawCircle(color.copy(alpha = color.alpha * fade * (0.4f + rnd(seed + i * 13) * 0.6f)), r, Offset(x, y))
    }
}

/** A soft ridge line made from three sines; [phase] moves the peaks so layers differ. */
private fun DrawScope.ridge(baseY: Float, amp: Float, phase: Float, brush: Brush) {
    val p = Path()
    p.moveTo(0f, size.height)
    val steps = 48
    for (i in 0..steps) {
        val f = i / steps.toFloat()
        val y = baseY - amp * (0.55f * sin(f * 2f * PI.toFloat() * 1.3f + phase) + 0.3f * sin(f * 2f * PI.toFloat() * 2.9f + phase * 1.7f) + 0.15f * sin(f * 2f * PI.toFloat() * 5.3f + phase * 0.6f) + 0.6f)
        p.lineTo(f * size.width, y)
    }
    p.lineTo(size.width, size.height); p.close()
    drawPath(p, brush)
}

/* ───────────────────────────── Celestial Meridian ───────────────────────────── */

/**
 * Midnight-navy field, sparse stars, a saffron horizon glow with a half-risen sun, layered mountains and still water.
 * The horizon sits at [horizon] (0..1 of the height). Stays low-contrast so text laid over it remains legible; callers
 * add their own scrim under text.
 */
@Composable
fun CelestialBackdrop(modifier: Modifier, tk: ThemeTokens, horizon: Float = 0.74f, glow: Float = 1f) {
    Canvas(modifier) {
        val w = size.width; val h = size.height; val hy = h * horizon
        drawRect(tk.backgroundBrush)
        stars(Color(0xFFFFF6E5).copy(alpha = 0.85f), count = (w * h / 14000f).toInt().coerceIn(20, 140), maxY = hy * 0.8f)
        // saffron horizon glow
        drawRect(Brush.radialGradient(listOf(tk.sun.copy(alpha = 0.55f * glow), tk.sun.copy(alpha = 0.16f * glow), Color.Transparent), Offset(w * 0.5f, hy), w * 0.55f))
        // half-risen sun
        drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF3CC), tk.sun), Offset(w * 0.5f, hy), h * 0.055f), h * 0.055f, Offset(w * 0.5f, hy))
        // mountains: far to near, each a little lighter than the field so they read without competing with text
        ridge(hy - h * 0.005f, h * 0.10f, 0.4f, Brush.verticalGradient(listOf(Color(0xFF1B3A63).copy(alpha = 0.75f), Color(0xFF12294A).copy(alpha = 0.9f)), hy - h * 0.12f, hy))
        ridge(hy + h * 0.015f, h * 0.075f, 2.1f, Brush.verticalGradient(listOf(Color(0xFF123056), Color(0xFF0B2242)), hy - h * 0.06f, hy + h * 0.06f))
        // water
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0B2A50), Color(0xFF061A36)), hy, h), Offset(0f, hy + h * 0.02f), Size(w, h - hy - h * 0.02f))
        // sun reflection: a few tapering glints
        for (i in 0 until 7) {
            val gy = hy + h * (0.045f + i * 0.028f)
            val gw = w * (0.10f - i * 0.011f).coerceAtLeast(0.02f)
            drawRect(tk.sun.copy(alpha = 0.32f * glow * (1f - i / 8f)), Offset(w * 0.5f - gw / 2, gy), Size(gw, max(1.5f, h * 0.004f)))
        }
    }
}

/** Where prayer times fall along the day, 0 at Fajr and 1 at Isha. Real times, so the arc always agrees with the list. */
private fun dayFraction(state: PrayerState, t: java.time.ZonedDateTime): Float {
    val a = state.today[Prayer.FAJR]; val b = state.today[Prayer.ISHA]
    val total = Duration.between(a, b).seconds.coerceAtLeast(1)
    return (Duration.between(a, t).seconds / total.toFloat()).coerceIn(0f, 1f)
}

/**
 * The sun's path over the horizon as a dotted arc with a node for every listed prayer, the hero prayer lit, and a
 * glowing sun where "now" falls. Node positions come from the actual prayer times.
 */
@Composable
fun SolarArc(modifier: Modifier, state: PrayerState, settings: AppSettings, tk: ThemeTokens) {
    val shown = listedPrayers(settings)
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val x0 = w * 0.03f; val x1 = w * 0.97f; val base = h * 0.92f; val rise = h * 0.82f
        fun pt(f: Float) = Offset(x0 + f * (x1 - x0), base - sin(f * PI.toFloat()) * rise)
        val dash = PathEffect.dashPathEffect(floatArrayOf(size.minDimension * 0.014f, size.minDimension * 0.02f))
        val arc = Path().apply { moveTo(pt(0f).x, pt(0f).y); for (i in 1..80) pt(i / 80f).let { lineTo(it.x, it.y) } }
        drawPath(arc, tk.contentPrimary.copy(alpha = 0.55f), style = Stroke(max(1.5f, size.minDimension * 0.006f), pathEffect = dash))
        val nodeR = size.minDimension * 0.022f
        shown.forEach { p ->
            val f = dayFraction(state, state.today[p]); val o = pt(f)
            val hero = p == state.hero
            drawLine(tk.contentPrimary.copy(alpha = if (hero) 0.55f else 0.22f), o, Offset(o.x, base), max(1f, nodeR * 0.18f), pathEffect = dash)
            if (hero) drawCircle(tk.sun.copy(alpha = 0.25f), nodeR * 2.4f, o)
            drawCircle(if (hero) tk.sun else tk.contentSecondary, if (hero) nodeR * 1.25f else nodeR, o)
        }
        // now: a small warm sun on the arc (before Fajr / after Isha it rests at the end of the arc)
        val nowP = pt(dayFraction(state, state.now))
        drawCircle(Brush.radialGradient(listOf(tk.sun.copy(alpha = 0.55f), Color.Transparent), nowP, nodeR * 4f), nodeR * 4f, nowP)
        drawCircle(Color(0xFFFFF3CC), nodeR * 0.9f, nowP)
    }
}

/* ───────────────────────────── Prayer Gallery ───────────────────────────── */

/** Pale abstract hero art: nested arches, a sun disc, a light shaft and an olive branch drawn as line-work. */
@Composable
fun GalleryHeroArt(modifier: Modifier, tk: ThemeTokens) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        fun arch(x0: Float, x1: Float, top: Float, bottom: Float): Path = Path().apply {
            val xm = (x0 + x1) / 2f; val spring = top + (x1 - x0) * 0.5f
            moveTo(x0, bottom); lineTo(x0, spring); cubicTo(x0, top + (spring - top) * 0.3f, xm - (x1 - x0) * 0.25f, top, xm, top)
            cubicTo(xm + (x1 - x0) * 0.25f, top, x1, top + (spring - top) * 0.3f, x1, spring); lineTo(x1, bottom); close()
        }
        drawPath(arch(w * 0.02f, w * 0.98f, h * 0.02f, h), Color(0xFFEDE3D2).copy(alpha = 0.55f))
        drawPath(arch(w * 0.16f, w * 0.84f, h * 0.10f, h), Color(0xFFF6EBD6).copy(alpha = 0.75f))
        drawCircle(tk.sun.copy(alpha = 0.35f), min(w, h) * 0.30f, Offset(w * 0.30f, h * 0.55f))
        // a diagonal shaft of light
        val shaft = Path().apply { moveTo(w * 0.42f, 0f); lineTo(w * 0.62f, 0f); lineTo(w * 0.86f, h); lineTo(w * 0.56f, h); close() }
        drawPath(shaft, Color.White.copy(alpha = 0.32f))
        // olive branch: a curved stem with paired leaves
        val stem = Path().apply { moveTo(w * 0.50f, h * 0.02f); quadraticBezierTo(w * 0.40f, h * 0.45f, w * 0.60f, h * 0.98f) }
        val ink = tk.success.copy(alpha = 0.85f)
        drawPath(stem, ink, style = Stroke(max(1.5f, w * 0.004f)))
        for (i in 1..8) {
            val f = i / 9f
            val cx = w * (0.50f + (0.10f * f) - 0.10f * sin(f * PI.toFloat()) * 0.9f)
            val cy = h * (0.02f + 0.96f * f)
            val lw = w * 0.085f; val lh = w * 0.026f
            rotate(-32f, Offset(cx, cy)) { drawOval(ink.copy(alpha = 0.7f), Offset(cx, cy - lh / 2), Size(lw, lh)) }
            rotate(212f, Offset(cx, cy)) { drawOval(ink.copy(alpha = 0.7f), Offset(cx, cy - lh / 2), Size(lw, lh)) }
        }
    }
}

/**
 * A small landscape for one prayer period: sky, hills and one emblem. Colours here are illustrative art, not text, so
 * they are local to the artwork rather than semantic tokens; nothing is ever laid over them without a scrim.
 */
@Composable
fun PrayerCardArt(prayer: Prayer, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val (top, bottom, hill1, hill2) = when (prayer) {
            Prayer.FAJR -> listOf(Color(0xFF6E86A8), Color(0xFFB7C4D6), Color(0xFF52698C), Color(0xFF8598B4))
            Prayer.SUNRISE -> listOf(Color(0xFFE9C9A0), Color(0xFFF8E6C6), Color(0xFFDDB98B), Color(0xFFEBCFA4))
            Prayer.DHUHR -> listOf(Color(0xFFF6EAC8), Color(0xFFFCF4E0), Color(0xFFE9D6A6), Color(0xFFF0E1B8))
            Prayer.ASR -> listOf(Color(0xFFEBD8A8), Color(0xFFF7EBCB), Color(0xFFD9C08A), Color(0xFFE6D3A2))
            Prayer.MAGHRIB -> listOf(Color(0xFFC9705A), Color(0xFFEDB48E), Color(0xFF9C4E45), Color(0xFFC0705F))
            Prayer.ISHA -> listOf(Color(0xFF5B5A86), Color(0xFF9B94B8), Color(0xFF454670), Color(0xFF6E6C98))
        }
        drawRect(Brush.verticalGradient(listOf(top, bottom)))
        val hy = h * 0.74f
        ridge(hy, h * 0.16f, 1.2f, Brush.verticalGradient(listOf(hill2, hill2.copy(alpha = 0.7f))))
        ridge(hy + h * 0.10f, h * 0.14f, 3.4f, Brush.verticalGradient(listOf(hill1, hill1.copy(alpha = 0.8f))))
        val cx = w * 0.5f
        when (prayer) {
            Prayer.FAJR, Prayer.ISHA -> {
                val r = min(w, h) * 0.13f; val c = Offset(w * 0.46f, h * 0.30f)
                drawCircle(Color(0xFFFFF6E5), r, c)
                drawCircle(if (prayer == Prayer.FAJR) top else Color(0xFF5B5A86), r * 0.85f, Offset(c.x + r * 0.5f, c.y - r * 0.12f))
                if (prayer == Prayer.ISHA) stars(Color(0xFFFFF6E5).copy(alpha = 0.8f), 9, h * 0.5f, seed = 31)
            }
            Prayer.SUNRISE -> {
                val r = min(w, h) * 0.17f; val c = Offset(cx, hy - h * 0.02f)
                for (i in 0 until 11) rotate(-90f + (i - 5) * 15f + 90f, c) { drawLine(Color(0xFFF6D089).copy(alpha = 0.7f), Offset(c.x, c.y - r * 1.25f), Offset(c.x, c.y - r * 1.9f), max(1f, w * 0.006f)) }
                drawCircle(Color(0xFFF2B865), r, c)
            }
            Prayer.DHUHR -> {
                val r = min(w, h) * 0.11f
                drawCircle(Color(0xFFEFA94A), r, Offset(cx, h * 0.38f))
                drawLine(Color(0xFFEFA94A).copy(alpha = 0.35f), Offset(cx, h * 0.10f), Offset(cx, hy), max(1f, w * 0.004f))
            }
            Prayer.ASR -> {
                val beam = Path().apply { moveTo(w * 0.05f, 0f); lineTo(w * 0.30f, 0f); lineTo(w * 0.72f, h); lineTo(w * 0.36f, h); close() }
                drawPath(beam, Color.White.copy(alpha = 0.30f))
                val beam2 = Path().apply { moveTo(w * 0.40f, 0f); lineTo(w * 0.55f, 0f); lineTo(w * 0.95f, h); lineTo(w * 0.78f, h); close() }
                drawPath(beam2, Color.White.copy(alpha = 0.20f))
            }
            Prayer.MAGHRIB -> {
                val r = min(w, h) * 0.15f; val c = Offset(cx, hy - h * 0.01f)
                drawCircle(Brush.radialGradient(listOf(Color(0xFFFFE3A3), Color(0xFFF0A24E)), c, r), r, c)
            }
        }
    }
}

/** A small eight-point star outline: the Miqaat mark on the Gallery header. Decorative. */
@Composable
fun GalleryMark(modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val c = Offset(size.width / 2, size.height / 2); val r = size.minDimension * 0.46f
        val p = Path()
        for (i in 0 until 16) {
            val a = (i * 22.5f - 90f) * PI.toFloat() / 180f
            val rr = if (i % 2 == 0) r else r * 0.72f
            val o = Offset(c.x + kotlin.math.cos(a) * rr, c.y + sin(a) * rr)
            if (i == 0) p.moveTo(o.x, o.y) else p.lineTo(o.x, o.y)
        }
        p.close()
        drawPath(p, color, style = Stroke(size.minDimension * 0.07f))
    }
}
