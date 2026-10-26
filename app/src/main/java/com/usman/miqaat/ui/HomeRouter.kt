package com.usman.miqaat.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.PrayerState

/**
 * The single place that decides which home layout to show. Moved out of MainActivity unchanged in behaviour so that the
 * screenshot/UI tests exercise exactly the same routing the app uses:
 * large-type → dedicated short landscape → the approved responsive phone/tablet compositions.
 */
@Composable
fun HomeRouter(state: PrayerState, settings: AppSettings, a: HomeActions, showLarge: Boolean, onLargeTap: () -> Unit) {
    val cfg = LocalConfiguration.current
    val portrait = cfg.orientation == Configuration.ORIENTATION_PORTRAIT
    // The system font size is honoured in full: beyond ~115% the dense one-screen boards give way to a reflowing, scrolling page
    // in which every label grows with the setting (see AccessibleHome). Nothing is capped or scaled back down.
    val largeFont = LocalDensity.current.fontScale > 1.15f
    when {
        showLarge -> LargeHome(state, settings, onLargeTap)
        largeFont -> AccessibleHome(state, settings, a)
        settings.fridayReminders && state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && (portrait || cfg.screenHeightDp >= 390) -> FridayApprovedHome(state, settings, a)
        !portrait && cfg.screenHeightDp < 500 -> PhoneLandscapeHome(state, settings, a)
        else -> ApprovedHome(state, settings, a)
    }
}
