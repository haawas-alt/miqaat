package com.usman.miqaat.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.Language
import com.usman.miqaat.data.PrayerEngine
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

/** Native review evidence for the approved Friday home mockups, not the separate Friday routine page. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class FridayHomeVisualAuditTest : ComposeSupport() {
    @Test fun captureFridayHomeMatrix() {
        assumeTrue(System.getenv("MIQAAT_FRIDAY_HOME_AUDIT") == "1")
        val theme = AppTheme.valueOf(System.getenv("MIQAAT_VISUAL_THEME") ?: "MIQAAT")
        val out = File("build/friday-home-audit/${theme.name}").also { it.mkdirs() }
        val zone = ZoneId.of("Australia/Sydney")
        val now = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, zone)
        val actions = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})

        for (dev in Dev.entries) for (language in Language.entries) {
            val settings = AppSettings(
                theme = theme,
                language = language,
                locationName = "Mascot, NSW",
                latitude = -33.93,
                longitude = 151.19,
                zoneId = zone.id,
                locationSet = true,
                setupDone = true,
                showEndTimes = true,
                jumuahEnabled = true,
                jumuahMinutes = 13 * 60 + 15,
                iqamahEnabled = true,
                jumuahIqamahMinutes = 13 * 60 + 30
            )
            app.settings.update { settings }
            val state = PrayerEngine.state(settings, now)
            show(
                dev,
                theme,
                fontScale = if (language == Language.UR) 1.15f else 1f,
                rtl = language == Language.UR,
                language = language
            ) { HomeRouter(state, settings, actions, false) {} }

            val file = File(out, "friday-home__${dev.label}__${language.name.lowercase()}.png")
            scenario.onActivity { activity ->
                val view = activity.findViewById<View>(android.R.id.content)
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
            scenario.close()
        }
    }
}
