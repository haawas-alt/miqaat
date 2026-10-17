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

    /** Every text node under the card must sit inside the card's own bounds (geometry check, not text-flag heuristics). */
    private fun noOverflowAt(tag: String) {
        val card = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        val cardBounds = card.boundsInRoot
        val bad = mutableListOf<String>()
        fun walk(node: androidx.compose.ui.semantics.SemanticsNode) {
            val text = node.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)
            if (!text.isNullOrEmpty()) {
                val b = node.boundsInRoot
                if (b.left < cardBounds.left - 0.5f || b.top < cardBounds.top - 0.5f || b.right > cardBounds.right + 0.5f || b.bottom > cardBounds.bottom + 0.5f)
                    bad += "${text.first().text} $b outside $cardBounds"
            }
            node.children.forEach { walk(it) }
        }
        walk(card)
        assertTrue("Text outside $tag: $bad", bad.isEmpty())
    }
    private val sydney = ZoneId.of("Australia/Sydney")

    private fun formatCase(name: String, urdu: Boolean, use24h: Boolean, at: ZonedDateTime, fontScale: Float) {
        val s = settings(urdu = urdu).copy(use24h = use24h)
        app.settings.update { s }
        show(Dev.TABLET_LANDSCAPE, AppTheme.PRAYER_GALLERY, fontScale = fontScale, rtl = urdu) { HomeRouter(state(s, at), s, actions, false) {} }
        capture("gallery-$name")
        if (fontScale <= 1.15f) {
            listOf("home-hero", "home-prayers", "home-time").forEach { inside(it) }
            listedPrayers(s).forEach { noOverflowAt("gallery-card-${it.name}") }
        }
    }

    @Test fun capture_en_12h_now_next() = formatCase("en-12h-now-next", false, false, ZonedDateTime.of(2026, 10, 17, 12, 50, 0, 0, sydney), 1f)
    @Test fun capture_en_24h() = formatCase("en-24h", false, true, ZonedDateTime.of(2026, 10, 17, 14, 0, 0, 0, sydney), 1f)
    @Test fun capture_ur_12h() = formatCase("ur-12h", true, false, ZonedDateTime.of(2026, 10, 17, 14, 0, 0, 0, sydney), 1f)
    @Test fun capture_en_overnight() = formatCase("en-overnight-2359", false, false, ZonedDateTime.of(2026, 10, 17, 23, 59, 0, 0, sydney), 1f)
    @Test fun capture_en_130pct() = formatCase("en-130pct", false, false, ZonedDateTime.of(2026, 10, 17, 14, 0, 0, 0, sydney), 1.3f)
    @Test fun capture_en_200pct() = formatCase("en-200pct", false, false, ZonedDateTime.of(2026, 10, 17, 14, 0, 0, 0, sydney), 2f)
}
