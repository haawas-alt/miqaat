package com.usman.miqaat.ui

import org.junit.Assert.*
import org.junit.Test

class FridayRevealTimingTest {
    @Test fun revealIsEightSecondsWithinFiveMinutesWithoutFlashing() {
        assertEquals(0f, fridayRevealEnvelope(0), 0f)
        assertEquals(.5f, fridayRevealEnvelope(500), .001f)
        assertEquals(1f, fridayRevealEnvelope(1000), 0f)
        assertEquals(1f, fridayRevealEnvelope(6999), 0f)
        assertEquals(.5f, fridayRevealEnvelope(7500), .001f)
        assertEquals(0f, fridayRevealEnvelope(8000), 0f)
        assertEquals(0f, fridayRevealEnvelope(299999), 0f)
        assertEquals(.5f, fridayRevealEnvelope(300500), .001f)
        assertEquals(8000L, FRIDAY_REVEAL_DURATION_MS)
        assertEquals(300000L, FRIDAY_REVEAL_PERIOD_MS)
    }
}
