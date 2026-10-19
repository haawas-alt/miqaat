package com.usman.miqaat.ui

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
            var toggles=0; var settings=0; var learns=0; var table=0
            val a=HomeActions({ table++ },{ settings++ },{}, {}, {}, {}, { learns++ },false,{}, { toggles++ })
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
                    val row=rule.onNodeWithTag("approved-row-${p.name}",true).fetchSemanticsNode().boundsInRoot
                    assertTrue("$theme/$dev/$lang: $p outside viewport: $row vs $root",row.top>=root.top && row.bottom<=root.bottom && row.left>=root.left && row.right<=root.right)
                    assertTrue("$theme/$dev/$lang: $p target height",row.height>=48*app.resources.displayMetrics.density)
                    for (kind in listOf("label","clock","detail")) {
                        val node=rule.onNodeWithTag("gallery-fit-$kind-${p.name}",true).fetchSemanticsNode()
                        val layouts=mutableListOf<TextLayoutResult>()
                        node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                        layouts.forEach { assertFalse("$theme/$dev/$lang $p $kind overflow: ${it.layoutInput.text} ${it.size} paragraph=${it.multiParagraph.width}x${it.multiParagraph.height} w=${it.didOverflowWidth} h=${it.didOverflowHeight}",it.didOverflowWidth || it.didOverflowHeight) }
                    }
                    val info=rule.onNodeWithTag("approved-info-${p.name}",true).fetchSemanticsNode().boundsInRoot
                    assertTrue("Info target too small",info.width>=48*app.resources.displayMetrics.density && info.height>=48*app.resources.displayMetrics.density)
                    rule.onNodeWithTag("approved-timeline-${p.name}",true).assertIsDisplayed()
                }
                if(theme==AppTheme.KISWAH) rule.onNodeWithTag("kiswah-calligraphy-band",true).assertIsDisplayed().assertTextEquals(Kiswah.BAND)
                rule.onNodeWithContentDescription(Str[R.string.s_settings]).performClick(); assertEquals(1,settings)
                rule.onNodeWithContentDescription(Str[R.string.s_learn_salah]).performClick(); assertEquals(1,learns)
                rule.onNodeWithContentDescription(Str[R.string.s_monthly_timetable]).performClick(); assertEquals(1,table)
                rule.onNodeWithTag("approved-row-FAJR",true).performClick(); assertEquals(1,toggles)
                rule.onNodeWithTag("approved-info-MAGHRIB",true).performClick()
                rule.onNodeWithText(Str[R.string.s_calculated_on_this_device],substring=true).assertExists()
            } finally { scenario.close() }
        }
    }
    @Test fun phonePortrait()=audit(Dev.PHONE_PORTRAIT)
    @Test fun tabletPortrait()=audit(Dev.TABLET_PORTRAIT)
    @Test fun tabletLandscape()=audit(Dev.TABLET_LANDSCAPE)
}
