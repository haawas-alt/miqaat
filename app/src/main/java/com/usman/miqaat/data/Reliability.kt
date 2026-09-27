package com.usman.miqaat.data

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * One place that answers "will the azaan actually fire?" — the same checks feed setup,
 * Settings › Azaan & alerts, the reliability screen and the home-screen warning chip.
 */
object Reliability {
    data class Check(val label: String, val ok: Boolean, val detail: String, val fix: ((Context) -> Unit)?)

    /** Exact alarms: below API 31 always; 31–32 via SCHEDULE_EXACT_ALARM (user-revocable); 33+ via USE_EXACT_ALARM (granted at install for alarm-clock apps). */
    fun exactAlarmsGranted(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun notificationsGranted(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
        return ctx.getSystemService(NotificationManager::class.java).areNotificationsEnabled()
    }

    fun batteryExempt(ctx: Context): Boolean = (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(ctx.packageName)

    fun fullScreenAllowed(ctx: Context): Boolean = Build.VERSION.SDK_INT < 34 || ctx.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()

    fun openExactAlarmSettings(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 31) runCatching { ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${ctx.packageName}"))) }
    }
    fun openBatterySettings(ctx: Context) {
        runCatching { ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}"))) }
    }
    fun openFullScreenSettings(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 34) runCatching { ctx.startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${ctx.packageName}"))) }
    }
    fun openNotificationSettings(ctx: Context) {
        runCatching { ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)) }
    }

    /** Everything that can stop or delay the azaan, worst first. */
    fun checks(ctx: Context): List<Check> = listOf(
        Check("Exact alarms", exactAlarmsGranted(ctx),
            if (exactAlarmsGranted(ctx)) "Granted — the azaan is scheduled to the second" else "Not granted — Android will only allow an approximate alarm, which can be minutes late",
            if (Build.VERSION.SDK_INT >= 31) ::openExactAlarmSettings else null),
        Check("Notifications", notificationsGranted(ctx),
            if (notificationsGranted(ctx)) "Allowed" else "Blocked — the azaan cannot wake the screen or show its Stop button",
            ::openNotificationSettings),
        Check("Battery optimisation", batteryExempt(ctx),
            if (batteryExempt(ctx)) "Miqaat is exempt" else "Not exempt — some phones delay or kill background apps; exempt Miqaat if an azaan is ever late",
            ::openBatterySettings),
        Check("Full-screen azaan", fullScreenAllowed(ctx),
            if (fullScreenAllowed(ctx)) "Allowed" else "Not allowed — the azaan will show as a notification when the screen is locked",
            if (Build.VERSION.SDK_INT >= 34) ::openFullScreenSettings else null)
    )

    fun allGood(ctx: Context) = exactAlarmsGranted(ctx) && notificationsGranted(ctx)
}
