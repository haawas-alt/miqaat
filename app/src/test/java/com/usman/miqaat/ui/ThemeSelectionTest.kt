package com.usman.miqaat.ui

import androidx.compose.foundation.verticalScroll
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

    @Test fun phonePickerUsesPortraitPreviewsAndKeepsCardsSelectable() {
        var picked: AppTheme? = null
        show(Dev.PHONE_PORTRAIT, theme=AppTheme.MIQAAT, qualifiers="w393dp-h852dp-port-xhdpi") {
            androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier
                .then(androidx.compose.ui.Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()))) {
                ThemePicker(AppTheme.MIQAAT) { picked=it }
            }
        }
        for (theme in AppTheme.entries) {
            val preview=rule.onNodeWithTag("theme-preview-${theme.name}",useUnmergedTree=true)
            preview.performScrollTo().assertIsDisplayed()
            val bounds=preview.fetchSemanticsNode().boundsInRoot
            assertTrue("Phone theme preview must show portrait",bounds.height>bounds.width*2f)
            rule.onNode(hasContentDescription(theme.text,substring=true)).performClick();assertEquals(theme,picked)
            scenario.onActivity { act ->
                val v=act.findViewById<android.view.View>(android.R.id.content)
                val b=android.graphics.Bitmap.createBitmap(v.width,v.height,android.graphics.Bitmap.Config.ARGB_8888)
                v.draw(android.graphics.Canvas(b))
                java.io.File("build/portrait-polish/${theme.name}/theme-picker-portrait.png").also { it.parentFile.mkdirs() }
                    .outputStream().use { b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) };b.recycle()
            }
        }
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

