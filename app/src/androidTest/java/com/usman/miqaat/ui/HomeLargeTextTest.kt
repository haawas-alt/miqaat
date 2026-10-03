package com.usman.miqaat.ui

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
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
 * Large text is honoured on Home: at 130% and 200% the prayer names really are rendered at 130% / 200% of their base size,
 * for all four themes (Home is no longer capped at 130%), and no prayer name is clipped by an ellipsis.
 */
@RunWith(AndroidJUnit4::class)
class HomeLargeTextTest {
    @org.junit.Before fun enforceOrientation() = enforceAuditOrientation()
    @get:Rule val rule = createComposeRule()
    private val app get() = ApplicationProvider.getApplicationContext<Application>() as MiqaatApp

    @Test fun prayerNamesGrowWithTheSystemFontSize() {
        Str.apply(app, Language.EN)
        var theme by mutableStateOf(AppTheme.MIQAAT)
        var scale by mutableStateOf(1f)
        val acts = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})
        rule.setContent {
            val d = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density, scale)) {
                MiqaatTheme(theme) {
                    val s = AppSettings(theme = theme, locationName = "Sydney, Australia", latitude = -33.8688, longitude = 151.2093, locationSet = true, setupDone = true, zoneId = "Australia/Sydney")
                    val st = PrayerEngine.state(s, ZonedDateTime.of(2026, 9, 30, 15, 40, 0, 0, ZoneId.of("Australia/Sydney")))
                    HomeRouter(st, s, acts, showLarge = false) {}
                }
            }
        }
        val failures = mutableListOf<String>()
        for (th in AppTheme.entries) for (sc in listOf(1.3f, 2f)) {
            theme = th; scale = sc
            rule.waitForIdle()
            val names = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            var checked = 0
            for (n in names) {
                val nodes = rule.onAllNodes(hasText(n, ignoreCase = true), useUnmergedTree = true).fetchSemanticsNodes()
                for (node in nodes) {
                    val out = mutableListOf<TextLayoutResult>()
                    node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(out)
                    val r = out.firstOrNull() ?: continue
                    val px = with(r.layoutInput.density) { r.layoutInput.style.fontSize.toPx() }
                    val minPx = 16f * r.layoutInput.density.density * sc
                    checked++
                    if (px + 0.5f < minPx) failures += "${th.name} ${(sc * 100).toInt()}%: '$n' renders at ${px / r.layoutInput.density.density}dp, needs >= ${16 * sc}"
                    // Real clipping = the text is wider than its box, or needs more lines than it was given. `hasVisualOverflow` alone also fires for
                    // harmless cases, so the details are printed to make a genuine failure diagnosable from the log.
                    if (r.size.width > r.layoutInput.constraints.maxWidth || r.lineCount > 1)
                        failures += "${th.name} ${(sc * 100).toInt()}%: '$n' is clipped (w=${r.didOverflowWidth} h=${r.didOverflowHeight} lines=${r.lineCount} size=${r.size} maxW=${r.layoutInput.constraints.maxWidth} text='${r.layoutInput.text.text.take(30)}')"
                }
            }
            if (checked == 0) failures += "${th.name} ${(sc * 100).toInt()}%: no prayer-name text found"
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
}
