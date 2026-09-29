package com.usman.miqaat.data

import com.usman.miqaat.R

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

/** [label]/[detail] are the English names used in logs and the correction e-mail; the UI shows [labelRes]/[detailRes] via Str. */
enum class Method(val label: String, val detail: String, val labelRes: Int, val detailRes: Int) {
    MWL("Muslim World League", "Fajr 18°, Isha 17° · widely used in Europe, Australia and much of the world · confirm with your masjid", R.string.e_m_mwl, R.string.e_m_mwl_d),
    ISNA("ISNA (North America)", "Fajr 15°, Isha 15°", R.string.e_m_isna, R.string.e_m_isna_d),
    EGYPT("Egyptian General Authority", "Fajr 19.5°, Isha 17.5°", R.string.e_m_egypt, R.string.e_m_egypt_d),
    UMM_AL_QURA("Umm al-Qura, Makkah", "Fajr 18.5°, Isha 90 min after Maghrib", R.string.e_m_uaq, R.string.e_m_uaq_d),
    KARACHI("University of Islamic Sciences, Karachi", "Fajr 18°, Isha 18°", R.string.e_m_karachi, R.string.e_m_karachi_d),
    TURKEY("Diyanet (Turkey)", "Fajr 18°, Isha 17°", R.string.e_m_turkey, R.string.e_m_turkey_d),
    DUBAI("Dubai", "Fajr 18.2°, Isha 18.2°", R.string.e_m_dubai, R.string.e_m_dubai_d),
    KUWAIT("Kuwait", "Fajr 18°, Isha 17.5°", R.string.e_m_kuwait, R.string.e_m_kuwait_d),
    QATAR("Qatar", "Fajr 18°, Isha 90 min after Maghrib", R.string.e_m_qatar, R.string.e_m_qatar_d),
    SINGAPORE("Singapore (MUIS)", "Fajr 20°, Isha 18°", R.string.e_m_singapore, R.string.e_m_singapore_d),
    MOONSIGHTING("Moonsighting Committee", "Fajr 18°, Isha 18° with seasonal adjustment", R.string.e_m_moon, R.string.e_m_moon_d);

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

enum class AsrMethod(val label: String, val madhab: Madhab, val labelRes: Int) {
    STANDARD("Shafiʿi, Maliki, Hanbali", Madhab.SHAFI, R.string.e_asr_std),
    HANAFI("Hanafi", Madhab.HANAFI, R.string.e_asr_hanafi)
}

enum class LatitudeRule(val label: String, val rule: HighLatitudeRule, val labelRes: Int) {
    MIDDLE("Middle of the night", HighLatitudeRule.MIDDLE_OF_THE_NIGHT, R.string.e_lat_middle),
    SEVENTH("One seventh of the night", HighLatitudeRule.SEVENTH_OF_THE_NIGHT, R.string.e_lat_seventh),
    ANGLE("Twilight angle", HighLatitudeRule.TWILIGHT_ANGLE, R.string.e_lat_angle)
}

enum class Narration(val label: String, val labelRes: Int) { OFF("Off", R.string.e_nar_off), ENGLISH("English", R.string.e_nar_en), BOTH("Arabic + English", R.string.e_nar_both) }

enum class RamadanMode(val label: String, val labelRes: Int) { AUTO("Automatic", R.string.e_ram_auto), ON("On", R.string.e_ram_on), OFF("Off", R.string.e_ram_off) }

enum class IqamahSound(val label: String, val labelRes: Int) { OFF("Off", R.string.e_iq_off), CHIME("Chime", R.string.e_iq_chime), RECORDING("Iqamah recording", R.string.e_iq_rec) }

enum class AppTheme(val label: String, val labelRes: Int) { MIQAAT("Miqaat · illuminated", R.string.e_theme_miqaat), KISWAH("Kiswah · black & gold", R.string.e_theme_kiswah),
    /** Added in the four-theme release. New values are appended so stored names never change meaning. */
    CELESTIAL_MERIDIAN("Celestial Meridian", R.string.e_theme_celestial), PRAYER_GALLERY("Prayer Gallery", R.string.e_theme_gallery) }

enum class Language(val label: String, val tag: String) { EN("English", "en"), UR("اردو · Urdu", "ur") }

enum class ArtTheme(val label: String, val labelRes: Int) { GEOMETRIC("Geometric lattice", R.string.e_art_geo), CALLIGRAPHY("Calligraphy only", R.string.e_art_calli), MINIMAL("Minimal", R.string.e_art_min) }

@androidx.compose.runtime.Immutable
data class AppSettings(
    /**
     * Coordinates are only meaningful once [locationSet] is true. Until then the app is in an
     * unconfigured state and must not present prayer times as valid (the placeholder below is the
     * Kaʿbah, so nothing city-specific can leak through).
     */
    val latitude: Double = 21.4225,
    val longitude: Double = 39.8262,
    val locationName: String = "",
    val locationSet: Boolean = false,
    val zoneId: String? = null,        // null = the device's zone
    /** True once the user picked the zone themselves; automatic location refresh must never overwrite it. */
    val zoneManual: Boolean = false,
    /** Set when a location refresh could not find a plausible zone; cleared when the user picks one. Alarms are labelled unverified meanwhile. */
    val zoneNeedsReview: Boolean = false,
    /** Home coordinates, set the first time location is detected; used to notice travel. */
    val homeLat: Double? = null,
    val homeLng: Double? = null,
    val travellerMode: Boolean = false,
    val travelQasr: Boolean = false,
    val travelJam: Boolean = false,
    /** Masjid timetable overrides: "yyyy-MM-dd" → minutes-from-midnight for Fajr,Sunrise,Dhuhr,Asr,Maghrib,Isha and optionally 5 iqamah values. */
    val masjidName: String = "",
    val overrides: Map<String, List<Int>> = emptyMap(),
    val useOverrides: Boolean = true,
    val autoLocation: Boolean = true,
    val method: Method = Method.MWL,
    val asrMethod: AsrMethod = AsrMethod.STANDARD,
    val latitudeRule: LatitudeRule = LatitudeRule.MIDDLE,
    val adjustments: Map<Prayer, Int> = Prayer.entries.associateWith { 0 },
    val azaanEnabled: Map<Prayer, Boolean> = Prayer.prayersOnly.associateWith { true },
    val azaanVolume: Int = 100,
    /** Extra loudness in dB (0, 6 or 12) applied with Android's LoudnessEnhancer — for tablets whose speaker is quiet even at full alarm volume. */
    val azaanBoostDb: Int = 0,
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
    val fridayHourReminder: Boolean = true,      // quiet notification 1 h before Friday Maghrib
    val postPrayerAdhkar: Boolean = true,        // chip for 40 min after each prayer
    val suhoorAlarmMinutes: Int = 45,            // minutes before Fajr in Ramaḍān; 0 = off
    val tarawihMinutesAfterIsha: Int = 30,
    val kidsMode: Boolean = true,                // show "Learn to pray" in the menu
    val language: Language = Language.EN,
    val largeType: Boolean = false,
    val adhkarEnabled: Boolean = true,
    val showQibla: Boolean = true,
    val iqamahEnabled: Boolean = false,
    val iqamahOffsets: Map<Prayer, Int> = mapOf(Prayer.FAJR to 20, Prayer.DHUHR to 10, Prayer.ASR to 10, Prayer.MAGHRIB to 8, Prayer.ISHA to 15),
    val jumuahIqamahMinutes: Int = 13 * 60 + 30,
    /** Per prayer: true = iqamah at a fixed clock time (iqamahFixed), false = minutes after azaan (iqamahOffsets). */
    val iqamahIsFixed: Map<Prayer, Boolean> = Prayer.prayersOnly.associateWith { false },
    /** Fixed iqamah times as minutes from midnight. */
    val iqamahFixed: Map<Prayer, Int> = mapOf(Prayer.FAJR to 5 * 60, Prayer.DHUHR to 13 * 60, Prayer.ASR to 16 * 60 + 30, Prayer.MAGHRIB to 18 * 60 + 15, Prayer.ISHA to 19 * 60 + 30),
    val iqamahSound: IqamahSound = IqamahSound.RECORDING,
    val iqamahCountdownSeconds: Int = 60,
    val quietMinutes: Int = 10,
    val showHijri: Boolean = true,
    val hijriOffsetDays: Int = 0,
    val showSunrise: Boolean = true,
    val use24h: Boolean = false,
    val showRelative: Boolean = false,
    val showEndTimes: Boolean = true,
    val showDisliked: Boolean = true,   // tap a prayer: show "in 2 h 5 min" / "40 min ago" instead of clock times
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

/** "2026-10-04=315,397,718,907,1042,1118;2026-10-05=..." — compact, human-readable, no JSON dependency. */
fun encodeOverrides(m: Map<String, List<Int>>): String = m.entries.joinToString(";") { it.key + "=" + it.value.joinToString(",") }
fun decodeOverrides(s: String): Map<String, List<Int>> =
    if (s.isBlank()) emptyMap() else s.split(';').mapNotNull { e ->
        val (k, v) = e.split('=').takeIf { it.size == 2 } ?: return@mapNotNull null
        val nums = v.split(',').mapNotNull { it.trim().toIntOrNull() }
        if (nums.size >= 6 && runCatching { java.time.LocalDate.parse(k) }.isSuccess) k to nums else null
    }.toMap()

class SettingsStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("miqaat", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings
    val value: AppSettings get() = _settings.value

    /** Re-read from disk (after a restore). */
    fun reload() { _settings.value = load() }

    fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(_settings.value)
        _settings.value = next
        save(next)
    }

    private fun load(): AppSettings {
        val d = AppSettings()
        fun enumOr(key: String, default: String) = prefs.getString(key, default) ?: default
        // Migration for installs made before the unconfigured state existed (build ≤ 33): a place
        // counts as chosen only if the user detected it (home coordinates were recorded) or picked
        // one (coordinates differ from the old shipped default). Anyone else is asked again.
        val legacyLocationSet = prefs.getBoolean("setupDone", false) &&
            (prefs.contains("homeLat") || prefs.getFloat("lat", -34.02f) != -34.02f || prefs.getFloat("lng", 150.77f) != 150.77f)
        val locationSet = prefs.getBoolean("locSet", legacyLocationSet)
        return AppSettings(
            latitude = prefs.getFloat("lat", d.latitude.toFloat()).toDouble(),
            longitude = prefs.getFloat("lng", d.longitude.toFloat()).toDouble(),
            locationName = prefs.getString("locName", d.locationName) ?: d.locationName,
            locationSet = locationSet,
            zoneId = prefs.getString("zone", null),
            zoneManual = prefs.getBoolean("zoneManual", false),
            zoneNeedsReview = prefs.getBoolean("zoneReview", false),
            homeLat = if (prefs.contains("homeLat")) prefs.getFloat("homeLat", 0f).toDouble() else null,
            homeLng = if (prefs.contains("homeLng")) prefs.getFloat("homeLng", 0f).toDouble() else null,
            travellerMode = prefs.getBoolean("travel", false),
            travelQasr = prefs.getBoolean("travelQasr", false),
            travelJam = prefs.getBoolean("travelJam", false),
            masjidName = prefs.getString("masjid", "") ?: "",
            overrides = decodeOverrides(prefs.getString("overrides", "") ?: ""),
            useOverrides = prefs.getBoolean("useOverrides", true),
            autoLocation = prefs.getBoolean("autoLoc", d.autoLocation),
            method = runCatching { Method.valueOf(enumOr("method", d.method.name)) }.getOrDefault(d.method),
            asrMethod = runCatching { AsrMethod.valueOf(enumOr("asr", d.asrMethod.name)) }.getOrDefault(d.asrMethod),
            latitudeRule = runCatching { LatitudeRule.valueOf(enumOr("latRule", d.latitudeRule.name)) }.getOrDefault(d.latitudeRule),
            adjustments = Prayer.entries.associateWith { prefs.getInt("adj_${it.key}", 0) },
            azaanEnabled = Prayer.prayersOnly.associateWith { prefs.getBoolean("az_${it.key}", true) },
            azaanVolume = prefs.getInt("azVol", d.azaanVolume),
            azaanBoostDb = prefs.getInt("azBoost", d.azaanBoostDb),
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
            fridayHourReminder = prefs.getBoolean("friHour", d.fridayHourReminder),
            postPrayerAdhkar = prefs.getBoolean("ppAdhkar", d.postPrayerAdhkar),
            suhoorAlarmMinutes = prefs.getInt("suhoor", d.suhoorAlarmMinutes),
            tarawihMinutesAfterIsha = prefs.getInt("tarawih", d.tarawihMinutesAfterIsha),
            kidsMode = prefs.getBoolean("kids", d.kidsMode),
            language = runCatching { Language.valueOf(enumOr("lang", d.language.name)) }.getOrDefault(d.language),
            largeType = prefs.getBoolean("large", d.largeType),
            adhkarEnabled = prefs.getBoolean("adhkar", d.adhkarEnabled),
            showQibla = prefs.getBoolean("qibla", d.showQibla),
            iqamahEnabled = prefs.getBoolean("iqEn", d.iqamahEnabled),
            iqamahOffsets = Prayer.prayersOnly.associateWith { prefs.getInt("iq_${it.key}", d.iqamahOffsets[it] ?: 10) },
            jumuahIqamahMinutes = prefs.getInt("iqJum", d.jumuahIqamahMinutes),
            iqamahIsFixed = Prayer.prayersOnly.associateWith { prefs.getBoolean("iqFixed_${it.key}", false) },
            iqamahFixed = Prayer.prayersOnly.associateWith { prefs.getInt("iqAt_${it.key}", d.iqamahFixed[it] ?: 12 * 60) },
            iqamahSound = runCatching { IqamahSound.valueOf(enumOr("iqSnd", d.iqamahSound.name)) }.getOrDefault(d.iqamahSound),
            iqamahCountdownSeconds = prefs.getInt("iqCd", d.iqamahCountdownSeconds),
            quietMinutes = prefs.getInt("quiet", d.quietMinutes),
            showHijri = prefs.getBoolean("hijri", d.showHijri),
            hijriOffsetDays = prefs.getInt("hijriOff", d.hijriOffsetDays),
            showSunrise = prefs.getBoolean("sunrise", d.showSunrise),
            use24h = prefs.getBoolean("h24", d.use24h),
            showRelative = prefs.getBoolean("rel", d.showRelative),
            showEndTimes = prefs.getBoolean("ends", d.showEndTimes),
            showDisliked = prefs.getBoolean("disliked", d.showDisliked),
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
            putString("locName", s.locationName); putString("zone", s.zoneId); putBoolean("zoneManual", s.zoneManual); putBoolean("zoneReview", s.zoneNeedsReview); putBoolean("locSet", s.locationSet)
            if (s.homeLat != null) putFloat("homeLat", s.homeLat.toFloat()) else remove("homeLat")
            if (s.homeLng != null) putFloat("homeLng", s.homeLng.toFloat()) else remove("homeLng")
            putBoolean("travel", s.travellerMode); putBoolean("travelQasr", s.travelQasr); putBoolean("travelJam", s.travelJam)
            putString("masjid", s.masjidName); putString("overrides", encodeOverrides(s.overrides)); putBoolean("useOverrides", s.useOverrides); putBoolean("autoLoc", s.autoLocation)
            putString("method", s.method.name); putString("asr", s.asrMethod.name); putString("latRule", s.latitudeRule.name)
            s.adjustments.forEach { (p, v) -> putInt("adj_${p.key}", v) }
            s.azaanEnabled.forEach { (p, v) -> putBoolean("az_${p.key}", v) }
            putInt("azVol", s.azaanVolume); putInt("azBoost", s.azaanBoostDb); putString("azUri", s.azaanUri); putString("azFajrUri", s.fajrAzaanUri)
            putInt("preMin", s.preReminderMinutes); putInt("afterMin", s.afterWindowMinutes)
            putBoolean("afterAz", s.afterAzaanEnabled); putString("narr", s.narration.name); putInt("hadMin", s.hadithMinutes)
            putString("ramadan", s.ramadanMode.name); putBoolean("jumuah", s.jumuahEnabled); putInt("jumuahMin", s.jumuahMinutes)
            putBoolean("friRem", s.fridayReminders); putBoolean("friHour", s.fridayHourReminder); putBoolean("ppAdhkar", s.postPrayerAdhkar); putInt("suhoor", s.suhoorAlarmMinutes); putInt("tarawih", s.tarawihMinutesAfterIsha); putBoolean("kids", s.kidsMode); putString("lang", s.language.name); putBoolean("large", s.largeType); putBoolean("adhkar", s.adhkarEnabled); putBoolean("qibla", s.showQibla)
            putBoolean("iqEn", s.iqamahEnabled); s.iqamahOffsets.forEach { (p, v) -> putInt("iq_${p.key}", v) }
            putInt("iqJum", s.jumuahIqamahMinutes); s.iqamahIsFixed.forEach { (p, v) -> putBoolean("iqFixed_${p.key}", v) }; s.iqamahFixed.forEach { (p, v) -> putInt("iqAt_${p.key}", v) }; putString("iqSnd", s.iqamahSound.name); putInt("iqCd", s.iqamahCountdownSeconds); putInt("quiet", s.quietMinutes)
            putBoolean("hijri", s.showHijri); putInt("hijriOff", s.hijriOffsetDays); putBoolean("sunrise", s.showSunrise)
            putBoolean("h24", s.use24h); putBoolean("rel", s.showRelative); putBoolean("ends", s.showEndTimes); putBoolean("disliked", s.showDisliked); putBoolean("keepOn", s.keepScreenOn); putBoolean("nightDim", s.nightDim)
            putString("art", s.artTheme.name); putString("theme", s.theme.name); putBoolean("boot", s.launchOnBoot); putBoolean("setupDone", s.setupDone)
        }.apply()
    }
}
