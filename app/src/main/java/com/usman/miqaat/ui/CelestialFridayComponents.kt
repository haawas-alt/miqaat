package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import java.time.Duration
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

internal val LocalCelestialFridayNow=staticCompositionLocalOf<ZonedDateTime?>{null}
private val celestialGold=Color(0xFFE7C77E)
internal const val CELESTIAL_FRIDAY_DHIKR="سُبْحَانَ اللَّهِ   ◆   الْحَمْدُ لِلَّهِ   ◆   لَا إِلَٰهَ إِلَّا اللَّهُ   ◆   اللَّهُ أَكْبَرُ"

@Composable internal fun CelestialFridayBackdrop(modifier:Modifier,minimal:Boolean) {
    Box(modifier.background(Color(0xFF03152D))) {
        if(!minimal) {
            val art=ImageBitmap.imageResource(R.drawable.friday_celestial_hero)
            Canvas(Modifier.fillMaxSize()) {
                val w=size.width.roundToInt();val h=size.height.roundToInt();val sky=(h*.34f).roundToInt()
                drawImage(art,IntOffset.Zero,IntSize(art.width,(art.height*.22f).roundToInt()),IntOffset.Zero,IntSize(w,sky),alpha=.62f,filterQuality=FilterQuality.High)
                drawImage(art,IntOffset(0,(art.height*.76f).roundToInt()),IntSize(art.width,(art.height*.24f).roundToInt()),IntOffset(0,sky),IntSize(w,h-sky),alpha=.40f,filterQuality=FilterQuality.High)
                drawRect(Brush.verticalGradient(listOf(Color(0xFF03152D).copy(alpha=.10f),Color(0xFF03152D).copy(alpha=.65f))))
            }
        }
    }
}

@Composable internal fun CelestialFridayBanner(tablet:Boolean,modifier:Modifier) {
    BoxWithConstraints(modifier.background(Color(0xFF03152D).copy(alpha=.88f))) {
        val cap=maxHeight
        Canvas(Modifier.matchParentSize()) {
            val h=size.height
            for(y in listOf(h*.12f,h*.88f))drawLine(celestialGold,Offset(h*.8f,y),Offset(size.width-h*.8f,y),.75.dp.toPx())
            val comet=Path().apply{moveTo(h*1.1f,h*.86f);cubicTo(size.width*.22f,h*.30f,size.width*.26f,h*.17f,size.width*.36f,h*.13f)}
            drawPath(comet,celestialGold.copy(alpha=.12f),style=Stroke(4.dp.toPx()))
            drawPath(comet,celestialGold.copy(alpha=.8f),style=Stroke(.65.dp.toPx()))
        }
        Image(painterResource(R.drawable.ca2_banner_left),null,Modifier.align(AbsoluteAlignment.CenterLeft).size(cap).testTag("celestial-banner-left"))
        Image(painterResource(R.drawable.ca2_banner_right),null,Modifier.align(AbsoluteAlignment.CenterRight).size(cap).testTag("celestial-banner-right"))
        val measurer=rememberTextMeasurer();val density=LocalDensity.current
        BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal=cap),contentAlignment=Alignment.Center) {
            val nominal=if(tablet)19f else 13f
            val base=TextStyle(fontFamily=Amiri,fontSize=nominal.sp,lineHeight=(nominal*1.7f).sp,color=Color(0xFFF8E7BE),textAlign=TextAlign.Center,platformStyle=PlatformTextStyle(includeFontPadding=false))
            val fitted=remember(maxWidth,density.fontScale,density.density,nominal) {
                val available=with(density){(maxWidth-2.dp).toPx()}.coerceAtLeast(1f);var value=nominal
                repeat(4){val l=measurer.measure(CELESTIAL_FRIDAY_DHIKR,base.copy(fontSize=value.sp),maxLines=1,softWrap=false);val width=maxOf(l.size.width.toFloat(),l.getLineRight(0)-l.getLineLeft(0));if(width>available)value*=available/width*.99f};value
            }
            Text(CELESTIAL_FRIDAY_DHIKR,Modifier.testTag("kiswah-calligraphy-band"),style=base.copy(fontSize=fitted.sp),maxLines=1,softWrap=false)
        }
    }
}

@Composable internal fun CelestialFridayHero(state:PrayerState,s:AppSettings,a:HomeActions,tablet:Boolean,short:Boolean,modifier:Modifier,onDetails:()->Unit) {
    val ur=L10n.isUrdu(s);val c=fridayOption2Palette(s.theme);val info=heroInfo(state,s,null)
    val now=LocalCelestialFridayNow.current?:state.now
    val elapsed=if(state.justPassed)Duration.between(state.heroTime,now) else Duration.between(now,state.heroTime)
    val seconds=elapsed.seconds.coerceAtLeast(0);val hh=seconds/3600;val mm=seconds/60%60;val ss=seconds%60
    val clock=String.format(Locale.ROOT,"%d:%02d:%02d",hh,mm,ss)
    val countdown=if(ur) "$clock ${if(state.justPassed)"گزر گئے" else "باقی"}" else "${if(state.justPassed)"" else "in "}${hh}h ${mm}m ${String.format(Locale.ROOT,"%02d",ss)}s${if(state.justPassed)" ago" else ""}"
    val iq=PrayerEngine.iqamah(s,state.today,info.prayer)
    val iqText=(if(!ur)info.label else "")+(if(iq!=null)(if(!ur)" · " else "")+"${L10n.word(s,"Iqamah")} ${L10n.at(iq,s.use24h)}" else "")
    @Composable fun label(value:String,size:Float,tag:String,color:Color=c.ink,arabic:Boolean=ur) {
        Text(value,Modifier.testTag(tag),fontFamily=if(arabic)Amiri else FontFamily.Serif,fontSize=size.sp,lineHeight=(size*if(arabic)1.6f else 1.25f).sp,
            maxLines=1,softWrap=false,color=color,textAlign=TextAlign.Center,style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false)))
    }
    BoxWithConstraints(modifier.testTag("home-hero")) {
        val scale=LocalDensity.current.fontScale
        val ar=if(short){if(ur)22f else 28f}else if(tablet)64f else 48f
        val kicker=if(short)9f else if(tablet)15f else 12f
        val relative=if(short)12f else if(tablet)21f else 17f
        val metadata=if(short)7.5f else if(tablet)11f else 9f
        val day=if(short)9f else if(tablet)16f else 13f
        val line=if(ur)1.6f else 1.25f
        val fixed=ar*1.6f+kicker*line+relative*line+metadata*line+day*line+8f
        val size=min(if(tablet)100f else if(short)56f else 68f,((maxHeight.value*.90f-6f)/scale-fixed).div(1.28f).coerceAtLeast(14f))
        Column(Modifier.fillMaxSize().padding(top=maxHeight*.08f,bottom=4.dp).padding(horizontal=if(tablet)24.dp else 12.dp)
            .clickable(role=Role.Button,onClick=onDetails),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            label(if(s.jumuahEnabled && info.prayer==Prayer.DHUHR)"الجمعة" else info.arabic,ar,"friday-hero-arabic",c.ink,true)
            label(if(ur)info.kickerLabel else info.kickerLabel.lowercase(Locale.ENGLISH).replaceFirstChar{it.uppercase()},kicker,"friday-hero-kicker")
            Box(Modifier.height((size*1.28f*scale).dp).fillMaxWidth(),contentAlignment=Alignment.Center) {
                FridayTime(state.heroTime,s,size,Modifier.fillMaxWidth().padding(horizontal=12.dp).clickable(onClick=a.onToggleRelative),"friday-hero-time",c.ink,suffixRatio=if(ur).24f else .60f)
            }
            label(countdown,relative,"friday-hero-status")
            Box(if(!ur)Modifier.testTag("friday-hero-name") else Modifier) {label(iqText,metadata,"friday-hero-iqamah",c.secondary)}
            label(if(ur)"── ◇ جمعہ ◇ ──" else "── ◇ Friday ◇ ──",day,"friday-hero-day",c.accent)
        }
    }
}
