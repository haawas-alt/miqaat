package com.usman.miqaat.ui

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*

/** Owner-approved portrait and landscape boards. Phones and accessible large type retain their routes. */
@Composable internal fun approvedKiswahTablet(s:AppSettings):Boolean {
    val c=LocalConfiguration.current
    return s.theme==AppTheme.KISWAH && minOf(c.screenWidthDp,c.screenHeightDp)>=600
}
private val kiswahNative=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false))
private fun kiswahUi(s:AppSettings)=if(L10n.isUrdu(s))Amiri else FontFamily.Serif

@Composable internal fun ApprovedKiswahTabletHome(state:PrayerState,s:AppSettings,a:HomeActions) {
    val tk=ThemeTokenSets.kiswah.copy(background=Color(0xFF030303),primary=Color(0xFFFFD078),
        outline=Color(0xFFB18C40),contentPrimary=Color(0xFFF5F0E3),contentSecondary=Color(0xFFD8D2C6))
    val hero=heroInfo(state,s,kicker(state,s))
    val links=doors(state,s,a)
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { p ->
        val day=if(p==state.next && state.nextTime.toLocalDate().isAfter(state.today.date))PrayerEngine.times(s,state.nextTime.toLocalDate()) else state.today
        WhyDialog(s,day,p) {why=null}
    }
    BoxWithConstraints(Modifier.fillMaxSize().clipToBounds().background(tk.background).testTag("approved-home")) {
        val wide=maxWidth>maxHeight
        val k=(if(wide)maxWidth.value/1280f else maxWidth.value/1000f).coerceIn(.60f,1.15f)
        val compact=wide && maxHeight<700.dp
        if(s.artTheme!=ArtTheme.MINIMAL) Image(painterResource(if(wide)R.drawable.kiswah_tablet_approved else R.drawable.kiswah_tablet_portrait_approved),
            null,Modifier.fillMaxSize().graphicsLayer(scaleX=if(L10n.isUrdu(s))-1f else 1f).testTag("kiswah-tablet-art"),contentScale=ContentScale.FillBounds)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().displayCutoutPadding()
            .padding(horizontal=(20*k).dp).padding(bottom=(16*k).dp).testTag("kiswah-tablet-approved")) {
            KiswahTabletHeader(state,s,a,tk,k,wide)
            KiswahTabletBanner(s,tk,k,wide)
            Spacer(Modifier.height((14*k).dp))
            val prayers:@Composable (Modifier)->Unit={ m ->
                Column(m.clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha=.88f))
                    .border(.7.dp,tk.outline.copy(alpha=.65f),RoundedCornerShape(12.dp)).testTag("home-prayers")) {
                    listedPrayers(s).forEach { p ->
                        val row=galleryRowInfo(p,state,s).let { r -> r.copy(isNext=p==state.next) }
                        TabletPrayer(row,s,a,tk,k,compact,Modifier.fillMaxWidth().weight(1f),false) {why=p}
                    }
                }
            }
            if(wide) Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy((24*k).dp)) {
                prayers(Modifier.weight(.50f).fillMaxHeight())
                KiswahTabletHero(state,s,tk,hero,k,compact,false,Modifier.weight(.50f).fillMaxHeight().testTag("home-hero")) {why=hero.prayer}
            } else {
                KiswahTabletHero(state,s,tk,hero,k,false,true,Modifier.weight(.40f).fillMaxWidth().testTag("home-hero")) {why=hero.prayer}
                prayers(Modifier.weight(.60f).fillMaxWidth())
            }
            ApprovedTimeline(state,s,tk,Modifier.fillMaxWidth().height(maxOf(48.dp,((if(wide)70 else 72)*k).dp)),landscape=true,mockupScale=if(wide)1.1f*k else 1.15f*k)
            TabletContext(links,s,tk,k)
            TabletDock(state,s,a,tk,k)
        }
        if(s.nightDim && state.period==Prayer.ISHA && !state.justPassed)Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.22f)))
    }
}

@Composable private fun KiswahTabletHeader(state:PrayerState,s:AppSettings,a:HomeActions,tk:ThemeTokens,k:Float,wide:Boolean) {
    val location:@Composable (Modifier)->Unit={ m ->
        Row(m.heightIn(min=48.dp).clip(RoundedCornerShape(16.dp)).clickable(role=Role.Button,onClick=a.onOpenLocation)
            .semantics(mergeDescendants=true) {contentDescription=Str.get(R.string.s_a11y_location,L10n.iso(L10n.place(s.locationName)))},verticalAlignment=Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn,null,Modifier.size((27*k).dp),tint=tk.primary)
            Text(L10n.place(s.locationName),Modifier.padding(start=(10*k).dp),fontFamily=kiswahUi(s),fontSize=(23*k).sp,
                color=tk.contentPrimary,maxLines=1,style=kiswahNative)
        }
    }
    val dates:@Composable (Modifier)->Unit={ m ->
        Row(m,verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
            Text(L10n.date(s,state.now),fontFamily=kiswahUi(s),fontSize=(18*k).sp,color=tk.contentPrimary,maxLines=1,style=kiswahNative)
            if(s.showHijri) {
                Box(Modifier.padding(horizontal=(16*k).dp).width(.6.dp).height((23*k).dp).background(tk.outline))
                Text(galleryHijriLine(s,PrayerEngine.hijri(state.now.toLocalDate(),s.hijriOffsetDays)),Modifier.testTag("approved-hijri"),
                    fontFamily=kiswahUi(s),fontSize=(18*k).sp,color=tk.contentPrimary,maxLines=1,style=kiswahNative)
            }
        }
    }
    Column(Modifier.fillMaxWidth().padding(vertical=(4*k).dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy((12*k).dp)) {
            location(Modifier.weight(if(wide).35f else 1f))
            if(wide)dates(Modifier.weight(.65f))
            Text(L10n.at(state.now,s.use24h),fontFamily=kiswahUi(s),fontSize=(23*k).sp,color=tk.contentPrimary,maxLines=1,style=kiswahNative)
            Box(Modifier.width(.6.dp).height((24*k).dp).background(tk.outline))
            IconButton(a.onOpenSettings,Modifier.size(48.dp).semantics {contentDescription=Str[R.string.s_settings]}) {
                Icon(Icons.Outlined.Settings,null,Modifier.size((29*k).dp),tint=tk.primary)
            }
        }
        if(!wide)dates(Modifier.fillMaxWidth().heightIn(min=(32*k).dp))
    }
}

@Composable private fun KiswahTabletBanner(s:AppSettings,tk:ThemeTokens,k:Float,wide:Boolean) {
    val text=if(wide)Kiswah.BAND else Kiswah.BAND.replaceFirst("   ✦   ","\n")
    Box(Modifier.fillMaxWidth().heightIn(min=((if(wide)76 else 108)*k).dp)
        .background(Color.Black.copy(alpha=.92f)).testTag("kiswah-calligraphy-frame"),contentAlignment=Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val gold=tk.primary.copy(alpha=.80f);val inset=4.dp.toPx()
            drawRect(gold,style=Stroke(.8.dp.toPx()))
            drawRect(gold,topLeft=Offset(inset,inset),size=androidx.compose.ui.geometry.Size(size.width-inset*2,size.height-inset*2),style=Stroke(.45.dp.toPx()))
            // Restrained native gold corner leaves; no raster lettering or heavy silver rails.
            for(x in listOf(14.dp.toPx(),size.width-14.dp.toPx()))for(y in listOf(14.dp.toPx(),size.height-14.dp.toPx())) {
                drawCircle(gold,2.dp.toPx(),Offset(x,y))
                drawOval(gold.copy(alpha=.8f),Offset(x-2.dp.toPx(),y-9.dp.toPx()),androidx.compose.ui.geometry.Size(4.dp.toPx(),7.dp.toPx()),style=Stroke(.7.dp.toPx()))
                drawOval(gold.copy(alpha=.8f),Offset(x-9.dp.toPx(),y-2.dp.toPx()),androidx.compose.ui.geometry.Size(7.dp.toPx(),4.dp.toPx()),style=Stroke(.7.dp.toPx()))
            }
        }
        Text(text,Modifier.fillMaxWidth().padding(horizontal=(40*k).dp,vertical=(10*k).dp).testTag("kiswah-calligraphy-band")
            .semantics {contentDescription=Kiswah.BAND},fontFamily=KiswahLandscapeArabic,fontSize=((if(wide)27 else 27)*k).sp,
            lineHeight=(43*k).sp,color=tk.primary,textAlign=TextAlign.Center,maxLines=if(wide)1 else 2,style=kiswahNative)
    }
}

@Composable private fun KiswahTabletHero(state:PrayerState,s:AppSettings,tk:ThemeTokens,hero:HeroInfo,k:Float,compact:Boolean,portrait:Boolean,modifier:Modifier,onDetails:()->Unit) {
    Column(modifier.semantics(mergeDescendants=true) {contentDescription=hero.spoken;heading()},
        horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Text(hero.special?:hero.kickerLabel,fontFamily=kiswahUi(s),fontSize=((if(compact)14 else 17)*k).sp,lineHeight=(24*k).sp,
            letterSpacing=if(L10n.isUrdu(s))0.sp else 2.sp,color=tk.primary,maxLines=2,textAlign=TextAlign.Center,style=kiswahNative)
        Text(hero.arabic,Modifier.testTag("approved-hero-arabic"),fontFamily=KiswahLandscapeArabic,fontSize=((if(compact)52 else 72)*k).sp,
            lineHeight=((if(compact)65 else 92)*k).sp,fontWeight=FontWeight.Bold,color=tk.primary,style=kiswahNative)
        if(!L10n.isUrdu(s))Text(hero.label.uppercase(),fontFamily=FontFamily.Serif,fontSize=((if(compact)19 else 23)*k).sp,
            lineHeight=(30*k).sp,letterSpacing=2.sp,color=tk.contentPrimary,maxLines=1,style=kiswahNative)
        TabletHeroClock(hero,s,tk,k,if(compact)90f else if(portrait)128f else 116f,"approved-hero-clock")
        Row(Modifier.fillMaxWidth().padding(horizontal=(12*k).dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
            Box(Modifier.size((8*k).dp).background(if(state.justPassed)Color(0xFF64DAC5)else tk.primary,RoundedCornerShape(10.dp)))
            val status=if(state.justPassed) L10n.t("Azaan was ","اذان: ")+hero.status else hero.status
            Text(status,Modifier.padding(start=(9*k).dp),fontFamily=kiswahUi(s),fontSize=((if(compact)16 else 19)*k).sp,
                lineHeight=(28*k).sp,color=tk.contentPrimary,maxLines=2,textAlign=TextAlign.Center,style=kiswahNative)
        }
        Spacer(Modifier.height((16*k).dp))
        TabletAction(Str[R.string.s_view_prayer_details],s,tk,k,Modifier.widthIn(min=(260*k).dp),false,onDetails)
        ApprovedSeasonal(state,s,tk)
    }
}
