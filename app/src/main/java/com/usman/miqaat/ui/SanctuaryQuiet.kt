package com.usman.miqaat.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import java.time.ZonedDateTime
import java.util.Locale

private val SanctuaryArabic = FontFamily(Font(R.font.sanctuary_arabic))

/** Live native facts over the approved A sanctuary artwork. No prayer fact is baked into art. */
@Composable internal fun SanctuaryQuietContent(p: Phase.Quiet, s: AppSettings, now: ZonedDateTime, onStop: () -> Unit) {
    val c = quietPalette(s.theme)
    val ur = L10n.isUrdu(s)
    val gallery = s.theme == AppTheme.PRAYER_GALLERY
    val ink = if (gallery) c.ink else Color(0xFFF6D996)
    val times = quietPrayerTimes(s, p.prayer, now)
    val remaining = ((p.endsAt - now.toInstant().toEpochMilli()).coerceAtLeast(0) + 59_999) / 60_000
    val progress = (1f - (p.endsAt - now.toInstant().toEpochMilli()).coerceAtLeast(0) /
        (s.quietMinutes.coerceAtLeast(1) * 60_000f)).coerceIn(0f, 1f)
    fun word(en: String, ar: String) = if (ur) ar else en
    fun name(prayer: Prayer) = L10n.prayer(s, prayer).let { if (ur) it else it.uppercase(Locale.ROOT) }
    val jumuah = s.jumuahEnabled && p.prayer == Prayer.DHUHR && times.current.dayOfWeek == java.time.DayOfWeek.FRIDAY
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    BoxWithConstraints(Modifier.fillMaxSize().background(c.background).testTag("quiet-board")
        .clickable(role = Role.Button, onClickLabel = Str[R.string.s_quiet_exit], onClick = onStop)) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val wide = maxWidth > maxHeight
            val tablet = minOf(maxWidth, maxHeight) >= 600.dp
            val short = wide && !tablet
            val w = maxWidth; val h = maxHeight
            val headerHeight = if(short) 30.dp else if(tablet) 52.dp else 42.dp
            val art = when(s.theme) {
                AppTheme.MIQAAT -> if(wide) R.drawable.sanctuary_miqaat_land else R.drawable.sanctuary_miqaat_port
                AppTheme.KISWAH -> if(wide) R.drawable.sanctuary_kiswah_land else R.drawable.sanctuary_kiswah_port
                AppTheme.CELESTIAL_MERIDIAN -> if(wide) R.drawable.sanctuary_celestial_land else R.drawable.sanctuary_celestial_port
                AppTheme.PRAYER_GALLERY -> if(wide) R.drawable.sanctuary_gallery_land else R.drawable.sanctuary_gallery_port
            }
            Box(Modifier.matchParentSize().testTag("quiet-art-${s.theme.name}")) {
                if(s.artTheme != ArtTheme.MINIMAL) Image(painterResource(art), null, Modifier.matchParentSize().then(if(!wide && s.theme==AppTheme.KISWAH) Modifier.padding(top=headerHeight) else if(!wide && gallery) Modifier.padding(top=h*.06f) else Modifier), contentScale = ContentScale.FillBounds)
            }
            if(s.theme == AppTheme.KISWAH) Box(Modifier.fillMaxWidth().height(h * .035f).offset(y = h * .062f)
                .testTag("quiet-sanctuary-ornament").semantics { contentDescription = word("Gold embroidered ornament", "سنہری کڑھائی") })
            fun region(x: Float,y: Float,width: Float,height: Float) = Modifier.offset(x = w*x,y = h*y).width(w*width).height(h*height)
            Row(Modifier.fillMaxWidth().height(headerHeight).padding(horizontal = w*(if(wide).04f else .075f)), verticalAlignment = Alignment.CenterVertically) {
                SanctuaryText("Miqaat",s,if(tablet) 29f else 23f,ink,Modifier.weight(1f).fillMaxHeight().testTag("quiet-brand"),FontFamily.Serif,TextAlign.Start)
                SanctuaryText(Str[R.string.s_quiet_mode_title].uppercase(Locale.ROOT),s,if(tablet) 12f else 9f,ink,
                    Modifier.weight(1.2f).fillMaxHeight().testTag("quiet-mode-label"),Cinzel)
                SanctuaryText(L10n.iso(L10n.at(now,s.use24h)),s,if(tablet) 25f else 17f,ink,
                    Modifier.weight(1f).fillMaxHeight().testTag("quiet-live-clock"),FontFamily.Serif,TextAlign.End)
            }
            val currentSize = if(wide) (if(short) h.value*.19f else h.value*.18f) else h.value*.105f
            val nextSize = currentSize*.85f
            val currentRegion = if(wide) region(.065f,.10f,.46f,.70f) else region(.10f,.075f,.80f,.33f)
            BoxWithConstraints(currentRegion.testTag("quiet-current-panel")) {
                val ph=maxHeight
                fun row(y:Float,height:Float) = Modifier.offset(y=ph*y).fillMaxWidth().height(ph*height)
                SanctuaryText(if(jumuah) "الجمعة" else p.prayer.arabic,s,currentSize*1.10f,ink,
                    row(-.035f,.33f).testTag("quiet-current-arabic").semantics { heading() },SanctuaryArabic)
                SanctuaryText(if(jumuah) L10n.word(s,"Jumuʿah").uppercase(Locale.ROOT) else name(p.prayer),s,currentSize*.28f,ink,
                    row(.28f,.12f).testTag("quiet-current-name"),Cinzel)
                SanctuaryText(Str[R.string.s_quiet_prayer_progress].uppercase(Locale.ROOT),s,currentSize*.135f,if(gallery)c.accent else ink,
                    row(.41f,.10f).testTag("quiet-progress"),Cinzel)
                SanctuaryText(word("AZAAN AT","اذان کا وقت"),s,currentSize*.12f,ink,row(.53f,.07f).testTag("quiet-current-label"),Cinzel)
                SanctuaryClock(times.current,s,currentSize*1.6f,ink,row(.58f,.33f).testTag("quiet-current-time"))
                SanctuaryText(Str[R.string.s_quiet_iqamah_complete],s,currentSize*.15f,ink,
                    row(.91f,.09f).testTag("quiet-iqamah-complete"),FontFamily.Serif)
            }
            val nextX = when(s.theme) { AppTheme.KISWAH -> .595f; AppTheme.CELESTIAL_MERIDIAN -> .645f; else -> .625f }
            val nextWidth = .25f
            val nextTop = if(s.theme == AppTheme.KISWAH || s.theme == AppTheme.CELESTIAL_MERIDIAN) .32f else .28f
            val celestial = s.theme==AppTheme.CELESTIAL_MERIDIAN
            val nextRegion = if(wide) region(nextX,nextTop,nextWidth,if(gallery || celestial).44f else .49f)
                else if(gallery) region(.23f,.535f,.54f,.25f) else region(.23f,.565f,.54f,.285f)
            BoxWithConstraints(nextRegion.testTag("quiet-next-panel")) {
                val ph=maxHeight
                fun row(y:Float,height:Float)=Modifier.offset(y=ph*y).fillMaxWidth().height(ph*height)
                SanctuaryText(Str[R.string.s_quiet_next_azaan].uppercase(Locale.ROOT),s,nextSize*.14f,ink,row(0f,.10f).testTag("quiet-next-label"),Cinzel)
                SanctuaryText(times.next.arabic,s,nextSize*.95f,ink,row(if(gallery && !wide).20f else if(celestial && wide).18f else .10f,if(gallery && !wide).24f else .28f).testTag("quiet-next-arabic"),SanctuaryArabic)
                SanctuaryText(name(times.next),s,nextSize*.25f,ink,row(if(gallery && !wide).44f else if(celestial && wide).46f else .36f,.10f).testTag("quiet-next-name"),Cinzel)
                SanctuaryClock(times.nextTime,s,nextSize*1.6f,ink,row(if(gallery && !wide).54f else if(celestial && wide).58f else .49f,if(gallery && !wide).38f else if(celestial && wide).36f else .40f).testTag("quiet-next-time"))
                val untilNext=java.time.Duration.between(now,times.nextTime).toMinutes()
                val relative=if(!ur && untilNext>0 && untilNext%60==0L) "In ${untilNext/60} ${if(untilNext==60L)"hour" else "hours"}"
                    else L10n.relative(s,times.nextTime,now).replaceFirstChar {it.uppercase()}
                SanctuaryText(relative,s,nextSize*.18f,ink,
                    row(if(gallery && !wide || celestial && wide).93f else .90f,if(gallery && !wide || celestial && wide).07f else .09f).testTag("quiet-next-relative"),FontFamily.Serif)
                if(times.nextTime.toLocalDate().isAfter(now.withZoneSameInstant(s.zone()).toLocalDate()))
                    SanctuaryText(Str[R.string.s_quiet_tomorrow],s,nextSize*.12f,ink,row(.98f,.08f).testTag("quiet-tomorrow"),FontFamily.Serif)
            }
            val footerHeight = if(wide || tablet) maxOf(56.dp,h*.095f) else maxOf(78.dp,h*.095f)
            Column(Modifier.offset(x=w*.04f,y=h-footerHeight-4.dp).width(w*.92f).height(footerHeight).testTag("quiet-sanctuary-footer")
                .background(c.background.copy(alpha=if(gallery) .94f else .45f),RoundedCornerShape(20.dp)).padding(horizontal=10.dp),
                verticalArrangement=Arrangement.Center) {
                val hint=word("Quiet mode ends in $remaining min","خاموش موڈ $remaining منٹ میں ختم ہوگا")
                val returnButton:@Composable (Modifier)->Unit={m->
                    Box(m.heightIn(min=48.dp).testTag("quiet-return").clickable(role=Role.Button,onClick=onStop),contentAlignment=Alignment.Center) {
                        // The visual phone pill follows the mockup; its native touch target remains 48dp.
                        Box(Modifier.fillMaxWidth().height(if(tablet)48.dp else 36.dp).clip(RoundedCornerShape(30.dp))
                            .background(if(gallery)c.ink else c.background.copy(alpha=.55f))
                            .border(1.dp,if(gallery)c.accent else ink.copy(alpha=.8f),RoundedCornerShape(30.dp)).padding(horizontal=10.dp),contentAlignment=Alignment.Center) {
                            SanctuaryText(Str[R.string.s_quiet_exit],s,if(tablet)18f else 13f,if(gallery)c.background else ink,
                                Modifier.fillMaxSize().testTag("quiet-exit-label"),FontFamily.Serif)
                        }
                    }
                }
                if(wide || tablet) Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(18.dp)) {
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.Center) {
                        SanctuaryText(hint,s,if(tablet)14f else 10f,ink,Modifier.fillMaxWidth().height(if(tablet)26.dp else 18.dp).testTag("quiet-expiry-hint"),FontFamily.Serif)
                        SanctuaryProgress(progress,ink,Modifier.fillMaxWidth().height(5.dp))
                    }
                    returnButton(Modifier.width(w*(if(wide).22f else .27f)).height(48.dp))
                } else {
                    Row(Modifier.fillMaxWidth().height(22.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        SanctuaryProgress(progress,ink,Modifier.weight(.32f).height(4.dp))
                        SanctuaryText(hint,s,10f,ink,Modifier.weight(.68f).fillMaxHeight().testTag("quiet-expiry-hint"),FontFamily.Serif)
                    }
                    returnButton(Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(.76f).height(48.dp))
                }
            }
            if(s.nightDim && p.prayer==Prayer.ISHA) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.22f)))
        }
    }
    }
}

@Composable private fun SanctuaryProgress(progress:Float,color:Color,modifier:Modifier) {
    Canvas(modifier.testTag("quiet-duration-progress").semantics {progressBarRangeInfo=ProgressBarRangeInfo(progress,0f..1f)}) {
        drawRoundRect(color.copy(alpha=.18f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(size.height))
        drawRoundRect(color,size=androidx.compose.ui.geometry.Size(size.width*progress,size.height),cornerRadius=androidx.compose.ui.geometry.CornerRadius(size.height))
    }
}

/** Fit native live text to its approved reading region, including Urdu and 24-hour forms. */
@Composable private fun SanctuaryText(value:String,s:AppSettings,size:Float,color:Color,modifier:Modifier,
    family:FontFamily,align:TextAlign=TextAlign.Center) {
    val ur=L10n.isUrdu(s)
    val measurer=rememberTextMeasurer()
    val density=LocalDensity.current
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    BoxWithConstraints(modifier.semantics {
        text=AnnotatedString(value)
        getTextLayoutResult { out -> layout?.let {out.add(it);true} ?: false }
    },contentAlignment=Alignment.Center) {
        fun style(fs:Float)=TextStyle(fontFamily=if(ur && family!=SanctuaryArabic)Amiri else family,fontSize=fs.sp,
            lineHeight=(fs*(if(family==SanctuaryArabic)1f else if(ur)1.65f else 1.3f)).sp,letterSpacing=if(!ur && family==Cinzel)(fs*.18f).sp else 0.sp,platformStyle=PlatformTextStyle(includeFontPadding=false),
            textAlign=align,textDirection=if(ur)TextDirection.ContentOrRtl else TextDirection.ContentOrLtr,color=color)
        val fitted=remember(value,size,maxWidth,maxHeight,density.fontScale) {
            val m=measurer.measure(value,style(size),softWrap=false,maxLines=1).size
            minOf(1f,with(density){maxWidth.toPx()}/m.width.coerceAtLeast(1),with(density){maxHeight.toPx()}/m.height.coerceAtLeast(1))*size
        }
        Text(value,Modifier.fillMaxWidth(),style=style(fitted),maxLines=1,onTextLayout={layout=it})
    }
}

@Composable private fun SanctuaryClock(time:ZonedDateTime,s:AppSettings,size:Float,color:Color,modifier:Modifier) {
    val text=L10n.iso(L10n.at(time,s.use24h))
    val digits=PrayerEngine.clock(time,s.use24h)
    val measurer=rememberTextMeasurer();val density=LocalDensity.current
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    BoxWithConstraints(modifier.semantics {
        this.text=AnnotatedString(text)
        getTextLayoutResult { out -> layout?.let {out.add(it);true} ?: false }
    },contentAlignment=Alignment.Center) {
        fun annotated(fs:Float)=buildAnnotatedString {
            val start=text.indexOf(digits)
            if(start<0)append(text)else {
                append(text.substring(0,start));withStyle(SpanStyle(fontSize=fs.sp)){append(digits)};append(text.substring(start+digits.length))
            }
        }
        fun style(fs:Float)=TextStyle(fontFamily=if(L10n.isUrdu(s))Amiri else QuietDisplay,fontSize=(fs*.31f).sp,
            fontFeatureSettings="'lnum' 1, 'tnum' 1",lineHeight=(fs*(if(L10n.isUrdu(s))1.5f else 1f)).sp,platformStyle=PlatformTextStyle(includeFontPadding=false),textAlign=TextAlign.Center,color=color)
        val fitted=remember(text,size,maxWidth,maxHeight,density.fontScale) {
            val m=measurer.measure(annotated(size),style(size),softWrap=false,maxLines=1).size
            minOf(1f,with(density){maxWidth.toPx()}/m.width.coerceAtLeast(1),with(density){maxHeight.toPx()}/m.height.coerceAtLeast(1))*size
        }
        Text(annotated(fitted),Modifier.fillMaxWidth(),style=style(fitted),maxLines=1,onTextLayout={layout=it})
    }
}
