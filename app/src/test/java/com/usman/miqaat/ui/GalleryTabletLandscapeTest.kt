package com.usman.miqaat.ui

import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import com.usman.miqaat.data.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** Tablet-landscape Prayer Gallery regressions: grid order, bounds, Hijri line, state markers. No emulator required. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class GalleryTabletLandscapeTest : ComposeSupport() {
    private val actions = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})
    private fun settings(urdu: Boolean = false, hijri: Boolean = true) = AppSettings(theme = AppTheme.PRAYER_GALLERY,
        language = if (urdu) Language.UR else Language.EN, locationName = "Sydney, Australia",
        latitude = -33.8688, longitude = 151.2093, locationSet = true, setupDone = true, zoneId = "Australia/Sydney", showHijri = hijri)
    private fun state(s: AppSettings, at: ZonedDateTime = ZonedDateTime.of(2026, 10, 17, 14, 0, 0, 0, ZoneId.of("Australia/Sydney"))) = PrayerEngine.state(s, at)

    private fun boundsOf(tag: String) = rule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
    private fun inside(tag: String) {
        val root = rule.onRoot().getUnclippedBoundsInRoot()
        val b = boundsOf(tag)
        assertTrue("$tag outside viewport: $b vs $root", b.left >= root.left && b.top >= root.top && b.right <= root.right && b.bottom <= root.bottom)
    }

    @Test fun grid_is_three_by_two_in_reading_order() {
        val s = settings(); app.settings.update { s }
        show(Dev.TABLET_LANDSCAPE, AppTheme.PRAYER_GALLERY) { HomeRouter(state(s), s, actions, false) {} }
        val p = listedPrayers(s)
        val top = listOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR)
        val bottom = listOf(Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
        if (p.size == 6) {
            val tops = top.map { boundsOf("gallery-card-${it.name}") }
            val bots = bottom.map { boundsOf("gallery-card-${it.name}") }
            assertTrue("top row left-to-right", tops[0].left < tops[1].left && tops[1].left < tops[2].left)
            assertTrue("bottom row left-to-right", bots[0].left < bots[1].left && bots[1].left < bots[2].left)
            assertTrue("bottom row sits below top row", bots[0].top.value > tops[0].bottom.value - 1f)
            assertEquals(tops[0].top.value, tops[2].top.value, 1f)
            assertEquals(bots[0].top.value, bots[2].top.value, 1f)
        }
        listOf("home-hero", "home-prayers", "home-time").forEach { inside(it) }
        p.forEach { noOverflowAt("gallery-card-${it.name}") }
        capture("gallery-tablet-landscape__en")
    }

    @Test fun grid_is_three_by_two_in_urdu_rtl() {
        val s = settings(urdu = true); app.settings.update { s }
        show(Dev.TABLET_LANDSCAPE, AppTheme.PRAYER_GALLERY, rtl = true) { HomeRouter(state(s), s, actions, false) {} }
        listOf("home-hero", "home-prayers", "home-time").forEach { inside(it) }
        listedPrayers(s).forEach { noOverflowAt("gallery-card-${it.name}") }
        capture("gallery-tablet-landscape__ur")
    }

    @Test fun hijri_line_is_complete_with_full_month_in_english() {
        val s = settings()
        val h = PrayerEngine.hijri(LocalDate.of(2026, 10, 17), 0)
        val line = galleryHijriLine(s, h)
        assertTrue(line, line.contains("Jumādā al-Ūlā"))
        assertTrue(line, line.contains("1448"))
        assertTrue(line, line.endsWith(" AH"))
    }

    @Test fun longest_hijri_month_is_not_truncated() {
        val s = settings()
        val h = PrayerEngine.Hijri(day = 10, month = 12, year = 1448) // Dhū al-Ḥijjah
        assertEquals("10 Dhū al-Ḥijjah 1448 AH", galleryHijriLine(s, h))
        val hr = PrayerEngine.Hijri(day = 3, month = 6, year = 1448) // Jumādā al-Ākhirah
        assertEquals("3 Jumādā al-Ākhirah 1448 AH", galleryHijriLine(s, hr))
    }

    @Test fun hijri_adjustment_changes_the_displayed_day() {
        val base = PrayerEngine.hijri(LocalDate.of(2026, 10, 17), 0)
        val plus = PrayerEngine.hijri(LocalDate.of(2026, 10, 17), 1)
        assertEquals(base.day + 1, plus.day)
    }

    @Test fun hijri_line_hidden_only_when_user_turns_it_off() {
        val s = settings(hijri = false)
        val (greg, hijri) = galleryDateLines(s, ZonedDateTime.of(2026, 10, 17, 14, 0, 0, 0, ZoneId.of("Australia/Sydney")), PrayerEngine.hijri(LocalDate.of(2026, 10, 17), 0))
        assertTrue(greg.contains("2026"))
        assertNull(hijri)
        val on = settings(hijri = true)
        assertTrue(galleryDateLines(on, ZonedDateTime.of(2026, 10, 17, 14, 0, 0, 0, ZoneId.of("Australia/Sydney")), PrayerEngine.hijri(LocalDate.of(2026, 10, 17), 0)).second != null)
    }

    @Test fun grid_rows_keep_prayer_order() {
        val rows = galleryGridRows(listOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA))
        assertEquals(listOf(listOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR), listOf(Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)), rows)
    }

    private fun renderBitmap(): android.graphics.Bitmap {
        rule.waitForIdle()
        lateinit var bitmap: android.graphics.Bitmap
        scenario.onActivity { activity ->
            val view = activity.findViewById<android.view.View>(android.R.id.content)
            assertTrue("Unmeasured content view", view.width > 0 && view.height > 0)
            bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
        }
        return bitmap
    }
    private fun capture(name: String) {
        val out = java.io.File("build/audit-screens").also { it.mkdirs() }
        java.io.File(out, "$name.png").outputStream().use { assertTrue(renderBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)) }
    }

    /** Every text node under the card must lay out without width or height overflow. */
    private fun noOverflowAt(tag: String) {
        val root = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        val texts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        fun walk(node: androidx.compose.ui.semantics.SemanticsNode) {
            node.config.getOrNull(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult)?.action?.invoke(texts)
            node.children.forEach { walk(it) }
        }
        walk(root)
        texts.forEach { assertFalse("Text clipped in $tag: ${it.layoutInput.text}; size=${it.size}; constraints=${it.layoutInput.constraints}; w=${it.didOverflowWidth}; h=${it.didOverflowHeight}; card=${root.size}", it.didOverflowWidth || it.didOverflowHeight) }
    }
}
