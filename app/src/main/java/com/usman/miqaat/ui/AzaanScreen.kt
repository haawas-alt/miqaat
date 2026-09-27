package com.usman.miqaat.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.Prayer
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AzaanScreen(prayer: Prayer, onStop: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val u = maxWidth / 100
        val bg = Brush.radialGradient(listOf(Color(0xFF3A1D4F), Color(0xFF160C2A), Color(0xFF0A0716)), center = Offset(0.5f, 0.3f), radius = 1.2f)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2A1440), Color(0xFF0A0716))))) {
            Box(Modifier.fillMaxSize().background(bg, alpha = 0.6f))
            GirihLattice(Modifier.fillMaxSize(), tile = u.value * 11f, alpha = 0.12f)
            Column(Modifier.align(Alignment.Center).padding(bottom = u * 8), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ٱللَّٰهُ أَكْبَرُ", fontFamily = Amiri, fontSize = (u.value * 12f).sp, lineHeight = (u.value * 14f).sp, color = Color(0xFFF6E7B8), textAlign = TextAlign.Center)
                Text(
                    "${prayer.english.uppercase()} AZAAN  ·  ${prayer.arabic}",
                    fontFamily = Cormorant, fontSize = (u.value * 2.6f).sp, letterSpacing = (u.value * 0.6f).sp, color = Palette.ivory.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = u * 1)
                )
                Wave(Modifier.padding(top = u * 4).width(u * 22).height(u * 8))
                Text(
                    "Hayya ʿalaṣ-ṣalāh · Come to prayer",
                    fontFamily = Nunito, fontSize = (u.value * 1.6f).sp, letterSpacing = (u.value * 0.1f).sp, color = Palette.ivory.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = u * 3)
                )
            }
            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = u * 3.4f), horizontalArrangement = Arrangement.spacedBy(u * 1.6f)) {
                Pill("Stop azaan", primary = true, u = u.value, onClick = onStop)
            }
        }
    }
}

@Composable
private fun Pill(label: String, primary: Boolean, u: Float, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        Modifier.clip(shape).background(if (primary) Palette.gold else Color.Transparent)
            .border(1.dp, if (primary) Palette.gold else Color.White.copy(alpha = 0.25f), shape)
            .clickable(onClick = onClick).padding(horizontal = (u * 3).dp, vertical = (u * 1.2f).dp)
    ) { Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = (u * 1.7f).sp, color = if (primary) Color(0xFF160C2A) else Palette.ivory) }
}

@Composable
private fun Wave(modifier: Modifier) {
    val t by rememberInfiniteTransition(label = "wave").animateFloat(0f, 1f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart), label = "t")
    Canvas(modifier) {
        val bars = 15
        val gap = size.width / bars
        for (i in 0 until bars) {
            val phase = (i / bars.toFloat()) * PI * 2
            val hgt = size.height * (0.2f + 0.8f * (0.5f + 0.5f * sin(t * 2 * PI + phase).toFloat()))
            val x = i * gap + gap * 0.3f
            drawRoundRect(
                Brush.verticalGradient(listOf(Palette.gold, Palette.goldDeep)),
                topLeft = Offset(x, (size.height - hgt) / 2), size = Size(gap * 0.4f, hgt),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(gap)
            )
        }
    }
}
