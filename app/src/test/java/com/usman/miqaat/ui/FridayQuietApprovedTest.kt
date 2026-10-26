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
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class FridayQuietApprovedTest : ComposeSupport() {
    private fun settings(theme: AppTheme, lang: Language) = AppSettings(theme = theme, language = lang,
        latitude = -33.93, longitude = 151.19, locationName = "Gledswood Hills, NSW", zoneId = "Australia/Sydney",
        locationSet = true, setupDone = true, jumuahEnabled = true, jumuahMinutes = 13 * 60 + 15,
        jumuahIqamahMinutes = 13 * 60 + 30, iqamahEnabled = true)
    private fun readable(tag: String) {
        val q = rule.onNodeWithTag(tag, true); q.assertIsDisplayed()
        val n = q.fetchSemanticsNode(); val root = rule.onRoot().fetchSemanticsNode().boundsInRoot; val b = n.boundsInRoot
        assertTrue("$tag outside root $b / $root", b.left >= root.left && b.right <= root.right && b.top >= root.top && b.bottom <= root.bottom)
        val layouts = mutableListOf<TextLayoutResult>()
        n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        layouts.forEach { l ->
            val w = (0 until l.lineCount).maxOfOrNull { l.getLineRight(it) - l.getLineLeft(it) } ?: 0f
            val h = if (l.lineCount > 0) l.getLineBottom(l.lineCount - 1) - l.getLineTop(0) else 0f
            assertFalse("$tag clipped ${l.layoutInput.text} $w/$h vs ${n.size}", l.multiParagraph.didExceedMaxLines || w > n.size.width + 1f || h > n.size.height + 1f)
        }
    }
    private fun capture(theme: AppTheme, dev: Dev, lang: Language, kind: String) {
        rule.mainClock.advanceTimeBy(1000); rule.waitForIdle()
        val path = File("build/friday-quiet-review/${theme.name}/${dev.label}-${lang.name}-$kind.png")
        path.parentFile.mkdirs()
        scenario.onActivity { a ->
            val v = a.findViewById<View>(android.R.id.content)
            val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            v.draw(Canvas(b)); path.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }; b.recycle()
        }
        File(path.path + ".txt").writeText(rule.onRoot(true).printToString(100))
    }
    @Test fun fridayBoardsFitEngineFactsAndActionsAcrossEveryThemeAndDevice() {
        val failures = mutableListOf<String>()
        for (theme in AppTheme.entries) for (lang in Language.entries) for (dev in Dev.entries) {
            val s = settings(theme, lang); val now = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, s.zone())
            val state = PrayerEngine.state(s, now)
            var friday = 0; var qibla = 0; var table = 0; var settings = 0
            val actions = HomeActions({ table++ }, { settings++ }, {}, { qibla++ }, {}, { friday++ }, {}, false, {}, {})
            try {
                show(dev, theme, language = lang, rtl = lang == Language.UR) { HomeRouter(state, s, actions, false, {}) }
                capture(theme, dev, lang, "friday")
                readable("friday-banner"); readable("friday-hero-time")
                rule.onNodeWithTag("friday-hero-time", true).assertTextEquals(L10n.at(state.heroTime, s.use24h))
                for (p in Prayer.entries) {
                    readable("friday-time-${p.name}")
                    val bounds = rule.onNodeWithTag("approved-row-${p.name}", true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Prayer row <48dp", bounds.height >= 48 * app.resources.displayMetrics.density - .5f)
                }
                rule.onNodeWithTag("friday-action-0", true).performClick(); assertEquals(1, friday)
                rule.onNodeWithTag("friday-action-1", true).performClick(); assertEquals(2, friday)
                rule.onNodeWithTag("friday-action-2", true).performClick(); assertEquals(3, friday)
                rule.onNodeWithContentDescription(if (lang == Language.UR) "قبلہ" else "Qibla").performClick(); assertEquals(1, qibla)
            } catch (e: AssertionError) { failures += "$theme/$dev/$lang: ${e.message}" } finally { scenario.close() }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
    @Test fun quietBoardsKeepPhaseTimesReturnAndNativeTextInEveryOrientation() {
        val failures = mutableListOf<String>()
        for (theme in AppTheme.entries) for (lang in Language.entries) for (dev in Dev.entries) {
            val s = settings(theme, lang); val now = ZonedDateTime.of(2026, 10, 30, 19, 24, 0, 0, s.zone())
            val phase = Phase.Quiet(Prayer.MAGHRIB, now.plusMinutes(10).toInstant().toEpochMilli())
            val times = quietPrayerTimes(s, phase.prayer, now); var stops = 0
            try {
                show(dev, theme, language = lang, rtl = lang == Language.UR) { QuietPrayerContent(phase, s, now) { stops++ } }
                capture(theme, dev, lang, "quiet")
                for (tag in listOf("quiet-brand", "quiet-live-clock", "quiet-current-arabic", "quiet-current-name", "quiet-progress", "quiet-current-label", "quiet-current-time", "quiet-iqamah-complete", "quiet-next-label", "quiet-next-name", "quiet-next-time", "quiet-expiry-hint", "quiet-exit-label")) readable(tag)
                rule.onNodeWithTag("quiet-current-time", true).assertTextEquals(L10n.iso(L10n.at(times.current, s.use24h)))
                rule.onNodeWithTag("quiet-next-time", true).assertTextEquals(L10n.iso(L10n.at(times.nextTime, s.use24h)))
                rule.onNodeWithTag("quiet-return", true).performClick(); assertEquals(1, stops)
                rule.onNodeWithTag("quiet-board", true).performClick(); assertEquals(2, stops)
            } catch (e: AssertionError) { failures += "$theme/$dev/$lang: ${e.message}" } finally { scenario.close() }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
    @Test fun fridayDoesNotChangeTheConfiguredPrayerEngineOrOtherDays() {
        val s = settings(AppTheme.MIQAAT, Language.EN)
        val friday = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, s.zone())
        assertTrue(fridayHomepageActive(s, friday))
        assertFalse(fridayHomepageActive(s.copy(fridayReminders = false), friday))
        assertFalse(fridayHomepageActive(s, friday.minusDays(1)))
        assertFalse(fridayHomepageActive(s, PrayerEngine.times(s, friday.toLocalDate())[Prayer.MAGHRIB]))
        assertFalse(fridayHomepageActive(s, friday.plusDays(1)))
        assertEquals(13, PrayerEngine.times(s, friday.toLocalDate())[Prayer.DHUHR].hour)
        assertEquals(15, PrayerEngine.times(s, friday.toLocalDate())[Prayer.DHUHR].minute)
        val regular = s.copy(jumuahEnabled = false)
        assertEquals(PrayerEngine.calculated(regular, friday.toLocalDate())[Prayer.DHUHR], PrayerEngine.times(regular, friday.toLocalDate())[Prayer.DHUHR])
    }
}
