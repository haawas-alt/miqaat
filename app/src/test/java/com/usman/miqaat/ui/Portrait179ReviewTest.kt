package com.usman.miqaat.ui

import android.Manifest
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import org.junit.Assert.*
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
class Portrait179ReviewTest : ComposeSupport() {
    private data class Profile(val width:Int,val height:Int,val fontScale:Float,val insets:Boolean)
    @Test fun realPhoneHeightKeepsHierarchyAndActions() {
        val manager=app.getSystemService(NotificationManager::class.java)
        val exactBefore=Reliability.exactAlarmsGranted(app)
        val notificationsBefore=manager.areNotificationsEnabled()
        val grantedBefore=app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED
        try {
            shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
            ShadowAlarmManager.setCanScheduleExactAlarms(true)
            shadowOf(manager).setNotificationsEnabled(true)
            for(theme in AppTheme.entries.filter { System.getenv("MIQAAT_VISUAL_THEME").let { v -> v==null || v==it.name } })
            for(lang in Language.entries)
            for(profile in listOf(Profile(393,852,1f,false),Profile(393,852,1f,true),
                Profile(393,852,1.1f,false),Profile(360,800,1f,true))) {
                val (w,h,scale,insets)=profile
                val s=AppSettings(theme=theme,language=lang,latitude=-33.93,longitude=151.19,
                    locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true,
                    showRelative=true,nightDim=false)
                app.settings.update { s }
                val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,25,11,17,0,0,s.zone()))
                var prepared:Prayer?=null
                try {
                    show(Dev.PHONE_PORTRAIT,theme,fontScale=scale,rtl=lang==Language.UR,language=lang,
                        qualifiers="w${w}dp-h${h}dp-port-xhdpi") {
                        Box(if(insets) Modifier.padding(top=24.dp,bottom=48.dp) else Modifier) {
                            HomeRouter(state,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{}, {}, { prepared=it }),false,{})
                        }
                    }
                    rule.mainClock.advanceTimeBy(32);rule.waitForIdle()
                    val prefix="build/portrait-polish/${theme.name}/device-${lang.name}-$w-${(scale*100).toInt()}-${if(insets)"insets" else "full"}"
                    scenario.onActivity { act ->
                        val v=act.findViewById<View>(android.R.id.content)
                        val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
                        File("$prefix.png").also { it.parentFile.mkdirs() }.outputStream().use { b.compress(Bitmap.CompressFormat.PNG,100,it) };b.recycle()
                    }
                    File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
                    val d=app.resources.displayMetrics.density
                    if(theme!=AppTheme.PRAYER_GALLERY) {
                        val title=rule.onNodeWithTag("portrait-prayer-lockup",true).fetchSemanticsNode().boundsInRoot
                        val clock=rule.onNodeWithTag("portrait-hero-time",true).fetchSemanticsNode().boundsInRoot
                        assertTrue("Normal portrait must keep time below prayer title: $theme/$profile/$lang $title $clock",
                            clock.top>=title.bottom-16*d && kotlin.math.abs(clock.center.x-title.center.x)<=d)
                    }
                    val details=rule.onNodeWithText(Str[R.string.s_view_prayer_details],useUnmergedTree=true)
                    runCatching { details.performScrollTo() };details.assertIsDisplayed()
                    val target=if(theme==AppTheme.PRAYER_GALLERY) details.onParent().fetchSemanticsNode().boundsInRoot
                        else rule.onNodeWithTag("portrait-hero-details",true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Details needs a full touch target",target.height>=48*d-.1f)
                    if(theme==AppTheme.PRAYER_GALLERY) {
                        run {
                            val layouts=mutableListOf<TextLayoutResult>()
                            rule.onNodeWithTag("gallery-fit-clock-DHUHR",true).fetchSemanticsNode()
                                .config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                            assertTrue("Selected Gallery countdown must stay on one line: $profile",layouts.isNotEmpty() && layouts.all { it.lineCount==1 })
                        }
                        for(p in listedPrayers(s)) for(kind in listOf("label","clock","detail")) {
                            val node=rule.onNodeWithTag("gallery-fit-$kind-${p.name}",true).fetchSemanticsNode()
                            val layouts=mutableListOf<TextLayoutResult>()
                            node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                            layouts.forEach { l ->
                                val inkWidth=(0 until l.lineCount).maxOfOrNull { l.getLineRight(it)-l.getLineLeft(it) } ?: 0f
                                val inkHeight=if(l.lineCount>0) l.getLineBottom(l.lineCount-1)-l.getLineTop(0) else 0f
                                assertTrue("Gallery $lang/$profile/$p/$kind clips ${l.layoutInput.text}: ${node.size} vs $inkWidth,$inkHeight",
                                    !l.multiParagraph.didExceedMaxLines && inkWidth<=node.size.width+1f && inkHeight<=node.size.height+1f)
                            }
                        }
                        val prep=rule.onNodeWithText(if(lang==Language.UR) "نماز کی تیاری" else "Prepare for Dhuhr",useUnmergedTree=true)
                        prep.assertIsDisplayed().performClick();assertEquals(Prayer.DHUHR,prepared)
                        val footer=rule.onNodeWithTag("gallery-phone-footer",true).fetchSemanticsNode().boundsInRoot
                        val root=rule.onRoot().fetchSemanticsNode().boundsInRoot
                        assertTrue("Gallery footer must fit",footer.bottom<=root.bottom)
                    }
                    details.performClick()
                    rule.onNodeWithText(Str[R.string.s_calculated_on_this_device],substring=true).assertExists()
                } finally { scenario.close() }
            }
        } finally {
            ShadowAlarmManager.setCanScheduleExactAlarms(exactBefore)
            shadowOf(manager).setNotificationsEnabled(notificationsBefore)
            if(!grantedBefore) shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
