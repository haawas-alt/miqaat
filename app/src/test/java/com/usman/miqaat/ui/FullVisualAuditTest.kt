package com.usman.miqaat.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZonedDateTime
import java.time.ZoneId

/** Opt-in, actual app composition captures; these are evidence, not pixel-golden acceptance. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class FullVisualAuditTest : ComposeSupport() {
    private val theme get() = AppTheme.valueOf(System.getenv("MIQAAT_VISUAL_THEME") ?: "PRAYER_GALLERY")
    private val phase get() = System.getenv("MIQAAT_CAPTURE_PHASE") ?: "current"
    private fun capture(dev: Dev, lang: Language, scale: Float, page: String) {
        rule.waitForIdle()
        val prefix="build/visual-audit/$phase/${theme.name}/${dev.label}/${lang.name}-${(scale*100).toInt()}-$page"
        scenario.onActivity { act ->
            val v=act.findViewById<android.view.View>(android.R.id.content)
            val b=android.graphics.Bitmap.createBitmap(v.width,v.height,android.graphics.Bitmap.Config.ARGB_8888)
            v.draw(android.graphics.Canvas(b))
            File("$prefix.png").also { it.parentFile.mkdirs() }.outputStream().use { b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
            b.recycle()
        }
        File("$prefix.txt").writeText(rule.onRoot(useUnmergedTree=true).printToString(maxDepth=100))
    }
    private fun audit(dev: Dev) {
        assumeTrue(System.getenv("MIQAAT_VISUAL_AUDIT")=="1")
        val failures=mutableListOf<String>()
        for (lang in Language.entries) {
            val s=AppSettings(theme=theme,language=lang,latitude=-33.93,longitude=151.19,locationName="Mascot, NSW",zoneId="Australia/Sydney",locationSet=true,setupDone=true,showEndTimes=true)
            val state=PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,ZoneId.of("Australia/Sydney")))
            fun shot(name:String, scale:Float=1f, content:@Composable ()->Unit) {
                app.settings.update { s }
                var opened=false
                try {
                    show(dev,theme,scale,rtl=lang==Language.UR,language=lang,content=content);opened=true
                    capture(dev,lang,scale,name)
                } catch(e:Throwable) { failures += "${lang.name}/$scale/$name: ${e.stackTraceToString()}" }
                finally { if(opened) scenario.close() }
            }
            val a=HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})
            shot("home") { HomeRouter(state,s,a,false,{}) }
            if(phase=="as-is") continue
            shot("settings") { SettingsScreen(app.settings,s) {} }
            for(section in Section.entries) shot("settings-${section.name.lowercase()}") { SettingsScreen(app.settings,s,section) {} }
            shot("timetable") { TimetableScreen(s) {} }
            shot("qibla") { QiblaScreen(s,fixedHeading=250f) {} }
            shot("learn-overview") { LearnScreen(s) {} }
            for(i in Learn.actions(Learn.Lesson.FAJR).indices) shot("learn-fajr-${(i+1).toString().padStart(2,'0')}") { LearnScreen(s,Learn.Lesson.FAJR,i) {} }
            for(mode in AdhkarMode.entries) shot("adhkar-${mode.name.lowercase()}") { AdhkarScreen(mode) {} }
            shot("friday") { FridayScreen(s) {} }
            val now=System.currentTimeMillis()
            val phases=listOf("azaan" to Phase.Azaan(Prayer.MAGHRIB,true),"dua" to Phase.Dua(Prayer.MAGHRIB),"iftar" to Phase.Iftar(Prayer.MAGHRIB),"hadith" to Phase.HadithPhase(Prayer.MAGHRIB,HadithLibrary.all.first(),now,now+180000,false),"iqamah-countdown" to Phase.IqamahCountdown(Prayer.MAGHRIB,now,now+180000),"iqamah-now" to Phase.IqamahNow(Prayer.MAGHRIB),"quiet" to Phase.Quiet(Prayer.MAGHRIB,now+180000))
            for((name,p) in phases) shot(name) { AzaanScreen(p,{}, {}) }
            for(scale in listOf(1.3f,2f)) {
                shot("home",scale) { HomeRouter(state,s,a,false,{}) }
                shot("settings-test",scale) { SettingsScreen(app.settings,s,Section.TEST) {} }
                shot("learn-fatiha",scale) { LearnScreen(s,Learn.Lesson.FAJR,3) {} }
                shot("adhkar-morning",scale) { AdhkarScreen(AdhkarMode.MORNING) {} }
                shot("qibla",scale) { QiblaScreen(s,fixedHeading=250f) {} }
            }
        }
        val f=File("build/visual-audit/$phase/${theme.name}/${dev.label}/failures.txt");f.parentFile.mkdirs();f.writeText(failures.joinToString("\n\n"))
        assertTrue("Capture failures: ${failures.map { it.lineSequence().first() }}",failures.isEmpty())
    }
    @Test fun phonePortrait()=audit(Dev.PHONE_PORTRAIT)
    @Test fun phoneLandscape()=audit(Dev.PHONE_LANDSCAPE)
    @Test fun tabletPortrait()=audit(Dev.TABLET_PORTRAIT)
    @Test fun tabletLandscape()=audit(Dev.TABLET_LANDSCAPE)
}
