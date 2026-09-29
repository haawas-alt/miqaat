package com.usman.miqaat

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.usman.miqaat.azaan.AzaanScheduler
import com.usman.miqaat.azaan.AzaanService
import com.usman.miqaat.data.LocationRepo
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.Updater
import com.usman.miqaat.ui.Amiri
import com.usman.miqaat.ui.AzaanScreen
import com.usman.miqaat.ui.Cormorant
import com.usman.miqaat.ui.HomeScreen
import com.usman.miqaat.ui.QiblaScreen
import com.usman.miqaat.ui.AdhkarScreen
import com.usman.miqaat.ui.MiqaatTheme
import com.usman.miqaat.ui.Nunito
import com.usman.miqaat.ui.Palette
import com.usman.miqaat.ui.Section
import com.usman.miqaat.ui.SettingsScreen
import com.usman.miqaat.ui.TimetableScreen
import com.usman.miqaat.ui.detect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

private enum class Screen { HOME, TIMETABLE, SETTINGS, QIBLA, ADHKAR, FRIDAY, LEARN }

class MainActivity : ComponentActivity() {
    private fun hideSystemBars() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) { super.onWindowFocusChanged(hasFocus); if (hasFocus) hideSystemBars() }
    override fun onResume() { super.onResume(); hideSystemBars() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.usman.miqaat.data.Device.applyOrientation(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()
        val store = (application as MiqaatApp).settings

        setContent {
            val themeChoice by store.settings.collectAsState()
            MiqaatTheme(themeChoice.theme) {
                val settings by store.settings.collectAsState()
                // UI strings follow the in-app language; re-key the tree so every screen picks them up.
                remember(settings.language) { com.usman.miqaat.ui.Str.apply(this@MainActivity, settings.language); settings.language }
                androidx.compose.runtime.key(settings.language) {
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                var settingsSection by remember { mutableStateOf(Section.TIMES) }
                var peek by remember { mutableStateOf(false) }
                LaunchedEffect(peek) { if (peek) { delay(25_000); peek = false } }
                var adhkarMode by remember { mutableStateOf(com.usman.miqaat.ui.AdhkarMode.MORNING) }
                val phase by AzaanService.phase.collectAsState()
                val updateState by Updater.state.collectAsState()
                val scope = rememberCoroutineScope()

                // keep-screen-on follows the setting
                LaunchedEffect(settings.keepScreenOn) {
                    if (settings.keepScreenOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }

                // tick once a second so the countdown and sky stay current
                var now by remember { mutableStateOf(ZonedDateTime.now()) }
                LaunchedEffect(settings.zoneId) { while (true) { now = ZonedDateTime.now(settings.zone()); delay(1000L - (System.currentTimeMillis() % 1000)) } }
                // Rebuilt once a minute, except in the last minute before (or first minute after) an azaan, when it ticks every second.
                val coarse = remember(settings, now.withSecond(0).withNano(0)) { PrayerEngine.state(settings, now) }
                val state = if (coarse.delta.seconds < 62 || java.time.Duration.between(now, coarse.nextTime).seconds < 62) remember(settings, now) { PrayerEngine.state(settings, now) } else coarse

                // On every return to the foreground: refresh location (if auto) and make sure an alarm is armed.
                val ready = com.usman.miqaat.data.Setup.ready(settings)
                LifecycleResumeEffect(ready) {
                    if (ready) {
                        AzaanScheduler.reschedule(this@MainActivity)
                        scope.launch { Updater.check(this@MainActivity) }
                        if (settings.autoLocation && LocationRepo.hasPermission(this@MainActivity)) scope.launch { com.usman.miqaat.ui.refreshIfDue(this@MainActivity, store) }
                    }
                    onPauseOrDispose { }
                }

                BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

                Box(Modifier.fillMaxSize().background(Palette.night)) {
                    Crossfade(targetState = screen, label = "screen") { s ->
                        when (s) {
                            Screen.HOME -> if (settings.largeType && !peek) com.usman.miqaat.ui.LargeHome(state, settings) { peek = true }
                            else if (androidx.compose.ui.platform.LocalConfiguration.current.orientation != android.content.res.Configuration.ORIENTATION_PORTRAIT && androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp < 500) com.usman.miqaat.ui.CapFontScale(1.3f) { com.usman.miqaat.ui.LandscapeHome(
                                state, settings,
                                onOpenTimetable = { screen = Screen.TIMETABLE },
                                onOpenSettings = { settingsSection = Section.TIMES; screen = Screen.SETTINGS },
                                onOpenLocation = { settingsSection = Section.LOCATION; screen = Screen.SETTINGS },
                                onOpenQibla = { screen = Screen.QIBLA },
                                onOpenAdhkar = { m -> adhkarMode = m; screen = Screen.ADHKAR },
                                onOpenFriday = { screen = Screen.FRIDAY },
                                onOpenLearn = { screen = Screen.LEARN },
                                updateAvailable = updateState is Updater.State.Available || updateState is Updater.State.Ready,
                                onOpenAbout = { settingsSection = Section.ABOUT; screen = Screen.SETTINGS },
                                onToggleRelative = { store.update { it.copy(showRelative = !it.showRelative) } }
                            ) }
                            else if (androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) com.usman.miqaat.ui.CapFontScale(if (androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600) 1.3f else 99f) { com.usman.miqaat.ui.PortraitHome(
                                state, settings,
                                onOpenTimetable = { screen = Screen.TIMETABLE },
                                onOpenSettings = { settingsSection = Section.TIMES; screen = Screen.SETTINGS },
                                onOpenLocation = { settingsSection = Section.LOCATION; screen = Screen.SETTINGS },
                                onOpenQibla = { screen = Screen.QIBLA },
                                onOpenAdhkar = { m -> adhkarMode = m; screen = Screen.ADHKAR },
                                onOpenFriday = { screen = Screen.FRIDAY },
                                onOpenLearn = { screen = Screen.LEARN },
                                updateAvailable = updateState is Updater.State.Available || updateState is Updater.State.Ready,
                                onOpenAbout = { settingsSection = Section.ABOUT; screen = Screen.SETTINGS },
                                onToggleRelative = { store.update { it.copy(showRelative = !it.showRelative) } }
                            ) } else {
                                val actions = com.usman.miqaat.ui.HomeActions(
                                    onOpenTimetable = { screen = Screen.TIMETABLE },
                                    onOpenSettings = { settingsSection = Section.TIMES; screen = Screen.SETTINGS },
                                    onOpenLocation = { settingsSection = Section.LOCATION; screen = Screen.SETTINGS },
                                    onOpenQibla = { screen = Screen.QIBLA },
                                    onOpenAdhkar = { m -> adhkarMode = m; screen = Screen.ADHKAR },
                                    onOpenFriday = { screen = Screen.FRIDAY },
                                    onOpenLearn = { screen = Screen.LEARN },
                                    updateAvailable = updateState is Updater.State.Available || updateState is Updater.State.Ready,
                                    onOpenAbout = { settingsSection = Section.ABOUT; screen = Screen.SETTINGS },
                                    onToggleRelative = { store.update { it.copy(showRelative = !it.showRelative) } }
                                )
                                com.usman.miqaat.ui.CapFontScale(1.3f) {
                                    if (settings.theme == com.usman.miqaat.data.AppTheme.KISWAH) com.usman.miqaat.ui.CourtyardHome(state, settings, actions)
                                    else com.usman.miqaat.ui.MihrabHome(state, settings, actions)
                                }
                            }
                            Screen.QIBLA -> QiblaScreen(settings) { screen = Screen.HOME }
                            Screen.ADHKAR -> AdhkarScreen(adhkarMode) { screen = Screen.HOME }
                            Screen.FRIDAY -> com.usman.miqaat.ui.FridayScreen(settings) { screen = Screen.HOME }
                            Screen.LEARN -> com.usman.miqaat.ui.LearnScreen(settings) { screen = Screen.HOME }
                            Screen.TIMETABLE -> TimetableScreen(settings) { screen = Screen.HOME }
                            Screen.SETTINGS -> SettingsScreen(store, settings, settingsSection) { screen = Screen.HOME }
                        }
                    }
                    phase?.let { ph ->
                        val preview = (ph as? AzaanService.Phase.Azaan)?.preview == true
                        if (!preview) AzaanScreen(phase = ph, onStop = { AzaanService.stop(this@MainActivity) }, onSkip = { AzaanService.skip(this@MainActivity) })
                    }
                    // Unconfigured state: no prayer times are shown as valid until a place is chosen or detected.
                    if (!ready) com.usman.miqaat.ui.SetupScreen(store, settings, onDone = { })
                }
                }
            }
        }
    }
}
