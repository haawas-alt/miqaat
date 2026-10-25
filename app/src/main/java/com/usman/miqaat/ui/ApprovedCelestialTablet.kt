package com.usman.miqaat.ui

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import kotlin.math.ceil
import kotlin.math.roundToInt
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*

/** Selected constellation rail, tablet only; large accessibility text retains its existing route. */
@Composable internal fun approvedCelestialTablet(s:AppSettings):Boolean {
    val c=LocalConfiguration.current
    return s.theme==AppTheme.CELESTIAL_MERIDIAN && minOf(c.screenWidthDp,c.screenHeightDp)>=600
}
private val celestialNative=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false))
private fun celestialUi(s:AppSettings)=if(L10n.isUrdu(s))Amiri else FontFamily.Serif

@Composable internal fun ApprovedCelestialTabletHome(state:PrayerState,s:AppSettings,a:HomeActions) {
    val tk=ThemeTokenSets.of(s.theme).copy(background=Color(0xFF00162B),primary=Color(0xFFFFD078),
        outline=Color(0xFF6E9DB7),contentPrimary=Color(0xFFF8F1E4),contentSecondary=Color(0xFFCDDBE9))
    val hero=heroInfo(state,s,kicker(state,s))
    var why by remember {mutableStateOf<Prayer?>(null)}
    why?.let {p ->
        val day=if(p==state.next && state.nextTime.toLocalDate().isAfter(state.today.date))PrayerEngine.times(s,state.nextTime.toLocalDate()) else state.today
        WhyDialog(s,day,p){why=null}
    }
    BoxWithConstraints(Modifier.fillMaxSize().clipToBounds().background(tk.background).testTag("approved-home")) {
        val wide=maxWidth>maxHeight
        val k=(if(wide)maxWidth.value/1280f else maxWidth.value/1000f).coerceIn(.60f,1.15f)
        val compact=wide && maxHeight<700.dp
        if(s.artTheme!=ArtTheme.MINIMAL)Image(painterResource(if(wide)R.drawable.celestial_tablet_constellation else R.drawable.celestial_tablet_constellation_portrait),null,
            Modifier.fillMaxSize().graphicsLayer(scaleX=if(L10n.isUrdu(s))-1f else 1f).testTag("celestial-tablet-art"),contentScale=ContentScale.FillBounds)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().displayCutoutPadding()
            .padding(horizontal=(28*k).dp).padding(bottom=(24*k).dp).testTag("celestial-tablet-approved")) {
            CelestialTabletHeader(state,s,a,tk,k,wide)
            Spacer(Modifier.height((12*k).dp))
            val prayers:@Composable (Modifier)->Unit={m ->
                Column(m.background(if(wide)Brush.horizontalGradient(listOf(tk.background.copy(alpha=.56f),tk.background.copy(alpha=.24f),Color.Transparent))else Brush.verticalGradient(0f to Color.Transparent,.12f to tk.background.copy(alpha=.4f),.9f to tk.background.copy(alpha=.4f),1f to Color.Transparent)).drawBehind {
                    val x=if(L10n.isUrdu(s))size.width-(32*k).dp.toPx()else (32*k).dp.toPx()
                    val edge=size.height/listedPrayers(s).size/2
                    drawLine(tk.primary.copy(alpha=.8f),Offset(x,edge),Offset(x,size.height-edge),1.dp.toPx())
                }) {
                    listedPrayers(s).forEachIndexed {i,p ->
                        val r=galleryRowInfo(p,state,s).let {if(p==Prayer.SUNRISE)it.copy(done=!state.today[p].isAfter(state.now))else it}
                        CelestialRailRow(r,s,a,tk,k,compact,i==0,i==listedPrayers(s).lastIndex,
                            r.isNow || (r.isNext && listedPrayers(s).none {galleryRowInfo(it,state,s).isNow}),
                            Modifier.fillMaxWidth().weight(1f)) {why=p}
                    }
                }
            }
            if(wide)Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy((30*k).dp)) {
                Box(Modifier.weight(.51f).fillMaxHeight().testTag("home-prayers")){prayers(Modifier.fillMaxSize())}
                CelestialHero(state,s,tk,hero,k,compact,Modifier.weight(.49f).fillMaxHeight().testTag("home-hero")){why=hero.prayer}
            } else {
                CelestialHero(state,s,tk,hero,k,false,Modifier.weight(.40f).fillMaxWidth().testTag("home-hero")){why=hero.prayer}
                Box(Modifier.weight(.60f).fillMaxWidth().testTag("home-prayers")){prayers(Modifier.fillMaxSize())}
            }
            ApprovedTimeline(state,s,tk,Modifier.fillMaxWidth().height(maxOf(48.dp,((if(wide)80 else 96)*k).dp)),landscape=true,mockupScale=1.1f*k)
            TabletContext(doors(state,s,a),s,tk,k)
            TabletDock(state,s,a,tk,k)
        }
        if(s.nightDim && state.period==Prayer.ISHA && !state.justPassed)Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.22f)))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable private fun CelestialRailRow(r:RowInfo,s:AppSettings,a:HomeActions,tk:ThemeTokens,k:Float,compact:Boolean,first:Boolean,last:Boolean,selected:Boolean,modifier:Modifier,onWhy:()->Unit) {
    val shape=RoundedCornerShape(50.dp)
    val rtl=L10n.isUrdu(s)
    Row(modifier.then(if(selected)Modifier.clip(shape).background(Brush.horizontalGradient(listOf(Color(0xFF664B20),Color(0xFF32291F)))).border(.8.dp,tk.primary,shape)else Modifier)
        .combinedClickable(onClick=a.onToggleRelative,onLongClick=onWhy,role=Role.Button).semantics(mergeDescendants=true){contentDescription=r.spoken}
        .testTag("approved-row-${r.prayer.name}"),verticalAlignment=Alignment.CenterVertically) {
        Canvas(Modifier.width((64*k).dp).fillMaxHeight().testTag("constellation-node-${r.prayer.name}")) {
            val cx=size.width/2;val cy=size.height/2;val rad=(if(selected)7 else 6)*k*1.dp.toPx()
            drawLine(tk.primary.copy(alpha=.8f),Offset(cx,if(first)cy else 0f),Offset(cx,if(last)cy else size.height),1.dp.toPx())
            if(selected)drawCircle(tk.primary.copy(alpha=.14f),rad*2.8f,Offset(cx,cy))
            val d=Path().apply {moveTo(cx,cy-rad);lineTo(cx+rad,cy);lineTo(cx,cy+rad);lineTo(cx-rad,cy);close()}
            drawPath(d,tk.background)
            if(selected || r.done)drawPath(d,tk.primary)
            drawPath(d,if(selected || r.done)tk.primary else tk.contentPrimary,style=Stroke(1.2.dp.toPx()))
            if(selected)drawCircle(tk.primary,rad*1.7f,Offset(cx,cy),style=Stroke(.7.dp.toPx()))
        }
        Column(Modifier.weight(.32f).padding(start=(10*k).dp)) {
            Text(r.label,Modifier.testTag("gallery-fit-label-${r.prayer.name}"),fontFamily=celestialUi(s),fontSize=((if(compact)23 else 26)*k).sp,
                lineHeight=((if(compact)29 else 32)*k).sp,color=tk.contentPrimary,maxLines=if(rtl)2 else 1,style=celestialNative)
            if(!rtl)Text(r.arabic,Modifier.testTag("gallery-fit-arabic-${r.prayer.name}"),fontFamily=Amiri,fontWeight=FontWeight.Bold,
                fontSize=((if(compact)22 else 25)*k).sp,lineHeight=((if(compact)29 else 33)*k).sp,color=tk.primary,maxLines=1,style=celestialNative)
        }
        Box(Modifier.padding(horizontal=(12*k).dp).width(.5.dp).height((44*k).dp).background(tk.outline.copy(alpha=.65f)))
        Column(Modifier.weight(.53f)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(if(s.showRelative)r.relative else r.clock,Modifier.testTag("gallery-fit-clock-${r.prayer.name}"),fontFamily=if(s.showRelative)ApprovedBody else FontFamily.Serif,
                    fontSize=((if(s.showRelative)18 else if(compact)30 else 36)*k).sp,lineHeight=((if(s.showRelative)24 else if(compact)36 else 43)*k).sp,
                    color=tk.contentPrimary,maxLines=if(s.showRelative)2 else 1,style=celestialNative)
                if(!s.showRelative && r.suffix.isNotEmpty())Text(" ${r.suffix}",fontFamily=celestialUi(s),fontSize=(16*k).sp,color=tk.contentPrimary,maxLines=1,style=celestialNative)
            }
            if(r.small.isNotEmpty())Text(r.small,Modifier.testTag("gallery-fit-detail-${r.prayer.name}"),fontFamily=if(rtl)Amiri else ApprovedBody,
                fontSize=((if(compact)14 else 16)*k).sp,lineHeight=((if(rtl)26 else if(compact)19 else 22)*k).sp,color=tk.contentSecondary,maxLines=2,style=celestialNative)
        }
        Box(Modifier.width((56*k).dp),contentAlignment=Alignment.Center) {
            if(r.isNow || r.isNext)Text(L10n.word(s,if(r.isNow)"NOW" else "NEXT"),Modifier.testTag(if(r.isNow)"home-status-now" else "home-status-next")
                .clip(RoundedCornerShape(20.dp)).background(if(selected)tk.primary else tk.background.copy(alpha=.7f)).border(.7.dp,tk.primary,RoundedCornerShape(20.dp))
                .padding(horizontal=(8*k).dp,vertical=(4*k).dp),fontFamily=if(rtl)Amiri else ApprovedBody,fontSize=(10*k).sp,lineHeight=(14*k).sp,
                color=if(selected)tk.background else tk.primary,maxLines=1,style=celestialNative)
        }
        Box(Modifier.width((34*k).dp).heightIn(min=48.dp),contentAlignment=Alignment.Center) {
            if(r.prayer.isPrayer)Icon(if(r.azaanOn)Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff,null,Modifier.size((25*k).dp),tint=tk.primary)
        }
        IconButton(onWhy,Modifier.size(48.dp).testTag("approved-info-${r.prayer.name}").semantics{contentDescription=Str[R.string.s_why_this_time]}) {
            Icon(Icons.Outlined.Info,null,Modifier.size((27*k).dp),tint=tk.contentPrimary)
        }
    }
}

@Composable private fun CelestialHero(state:PrayerState,s:AppSettings,tk:ThemeTokens,hero:HeroInfo,k:Float,compact:Boolean,modifier:Modifier,onDetails:()->Unit) {
    Column(modifier.semantics(mergeDescendants=true){contentDescription=hero.spoken;heading()},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Text(hero.special?:hero.kickerLabel,fontFamily=celestialUi(s),fontSize=(17*k).sp,lineHeight=(24*k).sp,letterSpacing=if(L10n.isUrdu(s))0.sp else 2.sp,
            color=tk.primary,maxLines=2,textAlign=TextAlign.Center,style=celestialNative)
        Text(hero.arabic,Modifier.testTag("approved-hero-arabic"),fontFamily=Amiri,fontWeight=FontWeight.Bold,fontSize=((if(compact)54 else 64)*k).sp,
            lineHeight=((if(compact)72 else 88)*k).sp,color=tk.primary,style=celestialNative)
        if(!L10n.isUrdu(s))Text(hero.label.uppercase(),fontFamily=FontFamily.Serif,fontSize=(25*k).sp,lineHeight=(33*k).sp,letterSpacing=2.sp,color=tk.contentPrimary,style=celestialNative)
        Row(verticalAlignment=Alignment.CenterVertically) {
            Text(hero.clock,Modifier.testTag("approved-hero-clock"),fontFamily=FontFamily.Serif,fontSize=((if(compact)78 else 88)*k).sp,
                lineHeight=((if(compact)94 else 114)*k).sp,color=tk.contentPrimary,maxLines=1,style=celestialNative)
            if(hero.suffix.isNotEmpty())Text(" ${hero.suffix}",fontFamily=celestialUi(s),fontSize=(26*k).sp,color=tk.contentPrimary,maxLines=1,style=celestialNative)
        }
        Row(Modifier.clip(RoundedCornerShape(28.dp)).background(tk.background.copy(alpha=.88f)).border(.7.dp,tk.primary.copy(alpha=.7f),RoundedCornerShape(28.dp))
            .padding(horizontal=(18*k).dp,vertical=(7*k).dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy((8*k).dp)) {
            Icon(Icons.Outlined.Schedule,null,Modifier.size((20*k).dp),tint=tk.primary)
            Text(hero.status,Modifier.testTag("celestial-tablet-status"),fontFamily=celestialUi(s),fontSize=(20*k).sp,lineHeight=(27*k).sp,color=tk.contentPrimary,maxLines=2,textAlign=TextAlign.Center,style=celestialNative)
        }
        Spacer(Modifier.height((22*k).dp))
        TabletAction(Str[R.string.s_view_prayer_details],s,tk,k,Modifier.widthIn(min=(270*k).dp),false,onDetails)
        ApprovedSeasonal(state,s,tk)
    }
}

@Composable private fun CelestialTabletHeader(state:PrayerState,s:AppSettings,a:HomeActions,tk:ThemeTokens,k:Float,wide:Boolean) {
    val location:@Composable (Modifier)->Unit={ m ->
        Row(m.heightIn(min=48.dp).clip(RoundedCornerShape(16.dp)).clickable(role=Role.Button,onClick=a.onOpenLocation)
            .semantics(mergeDescendants=true) {contentDescription=Str.get(R.string.s_a11y_location,L10n.iso(L10n.place(s.locationName)))},verticalAlignment=Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn,null,Modifier.size((27*k).dp),tint=tk.primary)
            Text(L10n.place(s.locationName),Modifier.padding(start=(10*k).dp),fontFamily=celestialUi(s),fontSize=(23*k).sp,
                color=tk.contentPrimary,maxLines=1,style=celestialNative)
        }
    }
    val dates:@Composable (Modifier)->Unit={ m ->
        Row(m,verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
            Text(L10n.date(s,state.now),fontFamily=celestialUi(s),fontSize=(16*k).sp,color=tk.contentPrimary,maxLines=1,style=celestialNative)
            if(s.showHijri) {
                Box(Modifier.padding(horizontal=(16*k).dp).width(.6.dp).height((23*k).dp).background(tk.outline))
                Text(galleryHijriLine(s,PrayerEngine.hijri(state.now.toLocalDate(),s.hijriOffsetDays)),Modifier.testTag("approved-hijri"),
                    fontFamily=celestialUi(s),fontSize=(16*k).sp,color=tk.contentPrimary,maxLines=1,style=celestialNative)
            }
        }
    }
    Column(Modifier.fillMaxWidth().padding(vertical=(4*k).dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy((12*k).dp)) {
            location(Modifier.weight(if(wide).35f else 1f))
            if(wide)dates(Modifier.weight(.65f))
            Text(L10n.at(state.now,s.use24h),Modifier.clip(RoundedCornerShape(28.dp)).background(tk.background.copy(alpha=.8f)).border(.7.dp,tk.primary,RoundedCornerShape(28.dp)).padding(horizontal=(18*k).dp,vertical=(6*k).dp),fontFamily=celestialUi(s),fontSize=(23*k).sp,color=tk.contentPrimary,maxLines=1,style=celestialNative)
            Box(Modifier.width(.6.dp).height((24*k).dp).background(tk.outline))
            IconButton(a.onOpenSettings,Modifier.size(48.dp).semantics {contentDescription=Str[R.string.s_settings]}) {
                Icon(Icons.Outlined.Settings,null,Modifier.size((29*k).dp),tint=tk.primary)
            }
            if(wide && k>=.9f)Text(Str[R.string.s_settings],Modifier.clickable(onClick=a.onOpenSettings),fontFamily=celestialUi(s),fontSize=(17*k).sp,color=tk.contentPrimary,maxLines=1,style=celestialNative)
        }
        if(!wide)dates(Modifier.fillMaxWidth().heightIn(min=(32*k).dp))
    }
}

