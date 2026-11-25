package com.usman.miqaat.ui

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.onNodeWithTag
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.PrayerEngine
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime

/** Native captures at the emulator's actual viewport for all twelve devotional variants. */
@RunWith(AndroidJUnit4::class)
class FridayDevotionalEmulatorTest : ShotSupport() {
    @Test fun allThreeThemesFourDesigns() {
        for (theme in listOf(AppTheme.MIQAAT, AppTheme.KISWAH, AppTheme.CELESTIAL_MERIDIAN)) for (design in 1..4) {
            shot("friday-devotional-${theme.name.lowercase()}-$design", theme, verify = {
                val date = rule.onNodeWithTag("friday-devotional-date", useUnmergedTree = true)
                    .fetchSemanticsNode().boundsInRoot
                val arabic = rule.onNodeWithTag("friday-devotional-arabic", useUnmergedTree = true)
                    .fetchSemanticsNode().boundsInRoot
                check(date.bottom <= arabic.top) {
                    "${theme.name} $design date overlaps Arabic title on $device: ${date.bottom} > ${arabic.top}"
                }
                if (theme == AppTheme.MIQAAT && device.contains("landscape") && design != 3) {
                    val hero = rule.onNodeWithTag("friday-devotional-hero", useUnmergedTree = true)
                        .fetchSemanticsNode().boundsInRoot
                    val message = rule.onNodeWithTag("friday-devotional-message", useUnmergedTree = true)
                        .fetchSemanticsNode().boundsInRoot
                    check(message.height >= 40f && message.bottom <= hero.bottom) {
                        "${theme.name} $design message clipped on $device: message=$message hero=$hero"
                    }
                }
            }) { base, _ ->
                val s = base.copy(jumuahEnabled = true, jumuahMinutes = 13 * 60 + 15)
                val now = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, s.zone())
                FridayDevotionalDisplay(PrayerEngine.state(s, now), s, actions, design) {}
            }
        }
        val captures = out.listFiles().orEmpty().filter { it.name.startsWith("friday-devotional-") && it.extension == "png" }
        check(captures.size == 12) { "Expected twelve devotional captures, found ${captures.size}" }
        for (file in captures) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MiqaatDevotional")
            }
            val uri = checkNotNull(app.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values))
            checkNotNull(app.contentResolver.openOutputStream(uri)).use { file.inputStream().use { source -> source.copyTo(it) } }
        }
    }
}
