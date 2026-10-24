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
import java.time.ZonedDateTime
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class KiswahOptionOneBandTest : ComposeSupport() {
    @Test fun fullHomeRetainsApprovedBorderAndPrayerTargets() {
        for (qualifier in listOf("w411dp-h891dp-port-xhdpi", "w360dp-h800dp-port-xhdpi", "w891dp-h411dp-land-xhdpi", "w740dp-h360dp-land-xhdpi")) {
            val wide=qualifier.contains("-land-")
            val s=AppSettings(theme=AppTheme.KISWAH,language=Language.EN,latitude=-33.93,longitude=151.19,
                locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true)
            app.settings.update{s}
            val st=PrayerEngine.state(s,ZonedDateTime.of(2026,10,24,16,27,0,0,s.zone()))
            try {
                show(if(wide)Dev.PHONE_LANDSCAPE else Dev.PHONE_PORTRAIT,AppTheme.KISWAH,qualifiers=qualifier) {
                    Box(Modifier.padding(start=if(wide)24.dp else 0.dp,bottom=12.dp)) {
                        HomeRouter(st,s,HomeActions({}, {}, {}, {}, {}, {}, {},false,{},{}),false,{})
                    }
                }
                val border=rule.onNodeWithTag("kiswah-band-ornament",true).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                assertEquals(if(wide)1421f/168f else 876f/200f,border.width/border.height,.03f)
                scenario.onActivity { act ->
                    val v=act.findViewById<View>(android.R.id.content)
                    val bitmap=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
                    v.draw(Canvas(bitmap))
                    val out=File("build/landscape-parity/KISWAH/option1-home-$qualifier.png")
                    out.parentFile.mkdirs();out.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
                    File(out.path+".bounds").writeText("${border.left},${border.top},${border.right},${border.bottom}")
                    bitmap.recycle()
                }
                for(p in listedPrayers(s)) {
                    if(wide) {
                        val labels=mutableListOf<TextLayoutResult>()
                        rule.onNodeWithTag("approved-timeline-${p.name}",true).fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(labels)
                        assertFalse("Timeline clips ${p.name} on $qualifier",labels.single().hasVisualOverflow)
                    }
                    val tag=if(wide)"landscape-info-${p.name}" else "approved-info-${p.name}"
                    val target=rule.onNodeWithTag(tag,true)
                    if(!wide)runCatching{target.performScrollTo()}
                    target.assertIsDisplayed()
                    val b=target.fetchSemanticsNode().boundsInRoot
                    val d=app.resources.displayMetrics.density
                    assertTrue("Prayer detail target shrank below 48dp: $qualifier/$p $b",b.width>=48*d-.1f&&b.height>=48*d-.1f)
                }
            } finally {scenario.close()}
        }
    }

    @Test fun approvedBorderKeepsEveryArabicLineInsideTheFrame() {
        for (wide in listOf(false, true)) for (scale in listOf(1f, 1.3f, 2f)) {
            try {
                show(if(wide) Dev.PHONE_LANDSCAPE else Dev.PHONE_PORTRAIT, AppTheme.KISWAH, fontScale=scale) {
                    Box(Modifier.width(if(wide)844.dp else 390.dp).padding(8.dp)) { KiswahOptionOneBand(wide) }
                }
                val ornament=rule.onNodeWithTag("kiswah-band-ornament",true).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
                val approvedAspect=if(wide)1421f/168f else 876f/200f
                assertEquals("The approved frame must not be flattened",approvedAspect,ornament.width/ornament.height,.03f)
                val node=rule.onNodeWithTag("kiswah-calligraphy-band",true).fetchSemanticsNode()
                val layouts=mutableListOf<TextLayoutResult>()
                node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                val layout=layouts.single()
                assertFalse("Arabic overflows at font scale $scale",layout.hasVisualOverflow)
                assertEquals(1,layout.lineCount)
                assertEquals("Portrait and landscape must use the same Arabic font", ReemKufi,layout.layoutInput.style.fontFamily)
                val expected=if(wide)Kiswah.BAND else Kiswah.BAND.substringBefore("   ✦   ")
                assertEquals(expected,layout.layoutInput.text.text)
                rule.onNodeWithTag("kiswah-calligraphy-band",true).assertContentDescriptionEquals(expected)
                val frame=rule.onNodeWithTag("kiswah-calligraphy-frame",true).fetchSemanticsNode().boundsInRoot
                assertTrue(node.boundsInRoot.left > frame.left)
                assertTrue(node.boundsInRoot.top > frame.top)
                assertTrue(node.boundsInRoot.right < frame.right)
                assertTrue(node.boundsInRoot.bottom < frame.bottom)
                scenario.onActivity { act ->
                    val v=act.findViewById<View>(android.R.id.content)
                    val bitmap=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888)
                    v.draw(Canvas(bitmap))
                    val out=File("build/landscape-parity/KISWAH/option1-${if(wide)"landscape" else "portrait"}-$scale.png")
                    out.parentFile.mkdirs();out.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
                    File(out.path+".bounds").writeText("${ornament.left},${ornament.top},${ornament.right},${ornament.bottom}")
                    bitmap.recycle()
                }
            } finally { scenario.close() }
        }
    }
}
