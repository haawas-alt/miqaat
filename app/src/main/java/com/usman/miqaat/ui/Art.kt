package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Eight-point star (khatam) lattice, the classic girih tiling, drawn procedurally so it scales to
 * any screen without an image. Fades toward the edges via a radial mask.
 */
@Composable
fun GirihLattice(modifier: Modifier = Modifier, color: Color = Palette.goldSoft, alpha: Float = 0.10f, tile: Float = 140f) {
    Canvas(modifier) {
        val cols = (size.width / tile).toInt() + 2
        val rows = (size.height / tile).toInt() + 2
        val stroke = Stroke(width = 1.2f)
        val cx = size.width / 2
        val cy = size.height * 0.45f
        val maxR = maxOf(size.width, size.height) * 0.62f
        for (r in 0 until rows) for (c in 0 until cols) {
            val x = c * tile
            val y = r * tile
            val d = kotlin.math.hypot(x + tile / 2 - cx, y + tile / 2 - cy)
            val fade = (1f - (d / maxR)).coerceIn(0f, 1f)
            if (fade <= 0.02f) continue
            val a = alpha * fade
            drawStar(x + tile / 2, y + tile / 2, tile * 0.46f, color.copy(alpha = a), stroke)
            drawStar(x + tile / 2, y + tile / 2, tile * 0.30f, color.copy(alpha = a * 0.8f), stroke, rotate = PI / 8)
            drawCircle(color.copy(alpha = a), radius = tile * 0.09f, center = Offset(x + tile / 2, y + tile / 2), style = stroke)
            // corner ties
            val t = tile * 0.16f
            drawLine(color.copy(alpha = a), Offset(x, y), Offset(x + t, y + t), 1.2f)
            drawLine(color.copy(alpha = a), Offset(x + tile, y), Offset(x + tile - t, y + t), 1.2f)
            drawLine(color.copy(alpha = a), Offset(x, y + tile), Offset(x + t, y + tile - t), 1.2f)
            drawLine(color.copy(alpha = a), Offset(x + tile, y + tile), Offset(x + tile - t, y + tile - t), 1.2f)
        }
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, r: Float, color: Color, stroke: Stroke, rotate: Double = 0.0) {
    val p = Path()
    val inner = r * 0.42f
    for (i in 0 until 16) {
        val ang = rotate + i * PI / 8 - PI / 2
        val rad = if (i % 2 == 0) r else inner
        val x = cx + (rad * cos(ang)).toFloat()
        val y = cy + (rad * sin(ang)).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, color, style = stroke)
}

/** Fixed, seeded starfield; only its alpha animates with the period. */
@Composable
fun Stars(modifier: Modifier = Modifier, alpha: Float) {
    val pts = remember { val rnd = Random(7); List(160) { Triple(rnd.nextFloat(), rnd.nextFloat() * 0.7f, rnd.nextFloat()) } }
    if (alpha <= 0.01f) return
    Canvas(modifier) {
        pts.forEach { (fx, fy, s) ->
            drawCircle(Color(0xFFF6E7B8).copy(alpha = alpha * (0.25f + 0.65f * s)), radius = 0.8f + 2.2f * s, center = Offset(fx * size.width, fy * size.height))
        }
    }
}

/** Mihrab arch, two fine lines and a finial, drawn to fit its bounds. */
@Composable
fun MihrabArch(modifier: Modifier = Modifier, color: Color = Palette.goldSoft) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        fun arch(inset: Float, alpha: Float, width: Float) {
            val p = Path().apply {
                moveTo(inset, h)
                lineTo(inset, h * 0.48f)
                cubicTo(inset, h * 0.22f, w * 0.27f + inset * 0.4f, h * 0.09f, w / 2, h * 0.05f + inset * 0.5f)
                cubicTo(w * 0.73f - inset * 0.4f, h * 0.09f, w - inset, h * 0.22f, w - inset, h * 0.48f)
                lineTo(w - inset, h)
            }
            drawPath(p, color.copy(alpha = alpha), style = Stroke(width))
        }
        arch(w * 0.02f, 0.55f, 2.2f)
        arch(w * 0.075f, 0.25f, 1.4f)
        drawCircle(color, radius = 5f, center = Offset(w / 2, h * 0.05f))
    }
}

/** Soft radial glow used behind the hero. */
@Composable
fun Glow(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier) {
        drawRect(Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), center = Offset(size.width / 2, size.height * 0.40f), radius = size.width * 0.5f))
    }
}
