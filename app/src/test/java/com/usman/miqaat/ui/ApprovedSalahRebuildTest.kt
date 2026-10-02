package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Bounds, text-layout and reachable-content assertions on actual Compose UI; captures support manual comparison.
 * The photograph is a stylistic approval, not a machine-readable golden. These tests do not assert 99% fidelity.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ApprovedSalahRebuildTest : ComposeSupport() {
    private fun render(name: String) {
        rule.waitForIdle()
        scenario.onActivity { act ->
            val view = act.findViewById<View>(android.R.id.content)
            assertTrue(view.width > 0 && view.height > 0)
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
            File("build/audit-screens/approved-salah").also { it.mkdirs() }.resolve("$name.png").outputStream().use {
                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
            bitmap.recycle()
        }
    }
    private fun noOverflow(tag: String) {
        val node = rule.onNodeWithTag(tag, useUnmergedTree = true)
        val layouts = mutableListOf<TextLayoutResult>()
        node.fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        assertTrue("Missing layout for $tag", layouts.isNotEmpty())
        layouts.forEach { assertFalse("$tag clipped: ${it.layoutInput.text}", it.didOverflowWidth || it.didOverflowHeight) }
    }
    private fun inside(tag: String) {
        val root = rule.onRoot().getUnclippedBoundsInRoot()
        val b = rule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("$tag outside viewport: $b vs $root", b.left >= root.left && b.top >= root.top && b.right <= root.right && b.bottom <= root.bottom)
    }
    @Test fun lessonMatrixKeepsArabicMeaningNotesAndNavigationReachable() {
        for (theme in AppTheme.entries) for (dev in Dev.entries) for (scale in listOf(1f, 1.3f, 2f)) for (urdu in listOf(false, true)) {
            val s = AppSettings(theme = theme, language = if (urdu) Language.UR else Language.EN)
            app.settings.update { s }
            val name = "takbir__${theme.name.lowercase()}__${dev.label}__${(scale * 100).toInt()}${if (urdu) "__ur" else ""}"
            try {
                show(dev, theme, fontScale = scale, rtl = urdu) { LearnScreen(s, previewLesson = Learn.Lesson.FAJR) {} }
                inside("lesson-next")
                noOverflow("lesson-next-label")
                val next = rule.onNodeWithTag("lesson-next") .getUnclippedBoundsInRoot()
                assertTrue("Continue target below 48dp", next.width >= 48.dp && next.height >= 48.dp)
                render(name)
                for (tag in listOf("lesson-arabic", "lesson-transliteration", "lesson-meaning", "lesson-note")) {
                    rule.onNodeWithTag(tag, useUnmergedTree = true).performScrollTo().assertIsDisplayed()
                    noOverflow(tag)
                }
                render(name + "__notes")
            } finally { runCatching { scenario.close() } }
        }
    }
    @Test fun allPosturesAndBothSalamTurnsRenderOnTabletInEveryTheme() {
        val actions = Learn.actions(Learn.Lesson.FAJR)
        for (theme in AppTheme.entries) for (posture in Learn.Posture.entries) {
            val s = AppSettings(theme = theme, language = Language.EN)
            app.settings.update { s }
            try {
                show(Dev.TABLET_LANDSCAPE, theme) {
                    LearnScreen(s, previewLesson = Learn.Lesson.FAJR, previewStep = actions.indexOfFirst { it.posture == posture }) {}
                }
                inside("lesson-posture")
                noOverflow("lesson-arabic")
                rule.onNodeWithText(actions.first { it.posture == posture }.cue).assertIsDisplayed()
                if (posture == Learn.Posture.SALAM) {
                    rule.onNodeWithText(Str[R.string.learn_salam_right]).assertIsDisplayed()
                    rule.onNodeWithText(Str[R.string.learn_salam_left]).assertIsDisplayed()
                }
                render("posture__${posture.name.lowercase()}__${theme.name.lowercase()}__tablet-landscape__100")
            } finally { runCatching { scenario.close() } }
        }
    }
}
