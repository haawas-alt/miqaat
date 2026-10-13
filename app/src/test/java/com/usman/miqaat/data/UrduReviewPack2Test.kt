package com.usman.miqaat.data

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

/** Guards for the 13 Oct 2026 Urdu review (row IDs in comments). Wording only; not a religious certification. */
class UrduReviewPack2Test {
    @After fun reset() { L10n.uiUrdu = false }

    /** R-01 / A32 and R-02 / A74: the evening supplication says "night" (رات), the morning one "day" (دن); phone and tablet read the same record. */
    @Test fun eveningSaysNightMorningSaysDay() {
        val evening = UrduContent.dhikrMeanings.getValue("amsayna")
        val morning = UrduContent.dhikrMeanings.getValue("asbahna")
        assertEquals(2, Regex("اس رات").findAll(evening).count())
        assertFalse(evening.contains("اس دن"))
        assertEquals(2, Regex("اس دن").findAll(morning).count())
        assertFalse(morning.contains("اس رات"))
    }

    /** Only the citation rows approved in the review are localised; "pending source check" rows stay exactly as written. */
    @Test fun onlyApprovedCitationsAreLocalised() {
        L10n.uiUrdu = true
        val pendingIds = setOf("kursi", "ikhlas", "falaq", "nas", "bika_e", "radeetu", "audhu", "tahlil", "salawat", "pp_kursi", "pp_ikhlas")
        (Adhkar.all + Adhkar.postPrayer).filter { it.id in pendingIds }.forEach { assertEquals(it.id, it.source, L10n.adhkarSource(it.source)) }
        (Adhkar.all + Adhkar.postPrayer).filter { it.id !in pendingIds }.forEach {
            val ur = L10n.adhkarSource(it.source)
            assertTrue("${it.id} not localised: $ur", ur.any { ch -> ch in '؀'..'ۿ' })
            assertFalse("${it.id} still has English book name: $ur", ur.contains("Ṣaḥīḥ") || ur.contains("Abū Dāwūd") || ur.contains("Tirmidhī"))
            Regex("\\d+\\w?").findAll(it.source).map { m -> m.value }.forEach { n -> assertTrue("${it.id} lost number $n", ur.contains(n)) }
        }
        L10n.uiUrdu = false
        (Adhkar.all + Adhkar.postPrayer).forEach { assertEquals(it.source, L10n.adhkarSource(it.source)) }
    }

    /** P1-14: hadith citations keep their numbers. */
    @Test fun hadithCitationKeepsNumbers() {
        L10n.uiUrdu = true
        assertEquals("صحیح بخاری، حدیث 6018 · صحیح مسلم، حدیث 47", L10n.source("Ṣaḥīḥ al-Bukhārī 6018 · Ṣaḥīḥ Muslim 47"))
    }

    /** P1-04 / E21-E32: Urdu spoken rows use the agreed state labels and never the English words. */
    @Test fun spokenRowsInUrdu() {
        val s = AppSettings(language = Language.UR)
        L10n.uiUrdu = true
        val z = ZoneId.of("Australia/Sydney")
        val t = ZonedDateTime.of(2026, 10, 12, 6, 18, 0, 0, z)
        val sunrisePassed = L10n.spokenRow(s, "طلوعِ آفتاب", Prayer.SUNRISE, t, t.plusHours(2), false, false, true, null, null, false)
        assertTrue(sunrisePassed, sunrisePassed.contains("طلوعِ آفتاب ہو چکا ہے"))
        val next = L10n.spokenRow(s, "ظہر", Prayer.DHUHR, t.plusHours(6), t, false, true, false, null, t.plusHours(10), false)
        assertTrue(next, next.contains("اگلی نماز") && next.contains("وقت کا اختتام:") && next.contains("بجے"))
        listOf("passed", "next prayer", "ends", "now").forEach { assertFalse(next.contains(it)); assertFalse(sunrisePassed.contains(it)) }
    }
}
