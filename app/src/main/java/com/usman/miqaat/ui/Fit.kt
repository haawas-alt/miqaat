package com.usman.miqaat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints

/**
 * Lays out [content] at its natural size, then scales it down (never up) so it fits the height it was given,
 * centred horizontally and vertically. The home hero uses it: the clock, name and countdown are sized from the
 * screen width, and on short screens (tablet landscape with tall cards, any phone in landscape) the remaining
 * height can be less than they need — before this, the bottom of the clock and the whole countdown were clipped
 * behind the day arc. Scaling keeps everything visible instead of hiding the most important line on the screen.
 */
@Composable
fun FitHeight(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content, modifier) { measurables, constraints ->
        val child = measurables.firstOrNull()?.measure(Constraints(maxWidth = constraints.maxWidth, minHeight = 0, maxHeight = Constraints.Infinity))
        val w = constraints.maxWidth
        val h = if (constraints.hasBoundedHeight) constraints.maxHeight else (child?.height ?: 0)
        if (child == null) return@Layout layout(w, h) {}
        val scale = if (child.height > h && child.height > 0) h.toFloat() / child.height else 1f
        val scaledH = (child.height * scale).toInt()
        layout(w, h) {
            child.placeWithLayer(x = (w - child.width) / 2, y = (h - scaledH) / 2) {
                scaleX = scale; scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
        }
    }
}

/**
 * Caps the system font scale for a dense, no-scroll dashboard. Beyond [max] the board would have to scale itself
 * down so far that it defeats the purpose; people who need bigger text than this use Settings › Display › Large
 * type, which is built for it. Everything outside the dashboard still follows the system setting in full.
 */
@Composable
fun CapFontScale(max: Float, content: @Composable () -> Unit) {
    val d = LocalDensity.current
    if (d.fontScale <= max) content() else CompositionLocalProvider(LocalDensity provides Density(d.density, max)) { content() }
}
