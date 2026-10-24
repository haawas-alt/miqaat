package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
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
    @Test fun actualPhoneFontScalingKeepsBandDatesAndColumnsStable() {
        for(t in themes())for(scale in listOf(1f,1.15f,1.3f)) {
            val s=settings(t,Language.EN).copy(showRelative=true,latitude=40.12,longitude=116.65,
                zoneId="Asia/Shanghai",locationName="Shun Yi Qu, Bei Jing Shi")
            app.settings.update{s}
            val st=PrayerEngine.state(s,ZonedDateTime.of(2026,10,24,16,27,0,0,s.zone()))
            try {
                show(Dev.PHONE_LANDSCAPE,t,fontScale=scale,qualifiers="w891dp-h411dp-land-xhdpi") {
                    Box(Modifier.padding(start=36.dp,bottom=12.dp)) {
                        HomeRouter(st,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{})
                    }
                }
                capture(t,"actual-font-$scale",Language.EN)
                if(scale>1.15f) {
                    // Production routing deliberately uses AccessibleHome above 115%.
                    // Verify that page rather than expecting dense-board tags there.
                    for(p in listedPrayers(s)) {
                        rule.onNodeWithTag("accessible-prayer-${p.name}",true).performScrollTo().assertIsDisplayed()
                        rule.onNodeWithTag("accessible-clock-${p.name}",true).performScrollTo()
                        readable("accessible-clock-${p.name}")
                    }
                    continue
                }
                for(tag in listOf("landscape-hijri","landscape-date")) {
                    readable(tag)
                    val layouts=mutableListOf<TextLayoutResult>()
                    rule.onNodeWithTag(tag,true).fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                    assertEquals("Date must not strand AH on a second line",1,layouts.single().lineCount)
                }
                for(col in listOf(listOf(Prayer.FAJR,Prayer.DHUHR,Prayer.MAGHRIB),listOf(Prayer.SUNRISE,Prayer.ASR,Prayer.ISHA))) {
                    val sizes=mutableListOf<Float>()
                    val starts=col.map { p ->
                        for(k in listOf("name","time","detail","relative")) {
                            val tag="landscape-row-$k-${p.name}"
                            if(scale>1.2f)rule.onNodeWithTag(tag,true).performScrollTo()
                            readable(tag)
                        }
                        val info=galleryRowInfo(p,st,s)
                        if(info.isNow||info.isNext)readable(if(t==AppTheme.PRAYER_GALLERY)if(info.isNow)"gallery-status-now"else"gallery-status-next"else if(info.isNow)"home-status-now"else"home-status-next")
                        val layouts=mutableListOf<TextLayoutResult>()
                        rule.onNodeWithTag("landscape-row-time-${p.name}",true).fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                        sizes+=layouts.single().layoutInput.style.fontSize.value
                        rule.onNodeWithTag("landscape-row-time-${p.name}",true).fetchSemanticsNode().boundsInRoot.left
                    }
                    assertTrue("Prayer times need one common column at fontScale=$scale",starts.max()-starts.min()<1.1f)
                    assertTrue("NOW/NEXT must not shrink only their own clocks",sizes.max()-sizes.min()<.2f)
                }
                if(t==AppTheme.KISWAH) {
                    if(scale>1.2f)rule.onNodeWithTag("kiswah-calligraphy-band",true).performScrollTo()
                    readable("kiswah-calligraphy-band")
                    val layouts=mutableListOf<TextLayoutResult>()
                    rule.onNodeWithTag("kiswah-calligraphy-band",true).fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                    assertEquals("Kiswah phrase must stay on one line",1,layouts.single().lineCount)
                    val name=rule.onNodeWithTag("landscape-hero-name",true).fetchSemanticsNode().boundsInRoot
                    val clock=rule.onNodeWithTag("landscape-hero-clock",true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Kiswah hero must retain the approved stack",name.bottom<=clock.top+1f)
                }
            } finally { scenario.close() }
        }
    }
    private fun themes()=AppTheme.entries.filter{System.getenv("MIQAAT_VISUAL_THEME").let{only->only==null||it.name==only}}
    private fun settings(t:AppTheme,l:Language)=AppSettings(theme=t,language=l,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true)
    private fun readable(tag:String,clockSingleLine:Boolean=true) {
        val n=rule.onNodeWithTag(tag,true).assertIsDisplayed().fetchSemanticsNode();val ls=mutableListOf<TextLayoutResult>()
        n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(ls)
        assertTrue("$tag lacks native text",ls.isNotEmpty())
        for(l in ls) {
            if(clockSingleLine&&tag.startsWith("landscape-row-time-"))assertEquals("Prayer clock and suffix must share one line",1,l.lineCount)
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
                if(device=="reference"&&l==Language.EN) {
                    val first=rule.onNodeWithTag("approved-timeline-FAJR",true).fetchSemanticsNode().boundsInRoot
                    val last=rule.onNodeWithTag("approved-timeline-ISHA",true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Timeline first stop drifted from approved composition",(first.center.x-board.left)/board.width in .11f.. .15f)
                    assertTrue("Timeline last stop extends past the approved Isha position",(last.center.x-board.left)/board.width in .75f.. .82f)
                    val clock=ink("landscape-hero-clock")
                    assertTrue("Hero clock lost the approved visual hierarchy",clock.height/board.height in .12f.. .23f)
                }
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
    @Test fun relativeTimeFooterIsPinnedOnBeijingPhone() {
        for(t in themes())for(hour in listOf(14,17,22)) {
            val s=settings(t,Language.EN).copy(showRelative=true,latitude=39.95,longitude=116.27,zoneId="Asia/Shanghai",locationName=when(hour){22->"Xi Cheng Qu, Bei Jing Shi";17->"Chao Yang Qu, Bei Jing Shi";else->"Hai Dian Qu, Bei Jing Shi"})
            app.settings.update{s};val st=PrayerEngine.state(s,ZonedDateTime.of(2026,10,23,hour,when(hour){22->4;17->27;else->35},0,0,s.zone()))
            try {
                show(Dev.PHONE_LANDSCAPE,t,qualifiers="w891dp-h411dp-land-xhdpi") {
                    Box(Modifier.padding(start=36.dp,bottom=12.dp)){HomeRouter(st,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{})}
                }
                capture(t,when(hour){22->"actual-night-relative";17->"actual-maghrib-relative";else->"actual-relative"},Language.EN)
                assertEquals("English relative view must fit without scrolling",0,rule.onAllNodes(SemanticsMatcher("scroll"){it.config.getOrNull(SemanticsActions.ScrollBy)!=null},true).fetchSemanticsNodes().size)
                val root=rule.onRoot().fetchSemanticsNode().boundsInRoot
                for(tag in listOf("landscape-door-0","landscape-door-1","landscape-more-actions","landscape-timeline-panel")) {
                    val b=rule.onNodeWithTag(tag,true).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                    assertTrue("Footer clipped on actual relative-mode phone: $tag",b.bottom<=root.bottom&&b.top>=root.top)
                }
                // The reported 891x411 phone must retain the approved stacked hero.
                val name=rule.onNodeWithTag("landscape-hero-name",true).fetchSemanticsNode().boundsInRoot
                val clock=rule.onNodeWithTag("landscape-hero-clock",true).fetchSemanticsNode().boundsInRoot
                assertTrue("Hero switched to a side-by-side layout on the reported phone",name.bottom<=clock.top+1f)
                for(tag in listOf("landscape-hero-name","landscape-hero-clock","landscape-hero-status"))readable(tag)
                val details=rule.onNodeWithTag("landscape-hero-details",true).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                val density=app.resources.displayMetrics.density
                assertTrue("Hero details clipped on the reported phone",details.height>=48*density-.1f&&details.bottom<=root.bottom)
                for(p in listedPrayers(s)) {
                    for(k in listOf("name","time","detail","relative"))readable("landscape-row-$k-${p.name}")
                    val label=ink("landscape-row-name-${p.name}")
                    val clockInk=ink("landscape-row-time-${p.name}")
                    assertTrue("Prayer label touches clock: $t $p gap=${(clockInk.left-label.right)/density}dp",clockInk.left-label.right>=7.5f*density)
                    val r=galleryRowInfo(p,st,s)
                    rule.onNodeWithTag("landscape-row-time-${p.name}",true).assertTextEquals(L10n.iso((r.clock+" "+r.suffix).trim()))
                    rule.onNodeWithTag("landscape-row-relative-${p.name}",true).assertTextEquals(r.relative)
                }
            }finally{scenario.close()}
        }
    }
    /** Text glyph extents, rather than the full-width semantic slot, define the protected area. */
    private fun ink(tag:String):Rect {
        val n=rule.onNodeWithTag(tag,true).fetchSemanticsNode()
        val layouts=mutableListOf<TextLayoutResult>()
        n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        assertTrue("Missing text layout for artwork audit: $tag",layouts.isNotEmpty())
        val l=layouts.first();assertTrue(l.lineCount>0)
        // fill=false text can report a paragraph wider than its measured semantic slot.
        // Normalize centered line offsets to the actual slot instead of centering twice.
        val lineLeft=(0 until l.lineCount).minOf{l.getLineLeft(it)}
        val lineRight=(0 until l.lineCount).maxOf{l.getLineRight(it)}
        val dx=if(l.layoutInput.style.textAlign==TextAlign.Center)n.size.width/2f-(lineLeft+lineRight)/2f else 0f
        return Rect(n.boundsInRoot.left+dx+lineLeft,
            n.boundsInRoot.top+l.getLineTop(0),
            n.boundsInRoot.left+dx+lineRight,
            n.boundsInRoot.top+l.getLineBottom(l.lineCount-1))
    }
    @Test fun reportedNightPhoneHasClearArtworkBehindHeroText() {
        for(t in themes()) {
            val s=settings(t,Language.EN).copy(showRelative=true,nightDim=false,latitude=39.95,longitude=116.27,zoneId="Asia/Shanghai",locationName="Xi Cheng Qu, Bei Jing Shi")
            app.settings.update{s};val st=PrayerEngine.state(s,ZonedDateTime.of(2026,10,23,22,4,0,0,s.zone()))
            val artOnly=mutableStateOf(false)
            try {
                show(Dev.PHONE_LANDSCAPE,t,qualifiers="w891dp-h411dp-land-xhdpi") {
                    Box(Modifier.padding(start=36.dp,bottom=12.dp)) {
                        if(artOnly.value)LandscapeHomeArt(s,Modifier.fillMaxSize())
                        else HomeRouter(st,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{})
                    }
                }
                for(tag in listOf("landscape-hero-kicker","landscape-hero-name","landscape-hero-clock","landscape-hero-status"))readable(tag)
                capture(t,"audit-night-ui",Language.EN)
                if(t==AppTheme.KISWAH) {
                    val frame=rule.onNodeWithTag("kiswah-calligraphy-frame",true).fetchSemanticsNode().boundsInRoot
                    val density=app.resources.displayMetrics.density
                    scenario.onActivity {act->
                        val v=act.findViewById<View>(android.R.id.content)
                        val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
                        try {
                            for(top in listOf(true,false)) {
                                val y0=(if(top)frame.top+5*density else frame.bottom-6*density).toInt()
                                val y1=(if(top)frame.top+6*density else frame.bottom-5*density).toInt()
                                var marked=0
                                for(y in y0 until y1)for(x in (frame.left+68*density).toInt() until (frame.right-68*density).toInt()) {
                                    val pixel=b.getPixel(x,y);val red=android.graphics.Color.red(pixel);val green=android.graphics.Color.green(pixel);val blue=android.graphics.Color.blue(pixel)
                                    if(red>145&&green>95&&red>blue*1.15f&&green>blue*1.05f)marked++
                                }
                                assertTrue("Kiswah Arabic touches embroidered rail: top=$top pixels=$marked",marked<=2)
                            }
                        }finally{b.recycle()}
                    }
                }
                val tags=when(t) {
                    AppTheme.MIQAAT->listOf("landscape-hero-kicker","landscape-hero-clock","landscape-hero-status")
                    AppTheme.PRAYER_GALLERY->listOf("landscape-hero-clock","landscape-hero-status")
                    else->emptyList()
                }
                if(tags.isNotEmpty()) {
                    val density=app.resources.displayMetrics.density
                    val protected=tags.associateWith{ink(it).inflate(4*density)}
                    rule.runOnIdle{artOnly.value=true};rule.waitForIdle()
                    capture(t,"audit-night-art-only",Language.EN)
                    scenario.onActivity {act->
                        val v=act.findViewById<View>(android.R.id.content)
                        val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
                        try {
                            val report=StringBuilder()
                            for((tag,r)in protected) {
                                var marked=0;var pixels=0
                                for(y in r.top.toInt().coerceAtLeast(0) until r.bottom.toInt().coerceAtMost(b.height))
                                    for(x in r.left.toInt().coerceAtLeast(0) until r.right.toInt().coerceAtMost(b.width)) {
                                        val pixel=b.getPixel(x,y);val red=android.graphics.Color.red(pixel);val green=android.graphics.Color.green(pixel);val blue=android.graphics.Color.blue(pixel)
                                        val intrudes=if(t==AppTheme.MIQAAT)red>145&&green>95&&red>blue*1.15f&&green>blue*1.05f
                                            else red<125&&green<145&&blue<130
                                        if(intrudes)marked++;pixels++
                                    }
                                val ratio=marked.toFloat()/pixels.coerceAtLeast(1)
                                report.append("$tag rect=$r marked=$marked pixels=$pixels ratio=$ratio\n")
                                File("build/landscape-parity/${t.name}/artwork-clearance.txt").writeText(report.toString())
                                val limit=if(t==AppTheme.MIQAAT).005f else .01f
                                assertTrue("Artwork intrudes on hero text: $t $tag pixels=$marked/$pixels ratio=$ratio rect=$r",ratio<limit)
                            }
                            File("build/landscape-parity/${t.name}/artwork-clearance.txt").writeText(report.toString())
                        }finally{b.recycle()}
                    }
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

