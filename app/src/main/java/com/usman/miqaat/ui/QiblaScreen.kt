package com.usman.miqaat.ui

import com.usman.miqaat.data.L10n
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import com.usman.miqaat.R
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
fun QiblaScreen(settings: AppSettings, fixedHeading: Float? = null, onBack: () -> Unit) {
    val tk = screenTokens()
    val ctx = LocalContext.current
    val bearing = remember(settings.latitude, settings.longitude) { PrayerEngine.qibla(settings) }
    // Sensors report a heading from MAGNETIC north; the bearing above is from TRUE north.
    // Android's World Magnetic Model gives the local declination to add (east-positive).
    val declination = remember(settings.latitude, settings.longitude) {
        runCatching { android.hardware.GeomagneticField(settings.latitude.toFloat(), settings.longitude.toFloat(), 0f, System.currentTimeMillis()).declination }.getOrDefault(0f)
    }
    // `fixedHeading` (tests only) replaces the live sensors with a still compass, so a screenshot is not fighting a continuously moving needle.
    var magneticHeading by remember { mutableFloatStateOf(fixedHeading ?: 0f) }
    var hasSensor by remember { mutableStateOf(fixedHeading != null) }
    var accuracy by remember { mutableStateOf(SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) }
    var gotReading by remember { mutableStateOf(fixedHeading != null) }
    val heading = PrayerEngine.trueHeading(magneticHeading.toDouble(), declination.toDouble()).toFloat()

    DisposableEffect(fixedHeading) {
        if (fixedHeading != null) return@DisposableEffect onDispose { }
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rot = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val mag = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        hasSensor = rot != null && mag != null
        val listener = object : SensorEventListener {
            val r = FloatArray(9); val r2 = FloatArray(9); val o = FloatArray(3)
            override fun onSensorChanged(e: SensorEvent) {
                if (e.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
                SensorManager.getRotationMatrixFromVector(r, e.values)
                // remap for the current screen rotation so 'up' on the screen is the reference edge
                @Suppress("DEPRECATION")
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
                magneticHeading = if (!gotReading) az else magneticHeading + ((az - magneticHeading + 540f) % 360f - 180f) * 0.15f   // low-pass, wrap-safe
                gotReading = true
            }
            // The magnetometer's own accuracy is the honest calibration signal; the fused sensor tends to report "high" regardless.
            override fun onAccuracyChanged(s: Sensor?, a: Int) { if (s?.type == Sensor.TYPE_MAGNETIC_FIELD) accuracy = a }
        }
        if (rot != null) sm.registerListener(listener, rot, SensorManager.SENSOR_DELAY_UI)
        if (mag != null) sm.registerListener(listener, mag, SensorManager.SENSOR_DELAY_NORMAL)   // for calibration state only
        onDispose { sm.unregisterListener(listener) }
    }
    val accuracyLow = accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
    val unreliable = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
    val accuracyWords = when {
        !hasSensor -> Str[R.string.s_no_compass_sensor]
        !gotReading -> Str[R.string.s_waiting_for_the_compass]
        unreliable -> Str[R.string.s_compass_unreliable_calibrate]
        accuracyLow -> Str[R.string.s_compass_accuracy_low_about_15]
        accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Str[R.string.s_compass_accuracy_medium_about_5]
        else -> Str[R.string.s_compass_accuracy_high]
    }
    val declWords = Str.get(R.string.s_magnetic_declination, declination)
    val spoken = PrayerEngine.qiblaWords(bearing, if (hasSensor && gotReading && !unreliable) heading.toDouble() else null)

    val needle by animateFloatAsState(if (hasSensor) ((bearing - heading).toFloat() + 360f) % 360f else bearing.toFloat(), tween(200), label = "needle")

    BoxWithConstraints(Modifier.fillMaxSize().background(tk.backgroundBrush)) {
        val u = minOf(maxWidth / 100, maxHeight / 56)
        if (tk.art == ArtStyle.CELESTIAL || tk.art == ArtStyle.GALLERY) ThemedBackdrop(maxHeight > maxWidth, Modifier.fillMaxSize(), scrim = 0.7f) else GirihLattice(Modifier.fillMaxSize(), tile = u.value * 11f, alpha = 0.08f)
        val portrait = maxHeight > maxWidth
        if (portrait) {
            Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = u * 4, vertical = u * 2), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = tk.contentPrimary) }
                    Text(Str[R.string.s_qibla], fontFamily = Cormorant, fontSize = (u.value * 8f).sp, color = tk.contentPrimary)
                    Text("  القبلة", fontFamily = Amiri, fontSize = (u.value * 6.5f).sp, color = tk.accent)
                }
                Box(Modifier.fillMaxWidth().aspectRatio(1f).padding(u * 4).semantics { contentDescription = spoken }, contentAlignment = Alignment.Center) { Compass(needle, heading = if (hasSensor) heading else 0f, u = u.value) }
                Text("${bearing.toInt()}°  ${PrayerEngine.compass(bearing)}", fontFamily = Cormorant, fontSize = (u.value * 14f).sp, lineHeight = (u.value * 14f).sp, color = tk.contentPrimary)
                Text(Str.get(R.string.s_qibla_from_north, L10n.iso(settings.locationName)), fontFamily = Nunito, fontSize = (u.value * 3.2f).sp, color = tk.contentSecondary)
                Text("$accuracyWords · $declWords", fontFamily = Nunito, fontSize = (u.value * 2.8f).sp, color = if (accuracyLow) tk.primary else tk.contentMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = u * 1))
                Text(if (!hasSensor) Str[R.string.s_no_compass_sensor_face_the_phone] else if (accuracyLow) Str[R.string.s_move_the_phone_in_a_figure] else Str[R.string.s_hold_the_phone_flat_and_turn],
                    fontFamily = Nunito, fontSize = (u.value * 3.4f).sp, lineHeight = (u.value * 5f).sp, color = tk.contentPrimary.copy(alpha = 0.8f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = u * 4).testTag("qibla-instruction"))
            }
            return@BoxWithConstraints
        }
        Row(Modifier.fillMaxSize().displayCutoutPadding().padding(horizontal = u * 3, vertical = u * 2)) {
            Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = tk.contentPrimary) }
                    Text(Str[R.string.s_qibla], fontFamily = Cormorant, fontSize = (u.value * 4.2f).sp, color = tk.contentPrimary)
                    Text("  القبلة", fontFamily = Amiri, fontSize = (u.value * 3.4f).sp, color = tk.accent)
                }
                Text("${bearing.toInt()}°  ${PrayerEngine.compass(bearing)}", fontFamily = Cormorant, fontSize = (u.value * 9f).sp, lineHeight = (u.value * 9f).sp, color = tk.contentPrimary, modifier = Modifier.padding(start = u * 1.5f))
                Text(Str.get(R.string.s_qibla_from_north, L10n.iso(settings.locationName)), fontFamily = Nunito, fontSize = (u.value * 1.6f).sp, color = tk.contentSecondary, modifier = Modifier.padding(start = u * 1.6f))
                Text("$accuracyWords · $declWords", fontFamily = Nunito, fontSize = (u.value * 1.5f).sp, color = if (accuracyLow) tk.primary else tk.contentMuted, modifier = Modifier.padding(start = u * 1.6f, top = u * 0.8f))
                Text(
                    when {
                        !hasSensor -> Str[R.string.s_this_tablet_has_no_compass_sensor]
                        accuracyLow -> Str[R.string.s_compass_needs_calibrating_move_the_tablet]
                        else -> Str[R.string.s_lay_the_tablet_flat_turn_until]
                    },
                    fontFamily = Nunito, fontSize = (u.value * 1.6f).sp, lineHeight = (u.value * 2.3f).sp, color = tk.contentPrimary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = u * 1.6f, top = u * 2, end = u * 4)
                )
            }
            Box(Modifier.fillMaxHeight().aspectRatio(1f, matchHeightConstraintsFirst = true).padding(u * 2).semantics { contentDescription = spoken }, contentAlignment = Alignment.Center) {
                Compass(needle, heading = if (hasSensor) heading else 0f, u = u.value)
            }
        }
    }
}

@Composable
private fun Compass(needleDeg: Float, heading: Float, u: Float) {
    val tk = screenTokens()
    Canvas(Modifier.fillMaxSize()) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension / 2
        drawCircle(Color.White.copy(alpha = 0.06f), r, c)
        drawCircle(tk.accent.copy(alpha = 0.5f), r, c, style = Stroke(2f))
        drawCircle(tk.accent.copy(alpha = 0.2f), r * 0.78f, c, style = Stroke(1f))
        // ticks rotate with the device heading so N stays north
        rotate(-heading, c) {
            for (i in 0 until 72) {
                val a = i * 5.0 * PI / 180
                val len = if (i % 18 == 0) r * 0.10f else if (i % 6 == 0) r * 0.06f else r * 0.03f
                val w = if (i % 18 == 0) 3f else 1.2f
                drawLine(tk.contentPrimary.copy(alpha = if (i % 6 == 0) 0.9f else 0.4f),
                    Offset(c.x + (r - len) * sin(a).toFloat(), c.y - (r - len) * cos(a).toFloat()),
                    Offset(c.x + r * 0.97f * sin(a).toFloat(), c.y - r * 0.97f * cos(a).toFloat()), w)
            }
            // N marker
            val nPath = Path().apply {
                moveTo(c.x, c.y - r * 0.86f); lineTo(c.x - r * 0.04f, c.y - r * 0.76f); lineTo(c.x + r * 0.04f, c.y - r * 0.76f); close()
            }
            drawPath(nPath, tk.contentPrimary)
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
            drawPath(p, tk.primary)
            // Kaʿbah mark at the tip
            drawRect(Color(0xFF111111), topLeft = Offset(c.x - r * 0.05f, c.y - r * 0.86f), size = androidx.compose.ui.geometry.Size(r * 0.10f, r * 0.10f))
            drawRect(tk.primary, topLeft = Offset(c.x - r * 0.05f, c.y - r * 0.83f), size = androidx.compose.ui.geometry.Size(r * 0.10f, r * 0.015f))
        }
        drawCircle(tk.primary, r * 0.035f, c)
        drawCircle(tk.background, r * 0.015f, c)
    }
}
