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
    val landscape = wide || LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bitmap = ImageBitmap.imageResource(if (landscape) R.drawable.kiswah_option1_landscape else R.drawable.kiswah_option1_portrait)
    val density = LocalDensity.current
    val measure = rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxWidth().padding(vertical = if (landscape) 1.dp else 5.dp)
        .testTag("kiswah-calligraphy-frame").background(Color(0xFF060605)), contentAlignment = Alignment.Center) {
        // One uniform scale preserves every vine and gold corner, including frame proportions.
        val height = maxWidth * (bitmap.height.toFloat() / bitmap.width)
        val rail = maxWidth * ((if (landscape) 38f else 46f) / bitmap.width)
        val clearance = rail + if (landscape) 3.dp else 4.dp
        val text = if (landscape) Kiswah.BAND else Kiswah.BAND.substringBefore("   ✦   ")
        val base = if (landscape) 20f else 22f
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
                drawImage(bitmap, dstSize = IntSize(size.width.toInt(), size.height.toInt()))
            }
            Text(text, Modifier.fillMaxWidth().padding(horizontal = clearance, vertical = clearance)
                .testTag("kiswah-calligraphy-band").semantics { contentDescription = text },
                style = atSize(low).copy(brush = Kiswah.goldText), maxLines = 1, softWrap = false)
        }
    }
}
