package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.usman.miqaat.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZonedDateTime
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class LandscapeMockupParityTest:ComposeSupport() {
    private fun themes()=AppTheme.entries.filter{System.getenv("MIQAAT_VISUAL_THEME").let{only->only==null||it.name==only}}
    private fun settings(t:AppTheme,l:Language)=AppSettings(theme=t,language=l,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true)
    private fun readable(tag:String) {
        val n=rule.onNodeWithTag(tag,true).assertIsDisplayed().fetchSemanticsNode();val ls=mutableListOf<TextLayoutResult>()
        n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(ls)
        assertTrue("$tag lacks native text",ls.isNotEmpty())
        for(l in ls) {
            val w=(0 until l.lineCount).maxOfOrNull{l.getLineRight(it)-l.getLineLeft(it)}?:0f
            val h=if(l.lineCount>0)l.getLineBottom(l.lineCount-1)-l.getLineTop(0)else 0f
            assertFalse("$tag clips '${l.layoutInput.text}': ${n.size} vs $w,$h",l.multiParagraph.didExceedMaxLines||w>n.size.width+1f||h>n.size.height+1f)
        }
    }
    private fun capture(t:AppTheme,name:String,l:Language) {
        rule.waitForIdle();val prefix="build/landscape-parity/${t.name}/$name-${l.name}"
        scenario.onActivity {act->val v=act.findViewById<View>(android.R.id.content);val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
            File("$prefix.png").also{it.parentFile.mkdirs()}.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
        File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
    }
    @Test fun referenceCompositionUsesCompactHeaderCardsHeroAndFooter() {
        for(t in themes())for(l in Language.entries)for(device in listOf("reference","device","short")) {
            val s=settings(t,l);app.settings.update{s};val st=PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,s.zone()))
            try {
                show(Dev.PHONE_LANDSCAPE,t,rtl=l==Language.UR,language=l,qualifiers=when(device){"reference"->"w960dp-h400dp-land-xhdpi";"short"->"w740dp-h360dp-land-xhdpi";else->null}) {
                    Box(Modifier.padding(start=if(device=="device")36.dp else 0.dp,bottom=if(device=="device")12.dp else 0.dp)) {
                        HomeRouter(st,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{})
                    }
                }
                capture(t,device,l)
                val board=rule.onNodeWithTag("landscape-fit-board",true).fetchSemanticsNode().boundsInRoot
                val header=rule.onNodeWithTag("landscape-compact-header",true).fetchSemanticsNode().boundsInRoot
                val dates=rule.onNodeWithTag("landscape-date",true).fetchSemanticsNode().boundsInRoot
                val hijri=rule.onNodeWithTag("landscape-hijri",true).fetchSemanticsNode().boundsInRoot
                assertTrue("Dates must share the reference header line",abs(dates.center.y-hijri.center.y)<1f)
                assertTrue("Header grew beyond reference",header.height<=board.height*.15f)
                val grid=rule.onNodeWithTag("home-prayers",true).fetchSemanticsNode().boundsInRoot
                val hero=rule.onNodeWithTag("landscape-authored-hero",true).fetchSemanticsNode().boundsInRoot
                assertTrue("Reference grid/hero ratio drifted",grid.width/(grid.width+hero.width) in .61f.. .63f)
                for(p in listedPrayers(s))for(k in listOf("name","time","detail"))readable("landscape-row-$k-${p.name}")
                if(l==Language.EN)for(p in listedPrayers(s)) {
                    val name=rule.onNodeWithTag("landscape-row-name-${p.name}",true).fetchSemanticsNode().boundsInRoot
                    val time=rule.onNodeWithTag("landscape-row-time-${p.name}",true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Name and clock no longer share a compact line",abs(name.top-time.top)<8*app.resources.displayMetrics.density)
                }
                for(p in listedPrayers(s)) {
                    val row=galleryRowInfo(p,st,s)
                    if(row.isNow||row.isNext) {
                        val tag=if(t==AppTheme.PRAYER_GALLERY)if(row.isNow)"gallery-status-now"else"gallery-status-next" else if(row.isNow)"home-status-now"else"home-status-next"
                        readable(tag)
                        val badge=rule.onNodeWithTag(tag,true).fetchSemanticsNode().boundsInRoot
                        val time=rule.onNodeWithTag("landscape-row-time-${p.name}",true).fetchSemanticsNode().boundsInRoot
                        assertTrue("Status must remain beside the clock rather than on artwork or name",abs(badge.center.y-time.center.y)<12*app.resources.displayMetrics.density)
                        assertTrue("Status must not cover the clock",badge.right<=time.left+1f||badge.left>=time.right-1f)
                    }
                }
                for(tag in listOf("landscape-location","landscape-date","landscape-hijri","landscape-live-clock","landscape-hero-name","landscape-hero-english","landscape-hero-clock","landscape-hero-status"))readable(tag)
                val details=rule.onNodeWithTag("landscape-hero-details",true).fetchSemanticsNode().boundsInRoot
                assertTrue("Reference details button became full-hero width",details.width/hero.width in .50f.. .87f)
                if(t==AppTheme.KISWAH)rule.onNodeWithTag("kiswah-band-ornament",true).assertIsDisplayed()
            }finally{scenario.close()}
        }
    }
    @Test fun clockBadgesFitBeforeFajrAndAroundMiddayWithLongLocation() {
        for(t in themes())for(hour in listOf(4,12,13)) {
            val s=settings(t,Language.EN).copy(latitude=39.95,longitude=116.27,zoneId="Asia/Shanghai",locationName="Hai Dian Qu, Bei Jing Shi")
            app.settings.update{s}
            val st=PrayerEngine.state(s,ZonedDateTime.of(2026,10,20,hour,15,0,0,s.zone()))
            try {
                show(Dev.PHONE_LANDSCAPE,t,qualifiers="w740dp-h360dp-land-xhdpi") {
                    Box(Modifier.padding(start=24.dp,bottom=12.dp)) {HomeRouter(st,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{})}
                }
                capture(t,"beijing-$hour",Language.EN)
                readable("landscape-location")
                for(p in listedPrayers(s)) {
                    for(k in listOf("name","time","detail"))readable("landscape-row-$k-${p.name}")
                    val row=galleryRowInfo(p,st,s)
                    if(row.isNow||row.isNext)readable(if(t==AppTheme.PRAYER_GALLERY)if(row.isNow)"gallery-status-now"else"gallery-status-next" else if(row.isNow)"home-status-now"else"home-status-next")
                }
            }finally{scenario.close()}
        }
    }
    @Test fun landscapeAlertsAndContextActionsRetainTheirDestinations() {
        for(t in themes()) {
            val s=settings(t,Language.EN).copy(zoneNeedsReview=true);app.settings.update{s}
            val st=PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,s.zone()));var q=0;var mode:AdhkarMode?=null;var location=0
            try {
                show(Dev.PHONE_LANDSCAPE,t) {HomeRouter(st,s,HomeActions({}, {}, {location++}, {q++}, {mode=it}, {}, {},false,{},{}),false,{})}
                rule.onNodeWithTag("landscape-door-0",true).performClick();assertEquals(1,q)
                rule.onNodeWithTag("landscape-door-1",true).performClick();assertEquals(AdhkarMode.EVENING,mode)
                rule.onNodeWithTag("landscape-more-actions",true).performClick()
                rule.onNodeWithText(Str[com.usman.miqaat.R.string.s_time_zone_needs_checking]).performClick();assertEquals(1,location)
            }finally{scenario.close()}
        }
    }
}
