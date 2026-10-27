package com.usman.miqaat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.usman.miqaat.data.*
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/** Display-only inputs. Never written to SettingsStore or handed to the alarm scheduler. */
internal fun jummahPreviewSettings(saved: AppSettings) = saved.copy(
    fridayReminders = true,
    jumuahEnabled = true,
    jumuahMinutes = if (saved.jumuahEnabled) saved.jumuahMinutes else 13 * 60 + 15
)

internal fun jummahPreviewTime(s: AppSettings, now: ZonedDateTime): ZonedDateTime {
    val friday = now.withZoneSameInstant(s.zone()).toLocalDate()
        .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
    // Show the lead-up to the actual configured Jumuah time using the normal engine.
    return PrayerEngine.times(s, friday, s.zone())[Prayer.DHUHR].minusMinutes(60)
}

@Composable
internal fun JummahHomepagePreview(saved: AppSettings, onClose: () -> Unit) {
    var destination by rememberSaveable { mutableStateOf("home") }
    var relative by rememberSaveable { mutableStateOf(saved.showRelative) }
    var mode by remember { mutableStateOf(AdhkarMode.MORNING) }
    val s = remember(saved, relative) { jummahPreviewSettings(saved).copy(showRelative = relative) }
    val now = remember(s) { jummahPreviewTime(s, ZonedDateTime.now(s.zone())) }
    val state = remember(s, now) { PrayerEngine.state(s, now) }
    val back = { destination = "home" }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(
        usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        BackHandler { if (destination == "home") onClose() else back() }
        when (destination) {
            "qibla" -> QiblaScreen(saved, onBack = back)
            "adhkar" -> AdhkarScreen(mode, onBack = back)
            "friday" -> FridayScreen(saved, onBack = back)
            "learn" -> LearnScreen(saved, onBack = back)
            "timetable" -> TimetableScreen(saved, onBack = back)
            else -> FridayApprovedHome(state, s, HomeActions(
                onOpenTimetable = { destination = "timetable" },
                onOpenSettings = onClose, onOpenLocation = onClose,
                onOpenQibla = { destination = "qibla" },
                onOpenAdhkar = { mode = it; destination = "adhkar" },
                onOpenFriday = { destination = "friday" },
                onOpenLearn = { destination = "learn" },
                updateAvailable = false, onOpenAbout = onClose,
                onToggleRelative = { relative = !relative }
            ), onClosePreview = onClose)
        }
    }
}
