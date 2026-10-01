package com.usman.miqaat.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.data.*
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Morning, Evening and after-prayer Adhkār must each be reachable on Home, in every theme, inside their valid window, without
 * scrolling (the entry has to be on screen), and must not appear outside it.
 */
@RunWith(AndroidJUnit4::class)
class AdhkarEntryTest {
    @get:Rule val rule = createComposeRule()
    private val app get() = ApplicationProvider.getApplicationContext<Application>() as MiqaatApp

    private val zone = ZoneId.of("Australia/Sydney")
    private fun at(h: Int, m: Int) = ZonedDateTime.of(2026, 9, 30, h, m, 0, 0, zone)

    @Test fun entriesAreOnScreenInTheirWindowsForEveryTheme() {
        Str.apply(app, Language.EN)
        var theme by mutableStateOf(AppTheme.MIQAAT)
        var now by mutableStateOf(at(4, 30))
        val acts = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})
        rule.setContent {
            MiqaatTheme(theme) {
                val s = AppSettings(theme = theme, locationName = "Sydney, Australia", latitude = -33.8688, longitude = 151.2093, locationSet = true, setupDone = true, zoneId = "Australia/Sydney", adhkarEnabled = true, postPrayerAdhkar = true)
                HomeRouter(PrayerEngine.state(s, now), s, acts, showLarge = false) {}
            }
        }
        val cases = listOf(
            Triple("Fajr window", at(4, 30), "Morning adhk"),
            Triple("Asr window", at(15, 40), "Evening adhk"),
            Triple("After Dhuhr", at(12, 5), "After-prayer adhk"),
        )
        val failures = mutableListOf<String>()
        for (th in AppTheme.entries) for ((label, t, text) in cases) {
            theme = th; now = t
            rule.waitForIdle()
            val nodes = rule.onAllNodes(hasText(text, substring = true, ignoreCase = true), useUnmergedTree = true).fetchSemanticsNodes()
            if (nodes.isEmpty()) { failures += "${th.name} / $label: '$text' entry is missing"; continue }
            val root = rule.onRoot().fetchSemanticsNode().boundsInRoot
            val onScreen = nodes.any { n -> val b = n.boundsInRoot; b.width > 0 && b.height > 0 && b.left >= root.left - 1 && b.right <= root.right + 1 && b.top >= root.top - 1 && b.bottom <= root.bottom + 1 }
            if (!onScreen) failures += "${th.name} / $label: '$text' entry exists but is off screen"
        }
        // After Dhuhr has begun (12:30) neither the Morning nor the Evening prompt may appear (Morning runs from Fajr until Dhuhr).
        for (th in AppTheme.entries) {
            theme = th; now = at(12, 30)
            rule.waitForIdle()
            for (t in listOf("Morning adhk", "Evening adhk")) {
                if (rule.onAllNodes(hasText(t, substring = true, ignoreCase = true), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()) failures += "${th.name}: '$t' shown outside its window"
            }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
}
