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
        class Iftar(prayer: Prayer) : Phase(prayer)
        class HadithPhase(prayer: Prayer, val hadith: Hadith, val startedAt: Long, val endsAt: Long, val narrating: Boolean) : Phase(prayer)
        class IqamahCountdown(prayer: Prayer, val startedAt: Long, val endsAt: Long) : Phase(prayer)
        class IqamahNow(prayer: Prayer) : Phase(prayer)
        class Quiet(prayer: Prayer, val endsAt: Long) : Phase(prayer)
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
            ACTION_REMINDER -> { if (prayer != null) showReminder(prayer, intent.getStringExtra(AzaanScheduler.EXTRA_NOTE)); stopSelf(); return START_NOT_STICKY }
            ACTION_PLAY, ACTION_PREVIEW -> if (prayer != null) startAzaan(prayer, preview = intent.action == ACTION_PREVIEW)
            ACTION_PREVIEW_AFTER -> if (prayer != null) { begin(prayer); startDua(prayer) }
            ACTION_IQAMAH -> if (prayer != null) startIqamahCountdown(prayer, intent.getIntExtra(EXTRA_SECONDS, -1))
            ACTION_IQAMAH_NOW -> if (prayer != null) { begin(prayer); startIqamahNow(prayer) }
            ACTION_QUIET -> if (prayer != null) { begin(prayer); startQuiet(prayer) }
        }
        return START_NOT_STICKY
    }

    // ------------------------------------------------------------ phases

    private var focusRequest: android.media.AudioFocusRequest? = null

    private fun begin(prayer: Prayer) {
        sequenceId++
        handler.removeCallbacksAndMessages(null)
        pendingAfterTts = null
        val n = buildNotification(prayer)
        if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(NOTIF_ID, n)
        acquireWakeLock()
        requestFocus()
        AlarmVolume.raise(this, (application as MiqaatApp).settings.value.azaanVolume)
    }

    /** Ask other audio (music, podcasts) to duck/pause for the duration of the sequence; released in [finishAll]. */
    private fun requestFocus() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (focusRequest != null) return
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        val req = android.media.AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attrs).setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener { change -> if (change == AudioManager.AUDIOFOCUS_LOSS) { /* another alarm/call took over: stop cleanly rather than fight it */ handler.post { finishAll() } } }
            .build()
        focusRequest = req
        runCatching { am.requestAudioFocus(req) }
    }

    private fun abandonFocus() {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        focusRequest?.let { runCatching { am.abandonAudioFocusRequest(it) } }
        focusRequest = null
    }

    private fun restoreAlarmVolume() = AlarmVolume.restore(this)

    private fun startAzaan(prayer: Prayer, preview: Boolean) {
        begin(prayer)
        val settings = (application as MiqaatApp).settings.value
        stopPlayer()
        player = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            isLooping = false
            setOnCompletionListener { afterAzaan(prayer, preview) }
            setOnErrorListener { _, _, _ -> afterAzaan(prayer, preview); true }
            val ok = runCatching { setDataSource(this@AzaanService, soundUri(prayer, settings.azaanUri, settings.fajrAzaanUri)); prepare(); start() }
                .recoverCatching {
                    reset()
                    val fallback = soundUri(prayer, null, null)   // bundled recording, ignoring a broken custom file
                    setDataSource(this@AzaanService, fallback); prepare(); start()
                }
            if (ok.isFailure) {
                com.usman.miqaat.data.Health.log(this@AzaanService, com.usman.miqaat.data.Health.Kind.MISSED, "${prayer.english} azaan could not play", ok.exceptionOrNull()?.message ?: "audio error")
                handler.post { afterAzaan(prayer, preview) }
            }
        }
        _phase.value = Phase.Azaan(prayer, preview)
        if (!preview) { showScreen(prayer); com.usman.miqaat.data.Health.log(this, com.usman.miqaat.data.Health.Kind.INFO, "${prayer.english} azaan playing", "Recording started") }
    }

    private fun afterAzaan(prayer: Prayer, preview: Boolean) {
        stopPlayer()
        val settings = (application as MiqaatApp).settings.value
        if (preview || !settings.afterAzaanEnabled) finishAll()
        else if (prayer == Prayer.MAGHRIB && com.usman.miqaat.data.PrayerEngine.isRamadan(settings, java.time.LocalDate.now(settings.zone()))) startIftar(prayer)
        else startDua(prayer)
    }

    private fun startIftar(prayer: Prayer) {
        _phase.value = Phase.Iftar(prayer)
        val settings = (application as MiqaatApp).settings.value
        val seq = sequenceId
        val spoken = narrate(settings.narration, com.usman.miqaat.data.Ramadan.IFTAR_AR, com.usman.miqaat.data.Ramadan.IFTAR_EN) { if (seq == sequenceId) startDua(prayer) }
        if (!spoken) handler.postDelayed({ if (seq == sequenceId) startDua(prayer) }, 25_000)
        else handler.postDelayed({ if (seq == sequenceId && _phase.value is Phase.Iftar) startDua(prayer) }, 90_000)
    }

    private fun startDua(prayer: Prayer) {
        handler.removeCallbacksAndMessages(null)
        _phase.value = Phase.Dua(prayer)
        val settings = (application as MiqaatApp).settings.value
        val seq = sequenceId
        // Hold until the narration is done; a guard timer covers a missing/failed TTS engine.
        val spoken = narrate(settings.narration, Duas.AFTER_AZAAN_AR, Duas.AFTER_AZAAN_EN, key = "dua") { if (seq == sequenceId) startHadith(prayer) }
        if (!spoken) handler.postDelayed({ if (seq == sequenceId) startHadith(prayer) }, 40_000)
        else handler.postDelayed({ if (seq == sequenceId && _phase.value is Phase.Dua) startHadith(prayer) }, 120_000)
    }

    private fun startHadith(prayer: Prayer) {
        handler.removeCallbacksAndMessages(null)
        val settings = (application as MiqaatApp).settings.value
        val h = HadithLibrary.next(this)
        com.usman.miqaat.data.Health.log(this, com.usman.miqaat.data.Health.Kind.INFO, "Hadith #${h.id} shown", h.source)
        val startedAt = System.currentTimeMillis()
        val endsAt = startedAt + settings.hadithMinutes.coerceAtLeast(1) * 60_000L
        val seq = sequenceId
        val spoken = narrate(settings.narration, h.arabic, "The Messenger of Allah, peace be upon him, said: " + h.english + ". Narrated by ${h.narrator}. ${h.source.replace("·", ", ")}", key = "h%02d".format(h.id)) {
            if (seq == sequenceId) _phase.value = Phase.HadithPhase(prayer, h, startedAt, endsAt, narrating = false)
        }
        _phase.value = Phase.HadithPhase(prayer, h, startedAt, endsAt, narrating = spoken)
        handler.postDelayed({ if (seq == sequenceId) finishAll() }, endsAt - System.currentTimeMillis())
    }

    /**
     * Narration. Prefers the bundled studio recordings (res/raw/<key>_ar.mp3 / <key>_en.mp3);
     * falls back to the tablet's text-to-speech when a recording is missing.
     * Returns true if anything will play.
     */
    private fun narrate(mode: Narration, arabic: String, english: String, key: String? = null, onDone: () -> Unit): Boolean {
        if (mode == Narration.OFF) return false
        if (key != null) {
            val ids = buildList {
                if (mode == Narration.BOTH) resources.getIdentifier("${key}_ar", "raw", packageName).takeIf { it != 0 }?.let { add(it) }
                resources.getIdentifier("${key}_en", "raw", packageName).takeIf { it != 0 }?.let { add(it) }
            }
            if (ids.isNotEmpty()) { playChain(ids, onDone); return true }
        }
        val t = tts
        if (t == null || !ttsReady) return false
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
            is Phase.Iftar -> { stopNarration(); handler.removeCallbacksAndMessages(null); startDua(p.prayer) }
            is Phase.Dua -> { stopNarration(); handler.removeCallbacksAndMessages(null); startHadith(p.prayer) }
            is Phase.IqamahCountdown -> startIqamahNow(p.prayer)
            is Phase.IqamahNow -> startQuiet(p.prayer)
            is Phase.HadithPhase, is Phase.Quiet, null -> finishAll()
        }
    }

    // ------------------------------------------------------------ iqamah

    /** Takes over whatever is running (hadith included): the countdown always wins. */
    private fun startIqamahCountdown(prayer: Prayer, secondsOverride: Int) {
        val settings = (application as MiqaatApp).settings.value
        stopPlayer(); stopNarration()
        begin(prayer)
        val secs = if (secondsOverride > 0) secondsOverride else settings.iqamahCountdownSeconds
        val start = System.currentTimeMillis()
        val end = start + secs * 1000L
        _phase.value = Phase.IqamahCountdown(prayer, start, end)
        val seq = sequenceId
        // soft tick for the last ten seconds
        for (i in 10 downTo 1) {
            val at = end - i * 1000L
            if (at > start) handler.postAtTime({ if (seq == sequenceId) playShort("tick", 0.5f) }, android.os.SystemClock.uptimeMillis() + (at - System.currentTimeMillis()))
        }
        handler.postDelayed({ if (seq == sequenceId) startIqamahNow(prayer) }, end - System.currentTimeMillis())
        showScreen(prayer)
    }

    private fun startIqamahNow(prayer: Prayer) {
        handler.removeCallbacksAndMessages(null)
        val settings = (application as MiqaatApp).settings.value
        _phase.value = Phase.IqamahNow(prayer)
        val seq = sequenceId
        var holdMs = 12_000L
        when (settings.iqamahSound) {
            com.usman.miqaat.data.IqamahSound.OFF -> {}
            com.usman.miqaat.data.IqamahSound.CHIME -> playShort("chime", 1f)
            com.usman.miqaat.data.IqamahSound.RECORDING -> {
                val id = resources.getIdentifier("iqamah", "raw", packageName)
                if (id != 0) {
                    stopPlayer()
                    player = MediaPlayer().apply {
                        setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                        setDataSource(this@AzaanService, Uri.parse("android.resource://$packageName/$id"))
                        prepare(); start()
                    }
                    holdMs = (player!!.duration + 1500L).coerceAtLeast(5_000L)
                } else playShort("chime", 1f)
            }
        }
        handler.postDelayed({ if (seq == sequenceId) startQuiet(prayer) }, holdMs)
    }

    private fun startQuiet(prayer: Prayer) {
        handler.removeCallbacksAndMessages(null)
        stopPlayer()
        val settings = (application as MiqaatApp).settings.value
        if (settings.quietMinutes <= 0) { finishAll(); return }
        val end = System.currentTimeMillis() + settings.quietMinutes * 60_000L
        _phase.value = Phase.Quiet(prayer, end)
        val seq = sequenceId
        // release the wake lock: the screen may sleep during prayer, the activity keeps its own flag
        handler.postDelayed({ if (seq == sequenceId) finishAll() }, end - System.currentTimeMillis())
    }

    private var narrPlayer: MediaPlayer? = null
    private fun stopNarration() { narrPlayer?.runCatching { if (isPlaying) stop(); release() }; narrPlayer = null; tts?.stop(); pendingAfterTts = null }
    private fun playChain(ids: List<Int>, onDone: () -> Unit) {
        stopNarration()
        val seq = sequenceId
        fun playAt(i: Int) {
            if (seq != sequenceId) return
            if (i >= ids.size) { handler.postDelayed({ if (seq == sequenceId) onDone() }, 600); return }
            narrPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
                runCatching { setDataSource(this@AzaanService, Uri.parse("android.resource://$packageName/${ids[i]}")); prepare() }
                    .onFailure { release(); narrPlayer = null; playAt(i + 1); return }
                setOnCompletionListener { it.release(); if (narrPlayer === it) narrPlayer = null; handler.postDelayed({ playAt(i + 1) }, 900) }
                setOnErrorListener { mp, _, _ -> mp.release(); if (narrPlayer === mp) narrPlayer = null; playAt(i + 1); true }
                start()
            }
        }
        playAt(0)
    }

    private var shortPlayer: MediaPlayer? = null
    private fun playShort(raw: String, volume: Float) {
        val id = resources.getIdentifier(raw, "raw", packageName)
        if (id == 0) return
        shortPlayer?.runCatching { release() }
        shortPlayer = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            setDataSource(this@AzaanService, Uri.parse("android.resource://$packageName/$id"))
            setVolume(volume, volume)
            setOnCompletionListener { it.release(); if (shortPlayer === it) shortPlayer = null }
            prepare(); start()
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
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: Uri.EMPTY
    }

    private fun showReminder(prayer: Prayer, note: String?) {
        val suhoor = note?.startsWith("Suhoor") == true
        val n = NotificationCompat.Builder(this, if (suhoor) MiqaatApp.CHANNEL_AZAAN else MiqaatApp.CHANNEL_SILENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(when { suhoor -> "Suhoor"; note != null -> "Jumuʿah"; else -> "${prayer.english} in a few minutes" })
            .setContentText(note ?: "Prepare for ${prayer.english} prayer")
            .setStyle(NotificationCompat.BigTextStyle().bigText(note ?: "Prepare for ${prayer.english} prayer"))
            .setAutoCancel(true).build()
        getSystemService(android.app.NotificationManager::class.java).notify(NOTIF_ID + 1, n)
        if (suhoor) { begin(prayer); playShort("chime", 0.8f); handler.postDelayed({ finishAll() }, 6000) }
    }

    /** Heads-up + full-screen intent only when the tablet is dark or locked; otherwise a silent entry. */
    private fun needsWakeUp(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val km = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        return !pm.isInteractive || km.isKeyguardLocked
    }

    private fun buildNotification(prayer: Prayer): Notification {
        val wake = needsWakeUp()
        val full = PendingIntent.getActivity(this, 0,
            Intent(this, AzaanActivity::class.java).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1, Intent(this, AzaanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val b = NotificationCompat.Builder(this, if (wake) MiqaatApp.CHANNEL_AZAAN else MiqaatApp.CHANNEL_AZAAN_QUIET)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${prayer.english} azaan  ·  ${prayer.arabic}")
            .setContentText("It is time for ${prayer.english} prayer")
            .setCategory(if (wake) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_SERVICE)
            .setPriority(if (wake) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSilent(!wake)
            .setContentIntent(full)
            .addAction(0, "Stop", stop)
        if (wake) b.setFullScreenIntent(full, true)
        return b.build()
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
        restoreAlarmVolume()
        abandonFocus()
        shortPlayer?.runCatching { release() }; shortPlayer = null
        stopNarration()
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
        restoreAlarmVolume()
        abandonFocus()
        handler.removeCallbacksAndMessages(null)
        stopPlayer(); stopNarration()
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
        const val ACTION_IQAMAH = "com.usman.miqaat.IQAMAH"
        const val ACTION_IQAMAH_NOW = "com.usman.miqaat.IQAMAH_NOW"
        const val ACTION_QUIET = "com.usman.miqaat.QUIET"
        const val EXTRA_SECONDS = "seconds"
        private const val NOTIF_ID = 41

        private val _phase = MutableStateFlow<Phase?>(null)
        val phase: StateFlow<Phase?> = _phase

        fun stop(ctx: Context) = ctx.startService(Intent(ctx, AzaanService::class.java).setAction(ACTION_STOP))
        fun skip(ctx: Context) = ctx.startService(Intent(ctx, AzaanService::class.java).setAction(ACTION_SKIP))
        fun preview(ctx: Context, prayer: Prayer) = androidx.core.content.ContextCompat.startForegroundService(
            ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_PREVIEW).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
        fun testIqamah(ctx: Context, prayer: Prayer, seconds: Int = -1) = androidx.core.content.ContextCompat.startForegroundService(
            ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_IQAMAH).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name).putExtra(EXTRA_SECONDS, seconds))
        fun testIqamahNow(ctx: Context, prayer: Prayer) = androidx.core.content.ContextCompat.startForegroundService(
            ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_IQAMAH_NOW).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
        fun testQuiet(ctx: Context, prayer: Prayer) = androidx.core.content.ContextCompat.startForegroundService(
            ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_QUIET).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
        /** Exactly what happens at prayer time: azaan, then dua, then hadith (and iqamah if scheduled). */
        fun playFull(ctx: Context, prayer: Prayer) = androidx.core.content.ContextCompat.startForegroundService(
            ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_PLAY).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
        fun previewAfter(ctx: Context, prayer: Prayer) = androidx.core.content.ContextCompat.startForegroundService(
            ctx, Intent(ctx, AzaanService::class.java).setAction(ACTION_PREVIEW_AFTER).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name))
    }
}
