package com.usman.miqaat.data

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.Locale

/** All six times for one calendar day, in the device's zone. */
@androidx.compose.runtime.Immutable   // lets Compose skip the rail, timeline and cards when only the seconds tick changed
data class DayTimes(val date: LocalDate, val times: Map<Prayer, ZonedDateTime>, val fromMasjid: Boolean = false) {
    operator fun get(p: Prayer): ZonedDateTime = times.getValue(p)
}

/** What the home screen should be showing right now. Rebuilt once a minute (see MainActivity), not once a second. */
@androidx.compose.runtime.Immutable
data class PrayerState(
    val now: ZonedDateTime,
    val today: DayTimes,
    val hero: Prayer,                 // the prayer named in the centre of the screen
    val heroTime: ZonedDateTime,
    val justPassed: Boolean,          // true => "azaan was X ago", false => "in X"
    val delta: Duration,              // time since (justPassed) or until (not)
    val current: Prayer?,             // last prayer whose time has come today (null before Fajr)
    val next: Prayer,
    val nextTime: ZonedDateTime,
    val period: Prayer                // drives the sky palette (SUNRISE = morning tint)
)

object PrayerEngine {

    /** Wall-clock time on a date: minutes from midnight as people read them, correct across DST changes. */
    fun at(date: LocalDate, zone: ZoneId, minutes: Int): ZonedDateTime =
        date.atTime(java.time.LocalTime.ofSecondOfDay((minutes.coerceIn(0, 1439) * 60).toLong())).atZone(zone)

    fun times(settings: AppSettings, date: LocalDate, zone: ZoneId = settings.zone()): DayTimes {
        val coords = Coordinates(settings.latitude, settings.longitude)
        val comps = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val pt = PrayerTimes(coords, comps, settings.calculationParameters())
        fun z(d: java.util.Date) = ZonedDateTime.ofInstant(Instant.ofEpochMilli(d.time), zone)
        val dhuhr = if (settings.jumuahEnabled && date.dayOfWeek == java.time.DayOfWeek.FRIDAY)
            at(date, zone, settings.jumuahMinutes) else z(pt.dhuhr)
        val calc = mapOf(
            Prayer.FAJR to z(pt.fajr),
            Prayer.SUNRISE to z(pt.sunrise),
            Prayer.DHUHR to dhuhr,
            Prayer.ASR to z(pt.asr),
            Prayer.MAGHRIB to z(pt.maghrib),
            Prayer.ISHA to z(pt.isha)
        )
        // Masjid timetable wins for any day it covers.
        val ov = if (settings.useOverrides) settings.overrides[date.toString()] else null
        if (ov != null) {
            val m = Prayer.entries.mapIndexed { i, p -> p to at(date, zone, ov[i]) }.toMap()
            return DayTimes(date, m, fromMasjid = true)
        }
        return DayTimes(date, calc)
    }

    /** Pure astronomical times, ignoring any masjid override (for the "why this time?" comparison). */
    fun calculated(settings: AppSettings, date: LocalDate): DayTimes = times(settings.copy(useOverrides = false), date)

    /** Same day computed with the other Asr madhab. */
    fun asrOther(settings: AppSettings, date: LocalDate): ZonedDateTime =
        calculated(settings.copy(asrMethod = if (settings.asrMethod == AsrMethod.HANAFI) AsrMethod.STANDARD else AsrMethod.HANAFI), date)[Prayer.ASR]

    /** Sharʿī midnight: halfway from Maghrib to the next Fajr. Isha is best prayed before it. */
    fun midnight(settings: AppSettings, day: DayTimes): ZonedDateTime {
        val nextFajr = times(settings, day.date.plusDays(1), day[Prayer.FAJR].zone)[Prayer.FAJR]
        return day[Prayer.MAGHRIB].plus(Duration.between(day[Prayer.MAGHRIB], nextFajr).dividedBy(2))
    }

    /** Last third of the night begins (for qiyām / suhoor planning). */
    fun lastThird(settings: AppSettings, day: DayTimes): ZonedDateTime {
        val nextFajr = times(settings, day.date.plusDays(1), day[Prayer.FAJR].zone)[Prayer.FAJR]
        val night = Duration.between(day[Prayer.MAGHRIB], nextFajr)
        return nextFajr.minus(night.dividedBy(3))
    }

    /** When a prayer's time ends. Isha: sharʿī midnight (preferred end); it remains valid until Fajr. */
    fun endOf(settings: AppSettings, day: DayTimes, p: Prayer): ZonedDateTime? = when (p) {
        Prayer.FAJR -> day[Prayer.SUNRISE]
        Prayer.SUNRISE -> null
        Prayer.DHUHR -> day[Prayer.ASR]
        Prayer.ASR -> day[Prayer.MAGHRIB]
        Prayer.MAGHRIB -> day[Prayer.ISHA]
        Prayer.ISHA -> midnight(settings, day)
    }

    data class Window(val start: ZonedDateTime, val end: ZonedDateTime, val label: String)

    /** Times when voluntary prayer is disliked: after sunrise (~15 min), at zawāl (~10 min before Dhuhr), after ʿAsr until Maghrib. */
    fun dislikedWindows(day: DayTimes, settings: AppSettings? = null): List<Window> {
        val noon = settings?.let { calculated(it.copy(jumuahEnabled = false), day.date)[Prayer.DHUHR] } ?: day[Prayer.DHUHR]
        return listOf(
        Window(day[Prayer.SUNRISE], day[Prayer.SUNRISE].plusMinutes(15), "After sunrise · until the sun has risen a spear's length (shown as ≈15 min; the event, not the number, is what the texts describe)"),
        Window(noon.minusMinutes(10), noon, "Zawāl · the sun at its zenith just before Dhuhr (shown as ≈10 min; a conservative estimate)"),
        Window(day[Prayer.ASR], day[Prayer.MAGHRIB], "After praying ʿAsr · until sunset (schools differ on whether this attaches to the time or to having prayed)")
    ) }

    /** Great-circle distance in km. */
    fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1); val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2)
        return 2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }

    /** True when the current location is far enough from home that travel rules may apply (~80 km). */
    fun isTravelling(settings: AppSettings): Boolean {
        val hl = settings.homeLat ?: return false; val hg = settings.homeLng ?: return false
        return distanceKm(hl, hg, settings.latitude, settings.longitude) >= 80.0
    }

    // ---- masjid timetable import -----------------------------------------

    data class ImportResult(val rows: Map<String, List<Int>>, val skipped: Int, val notes: List<String>)

    /**
     * Parses a CSV/TSV/text timetable. Accepts lines like
     *   4/10/2026, 5:15, 6:37, 12:58, 4:07, 6:22, 7:38 [, iq1..iq5]
     *   2026-10-04  05:15 06:37 12:58 16:07 18:22 19:38
     * Times may be 12h without AM/PM: values are read in prayer order and pushed into the afternoon as needed.
     */
    /**
     * Row-level review of an imported timetable before it is allowed to override the calculation.
     * Returns human-readable anomalies: order problems, big day-to-day jumps, and rows far from the calculated times.
     */
    fun reviewTimetable(settings: AppSettings, rows: Map<String, List<Int>>): List<String> {
        val out = mutableListOf<String>()
        val days = rows.keys.sorted()
        var prev: List<Int>? = null
        val names = listOf("Fajr", "Sunrise", "Dhuhr", "ʿAsr", "Maghrib", "Isha")
        for (d in days) {
            val r = rows.getValue(d)
            for (i in 0 until 5) if (r[i] >= r[i + 1]) out += "$d: ${names[i]} (${hm(r[i])}) is not before ${names[i + 1]} (${hm(r[i + 1])})"
            prev?.let { p -> for (i in 0 until 6) if (kotlin.math.abs(r[i] - p[i]) > 20) out += "$d: ${names[i]} jumps ${r[i] - p[i]} min from the day before" }
            val date = runCatching { LocalDate.parse(d) }.getOrNull()
            if (date != null) {
                val calc = calculated(settings.copy(jumuahEnabled = false), date)
                listOf(0 to Prayer.FAJR, 4 to Prayer.MAGHRIB).forEach { (i, pr) ->
                    val c = calc[pr]; val cm = c.hour * 60 + c.minute
                    if (kotlin.math.abs(r[i] - cm) > 15) out += "$d: ${names[i]} ${hm(r[i])} is ${r[i] - cm} min from the calculated ${hm(cm)}"
                }
            }
            if (r.size >= 11) for (k in 0 until 5) { val a = if (k == 0) 0 else k + 1; if (r[6 + k] < r[a]) out += "$d: ${names[a]} iqamah ${hm(r[6 + k])} is before its azaan ${hm(r[a])}" }
            prev = r
        }
        return out.take(40)
    }
    fun hm(m: Int) = "%d:%02d".format((m / 60) % 24, m % 60)

    fun parseTimetable(text: String, year: Int): ImportResult {
        val rows = linkedMapOf<String, List<Int>>(); var skipped = 0; val notes = mutableListOf<String>()
        val timeRe = Regex("""\b(\d{1,2})[:.](\d{2})\s*(am|pm|AM|PM)?""")
        val dateRe = Regex("""\b(\d{4})-(\d{1,2})-(\d{1,2})\b|\b(\d{1,2})[/.-](\d{1,2})(?:[/.-](\d{2,4}))?\b""")
        for (raw in text.lines()) {
            val line = raw.trim(); if (line.isEmpty()) continue
            val dm = dateRe.find(line)
            if (dm == null) { skipped++; continue }
            val dateOrNull: LocalDate? = runCatching {
                if (dm.groupValues[1].isNotEmpty()) LocalDate.of(dm.groupValues[1].toInt(), dm.groupValues[2].toInt(), dm.groupValues[3].toInt())
                else {
                    val d = dm.groupValues[4].toInt(); val mth = dm.groupValues[5].toInt()
                    val y = dm.groupValues[6].let { if (it.isEmpty()) year else if (it.length == 2) 2000 + it.toInt() else it.toInt() }
                    LocalDate.of(y, mth, d)
                }
            }.getOrNull()
            if (dateOrNull == null) { skipped++; continue }
            val date = dateOrNull
            val rest = line.substring(dm.range.last + 1)
            val ts = timeRe.findAll(rest).map { m ->
                var h = m.groupValues[1].toInt(); val mi = m.groupValues[2].toInt(); val ap = m.groupValues[3].lowercase()
                if (ap == "pm" && h < 12) h += 12; if (ap == "am" && h == 12) h = 0
                h * 60 + mi
            }.toMutableList()
            if (ts.size < 6) { skipped++; continue }
            // 12-hour sheets without AM/PM: make the sequence monotonic (Dhuhr onward is afternoon)
            for (i in 1 until ts.size) {
                if (i == 6) continue                                   // iqamah block starts again from the morning
                if (ts[i] < ts[i - 1] && ts[i] + 720 > ts[i - 1]) ts[i] += 720
            }
            // each iqamah must follow its own azaan
            for (k in 0 until 5) { val a = if (k == 0) 0 else k + 1; val iq = 6 + k; if (ts.size > iq && ts[iq] < ts[a] && ts[iq] + 720 >= ts[a]) ts[iq] += 720 }
            rows[date.toString()] = ts.take(11)
        }
        if (rows.isEmpty()) notes += "No rows with a date and at least six times were found."
        else notes += "Read ${rows.size} days (${rows.keys.first()} → ${rows.keys.last()})" + if (rows.values.any { it.size >= 11 }) ", with iqamah times." else "."
        if (skipped > 0) notes += "$skipped line(s) skipped (headers or incomplete)."
        return ImportResult(rows, skipped, notes)
    }

    fun state(settings: AppSettings, now: ZonedDateTime = ZonedDateTime.now(settings.zone())): PrayerState {
        val zone = settings.zone()
        val now = now.withZoneSameInstant(zone)
        val today = times(settings, now.toLocalDate(), zone)
        val prayers = Prayer.prayersOnly

        val current = prayers.lastOrNull { !today[it].isAfter(now) }
        val nextToday = prayers.firstOrNull { today[it].isAfter(now) }
        val (next, nextTime) = if (nextToday != null) nextToday to today[nextToday]
        else Prayer.FAJR to times(settings, now.toLocalDate().plusDays(1), zone)[Prayer.FAJR]

        val sinceCurrent = current?.let { Duration.between(today[it], now) }
        val justPassed = sinceCurrent != null && sinceCurrent.toMinutes() < settings.afterWindowMinutes

        val hero = if (justPassed) current!! else next
        val heroTime = if (justPassed) today[hero] else nextTime
        val delta = if (justPassed) sinceCurrent!! else Duration.between(now, nextTime)

        val period = when {
            current == null -> Prayer.ISHA                                  // before Fajr: night
            current == Prayer.FAJR && now.isAfter(today[Prayer.SUNRISE]) -> Prayer.SUNRISE
            else -> current
        }
        return PrayerState(now, today, hero, heroTime, justPassed, delta, current, next, nextTime, period)
    }

    /** Month of DayTimes for the timetable. */
    fun month(settings: AppSettings, year: Int, month: Int): List<DayTimes> {
        val first = LocalDate.of(year, month, 1)
        return (0 until first.lengthOfMonth()).map { times(settings, first.plusDays(it.toLong())) }
    }

    // ---- Hijri ----------------------------------------------------------

    private val hijriMonthsEn = listOf(
        "Muharram", "Safar", "Rabīʿ al-Awwal", "Rabīʿ al-Thānī", "Jumādā al-Ūlā", "Jumādā al-Ākhirah",
        "Rajab", "Shaʿbān", "Ramaḍān", "Shawwāl", "Dhū al-Qaʿdah", "Dhū al-Ḥijjah"
    )
    private val hijriMonthsAr = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    data class Hijri(val day: Int, val month: Int, val year: Int) {
        val english get() = "$day ${hijriMonthsEn[month - 1]} $year"
        val arabic get() = "${toArabicDigits(day)} ${hijriMonthsAr[month - 1]} ${toArabicDigits(year)}"
        val short get() = "$day ${hijriMonthsEn[month - 1].substringBefore(' ')}"
        val isRamadan get() = month == 9
    }

    fun hijri(date: LocalDate, offsetDays: Int): Hijri {
        val h: HijrahDate = HijrahChronology.INSTANCE.date(date.plusDays(offsetDays.toLong()))
        return Hijri(h.get(ChronoField.DAY_OF_MONTH), h.get(ChronoField.MONTH_OF_YEAR), h.get(ChronoField.YEAR))
    }

    /** Iqamah time for a prayer on a day, or null if iqamah is off for it. */
    fun iqamah(settings: AppSettings, day: DayTimes, p: Prayer): ZonedDateTime? {
        if (!settings.iqamahEnabled || !p.isPrayer) return null
        if (day.fromMasjid && settings.useOverrides) {
            val ov = settings.overrides[day.date.toString()]
            val idx = 6 + Prayer.prayersOnly.indexOf(p)
            if (ov != null && ov.size > idx) return at(day.date, day[p].zone, ov[idx])
        }
        if (p == Prayer.DHUHR && settings.jumuahEnabled && day.date.dayOfWeek == java.time.DayOfWeek.FRIDAY)
            return at(day.date, day[p].zone, settings.jumuahIqamahMinutes)
        if (settings.iqamahIsFixed[p] == true) {
            val fixed = at(day.date, day[p].zone, settings.iqamahFixed[p] ?: 0)
            // A fixed time can't be before the azaan (winter Fajr, say): fall back to azaan + 5 min that day.
            return if (fixed.isBefore(day[p].plusMinutes(1))) day[p].plusMinutes(5) else fixed
        }
        val off = settings.iqamahOffsets[p] ?: 0
        return if (off <= 0) null else day[p].plusMinutes(off.toLong())
    }

    fun isRamadan(settings: AppSettings, date: LocalDate): Boolean = when (settings.ramadanMode) {
        RamadanMode.ON -> true
        RamadanMode.OFF -> false
        RamadanMode.AUTO -> hijri(date, settings.hijriOffsetDays).isRamadan
    }

    /** Thursday after Maghrib until Friday Maghrib, when Friday reminders apply. */
    fun isJumuahWindow(settings: AppSettings, now: ZonedDateTime): Boolean {
        val d = now.toLocalDate()
        return when (d.dayOfWeek) {
            java.time.DayOfWeek.FRIDAY -> now.isBefore(times(settings, d, now.zone)[Prayer.MAGHRIB])
            java.time.DayOfWeek.THURSDAY -> !now.isBefore(times(settings, d, now.zone)[Prayer.MAGHRIB])
            else -> false
        }
    }

    /** Qibla bearing in degrees clockwise from true north. */
    /** Great-circle bearing to the Kaʿbah from TRUE north, 0..360. */
    fun qibla(settings: AppSettings): Double = qibla(settings.latitude, settings.longitude)
    fun qibla(lat: Double, lng: Double): Double = norm360(com.batoulapps.adhan.Qibla(Coordinates(lat, lng)).direction)

    fun norm360(deg: Double): Double = ((deg % 360.0) + 360.0) % 360.0

    /**
     * A compass sensor reports a heading from MAGNETIC north; the Qibla bearing is from TRUE north.
     * true = magnetic + declination (declination is positive when magnetic north lies east of true north).
     */
    fun trueHeading(magneticHeading: Double, declinationDeg: Double): Double = norm360(magneticHeading + declinationDeg)

    /** Signed degrees to turn (−180..180): positive = turn right/clockwise. */
    fun turnTo(bearing: Double, heading: Double): Double { val d = norm360(bearing - heading); return if (d > 180) d - 360 else d }

    /** Plain-language direction for screen readers and the sensor-less fallback. */
    fun qiblaWords(bearing: Double, heading: Double?): String {
        val b = "%.0f degrees, %s, from true north".format(bearing, compass(bearing))
        if (heading == null) return "Qibla is at $b."
        val t = turnTo(bearing, heading)
        return when {
            kotlin.math.abs(t) <= 3 -> "You are facing the Qibla ($b)."
            t > 0 -> "Turn right %.0f degrees to face the Qibla ($b).".format(t)
            else -> "Turn left %.0f degrees to face the Qibla ($b).".format(-t)
        }
    }

    fun compass(deg: Double): String {
        val dirs = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        return dirs[((deg % 360 + 360) % 360 / 22.5 + 0.5).toInt() % 16]
    }

    fun toArabicDigits(n: Int): String = n.toString().map { c -> if (c.isDigit()) '٠' + (c - '0') else c }.joinToString("")

    // ---- formatting -----------------------------------------------------

    private val f12 = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
    private val fAmPm = DateTimeFormatter.ofPattern("a", Locale.ENGLISH)
    private val f24 = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

    fun clock(t: ZonedDateTime, use24h: Boolean): String = if (use24h) t.format(f24) else t.format(f12)
    fun suffix(t: ZonedDateTime, use24h: Boolean): String = if (use24h) "" else t.format(fAmPm).uppercase()

    /** "in 2 h 5 min" or "40 min ago". */
    fun relative(t: ZonedDateTime, now: ZonedDateTime): String =
        if (t.isAfter(now)) "in " + humanDuration(Duration.between(now, t)) else humanDuration(Duration.between(t, now)) + " ago"

    /** The time to show on a prayer's row: after Isha, Fajr means tomorrow's Fajr. */
    fun rowTime(state: PrayerState, p: Prayer): ZonedDateTime =
        if (p == Prayer.FAJR && state.current == Prayer.ISHA) state.nextTime else state.today[p]

    fun humanDuration(d: Duration): String {
        val secs = d.abs().seconds
        if (secs < 60) return "$secs s"          // the last minute counts down second by second
        val total = d.abs().toMinutes()
        val h = total / 60
        val m = total % 60
        return when {
            h == 0L -> "$m min"
            m == 0L -> "$h h"
            else -> "$h h $m min"
        }
    }
}
