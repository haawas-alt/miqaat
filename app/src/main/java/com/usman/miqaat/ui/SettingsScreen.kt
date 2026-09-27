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

enum class Section(val label: String, val icon: ImageVector) {
    LOCATION("Location", Icons.Outlined.LocationOn),
    TIMES("Prayer times", Icons.Outlined.Schedule),
    AZAAN("Azaan & alerts", Icons.Outlined.NotificationsActive),
    IQAMAH("Iqamah", Icons.Outlined.Timer),
    HIJRI("Hijri calendar", Icons.Outlined.CalendarMonth),
    DISPLAY("Display & art", Icons.Outlined.Brush),
    TEST("Test & preview", Icons.Outlined.PlayCircle),
    HEALTH("Health & backup", Icons.Outlined.MonitorHeart),
    PRIVACY("Privacy", Icons.Outlined.Lock),
    ABOUT("About", Icons.Outlined.Info)
}

@Composable
fun SettingsScreen(store: SettingsStore, settings: AppSettings, initial: Section = Section.TIMES, onBack: () -> Unit) {
    var section by rememberSaveable { mutableStateOf(initial) }
    val ctx = LocalContext.current
    // Any change that affects times re-arms the alarm chain.
    LaunchedEffect(settings) { AzaanScheduler.reschedule(ctx) }

    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().background(Palette.panel)) {
    val compact = maxWidth < 720.dp
    if (compact) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Palette.ivory) }
                Text("Settings", fontFamily = Cormorant, fontSize = 30.sp, color = Palette.ivory)
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Section.entries.forEach { sec ->
                    val cur = sec == section
                    Box(Modifier.clip(RoundedCornerShape(50)).background(if (cur) Palette.gold else Color.Transparent).border(1.dp, if (cur) Palette.gold else Color.White.copy(alpha = 0.25f), RoundedCornerShape(50)).clickable { section = sec }.padding(horizontal = 14.dp, vertical = 8.dp)) {
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
        Column(Modifier.width(300.dp).fillMaxHeight().background(Color.Black.copy(alpha = 0.18f)).padding(vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp, bottom = 16.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Palette.ivory) }
                Text("Settings", fontFamily = Cormorant, fontSize = 34.sp, color = Palette.ivory)
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
        else status = "Location permission was not granted. Choose a city below instead."
    }
    fun detectNow() {
        if (LocationRepo.hasPermission(ctx)) scope.launch { busy = true; status = detect(ctx, store); busy = false }
        else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    Heading("Location", "Prayer times are calculated for these coordinates. Looking up the place name and searching for places uses Android's geocoder, which sends the coordinates or search text to Google. Nothing else leaves the device.")
    SettingRow("Current location", "%.4f, %.4f".format(s.latitude, s.longitude)) { GoldValue(s.locationName) }
    var pickZone by remember { mutableStateOf(false) }
    var zoneQuery by remember { mutableStateOf("") }
    SettingRow("Time zone for prayer times", "Must match the place above. Detected locations use the device's zone automatically.", onClick = { pickZone = true }) {
        GoldValue((s.zoneId ?: "Device · ${java.time.ZoneId.systemDefault().id}") + " ›")
    }
    if (pickZone) AlertDialog(
        onDismissRequest = { pickZone = false }, containerColor = Palette.panelRaised,
        title = { Text("Time zone", fontFamily = Cormorant, fontSize = 28.sp, color = Palette.ivory) },
        text = {
            Column {
                OutlinedTextField(value = zoneQuery, onValueChange = { zoneQuery = it }, placeholder = { Text("Search, e.g. Karachi, London") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                val opts = listOf("Device · ${java.time.ZoneId.systemDefault().id}") + java.time.ZoneId.getAvailableZoneIds().filter { it.contains('/') && !it.startsWith("Etc") && (zoneQuery.isBlank() || it.contains(zoneQuery, true)) }.sorted().take(60)
                Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
                    opts.forEach { z ->
                        Text(z, fontFamily = Nunito, fontSize = 15.sp, color = Palette.ivory, modifier = Modifier.fillMaxWidth().clickable { store.update { it.copy(zoneId = if (z.startsWith("Device")) null else z) }; pickZone = false }.padding(vertical = 10.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { pickZone = false }) { Text("Close", color = Palette.goldSoft) } }
    )
    SettingRow("Use the tablet's location", "Re-detects each time the app opens") {
        Toggle(s.autoLocation) { on -> store.update { it.copy(autoLocation = on) }; if (on) detectNow() }
    }
    Row(Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GoldButton(if (busy) "Detecting…" else "Detect now", enabled = !busy) { detectNow() }
    }
    if (status.isNotEmpty()) Text(status, fontFamily = Nunito, fontSize = 14.sp, color = Palette.goldSoft, modifier = Modifier.padding(bottom = 8.dp))

    Spacer(Modifier.height(10.dp))
    Text("Or choose a place", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    OutlinedTextField(
        value = query, onValueChange = { query = it; scope.launch { results = LocationRepo.search(ctx, it) } },
        placeholder = { Text("Search a suburb or city") }, singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
    )
    (if (query.isBlank()) LocationRepo.presets else results).take(10).forEach { p ->
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .clickable { store.update { it.copy(latitude = p.lat, longitude = p.lng, locationName = p.name, autoLocation = false, zoneId = p.zone) }; status = "Set to ${p.name}" + (if (p.zone == null) " · times shown in the device's time zone" else "") }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(p.name, fontFamily = Nunito, fontSize = 16.sp, color = Palette.ivory)
            Text("%.2f, %.2f".format(p.lat, p.lng), fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.5f))
        }
        HorizontalDivider(color = Palette.line)
    }

    // ---- Traveller
    Spacer(Modifier.height(22.dp))
    Text("Travelling", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    val homeSet = s.homeLat != null && s.homeLng != null
    val dist = if (homeSet) PrayerEngine.distanceKm(s.homeLat!!, s.homeLng!!, s.latitude, s.longitude) else 0.0
    SettingRow("Home", if (homeSet) "%.0f km from the current location".format(dist) else "Not set. Detect your location at home once, or set it now.") {
        TextButton(onClick = { store.update { it.copy(homeLat = it.latitude, homeLng = it.longitude) } }) { Text(if (homeSet) "Set home to here" else "Set home", color = Palette.goldSoft) }
    }
    SettingRow("Traveller mode", "When you are 80 km or more from home, show a travel chip and the options below. Times always follow the current place.") { Toggle(s.travellerMode) { on -> store.update { it.copy(travellerMode = on) } } }
    if (s.travellerMode) {
        SettingRow("Shorten 4-rakʿah prayers (qaṣr)", "A reminder on the cards for Dhuhr, ʿAsr and Isha while travelling. Permitted for a traveller who has not settled; conditions differ by madhab.") { Toggle(s.travelQasr) { on -> store.update { it.copy(travelQasr = on) } } }
        SettingRow("Combining prayers (jamʿ)", "Shows Dhuhr + ʿAsr and Maghrib + Isha as pairs and plays one azaan per pair. Ask your imam about your situation; this is a convenience, not a ruling.") { Toggle(s.travelJam) { on -> store.update { it.copy(travelJam = on) } } }
    }

    // ---- Masjid timetable
    Spacer(Modifier.height(22.dp))
    Text("Masjid timetable", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    Text("Use your masjid's published times instead of the calculation for the days it covers. Import a CSV or text file with one line per day: date, then Fajr, Sunrise, Dhuhr, ʿAsr, Maghrib, Isha, and optionally the five iqamah times. Calculated times take over again after the last day.", fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.7f), lineHeight = 20.sp)
    var importNotes by remember { mutableStateOf<List<String>>(emptyList()) }
    val pickSheet = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val text = runCatching { ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() }.getOrNull() ?: ""
        val r = PrayerEngine.parseTimetable(text, LocalDate.now().year)
        importNotes = r.notes
        if (r.rows.isNotEmpty()) store.update { it.copy(overrides = it.overrides + r.rows, useOverrides = true) }
    }
    OutlinedTextField(value = s.masjidName, onValueChange = { v -> store.update { it.copy(masjidName = v) } }, placeholder = { Text("Masjid name, e.g. Lakemba Masjid") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))
    val days = s.overrides.keys.sorted()
    SettingRow("Imported days", if (days.isEmpty()) "None yet" else "${days.size} days · ${days.first()} → ${days.last()}" + if (s.overrides.values.any { it.size >= 11 }) " · with iqamah" else "") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldButton("Import file") { pickSheet.launch(arrayOf("text/*", "text/csv", "text/comma-separated-values", "application/csv", "*/*")) }
            if (days.isNotEmpty()) TextButton(onClick = { store.update { it.copy(overrides = emptyMap()) } }) { Text("Clear", color = Palette.ivory.copy(alpha = 0.7f)) }
        }
    }
    if (days.isNotEmpty()) SettingRow("Use masjid times", "Off keeps the file but shows calculated times") { Toggle(s.useOverrides) { on -> store.update { it.copy(useOverrides = on) } } }
    importNotes.forEach { Text(it, fontFamily = Nunito, fontSize = 13.sp, color = Palette.goldSoft, modifier = Modifier.padding(top = 4.dp)) }
    if (days.isNotEmpty()) {
        val first = s.overrides.getValue(days.first())
        val calc = PrayerEngine.calculated(s, LocalDate.parse(days.first()))
        Text("Check · ${days.first()}: masjid Fajr %d:%02d vs calculated %s · Maghrib %d:%02d vs %s".format(first[0] / 60, first[0] % 60, PrayerEngine.clock(calc[Prayer.FAJR], true), first[4] / 60, first[4] % 60, PrayerEngine.clock(calc[Prayer.MAGHRIB], true)),
            fontFamily = Nunito, fontSize = 12.sp, color = Palette.ivory.copy(alpha = 0.6f), modifier = Modifier.padding(top = 6.dp))
    }
}

suspend fun detect(ctx: Context, store: SettingsStore): String {
    val loc = LocationRepo.current(ctx) ?: return "Could not get a location fix. Is location turned on in the tablet's settings?"
    val name = LocationRepo.name(ctx, loc.latitude, loc.longitude) ?: store.value.locationName
    store.update { it.copy(latitude = loc.latitude, longitude = loc.longitude, locationName = name, autoLocation = true, zoneId = null,
        homeLat = it.homeLat ?: loc.latitude, homeLng = it.homeLng ?: loc.longitude) }
    return "Location set to $name"
}

@Composable
private fun TimesSection(store: SettingsStore, s: AppSettings) {
    var pickMethod by remember { mutableStateOf(false) }
    var pickLat by remember { mutableStateOf(false) }
    Heading("Prayer times", "Match your local masjid exactly. Most Australian mosques follow the Muslim World League convention (Fajr 18°, Isha 17°).")
    SettingRow("Calculation method", s.method.detail, onClick = { pickMethod = true }) { GoldValue(s.method.label + " ›") }
    SettingRow("Asr juristic method", "Hanafi Asr begins later (shadow = 2× object)") {
        Chips(AsrMethod.entries.map { it.label }, AsrMethod.entries.indexOf(s.asrMethod)) { i -> store.update { it.copy(asrMethod = AsrMethod.entries[i]) } }
    }
    SettingRow("High-latitude rule", "Only matters above 48° latitude", onClick = { pickLat = true }) { Value(s.latitudeRule.label + " ›") }
    SettingRow("Show end times", "\"ends 5:57\" under each prayer · Isha ends at sharʿī midnight") { Toggle(s.showEndTimes) { on -> store.update { it.copy(showEndTimes = on) } } }
    SettingRow("Show disliked times for voluntary prayer", "A thin day bar: after sunrise, zawāl, after ʿAsr. Tap ⓘ on any prayer for \"why this time?\"") { Toggle(s.showDisliked) { on -> store.update { it.copy(showDisliked = on) } } }
    SettingRow("Show Sunrise on the home screen", "Marks the end of Fajr time") { Toggle(s.showSunrise) { on -> store.update { it.copy(showSunrise = on) } } }
    SettingRow("Show \"azaan was … ago\" after each prayer", "Then the screen moves on to the next prayer") {
        Stepper(s.afterWindowMinutes, 0, 120, 5, " min") { v -> store.update { it.copy(afterWindowMinutes = v) } }
    }
    Spacer(Modifier.height(18.dp))
    Text("Jumuʿah", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow("Use a Jumuʿah time on Fridays", "Replaces Dhuhr on Fridays for the display and the azaan") { Toggle(s.jumuahEnabled) { on -> store.update { it.copy(jumuahEnabled = on) } } }
    if (s.jumuahEnabled) SettingRow("Jumuʿah azaan time", "Your masjid's first azaan; adjust in 5-minute steps") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StepBtn("−", s.jumuahMinutes > 11 * 60) { store.update { it.copy(jumuahMinutes = it.jumuahMinutes - 5) } }
            Text("%d:%02d %s".format(((s.jumuahMinutes / 60) + 11) % 12 + 1, s.jumuahMinutes % 60, if (s.jumuahMinutes >= 720) "PM" else "AM"), fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.width(90.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            StepBtn("+", s.jumuahMinutes < 15 * 60) { store.update { it.copy(jumuahMinutes = it.jumuahMinutes + 5) } }
        }
    }
    SettingRow("Friday reminders", "A Jumuʿah chip from Thursday Maghrib to Friday Maghrib that opens the Friday routine: al-Kahf, ṣalawāt, the hour of acceptance") { Toggle(s.fridayReminders) { on -> store.update { it.copy(fridayReminders = on) } } }
    SettingRow("Hour-of-acceptance reminder", "A quiet notification one hour before Friday's Maghrib") { Toggle(s.fridayHourReminder) { on -> store.update { it.copy(fridayHourReminder = on) } } }
    Spacer(Modifier.height(18.dp))
    Text("Ramaḍān", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow("Ramaḍān mode", "Suhoor and Iftar labels, fasting progress, and the iftar dua after Maghrib azaan. Automatic follows the Hijri date.") {
        Chips(RamadanMode.entries.map { it.label }, RamadanMode.entries.indexOf(s.ramadanMode)) { i -> store.update { it.copy(ramadanMode = RamadanMode.entries[i]) } }
    }
    SettingRow("Suhoor alarm", "A chime and notification this many minutes before Fajr, Ramaḍān only") { Stepper(s.suhoorAlarmMinutes, 0, 120, 5, " min", zeroLabel = "Off") { v -> store.update { it.copy(suhoorAlarmMinutes = v) } } }
    SettingRow("Tarāwīḥ", "Shown on the home screen in Ramaḍān as minutes after Isha") { Stepper(s.tarawihMinutesAfterIsha, 0, 120, 5, " min") { v -> store.update { it.copy(tarawihMinutesAfterIsha = v) } } }
    Spacer(Modifier.height(18.dp))
    Text("Minute adjustments", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    Text("Nudge each time by a few minutes to match the timetable printed at your masjid.", fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.7f))
    Prayer.entries.forEach { p ->
        SettingRow(p.english, null) {
            Stepper(s.adjustments[p] ?: 0, -30, 30, 1, " min", signed = true) { v -> store.update { it.copy(adjustments = it.adjustments + (p to v)) } }
        }
    }
    val today = remember(s) { PrayerEngine.times(s, LocalDate.now()) }
    Spacer(Modifier.height(14.dp))
    Text(
        "Today with these settings:  " + Prayer.entries.joinToString("   ") { "${it.english} ${PrayerEngine.clock(today[it], s.use24h)}" },
        fontFamily = Nunito, fontSize = 13.sp, color = Palette.goldSoft
    )

    if (pickMethod) PickerDialog("Calculation method", Method.entries.map { it.label to it.detail }, Method.entries.indexOf(s.method),
        onPick = { i -> store.update { it.copy(method = Method.entries[i]) } }) { pickMethod = false }
    if (pickLat) PickerDialog("High-latitude rule", LatitudeRule.entries.map { it.label to "" }, LatitudeRule.entries.indexOf(s.latitudeRule),
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

    Heading("Azaan & alerts", "The azaan plays through the alarm channel, so it sounds even when the tablet is silenced.")
    if (next != null) Text(
        "Next: ${next.prayer.english} ${if (next.reminder) "reminder" else "azaan"} at ${PrayerEngine.clock(next.at, s.use24h)} ${PrayerEngine.suffix(next.at, s.use24h)}",
        fontFamily = Nunito, fontSize = 14.sp, color = Palette.goldSoft, modifier = Modifier.padding(bottom = 10.dp)
    )
    Prayer.prayersOnly.forEach { p ->
        SettingRow("${p.english}  ${p.arabic}", null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TextButton(onClick = { AzaanService.preview(ctx, p) }) { Text("Play", color = Palette.goldSoft) }
                Toggle(s.azaanEnabled[p] == true) { on -> store.update { it.copy(azaanEnabled = it.azaanEnabled + (p to on)) } }
            }
        }
    }
    SettingRow("Volume", "${s.azaanVolume}% of the alarm volume") {
        Slider(value = s.azaanVolume / 100f, onValueChange = { v -> store.update { it.copy(azaanVolume = (v * 100).toInt()) } }, modifier = Modifier.width(220.dp))
    }
    SettingRow("Reminder before azaan", "A quiet notification, no sound") {
        Stepper(s.preReminderMinutes, 0, 30, 5, " min", zeroLabel = "Off") { v -> store.update { it.copy(preReminderMinutes = v) } }
    }
    Spacer(Modifier.height(18.dp))
    Text("After the azaan", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    Text("When the azaan finishes: the dua after azaan (held until its narration ends), then one ṣaḥīḥ hadith, then back to the clock.", fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.7f))
    SettingRow("Dua and hadith after each azaan", "For all five prayers") { Toggle(s.afterAzaanEnabled) { on -> store.update { it.copy(afterAzaanEnabled = on) } } }
    SettingRow("Narration", "Studio recordings are built in for the dua and every hadith, Arabic and English. The tablet's voice is only used if a recording is missing.") {
        Chips(Narration.entries.map { it.label }, Narration.entries.indexOf(s.narration)) { i -> store.update { it.copy(narration = Narration.entries[i]) } }
    }
    SettingRow("Hadith stays on screen for", "Counted from when the hadith appears") {
        Stepper(s.hadithMinutes, 1, 10, 1, " min") { v -> store.update { it.copy(hadithMinutes = v) } }
    }
    SettingRow("Hadith source", "${HadithLibrary.all.size} narrations from Ṣaḥīḥ al-Bukhārī and Ṣaḥīḥ Muslim, each cited with its number. One per azaan, no repeats until all have been shown.") {
        TextButton(onClick = { AzaanService.previewAfter(ctx, Prayer.DHUHR) }) { Text("Preview", color = Palette.goldSoft) }
    }
    Spacer(Modifier.height(18.dp))
    Text("Azaan recording", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    Text(
        "Two recordings are built in: one for Fajr and one for the other prayers. You can replace either with any MP3 on the tablet.",
        fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.7f)
    )
    SettingRow("Azaan file", s.azaanUri?.let { Uri.parse(it).lastPathSegment } ?: "Built-in", onClick = { pickFile.launch(arrayOf("audio/*")) }) {
        Row {
            if (s.azaanUri != null) TextButton(onClick = { store.update { it.copy(azaanUri = null) } }) { Text("Reset", color = Palette.ivory.copy(alpha = 0.7f)) }
            Value("Choose ›")
        }
    }
    SettingRow("Fajr azaan file", s.fajrAzaanUri?.let { Uri.parse(it).lastPathSegment } ?: "Same as above", onClick = { pickFajr.launch(arrayOf("audio/*")) }) {
        Row {
            if (s.fajrAzaanUri != null) TextButton(onClick = { store.update { it.copy(fajrAzaanUri = null) } }) { Text("Reset", color = Palette.ivory.copy(alpha = 0.7f)) }
            Value("Choose ›")
        }
    }
    Spacer(Modifier.height(18.dp))
    Text("Reliability", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
    val ignoring = pm.isIgnoringBatteryOptimizations(ctx.packageName)
    SettingRow("Battery optimisation", if (ignoring) "Miqaat is exempt, so the azaan fires on time" else "Recommended: exempt Miqaat so Android never delays the azaan",
        onClick = { runCatching { ctx.startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}"))) } }) {
        Value(if (ignoring) "Exempt ✓" else "Fix ›")
    }
    if (Build.VERSION.SDK_INT >= 34) {
        val nm = ctx.getSystemService(android.app.NotificationManager::class.java)
        if (!nm.canUseFullScreenIntent()) SettingRow("Full-screen azaan", "Needed to show the azaan when the screen is locked", onClick = { runCatching { ctx.startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${ctx.packageName}"))) } }) { Value("Allow ›") }
    }
    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
        SettingRow("Notifications", "Needed for the azaan to wake the screen", onClick = { ask.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Value("Allow ›") }
    }
    if (Build.VERSION.SDK_INT >= 31) SettingRow("Exact alarms", "Needed on Android 12 and newer", onClick = { runCatching { ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)) } }) { Value("Open ›") }
}

@Composable
private fun IqamahSection(store: SettingsStore, s: AppSettings) {
    val today = remember(s) { PrayerEngine.times(s, LocalDate.now()) }
    Heading("Iqamah", "For praying in congregation at home. A full-screen countdown starts before each iqamah, a sound marks the iqamah itself, then the screen goes quiet for the prayer.")
    SettingRow("Iqamah times", "Shown under each azaan time and announced with the countdown") { Toggle(s.iqamahEnabled) { on -> store.update { it.copy(iqamahEnabled = on) } } }
    if (s.iqamahEnabled) {
        Spacer(Modifier.height(10.dp))
        Text("Iqamah for each prayer", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
        Text("Either a number of minutes after the azaan, or a fixed clock time (for example Fajr always at 5:00). If a fixed time would fall before the azaan on a given day, that day uses azaan + 5 min instead.", fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.7f))
        Prayer.prayersOnly.forEach { p ->
            val fixed = s.iqamahIsFixed[p] == true
            val off = s.iqamahOffsets[p] ?: 0
            val at = s.iqamahFixed[p] ?: 12 * 60
            val iq = PrayerEngine.iqamah(s, today, p)
            SettingRow("${p.english}  ${p.arabic}", if (iq != null) "Today: azaan ${PrayerEngine.clock(today[p], s.use24h)} ${PrayerEngine.suffix(today[p], s.use24h)} → iqamah ${PrayerEngine.clock(iq, s.use24h)} ${PrayerEngine.suffix(iq, s.use24h)}" else "Off for this prayer") {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chips(listOf("After azaan", "Fixed time"), if (fixed) 1 else 0) { i -> store.update { it.copy(iqamahIsFixed = it.iqamahIsFixed + (p to (i == 1))) } }
                    if (fixed) TimeStepper(at, s.use24h) { v -> store.update { it.copy(iqamahFixed = it.iqamahFixed + (p to v)) } }
                    else Stepper(off, 0, 60, 1, " min", zeroLabel = "Off") { v -> store.update { it.copy(iqamahOffsets = it.iqamahOffsets + (p to v)) } }
                }
            }
        }
        if (s.jumuahEnabled) SettingRow("Jumuʿah iqamah (fixed time)", "Used instead of the Dhuhr offset on Fridays") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StepBtn("−", s.jumuahIqamahMinutes > 11 * 60) { store.update { it.copy(jumuahIqamahMinutes = it.jumuahIqamahMinutes - 5) } }
                Text("%d:%02d %s".format(((s.jumuahIqamahMinutes / 60) + 11) % 12 + 1, s.jumuahIqamahMinutes % 60, if (s.jumuahIqamahMinutes >= 720) "PM" else "AM"), fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.width(90.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                StepBtn("+", s.jumuahIqamahMinutes < 16 * 60) { store.update { it.copy(jumuahIqamahMinutes = it.jumuahIqamahMinutes + 5) } }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Countdown and sound", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
        SettingRow("Countdown before iqamah", "Full screen, seconds only, with a soft tick for the last ten. It interrupts the hadith if they overlap.") {
            Stepper(s.iqamahCountdownSeconds, 30, 180, 15, " s") { v -> store.update { it.copy(iqamahCountdownSeconds = v) } }
        }
        SettingRow("Sound at iqamah", "\"Iqamah recording\" plays res/raw/iqamah.mp3 if it has been bundled, otherwise the chime") {
            Chips(IqamahSound.entries.map { it.label }, IqamahSound.entries.indexOf(s.iqamahSound)) { i -> store.update { it.copy(iqamahSound = IqamahSound.entries[i]) } }
        }
        SettingRow("Quiet screen after iqamah", "Dim clock only, nothing moving. Tap the screen to wake early.") {
            Stepper(s.quietMinutes, 0, 30, 1, " min", zeroLabel = "Off") { v -> store.update { it.copy(quietMinutes = v) } }
        }
    }
}

@Composable
private fun TestSection(store: SettingsStore, s: AppSettings) {
    val ctx = LocalContext.current
    Heading("Test & preview", "Run any part of the experience right now, without waiting for a prayer time. Each one uses your current settings and the real sounds.")
    Text("Theme", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow("Preview a theme", "Applies straight away; press back to see the home screen") {
        Chips(AppTheme.entries.map { it.label }, AppTheme.entries.indexOf(s.theme)) { i -> store.update { it.copy(theme = AppTheme.entries[i]) } }
    }
    Spacer(Modifier.height(14.dp))
    Text("Azaan", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow("Everything, exactly as at prayer time", "Azaan → dua → hadith → back to the clock (about 8 minutes). Iqamah is not included unless one is scheduled.") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldButton("Fajr") { AzaanService.playFull(ctx, Prayer.FAJR) }
            GoldButton("Maghrib") { AzaanService.playFull(ctx, Prayer.MAGHRIB) }
        }
    }
    SettingRow("Azaan recording", "Plays the full recording with the azaan screen, then stops (no dua or hadith)") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldButton("Fajr") { AzaanService.preview(ctx, Prayer.FAJR) }
            GoldButton("Other prayers") { AzaanService.preview(ctx, Prayer.MAGHRIB) }
        }
    }
    SettingRow("Full sequence after azaan", "Dua after azaan → hadith → back to the clock, with narration") { GoldButton("Start") { AzaanService.previewAfter(ctx, Prayer.DHUHR) } }
    SettingRow("Ramaḍān Maghrib sequence", "Iftar dua → dua after azaan → hadith") { GoldButton("Start") { AzaanService.previewAfter(ctx, Prayer.MAGHRIB) } }
    Spacer(Modifier.height(14.dp))
    Text("Iqamah", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow("Countdown → iqamah → quiet screen", "The whole iqamah flow, starting with a ${s.iqamahCountdownSeconds}-second countdown") { GoldButton("Start") { AzaanService.testIqamah(ctx, Prayer.MAGHRIB) } }
    SettingRow("Short countdown", "Same flow, 15-second countdown, to hear the ticks quickly") { GoldButton("Start") { AzaanService.testIqamah(ctx, Prayer.MAGHRIB, 15) } }
    SettingRow("Iqamah sound only", "Plays the ${s.iqamahSound.label.lowercase()} and shows the iqamah screen") { GoldButton("Play") { AzaanService.testIqamahNow(ctx, Prayer.MAGHRIB) } }
    SettingRow("Quiet screen", "Shows the in-prayer screen for ${s.quietMinutes} min; tap it to leave") { GoldButton("Show") { AzaanService.testQuiet(ctx, Prayer.MAGHRIB) } }
    Spacer(Modifier.height(14.dp))
    Text("Home screen modes", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    SettingRow("Ramaḍān mode", "Force it on to see Suhoor/Iftar labels and the fasting bar on the home screen") {
        Chips(RamadanMode.entries.map { it.label }, RamadanMode.entries.indexOf(s.ramadanMode)) { i -> store.update { it.copy(ramadanMode = RamadanMode.entries[i]) } }
    }
    SettingRow("Stop anything that is playing", null) { TextButton(onClick = { AzaanService.stop(ctx) }) { Text("Stop", color = Palette.goldSoft) } }
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

    Heading("Health & backup", "Did it fire? Every azaan, iqamah and reminder is logged on this device with the time it actually happened.")
    Text("Last 7 days · $fired played" + (if (late > 0) " · $late late" else "") + (if (missed > 0) " · $missed missed" else " · none missed"), fontFamily = Cormorant, fontSize = 26.sp, color = if (missed > 0) Color(0xFFF08C8C) else if (late > 0) Color(0xFFF0A050) else Palette.mint)
    Spacer(Modifier.height(6.dp))
    SettingRow("Next alarm armed", next?.let { "${it.prayer.english} ${if (it.iqamah) "iqamah" else if (it.reminder) "reminder" else "azaan"} · ${PrayerEngine.clock(it.at, s.use24h)} ${PrayerEngine.suffix(it.at, s.use24h)}" } ?: "Nothing scheduled: turn on an azaan or iqamah") { Value(if (next != null) "✓" else "!") }
    SettingRow("Battery optimisation", if (pm.isIgnoringBatteryOptimizations(ctx.packageName)) "Miqaat is exempt" else "Not exempt: Android may delay alarms. Fix in Azaan & alerts.") { Value(if (pm.isIgnoringBatteryOptimizations(ctx.packageName)) "✓" else "!") }
    SettingRow("Launch on boot", if (s.launchOnBoot) "On" else "Off · recommended for the wall tablet") { Toggle(s.launchOnBoot) { on -> store.update { it.copy(launchOnBoot = on) } } }
    SettingRow("Time-change self-check", "Runs automatically after any clock or zone change and on daylight-saving nights; result appears in the log") { Value("✓") }

    Spacer(Modifier.height(16.dp))
    Text("Log", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    if (log.isEmpty()) Text("Nothing yet. Entries appear after the first azaan.", fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.6f))
    log.take(40).forEach { e ->
        val col = when (e.kind) { com.usman.miqaat.data.Health.Kind.MISSED -> Color(0xFFF08C8C); com.usman.miqaat.data.Health.Kind.TIME_CHANGE, com.usman.miqaat.data.Health.Kind.BOOT -> Palette.goldSoft; else -> if (e.lateBy > 1) Color(0xFFF0A050) else Palette.mint }
        Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.padding(top = 6.dp).size(9.dp).clip(androidx.compose.foundation.shape.CircleShape).background(col))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(e.title, fontFamily = Nunito, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory)
                Text(e.detail, fontFamily = Nunito, fontSize = 12.sp, color = Palette.ivory.copy(alpha = 0.6f))
            }
            val t = e.time(s.zone())
            Text(t.format(java.time.format.DateTimeFormatter.ofPattern("EEE d MMM · " + (if (s.use24h) "HH:mm" else "h:mm a"), java.util.Locale.ENGLISH)), fontFamily = Nunito, fontSize = 12.sp, color = Palette.ivory.copy(alpha = 0.6f))
        }
        HorizontalDivider(color = Palette.line)
    }
    if (log.isNotEmpty()) TextButton(onClick = { com.usman.miqaat.data.Health.clear(ctx); tick++ }) { Text("Clear log", color = Palette.ivory.copy(alpha = 0.7f)) }

    Spacer(Modifier.height(16.dp))
    Text("Backup & restore", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
    var msg by remember { mutableStateOf("") }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching { ctx.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(com.usman.miqaat.data.Health.exportSettings(ctx)) } }
            .onSuccess { msg = "Settings saved" }.onFailure { msg = "Couldn't save: ${it.message}" }
    }
    val load = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val text = runCatching { ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() }.getOrNull() ?: ""
        val n = com.usman.miqaat.data.Health.importSettings(ctx, text)
        msg = if (n > 0) "$n settings restored · restart Miqaat to apply everything" else "That file isn't a Miqaat backup"
    }
    SettingRow("Settings file", "Everything in Settings, as one small text file. Move it to a new tablet or keep it with your key.") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GoldButton("Save") { save.launch("miqaat-settings-${LocalDate.now()}.txt") }
            TextButton(onClick = { load.launch(arrayOf("text/*", "*/*")) }) { Text("Restore", color = Palette.goldSoft) }
        }
    }
    if (msg.isNotEmpty()) Text(msg, fontFamily = Nunito, fontSize = 13.sp, color = Palette.goldSoft, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun PrivacySection() {
    Heading("Privacy", "What Miqaat does with your data, on one screen, and it is true.")
    SettingRow("Prayer times", "Calculated on this device from your coordinates with the Adhan library. Never uploaded.") { Value("On device") }
    SettingRow("Place name & search", "Android's geocoder sends your coordinates, or the text you search, to Google to get a name back. Only when you detect or search a location.") { GoldValue("Google") }
    SettingRow("Updates", "Miqaat asks github.com whether a newer build exists and downloads it from there. GitHub sees your IP address, nothing else.") { GoldValue("GitHub") }
    SettingRow("Narration", "The dua and hadith recordings are inside the app. The tablet's own text-to-speech is used only if a recording is missing.") { Value("On device") }
    SettingRow("Health log, adhkār counts, Friday tracker", "Stored in the app's private storage on this device. Cleared when you uninstall.") { Value("On device") }
    SettingRow("Analytics, advertising, accounts, crash reporting", "None. There is no Miqaat server.") { Value("None") }
    SettingRow("Permissions", "Location (once, for times), notifications (azaan), exact alarms, install packages (self-update), ignore battery optimisation (reliability). No contacts, camera, microphone or storage beyond files you pick.") { Value("Minimal") }
    SettingRow("Source code", "github.com/haawas-alt/miqaat · builds are produced by GitHub Actions from the public source and signed with a private key.") { Value("Open") }
}

@Composable
private fun HijriSection(store: SettingsStore, s: AppSettings) {
    val h = PrayerEngine.hijri(LocalDate.now(), s.hijriOffsetDays)
    Heading("Hijri calendar", "Dates follow the Umm al-Qura calendar. If your local community's moon sighting differs, shift by a day.")
    SettingRow("Show Hijri date", "On the home screen and timetable") { Toggle(s.showHijri) { on -> store.update { it.copy(showHijri = on) } } }
    SettingRow("Adjustment", "Today is ${h.english}") {
        Stepper(s.hijriOffsetDays, -2, 2, 1, " day", signed = true) { v -> store.update { it.copy(hijriOffsetDays = v) } }
    }
    val tomorrow = PrayerEngine.hijri(LocalDate.now().plusDays(1), s.hijriOffsetDays)
    if (h.day >= 29) {
        Spacer(Modifier.height(14.dp))
        Text("Moon sighting tonight", fontFamily = Cormorant, fontSize = 24.sp, color = Palette.ivory)
        Text(
            if (tomorrow.day == 1) "The calendar already turns to ${tomorrow.english.substringAfter(' ')} tomorrow. If the moon was not sighted in your community, complete 30 days instead."
            else "Tomorrow is day 30 by calculation. If the moon was sighted in your community tonight, start the new month tomorrow.",
            fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.7f), lineHeight = 20.sp
        )
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (tomorrow.day != 1) GoldButton("Moon sighted · new month tomorrow") { store.update { it.copy(hijriOffsetDays = it.hijriOffsetDays + 1) } }
            else GoldButton("Not sighted · complete 30 days") { store.update { it.copy(hijriOffsetDays = it.hijriOffsetDays - 1) } }
        }
        Text("This shifts the Hijri date by one day; Ramaḍān mode and Friday/Eid features follow it.", fontFamily = Nunito, fontSize = 12.sp, color = Palette.ivory.copy(alpha = 0.55f), modifier = Modifier.padding(top = 6.dp))
    }
    Spacer(Modifier.height(16.dp))
    Text(h.arabic, fontFamily = Amiri, fontSize = 40.sp, color = Palette.goldSoft)
}

@Composable
private fun DisplaySection(store: SettingsStore, s: AppSettings) {
    Heading("Display & art", "How Miqaat looks, on the wall or in your hand.")
    SettingRow("Theme", "Changes the home screen and the azaan screens immediately; go back to the clock to see it") {
        Chips(AppTheme.entries.map { it.label }, AppTheme.entries.indexOf(s.theme)) { i -> store.update { it.copy(theme = AppTheme.entries[i]) } }
    }
    SettingRow("Language", "Home screen and widget. Urdu is set in Nastaʿlīq; settings stay in English for now.") {
        Chips(Language.entries.map { it.label }, Language.entries.indexOf(s.language)) { i -> store.update { it.copy(language = Language.entries[i]) } }
    }
    SettingRow("Large type", "One prayer, one time, one line, readable across a big room. Tap the screen to see everything for 25 seconds.") { Toggle(s.largeType) { on -> store.update { it.copy(largeType = on) } } }
    SettingRow("Home-screen widget", "Long-press your phone's home screen › Widgets › Miqaat. Shows the next prayer, countdown and the following two.") { Value("Phone") }
    SettingRow("Time format", null) { Chips(listOf("12-hour", "24-hour"), if (s.use24h) 1 else 0) { i -> store.update { it.copy(use24h = i == 1) } } }
    SettingRow("Keep the screen on", "While Miqaat is open. Best with the device plugged in.") { Toggle(s.keepScreenOn) { on -> store.update { it.copy(keepScreenOn = on) } } }
    SettingRow("Dim after Isha", "Softens the screen through the night until Fajr") { Toggle(s.nightDim) { on -> store.update { it.copy(nightDim = on) } } }
    SettingRow("Qibla direction on the home screen", "Tap it for the compass") { Toggle(s.showQibla) { on -> store.update { it.copy(showQibla = on) } } }
    SettingRow("Morning and evening adhkār", "A prompt after Fajr and after ʿAsr, with sourced texts and a tap counter") { Toggle(s.adhkarEnabled) { on -> store.update { it.copy(adhkarEnabled = on) } } }
    SettingRow("After-prayer adhkār", "A prompt for 40 minutes after each prayer: istighfār, the tasbīḥ, Āyat al-Kursī, the Quls") { Toggle(s.postPrayerAdhkar) { on -> store.update { it.copy(postPrayerAdhkar = on) } } }
    SettingRow("Learn to pray (children)", "A book icon on the home screen opens the words of the prayer, one position at a time") { Toggle(s.kidsMode) { on -> store.update { it.copy(kidsMode = on) } } }
    SettingRow("Art theme", null) { Chips(ArtTheme.entries.map { it.label }, ArtTheme.entries.indexOf(s.artTheme)) { i -> store.update { it.copy(artTheme = ArtTheme.entries[i]) } } }
    SettingRow("Open Miqaat when the device starts", "So the wall tablet comes back after a power cut") { Toggle(s.launchOnBoot) { on -> store.update { it.copy(launchOnBoot = on) } } }
}

@Composable
private fun AboutSection(s: AppSettings) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val up by Updater.state.collectAsState()
    LaunchedEffect(Unit) { Updater.check(ctx) }
    Heading("About Miqaat", "ميقات · an appointed time")
    Text("Version ${Updater.currentName} · build ${Updater.currentBuild}", fontFamily = Nunito, fontSize = 15.sp, color = Palette.goldSoft)
    Spacer(Modifier.height(10.dp))
    when (val u = up) {
        is Updater.State.Available -> {
            SettingRow("Update available: version ${u.info.versionName}", if (Updater.canInstall(ctx)) "Downloads from GitHub and opens the installer. Your settings are kept." else "First allow Miqaat to install updates (one-time Android permission), then come back here.") {
                if (Updater.canInstall(ctx)) GoldButton("Download & install") { Updater.download(ctx, u.info) }
                else GoldButton("Allow installs") { Updater.openInstallPermission(ctx) }
            }
        }
        is Updater.State.Downloading -> SettingRow("Downloading version ${u.info.versionName}…", "The installer opens automatically when it finishes") { Value("…") }
        is Updater.State.Ready -> SettingRow("Update downloaded", "Tap if the installer didn't open") { GoldButton("Install") { Updater.install(ctx, u.file) } }
        is Updater.State.Failed -> SettingRow("Update check failed", u.reason) { GoldButton("Try again") { scope.launch { Updater.check(ctx, force = true) } } }
        Updater.State.Checking -> SettingRow("Checking for updates…", null) { Value("…") }
        Updater.State.UpToDate -> SettingRow("You have the latest version", "Checked just now") { TextButton(onClick = { scope.launch { Updater.check(ctx, force = true) } }) { Text("Check again", color = Palette.goldSoft) } }
        Updater.State.Idle -> SettingRow("Updates", "New builds are published automatically") { GoldButton("Check for updates") { scope.launch { Updater.check(ctx, force = true) } } }
    }
    Spacer(Modifier.height(14.dp))
    Text(
        "Prayer times are computed on the tablet with the Adhan library (Batoul Apps, MIT licence), using the high-precision astronomical algorithms of Jean Meeus. " +
            "No account, no advertising, no analytics. Network is used for three things only: the place-name lookup and place search (Android's geocoder, which contacts Google), and the update check against GitHub.\n\n" +
            "Current: ${s.method.label}, Asr ${s.asrMethod.label}, ${s.locationName} (%.3f, %.3f).".format(s.latitude, s.longitude),
        fontFamily = Nunito, fontSize = 15.sp, color = Palette.ivory.copy(alpha = 0.8f), lineHeight = 22.sp
    )
}

// ---------------------------------------------------------------- controls

@Composable
private fun Heading(title: String, desc: String) {
    Text(title, fontFamily = Cormorant, fontSize = 34.sp, color = Palette.ivory)
    Text(desc, fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.7f), lineHeight = 20.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))
}

@Composable
private fun SettingRow(title: String, subtitle: String?, onClick: (() -> Unit)? = null, trailing: @Composable () -> Unit) {
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val compact = maxWidth < 560.dp
        if (compact) {
            // Phone: label on top, control underneath, so neither squeezes the other.
            Column(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 14.dp)) {
                Text(title, fontFamily = Nunito, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory)
                if (subtitle != null) Text(subtitle, fontFamily = Nunito, fontSize = 13.sp, color = Palette.ivory.copy(alpha = 0.6f), lineHeight = 18.sp)
                Box(Modifier.padding(top = 10.dp).fillMaxWidth(), contentAlignment = Alignment.CenterStart) { trailing() }
            }
        } else {
            Row(
                Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f).padding(end = 20.dp)) {
                    Text(title, fontFamily = Nunito, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory)
                    if (subtitle != null) Text(subtitle, fontFamily = Nunito, fontSize = 13.sp, color = Palette.ivory.copy(alpha = 0.6f))
                }
                trailing()
            }
        }
    }
    HorizontalDivider(color = Palette.line)
}

@Composable private fun Value(t: String) = Text(t, fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.ivory.copy(alpha = 0.85f))
@Composable private fun GoldValue(t: String) = Text(t, fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft)

@Composable
private fun Toggle(on: Boolean, onChange: (Boolean) -> Unit) = Switch(
    checked = on, onCheckedChange = onChange,
    colors = SwitchDefaults.colors(checkedThumbColor = Palette.night, checkedTrackColor = Palette.gold, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.White.copy(alpha = 0.2f))
)

@Composable
private fun GoldButton(label: String, enabled: Boolean = true, onClick: () -> Unit) =
    Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = Palette.gold, contentColor = Palette.night)) {
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold)
    }

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun Chips(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { i, l ->
            val cur = i == selected
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(if (cur) Palette.gold else Color.Transparent)
                    .border(1.dp, if (cur) Palette.gold else Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                    .clickable { onSelect(i) }.padding(horizontal = 14.dp, vertical = 8.dp)
            ) { Text(l, fontFamily = Nunito, fontSize = 13.sp, fontWeight = if (cur) FontWeight.Bold else FontWeight.Normal, color = if (cur) Palette.night else Palette.ivory) }
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
        Text(label, fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.width(78.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
        Text(fmt(minutes), fontFamily = Nunito, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Palette.goldSoft, modifier = Modifier.width(92.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        StepBtn("+5", minutes <= 24 * 60 - 10) { onChange(minutes + 5) }
        StepBtn("+1h", minutes <= 23 * 60 - 5) { onChange(minutes + 60) }
    }
}

@Composable
private fun StepBtn(t: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.height(38.dp).widthIn(min = 38.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, Color.White.copy(alpha = if (enabled) 0.3f else 0.1f), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 8.dp), contentAlignment = Alignment.Center
    ) { Text(t, fontSize = if (t.length > 1) 13.sp else 20.sp, fontFamily = Nunito, fontWeight = FontWeight.Bold, color = Palette.ivory.copy(alpha = if (enabled) 1f else 0.3f)) }
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
                            if (d.isNotEmpty()) Text(d, fontFamily = Nunito, fontSize = 13.sp, color = Palette.ivory.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = Palette.goldSoft) } }
    )
}
