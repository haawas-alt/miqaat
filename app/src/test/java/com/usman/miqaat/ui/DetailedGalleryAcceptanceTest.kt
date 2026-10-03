package com.usman.miqaat.ui

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
        for (urdu in listOf(false, true)) for (hour in listOf(2, 12, 18, 22)) {
            val s = AppSettings(theme = AppTheme.PRAYER_GALLERY, language = if (urdu) Language.UR else Language.EN,
                locationName = "Oakville, Ontario", latitude = 43.4675, longitude = -79.6877, locationSet = true, setupDone = true,
                zoneId = "America/Toronto", kidsMode = true)
            val state = PrayerEngine.state(s, ZonedDateTime.of(2026, 10, 3, hour, 20, 0, 0, ZoneId.of(s.zoneId)))
            val a = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})
            app.settings.update { s }
            try {
                show(Dev.PHONE_PORTRAIT, s.theme, rtl = urdu) { GalleryHome(state, s, a) }
                val root = rule.onRoot().fetchSemanticsNode().boundsInRoot
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
                    layouts.forEach { assertFalse("$tag overflow", it.didOverflowWidth || it.didOverflowHeight) }
                }
                rule.waitForIdle()
                scenario.onActivity { act ->
                    val view = act.findViewById<View>(android.R.id.content)
                    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                    view.draw(Canvas(bitmap))
                    File("build/audit-screens/detailed-gallery").also { it.mkdirs() }
                        .resolve("home__phone-portrait__100__hour-$hour${if (urdu) "__ur" else ""}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    bitmap.recycle()
                }
            } finally { runCatching { scenario.close() } }
        }
    }
}
