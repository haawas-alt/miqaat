package com.usman.miqaat

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine

/** Home-screen widget: next prayer, time and countdown, plus the following two. Refreshes every 5 minutes and on every reschedule. */
class MiqaatWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) { render(context, mgr, ids); armRefresh(context) }
    override fun onEnabled(context: Context) { armRefresh(context) }
    override fun onDisabled(context: Context) { (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(refreshIntent(context)) }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) refresh(context)
    }

    companion object {
        const val ACTION_REFRESH = "com.usman.miqaat.WIDGET_REFRESH"

        fun refresh(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, MiqaatWidget::class.java))
            if (ids.isNotEmpty()) render(ctx, mgr, ids)
        }

        private fun refreshIntent(ctx: Context) = PendingIntent.getBroadcast(ctx, 77, Intent(ctx, MiqaatWidget::class.java).setAction(ACTION_REFRESH), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        private fun armRefresh(ctx: Context) {
            val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 60_000, 5 * 60_000L, refreshIntent(ctx))
        }

        private fun render(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
            val s = (ctx.applicationContext as MiqaatApp).settings.value
            val st = PrayerEngine.state(s)
            val open = PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val following = Prayer.prayersOnly.filter { it != st.hero && st.today[it].isAfter(st.now) }.take(2)
            for (id in ids) {
                val v = RemoteViews(ctx.packageName, R.layout.widget_next)
                if (!com.usman.miqaat.data.Setup.ready(s)) {
                    // Unconfigured: never show placeholder times on the home screen.
                    v.setTextViewText(R.id.w_name, "Miqaat")
                    v.setTextViewText(R.id.w_time, "Set up")
                    v.setTextViewText(R.id.w_state, "Tap to choose your place")
                    v.setTextViewText(R.id.w_next, "")
                    v.setContentDescription(R.id.w_root, "Miqaat is not set up yet. Tap to choose your place.")
                    v.setOnClickPendingIntent(R.id.w_root, open)
                    mgr.updateAppWidget(id, v)
                    continue
                }
                v.setTextViewText(R.id.w_name, "${L10n.prayer(s, st.hero)}  ·  ${st.hero.arabic}")
                v.setTextViewText(R.id.w_time, PrayerEngine.clock(st.heroTime, s.use24h) + " " + PrayerEngine.suffix(st.heroTime, s.use24h))
                v.setTextViewText(R.id.w_state, if (st.justPassed) L10n.ago(s, st.delta) else L10n.inFor(s, st.delta))
                val iq = st.current?.let { PrayerEngine.iqamah(s, st.today, it) }?.takeIf { it.isAfter(st.now) }
                val tail = following.joinToString("   ") { "${L10n.prayer(s, it)} ${PrayerEngine.clock(st.today[it], s.use24h)}" } + (iq?.let { "   ${L10n.word(s, "Iqamah")} ${PrayerEngine.clock(it, s.use24h)}" } ?: "")
                v.setTextViewText(R.id.w_next, tail)
                v.setContentDescription(R.id.w_root, "${if (st.justPassed) "" else "Next prayer "}${L10n.prayer(s, st.hero)} at ${PrayerEngine.clock(st.heroTime, s.use24h)} ${PrayerEngine.suffix(st.heroTime, s.use24h)}, ${if (st.justPassed) L10n.ago(s, st.delta) else L10n.inFor(s, st.delta)}. $tail. Opens Miqaat.")
                v.setOnClickPendingIntent(R.id.w_root, open)
                mgr.updateAppWidget(id, v)
            }
        }
    }
}
