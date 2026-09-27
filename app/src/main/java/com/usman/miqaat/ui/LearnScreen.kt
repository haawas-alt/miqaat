package com.usman.miqaat.ui

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.Adhkar
import java.util.Locale

/** Children's mode: the words of the prayer, one position at a time, with a picture and "hear it". */
@Composable
fun LearnScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val steps = Adhkar.salah
    var i by remember { mutableIntStateOf(0) }
    val step = steps[i]
    val tts = remember { TextToSpeech(ctx) { } }
    DisposableEffect(Unit) { onDispose { tts.stop(); tts.shutdown() } }
    fun speak() {
        val ar = tts.isLanguageAvailable(Locale("ar")) >= TextToSpeech.LANG_AVAILABLE
        tts.language = if (ar) Locale("ar") else Locale.ENGLISH
        tts.setSpeechRate(0.8f)
        tts.speak(if (ar) step.arabic.replace("۝", "،").replace("·", "،") else step.transliteration, TextToSpeech.QUEUE_FLUSH, null, "learn")
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1F6F73), Color(0xFF0E3B44))))) {
        val compact = maxWidth < 700.dp
        val u = if (compact) maxWidth / 60 else minOf(maxWidth / 100, maxHeight / 56)
        fun fs(x: Float) = (u.value * x).sp
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = u * 4, vertical = u * 2)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White) }
                Text("LEARN SALAH  ·  STEP ${i + 1} OF ${steps.size}", fontFamily = Nunito, fontSize = fs(1.4f), letterSpacing = fs(0.25f), fontWeight = FontWeight.Bold, color = Palette.goldSoft)
            }
            val picture: @Composable (Modifier) -> Unit = { m -> Box(m.clip(RoundedCornerShape(u * 2)).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Text(step.emoji, fontSize = fs(if (compact) 18f else 12f)) } }
            val words: @Composable (Modifier) -> Unit = { m ->
                Column(m.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center) {
                    Text(step.position.uppercase(), fontFamily = Nunito, fontSize = fs(1.5f), letterSpacing = fs(0.25f), fontWeight = FontWeight.Bold, color = Palette.goldSoft)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(step.arabic, fontFamily = Amiri, fontSize = fs(if (step.arabic.length > 160) 3.2f else 4.4f), lineHeight = fs(if (step.arabic.length > 160) 5.4f else 7f), color = Color(0xFFF6E7B8), textAlign = TextAlign.Start, modifier = Modifier.padding(vertical = u * 1).fillMaxWidth())
                    }
                    Text(step.transliteration, fontFamily = Cormorant, fontSize = fs(2.3f), lineHeight = fs(3f), color = Color.White)
                    Text(step.meaning, fontFamily = Nunito, fontSize = fs(1.7f), lineHeight = fs(2.5f), color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(top = u * 0.8f))
                    Text(step.note, fontFamily = Nunito, fontSize = fs(1.35f), lineHeight = fs(2f), color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(top = u * 0.8f))
                }
            }
            if (compact) Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(u * 1.5f)) { picture(Modifier.fillMaxWidth().height(u * 14)); words(Modifier.weight(1f)) }
            else Row(Modifier.weight(1f).fillMaxWidth().padding(top = u * 1), horizontalArrangement = Arrangement.spacedBy(u * 3)) { picture(Modifier.fillMaxHeight().aspectRatio(1f, matchHeightConstraintsFirst = true)); words(Modifier.weight(1f).fillMaxHeight()) }
            Row(Modifier.fillMaxWidth().padding(top = u * 1.5f), horizontalArrangement = Arrangement.spacedBy(u * 1.2f), verticalAlignment = Alignment.CenterVertically) {
                Big("‹", enabled = i > 0, u = u.value) { i-- }
                Box(Modifier.weight(1f).heightIn(min = 48.dp).height(u * 5).clip(RoundedCornerShape(50)).background(Palette.gold).clickable(role = androidx.compose.ui.semantics.Role.Button) { if (i < steps.lastIndex) i++ else onBack() }, contentAlignment = Alignment.Center) {
                    Text(if (i < steps.lastIndex) "Next: ${steps[i + 1].position}  ›" else "Finished · well done!", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = fs(1.9f), color = Palette.night)
                }
                Box(Modifier.size(maxOf(u * 5, 48.dp)).clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape).clickable(role = androidx.compose.ui.semantics.Role.Button) { speak() }, contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Outlined.VolumeUp, "Hear it", Modifier.size(u * 2.4f), tint = Color.White) }
            }
            Text(steps.indices.joinToString(" ") { if (it <= i) "●" else "○" }, fontFamily = Nunito, fontSize = fs(1.4f), letterSpacing = fs(0.2f), color = Color.White.copy(alpha = 0.75f), modifier = Modifier.padding(top = u * 1).align(Alignment.CenterHorizontally).semantics { contentDescription = "Step ${i + 1} of ${steps.size}" })
        }
    }
}

@Composable
private fun Big(t: String, enabled: Boolean, u: Float, onClick: () -> Unit) {
    Box(Modifier.size(maxOf((u * 5).dp, 48.dp)).clip(CircleShape).border(1.dp, Color.White.copy(alpha = if (enabled) 0.5f else 0.15f), CircleShape).clickable(enabled = enabled, onClick = onClick, role = androidx.compose.ui.semantics.Role.Button).semantics { contentDescription = "Previous step" }, contentAlignment = Alignment.Center) {
        Text(t, fontSize = (u * 3).sp, color = Color.White.copy(alpha = if (enabled) 1f else 0.4f))
    }
}
