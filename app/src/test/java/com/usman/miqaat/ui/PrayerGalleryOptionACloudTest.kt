package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import com.usman.miqaat.data.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZonedDateTime

/** Captures actual Compose rendering at the four Option A contract sizes. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class PrayerGalleryOptionACloudTest : ComposeSupport() {
    private fun assertInside(childTag: String, parentTag: String) {
        val child = rule.onNodeWithTag(childTag, true).fetchSemanticsNode().boundsInRoot
        val parent = rule.onNodeWithTag(parentTag, true).fetchSemanticsNode().boundsInRoot
        assertTrue("$childTag outside $parentTag: $child versus $parent",
            child.left >= parent.left - 1f && child.top >= parent.top - 1f &&
                child.right <= parent.right + 1f && child.bottom <= parent.bottom + 1f)
    }

    private fun assertTextFits(tag: String) {
        val node = rule.onNodeWithTag(tag, true).fetchSemanticsNode()
        val layouts = mutableListOf<TextLayoutResult>()
        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        assertTrue("No text layout result for $tag", layouts.isNotEmpty())
        layouts.forEach { layout ->
            assertFalse("$tag text clipped: ${layout.layoutInput.text}",
                layout.multiParagraph.didExceedMaxLines ||
                    (0 until layout.lineCount).any { line ->
                        layout.getLineRight(line) - layout.getLineLeft(line) > node.size.width + 1f
                    })
        }
    }

    private fun settings(language: Language) = AppSettings(
        theme = AppTheme.PRAYER_GALLERY, language = language,
        latitude = -34.02, longitude = 150.80, locationName = "Gledswood Hills NSW",
        zoneId = "Australia/Sydney", locationSet = true, setupDone = true,
        fridayReminders = true, showEndTimes = true, jumuahEnabled = true, jumuahMinutes = 13 * 60 + 15,
        jumuahIqamahMinutes = 13 * 60 + 30, iqamahEnabled = true
    )

    private fun capture(name: String) {
        rule.mainClock.advanceTimeBy(1000)
        rule.waitForIdle()
        val file = File("build/option-a-review/$name.png")
        file.parentFile.mkdirs()
        scenario.onActivity { activity ->
            val view = activity.findViewById<View>(android.R.id.content)
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
        File("${file.path}.txt").writeText(rule.onRoot(true).printToString(100))
    }

    @Test fun optionABoardsRenderAllSixPrayersAtContractSizes() {
        val profiles = listOf(
            Triple(Dev.PHONE_PORTRAIT, "w390dp-h844dp-port-xhdpi", "phone-portrait"),
            Triple(Dev.PHONE_LANDSCAPE, "w844dp-h390dp-land-xhdpi", "phone-landscape"),
            Triple(Dev.TABLET_PORTRAIT, "w768dp-h1024dp-port-xhdpi", "tablet-portrait"),
            Triple(Dev.TABLET_LANDSCAPE, "w1280dp-h800dp-land-xhdpi", "tablet-landscape")
        )
        val failures = mutableListOf<String>()
        for (language in Language.entries) for ((dev, qualifiers, label) in profiles) {
            val settings = settings(language)
            val now = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, settings.zone())
            val state = PrayerEngine.state(settings, now)
            try {
                show(dev, AppTheme.PRAYER_GALLERY, rtl = language == Language.UR,
                    language = language, qualifiers = qualifiers) {
                    HomeRouter(state, settings, HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {}), false, {})
                }
                capture("${label}-${language.name}")
                for (tag in listOf("gallery-friday-a", "gallery-friday-a-art", "gallery-friday-a-prayers"))
                    rule.onNodeWithTag(tag, true).assertIsDisplayed()
                assertTextFits("gallery-friday-a-date")
                if (dev == Dev.PHONE_PORTRAIT) assertTextFits("gallery-friday-a-location")
                if (dev != Dev.PHONE_LANDSCAPE) rule.onNodeWithTag("gallery-friday-a-title", true).assertIsDisplayed()
                if (dev != Dev.PHONE_LANDSCAPE) assertTextFits("gallery-friday-a-title-sub")
                if (dev != Dev.TABLET_LANDSCAPE) {
                    rule.onNodeWithTag("gallery-friday-a-clock", true).assertIsDisplayed()
                    rule.onNodeWithTag("gallery-friday-a-time", true).assertIsDisplayed()
                    assertInside("gallery-friday-a-time", "gallery-friday-a-clock")
                    assertTextFits("gallery-friday-a-time")
                }
                if (dev == Dev.PHONE_LANDSCAPE) {
                    val clock = rule.onNodeWithTag("gallery-friday-a-clock", true).fetchSemanticsNode().boundsInRoot
                    val footer = rule.onNodeWithTag("gallery-friday-a-nav", true).fetchSemanticsNode().boundsInRoot
                    val art = rule.onNodeWithTag("gallery-friday-a-art", true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Landscape clock overlaps footer: $clock versus $footer", clock.bottom <= footer.top + 1f)
                    // The approved landscape garden intentionally continues behind the header.
                    assertTrue("Landscape art extends into footer: $art versus $footer",
                        art.bottom <= footer.top + 1f)
                }
                if (dev == Dev.PHONE_PORTRAIT) {
                    val clock = rule.onNodeWithTag("gallery-friday-a-clock", true).fetchSemanticsNode().boundsInRoot
                    val time = rule.onNodeWithTag("gallery-friday-a-time", true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Portrait time touches clock border: $time versus $clock",
                        clock.bottom - time.bottom >= 3 * app.resources.displayMetrics.density)
                }
                if (dev != Dev.PHONE_LANDSCAPE) rule.onNodeWithTag("gallery-friday-a-actions", true).assertIsDisplayed()
                for (prayer in Prayer.entries) {
                    val row = rule.onNodeWithTag("gallery-friday-a-row-${prayer.name}", true)
                    row.assertIsDisplayed()
                    rule.onNodeWithTag("gallery-friday-a-row-time-${prayer.name}", true).assertIsDisplayed()
                    val bounds = row.fetchSemanticsNode().boundsInRoot
                    assertTrue("${prayer.name} row below 48dp: ${bounds.height}",
                        bounds.height >= 48 * app.resources.displayMetrics.density - .5f)
                }
                if (dev != Dev.PHONE_LANDSCAPE)
                    for (i in 0..2) {
                        assertInside("gallery-friday-a-action-sub-$i", "gallery-friday-a-action-$i")
                        assertTextFits("gallery-friday-a-action-sub-$i")
                    }
            } catch (error: AssertionError) {
                failures += "$language/$label: ${error.message}"
            } finally {
                scenario.close()
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test fun optionAIsFridayOnly() {
        val profiles = listOf(
            Triple(Dev.PHONE_PORTRAIT, "w390dp-h844dp-port-xhdpi", "phone-portrait"),
            Triple(Dev.PHONE_LANDSCAPE, "w844dp-h390dp-land-xhdpi", "phone-landscape"),
            Triple(Dev.TABLET_PORTRAIT, "w768dp-h1024dp-port-xhdpi", "tablet-portrait"),
            Triple(Dev.TABLET_LANDSCAPE, "w1280dp-h800dp-land-xhdpi", "tablet-landscape")
        )
        for (language in Language.entries) for ((dev, qualifiers, label) in profiles) {
            val s = settings(language)
            val friday = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, s.zone())
            val thursday = friday.minusDays(1)
            assertTrue(fridayHomepageActive(s, friday))
            assertFalse(fridayHomepageActive(s, thursday))
            val weekday = PrayerEngine.state(s, thursday)
            try {
                show(dev, AppTheme.PRAYER_GALLERY, rtl = language == Language.UR,
                    language = language, qualifiers = qualifiers) {
                    HomeRouter(weekday, s, HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {}), false, {})
                }
                rule.onRoot(true).assertIsDisplayed()
                assertTrue("Friday Option A rendered on $language/$label weekday",
                    rule.onAllNodesWithTag("gallery-friday-a", true).fetchSemanticsNodes().isEmpty())
            } finally {
                scenario.close()
            }
        }
    }

    @Test fun optionAUrduLargeTextAndTwentyFourHourCaptures() {
        val profiles = listOf(
            Triple(Dev.PHONE_PORTRAIT, "w390dp-h844dp-port-xhdpi", "phone-portrait"),
            Triple(Dev.PHONE_LANDSCAPE, "w844dp-h390dp-land-xhdpi", "phone-landscape"),
            Triple(Dev.TABLET_PORTRAIT, "w768dp-h1024dp-port-xhdpi", "tablet-portrait"),
            Triple(Dev.TABLET_LANDSCAPE, "w1280dp-h800dp-land-xhdpi", "tablet-landscape")
        )
        for ((dev, qualifiers, label) in profiles) for (variant in listOf("UR-115", "EN-24h")) {
            val language = if (variant == "UR-115") Language.UR else Language.EN
            val settings = settings(language).copy(use24h = variant == "EN-24h")
            val now = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, settings.zone())
            val state = PrayerEngine.state(settings, now)
            try {
                show(dev, AppTheme.PRAYER_GALLERY, fontScale = if (variant == "UR-115") 1.15f else 1f,
                    rtl = language == Language.UR, language = language, qualifiers = qualifiers) {
                    HomeRouter(state, settings, HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {}), false, {})
                }
                capture("${label}-${variant}")
                assertTextFits("gallery-friday-a-date")
                if (dev == Dev.PHONE_PORTRAIT) assertTextFits("gallery-friday-a-location")
                if (dev != Dev.PHONE_LANDSCAPE) assertTextFits("gallery-friday-a-title-sub")
                if (dev != Dev.TABLET_LANDSCAPE)
                    assertInside("gallery-friday-a-time", "gallery-friday-a-clock")
                if (dev != Dev.TABLET_LANDSCAPE) assertTextFits("gallery-friday-a-time")
                if (dev != Dev.PHONE_LANDSCAPE)
                    for (i in 0..2) {
                        assertInside("gallery-friday-a-action-sub-$i", "gallery-friday-a-action-$i")
                        assertTextFits("gallery-friday-a-action-sub-$i")
                    }
                for (prayer in Prayer.entries) {
                    rule.onNodeWithTag("gallery-friday-a-row-${prayer.name}", true).assertIsDisplayed()
                    rule.onNodeWithTag("gallery-friday-a-row-time-${prayer.name}", true).assertIsDisplayed()
                }
            } finally {
                scenario.close()
            }
        }
    }
}

