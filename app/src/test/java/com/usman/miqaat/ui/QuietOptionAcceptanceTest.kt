package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class QuietOptionAcceptanceTest : ComposeSupport() {
    @Test fun currentAndNextTimesAreEngineBackedIncludingMidnightAndOverrides() {
        val s = AppSettings(latitude = -33.86, longitude = 151.21, zoneId = "Australia/Sydney",
            locationName = "Sydney", locationSet = true, setupDone = true)
        val date = LocalDate.of(2026, 10, 13)
        val day = PrayerEngine.times(s, date)
        for (prayer in Prayer.prayersOnly) {
            val now = day[prayer].plusMinutes(12)
            val q = quietPrayerTimes(s, prayer, now)
            assertEquals(day[prayer].truncatedTo(ChronoUnit.MINUTES), q.current.truncatedTo(ChronoUnit.MINUTES))
            assertEquals(PrayerEngine.state(s, now).nextTime.truncatedTo(ChronoUnit.MINUTES), q.nextTime.truncatedTo(ChronoUnit.MINUTES))
            assertTrue(q.next.isPrayer)
            assertTrue(q.nextTime.isAfter(now))
        }
        val overnight = date.plusDays(1).atStartOfDay(s.zone()).plusMinutes(5)
        val q = quietPrayerTimes(s, Prayer.ISHA, overnight)
        assertEquals(day[Prayer.ISHA].truncatedTo(ChronoUnit.MINUTES), q.current.truncatedTo(ChronoUnit.MINUTES))
        assertEquals(Prayer.FAJR, q.next)
        assertEquals(PrayerEngine.times(s, date.plusDays(1))[Prayer.FAJR].truncatedTo(ChronoUnit.MINUTES), q.nextTime.truncatedTo(ChronoUnit.MINUTES))
        val imported = s.copy(useOverrides = true, overrides = mapOf(date.toString() to listOf(300, 390, 786, 977, 1120, 1210)))
        val now = date.atTime(13, 20).atZone(s.zone())
        val iq = quietPrayerTimes(imported, Prayer.DHUHR, now)
        assertEquals(13, iq.current.hour); assertEquals(6, iq.current.minute)
        assertEquals(16, iq.nextTime.hour); assertEquals(17, iq.nextTime.minute)
    }

    @Test fun optionTwoFitsNormalScreensAndKeepsLargeTextReachable() {
        for (dev in Dev.entries) for (urdu in listOf(false, true))
            for (scale in listOf(1f, 1.3f, 2f)) for (clock24 in listOf(false, true)) {
                val s = AppSettings(latitude = -33.86, longitude = 151.21, zoneId = "Australia/Sydney",
                    language = if (urdu) Language.UR else Language.EN, use24h = clock24,
                    locationName = "Sydney", locationSet = true, setupDone = true)
                val day = PrayerEngine.times(s, LocalDate.of(2026, 10, 13))
                val now = day[Prayer.DHUHR].plusMinutes(12)
                val phase = Phase.Quiet(Prayer.DHUHR, now.plusMinutes(15).toInstant().toEpochMilli())
                var stopped = false
                app.settings.update { s }
                try {
                    show(dev, AppTheme.MIQAAT, fontScale = scale, rtl = urdu) {
                        QuietPrayerContent(phase, s, now) { stopped = true }
                    }
                    scenario.onActivity { act ->
                        val view = act.findViewById<View>(android.R.id.content)
                        val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                        view.draw(Canvas(b))
                        val file = File("build/audit-screens/quiet-option2/quiet__${dev.label}__${if (urdu) "ur" else "en"}__${(scale * 100).toInt()}__${if (clock24) "24h" else "12h"}.png")
                        file.parentFile!!.mkdirs()
                        file.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        b.recycle()
                    }
                    val expected = quietPrayerTimes(s, phase.prayer, now)
                    rule.onNodeWithTag("quiet-current-time", true).assertTextEquals(L10n.iso(L10n.at(expected.current, clock24)))
                    rule.onNodeWithTag("quiet-next-name", true).assertTextEquals(L10n.prayer(s, expected.next).let { if (urdu) it else it.uppercase(java.util.Locale.ROOT) })
                    rule.onNodeWithTag("quiet-next-time", true).assertTextEquals(L10n.iso(L10n.at(expected.nextTime, clock24)))
                    val accessible = scale > 1.15f
                    val board = rule.onNodeWithTag("quiet-board", true).getUnclippedBoundsInRoot()
                    for (tag in listOf("quiet-live-clock", "quiet-brand", "quiet-progress", "quiet-current-name", "quiet-current-label", "quiet-current-time", "quiet-iqamah-complete", "quiet-next-label", "quiet-next-name", "quiet-next-time", "quiet-expiry-hint", "quiet-exit-label")) {
                        val node = rule.onNodeWithTag(tag, true)
                        if (accessible) node.performScrollTo()
                        node.assertIsDisplayed()
                        val bounds = node.getUnclippedBoundsInRoot()
                        assertTrue("$dev urdu=$urdu scale=$scale $tag outside screen", bounds.left >= board.left && bounds.right <= board.right && bounds.top >= board.top && bounds.bottom <= board.bottom)
                        val layouts = mutableListOf<TextLayoutResult>()
                        node.fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                        assertTrue(layouts.isNotEmpty())
                        for (l in layouts) assertFalse("$dev urdu=$urdu scale=$scale $tag clipped ${l.size} width=${l.didOverflowWidth} height=${l.didOverflowHeight} lines=${l.lineCount} text=${l.layoutInput.text} constraints=${l.layoutInput.constraints} style=${l.layoutInput.style}", l.multiParagraph.didExceedMaxLines || (0 until l.lineCount).any { line -> l.getLineRight(line)-l.getLineLeft(line)>node.fetchSemanticsNode().size.width+1f } || (l.lineCount>0 && l.getLineBottom(l.lineCount-1)-l.getLineTop(0)>node.fetchSemanticsNode().size.height+1f))
                    }
                    if (!accessible) {
                        val scroll = SemanticsMatcher("scroll") { it.config.getOrNull(SemanticsActions.ScrollBy) != null }
                        assertEquals(0, rule.onAllNodes(scroll, true).fetchSemanticsNodes().size)
                        val current = rule.onNodeWithTag("quiet-current-panel", true).getUnclippedBoundsInRoot()
                        val next = rule.onNodeWithTag("quiet-next-panel", true).getUnclippedBoundsInRoot()
                        assertTrue("Panels overlap", current.bottom <= next.top || current.right <= next.left || next.right <= current.left)
                        for (tag in listOf("quiet-progress", "quiet-current-name", "quiet-current-label", "quiet-current-time", "quiet-iqamah-complete")) {
                            val r = rule.onNodeWithTag(tag, true).getUnclippedBoundsInRoot()
                            assertTrue("$tag exceeds current panel", r.top >= current.top && r.bottom <= current.bottom)
                        }
                    }
                    val ret = rule.onNodeWithTag("quiet-return", true)
                    if (accessible) ret.performScrollTo()
                    ret.assertIsDisplayed()
                    val r = ret.getUnclippedBoundsInRoot()
                    assertTrue("Return target under 48dp", (r.bottom - r.top).value >= 47.99f)
                    scenario.onActivity { act ->
                        val view = act.findViewById<View>(android.R.id.content)
                        val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                        view.draw(Canvas(b))
                        val file = File("build/audit-screens/quiet-option2/quiet__${dev.label}__${if (urdu) "ur" else "en"}__${(scale * 100).toInt()}__${if (clock24) "24h" else "12h"}.png")
                        file.parentFile!!.mkdirs()
                        file.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        b.recycle()
                    }
                    ret.performClick()
                    assertTrue(stopped)
                } finally { runCatching { scenario.close() } }
            }
    }

    @Test fun afterIshaShowsTomorrowFajrAndTapAnywhereReturns() {
        val s = AppSettings(latitude = -33.86, longitude = 151.21, zoneId = "Australia/Sydney")
        val day = PrayerEngine.times(s, LocalDate.of(2026, 10, 13))
        val now = day[Prayer.ISHA].plusMinutes(12)
        var stopped = false
        app.settings.update { s }
        try {
            show(Dev.PHONE_PORTRAIT, AppTheme.MIQAAT) {
                QuietPrayerContent(Phase.Quiet(Prayer.ISHA, now.plusMinutes(15).toInstant().toEpochMilli()), s, now) { stopped = true }
            }
            rule.onNodeWithTag("quiet-tomorrow", true).assertIsDisplayed()
            rule.onNodeWithTag("quiet-next-name", true).assertTextEquals("FAJR")
            rule.onNodeWithTag("quiet-board", true).performClick()
            assertTrue(stopped)
        } finally { runCatching { scenario.close() } }
    }
}
