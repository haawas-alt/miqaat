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
class CelestialTabletAcceptanceTest:ComposeSupport() {
    private data class Case(val name:String,val dev:Dev,val q:String?=null,val scale:Float=1f,
        val relative:Boolean=false,val iqamah:Boolean=false,val five:Boolean=false,val night:Boolean=false,val current:Boolean=false)
    @Test fun bothApprovedBoardsFitLiveFactsAndKeepActions() {
        val manager=app.getSystemService(NotificationManager::class.java)
        val exactBefore=Reliability.exactAlarmsGranted(app)
        val notificationsBefore=manager.areNotificationsEnabled()
        val grantedBefore=app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED
        val failures=mutableListOf<String>()
        try {
            shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
            ShadowAlarmManager.setCanScheduleExactAlarms(true);shadowOf(manager).setNotificationsEnabled(true)
            for(lang in Language.entries)for(c in listOf(
                Case("landscape",Dev.TABLET_LANDSCAPE),Case("portrait",Dev.TABLET_PORTRAIT),
                Case("small-landscape",Dev.TABLET_LANDSCAPE,"w960dp-h600dp-land-xhdpi"),
                Case("small-portrait",Dev.TABLET_PORTRAIT,"w600dp-h960dp-port-xhdpi"),
                Case("current",Dev.TABLET_LANDSCAPE,current=true),Case("font110",Dev.TABLET_LANDSCAPE,scale=1.1f),
                Case("relative",Dev.TABLET_LANDSCAPE,relative=true),
                Case("ramadan-iqamah",Dev.TABLET_LANDSCAPE,"w960dp-h600dp-land-xhdpi",iqamah=true),
                Case("24h-five",Dev.TABLET_PORTRAIT,five=true),Case("night",Dev.TABLET_PORTRAIT,night=true))) {
                val s=AppSettings(theme=AppTheme.CELESTIAL_MERIDIAN,language=lang,latitude=-33.93,longitude=151.19,
                    locationName="Gledswood Hills, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true,
                    showEndTimes=true,showRelative=c.relative,iqamahEnabled=c.iqamah,showSunrise=!c.five,use24h=c.five,
                    ramadanMode=if(c.iqamah)RamadanMode.ON else RamadanMode.AUTO,nightDim=false)
                app.settings.update {s}
                val st=PrayerEngine.state(s,ZonedDateTime.of(2026,10,25,if(c.night)23 else if(c.current)12 else 11,if(c.night)59 else if(c.current)47 else 17,0,0,s.zone()))
                var settings=0;var table=0;var learn=0;var toggles=0;var qibla=0;var adhkar=0
                val a=HomeActions({table++},{settings++},{},{qibla++},{adhkar++},{},{learn++},false,{},{toggles++})
                try {
                    show(c.dev,AppTheme.CELESTIAL_MERIDIAN,fontScale=c.scale,rtl=lang==Language.UR,language=lang,qualifiers=c.q) {HomeRouter(st,s,a,false,{})}
                    rule.waitForIdle();capture("${c.name}-${lang.name}")
                    rule.onNodeWithTag("celestial-tablet-approved",true).assertIsDisplayed()
                    rule.onNodeWithTag("celestial-tablet-status",true).assertTextEquals(heroInfo(st,s,null).status)
                    val root=rule.onRoot(true).fetchSemanticsNode()
                    checkInk(root,"${c.name}/$lang")
                    val hero=rule.onNodeWithTag("home-hero",true).fetchSemanticsNode().boundsInRoot
                    val list=rule.onNodeWithTag("home-prayers",true).fetchSemanticsNode().boundsInRoot
                    if(c.dev==Dev.TABLET_PORTRAIT)assertTrue("Portrait hero must precede rows",hero.bottom<=list.top+.5f)
                    else assertTrue("Landscape hero must sit beside rows",hero.left>=list.right-.5f || hero.right<=list.left+.5f)
                    val clockEdges=listedPrayers(s).map {p ->
                        val b=rule.onNodeWithTag("gallery-fit-clock-${p.name}",true).fetchSemanticsNode().boundsInRoot
                        if(lang==Language.UR)b.right else b.left
                    }
                    assertTrue("Prayer clocks must align regardless of badge",clockEdges.max()-clockEdges.min()<=1f)
                    for(p in listedPrayers(s)) {
                        rule.onNodeWithTag("constellation-node-${p.name}",true).assertIsDisplayed()
                        val b=rule.onNodeWithTag("approved-row-${p.name}",true).fetchSemanticsNode().boundsInRoot
                        assertTrue("$c/$lang/$p off screen",b.top>=root.boundsInRoot.top && b.bottom<=root.boundsInRoot.bottom)
                        assertTrue("Row target below 48dp",b.height>=48*app.resources.displayMetrics.density-.5f)
                        val info=rule.onNodeWithTag("approved-info-${p.name}",true).fetchSemanticsNode().boundsInRoot
                        assertTrue("Info target below 48dp",info.height>=48*app.resources.displayMetrics.density-.5f && info.width>=48*app.resources.displayMetrics.density-.5f)
                        rule.onNodeWithTag("approved-timeline-${p.name}",true).assertIsDisplayed()
                    }
                    rule.onNodeWithContentDescription(Str[R.string.s_settings]).performClick();assertEquals(1,settings)
                    rule.onNodeWithContentDescription(Str[R.string.s_monthly_timetable]).performClick();assertEquals(1,table)
                    rule.onNodeWithContentDescription(Str[R.string.s_learn_salah]).performClick();assertEquals(1,learn)
                    rule.onNodeWithContentDescription(L10n.word(s,"Qibla")).performClick();assertEquals(1,qibla)
                    rule.onNodeWithTag("approved-row-FAJR",true).performClick();assertEquals(1,toggles)
                    rule.onNodeWithTag("approved-info-FAJR",true).performClick()
                    rule.onNodeWithText(Str[R.string.s_calculated_on_this_device],substring=true).assertExists()
                } catch(e:AssertionError) { failures.add("${c.name}/$lang: ${e.message}") } finally {scenario.close()}
            }
        } finally {
            ShadowAlarmManager.setCanScheduleExactAlarms(exactBefore);shadowOf(manager).setNotificationsEnabled(notificationsBefore)
            if(!grantedBefore)shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        }
        assertTrue(failures.joinToString("\n"),failures.isEmpty())
    }
    private fun checkInk(node:androidx.compose.ui.semantics.SemanticsNode,label:String) {
        val layouts=mutableListOf<TextLayoutResult>()
        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        layouts.forEach { l ->
            val width=(0 until l.lineCount).maxOfOrNull {l.getLineRight(it)-l.getLineLeft(it)}?:0f
            val height=if(l.lineCount>0)l.getLineBottom(l.lineCount-1)-l.getLineTop(0) else 0f
            assertFalse("$label clips ${l.layoutInput.text}: ${node.size} vs $width,$height",l.multiParagraph.didExceedMaxLines || width>node.size.width+1f || height>node.size.height+1f)
        }
        node.children.forEach {checkInk(it,label)}
    }
    private fun capture(name:String) {
        val prefix="build/celestial-tablet-review/$name"
        scenario.onActivity {act ->
            val v=act.findViewById<View>(android.R.id.content)
            val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
            File("$prefix.png").also {it.parentFile.mkdirs()}.outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
        }
        File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
    }
}
