package com.usman.miqaat.ui

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import java.util.Locale

@Composable
internal fun isPhoneLandscape(): Boolean {
    val c = LocalConfiguration.current
    return c.orientation == Configuration.ORIENTATION_LANDSCAPE && c.screenHeightDp < 500
}

/** Native landscape composition measured against the approved four-theme board. */
@Composable
internal fun PhoneLandscapeHome(state: PrayerState, s: AppSettings, a: HomeActions) {
    val tk=ThemeTokenSets.of(s.theme);val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s)
    val hero=heroInfo(state,s,kicker(state,s));val links=doors(state,s,a)
    val seasonal=listOfNotNull(hero.special,fastProgress(state,s)?.second,tarawihLine(state,s))
    var why by remember { mutableStateOf<Prayer?>(null) };var more by remember { mutableStateOf(false) }
    why?.let { p ->
        val day=if(p==state.next&&state.nextTime.toLocalDate().isAfter(state.today.date))PrayerEngine.times(s,state.nextTime.toLocalDate())else state.today
        WhyDialog(s,day,p){why=null}
    }
    Box(Modifier.fillMaxSize().clipToBounds().background(c.background).testTag("landscape-fit-board")) {
        LandscapeHomeArt(s,Modifier.matchParentSize())
        Box(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=12.dp,vertical=if(s.theme==AppTheme.KISWAH)0.dp else 2.dp)) {
            val reflow=s.iqamahEnabled||(urdu&&s.showRelative)
            Column(Modifier.fillMaxSize()) {
              Column(Modifier.weight(1f).fillMaxWidth().then(if(reflow)Modifier.verticalScroll(rememberScrollState()).testTag("landscape-home-reflow")else Modifier)) {
                LandscapeHomeHeader(state,s,a)
                if(s.theme==AppTheme.KISWAH)PhoneLandscapeBand(ornate=true)
                Row(Modifier.fillMaxWidth().then(if(reflow)Modifier.height(if(urdu)270.dp else 240.dp)else Modifier.weight(1f)).padding(vertical=if(urdu)0.dp else if(s.theme==AppTheme.KISWAH)if(LocalConfiguration.current.screenHeightDp<390)0.dp else 2.dp else 10.dp),
                    horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(.62f).fillMaxHeight().testTag("home-prayers"),verticalArrangement=Arrangement.spacedBy(if(s.theme==AppTheme.KISWAH&&LocalConfiguration.current.screenHeightDp<390)2.dp else 5.dp)) {
                        listedPrayers(s).chunked(2).forEach { row ->
                            Row(Modifier.weight(1f).fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                row.forEach { p ->PhoneLandscapePrayer(galleryRowInfo(p,state,s),s,a,Modifier.weight(1f).fillMaxHeight()){why=p} }
                                if(row.size==1)Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    LandscapeHomeHero(hero,state,s,Modifier.weight(.38f).fillMaxHeight(),a.onToggleRelative){why=hero.prayer}
                }
              }
                Box(Modifier.fillMaxWidth().height(if(LocalConfiguration.current.screenHeightDp>=390)52.dp else 40.dp).testTag("landscape-timeline-panel")) {ApprovedTimeline(state,s,tk,Modifier.fillMaxSize().padding(start=if(LocalConfiguration.current.screenWidthDp>=840)36.dp else 0.dp,end=if(LocalConfiguration.current.screenWidthDp>=840)136.dp else 0.dp),landscape=true)}
                val qibla=if(s.showQibla)links.firstOrNull()else null
                val adhkar=links.firstOrNull{it.kind==DoorKind.ADHKAR}
                val warning=links.firstOrNull{it.warn}
                Row(Modifier.fillMaxWidth().padding(end=44.dp).heightIn(min=48.dp).testTag("landscape-context-actions"),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                    if(qibla!=null)LandscapeHomeDoor(qibla.label,Icons.Outlined.Explore,s,Modifier.weight(.25f).testTag("landscape-door-0"),footer=true,onClick={qibla.onClick?.invoke()})
                    LandscapeHomeDoor(adhkar?.label?:L10n.t("Adhkar","اذکار"),Icons.Outlined.MenuBook,s,Modifier.weight(.24f).testTag("landscape-door-1"),footer=true,onClick={if(adhkar!=null)adhkar.onClick?.invoke()else a.onOpenAdhkar(AdhkarMode.MORNING)})
                    Spacer(Modifier.weight(.13f))
                    Box(Modifier.weight(.38f)) {
                        LandscapeHomeDoor(if(warning!=null)L10n.t("Alerts need attention · Fix","انتباہات پر توجہ دیں · درست کریں")else L10n.t("More actions","مزید اختیارات"),
                            if(warning!=null)Icons.Outlined.WarningAmber else Icons.Outlined.MoreHoriz,s,
                            Modifier.fillMaxWidth().testTag("landscape-more-actions").semantics{contentDescription=warning?.label?:L10n.word(s,"More actions")},warn=warning!=null,footer=true,onClick={more=true})
                        DropdownMenu(more,{more=false}) {
                            links.forEach { d ->DropdownMenuItem(text={Text(d.label)},onClick={more=false;d.onClick?.invoke()}) }
                            DropdownMenuItem(text={Text(L10n.t("Adhkar","اذکار"))},onClick={more=false;a.onOpenAdhkar(AdhkarMode.MORNING)})
                            if(s.kidsMode)DropdownMenuItem(text={Text(if(urdu)"نماز کی تیاری"else"Prepare for ${hero.label}")},onClick={more=false;a.onPreparePrayer(hero.prayer)})
                            seasonal.forEach{line->DropdownMenuItem(text={Text(line)},onClick={},enabled=false)}
                        }
                    }
                }
            }
        }
        if(s.nightDim&&state.period==Prayer.ISHA&&!state.justPassed)Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.22f)))
    }
}

@Composable
private fun LandscapeHomeHeader(state:PrayerState,s:AppSettings,a:HomeActions) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val f=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif
    BoxWithConstraints(Modifier.fillMaxWidth().height(if(LocalConfiguration.current.screenHeightDp>=390)52.dp else 48.dp).testTag("landscape-compact-header")) {
        val compact=maxWidth<780.dp
        val navWidth=if(maxWidth>=900.dp)82.dp else if(maxWidth>=800.dp)74.dp else 58.dp
        val place=L10n.place(s.locationName)
        val longPlace=!urdu&&!compact&&place.length>20
        Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically) {
            Row(Modifier.weight(if(longPlace).35f else .30f).fillMaxHeight().clickable(role=Role.Button,onClick=a.onOpenLocation),verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn,null,Modifier.size(20.dp),tint=c.accent)
                val placeSize=if(urdu)12.sp else if(place.length>20)if(compact)10.sp else 11.sp else if(compact)12.sp else if(place.length>16)14.sp else 16.sp
                Text(place,Modifier.weight(1f).padding(start=5.dp).testTag("landscape-location"),fontFamily=f,fontSize=placeSize,lineHeight=if(urdu)22.sp else 20.sp,color=c.ink,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
            Row(Modifier.weight(if(longPlace).44f else .49f),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                FittedLandscapeDate(L10n.dateShort(s,state.now)+" "+state.now.year,s,Modifier.weight(1f),"landscape-date",if(!urdu&&!compact)13f else 10f)
                if(s.showHijri) {
                    Box(Modifier.padding(horizontal=5.dp).width(.5.dp).height(16.dp).background(c.accent.copy(alpha=.45f)))
                    FittedLandscapeDate(galleryHijriLine(s,PrayerEngine.hijri(state.now.toLocalDate(),s.hijriOffsetDays)),s,Modifier.weight(1.35f),"landscape-hijri",if(!urdu&&!compact)13f else 10f)
                }
            }
            Row(Modifier.weight(.21f).padding(horizontal=4.dp).clip(RoundedCornerShape(16.dp)).background(c.panel)
                .border(.6.dp,c.accent.copy(alpha=.45f),RoundedCornerShape(16.dp)).padding(horizontal=6.dp,vertical=5.dp),
                horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                Text(L10n.word(s,"NOW"),fontFamily=if(urdu)Amiri else ApprovedBody,fontSize=if(!urdu&&!compact)9.sp else 8.sp,lineHeight=if(urdu)19.sp else 13.sp,color=c.accent)
                Text(" "+L10n.iso(L10n.at(state.now,s.use24h)),Modifier.testTag("landscape-live-clock"),fontFamily=f,fontSize=if(!urdu&&!compact)14.sp else 11.sp,lineHeight=if(urdu)21.sp else if(!compact)18.sp else 15.sp,color=c.ink)
            }
            Row(Modifier.clip(RoundedCornerShape(18.dp)).background(c.panel.copy(alpha=.55f)).border(.5.dp,c.accent.copy(alpha=.2f),RoundedCornerShape(18.dp))) {
            if(s.kidsMode)PhoneHomeNav(Icons.Outlined.MenuBook,Str[R.string.s_learn_salah],if(urdu)Str[R.string.s_theme_home_nav_learn]else"Learn Salah",s,a.onOpenLearn,navWidth)
            PhoneHomeNav(Icons.Outlined.CalendarMonth,Str[R.string.s_monthly_timetable],Str[R.string.s_theme_home_nav_timetable],s,a.onOpenTimetable,navWidth)
            PhoneHomeNav(Icons.Outlined.Settings,Str[R.string.s_settings],Str[R.string.s_theme_home_nav_settings],s,a.onOpenSettings,navWidth)
            }
        }
    }
}

@Composable
private fun FittedLandscapeDate(text:String,s:AppSettings,modifier:Modifier,tag:String,preferred:Float) {
    val font=if(L10n.isUrdu(s))Amiri else androidx.compose.ui.text.font.FontFamily.Serif
    val measure=rememberTextMeasurer();val density=LocalDensity.current
    BoxWithConstraints(modifier,contentAlignment=Alignment.Center) {
        val natural=measure.measure(text,TextStyle(fontFamily=font,fontSize=preferred.sp),softWrap=false,maxLines=1).size.width
        val available=with(density){maxWidth.toPx()}
        val fitted=(preferred*minOf(1f,available/natural.coerceAtLeast(1))).sp
        Text(text,Modifier.testTag(tag),fontFamily=font,fontSize=fitted,lineHeight=if(L10n.isUrdu(s))22.sp else 18.sp,
            color=quietPalette(s.theme).ink,textAlign=TextAlign.Center,maxLines=1,softWrap=false)
    }
}

@Composable
private fun FittedLandscapeLine(text:androidx.compose.ui.text.AnnotatedString,modifier:Modifier,font:androidx.compose.ui.text.font.FontFamily,size:TextUnit,line:TextUnit,color:Color,tag:String) {
    val measure=rememberTextMeasurer();val density=LocalDensity.current
    BoxWithConstraints(modifier) {
        val available=with(density){maxWidth.toPx()}.toInt()-2
        var low=size.value*.6f;var high=size.value
        repeat(10) {
            val trial=(low+high)/2
            val w=measure.measure(text,TextStyle(fontFamily=font,fontSize=trial.sp,letterSpacing=0.sp),softWrap=false,maxLines=1).size.width
            if(w<=available)low=trial else high=trial
        }
        Text(text,Modifier.testTag(tag),style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false)),fontFamily=font,fontSize=low.sp,letterSpacing=0.sp,
            lineHeight=(line.value*(low/size.value)).sp,color=color,maxLines=1,softWrap=false)
    }
}

@Composable
private fun LandscapeHomeHero(hero:HeroInfo,state:PrayerState,s:AppSettings,modifier:Modifier,onToggle:()->Unit,onDetails:()->Unit) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val gallery=s.theme==AppTheme.PRAYER_GALLERY
    BoxWithConstraints(modifier.testTag("landscape-authored-hero").padding(end=if(gallery)40.dp else if(s.theme==AppTheme.KISWAH)44.dp else 0.dp),contentAlignment=Alignment.Center) {
        val heroStyle=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false),shadow=if(gallery)null else Shadow(Color.Black.copy(alpha=.85f),Offset(0f,1.5f),4f))
        val paired=maxHeight<if(urdu)215.dp else if(s.theme==AppTheme.KISWAH)186.dp else 190.dp
        val compact=maxHeight<215.dp
        val tiny=maxHeight<175.dp
        val compactKiswah=s.theme==AppTheme.KISWAH&&compact
        val largeDisplay=!urdu&&maxHeight>=if(s.theme==AppTheme.KISWAH)190.dp else 222.dp
        // These display fonts reserve much more ascent/descent than the six prayer
        // names use. Center their complete native text in optical slots; never clip
        // glyphs or spend the details button's independent 48dp touch target.
        val arabicSlot=if(s.theme==AppTheme.KISWAH)if(tiny)34.dp else if(compact)if(largeDisplay)40.dp else 38.dp else 54.dp else if(compact)38.dp else 54.dp
        val clockSlot=if(compactKiswah)if(tiny)44.dp else 54.dp else if(compact)48.dp else if(largeDisplay)70.dp else 66.dp
        val name:@Composable (Modifier)->Unit={m->Column(m,horizontalAlignment=Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().then(if(urdu)Modifier else Modifier.height(arabicSlot)),contentAlignment=Alignment.Center) {
            Text(hero.arabic,Modifier.fillMaxWidth().then(if(urdu)Modifier else Modifier.wrapContentHeight(unbounded=true)).testTag("landscape-hero-name"),style=heroStyle.copy(textGeometricTransform=TextGeometricTransform(scaleX=1.35f)),fontFamily=Amiri,fontWeight=FontWeight.Bold,fontSize=if(s.theme==AppTheme.KISWAH)if(tiny)24.sp else if(compact)30.sp else 38.sp else if(compact)30.sp else if(s.iqamahEnabled)34.sp else 38.sp,lineHeight=if(s.theme==AppTheme.KISWAH)if(tiny)30.sp else if(compact)36.sp else 46.sp else if(compact)40.sp else 48.sp,color=c.accent,textAlign=TextAlign.Center)
            }
            Box(Modifier.fillMaxWidth().then(if(urdu)Modifier else Modifier.height(16.dp)),contentAlignment=Alignment.Center) {
            Text(if(urdu)hero.label else hero.label.uppercase(Locale.ROOT),Modifier.then(if(urdu)Modifier else Modifier.wrapContentHeight(unbounded=true)).testTag("landscape-hero-english"),style=heroStyle,fontFamily=if(urdu)Amiri else Cinzel,fontSize=if(urdu)9.sp else if(largeDisplay)14.sp else 11.sp,lineHeight=if(urdu)21.sp else 18.sp,letterSpacing=if(urdu)0.sp else 1.3.sp,color=if(gallery)c.accent else c.ink,textAlign=TextAlign.Center)
            }
        }}
        val heroClock=L10n.iso(L10n.at(state.heroTime,s.use24h))
        val clock:@Composable (Modifier)->Unit={m->Box(m.then(if(urdu)Modifier else Modifier.height(clockSlot)),contentAlignment=Alignment.Center) {Text(buildAnnotatedString {
            append(heroClock)
            if(hero.suffix.isNotBlank()) {val start=heroClock.indexOf(hero.suffix);if(start>=0)addStyle(SpanStyle(fontSize=14.sp,baselineShift=BaselineShift(.2f)),start,start+hero.suffix.length)}
        },Modifier.fillMaxWidth().then(if(urdu)Modifier else Modifier.wrapContentHeight(unbounded=true)).testTag("landscape-hero-clock").clickable(role=Role.Button,onClick=onToggle),style=heroStyle,fontFamily=if(urdu)Amiri else ApprovedDisplay,fontWeight=FontWeight.Bold,
            fontSize=if(urdu)25.sp else if(compactKiswah)if(tiny)38.sp else if(largeDisplay)58.sp else 52.sp else if(compact)40.sp else if(largeDisplay)64.sp else 62.sp,lineHeight=if(urdu)if(paired)38.sp else 45.sp else if(compactKiswah)if(tiny)42.sp else 54.sp else if(compact)49.sp else 66.sp,letterSpacing=if(urdu)0.sp else 1.4.sp,color=if(gallery)c.accent else c.ink,textAlign=TextAlign.Center)}}
        val galleryExtraInset=if(gallery&&maxWidth>=250.dp)60.dp else 0.dp
        Box(Modifier.fillMaxSize().padding(end=galleryExtraInset),contentAlignment=Alignment.Center) {
        Column(Modifier.widthIn(max=244.dp).fillMaxWidth().fillMaxHeight(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            val kicker:@Composable (Modifier)->Unit={m->Box(m.then(if(urdu||paired)Modifier else Modifier.height(if(compactKiswah)if(largeDisplay)16.dp else 14.5.dp else if(largeDisplay)18.dp else 16.dp)),contentAlignment=Alignment.Center) {
                Text(hero.special?:if(urdu)hero.kickerLabel else hero.kickerLabel.uppercase(Locale.ROOT),Modifier.fillMaxWidth().then(if(urdu||paired)Modifier else Modifier.wrapContentHeight(unbounded=true)).testTag("landscape-hero-kicker"),style=heroStyle,fontFamily=if(urdu)Amiri else Cinzel,fontWeight=FontWeight.Bold,fontSize=if(hero.special!=null)9.sp else if(largeDisplay)if(compactKiswah)13.sp else 14.sp else if(compactKiswah)11.sp else 12.sp,lineHeight=if(urdu)20.sp else 18.sp,letterSpacing=if(urdu)0.sp else 2.sp,color=c.accent,textAlign=TextAlign.Center)
            }}
            val status:@Composable (Modifier)->Unit={m->Row(m,horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule,null,Modifier.size(14.dp),tint=c.accent)
                Text(hero.status,Modifier.weight(1f,fill=false).padding(start=4.dp).testTag("landscape-hero-status"),style=heroStyle,fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,fontSize=if(largeDisplay)14.sp else 12.sp,lineHeight=if(urdu)22.sp else if(largeDisplay)18.sp else if(compactKiswah)14.sp else 17.sp,color=if(gallery)c.accent else c.ink,textAlign=TextAlign.Center)
            }}
            if(paired)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){kicker(Modifier.weight(.35f));status(Modifier.weight(.65f))}else kicker(Modifier.fillMaxWidth())
            if(paired)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){name(Modifier.weight(.43f));clock(Modifier.weight(.57f))}
            else {name(Modifier.fillMaxWidth());clock(Modifier.fillMaxWidth());status(Modifier.fillMaxWidth())}
            LandscapeHomeDoor(Str[R.string.s_view_prayer_details],null,s,Modifier.widthIn(max=190.dp).fillMaxWidth().testTag("landscape-hero-details"),solid=gallery,onClick=onDetails)
        }
        }
    }
}

/** The visual pill is shallow; its independent 48dp target never overlaps a neighbour. */
@Composable
private fun LandscapeHomeDoor(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector?,s:AppSettings,modifier:Modifier=Modifier,warn:Boolean=false,solid:Boolean=false,footer:Boolean=false,onClick:()->Unit) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val outline=if(warn&&s.theme==AppTheme.PRAYER_GALLERY)Color(0xFF83520D)else if(warn)Color(0xFFE9B85B)else c.accent
    Box(modifier.heightIn(min=48.dp).clickable(role=Role.Button,onClick=onClick).semantics(mergeDescendants=true){},contentAlignment=Alignment.Center) {
        Row(Modifier.fillMaxWidth().heightIn(min=if(LocalConfiguration.current.screenHeightDp>=390)34.dp else 30.dp).clip(RoundedCornerShape(16.dp))
            .background(if(solid)c.accent else if(warn&&s.theme==AppTheme.PRAYER_GALLERY)Color(0xFFFFF3DF).copy(alpha=.97f)else if(s.theme!=AppTheme.PRAYER_GALLERY)(if(s.theme==AppTheme.KISWAH)Color(0xFF10100F)else Color(0xFF061C35)).copy(alpha=if(warn).90f else .84f)else c.panel)
            .border(.8.dp,outline.copy(alpha=if(warn).85f else .65f),RoundedCornerShape(16.dp)).padding(horizontal=8.dp,vertical=2.dp),
            verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
            if(icon!=null)Icon(icon,null,Modifier.size(if(footer)22.dp else 20.dp),tint=if(solid)Color.White else outline)
            Text(label,Modifier.padding(horizontal=5.dp),fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,fontSize=if(urdu)13.sp else 15.sp,
                lineHeight=if(urdu)23.sp else if(footer)22.sp else 20.sp,color=if(solid)Color(0xFFFFF9ED)else if(warn)outline else c.ink,textAlign=TextAlign.Center)
            if(icon==null||warn)Icon(Icons.AutoMirrored.Outlined.ArrowForward,null,Modifier.size(15.dp),tint=if(solid)Color.White else outline)
        }
    }
}

@Composable
private fun PhoneHomeNav(icon: androidx.compose.ui.graphics.vector.ImageVector, label:String, short:String, s:AppSettings,onClick:()->Unit,width:Dp=58.dp) {
    Column(Modifier.width(width).height(48.dp).clip(RoundedCornerShape(10.dp)).clickable(role=Role.Button,onClick=onClick)
        .semantics(mergeDescendants=true){contentDescription=label},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Icon(icon,null,Modifier.size(if(width>58.dp)22.dp else 21.dp),tint=quietPalette(s.theme).ink)
        Text(short,fontFamily=if(L10n.isUrdu(s))Amiri else ApprovedBody,fontSize=if(width>58.dp&&!L10n.isUrdu(s))10.sp else 9.sp,lineHeight=if(L10n.isUrdu(s))18.sp else 13.sp,color=quietPalette(s.theme).ink)
    }
}

@Composable
internal fun PhoneHomeButton(label:String,s:AppSettings,modifier:Modifier=Modifier,labelTag:String?=null,onClick:()->Unit) {
    val c=quietPalette(s.theme)
    Box(modifier.heightIn(min=48.dp).clip(RoundedCornerShape(24.dp)).background(c.panel)
        .border(.8.dp,c.accent.copy(alpha=.65f),RoundedCornerShape(24.dp)).clickable(role=Role.Button,onClick=onClick)
        .padding(horizontal=10.dp,vertical=5.dp),contentAlignment=Alignment.Center) {
        Text(label,Modifier.fillMaxWidth().then(if(labelTag!=null) Modifier.testTag(labelTag) else Modifier),fontFamily=if(L10n.isUrdu(s))Amiri else ApprovedBody,fontSize=12.sp,
            lineHeight=if(L10n.isUrdu(s))24.sp else 18.sp,color=c.ink,textAlign=TextAlign.Center)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PhoneLandscapePrayer(r:RowInfo,s:AppSettings,a:HomeActions,modifier:Modifier,onWhy:()->Unit) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val gallery=s.theme==AppTheme.PRAYER_GALLERY
    val mark=if(r.isNow)Color(0xFF43DADE)else if(gallery&&r.isNext)Color(0xFFC49B61)else c.accent
    val fill=when {r.isNow->if(gallery)Color(0xFFE4EFEB)else Color(0xFF074148).copy(alpha=.88f)
        r.isNext->if(gallery)Color(0xFFFDF1E3)else Color(0xFF65491D).copy(alpha=.85f)
        else->if(gallery)Color(0xFFF8F5EC).copy(alpha=.85f)else if(s.theme==AppTheme.KISWAH)Color(0xFF090909).copy(alpha=.8f)else Color(0xFF03162C).copy(alpha=.65f)}
    val paint=if(gallery)Brush.horizontalGradient(listOf(fill,fill.copy(alpha=.94f))) else when {
        r.isNow&&s.theme==AppTheme.CELESTIAL_MERIDIAN->Brush.horizontalGradient(listOf(Color(0xFF07505A).copy(alpha=.94f),Color(0xFF032E37).copy(alpha=.90f)))
        r.isNow&&s.theme==AppTheme.KISWAH->Brush.horizontalGradient(listOf(Color(0xFF07505A).copy(alpha=.70f),Color(0xFF032E37).copy(alpha=.62f)))
        r.isNow->Brush.horizontalGradient(listOf(Color(0xFF074646).copy(alpha=.70f),Color(0xFF03262A).copy(alpha=.62f)))
        r.isNext&&s.theme==AppTheme.KISWAH->Brush.horizontalGradient(listOf(Color(0xFF9F732B).copy(alpha=.72f),Color(0xFF4F351B).copy(alpha=.68f)))
        r.isNext->Brush.horizontalGradient(listOf(Color(0xFF76501D).copy(alpha=.63f),Color(0xFF38220C).copy(alpha=.72f)))
        s.theme==AppTheme.KISWAH->Brush.horizontalGradient(listOf(Color(0xFF090909).copy(alpha=.82f),Color(0xFF10100F).copy(alpha=.58f)))
        s.theme==AppTheme.CELESTIAL_MERIDIAN->Brush.horizontalGradient(listOf(Color(0xFF061A30).copy(alpha=.94f),Color(0xFF071A30).copy(alpha=.88f)))
        else->Brush.horizontalGradient(listOf(Color(0xFF041A34).copy(alpha=.92f),Color(0xFF03142A).copy(alpha=.86f)))
    }
    BoxWithConstraints(modifier.heightIn(min=48.dp)) {
        val tight=maxWidth<260.dp
        val large=maxWidth>=240.dp
        val wideCard=maxWidth>=270.dp
        val tinyCard=maxWidth<225.dp
        val narrow=maxWidth<215.dp
        val thumbnail=gallery&&s.artTheme!=ArtTheme.MINIMAL&&!(tight&&urdu)
        val thumbnailSide=if(tight)if(narrow)22.dp else 30.dp else 46.dp
        val measure=rememberTextMeasurer()
        val density=LocalDensity.current
        val labelSize=if(urdu)14.sp else if(large&&!gallery)15.sp else if(gallery&&large)13.sp else if(tight)12.sp else 13.sp
        val arabicSize=if(large&&!gallery)17.sp else 14.sp
        val timeSize=if(urdu)18.sp else if(large&&!gallery)20.sp else if(large&&gallery)if(wideCard)19.sp else 17.sp else if(thumbnail&&narrow&&!urdu)12.sp else if(tinyCard)14.sp else if(gallery&&tight)16.sp else 18.sp
        val detailSize=if(r.prayer==Prayer.ISHA&&!urdu)if(large&&wideCard)9.5.sp else if(large)9.sp else 8.sp else if(large&&!urdu)11.sp else if(gallery&&!urdu)8.sp else 9.sp
        val detailText=if(r.prayer==Prayer.ISHA&&s.showEndTimes&&!urdu)r.small.replaceFirst(L10n.word(s,"ends"),if(wideCard)"Preferred end"else "Pref. end")else r.small
        val captionFont=if(urdu)Amiri else ApprovedBody
        val badgeWidth=with(density) {
            val size=if(large&&!urdu)9.sp else 7.sp
            val font=if(urdu)Amiri else ApprovedBody
            maxOf(if(urdu)30.dp else if(large)34.dp else 28.dp,
                listOf("NOW","NEXT").maxOf { label ->
                    measure.measure(L10n.word(s,label),TextStyle(fontFamily=font,fontWeight=FontWeight.Bold,fontSize=size),softWrap=false,maxLines=1).size.width.toDp()+8.dp
                })
        }
        val suffixSize=if(large&&(!gallery||wideCard))11.sp else 9.sp
        val raw=L10n.iso((r.clock+" "+r.suffix).trim())
        val clockText=buildAnnotatedString {
            append(raw)
            if(r.suffix.isNotBlank()){val start=raw.indexOf(r.suffix);if(start>=0)addStyle(SpanStyle(fontSize=suffixSize,baselineShift=BaselineShift(.15f)),start,start+r.suffix.length)}
        }
        val serif=androidx.compose.ui.text.font.FontFamily.Serif
        // One shared label column for every prayer, not six content-dependent offsets.
        val labelWidth=with(density) {
            listedPrayers(s).maxOf { p ->
                val label=if(p==Prayer.DHUHR&&s.jumuahEnabled)L10n.word(s,"Jumuʿah")else L10n.prayer(s,p)
                maxOf(measure.measure(label,TextStyle(fontFamily=if(urdu)Amiri else serif,fontSize=labelSize)).size.width.toDp(),
                    if(urdu)0.dp else measure.measure(p.arabic,TextStyle(fontFamily=Amiri,fontWeight=FontWeight.Bold,fontSize=arabicSize)).size.width.toDp())
            }+1.dp
        }
        val nameGap=if(gallery&&large&&!urdu)4.dp else if(large&&!urdu)8.dp else 2.dp
        // Match the board's inset labels without sacrificing a long clock, badge or
        // detail line on a narrower phone. Measure the real native text, not character counts.
        val leadingExtra=if(large&&!urdu&&!gallery)8.dp else 0.dp
        // Reserve the same badge lane in every card and fit a worst-case clock once.
        // A NOW/NEXT row must not silently use a smaller clock than its neighbours.
        val clockRoom=maxWidth-(if(thumbnail)thumbnailSide+8.dp else if(large)12.dp else 6.dp)-
            (4.dp+if(gallery)leadingExtra else leadingExtra*.52f)-labelWidth-nameGap-48.dp-badgeWidth-2.dp
        val template=buildAnnotatedString {
            append(if(s.use24h)"88:88"else"88:88 AM")
            if(!s.use24h)addStyle(SpanStyle(fontSize=suffixSize,baselineShift=BaselineShift(.15f)),6,8)
        }
        val uniformTimeSize=with(density) {
            var low=timeSize.value*.6f;var high=timeSize.value
            repeat(10) {
                val trial=(low+high)/2
                val width=measure.measure(template,TextStyle(fontFamily=if(urdu)Amiri else serif,fontSize=trial.sp),softWrap=false,maxLines=1).size.width
                if(width<=clockRoom.toPx()-4)low=trial else high=trial
            }
            low.sp
        }
        val badge:@Composable ()->Unit={
            Box(Modifier.width(badgeWidth).clip(RoundedCornerShape(10.dp))
                .background(if(r.isNow)Color(0xFF34D6DA)else Color(0xFFFFD56E)),contentAlignment=Alignment.Center) {
                Text(L10n.word(s,if(r.isNow)"NOW"else"NEXT"),Modifier.padding(horizontal=2.dp,vertical=2.dp).testTag(if(gallery)if(r.isNow)"gallery-status-now"else"gallery-status-next"else if(r.isNow)"home-status-now"else"home-status-next"),
                    fontFamily=if(urdu)Amiri else ApprovedBody,fontWeight=FontWeight.Bold,fontSize=if(large&&!urdu)9.sp else 7.sp,lineHeight=if(urdu)18.sp else if(large)13.sp else 11.sp,color=Color(0xFF112F32),maxLines=1)
            }
        }
        Row(Modifier.fillMaxSize().shadow(if(r.isNow||r.isNext)4.dp else 0.dp,RoundedCornerShape(8.dp),ambientColor=mark.copy(alpha=.30f),spotColor=mark.copy(alpha=.30f)).clip(RoundedCornerShape(8.dp)).background(paint)
            .border(.8.dp,if(r.isNow||r.isNext)mark.copy(alpha=.9f)else c.accent.copy(alpha=.35f),RoundedCornerShape(8.dp))
            .testTag("compact-prayer-${r.prayer.name}").combinedClickable(onClick=a.onToggleRelative,onLongClick=onWhy,role=Role.Button)
            .semantics(mergeDescendants=true){contentDescription=r.spoken},verticalAlignment=Alignment.CenterVertically) {
            if(!gallery)Spacer(Modifier.width(if(large)12.dp else 6.dp))
            if(thumbnail) {
                Box(Modifier.padding(start=4.dp,top=4.dp,bottom=4.dp).width(thumbnailSide).aspectRatio(1f)) {
                    PhoneLandscapePeriodArt(r.prayer.ordinal,Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp)))
                }
            }
            Row(Modifier.weight(1f).padding(start=4.dp+if(gallery)leadingExtra else leadingExtra*.52f),verticalAlignment=Alignment.Top) {
                Column(Modifier.width(labelWidth)) {
                    Text(r.label,Modifier.testTag("landscape-row-name-${r.prayer.name}"),fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,fontSize=labelSize,fontWeight=FontWeight.Normal,letterSpacing=0.sp,lineHeight=if(urdu)25.sp else if(large&&!gallery)20.sp else 17.sp,color=c.ink,maxLines=1)
                    if(!urdu)Text(r.arabic,Modifier.testTag("landscape-row-arabic-${r.prayer.name}"),style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false)),fontFamily=Amiri,fontWeight=FontWeight.Bold,fontSize=arabicSize,letterSpacing=0.sp,lineHeight=if(large&&!gallery)22.sp else 21.sp,color=c.accent)
                }
                Column(Modifier.weight(1f).padding(start=nameGap),verticalArrangement=Arrangement.Top) {
                    // Landscape keeps the authored large clock even when relative details are enabled.
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(2.dp)) {
                        FittedLandscapeLine(clockText,Modifier.weight(1f,fill=false),if(urdu)Amiri else serif,uniformTimeSize,
                            ((if(urdu)25f else if(large&&!gallery)24f else 22f)*uniformTimeSize.value/timeSize.value).sp,c.ink,"landscape-row-time-${r.prayer.name}")
                        if(r.isNow||r.isNext)badge()else Spacer(Modifier.width(badgeWidth))
                    }
                    if(s.showRelative)FittedLandscapeLine(androidx.compose.ui.text.AnnotatedString(r.relative),Modifier.fillMaxWidth(),captionFont,
                        if(large&&!urdu)10.sp else 9.sp,if(urdu)18.sp else if(large)13.sp else 12.sp,c.secondary,"landscape-row-relative-${r.prayer.name}")
                    if(detailText.isNotEmpty())FittedLandscapeLine(androidx.compose.ui.text.AnnotatedString(detailText),Modifier.fillMaxWidth(),captionFont,
                        detailSize,if(urdu)16.sp else if(large)14.sp else 12.sp,c.secondary,"landscape-row-detail-${r.prayer.name}")
                }
            }
            Row(Modifier.width(48.dp).fillMaxHeight().heightIn(min=48.dp).testTag("landscape-info-${r.prayer.name}").clickable(role=Role.Button,onClick=onWhy)
                .semantics(mergeDescendants=true){contentDescription=Str[R.string.s_why_this_time]},horizontalArrangement=Arrangement.spacedBy(5.dp,Alignment.CenterHorizontally),verticalAlignment=Alignment.CenterVertically) {
                if(r.prayer.isPrayer)Icon(if(r.azaanOn)Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff,null,Modifier.size(if(large)21.dp else 16.dp)
                    .semantics{contentDescription=if(r.azaanOn)L10n.t("Azaan enabled","اذان فعال ہے")else L10n.t("Azaan off","اذان بند ہے")},tint=c.accent)
                Icon(Icons.Outlined.Info,null,Modifier.size(if(large)21.dp else 15.dp),tint=c.secondary)
            }
        }
    }
}

