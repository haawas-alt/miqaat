package com.usman.miqaat.ui

import android.Manifest
import android.app.NotificationManager
import android.content.pm.PackageManager
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlarmManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.*
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.usman.miqaat.R
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
@Config(sdk = [34])
class PortraitPolishAcceptanceTest : ComposeSupport() {
    @Test fun portraitDesignReview() {
        for (theme in AppTheme.entries.filter { it != AppTheme.PRAYER_GALLERY &&
            System.getenv("MIQAAT_VISUAL_THEME").let { v -> v == null || v == it.name } })
        for (lang in Language.entries) for (small in listOf(false, true)) {
            val s = AppSettings(theme = theme, language = lang, latitude = -33.93, longitude = 151.19,
                locationName = "Mascot, NSW", zoneId = "Australia/Sydney", locationSet = true,
                setupDone = true, showRelative = true, nightDim = false)
            app.settings.update { s }
            val state = PrayerEngine.state(s, ZonedDateTime.of(2026, 10, 25, 8, 38, 0, 0, s.zone()))
            val manager = app.getSystemService(NotificationManager::class.java)
            val exactBefore = Reliability.exactAlarmsGranted(app)
            val notificationsBefore = manager.areNotificationsEnabled()
            val permissionBefore = app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            try {
                shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
                ShadowAlarmManager.setCanScheduleExactAlarms(true)
                shadowOf(manager).setNotificationsEnabled(true)
                assertTrue("Morning preview must match configured alerts", Reliability.allGood(app))
                show(Dev.PHONE_PORTRAIT, theme, rtl = lang == Language.UR, language = lang,
                    qualifiers = if (small) "w360dp-h800dp-port-xhdpi" else null) {
                    Box(Modifier.padding(top = 12.dp, bottom = 20.dp)) {
                        HomeRouter(state, s, HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {}), false, {})
                    }
                }
                rule.mainClock.advanceTimeBy(32); rule.waitForIdle()
                scenario.onActivity { act ->
                    val v = act.findViewById<View>(android.R.id.content)
                    val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
                    v.draw(Canvas(b))
                    File("build/portrait-polish/${theme.name}/review-${lang.name}-${if(small)360 else 411}.png")
                        .also { it.parentFile?.mkdirs() }.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    b.recycle()
                }
                File("build/portrait-polish/${theme.name}/review-${lang.name}-${if(small)360 else 411}.txt")
                    .writeText(rule.onRoot(true).printToString(100))
                val clock = rule.onNodeWithTag("approved-hero-clock", true).fetchSemanticsNode().boundsInRoot
                val status = rule.onNodeWithTag("portrait-hero-status", true).fetchSemanticsNode().boundsInRoot
                val density = app.resources.displayMetrics.density
                val title = rule.onNodeWithTag("portrait-prayer-lockup", true).fetchSemanticsNode().boundsInRoot
                val time = rule.onNodeWithTag("portrait-hero-time", true).fetchSemanticsNode().boundsInRoot
                assertTrue("Configured portrait must centre the time below the name: $title / $time",
                    time.top >= title.bottom-16*density && kotlin.math.abs(time.center.x-title.center.x) <= density)
                assertTrue("Countdown detached from clock: $clock / $status", status.top-clock.bottom <= 12*density)
                val action = rule.onNodeWithTag("portrait-hero-details", true).fetchSemanticsNode().boundsInRoot
                assertTrue("Details action detached from countdown", action.top-status.bottom <= 8*density)
                assertTrue("Details target below 48dp", action.height >= 48*density-.1f)
            } finally {
                scenario.close()
                ShadowAlarmManager.setCanScheduleExactAlarms(exactBefore)
                shadowOf(manager).setNotificationsEnabled(notificationsBefore)
                if (!permissionBefore) shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @Test fun nightAndDayPortraitKeepTextTargetsAndDestinationsReadable() {
        for (theme in AppTheme.entries.filter { it != AppTheme.PRAYER_GALLERY &&
            System.getenv("MIQAAT_VISUAL_THEME").let { v -> v == null || v == it.name } })
        for (lang in Language.entries) for (small in listOf(false, true)) for (relative in listOf(false, true)) {
            val s = AppSettings(theme = theme, language = lang, latitude = -33.93, longitude = 151.19,
                locationName = "Mascot, NSW", zoneId = "Australia/Sydney", locationSet = true,
                setupDone = true, showRelative = relative)
            app.settings.update { s }
            val now = ZonedDateTime.of(2026, 10, 24, if (relative) 23 else 13, 10, 0, 0, s.zone())
            val state = PrayerEngine.state(s, now)
            var learn = 0; var table = 0
            val actions = HomeActions({ table++ }, {}, {}, {}, {}, {}, { learn++ }, false, {}, {})
            try {
                show(Dev.PHONE_PORTRAIT, theme, rtl = lang == Language.UR, language = lang,
                    qualifiers = if (small) "w360dp-h800dp-port-xhdpi" else null) {
                    Box(Modifier.padding(top = 12.dp, bottom = 20.dp)) { HomeRouter(state, s, actions, false, {}) }
                }
                scenario.onActivity { act ->
                    val v = act.findViewById<View>(android.R.id.content)
                    val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
                    v.draw(Canvas(b))
                    File("build/portrait-polish/${theme.name}/${lang.name}-${if(small)360 else 411}-${if(relative)"night-relative" else "day"}.png")
                        .also { it.parentFile?.mkdirs() }.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    b.recycle()
                }
                fun reach(tag: String): SemanticsNodeInteraction = rule.onNodeWithTag(tag, true).also {
                    runCatching { it.performScrollTo() }; it.assertIsDisplayed()
                }
                for (tag in listOf("approved-hero-arabic", "approved-hero-clock")) {
                    val n = reach(tag).fetchSemanticsNode()
                    val layouts = mutableListOf<TextLayoutResult>()
                    n.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action?.invoke(layouts)
                    assertTrue(layouts.isNotEmpty())
                    layouts.forEach { l ->
                        val width = (0 until l.lineCount).maxOf { l.getLineRight(it)-l.getLineLeft(it) }
                        val height = l.getLineBottom(l.lineCount-1)-l.getLineTop(0)
                        assertFalse("$theme/$lang/$small clips $tag: node=${n.size} ink=$width,$height text=${l.layoutInput.text}", l.multiParagraph.didExceedMaxLines || width>n.size.width+1 || height>n.size.height+1)
                    }
                }
                for (tag in listOf("portrait-hero-details", "phone-dock-learn", "phone-dock-timetable")) {
                    val b = reach(tag).fetchSemanticsNode().boundsInRoot
                    val d = app.resources.displayMetrics.density
                    assertTrue("$tag below 48dp: $b", b.width >= 48*d-.1f && b.height >= 48*d-.1f)
                }
                assertEquals(1, rule.onAllNodesWithContentDescription(Str[R.string.s_learn_salah]).fetchSemanticsNodes().size)
                assertEquals(1, rule.onAllNodesWithContentDescription(Str[R.string.s_monthly_timetable]).fetchSemanticsNodes().size)
                reach("phone-dock-learn").performClick(); assertEquals(1, learn)
                reach("phone-dock-timetable").performClick(); assertEquals(1, table)
                reach("portrait-hero-details").performClick()
                rule.onNodeWithText(Str[R.string.s_calculated_on_this_device], substring = true).assertExists()
            } finally { scenario.close() }
        }
    }
}
