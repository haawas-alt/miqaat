package com.usman.miqaat.ui

import android.Manifest
import android.app.NotificationManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
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
class Tablet181ReviewTest:ComposeSupport() {
    private data class Case(val name:String,val width:Int=1280,val height:Int=800,val scale:Float=1f,
        val relative:Boolean=false,val use24:Boolean=false,val five:Boolean=false,val iqamah:Boolean=false,val night:Boolean=false)

    @Test fun approvedTabletBoardsKeepNativeFactsGeometryAndActions() {
        val manager=app.getSystemService(NotificationManager::class.java)
        val exactBefore=Reliability.exactAlarmsGranted(app)
        val notificationsBefore=manager.areNotificationsEnabled()
        val grantedBefore=app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED
        try {
            shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
            ShadowAlarmManager.setCanScheduleExactAlarms(true)
            shadowOf(manager).setNotificationsEnabled(true)
            for(theme in listOf(AppTheme.MIQAAT,AppTheme.PRAYER_GALLERY).filter {
                System.getenv("MIQAAT_VISUAL_THEME").let { v->v==null||v==it.name } })
            for(lang in Language.entries)
            for(c in listOf(Case("mockup"),Case("small",960,600),Case("font110",scale=1.1f),
                Case("relative",relative=true),Case("small-relative",960,600,relative=true),
                Case("24h-five",use24=true,five=true),Case("small-iqamah",960,600,iqamah=true),Case("night",night=true))) {
                val s=AppSettings(theme=theme,language=lang,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",
                    zoneId="Australia/Sydney",locationSet=true,setupDone=true,showRelative=c.relative,use24h=c.use24,
                    showSunrise=!c.five,iqamahEnabled=c.iqamah,ramadanMode=if(c.iqamah)RamadanMode.ON else RamadanMode.AUTO,nightDim=false)
                app.settings.update { s }
                val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,25,if(c.night)23 else 11,if(c.night)59 else 17,0,0,s.zone()))
                var settings=0;var learn=0;var timetable=0;var qibla=0;var adhkar=0;var toggles=0;var prepared:Prayer?=null
                val a=HomeActions({timetable++},{settings++},{},{qibla++},{adhkar++},{},{learn++},false,{},{toggles++},{prepared=it})
                try {
                    show(Dev.TABLET_LANDSCAPE,theme,fontScale=c.scale,rtl=lang==Language.UR,language=lang,
                        qualifiers="w${c.width}dp-h${c.height}dp-land-xhdpi") { HomeRouter(state,s,a,false,{}) }
                    rule.waitForIdle()
                    capture("build/tablet-polish/${theme.name}/${c.name}-${lang.name}")
                    val root=rule.onRoot(true).fetchSemanticsNode().boundsInRoot
                    rule.onNodeWithTag("tablet-approved-${theme.name}",true).assertIsDisplayed()
                    for(p in listedPrayers(s)) {
                        val node=rule.onNodeWithTag("approved-row-${p.name}",true).fetchSemanticsNode()
                        val b=node.boundsInRoot
                        assertTrue("$theme/$lang/$c/$p leaves viewport",b.left>=root.left && b.top>=root.top && b.right<=root.right && b.bottom<=root.bottom)
                        assertTrue("Prayer target too short",b.height>=48*app.resources.displayMetrics.density-.1f)
                        checkInk(node,"$theme/$lang/$c/$p")
                        val info=rule.onNodeWithTag("approved-info-${p.name}",true).fetchSemanticsNode().boundsInRoot
                        assertTrue("Info needs 48dp",info.width>=48*app.resources.displayMetrics.density-.1f && info.height>=48*app.resources.displayMetrics.density-.1f)
                        rule.onNodeWithTag("approved-timeline-${p.name}",true).assertIsDisplayed()
                    }
                    checkInk(rule.onNodeWithTag("home-hero",true).fetchSemanticsNode(),"hero/$theme/$lang/$c")
                    checkInk(rule.onNodeWithTag("approved-hijri",true).fetchSemanticsNode(),"date/$theme/$lang/$c")
                    val hero=rule.onNodeWithTag("home-hero",true).fetchSemanticsNode().boundsInRoot
                    val fajr=rule.onNodeWithTag("approved-row-FAJR",true).fetchSemanticsNode().boundsInRoot
                    val timeline=rule.onNodeWithTag("approved-day-timeline",true).fetchSemanticsNode().boundsInRoot
                    if(theme==AppTheme.MIQAAT) assertTrue("Miqaat prayer panel and hero share a horizontal board",
                        if(lang==Language.EN)fajr.right<=hero.left else fajr.left>=hero.right)
                    else {
                        val asr=rule.onNodeWithTag("approved-row-ASR",true).fetchSemanticsNode().boundsInRoot
                        assertTrue("Gallery unified hero must sit above grid",hero.bottom<=fajr.top+.1f)
                        if(!c.five) assertTrue("Gallery second row below first",asr.top>=fajr.bottom)
                        assertTrue("Gallery timeline follows all cards",timeline.top>=asr.bottom-.1f)
                    }
                    rule.onNodeWithContentDescription(Str[R.string.s_settings]).performClick();assertEquals(1,settings)
                    rule.onNodeWithContentDescription(Str[R.string.s_learn_salah]).performClick();assertEquals(1,learn)
                    rule.onNodeWithContentDescription(Str[R.string.s_monthly_timetable]).performClick();assertEquals(1,timetable)
                    rule.onNodeWithTag("approved-row-FAJR",true).performClick();assertEquals(1,toggles)
                    if(theme==AppTheme.PRAYER_GALLERY) {
                        rule.onNodeWithText(if(lang==Language.UR)"نماز کی تیاری" else "Prepare for ${heroInfo(state,s,null).label}",useUnmergedTree=true).performClick()
                        assertEquals(state.hero,prepared)
                    }
                    rule.onNodeWithTag("approved-info-FAJR",true).performClick()
                    rule.onNodeWithText(Str[R.string.s_calculated_on_this_device],substring=true).assertExists()
                } finally { scenario.close() }
            }
        } finally {
            ShadowAlarmManager.setCanScheduleExactAlarms(exactBefore)
            shadowOf(manager).setNotificationsEnabled(notificationsBefore)
            if(!grantedBefore)shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun checkInk(node:androidx.compose.ui.semantics.SemanticsNode,label:String) {
        val layouts=mutableListOf<TextLayoutResult>()
        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        layouts.forEach { l ->
            val width=(0 until l.lineCount).maxOfOrNull { l.getLineRight(it)-l.getLineLeft(it) }?:0f
            val height=if(l.lineCount>0)l.getLineBottom(l.lineCount-1)-l.getLineTop(0) else 0f
            assertFalse("$label clips ${l.layoutInput.text}: ${node.size} vs $width,$height",
                l.multiParagraph.didExceedMaxLines || width>node.size.width+1f || height>node.size.height+1f)
        }
        node.children.forEach { checkInk(it,label) }
    }
    private fun capture(prefix:String) {
        rule.mainClock.advanceTimeBy(1000)
        rule.waitForIdle()
        scenario.onActivity { act ->
            val v=act.findViewById<View>(android.R.id.content)
            val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
            File("$prefix.png").also { it.parentFile.mkdirs() }.outputStream().use { b.compress(Bitmap.CompressFormat.PNG,100,it) };b.recycle()
        }
        File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
    }
}
