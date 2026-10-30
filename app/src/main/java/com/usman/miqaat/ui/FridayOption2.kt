package com.usman.miqaat.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import kotlinx.coroutines.delay

/** Deterministic peak frame for native review capture; never enabled by the production router. */
internal val LocalFridayRevealFrame = staticCompositionLocalOf<Float?> { null }
internal const val FRIDAY_REVEAL_PERIOD_MS = 300_000L
internal const val FRIDAY_REVEAL_DURATION_MS = 8_000L
internal fun fridayRevealEnvelope(elapsedMillis: Long): Float {
    val phase = Math.floorMod(elapsedMillis, FRIDAY_REVEAL_PERIOD_MS)
    return when {
        phase < 1_000 -> phase / 1_000f
        phase < 7_000 -> 1f
        phase < FRIDAY_REVEAL_DURATION_MS -> (FRIDAY_REVEAL_DURATION_MS - phase) / 1_000f
        else -> 0f
    }
}

/** Friday-only palette. Weekday and quiet-mode token sets are deliberately untouched. */
internal fun fridayOption2Palette(theme: AppTheme): QuietPalette = when (theme) {
    AppTheme.MIQAAT -> QuietPalette(Color(0xFFF6EFE2), Color(0xFF103E37), Color(0xFF9E4027), Color(0xFF3F584E), Color(0xFFF9F2E6).copy(alpha=.90f))
    AppTheme.KISWAH -> quietPalette(theme)
    else -> QuietPalette(Color(0xFF03152D), Color(0xFFF5ECD8), Color(0xFFE7C77E), Color(0xFFD8CFBD), Color(0xFF061C35).copy(alpha=.72f))
}

@Composable internal fun FridayOption2Backdrop(s: AppSettings, wide: Boolean, modifier: Modifier) {
    val c = fridayOption2Palette(s.theme)
    Box(modifier.background(c.background).testTag("friday-option2-art-${s.theme.name}")) {
        if (s.artTheme != ArtTheme.MINIMAL && s.theme != AppTheme.MIQAAT) {
            val art = when (s.theme) {
                AppTheme.MIQAAT -> if (wide) R.drawable.sanctuary_gallery_land else R.drawable.sanctuary_gallery_port
                AppTheme.KISWAH -> if (wide) R.drawable.kiswah_tablet_approved else R.drawable.kiswah_tablet_portrait_approved
                AppTheme.CELESTIAL_MERIDIAN -> R.drawable.celestial_landscape_approved
                AppTheme.PRAYER_GALLERY -> if (wide) R.drawable.quiet_miqaat_landscape else R.drawable.approved_miqaat_arch
            }
            Image(painterResource(art), null, Modifier.fillMaxSize(), contentScale=ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                0f to c.background.copy(alpha=.78f), .20f to c.background.copy(alpha=.32f),
                .65f to c.background.copy(alpha=.16f), 1f to c.background.copy(alpha=.66f))))
        }
    }
}

/** One calm 8-second light reveal per five minutes while the home is visible.
 * Lifecycle cancellation stops work in the background; quiet mode never composes this component.
 * Reduced-motion users get a static fine outline, without pulses or moving particles.
 */
@Composable internal fun FridayMihrabReveal(s: AppSettings, modifier: Modifier) {
    val frame = LocalFridayRevealFrame.current
    val still = reduceMotion()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(lifecycle, still, frame, s.theme) {
        if (still || frame != null) return@LaunchedEffect
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                alpha.snapTo(0f)
                alpha.animateTo(1f, tween(1_000))
                delay(6_000)
                alpha.animateTo(0f, tween(1_000))
                delay(FRIDAY_REVEAL_PERIOD_MS - FRIDAY_REVEAL_DURATION_MS)
            }
        }
    }
    val intensity = frame?.coerceIn(0f,1f) ?: if (still) .28f else alpha.value
    val gold = if(s.theme == AppTheme.MIQAAT) Color(0xFFB98A43) else Color(0xFFE7C77E)
    Canvas(modifier.testTag("friday-mihrab-reveal")) {
        if (intensity <= 0f || s.artTheme == ArtTheme.MINIMAL) return@Canvas
        val h=size.height
        val w=minOf(size.width, h * 1.12f)
        val inset=(size.width-w)/2f
        val arch=Path().apply {
            moveTo(inset+w*.09f,h*.97f); lineTo(inset+w*.09f,h*.47f)
            cubicTo(inset+w*.09f,h*.26f,inset+w*.37f,h*.13f,inset+w*.5f,h*.025f)
            cubicTo(inset+w*.63f,h*.13f,inset+w*.91f,h*.26f,inset+w*.91f,h*.47f)
            lineTo(inset+w*.91f,h*.97f)
        }
        for ((width, opacity) in listOf(22f to .025f, 12f to .05f, 6f to .12f, 2f to .85f))
            drawPath(arch, gold.copy(alpha=intensity*opacity), style=Stroke(width=width*density))
        // Small stationary geometric lights emphasize the arch without obscuring the prayer facts.
        for (side in listOf(.09f,.91f)) for (level in listOf(.48f,.70f,.91f)) {
            val x=inset+w*side; val y=h*level; val r=3*density
            drawLine(gold.copy(alpha=intensity*.7f),Offset(x-r,y),Offset(x+r,y),density)
            drawLine(gold.copy(alpha=intensity*.7f),Offset(x,y-r),Offset(x,y+r),density)
        }
    }
}
