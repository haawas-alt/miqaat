package com.usman.miqaat.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Actual navigation/scroll assertions, including the UI's intentionally uppercase section headings. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class SettingsBackAcceptanceTest : ComposeSupport() {
    private fun checkBack(dev: Dev) {
        val s = AppSettings(theme = AppTheme.PRAYER_GALLERY, language = Language.EN,
            locationName = "Sydney, Australia", latitude = -33.8688, longitude = 151.2093,
            locationSet = true, setupDone = true, zoneId = "Australia/Sydney")
        app.settings.update { s }
        show(dev) { SettingsScreen(app.settings, s) {} }
        rule.onNodeWithText(Str[R.string.s_display_art]).performScrollTo().performClick()
        rule.onNodeWithText(Str[R.string.s_how_miqaat_looks_on_the_wall]).assertIsDisplayed()
        rule.onNodeWithText(Str[R.string.s_open_miqaat_when_the_device_starts]).performScrollTo()
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithTag("settings-landing-scroll").assert(
            SemanticsMatcher("landing scroll starts at zero") {
                it.config.getOrNull(SemanticsProperties.VerticalScrollAxisRange)
                    ?.value?.invoke()?.let { position -> kotlin.math.abs(position) < 1f } == true
            }
        )
        rule.onNodeWithText(Str[R.string.s_how_miqaat_looks_on_the_wall]).assertDoesNotExist()
        rule.onNodeWithContentDescription(Str[R.string.s_settings_search_hint]).assertIsDisplayed()
        rule.onNodeWithText(Str[R.string.s_group_prayer_setup], ignoreCase = true)
            .performScrollTo().assertIsDisplayed()
    }
    @Test fun portraitBackRestoresLandingAndCategoriesRemainReachable() = checkBack(Dev.PHONE_PORTRAIT)
    @Test fun landscapeBackRestoresLandingAndCategoriesRemainReachable() = checkBack(Dev.PHONE_LANDSCAPE)
    @After fun close() { runCatching { scenario.close() } }
}
