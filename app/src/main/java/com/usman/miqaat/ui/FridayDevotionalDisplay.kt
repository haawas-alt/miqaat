package com.usman.miqaat.ui

import android.os.SystemClock
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.usman.miqaat.R
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.Prayer
import com.usman.miqaat.data.PrayerEngine
import com.usman.miqaat.data.PrayerState
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class DevotionalSkin(val base: Color, val gold: Color, val pale: Color, val muted: Color)

private fun devotionalSkin(theme: AppTheme): DevotionalSkin = when (theme) {
    AppTheme.MIQAAT -> DevotionalSkin(Color(0xFF062D25), Color(0xFFD9AF5D), Color(0xFFFFF4DC), Color(0xFFC7D8CA))
    AppTheme.KISWAH -> DevotionalSkin(Color(0xFF11100F), Color(0xFFC99A42), Color(0xFFFFF5E2), Color(0xFFD5C8B4))
    AppTheme.CELESTIAL_MERIDIAN -> DevotionalSkin(Color(0xFF081B37), Color(0xFFDBB46C), Color(0xFFF7F3EB), Color(0xFFC2D1E8))
    AppTheme.PRAYER_GALLERY -> error("Prayer Gallery does not use the devotional rotation")
}

private fun devotionalArt(theme: AppTheme, design: Int, wide: Boolean): Int = when (theme) {
    AppTheme.MIQAAT -> when (design) {
        1 -> if (wide) R.drawable.friday_devotional_ma_1_landscape_refined else R.drawable.friday_devotional_ma_1
        2 -> if (wide) R.drawable.friday_devotional_ma_2_landscape_clean_pages else R.drawable.friday_devotional_ma_2_portrait
        3 -> if (wide) R.drawable.friday_devotional_ma_3_landscape_refined else R.drawable.friday_devotional_ma_3_portrait
        else -> if (wide) R.drawable.friday_devotional_ma_4_landscape_refined else R.drawable.friday_devotional_ma_4_portrait
    }
    AppTheme.KISWAH -> when (design) {
        1 -> if (wide) R.drawable.friday_devotional_ka_1 else R.drawable.friday_devotional_ka_1_portrait_refined
        2 -> if (wide) R.drawable.friday_devotional_ka_2 else R.drawable.friday_devotional_ka_2_portrait_refined
        3 -> R.drawable.friday_devotional_ka_3
        else -> R.drawable.friday_devotional_ka_4
    }
    AppTheme.CELESTIAL_MERIDIAN -> when (design) {
        1 -> if (wide) R.drawable.friday_devotional_ca_1 else R.drawable.friday_devotional_ca_1_portrait_refined
        2 -> if (wide) R.drawable.friday_devotional_ca_2 else R.drawable.friday_devotional_ca_2_portrait_refined
        3 -> R.drawable.friday_devotional_ca_3
        else -> R.drawable.friday_devotional_ca_4
    }
    AppTheme.PRAYER_GALLERY -> error("Prayer Gallery does not use the devotional rotation")
}

private data class DevotionalCopy(val arabic: String, val heading: String, val message: String)

private fun devotionalCopy(design: Int, urdu: Boolean): DevotionalCopy = when (design) {
    1 -> DevotionalCopy("جُمُعَةٌ مُبَارَكَةٌ", if (urdu) "جمعہ مبارک" else "Jumu’ah Mubarak",
        if (urdu) "اللہ ہماری عبادت قبول فرمائے۔" else "May Allah accept our worship.")
    2 -> DevotionalCopy("سُورَةُ الْكَهْفِ", if (urdu) "سورۃ الکہف" else "Surah Al-Kahf",
        if (urdu) "آج سورۃ الکہف کی تلاوت کریں۔" else "Recite Surah Al-Kahf today.")
    3 -> DevotionalCopy("اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", if (urdu) "نبی محمد ﷺ پر درود بھیجیں" else "Send salawat upon Prophet Muhammad ﷺ", "")
    else -> DevotionalCopy("رَبَّنَا تَقَبَّلْ مِنَّا", if (urdu) "اے ہمارے رب، ہم سے قبول فرما" else "O Allah, accept from us.",
        if (urdu) "جمعہ کی نماز کی تیاری کریں" else "Prepare for Jumu’ah prayer")
}

/** The existing Friday home remains mounted and retains its actions and prayer state. */
@Composable
internal fun FridayRotatingHome(state: PrayerState, settings: AppSettings, actions: HomeActions, allowed: Boolean) {
    val date = state.now.withZoneSameInstant(settings.zone()).toLocalDate()
    val cycle = remember(settings.theme, date) { FridayDevotionalCycle() }
    var design by remember(settings.theme, date) { mutableStateOf<Int?>(null) }
    var resumed by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        resumed = true
        onPauseOrDispose { resumed = false }
    }
    val active = allowed && resumed && fridayDevotionalEligible(settings, state.now)
    LaunchedEffect(active, settings.theme, date) {
        if (!active) {
            cycle.interrupt()
            design = null
            return@LaunchedEffect
        }
        var previous = SystemClock.elapsedRealtime()
        while (true) {
            delay(minOf(cycle.remainingMillis, 1_000L))
            val now = SystemClock.elapsedRealtime()
            design = cycle.advance(now - previous)
            previous = now
        }
    }
    BackHandler(enabled = design != null) {
        cycle.dismiss()
        design = null
    }
    Box(Modifier.fillMaxSize()) {
        if (fridayHomepageActive(settings, state.now)) {
            FridayApprovedHome(state, settings, actions)
        } else {
            val cfg = androidx.compose.ui.platform.LocalConfiguration.current
            if (cfg.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE && cfg.screenHeightDp < 500)
                PhoneLandscapeHome(state, settings, actions)
            else ApprovedHome(state, settings, actions)
        }
        design?.let { current ->
            FridayDevotionalDisplay(state, settings, actions, current) {
                cycle.dismiss()
                design = null
            }
        }
    }
}

/** A single live-text composition; only artwork, colour, and message change across 12 variants. */
@Composable
internal fun FridayDevotionalDisplay(
    state: PrayerState, settings: AppSettings, actions: HomeActions, design: Int, onDismiss: () -> Unit,
) {
    val skin = devotionalSkin(settings.theme)
    val copy = devotionalCopy(design, L10n.isUrdu(settings))
    val artworkWide = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Box(Modifier.fillMaxSize().background(skin.base).testTag("friday-devotional-${settings.theme.name}-$design")) {
        Image(painterResource(devotionalArt(settings.theme, design, artworkWide)), null,
            Modifier.matchParentSize().testTag("friday-devotional-art"),
            alignment = Alignment.CenterStart, contentScale = ContentScale.Crop)
        val preservePortraitArtwork = !artworkWide && settings.theme != AppTheme.MIQAAT
        Box(Modifier.matchParentSize().background(if (artworkWide)
            Brush.horizontalGradient(0f to Color.Transparent, .43f to skin.base.copy(alpha = .10f), 1f to skin.base.copy(alpha = .78f))
            else if (preservePortraitArtwork) Brush.verticalGradient(
                0f to skin.base.copy(alpha = .24f), .28f to Color.Transparent,
                .55f to skin.base.copy(alpha = .06f), 1f to skin.base.copy(alpha = .32f))
            else Brush.verticalGradient(0f to skin.base.copy(alpha = .30f), .28f to Color.Transparent,
                .55f to skin.base.copy(alpha = .12f), 1f to skin.base.copy(alpha = .80f))))
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(
            0f to Color.Transparent, .60f to Color.Transparent,
            1f to skin.base.copy(alpha = if (preservePortraitArtwork) .15f else .45f))))
        BoxWithConstraints(Modifier.fillMaxSize().clickable(onClick = onDismiss).safeDrawingPadding(),
            contentAlignment = Alignment.TopStart) {
            val wide = maxWidth > maxHeight
            val tablet = minOf(maxWidth, maxHeight) >= 600.dp
            val profile = when {
                tablet && wide -> 3
                tablet -> 2
                wide -> 1
                else -> 0
            }
            fun region(x: Float, y: Float, w: Float, h: Float): Modifier = Modifier
                .offset(x = maxWidth * x, y = maxHeight * y)
                .size(maxWidth * w, maxHeight * h)
            // The raster boards have shorter aspect ratios than the target devices. Match the
            // type-to-width relationship, then let the artwork fill the extra safe height.
            val clockSize = listOf(44f, 58f, 76f, 88f)[profile]
            val dateSize = listOf(14f, 16f, 23f, 24f)[profile]
            val arabicScale = when {
                design == 3 && tablet && wide -> .60f
                design == 3 && wide -> .72f
                design == 3 || design == 4 -> .82f
                else -> 1f
            }
            val arabicSize = listOf(54f, 68f, 98f, 96f)[profile] * arabicScale
            val headingSize = listOf(24f, 28f, 40f, 40f)[profile]
            val messageSize = listOf(18f, 20f, 28f, 28f)[profile]
            val prayerSize = listOf(20, 22, 36, 44)[profile]
            val solarSize = listOf(16, 19, 26, 30)[profile]

            Row(region(if (wide) .035f else .045f, .025f, if (wide) .30f else .34f, .085f),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Mosque, null, Modifier.size(if (tablet) 28.dp else 20.dp), tint = skin.gold)
                Spacer(Modifier.width(5.dp))
                Text("Miqaat", fontFamily = Cormorant, fontSize = (if (tablet) 30 else 19).sp,
                    color = skin.pale, maxLines = 1, modifier = Modifier.testTag("friday-devotional-brand"))
            }
            IconButton(onClick = actions.onOpenSettings,
                modifier = region(if (wide) .32f else .035f, if (wide) .035f else .09f, .12f, .075f)
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp).testTag("friday-devotional-audio-settings")) {
                Icon(Icons.Outlined.VolumeUp, "Audio settings", tint = skin.gold)
            }

            val kiswahPortrait = !wide && settings.theme == AppTheme.KISWAH
            Column(region(if (wide) .49f else if (kiswahPortrait) .48f else .22f,
                if (wide) .035f else if (kiswahPortrait) .035f else .08f,
                if (wide) .47f else if (kiswahPortrait) .48f else .56f,
                if (wide) .29f else if (kiswahPortrait) .21f else .17f),
                horizontalAlignment = if (wide || kiswahPortrait) Alignment.End else Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(PrayerEngine.clock(state.now, settings.use24h),
                        fontFamily = Cormorant, fontSize = clockSize.sp, lineHeight = (clockSize * 1.05f).sp,
                        color = skin.pale, maxLines = 1, style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                        modifier = Modifier.testTag("friday-devotional-clock"))
                    if (!settings.use24h) Text(" ${PrayerEngine.suffix(state.now, false)}", fontFamily = Cormorant,
                        fontSize = (clockSize * .4f).sp, color = skin.pale, maxLines = 1)
                }
                val gregorian = if (L10n.isUrdu(settings)) L10n.fridayDate(state.now.toLocalDate())
                    else state.now.format(DateTimeFormatter.ofPattern(
                        if (wide) "EEE, d MMM yyyy" else "EEEE, d MMMM yyyy", Locale.ENGLISH))
                val hijri = if (settings.showHijri) PrayerEngine.hijri(state.now.toLocalDate(), settings.hijriOffsetDays)
                    .let { if (L10n.isUrdu(settings)) it.arabic else it.english } else null
                Text(listOfNotNull(gregorian, hijri).joinToString("  ·  "), fontFamily = if (L10n.isUrdu(settings)) Amiri else Nunito,
                    fontSize = dateSize.sp, lineHeight = (dateSize * 1.3f).sp, color = skin.muted,
                    textAlign = if (wide || kiswahPortrait) TextAlign.End else TextAlign.Center, maxLines = 2,
                    modifier = Modifier.testTag("friday-devotional-date"))
                val jummah = if (settings.jumuahEnabled) state.today[Prayer.DHUHR] else null
                val relevant = jummah?.takeIf { it.isAfter(state.now) }
                if (relevant != null) {
                    val label = if (L10n.isUrdu(settings)) "جمعہ میں " else "Jumu’ah in "
                    Text(label + L10n.duration(settings, Duration.between(state.now, relevant)),
                        Modifier.padding(top = 3.dp).clip(RoundedCornerShape(50)).background(skin.gold)
                            .padding(horizontal = 10.dp, vertical = 3.dp).testTag("friday-devotional-jummah-countdown"),
                        fontFamily = Nunito, fontWeight = FontWeight.SemiBold,
                        fontSize = (dateSize * .9f).sp, color = skin.base, maxLines = 1)
                }
            }

            val heroX = if (wide) .49f else if (kiswahPortrait) .42f else .07f
            val heroW = if (wide) .47f else if (kiswahPortrait) .54f else .86f
            val heroY = if (wide) (if (tablet) .32f else .34f)
                else if (kiswahPortrait) .29f else if (tablet) .25f else .20f
            Column(region(heroX, heroY, heroW, if (wide) (if (tablet) .43f else .59f) else if (tablet) .39f else .32f)
                    .testTag("friday-devotional-hero"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (wide) Arrangement.Top else Arrangement.Center) {
                Text(copy.arabic, fontFamily = Amiri, fontSize = arabicSize.sp,
                    lineHeight = (arabicSize * 1.35f).sp, color = skin.gold,
                    textAlign = TextAlign.Center, maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("friday-devotional-arabic"))
                if (!(settings.theme == AppTheme.MIQAAT && !wide && design == 3)) Text(if (!L10n.isUrdu(settings) && settings.theme == AppTheme.MIQAAT && design <= 2)
                        copy.heading.uppercase(Locale.ENGLISH) else copy.heading,
                    fontFamily = if (L10n.isUrdu(settings)) Nastaliq else Cormorant,
                    fontWeight = FontWeight.SemiBold, fontSize = headingSize.sp,
                    lineHeight = (headingSize * if (L10n.isUrdu(settings)) 1.65f else 1.18f).sp,
                    color = skin.pale, textAlign = TextAlign.Center, maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("friday-devotional-heading"))
                val separateMA = settings.theme == AppTheme.MIQAAT && !wide && (design == 2 || design == 3 || design == 4)
                if (copy.message.isNotEmpty() && !separateMA) {
                    Spacer(Modifier.height(if (tablet) 9.dp else 4.dp))
                    Text(copy.message, modifier = if (settings.theme == AppTheme.MIQAAT && wide && design != 3)
                            Modifier.fillMaxWidth().border(2.dp, skin.gold, RoundedCornerShape(8.dp))
                                .background(skin.base.copy(alpha = .70f)).padding(6.dp).testTag("friday-devotional-message")
                        else Modifier.fillMaxWidth().testTag("friday-devotional-message"),
                        fontFamily = if (L10n.isUrdu(settings)) Nastaliq else Nunito,
                        fontSize = messageSize.sp, lineHeight = (messageSize * if (L10n.isUrdu(settings)) 1.65f else 1.25f).sp,
                        color = skin.pale, textAlign = TextAlign.Center, maxLines = 3)
                }
            }

            if (settings.theme == AppTheme.MIQAAT && !wide && design in 2..4) {
                val text = if (design == 3) copy.heading else copy.message
                Text(text,
                    modifier = region(.10f, if (design == 3) .57f else .61f, .80f, .10f)
                        .then(if (design == 3) Modifier else Modifier.border(1.dp, skin.gold, RoundedCornerShape(8.dp))
                            .background(skin.base.copy(alpha = .72f)).padding(7.dp))
                        .testTag("friday-devotional-message"),
                    fontFamily = if (L10n.isUrdu(settings)) Nastaliq else Cormorant,
                    fontSize = (if (design == 3) headingSize * .82f else messageSize.toFloat()).sp,
                    lineHeight = (if (design == 3) headingSize * 1.1f else messageSize * 1.2f).sp,
                    color = skin.pale, textAlign = TextAlign.Center, maxLines = 3)
            }

            val diskWidth = if (wide) if (tablet) .23f else .22f else .42f
            val diskHeight = if (wide) .38f else .22f
            val diskSize: Dp = minOf(maxWidth * diskWidth, maxHeight * diskHeight)
            val portraitDiskY = if (settings.theme == AppTheme.MIQAAT) .73f else .64f
            DevotionalPrayerDisk(state, settings, skin, prayerSize,
                Modifier.offset(x = maxWidth * .035f, y = maxHeight * if (wide) .57f else portraitDiskY)
                    .size(diskSize).testTag("friday-devotional-prayer-disk"))
            if (settings.theme == AppTheme.MIQAAT) {
                DevotionalMAPrayerRows(state, settings, skin, solarSize, wide && tablet,
                    region(if (wide) if (tablet) .31f else .27f else .54f,
                        if (wide) if (tablet) .76f else .66f else .76f,
                        if (wide) if (tablet) .64f else .22f else .42f,
                        if (wide) if (tablet) .14f else .28f else .16f)
                        .then(if (wide) Modifier.clip(RoundedCornerShape(9.dp))
                            .background(skin.base.copy(alpha = .78f))
                            .border(1.dp, skin.gold.copy(alpha = .45f), RoundedCornerShape(9.dp))
                            .padding(horizontal = 5.dp) else Modifier)
                        .testTag("friday-devotional-prayer-rows"))
            } else {
                DevotionalSolar(state, settings, skin, solarSize,
                    region(if (wide) .28f else .55f, if (wide) .66f else .67f,
                        if (wide) .18f else .40f, if (wide) .26f else .16f)
                        .testTag("friday-devotional-solar"))
            }
        }
    }
}

@Composable private fun DevotionalPrayerDisk(
    state: PrayerState, settings: AppSettings, skin: DevotionalSkin, size: Int, modifier: Modifier,
) {
    val current = state.current ?: state.next
    val next = state.next
    val currentTime = if (state.current == null) state.nextTime else state.today[current]
    Box(modifier.clip(CircleShape).background(skin.base.copy(alpha = .91f))
        .border(if (size > 20) 2.dp else 1.dp, skin.gold, CircleShape), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(L10n.prayer(settings, current), fontFamily = Nunito, fontSize = (size * .72f).sp,
                color = skin.pale, maxLines = 1)
            Text(L10n.at(currentTime, settings.use24h),
                Modifier.clip(RoundedCornerShape(50)).background(skin.gold)
                    .padding(horizontal = 8.dp, vertical = 1.dp),
                fontFamily = Cormorant, fontSize = (size * 1.15f).sp,
                lineHeight = (size * 1.18f).sp, color = skin.base, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            if (next != current || state.current != null) {
                Text(L10n.prayer(settings, next), fontFamily = Nunito, fontSize = (size * .72f).sp,
                    color = skin.muted, maxLines = 1)
                Text(L10n.at(state.nextTime, settings.use24h), fontFamily = Cormorant,
                    fontSize = size.sp, color = skin.pale, maxLines = 1)
            }
        }
    }
}

@Composable private fun DevotionalSolar(
    state: PrayerState, settings: AppSettings, skin: DevotionalSkin, size: Int, modifier: Modifier,
) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(skin.base.copy(alpha = .68f))
        .border(1.dp, skin.gold.copy(alpha = .5f), RoundedCornerShape(12.dp))
        .padding(horizontal = 6.dp, vertical = 4.dp), verticalArrangement = Arrangement.Center) {
        for (prayer in listOf(Prayer.SUNRISE, Prayer.MAGHRIB)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (prayer == Prayer.SUNRISE) "☀" else "☾", fontSize = size.sp, color = skin.gold)
                Spacer(Modifier.width(3.dp))
                Column {
                    Text(L10n.prayer(settings, prayer), fontFamily = Nunito, fontSize = (size * .72f).sp,
                        color = skin.muted, maxLines = 1)
                    Text(L10n.at(state.today[prayer], settings.use24h), fontFamily = Cormorant,
                        fontSize = size.sp, color = skin.pale, maxLines = 1)
                }
            }
        }
    }
}

/** MA's approved boards use three compact prayer lines, not the paired solar card. */
@Composable private fun DevotionalMAPrayerRows(
    state: PrayerState, settings: AppSettings, skin: DevotionalSkin,
    size: Int, horizontal: Boolean, modifier: Modifier,
) {
    val prayers = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR)
    if (horizontal) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            prayers.forEach { prayer ->
                DevotionalMAPrayerRow(state, settings, skin, size, prayer, Modifier.weight(1f))
            }
        }
    } else {
        Column(modifier, verticalArrangement = Arrangement.SpaceEvenly) {
            prayers.forEach { prayer ->
                DevotionalMAPrayerRow(state, settings, skin, size, prayer, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable private fun DevotionalMAPrayerRow(
    state: PrayerState, settings: AppSettings, skin: DevotionalSkin,
    size: Int, prayer: Prayer, modifier: Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(when (prayer) {
            Prayer.FAJR -> "☼"
            Prayer.DHUHR -> "☀"
            else -> "◕"
        }, fontSize = (size * 1.05f).sp, color = skin.gold)
        Spacer(Modifier.width(4.dp))
        Text(L10n.prayer(settings, prayer), fontFamily = Nunito,
            fontSize = (size * .85f).sp, color = skin.pale, maxLines = 1)
        Spacer(Modifier.width(5.dp))
        Text(L10n.at(state.today[prayer], settings.use24h), fontFamily = Cormorant,
            fontSize = size.sp, color = skin.pale, maxLines = 1)
    }
}
