package com.usman.miqaat.ui

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.Language
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Review evidence for the complete Friday page on every supported device shape and language. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class FridayVisualAuditTest : ComposeSupport() {
    @Test fun captureFridayMatrix() {
        assumeTrue(System.getenv("MIQAAT_FRIDAY_AUDIT") == "1")
        val theme = AppTheme.valueOf(System.getenv("MIQAAT_VISUAL_THEME") ?: "MIQAAT")
        val out = File("build/friday-audit/${theme.name}").also { it.mkdirs() }
        for (dev in Dev.entries) for (language in Language.entries) {
            val s = AppSettings(
                theme = theme, language = language, locationName = "Sydney, Australia",
                latitude = -33.8688, longitude = 151.2093, zoneId = "Australia/Sydney",
                locationSet = true, setupDone = true, jumuahEnabled = true, iqamahEnabled = true
            )
            app.settings.update { s }
            show(dev, theme, rtl = language == Language.UR, language = language) { FridayScreen(s) {} }
            rule.mainClock.advanceTimeBy(600)
            val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
            File(out, "friday__${dev.label}__${language.name.lowercase()}.png").outputStream().use {
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
            scenario.close()
        }
    }
}
