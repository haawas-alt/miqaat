package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class ApprovedPhoneRefinementAcceptanceTest:ComposeSupport() {
    private fun themes()=AppTheme.entries.filter{System.getenv("MIQAAT_VISUAL_THEME").let{v->v==null||v==it.name}}
    private fun settings(t:AppTheme,l:Language)=AppSettings(theme=t,language=l,latitude=-33.93,longitude=151.19,
        locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true)
    private fun now(s:AppSettings)=ZonedDateTime.of(2026,10,18,18,53,0,0,s.zone())
    private fun reach(tag:String):SemanticsNodeInteraction {
        val q=rule.onNodeWithTag(tag,true);runCatching{q.performScrollTo()};q.assertIsDisplayed();return q
    }
    private fun readable(tag:String) {
        val n=reach(tag).fetchSemanticsNode();val ls=mutableListOf<TextLayoutResult>()
        n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(ls)
        assertTrue("$tag lacks native text",ls.isNotEmpty())
        for(l in ls) {
            val w=(0 until l.lineCount).maxOfOrNull{l.getLineRight(it)-l.getLineLeft(it)}?:0f
            val h=if(l.lineCount>0)l.getLineBottom(l.lineCount-1)-l.getLineTop(0) else 0f
            assertFalse("$tag clips '${l.layoutInput.text}': ${n.size} vs $w,$h",l.multiParagraph.didExceedMaxLines||w>n.size.width+1f||h>n.size.height+1f)
        }
    }
    private fun target(tag:String) {
        val b=reach(tag).fetchSemanticsNode().boundsInRoot;val d=app.resources.displayMetrics.density
        assertTrue("$tag is below 48dp: $b",b.width>=48*d-.1f&&b.height>=48*d-.1f)
    }
    private fun capture(t:AppTheme,name:String,l:Language) {
        rule.waitForIdle();val prefix="build/phone-refinement/${t.name}/$name-${l.name}"
        scenario.onActivity { act->val v=act.findViewById<View>(android.R.id.content);val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
            v.draw(Canvas(b));File("$prefix.png").also{it.parentFile.mkdirs()}.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle() }
        File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
    }
    @Test fun portraitDockAndClockColumnsFitBothLanguagesAndPhoneSizes() {
        for(t in themes())for(l in Language.entries)for(small in listOf(false,true))for(variant in listOf("default","relative","24h")) {
            val s=settings(t,l).copy(showRelative=variant=="relative",use24h=variant=="24h");app.settings.update{s}
            val st=PrayerEngine.state(s,now(s));var qibla=0;var learn=0;var table=0;var mode:AdhkarMode?=null
            val a=HomeActions({table++},{},{},{qibla++},{mode=it},{},{learn++},false,{},{})
            try {
                show(Dev.PHONE_PORTRAIT,t,rtl=l==Language.UR,language=l,qualifiers=if(small)"w360dp-h800dp-port-xhdpi"else null) {
                    Box(Modifier.padding(top=12.dp,bottom=20.dp)){HomeRouter(st,s,a,false,{})}
                }
                val origins=listedPrayers(s).map { p ->
                    readable("gallery-fit-clock-${p.name}");readable("gallery-fit-detail-${p.name}")
                    val b=reach("gallery-fit-clock-${p.name}").fetchSemanticsNode().boundsInRoot
                    if(l==Language.UR)b.right else b.left
                }
                assertTrue("Clock columns drift: $t/$l/$small/$variant $origins",origins.max()-origins.min()<=1f)
                for(item in listOf("qibla","adhkar","learn","timetable")) {
                    target("phone-dock-$item");readable("phone-dock-$item-label");readable("phone-dock-$item-context")
                }
                reach("phone-dock-qibla-context").assertTextEquals(L10n.iso(PrayerEngine.qibla(s).let{"${it.toInt()}° ${PrayerEngine.compass(it)}"}))
                reach("phone-dock-adhkar-context").assertTextEquals(if(l==Language.UR)"شام"else"Evening")
                capture(t,"portrait-${if(small)"small"else"normal"}-$variant",l)
                reach("phone-dock-qibla").performClick();assertEquals(1,qibla)
                reach("phone-dock-adhkar").performClick();assertEquals(AdhkarMode.EVENING,mode)
                reach("phone-dock-learn").performClick();assertEquals(1,learn)
                reach("phone-dock-timetable").performClick();assertEquals(1,table)
                assertEquals(1,rule.onAllNodesWithContentDescription(L10n.word(s,"Evening adhkār")).fetchSemanticsNodes().size)
            }finally{scenario.close()}
        }
    }
    @Test fun dockPreferencesAndAdhkarPeriodsRetainTheirActions() {
        for(t in themes())for(l in Language.entries)for(hour in listOf(2,6,13,18)) {
            val s=settings(t,l).copy(showQibla=false,kidsMode=false);app.settings.update{s}
            val st=PrayerEngine.state(s,now(s).withHour(hour).withMinute(if(hour==13)0 else 0));var picked:AdhkarMode?=null
            try {
                show(Dev.PHONE_PORTRAIT,t,rtl=l==Language.UR,language=l) { HomeRouter(st,s,HomeActions({}, {}, {}, {}, {picked=it}, {}, {},false,{},{}),false,{}) }
                rule.onNodeWithTag("phone-dock-qibla",true).assertDoesNotExist();rule.onNodeWithTag("phone-dock-learn",true).assertDoesNotExist()
                reach("phone-dock-adhkar").performClick()
                assertEquals(when(hour){6->AdhkarMode.MORNING;13->AdhkarMode.POST;18->AdhkarMode.EVENING;else->AdhkarMode.MORNING},picked)
            }finally{scenario.close()}
        }
    }
    @Test fun themePreviewsSelectCorrectlyOnPhonesAtNormalAndLargeText() {
        for(t in themes())for(l in Language.entries)for(large in listOf(false,true)) {
            val s=settings(t,l);app.settings.update{s};var picked:AppTheme?=null
            try {
                show(Dev.PHONE_PORTRAIT,t,rtl=l==Language.UR,language=l,fontScale=if(large)2f else 1f) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { ThemePicker(t,s) {picked=it} }
                }
                for(choice in AppTheme.entries) {
                    reach("theme-preview-${choice.name}").performTouchInput{click()};assertEquals(choice,picked)
                }
                capture(t,"theme-picker-phone-${if(large)"large"else"normal"}",l)
            }finally{scenario.close()}
        }
    }
    @Test fun kiswahGlyphClearanceAndCurrentThemePreviews() {
        for(t in themes())for(l in Language.entries) {
            val s=settings(t,l);app.settings.update{s}
            if(t==AppTheme.KISWAH)for(small in listOf(false,true)) {
                try {
                    show(Dev.PHONE_LANDSCAPE,t,rtl=l==Language.UR,language=l,qualifiers=if(small)"w740dp-h360dp-land-xhdpi"else null) {
                        Box(Modifier.padding(top=12.dp,bottom=20.dp)){HomeRouter(PrayerEngine.state(s,now(s)),s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{})}
                    }
                    val frame=rule.onNodeWithTag("kiswah-calligraphy-frame",true).fetchSemanticsNode().boundsInRoot
                    val text=rule.onNodeWithTag("kiswah-calligraphy-band",true).fetchSemanticsNode().boundsInRoot
                    val d=app.resources.displayMetrics.density;val min=if(small)6 else 10
                    assertTrue("Arabic touches top frame: $frame vs $text",text.top-frame.top>=min*d-.1f)
                    assertTrue("Arabic touches bottom frame: $frame vs $text",frame.bottom-text.bottom>=min*d-.1f)
                    val ornament=rule.onNodeWithTag("kiswah-band-ornament",true).fetchSemanticsNode().boundsInRoot
                    assertEquals("Option 1 frame proportions differ from the approved artwork",1421f/168f,ornament.width/ornament.height,.03f)
                    readable("kiswah-calligraphy-band");capture(t,"band-${if(small)"small"else"normal"}",l)
                }finally{scenario.close()}
            }
            var picked:AppTheme?=null
            try {
                show(Dev.TABLET_LANDSCAPE,t,rtl=l==Language.UR,language=l) { ThemePicker(t,s) {picked=it} }
                for(choice in AppTheme.entries) {rule.onNodeWithTag("theme-preview-${choice.name}",true).assertIsDisplayed().performTouchInput{click()};assertEquals(choice,picked)}
                capture(t,"theme-picker",l)
            }finally{scenario.close()}
        }
    }
}
