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
class ApprovedQuietAcceptanceTest : ComposeSupport() {
    private val tags = listOf("quiet-brand", "quiet-live-clock", "quiet-progress", "quiet-current-arabic", "quiet-current-name",
        "quiet-current-label", "quiet-current-time", "quiet-iqamah-complete", "quiet-next-label", "quiet-next-name",
        "quiet-next-time", "quiet-expiry-hint", "quiet-exit-label")
    private fun themes() = AppTheme.entries.filter { System.getenv("MIQAAT_VISUAL_THEME").let { only -> only == null || it.name == only } }

    private fun check(dev: Dev, theme: AppTheme, lang: Language, name: String, q: String? = null, scale: Float = 1f, minimal: Boolean = false, overnight: Boolean = false) {
        val s = AppSettings(theme = theme, language = lang, latitude = -33.93, longitude = 151.19, locationName = "Mascot, NSW",
            zoneId = "Australia/Sydney", locationSet = true, setupDone = true,
            artTheme = if (minimal) ArtTheme.MINIMAL else ArtTheme.GEOMETRIC, use24h = minimal)
        val now = ZonedDateTime.of(2026, 10, 18, if (overnight) 21 else 19, 24, 0, 0, s.zone())
        val phase = Phase.Quiet(if (overnight) Prayer.ISHA else Prayer.MAGHRIB, now.plusMinutes(10).toInstant().toEpochMilli())
        var returns = 0
        app.settings.update { s }
        try {
            show(dev, theme, fontScale = scale, rtl = lang == Language.UR, language = lang, qualifiers = q) {
                QuietPrayerContent(phase, s, now) { returns++ }
            }
            val prefix = "build/quiet-designs/${theme.name}/$name-${lang.name}"
            scenario.onActivity { act ->
                val view = act.findViewById<View>(android.R.id.content)
                val b = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(b))
                File("$prefix.png").also { it.parentFile.mkdirs() }.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
                b.recycle()
            }
            File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
            val root = rule.onNodeWithTag("quiet-board", true).fetchSemanticsNode().boundsInRoot
            rule.onNodeWithTag("quiet-art-${theme.name}", true).assertExists()
            val times = quietPrayerTimes(s, phase.prayer, now)
            rule.onNodeWithTag("quiet-current-time", true).assertTextEquals(L10n.iso(L10n.at(times.current, s.use24h)))
            rule.onNodeWithTag("quiet-next-time", true).assertTextEquals(L10n.iso(L10n.at(times.nextTime, s.use24h)))
            for (tag in tags + if (overnight) listOf("quiet-tomorrow") else emptyList()) {
                val target = rule.onNodeWithTag(tag, true)
                if (scale > 1.15f) target.performScrollTo()
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
                if (scale > 1.15f) band.performScrollTo()
                val cfg=app.resources.configuration
                val phonePortrait=cfg.orientation==android.content.res.Configuration.ORIENTATION_PORTRAIT && minOf(cfg.screenWidthDp,cfg.screenHeightDp)<600
                band.assertIsDisplayed().assertContentDescriptionEquals(if(phonePortrait)Kiswah.BAND.substringBefore("   ✦   ")else Kiswah.BAND)
            }
            if (scale <= 1.15f) {
                val current = rule.onNodeWithTag("quiet-current-panel", true).fetchSemanticsNode().boundsInRoot
                val next = rule.onNodeWithTag("quiet-next-panel", true).fetchSemanticsNode().boundsInRoot
                assertTrue("Current/next panels overlap", current.bottom<=next.top || current.right<=next.left || next.right<=current.left)
            }
            val ret = rule.onNodeWithTag("quiet-return", true)
            if (scale > 1.15f) ret.performScrollTo()
            val bounds = ret.fetchSemanticsNode().boundsInRoot
            assertTrue("Return touch target below 48dp", bounds.height >= 48*app.resources.displayMetrics.density)
            ret.performClick(); assertEquals(1, returns)
            rule.onNodeWithTag("quiet-board", true).performClick(); assertEquals(2, returns)
        } finally { scenario.close() }
    }

    @Test fun approvedPhoneAndTabletScreens() {
        for (theme in themes()) for (lang in Language.entries) for (dev in listOf(Dev.PHONE_PORTRAIT, Dev.TABLET_PORTRAIT, Dev.TABLET_LANDSCAPE))
            check(dev, theme, lang, dev.label)
    }
    @Test fun compactScreensKeepAllInformationAndReturnVisible() {
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
