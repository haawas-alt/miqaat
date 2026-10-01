package com.usman.miqaat.ui

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.usman.miqaat.azaan.AzaanService
import com.usman.miqaat.data.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/**
 * The acceptance screenshot matrix, rendered on the emulator. The CI "emulator" job runs it once per device profile and
 * orientation and publishes the PNGs to the ci-screenshots branch. Screens × four themes × 100/130/200% × English/Urdu.
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotMatrixTest : ShotSupport() {
    @After fun done() = flushErrors()

    @Composable private fun settingsUi(s: AppSettings, initial: Section?) {
        val live by app.settings.settings.collectAsState()
        SettingsScreen(app.settings, live.copy(theme = s.theme, language = s.language, locationName = s.locationName, locationSet = true, setupDone = true), initial) {}
    }
    private val themes = AppTheme.entries

    @Test fun homes() { for (t in themes) for (sc in listOf(1f, 1.3f, 2f)) shot("home", t, sc) { s, st -> HomeRouter(st, s, actions, showLarge = false) {} } }

    @Test fun homesUrdu() { for (t in themes) for (sc in listOf(1f, 1.3f)) shot("home", t, sc, rtl = true) { s, st -> HomeRouter(st, s, actions, showLarge = false) {} } }

    @Test fun settingsLanding() {
        for (t in themes) for (sc in listOf(1f, 1.3f, 2f)) shot("settings", t, sc) { s, _ -> settingsUi(s, null) }
        for (t in themes) shot("settings", t, 1f, rtl = true) { s, _ -> settingsUi(s, null) }
    }

    @Test fun settingsCategories() {
        for (t in themes) for (sec in Section.entries) shot("settings-${sec.name.lowercase()}", t) { s, _ -> settingsUi(s, sec) }
        for (t in themes) for (sec in listOf(Section.DISPLAY, Section.TEST)) shot("settings-${sec.name.lowercase()}", t, 2f) { s, _ -> settingsUi(s, sec) }
        for (t in themes) for (sec in listOf(Section.DISPLAY, Section.LOCATION)) shot("settings-${sec.name.lowercase()}", t, 1f, rtl = true) { s, _ -> settingsUi(s, sec) }
    }

    @Test fun secondaryPages() {
        for (t in themes) for (sc in listOf(1f, 2f)) {
            shot("timetable", t, sc) { s, _ -> TimetableScreen(s) {} }
            shot("qibla", t, sc) { s, _ -> QiblaScreen(s, fixedHeading = 250f) {} }
            shot("adhkar-morning", t, sc) { _, _ -> AdhkarScreen(AdhkarMode.MORNING) {} }
            shot("adhkar-evening", t, sc) { _, _ -> AdhkarScreen(AdhkarMode.EVENING) {} }
            shot("friday", t, sc) { s, _ -> FridayScreen(s) {} }
            shot("learn", t, sc) { s, _ -> LearnScreen(s) {} }
        }
        for (t in themes) for (page in listOf("timetable", "qibla", "adhkar-morning", "friday", "learn")) shot(page, t, 1f, rtl = true) { s, _ ->
            when (page) { "timetable" -> TimetableScreen(s) {}; "qibla" -> QiblaScreen(s, fixedHeading = 250f) {}; "adhkar-morning" -> AdhkarScreen(AdhkarMode.MORNING) {}; "friday" -> FridayScreen(s) {}; else -> LearnScreen(s) {} }
        }
    }

    /** One screenshot per distinct posture, for every theme: each position must show its own illustration. */
    @Test fun learnLessons() {
        val acts = com.usman.miqaat.data.Learn.actions(com.usman.miqaat.data.Learn.Lesson.FOUR)
        for (t in themes) for (p in com.usman.miqaat.data.Learn.Posture.entries) {
            val idx = acts.indexOfFirst { it.posture == p }
            shot("lesson-${p.name.lowercase()}", t, 1f) { s, _ -> LearnScreen(s, com.usman.miqaat.data.Learn.Lesson.FOUR, idx) {} }
        }
    }

    @Test fun azaanFlow() {
        val now = System.currentTimeMillis()
        for (t in themes) for (sc in listOf(1f, 2f)) {
            shot("azaan", t, sc) { _, _ -> AzaanScreen(AzaanService.Phase.Azaan(Prayer.DHUHR, false), {}, {}) }
            shot("dua", t, sc) { _, _ -> AzaanScreen(AzaanService.Phase.Dua(Prayer.DHUHR), {}, {}) }
            shot("hadith", t, sc) { _, _ -> AzaanScreen(AzaanService.Phase.HadithPhase(Prayer.DHUHR, HadithLibrary.all.first(), now, now + 60_000, false), {}, {}) }
            shot("iqamah", t, sc) { _, _ -> AzaanScreen(AzaanService.Phase.IqamahCountdown(Prayer.DHUHR, now, now + 300_000), {}, {}) }
        }
        for (t in themes) shot("dua", t, 1f, rtl = true) { _, _ -> AzaanScreen(AzaanService.Phase.Dua(Prayer.DHUHR), {}, {}) }
    }

    @Test fun states() {
        for (t in themes) for (sc in listOf(1f, 2f)) {
            val ok = ReadinessState("Dhuhr · 12:24 PM", "Sydney", listOf(ReadinessCheck("Exact alarms", "On", true, null), ReadinessCheck("Battery", "Unrestricted", true, null), ReadinessCheck("Notifications", "Allowed", true, null)))
            val warn = ReadinessState("Asr · 3:52 PM", "Sydney", listOf(ReadinessCheck("Exact alarms", "Off", false) {}, ReadinessCheck("Battery", "Restricted", false) {}, ReadinessCheck("Notifications", "Allowed", true, null)))
            shot("state-ready", t, sc) { _, _ -> androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding(16.dp)) { ReadinessSummary(ok, wide = true, onOpen = {}) } }
            shot("state-warning", t, sc) { _, _ -> androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding(16.dp)) { ReadinessSummary(warn, wide = true, onOpen = {}) } }
        }
    }
}

