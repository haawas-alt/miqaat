package com.usman.miqaat.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.PrayerEngine
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(settings: AppSettings, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val bearing = remember(settings.latitude, settings.longitude) { PrayerEngine.qibla(settings) }
    var heading by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(false) }
    var accuracyLow by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rot = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        hasSensor = rot != null
        val listener = object : SensorEventListener {
            val r = FloatArray(9); val r2 = FloatArray(9); val o = FloatArray(3)
            override fun onSensorChanged(e: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(r, e.values)
                // remap for landscape so 'up' on the screen is the reference edge
                val rotation = (ctx.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay.rotation
                val (ax, ay) = when (rotation) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }
                SensorManager.remapCoordinateSystem(r, ax, ay, r2)
                SensorManager.getOrientation(r2, o)
                val az = (Math.toDegrees(o[0].toDouble()).toFloat() + 360f) % 360f
                heading = heading + ((az - heading + 540f) % 360f - 180f) * 0.15f   // low-pass, wrap-safe
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) { accuracyLow = a < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM }
        }
        if (rot != null) sm.registerListener(listener, rot, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(listener) }
    }

    val needle by animateFloatAsState(if (hasSensor) ((bearing - heading).toFloat() + 360f) % 360f else bearing.toFloat(), tween(200), label = "needle")

    BoxWithConstraints(Modifier.fillMaxSize().background(Palette.panel)) {
        val u = minOf(maxWidth / 100, maxHeight / 56)
        GirihLattice(Modifier.fillMaxSize(), tile = u.value * 11f, alpha = 0.08f)
        Row(Modifier.fillMaxSize().padding(horizontal = u * 3, vertical = u * 2)) {
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Palette.ivory) }
                    Text("Qibla", fontFamily = Cormorant, fontSize = (u.value * 4.2f).sp, color = Palette.ivory)
                    Text("  القبلة", fontFamily = Amiri, fontSize = (u.value * 3.4f).sp, color = Palette.goldSoft)
                }
                Text("${bearing.toInt()}°  ${PrayerEngine.compass(bearing)}", fontFamily = Cormorant, fontSize = (u.value * 9f).sp, lineHeight = (u.value * 9f).sp, color = Color(0xFFF6E7B8), modifier = Modifier.padding(start = u * 1.5f))
                Text("from true north, at ${settings.locationName}", fontFamily = Nunito, fontSize = (u.value * 1.6f).sp, color = Palette.ivory.copy(alpha = 0.7f), modifier = Modifier.padding(start = u * 1.6f))
                Text(
                    when {
                        !hasSensor -> "This tablet has no compass sensor. Lay it flat with its top edge facing north (a phone compass helps), and the gold needle shows the Qibla."
                        accuracyLow -> "Compass needs calibrating: move the tablet in a figure-of-eight a few times, away from speakers and metal."
                        else -> "Lay the tablet flat. Turn until the gold needle points straight up, then you are facing the Kaʿbah."
                    },
                    fontFamily = Nunito, fontSize = (u.value * 1.6f).sp, lineHeight = (u.value * 2.3f).sp, color = Palette.ivory.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = u * 1.6f, top = u * 2, end = u * 4)
                )
            }
            Box(Modifier.fillMaxHeight().aspectRatio(1f, matchHeightConstraintsFirst = true).padding(u * 2), contentAlignment = Alignment.Center) {
                Compass(needle, heading = if (hasSensor) heading else 0f, u = u.value)
            }
        }
    }
}

@Composable
private fun Compass(needleDeg: Float, heading: Float, u: Float) {
    Canvas(Modifier.fillMaxSize()) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension / 2
        drawCircle(Color.White.copy(alpha = 0.06f), r, c)
        drawCircle(Palette.goldSoft.copy(alpha = 0.5f), r, c, style = Stroke(2f))
        drawCircle(Palette.goldSoft.copy(alpha = 0.2f), r * 0.78f, c, style = Stroke(1f))
        // ticks rotate with the device heading so N stays north
        rotate(-heading, c) {
            for (i in 0 until 72) {
                val a = i * 5.0 * PI / 180
                val len = if (i % 18 == 0) r * 0.10f else if (i % 6 == 0) r * 0.06f else r * 0.03f
                val w = if (i % 18 == 0) 3f else 1.2f
                drawLine(Palette.ivory.copy(alpha = if (i % 6 == 0) 0.9f else 0.4f),
                    Offset(c.x + (r - len) * sin(a).toFloat(), c.y - (r - len) * cos(a).toFloat()),
                    Offset(c.x + r * 0.97f * sin(a).toFloat(), c.y - r * 0.97f * cos(a).toFloat()), w)
            }
            // N marker
            val nPath = Path().apply {
                moveTo(c.x, c.y - r * 0.86f); lineTo(c.x - r * 0.04f, c.y - r * 0.76f); lineTo(c.x + r * 0.04f, c.y - r * 0.76f); close()
            }
            drawPath(nPath, Palette.ivory)
        }
        // Qibla needle
        rotate(needleDeg, c) {
            val p = Path().apply {
                moveTo(c.x, c.y - r * 0.72f)
                lineTo(c.x - r * 0.07f, c.y + r * 0.08f)
                lineTo(c.x, c.y + r * 0.02f)
                lineTo(c.x + r * 0.07f, c.y + r * 0.08f)
                close()
            }
            drawPath(p, Palette.gold)
            // Kaʿbah mark at the tip
            drawRect(Color(0xFF111111), topLeft = Offset(c.x - r * 0.05f, c.y - r * 0.86f), size = androidx.compose.ui.geometry.Size(r * 0.10f, r * 0.10f))
            drawRect(Palette.gold, topLeft = Offset(c.x - r * 0.05f, c.y - r * 0.83f), size = androidx.compose.ui.geometry.Size(r * 0.10f, r * 0.015f))
        }
        drawCircle(Palette.gold, r * 0.035f, c)
        drawCircle(Palette.night, r * 0.015f, c)
    }
}
