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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                Box(Modifier.fillMaxWidth().height(if(LocalConfiguration.current.screenHeightDp>=390)52.dp else 40.dp).testTag("landscape-timeline-panel")) {ApprovedTimeline(state,s,tk,Modifier.fillMaxSize())}
                val qibla=if(s.showQibla)links.firstOrNull()else null
                val adhkar=links.firstOrNull{it.kind==DoorKind.ADHKAR}
                val warning=links.firstOrNull{it.warn}
                Row(Modifier.fillMaxWidth().padding(end=44.dp).heightIn(min=48.dp).testTag("landscape-context-actions"),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                    if(qibla!=null)LandscapeHomeDoor(qibla.label,Icons.Outlined.Explore,s,Modifier.weight(.25f).testTag("landscape-door-0"),onClick={qibla.onClick?.invoke()})
                    LandscapeHomeDoor(adhkar?.label?:L10n.t("Adhkar","اذکار"),Icons.Outlined.MenuBook,s,Modifier.weight(.24f).testTag("landscape-door-1"),onClick={if(adhkar!=null)adhkar.onClick?.invoke()else a.onOpenAdhkar(AdhkarMode.MORNING)})
                    Spacer(Modifier.weight(.13f))
                    Box(Modifier.weight(.38f)) {
                        LandscapeHomeDoor(if(warning!=null)L10n.t("Alerts need attention · Fix","انتباہات پر توجہ دیں · درست کریں")else L10n.t("More actions","مزید اختیارات"),
                            if(warning!=null)Icons.Outlined.WarningAmber else Icons.Outlined.MoreHoriz,s,
                            Modifier.fillMaxWidth().testTag("landscape-more-actions").semantics{contentDescription=warning?.label?:L10n.word(s,"More actions")},warn=warning!=null,onClick={more=true})
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
        Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically) {
            Row(Modifier.weight(.27f).fillMaxHeight().clickable(role=Role.Button,onClick=a.onOpenLocation),verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn,null,Modifier.size(20.dp),tint=c.accent)
                Text(L10n.place(s.locationName),Modifier.weight(1f).padding(start=5.dp).testTag("landscape-location"),fontFamily=f,fontSize=12.sp,lineHeight=if(urdu)22.sp else 17.sp,color=c.ink,maxLines=2)
            }
            Row(Modifier.weight(.52f),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                Text(L10n.dateShort(s,state.now)+" "+state.now.year,Modifier.weight(1f).testTag("landscape-date"),fontFamily=f,fontSize=10.sp,lineHeight=if(urdu)20.sp else 15.sp,color=c.ink,textAlign=TextAlign.Center)
                if(s.showHijri) {
                    Box(Modifier.padding(horizontal=5.dp).width(.5.dp).height(16.dp).background(c.accent.copy(alpha=.45f)))
                    Text(galleryHijriLine(s,PrayerEngine.hijri(state.now.toLocalDate(),s.hijriOffsetDays)),Modifier.weight(1.35f).testTag("landscape-hijri"),fontFamily=f,fontSize=10.sp,lineHeight=if(urdu)20.sp else 15.sp,color=c.ink,textAlign=TextAlign.Center)
                }
            }
            Row(Modifier.weight(.21f).padding(horizontal=4.dp).clip(RoundedCornerShape(16.dp)).background(c.panel)
                .border(.6.dp,c.accent.copy(alpha=.45f),RoundedCornerShape(16.dp)).padding(horizontal=6.dp,vertical=5.dp),
                horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                Text(L10n.word(s,"NOW"),fontFamily=if(urdu)Amiri else ApprovedBody,fontSize=8.sp,lineHeight=if(urdu)19.sp else 13.sp,color=c.accent)
                Text(" "+L10n.iso(L10n.at(state.now,s.use24h)),Modifier.testTag("landscape-live-clock"),fontFamily=f,fontSize=11.sp,lineHeight=if(urdu)21.sp else 15.sp,color=c.ink)
            }
            Row(Modifier.clip(RoundedCornerShape(18.dp)).background(c.panel.copy(alpha=.55f)).border(.5.dp,c.accent.copy(alpha=.2f),RoundedCornerShape(18.dp))) {
            if(s.kidsMode)PhoneHomeNav(Icons.Outlined.MenuBook,Str[R.string.s_learn_salah],if(urdu)Str[R.string.s_theme_home_nav_learn]else"Learn Salah",s,a.onOpenLearn)
            PhoneHomeNav(Icons.Outlined.CalendarMonth,Str[R.string.s_monthly_timetable],Str[R.string.s_theme_home_nav_timetable],s,a.onOpenTimetable)
            PhoneHomeNav(Icons.Outlined.Settings,Str[R.string.s_settings],Str[R.string.s_theme_home_nav_settings],s,a.onOpenSettings)
            }
        }
    }
}

@Composable
private fun LandscapeHomeHero(hero:HeroInfo,state:PrayerState,s:AppSettings,modifier:Modifier,onToggle:()->Unit,onDetails:()->Unit) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val gallery=s.theme==AppTheme.PRAYER_GALLERY
    BoxWithConstraints(modifier.testTag("landscape-authored-hero").padding(end=if(gallery)40.dp else 0.dp),contentAlignment=Alignment.Center) {
        val heroStyle=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false),shadow=if(gallery)null else Shadow(Color.Black.copy(alpha=.85f),Offset(0f,1.5f),4f))
        val paired=maxHeight<if(urdu)215.dp else if(s.theme==AppTheme.KISWAH)172.dp else 190.dp
        val compact=maxHeight<215.dp
        val tiny=maxHeight<175.dp
        val compactKiswah=s.theme==AppTheme.KISWAH&&compact
        val name:@Composable (Modifier)->Unit={m->Column(m,horizontalAlignment=Alignment.CenterHorizontally) {
            Text(hero.arabic,Modifier.fillMaxWidth().testTag("landscape-hero-name"),style=heroStyle.copy(textGeometricTransform=TextGeometricTransform(scaleX=1.35f)),fontFamily=Amiri,fontWeight=FontWeight.Bold,fontSize=if(s.theme==AppTheme.KISWAH)if(tiny)24.sp else if(compact)26.sp else 34.sp else if(compact)30.sp else if(s.iqamahEnabled)34.sp else 36.sp,lineHeight=if(s.theme==AppTheme.KISWAH)if(tiny)30.sp else if(compact)34.sp else 44.sp else if(compact)40.sp else 46.sp,color=c.accent,textAlign=TextAlign.Center)
            Text(if(urdu)hero.label else hero.label.uppercase(Locale.ROOT),Modifier.testTag("landscape-hero-english"),style=heroStyle,fontFamily=if(urdu)Amiri else Cinzel,fontSize=9.sp,lineHeight=if(urdu)21.sp else 12.sp,letterSpacing=if(urdu)0.sp else 1.3.sp,color=if(gallery)c.accent else c.ink,textAlign=TextAlign.Center)
        }}
        val heroClock=L10n.iso(L10n.at(state.heroTime,s.use24h))
        val clock:@Composable (Modifier)->Unit={m->Text(buildAnnotatedString {
            append(heroClock)
            if(hero.suffix.isNotBlank()) {val start=heroClock.indexOf(hero.suffix);if(start>=0)addStyle(SpanStyle(fontSize=14.sp,baselineShift=BaselineShift(.2f)),start,start+hero.suffix.length)}
        },m.testTag("landscape-hero-clock").clickable(role=Role.Button,onClick=onToggle),style=heroStyle,fontFamily=if(urdu)Amiri else ApprovedDisplay,fontWeight=FontWeight.Bold,
            fontSize=if(urdu)25.sp else if(compactKiswah)if(tiny)38.sp else 43.sp else if(compact)40.sp else 56.sp,lineHeight=if(urdu)if(paired)38.sp else 45.sp else if(compactKiswah)if(tiny)42.sp else 50.sp else if(compact)49.sp else 59.sp,letterSpacing=if(urdu)0.sp else 1.4.sp,color=if(gallery)c.accent else c.ink,textAlign=TextAlign.Center)}
        val galleryExtraInset=if(gallery&&maxWidth>=250.dp)20.dp else 0.dp
        Box(Modifier.fillMaxSize().padding(end=galleryExtraInset),contentAlignment=Alignment.Center) {
        Column(Modifier.widthIn(max=244.dp).fillMaxWidth().fillMaxHeight(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            val kicker:@Composable (Modifier)->Unit={m->Text(hero.special?:if(urdu)hero.kickerLabel else hero.kickerLabel.uppercase(Locale.ROOT),m.testTag("landscape-hero-kicker"),style=heroStyle,fontFamily=if(urdu)Amiri else Cinzel,fontWeight=FontWeight.Bold,fontSize=if(hero.special!=null)9.sp else if(compactKiswah)11.sp else 12.sp,lineHeight=if(urdu)20.sp else if(compactKiswah)14.sp else 17.sp,letterSpacing=if(urdu)0.sp else 2.sp,color=c.accent,textAlign=TextAlign.Center)}
            val status:@Composable (Modifier)->Unit={m->Row(m,horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule,null,Modifier.size(14.dp),tint=c.accent)
                Text(hero.status,Modifier.weight(1f,fill=false).padding(start=4.dp).testTag("landscape-hero-status"),style=heroStyle,fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,fontSize=12.sp,lineHeight=if(urdu)22.sp else if(compactKiswah)14.sp else 17.sp,color=if(gallery)c.accent else c.ink,textAlign=TextAlign.Center)
            }}
            if(paired)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){kicker(Modifier.weight(.35f));status(Modifier.weight(.65f))}else kicker(Modifier.fillMaxWidth())
            if(paired)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){name(Modifier.weight(.43f));clock(Modifier.weight(.57f))}
            else {name(Modifier.fillMaxWidth());clock(Modifier.fillMaxWidth());status(Modifier.fillMaxWidth())}
            LandscapeHomeDoor(Str[R.string.s_view_prayer_details],null,s,Modifier.fillMaxWidth(.78f).testTag("landscape-hero-details"),solid=gallery,onClick=onDetails)
        }
        }
    }
}

/** The visual pill is shallow; its independent 48dp target never overlaps a neighbour. */
@Composable
private fun LandscapeHomeDoor(label:String,icon:androidx.compose.ui.graphics.vector.ImageVector?,s:AppSettings,modifier:Modifier=Modifier,warn:Boolean=false,solid:Boolean=false,onClick:()->Unit) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val outline=if(warn&&s.theme==AppTheme.PRAYER_GALLERY)Color(0xFF83520D)else if(warn)Color(0xFFE9B85B)else c.accent
    Box(modifier.heightIn(min=48.dp).clickable(role=Role.Button,onClick=onClick).semantics(mergeDescendants=true){},contentAlignment=Alignment.Center) {
        Row(Modifier.fillMaxWidth().heightIn(min=if(LocalConfiguration.current.screenHeightDp>=390)34.dp else 30.dp).clip(RoundedCornerShape(16.dp))
            .background(if(solid)c.accent else if(warn&&s.theme==AppTheme.PRAYER_GALLERY)Color(0xFFFFF3DF).copy(alpha=.97f)else if(s.theme!=AppTheme.PRAYER_GALLERY)(if(s.theme==AppTheme.KISWAH)Color(0xFF10100F)else Color(0xFF061C35)).copy(alpha=if(warn).90f else .84f)else c.panel)
            .border(.8.dp,outline.copy(alpha=if(warn).85f else .65f),RoundedCornerShape(16.dp)).padding(horizontal=8.dp,vertical=2.dp),
            verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
            if(icon!=null)Icon(icon,null,Modifier.size(20.dp),tint=if(solid)Color.White else outline)
            Text(label,Modifier.padding(horizontal=5.dp),fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,fontSize=if(urdu)13.sp else 15.sp,
                lineHeight=if(urdu)23.sp else 20.sp,color=if(solid)Color(0xFFFFF9ED)else if(warn)outline else c.ink,textAlign=TextAlign.Center)
            if(icon==null||warn)Icon(Icons.AutoMirrored.Outlined.ArrowForward,null,Modifier.size(15.dp),tint=if(solid)Color.White else outline)
        }
    }
}

@Composable
private fun PhoneHomeNav(icon: androidx.compose.ui.graphics.vector.ImageVector, label:String, short:String, s:AppSettings,onClick:()->Unit) {
    Column(Modifier.width(58.dp).height(48.dp).clip(RoundedCornerShape(10.dp)).clickable(role=Role.Button,onClick=onClick)
        .semantics(mergeDescendants=true){contentDescription=label},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Icon(icon,null,Modifier.size(21.dp),tint=quietPalette(s.theme).ink)
        Text(short,fontFamily=if(L10n.isUrdu(s))Amiri else ApprovedBody,fontSize=9.sp,lineHeight=if(L10n.isUrdu(s))18.sp else 13.sp,color=quietPalette(s.theme).ink)
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
    val mark=if(r.isNow)Color(0xFF43DADE)else c.accent
    val fill=when {r.isNow->if(gallery)Color(0xFFE6F3ED)else Color(0xFF074148).copy(alpha=.88f)
        r.isNext->if(gallery)Color(0xFFFFECD9)else Color(0xFF65491D).copy(alpha=.85f)
        else->if(gallery)Color(0xFFFFF9EE).copy(alpha=.85f)else if(s.theme==AppTheme.KISWAH)Color(0xFF090909).copy(alpha=.8f)else Color(0xFF03162C).copy(alpha=.65f)}
    val paint=if(gallery)Brush.horizontalGradient(listOf(fill,fill.copy(alpha=.94f))) else when {
        r.isNow->Brush.horizontalGradient(listOf(Color(0xFF07505A).copy(alpha=.70f),Color(0xFF032E37).copy(alpha=.62f)))
        r.isNext->Brush.horizontalGradient(listOf(Color(0xFF856326).copy(alpha=.54f),Color(0xFF222526).copy(alpha=.68f)))
        s.theme==AppTheme.KISWAH->Brush.horizontalGradient(listOf(Color(0xFF090909).copy(alpha=.82f),Color(0xFF10100F).copy(alpha=.58f)))
        else->Brush.horizontalGradient(listOf(Color(0xFF041A34).copy(alpha=.70f),Color(0xFF03142A).copy(alpha=.46f)))
    }
    BoxWithConstraints(modifier.heightIn(min=48.dp)) {
        val tight=maxWidth<260.dp
        val large=maxWidth>=240.dp
        val tinyCard=maxWidth<225.dp
        val narrow=maxWidth<215.dp
        val thumbnail=gallery&&s.artTheme!=ArtTheme.MINIMAL&&!(tight&&urdu)
        val thumbnailSide=if(tight)if(narrow)22.dp else if(maxWidth>=230.dp)40.dp else 30.dp else 58.dp
        val badge:@Composable ()->Unit={
            Box(Modifier.width(if(urdu)30.dp else 24.dp).clip(RoundedCornerShape(10.dp))
                .background(if(r.isNow)Color(0xFF34D6DA)else Color(0xFFFFD56E)),contentAlignment=Alignment.Center) {
                Text(L10n.word(s,if(r.isNow)"NOW"else"NEXT"),Modifier.padding(horizontal=2.dp,vertical=2.dp).testTag(if(gallery)if(r.isNow)"gallery-status-now"else"gallery-status-next"else if(r.isNow)"home-status-now"else"home-status-next"),
                    fontFamily=if(urdu)Amiri else ApprovedBody,fontWeight=FontWeight.Bold,fontSize=7.sp,lineHeight=if(urdu)18.sp else 11.sp,color=Color(0xFF112F32),maxLines=1)
            }
        }
        Row(Modifier.fillMaxSize().shadow(if(r.isNow||r.isNext)4.dp else 0.dp,RoundedCornerShape(8.dp),ambientColor=mark.copy(alpha=.30f),spotColor=mark.copy(alpha=.30f)).clip(RoundedCornerShape(8.dp)).background(paint)
            .border(.8.dp,if(r.isNow||r.isNext)mark.copy(alpha=.9f)else c.accent.copy(alpha=.35f),RoundedCornerShape(8.dp))
            .testTag("compact-prayer-${r.prayer.name}").combinedClickable(onClick=a.onToggleRelative,onLongClick=onWhy,role=Role.Button)
            .semantics(mergeDescendants=true){contentDescription=r.spoken},verticalAlignment=Alignment.CenterVertically) {
            if(thumbnail) {
                Box(Modifier.padding(start=4.dp,top=4.dp,bottom=4.dp).width(thumbnailSide).aspectRatio(1f)) {
                    PhoneLandscapePeriodArt(r.prayer.ordinal,Modifier.fillMaxSize().clip(RoundedCornerShape(5.dp)))
                }
            } else if(!tight||!urdu)Icon(Icons.Outlined.Schedule,null,Modifier.padding(start=4.dp,end=3.dp).size(13.dp),tint=c.secondary)
            Row(Modifier.weight(1f).padding(start=4.dp),verticalAlignment=Alignment.Top) {
                Column(Modifier.width(if(urdu)58.dp else if(large&&!gallery)67.dp else if(large&&gallery)59.dp else if(tight)50.dp else 54.dp)) {
                    Text(r.label,Modifier.testTag("landscape-row-name-${r.prayer.name}"),fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,fontSize=if(urdu)14.sp else if(large&&!gallery)15.sp else if(large)13.sp else if(tight)12.sp else 13.sp,lineHeight=if(urdu)25.sp else if(large&&!gallery)20.sp else 17.sp,color=c.ink,maxLines=1)
                    if(!urdu)Text(r.arabic,Modifier.testTag("landscape-row-arabic-${r.prayer.name}"),style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false)),fontFamily=Amiri,fontSize=if(large&&!gallery)16.sp else 14.sp,lineHeight=if(large&&!gallery)22.sp else 21.sp,color=c.accent)
                }
                Column(Modifier.weight(1f).padding(start=2.dp),verticalArrangement=Arrangement.Top) {
                    // Landscape keeps the authored large clock even when relative details are enabled.
                    val raw=L10n.iso((r.clock+" "+r.suffix).trim())
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(2.dp)) {
                        Text(buildAnnotatedString{append(raw);if(r.suffix.isNotBlank()){val start=raw.indexOf(r.suffix);if(start>=0)addStyle(SpanStyle(fontSize=if(large)11.sp else 9.sp,baselineShift=BaselineShift(.15f)),start,start+r.suffix.length)}},
                            Modifier.weight(1f,fill=false).testTag("landscape-row-time-${r.prayer.name}"),fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,
                            fontSize=if(urdu)18.sp else if(large&&!gallery)23.sp else if(large&&gallery)17.sp else if(thumbnail&&narrow&&!urdu)12.sp else if(tinyCard)14.sp else if(gallery&&tight)16.sp else 18.sp,lineHeight=if(urdu)25.sp else if(large&&!gallery)28.sp else 22.sp,color=c.ink,maxLines=1)
                        if(r.isNow||r.isNext)badge()
                    }
                    if(s.showRelative)Text(r.relative,Modifier.testTag("landscape-row-relative-${r.prayer.name}"),fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,fontSize=9.sp,lineHeight=if(urdu)18.sp else 12.sp,color=c.secondary)
                    if(r.small.isNotEmpty())Text(r.small,Modifier.testTag("landscape-row-detail-${r.prayer.name}"),fontFamily=if(urdu)Amiri else androidx.compose.ui.text.font.FontFamily.Serif,fontSize=if(gallery&&!large&&!urdu)8.sp else 9.sp,lineHeight=if(urdu)16.sp else 12.sp,color=c.secondary)
                }
            }
            Row(Modifier.width(48.dp).fillMaxHeight().heightIn(min=48.dp).testTag("landscape-info-${r.prayer.name}").clickable(role=Role.Button,onClick=onWhy)
                .semantics(mergeDescendants=true){contentDescription=Str[R.string.s_why_this_time]},horizontalArrangement=Arrangement.spacedBy(5.dp,Alignment.CenterHorizontally),verticalAlignment=Alignment.CenterVertically) {
                if(r.prayer.isPrayer)Icon(if(r.azaanOn)Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff,null,Modifier.size(16.dp)
                    .semantics{contentDescription=if(r.azaanOn)L10n.t("Azaan enabled","اذان فعال ہے")else L10n.t("Azaan off","اذان بند ہے")},tint=c.accent)
                Icon(Icons.Outlined.Info,null,Modifier.size(15.dp),tint=c.secondary)
            }
        }
    }
}
