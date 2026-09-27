package com.usman.miqaat.azaan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.usman.miqaat.MainActivity
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.data.Health

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> { Health.checkMissed(context, "The device was off or restarting"); Health.log(context, Health.Kind.BOOT, "Device started", "Alarms re-armed") }
            "android.intent.action.TIME_SET", Intent.ACTION_TIMEZONE_CHANGED -> {
                Health.checkMissed(context, "The clock changed")
                val next = AzaanScheduler.nextEvent(context)
                Health.log(context, Health.Kind.TIME_CHANGE, if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) "Time zone changed" else "Clock changed",
                    "Self-check passed · next: " + (next?.let { "${it.prayer.english} ${if (it.iqamah) "iqamah" else if (it.reminder) "reminder" else "azaan"} at ${it.at.toLocalTime().withSecond(0).withNano(0)} ${it.at.zone.id}" } ?: "nothing scheduled"))
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> Health.log(context, Health.Kind.INFO, "Miqaat updated", "Version ${com.usman.miqaat.BuildConfig.VERSION_NAME} · alarms re-armed")
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
