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

    /**
     * Best available time zone for a detected position, without a boundary database:
     *  1. the device zone, if its offset is plausible for that longitude (the normal case — the phone is where the user is);
     *  2. otherwise the zone of the nearest known place within [maxKm];
     *  3. otherwise null: the caller must ask the user, never guess.
     */
    fun suggestZone(lat: Double, lng: Double, deviceZone: ZoneId, places: List<Place>, at: Instant = Instant.now(), maxKm: Double = 600.0): String? {
        if (!zoneLooksWrong(lng, deviceZone, at)) return null   // null here means "device zone is fine" (zoneId = null)
        val near = places.filter { it.zone != null }.minByOrNull { PrayerEngine.distanceKm(lat, lng, it.lat, it.lng) } ?: return UNKNOWN
        return if (PrayerEngine.distanceKm(lat, lng, near.lat, near.lng) <= maxKm) near.zone else UNKNOWN
    }
    /** Sentinel from [suggestZone]: no plausible zone could be found; the user must choose. */
    const val UNKNOWN = "?"

    /**
     * Applies a successful location fix to the settings. Pure, so the travel/refresh cases are unit-tested:
     *  • a zone the user chose by hand is never touched;
     *  • an automatic zone follows the device only while that stays plausible for the new longitude, else the nearest known zone;
     *  • if nothing plausible exists the previous zone is kept and [needsZoneChoice] tells the UI to ask.
     */
    data class Applied(val settings: AppSettings, val needsZoneChoice: Boolean, val moved: Boolean)

    fun applyFix(s: AppSettings, lat: Double, lng: Double, name: String, deviceZone: ZoneId, places: List<Place>, at: Instant = Instant.now()): Applied {
        val moved = !s.locationSet || PrayerEngine.distanceKm(s.latitude, s.longitude, lat, lng) > 1.0
        var zone = s.zoneId; var ask = false
        if (!s.zoneManual) {
            when (val sug = suggestZone(lat, lng, deviceZone, places, at)) {
                UNKNOWN -> ask = true            // keep whatever we had, but the UI must ask
                else -> zone = sug               // null = device zone, or a nearby known zone
            }
        }
        val next = s.copy(latitude = lat, longitude = lng, locationName = name, locationSet = true, autoLocation = true, zoneId = zone,
            homeLat = s.homeLat ?: lat, homeLng = s.homeLng ?: lng)
        return Applied(next, ask, moved)
    }

    /** Coordinates as shown to the user when no place name is known. */
    fun coordLabel(lat: Double, lng: Double): String = "%.2f°%s, %.2f°%s".format(kotlin.math.abs(lat), if (lat >= 0) "N" else "S", kotlin.math.abs(lng), if (lng >= 0) "E" else "W")
}
