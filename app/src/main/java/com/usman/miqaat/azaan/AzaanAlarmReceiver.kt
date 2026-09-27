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
        if (prayer != null) {
            val svc = Intent(context, AzaanService::class.java).apply {
                action = if (iqamah) AzaanService.ACTION_IQAMAH else if (reminder) AzaanService.ACTION_REMINDER else AzaanService.ACTION_PLAY
                putExtra(AzaanScheduler.EXTRA_PRAYER, prayer.name)
                putExtra(AzaanScheduler.EXTRA_NOTE, intent.getStringExtra(AzaanScheduler.EXTRA_NOTE))
            }
            ContextCompat.startForegroundService(context, svc)
        }
        // Arm the next one straight away so the chain never breaks.
        AzaanScheduler.reschedule(context)
    }
}
