package com.usman.miqaat.azaan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import java.time.ZonedDateTime

/**
 * Keeps exactly one exact alarm armed: the next azaan (or pre-azaan reminder) that is enabled.
 * Each firing reschedules the one after it, so the chain survives Doze, reboots and DST changes.
 */
object AzaanScheduler {
    private const val TAG = "AzaanScheduler"
    const val EXTRA_PRAYER = "prayer"
    const val EXTRA_REMINDER = "reminder"
    const val EXTRA_IQAMAH = "iqamah"
    private const val REQ_AZAAN = 1001

    data class Upcoming(val prayer: Prayer, val at: ZonedDateTime, val reminder: Boolean, val iqamah: Boolean = false)

    fun nextEvent(ctx: Context, from: ZonedDateTime = ZonedDateTime.now()): Upcoming? {
        val s = (ctx.applicationContext as MiqaatApp).settings.value
        val candidates = mutableListOf<Upcoming>()
        for (dayOffset in 0L..1L) {
            val day = PrayerEngine.times(s, from.toLocalDate().plusDays(dayOffset), from.zone)
            for (p in Prayer.prayersOnly) {
                val t = day[p]
                if (s.azaanEnabled[p] != true) {
                    PrayerEngine.iqamah(s, day, p)?.let { iq ->
                        val start = iq.minusSeconds(s.iqamahCountdownSeconds.toLong())
                        if (start.isAfter(from)) candidates += Upcoming(p, start, reminder = false, iqamah = true)
                    }
                    continue
                }
                if (s.preReminderMinutes > 0) {
                    val r = t.minusMinutes(s.preReminderMinutes.toLong())
                    if (r.isAfter(from)) candidates += Upcoming(p, r, reminder = true)
                }
                if (t.isAfter(from)) candidates += Upcoming(p, t, reminder = false)
                PrayerEngine.iqamah(s, day, p)?.let { iq ->
                    val start = iq.minusSeconds(s.iqamahCountdownSeconds.toLong())
                    if (start.isAfter(from)) candidates += Upcoming(p, start, reminder = false, iqamah = true)
                }
            }
        }
        return candidates.minByOrNull { it.at }
    }

    fun reschedule(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(ctx, null)
        am.cancel(pi)
        val next = nextEvent(ctx) ?: run { Log.i(TAG, "No azaan enabled; nothing scheduled"); return }
        val fire = pendingIntent(ctx, next)
        val whenMs = next.at.toInstant().toEpochMilli()
        val canExact = Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()
        if (canExact) {
            // Alarm-clock semantics: highest priority, shows in the status bar, fires even in Doze.
            am.setAlarmClock(AlarmManager.AlarmClockInfo(whenMs, fire), fire)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMs, fire)
        }
        Log.i(TAG, "Scheduled ${next.prayer} ${if (next.iqamah) "iqamah" else if (next.reminder) "reminder" else "azaan"} at ${next.at}")
    }

    private fun pendingIntent(ctx: Context, u: Upcoming?): PendingIntent {
        val i = Intent(ctx, AzaanAlarmReceiver::class.java).apply {
            action = "com.usman.miqaat.AZAAN"
            if (u != null) {
                putExtra(EXTRA_PRAYER, u.prayer.name)
                putExtra(EXTRA_REMINDER, u.reminder)
                putExtra(EXTRA_IQAMAH, u.iqamah)
            }
        }
        return PendingIntent.getBroadcast(ctx, REQ_AZAAN, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
