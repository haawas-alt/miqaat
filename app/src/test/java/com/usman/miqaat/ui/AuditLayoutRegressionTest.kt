package com.usman.miqaat.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import com.usman.miqaat.azaan.AzaanService
import com.usman.miqaat.data.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

/** Targeted regressions for independently observed failures; no golden-fidelity claim. No emulator required. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AuditLayoutRegressionTest : ComposeSupport() {
    private val actions = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})
    private fun settings(theme: AppTheme, urdu: Boolean = false) = AppSettings(theme = theme,
        language = if (urdu) Language.UR else Language.EN, locationName = "Sydney, Australia",
        latitude = -33.8688, longitude = 151.2093, locationSet = true, setupDone = true, zoneId = "Australia/Sydney")
    private fun state(s: AppSettings) = PrayerEngine.state(s, ZonedDateTime.of(2026, 9, 30, 15, 40, 0, 0, ZoneId.of("Australia/Sydney")))
    // PixelCopy-backed captureToImage does not deliver callbacks in this Robolectric host.
    // Draw the measured Android content view with native graphics; bounds assertions remain Compose semantics.
    private fun renderBitmap(): android.graphics.Bitmap {
        rule.waitForIdle()
        lateinit var bitmap: android.graphics.Bitmap
        scenario.onActivity { activity ->
            val view = activity.findViewById<android.view.View>(android.R.id.content)
            assertTrue("Unmeasured content view", view.width > 0 && view.height > 0)
            bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
        }
        return bitmap
    }
    private fun capture(name: String) {
        val out = File("build/audit-screens").also { it.mkdirs() }
        File(out, "$name.png").outputStream().use { assertTrue(renderBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)) }
    }
    private fun inside(tag: String) {
        val root = rule.onRoot().getUnclippedBoundsInRoot()
        val b = rule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("$tag outside viewport: $b vs $root", b.left >= root.left && b.top >= root.top && b.right <= root.right && b.bottom <= root.bottom)
    }
    private fun noOverflow(node: SemanticsNodeInteraction) {
        val results = mutableListOf<TextLayoutResult>()
        node.fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(results)
        assertTrue("Missing text layout", results.isNotEmpty())
        results.forEach { assertFalse("Text clipped: ${it.layoutInput.text}; size=${it.size}; constraints=${it.layoutInput.constraints}; widthOverflow=${it.didOverflowWidth}; heightOverflow=${it.didOverflowHeight}", it.didOverflowWidth || it.didOverflowHeight) }
    }
    @Test fun allEightPosturesHaveDistinctArtworkWithoutLabels() {
        val hashes = mutableSetOf<Int>()
        for (posture in Learn.Posture.entries) {
            try {
                show(Dev.TABLET_PORTRAIT) {
                    androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Figure(posture, androidx.compose.ui.graphics.Color(0xFFB68D46),
                            androidx.compose.ui.Modifier.testTag("posture-art").size(320.dp))
                    }
                }
                val bitmap = renderBitmap()
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                assertTrue("Repeated artwork for $posture", hashes.add(pixels.contentHashCode()))
                capture("posture-art__${posture.name.lowercase()}")
            } finally { scenario.close() }
        }
        assertTrue(hashes.size == 8)
    }
    @Test fun newThemeRtlTabletBoardsKeepHeroTimeAndPrayersInsideViewport() {
        for (theme in listOf(AppTheme.CELESTIAL_MERIDIAN, AppTheme.PRAYER_GALLERY)) {
            val s = settings(theme, true)
            app.settings.update { s }
            try {
                show(Dev.TABLET_LANDSCAPE, theme, rtl = true) { HomeRouter(state(s), s, actions, false) {} }
                inside("home-hero"); inside("home-prayers")
                if (theme == AppTheme.PRAYER_GALLERY) inside("home-time")
                capture("home__${theme.name.lowercase()}__tablet-landscape__100__ur")
            } finally { scenario.close() }
        }
    }
    @Test fun twoHundredPercentFlowHeaderDoesNotOverlapAndTitleIsScrollable() {
        for (theme in AppTheme.entries) for (dev in listOf(Dev.PHONE_PORTRAIT, Dev.TABLET_PORTRAIT)) {
            app.settings.update { settings(theme) }
            try {
                show(dev, theme, fontScale = 2f) { AzaanScreen(AzaanService.Phase.Azaan(Prayer.DHUHR, false), {}, {}) }
                val status = rule.onNodeWithTag("flow-status").getUnclippedBoundsInRoot()
                val clock = rule.onNodeWithTag("flow-clock").getUnclippedBoundsInRoot()
                val steps = rule.onNodeWithTag("flow-steps").getUnclippedBoundsInRoot()
                assertTrue("Status overlaps clock", status.bottom <= clock.top)
                assertTrue("Clock overlaps steps", clock.bottom <= steps.top)
                noOverflow(rule.onNodeWithTag("flow-status"))
                rule.onNodeWithTag("flow-prayer-title").performScrollTo().assertIsDisplayed()
                noOverflow(rule.onNodeWithTag("flow-prayer-title"))
                rule.onNodeWithText(Str[com.usman.miqaat.R.string.s_stop_azaan]).assertIsDisplayed()
                capture("azaan__${theme.name.lowercase()}__${dev.label}__200")
            } finally { scenario.close() }
        }
    }
    @Test fun shortLandscapeNewThemeHomesKeepEveryPrayerReachableInBothLanguages() {
        for (theme in listOf(AppTheme.CELESTIAL_MERIDIAN, AppTheme.PRAYER_GALLERY)) for (urdu in listOf(false, true)) {
            val s = settings(theme, urdu)
            app.settings.update { s }
            try {
                show(Dev.PHONE_LANDSCAPE, theme, rtl = urdu) { HomeRouter(state(s), s, actions, false) {} }
                capture("home__${theme.name.lowercase()}__phone-landscape__100${if (urdu) "__ur" else ""}")
                for (prayer in listedPrayers(s)) {
                    val node = rule.onNodeWithTag("compact-prayer-${prayer.name}")
                    node.performScrollTo().assertIsDisplayed()
                    inside("compact-prayer-${prayer.name}")
                }
                capture("home__${theme.name.lowercase()}__phone-landscape__100${if (urdu) "__ur" else ""}__last-prayer")
            } finally { scenario.close() }
        }
    }
    @Test fun tabletPortraitLessonsKeepAllEightPostureCuesReachableAtLargeText() {
        for (theme in AppTheme.entries) {
            val s = settings(theme)
            app.settings.update { s }
            val actions = Learn.actions(Learn.Lesson.FAJR)
            for (posture in Learn.Posture.entries) {
                val index = actions.indexOfFirst { it.posture == posture }
                try {
                    show(Dev.TABLET_PORTRAIT, theme, fontScale = 2f) {
                        LearnScreen(s, previewLesson = Learn.Lesson.FAJR, previewStep = index) {}
                    }
                    val cue = rule.onNodeWithText(actions[index].cue)
                    cue.performScrollTo().assertIsDisplayed()
                    capture("lesson__${posture.name.lowercase()}__${theme.name.lowercase()}__tablet-portrait__200")
                    noOverflow(cue)
                } finally { scenario.close() }
            }
        }
    }
    @Test fun activeHadithStepStaysVisibleOnPhoneAtTwoHundredPercent() {
        for (theme in AppTheme.entries) {
            app.settings.update { settings(theme) }
            try {
                show(Dev.PHONE_PORTRAIT, theme, fontScale = 2f) {
                    AzaanScreen(AzaanService.Phase.HadithPhase(Prayer.DHUHR, HadithLibrary.all.first(),
                        System.currentTimeMillis(), System.currentTimeMillis() + 60_000, false), {}, {})
                }
                inside("flow-current-step")
                capture("hadith__${theme.name.lowercase()}__phone-portrait__200__active-step")
            } finally { scenario.close() }
        }
    }
    @Test fun qiblaInstructionsRemainReachableAtTwoHundredPercent() {
        for (theme in AppTheme.entries) {
            val s = settings(theme)
            try {
                show(Dev.TABLET_PORTRAIT, theme, fontScale = 2f) { QiblaScreen(s, fixedHeading = 250f) {} }
                rule.onNodeWithTag("qibla-instruction").performScrollTo().assertIsDisplayed()
                noOverflow(rule.onNodeWithTag("qibla-instruction"))
                capture("qibla__${theme.name.lowercase()}__tablet-portrait__200__instruction")
            } finally { scenario.close() }
        }
    }
    @Test fun shortLandscapeSettingsShowsSelectedCategoryControlsWithoutReadinessBlocking() {
        for (theme in AppTheme.entries) {
            val s = settings(theme)
            app.settings.update { s }
            try {
                show(Dev.PHONE_LANDSCAPE, theme, fontScale = 2f) { SettingsScreen(app.settings, s, Section.DISPLAY) {} }
                rule.onNodeWithText(Str[com.usman.miqaat.R.string.s_how_miqaat_looks_on_the_wall]).assertIsDisplayed()
                rule.onAllNodes(hasContentDescription("Clean and modern", substring = true)).onFirst().performScrollTo().assertIsDisplayed()
                capture("settings-display__${theme.name.lowercase()}__phone-landscape__200")
            } finally { scenario.close() }
        }
    }

    @Test fun adhkarMeaningAndNavigationStayReachableInBothLanguagesAtLargeText() {
        for (theme in AppTheme.entries) for (dev in listOf(Dev.PHONE_PORTRAIT, Dev.PHONE_LANDSCAPE)) for (urdu in listOf(false, true)) {
            val s = settings(theme, urdu)
            app.settings.update { s }
            try {
                show(dev, theme, fontScale = 2f, rtl = urdu) { AdhkarScreen(AdhkarMode.MORNING) {} }
                val dhikr = Adhkar.morning().first()
                val meaning = rule.onNodeWithText(if (urdu) UrduContent.dhikrMeanings.getValue(dhikr.id) else dhikr.english)
                meaning.performScrollTo().assertIsDisplayed()
                noOverflow(meaning)
                val next = rule.onNodeWithText(Str[com.usman.miqaat.R.string.s_next_2])
                next.assertIsDisplayed()
                val bounds = next.getUnclippedBoundsInRoot()
                assertTrue("Next target too small: $bounds", bounds.bottom - bounds.top >= 48.dp && bounds.right - bounds.left >= 48.dp)
                capture("adhkar-meaning__${theme.name.lowercase()}__${dev.label}__200${if (urdu) "__ur" else ""}")
            } finally { scenario.close() }
        }
    }
}
