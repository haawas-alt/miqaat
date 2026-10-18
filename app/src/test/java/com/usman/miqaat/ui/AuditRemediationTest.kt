package com.usman.miqaat.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class AuditRemediationTest : ComposeSupport() {
    private fun visible(tag: String) {
        val root = rule.onRoot().fetchSemanticsNode().boundsInRoot
        val node = rule.onNodeWithTag(tag).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        assertTrue("$tag must fit vertically: $node within $root", node.top >= root.top && node.bottom <= root.bottom)
        assertTrue("$tag must fit horizontally", node.left >= root.left && node.right <= root.right)
    }
    private fun capture(name: String) {
        val f = File("build/audit-screens/remediation/$name.png"); f.parentFile.mkdirs()
        // PixelCopy callbacks are unavailable in this Robolectric host. Draw the actual composed view.
        rule.waitForIdle()
        scenario.onActivity { activity ->
            val view = activity.findViewById<android.view.View>(android.R.id.content)
            val bitmap = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(bitmap))
            f.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
            bitmap.recycle()
        }
    }

    @Test fun countdownNumberAndBothActionsRemainVisibleAndWorkInShortLandscape() {
        for (dev in listOf(Dev.PHONE_LANDSCAPE,Dev.TABLET_LANDSCAPE)) for (theme in AppTheme.entries) for (urdu in listOf(false,true)) for (scale in listOf(1f,1.3f,2f)) {
            var start = 0; var dismiss = 0
            val s = AppSettings(theme = theme, language = if (urdu) Language.UR else Language.EN)
            app.settings.update { s }
            val now = System.currentTimeMillis()
            try {
                show(dev, theme, scale, urdu, qualifiers=if(dev==Dev.PHONE_LANDSCAPE) "w740dp-h360dp-land-xhdpi" else null) {
                    AzaanScreen(Phase.IqamahCountdown(Prayer.DHUHR, now, now+180_000), { dismiss++ }, { start++ })
                }
                visible("iqamah-seconds"); visible("iqamah-unit")
                if (dev == Dev.TABLET_LANDSCAPE && scale == 1f && !urdu) {
                    val label = Str[R.string.s_iqamah_countdown].uppercase()
                    val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
                    rule.onNodeWithText(label).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
                    val layout = layouts.single()
                    assertFalse("Phase label must not overflow", layout.hasVisualOverflow)
                    for (line in 0 until layout.lineCount - 1) {
                        val end = layout.getLineEnd(line, visibleEnd = true)
                        assertTrue("Phase label must wrap between complete words: $label", end == 0 || end >= label.length || label[end].isWhitespace() || label[end - 1].isWhitespace())
                    }
                }
                rule.onNodeWithText(Str[R.string.s_start_iqamah_now]).assertIsDisplayed().performClick()
                rule.onNodeWithText(Str[R.string.s_dismiss]).assertIsDisplayed().performClick()
                assertEquals(1,start); assertEquals(1,dismiss)
                capture("countdown-${dev.name}-${theme.name}-$urdu-$scale")
            } finally { scenario.close() }
        }
    }

    @Test fun scaledSettingsLabelKeepsReadingWidthAndThemeSelectionWorks() {
        for (dev in listOf(Dev.PHONE_LANDSCAPE,Dev.TABLET_LANDSCAPE)) for (urdu in listOf(false,true)) for (scale in listOf(1f,1.3f,2f)) {
            var selected = -1
            try {
                show(dev, fontScale=scale, rtl=urdu) {
                    Column(Modifier.fillMaxSize().padding(20.dp)) {
                        SettingRow(Str[R.string.s_preview_a_theme], Str[R.string.s_applies_straight_away_press_back_to], stacked=true) {
                            val choice = remember { mutableStateOf(0) }
                            Chips(listOf("Miqaat", "Kiswah", "Celestial Meridian", "Prayer Gallery"),choice.value) { choice.value=it;selected=it }
                        }
                    }
                }
                val node=rule.onNodeWithText(Str[R.string.s_preview_a_theme]).assertIsDisplayed().fetchSemanticsNode()
                val root=rule.onRoot().fetchSemanticsNode().boundsInRoot
                assertTrue("Label must not be squeezed beside the chips",node.boundsInRoot.height < root.height/2 && node.boundsInRoot.width > root.width/12)
                rule.onNodeWithText("Prayer Gallery").assertIsDisplayed().performClick().assertIsSelected()
                assertEquals(3,selected)
                capture("settings-${dev.name}-$urdu-$scale")
            } finally { scenario.close() }
        }
    }

    @Test fun laterRakahBasmalaCueIsLocalizedAndFatihaAudioIdentityIsPreserved() {
        val actions=Learn.actions(Learn.Lesson.FAJR)
        val i=actions.indexOfFirst { it.rakah==2 && it.step.reciteBasmalaBefore }
        assertTrue(i>=0)
        for (urdu in listOf(false,true)) {
            val s=AppSettings(theme=AppTheme.PRAYER_GALLERY,language=if(urdu) Language.UR else Language.EN)
            app.settings.update { s }
            try {
                show(Dev.PHONE_PORTRAIT, rtl=urdu) { LearnScreen(s,previewLesson=Learn.Lesson.FAJR,previewStep=i) {} }
                rule.onNodeWithTag("lesson-basmala-cue").performScrollTo().assertIsDisplayed()
                rule.onNodeWithTag("lesson-basmala-arabic").performScrollTo().assertIsDisplayed()
                capture("basmala-$urdu")
            } finally { scenario.close() }
        }
        assertEquals(3,Learn.words.indexOfFirst { it.arabic==actions[i].step.arabic })
    }
    @Test fun tabletPortraitAdhkarUsesReadableArabicInBothLanguagesAndScales() {
        for (urdu in listOf(false,true)) for (scale in listOf(1f,2f)) {
            app.settings.update { AppSettings(theme=AppTheme.PRAYER_GALLERY,language=if(urdu) Language.UR else Language.EN) }
            try {
                show(Dev.TABLET_PORTRAIT,fontScale=scale,rtl=urdu) { AdhkarScreen(AdhkarMode.MORNING) {} }
                val arabic=rule.onNodeWithText(Adhkar.morning().first().arabic,useUnmergedTree=true)
                arabic.performScrollTo().assertIsDisplayed()
                val layouts=mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
                arabic.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
                assertTrue(layouts.isNotEmpty())
                assertTrue("Portrait tablet Arabic must use its reading space",layouts.first().layoutInput.style.fontSize.value>=28f)
                assertFalse(layouts.first().didOverflowWidth)
                capture("adhkar-tablet-portrait-$urdu-$scale")
            } finally { scenario.close() }
        }
    }

}
