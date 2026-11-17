package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
private val gardenArabicBold = FontFamily(Font(R.font.amiri_bold))
private val gardenBody = FontFamily(Font(R.font.nunito_sans))

private data class GBox(val x: Int, val y: Int, val w: Int, val h: Int)
private data class GLayout(val width: Int, val height: Int, val header: GBox, val art: GBox,
    val title: GBox, val clock: GBox, val prayers: GBox, val actions: GBox, val nav: GBox)

private val gardenLayouts = listOf(
    GLayout(390,844,GBox(0,0,390,80),GBox(10,80,370,180),GBox(30,245,330,62),GBox(18,310,354,92),GBox(14,420,362,234),GBox(14,665,362,98),GBox(0,775,390,69)),
    GLayout(844,390,GBox(0,0,844,44),GBox(300,0,544,390),GBox(0,0,0,0),GBox(598,137,232,226),GBox(14,46,300,288),GBox(0,0,0,0),GBox(0,340,590,50)),
    GLayout(768,1024,GBox(0,0,768,70),GBox(18,70,732,292),GBox(90,320,588,75),GBox(24,415,340,220),GBox(20,665,728,238),GBox(380,415,360,220),GBox(0,922,768,102)),
    GLayout(1280,800,GBox(0,0,1280,80),GBox(480,0,800,640),GBox(620,520,600,110),GBox(0,0,0,0),GBox(20,140,500,490),GBox(20,632,1240,110),GBox(0,744,1280,56))
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
        if(layout.title.w>0) GardenTitle(s,layout.title.modifier(),wide,tablet)
        if(layout.clock.w>0) GardenClock(state,s,layout.clock.modifier(),wide,tablet)
        GardenPrayers(state,s,a,layout.prayers.modifier(),wide,tablet)
        if(layout.actions.w>0) GardenActions(s,a,layout.actions.modifier(),wide,tablet)
        GardenNav(s,a,layout.nav.modifier(),wide,tablet)
    }
}

@Composable private fun GardenHeader(state:PrayerState,s:AppSettings,a:HomeActions,m:Modifier,wide:Boolean,onClosePreview:(()->Unit)?) {
    val now=LocalGalleryFridayNow.current?:state.now
    Row(m.background(if(wide)Color.Transparent else gardenPaper.copy(alpha=.97f))
        .padding(horizontal=if(wide)10.dp else 12.dp),verticalAlignment=Alignment.CenterVertically) {
        Row(verticalAlignment=Alignment.CenterVertically) {
            GardenMosque(Modifier.size(if(wide)24.dp else 28.dp))
            Spacer(Modifier.width(4.dp))
            Text("MIQAAT",fontFamily=FontFamily.Serif,fontWeight=FontWeight.Bold,
                fontSize=if(wide)14.sp else 16.sp,color=gardenInk,maxLines=1)
        }
        Spacer(Modifier.width(if(wide)14.dp else 10.dp))
        Icon(Icons.Outlined.Place,null,Modifier.size(17.dp).clickable(onClick=a.onOpenLocation),tint=gardenInk)
        Column(Modifier.weight(1f).clickable(onClick=a.onOpenLocation)) {
            Text(L10n.place(s.locationName),fontFamily=gardenBody,fontWeight=FontWeight.Bold,
                fontSize=if(wide)10.sp else 11.sp,color=gardenInk,maxLines=1,overflow=TextOverflow.Ellipsis)
            Text(now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy",Locale.ENGLISH)),fontFamily=gardenBody,
                fontSize=if(wide)8.sp else 9.sp,color=gardenInk.copy(alpha=.72f),maxLines=1)
        }
        Spacer(Modifier.width(6.dp))
        Text(PrayerEngine.clock(now,s.use24h)+" "+PrayerEngine.suffix(now,s.use24h),fontFamily=gardenClock,
            fontWeight=FontWeight.Bold,fontSize=if(wide)10.sp else 12.sp,color=gardenInk,maxLines=1)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.Outlined.Settings,"Settings",Modifier.size(40.dp).clickable(onClick=onClosePreview?:a.onOpenSettings).padding(8.dp),tint=gardenInk)
    }
}

@Composable private fun GardenMosque(m:Modifier) {
    Canvas(m) {
        val w=size.width; val h=size.height
        val dome=Path().apply { moveTo(w*.18f,h*.52f); quadraticTo(w*.5f,h*.05f,w*.82f,h*.52f); close() }
        drawPath(dome,gardenInk)
        drawRect(gardenInk,Offset(w*.18f,h*.49f),androidx.compose.ui.geometry.Size(w*.64f,h*.38f))
        drawRect(gardenInk,Offset(w*.04f,h*.34f),androidx.compose.ui.geometry.Size(w*.09f,h*.53f))
        drawRect(gardenInk,Offset(w*.87f,h*.34f),androidx.compose.ui.geometry.Size(w*.09f,h*.53f))
        drawCircle(gardenInk,w*.045f,Offset(w*.085f,h*.28f))
        drawCircle(gardenInk,w*.045f,Offset(w*.915f,h*.28f))
        drawCircle(gardenInk,w*.04f,Offset(w*.5f,h*.09f))
        drawRect(gardenPaper,Offset(w*.43f,h*.65f),androidx.compose.ui.geometry.Size(w*.14f,h*.22f))
    }
}

@Composable private fun GardenTitle(s:AppSettings,m:Modifier,wide:Boolean,tablet:Boolean) {
    Column(m.clip(RoundedCornerShape(topStart=24.dp,topEnd=24.dp,bottomStart=12.dp,bottomEnd=12.dp))
        .background(gardenPaper.copy(alpha=.98f)).border(1.dp,gardenGold,RoundedCornerShape(topStart=24.dp,topEnd=24.dp,bottomStart=12.dp,bottomEnd=12.dp))
        .testTag("gallery-friday-a-title"),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Text(if(L10n.isUrdu(s))"JUMU’AH / جمعہ" else "JUMU’AH / الجمعة",
            fontFamily=FontFamily.Serif,fontWeight=FontWeight.Bold,
            fontSize=if(tablet)31.sp else 26.sp,lineHeight=if(tablet)35.sp else 29.sp,color=gardenRust,maxLines=1)
        Text(if(L10n.isUrdu(s))"دعا، ذکر اور غور و فکر کا دن" else "A day for prayer, remembrance and reflection",
            fontFamily=if(L10n.isUrdu(s))gardenArabic else gardenBody,fontSize=if(tablet)12.sp else 10.sp,color=gardenRust,maxLines=1)
    }
}

@Composable private fun GardenClock(state:PrayerState,s:AppSettings,m:Modifier,wide:Boolean,tablet:Boolean) {
    val now=LocalGalleryFridayNow.current?:state.now
    val left=Duration.between(now,state.nextTime).seconds.coerceAtLeast(0)
    val countdown="In ${left/3600} h ${(left%3600)/60} min"
    val isJumuah=state.next==Prayer.DHUHR && s.jumuahEnabled
    val iq=PrayerEngine.iqamah(s,state.today,Prayer.DHUHR)
    val name=if(isJumuah) { if(L10n.isUrdu(s))"جمعہ" else "JUMU’AH / الجمعة" } else L10n.prayer(s,state.next)
    val detail=if(isJumuah&&iq!=null)"Masjid time · Iqamah ${L10n.at(iq,s.use24h)}" else ""
    val shape=RoundedCornerShape(if(wide&&!tablet)24.dp else 12.dp)
    val frame=m.clip(shape).background(gardenPaper.copy(alpha=.97f)).border(1.dp,gardenLine,shape)
        .testTag("gallery-friday-a-clock")
    if(wide&&!tablet || tablet&&!wide) {
        Column(frame.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            Text(if(L10n.isUrdu(s))"اگلی نماز" else "Next Prayer",fontFamily=gardenBody,fontWeight=FontWeight.Bold,
                fontSize=if(tablet)13.sp else 11.sp,color=gardenInk,maxLines=1)
            Text(name,fontFamily=if(L10n.isUrdu(s))gardenArabicBold else FontFamily.Serif,
                fontWeight=FontWeight.Bold,fontSize=if(tablet)18.sp else 15.sp,color=gardenInk,maxLines=1)
            Row(verticalAlignment=Alignment.Bottom) {
                Text(PrayerEngine.clock(state.nextTime,s.use24h),Modifier.testTag("gallery-friday-a-time"),
                    fontFamily=gardenClock,fontWeight=FontWeight.Bold,fontSize=if(tablet)54.sp else 43.sp,
                    lineHeight=if(tablet)57.sp else 46.sp,color=gardenInk,maxLines=1)
                if(!s.use24h)Text(PrayerEngine.suffix(state.nextTime,false),fontFamily=FontFamily.Serif,
                    fontSize=if(tablet)20.sp else 15.sp,color=gardenInk,maxLines=1)
            }
            Text(countdown,Modifier.clip(RoundedCornerShape(20.dp)).background(gardenRust)
                .padding(horizontal=13.dp,vertical=4.dp),fontFamily=gardenBody,fontWeight=FontWeight.Bold,
                fontSize=if(tablet)13.sp else 11.sp,color=Color.White,maxLines=1)
            if(detail.isNotEmpty())Text(detail,fontFamily=gardenBody,fontSize=if(tablet)10.sp else 8.sp,
                color=gardenInk,maxLines=1)
        }
    } else {
        Row(frame.padding(horizontal=12.dp,vertical=4.dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if(L10n.isUrdu(s))"اگلی نماز" else "Next Prayer",fontFamily=gardenBody,fontSize=9.sp,color=gardenInk,maxLines=1)
                Text(name,fontFamily=if(L10n.isUrdu(s))gardenArabicBold else FontFamily.Serif,
                    fontWeight=FontWeight.Bold,fontSize=15.sp,color=gardenInk,maxLines=1)
                Row(verticalAlignment=Alignment.Bottom) {
                    Text(PrayerEngine.clock(state.nextTime,s.use24h),Modifier.testTag("gallery-friday-a-time"),
                        fontFamily=gardenClock,fontWeight=FontWeight.Bold,fontSize=42.sp,lineHeight=45.sp,
                        color=gardenInk,maxLines=1)
                    if(!s.use24h)Text(PrayerEngine.suffix(state.nextTime,false),fontFamily=FontFamily.Serif,
                        fontSize=16.sp,color=gardenInk,maxLines=1)
                }
            }
            Column(horizontalAlignment=Alignment.End) {
                Text(countdown,Modifier.clip(RoundedCornerShape(20.dp)).background(gardenRust)
                    .padding(horizontal=8.dp,vertical=4.dp),fontFamily=gardenBody,
                    fontSize=10.sp,color=Color.White,maxLines=1)
                if(detail.isNotEmpty())Text(detail,fontFamily=gardenBody,fontSize=8.sp,color=gardenInk,maxLines=1)
            }
        }
    }
}

@Composable private fun GardenPrayers(state:PrayerState,s:AppSettings,a:HomeActions,m:Modifier,wide:Boolean,tablet:Boolean) {
    val prayers=listOf(Prayer.FAJR,Prayer.SUNRISE,Prayer.DHUHR,Prayer.ASR,Prayer.MAGHRIB,Prayer.ISHA)
    val columns=if(tablet&&!wide)2 else 1
    Box(m.testTag("gallery-friday-a-prayers")) {
        if(!tablet&&!wide)Canvas(Modifier.fillMaxSize()) {
            val x=10.dp.toPx()
            drawLine(gardenGold,Offset(x,10.dp.toPx()),Offset(x,size.height-10.dp.toPx()),1.4.dp.toPx())
        }
        Column(Modifier.fillMaxSize(),verticalArrangement=Arrangement.spacedBy(if(tablet)5.dp else 2.dp)) {
            for(row in 0 until (6/columns)) Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                for(col in 0 until columns) {
                    val index=row*columns+col
                    val prayer=prayers[index]
                    val info=fridayRowInfo(prayer,state,s)
                    val selected=prayer==state.next
                    val passed=state.today[prayer].isBefore(state.now)
                    val end=when(prayer) {
                        Prayer.DHUHR -> PrayerEngine.iqamah(s,state.today,prayer)
                        Prayer.SUNRISE -> state.today[prayer]
                        else -> if(s.showEndTimes)PrayerEngine.endOf(s,state.today,prayer) else null
                    }
                    val endLabel=end?.let { "– ${PrayerEngine.clock(it,s.use24h)} ${PrayerEngine.suffix(it,s.use24h)}" } ?: ""
                    val shape=RoundedCornerShape(if(tablet)10.dp else 8.dp)
                    Row(Modifier.weight(1f).fillMaxHeight().testTag("gallery-friday-a-row-${prayer.name}")
                        .clip(shape).background(if(selected)gardenSage else gardenPaper.copy(alpha=.96f))
                        .border(if(selected)1.dp else .5.dp,if(selected)gardenGold else gardenLine,shape)
                        .semantics { contentDescription=info.spoken }
                        .clickable { a.onPreparePrayer(prayer) }.padding(horizontal=if(tablet)8.dp else 5.dp),
                        verticalAlignment=Alignment.CenterVertically) {
                        GardenMiniature(index,Modifier.size(if(tablet)if(wide)50.dp else 54.dp else if(wide)30.dp else 29.dp)
                            .clip(CircleShape).border(1.dp,gardenGold,CircleShape))
                        Spacer(Modifier.width(if(tablet)8.dp else 5.dp))
                        val label=if(prayer==Prayer.DHUHR&&s.jumuahEnabled) {
                            if(L10n.isUrdu(s))"جمعہ" else "Jumu’ah"
                        } else info.label
                        if(tablet&&!wide) Column(Modifier.weight(1f)) {
                            Row(verticalAlignment=Alignment.CenterVertically) {
                                Text(label,fontFamily=if(L10n.isUrdu(s))gardenArabicBold else FontFamily.Serif,
                                    fontWeight=FontWeight.Bold,fontSize=15.sp,color=gardenInk,maxLines=1)
                                Spacer(Modifier.width(6.dp))
                                Text(info.arabic,fontFamily=gardenArabic,fontSize=12.sp,color=gardenInk,maxLines=1)
                            }
                            Row(verticalAlignment=Alignment.CenterVertically) {
                                Text(L10n.iso(L10n.at(state.today[prayer],s.use24h)),Modifier.testTag("gallery-friday-a-row-time-${prayer.name}"),
                                    fontFamily=gardenClock,fontWeight=FontWeight.Bold,fontSize=15.sp,color=gardenInk,maxLines=1)
                                Spacer(Modifier.width(5.dp))
                                Text(endLabel,fontFamily=gardenBody,fontSize=9.sp,color=gardenInk.copy(alpha=.75f),maxLines=1)
                            }
                        } else {
                            Text(label,Modifier.weight(.25f),fontFamily=if(L10n.isUrdu(s))gardenArabicBold else FontFamily.Serif,
                                fontWeight=FontWeight.Bold,fontSize=if(tablet)15.sp else if(wide)11.sp else 12.sp,
                                color=gardenInk,maxLines=1,overflow=TextOverflow.Ellipsis)
                            Text(info.arabic,Modifier.weight(.18f),fontFamily=gardenArabic,
                                fontSize=if(tablet)12.sp else 9.sp,color=gardenInk,maxLines=1)
                            Text(L10n.iso(L10n.at(state.today[prayer],s.use24h)),Modifier.weight(.31f).testTag("gallery-friday-a-row-time-${prayer.name}"),
                                fontFamily=gardenClock,fontWeight=FontWeight.Bold,fontSize=if(tablet)16.sp else if(wide)11.sp else 12.sp,
                                color=gardenInk,maxLines=1)
                            Text(endLabel,Modifier.weight(.26f),fontFamily=gardenBody,
                                fontSize=if(tablet)10.sp else 8.sp,color=gardenInk.copy(alpha=.75f),maxLines=1)
                        }
                        if(selected)Icon(Icons.Outlined.ChevronRight,null,Modifier.size(if(tablet)18.dp else 14.dp),tint=gardenInk)
                        else if(passed)Icon(Icons.Outlined.CheckCircle,null,Modifier.size(if(tablet)18.dp else 14.dp),tint=Color(0xFF3C8366))
                        else Icon(Icons.Outlined.Schedule,null,Modifier.size(if(tablet)18.dp else 14.dp),tint=gardenInk)
                    }
                }
            }
        }
    }
}

@Composable private fun GardenMiniature(index:Int,m:Modifier) {
    val atlas=ImageBitmap.imageResource(R.drawable.tablet_gallery_atlas_181)
    Canvas(m) {
        val sceneTop=(atlas.height*.455f).toInt()
        val tileWidth=atlas.width/3
        val tileHeight=(atlas.height-sceneTop)/2
        val side=minOf(tileWidth,tileHeight)
        val x=(index%3)*tileWidth+(tileWidth-side)/2
        val y=sceneTop+(index/3)*tileHeight
        drawImage(atlas,srcOffset=androidx.compose.ui.unit.IntOffset(x,y),
            srcSize=androidx.compose.ui.unit.IntSize(side,side),
            dstSize=androidx.compose.ui.unit.IntSize(size.width.toInt(),size.height.toInt()),filterQuality=FilterQuality.High)
    }
}

@Composable private fun GardenActions(s:AppSettings,a:HomeActions,m:Modifier,wide:Boolean,tablet:Boolean) {
    val labels=listOf(L10n.word(s,"Al-Kahf"),L10n.word(s,"Salawat"),L10n.word(s,"Friday reminders"))
    val sub=listOf("Read this Surah","Send blessings","Prepare for Jumu’ah")
    val clicks=listOf(a.onOpenKahf,a.onOpenSalawat,a.onOpenFriday)
    if(tablet&&!wide) {
        Column(m.testTag("gallery-friday-a-actions"),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            for(i in 0..2) Row(Modifier.fillMaxWidth().weight(1f).testTag("gallery-friday-a-action-$i")
                .clip(RoundedCornerShape(12.dp)).background(gardenPaper)
                .border(.8.dp,gardenLine,RoundedCornerShape(12.dp)).clickable(onClick=clicks[i])
                .padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(gardenGold.copy(alpha=.22f)),contentAlignment=Alignment.Center) {
                    GardenActionIcon(i,30.dp)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(labels[i],fontFamily=FontFamily.Serif,fontWeight=FontWeight.Bold,fontSize=16.sp,color=gardenInk,maxLines=1)
                    Text(sub[i],fontFamily=gardenBody,fontSize=10.sp,color=gardenInk.copy(alpha=.75f),maxLines=1)
                }
                Icon(Icons.Outlined.ChevronRight,null,Modifier.size(22.dp),tint=gardenRust)
            }
        }
        return
    }
    if(tablet&&wide) {
        Row(m.testTag("gallery-friday-a-actions"),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            for(i in 0..2) Column(Modifier.weight(1f).fillMaxHeight().testTag("gallery-friday-a-action-$i")
                .clip(RoundedCornerShape(12.dp)).background(gardenPaper)
                .border(.8.dp,gardenLine,RoundedCornerShape(12.dp)).clickable(onClick=clicks[i])) {
                GardenActionScene(i,Modifier.fillMaxWidth().weight(1f))
                Row(Modifier.fillMaxWidth().height(40.dp).padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(gardenGold.copy(alpha=.22f)),contentAlignment=Alignment.Center) {
                        GardenActionIcon(i,20.dp)
                    }
                    Spacer(Modifier.width(7.dp))
                    Column(Modifier.weight(1f)) {
                        Text(labels[i],fontFamily=FontFamily.Serif,fontWeight=FontWeight.Bold,fontSize=14.sp,color=gardenInk,maxLines=1)
                        Text(sub[i],fontFamily=gardenBody,fontSize=9.sp,color=gardenInk,maxLines=1)
                    }
                    Icon(Icons.Outlined.ChevronRight,null,Modifier.size(18.dp),tint=gardenRust)
                }
            }
        }
        return
    }
    Row(m.testTag("gallery-friday-a-actions"),horizontalArrangement=Arrangement.spacedBy(if(tablet)10.dp else 5.dp)) {
        for(i in 0..2) {
            val vertical=!wide&&!tablet || tablet&&wide
            Column(Modifier.weight(1f).fillMaxHeight().testTag("gallery-friday-a-action-$i")
                .clip(RoundedCornerShape(12.dp)).background(gardenPaper.copy(alpha=.98f))
                .border(.8.dp,gardenLine,RoundedCornerShape(12.dp)).clickable(onClick=clicks[i])
                .padding(horizontal=if(tablet)10.dp else 4.dp,vertical=if(vertical)3.dp else 2.dp),
                horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                if(vertical) {
                    Box(Modifier.size(if(tablet)45.dp else 28.dp).clip(CircleShape).background(gardenGold.copy(alpha=.22f)),contentAlignment=Alignment.Center) {
                        GardenActionIcon(i,if(tablet)30.dp else 21.dp)
                    }
                    Text(labels[i],fontFamily=if(L10n.isUrdu(s))gardenArabicBold else FontFamily.Serif,
                        fontWeight=FontWeight.Bold,fontSize=if(tablet)16.sp else 10.sp,color=gardenInk,maxLines=1)
                    Text(sub[i],fontFamily=gardenBody,fontSize=if(tablet)10.sp else 8.sp,
                        color=gardenInk.copy(alpha=.7f),maxLines=if(tablet)1 else 2,textAlign=TextAlign.Center)
                } else Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
                    GardenActionIcon(i,if(wide)19.dp else 25.dp)
                    Spacer(Modifier.width(5.dp))
                    Column {
                        Text(labels[i],fontFamily=if(L10n.isUrdu(s))gardenArabicBold else FontFamily.Serif,
                            fontWeight=FontWeight.Bold,fontSize=if(tablet)14.sp else 10.sp,color=gardenInk,maxLines=1,overflow=TextOverflow.Ellipsis)
                        if(tablet)Text(sub[i],fontFamily=gardenBody,fontSize=8.sp,color=gardenInk.copy(alpha=.7f),maxLines=1)
                    }
                }
            }
        }
    }
}

@Composable private fun GardenActionScene(index:Int,m:Modifier) {
    val art=ImageBitmap.imageResource(R.drawable.prayer_gallery_friday_action_art)
    Canvas(m) {
        val rowHeight=art.height/3
        val ratio=if(size.height>0f)size.width/size.height else 1f
        val sourceWidth=minOf(art.width,(rowHeight*ratio).toInt()).coerceAtLeast(1)
        drawImage(art,srcOffset=androidx.compose.ui.unit.IntOffset((art.width-sourceWidth)/2,index*rowHeight),
            srcSize=androidx.compose.ui.unit.IntSize(sourceWidth,rowHeight),
            dstSize=androidx.compose.ui.unit.IntSize(size.width.toInt(),size.height.toInt()),filterQuality=FilterQuality.High)
    }
}

@Composable private fun GardenActionIcon(index:Int,size:Dp) {
    when(index) {
        0 -> Icon(Icons.Outlined.MenuBook,null,Modifier.size(size),tint=gardenInk)
        1 -> GardenBeads(Modifier.size(size))
        else -> Icon(Icons.Outlined.EventAvailable,null,Modifier.size(size),tint=gardenInk)
    }
}

@Composable private fun GardenBeads(m:Modifier) {
    Canvas(m) {
        val unit=size.minDimension
        val cx=size.width*.54f; val cy=size.height*.42f
        val rx=unit*.22f; val ry=unit*.29f
        for(i in 0 until 18) {
            val t=i*2.0*Math.PI/18.0
            drawCircle(gardenInk,unit*.038f,Offset(cx+rx*kotlin.math.cos(t).toFloat(),cy+ry*kotlin.math.sin(t).toFloat()))
        }
        val knot=Offset(cx,cy+ry)
        val tail=Offset(size.width*.34f,size.height*.95f)
        drawLine(gardenInk,knot,tail,unit*.04f,cap=StrokeCap.Round)
        drawCircle(gardenInk,unit*.05f,tail)
    }
}

@Composable private fun GardenNav(s:AppSettings,a:HomeActions,m:Modifier,wide:Boolean,tablet:Boolean) {
    val labels=listOf(L10n.word(s,"Qibla"),L10n.word(s,"Adhkar"),L10n.word(s,"Learn"),L10n.word(s,"Timetable"))
    val clicks=listOf(a.onOpenQibla,{a.onOpenAdhkar(AdhkarMode.MORNING)},a.onOpenLearn,a.onOpenTimetable)
    Row(m.background(gardenPaper).border(.8.dp,gardenLine),verticalAlignment=Alignment.CenterVertically) {
        for(i in 0..3) {
            if(i>0)Box(Modifier.width(.5.dp).height(if(wide)22.dp else 28.dp).background(gardenLine))
            Column(Modifier.weight(1f).fillMaxHeight().clickable(onClick=clicks[i]),
                horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                when(i) {
                    0 -> Icon(Icons.Outlined.Explore,null,Modifier.size(if(wide&&!tablet)18.dp else 23.dp),tint=gardenInk)
                    1 -> GardenBeads(Modifier.size(if(wide&&!tablet)18.dp else 23.dp))
                    2 -> Icon(Icons.Outlined.MenuBook,null,Modifier.size(if(wide&&!tablet)18.dp else 23.dp),tint=gardenInk)
                    else -> Icon(Icons.Outlined.CalendarMonth,null,Modifier.size(if(wide&&!tablet)18.dp else 23.dp),tint=gardenInk)
                }
                Text(labels[i],fontFamily=if(L10n.isUrdu(s))gardenArabic else gardenBody,
                    fontSize=if(wide&&!tablet)8.sp else 10.sp,color=gardenInk,maxLines=1)
            }
        }
    }
}
