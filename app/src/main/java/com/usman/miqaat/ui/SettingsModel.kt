package com.usman.miqaat.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanScheduler
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.Reliability
import java.time.Duration

/* ───────────────────────────── search index ───────────────────────────── */

/** One searchable thing: a destination, or a visible setting inside one. `synonyms` are deliberate extra keywords. */
data class SettingEntry(val section: Section, val title: String, val synonyms: String, val isDestination: Boolean)

/**
 * Declarative index of every destination and visible setting title. Search never inspects feature code: adding a setting
 * means adding one line here (its title resource must be the text shown on the row so the row can be scrolled to).
 */
object SettingsIndex {
    private class Spec(val section: Section, val syn: String, val titles: List<Int>)

    private val specs = listOf(
        Spec(Section.LOCATION, "city suburb place gps timezone masjid mosque timetable csv travel",
            listOf(R.string.s_current_location, R.string.s_use_the_tablet_s_location, R.string.s_traveller_mode, R.string.s_time_zone_for_prayer_times, R.string.s_use_masjid_times, R.string.s_import_file)),
        Spec(Section.TIMES, "method hanafi shafi juristic ramadan fasting suhoor friday jumuah latitude sunrise",
            listOf(R.string.s_calculation_method, R.string.s_asr_juristic_method, R.string.s_high_latitude_rule, R.string.s_rama_n_mode, R.string.s_suhoor_alarm, R.string.s_friday_reminders,
                R.string.s_hour_of_acceptance_reminder, R.string.s_jumu_ah_azaan_time, R.string.s_show_end_times, R.string.s_show_sunrise_on_the_home_screen, R.string.s_show_disliked_times_for_voluntary_prayer)),
        Spec(Section.AZAAN, "alarm adhan azan sound loud audio recording notification reminder narration",
            listOf(R.string.s_volume, R.string.s_speaker_boost, R.string.s_azaan_file, R.string.s_fajr_azaan_file, R.string.s_reminder_before_azaan, R.string.s_dua_and_hadith_after_each_azaan, R.string.s_hadith_source, R.string.s_narration)),
        Spec(Section.IQAMAH, "congregation countdown quiet sound",
            listOf(R.string.s_countdown_before_iqamah, R.string.s_iqamah_times, R.string.s_jumu_ah_iqamah_fixed_time, R.string.s_quiet_screen_after_iqamah, R.string.s_sound_at_iqamah)),
        Spec(Section.HIJRI, "islamic date moon sighting calendar offset month",
            listOf(R.string.s_show_hijri_date, R.string.s_adjustment)),
        Spec(Section.DISPLAY, "appearance theme dark light font size text clock 24 hour widget boot",
            listOf(R.string.s_art_theme, R.string.s_language, R.string.s_time_format, R.string.s_keep_the_screen_on, R.string.s_dim_after_isha, R.string.s_large_type, R.string.s_qibla_direction_on_the_home_screen,
                R.string.s_learn_salah, R.string.s_morning_and_evening_adhk_r, R.string.s_after_prayer_adhk_r, R.string.s_home_screen_widget, R.string.s_open_miqaat_when_the_device_starts)),
        Spec(Section.TEST, "test preview demo try",
            listOf(R.string.s_azaan_recording, R.string.s_short_countdown, R.string.s_quiet_screen, R.string.s_iqamah_sound_only, R.string.s_full_sequence_after_azaan, R.string.s_play_one_hadith, R.string.s_rama_n_maghrib_sequence)),
        Spec(Section.HEALTH, "backup export import restore reliability alarm permission exact battery log",
            listOf(R.string.s_battery_optimisation, R.string.s_next_alarm_armed, R.string.s_launch_on_boot, R.string.s_time_change_self_check, R.string.s_settings_file)),
        Spec(Section.PRIVACY, "data tracking analytics advertising permissions source",
            listOf(R.string.s_permissions, R.string.s_android_backup, R.string.s_place_name_search, R.string.s_source_code)),
        Spec(Section.ABOUT, "version update updates content sources correction recitation",
            listOf(R.string.s_check_for_updates, R.string.s_content_sources, R.string.s_report_a_content_correction, R.string.s_learn_recitation))
    )

    /** Builds the index in the current language. */
    fun build(resolve: (Int) -> String = { Str[it] }): List<SettingEntry> = specs.flatMap { sp ->
        listOf(SettingEntry(sp.section, resolve(sp.section.labelRes), sp.syn, true)) + sp.titles.map { SettingEntry(sp.section, resolve(it), sp.syn, false) }
    }

    /**
     * Every whitespace-separated token of the query must appear in the entry's title, its category label or its synonyms.
     * Ranked: title starts with query → title contains → category/synonym match; destinations before settings on ties.
     */
    fun search(index: List<SettingEntry>, query: String, categoryLabel: (Section) -> String = { it.label }): List<SettingEntry> {
        val toks = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (toks.isEmpty()) return emptyList()
        val q = toks.joinToString(" ")
        return index.mapNotNull { e ->
            val title = e.title.lowercase(); val hay = title + " " + categoryLabel(e.section).lowercase() + " " + e.synonyms
            if (!toks.all { hay.contains(it) }) return@mapNotNull null
            val rank = when { title.startsWith(q) -> 0; toks.all { title.contains(it) } -> 1; else -> 2 }
            Triple(e, rank, if (e.isDestination) 0 else 1)
        }.sortedWith(compareBy({ it.second }, { it.third })).map { it.first }.distinctBy { it.section to it.title }
    }
}

/** Set by the search route so a `SettingRow` with this exact title scrolls into view and shows a highlight. */
val LocalSettingsFocus = compositionLocalOf<String?> { null }

/* ───────────────────────────── readiness ───────────────────────────── */

data class ReadinessCheck(val label: String, val stateText: String, val ok: Boolean, val fix: (() -> Unit)?)

/** Everything the summary shows. Built from live scheduler/reliability state; nothing is hard-coded. */
data class ReadinessState(val eventTitle: String?, val location: String?, val checks: List<ReadinessCheck>) {
    val allOk: Boolean get() = eventTitle != null && checks.all { it.ok }
    /** One spoken sentence for TalkBack. */
    fun spoken(header: String): String = buildString {
        append(header); if (eventTitle != null) append(", ").append(eventTitle); if (location != null) append(", ").append(location)
        checks.forEach { append(". ").append(it.label).append(": ").append(it.stateText).append(if (it.ok) "" else ", " + Str[R.string.s_needs_attention_word]) }
    }
}

/** Pure formatter for the "next event" line so it can be unit-tested without Android. */
fun eventTitleText(prayerName: String, clock: String, suffix: String, kind: EventKind, dayLabel: String?): String {
    val base = when (kind) { EventKind.AZAAN -> prayerName; EventKind.REMINDER -> Str.get(R.string.s_ready_reminder, prayerName); EventKind.IQAMAH -> Str.get(R.string.s_ready_iqamah, prayerName) }
    val time = "$clock $suffix".trim().let { if (dayLabel != null) dayLabel.format(it) else it }
    return "$base · $time"
}

enum class EventKind { AZAAN, REMINDER, IQAMAH }

@Composable
fun rememberReadiness(settings: AppSettings): ReadinessState {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) { tick++; onPauseOrDispose { } }
    return remember(tick, settings, Str.res) { computeReadiness(ctx, settings) }
}

fun computeReadiness(ctx: Context, s: AppSettings): ReadinessState {
    val up = runCatching { AzaanScheduler.nextEvent(ctx) }.getOrNull()
    val title = up?.let { e ->
        val z = e.at.withZoneSameInstant(s.zone())
        val today = java.time.LocalDate.now(s.zone())
        val dayLabel = if (z.toLocalDate() != today) Str.get(R.string.s_ready_tomorrow, "%s") else null
        eventTitleText(L10n.prayer(s, e.prayer), PrayerEngine.clock(z, s.use24h), PrayerEngine.suffix(z, s.use24h),
            if (e.iqamah) EventKind.IQAMAH else if (e.reminder) EventKind.REMINDER else EventKind.AZAAN, dayLabel)
    }
    val exact = Reliability.exactAlarmsGranted(ctx); val notif = Reliability.notificationsGranted(ctx); val batt = Reliability.batteryExempt(ctx)
    val checks = listOf(
        ReadinessCheck(Str[R.string.s_check_exact], if (exact) Str[R.string.s_state_on] else Str[R.string.s_state_off], exact, if (exact) null else { { Reliability.openExactAlarmSettings(ctx) } }),
        ReadinessCheck(Str[R.string.s_check_battery], if (batt) Str[R.string.s_state_unrestricted] else Str[R.string.s_state_restricted], batt, if (batt) null else { { Reliability.openBatterySettings(ctx) } }),
        ReadinessCheck(Str[R.string.s_check_notifications], if (notif) Str[R.string.s_state_allowed] else Str[R.string.s_state_blocked], notif, if (notif) null else { { Reliability.openNotificationSettings(ctx) } })
    )
    val loc = if (com.usman.miqaat.data.Setup.ready(s) && s.locationName.isNotBlank()) s.locationName else null
    return ReadinessState(title, loc ?: Str[R.string.s_ready_location_unset], checks)
}

/**
 * The reusable summary. `wide` lays the three checks beside the headline (tablet); otherwise they sit under it (phone),
 * and stack vertically when the system font is large. The headline area opens Reliability & backup.
 */
@Composable
fun ReadinessSummary(state: ReadinessState, wide: Boolean, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    val tk = screenTokens()
    val header = if (state.allOk) Str[R.string.s_ready_ok] else if (state.eventTitle == null) Str[R.string.s_ready_none] else Str[R.string.s_ready_attention]
    val shape = RoundedCornerShape(tk.cornerMedium)
    val big = LocalDensity.current.fontScale > 1.3f
    val headline: @Composable (Modifier) -> Unit = { m ->
        Row(m.clickable(role = Role.Button, onClickLabel = Str[R.string.s_readiness_open], onClick = onOpen).semantics(mergeDescendants = true) { contentDescription = state.spoken(header) }.padding(Space.l), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(if (state.allOk) tk.selectedSurface else containerOf(tk.warning, tk)), contentAlignment = Alignment.Center) {
                Icon(if (state.allOk) Icons.Outlined.Schedule else Icons.Outlined.WarningAmber, null, Modifier.size(26.dp), tint = if (state.allOk) tk.primary else tk.warning)
            }
            Spacer(Modifier.width(Space.l))
            Column(Modifier.weight(1f)) {
                Text(header, fontFamily = Nunito, fontSize = 14.sp, color = tk.contentSecondary)
                Text(state.eventTitle ?: Str[R.string.s_ready_none_action], fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 28.sp, color = tk.contentPrimary, maxLines = 2)
                state.location?.let { Text(it, fontFamily = Nunito, fontSize = 14.sp, color = tk.contentSecondary) }
            }
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, Modifier.size(24.dp), tint = tk.contentSecondary)
        }
    }
    val checkItem: @Composable (ReadinessCheck, Modifier, Boolean) -> Unit = { c, m, stacked ->
        val kind = if (c.ok) NoticeKind.Success else NoticeKind.Warning
        val col = noticeColor(kind, tk)
        val base = m.heightIn(min = Space.target).then(if (c.fix != null) Modifier.clickable(role = Role.Button, onClickLabel = Str[R.string.s_readiness_fix], onClick = c.fix) else Modifier)
            .semantics(mergeDescendants = true) { stateDescription = c.stateText; contentDescription = c.label + ", " + c.stateText }.padding(horizontal = Space.s, vertical = Space.s)
        val texts: @Composable (Alignment.Horizontal) -> Unit = { h ->
            Column(horizontalAlignment = h) {
                Text(c.label, fontFamily = Nunito, fontSize = if (stacked) 13.sp else 14.sp, fontWeight = FontWeight.SemiBold, color = tk.contentPrimary, textAlign = if (stacked) TextAlign.Center else TextAlign.Start)
                Text(c.stateText + if (!c.ok && c.fix != null) " · " + Str[R.string.s_readiness_fix] else "", fontFamily = Nunito, fontSize = if (stacked) 12.sp else 14.sp, color = tk.contentSecondary, textAlign = if (stacked) TextAlign.Center else TextAlign.Start)
            }
        }
        if (stacked) Column(base, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(noticeIcon(kind), null, Modifier.size(24.dp), tint = col)
            Spacer(Modifier.height(Space.xs))
            texts(Alignment.CenterHorizontally)
        } else Row(base, verticalAlignment = Alignment.CenterVertically) {
            Icon(noticeIcon(kind), null, Modifier.size(24.dp), tint = col)
            Spacer(Modifier.width(Space.s))
            texts(Alignment.Start)
        }
    }
    Column(modifier.fillMaxWidth().clip(shape).background(tk.surface).border(1.dp, tk.divider, shape)) {
        if (wide && !big) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                headline(Modifier.weight(1.7f))
                Box(Modifier.width(1.dp).height(56.dp).background(tk.divider))
                state.checks.forEach { c -> checkItem(c, Modifier.weight(1f), false) }
            }
        } else {
            headline(Modifier)
            Box(Modifier.padding(horizontal = Space.l).fillMaxWidth().height(1.dp).background(tk.divider))
            if (big) Column(Modifier.padding(Space.s)) { state.checks.forEach { c -> checkItem(c, Modifier.fillMaxWidth(), false) } }
            else Row(Modifier.padding(horizontal = Space.xs, vertical = Space.xs)) { state.checks.forEach { c -> checkItem(c, Modifier.weight(1f), true) } }
        }
    }
}
