package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.test.*
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
    private fun settings(language: Language) = AppSettings(
        theme = AppTheme.PRAYER_GALLERY, language = language,
        latitude = -33.93, longitude = 151.19, locationName = "Mascot, NSW",
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
                if (dev != Dev.PHONE_LANDSCAPE) rule.onNodeWithTag("gallery-friday-a-title", true).assertIsDisplayed()
                if (dev != Dev.TABLET_LANDSCAPE) {
                    rule.onNodeWithTag("gallery-friday-a-clock", true).assertIsDisplayed()
                    rule.onNodeWithTag("gallery-friday-a-time", true).assertIsDisplayed()
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
            } catch (error: AssertionError) {
                failures += "$language/$label: ${error.message}"
            } finally {
                scenario.close()
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    @Test fun optionAIsFridayOnly() {
        val s = settings(Language.EN)
        val friday = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, s.zone())
        val thursday = friday.minusDays(1)
        assertTrue(fridayHomepageActive(s, friday))
        assertFalse(fridayHomepageActive(s, thursday))
        val weekday = PrayerEngine.state(s, thursday)
        show(Dev.PHONE_PORTRAIT, AppTheme.PRAYER_GALLERY) {
            HomeRouter(weekday, s, HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {}), false, {})
        }
        assertTrue(rule.onAllNodesWithTag("gallery-friday-a", true).fetchSemanticsNodes().isEmpty())
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
