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
 * large-type → the two new themes' own homes → short landscape (phone) → portrait → tablet landscape (Mihrab / Courtyard).
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
        !portrait && cfg.screenHeightDp < 500 && settings.theme in listOf(AppTheme.CELESTIAL_MERIDIAN, AppTheme.PRAYER_GALLERY) -> CompactThemeHome(state, settings, a)
        settings.theme == AppTheme.CELESTIAL_MERIDIAN -> CelestialHome(state, settings, a)
        settings.theme == AppTheme.PRAYER_GALLERY -> GalleryHome(state, settings, a)
        !portrait && cfg.screenHeightDp < 500 -> {
            LandscapeHome(state, settings, a.onOpenTimetable, a.onOpenSettings, a.onOpenLocation, a.onOpenQibla, a.onOpenAdhkar, a.onOpenFriday, a.onOpenLearn, a.updateAvailable, a.onOpenAbout, a.onToggleRelative)
        }
        portrait -> {
            PortraitHome(state, settings, a.onOpenTimetable, a.onOpenSettings, a.onOpenLocation, a.onOpenQibla, a.onOpenAdhkar, a.onOpenFriday, a.onOpenLearn, a.updateAvailable, a.onOpenAbout, a.onToggleRelative)
        }
        else -> if (settings.theme == AppTheme.KISWAH) CourtyardHome(state, settings, a) else MihrabHome(state, settings, a)
    }
}
