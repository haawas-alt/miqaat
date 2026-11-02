package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.data.*
import java.util.Locale
import java.time.ZonedDateTime
import java.time.Duration
import kotlin.math.*

private val KiswahFridayGold = Color(0xFFE7C77E)
/** Fresh UI tick; the prayer engine can keep its existing minute-level cache. */
internal val LocalKiswahFridayNow = staticCompositionLocalOf<ZonedDateTime?> { null }

/** The four phrases in the approved KA2 banner; no legacy weekday banner text. */
internal const val KISWAH_FRIDAY_DHIKR = "سُبْحَانَ اللَّهِ   الْحَمْدُ لِلَّهِ   لَا إِلَٰهَ إِلَّا اللَّهُ   اللَّهُ أَكْبَرُ"

@Composable internal fun KiswahFridayBackdrop(modifier: Modifier) {
    Box(modifier.background(Brush.linearGradient(listOf(Color(0xFF030404), Color(0xFF101211),
        Color(0xFF050606), Color(0xFF0D0F0E), Color(0xFF030404)))))
}

/** Native notched embroidery, with proportional end motifs instead of floral box corners. */
@Composable internal fun KiswahFridayBanner(tablet: Boolean, wide: Boolean, modifier: Modifier) {
    Box(modifier.background(Color(0xFF050606))) {
        Canvas(Modifier.matchParentSize().clip(RectangleShape)) {
            val h = size.height; val notch = min(h * .31f, size.width * .06f)
            fun outline(inset: Float): Path = Path().apply {
                moveTo(notch, inset); lineTo(size.width-notch, inset)
                lineTo(size.width-notch*.42f, h*.23f); lineTo(size.width-inset, h*.5f)
                lineTo(size.width-notch*.42f, h*.77f); lineTo(size.width-notch, h-inset)
                lineTo(notch, h-inset); lineTo(notch*.42f, h*.77f)
                lineTo(inset, h*.5f); lineTo(notch*.42f, h*.23f); close()
            }
            drawPath(outline(1.dp.toPx()), KiswahFridayGold, style=Stroke(.8.dp.toPx()))
            drawPath(outline(3.dp.toPx()), KiswahFridayGold.copy(alpha=.6f), style=Stroke(.45.dp.toPx()))
            for (right in listOf(false,true)) {
                val center = Offset(if(right) size.width-notch else notch, h/2)
                val radius = h*.20f
                for (rotation in listOf(0f,45f)) {
                    val star = Path()
                    for(i in 0..4) {
                        val angle = Math.toRadians((rotation+i*90).toDouble())
                        val point = Offset(center.x+radius*cos(angle).toFloat(),center.y+radius*sin(angle).toFloat())
                        if(i==0) star.moveTo(point.x,point.y) else star.lineTo(point.x,point.y)
                    }
                    drawPath(star,KiswahFridayGold.copy(alpha=.8f),style=Stroke(.6.dp.toPx()))
                }
                drawCircle(KiswahFridayGold,h*.026f,center)
            }
        }
        KiswahFridayFitText(KISWAH_FRIDAY_DHIKR, if(tablet) 18f else if(wide) 13f else 12f,
            Amiri, KiswahFridayGold, Modifier.fillMaxSize().padding(horizontal=if(tablet) 32.dp else 22.dp)
                , lineRatio=1.75f, tag="kiswah-calligraphy-band")
    }
}

@Composable private fun KiswahFridayFitText(value: String, nominal: Float, font: FontFamily, color: Color,
    modifier: Modifier = Modifier, lineRatio: Float = 1.3f, tag: String) {
    val measurer = rememberTextMeasurer(); val density=LocalDensity.current
    BoxWithConstraints(modifier,contentAlignment=Alignment.Center) {
        val style=TextStyle(fontFamily=font,fontSize=nominal.sp,lineHeight=(nominal*lineRatio).sp,
            color=color,textAlign=TextAlign.Center,platformStyle=PlatformTextStyle(includeFontPadding=false))
        val fitted=remember(value,nominal,maxWidth,density.fontScale,density.density) {
            val available=with(density){(maxWidth-2.dp).toPx()}.coerceAtLeast(1f)
            var size=nominal
            repeat(4) {
                val result=measurer.measure(value,style.copy(fontSize=size.sp),softWrap=false,maxLines=1)
                val width=maxOf(result.size.width.toFloat(),result.getLineRight(0)-result.getLineLeft(0))
                if(width>available)size*=(available/width)*.99f
            }
            size
        }
        Text(value,Modifier.testTag(tag),style=style.copy(fontSize=fitted.sp),softWrap=false,maxLines=1)
    }
}

@Composable internal fun KiswahFridayHero(state: PrayerState, s: AppSettings, a: HomeActions,
    tablet: Boolean, short: Boolean, modifier: Modifier, onDetails: ()->Unit) {
    val h=heroInfo(state,s,null);val ur=L10n.isUrdu(s);val c=fridayOption2Palette(s.theme)
    val iq=PrayerEngine.iqamah(s,state.today,h.prayer)
    val now=LocalKiswahFridayNow.current?:state.now
    val seconds=(if(state.justPassed)Duration.between(state.heroTime,now) else Duration.between(now,state.heroTime)).seconds.coerceAtLeast(0)
    val countdown=String.format(Locale.ROOT,"%02d:%02d:%02d",seconds/3600,(seconds/60)%60,seconds%60)
    val until=if(ur) {
        if(state.justPassed) "${h.label} کے بعد" else "${if(state.heroTime.toLocalDate()!=state.now.toLocalDate()) "کل " else ""}${h.label} تک"
    } else "${if(state.justPassed) "since" else "until"} ${h.label}${if(!state.justPassed && state.heroTime.toLocalDate()!=state.now.toLocalDate()) " · tomorrow" else ""}"
    val kicker=if(ur)h.kickerLabel else h.kickerLabel.lowercase(Locale.ENGLISH).replaceFirstChar{it.uppercase()}
    val detail=if(ur)"نماز کی تفصیل" else "View prayer details"
    @Composable fun label(value:String,size:Float,tag:String,color:Color=c.ink,font:FontFamily=if(ur)Amiri else FontFamily.Serif) {
        Text(value,Modifier.testTag(tag),fontFamily=font,fontSize=size.sp,
            lineHeight=(size*if(font==Amiri)1.8f else 1.25f).sp,color=color,
            textAlign=TextAlign.Center,maxLines=1,softWrap=false)
    }
    BoxWithConstraints(modifier.testTag("home-hero")) {
        val scale=LocalDensity.current.fontScale
        val compact=short || maxHeight<(if(tablet)360.dp else 250.dp)*scale
        val arabicSize=if(short)24f else if(tablet && !compact)54f else 38f
        Column(Modifier.fillMaxSize().absolutePadding(right=maxWidth*.24f)
            .padding(top=maxHeight*.12f, bottom=if(short)1.dp else 5.dp)
            .padding(horizontal=if(tablet)12.dp else 6.dp)
            .clickable(role=Role.Button,onClickLabel=detail,onClick=onDetails),
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            if(short) Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                label(kicker,9f,"friday-hero-kicker",c.ink)
                label(if(s.jumuahEnabled && h.prayer==Prayer.DHUHR)"الجمعة" else h.arabic,arabicSize,"friday-hero-arabic",c.accent,Amiri)
            } else {
                label(kicker,if(tablet)13f else 11f,"friday-hero-kicker")
                label(if(s.jumuahEnabled && h.prayer==Prayer.DHUHR)"الجمعة" else h.arabic,arabicSize,"friday-hero-arabic",c.accent,Amiri)
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center) {
                val nominal=if(tablet)122f else if(short)68f else 82f
                val fitted=minOf(nominal,(maxHeight.value/scale/1.28f).coerceAtLeast(12f))
                FridayTime(state.heroTime,s,fitted,Modifier.fillMaxWidth().clickable(onClick=a.onToggleRelative),
                    "friday-hero-time",c.ink,suffixRatio=if(ur).24f else .43f)
            }
            label(countdown,if(tablet)26f else if(short)15f else 21f,"friday-hero-countdown",c.ink,FontFamily.Serif)
            Box(if(!ur)Modifier.testTag("friday-hero-name") else Modifier) {
            Text(until,Modifier.testTag("friday-hero-status"),
                fontFamily=if(ur)Amiri else FontFamily.Serif,fontSize=(if(tablet)13f else if(short)9f else 11f).sp,
                lineHeight=(if(ur)20f else 16f).sp,color=c.ink,textAlign=TextAlign.Center,maxLines=1,softWrap=false)
            }
            if(compact) Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically) {
                label(if(ur)"◇ جمعہ ◇" else "◇ Friday ◇",if(short)9f else 10f,"friday-hero-day",c.accent)
                if(iq!=null)label("${L10n.word(s,"Iqamah")} ${L10n.at(iq,s.use24h)}",if(short)8f else 9f,"friday-hero-iqamah",c.secondary)
            } else {
                if(iq!=null)label("${L10n.word(s,"Iqamah")} ${L10n.at(iq,s.use24h)}",if(tablet)11f else 9f,"friday-hero-iqamah",c.secondary)
                label(if(ur)"◇ ── جمعہ ── ◇" else "◇ ── Friday ── ◇",if(tablet)14f else 12f,"friday-hero-day",c.accent)
            }
        }
    }
}
