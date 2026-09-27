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
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.app.NotificationCompat
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.R
import com.usman.miqaat.data.Duas
import com.usman.miqaat.data.Hadith
import com.usman.miqaat.data.HadithLibrary
import com.usman.miqaat.data.Narration
import com.usman.miqaat.data.Prayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * Runs the whole prayer-time sequence in the foreground so Android never kills it:
 *   AZAAN (recording)  →  DUA (narrated, holds until narration finishes)  →  HADITH (narrated, then rests for N minutes)  →  done
 */
class AzaanService : Service() {

    sealed class Phase(val prayer: Prayer) {
        class Azaan(prayer: Prayer, val preview: Boolean) : Phase(prayer)
        class Dua(prayer: Prayer) : Phase(prayer)
        class HadithPhase(prayer: Prayer, val hadith: Hadith, val startedAt: Long, val endsAt: Long, val narrating: Boolean) : Phase(prayer)
    }

    private var player: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var arabicVoice = false
    private val handler = Handler(Looper.getMainLooper())
    private var pendingAfterTts: (() -> Unit)? = null
    private var sequenceId = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) {
                val t = tts!!
                arabicVoice = t.isLanguageAvailable(Locale("ar")) >= TextToSpeech.LANG_AVAILABLE
                t.setSpeechRate(0.9f)
                t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) { if (id?.endsWith("_last") == true) handler.post { pendingAfterTts?.also { pendingAfterTts = null; it() } } }
                    @Deprecated("Deprecated in Java") override fun onError(id: String?) { onDone(id?.let { "${it}_last" }) }
                    override fun onError(id: String?, code: Int) { onDone("${id}_last") }
                })
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prayer = intent?.getStringExtra(AzaanScheduler.EXTRA_PRAYER)?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
        when (intent?.action) {
            ACTION_STOP -> { finishAll(); return START_NOT_STICKY }
            ACTION_SKIP -> { skip(); return START_NOT_STICKY }
            ACTION_REMINDER -> { if (prayer != null) showReminder(prayer); stopSelf(); return START_NOT_STICKY }
            ACTION_PLAY, ACTION_PREVIEW -> if (prayer != null) startAzaan(prayer, preview = intent.action == ACTION_PREVIEW)
            ACTION_PREVIEW_AFTER -> if (prayer != null) { begin(prayer); startDua(prayer) }
        }
        return START_NOT_STICKY
    }

    // ------------------------------------------------------------ phases

    private fun begin(prayer: Prayer) {
        sequenceId++
        handler.removeCallbacksAndMessages(null)
        pendingAfterTts = null
        val n = buildNotification(prayer)
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(NOTIF_ID, n)
        acquireWakeLock()
        val settings = (application as MiqaatApp).settings.value
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        runCatching { am.setStreamVolume(AudioManager.STREAM_ALARM, (max * settings.azaanVolume / 100f).toInt().coerceAtLeast(1), 0) }
    }

    private fun startAzaan(prayer: Prayer, preview: Boolean) {
        begin(prayer)
        val settings = (application as MiqaatApp).settings.value
        stopPlayer()
        player = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            runCatching { setDataSource(this@AzaanService, soundUri(prayer, settings.azaanUri, settings.fajrAzaanUri)) }
                .onFailure { setDataSource(this@AzaanService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)) }
            isLooping = false
            setOnCompletionListener { afterAzaan(prayer, preview) }
            setOnErrorListener { _, _, _ -> afterAzaan(prayer, preview); true }
            prepare()
            start()
        }
        _phase.value = Phase.Azaan(prayer, preview)
        if (!preview) showScreen(prayer)
    }

    private fun afterAzaan(prayer: Prayer, preview: Boolean) {
        stopPlayer()
        val settings = (application as MiqaatApp).settings.value
        if (preview || !settings.afterAzaanEnabled) finishAll() else startDua(prayer)
    }

    private fun startDua(prayer: Prayer) {
        _phase.value = Phase.Dua(prayer)
        val settings = (application as MiqaatApp).settings.value
        val seq = sequenceId
        // Hold until the narration is done; a guard timer covers a missing/failed TTS engine.
        val spoken = narrate(settings.narration, Duas.AFTER_AZAAN_AR, Duas.AFTER_AZAAN_EN) { if (seq == sequenceId) startHadith(prayer) }
        if (!spoken) handler.postDelayed({ if (seq == sequenceId) startHadith(prayer) }, 40_000)
        else handler.postDelayed({ if (seq == sequenceId && _phase.value is Phase.Dua) startHadith(prayer) }, 120_000)
    }

    private fun startHadith(prayer: Prayer) {
        handler.removeCallbacksAndMessages(null)
        val settings = (application as MiqaatApp).settings.value
        val h = HadithLibrary.next(this)
        val startedAt = System.currentTimeMillis()
        val endsAt = startedAt + settings.hadithMinutes.coerceAtLeast(1) * 60_000L
        val seq = sequenceId
        val spoken = narrate(settings.narration, h.arabic, "The Messenger of Allah, peace be upon him, said: " + h.english + ". Narrated by ${h.narrator}. ${h.source.replace("·", ", ")}") {
            if (seq == sequenceId) _phase.value = Phase.HadithPhase(prayer, h, startedAt, endsAt, narrating = false)
        }
        _phase.value = Phase.HadithPhase(prayer, h, startedAt, endsAt, narrating = spoken)
        handler.postDelayed({ if (seq == sequenceId) finishAll() }, endsAt - System.currentTimeMillis())
    }

    /** Returns true if anything was queued to speak. */
    private fun narrate(mode: Narration, arabic: String, english: String, onDone: () -> Unit): Boolean {
        val t = tts
        if (mode == Narration.OFF || t == null || !ttsReady) return false
        val params = Bundle().apply { putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_ALARM) }
        pendingAfterTts = onDone
        t.stop()
        var queued = false
        if (mode == Narration.BOTH && arabicVoice) {
            t.language = Locale("ar")
            if (t.speak(arabic, TextToSpeech.QUEUE_ADD, params, "ar") == TextToSpeech.SUCCESS) queued = true
            t.playSilentUtterance(900, TextToSpeech.QUEUE_ADD, "gap")
        }
        t.language = Locale.UK.takeIf { t.isLanguageAvailable(Locale.UK) >= TextToSpeech.LANG_AVAILABLE } ?: Locale.ENGLISH
        if (t.speak(english, TextToSpeech.QUEUE_ADD, params, "en_last") == TextToSpeech.SUCCESS) queued = true
        if (!queued) pendingAfterTts = null
        return queued
    }

    private fun skip() {
        when (val p = _phase.value) {
            is Phase.Azaan -> afterAzaan(p.prayer, p.preview)
            is Phase.Dua -> { tts?.stop(); pendingAfterTts = null; startHadith(p.prayer) }
            is Phase.HadithPhase, null -> finishAll()
        }
    }

    // ------------------------------------------------------------ plumbing

    private fun showScreen(prayer: Prayer) {
        runCatching {
            startActivity(Intent(this, AzaanActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
        }
    }

    /** Priority: user-chosen file → bundled res/raw/azaan(_fajr) → system alarm tone. */
    private fun soundUri(prayer: Prayer, custom: String?, customFajr: String?): Uri {
        if (prayer == Prayer.FAJR && customFajr != null) return Uri.parse(customFajr)
        if (custom != null) return Uri.parse(custom)
        val rawName = if (prayer == Prayer.FAJR) "azaan_fajr" else "azaan"
        val id = resources.getIdentifier(rawName, "raw", packageName).takeIf { it != 0 }
            ?: resources.getIdentifier("azaan", "raw", packageName)
        if (id != 0) return Uri.parse("android.resource://$packageName/$id")
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    }

    private fun showReminder(prayer: Prayer) {
        val n = NotificationCompat.Builder(this, MiqaatApp.CHANNEL_SILENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${prayer.english} in a few minutes")
            .setContentText("Prepare for ${prayer.english} prayer")
            .setAutoCancel(true).build()
        getSystemService(android.app.NotificationManager::class.java).notify(NOTIF_ID + 1, n)
    }

    private fun buildNotification(prayer: Prayer): Notification {
        val full = PendingIntent.getActivity(this, 0,
            Intent(this, AzaanActivity::class.java).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, AzaanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "miqaat:azaan").apply { acquire(15 * 60_000L) }
        }
    }

    private fun stopPlayer() {
        player?.runCatching { if (isPlaying) stop(); release() }
        player = null
    }

    private fun finishAll() {
        sequenceId++
        handler.removeCallbacksAndMessages(null)
        pendingAfterTts = null
        tts?.stop()
        stopPlayer()
        _phase.value = null
        wakeLock?.runCatching { if (isHeld) release() }
        wakeLock = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        stopPlayer()
        tts?.runCatching { stop(); shutdown() }
        _phase.value = null
        wakeLock?.runCatching { if (isHeld) release() }
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY = "com.usman.miqaat.PLAY"
        const val ACTION_PREVIEW = "com.usman.miqaat.PREVIEW"
        const val ACTION_PREVIEW_AFTER = "com.usman.miqaat.PREVIEW_AFTER"
        const val ACTION_REMINDER = "com.usman.miqaat.REMINDER"
        const val ACTION_STOP = "com.usman.miqaat.STOP"
        const val ACTION_SKIP = "com.usman.miqaat.SKIP"
        private const val NOTIF_ID = 41

        private val _phase = MutableStateFlow<Phase?>(null)
        val phase: StateFlow<Phase?> = _phase

        fun stop(ctx: Context) = ctx.startService(Intent(ctx, AzaanService::class.java).setAction(ACTION_STOP))
        fun skip(ctx: Context) = ctx.startService(Intent(ctx, AzaanService::class.java).setAction(ACTION_SKIP))
        fun preview(ctx: Context, prayer: Prayer) = androidx.core.content.ContextCompat.startForegroundService(
            ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_PREVIEW).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
        fun previewAfter(ctx: Context, prayer: Prayer) = androidx.core.content.ContextCompat.startForegroundService(
            ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_PREVIEW_AFTER).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
    }
}
