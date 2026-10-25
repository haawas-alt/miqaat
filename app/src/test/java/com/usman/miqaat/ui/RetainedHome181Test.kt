package com.usman.miqaat.ui

import android.Manifest
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.test.*
import com.usman.miqaat.data.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowAlarmManager
import java.io.File
import java.time.ZonedDateTime

/** Same isolated capture process before/after: phones, tablet portrait, and deferred tablet themes. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class RetainedHome181Test:ComposeSupport() {
    @Test fun protectedHomes() {
        val manager=app.getSystemService(NotificationManager::class.java)
        val exactBefore=Reliability.exactAlarmsGranted(app)
        val notificationsBefore=manager.areNotificationsEnabled()
        val grantedBefore=app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED
        try {
            shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
            ShadowAlarmManager.setCanScheduleExactAlarms(true)
            shadowOf(manager).setNotificationsEnabled(true)
            for(theme in AppTheme.entries.filter { System.getenv("MIQAAT_VISUAL_THEME").let { v->v==null||v==it.name } })
            for(lang in Language.entries)
            for(dev in listOf(Dev.PHONE_PORTRAIT,Dev.TABLET_PORTRAIT,Dev.TABLET_LANDSCAPE)) {
                if(dev==Dev.TABLET_LANDSCAPE && theme in listOf(AppTheme.MIQAAT,AppTheme.PRAYER_GALLERY))continue
                for(relative in if(dev==Dev.PHONE_PORTRAIT)listOf(false,true)else listOf(false)) {
                    val s=AppSettings(theme=theme,language=lang,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",
                        zoneId="Australia/Sydney",locationSet=true,setupDone=true,showRelative=relative,nightDim=false)
                    app.settings.update { s }
                    val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,25,11,17,0,0,s.zone()))
                    try {
                        show(dev,theme,rtl=lang==Language.UR,language=lang) { HomeRouter(state,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{}) }
                        rule.mainClock.advanceTimeBy(32);rule.waitForIdle()
                        val prefix="build/protected-home/${theme.name}/${dev.label}-${lang.name}-${if(relative)"relative" else "clock"}"
                        scenario.onActivity { act ->
                            val v=act.findViewById<View>(android.R.id.content);val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
                            File("$prefix.png").also { it.parentFile.mkdirs() }.outputStream().use { b.compress(Bitmap.CompressFormat.PNG,100,it) };b.recycle()
                        }
                        File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
                    } finally { scenario.close() }
                }
            }
        } finally {
            ShadowAlarmManager.setCanScheduleExactAlarms(exactBefore)
            shadowOf(manager).setNotificationsEnabled(notificationsBefore)
            if(!grantedBefore)shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
