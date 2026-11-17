package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Friday-only Prayer Gallery presentation. Coordinates: Option A contract A-1.0. */
private val gardenInk = Color(0xFF073B34)
private val gardenPaper = Color(0xFFFFF9EE)
private val gardenRust = Color(0xFFA9472D)
private val gardenGold = Color(0xFFC99845)
private val gardenSage = Color(0xFFDCE8D8)
private val gardenLine = Color(0xFFD9C9AA)
private val gardenDisplay = FontFamily(Font(R.font.sanctuary_display))
private val gardenClock = FontFamily(Font(R.font.sanctuary_clock))
private val gardenArabic = FontFamily(Font(R.font.amiri_regular))

private data class GBox(val x: Int, val y: Int, val w: Int, val h: Int)
private data class GLayout(val width: Int, val height: Int, val header: GBox, val art: GBox,
    val title: GBox, val clock: GBox, val prayers: GBox, val actions: GBox, val nav: GBox)

private val gardenLayouts = listOf(
    GLayout(390,844,GBox(0,0,390,58),GBox(10,58,370,252),GBox(30,295,330,56),GBox(18,356,354,100),GBox(14,464,362,234),GBox(14,706,362,70),GBox(0,786,390,58)),
    GLayout(844,390,GBox(0,0,844,42),GBox(322,42,512,268),GBox(490,214,326,42),GBox(590,93,226,113),GBox(12,46,300,264),GBox(322,312,512,40),GBox(0,354,844,36)),
    GLayout(768,1024,GBox(0,0,768,70),GBox(18,70,732,350),GBox(116,392,536,72),GBox(98,468,572,116),GBox(20,596,728,304),GBox(20,908,728,54),GBox(0,972,768,52)),
    GLayout(1280,800,GBox(0,0,1280,62),GBox(474,62,790,502),GBox(644,470,450,62),GBox(760,297,312,155),GBox(18,74,438,488),GBox(474,574,790,120),GBox(0,714,1280,86))
)

@Composable
internal fun PrayerGalleryFridayOptionA(state: PrayerState, s: AppSettings, a: HomeActions,
    onClosePreview: (() -> Unit)? = null) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFFF8F3E8)).safeDrawingPadding().testTag("gallery-friday-a")) {
        val wide = maxWidth > maxHeight
        val tablet = minOf(maxWidth,maxHeight) >= 600.dp
        val layout = gardenLayouts[if (tablet) { if (wide) 3 else 2 } else { if (wide) 1 else 0 }]
        val sx = maxWidth.value / layout.width
        val sy = maxHeight.value / layout.height
        fun GBox.modifier() = Modifier.offset((x*sx).dp,(y*sy).dp).size((w*sx).dp,(h*sy).dp)
        Image(painterResource(R.drawable.prayer_gallery_friday_garden_a),null,
            layout.art.modifier().clip(RoundedCornerShape(if(wide)12.dp else 16.dp)).testTag("gallery-friday-a-art"),
            contentScale=ContentScale.Crop)
        GardenHeader(state,s,a,layout.header.modifier(),wide,onClosePreview)
        GardenTitle(s,layout.title.modifier(),wide)
        GardenClock(state,s,layout.clock.modifier(),wide,tablet)
        GardenPrayers(state,s,a,layout.prayers.modifier(),wide,tablet)
        GardenActions(s,a,layout.actions.modifier(),wide)
        GardenNav(s,a,layout.nav.modifier(),wide)
    }
}

@Composable private fun GardenHeader(state:PrayerState,s:AppSettings,a:HomeActions,m:Modifier,wide:Boolean,onClosePreview:(()->Unit)?) {
    val now=LocalGalleryFridayNow.current?:state.now
    Row(m.background(gardenPaper.copy(alpha=.96f)).padding(horizontal=if(wide)12.dp else 14.dp),verticalAlignment=Alignment.CenterVertically) {
        Icon(Icons.Outlined.Place,null,Modifier.size(20.dp).clickable(onClick=a.onOpenLocation),tint=gardenRust)
        Text(L10n.place(s.locationName),Modifier.weight(1f).clickable(onClick=a.onOpenLocation),fontSize=if(wide)11.sp else 13.sp,color=gardenInk,maxLines=1,overflow=TextOverflow.Ellipsis)
        Text(now.format(DateTimeFormatter.ofPattern("EEE d MMM yyyy",Locale.ENGLISH)),fontSize=if(wide)9.sp else 10.sp,color=gardenRust,maxLines=1)
        Spacer(Modifier.width(10.dp))
        Text(L10n.iso(L10n.at(now,s.use24h)),fontFamily=gardenClock,fontSize=if(wide)11.sp else 13.sp,color=gardenInk,maxLines=1)
        Icon(Icons.Outlined.Settings,null,Modifier.size(40.dp).clickable(onClick=onClosePreview?:a.onOpenSettings).padding(8.dp),tint=gardenInk)
    }
}

@Composable private fun GardenTitle(s:AppSettings,m:Modifier,wide:Boolean) {
    Column(m.clip(RoundedCornerShape(12.dp)).background(gardenPaper.copy(alpha=.97f)).border(1.dp,gardenGold,RoundedCornerShape(12.dp))
        .testTag("gallery-friday-a-title"),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Text(if(L10n.isUrdu(s))"جمعہ / JUMU'AH" else "JUMU'AH / الجمعة",fontFamily=if(L10n.isUrdu(s))gardenArabic else gardenDisplay,
            fontSize=if(wide)18.sp else 26.sp,lineHeight=29.sp,color=gardenRust,maxLines=1)
        if(!wide)Text("A day for prayer, remembrance and reflection",fontSize=10.sp,color=gardenRust,maxLines=1)
    }
}

@Composable private fun GardenClock(state:PrayerState,s:AppSettings,m:Modifier,wide:Boolean,tablet:Boolean) {
    val now=LocalGalleryFridayNow.current?:state.now
    val left=Duration.between(now,state.nextTime).seconds.coerceAtLeast(0)
    val countdown="In ${left/3600} h ${(left%3600)/60} min"
    Row(m.clip(RoundedCornerShape(12.dp)).background(gardenPaper.copy(alpha=.97f)).border(1.dp,gardenLine,RoundedCornerShape(12.dp))
        .padding(horizontal=12.dp,vertical=6.dp).testTag("gallery-friday-a-clock"),verticalAlignment=Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("NEXT PRAYER",fontSize=9.sp,color=gardenRust)
            Text(L10n.prayer(s,state.next),fontFamily=if(L10n.isUrdu(s))gardenArabic else gardenDisplay,fontSize=if(wide)14.sp else 18.sp,color=gardenInk,maxLines=1)
            Text(L10n.iso(L10n.at(state.nextTime,s.use24h)),fontFamily=gardenClock,fontSize=if(tablet)42.sp else if(wide)34.sp else 43.sp,
                lineHeight=if(tablet)46.sp else 45.sp,color=gardenInk,maxLines=1)
        }
        Column(horizontalAlignment=Alignment.End) {
            Text(countdown,Modifier.clip(RoundedCornerShape(20.dp)).background(gardenRust).padding(horizontal=9.dp,vertical=5.dp),
                fontSize=if(wide)9.sp else 11.sp,color=Color.White,maxLines=1)
            if(s.jumuahEnabled)Text("Iqamah ${s.jumuahMinutes/60}:${(s.jumuahMinutes%60).toString().padStart(2,'0')}",fontSize=9.sp,color=gardenInk,maxLines=1)
        }
    }
}

@Composable private fun GardenPrayers(state:PrayerState,s:AppSettings,a:HomeActions,m:Modifier,wide:Boolean,tablet:Boolean) {
    val prayers=listOf(Prayer.FAJR,Prayer.SUNRISE,Prayer.DHUHR,Prayer.ASR,Prayer.MAGHRIB,Prayer.ISHA)
    val columns=if(tablet&&!wide)2 else 1
    Column(m.testTag("gallery-friday-a-prayers"),verticalArrangement=Arrangement.spacedBy(if(tablet)4.dp else 2.dp)) {
        for(row in 0 until (6/columns)) Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            for(col in 0 until columns) {
                val index=row*columns+col;val prayer=prayers[index];val selected=prayer==state.next
                Row(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(if(selected)gardenSage else gardenPaper.copy(alpha=.97f))
                    .border(1.dp,if(selected)gardenGold else gardenLine,RoundedCornerShape(10.dp))
                    .clickable { a.onPreparePrayer(prayer) }.padding(horizontal=6.dp),verticalAlignment=Alignment.CenterVertically) {
                    GardenMiniature(index,Modifier.size(if(tablet)52.dp else if(wide)29.dp else 30.dp).clip(RoundedCornerShape(8.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text(if(prayer==Prayer.DHUHR&&s.jumuahEnabled)"Jumu'ah" else L10n.prayer(s,prayer),Modifier.weight(1f),
                        fontFamily=if(L10n.isUrdu(s))gardenArabic else gardenDisplay,fontSize=if(tablet)15.sp else if(wide)11.sp else 12.sp,
                        color=gardenInk,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(L10n.iso(L10n.at(state.today[prayer],s.use24h)),fontFamily=gardenClock,fontSize=if(tablet)17.sp else if(wide)12.sp else 13.sp,color=gardenInk,maxLines=1)
                }
            }
        }
    }
}

@Composable private fun GardenMiniature(index:Int,m:Modifier) {
    val atlas=ImageBitmap.imageResource(R.drawable.tablet_gallery_atlas_181)
    Canvas(m) {
        val sw=atlas.width/3;val sh=atlas.height/2
        drawImage(atlas,srcOffset=androidx.compose.ui.unit.IntOffset((index%3)*sw,(index/3)*sh),
            srcSize=androidx.compose.ui.unit.IntSize(sw,sh),dstSize=androidx.compose.ui.unit.IntSize(size.width.toInt(),size.height.toInt()))
    }
}

@Composable private fun GardenActions(s:AppSettings,a:HomeActions,m:Modifier,wide:Boolean) {
    val items=listOf(Triple("Al-Kahf",Icons.Outlined.MenuBook,a.onOpenKahf),Triple("Salawat",Icons.Outlined.FavoriteBorder,a.onOpenSalawat),
        Triple("Friday reminders",Icons.Outlined.Event,a.onOpenFriday))
    Row(m.testTag("gallery-friday-a-actions"),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        items.forEachIndexed { i,item ->
            Row(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(if(i==0)gardenRust else gardenPaper)
                .border(1.dp,if(i==0)gardenRust else gardenGold,RoundedCornerShape(12.dp)).clickable(onClick=item.third).padding(horizontal=7.dp),
                verticalAlignment=Alignment.CenterVertically) {
                Icon(item.second,null,Modifier.size(if(wide)18.dp else 25.dp),tint=if(i==0)Color.White else gardenInk)
                Spacer(Modifier.width(5.dp))
                Text(item.first,fontSize=if(wide)10.sp else 11.sp,color=if(i==0)Color.White else gardenInk,maxLines=2,overflow=TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable private fun GardenNav(s:AppSettings,a:HomeActions,m:Modifier,wide:Boolean) {
    val items=listOf(Triple("Qibla",Icons.Outlined.Explore,a.onOpenQibla),Triple("Adhkar",Icons.Outlined.AutoStories,{a.onOpenAdhkar(AdhkarMode.MORNING)}),
        Triple("Learn",Icons.Outlined.MenuBook,a.onOpenLearn),Triple("Timetable",Icons.Outlined.CalendarMonth,a.onOpenTimetable))
    Row(m.background(gardenPaper).border(1.dp,gardenLine),verticalAlignment=Alignment.CenterVertically) {
        items.forEach { item ->
            Column(Modifier.weight(1f).fillMaxHeight().clickable(onClick=item.third),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                Icon(item.second,null,Modifier.size(if(wide)17.dp else 22.dp),tint=gardenInk)
                Text(item.first,fontSize=if(wide)8.sp else 10.sp,color=gardenInk,maxLines=1)
            }
        }
    }
}
