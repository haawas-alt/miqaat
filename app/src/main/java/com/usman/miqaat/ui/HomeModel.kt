package com.usman.miqaat.ui

import com.usman.miqaat.R
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState
import java.time.Duration

/**
 * Live, already-formatted facts for the two new homes. Everything on screen comes from here, and every string is built
 * by the same engine and L10n calls the Miqaat and Kiswah homes use, so nothing on the new homes can disagree with them.
 */
internal data class RowInfo(
    val prayer: Prayer, val label: String, val arabic: String, val clock: String, val suffix: String, val relative: String,
    val small: String, val isNow: Boolean, val isNext: Boolean, val done: Boolean, val azaanOn: Boolean, val spoken: String
)

internal fun rowInfo(p: Prayer, state: PrayerState, s: AppSettings): RowInfo {
    val t = state.today[p]
    val urdu = L10n.isUrdu(s)
    val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && s.jumuahEnabled
    val label = if (isFri && p == Prayer.DHUHR) L10n.word(s, "Jumuʿah") else L10n.prayer(s, p)
    val passed = !t.isAfter(state.now)
    val isNow = p == state.hero && state.justPassed
    val isNext = p == state.next && !state.justPassed
    val done = passed && p.isPrayer && !isNow
    val iq = PrayerEngine.iqamah(s, state.today, p)
    val endT = if (s.showEndTimes) PrayerEngine.endOf(s, state.today, p) else null
    val clock = PrayerEngine.clock(t, s.use24h)
    val suffix = PrayerEngine.suffix(t, s.use24h)
    val rel = L10n.relative(s, t, state.now)
    val small = listOfNotNull(
        endT?.let { "${L10n.word(s, "ends")} ${PrayerEngine.clock(it, s.use24h)}" },
        iq?.let { "${if (urdu) L10n.word(s, "Iqamah") else "iq"} ${PrayerEngine.clock(it, s.use24h)}" },
        if (p == Prayer.SUNRISE) "ḍuḥā from ${PrayerEngine.clock(t.plusMinutes(15), s.use24h)}" else null
    ).joinToString("  ·  ")
    val azaanOn = s.azaanEnabled[p] == true
    val spoken = buildString {
        append(label); append(", "); append(clock); append(' '); append(suffix); append(", "); append(rel)
        when { isNow -> append(", now"); isNext -> append(", next prayer"); done -> append(", passed") }
        if (iq != null) { append(", iqamah "); append(PrayerEngine.clock(iq, s.use24h)) }
        if (endT != null) { append(", ends "); append(PrayerEngine.clock(endT, s.use24h)) }
        if (!azaanOn && p.isPrayer) append(", azaan off")
    }
    return RowInfo(p, label, p.arabic, clock, suffix, rel, small, isNow, isNext, done, azaanOn, spoken)
}

internal data class HeroInfo(
    val kickerLabel: String, val special: String?, val label: String, val arabic: String, val clock: String, val suffix: String,
    val status: String, val justPassed: Boolean, val spoken: String, val prayer: Prayer
)

internal fun heroInfo(state: PrayerState, s: AppSettings, special: String?): HeroInfo {
    val isFri = state.now.dayOfWeek == java.time.DayOfWeek.FRIDAY && s.jumuahEnabled
    val label = if (isFri && state.hero == Prayer.DHUHR) L10n.word(s, "Jumuʿah") else L10n.prayer(s, state.hero)
    val clock = PrayerEngine.clock(state.heroTime, s.use24h)
    val suffix = PrayerEngine.suffix(state.heroTime, s.use24h)
    val status = buildString {
        append(if (state.justPassed) L10n.ago(s, state.delta).replaceFirstChar { it.uppercase() } else L10n.inFor(s, state.delta).replaceFirstChar { it.uppercase() })
        state.current?.let { cur -> PrayerEngine.iqamah(s, state.today, cur)?.takeIf { it.isAfter(state.now) }?.let { iq -> append("  ·  ${L10n.word(s, "Iqamah")} ${PrayerEngine.clock(iq, s.use24h)}") } }
    }
    val kicker = Str[if (state.justPassed) R.string.s_home_prayer_now else R.string.s_home_next_prayer]
    val spoken = "${special?.let { "$it. " } ?: ""}$kicker. $label, $clock $suffix, $status"
    return HeroInfo(kicker, special, label, state.hero.arabic, clock, suffix, status, state.justPassed, spoken, state.hero)
}

/** Progress of the fast in Ramaḍān, 0..1 between Fajr and Maghrib, or null when it is not a fasting-day daytime. */
internal fun fastProgress(state: PrayerState, s: AppSettings): Pair<Float, String>? {
    if (!PrayerEngine.isRamadan(s, state.now.toLocalDate())) return null
    val cur = state.current ?: return null
    if (cur == Prayer.MAGHRIB || cur == Prayer.ISHA) return null
    val start = state.today[Prayer.FAJR]; val end = state.today[Prayer.MAGHRIB]
    val total = Duration.between(start, end).toMinutes().coerceAtLeast(1)
    val done = Duration.between(start, state.now).toMinutes().coerceIn(0, total)
    return (done / total.toFloat()) to "Iftar in ${PrayerEngine.humanDuration(Duration.between(state.now, end))}"
}

/** Ramaḍān evening line: tarāwīḥ and the last third of the night. */
internal fun tarawihLine(state: PrayerState, s: AppSettings): String? {
    if (!PrayerEngine.isRamadan(s, state.now.toLocalDate())) return null
    if (!(state.current == Prayer.ISHA || state.current == Prayer.MAGHRIB || state.current == null)) return null
    val lt = PrayerEngine.lastThird(s, state.today)
    return "Tarāwīḥ ${PrayerEngine.clock(state.today[Prayer.ISHA].plusMinutes(s.tarawihMinutesAfterIsha.toLong()), s.use24h)}  ·  last third of the night from ${PrayerEngine.clock(lt, s.use24h)} ${PrayerEngine.suffix(lt, s.use24h)}".trim()
}

/** The prayers a home lists: with or without Sunrise, exactly as the existing homes decide. */
internal fun listedPrayers(s: AppSettings): List<Prayer> = if (s.showSunrise) Prayer.entries else Prayer.prayersOnly
