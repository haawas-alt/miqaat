package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
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
class SanctuaryQuietMockupTest:ComposeSupport() {
    private fun readable(tag:String) {
        val q=rule.onNodeWithTag(tag,true);q.assertIsDisplayed()
        val n=q.fetchSemanticsNode();val root=rule.onRoot(true).fetchSemanticsNode().boundsInRoot;val b=n.boundsInRoot
        assertTrue("$tag outside screen $b / $root",b.left>=root.left && b.right<=root.right && b.top>=root.top && b.bottom<=root.bottom)
        val layouts=mutableListOf<TextLayoutResult>();n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
        assertTrue("$tag must expose actual native text layout",layouts.isNotEmpty())
        for(l in layouts) {
            val w=(0 until l.lineCount).maxOfOrNull {l.getLineRight(it)-l.getLineLeft(it)}?:0f
            val h=if(l.lineCount>0)l.getLineBottom(l.lineCount-1)-l.getLineTop(0)else 0f
            assertFalse("$tag clipped ${l.layoutInput.text}: $w/$h vs ${n.size}",l.multiParagraph.didExceedMaxLines || w>n.size.width+1f || h>n.size.height+1f)
        }
    }
    @Test fun selectedSanctuariesKeepLiveFactsReadableAndReturnWorking() {
        val failures=mutableListOf<String>()
        for(theme in AppTheme.entries)for(lang in Language.entries)for(dev in Dev.entries)for(use24 in listOf(false,true)) {
            val s=AppSettings(theme=theme,language=lang,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true,
                use24h=use24,useOverrides=true,overrides=mapOf("2026-10-27" to listOf(287,357,765,981,1160,1235)))
            val now=ZonedDateTime.of(2026,10,27,19,35,0,0,s.zone())
            val p=Phase.Quiet(Prayer.MAGHRIB,now.plusMinutes(10).toInstant().toEpochMilli());var stopped=0
            try {
                show(dev,theme,language=lang,rtl=lang==Language.UR) {QuietPrayerContent(p,s,now){stopped++}}
                rule.waitForIdle()
                val path=File("build/sanctuary-review/${theme.name}/${dev.label}-${lang.name}-${if(use24)"24h" else "12h"}.png");path.parentFile.mkdirs()
                scenario.onActivity {a->val v=a.findViewById<View>(android.R.id.content);val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b));path.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
                File(path.path+".txt").writeText(rule.onRoot(true).printToString(100))
                for(tag in listOf("quiet-brand","quiet-live-clock","quiet-current-arabic","quiet-current-name","quiet-progress","quiet-current-label","quiet-current-time","quiet-iqamah-complete","quiet-next-label","quiet-next-arabic","quiet-next-name","quiet-next-time","quiet-next-relative","quiet-expiry-hint","quiet-exit-label"))readable(tag)
                val times=quietPrayerTimes(s,p.prayer,now)
                assertEquals(Prayer.ISHA,times.next)
                rule.onNodeWithTag("quiet-current-time",true).assertTextEquals(L10n.iso(L10n.at(times.current,use24)))
                rule.onNodeWithTag("quiet-next-time",true).assertTextEquals(L10n.iso(L10n.at(times.nextTime,use24)))
                val current=rule.onNodeWithTag("quiet-current-panel",true).fetchSemanticsNode().boundsInRoot
                val next=rule.onNodeWithTag("quiet-next-panel",true).fetchSemanticsNode().boundsInRoot
                assertTrue("Sanctuary panels overlap",current.right<=next.left || current.bottom<=next.top)
                val ret=rule.onNodeWithTag("quiet-return",true)
                assertTrue("Return target under48dp",ret.fetchSemanticsNode().boundsInRoot.height>=48*app.resources.displayMetrics.density-.5f)
                ret.performClick();assertEquals(1,stopped)
                rule.onNodeWithTag("quiet-board",true).performClick();assertEquals(2,stopped)
            }catch(e:AssertionError){failures+="$theme/$dev/$lang/$use24: ${e.message}"}finally{scenario.close()}
        }
        assertTrue(failures.joinToString("\n"),failures.isEmpty())
    }
}
