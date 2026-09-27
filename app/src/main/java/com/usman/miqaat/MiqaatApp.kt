package com.usman.miqaat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.usman.miqaat.azaan.AzaanScheduler
import com.usman.miqaat.data.SettingsStore

class MiqaatApp : Application() {
    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        settings = SettingsStore(this)
        val prefs = getSharedPreferences("miqaat_meta", MODE_PRIVATE)
        if (!prefs.getBoolean("defaultsApplied", false)) {
            if (!com.usman.miqaat.data.Device.isTablet(this)) {
                // Pocket defaults: the wall-tablet extras stay available but start off.
                settings.update { it.copy(afterAzaanEnabled = false, keepScreenOn = false, nightDim = false, quietMinutes = 0) }
            }
            prefs.edit().putBoolean("defaultsApplied", true).apply()
        }
        createChannels()
        com.usman.miqaat.azaan.AlarmVolume.restoreIfStale(this)   // crash-safe: undo a volume change a dead sequence left behind
        AzaanScheduler.reschedule(this)
    }

    private fun createChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_AZAAN, getString(R.string.channel_azaan), NotificationManager.IMPORTANCE_HIGH).apply {
                description = getString(R.string.channel_azaan_desc)
                setSound(null, null)        // the service plays the azaan itself
                enableVibration(false)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_SILENT, getString(R.string.channel_silent), NotificationManager.IMPORTANCE_LOW)
        )
        // Used while Miqaat itself is on screen: no heads-up pop-up, just a quiet entry in the shade.
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_AZAAN_QUIET, getString(R.string.channel_azaan_quiet), NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null); enableVibration(false); setShowBadge(false)
            }
        )
    }

    companion object {
        const val CHANNEL_AZAAN = "azaan"
        const val CHANNEL_SILENT = "silent"
        const val CHANNEL_AZAAN_QUIET = "azaan_quiet"
        lateinit var instance: MiqaatApp
            private set
    }
}
