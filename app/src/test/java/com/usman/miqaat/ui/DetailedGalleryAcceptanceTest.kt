package com.usman.miqaat.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextLayoutResult
import org.junit.Assert.assertFalse
import com.usman.miqaat.data.*
import com.usman.miqaat.R
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class DetailedGalleryAcceptanceTest : ComposeSupport() {
    @Test fun mobilePrayerAndClockShareTheHeroLineAndRemainInsideTheScreen() {
        for (small in listOf(false, true)) for (urdu in listOf(false, true)) for (hour in listOf(2, 12, 18, 22)) {
            val s = AppSettings(theme = AppTheme.PRAYER_GALLERY, language = if (urdu) Language.UR else Language.EN,
                locationName = "Oakville, Ontario", latitude = 43.4675, longitude = -79.6877, locationSet = true, setupDone = true,
                zoneId = "America/Toronto", kidsMode = true)
            val state = PrayerEngine.state(s, ZonedDateTime.of(2026, 10, 3, hour, 20, 0, 0, ZoneId.of(s.zoneId)))
            val a = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})
            app.settings.update { s }
            try {
                show(Dev.PHONE_PORTRAIT, s.theme, rtl = urdu,
                    qualifiers = if (small) "w360dp-h800dp-port-xhdpi" else null) {
                    androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier
                        .then(if (small) androidx.compose.ui.Modifier.padding(top = 24.dp, bottom = 48.dp) else androidx.compose.ui.Modifier)) {
                        GalleryHome(state, s, a)
                    }
                }
                rule.waitForIdle()
                scenario.onActivity { act ->
                    val view = act.findViewById<View>(android.R.id.content)
                    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    view.draw(Canvas(bitmap))
                    File("build/audit-screens/detailed-gallery").also { it.mkdirs() }
                        .resolve("home__phone-portrait__100__${if (small) "360-insets__" else ""}hour-$hour${if (urdu) "__ur" else ""}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bitmap.recycle()
                }
                val root = rule.onRoot().fetchSemanticsNode().boundsInRoot
                val fit = rule.onNodeWithTag("gallery-phone-fit", true).fetchSemanticsNode()
                assertTrue("Normal phone home must not scroll",
                    fit.config.getOrNull(androidx.compose.ui.semantics.SemanticsActions.ScrollBy) == null)
                val footer = rule.onNodeWithTag("gallery-phone-footer", true).fetchSemanticsNode().boundsInRoot
                assertTrue("Footer must fit the viewport: $footer / $root", footer.bottom <= root.bottom)
                for (prayer in listOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)) {
                    val row = rule.onNodeWithTag("gallery-fit-row-${prayer.name}", true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Prayer row outside viewport: $prayer / $row", row.top >= root.top && row.bottom <= footer.top)
                    assertTrue("Prayer row smaller than a touch target: $prayer / $row", row.height >= 48f * app.resources.displayMetrics.density)
                    for (kind in listOf("label", "detail")) {
                        val node = rule.onNodeWithTag("gallery-fit-$kind-${prayer.name}", true).fetchSemanticsNode()
                        val layouts = mutableListOf<TextLayoutResult>()
                        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                        layouts.forEach {
                            assertFalse("Clipped $kind: $prayer, urdu=$urdu small=$small", it.didOverflowWidth || it.didOverflowHeight)
                            assertTrue("Row clips $kind: $prayer, urdu=$urdu small=$small",
                                node.boundsInRoot.height + 1f >= it.size.height.toFloat())
                            assertTrue("Text exceeds row: $prayer/$kind", node.boundsInRoot.top >= row.top && node.boundsInRoot.bottom <= row.bottom)
                        }
                    }
                }
                val name = rule.onNodeWithTag("gallery-mobile-prayer-name", true).fetchSemanticsNode().boundsInRoot
                val time = rule.onNodeWithTag("gallery-mobile-prayer-time", true).fetchSemanticsNode().boundsInRoot
                assertTrue("Prayer and time must occupy the same line", name.top < time.bottom && time.top < name.bottom)
                assertTrue("Clock must remain inside viewport", time.left >= root.left && time.right <= root.right)
                assertTrue("Name and time must not overlap: $name / $time / $root", name.right <= time.left || time.right <= name.left)
                for (description in listOf(Str[R.string.s_learn_salah], Str[R.string.s_monthly_timetable], Str[R.string.s_settings])) {
                    val target = rule.onNodeWithContentDescription(description).fetchSemanticsNode().boundsInRoot
                    val minimum = 48f * app.resources.displayMetrics.density
                    assertTrue("Small navigation target: $description / $target", target.width >= minimum && target.height >= minimum)
                }
                for (tag in listOf("gallery-mobile-prayer-name", "gallery-mobile-prayer-time")) {
                    val layouts = mutableListOf<TextLayoutResult>()
                    rule.onNodeWithTag(tag, true).fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                    assertTrue("Missing layout for $tag", layouts.isNotEmpty())
                    layouts.forEach { assertFalse("$tag overflow; urdu=$urdu hour=$hour; text=${it.layoutInput.text}; size=${it.size}; constraints=${it.layoutInput.constraints}; paragraph=${it.multiParagraph.width} x ${it.multiParagraph.height}; width=${it.didOverflowWidth}; height=${it.didOverflowHeight}", it.didOverflowWidth || it.didOverflowHeight) }
                }
            } finally { runCatching { scenario.close() } }
        }
    }
}
