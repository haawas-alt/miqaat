package com.usman.miqaat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState

/** Large-type mode: one prayer, one time, one line. Everything else behind a tap. */
@Composable
fun LargeHome(state: PrayerState, settings: AppSettings, onTap: () -> Unit) {
    val kiswah = settings.theme == AppTheme.KISWAH
    val sky = skyFor(state.period)
    val urdu = L10n.isUrdu(settings)
    BoxWithConstraints(Modifier.fillMaxSize().background(if (kiswah) Brush.verticalGradient(listOf(Color(0xFF0B0B0B), Kiswah.silk)) else Brush.verticalGradient(listOf(sky.top, sky.bottom))).clickable(onClick = onTap)) {
        val portrait = maxHeight > maxWidth
        val u = if (portrait) minOf(maxWidth / 74, maxHeight / 120) else minOf(maxWidth / 100, maxHeight / 56)
        fun fs(x: Float) = (u.value * x).sp
        if (kiswah) Weave(Modifier.fillMaxSize()) else Stars(Modifier.fillMaxSize(), sky.stars)
        val gold = if (kiswah) Kiswah.threadSoft else Palette.goldSoft
        Column(Modifier.fillMaxSize().padding(u * 3), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(state.hero.arabic, fontFamily = if (kiswah) ReemKufi else Amiri, fontSize = fs(9f), lineHeight = fs(10.5f), color = Color(0xFFF6E7B8))
            Text(L10n.prayer(settings, state.hero).let { if (urdu) it else it.uppercase() }, fontFamily = if (urdu) Nastaliq else if (kiswah) Cinzel else Cormorant, fontSize = fs(if (urdu) 5f else 4f), letterSpacing = if (urdu) 0.sp else fs(0.8f), color = gold)
            Row(verticalAlignment = Alignment.Top) {
                Text(PrayerEngine.clock(state.heroTime, settings.use24h), style = TextStyle(fontFamily = if (kiswah) Cinzel else Cormorant, fontSize = fs(24f), lineHeight = fs(24f), brush = if (kiswah) Kiswah.goldText else Brush.verticalGradient(listOf(Palette.ivory, Palette.ivory))))
                val suf = PrayerEngine.suffix(state.heroTime, settings.use24h)
                if (suf.isNotEmpty()) Text(" $suf", fontFamily = if (kiswah) Cinzel else Cormorant, fontSize = fs(5f), color = Palette.ivory, modifier = Modifier.padding(top = u * 4))
            }
            Text(if (state.justPassed) L10n.ago(settings, state.delta) else L10n.inFor(settings, state.delta), fontFamily = if (urdu) Nastaliq else Nunito, fontSize = fs(3.6f), fontWeight = FontWeight.Bold, color = gold, textAlign = TextAlign.Center)
            state.current?.let { cur ->
                PrayerEngine.iqamah(settings, state.today, cur)?.takeIf { it.isAfter(state.now) }?.let { iq ->
                    Text(L10n.iqamahIn(settings, java.time.Duration.between(state.now, iq)), fontFamily = if (urdu) Nastaliq else Nunito, fontSize = fs(2.6f), color = Palette.ivory.copy(alpha = 0.85f), modifier = Modifier.padding(top = u * 1))
                }
            }
        }
        val nxt = Prayer.prayersOnly.firstOrNull { it != state.hero && state.today[it].isAfter(state.now) }
        Text(
            (if (urdu) "سب نمازیں دیکھنے کے لیے چھوئیں" else "Tap for all prayers") + (nxt?.let { "  ·  ${L10n.prayer(settings, it)} ${PrayerEngine.clock(state.today[it], settings.use24h)} ${PrayerEngine.suffix(state.today[it], settings.use24h)}" } ?: ""),
            fontFamily = if (urdu) Nastaliq else Nunito, fontSize = fs(1.8f), color = Palette.ivory.copy(alpha = 0.55f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = u * 2.5f)
        )
    }
}
