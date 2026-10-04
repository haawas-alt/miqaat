package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.usman.miqaat.data.*
import org.junit.Assert.*
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
class LandscapeConceptAcceptanceTest : ComposeSupport() {
    @Test fun approvedBoardsFitWithoutEitherColumnScrolling() {
        for (theme in listOf(AppTheme.CELESTIAL_MERIDIAN, AppTheme.PRAYER_GALLERY))
            for (small in listOf(false, true)) for (urdu in listOf(false, true))
                for (relative in listOf(false, true)) for (hour in listOf(2, 6, 18, 22)) {
                    val s = AppSettings(theme = theme, language = if (urdu) Language.UR else Language.EN,
                        locationName = "Niagara Falls, Ontario", latitude = 43.0896, longitude = -79.0849,
                        locationSet = true, setupDone = true, zoneId = "America/Toronto", kidsMode = true,
                        showRelative = relative)
                    val state = PrayerEngine.state(s, ZonedDateTime.of(2026, 10, 4, hour, 20, 0, 0, ZoneId.of(s.zoneId)))
                    var learn = false
                    var firstMenuLabel = ""
                    val a = HomeActions({}, {}, {}, {}, {}, {}, { learn = true }, false, {}, {})
                    app.settings.update { s }
                    try {
                        show(Dev.PHONE_LANDSCAPE, theme, rtl = urdu,
                            qualifiers = if (small) "w740dp-h360dp-land-xhdpi" else null) {
                            firstMenuLabel = doors(state, s, a).first().label
                            // Explicit usable-space reserve: status bar plus landscape side navigation.
                            Box(Modifier.fillMaxSize().padding(top = 24.dp, end = 24.dp)) {
                                HomeRouter(state, s, a, false, {})
                            }
                        }
                        scenario.onActivity { act ->
                            val view = act.findViewById<View>(android.R.id.content)
                            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                            view.draw(Canvas(bitmap))
                            File("build/audit-screens/landscape-concepts").also { it.mkdirs() }
                                .resolve("home__${theme.name.lowercase()}__${if (small) "740x360" else "891x411"}__hour-$hour${if (urdu) "__ur" else ""}${if (relative) "__relative" else ""}.png")
                                .outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                            bitmap.recycle()
                        }
                        val board = rule.onNodeWithTag("landscape-fit-board", true).getUnclippedBoundsInRoot()
                        val noScroll = SemanticsMatcher("Home must have no scrolling container") {
                            it.config.getOrNull(SemanticsActions.ScrollBy) != null
                        }
                        assertEquals("Scrolling Home is a release failure", 0, rule.onAllNodes(noScroll, true).fetchSemanticsNodes().size)
                        fun inside(tag: String, minTarget: Boolean = false) {
                            val query = rule.onNodeWithTag(tag, true)
                            query.assertIsDisplayed()
                            val r = query.getUnclippedBoundsInRoot()
                            assertTrue("$tag exceeds usable screen", r.left >= board.left && r.right <= board.right && r.top >= board.top && r.bottom <= board.bottom)
                            if (minTarget) assertTrue("$tag must be at least 48dp", (r.bottom - r.top).value >= 47.99f && (r.right - r.left).value >= 48f)
                        }
                        fun readable(tag: String) {
                            inside(tag)
                            val node = rule.onNodeWithTag(tag, true).fetchSemanticsNode()
                            val layouts = mutableListOf<TextLayoutResult>()
                            node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                            assertTrue("Missing text layout for $tag", layouts.isNotEmpty())
                            layouts.forEach { assertFalse("Clipped $tag: theme=$theme small=$small urdu=$urdu relative=$relative hour=$hour size=${it.size} width=${it.didOverflowWidth} height=${it.didOverflowHeight}",
                                it.didOverflowWidth || it.didOverflowHeight) }
                        }
                        val location = rule.onNodeWithTag("landscape-location", true).getUnclippedBoundsInRoot()
                        val date = rule.onNodeWithTag("landscape-date", true).getUnclippedBoundsInRoot()
                        assertTrue("Date overlaps location", location.right <= date.left || date.right <= location.left)
                        inside("landscape-authored-hero")
                        inside("landscape-timeline-panel")
                        inside("landscape-more-actions", true)
                        inside("landscape-hero-details", true)
                        for (tag in listOf("landscape-location", "landscape-date", "landscape-hero-name", "landscape-hero-clock", "landscape-hero-status", "landscape-hijri")) readable(tag)
                        rule.onNodeWithTag("landscape-hero-status", true).assertTextEquals(heroInfo(state, s, null).status)
                        for (prayer in Prayer.entries) {
                            inside("compact-prayer-${prayer.name}", true)
                            readable("landscape-row-name-${prayer.name}")
                            readable("landscape-row-time-${prayer.name}")
                            readable("landscape-timeline-${prayer.name}")
                        }
                        if (theme == AppTheme.CELESTIAL_MERIDIAN) {
                            inside("landscape-countdown-dial")
                            if (!state.justPassed) assertTrue("Live countdown arc must preserve elapsed and remaining portions, including overnight", landscapeCountdownFraction(state) in 0.001f..0.999f)
                            readable("landscape-countdown-value")
                            rule.onNodeWithTag("landscape-countdown-value", true).assertTextEquals(L10n.duration(s, state.delta))
                        } else {
                            inside("landscape-gallery-art")
                            inside("landscape-gallery-learn", true)
                            rule.onNodeWithTag("landscape-gallery-learn", true).performClick()
                            assertTrue("Prepare shortcut must open Learn Salah without scrolling", learn)
                        }
                        rule.onNodeWithTag("landscape-more-actions", true).performClick()
                        // Qibla and timely Adhkar remain accessible, without consuming Home height.
                        rule.onNodeWithText(firstMenuLabel).assertExists()
                    } finally { runCatching { scenario.close() } }
                }
    }
}
