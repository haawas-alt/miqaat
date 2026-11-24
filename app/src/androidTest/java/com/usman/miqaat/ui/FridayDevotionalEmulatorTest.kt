package com.usman.miqaat.ui

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.PrayerEngine
import org.junit.Test
import org.junit.runner.RunWith
import java.time.ZonedDateTime

/** Native captures at the emulator's actual viewport; MA is the first visual gate. */
@RunWith(AndroidJUnit4::class)
class FridayDevotionalEmulatorTest : ShotSupport() {
    @Test fun maPilotFourDesigns() {
        for (design in 1..4) {
            shot("friday-devotional-ma-$design", AppTheme.MIQAAT) { base, _ ->
                val s = base.copy(jumuahEnabled = true, jumuahMinutes = 13 * 60 + 15)
                val now = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, s.zone())
                FridayDevotionalDisplay(PrayerEngine.state(s, now), s, actions, design) {}
            }
        }
        val captures = out.listFiles().orEmpty().filter { it.name.startsWith("friday-devotional-ma-") && it.extension == "png" }
        check(captures.size == 4) { "Expected four MA captures, found ${captures.size}" }
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
