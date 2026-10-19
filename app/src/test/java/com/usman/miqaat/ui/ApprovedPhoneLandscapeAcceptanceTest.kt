package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class ApprovedPhoneLandscapeAcceptanceTest:ComposeSupport() {
    private fun themes()=AppTheme.entries.filter{System.getenv("MIQAAT_VISUAL_THEME").let{only->only==null||it.name==only}}
    private fun settings(t:AppTheme,l:Language)=AppSettings(theme=t,language=l,latitude=-33.93,longitude=151.19,
        locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true,showEndTimes=true)
    private fun noScroll() {
        val scroll=SemanticsMatcher("scroll"){it.config.getOrNull(SemanticsActions.ScrollBy)!=null}
        assertEquals("Default phone landscape must fit without scrolling",0,rule.onAllNodes(scroll,true).fetchSemanticsNodes().size)
    }
    private fun readable(tag:String,reflow:Boolean=false) {
        val q=rule.onNodeWithTag(tag,true)
        if(reflow)q.performScrollTo()
        q.assertIsDisplayed()
        val n=q.fetchSemanticsNode();val b=n.boundsInRoot;val root=rule.onRoot().fetchSemanticsNode().boundsInRoot
        assertTrue("$tag leaves viewport: $b vs $root",b.left>=root.left&&b.right<=root.right&&b.top>=root.top&&b.bottom<=root.bottom)
        val layouts=mutableListOf<TextLayoutResult>();n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        assertTrue("$tag missing native text layout",layouts.isNotEmpty())
        for(l in layouts) {
            val w=(0 until l.lineCount).maxOfOrNull{l.getLineRight(it)-l.getLineLeft(it)}?:0f
            val h=if(l.lineCount>0)l.getLineBottom(l.lineCount-1)-l.getLineTop(0) else 0f
            assertFalse("$tag clipped '${l.layoutInput.text}': ${n.size} vs $w,$h",l.multiParagraph.didExceedMaxLines||w>n.size.width+1f||h>n.size.height+1f)
        }
    }
    private fun target(tag:String,reflow:Boolean=false) {
        val q=rule.onNodeWithTag(tag,true);if(reflow)q.performScrollTo();q.assertIsDisplayed()
        val b=q.fetchSemanticsNode().boundsInRoot;val density=app.resources.displayMetrics.density
        assertTrue("$tag below 48dp: $b",b.width>=48*density-.1f&&b.height>=48*density-.1f)
    }
    private fun capture(t:AppTheme,name:String,l:Language) {
        val prefix="build/phone-landscape/${t.name}/$name-${l.name}";rule.waitForIdle()
        scenario.onActivity{act->val view=act.findViewById<View>(android.R.id.content);val b=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            view.draw(Canvas(b));File("$prefix.png").also{it.parentFile.mkdirs()}.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
        File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
    }
    @Test fun homeFitsAllPrayersHighlightsAndActionsOnNormalAndInsetPhones() {
        for(t in themes())for(l in Language.entries)for(small in listOf(false,true))home(t,l,small,"default")
    }
    @Test fun retainedRelativeIqamahAndFivePrayerPreferencesStayReachable() {
        for(t in themes())for(l in Language.entries)for(variant in listOf("relative","iqamah","24h-five"))home(t,l,true,variant)
    }
    private fun home(t:AppTheme,l:Language,small:Boolean,variant:String) {
        val base=settings(t,l);val s=when(variant){"relative"->base.copy(showRelative=true);"iqamah"->base.copy(iqamahEnabled=true);"24h-five"->base.copy(use24h=true,showSunrise=false,showHijri=false,showEndTimes=false);else->base}
        app.settings.update{s};val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,s.zone()))
        var toggles=0;var learn=0;var timetable=0;var settings=0;var location=0;var adhkar=0;var qibla=0
        val a=HomeActions({timetable++},{settings++},{location++},{qibla++},{adhkar++},{},{learn++},false,{},{toggles++})
        try {
            show(Dev.PHONE_LANDSCAPE,t,rtl=l==Language.UR,language=l,qualifiers=if(small)"w740dp-h360dp-land-xhdpi"else null) {
                Box(Modifier.padding(top=12.dp,bottom=20.dp)){HomeRouter(state,s,a,false,{})}
            }
            val reflow=s.iqamahEnabled||s.showRelative
            capture(t,"home-${if(small)"small"else"normal"}-$variant",l)
            if(!reflow)noScroll()
            for(tag in listOf("landscape-location","landscape-date","landscape-live-clock","landscape-hero-name","landscape-hero-clock","landscape-hero-status"))readable(tag,reflow)
            rule.onNodeWithTag("landscape-hero-clock",true).assertTextEquals(L10n.iso(L10n.at(state.heroTime,s.use24h)))
            if(s.showHijri)readable("landscape-hijri",reflow)
            for(p in listedPrayers(s)) {
                target("compact-prayer-${p.name}",reflow);target("landscape-info-${p.name}",reflow)
                if(l==Language.EN)readable("landscape-row-arabic-${p.name}",reflow)
                for(kind in listOf("name","time"))readable("landscape-row-$kind-${p.name}",reflow)
                if(galleryRowInfo(p,state,s).small.isNotBlank())readable("landscape-row-detail-${p.name}",reflow)
                readable("approved-timeline-${p.name}",reflow)
                val r=galleryRowInfo(p,state,s)
                rule.onNodeWithTag("landscape-row-time-${p.name}",true).assertTextEquals(if(s.showRelative)r.relative else L10n.iso((r.clock+" "+r.suffix).trim()))
            }
            if(!s.showSunrise)rule.onNodeWithTag("compact-prayer-SUNRISE",true).assertDoesNotExist()
            for((p,r)in listedPrayers(s).map{it to galleryRowInfo(it,state,s)})if(r.isNow||r.isNext){
                val tag=if(t==AppTheme.PRAYER_GALLERY)if(r.isNow)"gallery-status-now"else"gallery-status-next" else if(r.isNow)"home-status-now"else"home-status-next"
                readable(tag,reflow)
            }
            target("landscape-hero-details",reflow);target("landscape-more-actions",reflow)
            if(t==AppTheme.KISWAH){val band=rule.onNodeWithTag("kiswah-calligraphy-band",true);if(reflow)band.performScrollTo();band.assertIsDisplayed().assertContentDescriptionEquals(Kiswah.BAND)}
            rule.onNodeWithContentDescription(Str[R.string.s_settings]).also{if(reflow)it.performScrollTo()}.performClick();assertEquals(1,settings)
            rule.onNodeWithContentDescription(Str[R.string.s_learn_salah]).also{if(reflow)it.performScrollTo()}.performClick();assertEquals(1,learn)
            rule.onNodeWithContentDescription(Str[R.string.s_monthly_timetable]).also{if(reflow)it.performScrollTo()}.performClick();assertEquals(1,timetable)
            val row=rule.onNodeWithTag("compact-prayer-FAJR",true);if(reflow)row.performScrollTo();row.performClick();assertEquals(1,toggles)
            val info=rule.onNodeWithTag("landscape-info-MAGHRIB",true);if(reflow)info.performScrollTo();info.performClick()
            rule.onNodeWithText(Str[R.string.s_calculated_on_this_device],substring=true).assertExists()
        }finally{scenario.close()}
    }
    @Test fun iqamahAndQuietKeepAllDetailsAndControlsVisibleWithoutScrolling() {
        for(t in themes())for(l in Language.entries)for(small in listOf(false,true))for(kind in listOf("iqamah","quiet")) {
            val s=settings(t,l);app.settings.update{s};val now=ZonedDateTime.of(2026,10,18,19,if(kind=="iqamah")21 else 24,0,0,s.zone())
            var starts=0;var stops=0
            try {
                show(Dev.PHONE_LANDSCAPE,t,rtl=l==Language.UR,language=l,qualifiers=if(small)"w740dp-h360dp-land-xhdpi"else null) {
                    Box(Modifier.padding(top=12.dp,bottom=20.dp)) {
                        if(kind=="iqamah")IqamahPrayerContent(Phase.IqamahCountdown(Prayer.MAGHRIB,now.minusMinutes(7).toInstant().toEpochMilli(),now.plusMinutes(3).toInstant().toEpochMilli()),s,now,{stops++},{starts++})
                        else QuietPrayerContent(Phase.Quiet(Prayer.MAGHRIB,now.plusMinutes(10).toInstant().toEpochMilli()),s,now){stops++}
                    }
                }
                capture(t,"$kind-${if(small)"small"else"normal"}",l);noScroll()
                val tags=if(kind=="iqamah")listOf("iqamah-brand","iqamah-mode","iqamah-live-clock","iqamah-location","iqamah-prayer-arabic","iqamah-prayer-name","iqamah-until","iqamah-seconds","iqamah-unit","iqamah-at-label","iqamah-at-time","iqamah-reminder","iqamah-source","iqamah-hint","iqamah-start-label","iqamah-dismiss-label")+(0..3).map{"iqamah-stage-$it"}
                    else listOf("quiet-brand","quiet-live-clock","quiet-progress","quiet-current-arabic","quiet-current-name","quiet-current-label","quiet-current-time","quiet-iqamah-complete","quiet-next-label","quiet-next-name","quiet-next-time","quiet-expiry-hint","quiet-exit-label")
                tags.forEach{readable(it)}
                if(t==AppTheme.KISWAH)rule.onNodeWithTag("kiswah-calligraphy-band",true).assertIsDisplayed().assertContentDescriptionEquals(Kiswah.BAND)
                if(kind=="iqamah") {
                    rule.onNodeWithTag("iqamah-seconds",true).assertTextEquals("03:00")
                    rule.onNodeWithTag("iqamah-at-time",true).assertTextEquals(L10n.iso(L10n.at(now.plusMinutes(3),s.use24h)))
                    target("iqamah-start");target("iqamah-dismiss")
                    rule.onNodeWithTag("iqamah-start",true).performClick();assertEquals(1,starts)
                    rule.onNodeWithTag("iqamah-dismiss",true).performClick();assertEquals(1,stops)
                }else {
                    val times=quietPrayerTimes(s,Prayer.MAGHRIB,now)
                    rule.onNodeWithTag("quiet-current-time",true).assertTextEquals(L10n.iso(L10n.at(times.current,s.use24h)))
                    rule.onNodeWithTag("quiet-next-time",true).assertTextEquals(L10n.iso(L10n.at(times.nextTime,s.use24h)))
                    target("quiet-return");rule.onNodeWithTag("quiet-return",true).performClick();assertEquals(1,stops)
                    rule.onNodeWithTag("quiet-board",true).performClick();assertEquals(2,stops)
                }
            }finally{scenario.close()}
        }
    }
}
