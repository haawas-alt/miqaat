package com.usman.miqaat.azaan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.usman.miqaat.MainActivity
import com.usman.miqaat.MiqaatApp

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
