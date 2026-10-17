package com.usman.miqaat.data

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Short Qur'an / authentic-hadith lines shown in the notification shade when a prayer time begins.
 *
 * English only for now: Urdu must come from a published translation, never translated here.
 * Wording follows standard English translations; references use sunnah.com numbering.
 * Approved list: 17 Oct 2026, pending the imam's review. Do not add lines without a verified reference.
 * Excluded on purpose: Tirmidhi 586 ("reward of Hajj and Umrah"); its chain is disputed.
 */
object PrayerMessages {
    data class Msg(val title: String, val body: String, val source: String, val grade: String) {
        /** Body plus reference, as shown in the shade. */
        val text: String get() = "$body ($source)"
    }

    private const val P = "{prayer}"

    /** Valid for any of the five prayers; {prayer} is replaced with the prayer's name. */
    val general = listOf(
        Msg("It's time for $P", "“Indeed, prayer has been decreed upon the believers at specified times.”", "Qur'an 4:103", "Qur'an"),
        Msg("$P has begun", "The Prophet ﷺ was asked which deed Allah loves most. He said: “Prayer at its proper time.”", "Bukhari 527, Muslim 85", "Sahih"),
        Msg("Repeat after the azaan", "“When you hear the caller, say what he says, then send blessings upon me.”", "Muslim 384", "Sahih"),
    )

    val specific: Map<Prayer, List<Msg>> = mapOf(
        Prayer.FAJR to listOf(
            Msg("Fajr has begun", "“Whoever prays Fajr is under the protection of Allah.”", "Muslim 657", "Sahih"),
            Msg("Fajr has begun", "“The two rakʿahs of Fajr are better than the world and all it contains.”", "Muslim 725", "Sahih"),
            Msg("Fajr has begun", "“Indeed, the recitation at dawn is ever witnessed.”", "Qur'an 17:78", "Qur'an"),
        ),
        Prayer.SUNRISE to listOf(
            Msg("Sunrise: Fajr time has ended", "The Prophet ﷺ would stay sitting where he prayed Fajr until the sun had fully risen.", "Muslim 670", "Sahih"),
        ),
        Prayer.DHUHR to listOf(
            Msg("Dhuhr has begun", "“This is an hour when the gates of heaven are opened, and I love that a good deed of mine rises up in it.”", "Tirmidhi 478", "Sahih (al-Albani)"),
            Msg("Dhuhr has begun", "“Whoever prays twelve voluntary rakʿahs each day, Allah builds for him a house in Paradise.”", "Muslim 728", "Sahih"),
        ),
        Prayer.ASR to listOf(
            Msg("Asr: the middle prayer", "“Guard the prayers, and the middle prayer.” In the hadith, the middle prayer is named as Asr.", "Qur'an 2:238; Bukhari 2931, Muslim 627", "Qur'an / Sahih"),
            Msg("Asr has begun", "“Whoever misses Asr, it is as if he lost his family and his wealth.”", "Bukhari 552, Muslim 626", "Sahih"),
            Msg("Asr has begun", "Angels take turns among you by night and by day; they gather at Fajr and at Asr.", "Bukhari 555, Muslim 632", "Sahih"),
        ),
        Prayer.MAGHRIB to listOf(
            Msg("Maghrib has begun", "“My ummah will remain upon good as long as they do not delay Maghrib until the stars appear.”", "Abu Dawud 418", "Hasan sahih"),
            Msg("Maghrib has begun", "“So glorify Allah when you reach the evening and when you reach the morning.”", "Qur'an 30:17", "Qur'an"),
        ),
        Prayer.ISHA to listOf(
            Msg("Isha has begun", "“Whoever prays Isha in congregation, it is as if he prayed half the night.”", "Muslim 656", "Sahih"),
            Msg("Isha has begun", "“If they knew what is in Isha and Fajr, they would come to them even if they had to crawl.”", "Bukhari 657, Muslim 651", "Sahih"),
        ),
    )

    /** Used at Dhuhr on Fridays instead of the Dhuhr lines. */
    val friday = listOf(
        Msg("Jumuʿah Mubarak", "“The best day on which the sun rises is Friday.”", "Muslim 854", "Sahih"),
        Msg("Send salawat today", "“Send blessings upon me abundantly on Friday.”", "Abu Dawud 1047", "Sahih"),
    )

    fun pool(prayer: Prayer, date: LocalDate): List<Msg> = when {
        prayer == Prayer.SUNRISE -> specific.getValue(Prayer.SUNRISE)
        prayer == Prayer.DHUHR && date.dayOfWeek == DayOfWeek.FRIDAY -> friday + general
        else -> specific.getValue(prayer) + general
    }

    /**
     * One line per prayer per day, changing daily. The prayer's position shifts the starting point so
     * the five prayers of one day don't all land on a general line together.
     */
    fun forDay(prayer: Prayer, date: LocalDate): Msg {
        val list = pool(prayer, date)
        val i = Math.floorMod(date.toEpochDay() + prayer.ordinal * 2L, list.size.toLong()).toInt()
        val m = list[i]
        return m.copy(title = m.title.replace(P, prayer.english), body = m.body.replace(P, prayer.english))
    }
}
