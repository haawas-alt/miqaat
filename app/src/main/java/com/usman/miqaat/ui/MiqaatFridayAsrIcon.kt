package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import kotlin.math.cos
import kotlin.math.sin

/** MA2's afternoon crescent and dotted sun edge, distinct from the noon sun. */
@Composable internal fun MiqaatFridayAsrIcon(color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val unit = size.minDimension
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = unit * .31f
        val disc = Path().apply {
            addOval(Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
        }
        val opening = Path().apply {
            addOval(Rect(center.x - radius * .42f, center.y - radius * 1.48f,
                center.x + radius * 1.58f, center.y + radius * .52f))
        }
        drawPath(Path.combine(PathOperation.Difference, disc, opening), color)
        for (degrees in listOf(-85, -55, -25, 5, 35)) {
            val angle = Math.toRadians(degrees.toDouble())
            drawCircle(color, unit * .032f, Offset(
                center.x + unit * .43f * cos(angle).toFloat(),
                center.y + unit * .43f * sin(angle).toFloat()))
        }
    }
}
