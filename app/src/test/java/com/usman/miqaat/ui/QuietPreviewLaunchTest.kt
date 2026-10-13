package com.usman.miqaat.ui

import android.content.Intent
import androidx.compose.ui.test.*
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanActivity
import com.usman.miqaat.azaan.AzaanScheduler
import com.usman.miqaat.azaan.AzaanService
import com.usman.miqaat.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class QuietPreviewLaunchTest : ComposeSupport() {
    @Test fun settingsShowButtonRequestsQuietPreviewOnPhoneAndTablet() {
        for (dev in listOf(Dev.PHONE_PORTRAIT, Dev.TABLET_LANDSCAPE)) {
            val s = AppSettings(locationSet = true, setupDone = true, quietMinutes = 0)
            app.settings.update { s }
            while (shadowOf(app).nextStartedService != null) {}
            try {
                show(dev) { SettingsScreen(app.settings, s, initial = Section.TEST) {} }
                rule.onNodeWithText(Str[R.string.s_show]).performScrollTo().performClick()
                val launched = shadowOf(app).nextStartedService
                assertNotNull("Show must request the quiet service", launched)
                assertEquals(AzaanService::class.java.name, launched.component?.className)
                assertEquals(AzaanService.ACTION_QUIET, launched.action)
                assertEquals(Prayer.MAGHRIB.name, launched.getStringExtra(AzaanScheduler.EXTRA_PRAYER))
            } finally { runCatching { scenario.close() } }
        }
    }

    @Test fun quietPreviewPublishesPhaseAndOpensScreenEvenWhenDisabled() {
        for (minutes in listOf(0, 3)) {
            app.settings.update { it.copy(quietMinutes = minutes) }
            while (shadowOf(app).nextStartedActivity != null) {}
            val controller = Robolectric.buildService(AzaanService::class.java).create()
            try {
                val service = controller.get()
                val before = System.currentTimeMillis()
                service.onStartCommand(Intent(app, AzaanService::class.java)
                    .setAction(AzaanService.ACTION_QUIET)
                    .putExtra(AzaanScheduler.EXTRA_PRAYER, Prayer.MAGHRIB.name), 0, 1)
                val phase = AzaanService.phase.value
                assertTrue("Phase must exist before the activity launches", phase is AzaanService.Phase.Quiet)
                phase as AzaanService.Phase.Quiet
                assertEquals(Prayer.MAGHRIB, phase.prayer)
                val expected = (if (minutes == 0) 1 else minutes) * 60_000L
                assertTrue(phase.endsAt >= before + expected)
                assertTrue(phase.endsAt <= System.currentTimeMillis() + expected)
                val screen = shadowOf(app).nextStartedActivity
                assertNotNull("Quiet preview must actually open its screen", screen)
                assertEquals(AzaanActivity::class.java.name, screen.component?.className)
                assertEquals(Prayer.MAGHRIB.name, screen.getStringExtra(AzaanScheduler.EXTRA_PRAYER))
                assertTrue(screen.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
                assertEquals(minutes, app.settings.value.quietMinutes)
                service.onStartCommand(Intent(app, AzaanService::class.java).setAction(AzaanService.ACTION_STOP), 0, 2)
                assertNull(AzaanService.phase.value)
            } finally { controller.destroy() }
        }
    }
}
