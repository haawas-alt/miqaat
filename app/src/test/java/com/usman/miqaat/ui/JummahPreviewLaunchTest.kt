package com.usman.miqaat.ui

import androidx.compose.ui.test.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.DayOfWeek
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class JummahPreviewLaunchTest : ComposeSupport() {
    @Test fun tryPreviewLaunchesAndClosesWithoutChangingSavedSettings() {
        for (dev in Dev.entries) {
            val s = AppSettings(locationSet = true, setupDone = true, fridayReminders = false,
                jumuahEnabled = false, latitude = -33.93, longitude = 151.19, zoneId = "Australia/Sydney")
            app.settings.update { s }
            try {
                show(dev, s.theme) { SettingsScreen(app.settings, s, initial = Section.TEST) {} }
                rule.onNode(hasText(Str[R.string.s_preview]) and hasAnyAncestor(hasTestTag("jummah-preview-launch")))
                    .performScrollTo().performClick()
                rule.onNodeWithTag("friday-home", true).assertIsDisplayed()
                rule.onNodeWithTag("friday-hero-name", true).assertTextEquals("JUMUʿAH")
                assertEquals(s, app.settings.value)
                rule.onNodeWithTag("friday-hero-time", true).performClick()
                assertEquals("Relative toggle must remain preview-local", s, app.settings.value)
                rule.onNodeWithTag("jummah-preview-close", true).performClick()
                rule.onNodeWithTag("friday-home", true).assertDoesNotExist()
                rule.onNodeWithText(Str[R.string.s_jummah_homepage]).assertIsDisplayed()
                assertEquals(s, app.settings.value)
            } finally { scenario.close() }
        }
    }
    @Test fun previewUsesLocalFridayAndPreservesConfiguredMasjidTime() {
        val original = AppSettings(zoneId = "Australia/Sydney", jumuahEnabled = true, jumuahMinutes = 14 * 60)
        val preview = jummahPreviewSettings(original)
        val now = jummahPreviewTime(preview, ZonedDateTime.parse("2026-10-26T23:30:00Z"))
        assertEquals(DayOfWeek.FRIDAY, now.dayOfWeek)
        assertEquals(13, now.hour)
        assertEquals(14 * 60, preview.jumuahMinutes)
        assertTrue(fridayHomepageActive(preview, now))
        assertFalse(fridayHomepageActive(original.copy(fridayReminders = false), now))
    }
}
