package com.usman.miqaat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.graphics.asAndroidBitmap
import com.usman.miqaat.azaan.AzaanService
import com.usman.miqaat.data.*
import org.junit.Assume
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Renders the real app UI on the JVM for every theme × device × scale × direction and writes PNGs to app/build/screens.
 * Only runs when MIQAAT_SCREENSHOTS=1 (the CI "screens" job); the normal gate skips it.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34])
class ScreenshotTest : ComposeSupport() {
    private val out = File("build/screens").also { it.mkdirs() }
    private val errors = mutableListOf<String>()

    private fun settingsFor(theme: AppTheme, rtl: Boolean) = AppSettings(
        theme = theme, language = if (rtl) Language.UR else Language.EN, locationName = "Sydney, Australia", latitude = -33.8688, longitude = 151.2093,
        locationSet = true, setupDone = true, zoneId = "Australia/Sydney"
    )
    private fun stateFor(s: AppSettings): PrayerState = PrayerEngine.state(s, ZonedDateTime.of(2026, 9, 30, 13, 40, 0, 0, ZoneId.of("Australia/Sydney")))
    private val actions = HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {})

    private fun shot(name: String, dev: Dev, theme: AppTheme, scale: Float = 1f, rtl: Boolean = false, content: @Composable (AppSettings, PrayerState) -> Unit) {
        val label = "${name}__${theme.name.lowercase()}__${dev.label}__${(scale * 100).toInt()}${if (rtl) "__ur" else ""}"
        try {
            val s = settingsFor(theme, rtl); val st = stateFor(s)
            show(dev, theme = theme, fontScale = scale, rtl = rtl) { content(s, st) }
            rule.mainClock.advanceTimeBy(600)
            val bmp = rule.onRoot().captureToImage().asAndroidBitmap()
            File(out, "$label.png").outputStream().use { bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
            scenario.close()
        } catch (t: Throwable) {
            errors += "$label: ${t.javaClass.simpleName}: ${t.message?.take(300)}"
            runCatching { scenario.close() }
        }
    }

    @Composable private fun settingsUi(s: AppSettings, initial: Section?) {
        val live by app.settings.settings.collectAsState()
        SettingsScreen(app.settings, live.copy(theme = s.theme, language = s.language, locationName = s.locationName, locationSet = true, setupDone = true), initial) {}
    }

    @Test fun render() {
        Assume.assumeTrue(System.getenv("MIQAAT_SCREENSHOTS") == "1")
        val themes = AppTheme.entries
        // 1) every screen × theme × device at normal size, English
        for (t in themes) for (d in Dev.entries) {
            shot("home", d, t) { s, st -> HomeRouter(st, s, actions, showLarge = false) {} }
            shot("settings", d, t) { s, _ -> settingsUi(s, null) }
            shot("settings-display", d, t) { s, _ -> settingsUi(s, Section.DISPLAY) }
            shot("timetable", d, t) { s, _ -> TimetableScreen(s) {} }
            shot("qibla", d, t) { s, _ -> QiblaScreen(s) {} }
            shot("adhkar-morning", d, t) { _, _ -> AdhkarScreen(AdhkarMode.MORNING) {} }
            shot("friday", d, t) { s, _ -> FridayScreen(s) {} }
            shot("learn", d, t) { s, _ -> LearnScreen(s) {} }
            shot("azaan", d, t) { _, _ -> com.usman.miqaat.ui.AzaanScreen(AzaanService.Phase.Azaan(Prayer.DHUHR, false), {}, {}) }
            shot("iqamah", d, t) { _, _ -> com.usman.miqaat.ui.AzaanScreen(AzaanService.Phase.IqamahCountdown(Prayer.DHUHR, System.currentTimeMillis(), System.currentTimeMillis() + 300_000), {}, {}) }
            shot("hadith", d, t) { _, _ -> com.usman.miqaat.ui.AzaanScreen(AzaanService.Phase.HadithPhase(Prayer.DHUHR, HadithLibrary.all.first(), System.currentTimeMillis(), System.currentTimeMillis() + 60_000, false), {}, {}) }
        }
        // 2) large text on the main screens
        for (t in themes) for (d in listOf(Dev.PHONE_PORTRAIT, Dev.TABLET_LANDSCAPE)) for (sc in listOf(1.3f, 2f)) {
            shot("home", d, t, sc) { s, st -> HomeRouter(st, s, actions, showLarge = false) {} }
            shot("settings", d, t, sc) { s, _ -> settingsUi(s, null) }
            shot("timetable", d, t, sc) { s, _ -> TimetableScreen(s) {} }
            shot("adhkar-morning", d, t, sc) { _, _ -> AdhkarScreen(AdhkarMode.MORNING) {} }
        }
        // 3) Urdu / right-to-left
        for (t in themes) for (d in listOf(Dev.PHONE_PORTRAIT, Dev.TABLET_LANDSCAPE)) {
            shot("home", d, t, rtl = true) { s, st -> HomeRouter(st, s, actions, showLarge = false) {} }
            shot("settings", d, t, rtl = true) { s, _ -> settingsUi(s, null) }
            shot("timetable", d, t, rtl = true) { s, _ -> TimetableScreen(s) {} }
        }
        // 4) states: readiness variants and the theme picker
        for (t in themes) for (d in listOf(Dev.PHONE_PORTRAIT, Dev.TABLET_LANDSCAPE)) {
            val ok = ReadinessState("Dhuhr · 12:24 PM", "Sydney", listOf(ReadinessCheck("Exact alarms", "On", true, null), ReadinessCheck("Battery", "Unrestricted", true, null), ReadinessCheck("Notifications", "Allowed", true, null)))
            val warn = ReadinessState("Asr · 3:52 PM", "Sydney", listOf(ReadinessCheck("Exact alarms", "Off", false) {}, ReadinessCheck("Battery", "Restricted", false) {}, ReadinessCheck("Notifications", "Allowed", true, null)))
            shot("state-ready", d, t) { _, _ -> androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding16()) { ReadinessSummary(ok, wide = d.q.startsWith("w1280") || d.q.startsWith("w800"), onOpen = {}) } }
            shot("state-warning", d, t) { _, _ -> androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding16()) { ReadinessSummary(warn, wide = d.q.startsWith("w1280") || d.q.startsWith("w800"), onOpen = {}) } }
            shot("component-board", d, t) { _, _ -> ComponentBoard() }
            shot("theme-picker", d, t) { s, _ -> androidx.compose.foundation.layout.Box(androidx.compose.ui.Modifier.padding16()) { ThemePicker(s.theme) {} } }
        }
        if (errors.isNotEmpty()) File(out, "_errors.txt").writeText(errors.joinToString("\n"))
    }
}

private fun androidx.compose.ui.Modifier.padding16() = this.then(androidx.compose.ui.Modifier.padding(androidx.compose.ui.unit.Dp(16f)))

/** Every shared component in one column, for visual review per theme. */
@Composable
private fun ComponentBoard() {
    val tk = screenTokens()
    androidx.compose.foundation.layout.Column(
        androidx.compose.ui.Modifier.padding16().let { it.then(androidx.compose.ui.Modifier.fillMaxSize()) }.then(androidx.compose.ui.Modifier.background(tk.background)),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Space.m)
    ) {
        SectionHeading("Buttons and chips")
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Space.s)) {
            MiqButton("Primary", {}); MiqButton("Secondary", {}, kind = ButtonKind.Secondary); MiqTextButton("Text", {})
        }
        androidx.compose.foundation.layout.Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Space.s)) { MiqChip("Selected", true, {}); MiqChip("Unselected", false, {}) }
        MiqSearchField("", {}, "Search settings", "Clear")
        StatusNotice(NoticeKind.Success, "Success", body = "Saved")
        StatusNotice(NoticeKind.Warning, "Warning", body = "Needs attention", actionLabel = "Fix", onAction = {})
        StatusNotice(NoticeKind.Error, "Error", body = "Something failed")
        GroupCard {
            DestinationRow(androidx.compose.material.icons.Icons.Outlined.Search, "Destination row", "Subtitle text", {})
            RailItem(androidx.compose.material.icons.Icons.Outlined.Search, "Rail item selected", true, {})
        }
    }
}
