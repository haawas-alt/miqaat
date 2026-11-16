package com.usman.miqaat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import com.usman.miqaat.R
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.usman.miqaat.data.*
import java.util.Locale
import java.time.ZonedDateTime
import java.time.Duration
import kotlin.math.*

private val KiswahFridayGold = Color(0xFFE7C77E)
private val KiswahBannerArabic = FontFamily(Font(R.font.layla_thuluth))
/** Fresh UI tick; the prayer engine can keep its existing minute-level cache. */
internal val LocalKiswahFridayNow = staticCompositionLocalOf<ZonedDateTime?> { null }

/** The four phrases in the approved KA2 banner; no legacy weekday banner text. */
internal const val KISWAH_FRIDAY_DHIKR = "سُبْحَانَ اللَّهِ   الْحَمْدُ لِلَّهِ   لَا إِلَٰهَ إِلَّا اللَّهُ   اللَّهُ أَكْبَرُ"
private val KISWAH_FRIDAY_PHRASES = listOf(
    "اللَّهُ أَكْبَرُ",
    "لَا إِلَٰهَ إِلَّا اللَّهُ",
    "الْحَمْدُ لِلَّهِ",
    "سُبْحَانَ اللَّهِ",
)

@Composable internal fun KiswahFridayBackdrop(modifier: Modifier) {
    Box(modifier.background(Brush.linearGradient(listOf(Color(0xFF030404), Color(0xFF101211),
        Color(0xFF050606), Color(0xFF0D0F0E), Color(0xFF030404)))))
}

/** Text-free embroidery caps traced from the approved KA2 board, with live native dhikr. */
@Composable internal fun KiswahFridayBanner(tablet: Boolean, wide: Boolean, modifier: Modifier) {
    val horizontalInset=if(wide && !tablet)48.dp else if(tablet)24.dp else 10.dp
    BoxWithConstraints(modifier.padding(horizontal=horizontalInset).background(Color(0xFF050606))) {
        val cap=maxHeight
        Canvas(Modifier.matchParentSize().clip(RectangleShape)) {
            val h=size.height
            // The reference has curved embroidered shoulders and double gold rails.
            for(y in listOf(h*.045f,h*.955f))
                drawLine(KiswahFridayGold,Offset(h*.88f,y),Offset(size.width-h*.88f,y),.85.dp.toPx())
            for(y in listOf(h*.075f,h*.925f))
                drawLine(KiswahFridayGold.copy(alpha=.45f),Offset(h*.88f,y),Offset(size.width-h*.88f,y),.4.dp.toPx())
            var x=h
            while(x<size.width-h) {
                val vine=Path().apply {
                    moveTo(x,h*.5f)
                    cubicTo(x-h*.35f,h*.1f,x+h*.55f,h*.03f,x+h*.4f,h*.48f)
                    cubicTo(x+h*.65f,h*.97f,x-h*.25f,h*.9f,x,h*.5f)
                    moveTo(x+h*.12f,h*.5f)
                    cubicTo(x+h*.6f,h*.08f,x+h*.9f,h*.91f,x+h*.12f,h*.5f)
                }
                drawPath(vine,KiswahFridayGold.copy(alpha=.025f),style=Stroke(.45.dp.toPx()))
                x+=h*.8f
            }
        }
        Image(painterResource(R.drawable.ka2_banner_left),null,
            Modifier.align(AbsoluteAlignment.CenterLeft).size(cap).testTag("kiswah-banner-left-embroidery"))
        Image(painterResource(R.drawable.ka2_banner_right),null,
            Modifier.align(AbsoluteAlignment.CenterRight).size(cap).testTag("kiswah-banner-right-embroidery"))
        Row(
            Modifier.fillMaxSize().padding(horizontal=cap + if (wide && !tablet) 7.dp else 5.dp, vertical=3.dp)
                .testTag("kiswah-calligraphy-band"),
            horizontalArrangement=Arrangement.SpaceEvenly,
            verticalAlignment=Alignment.CenterVertically,
        ) {
            KISWAH_FRIDAY_PHRASES.forEachIndexed { index, phrase ->
                KiswahFridayFitText(
                    value=phrase,
                    nominal=if(tablet)19f else if(wide)15f else 15f,
                    font=KiswahBannerArabic,
                    color=Color(0xFFF2D99A),
                    modifier=Modifier.weight(when(index){1->1.48f;3->1.15f;else->1f}).fillMaxHeight(),
                    lineRatio=1.34f,
                    tag="kiswah-calligraphy-phrase-$index",
                )
            }
        }
    }
}

@Composable private fun KiswahFridayFitText(value: String, nominal: Float, font: FontFamily, color: Color,
    modifier: Modifier = Modifier, lineRatio: Float = 1.3f, tag: String) {
    val measurer = rememberTextMeasurer(); val density=LocalDensity.current
    BoxWithConstraints(modifier,contentAlignment=Alignment.Center) {
        val style=TextStyle(fontFamily=font,fontSize=nominal.sp,lineHeight=(nominal*lineRatio).sp,
            color=color,textAlign=TextAlign.Center,platformStyle=PlatformTextStyle(includeFontPadding=true))
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
        Text(value,Modifier.offset(y=(-1).dp).testTag(tag),style=style.copy(fontSize=fitted.sp),softWrap=false,maxLines=1)
    }
}

@Composable internal fun KiswahFridayHero(state: PrayerState, s: AppSettings, a: HomeActions,
    tablet: Boolean, short: Boolean, wide: Boolean, modifier: Modifier, onDetails: ()->Unit) {
    val h=heroInfo(state,s,null);val ur=L10n.isUrdu(s);val c=fridayOption2Palette(s.theme)
    val iq=PrayerEngine.iqamah(s,state.today,h.prayer)
    val now=LocalKiswahFridayNow.current?:state.now
    val seconds=(if(state.justPassed)Duration.between(state.heroTime,now) else Duration.between(now,state.heroTime)).seconds.coerceAtLeast(0)
    val countdown=String.format(Locale.ROOT,"%02d:%02d:%02d",seconds/3600,(seconds/60)%60,seconds%60)
    val until=if(ur) {
        if(state.justPassed) "${h.label} کے بعد" else "${if(state.heroTime.toLocalDate()!=state.now.toLocalDate()) "کل " else ""}${h.label} تک"
    } else "${if(state.justPassed) "since" else "until"} ${h.label}${if(!state.justPassed && state.heroTime.toLocalDate()!=state.now.toLocalDate()) " · tomorrow" else ""}"
    val kicker=if(ur)h.kickerLabel else h.kickerLabel.lowercase(Locale.ENGLISH).split(" ").joinToString(" "){it.replaceFirstChar{ch->ch.uppercase()}}
    val detail=if(ur)"نماز کی تفصیل" else "View prayer details"
    @Composable fun label(value:String,size:Float,tag:String,color:Color=c.ink,font:FontFamily=if(ur)Amiri else FontFamily.Serif) {
        Text(value,Modifier.testTag(tag),fontFamily=font,fontSize=size.sp,
            lineHeight=(size*if(font==Amiri)1.8f else 1.25f).sp,color=color,
            textAlign=TextAlign.Center,maxLines=1,softWrap=false,
            style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false)))
    }
    BoxWithConstraints(modifier.testTag("home-hero")) {
        val scale=LocalDensity.current.fontScale
        val compact=short || maxHeight<(if(tablet)360.dp else 250.dp)*scale
        // The approved landscape arch gives the Arabic name roughly one and a
        // half times the vertical share of the previous native render.
        val arabicSize=if(short) { if(ur)22f else 25f } else if(tablet && !compact)44f else if(ur)30f else 35f
        val kickerSize=if(short)8f else if(tablet)13f else 11f
        val countSize=if(tablet)20f else if(short)12f else 18f
        val daySize=if(compact) { if(short)8f else 10f } else if(tablet)14f else 12f
        val iqSize=if(compact) { if(short)7.5f else 9f } else if(tablet)11f else 9f
        val statusLine=if(short) { if(ur)16f else 12f } else if(ur)20f else 16f
        val bodyLine=if(ur)1.8f else 1.25f
        val fixed=kickerSize*bodyLine+arabicSize*1.8f+countSize*1.25f+statusLine+
            if(compact) maxOf(daySize*bodyLine,if(iq!=null)iqSize*bodyLine else 0f)
            else daySize*bodyLine+if(iq!=null)iqSize*bodyLine else 0f
        // One centred stack: the old weighted clock created large empty gaps and
        // pushed oversized bold numerals across the arch's embroidered rails.
        val clockSize=minOf(if(tablet)68f else if(short)46f else 54f,
            ((maxHeight.value*.88f-9f)/scale-fixed).div(1.28f).coerceAtLeast(12f))
        Column(Modifier.fillMaxSize().absolutePadding(left=0.dp,
            right=maxWidth*(if(wide).24f else if(tablet).20f else .15f))
            .padding(top=maxHeight*.08f,bottom=5.dp)
            .padding(horizontal=if(tablet)12.dp else 6.dp)
            .clickable(role=Role.Button,onClickLabel=detail,onClick=onDetails),
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            label(kicker,kickerSize,"friday-hero-kicker")
            label(if(s.jumuahEnabled && h.prayer==Prayer.DHUHR)"الجمعة" else h.arabic,
                arabicSize,"friday-hero-arabic",c.accent,Amiri)
            Box(Modifier.height((clockSize*1.28f*scale).dp).fillMaxWidth(),contentAlignment=Alignment.Center) {
                FridayTime(state.heroTime,s,clockSize,Modifier.fillMaxWidth()
                    .padding(horizontal=if(tablet)24.dp else 20.dp).clickable(onClick=a.onToggleRelative),
                    "friday-hero-time",c.ink,suffixRatio=if(ur).30f else .56f)
            }
            label(countdown,countSize,"friday-hero-countdown",c.ink,FontFamily.Serif)
            Box(if(!ur)Modifier.testTag("friday-hero-name") else Modifier) {
                Text(until,Modifier.testTag("friday-hero-status"),fontFamily=if(ur)Amiri else FontFamily.Serif,
                    fontSize=(if(tablet)13f else if(short)8f else 11f).sp,lineHeight=statusLine.sp,
                    color=c.ink,textAlign=TextAlign.Center,maxLines=1,softWrap=false)
            }
            if(compact) Row(horizontalArrangement=Arrangement.spacedBy(6.dp),verticalAlignment=Alignment.CenterVertically) {
                label(if(ur)"◇ جمعہ ◇" else "◇ Friday ◇",daySize,"friday-hero-day",c.accent)
                if(iq!=null)label("${L10n.word(s,"Iqamah")} ${L10n.at(iq,s.use24h)}",iqSize,"friday-hero-iqamah",c.secondary)
            } else {
                if(iq!=null)label("${L10n.word(s,"Iqamah")} ${L10n.at(iq,s.use24h)}",iqSize,"friday-hero-iqamah",c.secondary)
                label(if(ur)"◇ ── جمعہ ── ◇" else "◇ ── Friday ── ◇",daySize,"friday-hero-day",c.accent)
            }
        }
    }
}

/** KA2 tablet landscape follows the supplied black-cloth concept: the hero is
 * typography over the Kiswah backdrop, not a second portrait mihrab painting. */
@Composable internal fun KiswahTabletLandscapeFridayHero(state: PrayerState, s: AppSettings,
    a: HomeActions, modifier: Modifier, onDetails: () -> Unit) {
    val c = fridayOption2Palette(s.theme)
    val h = heroInfo(state, s, null)
    val ur = L10n.isUrdu(s)
    val iq = PrayerEngine.iqamah(s, state.today, h.prayer)
    val detail = if (ur) "نماز کی تفصیل" else "View prayer details"
    val title = if (ur) "جمعہ" else "JUMUʿAH"
    val status = if (state.justPassed) L10n.ago(s, state.delta) else L10n.inForState(s, state)
    Column(modifier.testTag("home-hero").clickable(role = Role.Button,
        onClickLabel = detail, onClick = onDetails).padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Mosque, null, Modifier.size(34.dp), tint = c.accent)
        Text(if (ur) "اگلی نماز" else "Next Prayer", Modifier.testTag("friday-hero-kicker"),
            fontFamily = if (ur) Amiri else Cormorant, fontSize = (if (ur) 17 else 16).sp,
            lineHeight = 24.sp, color = c.ink, maxLines = 1)
        Text(title, Modifier.testTag("friday-hero-arabic"),
            fontFamily = if (ur) Nastaliq else Cormorant,
            fontWeight = if (ur) FontWeight.Normal else FontWeight.Medium,
            fontSize = (if (ur) 32 else 40).sp, lineHeight = (if (ur) 52 else 45).sp,
            color = c.accent, maxLines = 1)
        if (!ur) Text("الجمعة", fontFamily = Amiri, fontWeight = FontWeight.Bold,
            fontSize = 27.sp, lineHeight = 32.sp, color = c.accent, maxLines = 1)
        FridayTime(state.heroTime, s, 48f, Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            "friday-hero-time", c.ink, suffixRatio = if (ur) .30f else .48f)
        Box(Modifier.padding(top = 5.dp).clip(RoundedCornerShape(13.dp))
            .background(Color(0xFF090A09).copy(alpha=.88f))
            .border(1.dp, Color(0xFFE7B75E), RoundedCornerShape(13.dp))
            .padding(horizontal = 20.dp, vertical = 5.dp)) {
            Text(status, Modifier.testTag("friday-hero-countdown"),
                fontFamily = if (ur) Amiri else Cormorant, fontSize = (if (ur) 15 else 16).sp,
                lineHeight = 20.sp, color = c.ink, maxLines = 1)
        }
        Box(if(!ur) Modifier.testTag("friday-hero-name") else Modifier) {
            Text(if (ur) "جمعہ تک" else "until Jumuʿah", Modifier.testTag("friday-hero-status"),
                fontFamily = if (ur) Amiri else Cormorant, fontSize = 14.sp, lineHeight = 19.sp,
                color = c.ink, maxLines = 1)
        }
        Text(iq?.let { "${L10n.word(s, "Iqamah")} ${L10n.at(it, s.use24h)}" }
            ?: if (ur) "مسجد کا وقت" else "Masjid time", Modifier.testTag("friday-hero-iqamah"),
            fontFamily = if (ur) Amiri else Cormorant, fontSize = 13.sp, lineHeight = 18.sp,
            color = c.secondary, maxLines = 1)
    }
}




