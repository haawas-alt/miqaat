package com.usman.miqaat.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class PrayerMessagesTest {
    private val all = PrayerMessages.general + PrayerMessages.specific.values.flatten() + PrayerMessages.friday
    private val monday = LocalDate.of(2026, 10, 19)

    @Test fun everyLineCarriesAReferenceAndGrade() {
        all.forEach {
            assertTrue(it.title, it.source.isNotBlank() && it.grade.isNotBlank())
            assertTrue(it.source, Regex("(Qur'an \\d+:\\d+|(Bukhari|Muslim|Tirmidhi|Abu Dawud) \\d+)").containsMatchIn(it.source))
            assertTrue(it.text, it.text.endsWith("(${it.source})"))
        }
    }

    @Test fun approvedListOnlyAndDisputedNarrationExcluded() {
        assertEquals(18, all.size)
        assertFalse(all.any { "586" in it.source || "Hajj" in it.body })
    }

    @Test fun everyPrayerHasALineAndTheNameIsFilledIn() {
        Prayer.entries.forEach { p ->
            for (d in 0L..13L) {
                val m = PrayerMessages.forDay(p, monday.plusDays(d))
                assertFalse(m.title, "{prayer}" in m.title || "{prayer}" in m.body)
            }
        }
    }

    @Test fun messageChangesFromOneDayToTheNext() {
        Prayer.prayersOnly.forEach { p ->
            for (d in 0L..5L) {
                val a = monday.plusDays(d); val b = a.plusDays(1)
                if (a.dayOfWeek == DayOfWeek.FRIDAY || b.dayOfWeek == DayOfWeek.FRIDAY) continue
                assertNotEquals("$p $a", PrayerMessages.forDay(p, a).text, PrayerMessages.forDay(p, b).text)
            }
        }
    }

    @Test fun everyLineInAPoolIsReachedWithinItsCycle() {
        Prayer.prayersOnly.forEach { p ->
            val pool = PrayerMessages.pool(p, monday)
            val seen = (0L until 70L).map { monday.plusDays(it) }.filter { it.dayOfWeek != DayOfWeek.FRIDAY || p != Prayer.DHUHR }
                .map { PrayerMessages.forDay(p, it).text }.toSet()
            assertEquals("$p", pool.size, seen.size)
        }
    }

    @Test fun fridayDhuhrUsesFridayLinesAndSunriseNeverInvitesPrayer() {
        val fri = LocalDate.of(2026, 10, 23)
        assertTrue(PrayerMessages.pool(Prayer.DHUHR, fri).containsAll(PrayerMessages.friday))
        assertFalse(PrayerMessages.pool(Prayer.DHUHR, monday).any { it in PrayerMessages.friday })
        val s = PrayerMessages.forDay(Prayer.SUNRISE, monday)
        assertTrue(s.title.contains("ended"))
        assertEquals(PrayerMessages.specific.getValue(Prayer.SUNRISE), PrayerMessages.pool(Prayer.SUNRISE, monday))
    }
}
