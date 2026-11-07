package com.usman.miqaat.ui

import com.usman.miqaat.data.L10n
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.usman.miqaat.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.Ramadan
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

/** The Friday routine: Sūrat al-Kahf, ṣalawāt, the hour of acceptance, and the day's sunnahs. */
@Composable
fun FridayScreen(settings: AppSettings, onBack: () -> Unit) {
    val tk = screenTokens()
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("miqaat_friday", Context.MODE_PRIVATE) }
    val today = LocalDate.now(settings.zone())
    val friday = if (today.dayOfWeek == DayOfWeek.FRIDAY) today else today.with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
    val weekKey = friday.toString()
    var kahf by remember { mutableStateOf(prefs.getBoolean("kahf_$weekKey", false)) }
    var salawat by remember { mutableIntStateOf(prefs.getInt("salawat_$weekKey", 0)) }
    val day = PrayerEngine.times(settings, friday)
    val hourStart = day[Prayer.MAGHRIB].minusMinutes(60)
    val now = ZonedDateTime.now(settings.zone())
    val h24 = settings.use24h
    fun c(z: ZonedDateTime) = PrayerEngine.clock(z, h24) + " " + PrayerEngine.suffix(z, h24)

    BoxWithConstraints(Modifier.fillMaxSize().background(tk.backgroundBrush)) {
        val landscape = maxWidth > maxHeight
        val phone = minOf(maxWidth, maxHeight) < 600.dp
        val compactLandscape = phone && landscape
        val cardsAcross = maxWidth >= 700.dp
        val k = when { compactLandscape -> .78f; maxWidth >= 1000.dp -> 1.2f; else -> 1f }
        when (tk.art) {
            ArtStyle.CELESTIAL, ArtStyle.GALLERY -> ThemedBackdrop(!landscape, Modifier.fillMaxSize(), scrim = if (tk.art == ArtStyle.GALLERY) .78f else .68f)
            ArtStyle.KISWAH_SILK -> Weave(Modifier.fillMaxSize())
            ArtStyle.ILLUMINATED_SKY -> GirihLattice(Modifier.fillMaxSize(), tile = if (phone) 96f else 120f, alpha = 0.07f)
        }
        androidx.compose.runtime.CompositionLocalProvider(LocalK provides k) {
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = if (phone) 14.dp else 28.dp, vertical = if (compactLandscape) 4.dp else 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = tk.contentPrimary) }
                Column {
                    Text(Str[R.string.s_jumu_ah], fontFamily = tk.fontDisplay, fontSize = (34 * LocalK.current).sp, color = tk.contentPrimary, lineHeight = (36 * LocalK.current).sp)
                    Text(if (L10n.uiUrdu) L10n.fridayDate(friday) else "يوم الجمعة  ·  ${friday.dayOfMonth} ${friday.month.name.lowercase().replaceFirstChar { it.uppercase() }}", fontFamily = if (L10n.uiUrdu) uiFont(settings) else Amiri, fontSize = (20 * LocalK.current).sp, color = tk.accent)
                }
            }
            Text(Str[R.string.s_the_best_day_on_which_the], fontFamily = tk.fontDisplay, fontSize = (18 * LocalK.current).sp, color = tk.contentSecondary,
                modifier = Modifier.padding(start = 12.dp, bottom = if (compactLandscape) 6.dp else 14.dp))

            val cards: @Composable (Modifier) -> Unit = { m ->
                Card(m, Str[R.string.s_s_rat_al_kahf], "سورة الكهف", if (L10n.uiUrdu) "“${Ramadan.KAHF_NOTE_UR}” · ${Ramadan.KAHF_SRC_UR}" else "\"Whoever reads Sūrat al-Kahf on Friday, a light shines for him between the two Fridays\" · ${Ramadan.KAHF_SRC}") {
                    val action: @Composable () -> Unit = {
                        Pill(if (kahf) Str[R.string.s_read_this_friday] else Str[R.string.s_mark_as_read], kahf) { kahf = !kahf; prefs.edit().putBoolean("kahf_$weekKey", kahf).apply() }
                    }
                    val window: @Composable () -> Unit = {
                        Text(if (kahf) "" else Str[R.string.s_from_thursday_maghrib_to_friday_maghrib], modifier = Modifier.fillMaxWidth(), fontFamily = if (L10n.uiUrdu) uiFont(settings) else tk.fontUi, fontSize = (12 * LocalK.current).sp, color = tk.contentSecondary)
                    }
                    if (L10n.uiUrdu) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { action(); window() }
                    else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { action(); window() }
                }
                Card(m, L10n.t("Ṣalawāt", "درود شریف"), "الصلاة على النبي ﷺ", if (L10n.uiUrdu) "“${Ramadan.SALAWAT_NOTE_UR}” (اقتباس) · ${Ramadan.SALAWAT_SRC_UR}" else "\"Increase your ṣalawāt upon me on Friday\" · ${Ramadan.SALAWAT_SRC}") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.size((64 * LocalK.current).dp).clip(CircleShape).background(tk.primary).clickable { salawat++; prefs.edit().putInt("salawat_$weekKey", salawat).apply() }, contentAlignment = Alignment.Center) {
                            Text("$salawat", fontFamily = tk.fontDisplay, fontSize = (26 * LocalK.current).sp, color = tk.onPrimary)
                        }
                        Column {
                            Text("اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", fontFamily = Amiri, fontSize = (20 * LocalK.current).sp, color = tk.arabicText)
                            Text(Str[R.string.s_tap_the_circle_for_each_one], fontFamily = tk.fontUi, fontSize = (12 * LocalK.current).sp, color = tk.contentSecondary)
                        }
                    }
                }
                Card(m, Str[R.string.s_hour_of_acceptance], "ساعة الإجابة", Str[R.string.s_an_hour_on_friday_when_du]) {
                    Column {
                        Text("${c(hourStart)}  →  ${c(day[Prayer.MAGHRIB])}", fontFamily = tk.fontDisplay, fontSize = (26 * LocalK.current).sp, color = tk.contentPrimary)
                        Text(when {
                            today != friday -> Str[R.string.s_this_friday]
                            now.isBefore(hourStart) -> (if (L10n.uiUrdu) "آغاز میں ${L10n.duration(settings, java.time.Duration.between(now, hourStart))} باقی ہیں۔" else "Begins in ${PrayerEngine.humanDuration(java.time.Duration.between(now, hourStart))}") + if (settings.fridayHourReminder) Str[R.string.s_a_quiet_reminder_will_show] else ""
                            now.isBefore(day[Prayer.MAGHRIB]) -> Str[R.string.s_now_make_du]
                            else -> Str[R.string.s_passed_for_this_week]
                        }, fontFamily = tk.fontUi, fontSize = (13 * LocalK.current).sp, color = tk.accent)
                    }
                }
            }
            if (cardsAcross) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if (compactLandscape) 8.dp else 12.dp), verticalAlignment = Alignment.Top) { cards(Modifier.weight(1f)) }
            else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { cards(Modifier.fillMaxWidth()) }

            Spacer(Modifier.height(if (compactLandscape) 10.dp else 18.dp))
            Text(Str[R.string.s_friday_s_sunnahs], fontFamily = tk.fontDisplay, fontSize = (24 * LocalK.current).sp, color = tk.contentPrimary)
            val sunnahs = listOf(
                Str[R.string.s_ghusl_clean_clothes_and_perfume_before] to Str[R.string.s_a_al_bukh_r_880_883],
                Str[R.string.s_go_early_and_walk_if_you] to Str[R.string.s_a_al_bukh_r_881_ab],
                Str[R.string.s_listen_to_the_khu_bah_in] to Str[R.string.s_a_al_bukh_r_934],
                Str[R.string.s_sunnah_prayers_four_after_jumu_ah] to Str[R.string.s_a_muslim_881_882],
                Str[R.string.s_read_s_rat_al_kahf_and] to L10n.t("above", "اوپر دیا گیا حوالہ"),
            )
            val sunnah: @Composable (Pair<String, String>, Modifier) -> Unit = { (t, src), modifier ->
                Row(modifier.padding(vertical = if (compactLandscape) 4.dp else 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Check, null, Modifier.size(18.dp), tint = tk.success)
                    Spacer(Modifier.width(10.dp))
                    Column { Text(t, fontFamily = tk.fontUi, fontSize = (15 * LocalK.current).sp, color = tk.contentPrimary); Text(src, fontFamily = tk.fontUi, fontSize = (12 * LocalK.current).sp, color = tk.contentMuted) }
                }
            }
            if (cardsAcross) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) { sunnahs.filterIndexed { i, _ -> i % 2 == 0 }.forEach { sunnah(it, Modifier.fillMaxWidth()) } }
                Column(Modifier.weight(1f)) { sunnahs.filterIndexed { i, _ -> i % 2 == 1 }.forEach { sunnah(it, Modifier.fillMaxWidth()) } }
            } else sunnahs.forEach { sunnah(it, Modifier.fillMaxWidth()) }
            if (settings.jumuahEnabled) {
                Spacer(Modifier.height(10.dp))
                val iq = PrayerEngine.iqamah(settings, day, Prayer.DHUHR)
                Text(Str.get(R.string.s_friday_masjid_azaan, c(day[Prayer.DHUHR])) + (iq?.let { Str.get(R.string.s_friday_masjid_iqamah, c(it)) } ?: ""),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(tk.cornerMedium)).background(tk.softFill).border(1.dp, tk.divider, RoundedCornerShape(tk.cornerMedium)).padding(horizontal = 14.dp, vertical = 10.dp),
                    fontFamily = tk.fontUi, fontSize = (14 * LocalK.current).sp, color = tk.accent)
            }
        }
        }
    }
}

private val LocalK = androidx.compose.runtime.compositionLocalOf { 1f }

@Composable
private fun Card(m: Modifier, title: String, arabic: String, source: String, content: @Composable () -> Unit) {
    val tk = screenTokens()
    val compact = LocalK.current < .9f
    Column(m.clip(RoundedCornerShape(tk.cornerMedium)).background(tk.surface.copy(alpha = if (tk.dark) .88f else .94f))
        .border(1.dp, tk.outline.copy(alpha = .65f), RoundedCornerShape(tk.cornerMedium)).padding(if (compact) 10.dp else 16.dp)) {
        Text(title.uppercase(), fontFamily = tk.fontUi, fontSize = (11 * LocalK.current).sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold, color = tk.accent)
        Text(arabic, fontFamily = Amiri, fontSize = (24 * LocalK.current).sp, lineHeight = (32 * LocalK.current).sp, color = tk.arabicText)
        Text(source, fontFamily = tk.fontUi, fontSize = (12 * LocalK.current).sp, color = tk.contentSecondary, lineHeight = (16 * LocalK.current).sp, modifier = Modifier.padding(bottom = if (compact) 6.dp else 12.dp))
        content()
    }
}

@Composable
private fun Pill(label: String, on: Boolean, onClick: () -> Unit) {
    val tk = screenTokens()
    Box(Modifier.clip(RoundedCornerShape(50)).background(if (on) tk.success else tk.primary).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = (14 * LocalK.current).sp, color = tk.onPrimary)
    }
}
