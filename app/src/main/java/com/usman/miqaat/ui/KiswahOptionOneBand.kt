package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R

/** Approved Traditional Silver Vine artwork. Only the eight border slices are drawn;
 * Arabic remains accessible native text, never generated image lettering.
 */
@Composable
internal fun KiswahOptionOneBand(wide: Boolean) {
    val configuration = LocalConfiguration.current
    val landscape = wide || configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bitmap = ImageBitmap.imageResource(if (landscape) R.drawable.kiswah_option1_landscape else R.drawable.kiswah_option1_portrait)
    val density = LocalDensity.current
    val measure = rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = if (landscape) 0.dp else 5.dp)
        .testTag("kiswah-calligraphy-frame").background(Color(0xFF060605)), contentAlignment = Alignment.Center) {
        // The approved landscape banner is a visual section, not a hairline divider.
        // Size it from viewport height so cutouts/density never collapse the vines and Arabic.
        val height = if (landscape) (configuration.screenHeightDp * .13f).dp.coerceIn(48.dp, 64.dp)
            else maxWidth * (bitmap.height.toFloat() / bitmap.width)
        val rail = if (landscape) height * (38f / bitmap.height) else maxWidth * (46f / bitmap.width)
        val clearance = rail + if (landscape) 3.dp else 4.dp
        val text = if (landscape) Kiswah.BAND else Kiswah.BAND.substringBefore("   ✦   ")
        val base = if (landscape) 24f else 22f
        val style = TextStyle(fontFamily = KiswahLandscapeArabic,
            fontSize = base.sp, lineHeight = (base * 1.45f).sp,
            platformStyle = PlatformTextStyle(includeFontPadding = false), textAlign = TextAlign.Center)
        val availableWidth = with(density) { (maxWidth - clearance * 2).roundToPx() }.coerceAtLeast(1)
        val availableHeight = with(density) { (height - clearance * 2).toPx() }
        fun atSize(size: Float) = style.copy(fontSize = size.sp, lineHeight = (size * 1.45f).sp)
        // Android's larger-font scaling and line rounding are not linear. Measure
        // each candidate with the actual density instead of multiplying one result.
        var low = 1f
        var high = base
        repeat(12) {
            val size = (low + high) / 2
            val layout = measure.measure(text, atSize(size), softWrap = false, maxLines = 1)
            if (layout.size.width <= availableWidth - 2 && layout.size.height <= availableHeight - 2) low = size
            else high = size
        }
        Box(Modifier.fillMaxWidth().height(height), contentAlignment = Alignment.Center) {
            Canvas(Modifier.matchParentSize().testTag("kiswah-band-ornament")) {
                if (!landscape) {
                    drawImage(bitmap, dstSize = IntSize(size.width.toInt(), size.height.toInt()))
                } else {
                    // Nine-slice the approved embroidery: end medallions and vine rails
                    // keep their native proportions while the empty centre becomes wider.
                    val sourceEnd = (bitmap.height * .48f).toInt().coerceAtLeast(1)
                    val sourceRail = (bitmap.height * .27f).toInt().coerceAtLeast(1)
                    val end = (size.height * sourceEnd / bitmap.height).toInt().coerceAtLeast(1)
                    val railHeight = (size.height * sourceRail / bitmap.height).toInt().coerceAtLeast(1)
                    val middleSourceWidth = bitmap.width - sourceEnd * 2
                    val middleWidth = size.width.toInt() - end * 2
                    drawImage(bitmap, IntOffset(sourceEnd, 0), IntSize(middleSourceWidth, sourceRail),
                        IntOffset(end, 0), IntSize(middleWidth, railHeight))
                    drawImage(bitmap, IntOffset(sourceEnd, bitmap.height - sourceRail), IntSize(middleSourceWidth, sourceRail),
                        IntOffset(end, size.height.toInt() - railHeight), IntSize(middleWidth, railHeight))
                    drawImage(bitmap, IntOffset.Zero, IntSize(sourceEnd, bitmap.height),
                        IntOffset.Zero, IntSize(end, size.height.toInt()))
                    drawImage(bitmap, IntOffset(bitmap.width - sourceEnd, 0), IntSize(sourceEnd, bitmap.height),
                        IntOffset(size.width.toInt() - end, 0), IntSize(end, size.height.toInt()))
                }
            }
            Text(text, Modifier.fillMaxWidth().padding(horizontal = clearance, vertical = clearance)
                .testTag("kiswah-calligraphy-band").semantics { contentDescription = text },
                style = atSize(low).copy(brush = Kiswah.goldText), maxLines = 1, softWrap = false)
        }
    }
}
