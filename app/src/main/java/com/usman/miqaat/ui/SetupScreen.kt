package com.usman.miqaat.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.azaan.AzaanScheduler
import com.usman.miqaat.azaan.AzaanService
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AsrMethod
import com.usman.miqaat.data.LocationRepo
import com.usman.miqaat.data.Method
import com.usman.miqaat.data.Place
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.Reliability
import com.usman.miqaat.data.SettingsStore
import com.usman.miqaat.data.Setup
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

private enum class Step(val title: String) { WELCOME("Welcome"), PLACE(Str[R.string.s_your_place]), CONFIRM(Str[R.string.s_check_the_times]), ALERTS(Str[R.string.s_azaan_alerts_2]) }

/**
 * First-run setup. Nothing about prayer times is shown as valid until the person has chosen or
 * detected a place and seen the resulting zone, method and today's Fajr/Maghrib.
 * Every step can be revisited; the flow cannot be completed without a place.
 */
@Composable
fun SetupScreen(store: SettingsStore, settings: AppSettings, onDone: () -> Unit) {
    var step by rememberSaveable { mutableStateOf(Step.WELCOME) }
    val ctx = LocalContext.current
    Box(Modifier.fillMaxSize().background(Palette.night)) {
        GirihLattice(Modifier.fillMaxSize(), tile = 90f, alpha = 0.07f)
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // progress
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp).semantics { contentDescription = "Step ${step.ordinal + 1} of ${Step.entries.size}: ${step.title}" }) {
                Step.entries.forEach { s -> Box(Modifier.size(width = 34.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(if (s.ordinal <= step.ordinal) Palette.gold else Palette.line)) }
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().widthIn(max = 640.dp).clip(RoundedCornerShape(24.dp)).background(Palette.panelRaised).padding(horizontal = 24.dp, vertical = 22.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (step) {
                    Step.WELCOME -> Welcome { step = Step.PLACE }
                    Step.PLACE -> PlaceStep(store, settings, onBack = { step = Step.WELCOME }, onNext = { step = Step.CONFIRM })
                    Step.CONFIRM -> ConfirmStep(store, settings, onBack = { step = Step.PLACE }, onNext = { step = Step.ALERTS })
                    Step.ALERTS -> AlertsStep(store, settings, onBack = { step = Step.CONFIRM }, onFinish = {
                        store.update { it.copy(setupDone = true) }
                        AzaanScheduler.reschedule(ctx)
                        onDone()
                    })
                }
            }
        }
    }
}

@Composable
private fun Welcome(onNext: () -> Unit) {
    Text("ميقات", fontFamily = Amiri, fontSize = 56.sp, color = Palette.goldSoft, modifier = Modifier.semantics { contentDescription = "Miqaat" })
    Text(Str[R.string.s_as_sal_mu_alaykum], fontFamily = Cormorant, fontSize = 34.sp, color = Palette.ivory, modifier = Modifier.semantics { heading() })
    Text(
        Str[R.string.s_miqaat_is_a_prayer_clock_it] +
            Str[R.string.s_nothing_is_uploaded_there_is_no],
        fontFamily = Nunito, fontSize = 15.sp, color = Palette.textSecondary, lineHeight = 22.sp
    )
    Text(Str[R.string.s_setup_takes_about_a_minute_your], fontFamily = Nunito, fontSize = 15.sp, color = Palette.ivory, lineHeight = 22.sp)
    Spacer(Modifier.height(6.dp))
    GoldButton(Str[R.string.s_begin], onClick = onNext)
}

@Composable
private fun PlaceStep(store: SettingsStore, s: AppSettings, onBack: () -> Unit, onNext: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }

    fun runDetect() { scope.launch { busy = true; status = Str[R.string.s_detecting_your_location]; status = detect(ctx, store); busy = false } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { g ->
        if (g.values.any { it }) runDetect() else status = LocationRepo.Problem.NO_PERMISSION.message
    }

    Text(Str[R.string.s_where_will_miqaat_be_used], fontFamily = Cormorant, fontSize = 30.sp, color = Palette.ivory, modifier = Modifier.semantics { heading() })
    Text(Str[R.string.s_prayer_times_depend_on_the_exact], fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary, lineHeight = 20.sp)

    if (s.locationSet) {
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Palette.gold.copy(alpha = 0.14f)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.locationName, fontFamily = Nunito, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Palette.goldSoft)
                Text(Setup.coordLabel(s.latitude, s.longitude), fontFamily = Nunito, fontSize = 13.sp, color = Palette.textSecondary)
            }
            Text(Str[R.string.s_chosen], fontFamily = Nunito, fontSize = 14.sp, color = Palette.mint)
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        GoldButton(if (busy) "Detecting…" else Str[R.string.s_use_my_location], enabled = !busy) {
            if (LocationRepo.hasPermission(ctx)) runDetect() else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
        Text(Str[R.string.s_one_fix_then_only_when_you], fontFamily = Nunito, fontSize = 13.sp, color = Palette.textMuted)
    }
    status?.let { Text(it, fontFamily = Nunito, fontSize = 14.sp, color = if (it.startsWith("Location set")) Palette.mint else Palette.gold, lineHeight = 20.sp, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) }

    Text(Str[R.string.s_or_search_for_a_place], fontFamily = Cormorant, fontSize = 22.sp, color = Palette.ivory, modifier = Modifier.padding(top = 6.dp).semantics { heading() })
    OutlinedTextField(
        value = query, onValueChange = { query = it; scope.launch { results = LocationRepo.search(ctx, it) } },
        placeholder = { Text(Str[R.string.s_suburb_or_city_e_g_lakemba]) }, singleLine = true, modifier = Modifier.fillMaxWidth()
    )
    (if (query.isBlank()) LocationRepo.presets else results).take(12).forEach { p ->
        Row(
            Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(RoundedCornerShape(10.dp))
                .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                    store.update { it.copy(latitude = p.lat, longitude = p.lng, locationName = p.name, locationSet = true, autoLocation = false, zoneId = p.zone, zoneManual = false) }
                    status = "Location set to ${p.name}"
                }
                .padding(horizontal = 8.dp, vertical = 12.dp).semantics(mergeDescendants = true) {},
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(p.name, fontFamily = Nunito, fontSize = 16.sp, color = Palette.ivory)
            Text(Setup.coordLabel(p.lat, p.lng), fontFamily = Nunito, fontSize = 13.sp, color = Palette.textMuted)
        }
        HorizontalDivider(color = Palette.line)
    }
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text(Str[R.string.s_back], color = Palette.textSecondary) }
        GoldButton(Str[R.string.s_next], enabled = s.locationSet, onClick = onNext)
    }
    if (!s.locationSet) Text(Str[R.string.s_choose_or_detect_a_place_to], fontFamily = Nunito, fontSize = 13.sp, color = Palette.textMuted)
}

@Composable
private fun ConfirmStep(store: SettingsStore, s: AppSettings, onBack: () -> Unit, onNext: () -> Unit) {
    val zone = s.zone()
    val today = remember(s) { PrayerEngine.times(s, LocalDate.now(zone), zone) }
    val zoneWarn = Setup.zoneLooksWrong(s.longitude, zone)
    var pickZone by remember { mutableStateOf(false) }
    var pickMethod by remember { mutableStateOf(false) }

    Text(Str[R.string.s_check_these_before_trusting_the_times], fontFamily = Cormorant, fontSize = 30.sp, color = Palette.ivory, modifier = Modifier.semantics { heading() })
    Text("Compare today's Fajr and Maghrib with your masjid. If they differ by more than a couple of minutes, change the convention here or import your masjid's timetable later in Settings › Location.", fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary, lineHeight = 20.sp)

    SettingRow(Str[R.string.s_place], Setup.coordLabel(s.latitude, s.longitude)) { GoldValue(s.locationName) }
    SettingRow(Str[R.string.s_time_zone], if (zoneWarn) Str[R.string.s_this_zone_is_hours_away_from] else if (s.zoneId == null) Str[R.string.s_using_the_device_s_zone] else Str[R.string.s_from_the_chosen_place], onClick = { pickZone = true }) {
        Text(zone.id + " ›", fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = if (zoneWarn) Palette.gold else Palette.goldSoft)
    }
    SettingRow(Str[R.string.s_convention], s.method.detail, onClick = { pickMethod = true }) { GoldValue(s.method.label + " ›") }
    SettingRow("ʿAsr", Str[R.string.s_hanafi_asr_begins_later_shadow_2_2]) {
        Chips(AsrMethod.entries.map { it.label }, AsrMethod.entries.indexOf(s.asrMethod)) { i -> store.update { it.copy(asrMethod = AsrMethod.entries[i]) } }
    }
    Spacer(Modifier.height(4.dp))
    Text("Today · ${LocalDate.now(zone)}", fontFamily = Cormorant, fontSize = 22.sp, color = Palette.ivory, modifier = Modifier.semantics { heading() })
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA).forEach { p ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.semantics(mergeDescendants = true) {}) {
                Text(p.english, fontFamily = Nunito, fontSize = 12.sp, color = Palette.textSecondary)
                Text(PrayerEngine.clock(today[p], s.use24h), fontFamily = Cormorant, fontSize = 24.sp, color = if (p == Prayer.FAJR || p == Prayer.MAGHRIB) Palette.goldSoft else Palette.ivory)
                Text(PrayerEngine.suffix(today[p], s.use24h), fontFamily = Nunito, fontSize = 11.sp, color = Palette.textMuted)
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text(Str[R.string.s_back], color = Palette.textSecondary) }
        GoldButton(if (zoneWarn) Str[R.string.s_fix_the_time_zone_first] else Str[R.string.s_these_look_right], enabled = !zoneWarn, onClick = onNext)
    }

    if (pickZone) ZonePicker(current = s.zoneId, onPick = { pickZone = false }, onDismiss = { pickZone = false }, store = store)
    if (pickMethod) AlertDialog(
        onDismissRequest = { pickMethod = false }, containerColor = Palette.panelRaised,
        title = { Text(Str[R.string.s_calculation_convention], fontFamily = Cormorant, fontSize = 28.sp, color = Palette.ivory) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Method.entries.forEach { m ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = androidx.compose.ui.semantics.Role.RadioButton) { store.update { it.copy(method = m) }; pickMethod = false }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(18.dp).clip(CircleShape).background(if (m == s.method) Palette.gold else Color.Transparent).padding(2.dp))
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(m.label, fontFamily = Nunito, fontSize = 16.sp, color = Palette.ivory)
                            Text(m.detail, fontFamily = Nunito, fontSize = 12.sp, color = Palette.textSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { pickMethod = false }) { Text(Str[R.string.s_close], color = Palette.goldSoft) } }
    )
}

@Composable
internal fun ZonePicker(current: String?, onPick: (String?) -> Unit, onDismiss: () -> Unit, store: SettingsStore) {
    var zoneQuery by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Palette.panelRaised,
        title = { Text(Str[R.string.s_time_zone], fontFamily = Cormorant, fontSize = 28.sp, color = Palette.ivory) },
        text = {
            Column {
                OutlinedTextField(value = zoneQuery, onValueChange = { zoneQuery = it }, placeholder = { Text(Str[R.string.s_search_e_g_karachi_london]) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                val device = ZoneId.systemDefault().id
                val opts = listOf<String?>(null) + ZoneId.getAvailableZoneIds().filter { it.contains('/') && !it.startsWith("Etc") && (zoneQuery.isBlank() || it.contains(zoneQuery, true)) }.sorted().take(60)
                Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
                    opts.forEach { z ->
                        val label = z ?: "Device · $device"
                        Text(label, fontFamily = Nunito, fontSize = 15.sp, color = if (z == current) Palette.goldSoft else Palette.ivory,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = androidx.compose.ui.semantics.Role.RadioButton) { store.update { it.copy(zoneId = z, zoneManual = true, zoneNeedsReview = false) }; onPick(z) }.padding(vertical = 12.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(Str[R.string.s_close], color = Palette.goldSoft) } }
    )
}

@Composable
private fun AlertsStep(store: SettingsStore, s: AppSettings, onBack: () -> Unit, onFinish: () -> Unit) {
    val ctx = LocalContext.current
    val phase by AzaanService.phase.collectAsState()
    Text(Str[R.string.s_will_the_azaan_reach_you], fontFamily = Cormorant, fontSize = 30.sp, color = Palette.ivory, modifier = Modifier.semantics { heading() })
    Text(Str[R.string.s_the_azaan_plays_on_the_alarm], fontFamily = Nunito, fontSize = 14.sp, color = Palette.textSecondary, lineHeight = 20.sp)
    ReliabilityRows()
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        if (phase == null) GoldButton(Str[R.string.s_play_a_test_azaan]) { AzaanService.preview(ctx, Prayer.DHUHR) }
        else GoldButton(Str[R.string.s_stop]) { AzaanService.stop(ctx) }
        Text("Plays at the azaan volume (${s.azaanVolume}%). Adjust it in Settings › Azaan & alerts.", fontFamily = Nunito, fontSize = 13.sp, color = Palette.textMuted, modifier = Modifier.weight(1f))
    }
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text(Str[R.string.s_back], color = Palette.textSecondary) }
        GoldButton(Str[R.string.s_finish], enabled = s.locationSet, onClick = onFinish)
    }
    if (!Reliability.allGood(ctx)) Text(Str[R.string.s_you_can_finish_now_the_reliability], fontFamily = Nunito, fontSize = 13.sp, color = Palette.textMuted, lineHeight = 18.sp)
}
