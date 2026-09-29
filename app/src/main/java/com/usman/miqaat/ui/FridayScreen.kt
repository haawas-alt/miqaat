package com.usman.miqaat.ui

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
        val compact = maxWidth < 700.dp
        GirihLattice(Modifier.fillMaxSize(), tile = 120f, alpha = 0.07f)
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = if (compact) 16.dp else 32.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = tk.contentPrimary) }
                Column {
                    Text(Str[R.string.s_jumu_ah], fontFamily = Cormorant, fontSize = 34.sp, color = tk.contentPrimary, lineHeight = 36.sp)
                    Text("يوم الجمعة  ·  ${friday.dayOfMonth} ${friday.month.name.lowercase().replaceFirstChar { it.uppercase() }}", fontFamily = Amiri, fontSize = 20.sp, color = tk.accent)
                }
            }
            Text(Str[R.string.s_the_best_day_on_which_the], fontFamily = Cormorant, fontSize = 18.sp, color = tk.contentPrimary.copy(alpha = 0.8f), modifier = Modifier.padding(start = 12.dp, bottom = 14.dp))

            val cards: @Composable (Modifier) -> Unit = { m ->
                Card(m, Str[R.string.s_s_rat_al_kahf], "سورة الكهف", "\"Whoever reads Sūrat al-Kahf on Friday, a light shines for him between the two Fridays\" · ${Ramadan.KAHF_SRC}") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Pill(if (kahf) Str[R.string.s_read_this_friday] else Str[R.string.s_mark_as_read], kahf) { kahf = !kahf; prefs.edit().putBoolean("kahf_$weekKey", kahf).apply() }
                        Text(if (kahf) "" else Str[R.string.s_from_thursday_maghrib_to_friday_maghrib], fontFamily = Nunito, fontSize = 12.sp, color = tk.contentSecondary)
                    }
                }
                Card(m, "Ṣalawāt", "الصلاة على النبي ﷺ", "\"Increase your ṣalawāt upon me on Friday\" · ${Ramadan.SALAWAT_SRC}") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.size(64.dp).clip(CircleShape).background(tk.primary).clickable { salawat++; prefs.edit().putInt("salawat_$weekKey", salawat).apply() }, contentAlignment = Alignment.Center) {
                            Text("$salawat", fontFamily = Cormorant, fontSize = 26.sp, color = tk.onPrimary)
                        }
                        Column {
                            Text("اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", fontFamily = Amiri, fontSize = 20.sp, color = tk.arabicText)
                            Text(Str[R.string.s_tap_the_circle_for_each_one], fontFamily = Nunito, fontSize = 12.sp, color = tk.contentSecondary)
                        }
                    }
                }
                Card(m, Str[R.string.s_hour_of_acceptance], "ساعة الإجابة", Str[R.string.s_an_hour_on_friday_when_du]) {
                    Column {
                        Text("${c(hourStart)}  →  ${c(day[Prayer.MAGHRIB])}", fontFamily = Cormorant, fontSize = 26.sp, color = tk.contentPrimary)
                        Text(when {
                            today != friday -> Str[R.string.s_this_friday]
                            now.isBefore(hourStart) -> "Begins in ${PrayerEngine.humanDuration(java.time.Duration.between(now, hourStart))}" + if (settings.fridayHourReminder) Str[R.string.s_a_quiet_reminder_will_show] else ""
                            now.isBefore(day[Prayer.MAGHRIB]) -> Str[R.string.s_now_make_du]
                            else -> Str[R.string.s_passed_for_this_week]
                        }, fontFamily = Nunito, fontSize = 13.sp, color = tk.accent)
                    }
                }
            }
            if (compact) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { cards(Modifier.fillMaxWidth()) }
            else Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { cards(Modifier.weight(1f)) }

            Spacer(Modifier.height(18.dp))
            Text(Str[R.string.s_friday_s_sunnahs], fontFamily = Cormorant, fontSize = 24.sp, color = tk.contentPrimary)
            listOf(
                Str[R.string.s_ghusl_clean_clothes_and_perfume_before] to Str[R.string.s_a_al_bukh_r_880_883],
                Str[R.string.s_go_early_and_walk_if_you] to Str[R.string.s_a_al_bukh_r_881_ab],
                Str[R.string.s_listen_to_the_khu_bah_in] to Str[R.string.s_a_al_bukh_r_934],
                Str[R.string.s_sunnah_prayers_four_after_jumu_ah] to Str[R.string.s_a_muslim_881_882],
                Str[R.string.s_read_s_rat_al_kahf_and] to "above",
            ).forEach { (t, src) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Check, null, Modifier.size(18.dp), tint = tk.success)
                    Spacer(Modifier.width(10.dp))
                    Column { Text(t, fontFamily = Nunito, fontSize = 15.sp, color = tk.contentPrimary); Text(src, fontFamily = Nunito, fontSize = 12.sp, color = tk.contentMuted) }
                }
            }
            if (settings.jumuahEnabled) {
                Spacer(Modifier.height(10.dp))
                val iq = PrayerEngine.iqamah(settings, day, Prayer.DHUHR)
                Text("Jumuʿah at your masjid: azaan ${c(day[Prayer.DHUHR])}" + (iq?.let { " · iqamah ${c(it)}" } ?: ""), fontFamily = Nunito, fontSize = 14.sp, color = tk.accent)
            }
        }
    }
}

@Composable
private fun Card(m: Modifier, title: String, arabic: String, source: String, content: @Composable () -> Unit) {
    val tk = screenTokens()
    Column(m.clip(RoundedCornerShape(16.dp)).background(tk.softFill).border(1.dp, tk.divider, RoundedCornerShape(16.dp)).padding(16.dp)) {
        Text(title.uppercase(), fontFamily = Nunito, fontSize = 11.sp, letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold, color = tk.accent)
        Text(arabic, fontFamily = Amiri, fontSize = 24.sp, color = tk.arabicText)
        Text(source, fontFamily = Nunito, fontSize = 12.sp, color = tk.contentSecondary, lineHeight = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
        content()
    }
}

@Composable
private fun Pill(label: String, on: Boolean, onClick: () -> Unit) {
    val tk = screenTokens()
    Box(Modifier.clip(RoundedCornerShape(50)).background(if (on) tk.success else tk.primary).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = tk.onPrimary)
    }
}
