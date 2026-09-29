package com.usman.miqaat.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
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
    when {
        showLarge -> LargeHome(state, settings, onLargeTap)
        settings.theme == AppTheme.CELESTIAL_MERIDIAN -> CapFontScale(1.3f) { CelestialHome(state, settings, a) }
        settings.theme == AppTheme.PRAYER_GALLERY -> CapFontScale(1.3f) { GalleryHome(state, settings, a) }
        !portrait && cfg.screenHeightDp < 500 -> CapFontScale(1.3f) {
            LandscapeHome(state, settings, a.onOpenTimetable, a.onOpenSettings, a.onOpenLocation, a.onOpenQibla, a.onOpenAdhkar, a.onOpenFriday, a.onOpenLearn, a.updateAvailable, a.onOpenAbout, a.onToggleRelative)
        }
        portrait -> CapFontScale(if (cfg.screenWidthDp >= 600) 1.3f else 99f) {
            PortraitHome(state, settings, a.onOpenTimetable, a.onOpenSettings, a.onOpenLocation, a.onOpenQibla, a.onOpenAdhkar, a.onOpenFriday, a.onOpenLearn, a.updateAvailable, a.onOpenAbout, a.onToggleRelative)
        }
        else -> CapFontScale(1.3f) { if (settings.theme == AppTheme.KISWAH) CourtyardHome(state, settings, a) else MihrabHome(state, settings, a) }
    }
}
