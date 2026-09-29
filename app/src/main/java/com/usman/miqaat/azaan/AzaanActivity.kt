package com.usman.miqaat.azaan

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.ui.AzaanScreen
import com.usman.miqaat.ui.MiqaatTheme

/** Full-screen "azaan in progress" view. Finishes itself when playback ends. */
class AzaanActivity : ComponentActivity() {
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.usman.miqaat.data.Device.applyOrientation(this)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        else @Suppress("DEPRECATION") window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars()); systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val prayer = intent.getStringExtra(AzaanScheduler.EXTRA_PRAYER)?.let { runCatching { Prayer.valueOf(it) }.getOrNull() } ?: Prayer.DHUHR
        setContent {
            MiqaatTheme(com.usman.miqaat.MiqaatApp.instance.settings.settings.collectAsState().value.theme) {
                val phase by AzaanService.phase.collectAsState()
                LaunchedEffect(phase) { if (phase == null) finish() }
                phase?.let { AzaanScreen(phase = it, onStop = { AzaanService.stop(this); finish() }, onSkip = { AzaanService.skip(this) }) }
            }
        }
    }
}
