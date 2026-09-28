package com.usman.miqaat

import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.Setup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

/** Reproduces the audit's release blockers 1 (silent default location) and 2 (Qibla north reference). */
class TrustTest {

    // ---- Blocker 1: a fresh install must never present another city's schedule as the user's own

    @Test fun freshInstallIsUnconfigured() {
        val d = AppSettings()
        assertFalse("a fresh install must not claim a location", d.locationSet)
        assertFalse(Setup.ready(d))
        assertEquals("", d.locationName)
    }

    @Test fun finishingSetupWithoutAPlaceIsNotReady() {
        // Simulates the old "Continue" path: setupDone flips but no place was chosen.
        assertFalse(Setup.ready(AppSettings(setupDone = true)))
        // Even a name without the flag (e.g. a stale name from a failed geocode) is not enough.
        assertFalse(Setup.ready(AppSettings(setupDone = true, locationName = "Somewhere")))
    }

    @Test fun readyOnlyWhenPlaceChosenAndSetupFinished() {
        assertFalse(Setup.ready(AppSettings(locationSet = true, locationName = "Lahore, Pakistan")))          // setup not finished
        assertTrue(Setup.ready(AppSettings(locationSet = true, setupDone = true, locationName = "Lahore, Pakistan", latitude = 31.55, longitude = 74.34)))
    }

    @Test fun zoneMismatchIsNoticed() {
        val jan = Instant.parse("2026-01-15T00:00:00Z")
        // London coordinates on a phone still set to Sydney time: ~11 h out.
        assertTrue(Setup.zoneLooksWrong(-0.13, ZoneId.of("Australia/Sydney"), jan))
        // Matching zones are fine, including wide legal zones (Karachi on Karachi time, New York on Eastern).
        assertFalse(Setup.zoneLooksWrong(151.21, ZoneId.of("Australia/Sydney"), jan))
        assertFalse(Setup.zoneLooksWrong(74.34, ZoneId.of("Asia/Karachi"), jan))
        assertFalse(Setup.zoneLooksWrong(-74.01, ZoneId.of("America/New_York"), jan))
        // Date-line wrap: Auckland (174.8°E, UTC+13 in January) must not be flagged.
        assertFalse(Setup.zoneLooksWrong(174.76, ZoneId.of("Pacific/Auckland"), jan))
    }

    // ---- Blocker 2: bearings are from TRUE north; a magnetic heading must be corrected by the declination

    private fun close(expected: Double, actual: Double, tol: Double = 1.5) {
        val d = abs(((actual - expected + 540) % 360) - 180)
        assertTrue("expected $expected got $actual", d <= tol)
    }

    @Test fun qiblaBearingsForKnownCities() {
        // Reference values from the standard great-circle formula (Kaʿbah 21.4225 N, 39.8262 E).
        close(118.99, PrayerEngine.qibla(51.51, -0.13))     // London
        close(58.48, PrayerEngine.qibla(40.71, -74.01))     // New York
        close(277.50, PrayerEngine.qibla(-33.87, 151.21))   // Sydney
        close(292.54, PrayerEngine.qibla(3.14, 101.69))     // Kuala Lumpur
        close(23.36, PrayerEngine.qibla(-33.93, 18.42))     // Cape Town
        close(267.75, PrayerEngine.qibla(24.86, 67.01))     // Karachi
        close(151.63, PrayerEngine.qibla(41.01, 28.98))     // Istanbul
        close(295.15, PrayerEngine.qibla(-6.21, 106.85))    // Jakarta
    }

    @Test fun bearingNormalisation() {
        assertEquals(350.0, PrayerEngine.norm360(-10.0), 1e-9)
        assertEquals(10.0, PrayerEngine.norm360(370.0), 1e-9)
        assertEquals(0.0, PrayerEngine.norm360(720.0), 1e-9)
    }

    @Test fun magneticHeadingIsCorrectedByDeclination() {
        // Sydney: declination ≈ +12.6° (magnetic north lies east of true north).
        // A phone whose magnetic heading reads 265° is actually facing 277.6° true — i.e. the Qibla.
        val trueHeading = PrayerEngine.trueHeading(265.0, 12.6)
        close(277.6, trueHeading, 0.01)
        close(0.0, PrayerEngine.turnTo(PrayerEngine.qibla(-33.87, 151.21), trueHeading), 0.2)
        // Without the correction the user would be told to turn ~12.6° — the bug the audit found.
        assertTrue(abs(PrayerEngine.turnTo(PrayerEngine.qibla(-33.87, 151.21), 265.0)) > 12.0)
        // Western declination (e.g. New York ≈ −12.9°) subtracts.
        close(347.1, PrayerEngine.trueHeading(0.0, -12.9), 0.01)
    }

    @Test fun turnDirectionsAreShortestWay() {
        assertEquals(20.0, PrayerEngine.turnTo(30.0, 10.0), 1e-9)      // turn right 20
        assertEquals(-20.0, PrayerEngine.turnTo(350.0, 10.0), 1e-9)    // turn left 20 (not right 340)
        assertTrue(PrayerEngine.qiblaWords(277.5, 277.5).startsWith("You are facing"))
        assertTrue(PrayerEngine.qiblaWords(277.5, 200.0).startsWith("Turn right"))
        assertTrue(PrayerEngine.qiblaWords(277.5, null).startsWith("Qibla is at"))
    }
}

/** Re-audit P0: automatic location refresh must never undo a time zone the user chose. */
class ZoneRefreshTest {
    private val places = listOf(
        com.usman.miqaat.data.Place("Sydney, NSW", -33.87, 151.21, "Australia/Sydney"),
        com.usman.miqaat.data.Place("London, UK", 51.51, -0.13, "Europe/London"),
        com.usman.miqaat.data.Place("Karachi, Pakistan", 24.86, 67.01, "Asia/Karachi"))
    private val jan = Instant.parse("2026-01-15T00:00:00Z")
    private val sydney = ZoneId.of("Australia/Sydney")

    @Test fun manualZoneSurvivesRefresh() {
        // Traveller in London, phone still on Sydney time, chose Europe/London by hand during setup.
        val s = AppSettings(latitude = 51.5, longitude = -0.1, locationName = "London", locationSet = true, setupDone = true, zoneId = "Europe/London", zoneManual = true)
        val r = Setup.applyFix(s, 51.52, -0.12, "London, UK", deviceZone = sydney, places = places, at = jan)
        assertEquals("Europe/London", r.settings.zoneId)
        assertTrue(r.settings.zoneManual)
        assertFalse(r.needsZoneChoice)
    }

    @Test fun automaticZoneFollowsDeviceWhilePlausible() {
        val s = AppSettings(latitude = -33.9, longitude = 151.2, locationName = "Sydney", locationSet = true, setupDone = true, zoneId = null)
        val r = Setup.applyFix(s, -33.8, 151.0, "Parramatta, NSW", deviceZone = sydney, places = places, at = jan)
        assertEquals(null, r.settings.zoneId)          // device zone, still plausible
        assertFalse(r.needsZoneChoice)
    }

    @Test fun automaticZoneSwitchesToNearestKnownZoneWhenDeviceIsWrong() {
        // Landed in London; phone still on Sydney time; zone was automatic.
        val s = AppSettings(latitude = -33.9, longitude = 151.2, locationName = "Sydney", locationSet = true, setupDone = true, zoneId = null)
        val r = Setup.applyFix(s, 51.51, -0.13, "London, UK", deviceZone = sydney, places = places, at = jan)
        assertEquals("Europe/London", r.settings.zoneId)
        assertFalse(r.needsZoneChoice)
        assertTrue(r.moved)
    }

    @Test fun asksWhenNoPlausibleZoneIsKnown() {
        // Somewhere in the mid-Atlantic with a Sydney phone: nothing within 600 km, previous zone kept, UI must ask.
        val s = AppSettings(latitude = 10.0, longitude = -30.0, locationName = "Sea", locationSet = true, setupDone = true, zoneId = "Atlantic/Azores")
        val r = Setup.applyFix(s, 10.0, -30.0, "Sea", deviceZone = sydney, places = places, at = jan)
        assertTrue(r.needsZoneChoice)
        assertEquals("Atlantic/Azores", r.settings.zoneId)
    }

    @Test fun smallMovesAreNotCountedAsMoves() {
        val s = AppSettings(latitude = -33.87, longitude = 151.21, locationName = "Sydney", locationSet = true, setupDone = true)
        assertFalse(Setup.applyFix(s, -33.871, 151.212, "Sydney", sydney, places, jan).moved)
    }
}
