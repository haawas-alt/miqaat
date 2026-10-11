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
        // Opens Android's own battery-optimisation list (no special permission needed; Google Play restricts the direct-exemption prompt).
        runCatching { ctx.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            .onFailure { runCatching { ctx.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
    }
    fun openFullScreenSettings(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 34) runCatching { ctx.startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${ctx.packageName}"))) }
    }
    fun openNotificationSettings(ctx: Context) {
        runCatching { ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)) }
    }

    /** Everything that can stop or delay the azaan, worst first. */
    fun checks(ctx: Context): List<Check> {
        val ur = L10n.uiUrdu
        fun t(en: String, urdu: String) = if (ur) urdu else en
        return listOf(
            Check(t("Exact alarms", "درست الارم"), exactAlarmsGranted(ctx),
                if (exactAlarmsGranted(ctx)) t("Granted — the azaan is scheduled to the second", "اجازت ہے — اذان کا وقت سیکنڈ کی درستگی کے ساتھ مقرر ہے")
                else t("Not granted — Android will only allow an approximate alarm, which can be minutes late", "اجازت نہیں — اینڈرائیڈ صرف تخمینی الارم کی اجازت دے گا، جو چند منٹ دیر سے بج سکتا ہے"),
                if (Build.VERSION.SDK_INT >= 31) ::openExactAlarmSettings else null),
            Check(t("Notifications", "اطلاعات"), notificationsGranted(ctx),
                if (notificationsGranted(ctx)) t("Allowed", "اجازت ہے") else t("Blocked — the azaan cannot wake the screen or show its Stop button", "بلاک — اذان اسکرین کو نہیں جگا سکتی اور نہ روکنے کا بٹن دکھا سکتی ہے"),
                ::openNotificationSettings),
            Check(t("Battery optimisation", "بیٹری کی بچت"), batteryExempt(ctx),
                if (batteryExempt(ctx)) t("Miqaat is exempt", "میقات مستثنیٰ ہے") else t("Not exempt — some phones delay or kill background apps; exempt Miqaat if an azaan is ever late", "مستثنیٰ نہیں — کچھ فون پس منظر کی ایپس کو روک یا بند کر دیتے ہیں؛ اگر کبھی اذان دیر سے ہو تو میقات کو مستثنیٰ کریں"),
                ::openBatterySettings),
            Check(t("Full-screen azaan", "فل اسکرین اذان"), fullScreenAllowed(ctx),
                if (fullScreenAllowed(ctx)) t("Allowed", "اجازت ہے") else t("Not allowed — the azaan will show as a notification when the screen is locked", "اجازت نہیں — اسکرین لاک ہونے پر اذان صرف اطلاع کے طور پر دکھائی دے گی"),
                if (Build.VERSION.SDK_INT >= 34) ::openFullScreenSettings else null)
        )
    }

    fun allGood(ctx: Context) = exactAlarmsGranted(ctx) && notificationsGranted(ctx)
}
