package com.usman.miqaat.ui

import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.Prayer
import org.junit.Assert.*
import org.junit.Test

class IqamahCounterTest {
    private val phase = Phase.IqamahCountdown(Prayer.MAGHRIB, 0, 600_000)
    @Test fun roundsPartialSecondsUpAndChangesAtTheDeadline() {
        assertEquals("03:00", iqamahCounter(phase, 420_000).label)
        assertEquals("03:00", iqamahCounter(phase, 420_001).label)
        assertEquals("02:59", iqamahCounter(phase, 421_000).label)
        assertEquals("00:01", iqamahCounter(phase, 599_999).label)
        assertEquals("00:00", iqamahCounter(phase, 600_000).label)
        assertEquals("00:00", iqamahCounter(phase, 600_001).label)
    }
    @Test fun progressUsesTheWholeServiceIntervalAndClampsClockChanges() {
        assertEquals(.3f, iqamahCounter(phase, 420_000).fraction, .0001f)
        assertEquals(1f, iqamahCounter(phase, -1_000).fraction, .0001f)
        assertEquals(0f, iqamahCounter(phase, 700_000).fraction, .0001f)
        assertEquals(0f, iqamahCounter(Phase.IqamahCountdown(Prayer.ISHA, 100, 100), 100).fraction, .0001f)
    }
    @Test fun longIntervalsDoNotWrapLikeAClock() {
        assertEquals("120:00", iqamahCounter(Phase.IqamahCountdown(Prayer.ISHA, 0, 7_200_000), 0).label)
    }
}
