package com.usman.miqaat.azaan

import android.content.Context
import android.media.AudioManager

/**
 * The azaan plays on the alarm stream at the user's chosen loudness. The previous alarm volume is
 * written to disk BEFORE it is changed, so it is restored even if the process dies mid-sequence
 * (checked on every app start and every alarm receipt via [restoreIfStale]).
 */
object AlarmVolume {
    private const val PREFS = "miqaat_audio"
    private const val KEY = "savedAlarmVolume"
    private const val KEY_AT = "savedAt"

    fun raise(ctx: Context, percent: Int) {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY)) prefs.edit().putInt(KEY, am.getStreamVolume(AudioManager.STREAM_ALARM)).putLong(KEY_AT, System.currentTimeMillis()).commit()
        val max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        runCatching { am.setStreamVolume(AudioManager.STREAM_ALARM, (max * percent / 100f).toInt().coerceAtLeast(1), 0) }
    }

    fun restore(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY)) return
        val v = prefs.getInt(KEY, -1)
        prefs.edit().remove(KEY).remove(KEY_AT).commit()
        if (v >= 0) runCatching { (ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager).setStreamVolume(AudioManager.STREAM_ALARM, v, 0) }
    }

    /** A saved value older than 30 minutes can only mean the sequence died: put the volume back. */
    fun restoreIfStale(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val at = prefs.getLong(KEY_AT, 0L)
        if (prefs.contains(KEY) && System.currentTimeMillis() - at > 30 * 60_000L) restore(ctx)
    }
}
