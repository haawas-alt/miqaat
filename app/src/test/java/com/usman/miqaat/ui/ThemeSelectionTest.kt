package com.usman.miqaat.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.SettingsStore
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ThemeSelectionTest : ComposeSupport() {
    @Test fun pickerShowsFourThemesAndReportsSelection() {
        var picked: AppTheme? = null
        show(Dev.TABLET_LANDSCAPE, theme = AppTheme.KISWAH) { ThemePicker(AppTheme.KISWAH) { picked = it } }
        val cards = rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, androidx.compose.ui.semantics.Role.RadioButton))
        cards.assertCountEquals(4)
        rule.onNode(hasContentDescription("Kiswah", substring = true)).assertIsSelected()
        rule.onNode(hasContentDescription("Celestial Meridian", substring = true)).assertIsNotSelected().performClick()
        assertEquals(AppTheme.CELESTIAL_MERIDIAN, picked)
        rule.onNode(hasContentDescription("Prayer Gallery", substring = true)).performClick()
        assertEquals(AppTheme.PRAYER_GALLERY, picked)
    }

    @Test fun selectionIsPersistedAndRestoredAcrossStoreInstances() {
        val ctx = app
        val store = SettingsStore(ctx)
        for (t in AppTheme.entries) { store.update { it.copy(theme = t) }; assertEquals(t, SettingsStore(ctx).value.theme) }
    }

    @Test fun legacyAndUnknownStoredValuesFailSafely() {
        val ctx = app
        val prefs = ctx.getSharedPreferences("miqaat", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("theme", "KISWAH").commit(); assertEquals(AppTheme.KISWAH, SettingsStore(ctx).value.theme)
        prefs.edit().putString("theme", "MIQAAT").commit(); assertEquals(AppTheme.MIQAAT, SettingsStore(ctx).value.theme)
        prefs.edit().putString("theme", "SOME_FUTURE_THEME").commit(); assertEquals(AppTheme.MIQAAT, SettingsStore(ctx).value.theme)
        prefs.edit().remove("theme").commit(); assertEquals(AppTheme.MIQAAT, SettingsStore(ctx).value.theme)
    }

    @Test fun everyThemeProvidesItsOwnTokensToTheTree() {
        for (t in AppTheme.entries) {
            var seen: AppTheme? = null
            show(Dev.PHONE_PORTRAIT, theme = t) { seen = Tokens.current.theme }
            assertEquals(t, seen); scenario.close()
        }
    }

    @Test fun sharedComponentsKeepTouchTargetsAndSemanticsInEveryTheme() {
        for (t in AppTheme.entries) {
            var clicks = 0
            show(Dev.PHONE_PORTRAIT, theme = t) {
                androidx.compose.foundation.layout.Column {
                    MiqButton("Primary", { clicks++ }); MiqChip("Chip", selected = true, onClick = {}); MiqTextButton("Text", {})
                    StatusNotice(NoticeKind.Warning, "Careful", body = "Body", actionLabel = "Fix", onAction = {})
                }
            }
            rule.onNodeWithText("Primary").assertHeightIsAtLeast(48.dp).performClick(); assertEquals(1, clicks)
            rule.onNodeWithText("Chip").assertIsSelected()
            rule.onNodeWithText("Text").assertHeightIsAtLeast(48.dp)
            rule.onNodeWithText("Careful").assertExists(); rule.onNodeWithText("Fix").assertHeightIsAtLeast(48.dp)
            scenario.close()
        }
    }
}

