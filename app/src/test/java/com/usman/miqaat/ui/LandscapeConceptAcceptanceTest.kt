package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextLayoutResult
import com.usman.miqaat.R
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
    @Test fun authoredHeroUsesSpareSpaceAndPreservesDetailsAndDestinations() {
        for (theme in listOf(AppTheme.CELESTIAL_MERIDIAN, AppTheme.PRAYER_GALLERY))
            for (small in listOf(false, true)) for (urdu in listOf(false, true)) for (hour in listOf(2, 6, 18, 22)) {
                val s = AppSettings(theme = theme, language = if (urdu) Language.UR else Language.EN,
                    locationName = "Oakville, Ontario", latitude = 43.4675, longitude = -79.6877,
                    locationSet = true, setupDone = true, zoneId = "America/Toronto", kidsMode = true)
                val state = PrayerEngine.state(s, ZonedDateTime.of(2026, 10, 4, hour, 20, 0, 0, ZoneId.of(s.zoneId)))
                var learn = false
                val a = HomeActions({}, {}, {}, {}, {}, {}, { learn = true }, false, {}, {})
                app.settings.update { s }
                try {
                    show(Dev.PHONE_LANDSCAPE, theme, rtl = urdu,
                        qualifiers = if (small) "w740dp-h360dp-land-xhdpi" else null) {
                        CompactThemeHome(state, s, a)
                    }
                    scenario.onActivity { act ->
                        val view = act.findViewById<View>(android.R.id.content)
                        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                        view.draw(Canvas(bitmap))
                        File("build/audit-screens/landscape-concepts").also { it.mkdirs() }
                            .resolve("home__${theme.name.lowercase()}__${if (small) "740x360" else "891x411"}__hour-$hour${if (urdu) "__ur" else ""}.png")
                            .outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        bitmap.recycle()
                    }
                    val root = rule.onRoot().fetchSemanticsNode().boundsInRoot
                    val hero = rule.onNodeWithTag("landscape-authored-hero", true).fetchSemanticsNode().boundsInRoot
                    val visualTag = if (theme == AppTheme.PRAYER_GALLERY) "landscape-gallery-art" else "landscape-countdown-dial"
                    val visual = rule.onNodeWithTag(visualTag, true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Visual must fill the spare hero space", visual.width >= 80 * app.resources.displayMetrics.density)
                    assertTrue("Visual must remain inside hero", visual.left >= hero.left && visual.right <= hero.right && visual.top >= hero.top && visual.bottom <= hero.bottom)
                    assertTrue("Hero must remain inside horizontal viewport", hero.left >= root.left && hero.right <= root.right)
                    val detail = rule.onNodeWithTag("landscape-hero-details", true).getUnclippedBoundsInRoot()
                    assertTrue("Details target must retain its full size inside the scrolling column", detail.height.value >= 48f && detail.width.value >= 48f)
                    for (tag in listOf("landscape-hero-name", "landscape-hero-clock")) {
                        val node = rule.onNodeWithTag(tag, true).fetchSemanticsNode()
                        val layouts = mutableListOf<TextLayoutResult>()
                        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                        assertTrue("Missing text layout", layouts.isNotEmpty())
                        layouts.forEach { assertFalse("Hero text clips: $tag / $theme / urdu=$urdu small=$small",
                            it.didOverflowWidth || it.didOverflowHeight) }
                        assertTrue("Hero text overlaps illustration", node.boundsInRoot.right <= visual.left || visual.right <= node.boundsInRoot.left)
                    }
                    if (theme == AppTheme.CELESTIAL_MERIDIAN)
                        rule.onNodeWithTag("landscape-countdown-value", true).assertTextEquals(L10n.duration(s, state.delta))
                    else rule.onNodeWithTag("landscape-hero-status", true).assertTextEquals(heroInfo(state, s, null).status)
                    for (prayer in Prayer.entries) {
                        rule.onNodeWithTag("compact-prayer-${prayer.name}", true).assertExists()
                    }
                    if (theme == AppTheme.PRAYER_GALLERY) {
                        rule.onNodeWithTag("landscape-gallery-learn", true).performScrollTo().performClick()
                        assertTrue("Learn shortcut must open the existing destination", learn)
                    }
                } finally { runCatching { scenario.close() } }
            }
    }
}
