package com.usman.miqaat.azaan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.usman.miqaat.data.Prayer

class AzaanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra(AzaanScheduler.EXTRA_PRAYER)?.let { runCatching { Prayer.valueOf(it) }.getOrNull() }
        val reminder = intent.getBooleanExtra(AzaanScheduler.EXTRA_REMINDER, false)
        val iqamah = intent.getBooleanExtra(AzaanScheduler.EXTRA_IQAMAH, false)
        com.usman.miqaat.data.Health.markFired(context)
        if (prayer != null) {
            val planned = context.getSharedPreferences("miqaat_health", Context.MODE_PRIVATE).getLong("plannedAt", 0L)
            val kind = if (iqamah) com.usman.miqaat.data.Health.Kind.IQAMAH else if (reminder) com.usman.miqaat.data.Health.Kind.REMINDER else com.usman.miqaat.data.Health.Kind.AZAAN
            com.usman.miqaat.data.Health.log(context, kind, "${prayer.english} ${if (iqamah) "iqamah" else if (reminder) "reminder" else "azaan"}",
                if (planned > 0 && System.currentTimeMillis() - planned > 90_000) "Fired ${(System.currentTimeMillis() - planned) / 60_000} min late · Android deferred the alarm" else "On time", planned)
            val svc = Intent(context, AzaanService::class.java).apply {
                action = if (iqamah) AzaanService.ACTION_IQAMAH else if (reminder) AzaanService.ACTION_REMINDER else AzaanService.ACTION_PLAY
                putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name)
                putExtra(AzaanScheduler.EXTRA_NOTE, intent.getStringExtra(AzaanScheduler.EXTRA_NOTE))
            }
            // Android 12+ can refuse a foreground-service start from the background (e.g. an alarm that is not treated as exact).
            // That used to crash the receiver; now the user still gets a visible alert and the chain keeps going.
            try { ContextCompat.startForegroundService(context, svc) }
            catch (e: Exception) {
                com.usman.miqaat.data.Health.log(context, com.usman.miqaat.data.Health.Kind.INFO, "${prayer.english} service start refused", "${e.javaClass.simpleName}: showing a notification instead")
                fallbackNotice(context, prayer, iqamah, reminder)
            }
        }
        // Arm the next one straight away so the chain never breaks.
        AzaanScheduler.reschedule(context)
    }

    private fun fallbackNotice(context: Context, prayer: Prayer, iqamah: Boolean, reminder: Boolean) {
        runCatching {
            val st = (context.applicationContext as com.usman.miqaat.MiqaatApp).settings.value
            val ur = com.usman.miqaat.data.L10n.isUrdu(st)
            val name = com.usman.miqaat.data.L10n.prayer(st, prayer)
            val title = when { iqamah -> if (ur) "$name کی اقامت" else "${prayer.english} iqamah"; reminder -> if (ur) "$name میں چند منٹ باقی" else "${prayer.english} in a few minutes"; else -> if (ur) "$name کی اذان کا وقت" else "${prayer.english} azaan time" }
            val open = android.app.PendingIntent.getActivity(context, 7, Intent(context, com.usman.miqaat.azaan.AzaanActivity::class.java).putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name), android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            val n = androidx.core.app.NotificationCompat.Builder(context, if (reminder) com.usman.miqaat.MiqaatApp.CHANNEL_SILENT else com.usman.miqaat.MiqaatApp.CHANNEL_AZAAN)
                .setSmallIcon(com.usman.miqaat.R.drawable.ic_launcher_monochrome).setContentTitle(title).setContentIntent(open).setAutoCancel(true)
                .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM).build()
            context.getSystemService(android.app.NotificationManager::class.java).notify(43, n)
        }
    }
}
