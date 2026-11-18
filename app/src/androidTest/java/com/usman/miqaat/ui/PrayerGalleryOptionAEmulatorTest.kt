package com.usman.miqaat.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.usman.miqaat.data.*
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.ZonedDateTime

/** Full Android emulator screenshots for the four Friday layouts and their weekday counterparts. */
@RunWith(AndroidJUnit4::class)
class PrayerGalleryOptionAEmulatorTest : ShotSupport() {
    @Test fun fridayAndWeekdayAtActualDeviceSize() {
        for (language in Language.entries) for (friday in listOf(true, false)) {
            val s = settingsFor(AppTheme.PRAYER_GALLERY, language == Language.UR).copy(
                locationName = "Gledswood Hills NSW", latitude = -34.02, longitude = 150.80,
                jumuahEnabled = true, jumuahMinutes = 13 * 60 + 15,
                jumuahIqamahMinutes = 13 * 60 + 30, iqamahEnabled = true,
                showEndTimes = true
            )
            val now = ZonedDateTime.of(2026, 10, if (friday) 30 else 29,
                11, 47, 0, 0, s.zone())
            val state = PrayerEngine.state(s, now)
            try {
                app.settings.update { s }
                host(AppTheme.PRAYER_GALLERY, 1f, language == Language.UR) {
                    HomeRouter(state, s, actions, showLarge = false) {}
                }
                if (friday) rule.onNodeWithTag("gallery-friday-a", true).assertIsDisplayed()
                else {
                    rule.onRoot().assertIsDisplayed()
                    assertTrue(rule.onAllNodesWithTag("gallery-friday-a", true)
                        .fetchSemanticsNodes().isEmpty())
                }
                rule.waitForIdle()
                Thread.sleep(500)
                val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                checkNotNull(bitmap) { "Android did not return a device screenshot" }
                val name = "option-a-${device}-${if (friday) "friday" else "weekday"}-${language.name}.png"
                val screenshotDir = File(app.getExternalFilesDir(null), "option-a-screens").also { it.mkdirs() }
                File(screenshotDir, name).outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                bitmap.recycle()
            } finally {
                runCatching { scenario.close() }
            }
        }
    }
}
