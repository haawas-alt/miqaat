package com.usman.miqaat.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.test.*
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanScheduler
import com.usman.miqaat.azaan.AzaanService
import com.usman.miqaat.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk=[34])
class AzaanPreviewControlTest:ComposeSupport() {
    @Suppress("UNCHECKED_CAST")
    private fun publish(phase:AzaanService.Phase?,wait:Boolean=true) {
        val field=AzaanService::class.java.getDeclaredField("_phase").apply {isAccessible=true}
        (field.get(null) as MutableStateFlow<AzaanService.Phase?>).value=phase
        if(wait)rule.waitForIdle()
    }
    private fun requested():Intent {
        var i=shadowOf(app).nextStartedService
        while(i!=null) {
            if(i.component?.className==AzaanService::class.java.name)return i
            i=shadowOf(app).nextStartedService
        }
        error("No AzaanService command requested")
    }
    private fun button(p:Prayer)=rule.onNodeWithTag("azaan-preview-${p.name}")
    @Test fun playStopFollowsServiceStateAcrossPrayersCompletionAndReentry() {
        val before=AzaanService.phase.value
        try {
            for(lang in Language.entries)for(dev in listOf(Dev.PHONE_PORTRAIT,Dev.TABLET_LANDSCAPE,Dev.TABLET_PORTRAIT)) {
                val s=AppSettings(theme=AppTheme.KISWAH,language=lang,locationSet=true,setupDone=true)
                app.settings.update {s}
                publish(null,false)
                while(shadowOf(app).nextStartedService!=null){}
                try {
                    show(dev,AppTheme.KISWAH,rtl=lang==Language.UR,language=lang){SettingsScreen(app.settings,s,Section.AZAAN){}}
                    button(Prayer.FAJR).performScrollTo().assertTextEquals(Str[R.string.s_play]).performClick()
                    val play=requested();assertEquals(AzaanService.ACTION_PREVIEW,play.action)
                    assertEquals(Prayer.FAJR.name,play.getStringExtra(AzaanScheduler.EXTRA_PRAYER))
                    publish(AzaanService.Phase.Azaan(Prayer.FAJR,true))
                    button(Prayer.FAJR).assertTextEquals(Str[R.string.s_stop])
                    button(Prayer.DHUHR).assertTextEquals(Str[R.string.s_play])
                    capture("${dev.name}-${lang.name}-playing")
                    button(Prayer.FAJR).performScrollTo().performClick()
                    assertEquals(AzaanService.ACTION_STOP,requested().action)
                    publish(null)
                    button(Prayer.FAJR).assertTextEquals(Str[R.string.s_play])
                    // Completion/error/service destruction all publish null through the existing service.
                    publish(AzaanService.Phase.Azaan(Prayer.FAJR,true));publish(null)
                    button(Prayer.FAJR).assertTextEquals(Str[R.string.s_play])
                    // Another preview takes over; only its row can stop it.
                    publish(AzaanService.Phase.Azaan(Prayer.DHUHR,true))
                    button(Prayer.FAJR).assertTextEquals(Str[R.string.s_play])
                    button(Prayer.DHUHR).performScrollTo().assertTextEquals(Str[R.string.s_stop])
                    assertEquals(s.azaanEnabled,app.settings.value.azaanEnabled)
                } finally {scenario.close()}
                try {
                    show(dev,AppTheme.KISWAH,rtl=lang==Language.UR,language=lang){SettingsScreen(app.settings,s,Section.AZAAN){}}
                    button(Prayer.DHUHR).performScrollTo().assertTextEquals(Str[R.string.s_stop])
                    publish(null)
                    button(Prayer.DHUHR).assertTextEquals(Str[R.string.s_play])
                } finally {scenario.close()}
            }
        } finally {publish(before,false)}
    }
    private fun capture(name:String) {
        scenario.onActivity {act ->
            val v=act.findViewById<View>(android.R.id.content)
            val b=Bitmap.createBitmap(v.width,v.height,Bitmap.Config.ARGB_8888);v.draw(Canvas(b))
            File("build/azaan-preview-review/$name.png").also {it.parentFile.mkdirs()}.outputStream().use {b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
        }
    }
}
