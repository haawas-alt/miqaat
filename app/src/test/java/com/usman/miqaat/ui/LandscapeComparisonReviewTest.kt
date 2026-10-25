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

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class LandscapeComparisonReviewTest : ComposeSupport() {
    @Test fun landscapeHomeComparison() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        ShadowAlarmManager.setCanScheduleExactAlarms(true)
        shadowOf(app.getSystemService(NotificationManager::class.java)).setNotificationsEnabled(true)
        for(theme in AppTheme.entries.filter { System.getenv("MIQAAT_VISUAL_THEME").let { v -> v==null || v==it.name } })
        for(dev in listOf(Dev.PHONE_LANDSCAPE,Dev.TABLET_LANDSCAPE)) {
            val s=AppSettings(theme=theme,language=Language.EN,latitude=-33.93,longitude=151.19,
                locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true,
                showRelative=true,nightDim=false)
            app.settings.update { s }
            val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,25,11,17,0,0,s.zone()))
            try {
                show(dev,theme) { HomeRouter(state,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{}) }
                rule.mainClock.advanceTimeBy(32);rule.waitForIdle()
                val prefix="build/landscape-parity/${theme.name}/comparison-${dev.label}"
                scenario.onActivity { act ->
                    val v=act.findViewById<View>(android.R.id.content)
                    val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
                    v.draw(Canvas(b))
                    File("$prefix.png").also { it.parentFile.mkdirs() }.outputStream().use { b.compress(Bitmap.CompressFormat.PNG,100,it) }
                    b.recycle()
                }
                File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
            } finally { scenario.close() }
        }
    }
}
