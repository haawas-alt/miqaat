package com.usman.miqaat.data

import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The handful of strings that appear on the home screen, in English and Urdu. Settings stay in English for now. */
object L10n {
    private val urPrayer = mapOf(Prayer.FAJR to "فجر", Prayer.SUNRISE to "طلوعِ آفتاب", Prayer.DHUHR to "ظہر", Prayer.ASR to "عصر", Prayer.MAGHRIB to "مغرب", Prayer.ISHA to "عشاء")
    private val urMonths = listOf("محرم", "صفر", "ربیع الاول", "ربیع الثانی", "جمادی الاول", "جمادی الثانی", "رجب", "شعبان", "رمضان", "شوال", "ذوالقعدہ", "ذوالحجہ")
    private val urDays = mapOf("MONDAY" to "پیر", "TUESDAY" to "منگل", "WEDNESDAY" to "بدھ", "THURSDAY" to "جمعرات", "FRIDAY" to "جمعہ", "SATURDAY" to "ہفتہ", "SUNDAY" to "اتوار")
    private val urGreg = listOf("جنوری", "فروری", "مارچ", "اپریل", "مئی", "جون", "جولائی", "اگست", "ستمبر", "اکتوبر", "نومبر", "دسمبر")

    fun isUrdu(s: AppSettings) = s.language == Language.UR

    /** Set by the UI whenever the app language changes, so number-and-suffix helpers deep in the engine follow it. */
    @Volatile var uiUrdu: Boolean = false

    /** AM/PM for English; for Urdu the time-of-day word for the same hour (the digits themselves never change). */
    fun ampm(hour: Int): String = if (!uiUrdu) (if (hour >= 12) "PM" else "AM") else when { hour < 12 -> "صبح"; hour < 16 -> "دوپہر"; hour < 19 -> "شام"; else -> "رات" }

    fun dateShort(s: AppSettings, t: ZonedDateTime): String =
        if (isUrdu(s)) "${urDays[t.dayOfWeek.name]}، ${t.dayOfMonth} ${urGreg[t.monthValue - 1]}"
        else t.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH))

    fun prayer(s: AppSettings, p: Prayer): String = if (isUrdu(s)) urPrayer.getValue(p) else p.english

    fun date(s: AppSettings, t: ZonedDateTime): String =
        if (isUrdu(s)) "${urDays[t.dayOfWeek.name]}، ${t.dayOfMonth} ${urGreg[t.monthValue - 1]} ${t.year}"
        else t.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ENGLISH))

    fun hijri(s: AppSettings, h: PrayerEngine.Hijri): String =
        // RLI…PDI: the string starts with a digit, so without a right-to-left isolate the day and year collapse together.
        if (isUrdu(s)) "\u2067${h.day} ${urMonths[h.month - 1]} ${h.year}\u2069" else h.english

    fun duration(s: AppSettings, d: Duration): String = durationIn(isUrdu(s), d)
    /** Same, for code that has no settings object to hand: follows the app language flag. */
    fun durationUi(d: Duration): String = durationIn(uiUrdu, d)
    private fun durationIn(urdu: Boolean, d: Duration): String {
        if (!urdu) return PrayerEngine.humanDuration(d)
        // Isolated right-to-left so "1 گھنٹے 53 منٹ" keeps its order when it starts with a digit (seen live as "گھنٹے 53 منٹ 1").
        if (d.abs().seconds < 60) return "\u2067${d.abs().seconds} سیکنڈ\u2069"
        val total = d.abs().toMinutes(); val h = total / 60; val m = total % 60
        return "\u2067" + when { h == 0L -> "$m منٹ"; m == 0L -> "$h گھنٹے"; else -> "$h گھنٹے $m منٹ" } + "\u2069"
    }

    /** Wraps a Latin/number run in Unicode first-strong isolates so it keeps its order inside Urdu text. */
    fun iso(t: String): String = "\u2068" + t + "\u2069"

    fun inFor(s: AppSettings, d: Duration) = if (isUrdu(s)) "${duration(s, d)} باقی" else "in ${PrayerEngine.humanDuration(d)}"
    /** Countdown to the hero prayer; says "tomorrow" when that prayer is on the next calendar day (after Isha), so a 5:01 hero beside a 5:03 list row is explained. */
    fun inForState(s: AppSettings, st: PrayerState): String {
        val base = inFor(s, st.delta)
        val tomorrow = !st.justPassed && st.heroTime.toLocalDate() != st.now.toLocalDate()
        return if (!tomorrow) base else if (isUrdu(s)) "کل · $base" else "$base · tomorrow"
    }
    fun ago(s: AppSettings, d: Duration) = if (isUrdu(s)) "اذان ${duration(s, d)} پہلے" else "azaan was ${PrayerEngine.humanDuration(d)} ago"
    fun relative(s: AppSettings, t: ZonedDateTime, now: ZonedDateTime) =
        if (t.isAfter(now)) (if (isUrdu(s)) "${duration(s, Duration.between(now, t))} باقی" else "in " + PrayerEngine.humanDuration(Duration.between(now, t)))
        else (if (isUrdu(s)) "${duration(s, Duration.between(t, now))} پہلے" else PrayerEngine.humanDuration(Duration.between(t, now)) + " ago")

    fun iqamahIn(s: AppSettings, d: Duration) = if (isUrdu(s)) "اقامت ${duration(s, d)} میں" else "Iqamah in ${PrayerEngine.humanDuration(d)}"
    fun word(s: AppSettings, en: String): String = if (!isUrdu(s)) en else when (en) {
        "NOW" -> "اب"; "NEXT" -> "اگلی"; "Iqamah" -> "اقامت"; "IQAMAH" -> "اقامت"; "ends" -> "اختتام"; "Jumuʿah" -> "جمعہ"
        "Qibla" -> "قبلہ"; "Morning adhkār" -> "صبح کے اذکار"; "Evening adhkār" -> "شام کے اذکار"; "After-prayer adhkār" -> "نماز کے بعد اذکار"
        "Update available" -> "اپ ڈیٹ دستیاب"; "Suhoor" -> "سحری"; "Iftar" -> "افطار"; "Fajr · Suhoor" -> "فجر · سحری"; "Maghrib · Iftar" -> "مغرب · افطار"
        "Designed by UZR · Make duʿā for me" -> "ڈیزائن: UZR · میرے لیے دعا کیجیے"
        else -> en
    }
}
