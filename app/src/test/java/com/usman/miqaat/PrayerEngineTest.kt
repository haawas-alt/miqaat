package com.usman.miqaat

import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.decodeOverrides
import com.usman.miqaat.data.encodeOverrides
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class PrayerEngineTest {
    private val syd = ZoneId.of("Australia/Sydney")
    private val s = AppSettings(latitude = -34.02, longitude = 150.77, zoneId = "Australia/Sydney")
    private val d = LocalDate.of(2026, 9, 27)

    @Test fun timesAreInOrderAndPlausibleForSydney() {
        val t = PrayerEngine.times(s, d)
        val order = Prayer.entries.map { t[it] }
        for (i in 1 until order.size) assertTrue("order", order[i].isAfter(order[i - 1]))
        assertEquals(4, t[Prayer.FAJR].hour); assertEquals(17, t[Prayer.MAGHRIB].hour)   // 4:1x AM, 5:5x PM
    }

    @Test fun fridayUsesJumuahTime() {
        val fri = LocalDate.of(2026, 10, 2)
        val t = PrayerEngine.times(s.copy(jumuahEnabled = true, jumuahMinutes = 13 * 60 + 15), fri)
        assertEquals(13, t[Prayer.DHUHR].hour); assertEquals(15, t[Prayer.DHUHR].minute)
    }

    @Test fun fixedIqamahNeverBeforeAzaan() {
        val t = PrayerEngine.times(s, d)
        val fixedEarly = s.copy(iqamahEnabled = true, iqamahIsFixed = mapOf(Prayer.FAJR to true), iqamahFixed = mapOf(Prayer.FAJR to 3 * 60))
        val iq = PrayerEngine.iqamah(fixedEarly, t, Prayer.FAJR)!!
        assertEquals(t[Prayer.FAJR].plusMinutes(5), iq)
        val fixedOk = s.copy(iqamahEnabled = true, iqamahIsFixed = mapOf(Prayer.FAJR to true), iqamahFixed = mapOf(Prayer.FAJR to 5 * 60))
        assertEquals(5, PrayerEngine.iqamah(fixedOk, t, Prayer.FAJR)!!.hour)
        assertNull(PrayerEngine.iqamah(s.copy(iqamahEnabled = true, iqamahOffsets = mapOf(Prayer.ASR to 0)), t, Prayer.ASR))
    }

    @Test fun stateAfterWindowAndBeforeFajr() {
        val t = PrayerEngine.times(s, d)
        val tenAfterMaghrib = PrayerEngine.state(s, t[Prayer.MAGHRIB].plusMinutes(10))
        assertTrue(tenAfterMaghrib.justPassed); assertEquals(Prayer.MAGHRIB, tenAfterMaghrib.hero)
        val hourAfter = PrayerEngine.state(s, t[Prayer.MAGHRIB].plusMinutes(60))
        assertFalse(hourAfter.justPassed); assertEquals(Prayer.ISHA, hourAfter.hero)
        val night = PrayerEngine.state(s, ZonedDateTime.of(d, java.time.LocalTime.of(2, 0), syd))
        assertNull(night.current); assertEquals(Prayer.FAJR, night.next); assertEquals(Prayer.ISHA, night.period)
        val late = PrayerEngine.state(s, t[Prayer.ISHA].plusHours(2))
        assertEquals(d.plusDays(1), late.nextTime.toLocalDate())
    }

    @Test fun relativeAndDurations() {
        val now = ZonedDateTime.of(d, java.time.LocalTime.of(12, 0), syd)
        assertEquals("in 1 h 5 min", PrayerEngine.relative(now.plusMinutes(65), now))
        assertEquals("40 min ago", PrayerEngine.relative(now.minusMinutes(40), now))
        assertEquals("2 h", PrayerEngine.humanDuration(Duration.ofHours(2)))
    }

    @Test fun endTimesAndMidnight() {
        val t = PrayerEngine.times(s, d)
        assertEquals(t[Prayer.SUNRISE], PrayerEngine.endOf(s, t, Prayer.FAJR))
        assertEquals(t[Prayer.MAGHRIB], PrayerEngine.endOf(s, t, Prayer.ASR))
        val mid = PrayerEngine.midnight(s, t)
        assertTrue(mid.isAfter(t[Prayer.ISHA])); assertTrue(mid.hour in 22..23 || mid.hour in 0..1)
        assertTrue(PrayerEngine.lastThird(s, t).isAfter(mid))
        assertEquals(3, PrayerEngine.dislikedWindows(t).size)
    }

    @Test fun overridesWinAndRoundTrip() {
        val ov = mapOf("2026-09-27" to listOf(5 * 60, 6 * 60 + 30, 13 * 60, 16 * 60, 18 * 60 + 15, 19 * 60 + 40, 5 * 60 + 30, 13 * 60 + 15, 16 * 60 + 15, 18 * 60 + 20, 20 * 60))
        assertEquals(ov, decodeOverrides(encodeOverrides(ov)))
        val t = PrayerEngine.times(s.copy(overrides = ov), d)
        assertTrue(t.fromMasjid); assertEquals(13, t[Prayer.DHUHR].hour); assertEquals(0, t[Prayer.DHUHR].minute)
        val iq = PrayerEngine.iqamah(s.copy(overrides = ov, iqamahEnabled = true), t, Prayer.FAJR)
        assertNotNull(iq); assertEquals(30, iq!!.minute)
        assertFalse(PrayerEngine.times(s.copy(overrides = ov, useOverrides = false), d).fromMasjid)
    }

    @Test fun timetableParserHandlesCommonSheets() {
        val csv = """
            Date,Fajr,Sunrise,Dhuhr,Asr,Maghrib,Isha
            4/10/2026, 5:15, 6:37, 12:58, 4:07, 6:22, 7:38
            2026-10-05  05:14 06:36 12:58 16:07 18:23 19:39
            6/10 5:12am 6:34am 12:57pm 4:08pm 6:24pm 7:40pm 5:45am 1:15pm 4:30pm 6:30pm 8:00pm
        """.trimIndent()
        val r = PrayerEngine.parseTimetable(csv, 2026)
        assertEquals(3, r.rows.size)
        assertEquals(listOf(315, 397, 778, 967, 1102, 1178), r.rows["2026-10-04"])
        assertEquals(11, r.rows["2026-10-06"]!!.size)
        assertEquals(1, r.skipped)
    }

    @Test fun travelDistance() {
        assertTrue(PrayerEngine.distanceKm(-34.02, 150.77, -37.81, 144.96) > 700)
        assertTrue(PrayerEngine.isTravelling(s.copy(homeLat = -34.02, homeLng = 150.77, latitude = -37.81, longitude = 144.96)))
        assertFalse(PrayerEngine.isTravelling(s.copy(homeLat = -34.02, homeLng = 150.77)))
    }
}
