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
class ApprovedIqamahAcceptanceTest : ComposeSupport() {
    private val tags = listOf("iqamah-brand", "iqamah-mode", "iqamah-location", "iqamah-stage-0", "iqamah-stage-1", "iqamah-stage-2", "iqamah-stage-3", "iqamah-live-clock", "iqamah-prayer-arabic", "iqamah-prayer-name",
        "iqamah-until", "iqamah-seconds", "iqamah-unit", "iqamah-at-label", "iqamah-at-time", "iqamah-reminder", "iqamah-source",
        "iqamah-hint", "iqamah-start-label", "iqamah-dismiss-label")
    private fun themes() = AppTheme.entries.filter { System.getenv("MIQAAT_VISUAL_THEME").let { only -> only == null || it.name == only } }

    private fun check(dev: Dev, theme: AppTheme, lang: Language, name: String, q: String? = null, scale: Float = 1f, minimal: Boolean = false, overnight: Boolean = false) {
        val s = AppSettings(theme = theme, language = lang, latitude = -33.93, longitude = 151.19, locationName = "Mascot, NSW",
            zoneId = "Australia/Sydney", locationSet = true, setupDone = true,
            artTheme = if (minimal) ArtTheme.MINIMAL else ArtTheme.GEOMETRIC, use24h = minimal)
        val now = ZonedDateTime.of(2026, 10, 18, if (overnight) 23 else 19, if (overnight) 59 else 21, 0, 0, s.zone())
        val phase = Phase.IqamahCountdown(if (overnight) Prayer.ISHA else Prayer.MAGHRIB, now.minusMinutes(7).toInstant().toEpochMilli(), now.plusMinutes(3).toInstant().toEpochMilli())
        var returns = 0; var starts = 0
        app.settings.update { s }
        try {
            show(dev, theme, fontScale = scale, rtl = lang == Language.UR, language = lang, qualifiers = q) {
                IqamahPrayerContent(phase, s, now, { returns++ }, { starts++ })
            }
            val prefix = "build/iqamah-designs/${theme.name}/$name-${lang.name}"
            scenario.onActivity { act ->
                val view = act.findViewById<View>(android.R.id.content)
                val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(b))
                File("$prefix.png").also { it.parentFile.mkdirs() }.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
                b.recycle()
            }
            File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
            val root = rule.onNodeWithTag("iqamah-board", true).fetchSemanticsNode().boundsInRoot
            rule.onNodeWithTag("quiet-art-${theme.name}", true).assertExists()
            rule.onNodeWithTag("iqamah-seconds", true).assertTextEquals("03:00")
            rule.onNodeWithTag("iqamah-at-time", true).assertTextEquals(L10n.iso(L10n.at(now.plusMinutes(3), s.use24h)))
            rule.onNodeWithTag("iqamah-live-clock", true).assertTextEquals(L10n.iso(L10n.at(now, s.use24h)))
            rule.onNodeWithTag("iqamah-stage-1", true).assertTextEquals(Str[com.usman.miqaat.R.string.s_iqamah_countdown].let { if (lang == Language.UR) it else it.uppercase(java.util.Locale.ROOT) })
            for (tag in tags + if (overnight) listOf("iqamah-tomorrow") else emptyList()) {
                val target = rule.onNodeWithTag(tag, true)
                if (scale > 1.15f || dev == Dev.PHONE_LANDSCAPE) {
                    if (tag !in listOf("iqamah-seconds", "iqamah-unit", "iqamah-until", "iqamah-start-label", "iqamah-dismiss-label") || dev != Dev.PHONE_LANDSCAPE) target.performScrollTo()
                }
                target.assertIsDisplayed()
                val node = target.fetchSemanticsNode()
                val bounds = node.boundsInRoot
                assertTrue("$theme/$name/$lang $tag outside viewport: $bounds vs $root", bounds.left >= root.left && bounds.top >= root.top && bounds.right <= root.right && bounds.bottom <= root.bottom)
                val layouts = mutableListOf<TextLayoutResult>()
                node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                assertTrue("$tag has no native text layout", layouts.isNotEmpty())
                layouts.forEach { l ->
                    val w = (0 until l.lineCount).maxOfOrNull { l.getLineRight(it)-l.getLineLeft(it) } ?: 0f
                    val h = if (l.lineCount>0) l.getLineBottom(l.lineCount-1)-l.getLineTop(0) else 0f
                    assertFalse("$theme/$name/$lang $tag clipped '${l.layoutInput.text}': ${node.size} vs $w,$h", l.multiParagraph.didExceedMaxLines || w>node.size.width+1f || h>node.size.height+1f)
                }
            }
            if (theme == AppTheme.KISWAH) {
                val band = rule.onNodeWithTag("kiswah-calligraphy-band", true)
                if (scale > 1.15f || dev == Dev.PHONE_LANDSCAPE) band.performScrollTo()
                band.assertIsDisplayed().assertContentDescriptionEquals(Kiswah.BAND)
            }
            if (scale <= 1.15f && dev != Dev.PHONE_LANDSCAPE) {
                val dial = rule.onNodeWithTag("iqamah-dial", true).fetchSemanticsNode().boundsInRoot
                val details = rule.onNodeWithTag("iqamah-details-panel", true).fetchSemanticsNode().boundsInRoot
                assertTrue("Counter and details overlap", dial.bottom<=details.top || dial.right<=details.left || details.right<=dial.left)
            }
            for ((tag, expected) in listOf("iqamah-start" to 1, "iqamah-dismiss" to 1)) {
                val action = rule.onNodeWithTag(tag, true)
                if (scale > 1.15f && dev != Dev.PHONE_LANDSCAPE) action.performScrollTo()
                action.assertIsDisplayed()
                val bounds = action.fetchSemanticsNode().boundsInRoot
                assertTrue("Action touch target below 48dp", bounds.height >= 48*app.resources.displayMetrics.density)
                action.performClick()
                assertEquals(expected, if (tag == "iqamah-start") starts else returns)
            }
        } finally { scenario.close() }
    }

    @Test fun approvedPhoneAndTabletScreens() {
        for (theme in themes()) for (lang in Language.entries) for (dev in listOf(Dev.PHONE_PORTRAIT, Dev.TABLET_PORTRAIT, Dev.TABLET_LANDSCAPE))
            check(dev, theme, lang, dev.label)
    }
    @Test fun compactScreensKeepAllInformationAndActionsReachable() {
        for (theme in themes()) for (lang in Language.entries) {
            check(Dev.PHONE_PORTRAIT, theme, lang, "compact-phone", "w360dp-h800dp-port-xhdpi")
            check(Dev.TABLET_LANDSCAPE, theme, lang, "compact-tablet", "w960dp-h600dp-land-xhdpi")
            check(Dev.PHONE_LANDSCAPE, theme, lang, "compact-landscape")
        }
    }
    @Test fun largeTextAndMinimalOvernightPreferencesRemainUsable() {
        for (theme in themes()) for (lang in Language.entries) {
            check(Dev.PHONE_PORTRAIT, theme, lang, "large-text", scale = 2f, overnight = true)
            check(Dev.TABLET_LANDSCAPE, theme, lang, "minimal-24h-overnight", minimal = true, overnight = true)
        }
    }
}
