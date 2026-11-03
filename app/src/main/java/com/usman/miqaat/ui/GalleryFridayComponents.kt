package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.*
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
import androidx.compose.ui.layout.ContentScale
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

internal val LocalGalleryFridayNow=staticCompositionLocalOf<ZonedDateTime?>{null}
private val galleryGold=Color(0xFFE7C77E)
private const val GALLERY_DHIKR_EN="SubhanAllah   ♦   Alhamdulillah   ♦   La ilaha illallah   ♦   Allahu Akbar"
private const val GALLERY_DHIKR_AR="سُبْحَانَ اللَّهِ   ◆   الْحَمْدُ لِلَّهِ   ◆   لَا إِلَٰهَ إِلَّا اللَّهُ   ◆   اللَّهُ أَكْبَرُ"

@Composable internal fun GalleryFridayBackdrop(s:AppSettings,wide:Boolean,modifier:Modifier) {
    BoxWithConstraints(modifier.background(Color(0xFF03152D))) {
        if(s.artTheme!=ArtTheme.MINIMAL) {
            if(wide) {
                val tablet=minOf(maxWidth,maxHeight)>=600.dp
                FridayMihrabReveal(s,Modifier.fillMaxSize().padding(top=if(tablet)108.dp else 48.dp,bottom=if(tablet)40.dp else 28.dp)
                    .graphicsLayer{scaleX=if(L10n.isUrdu(s))-1f else 1f},R.drawable.friday_gallery_landscape,ContentScale.FillBounds)
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to Color(0xFF03152D).copy(alpha=.40f),.25f to Color.Transparent,1f to Color(0xFF03152D).copy(alpha=.15f))))
            } else {
                val art=ImageBitmap.imageResource(R.drawable.friday_gallery_hero)
                Canvas(Modifier.fillMaxSize()) {
                    val top=(size.height*.40f).roundToInt()
                    drawImage(art,IntOffset(0,(art.height*.78f).roundToInt()),IntSize(art.width,(art.height*.22f).roundToInt()),IntOffset(0,top),IntSize(size.width.roundToInt(),size.height.roundToInt()-top),alpha=.72f,filterQuality=FilterQuality.High)
                    drawRect(Brush.verticalGradient(0f to Color(0xFF03152D),.38f to Color(0xFF03152D),.65f to Color(0xFF03152D).copy(alpha=.30f),1f to Color(0xFF03152D).copy(alpha=.15f)))
                }
            }
        }
    }
}

@Composable internal fun GalleryFridayBanner(s:AppSettings,tablet:Boolean,modifier:Modifier) {
    val art=ImageBitmap.imageResource(R.drawable.friday_gallery_hero)
    val ur=L10n.isUrdu(s);val dhikr=if(ur)GALLERY_DHIKR_AR else GALLERY_DHIKR_EN
    BoxWithConstraints(modifier.background(Color(0xFF03152D))) {
        Canvas(Modifier.matchParentSize()) {
            drawImage(art,IntOffset.Zero,IntSize(art.width,60),IntOffset.Zero,IntSize(size.width.roundToInt(),size.height.roundToInt()),alpha=.78f,filterQuality=FilterQuality.High)
            drawLine(galleryGold,Offset(0f,1.dp.toPx()),Offset(size.width,1.dp.toPx()),.6.dp.toPx())
            drawLine(galleryGold,Offset(0f,size.height-1.dp.toPx()),Offset(size.width,size.height-1.dp.toPx()),.6.dp.toPx())
        }
        BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal=if(tablet)38.dp else 18.dp),contentAlignment=Alignment.Center) {
            val measurer=rememberTextMeasurer();val density=LocalDensity.current
            val nominal=if(tablet)18f else if(ur)13f else 12f
            val base=TextStyle(fontFamily=if(ur)Amiri else FontFamily.Serif,fontSize=nominal.sp,lineHeight=(nominal*if(ur)1.8f else 1.25f).sp,color=Color(0xFFF8E7BE),textAlign=TextAlign.Center,platformStyle=PlatformTextStyle(includeFontPadding=false))
            val fitted=remember(maxWidth,density.fontScale,density.density,nominal,dhikr) {
                val available=with(density){(maxWidth-2.dp).toPx()}.coerceAtLeast(1f);var value=nominal
                repeat(4){val l=measurer.measure(dhikr,base.copy(fontSize=value.sp),maxLines=1,softWrap=false);val width=maxOf(l.size.width.toFloat(),l.getLineRight(0)-l.getLineLeft(0));if(width>available)value*=available/width*.99f};value
            }
            Text(dhikr,Modifier.testTag("kiswah-calligraphy-band"),style=base.copy(fontSize=fitted.sp),maxLines=1,softWrap=false)
        }
    }
}

@Composable internal fun GalleryFridayHero(state:PrayerState,s:AppSettings,a:HomeActions,tablet:Boolean,short:Boolean,modifier:Modifier,onDetails:()->Unit) {
    val ur=L10n.isUrdu(s);val c=fridayOption2Palette(s.theme);val info=heroInfo(state,s,null)
    val now=LocalGalleryFridayNow.current?:state.now
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
        val ar=if(short){if(ur)20f else 24f}else if(tablet)64f else 48f
        val kicker=if(short)8f else if(tablet)15f else 12f
        val relative=if(short)11f else if(tablet)21f else 17f
        val metadata=if(short)7.5f else if(tablet)11f else 9f
        val day=if(short)9f else if(tablet)16f else 13f
        // Amiri glyph extents exceed its requested line height at small sizes.
        // Reserve their measured ascent/descent before fitting the display clock.
        val line=if(ur)1.8f else 1.25f
        val fixed=ar*1.8f+kicker*line+relative*line+(if(short)maxOf(metadata,day) else metadata+day)*line+12f
        val size=min(if(tablet)88f else if(short)56f else 68f,((maxHeight.value*.90f-6f)/scale-fixed).div(1.28f).coerceAtLeast(14f))
        Column(Modifier.fillMaxSize().padding(top=maxHeight*.08f,bottom=4.dp).padding(horizontal=if(tablet)24.dp else 12.dp)
            .clickable(role=Role.Button,onClick=onDetails),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            label(if(s.jumuahEnabled && info.prayer==Prayer.DHUHR)"الجمعة" else info.arabic,ar,"friday-hero-arabic",c.accent,true)
            label(if(ur)info.kickerLabel else "Next Prayer",kicker,"friday-hero-kicker")
            Box(Modifier.height((size*1.28f*scale).dp).fillMaxWidth(),contentAlignment=Alignment.Center) {
                FridayTime(state.heroTime,s,size,Modifier.fillMaxWidth().padding(horizontal=12.dp).clickable(onClick=a.onToggleRelative),"friday-hero-time",c.ink,suffixRatio=if(ur).24f else .48f)
            }
            label(countdown,relative,"friday-hero-status")
            val facts:@Composable ()->Unit={Box(if(!ur)Modifier.testTag("friday-hero-name") else Modifier) {label(iqText,metadata,"friday-hero-iqamah",c.secondary)}}
            val friday:@Composable ()->Unit={label(if(ur)"── ◇ جمعہ ◇ ──" else "── ◇ Friday ◇ ──",day,"friday-hero-day",c.accent)}
            if(short)Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){friday();facts()}
            else {facts();friday()}
        }
    }
}

@Composable internal fun GalleryFridayDock(s:AppSettings,state:PrayerState,a:HomeActions,tablet:Boolean,wide:Boolean) {
    val c=fridayOption2Palette(s.theme);val ur=L10n.isUrdu(s)
    val mode=adhkarModes(state,s).firstOrNull()?:AdhkarMode.MORNING
    val inline=tablet||wide
    Row(Modifier.fillMaxWidth().height(if(tablet)66.dp else 56.dp).padding(vertical=3.dp)
        .background(c.background.copy(alpha=.92f),androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
        .border(.7.dp,c.secondary.copy(alpha=.4f),androidx.compose.foundation.shape.RoundedCornerShape(14.dp)),
        verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.weight(1f).fillMaxHeight().testTag("gallery-home-tab").clickable(role=Role.Tab,onClick={})
            .semantics{selected=true;contentDescription=if(ur)"ہوم" else "Home"},contentAlignment=Alignment.Center) {
            Canvas(Modifier.size(48.dp)){drawCircle(Brush.radialGradient(listOf(c.accent.copy(alpha=.5f),Color.Transparent)),radius=size.minDimension*.5f)}
            Icon(Icons.Filled.Home,null,Modifier.size(if(tablet)28.dp else 25.dp),tint=c.ink)
        }
        val items=listOfNotNull(
            if(s.showQibla)Triple(Icons.Outlined.Explore,if(ur)"قبلہ" else "Qibla",a.onOpenQibla) else null,
            if(s.adhkarEnabled)Triple(Icons.Outlined.MenuBook,if(ur)"اذکار" else "Adhkar",{a.onOpenAdhkar(mode)}) else null,
            Triple(Icons.Outlined.AutoStories,if(ur)"سیکھیں" else "Learn",a.onOpenLearn),
            Triple(Icons.Outlined.CalendarMonth,if(ur)"اوقات" else "Timetable",a.onOpenTimetable))
        items.forEach { (icon,label,action)->
            Spacer(Modifier.width(.5.dp).fillMaxHeight(.45f).background(c.secondary.copy(alpha=.5f)))
            val item=Modifier.weight(1f).fillMaxHeight().clickable(role=Role.Button,onClick=action)
            val content:@Composable ()->Unit={
                Icon(icon,label,Modifier.size(if(tablet)25.dp else 20.dp),tint=c.ink)
                Text(label,fontFamily=if(ur)Amiri else FontFamily.Serif,fontSize=(if(tablet)15 else 10).sp,
                    lineHeight=(if(tablet){if(ur)27f else 18.75f}else if(ur)18f else 12.5f).sp,color=c.ink,maxLines=1)
            }
            if(inline)Row(item,verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(if(tablet)8.dp else 5.dp,Alignment.CenterHorizontally)){content()}
            else Column(item,horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){content()}
        }
    }
}
