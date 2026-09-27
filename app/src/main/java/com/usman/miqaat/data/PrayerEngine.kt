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
data class DayTimes(val date: LocalDate, val times: Map<Prayer, ZonedDateTime>) {
    operator fun get(p: Prayer): ZonedDateTime = times.getValue(p)
}

/** What the home screen should be showing right now. */
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

    fun times(settings: AppSettings, date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): DayTimes {
        val coords = Coordinates(settings.latitude, settings.longitude)
        val comps = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val pt = PrayerTimes(coords, comps, settings.calculationParameters())
        fun z(d: java.util.Date) = ZonedDateTime.ofInstant(Instant.ofEpochMilli(d.time), zone)
        val dhuhr = if (settings.jumuahEnabled && date.dayOfWeek == java.time.DayOfWeek.FRIDAY)
            date.atStartOfDay(zone).plusMinutes(settings.jumuahMinutes.toLong()) else z(pt.dhuhr)
        return DayTimes(
            date,
            mapOf(
                Prayer.FAJR to z(pt.fajr),
                Prayer.SUNRISE to z(pt.sunrise),
                Prayer.DHUHR to dhuhr,
                Prayer.ASR to z(pt.asr),
                Prayer.MAGHRIB to z(pt.maghrib),
                Prayer.ISHA to z(pt.isha)
            )
        )
    }

    fun state(settings: AppSettings, now: ZonedDateTime = ZonedDateTime.now()): PrayerState {
        val zone = now.zone
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
        if (p == Prayer.DHUHR && settings.jumuahEnabled && day.date.dayOfWeek == java.time.DayOfWeek.FRIDAY)
            return day.date.atStartOfDay(day[p].zone).plusMinutes(settings.jumuahIqamahMinutes.toLong())
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
    fun qibla(settings: AppSettings): Double = com.batoulapps.adhan.Qibla(Coordinates(settings.latitude, settings.longitude)).direction

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

    fun humanDuration(d: Duration): String {
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
