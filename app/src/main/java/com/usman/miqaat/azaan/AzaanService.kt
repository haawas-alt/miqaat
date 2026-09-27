package com.usman.miqaat.azaan

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.R
import com.usman.miqaat.data.Prayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Plays the azaan in the foreground so Android never kills it mid-call. */
class AzaanService : Service() {

    private var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayer = intent?.getStringExtra(AzaanScheduler.EXTRA_PRAYER)?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
        when (intent?.action) {
            ACTION_STOP -> { stopPlayback(); return START_NOT_STICKY }
            ACTION_REMINDER -> { if (prayer != null) showReminder(prayer); stopSelf(); return START_NOT_STICKY }
            ACTION_PLAY, ACTION_PREVIEW -> if (prayer != null) play(prayer, preview = intent.action == ACTION_PREVIEW)
        }
        return START_NOT_STICKY
    }

    private fun play(prayer: Prayer, preview: Boolean) {
        val settings = (application as MiqaatApp).settings.value
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIF_ID, buildNotification(prayer), foregroundType()) else startForeground(NOTIF_ID, buildNotification(prayer))
        acquireWakeLock()

        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        runCatching { am.setStreamVolume(AudioManager.STREAM_ALARM, (max * settings.azaanVolume / 100f).toInt().coerceAtLeast(1), 0) }

        val uri = soundUri(prayer, settings.azaanUri, settings.fajrAzaanUri)
        stopPlayer()
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            runCatching { setDataSource(this@AzaanService, uri) }
                .onFailure { setDataSource(this@AzaanService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) }
            isLooping = false
            setOnCompletionListener { stopPlayback() }
            setOnErrorListener { _, _, _ -> stopPlayback(); true }
            prepare()
            start()
        }
        _playing.value = Playing(prayer, preview)

        // Bring up the azaan screen. Works directly while Miqaat is on screen; the full-screen
        // notification intent covers the locked / screen-off case.
        if (!preview) {
            runCatching {
                startActivity(Intent(this, AzaanActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
            }
        }
    }

    /** Priority: user-chosen file → bundled res/raw/azaan(_fajr) → system alarm tone. */
    private fun soundUri(prayer: Prayer, custom: String?, customFajr: String?): Uri {
        if (prayer == Prayer.FAJR && customFajr != null) return Uri.parse(customFajr)
        if (custom != null) return Uri.parse(custom)
        val rawName = if (prayer == Prayer.FAJR) "azaan_fajr" else "azaan"
        val id = resources.getIdentifier(rawName, "raw", packageName)
            .takeIf { it != 0 } ?: resources.getIdentifier("azaan", "raw", packageName)
        if (id != 0) return Uri.parse("android.resource://$packageName/$id")
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }

    private fun showReminder(prayer: Prayer) {
        val n = NotificationCompat.Builder(this, MiqaatApp.CHANNEL_SILENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${prayer.english} in a few minutes")
            .setContentText("Prepare for ${prayer.english} prayer")
            .setAutoCancel(true)
            .build()
        getSystemService(android.app.NotificationManager::class.java).notify(NOTIF_ID + 1, n)
    }

    private fun buildNotification(prayer: Prayer): Notification {
        val full = PendingIntent.getActivity(
            this, 0,
            Intent(this, AzaanActivity::class.java).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, AzaanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, MiqaatApp.CHANNEL_AZAAN)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${prayer.english} azaan  ·  ${prayer.arabic}")
            .setContentText("It is time for ${prayer.english} prayer")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setFullScreenIntent(full, true)
            .setContentIntent(full)
            .addAction(0, "Stop", stop)
            .build()
    }

    private fun foregroundType(): Int =
        if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "miqaat:azaan").apply { acquire(10 * 60_000L) }
        }
    }

    private fun stopPlayer() {
        player?.runCatching { if (isPlaying) stop(); release() }
        player = null
    }

    private fun stopPlayback() {
        stopPlayer()
        _playing.value = null
        wakeLock?.runCatching { if (isHeld) release() }
        wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopPlayer()
        _playing.value = null
        wakeLock?.runCatching { if (isHeld) release() }
        super.onDestroy()
    }

    data class Playing(val prayer: Prayer, val preview: Boolean)

    companion object {
        const val ACTION_PLAY = "com.usman.miqaat.PLAY"
        const val ACTION_PREVIEW = "com.usman.miqaat.PREVIEW"
        const val ACTION_REMINDER = "com.usman.miqaat.REMINDER"
        const val ACTION_STOP = "com.usman.miqaat.STOP"
        private const val NOTIF_ID = 41

        private val _playing = MutableStateFlow<Playing?>(null)
        val playing: StateFlow<Playing?> = _playing

        fun stop(ctx: Context) = ctx.startService(Intent(ctx, AzaanService::class.java).setAction(ACTION_STOP))
        fun preview(ctx: Context, prayer: Prayer) =
            androidx.core.content.ContextCompat.startForegroundService(
                ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_PREVIEW).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name)
            )
    }
}
