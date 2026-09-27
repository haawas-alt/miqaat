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
import androidx.compose.foundation.rememberScrollState
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
import com.usman.miqaat.data.HadithLibrary
import com.usman.miqaat.data.Place
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.SettingsStore
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class Section(val label: String, val icon: ImageVector) {
    LOCATION("Location", Icons.Outlined.LocationOn),
    TIMES("Prayer times", Icons.Outlined.Schedule),
    AZAAN("Azaan & alerts", Icons.Outlined.NotificationsActive),
    HIJRI("Hijri calendar", Icons.Outlined.CalendarMonth),
    DISPLAY("Display & art", Icons.Outlined.Brush),
    ABOUT("About", Icons.Outlined.Info)
}

@Composable
fun SettingsScreen(store: SettingsStore, settings: AppSettings, initial: Section = Section.TIMES, onBack: () -> Unit) {
    var section by rememberSaveable { mutableStateOf(initial) }
    val ctx = LocalContext.current
    // Any change that affects times re-arms the alarm chain.
    LaunchedEffect(settings) { AzaanScheduler.reschedule(ctx) }

    Row(Modifier.fillMaxSize().background(Palette.panel)) {
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
                Section.HIJRI -> HijriSection(store, settings)
                Section.DISPLAY -> DisplaySection(store, settings)
                Section.ABOUT -> AboutSection(settings)
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

    Heading("Location", "Prayer times are calculated for these coordinates. The name is what the home screen shows.")
    SettingRow("Current location", "%.4f, %.4f".format(s.latitude, s.longitude)) { GoldValue(s.locationName) }
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
                .clickable { store.update { it.copy(latitude = p.lat, longitude = p.lng, locationName = p.name, autoLocation = false) }; status = "Set to ${p.name}" }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(p.name, fontFamily = Nunito, fontSize = 16.sp, color = Palette.ivory)
            Text("%.2f, %.2f".format(p.lat, p.lng), fontFamily = Nunito, fontSize = 14.sp, color = Palette.ivory.copy(alpha = 0.5f))
        }
        HorizontalDivider(color = Palette.line)
    }
}

suspend fun detect(ctx: Context, store: SettingsStore): String {
    val loc = LocationRepo.current(ctx) ?: return "Could not get a location fix. Is location turned on in the tablet's settings?"
    val name = LocationRepo.name(ctx, loc.latitude, loc.longitude) ?: store.value.locationName
    store.update { it.copy(latitude = loc.latitude, longitude = loc.longitude, locationName = name, autoLocation = true) }
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
    SettingRow("Show Sunrise on the home screen", "Marks the end of Fajr time") { Toggle(s.showSunrise) { on -> store.update { it.copy(showSunrise = on) } } }
    SettingRow("Show \"azaan was … ago\" after each prayer", "Then the screen moves on to the next prayer") {
        Stepper(s.afterWindowMinutes, 0, 120, 5, " min") { v -> store.update { it.copy(afterWindowMinutes = v) } }
    }
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
    SettingRow("Narration", "Uses the tablet's text-to-speech voice. Arabic is read only if an Arabic voice is installed.") {
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
    if (Build.VERSION.SDK_INT >= 31) SettingRow("Exact alarms", "Needed on Android 12 and newer", onClick = { runCatching { ctx.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)) } }) { Value("Open ›") }
}

@Composable
private fun HijriSection(store: SettingsStore, s: AppSettings) {
    val h = PrayerEngine.hijri(LocalDate.now(), s.hijriOffsetDays)
    Heading("Hijri calendar", "Dates follow the Umm al-Qura calendar. If your local community's moon sighting differs, shift by a day.")
    SettingRow("Show Hijri date", "On the home screen and timetable") { Toggle(s.showHijri) { on -> store.update { it.copy(showHijri = on) } } }
    SettingRow("Adjustment", "Today is ${h.english}") {
        Stepper(s.hijriOffsetDays, -2, 2, 1, " day", signed = true) { v -> store.update { it.copy(hijriOffsetDays = v) } }
    }
    Spacer(Modifier.height(16.dp))
    Text(h.arabic, fontFamily = Amiri, fontSize = 40.sp, color = Palette.goldSoft)
}

@Composable
private fun DisplaySection(store: SettingsStore, s: AppSettings) {
    Heading("Display & art", "Made for a tablet that stays on. Everything here is about how it looks from across the room.")
    SettingRow("Time format", null) { Chips(listOf("12-hour", "24-hour"), if (s.use24h) 1 else 0) { i -> store.update { it.copy(use24h = i == 1) } } }
    SettingRow("Keep the screen on", "While Miqaat is open. Best with the tablet plugged in.") { Toggle(s.keepScreenOn) { on -> store.update { it.copy(keepScreenOn = on) } } }
    SettingRow("Dim after Isha", "Softens the screen through the night until Fajr") { Toggle(s.nightDim) { on -> store.update { it.copy(nightDim = on) } } }
    SettingRow("Art theme", null) { Chips(ArtTheme.entries.map { it.label }, ArtTheme.entries.indexOf(s.artTheme)) { i -> store.update { it.copy(artTheme = ArtTheme.entries[i]) } } }
    SettingRow("Open Miqaat when the tablet starts", "So it comes back after a power cut") { Toggle(s.launchOnBoot) { on -> store.update { it.copy(launchOnBoot = on) } } }
}

@Composable
private fun AboutSection(s: AppSettings) {
    Heading("About Miqaat", "ميقات · an appointed time")
    Text(
        "Prayer times are computed on the tablet with the Adhan library (Batoul Apps, MIT licence), using the high-precision astronomical algorithms of Jean Meeus. " +
            "No account, no advertising, no analytics, and nothing leaves the device except the optional address lookup for your location name.\n\n" +
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

@Composable
private fun Chips(labels: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

@Composable
private fun StepBtn(t: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, Color.White.copy(alpha = if (enabled) 0.3f else 0.1f), RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center
    ) { Text(t, fontSize = 20.sp, color = Palette.ivory.copy(alpha = if (enabled) 1f else 0.3f)) }
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
