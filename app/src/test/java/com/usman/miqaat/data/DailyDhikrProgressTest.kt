package com.usman.miqaat.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DailyDhikrProgressTest {
    private val ctx: Context get() = ApplicationProvider.getApplicationContext()
    private val day = LocalDate.of(2026, 10, 2)
    @Before fun clear() { ctx.getSharedPreferences("miqaat_daily_tahlil", Context.MODE_PRIVATE).edit().clear().commit() }
    @Test fun morningAndEveningShareOnePersistedTotalAfterReaderRecreation() {
        val morning = DailyDhikrProgress(ctx)
        repeat(40) { morning.increment(day) }
        val evening = DailyDhikrProgress(ctx)
        assertEquals(40, evening.count(day))
        repeat(60) { evening.increment(day) }
        assertEquals(100, DailyDhikrProgress(ctx).count(day))
    }
    @Test fun aNewCivilDayStartsAtZeroThenPersistsItsOwnCount() {
        val store = DailyDhikrProgress(ctx)
        repeat(100) { store.increment(day) }
        assertEquals(0, store.count(day.plusDays(1)))
        assertEquals(1, store.increment(day.plusDays(1)))
        assertEquals(1, DailyDhikrProgress(ctx).count(day.plusDays(1)))
    }
    @Test fun repeatedTapsAfterCompletionDoNotExceedTheDailyTarget() {
        val store = DailyDhikrProgress(ctx)
        repeat(110) { store.increment(day) }
        assertEquals(100, store.count(day))
    }
}
