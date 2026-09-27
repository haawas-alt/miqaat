package com.usman.miqaat.azaan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.usman.miqaat.MainActivity
import com.usman.miqaat.MiqaatApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val h = com.usman.miqaat.data.Health
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> { h.checkMissed(context, "The device was off or restarting"); h.log(context, h.Kind.BOOT, "Device started", "Alarms re-armed") }
            Intent.ACTION_TIME_SET, Intent.ACTION_TIMEZONE_CHANGED -> {
                h.checkMissed(context, "The clock changed")
                val next = AzaanScheduler.nextEvent(context)
                h.log(context, h.Kind.TIME_CHANGE, if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) "Time zone changed" else "Clock changed",
                    "Self-check passed · next: " + (next?.let { "${it.prayer.english} ${if (it.iqamah) "iqamah" else if (it.reminder) "reminder" else "azaan"} at ${it.at.toLocalTime().withSecond(0).withNano(0)} ${it.at.zone.id}" } ?: "nothing scheduled"))
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> h.log(context, h.Kind.INFO, "Miqaat updated", "Version ${com.usman.miqaat.BuildConfig.VERSION_NAME} · alarms re-armed")
        }
        AzaanScheduler.reschedule(context)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val s = (context.applicationContext as MiqaatApp).settings.value
            if (s.launchOnBoot) {
                runCatching {
                    context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        }
    }
}
