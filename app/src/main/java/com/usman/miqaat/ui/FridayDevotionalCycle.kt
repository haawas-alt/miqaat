package com.usman.miqaat.ui

import androidx.compose.runtime.compositionLocalOf
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import java.time.DayOfWeek
import java.time.ZonedDateTime

/** MainActivity disables the foreground timer while another screen or an azaan owns focus. */
internal val LocalFridayDevotionalAllowed = compositionLocalOf { true }

/** A foreground-only clock. It never changes prayer, alarm, or persisted theme state. */
internal class FridayDevotionalCycle(
    private val homeMillis: Long = 300_000L,
    private val displayMillis: Long = 30_000L,
) {
    init {
        require(homeMillis > 0 && displayMillis > 0)
    }

    var design: Int? = null
        private set
    var nextDesign: Int = 1
        private set
    var remainingMillis: Long = homeMillis
        private set

    fun advance(elapsedMillis: Long): Int? {
        var elapsed = elapsedMillis.coerceAtLeast(0L)
        // A delayed foreground callback must not replay missed devotional screens.
        elapsed = elapsed.coerceAtMost(homeMillis + displayMillis)
        while (elapsed >= remainingMillis) {
            elapsed -= remainingMillis
            if (design == null) {
                design = nextDesign
                nextDesign = nextDesign % 4 + 1
                remainingMillis = displayMillis
            } else {
                design = null
                remainingMillis = homeMillis
            }
        }
        remainingMillis -= elapsed
        return design
    }

    fun dismiss() {
        design = null
        remainingMillis = homeMillis
    }

    /** An interruption restarts the home interval without replaying the same design. */
    fun interrupt() {
        design = null
        remainingMillis = homeMillis
    }

    fun reset() {
        design = null
        nextDesign = 1
        remainingMillis = homeMillis
    }
}

/** MA pilot only. KA and CA are enabled after the four-profile visual gate; GA never rotates. */
internal fun fridayDevotionalEligible(settings: AppSettings, now: ZonedDateTime): Boolean =
    settings.fridayReminders &&
        settings.theme == AppTheme.MIQAAT &&
        now.withZoneSameInstant(settings.zone()).dayOfWeek == DayOfWeek.FRIDAY
