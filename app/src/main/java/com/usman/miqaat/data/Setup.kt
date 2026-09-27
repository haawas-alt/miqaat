package com.usman.miqaat.data

import java.time.Instant
import java.time.ZoneId

/**
 * Pure rules for the first-run gate. Kept free of Android so they can be unit-tested:
 * a fresh install must never present prayer times until a place has been chosen or detected.
 */
object Setup {
    /** True only when the app may show prayer times as valid and arm alarms for them. */
    fun ready(s: AppSettings): Boolean = s.locationSet && s.setupDone && s.locationName.isNotBlank()

    /**
     * Rough sanity check between a longitude and a time zone: solar noon drifts about 1 h per 15°,
     * so a zone whose offset is more than ~3 h from lng/15 almost certainly belongs to a different place
     * (e.g. a phone still on Sydney time after landing in London). Returns the discrepancy in hours.
     */
    fun zoneMismatchHours(longitude: Double, zone: ZoneId, at: Instant = Instant.now()): Double {
        val offsetHours = zone.rules.getOffset(at).totalSeconds / 3600.0
        val solarHours = longitude / 15.0
        var d = offsetHours - solarHours
        while (d > 12) d -= 24
        while (d < -12) d += 24
        return kotlin.math.abs(d)
    }

    fun zoneLooksWrong(longitude: Double, zone: ZoneId, at: Instant = Instant.now()): Boolean = zoneMismatchHours(longitude, zone, at) > 3.0

    /** Coordinates as shown to the user when no place name is known. */
    fun coordLabel(lat: Double, lng: Double): String = "%.2f°%s, %.2f°%s".format(kotlin.math.abs(lat), if (lat >= 0) "N" else "S", kotlin.math.abs(lng), if (lng >= 0) "E" else "W")
}
