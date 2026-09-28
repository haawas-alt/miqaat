package com.usman.miqaat.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.usman.miqaat.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.azaan.AzaanScheduler
import com.usman.miqaat.azaan.AzaanService
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.ArtTheme
import com.usman.miqaat.data.AsrMethod
import com.usman.miqaat.data.LatitudeRule
import com.usman.miqaat.data.LocationRepo
import com.usman.miqaat.data.Method
import com.usman.miqaat.data.Narration
import com.usman.miqaat.data.RamadanMode
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.Language
import com.usman.miqaat.data.IqamahSound
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.Lock
import com.usman.miqaat.data.HadithLibrary
import com.usman.miqaat.data.Place
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.SettingsStore
import com.usman.miqaat.data.Updater
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class Section(val labelRes: Int, val icon: ImageVector) {
    LOCATION(R.string.s_location, Icons.Outlined.LocationOn),
    TIMES(R.string.s_prayer_times, Icons.Outlined.Schedule),
    AZAAN(R.string.s_azaan_alerts, Icons.Outlined.NotificationsActive),
    IQAMAH(R.string.s_iqamah, Icons.Outlined.Timer),
    HIJRI(R.string.s_hijri_calendar, Icons.Outlined.CalendarMonth),
    DISPLAY(R.string.s_display_art, Icons.Outlined.Brush),
    TEST(R.string.s_test_preview, Icons.Outlined.PlayCircle),
    HEALTH(R.string.s_reliability_backup, Icons.Outlined.MonitorHeart),
    PRIVACY(R.string.s_privacy, Icons.Outlined.Lock),
    ABOUT(R.string.s_about, Icons.Outlined.Info);
    val label: String get() = Str[labelRes]
}

@Composable
fun SettingsScreen(store: SettingsStore, settings: AppSettings, initial: Section = Section.TIMES, onBack: () -> Unit) {
    var section by rememberSaveable { mutableStateOf(initial) }
    val ctx = LocalContext.current
    // Any change that affects times re-arms the alarm chain.
    // Re-arm alarms only when something that affects timing changes (not on every keystroke or slider frame).
    val timingKey = settings.copy(masjidName = "", azaanVolume = 0, locationName = "", theme = settings.theme, showRelative = false)
    LaunchedEffect(timingKey) { AzaanScheduler.reschedule(ctx) }

    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().background(Palette.panel)) {
    val compact = maxWidth < 720.dp
    if (compact) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = Palette.ivory) }
                Text(Str[R.string.s_settings], fontFamily = Cormorant, fontSize = 30.sp, color = Palette.ivory)
            }
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Section.entries.forEach { sec ->
                    val cur = sec == section
                    Box(Modifier.heightIn(min = 40.dp).clip(RoundedCornerShape(50)).background(if (cur) Palette.gold else Color.Transparent).border(1.dp, if (cur) Palette.gold else Palette.lineStrong, RoundedCornerShape(50)).selectable(selected = cur, role = androidx.compose.ui.semantics.Role.Tab) { section = sec }.padding(horizontal = 14.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                        Text(sec.label, fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (cur) Palette.night else Palette.ivory)
                    }
                }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding()) {
                when (section) {
                    Section.LOCATION -> LocationSection(store, settings)
                    Section.TIMES -> TimesSection(store, settings)
                    Section.AZAAN -> AzaanSection(store, settings)
                    Section.IQAMAH -> IqamahSection(store, settings)
                    Section.TEST -> TestSection(store, settings)
                    Section.HEALTH -> HealthSection(store, settings)
                    Section.PRIVACY -> PrivacySection()
                    Section.HIJRI -> HijriSection(store, settings)
                    Section.DISPLAY -> DisplaySection(store, settings)
                    Section.ABOUT -> AboutSection(settings)
                }
            }
        }
        return@BoxWithConstraints
    }
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.width(300.dp).fillMaxHeight().background(Color.Black.copy(alpha = 0.18f)).verticalScroll(rememberScrollState()).padding(vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp, bottom = 16.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = Palette.ivory) }
                Text(Str[R.string.s_settings], fontFamily = Cormorant, fontSize = 34.sp, color = Palette.ivory)
            }
            Section.entries.forEach { s ->
                val cur = s == section
                Row(
                    Modifier.fillMaxWidth().background(if (cur) Palette.gold.copy(alpha = 0.14f) else Color.Transparent)
                        .clickable { section = s }.padding(horizontal = 28.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cur) Box(Modifier.width(4.dp).height(22.dp).background(Palette.gold)) else Spacer(Modifier.width(4.dp))
                    Spacer(Modifier.width(14.dp))
                    Icon(s.icon, null, Modifier.size(20.dp), tint = Palette.ivory.copy(alpha = if (cur) 1f else 0.75f))
                    Spacer(Modifier.width(12.dp))
                    Text(s.label, fontFamily = Nunito, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory.copy(alpha = if (cur) 1f else 0.75f))
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = 34.dp, vertical = 26.dp)) {
            when (section) {
                Section.LOCATION -> LocationSection(store, settings)
                Section.TIMES -> TimesSection(store, settings)
                Section.AZAAN -> AzaanSection(store, settings)
                Section.IQAMAH -> IqamahSection(store, settings)
                Section.TEST -> TestSection(store, settings)
                Section.HEALTH -> HealthSection(store, settings)
                Section.PRIVACY -> PrivacySection()
                Section.HIJRI -> HijriSection(store, settings)
                Section.DISPLAY -> DisplaySection(store, settings)
                Section.ABOUT -> AboutSection(settings)
            }
        }
    }
    }
}

// ---------------------------------------------------------------- sections

@Composable
private fun LocationSection(store: SettingsStore, s: AppSettings) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted.values.any { it }) scope.launch { busy = true; status = detect(ctx, store); busy = false }
        else status = Str[R.string.s_location_permission_was_not_granted_choose]
    }
    fun detectNow() {
        if (LocationRepo.hasPermission(ctx)) scope.launch { busy = true; status = detect(ctx, store); busy = false }
        else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    Heading(Str[R.string.s_location], Str[R.string.s_prayer_times_are_calculated_for_these])
    SettingRow("Current location", if (s.locationSet) "%.4f, %.4f".format(s.latitude, s.longitude) else "Not set yet — detect it or choose a place below") { GoldValue(if (s.locationSet) s.locationName else "—") }
    var pickZone by remember { mutableStateOf(false) }
    val zoneWarn = s.locationSet && (s.zoneNeedsReview || com.usman.miqaat.data.Setup.zoneLooksWrong(s.longitude, s.zone()))
    SettingRow(Str[R.string.s_time_zone_for_prayer_times], if (zoneWarn) Str[R.string.s_this_zone_is_several_hours_away] else if (s.zoneManual) Str[R.string.s_chosen_by_you_automatic_location_refresh] else Str[R.string.s_follows_the_device_while_that_is], onClick = { pickZone = true }) {
        GoldValue((s.zoneId ?: "Device · ${java.time.ZoneId.systemDefault().id}") + " ›")
    }
    if (pickZone) ZonePicker(current = s.zoneId, onPick = { pickZone = false }, onDismiss = { pickZone = false }, store = store)
    SettingRow(Str[R.string.s_use_the_tablet_s_location], Str[R.string.s_re_detects_each_time_the_app]) {
        Toggle(s.autoLocation) { on -> store.update { it.copy(autoLocation = on) }; if (on) detectNow() }
    }
    Row(Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GoldButton(if (busy) "Detecting…" else Str[R.string.s_detect_now], enabled = !busy) { detectNow() }
    }
    if (status.isNotEmpty()) Text(status, fontFamily = Nunito, fontSize = 14.sp, color = Palette.goldSoft, modifier = Modifier.padding(bottom = 8.dp))

    Spacer(Modifier.height(10.dp))
    Text(Str[R.string.s_or_choose_a_place], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    OutlinedTextField(
        value = query, onValueChange = { query = it; scope.launch { results = LocationRepo.search(ctx, it) } },
        placeholder = { Text(Str[R.string.s_search_a_suburb_or_city]) }, singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
    )
    (if (query.isBlank()) LocationRepo.presets else results).take(10).forEach { p ->
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .clickable { store.update { it.copy(latitude = p.lat, longitude = p.lng, locationName = p.name, locationSet = true, autoLocation = false, zoneId = p.zone, zoneManual = false) }; AzaanScheduler.reschedule(ctx); status = "Set to ${p.name}" + (if (p.zone == null) Str[R.string.s_times_shown_in_the_device_s] else "") }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(p.name, fontFamily = Nunito, fontSize = 16.sp, color = Palette.ivory)
            Text("%.2f, %.2f".format(p.lat, p.lng), fontFamily = Nunito, fontSize = 14.sp, color = Palette.textMuted)
        }
        HorizontalDivider(color = Palette.line)
    }

    // ---- Traveller
    Spacer(Modifier.height(22.dp))
    Text(Str[R.string.s_travelling], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    val homeSet = s.homeLat != null && s.homeLng != null
    val dist = if (homeSet) PrayerEngine.distanceKm(s.homeLat!!, s.homeLng!!, s.latitude, s.longitude) else 0.0
    SettingRow("Home", if (homeSet) "%.0f km from the current location".format(dist) else "Not set. Detect your location at home once, or set it now.") {
        TextButton(onClick = { store.update { it.copy(homeLat = it.latitude, homeLng = it.longitude) } }) { Text(if (homeSet) Str[R.string.s_set_home_to_here] else Str[R.string.s_set_home], color = Palette.goldSoft) }
    }
    SettingRow(Str[R.string.s_traveller_mode], Str[R.string.s_when_you_are_80_km_or]) { Toggle(s.travellerMode) { on -> store.update { it.copy(travellerMode = on) } } }

    // ---- Masjid timetable
    Spacer(Modifier.height(22.dp))
    Text(Str[R.string.s_masjid_timetable], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    Text(Str[R.string.s_use_your_masjid_s_published_times], fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary, lineHeight = 20.sp)
    var importNotes by remember { mutableStateOf<List<String>>(emptyList()) }
    var pending by remember { mutableStateOf<PrayerEngine.ImportResult?>(null) }
    val pickSheet = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val text = runCatching { ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() }.getOrNull() ?: ""
        val r = PrayerEngine.parseTimetable(text, LocalDate.now().year)
        importNotes = r.notes
        if (r.rows.isNotEmpty()) pending = r else importNotes = r.notes + Str[R.string.s_no_usable_rows_were_found_in]
    }
    // Nothing becomes active until the person has seen what was read and confirmed it.
    pending?.let { r ->
        val days = r.rows.keys.sorted()
        val anomalies = remember(r) { PrayerEngine.reviewTimetable(s, r.rows) }
        val zone = s.zone().id
        AlertDialog(
            onDismissRequest = { pending = null }, containerColor = Palette.panelRaised,
            title = { Text(Str[R.string.s_check_the_imported_timetable], fontFamily = Cormorant, fontSize = 26.sp, color = Palette.ivory) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("${days.size} days · ${days.first()} → ${days.last()} · ${r.skipped} line${if (r.skipped == 1) "" else "s"} skipped" + (if (r.rows.values.any { it.size >= 11 }) Str[R.string.s_iqamah_columns_found] else Str[R.string.s_no_iqamah_columns]), fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory, lineHeight = 20.sp)
                    Text("Times are read as wall-clock in $zone. Columns: Fajr, Sunrise, Dhuhr, ʿAsr, Maghrib, Isha" + (if (r.rows.values.any { it.size >= 11 }) Str[R.string.s_then_five_iqamah_times] else "."), fontFamily = Nunito, fontSize = 13.sp, color = Palette.textSecondary, lineHeight = 18.sp, modifier = Modifier.padding(top = 6.dp))
                    listOf(days.first(), days.last()).distinct().forEach { d ->
                        val row = r.rows.getValue(d); val calc = PrayerEngine.calculated(s, LocalDate.parse(d))
                        Text(d, fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.goldSoft, modifier = Modifier.padding(top = 10.dp))
                        Text(Str[R.string.s_sheet] + row.take(6).joinToString("  ") { PrayerEngine.hm(it) }, fontFamily = Nunito, fontSize = 13.sp, color = Palette.ivory)
                        Text(Str[R.string.s_calc] + Prayer.entries.joinToString("  ") { PrayerEngine.clock(calc[it], true) }, fontFamily = Nunito, fontSize = 13.sp, color = Palette.textMuted)
                    }
                    if (anomalies.isEmpty()) Text(Str[R.string.s_no_anomalies_found_every_row_is], fontFamily = Nunito, fontSize = 13.sp, color = Palette.mint, lineHeight = 18.sp, modifier = Modifier.padding(top = 12.dp))
                    else {
                        Text("${anomalies.size} thing${if (anomalies.size == 1) "" else "s"} to check before using this:", fontFamily = Nunito, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Palette.gold, modifier = Modifier.padding(top = 12.dp))
                        anomalies.forEach { Text("• $it", fontFamily = Nunito, fontSize = 12.sp, color = Palette.ivory, lineHeight = 17.sp) }
                    }
                    r.notes.forEach { Text(it, fontFamily = Nunito, fontSize = 12.sp, color = Palette.textSecondary, modifier = Modifier.padding(top = 4.dp)) }
                }
            },
            confirmButton = { GoldButton(if (anomalies.isEmpty()) Str[R.string.s_use_these_times] else Str[R.string.s_use_anyway]) { store.update { it.copy(overrides = it.overrides + r.rows, useOverrides = true) }; AzaanScheduler.reschedule(ctx); pending = null } },
            dismissButton = { TextButton(onClick = { pending = null }) { Text(Str[R.string.s_discard], color = Palette.textSecondary) } }
        )
    }
    OutlinedTextField(value = s.masjidName, onValueChange = { v -> store.update { it.copy(masjidName = v) } }, placeholder = { Text(Str[R.string.s_masjid_name_e_g_lakemba_masjid]) }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
    val days = s.overrides.keys.sorted()
    SettingRow(Str[R.string.s_imported_days], if (days.isEmpty()) Str[R.string.s_none_yet] else "${days.size} days · ${days.first()} → ${days.last()}" + if (s.overrides.values.any { it.size >= 11 }) Str[R.string.s_with_iqamah] else "") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldButton(Str[R.string.s_import_file]) { pickSheet.launch(arrayOf("text/*", "text/csv", "text/comma-separated-values", "application/csv", "*/*")) }
            if (days.isNotEmpty()) TextButton(onClick = { store.update { it.copy(overrides = emptyMap()) } }) { Text(Str[R.string.s_clear], color = Palette.textSecondary) }
        }
    }
    if (days.isNotEmpty()) SettingRow(Str[R.string.s_use_masjid_times], Str[R.string.s_off_keeps_the_file_but_shows]) { Toggle(s.useOverrides) { on -> store.update { it.copy(useOverrides = on) } } }
    importNotes.forEach { Text(it, fontFamily = Nunito, fontSize = 13.sp, color = Palette.goldSoft, modifier = Modifier.padding(top = 4.dp)) }
    if (days.isNotEmpty()) {
        val first = s.overrides.getValue(days.first())
        val calc = PrayerEngine.calculated(s, LocalDate.parse(days.first()))
        Text("Check · ${days.first()}: masjid Fajr %d:%02d vs calculated %s · Maghrib %d:%02d vs %s".format(first[0] / 60, first[0] % 60, PrayerEngine.clock(calc[Prayer.FAJR], true), first[4] / 60, first[4] % 60, PrayerEngine.clock(calc[Prayer.MAGHRIB], true)),
            fontFamily = Nunito, fontSize = 12.sp, color = Palette.textSecondary, modifier = Modifier.padding(top = 6.dp))
    }
}

/**
 * Detects the device's position and, only on success, makes it the active place.
 * On failure nothing is changed and the reason is returned — a failed detection must never
 * leave a previous or placeholder city looking like the user's own.
 */
suspend fun detect(ctx: Context, store: SettingsStore): String = when (val r = LocationRepo.fix(ctx)) {
    is LocationRepo.Fix.Failed -> r.why.message
    is LocationRepo.Fix.Ok -> {
        val loc = r.location
        // If the geocoder fails we still have a valid place: show its coordinates rather than a stale name.
        val name = LocationRepo.name(ctx, loc.latitude, loc.longitude) ?: com.usman.miqaat.data.Setup.coordLabel(loc.latitude, loc.longitude)
        val applied = com.usman.miqaat.data.Setup.applyFix(store.value, loc.latitude, loc.longitude, name, java.time.ZoneId.systemDefault(), LocationRepo.presets)
        store.update { applied.settings }
        ctx.getSharedPreferences("miqaat_meta", Context.MODE_PRIVATE).edit().putLong("lastDetect", System.currentTimeMillis()).apply()
        AzaanScheduler.reschedule(ctx)
        if (applied.needsZoneChoice) "Location set to $name — the device's time zone does not match this place; choose the time zone below."
        else "Location set to $name" + (applied.settings.zoneId?.let { " · time zone $it" } ?: "")
    }
}

/** Background refresh on resume: at most every 30 minutes, and never a source of surprise (zone rules live in Setup.applyFix). */
suspend fun refreshIfDue(ctx: Context, store: SettingsStore) {
    val prefs = ctx.getSharedPreferences("miqaat_meta", Context.MODE_PRIVATE)
    if (System.currentTimeMillis() - prefs.getLong("lastDetect", 0L) < 30 * 60_000L) return
    detect(ctx, store)
}

@Composable
private fun TimesSection(store: SettingsStore, s: AppSettings) {
    var pickMethod by remember { mutableStateOf(false) }
    var pickLat by remember { mutableStateOf(false) }
    Heading(Str[R.string.s_prayer_times], Str[R.string.s_match_your_local_masjid])
    SettingRow(Str[R.string.s_calculation_method], s.method.info, onClick = { pickMethod = true }) { GoldValue(s.method.text + " ›") }
    SettingRow(Str[R.string.s_asr_juristic_method], Str[R.string.s_hanafi_asr_begins_later_shadow_2]) {
        Chips(AsrMethod.entries.map { it.text }, AsrMethod.entries.indexOf(s.asrMethod)) { i -> store.update { it.copy(asrMethod = AsrMethod.entries[i]) } }
    }
    SettingRow(Str[R.string.s_high_latitude_rule], Str[R.string.s_only_matters_above_48_latitude], onClick = { pickLat = true }) { Value(s.latitudeRule.text + " ›") }
    SettingRow(Str[R.string.s_show_end_times], Str[R.string.s_ends_5_57_under_each_prayer]) { Toggle(s.showEndTimes) { on -> store.update { it.copy(showEndTimes = on) } } }
    SettingRow(Str[R.string.s_show_disliked_times_for_voluntary_prayer], Str[R.string.s_a_thin_day_bar_marking_approximate]) { Toggle(s.showDisliked) { on -> store.update { it.copy(showDisliked = on) } } }
    SettingRow(Str[R.string.s_show_sunrise_on_the_home_screen], Str[R.string.s_marks_the_end_of_fajr_time]) { Toggle(s.showSunrise) { on -> store.update { it.copy(showSunrise = on) } } }
    SettingRow(Str[R.string.s_show_azaan_was_ago_after_each], Str[R.string.s_then_the_screen_moves_on_to]) {
        Stepper(s.afterWindowMinutes, 0, 120, 5, Str[R.string.s_min]) { v -> store.update { it.copy(afterWindowMinutes = v) } }
    }
    Spacer(Modifier.height(18.dp))
    Text(Str[R.string.s_jumu_ah], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow(Str[R.string.s_use_a_jumu_ah_time_on], Str[R.string.s_replaces_dhuhr_on_fridays_for_the]) { Toggle(s.jumuahEnabled) { on -> store.update { it.copy(jumuahEnabled = on) } } }
    if (s.jumuahEnabled) SettingRow(Str[R.string.s_jumu_ah_azaan_time], Str[R.string.s_your_masjid_s_first_azaan_adjust]) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StepBtn("−", s.jumuahMinutes > 11 * 60) { store.update { it.copy(jumuahMinutes = it.jumuahMinutes - 5) } }
            Text("%d:%02d %s".format(((s.jumuahMinutes / 60) + 11) % 12 + 1, s.jumuahMinutes % 60, if (s.jumuahMinutes >= 720) "PM" else "AM"), fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.width(90.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            StepBtn("+", s.jumuahMinutes < 15 * 60) { store.update { it.copy(jumuahMinutes = it.jumuahMinutes + 5) } }
        }
    }
    SettingRow(Str[R.string.s_friday_reminders], Str[R.string.s_a_jumu_ah_chip_from_thursday]) { Toggle(s.fridayReminders) { on -> store.update { it.copy(fridayReminders = on) } } }
    SettingRow(Str[R.string.s_hour_of_acceptance_reminder], Str[R.string.s_a_quiet_notification_one_hour_before]) { Toggle(s.fridayHourReminder) { on -> store.update { it.copy(fridayHourReminder = on) } } }
    Spacer(Modifier.height(18.dp))
    Text("Ramaḍān", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow(Str[R.string.s_rama_n_mode], Str[R.string.s_suhoor_and_iftar_labels_fasting_progress]) {
        Chips(RamadanMode.entries.map { it.text }, RamadanMode.entries.indexOf(s.ramadanMode)) { i -> store.update { it.copy(ramadanMode = RamadanMode.entries[i]) } }
    }
    SettingRow(Str[R.string.s_suhoor_alarm], Str[R.string.s_a_chime_and_notification_this_many]) { Stepper(s.suhoorAlarmMinutes, 0, 120, 5, Str[R.string.s_min], zeroLabel = Str[R.string.s_off]) { v -> store.update { it.copy(suhoorAlarmMinutes = v) } } }
    SettingRow("Tarāwīḥ", Str[R.string.s_shown_on_the_home_screen_in]) { Stepper(s.tarawihMinutesAfterIsha, 0, 120, 5, Str[R.string.s_min]) { v -> store.update { it.copy(tarawihMinutesAfterIsha = v) } } }
    Spacer(Modifier.height(18.dp))
    Text(Str[R.string.s_minute_adjustments], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    Text(Str[R.string.s_nudge_each_time_by_a_few], fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary)
    Prayer.entries.forEach { p ->
        SettingRow(p.english, null) {
            Stepper(s.adjustments[p] ?: 0, -30, 30, 1, Str[R.string.s_min], signed = true) { v -> store.update { it.copy(adjustments = it.adjustments + (p to v)) } }
        }
    }
    val today = remember(s) { PrayerEngine.times(s, LocalDate.now(s.zone())) }
    Spacer(Modifier.height(14.dp))
    Text(
        Str[R.string.s_today_with_these_settings] + Prayer.entries.joinToString("   ") { "${it.english} ${PrayerEngine.clock(today[it], s.use24h)}" },
        fontFamily = Nunito, fontSize = 13.sp, color = Palette.goldSoft
    )

    if (pickMethod) PickerDialog(Str[R.string.s_calculation_method], Method.entries.map { it.text to it.info }, Method.entries.indexOf(s.method),
        onPick = { i -> store.update { it.copy(method = Method.entries[i]) } }) { pickMethod = false }
    if (pickLat) PickerDialog(Str[R.string.s_high_latitude_rule], LatitudeRule.entries.map { it.text to "" }, LatitudeRule.entries.indexOf(s.latitudeRule),
        onPick = { i -> store.update { it.copy(latitudeRule = LatitudeRule.entries[i]) } }) { pickLat = false }
}

@Composable
private fun AzaanSection(store: SettingsStore, s: AppSettings) {
    val ctx = LocalContext.current
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        store.update { it.copy(azaanUri = uri.toString()) }
    }
    val pickFajr = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        store.update { it.copy(fajrAzaanUri = uri.toString()) }
    }
    val next = remember(s) { AzaanScheduler.nextEvent(ctx) }

    Heading(Str[R.string.s_azaan_alerts], Str[R.string.s_the_azaan_plays_through_the_alarm])
    if (next != null) Text(
        "Next: ${next.prayer.english} ${if (next.reminder) "reminder" else "azaan"} at ${PrayerEngine.clock(next.at, s.use24h)} ${PrayerEngine.suffix(next.at, s.use24h)}",
        fontFamily = Nunito, fontSize = 14.sp, color = Palette.goldSoft, modifier = Modifier.padding(bottom = 10.dp)
    )
    Prayer.prayersOnly.forEach { p ->
        SettingRow("${p.english}  ${p.arabic}", null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TextButton(onClick = { AzaanService.preview(ctx, p) }) { Text(Str[R.string.s_play], color = Palette.goldSoft) }
                Toggle(s.azaanEnabled[p] == true) { on -> store.update { it.copy(azaanEnabled = it.azaanEnabled + (p to on)) } }
            }
        }
    }
    SettingRow(Str[R.string.s_volume], "${s.azaanVolume}% of the alarm volume") {
        Slider(value = s.azaanVolume / 100f, onValueChange = { v -> store.update { it.copy(azaanVolume = (v * 100).toInt()) } }, modifier = Modifier.width(220.dp).semantics { contentDescription = Str[R.string.s_azaan_volume]; stateDescription = "${s.azaanVolume} percent" })
    }
    SettingRow(Str[R.string.s_reminder_before_azaan], Str[R.string.s_a_quiet_notification_no_sound]) {
        Stepper(s.preReminderMinutes, 0, 30, 5, Str[R.string.s_min], zeroLabel = Str[R.string.s_off]) { v -> store.update { it.copy(preReminderMinutes = v) } }
    }
    Spacer(Modifier.height(18.dp))
    Text(Str[R.string.s_after_the_azaan], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    Text(Str[R.string.s_when_the_azaan_finishes_the_dua], fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary)
    SettingRow(Str[R.string.s_dua_and_hadith_after_each_azaan], Str[R.string.s_for_all_five_prayers]) { Toggle(s.afterAzaanEnabled) { on -> store.update { it.copy(afterAzaanEnabled = on) } } }
    SettingRow(Str[R.string.s_narration], Str[R.string.s_studio_recordings_are_built_in_for]) {
        Chips(Narration.entries.map { it.text }, Narration.entries.indexOf(s.narration)) { i -> store.update { it.copy(narration = Narration.entries[i]) } }
    }
    SettingRow(Str[R.string.s_hadith_stays_on_screen_for], Str[R.string.s_counted_from_when_the_hadith_appears]) {
        Stepper(s.hadithMinutes, 1, 10, 1, Str[R.string.s_min]) { v -> store.update { it.copy(hadithMinutes = v) } }
    }
    SettingRow(Str[R.string.s_hadith_source], "${HadithLibrary.all.size} narrations from Ṣaḥīḥ al-Bukhārī and Ṣaḥīḥ Muslim, each cited with its number. One per azaan, no repeats until all have been shown.") {
        TextButton(onClick = { AzaanService.previewAfter(ctx, Prayer.DHUHR) }) { Text(Str[R.string.s_preview], color = Palette.goldSoft) }
    }
    Spacer(Modifier.height(18.dp))
    Text(Str[R.string.s_azaan_recording], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    Text(
        Str[R.string.s_two_recordings_are_built_in_one],
        fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary
    )
    SettingRow("Azaan file", s.azaanUri?.let { Uri.parse(it).lastPathSegment } ?: "Built-in", onClick = { pickFile.launch(arrayOf("audio/*")) }) {
        Row {
            if (s.azaanUri != null) TextButton(onClick = { store.update { it.copy(azaanUri = null) } }) { Text(Str[R.string.s_reset], color = Palette.textSecondary) }
            Value(Str[R.string.s_choose])
        }
    }
    SettingRow("Fajr azaan file", s.fajrAzaanUri?.let { Uri.parse(it).lastPathSegment } ?: "Same as above", onClick = { pickFajr.launch(arrayOf("audio/*")) }) {
        Row {
            if (s.fajrAzaanUri != null) TextButton(onClick = { store.update { it.copy(fajrAzaanUri = null) } }) { Text(Str[R.string.s_reset], color = Palette.textSecondary) }
            Value(Str[R.string.s_choose])
        }
    }
    Spacer(Modifier.height(18.dp))
    Text(Str[R.string.s_reliability], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory, modifier = Modifier.semantics { heading() })
    ReliabilityRows()
}

/** Granted / Not granted for each thing that can stop the azaan, each with a one-tap fix. Shared with setup. */
@Composable
internal fun ReliabilityRows() {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) { tick++; onPauseOrDispose { } }
    val checks = remember(tick) { com.usman.miqaat.data.Reliability.checks(ctx) }
    val askNotif = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { tick++ }
    checks.forEach { c ->
        val fix: (() -> Unit)? = when {
            c.label == Str[R.string.s_notifications] && !c.ok && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED -> { { askNotif.launch(Manifest.permission.POST_NOTIFICATIONS) } }
            c.fix != null && !c.ok -> { { c.fix.invoke(ctx) } }
            else -> null
        }
        SettingRow(c.label, c.detail, onClick = fix) {
            Text(if (c.ok) Str[R.string.s_granted] else if (fix != null) Str[R.string.s_not_granted_fix] else Str[R.string.s_not_granted], fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                color = if (c.ok) Palette.mint else Palette.gold, modifier = Modifier.semantics { stateDescription = if (c.ok) Str[R.string.s_granted_2] else Str[R.string.s_not_granted] })
        }
    }
}

@Composable
private fun IqamahSection(store: SettingsStore, s: AppSettings) {
    val today = remember(s) { PrayerEngine.times(s, LocalDate.now(s.zone())) }
    Heading(Str[R.string.s_iqamah], Str[R.string.s_for_praying_in_congregation_at_home])
    SettingRow(Str[R.string.s_iqamah_times], Str[R.string.s_shown_under_each_azaan_time_and]) { Toggle(s.iqamahEnabled) { on -> store.update { it.copy(iqamahEnabled = on) } } }
    if (s.iqamahEnabled) {
        Spacer(Modifier.height(10.dp))
        Text(Str[R.string.s_iqamah_for_each_prayer], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
        Text(Str[R.string.s_either_a_number_of_minutes_after], fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary)
        Prayer.prayersOnly.forEach { p ->
            val fixed = s.iqamahIsFixed[p] == true
            val off = s.iqamahOffsets[p] ?: 0
            val at = s.iqamahFixed[p] ?: 12 * 60
            val iq = PrayerEngine.iqamah(s, today, p)
            SettingRow("${p.english}  ${p.arabic}", if (iq != null) "Today: azaan ${PrayerEngine.clock(today[p], s.use24h)} ${PrayerEngine.suffix(today[p], s.use24h)} → iqamah ${PrayerEngine.clock(iq, s.use24h)} ${PrayerEngine.suffix(iq, s.use24h)}" else Str[R.string.s_off_for_this_prayer]) {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chips(listOf(Str[R.string.s_after_azaan], Str[R.string.s_fixed_time]), if (fixed) 1 else 0) { i -> store.update { it.copy(iqamahIsFixed = it.iqamahIsFixed + (p to (i == 1))) } }
                    if (fixed) TimeStepper(at, s.use24h) { v -> store.update { it.copy(iqamahFixed = it.iqamahFixed + (p to v)) } }
                    else Stepper(off, 0, 60, 1, Str[R.string.s_min], zeroLabel = Str[R.string.s_off]) { v -> store.update { it.copy(iqamahOffsets = it.iqamahOffsets + (p to v)) } }
                }
            }
        }
        if (s.jumuahEnabled) SettingRow(Str[R.string.s_jumu_ah_iqamah_fixed_time], Str[R.string.s_used_instead_of_the_dhuhr_offset]) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StepBtn("−", s.jumuahIqamahMinutes > 11 * 60) { store.update { it.copy(jumuahIqamahMinutes = it.jumuahIqamahMinutes - 5) } }
                Text("%d:%02d %s".format(((s.jumuahIqamahMinutes / 60) + 11) % 12 + 1, s.jumuahIqamahMinutes % 60, if (s.jumuahIqamahMinutes >= 720) "PM" else "AM"), fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.width(90.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                StepBtn("+", s.jumuahIqamahMinutes < 16 * 60) { store.update { it.copy(jumuahIqamahMinutes = it.jumuahIqamahMinutes + 5) } }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(Str[R.string.s_countdown_and_sound], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
        SettingRow(Str[R.string.s_countdown_before_iqamah], Str[R.string.s_full_screen_seconds_only_with_a]) {
            Stepper(s.iqamahCountdownSeconds, 30, 180, 15, " s") { v -> store.update { it.copy(iqamahCountdownSeconds = v) } }
        }
        SettingRow(Str[R.string.s_sound_at_iqamah], Str[R.string.s_iqamah_recording_plays_res_raw_iqamah]) {
            Chips(IqamahSound.entries.map { it.text }, IqamahSound.entries.indexOf(s.iqamahSound)) { i -> store.update { it.copy(iqamahSound = IqamahSound.entries[i]) } }
        }
        SettingRow(Str[R.string.s_quiet_screen_after_iqamah], Str[R.string.s_dim_clock_only_nothing_moving_tap]) {
            Stepper(s.quietMinutes, 0, 30, 1, Str[R.string.s_min], zeroLabel = Str[R.string.s_off]) { v -> store.update { it.copy(quietMinutes = v) } }
        }
    }
}

@Composable
private fun TestSection(store: SettingsStore, s: AppSettings) {
    val ctx = LocalContext.current
    Heading(Str[R.string.s_test_preview], Str[R.string.s_run_any_part_of_the_experience])
    Text(Str[R.string.s_theme], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow(Str[R.string.s_preview_a_theme], Str[R.string.s_applies_straight_away_press_back_to]) {
        Chips(AppTheme.entries.map { it.text }, AppTheme.entries.indexOf(s.theme)) { i -> store.update { it.copy(theme = AppTheme.entries[i]) } }
    }
    Spacer(Modifier.height(14.dp))
    Text("Azaan", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow(Str[R.string.s_everything_exactly_as_at_prayer_time], Str[R.string.s_azaan_dua_hadith_back_to_the]) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldButton("Fajr") { AzaanService.playFull(ctx, Prayer.FAJR) }
            GoldButton("Maghrib") { AzaanService.playFull(ctx, Prayer.MAGHRIB) }
        }
    }
    SettingRow(Str[R.string.s_azaan_recording], Str[R.string.s_plays_the_full_recording_with_the]) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldButton("Fajr") { AzaanService.preview(ctx, Prayer.FAJR) }
            GoldButton(Str[R.string.s_other_prayers]) { AzaanService.preview(ctx, Prayer.MAGHRIB) }
        }
    }
    SettingRow(Str[R.string.s_full_sequence_after_azaan], Str[R.string.s_dua_after_azaan_hadith_back_to]) { GoldButton("Start") { AzaanService.previewAfter(ctx, Prayer.DHUHR) } }
    SettingRow(Str[R.string.s_rama_n_maghrib_sequence], Str[R.string.s_iftar_dua_dua_after_azaan_hadith]) { GoldButton("Start") { AzaanService.previewAfter(ctx, Prayer.MAGHRIB) } }
    Spacer(Modifier.height(14.dp))
    Text(Str[R.string.s_iqamah], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow(Str[R.string.s_countdown_iqamah_quiet_screen], "The whole iqamah flow, starting with a ${s.iqamahCountdownSeconds}-second countdown") { GoldButton("Start") { AzaanService.testIqamah(ctx, Prayer.MAGHRIB) } }
    SettingRow(Str[R.string.s_short_countdown], Str[R.string.s_same_flow_15_second_countdown_to]) { GoldButton("Start") { AzaanService.testIqamah(ctx, Prayer.MAGHRIB, 15) } }
    SettingRow(Str[R.string.s_iqamah_sound_only], Str.get(R.string.s_plays_x_and_shows_iqamah, s.iqamahSound.text.lowercase())) { GoldButton(Str[R.string.s_play]) { AzaanService.testIqamahNow(ctx, Prayer.MAGHRIB) } }
    SettingRow(Str[R.string.s_quiet_screen], "Shows the in-prayer screen for ${s.quietMinutes} min; tap it to leave") { GoldButton("Show") { AzaanService.testQuiet(ctx, Prayer.MAGHRIB) } }
    Spacer(Modifier.height(14.dp))
    Text(Str[R.string.s_home_screen_modes], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow(Str[R.string.s_rama_n_mode], Str[R.string.s_force_it_on_to_see_suhoor]) {
        Chips(RamadanMode.entries.map { it.text }, RamadanMode.entries.indexOf(s.ramadanMode)) { i -> store.update { it.copy(ramadanMode = RamadanMode.entries[i]) } }
    }
    SettingRow(Str[R.string.s_stop_anything_that_is_playing], null) { TextButton(onClick = { AzaanService.stop(ctx) }) { Text(Str[R.string.s_stop], color = Palette.goldSoft) } }
}

@Composable
private fun HealthSection(store: SettingsStore, s: AppSettings) {
    val ctx = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    val log = remember(tick) { com.usman.miqaat.data.Health.read(ctx) }
    val week = log.filter { it.at > System.currentTimeMillis() - 7 * 86_400_000L }
    val fired = week.count { it.kind == com.usman.miqaat.data.Health.Kind.AZAAN || it.kind == com.usman.miqaat.data.Health.Kind.IQAMAH }
    val late = week.count { (it.kind == com.usman.miqaat.data.Health.Kind.AZAAN || it.kind == com.usman.miqaat.data.Health.Kind.IQAMAH) && it.lateBy > 1 }
    val missed = week.count { it.kind == com.usman.miqaat.data.Health.Kind.MISSED }
    val next = remember(s) { AzaanScheduler.nextEvent(ctx) }
    val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager

    Heading(Str[R.string.s_reliability_backup], Str[R.string.s_did_it_fire_every_azaan_iqamah])
    Text("Last 7 days · $fired played" + (if (late > 0) " · $late late" else "") + (if (missed > 0) " · $missed missed" else Str[R.string.s_none_missed]), fontFamily = Cormorant, fontSize = 26.sp, color = if (missed > 0) Color(0xFFF08C8C) else if (late > 0) Color(0xFFF0A050) else Palette.mint)
    Spacer(Modifier.height(6.dp))
    SettingRow(Str[R.string.s_next_alarm_armed], next?.let { "${it.prayer.english} ${if (it.iqamah) "iqamah" else if (it.reminder) "reminder" else "azaan"} · ${PrayerEngine.clock(it.at, s.use24h)} ${PrayerEngine.suffix(it.at, s.use24h)}" } ?: Str[R.string.s_nothing_scheduled_turn_on_an_azaan]) { Value(if (next != null) "✓" else "!") }
    SettingRow(Str[R.string.s_battery_optimisation], if (pm.isIgnoringBatteryOptimizations(ctx.packageName)) Str[R.string.s_miqaat_is_exempt] else Str[R.string.s_not_exempt_android_may_delay_alarms]) { Value(if (pm.isIgnoringBatteryOptimizations(ctx.packageName)) "✓" else "!") }
    SettingRow(Str[R.string.s_launch_on_boot], if (s.launchOnBoot) Str[R.string.s_on] else Str[R.string.s_off_recommended_for_the_wall_tablet]) { Toggle(s.launchOnBoot) { on -> store.update { it.copy(launchOnBoot = on) } } }
    SettingRow(Str[R.string.s_time_change_self_check], Str[R.string.s_runs_automatically_after_any_clock_or]) { Value("✓") }

    Spacer(Modifier.height(16.dp))
    Text(Str[R.string.s_log], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    if (log.isEmpty()) Text(Str[R.string.s_nothing_yet_entries_appear_after_the], fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary)
    log.take(40).forEach { e ->
        val col = when (e.kind) { com.usman.miqaat.data.Health.Kind.MISSED -> Color(0xFFF08C8C); com.usman.miqaat.data.Health.Kind.TIME_CHANGE, com.usman.miqaat.data.Health.Kind.BOOT -> Palette.goldSoft; else -> if (e.lateBy > 1) Color(0xFFF0A050) else Palette.mint }
        Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.padding(top = 6.dp).size(9.dp).clip(androidx.compose.foundation.shape.CircleShape).background(col))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(e.title, fontFamily = Nunito, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory)
                Text(e.detail, fontFamily = Nunito, fontSize = 12.sp, color = Palette.textSecondary)
            }
            val t = e.time(s.zone())
            Text(t.format(java.time.format.DateTimeFormatter.ofPattern("EEE d MMM · " + (if (s.use24h) "HH:mm" else "h:mm a"), java.util.Locale.ENGLISH)), fontFamily = Nunito, fontSize = 12.sp, color = Palette.textSecondary)
        }
        HorizontalDivider(color = Palette.line)
    }
    if (log.isNotEmpty()) TextButton(onClick = { com.usman.miqaat.data.Health.clear(ctx); tick++ }) { Text(Str[R.string.s_clear_log], color = Palette.textSecondary) }

    Spacer(Modifier.height(16.dp))
    Text(Str[R.string.s_backup_restore], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    var msg by remember { mutableStateOf("") }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { ctx.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(com.usman.miqaat.data.Health.exportSettings(ctx)) } }
            .onSuccess { msg = Str[R.string.s_settings_saved] }.onFailure { msg = "Couldn't save: ${it.message}" }
    }
    val load = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val text = runCatching { ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() }.getOrNull() ?: ""
        val n = com.usman.miqaat.data.Health.importSettings(ctx, text)
        if (n > 0) store.reload()
        msg = if (n > 0) "$n settings restored" else Str[R.string.s_that_file_isn_t_a_miqaat]
    }
    SettingRow(Str[R.string.s_settings_file], Str[R.string.s_everything_in_settings_as_one_small]) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldButton(Str[R.string.s_save]) { save.launch("miqaat-settings-${LocalDate.now()}.txt") }
            TextButton(onClick = { load.launch(arrayOf("text/*", "*/*")) }) { Text(Str[R.string.s_restore], color = Palette.goldSoft) }
        }
    }
    if (msg.isNotEmpty()) Text(msg, fontFamily = Nunito, fontSize = 13.sp, color = Palette.goldSoft, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun PrivacySection() {
    Heading("Privacy", Str[R.string.s_what_miqaat_does_with_your_data])
    SettingRow(Str[R.string.s_prayer_times], Str[R.string.s_calculated_on_this_device_from_your]) { Value(Str[R.string.s_on_device]) }
    SettingRow(Str[R.string.s_place_name_search], Str[R.string.s_android_s_geocoder_sends_your_coordinates]) { GoldValue(Str[R.string.s_google]) }
    if (Updater.enabled) SettingRow(Str[R.string.s_updates], Str[R.string.s_miqaat_asks_github_com_whether_a]) { GoldValue(Str[R.string.s_github]) }
    else SettingRow(Str[R.string.s_updates], Str[R.string.s_delivered_by_google_play_miqaat_itself]) { Value(Str[R.string.s_play]) }
    SettingRow(Str[R.string.s_narration], Str[R.string.s_the_dua_and_hadith_recordings_are]) { Value(Str[R.string.s_on_device]) }
    SettingRow(Str[R.string.s_health_log_adhk_r_counts_friday], Str[R.string.s_stored_in_the_app_s_private]) { Value(Str[R.string.s_on_device]) }
    SettingRow(Str[R.string.s_analytics_advertising_accounts_crash_reporting], Str[R.string.s_none_there_is_no_miqaat_server]) { Value(Str[R.string.s_none]) }
    SettingRow(Str[R.string.s_permissions], Str[R.string.s_location_once_for_times_notifications_azaan] + (if (Updater.enabled) Str[R.string.s_install_packages_self_update] else "") + Str[R.string.s_no_contacts_camera_microphone_or_storage]) { Value(Str[R.string.s_minimal]) }
    SettingRow(Str[R.string.s_android_backup], Str[R.string.s_off_your_settings_are_never_copied]) { Value(Str[R.string.s_off]) }
    SettingRow(Str[R.string.s_source_code], Str[R.string.s_github_com_haawas_alt_miqaat_builds]) { Value(Str[R.string.s_open]) }
}

@Composable
private fun HijriSection(store: SettingsStore, s: AppSettings) {
    val h = PrayerEngine.hijri(LocalDate.now(s.zone()), s.hijriOffsetDays)
    Heading(Str[R.string.s_hijri_calendar], Str[R.string.s_dates_follow_the_umm_al_qura])
    SettingRow(Str[R.string.s_show_hijri_date], Str[R.string.s_on_the_home_screen_and_timetable]) { Toggle(s.showHijri) { on -> store.update { it.copy(showHijri = on) } } }
    SettingRow("Adjustment", "Today is ${h.english}") {
        Stepper(s.hijriOffsetDays, -2, 2, 1, Str[R.string.s_day], signed = true) { v -> store.update { it.copy(hijriOffsetDays = v) } }
    }
    val tomorrow = PrayerEngine.hijri(LocalDate.now(s.zone()).plusDays(1), s.hijriOffsetDays)
    if (h.day >= 29) {
        Spacer(Modifier.height(14.dp))
        Text(Str[R.string.s_moon_sighting_tonight], fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
        Text(
            if (tomorrow.day == 1) "The calendar already turns to ${tomorrow.english.substringAfter(' ')} tomorrow. If the moon was not sighted in your community, complete 30 days instead."
            else Str[R.string.s_tomorrow_is_day_30_by_calculation],
            fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary, lineHeight = 20.sp
        )
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (tomorrow.day != 1) GoldButton(Str[R.string.s_moon_sighted_new_month_tomorrow]) { store.update { it.copy(hijriOffsetDays = it.hijriOffsetDays + 1) } }
            else GoldButton(Str[R.string.s_not_sighted_complete_30_days]) { store.update { it.copy(hijriOffsetDays = it.hijriOffsetDays - 1) } }
        }
        Text(Str[R.string.s_this_shifts_the_hijri_date_by], fontFamily = Nunito, fontSize = 12.sp, color = Palette.textMuted, modifier = Modifier.padding(top = 6.dp))
    }
    Spacer(Modifier.height(16.dp))
    Text(h.arabic, fontFamily = Amiri, fontSize = 40.sp, color = Palette.goldSoft)
}

@Composable
private fun DisplaySection(store: SettingsStore, s: AppSettings) {
    Heading(Str[R.string.s_display_art], Str[R.string.s_how_miqaat_looks_on_the_wall])
    SettingRow(Str[R.string.s_theme], Str[R.string.s_changes_the_home_screen_and_the]) {
        Chips(AppTheme.entries.map { it.text }, AppTheme.entries.indexOf(s.theme)) { i -> store.update { it.copy(theme = AppTheme.entries[i]) } }
    }
    SettingRow(Str[R.string.s_language], Str[R.string.s_home_screen_and_widget_urdu_is]) {
        Chips(Language.entries.map { it.label }, Language.entries.indexOf(s.language)) { i -> store.update { it.copy(language = Language.entries[i]) } }
    }
    SettingRow(Str[R.string.s_large_type], Str[R.string.s_one_prayer_one_time_one_line]) { Toggle(s.largeType) { on -> store.update { it.copy(largeType = on) } } }
    SettingRow(Str[R.string.s_home_screen_widget], Str[R.string.s_long_press_your_phone_s_home]) { Value(Str[R.string.s_phone]) }
    SettingRow(Str[R.string.s_time_format], null) { Chips(listOf("12-hour", "24-hour"), if (s.use24h) 1 else 0) { i -> store.update { it.copy(use24h = i == 1) } } }
    SettingRow(Str[R.string.s_keep_the_screen_on], Str[R.string.s_while_miqaat_is_open_best_with]) { Toggle(s.keepScreenOn) { on -> store.update { it.copy(keepScreenOn = on) } } }
    SettingRow(Str[R.string.s_dim_after_isha], Str[R.string.s_softens_the_screen_through_the_night]) { Toggle(s.nightDim) { on -> store.update { it.copy(nightDim = on) } } }
    SettingRow(Str[R.string.s_qibla_direction_on_the_home_screen], Str[R.string.s_tap_it_for_the_compass]) { Toggle(s.showQibla) { on -> store.update { it.copy(showQibla = on) } } }
    SettingRow(Str[R.string.s_morning_and_evening_adhk_r], Str[R.string.s_a_prompt_after_fajr_and_after]) { Toggle(s.adhkarEnabled) { on -> store.update { it.copy(adhkarEnabled = on) } } }
    SettingRow(Str[R.string.s_after_prayer_adhk_r], Str[R.string.s_a_prompt_for_40_minutes_after]) { Toggle(s.postPrayerAdhkar) { on -> store.update { it.copy(postPrayerAdhkar = on) } } }
    SettingRow(Str[R.string.s_learn_salah], Str[R.string.s_a_book_icon_on_the_home]) { Toggle(s.kidsMode) { on -> store.update { it.copy(kidsMode = on) } } }
    SettingRow(Str[R.string.s_art_theme], null) { Chips(ArtTheme.entries.map { it.text }, ArtTheme.entries.indexOf(s.artTheme)) { i -> store.update { it.copy(artTheme = ArtTheme.entries[i]) } } }
    SettingRow(Str[R.string.s_open_miqaat_when_the_device_starts], Str[R.string.s_so_the_wall_tablet_comes_back]) { Toggle(s.launchOnBoot) { on -> store.update { it.copy(launchOnBoot = on) } } }
}

@Composable
private fun AboutSection(s: AppSettings) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val up by Updater.state.collectAsState()
    LaunchedEffect(Unit) { Updater.check(ctx) }
    // Re-check the install permission when the user comes back from the system "Allow installs" screen.
    var tick by remember { mutableStateOf(0) }
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) { tick++; onPauseOrDispose { } }
    val canInstall = remember(tick) { Updater.canInstall(ctx) }
    Heading(Str[R.string.s_about_miqaat], Str[R.string.s_an_appointed_time])
    Text("Version ${Updater.currentName} · build ${Updater.currentBuild} · ${if (Updater.enabled) "direct-download edition" else "Google Play edition"}", fontFamily = Nunito, fontSize = 15.sp, color = Palette.goldSoft)
    Text("Built from commit ${com.usman.miqaat.BuildConfig.GIT_SHA.take(12)} · release tag ${com.usman.miqaat.BuildConfig.BUILD_TAG}. The SHA-256 of every release is published next to it on GitHub.", fontFamily = Nunito, fontSize = 13.sp, color = Palette.textSecondary, lineHeight = 18.sp)
    Spacer(Modifier.height(10.dp))
    if (!Updater.enabled) SettingRow(Str[R.string.s_updates], Str[R.string.s_this_edition_is_updated_by_google]) { Value(Str[R.string.s_play]) }
    else when (val u = up) {
        is Updater.State.Available -> {
            SettingRow("Update available: version ${u.info.versionName}", if (canInstall) Str[R.string.s_downloads_from_github_and_opens_the] else Str[R.string.s_first_allow_miqaat_to_install_updates]) {
                if (canInstall) GoldButton(Str[R.string.s_download_install]) { Updater.download(ctx, u.info) }
                else GoldButton(Str[R.string.s_allow_installs]) { Updater.openInstallPermission(ctx) }
            }
        }
        is Updater.State.Downloading -> SettingRow("Downloading version ${u.info.versionName}…", Str[R.string.s_the_installer_opens_automatically_when_it]) { Value("…") }
        is Updater.State.Ready -> SettingRow(Str[R.string.s_update_downloaded], Str[R.string.s_tap_if_the_installer_didn_t]) { GoldButton(Str[R.string.s_install]) { Updater.install(ctx, u.file) } }
        is Updater.State.Failed -> SettingRow(Str[R.string.s_update_check_failed], u.reason) { GoldButton(Str[R.string.s_try_again]) { scope.launch { Updater.check(ctx, force = true) } } }
        Updater.State.Checking -> SettingRow(Str[R.string.s_checking_for_updates], null) { Value("…") }
        Updater.State.UpToDate -> SettingRow(Str[R.string.s_you_have_the_latest_version], Str[R.string.s_checked_just_now]) { TextButton(onClick = { scope.launch { Updater.check(ctx, force = true) } }) { Text(Str[R.string.s_check_again], color = Palette.goldSoft) } }
        Updater.State.Idle -> SettingRow(Str[R.string.s_updates], Str[R.string.s_new_builds_are_published_automatically_each]) { GoldButton(Str[R.string.s_check_for_updates]) { scope.launch { Updater.check(ctx, force = true) } } }
    }
    SettingRow("Report a content correction", Str[R.string.s_found_an_error_report], onClick = { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/${com.usman.miqaat.BuildConfig.REPO}/issues/new?title=Content%20correction"))) } }) { Value("GitHub ›") }
    Spacer(Modifier.height(14.dp))
    SettingRow(Str[R.string.s_content_sources], Str[R.string.s_every_hadith_dhikr_dua_and_ruling]) { Value(Str[R.string.s_content_review_pending]) }
    Text(
        Str[R.string.s_prayer_times_are_computed_on_the] +
            Str[R.string.s_no_account_no_advertising_no_analytics] +
            "Current: ${s.method.label}, Asr ${s.asrMethod.label}, ${s.locationName} (%.3f, %.3f).".format(s.latitude, s.longitude),
        fontFamily = Nunito, fontSize = 15.sp, color = Palette.textSecondary, lineHeight = 22.sp
    )
}

// ---------------------------------------------------------------- controls

@Composable
internal fun Heading(title: String, desc: String) {
    Text(title, fontFamily = Cormorant, fontSize = 34.sp, color = Palette.ivory, modifier = Modifier.semantics { heading() })
    Text(desc, fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary, lineHeight = 20.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))
}

@Composable
internal fun SettingRow(title: String, subtitle: String?, onClick: (() -> Unit)? = null, trailing: @Composable () -> Unit) {
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compact = maxWidth < 560.dp
        // The row reads as one element to TalkBack ("title, subtitle") and the control keeps its own role.
        val rowMod = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick, role = androidx.compose.ui.semantics.Role.Button) else Modifier).padding(vertical = 14.dp)
        if (compact) {
            // Phone: label on top, control underneath, so neither squeezes the other.
            Column(rowMod) {
                Column(Modifier.semantics(mergeDescendants = true) {}) {
                    Text(title, fontFamily = Nunito, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory)
                    if (subtitle != null) Text(subtitle, fontFamily = Nunito, fontSize = 13.sp, color = Palette.textSecondary, lineHeight = 18.sp)
                }
                Box(Modifier.padding(top = 10.dp).fillMaxWidth(), contentAlignment = Alignment.CenterStart) { trailing() }
            }
        } else {
            Row(rowMod, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f).padding(end = 20.dp).semantics(mergeDescendants = true) {}) {
                    Text(title, fontFamily = Nunito, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory)
                    if (subtitle != null) Text(subtitle, fontFamily = Nunito, fontSize = 13.sp, color = Palette.textSecondary, lineHeight = 18.sp)
                }
                trailing()
            }
        }
    }
    HorizontalDivider(color = Palette.line)
}

@Composable internal fun Value(t: String) = Text(t, fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory)
@Composable internal fun GoldValue(t: String) = Text(t, fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft)

@Composable
internal fun Toggle(on: Boolean, onChange: (Boolean) -> Unit) = Switch(
    checked = on, onCheckedChange = onChange,
    colors = SwitchDefaults.colors(checkedThumbColor = Palette.night, checkedTrackColor = Palette.gold, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.White.copy(alpha = 0.2f))
)

@Composable
internal fun GoldButton(label: String, enabled: Boolean = true, onClick: () -> Unit) =
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.heightIn(min = 48.dp), colors = ButtonDefaults.buttonColors(containerColor = Palette.gold, contentColor = Palette.night, disabledContainerColor = Palette.gold.copy(alpha = 0.35f), disabledContentColor = Palette.night)) {
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold)
    }

/** Single-choice chip row. Exposed to TalkBack as radio buttons with a selected state. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun Chips(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
        labels.forEachIndexed { i, l ->
            val cur = i == selected
            Box(
                Modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(50)).background(if (cur) Palette.gold else Color.Transparent)
                    .border(1.dp, if (cur) Palette.gold else Palette.lineStrong, RoundedCornerShape(50))
                    .selectable(selected = cur, role = androidx.compose.ui.semantics.Role.RadioButton) { onSelect(i) }.padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) { Text(l, fontFamily = Nunito, fontSize = 14.sp, fontWeight = if (cur) FontWeight.Bold else FontWeight.Normal, color = if (cur) Palette.night else Palette.ivory) }
        }
    }
}

@Composable
private fun Stepper(value: Int, min: Int, max: Int, step: Int, unit: String, signed: Boolean = false, zeroLabel: String? = null, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        StepBtn("−", value > min) { onChange((value - step).coerceAtLeast(min)) }
        val label = when {
            value == 0 && zeroLabel != null -> zeroLabel
            signed && value > 0 -> "+$value$unit"
            else -> "$value$unit"
        }
        Text(label, fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.width(78.dp).semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite }, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        StepBtn("+", value < max) { onChange((value + step).coerceAtMost(max)) }
    }
}

/** Clock-time control: ±1 h and ±5 min around a minutes-from-midnight value. */
@Composable
private fun TimeStepper(minutes: Int, use24h: Boolean, onChange: (Int) -> Unit) {
    fun fmt(m: Int): String { val h = (m / 60) % 24; val mi = m % 60; return if (use24h) "%02d:%02d".format(h, mi) else "%d:%02d %s".format((h + 11) % 12 + 1, mi, if (h >= 12) "PM" else "AM") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        StepBtn("−1h", minutes >= 60) { onChange(minutes - 60) }
        StepBtn("−5", minutes >= 5) { onChange(minutes - 5) }
        Text(fmt(minutes), fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.width(92.dp).semantics { liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite }, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        StepBtn("+5", minutes <= 24 * 60 - 10) { onChange(minutes + 5) }
        StepBtn("+1h", minutes <= 23 * 60 - 5) { onChange(minutes + 60) }
    }
}

@Composable
internal fun StepBtn(t: String, enabled: Boolean, onClick: () -> Unit) {
    val label = when (t) { "−" -> Str[R.string.s_decrease]; "+" -> Str[R.string.s_increase]; "−1h" -> Str[R.string.s_one_hour_earlier]; "+1h" -> Str[R.string.s_one_hour_later]; "−5" -> Str[R.string.s_five_minutes_earlier]; "+5" -> Str[R.string.s_five_minutes_later]; else -> t }
    Box(
        Modifier.size(width = 48.dp, height = 48.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, if (enabled) Palette.lineStrong else Palette.line, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick, role = androidx.compose.ui.semantics.Role.Button).semantics { contentDescription = label }, contentAlignment = Alignment.Center
    ) { Text(t, fontSize = if (t.length > 1) 13.sp else 22.sp, fontFamily = Nunito, fontWeight = FontWeight.Bold, color = if (enabled) Palette.ivory else Palette.textDisabled) }
}

@Composable
private fun PickerDialog(title: String, options: List<Pair<String, String>>, selected: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Palette.panelRaised,
        title = { Text(title, fontFamily = Cormorant, fontSize = 28.sp, color = Palette.ivory) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                options.forEachIndexed { i, (l, d) ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onPick(i); onDismiss() }.padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = i == selected, onClick = { onPick(i); onDismiss() })
                        Column {
                            Text(l, fontFamily = Nunito, fontSize = 16.sp, color = Palette.ivory)
                            if (d.isNotEmpty()) Text(d, fontFamily = Nunito, fontSize = 13.sp, color = Palette.textSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(Str[R.string.s_close], color = Palette.goldSoft) } }
    )
}
