package com.usman.miqaat.ui

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.onNodeWithTag
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.Language
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
                if (device.contains("landscape")) {
                    val countdown = rule.onNodeWithTag("friday-devotional-jummah-countdown", useUnmergedTree = true)
                        .fetchSemanticsNode().boundsInRoot
                    check(countdown.bottom <= arabic.top) {
                        "${theme.name} $design countdown overlaps Arabic title on $device: $countdown vs $arabic"
                    }
                }
                if (theme == AppTheme.MIQAAT && device.contains("landscape") && design != 3) {
                    val hero = rule.onNodeWithTag("friday-devotional-hero", useUnmergedTree = true)
                        .fetchSemanticsNode().boundsInRoot
                    val message = rule.onNodeWithTag("friday-devotional-message", useUnmergedTree = true)
                        .fetchSemanticsNode().boundsInRoot
                    check(message.height >= 40f && message.bottom <= hero.bottom) {
                        "${theme.name} $design message clipped on $device: message=$message hero=$hero"
                    }
                    if (device.startsWith("tablet")) {
                        val prayers = rule.onNodeWithTag("friday-devotional-prayer-rows", useUnmergedTree = true)
                            .fetchSemanticsNode().boundsInRoot
                        check(message.bottom <= prayers.top) {
                            "${theme.name} $design message overlaps prayer row on $device: $message vs $prayers"
                        }
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

    @Test fun maUrduRtlLargeTextAndTwentyFourHourClock() {
        for (design in 1..4) {
            shot("friday-accessibility-ma-$design", AppTheme.MIQAAT, scale = 1.15f, rtl = true,
                verify = {
                    val arabic = rule.onNodeWithTag("friday-devotional-arabic", useUnmergedTree = true)
                        .fetchSemanticsNode().boundsInRoot
                    val date = rule.onNodeWithTag("friday-devotional-date", useUnmergedTree = true)
                        .fetchSemanticsNode().boundsInRoot
                    check(date.bottom <= arabic.top) {
                        "MA$design Urdu date/title overlap on $device: $date vs $arabic"
                    }
                    if (device.contains("landscape")) {
                        val countdown = rule.onNodeWithTag("friday-devotional-jummah-countdown", useUnmergedTree = true)
                            .fetchSemanticsNode().boundsInRoot
                        check(countdown.bottom <= arabic.top) {
                            "MA$design Urdu countdown/title overlap on $device: $countdown vs $arabic"
                        }
                    }
                }) { base, _ ->
                val s = base.copy(language = Language.UR, use24h = true,
                    jumuahEnabled = true, jumuahMinutes = 13 * 60 + 15)
                val now = ZonedDateTime.of(2026, 10, 30, 11, 47, 0, 0, s.zone())
                FridayDevotionalDisplay(PrayerEngine.state(s, now), s, actions, design) {}
            }
        }
        val captures = out.listFiles().orEmpty().filter { it.name.startsWith("friday-accessibility-ma-") && it.extension == "png" }
        check(captures.size == 4) { "Expected four Urdu large-text captures, found ${captures.size}" }
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
