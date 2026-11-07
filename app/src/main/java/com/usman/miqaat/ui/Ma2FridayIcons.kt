package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.usman.miqaat.data.Prayer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Four small diamonds used by the approved MA2 header instead of a square grid. */
@Composable internal fun Ma2FridayGridIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val u=size.minDimension
        val r=u*.14f
        listOf(Offset(u*.31f,u*.31f),Offset(u*.69f,u*.31f),Offset(u*.31f,u*.69f),Offset(u*.69f,u*.69f)).forEach { c ->
            val diamond=Path().apply {
                moveTo(c.x,c.y-r);lineTo(c.x+r,c.y);lineTo(c.x,c.y+r);lineTo(c.x-r,c.y);close()
            }
            drawPath(diamond,color,style=Stroke(u*.075f,cap=StrokeCap.Round))
        }
    }
}

/** Distinct MA2 prayer symbols, drawn to match the approved gold line-art set. */
@Composable internal fun Ma2FridayPrayerIcon(prayer: Prayer, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val u = size.minDimension
        val stroke = u * .075f
        fun line(a: Offset, b: Offset, width: Float = stroke) =
            drawLine(color, a, b, width, StrokeCap.Round)
        fun ray(cx: Float, cy: Float, inner: Float, outer: Float, angle: Double) {
            val x1 = cx + cos(angle).toFloat() * inner
            val y1 = cy + sin(angle).toFloat() * inner
            val x2 = cx + cos(angle).toFloat() * outer
            val y2 = cy + sin(angle).toFloat() * outer
            line(Offset(x1, y1), Offset(x2, y2))
        }
        fun horizon(y: Float) {
            line(Offset(u * .12f, y), Offset(u * .88f, y))
            line(Offset(u * .22f, y + u * .12f), Offset(u * .78f, y + u * .12f), stroke * .72f)
        }
        when (prayer) {
            Prayer.FAJR -> {
                val c = Offset(u * .5f, u * .65f)
                drawArc(color, 180f, 180f, false, Offset(c.x - u * .18f, c.y - u * .18f),
                    androidx.compose.ui.geometry.Size(u * .36f, u * .36f), style = Stroke(stroke, cap = StrokeCap.Round))
                horizon(u * .65f)
                for (a in listOf(205.0, 235.0, 270.0, 305.0, 335.0)) ray(c.x, c.y, u * .27f, u * .38f, Math.toRadians(a))
            }
            Prayer.SUNRISE -> {
                val c = Offset(u * .5f, u * .48f)
                drawCircle(color, u * .16f, c, style = Stroke(stroke))
                for (i in 0 until 8) ray(c.x, c.y, u * .27f, u * .39f, i * PI / 4)
                line(Offset(u * .15f, u * .84f), Offset(u * .85f, u * .84f), stroke * .72f)
            }
            Prayer.DHUHR -> {
                val c = Offset(u * .5f, u * .5f)
                drawCircle(color, u * .18f, c, style = Stroke(stroke))
                for (i in 0 until 8) ray(c.x, c.y, u * .30f, u * .43f, i * PI / 4)
            }
            Prayer.ASR -> {
                val sun = Offset(u * .67f, u * .36f)
                drawCircle(color, u * .12f, sun, style = Stroke(stroke * .82f))
                for (i in 0 until 6) ray(sun.x, sun.y, u * .20f, u * .27f, (-PI * .92) + i * PI / 3.5)
                val cloud = Path().apply {
                    moveTo(u * .16f, u * .69f)
                    cubicTo(u * .17f, u * .55f, u * .29f, u * .51f, u * .39f, u * .57f)
                    cubicTo(u * .45f, u * .41f, u * .68f, u * .42f, u * .72f, u * .59f)
                    cubicTo(u * .88f, u * .58f, u * .91f, u * .78f, u * .75f, u * .81f)
                    lineTo(u * .29f, u * .81f)
                    cubicTo(u * .20f, u * .81f, u * .15f, u * .76f, u * .16f, u * .69f)
                }
                drawPath(cloud, color, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Prayer.MAGHRIB -> {
                val c = Offset(u * .5f, u * .61f)
                drawArc(color, 180f, 180f, false, Offset(c.x - u * .17f, c.y - u * .17f),
                    androidx.compose.ui.geometry.Size(u * .34f, u * .34f), style = Stroke(stroke, cap = StrokeCap.Round))
                horizon(u * .61f)
                for (a in listOf(215.0, 270.0, 325.0)) ray(c.x, c.y, u * .26f, u * .36f, Math.toRadians(a))
            }
            Prayer.ISHA -> {
                val center = Offset(u * .49f, u * .49f)
                val r = u * .31f
                val disc = Path().apply { addOval(Rect(center.x - r, center.y - r, center.x + r, center.y + r)) }
                val opening = Path().apply { addOval(Rect(center.x - r * .22f, center.y - r * 1.12f,
                    center.x + r * 1.58f, center.y + r * .68f)) }
                drawPath(Path.combine(PathOperation.Difference, disc, opening), color)
            }
        }
    }
}

/** MA2's approved tasbih: an elongated beaded loop with a curved tail and tassel. */
@Composable internal fun Ma2FridaySalawatIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val u = size.minDimension
        val cx = size.width * .52f
        val cy = size.height * .43f
        val rotation = Math.toRadians(-27.0)
        for (i in 0 until 18) {
            val angle = i * 2.0 * PI / 18.0
            val x = cos(angle).toFloat() * u * .27f
            val y = sin(angle).toFloat() * u * .38f
            val rx = x * cos(rotation).toFloat() - y * sin(rotation).toFloat()
            val ry = x * sin(rotation).toFloat() + y * cos(rotation).toFloat()
            drawCircle(color, u * .045f, Offset(cx + rx, cy + ry))
        }
        val path = Path().apply {
            moveTo(cx - u * .18f, cy + u * .30f)
            cubicTo(cx - u * .27f, cy + u * .48f, cx - u * .40f, cy + u * .54f, cx - u * .36f, cy + u * .72f)
        }
        drawPath(path, color, style = Stroke(u * .055f, cap = StrokeCap.Round))
        drawCircle(color, u * .055f, Offset(cx - u * .36f, cy + u * .78f))
        line@ run {
            val top = Offset(cx - u * .36f, cy + u * .83f)
            drawLine(color, top, Offset(top.x - u * .07f, top.y + u * .12f), u * .04f, StrokeCap.Round)
            drawLine(color, top, Offset(top.x + u * .07f, top.y + u * .12f), u * .04f, StrokeCap.Round)
        }
    }
}
