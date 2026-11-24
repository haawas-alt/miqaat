package com.usman.miqaat.ui

import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class FridayDevotionalCycleTest {
    @Test fun fourDesignsAlternateWithFiveMinutesOfHomeAndThirtySecondsOfDisplay() {
        val cycle = FridayDevotionalCycle()
        repeat(4) { index ->
            assertNull(cycle.advance(299_999))
            assertEquals(index + 1, cycle.advance(1))
            assertEquals(index + 1, cycle.advance(29_999))
            assertNull(cycle.advance(1))
        }
        assertEquals(1, cycle.advance(300_000))
    }

    @Test fun dismissRestartsFullHomeIntervalWithoutRepeatingTheSameDesign() {
        val cycle = FridayDevotionalCycle()
        assertEquals(1, cycle.advance(300_000))
        cycle.dismiss()
        assertNull(cycle.advance(299_999))
        assertEquals(2, cycle.advance(1))
    }

    @Test fun pauseOrThemeChangeResetsSequenceAndNeverReplaysBacklog() {
        val cycle = FridayDevotionalCycle()
        assertEquals(1, cycle.advance(300_000))
        cycle.reset()
        assertNull(cycle.advance(299_999))
        assertEquals(1, cycle.advance(1))
    }

    @Test fun gaAndWeekdaysNeverEnterRotation() {
        val zone = ZoneId.of("Australia/Sydney")
        val friday = ZonedDateTime.of(2026, 10, 30, 11, 0, 0, 0, zone)
        val settings = AppSettings(zoneId = zone.id, theme = AppTheme.MIQAAT, fridayReminders = true)
        assertTrue(fridayDevotionalEligible(settings, friday))
        assertFalse(fridayDevotionalEligible(settings.copy(theme = AppTheme.PRAYER_GALLERY), friday))
        assertFalse(fridayDevotionalEligible(settings, friday.minusDays(1)))
        assertFalse(fridayDevotionalEligible(settings, friday.plusDays(1)))
        assertFalse(fridayDevotionalEligible(settings.copy(fridayReminders = false), friday))
    }
}
