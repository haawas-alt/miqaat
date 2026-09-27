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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setShowWhenLocked(true); setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars()); systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val prayer = intent.getStringExtra(AzaanScheduler.EXTRA_PRAYER)?.let { runCatching { Prayer.valueOf(it) }.getOrNull() } ?: Prayer.DHUHR
        setContent {
            MiqaatTheme {
                val playing by AzaanService.playing.collectAsState()
                LaunchedEffect(playing) { if (playing == null) finish() }
                AzaanScreen(prayer = playing?.prayer ?: prayer, onStop = { AzaanService.stop(this); finish() })
            }
        }
    }
}
