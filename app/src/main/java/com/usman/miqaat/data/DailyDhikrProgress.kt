package com.usman.miqaat.data

import android.content.Context
import java.time.LocalDate

/** One on-device daily total for tahlil, shared by morning and evening. Uses the device's civil date.
 * Session-specific adhkar are intentionally not merged into this daily total.
 */
class DailyDhikrProgress(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("miqaat_daily_tahlil", Context.MODE_PRIVATE)
    fun count(day: LocalDate = LocalDate.now()): Int = synchronized(lock) {
        if (prefs.getString("day", null) == day.toString()) prefs.getInt("count", 0).coerceIn(0, 100) else 0
    }
    fun increment(day: LocalDate = LocalDate.now()): Int = synchronized(lock) {
        val next = (count(day) + 1).coerceAtMost(100)
        prefs.edit().putString("day", day.toString()).putInt("count", next).apply()
        next
    }
    private companion object { val lock = Any() }
}
