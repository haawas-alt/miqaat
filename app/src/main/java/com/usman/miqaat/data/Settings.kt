package com.usman.miqaat.data

import android.content.Context
import android.content.SharedPreferences
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.Madhab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Prayer(val key: String, val english: String, val arabic: String, val isPrayer: Boolean = true) {
    FAJR("fajr", "Fajr", "الفجر"),
    SUNRISE("sunrise", "Sunrise", "الشروق", isPrayer = false),
    DHUHR("dhuhr", "Dhuhr", "الظهر"),
    ASR("asr", "Asr", "العصر"),
    MAGHRIB("maghrib", "Maghrib", "المغرب"),
    ISHA("isha", "Isha", "العشاء");

    companion object {
        val prayersOnly = entries.filter { it.isPrayer }
    }
}

enum class Method(val label: String, val detail: String) {
    MWL("Muslim World League", "Fajr 18°, Isha 17° · used by most Australian mosques"),
    ISNA("ISNA (North America)", "Fajr 15°, Isha 15°"),
    EGYPT("Egyptian General Authority", "Fajr 19.5°, Isha 17.5°"),
    UMM_AL_QURA("Umm al-Qura, Makkah", "Fajr 18.5°, Isha 90 min after Maghrib"),
    KARACHI("University of Islamic Sciences, Karachi", "Fajr 18°, Isha 18°"),
    TURKEY("Diyanet (Turkey)", "Fajr 18°, Isha 17°"),
    DUBAI("Dubai", "Fajr 18.2°, Isha 18.2°"),
    KUWAIT("Kuwait", "Fajr 18°, Isha 17.5°"),
    QATAR("Qatar", "Fajr 18°, Isha 90 min after Maghrib"),
    SINGAPORE("Singapore (MUIS)", "Fajr 20°, Isha 18°"),
    MOONSIGHTING("Moonsighting Committee", "Fajr 18°, Isha 18° with seasonal adjustment");

    fun parameters(): CalculationParameters = when (this) {
        MWL -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        ISNA -> CalculationMethod.NORTH_AMERICA.parameters
        EGYPT -> CalculationMethod.EGYPTIAN.parameters
        UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA.parameters
        KARACHI -> CalculationMethod.KARACHI.parameters
        TURKEY -> CalculationMethod.OTHER.parameters.also { it.fajrAngle = 18.0; it.ishaAngle = 17.0 }
        DUBAI -> CalculationMethod.DUBAI.parameters
        KUWAIT -> CalculationMethod.KUWAIT.parameters
        QATAR -> CalculationMethod.QATAR.parameters
        SINGAPORE -> CalculationMethod.SINGAPORE.parameters
        MOONSIGHTING -> CalculationMethod.MOON_SIGHTING_COMMITTEE.parameters
    }
}

enum class AsrMethod(val label: String, val madhab: Madhab) {
    STANDARD("Shafiʿi, Maliki, Hanbali", Madhab.SHAFI),
    HANAFI("Hanafi", Madhab.HANAFI)
}

enum class LatitudeRule(val label: String, val rule: HighLatitudeRule) {
    MIDDLE("Middle of the night", HighLatitudeRule.MIDDLE_OF_THE_NIGHT),
    SEVENTH("One seventh of the night", HighLatitudeRule.SEVENTH_OF_THE_NIGHT),
    ANGLE("Twilight angle", HighLatitudeRule.TWILIGHT_ANGLE)
}

enum class Narration(val label: String) { OFF("Off"), ENGLISH("English"), BOTH("Arabic + English") }

enum class RamadanMode(val label: String) { AUTO("Automatic"), ON("On"), OFF("Off") }

enum class IqamahSound(val label: String) { OFF("Off"), CHIME("Chime"), RECORDING("Iqamah recording") }

enum class AppTheme(val label: String) { MIQAAT("Miqaat · illuminated"), KISWAH("Kiswah · black & gold") }

enum class ArtTheme(val label: String) { GEOMETRIC("Geometric lattice"), CALLIGRAPHY("Calligraphy only"), MINIMAL("Minimal") }

data class AppSettings(
    val latitude: Double = -34.02,
    val longitude: Double = 150.77,
    val locationName: String = "Gledswood Hills, NSW",
    val zoneId: String? = null,        // null = the device's zone
    val autoLocation: Boolean = true,
    val method: Method = Method.MWL,
    val asrMethod: AsrMethod = AsrMethod.STANDARD,
    val latitudeRule: LatitudeRule = LatitudeRule.MIDDLE,
    val adjustments: Map<Prayer, Int> = Prayer.entries.associateWith { 0 },
    val azaanEnabled: Map<Prayer, Boolean> = Prayer.prayersOnly.associateWith { true },
    val azaanVolume: Int = 80,
    val azaanUri: String? = null,
    val fajrAzaanUri: String? = null,
    val preReminderMinutes: Int = 0,
    val afterWindowMinutes: Int = 45,
    val afterAzaanEnabled: Boolean = true,
    val narration: Narration = Narration.BOTH,
    val hadithMinutes: Int = 3,
    val ramadanMode: RamadanMode = RamadanMode.AUTO,
    val jumuahEnabled: Boolean = false,
    val jumuahMinutes: Int = 13 * 60 + 15,      // minutes from midnight, default 1:15 PM
    val fridayReminders: Boolean = true,
    val adhkarEnabled: Boolean = true,
    val showQibla: Boolean = true,
    val iqamahEnabled: Boolean = false,
    val iqamahOffsets: Map<Prayer, Int> = mapOf(Prayer.FAJR to 20, Prayer.DHUHR to 10, Prayer.ASR to 10, Prayer.MAGHRIB to 8, Prayer.ISHA to 15),
    val jumuahIqamahMinutes: Int = 13 * 60 + 30,
    val iqamahSound: IqamahSound = IqamahSound.RECORDING,
    val iqamahCountdownSeconds: Int = 60,
    val quietMinutes: Int = 10,
    val showHijri: Boolean = true,
    val hijriOffsetDays: Int = 0,
    val showSunrise: Boolean = true,
    val use24h: Boolean = false,
    val showRelative: Boolean = false,   // tap a prayer: show "in 2 h 5 min" / "40 min ago" instead of clock times
    val keepScreenOn: Boolean = true,
    val nightDim: Boolean = true,
    val artTheme: ArtTheme = ArtTheme.GEOMETRIC,
    val theme: AppTheme = AppTheme.MIQAAT,
    val launchOnBoot: Boolean = false,
    val setupDone: Boolean = false
) {
    /** Zone the prayer times are shown in: the chosen place's zone, else the device's. */
    fun zone(): java.time.ZoneId = zoneId?.let { runCatching { java.time.ZoneId.of(it) }.getOrNull() } ?: java.time.ZoneId.systemDefault()

    fun calculationParameters(): CalculationParameters {
        val p = method.parameters()
        p.madhab = asrMethod.madhab
        p.highLatitudeRule = latitudeRule.rule
        p.adjustments.fajr = adjustments[Prayer.FAJR] ?: 0
        p.adjustments.sunrise = adjustments[Prayer.SUNRISE] ?: 0
        p.adjustments.dhuhr = adjustments[Prayer.DHUHR] ?: 0
        p.adjustments.asr = adjustments[Prayer.ASR] ?: 0
        p.adjustments.maghrib = adjustments[Prayer.MAGHRIB] ?: 0
        p.adjustments.isha = adjustments[Prayer.ISHA] ?: 0
        return p
    }
}

class SettingsStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("miqaat", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings
    val value: AppSettings get() = _settings.value

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_settings.value)
        _settings.value = next
        save(next)
    }

    private fun load(): AppSettings {
        val d = AppSettings()
        fun enumOr(key: String, default: String) = prefs.getString(key, default) ?: default
        return AppSettings(
            latitude = prefs.getFloat("lat", d.latitude.toFloat()).toDouble(),
            longitude = prefs.getFloat("lng", d.longitude.toFloat()).toDouble(),
            locationName = prefs.getString("locName", d.locationName) ?: d.locationName,
            zoneId = prefs.getString("zone", null),
            autoLocation = prefs.getBoolean("autoLoc", d.autoLocation),
            method = runCatching { Method.valueOf(enumOr("method", d.method.name)) }.getOrDefault(d.method),
            asrMethod = runCatching { AsrMethod.valueOf(enumOr("asr", d.asrMethod.name)) }.getOrDefault(d.asrMethod),
            latitudeRule = runCatching { LatitudeRule.valueOf(enumOr("latRule", d.latitudeRule.name)) }.getOrDefault(d.latitudeRule),
            adjustments = Prayer.entries.associateWith { prefs.getInt("adj_${it.key}", 0) },
            azaanEnabled = Prayer.prayersOnly.associateWith { prefs.getBoolean("az_${it.key}", true) },
            azaanVolume = prefs.getInt("azVol", d.azaanVolume),
            azaanUri = prefs.getString("azUri", null),
            fajrAzaanUri = prefs.getString("azFajrUri", null),
            preReminderMinutes = prefs.getInt("preMin", d.preReminderMinutes),
            afterWindowMinutes = prefs.getInt("afterMin", d.afterWindowMinutes),
            afterAzaanEnabled = prefs.getBoolean("afterAz", d.afterAzaanEnabled),
            narration = runCatching { Narration.valueOf(enumOr("narr", d.narration.name)) }.getOrDefault(d.narration),
            hadithMinutes = prefs.getInt("hadMin", d.hadithMinutes),
            ramadanMode = runCatching { RamadanMode.valueOf(enumOr("ramadan", d.ramadanMode.name)) }.getOrDefault(d.ramadanMode),
            jumuahEnabled = prefs.getBoolean("jumuah", d.jumuahEnabled),
            jumuahMinutes = prefs.getInt("jumuahMin", d.jumuahMinutes),
            fridayReminders = prefs.getBoolean("friRem", d.fridayReminders),
            adhkarEnabled = prefs.getBoolean("adhkar", d.adhkarEnabled),
            showQibla = prefs.getBoolean("qibla", d.showQibla),
            iqamahEnabled = prefs.getBoolean("iqEn", d.iqamahEnabled),
            iqamahOffsets = Prayer.prayersOnly.associateWith { prefs.getInt("iq_${it.key}", d.iqamahOffsets[it] ?: 10) },
            jumuahIqamahMinutes = prefs.getInt("iqJum", d.jumuahIqamahMinutes),
            iqamahSound = runCatching { IqamahSound.valueOf(enumOr("iqSnd", d.iqamahSound.name)) }.getOrDefault(d.iqamahSound),
            iqamahCountdownSeconds = prefs.getInt("iqCd", d.iqamahCountdownSeconds),
            quietMinutes = prefs.getInt("quiet", d.quietMinutes),
            showHijri = prefs.getBoolean("hijri", d.showHijri),
            hijriOffsetDays = prefs.getInt("hijriOff", d.hijriOffsetDays),
            showSunrise = prefs.getBoolean("sunrise", d.showSunrise),
            use24h = prefs.getBoolean("h24", d.use24h),
            showRelative = prefs.getBoolean("rel", d.showRelative),
            keepScreenOn = prefs.getBoolean("keepOn", d.keepScreenOn),
            nightDim = prefs.getBoolean("nightDim", d.nightDim),
            artTheme = runCatching { ArtTheme.valueOf(enumOr("art", d.artTheme.name)) }.getOrDefault(d.artTheme),
            theme = runCatching { AppTheme.valueOf(enumOr("theme", d.theme.name)) }.getOrDefault(d.theme),
            launchOnBoot = prefs.getBoolean("boot", d.launchOnBoot),
            setupDone = prefs.getBoolean("setupDone", d.setupDone)
        )
    }

    private fun save(s: AppSettings) {
        prefs.edit().apply {
            putFloat("lat", s.latitude.toFloat()); putFloat("lng", s.longitude.toFloat())
            putString("locName", s.locationName); putString("zone", s.zoneId); putBoolean("autoLoc", s.autoLocation)
            putString("method", s.method.name); putString("asr", s.asrMethod.name); putString("latRule", s.latitudeRule.name)
            s.adjustments.forEach { (p, v) -> putInt("adj_${p.key}", v) }
            s.azaanEnabled.forEach { (p, v) -> putBoolean("az_${p.key}", v) }
            putInt("azVol", s.azaanVolume); putString("azUri", s.azaanUri); putString("azFajrUri", s.fajrAzaanUri)
            putInt("preMin", s.preReminderMinutes); putInt("afterMin", s.afterWindowMinutes)
            putBoolean("afterAz", s.afterAzaanEnabled); putString("narr", s.narration.name); putInt("hadMin", s.hadithMinutes)
            putString("ramadan", s.ramadanMode.name); putBoolean("jumuah", s.jumuahEnabled); putInt("jumuahMin", s.jumuahMinutes)
            putBoolean("friRem", s.fridayReminders); putBoolean("adhkar", s.adhkarEnabled); putBoolean("qibla", s.showQibla)
            putBoolean("iqEn", s.iqamahEnabled); s.iqamahOffsets.forEach { (p, v) -> putInt("iq_${p.key}", v) }
            putInt("iqJum", s.jumuahIqamahMinutes); putString("iqSnd", s.iqamahSound.name); putInt("iqCd", s.iqamahCountdownSeconds); putInt("quiet", s.quietMinutes)
            putBoolean("hijri", s.showHijri); putInt("hijriOff", s.hijriOffsetDays); putBoolean("sunrise", s.showSunrise)
            putBoolean("h24", s.use24h); putBoolean("rel", s.showRelative); putBoolean("keepOn", s.keepScreenOn); putBoolean("nightDim", s.nightDim)
            putString("art", s.artTheme.name); putString("theme", s.theme.name); putBoolean("boot", s.launchOnBoot); putBoolean("setupDone", s.setupDone)
        }.apply()
    }
}
