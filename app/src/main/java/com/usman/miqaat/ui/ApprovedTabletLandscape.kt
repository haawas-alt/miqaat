package com.usman.miqaat.ui

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*

/** Only the two approved tablet landscape boards opt in. Every other home keeps its renderer. */
@Composable
internal fun approvedTabletLandscape(s: AppSettings): Boolean {
    val c=LocalConfiguration.current
    return c.orientation==Configuration.ORIENTATION_LANDSCAPE && minOf(c.screenWidthDp,c.screenHeightDp)>=600 &&
        s.theme in listOf(AppTheme.MIQAAT,AppTheme.PRAYER_GALLERY)
}

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private val tabletMiqaatDisplay=FontFamily(Font(R.font.cormorant_garamond_medium,FontWeight.Medium,
    variationSettings=FontVariation.Settings(FontVariation.weight(600))))
private fun tabletDisplay(tk:ThemeTokens)=if(tk.dark)tabletMiqaatDisplay else ApprovedDisplay

private val tabletNativeText=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false))
private val tabletClockText=tabletNativeText.copy(fontFeatureSettings="'lnum' 1, 'tnum' 1")
private fun tabletUi(s:AppSettings)=if(L10n.isUrdu(s)) Amiri else if(s.theme==AppTheme.PRAYER_GALLERY)FontFamily.SansSerif else ApprovedBody

@Composable
internal fun ApprovedTabletLandscapeHome(state:PrayerState,s:AppSettings,a:HomeActions) {
    val tk=ThemeTokenSets.of(s.theme).let { if(s.theme==AppTheme.MIQAAT)it.copy(primary=Color(0xFFFFD078)) else it }
    val gallery=s.theme==AppTheme.PRAYER_GALLERY
    val hero=heroInfo(state,s,kicker(state,s))
    val links=doors(state,s,a)
    var why by remember { mutableStateOf<Prayer?>(null) }
    why?.let { p ->
        val day=if(p==state.next && state.nextTime.toLocalDate().isAfter(state.today.date))
            PrayerEngine.times(s,state.nextTime.toLocalDate()) else state.today
        WhyDialog(s,day,p) { why=null }
    }
    BoxWithConstraints(Modifier.fillMaxSize().clipToBounds().background(tk.background).testTag("approved-home")) {
        val k=(maxWidth.value/1280f).coerceIn(.72f,1.35f)
        val compact=maxHeight<700.dp
        if(s.artTheme!=ArtTheme.MINIMAL) Image(painterResource(if(gallery) R.drawable.tablet_gallery_paper_181 else R.drawable.tablet_miqaat_181),
            null,Modifier.fillMaxSize().graphicsLayer(scaleX=if(!gallery && L10n.isUrdu(s))-1f else 1f).testTag("tablet-approved-art"),contentScale=ContentScale.FillBounds)
        if(gallery) Box(Modifier.fillMaxSize().background(Color(0xFFFFFAED).copy(alpha=.18f)))
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().displayCutoutPadding()
            .padding(horizontal=(24*k).dp).padding(bottom=((if(gallery)12 else 16)*k).dp).testTag("tablet-approved-${s.theme.name}")) {
            TabletHeader(state,s,a,tk,k)
            if(gallery) {
                val seasonal=fastProgress(state,s)!=null || tarawihLine(state,s)!=null
                Column(Modifier.weight(1f).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy((9*k).dp)) {
                    TabletGalleryHero(state,s,a,tk,hero,k,Modifier.weight(if(seasonal) .44f else .375f).fillMaxWidth()
                        .testTag("home-hero")) { why=hero.prayer }
                    Column(Modifier.weight(if(seasonal) .56f else .625f).fillMaxWidth().testTag("home-prayers"),
                        verticalArrangement=Arrangement.spacedBy((9*k).dp)) {
                        galleryGridRows(listedPrayers(s)).forEach { row ->
                            Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy((12*k).dp)) {
                                row.forEach { p -> TabletPrayer(galleryRowInfo(p,state,s),s,a,tk,k,compact,
                                    Modifier.weight(1f).fillMaxHeight(),true) { why=p } }
                                repeat(3-row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            } else {
                Row(Modifier.weight(1f).fillMaxWidth().padding(top=(8*k).dp).testTag("home-prayers"),
                    horizontalArrangement=Arrangement.spacedBy((12*k).dp)) {
                    Column(Modifier.weight(.51f).fillMaxHeight().clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF001629).copy(alpha=.94f)).border(.8.dp,tk.outline.copy(alpha=.8f),RoundedCornerShape(12.dp))) {
                        listedPrayers(s).forEach { p -> TabletPrayer(galleryRowInfo(p,state,s),s,a,tk,k,compact,
                            Modifier.weight(1f).fillMaxWidth(),false) { why=p } }
                    }
                    TabletMiqaatHero(state,s,tk,hero,k,Modifier.weight(.49f).fillMaxHeight().testTag("home-hero")) { why=hero.prayer }
                }
            }
            ApprovedTimeline(state,s,tk,Modifier.fillMaxWidth().height(maxOf(48.dp,(gallery.let { if(it)60 else 80 }*k).dp))
                .padding(horizontal=if(gallery)0.dp else (40*k).dp),landscape=true,mockupScale=if(gallery)1f else 1.3f*k)
            TabletContext(links,s,tk,k)
            TabletDock(state,s,a,tk,k)
        }
        if(s.nightDim && state.period==Prayer.ISHA && !state.justPassed)
            Box(Modifier.fillMaxSize().background(Color(0xFF100A04).copy(alpha=.22f)))
    }
}

@Composable
private fun TabletHeader(state:PrayerState,s:AppSettings,a:HomeActions,tk:ThemeTokens,k:Float) {
    val gallery=s.theme==AppTheme.PRAYER_GALLERY
    val ui=tabletUi(s)
    Row(Modifier.fillMaxWidth().height(maxOf(48.dp,((if(gallery)64 else 72)*k).dp)),verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy((12*k).dp)) {
        Row(Modifier.weight(.24f).heightIn(min=48.dp).clip(RoundedCornerShape(20.dp))
            .clickable(role=Role.Button,onClick=a.onOpenLocation)
            .semantics(mergeDescendants=true) { contentDescription=Str.get(R.string.s_a11y_location,L10n.iso(L10n.place(s.locationName))) },
            verticalAlignment=Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn,null,Modifier.size((28*k).dp),tint=if(gallery)tk.accent else tk.primary)
            Text(L10n.place(s.locationName),Modifier.padding(start=(10*k).dp),fontFamily=if(L10n.isUrdu(s))Amiri else if(gallery)tabletUi(s) else tabletDisplay(tk),
                fontSize=((if(gallery)21 else 28)*k).sp,color=tk.contentPrimary,maxLines=1,style=tabletNativeText)
        }
        Row(Modifier.weight(.58f),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
            Text(L10n.date(s,state.now),fontFamily=if(gallery)ui else if(L10n.isUrdu(s))Amiri else tabletDisplay(tk),
                fontSize=((if(gallery)16 else 20)*k).sp,color=tk.contentPrimary,maxLines=1,style=tabletNativeText)
            if(s.showHijri) {
                Box(Modifier.padding(horizontal=(18*k).dp).width(.6.dp).height((24*k).dp).background(tk.primary.copy(alpha=.6f)))
                Text(galleryHijriLine(s,PrayerEngine.hijri(state.now.toLocalDate(),s.hijriOffsetDays)),
                    Modifier.testTag("approved-hijri"),fontFamily=if(gallery)ui else if(L10n.isUrdu(s))Amiri else tabletDisplay(tk),
                    fontSize=((if(gallery)16 else 20)*k).sp,color=tk.contentPrimary,maxLines=1,style=tabletNativeText)
            }
        }
        Text(L10n.at(state.now,s.use24h),Modifier.testTag("tablet-live-clock")
            .then(if(gallery)Modifier else Modifier.clip(RoundedCornerShape(24.dp)).border(.8.dp,tk.outline,RoundedCornerShape(24.dp)))
            .padding(horizontal=(20*k).dp,vertical=(7*k).dp),fontFamily=if(L10n.isUrdu(s))Amiri else tabletDisplay(tk),
            fontSize=(23*k).sp,color=tk.contentPrimary,maxLines=1,style=tabletClockText)
        Box(Modifier.width(.6.dp).height((26*k).dp).background(tk.primary.copy(alpha=.6f)))
        IconButton(a.onOpenSettings,Modifier.size(48.dp).semantics { contentDescription=Str[R.string.s_settings] }) {
            Icon(Icons.Outlined.Settings,null,Modifier.size((32*k).dp),tint=tk.contentPrimary)
        }
    }
}

@Composable
private fun TabletMiqaatHero(state:PrayerState,s:AppSettings,tk:ThemeTokens,hero:HeroInfo,k:Float,modifier:Modifier,onDetails:()->Unit) {
    Column(modifier.padding(bottom=(56*k).dp).semantics(mergeDescendants=true) { contentDescription=hero.spoken;heading() },
        horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Text(hero.special?:hero.kickerLabel,fontFamily=tabletUi(s),fontSize=(17*k).sp,lineHeight=(22*k).sp,
            letterSpacing=if(L10n.isUrdu(s))0.sp else 2.sp,color=tk.primary,textAlign=TextAlign.Center,maxLines=2,style=tabletNativeText)
        Text(hero.arabic,Modifier.testTag("approved-hero-arabic"),fontFamily=Amiri,fontSize=(64*k).sp,lineHeight=(84*k).sp,
            color=tk.primary,fontWeight=FontWeight.Bold,style=tabletNativeText)
        if(!L10n.isUrdu(s)) Text(hero.label.uppercase(),fontFamily=tabletDisplay(tk),fontSize=(28*k).sp,lineHeight=(34*k).sp,
            letterSpacing=2.sp,color=tk.contentPrimary,maxLines=1,style=tabletNativeText)
        Spacer(Modifier.height((6*k).dp))
        TabletHeroClock(hero,s,tk,k,82f,"approved-hero-clock")
        Spacer(Modifier.height((4*k).dp))
        Row(Modifier.clip(RoundedCornerShape(20.dp)).background(tk.background.copy(alpha=.92f))
            .border(.7.dp,tk.outline.copy(alpha=.7f),RoundedCornerShape(20.dp)).padding(horizontal=(16*k).dp,vertical=(4*k).dp),
            verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy((7*k).dp)) {
            Icon(Icons.Outlined.Schedule,null,Modifier.size((20*k).dp),tint=tk.primary)
            Text(hero.status,fontFamily=tabletDisplay(tk),fontSize=(20*k).sp,lineHeight=(24*k).sp,color=tk.contentPrimary,
                maxLines=2,textAlign=TextAlign.Center,style=tabletNativeText)
        }
        Spacer(Modifier.height((16*k).dp))
        TabletAction(Str[R.string.s_view_prayer_details],s,tk,k,Modifier.widthIn(min=(248*k).dp),false,onDetails)
        ApprovedSeasonal(state,s,tk)
    }
}

@Composable
private fun TabletGalleryHero(state:PrayerState,s:AppSettings,a:HomeActions,tk:ThemeTokens,hero:HeroInfo,k:Float,modifier:Modifier,onDetails:()->Unit) {
    Row(modifier.testTag("gallery-mobile-hero").semantics(mergeDescendants=true) { contentDescription=hero.spoken;heading() },
        horizontalArrangement=Arrangement.spacedBy((20*k).dp),verticalAlignment=Alignment.CenterVertically) {
        if(s.artTheme!=ArtTheme.MINIMAL) TabletGalleryArt(null,Modifier.weight(.55f).fillMaxHeight().clip(RoundedCornerShape(10.dp)))
        Column(Modifier.weight(.45f).fillMaxHeight().padding(vertical=(9*k).dp),verticalArrangement=Arrangement.Center) {
            Row(Modifier.weight(1f).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(.39f),horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(hero.special?:hero.kickerLabel,fontFamily=if(L10n.isUrdu(s))Amiri else tabletDisplay(tk),
                        fontSize=(16*k).sp,lineHeight=(22*k).sp,letterSpacing=if(L10n.isUrdu(s))0.sp else 1.5.sp,
                        color=tk.accent,maxLines=2,textAlign=TextAlign.Center,style=tabletNativeText)
                    Text(if(L10n.isUrdu(s))hero.label else hero.arabic,Modifier.testTag("gallery-mobile-prayer-name"),
                        fontFamily=Amiri,fontSize=(52*k).sp,lineHeight=(70*k).sp,color=tk.accent,maxLines=1,style=tabletNativeText)
                    if(!L10n.isUrdu(s)) Text(hero.label.uppercase(),fontFamily=tabletDisplay(tk),fontSize=(17*k).sp,lineHeight=(24*k).sp,
                        letterSpacing=2.sp,color=tk.accent,maxLines=1,style=tabletNativeText)
                }
                Box(Modifier.padding(horizontal=(10*k).dp).width(.8.dp).height((64*k).dp).background(tk.accent.copy(alpha=.6f)))
                Column(Modifier.weight(.61f),horizontalAlignment=Alignment.CenterHorizontally) {
                    TabletHeroClock(hero,s,tk,k,76f,"gallery-mobile-prayer-time")
                    Text(hero.status,fontFamily=tabletUi(s),fontSize=(17*k).sp,lineHeight=(24*k).sp,
                        color=tk.contentPrimary,maxLines=2,textAlign=TextAlign.Center,style=tabletNativeText)
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy((10*k).dp)) {
                TabletAction(Str[R.string.s_view_prayer_details],s,tk,k,Modifier.weight(1.25f),true,onDetails)
                if(s.kidsMode) TabletAction(if(L10n.isUrdu(s))"نماز کی تیاری" else "Prepare for ${hero.label}",s,tk,k,
                    Modifier.weight(1f),false) { a.onPreparePrayer(hero.prayer) }
            }
            ApprovedSeasonal(state,s,tk)
        }
    }
}

@Composable
private fun TabletHeroClock(hero:HeroInfo,s:AppSettings,tk:ThemeTokens,k:Float,sp:Float,tag:String) {
    Row(verticalAlignment=Alignment.CenterVertically) {
        Text(hero.clock,Modifier.testTag(tag),fontFamily=tabletDisplay(tk),fontWeight=FontWeight.Medium,fontSize=(sp*k).sp,
            lineHeight=((sp+10)*k).sp,color=if(tk.dark)tk.contentPrimary else tk.accent,maxLines=1,style=tabletClockText)
        if(hero.suffix.isNotEmpty()) Text(" ${hero.suffix}",fontFamily=if(L10n.isUrdu(s))Amiri else tabletDisplay(tk),
            fontSize=(22*k).sp,color=tk.contentPrimary,maxLines=1,style=tabletNativeText)
    }
}

@Composable
private fun TabletAction(label:String,s:AppSettings,tk:ThemeTokens,k:Float,modifier:Modifier,filled:Boolean,onClick:()->Unit) {
    Row(modifier.heightIn(min=48.dp).clip(RoundedCornerShape(28.dp))
        .background(if(filled)tk.accent else if(tk.dark)tk.background.copy(alpha=.96f) else tk.background.copy(alpha=.4f))
        .border(.8.dp,if(tk.dark)tk.primary else tk.accent,RoundedCornerShape(28.dp))
        .clickable(role=Role.Button,onClick=onClick).padding(horizontal=(14*k).dp,vertical=(4*k).dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
        Text(label,fontFamily=if(tk.dark && !L10n.isUrdu(s))tabletDisplay(tk) else tabletUi(s),fontSize=((if(tk.dark)20 else 16)*k).sp,lineHeight=((if(tk.dark)26 else 22)*k).sp,
            color=if(filled)tk.background else tk.contentPrimary,maxLines=2,style=tabletNativeText)
        if(filled || tk.dark) Icon(Icons.AutoMirrored.Outlined.ArrowForward,null,Modifier.padding(start=(8*k).dp).size((21*k).dp),
            tint=if(filled)tk.background else if(tk.dark)tk.primary else tk.contentPrimary)
    }
}

/** Artwork-only atlas; labels, times, badges and controls are independent native elements. */
@Composable
private fun TabletGalleryArt(index:Int?,modifier:Modifier) {
    val bmp=ImageBitmap.imageResource(R.drawable.tablet_gallery_atlas_181)
    Canvas(modifier.clearAndSetSemantics { }) {
        val sw=if(index==null)bmp.width else bmp.width/3
        val x=if(index==null)0 else (index%3)*sw
        val y=if(index==null)0 else if(index<3)(bmp.height*.456f).toInt() else (bmp.height*.712f).toInt()
        val sh=if(index==null)(bmp.height*.456f).toInt() else if(index<3)(bmp.height*.256f).toInt() else bmp.height-y
        val cw=minOf(sw,(sh*size.width/size.height).toInt()).coerceAtLeast(1)
        val ch=minOf(sh,(sw*size.height/size.width).toInt()).coerceAtLeast(1)
        drawImage(bmp,srcOffset=IntOffset(x+(sw-cw)/2,y+(sh-ch)/2),srcSize=IntSize(cw,ch),dstSize=IntSize(size.width.toInt(),size.height.toInt()))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabletPrayer(r:RowInfo,s:AppSettings,a:HomeActions,tk:ThemeTokens,k:Float,compact:Boolean,modifier:Modifier,gallery:Boolean,onWhy:()->Unit) {
    val selected=r.isNow||r.isNext
    val shape=RoundedCornerShape(if(gallery)10.dp else 11.dp)
    val marker=if(r.isNow)Color(0xFF0A8279) else if(gallery)Color(0xFFFFCF70) else tk.primary
    val accent=if(gallery)tk.accent else tk.primary
    val selectedColors=if(!gallery && !r.isNow)listOf(Color(0xFF765525),Color(0xFF251E19)) else listOf(
        (if(r.isNow)marker else accent).copy(alpha=if(gallery).12f else .24f),
        (if(r.isNow)marker else accent).copy(alpha=if(gallery).05f else .12f))
    val width=LocalConfiguration.current.screenWidthDp
    val compactBadge=gallery && (width<1100 || L10n.isUrdu(s))
    Column(modifier.clip(shape)
        .background(if(gallery)tk.background.copy(alpha=.88f) else Color.Transparent)
        .then(if(selected)Modifier.background(Brush.horizontalGradient(selectedColors)) else Modifier)
        .border(if(selected).9.dp else .6.dp,if(selected)accent.copy(alpha=.75f) else if(gallery)tk.outline.copy(alpha=.45f) else Color.Transparent,shape)
        .combinedClickable(onClick=a.onToggleRelative,onLongClick=onWhy,role=Role.Button)
        .semantics(mergeDescendants=true) { contentDescription=r.spoken }.testTag("approved-row-${r.prayer.name}")) {
        if(gallery && s.artTheme!=ArtTheme.MINIMAL) TabletGalleryArt(r.prayer.ordinal,Modifier.fillMaxWidth().weight(1f))
        Row(Modifier.fillMaxWidth().then(if(gallery && s.artTheme!=ArtTheme.MINIMAL)
            Modifier.height(if(s.iqamahEnabled) (if(compact)84.dp else 94.dp) else (if(compact && L10n.isUrdu(s))80.dp else if(compact)68.dp else 84.dp)) else Modifier.weight(1f))
            .then(if(!gallery && !selected && r.prayer!=Prayer.FAJR)Modifier.border(.4.dp,tk.divider) else Modifier)
            .padding(horizontal=((if(gallery)12 else 28)*k).dp),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.fillMaxWidth(if(gallery).31f else .28f)) {
                Text(r.label,Modifier.testTag("gallery-fit-label-${r.prayer.name}"),
                    fontFamily=if(L10n.isUrdu(s))Amiri else tabletDisplay(tk),fontWeight=FontWeight.Medium,
                    fontSize=((if(gallery && compact && L10n.isUrdu(s))20 else if(gallery)24 else 32)*k).sp,lineHeight=((if(gallery && compact && L10n.isUrdu(s))28 else if(gallery)30 else 38)*k).sp,
                    color=tk.contentPrimary,maxLines=if(L10n.isUrdu(s))2 else 1,style=tabletNativeText)
                if(!L10n.isUrdu(s)) Text(r.arabic,Modifier.testTag("gallery-fit-arabic-${r.prayer.name}"),fontFamily=Amiri,
                    fontSize=((if(gallery)24 else 28)*k).sp,lineHeight=((if(gallery)30 else 34)*k).sp,
                    color=accent,fontWeight=FontWeight.Bold,maxLines=1,style=tabletNativeText)
                if(selected && compactBadge) TabletBadge(r,s,marker,k)
            }
            Column(Modifier.weight(1f).padding(start=(4*k).dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy((5*k).dp)) {
                    Row(Modifier.weight(1f,fill=false),verticalAlignment=Alignment.CenterVertically) {
                        Text(if(s.showRelative)r.relative else r.clock,Modifier.testTag("gallery-fit-clock-${r.prayer.name}"),
                            fontFamily=if(s.showRelative)tabletUi(s) else tabletDisplay(tk),fontWeight=FontWeight.Medium,
                            fontSize=((if(s.showRelative) (if(gallery)18 else 22) else if(gallery)36 else if(L10n.isUrdu(s) && s.showEndTimes)36 else if(compact && s.iqamahEnabled)36 else 46)*k).sp,
                            lineHeight=((if(s.showRelative) (if(gallery)24 else 28) else if(gallery)44 else if(L10n.isUrdu(s) && s.showEndTimes)44 else if(compact && s.iqamahEnabled)44 else 54)*k).sp,
                            color=if(gallery && selected)tk.accent else tk.contentPrimary,maxLines=if(s.showRelative)2 else 1,
                            style=if(s.showRelative)tabletNativeText else tabletClockText)
                        if(!s.showRelative && r.suffix.isNotEmpty()) Text(" ${r.suffix}",fontFamily=if(L10n.isUrdu(s))Amiri else tabletDisplay(tk),
                            fontSize=((if(gallery)16 else 19)*k).sp,color=tk.contentPrimary,maxLines=1,style=tabletNativeText)
                    }
                    if(selected && !compactBadge) TabletBadge(r,s,marker,k)
                }
                if(r.small.isNotEmpty()) Text(r.small,Modifier.testTag("gallery-fit-detail-${r.prayer.name}"),fontFamily=tabletUi(s),
                    fontSize=((if(gallery)16 else if(compact && s.iqamahEnabled)16 else 18)*k).sp,lineHeight=((if(L10n.isUrdu(s))32 else if(gallery)21 else if(compact && s.iqamahEnabled)20 else 24)*k).sp,
                    color=tk.contentSecondary,maxLines=2,style=tabletNativeText)
            }
            if(r.prayer.isPrayer) Box(Modifier.width((28*k).dp).heightIn(min=48.dp).semantics {
                contentDescription=if(r.azaanOn)L10n.t("Azaan enabled","اذان فعال ہے") else L10n.t("Azaan off","اذان بند ہے")
            },contentAlignment=Alignment.Center) {
                Icon(if(r.azaanOn)Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff,null,Modifier.size(((if(gallery)22 else 28)*k).dp),tint=accent)
            }
            IconButton(onWhy,Modifier.size(48.dp).testTag("approved-info-${r.prayer.name}").semantics { contentDescription=Str[R.string.s_why_this_time] }) {
                Icon(Icons.Outlined.Info,null,Modifier.size(((if(gallery)25 else 29)*k).dp),tint=tk.contentPrimary)
            }
        }
    }
}

@Composable
private fun TabletBadge(r:RowInfo,s:AppSettings,marker:Color,k:Float) {
    Text(L10n.word(s,if(r.isNow)"NOW" else "NEXT"),Modifier.testTag(if(s.theme==AppTheme.PRAYER_GALLERY)
        (if(r.isNow)"gallery-status-now" else "gallery-status-next") else (if(r.isNow)"home-status-now" else "home-status-next"))
        .clip(RoundedCornerShape(20.dp)).background(marker).padding(horizontal=(10*k).dp,vertical=(4*k).dp),
        fontFamily=tabletUi(s),fontSize=(12*k).sp,lineHeight=(16*k).sp,color=if(r.isNow)Color.White else Color(0xFF062325),maxLines=1,style=tabletNativeText)
}

@Composable
private fun TabletDock(state:PrayerState,s:AppSettings,a:HomeActions,tk:ThemeTokens,k:Float) {
    val gallery=s.theme==AppTheme.PRAYER_GALLERY
    val mode=adhkarModes(state,s).firstOrNull()?:AdhkarMode.MORNING
    val q=PrayerEngine.qibla(s)
    val bg=if(gallery)tk.background.copy(alpha=.65f) else Color(0xFF001629).copy(alpha=.94f)
    Row(Modifier.fillMaxWidth().heightIn(min=maxOf(56.dp,((if(gallery)56 else 68)*k).dp)).clip(RoundedCornerShape(if(gallery)36.dp else 22.dp))
        .background(bg).border(.7.dp,if(gallery)tk.accent else tk.outline,RoundedCornerShape(if(gallery)36.dp else 22.dp))
        .testTag("gallery-phone-footer"),verticalAlignment=Alignment.CenterVertically) {
        if(s.showQibla) TabletDockItem(Icons.Outlined.Explore,L10n.word(s,"Qibla"),L10n.iso("${q.toInt()}° ${PrayerEngine.compass(q)}"),
            L10n.word(s,"Qibla"),s,tk,k,Modifier.weight(1f),a.onOpenQibla)
        if(s.showQibla) Box(Modifier.width(.6.dp).height((38*k).dp).background(tk.primary.copy(alpha=.5f)))
        TabletDockItem(Icons.Outlined.MenuBook,L10n.t("Adhkar","اذکار"),when(mode){AdhkarMode.MORNING->L10n.t("Morning","صبح");AdhkarMode.EVENING->L10n.t("Evening","شام");else->L10n.t("After prayer","نماز کے بعد")},
            L10n.t("Adhkar","اذکار"),s,tk,k,Modifier.weight(1f)) { a.onOpenAdhkar(mode) }
        if(s.kidsMode) Box(Modifier.width(.6.dp).height((38*k).dp).background(tk.primary.copy(alpha=.5f)))
        if(s.kidsMode) TabletDockItem(Icons.Outlined.MenuBook,L10n.t("Learn","سیکھیں"),L10n.t("Salah","نماز"),
            Str[R.string.s_learn_salah],s,tk,k,Modifier.weight(1f),a.onOpenLearn)
        Box(Modifier.width(.6.dp).height((38*k).dp).background(tk.primary.copy(alpha=.5f)))
        TabletDockItem(Icons.Outlined.CalendarMonth,L10n.t("Timetable","اوقات"),L10n.t("Monthly","ماہانہ"),
            Str[R.string.s_monthly_timetable],s,tk,k,Modifier.weight(1f),a.onOpenTimetable)
    }
}

@Composable
private fun TabletDockItem(icon:ImageVector,label:String,sub:String,spoken:String,s:AppSettings,tk:ThemeTokens,k:Float,modifier:Modifier,onClick:()->Unit) {
    Row(modifier.heightIn(min=maxOf(56.dp,((if(tk.dark)68 else 56)*k).dp)).clip(RoundedCornerShape(24.dp)).clickable(role=Role.Button,onClick=onClick)
        .semantics(mergeDescendants=true) { contentDescription=spoken }.padding(horizontal=(18*k).dp),
        verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
        Icon(icon,null,Modifier.size((38*k).dp),tint=if(tk.dark)tk.primary else tk.accent)
        Column(Modifier.padding(start=(18*k).dp)) {
            Text(label,fontFamily=if(tk.dark && !L10n.isUrdu(s))ApprovedDisplay else tabletUi(s),fontSize=(20*k).sp,
                lineHeight=(24*k).sp,color=tk.contentPrimary,maxLines=1,style=tabletNativeText)
            Text(sub,fontFamily=tabletUi(s),fontSize=(14*k).sp,lineHeight=(19*k).sp,color=tk.contentSecondary,maxLines=1,style=tabletNativeText)
        }
    }
}

@Composable
private fun TabletContext(links:List<Door>,s:AppSettings,tk:ThemeTokens,k:Float) {
    val extras=links.filter { it.kind!=DoorKind.ADHKAR && !(s.showQibla && it===links.firstOrNull()) }
    if(extras.isEmpty()) return
    val notice=extras.firstOrNull { it.warn }?:extras.first()
    var more by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        Text(notice.label,Modifier.weight(1f).heightIn(min=48.dp).wrapContentHeight()
            .then(if(notice.onClick!=null)Modifier.clickable(role=Role.Button,onClick=notice.onClick) else Modifier),
            fontFamily=tabletUi(s),fontSize=(14*k).sp,lineHeight=(20*k).sp,color=if(notice.warn)tk.warning else tk.contentSecondary,maxLines=2,style=tabletNativeText)
        Box {
            IconButton({more=true},Modifier.size(48.dp).semantics { contentDescription=L10n.word(s,"More actions") }) {
                Icon(Icons.Outlined.MoreHoriz,null,tint=tk.contentPrimary)
            }
            DropdownMenu(more,{more=false}) { extras.forEach { d -> DropdownMenuItem(text={Text(d.label)},onClick={more=false;d.onClick?.invoke()},enabled=d.onClick!=null) } }
        }
    }
}
