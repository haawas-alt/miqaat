package com.usman.miqaat.ui

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.data.*
import org.junit.Rule
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Shared plumbing for the emulator tests: hosts real app UI in a real activity on a real device, inside the real theme,
 * with the font scale and layout direction the caller asks for. Device size and orientation come from the emulator itself.
 */
abstract class ShotSupport {
    @get:Rule val rule = createEmptyComposeRule()
    protected lateinit var scenario: ActivityScenario<ComponentActivity>
    protected val app: MiqaatApp get() = ApplicationProvider.getApplicationContext<Application>() as MiqaatApp
    private val ctx get() = InstrumentationRegistry.getInstrumentation().targetContext
    protected val out: File by lazy { File(ctx.filesDir, "screens").also { it.mkdirs() } }
    protected val errors = mutableListOf<String>()

    /** e.g. "tablet-landscape": read from the activity actually on screen, after rotation, not assumed. */
    protected var device: String = "unknown"
    private fun readDevice() {
        scenario.onActivity { a ->
            val c = a.resources.configuration
            val land = a.resources.displayMetrics.widthPixels > a.resources.displayMetrics.heightPixels
            val short = minOf(c.screenWidthDp, c.screenHeightDp)
            device = (if (short >= 600) "tablet" else "phone") + "-" + if (land) "landscape" else "portrait"
        }
    }

    protected fun settingsFor(theme: AppTheme, rtl: Boolean) = AppSettings(
        theme = theme, language = if (rtl) Language.UR else Language.EN, locationName = "Sydney, Australia", latitude = -33.8688, longitude = 151.2093,
        locationSet = true, setupDone = true, zoneId = "Australia/Sydney"
    )
    protected fun stateAt(s: AppSettings, hour: Int = 15, minute: Int = 40): PrayerState =
        PrayerEngine.state(s, ZonedDateTime.of(2026, 9, 30, hour, minute, 0, 0, ZoneId.of("Australia/Sydney")))
    protected val actions = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})

    /** The CI job asks for a rotation through the runner argument; the shell settings were not honoured on every emulator, so the test enforces it. */
    private fun enforceRotation() {
        val want = InstrumentationRegistry.getArguments().getString("orientation") ?: return   // "landscape" or "portrait"
        val inst = InstrumentationRegistry.getInstrumentation()
        val dm = ctx.getSystemService(android.content.Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager
        fun landscape(): Boolean { val p = android.graphics.Point(); dm.getDisplay(android.view.Display.DEFAULT_DISPLAY).getRealSize(p); return p.x > p.y }
        val wantLandscape = want == "landscape"
        if (landscape() == wantLandscape) return
        for (rot in listOf(android.app.UiAutomation.ROTATION_FREEZE_90, android.app.UiAutomation.ROTATION_FREEZE_270, android.app.UiAutomation.ROTATION_FREEZE_0)) {
            inst.uiAutomation.setRotation(rot); Thread.sleep(900)
            if (landscape() == wantLandscape) return
        }
    }

    protected fun host(theme: AppTheme, fontScale: Float, rtl: Boolean, content: @Composable () -> Unit) {
        enforceRotation()
        Str.apply(app, if (rtl) Language.UR else Language.EN)
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { act ->
            act.setContent {
                val d = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(d.density, fontScale),
                    LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) { MiqaatTheme(theme) { content() } }
            }
        }
        rule.waitForIdle()
        readDevice()
    }

    protected fun shot(name: String, theme: AppTheme, scale: Float = 1f, rtl: Boolean = false, content: @Composable (AppSettings, PrayerState) -> Unit) {
        var label = "$name-$theme"
        try {
            val s = settingsFor(theme, rtl); val st = stateAt(s)
            host(theme, scale, rtl) { content(s, st) }
            label = "${name}__${theme.name.lowercase()}__${device}__${(scale * 100).toInt()}${if (rtl) "__ur" else ""}"
            Thread.sleep(700); rule.waitForIdle()
            val bmp = rule.onRoot().captureToImage().asAndroidBitmap()
            File(out, "$label.png").outputStream().use { bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            scenario.close()
        } catch (t: Throwable) {
            errors += "$label: ${t.javaClass.simpleName}: ${t.message?.take(200)}"
            runCatching { scenario.close() }
        }
    }

    protected fun flushErrors() { if (errors.isNotEmpty()) File(out, "_errors_${device}.txt").appendText(errors.joinToString("\n") + "\n") }
}
