package com.usman.miqaat

import com.usman.miqaat.ui.Section
import com.usman.miqaat.ui.SettingEntry
import com.usman.miqaat.ui.SettingsIndex
import org.junit.Assert.*
import org.junit.Test

/** Pure JVM tests of the declarative search index and ranking (no Android resources needed). */
class SettingsIndexTest {
    private val names = mapOf(
        Section.LOCATION to "Location", Section.TIMES to "Prayer times", Section.AZAAN to "Azaan & alerts", Section.IQAMAH to "Iqamah", Section.HIJRI to "Hijri calendar",
        Section.DISPLAY to "Display & art", Section.TEST to "Try it now", Section.HEALTH to "Reliability & backup", Section.PRIVACY to "Privacy", Section.ABOUT to "About"
    )
    private fun cat(s: Section) = names.getValue(s)
    // A small hand-built index in the same shape SettingsIndex.build() produces.
    private val index = names.flatMap { (sec, n) -> listOf(SettingEntry(sec, n, syn(sec), true)) } + listOf(
        SettingEntry(Section.AZAAN, "Volume", syn(Section.AZAAN), false),
        SettingEntry(Section.DISPLAY, "Time format", syn(Section.DISPLAY), false),
        SettingEntry(Section.HEALTH, "Battery optimisation", syn(Section.HEALTH), false)
    )
    private fun syn(s: Section) = when (s) {
        Section.AZAAN -> "alarm adhan azan sound loud"; Section.DISPLAY -> "appearance theme dark light font clock 24 hour"
        Section.HEALTH -> "backup export import restore alarm battery"; Section.LOCATION -> "city suburb place gps"; else -> ""
    }

    @Test fun emptyQueryReturnsNothing() { assertTrue(SettingsIndex.search(index, "", ::cat).isEmpty()); assertTrue(SettingsIndex.search(index, "   ", ::cat).isEmpty()) }

    @Test fun titleMatchOpensItsDestination() {
        val r = SettingsIndex.search(index, "volume", ::cat)
        assertEquals(Section.AZAAN, r.first().section); assertEquals("Volume", r.first().title)
    }

    @Test fun synonymsRouteToTheRightCategory() {
        assertEquals(Section.LOCATION, SettingsIndex.search(index, "city", ::cat).first().section)
        assertEquals(Section.DISPLAY, SettingsIndex.search(index, "appearance", ::cat).first().section)
        assertTrue(SettingsIndex.search(index, "backup", ::cat).any { it.section == Section.HEALTH })
        assertTrue(SettingsIndex.search(index, "alarm", ::cat).any { it.section == Section.AZAAN })
    }

    @Test fun everyTokenMustMatch() {
        assertTrue(SettingsIndex.search(index, "volume zzz", ::cat).isEmpty())
        assertTrue(SettingsIndex.search(index, "qwertyuiop", ::cat).isEmpty())
    }

    @Test fun caseAndWhitespaceInsensitive() { assertEquals(SettingsIndex.search(index, "TIME  format", ::cat).map { it.title }, listOf("Time format")) }

    @Test fun tryItNowIsADestinationAndTheOldNameIsGone() {
        assertTrue(SettingsIndex.search(index, "try it", ::cat).any { it.section == Section.TEST && it.isDestination })
        assertTrue(names.values.none { it.contains("Test & preview") })
    }

    @Test fun destinationOrderMatchesTheSpec() {
        assertEquals(listOf("LOCATION", "TIMES", "AZAAN", "IQAMAH", "HIJRI", "DISPLAY", "TEST", "HEALTH", "PRIVACY", "ABOUT"), Section.entries.map { it.name })
    }
}
