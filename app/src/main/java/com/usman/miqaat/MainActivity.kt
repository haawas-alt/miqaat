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

private enum class Screen { HOME, TIMETABLE, SETTINGS, QIBLA, ADHKAR }

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
            MiqaatTheme {
                val settings by store.settings.collectAsState()
                var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
                var settingsSection by remember { mutableStateOf(Section.TIMES) }
                var adhkarMorning by remember { mutableStateOf(true) }
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
                val state = remember(settings, now.withSecond(0).withNano(0)) { PrayerEngine.state(settings, now) }

                // On every return to the foreground: refresh location (if auto) and make sure an alarm is armed.
                LifecycleResumeEffect(Unit) {
                    AzaanScheduler.reschedule(this@MainActivity)
                    scope.launch { Updater.check(this@MainActivity) }
                    if (settings.autoLocation && settings.setupDone && LocationRepo.hasPermission(this@MainActivity)) scope.launch { detect(this@MainActivity, store) }
                    onPauseOrDispose { }
                }

                BackHandler(enabled = screen != Screen.HOME) { screen = Screen.HOME }

                Box(Modifier.fillMaxSize().background(Palette.night)) {
                    Crossfade(targetState = screen, label = "screen") { s ->
                        when (s) {
                            Screen.HOME -> if (androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) com.usman.miqaat.ui.PortraitHome(
                                state, settings,
                                onOpenTimetable = { screen = Screen.TIMETABLE },
                                onOpenSettings = { settingsSection = Section.TIMES; screen = Screen.SETTINGS },
                                onOpenLocation = { settingsSection = Section.LOCATION; screen = Screen.SETTINGS },
                                onOpenQibla = { screen = Screen.QIBLA },
                                onOpenAdhkar = { m -> adhkarMorning = m; screen = Screen.ADHKAR },
                                updateAvailable = updateState is Updater.State.Available || updateState is Updater.State.Ready,
                                onOpenAbout = { settingsSection = Section.ABOUT; screen = Screen.SETTINGS }
                            ) else if (settings.theme == com.usman.miqaat.data.AppTheme.KISWAH) com.usman.miqaat.ui.KiswahHome(
                                state, settings,
                                onOpenTimetable = { screen = Screen.TIMETABLE },
                                onOpenSettings = { settingsSection = Section.TIMES; screen = Screen.SETTINGS },
                                onOpenLocation = { settingsSection = Section.LOCATION; screen = Screen.SETTINGS },
                                onOpenQibla = { screen = Screen.QIBLA },
                                onOpenAdhkar = { m -> adhkarMorning = m; screen = Screen.ADHKAR },
                                updateAvailable = updateState is Updater.State.Available || updateState is Updater.State.Ready,
                                onOpenAbout = { settingsSection = Section.ABOUT; screen = Screen.SETTINGS }
                            ) else HomeScreen(
                                state, settings,
                                onOpenTimetable = { screen = Screen.TIMETABLE },
                                onOpenSettings = { settingsSection = Section.TIMES; screen = Screen.SETTINGS },
                                onOpenLocation = { settingsSection = Section.LOCATION; screen = Screen.SETTINGS },
                                onOpenQibla = { screen = Screen.QIBLA },
                                onOpenAdhkar = { m -> adhkarMorning = m; screen = Screen.ADHKAR },
                                updateAvailable = updateState is Updater.State.Available || updateState is Updater.State.Ready,
                                onOpenAbout = { settingsSection = Section.ABOUT; screen = Screen.SETTINGS }
                            )
                            Screen.QIBLA -> QiblaScreen(settings) { screen = Screen.HOME }
                            Screen.ADHKAR -> AdhkarScreen(adhkarMorning) { screen = Screen.HOME }
                            Screen.TIMETABLE -> TimetableScreen(settings) { screen = Screen.HOME }
                            Screen.SETTINGS -> SettingsScreen(store, settings, settingsSection) { screen = Screen.HOME }
                        }
                    }
                    phase?.let { ph ->
                        val preview = (ph as? AzaanService.Phase.Azaan)?.preview == true
                        if (!preview) AzaanScreen(phase = ph, onStop = { AzaanService.stop(this@MainActivity) }, onSkip = { AzaanService.skip(this@MainActivity) })
                    }
                    if (!settings.setupDone) FirstRun(onDone = { store.update { it.copy(setupDone = true) } }, onDetect = { scope.launch { detect(this@MainActivity, store) } })
                }
            }
        }
    }
}

/** One-time welcome: asks for location + notification permission, then gets out of the way. */
@Composable
private fun FirstRun(onDone: () -> Unit, onDetect: () -> Unit) {
    var status by remember { mutableStateOf<String?>(null) }
    val notif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val loc = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { g ->
        if (Build.VERSION.SDK_INT >= 33) notif.launch(Manifest.permission.POST_NOTIFICATIONS)
        if (g.values.any { it }) onDetect() else status = "You can pick a city in Settings › Location."
        onDone()
    }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth(0.92f).widthIn(max = 560.dp).clip(RoundedCornerShape(24.dp)).background(Palette.panelRaised).padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("ميقات", fontFamily = Amiri, fontSize = 56.sp, color = Palette.goldSoft)
            Text("As-salāmu ʿalaykum", fontFamily = Cormorant, fontSize = 34.sp, color = Palette.ivory)
            Text(
                "Miqaat needs your location once to calculate prayer times for where the tablet lives. Times are calculated on the device; only the optional place-name lookup contacts Google's geocoder. " +
                    "Times default to the Muslim World League method used by most Australian mosques; you can change that in Settings.",
                fontFamily = Nunito, fontSize = 15.sp, color = Palette.ivory.copy(alpha = 0.8f), lineHeight = 22.sp
            )
            status?.let { Text(it, fontFamily = Nunito, fontSize = 14.sp, color = Palette.goldSoft) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { loc.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Palette.gold, contentColor = Palette.night)
                ) { Text("Use my location", fontFamily = Nunito, fontWeight = FontWeight.Bold) }
                TextButton(onClick = { if (Build.VERSION.SDK_INT >= 33) notif.launch(Manifest.permission.POST_NOTIFICATIONS); onDone() }) { Text("Continue", color = Palette.ivory) }
            }
        }
    }
}
