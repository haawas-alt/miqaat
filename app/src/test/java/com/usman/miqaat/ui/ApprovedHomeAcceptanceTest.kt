package com.usman.miqaat.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextLayoutResult
import com.usman.miqaat.data.*
import com.usman.miqaat.R
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class ApprovedHomeAcceptanceTest : ComposeSupport() {
    private fun audit(dev: Dev) {
        val only = System.getenv("MIQAAT_VISUAL_THEME")
        for (theme in AppTheme.entries.filter { only == null || it.name == only }) for (lang in Language.entries) {
            val s = AppSettings(theme=theme,language=lang,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true,showEndTimes=true)
            val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,ZoneId.of(s.zoneId)))
            app.settings.update { s }
            var toggles=0; var settings=0; var learns=0; var table=0; var prepared:Prayer?=null
            val a=HomeActions({ table++ },{ settings++ },{}, {}, {}, {}, { learns++ },false,{}, { toggles++ }, { prepared=it })
            try {
                show(dev,theme,rtl=lang==Language.UR,language=lang) { HomeRouter(state,s,a,false,{}) }
                val prefix="build/approved-designs/${theme.name}/${dev.label}-${lang.name}"
                scenario.onActivity { act ->
                    val view=act.findViewById<View>(android.R.id.content)
                    val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
                    view.draw(Canvas(bitmap))
                    File("$prefix.png").also { it.parentFile.mkdirs() }.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) };bitmap.recycle()
                }
                File("$prefix.txt").writeText(rule.onRoot(true).printToString(100))
                val root=rule.onRoot().fetchSemanticsNode().boundsInRoot
                for (p in Prayer.entries) {
                    val target=rule.onNodeWithTag("approved-row-${p.name}",true)
                    if((s.iqamahEnabled || s.showRelative) && lang==Language.UR) target.performScrollTo()
                    val row=target.fetchSemanticsNode().boundsInRoot
                    assertTrue("$theme/$dev/$lang: $p outside viewport: $row vs $root",row.top>=root.top && row.bottom<=root.bottom && row.left>=root.left && row.right<=root.right)
                    assertTrue("$theme/$dev/$lang: $p target height",row.height>=48*app.resources.displayMetrics.density)
                    for (kind in listOf("label","clock","detail")) {
                        val node=rule.onNodeWithTag("gallery-fit-$kind-${p.name}",true).fetchSemanticsNode()
                        val layouts=mutableListOf<TextLayoutResult>()
                        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                        layouts.forEach { assertFalse("$theme/$dev/$lang $p $kind overflow: ${it.layoutInput.text} ${it.size} paragraph=${it.multiParagraph.width}x${it.multiParagraph.height} w=${it.didOverflowWidth} h=${it.didOverflowHeight}",it.multiParagraph.didExceedMaxLines || (0 until it.lineCount).any { line -> it.getLineRight(line)-it.getLineLeft(line) > node.size.width+1f } || (it.lineCount>0 && it.getLineBottom(it.lineCount-1)-it.getLineTop(0)>node.size.height+1f)) }
                    }
                    val info=rule.onNodeWithTag("approved-info-${p.name}",true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Info target too small",info.width>=48*app.resources.displayMetrics.density && info.height>=48*app.resources.displayMetrics.density)
                    rule.onNodeWithTag("approved-timeline-${p.name}",true).assertIsDisplayed()
                }
                if(theme==AppTheme.KISWAH) rule.onNodeWithTag("kiswah-calligraphy-band",true).assertIsDisplayed().assertContentDescriptionEquals(Kiswah.BAND)
                rule.onNodeWithContentDescription(Str[R.string.s_settings]).performClick(); assertEquals(1,settings)
                rule.onNodeWithContentDescription(Str[R.string.s_learn_salah]).performClick(); assertEquals(1,learns)
                rule.onNodeWithContentDescription(Str[R.string.s_monthly_timetable]).performClick(); assertEquals(1,table)
                rule.onNodeWithTag("approved-row-FAJR",true).performClick(); assertEquals(1,toggles)
                if(theme==AppTheme.PRAYER_GALLERY) {
                    rule.onNodeWithText(if(lang==Language.UR) "نماز کی تیاری" else "Prepare for ${heroInfo(state,s,null).label}",useUnmergedTree=true).performClick()
                    assertEquals(state.hero,prepared)
                }
                rule.onNodeWithTag("approved-info-MAGHRIB",true).performClick()
                rule.onNodeWithText(Str[R.string.s_calculated_on_this_device],substring=true).assertExists()
            } finally { scenario.close() }
        }
    }
    @Test fun retainedDisplaySettingsFit() {
        val only=System.getenv("MIQAAT_VISUAL_THEME")
        for(theme in AppTheme.entries.filter { only==null || it.name==only }) for(lang in Language.entries) for(variant in listOf("relative","24h-five-prayers","ramadan-iqamah")) {
            val base=AppSettings(theme=theme,language=lang,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true)
            val s=when(variant) {
                "relative" -> base.copy(showRelative=true)
                "24h-five-prayers" -> base.copy(use24h=true,showSunrise=false,showHijri=false,showEndTimes=false)
                else -> base.copy(ramadanMode=RamadanMode.ON,iqamahEnabled=true)
            }
            app.settings.update { s }
            val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,ZoneId.of(s.zoneId)))
            try {
                show(Dev.PHONE_PORTRAIT,theme,rtl=lang==Language.UR,language=lang) { HomeRouter(state,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{}) }
                for(p in listedPrayers(s)) {
                    val target=rule.onNodeWithTag("approved-row-${p.name}",true)
                    if((s.iqamahEnabled || s.showRelative) && lang==Language.UR) target.performScrollTo()
                    val row=target.fetchSemanticsNode()
                    val bounds=row.boundsInRoot
                    fun walk(n:androidx.compose.ui.semantics.SemanticsNode) {
                        val layouts=mutableListOf<TextLayoutResult>()
                        n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                        layouts.forEach { layout ->
                            val width=(0 until layout.lineCount).maxOfOrNull { layout.getLineRight(it)-layout.getLineLeft(it) } ?: 0f
                            val height=if(layout.lineCount>0) layout.getLineBottom(layout.lineCount-1)-layout.getLineTop(0) else 0f
                            assertFalse("$theme/$lang/$variant/$p clipped ${layout.layoutInput.text}: ${n.size} vs $width,$height", layout.multiParagraph.didExceedMaxLines || width>n.size.width+1f || height>n.size.height+1f)
                            assertTrue("$theme/$lang/$variant/$p text leaves its row",n.boundsInRoot.top>=bounds.top && n.boundsInRoot.bottom<=bounds.bottom)
                        }
                        n.children.forEach { walk(it) }
                    }
                    walk(row)
                }
            } finally { scenario.close() }
        }
    }
    @Test fun compactPhonesKeepHeroActionsAndPrayerRowsReachable() {
        val only=System.getenv("MIQAAT_VISUAL_THEME")
        for(theme in AppTheme.entries.filter { only==null || it.name==only }) for(lang in Language.entries) {
            val s=AppSettings(theme=theme,language=lang,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true)
            val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,ZoneId.of(s.zoneId)))
            app.settings.update { s }
            try {
                show(Dev.PHONE_PORTRAIT,theme,rtl=lang==Language.UR,language=lang,qualifiers="w360dp-h800dp-port-xhdpi") {
                    androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding(top=24.dp,bottom=48.dp)) {
                        HomeRouter(state,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{})
                    }
                }
                val details=rule.onNodeWithText(Str[R.string.s_view_prayer_details],useUnmergedTree=true)
                details.assertIsDisplayed()
                val target=details.onParent().fetchSemanticsNode().boundsInRoot
                assertTrue("$theme/$lang: compact hero details target clipped: $target", target.height>=48*app.resources.displayMetrics.density)
                for(p in listedPrayers(s)) {
                    val row=rule.onNodeWithTag("approved-row-${p.name}",true)
                    if(theme!=AppTheme.PRAYER_GALLERY) row.performScrollTo()
                    row.assertIsDisplayed()
                }
            } finally { scenario.close() }
        }
    }
    @Test fun preparationOpensTheMatchingExistingLesson() {
        assertEquals(Learn.Lesson.FAJR,preparationLesson(Prayer.FAJR))
        assertEquals(Learn.Lesson.MAGHRIB,preparationLesson(Prayer.MAGHRIB))
        for(p in listOf(Prayer.DHUHR,Prayer.ASR,Prayer.ISHA)) assertEquals(Learn.Lesson.FOUR,preparationLesson(p))
        assertNull(preparationLesson(Prayer.SUNRISE))
    }
    @Test fun phonePortrait()=audit(Dev.PHONE_PORTRAIT)
    @Test fun tabletPortrait()=audit(Dev.TABLET_PORTRAIT)
    @Test fun tabletLandscape()=audit(Dev.TABLET_LANDSCAPE)
}
