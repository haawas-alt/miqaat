package com.usman.miqaat.ui

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.R
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.Language
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Acceptance P0 regression tests for the Settings shell, run on the emulator (real device sizes):
 * category/scroll isolation, restoration after rotation or process recreation, and no mid-word wrapping in the readiness card.
 */
@RunWith(AndroidJUnit4::class)
class SettingsStateTest {
    @get:Rule val rule = createComposeRule()
    private val app get() = ApplicationProvider.getApplicationContext<Application>() as MiqaatApp
    private val config get() = InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration
    private val widthDp get() = config.screenWidthDp
    private val railLayout get() = widthDp >= 720 && config.screenHeightDp >= 500
    @org.junit.Before fun enforceOrientation() = enforceAuditOrientation()

    private fun prepare(theme: AppTheme = AppTheme.PRAYER_GALLERY) {
        Str.apply(app, Language.EN)
        app.settings.update { it.copy(theme = theme, language = Language.EN, locationName = "Sydney, Australia", latitude = -33.8688, longitude = 151.2093, locationSet = true, setupDone = true, zoneId = "Australia/Sydney") }
    }

    @androidx.compose.runtime.Composable private fun screen(initial: Section? = null) {
        val s by app.settings.settings.collectAsState()
        MiqaatTheme(s.theme) { SettingsScreen(app.settings, s, initial) {} }
    }

    private fun railItem(label: String) = rule.onAllNodesWithText(label)[0]

    @Test fun switchingFromScrolledCategoryShowsOnlyTheNewCategoryAtTop() {
        assumeTrue("rail layout needs a >=720dp wide screen", railLayout)
        prepare()
        rule.setContent { screen() }
        railItem(Str[R.string.s_display_art]).performClick()
        rule.waitForIdle()
        // scroll the Display & art pane to its last row
        rule.onNodeWithText(Str[R.string.s_open_miqaat_when_the_device_starts]).performScrollTo()
        railItem(Str[R.string.s_test_preview]).performClick()
        rule.waitForIdle()
        // Try it now content is on screen, at the top…
        rule.onNodeWithText(Str[R.string.s_run_any_part_of_the_experience]).assertIsDisplayed()
        // …and nothing from Display & art survived, in particular the large theme cards.
        rule.onNodeWithText(Str[R.string.s_open_miqaat_when_the_device_starts]).assertDoesNotExist()
        rule.onAllNodes(hasContentDescription("Bold and classic", substring = true)).assertCountEquals(0)
    }

    @Test fun displayAndArtOpensAtTopWithContentImmediately() {
        assumeTrue(railLayout)
        prepare()
        rule.setContent { screen() }
        railItem(Str[R.string.s_display_art]).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(Str[R.string.s_how_miqaat_looks_on_the_wall]).assertIsDisplayed()
        // Visible, not merely present: a collapsed label once pushed the whole pane below the fold.
        rule.onAllNodes(hasContentDescription("Bold and classic", substring = true)).onFirst().assertIsDisplayed()
        rule.onAllNodes(hasContentDescription("Clean and modern", substring = true)).onFirst().assertIsDisplayed()
    }

    @Test fun selectedCategoryAndItsOwnScrollSurviveRecreation() {
        assumeTrue(railLayout)
        prepare()
        val tester = StateRestorationTester(rule)
        tester.setContent { screen() }
        railItem(Str[R.string.s_display_art]).performClick()
        rule.onNodeWithText(Str[R.string.s_open_miqaat_when_the_device_starts]).performScrollTo()
        tester.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        // same category, same position: the last row is still on screen and Try it now content is not
        rule.onNodeWithText(Str[R.string.s_open_miqaat_when_the_device_starts]).assertIsDisplayed()
        rule.onNodeWithText(Str[R.string.s_everything_exactly_as_at_prayer_time]).assertDoesNotExist()
    }

    @Test fun phoneDetailStartsAtTopAndBackShowsLanding() {
        assumeTrue(!railLayout)
        prepare()
        rule.setContent { screen() }
        rule.onNodeWithText(Str[R.string.s_display_art]).performClick()
        rule.waitForIdle()
        rule.onNodeWithText(Str[R.string.s_how_miqaat_looks_on_the_wall]).assertIsDisplayed()
        rule.onNodeWithText(Str[R.string.s_open_miqaat_when_the_device_starts]).performScrollTo()
        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val activity = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).first() as androidx.activity.ComponentActivity
            activity.onBackPressedDispatcher.onBackPressed()
        }
        rule.waitForIdle()
        rule.onNodeWithText(Str[R.string.s_group_prayer_setup]).assertIsDisplayed()
    }

    /** No word of any readiness label may be split across lines, at 100%, 130% and 200%, in every theme. */
    @Test fun readinessLabelsNeverBreakMidWord() {
        val ok = ReadinessState("Asr · 3:52 PM", "Sydney", listOf(ReadinessCheck("Exact alarms", "Off", false) {}, ReadinessCheck("Battery", "Restricted", false) {}, ReadinessCheck("Notifications", "Allowed", true, null)))
        prepare()
        var theme by androidx.compose.runtime.mutableStateOf(AppTheme.MIQAAT)
        var scale by androidx.compose.runtime.mutableStateOf(1f)
        rule.setContent {
            val d = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density, scale)) {
                MiqaatTheme(theme) { androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding(16.dp)) { ReadinessSummary(ok, wide = railLayout, onOpen = {}) } }
            }
        }
        for (th in AppTheme.entries) for (sc in listOf(1f, 1.3f, 2f)) {
            theme = th; scale = sc
            rule.waitForIdle()
            val broken = mutableListOf<String>()
            rule.onAllNodes(SemanticsMatcher("has text layout") { it.config.getOrNull(SemanticsActions.GetTextLayoutResult) != null }).fetchSemanticsNodes().forEach { n ->
                val out = mutableListOf<TextLayoutResult>()
                n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(out)
                val r = out.firstOrNull() ?: return@forEach
                val text = r.layoutInput.text.text
                for (i in 0 until r.lineCount - 1) {
                    val end = r.getLineEnd(i, visibleEnd = false)
                    if (end in 1 until text.length && text[end - 1].isLetterOrDigit() && text[end].isLetterOrDigit()) broken += "'$text' breaks inside a word at $end (${th.name} ${(sc * 100).toInt()}%)"
                }
            }
            assertTrue(broken.joinToString("; "), broken.isEmpty())
        }
    }
}
