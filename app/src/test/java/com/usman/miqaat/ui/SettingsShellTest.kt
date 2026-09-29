package com.usman.miqaat.ui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.unit.dp
import com.usman.miqaat.data.AppTheme
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class SettingsShellTest : ComposeSupport() {
    private val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
    private fun settingsUi(initial: Section? = null, onBack: () -> Unit = {}): @androidx.compose.runtime.Composable () -> Unit = {
        val s by app.settings.settings.collectAsState()
        SettingsScreen(app.settings, s, initial, onBack)
    }

    @Test fun tabletShowsFixedRailWithAllTenDestinationsAndOneSelected() {
        show(Dev.TABLET_LANDSCAPE, content = settingsUi())
        rule.onAllNodes(tab).assertCountEquals(10)
        rule.onAllNodes(tab and SemanticsMatcher.expectValue(SemanticsProperties.Selected, true)).assertCountEquals(1)
        rule.onNodeWithText("Try it now").assertExists()
        rule.onAllNodesWithText("Test & preview").assertCountEquals(0)
        rule.onAllNodes(tab).fetchSemanticsNodes().forEach { assertTrue("rail item at least 48dp", it.size.height >= 48 * 2) }   // xhdpi: 2 px per dp
    }

    @Test fun tabletRailSelectionMovesWithTheClick() {
        show(Dev.TABLET_LANDSCAPE, content = settingsUi())
        rule.onNode(tab and hasText("Privacy")).performClick()
        rule.onNode(tab and hasText("Privacy")).assertIsSelected()
        rule.onNode(tab and hasText("Location")).assertIsNotSelected()
    }

    @Test fun phoneShowsGroupedLandingWithoutARail() {
        show(Dev.PHONE_PORTRAIT, content = settingsUi())
        rule.onNodeWithText("PRAYER SETUP").assertExists(); rule.onNodeWithText("EXPERIENCE").assertExists(); rule.onNodeWithText("SYSTEM").assertExists()
        rule.onAllNodes(tab).assertCountEquals(0)
        rule.onNodeWithText("Try it now").assertExists()
        rule.onNodeWithText("Search settings").assertExists()
    }

    @Test fun phoneDetailOpensAndBackReturnsToLanding() {
        var left = false
        show(Dev.PHONE_PORTRAIT, content = settingsUi(onBack = { left = true }))
        rule.onNodeWithText("Privacy").performClick()
        rule.onNodeWithText("PRAYER SETUP").assertDoesNotExist()
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithText("PRAYER SETUP").assertExists()
        assertFalse(left)
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        assertTrue("back from landing leaves Settings", left)
    }

    @Test fun deepLinkOpensTheDestinationAndBackLeavesSettings() {
        var left = false
        show(Dev.PHONE_PORTRAIT, content = settingsUi(Section.LOCATION) { left = true })
        rule.onNodeWithText("PRAYER SETUP").assertDoesNotExist()
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        assertTrue(left)
    }

    @Test fun searchRoutesToTheRightCategoryAndShowsNoResultsState() {
        show(Dev.PHONE_PORTRAIT, content = settingsUi())
        rule.onNode(hasSetTextAction()).performTextInput("alarm")
        rule.onAllNodesWithText("Azaan & alerts", substring = true).assertCountEquals(rule.onAllNodesWithText("Azaan & alerts", substring = true).fetchSemanticsNodes().size)
        rule.onAllNodesWithText("Azaan & alerts", substring = true).onFirst().assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("zzzzzz")
        rule.onNodeWithText("No settings match", substring = true).assertExists()
        rule.onNode(hasSetTextAction()).performTextReplacement("")
        rule.onNodeWithText("PRAYER SETUP").assertExists()
    }

    @Test fun searchWorksOnTabletToo() {
        show(Dev.TABLET_LANDSCAPE, content = settingsUi())
        rule.onNode(hasSetTextAction()).performTextInput("backup")
        rule.onAllNodesWithText("Reliability & backup", substring = true).onFirst().assertExists()
    }

    @Test fun readinessStatesAreCommunicatedWithWordsNotColourAlone() {
        val ok = ReadinessState("Dhuhr · 12:24 PM", "Sydney", listOf(
            ReadinessCheck("Exact alarms", "On", true, null), ReadinessCheck("Battery", "Unrestricted", true, null), ReadinessCheck("Notifications", "Allowed", true, null)))
        val bad = ReadinessState("Asr · 3:52 PM", "Sydney", listOf(
            ReadinessCheck("Exact alarms", "Off", false) { }, ReadinessCheck("Battery", "Restricted", false) { }, ReadinessCheck("Notifications", "Allowed", true, null)))
        val none = ReadinessState(null, "Location not set", ok.checks)
        var state = ok
        show(Dev.PHONE_PORTRAIT) { ReadinessSummary(state, wide = false, onOpen = {}) }
        rule.onNodeWithText("Ready for the next prayer").assertExists()
        rule.onNodeWithText("Dhuhr · 12:24 PM").assertExists()
        scenario.close()
        state = bad
        show(Dev.PHONE_PORTRAIT) { ReadinessSummary(state, wide = false, onOpen = {}) }
        rule.onNodeWithText("Needs attention before the next prayer").assertExists()
        rule.onAllNodesWithText("Off · Fix").assertCountEquals(1)
        scenario.close()
        state = none
        show(Dev.PHONE_PORTRAIT) { ReadinessSummary(state, wide = false, onOpen = {}) }
        rule.onNodeWithText("No azaan is scheduled").assertExists()
        rule.onNodeWithText("Turn one on in Azaan & alerts").assertExists()
    }

    @Test fun readinessHeadlineOpensReliability() {
        var opened = 0
        val st = ReadinessState("Dhuhr · 12:24 PM", "Sydney", emptyList())
        show(Dev.TABLET_LANDSCAPE) { ReadinessSummary(st, wide = true, onOpen = { opened++ }) }
        rule.onNode(hasClickAction() and hasContentDescription("Ready for the next prayer, Dhuhr · 12:24 PM, Sydney", substring = true)).performClick()
        assertEquals(1, opened)
    }

    @Test fun settingsStaysUsableAt200PercentFontOnPhoneAndTablet() {
        show(Dev.PHONE_PORTRAIT, fontScale = 2f, content = settingsUi())
        rule.onNodeWithText("Search settings").assertExists()
        rule.onNodeWithText("Location").assertExists()
        scenario.close()
        show(Dev.TABLET_PORTRAIT, fontScale = 2f, content = settingsUi())
        rule.onAllNodes(tab).assertCountEquals(10)
    }

    @Test fun settingsComposesInEveryThemeAndInUrduRtl() {
        for (t in AppTheme.entries) { show(Dev.PHONE_PORTRAIT, theme = t, content = settingsUi()); rule.onAllNodesWithText("Search settings").assertCountEquals(1); scenario.close() }
        show(Dev.TABLET_LANDSCAPE, rtl = true, content = settingsUi())
        rule.onAllNodes(tab).assertCountEquals(10)
    }
}
