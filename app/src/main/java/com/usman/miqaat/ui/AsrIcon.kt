package com.usman.miqaat.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Asr: a low sun with rays, a stick and its long shadow. Asr is the time when a shadow has grown longer than the object.
 * Drawn as an outline icon to match the Celestial set. Dhuhr keeps the plain sun.
 */
internal val AsrShadowIcon: ImageVector by lazy {
    val d = listOf(
        path("M8.7 6.2A2.5 2.5 0 1 1 3.7 6.2A2.5 2.5 0 1 1 8.7 6.2"),
        path("M10.00 6.20L11.20 6.20"),
        path("M8.89 8.89L9.74 9.74"),
        path("M6.20 10.00L6.20 11.20"),
        path("M3.51 8.89L2.66 9.74"),
        path("M2.40 6.20L1.20 6.20"),
        path("M3.51 3.51L2.66 2.66"),
        path("M6.20 2.40L6.20 1.20"),
        path("M8.89 3.51L9.74 2.66"),
        path("M16 20V11"),
        path("M16 15L22 20"),
        path("M1.5 20H22.5"),
    )
    ImageVector.Builder(name = "AsrShadow", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).apply {
        d.forEach { p ->
            addPath(
                pathData = PathParser().parsePathString(p).toNodes(),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()
}
