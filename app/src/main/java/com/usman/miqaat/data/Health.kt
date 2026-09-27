package com.usman.miqaat.data

import android.content.Context
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * A small on-device log of what actually happened: azaans played, iqamahs, reminders, reschedules,
 * time changes. Kept to the last 200 entries in SharedPreferences; no network.
 */
object Health {
    enum class Kind { AZAAN, IQAMAH, REMINDER, SCHEDULED, TIME_CHANGE, BOOT, MISSED, INFO }
    data class Entry(val at: Long, val kind: Kind, val title: String, val detail: String, val plannedAt: Long = 0L) {
        val lateBy: Long get() = if (plannedAt > 0) (at - plannedAt) / 60_000 else 0
        fun time(zone: ZoneId): ZonedDateTime = Instant.ofEpochMilli(at).atZone(zone)
    }

    private const val PREF = "miqaat_health"
    private const val MAX = 200

    fun log(ctx: Context, kind: Kind, title: String, detail: String = "", plannedAt: Long = 0L) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val list = read(ctx).toMutableList()
        list.add(0, Entry(System.currentTimeMillis(), kind, title, detail, plannedAt))
        while (list.size > MAX) list.removeAt(list.size - 1)
        p.edit().putString("log", list.joinToString("\n") { listOf(it.at, it.kind.name, esc(it.title), esc(it.detail), it.plannedAt).joinToString("\t") }).apply()
    }

    fun read(ctx: Context): List<Entry> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("log", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.lines().mapNotNull { l ->
            val f = l.split('\t'); if (f.size < 5) return@mapNotNull null
            runCatching { Entry(f[0].toLong(), Kind.valueOf(f[1]), unesc(f[2]), unesc(f[3]), f[4].toLong()) }.getOrNull()
        }
    }

    fun clear(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().remove("log").apply()

    /** Remember the last planned event so a boot or time change can tell whether it was missed. */
    fun setPlanned(ctx: Context, whenMs: Long, label: String) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putLong("plannedAt", whenMs).putString("plannedLabel", label).apply()

    fun checkMissed(ctx: Context, reason: String) {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val at = p.getLong("plannedAt", 0L); val label = p.getString("plannedLabel", "") ?: ""
        if (at in 1 until System.currentTimeMillis() - 90_000 && p.getLong("firedAt", 0L) < at) {
            log(ctx, Kind.MISSED, "$label not played", "$reason. It was due at " + Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0), at)
            p.edit().putLong("plannedAt", 0L).apply()
        }
    }

    fun markFired(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putLong("firedAt", System.currentTimeMillis()).apply()

    private fun esc(s: String) = s.replace("\t", " ").replace("\n", " ")
    private fun unesc(s: String) = s

    // ---- backup -------------------------------------------------------

    /** Every setting as key=value lines; SharedPreferences types are restored by name. */
    fun exportSettings(ctx: Context): String {
        val p = ctx.getSharedPreferences("miqaat", Context.MODE_PRIVATE)
        val sb = StringBuilder("# Miqaat settings backup · ${ZonedDateTime.now()}\n")
        p.all.toSortedMap().forEach { (k, v) ->
            val t = when (v) { is Boolean -> "b"; is Int -> "i"; is Float -> "f"; is Long -> "l"; else -> "s" }
            sb.append(t).append(':').append(k).append('=').append(v.toString().replace("\n", "\\n")).append('\n')
        }
        return sb.toString()
    }

    fun importSettings(ctx: Context, text: String): Int {
        val p = ctx.getSharedPreferences("miqaat", Context.MODE_PRIVATE).edit()
        var n = 0
        text.lines().forEach { l ->
            if (l.startsWith("#") || l.isBlank() || l.length < 3 || l[1] != ':') return@forEach
            val t = l[0]; val eq = l.indexOf('='); if (eq < 2) return@forEach
            val k = l.substring(2, eq); val v = l.substring(eq + 1).replace("\\n", "\n")
            runCatching {
                when (t) { 'b' -> p.putBoolean(k, v.toBoolean()); 'i' -> p.putInt(k, v.toInt()); 'f' -> p.putFloat(k, v.toFloat()); 'l' -> p.putLong(k, v.toLong()); else -> p.putString(k, v) }
                n++
            }
        }
        p.apply()
        return n
    }
}
