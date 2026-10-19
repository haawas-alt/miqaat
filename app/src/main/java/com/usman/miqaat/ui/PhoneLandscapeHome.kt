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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.data.*
import java.util.Locale

@Composable
internal fun isPhoneLandscape(): Boolean {
    val c = LocalConfiguration.current
    return c.orientation == Configuration.ORIENTATION_LANDSCAPE && c.screenHeightDp < 500
}

/** Six prayers in reading order, with unchanged relative-time and information actions. */
@Composable
internal fun PhoneLandscapeHome(state: PrayerState, s: AppSettings, a: HomeActions) {
    val tk = ThemeTokenSets.of(s.theme)
    val colors = quietPalette(s.theme)
    val urdu = L10n.isUrdu(s)
    val font = if (urdu) Amiri else ApprovedBody
    val hero = heroInfo(state, s, kicker(state, s))
    val links = doors(state, s, a)
    val seasonal = listOfNotNull(hero.special, fastProgress(state, s)?.second, tarawihLine(state, s))
    var why by remember { mutableStateOf<Prayer?>(null) }
    var more by remember { mutableStateOf(false) }
    why?.let { p ->
        val day = if (p == state.next && state.nextTime.toLocalDate().isAfter(state.today.date)) PrayerEngine.times(s, state.nextTime.toLocalDate()) else state.today
        WhyDialog(s, day, p) { why = null }
    }
    Box(Modifier.fillMaxSize().background(colors.background).testTag("landscape-fit-board")) {
        PrayerFlowBackdrop(s, true, Modifier.matchParentSize())
        Box(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 12.dp, vertical = 2.dp)) {
            // Extra per-prayer Iqamah/relative detail remains readable through explicit reflow on a short phone.
            val reflow = s.iqamahEnabled || s.showRelative
            Column(Modifier.fillMaxSize().then(if (reflow) Modifier.verticalScroll(rememberScrollState()).testTag("landscape-home-reflow") else Modifier)) {
                Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(.26f).fillMaxHeight().clickable(role = Role.Button, onClick = a.onOpenLocation), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, null, Modifier.size(18.dp), tint = colors.accent)
                        Text(L10n.place(s.locationName), Modifier.weight(1f).padding(start = 4.dp).testTag("landscape-location"), fontFamily = font,
                            fontSize = 12.sp, lineHeight = if (urdu) 22.sp else 16.sp, color = colors.ink)
                    }
                    Column(Modifier.weight(.42f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(L10n.date(s, state.now), Modifier.testTag("landscape-date"), fontFamily = font, fontSize = 10.sp,
                            lineHeight = if (urdu) 19.sp else 14.sp, color = colors.ink, textAlign = TextAlign.Center)
                        if (s.showHijri) Text(galleryHijriLine(s, PrayerEngine.hijri(state.now.toLocalDate(), s.hijriOffsetDays)),
                            Modifier.testTag("landscape-hijri"), fontFamily = font, fontSize = 10.sp, lineHeight = if (urdu) 19.sp else 14.sp,
                            color = colors.secondary, textAlign = TextAlign.Center)
                    }
                    Column(Modifier.weight(.16f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(L10n.word(s,"NOW"), fontFamily = font, fontSize = 8.sp, lineHeight = 12.sp, color = colors.accent)
                        Text(L10n.iso(L10n.at(state.now,s.use24h)), Modifier.testTag("landscape-live-clock"), fontFamily = font,
                            fontSize = 10.sp, lineHeight = if (urdu) 20.sp else 15.sp, color = colors.ink, textAlign = TextAlign.Center)
                    }
                    if (s.kidsMode) PhoneHomeNav(Icons.Outlined.MenuBook, Str[R.string.s_learn_salah], Str[R.string.s_theme_home_nav_learn], s, a.onOpenLearn)
                    PhoneHomeNav(Icons.Outlined.CalendarMonth, Str[R.string.s_monthly_timetable], Str[R.string.s_theme_home_nav_timetable], s, a.onOpenTimetable)
                    PhoneHomeNav(Icons.Outlined.Settings, Str[R.string.s_settings], Str[R.string.s_theme_home_nav_settings], s, a.onOpenSettings)
                }
                if (s.theme == AppTheme.KISWAH) PhoneLandscapeBand()
                Row(Modifier.fillMaxWidth().then(if (reflow) Modifier.height(if (urdu) 294.dp else 258.dp) else Modifier.weight(1f)),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(.60f).fillMaxHeight().testTag("home-prayers"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listedPrayers(s).chunked(2).forEach { row ->
                            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                row.forEach { p -> PhoneLandscapePrayer(galleryRowInfo(p,state,s),s,a,Modifier.weight(1f).fillMaxHeight()) { why=p } }
                                if (row.size==1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    BoxWithConstraints(Modifier.weight(.40f).fillMaxHeight().testTag("landscape-authored-hero")) {
                        val tight = maxHeight < 235.dp
                        Column(Modifier.fillMaxSize().padding(horizontal=4.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.SpaceEvenly) {
                            Text(hero.special ?: hero.kickerLabel, Modifier.fillMaxWidth().testTag("landscape-hero-kicker"), fontFamily=font,
                                fontSize=10.sp,lineHeight=if(urdu) 20.sp else 14.sp,color=colors.accent,textAlign=TextAlign.Center)
                            val name: @Composable (Modifier)->Unit = { m -> Column(m,horizontalAlignment=Alignment.CenterHorizontally) {
                                Text(hero.arabic,Modifier.fillMaxWidth().testTag("landscape-hero-name"),fontFamily=if(s.theme==AppTheme.KISWAH) ReemKufi else Amiri,
                                    fontSize=if(tight) 30.sp else 38.sp,lineHeight=if(tight) 44.sp else 56.sp,color=colors.accent,textAlign=TextAlign.Center)
                                Text(if(urdu) hero.label else hero.label.uppercase(Locale.ROOT),Modifier.fillMaxWidth().testTag("landscape-hero-english"),
                                    fontFamily=if(urdu) Amiri else Cinzel,fontSize=11.sp,lineHeight=if(urdu) 22.sp else 16.sp,color=colors.ink,textAlign=TextAlign.Center)
                            } }
                            val time: @Composable (Modifier)->Unit = { m -> QuietTime(state.heroTime,s,if(urdu)23f else if(tight)42f else 56f,
                                colors.ink,m.testTag("landscape-hero-clock").clickable(role=Role.Button,onClick=a.onToggleRelative)) }
                            if(tight) Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { name(Modifier.weight(1f));time(Modifier.weight(1f)) }
                            else {name(Modifier.fillMaxWidth());time(Modifier.fillMaxWidth())}
                            Text(hero.status,Modifier.fillMaxWidth().testTag("landscape-hero-status"),fontFamily=font,fontSize=11.sp,
                                lineHeight=if(urdu) 22.sp else 16.sp,color=colors.ink,textAlign=TextAlign.Center)
                            PhoneHomeButton(Str[R.string.s_view_prayer_details],s,Modifier.fillMaxWidth().testTag("landscape-hero-details")) { why=hero.prayer }
                            if (seasonal.isNotEmpty()) Text(seasonal.first(),Modifier.fillMaxWidth().testTag("landscape-seasonal"),fontFamily=font,
                                fontSize=10.sp,lineHeight=if(urdu) 20.sp else 15.sp,color=colors.secondary,textAlign=TextAlign.Center)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(8.dp)).background(colors.panel).testTag("landscape-timeline-panel")) { ApprovedTimeline(state,s,tk,Modifier.fillMaxSize()) }
                val visible = (links.filter{!it.warn&&it.kind!=DoorKind.ADHKAR}.take(1)+links.filter{it.kind==DoorKind.ADHKAR}.take(1)+links.filter{it.warn}.take(1)).distinct()
                Row(Modifier.fillMaxWidth().heightIn(min=48.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
                    visible.forEachIndexed { index,d ->
                        Row(Modifier.weight(1f).heightIn(min=48.dp).clip(RoundedCornerShape(24.dp)).background(colors.panel)
                            .border(.7.dp,if(d.warn) tk.warning else colors.accent.copy(alpha=.32f),RoundedCornerShape(24.dp))
                            .then(if(d.onClick!=null) Modifier.clickable(role=Role.Button,onClick=d.onClick) else Modifier)
                            .testTag("landscape-door-$index").semantics(mergeDescendants=true){contentDescription=d.label}
                            .padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
                            Icon(if(d.warn) Icons.Outlined.WarningAmber else if(d.kind==DoorKind.ADHKAR) Icons.Outlined.MenuBook else Icons.Outlined.Explore,
                                null,Modifier.size(17.dp),tint=if(d.warn)tk.warning else colors.accent)
                            Text(d.label,Modifier.weight(1f).padding(start=5.dp),fontFamily=font,fontSize=11.sp,
                                lineHeight=if(urdu) 20.sp else 16.sp,color=colors.ink,textAlign=TextAlign.Center)
                        }
                    }
                    Box {
                        IconButton({more=true},Modifier.size(48.dp).testTag("landscape-more-actions")) {Icon(Icons.Outlined.MoreHoriz,L10n.word(s,"More actions"),tint=colors.ink)}
                        DropdownMenu(more,{more=false}) {
                            links.forEach { d -> DropdownMenuItem(text={Text(d.label)},onClick={more=false;d.onClick?.invoke()}) }
                            DropdownMenuItem(text={Text(L10n.t("Adhkar","اذکار"))},onClick={more=false;a.onOpenAdhkar(AdhkarMode.MORNING)})
                            if(s.kidsMode) DropdownMenuItem(text={Text(if(urdu) "نماز کی تیاری" else "Prepare for ${hero.label}")},onClick={more=false;a.onPreparePrayer(hero.prayer)})
                            seasonal.forEach { line -> DropdownMenuItem(text={Text(line)},onClick={},enabled=false) }
                        }
                    }
                }
            }
        }
        if(s.nightDim&&state.period==Prayer.ISHA&&!state.justPassed) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.22f)))
    }
}

@Composable
private fun PhoneHomeNav(icon: androidx.compose.ui.graphics.vector.ImageVector, label:String, short:String, s:AppSettings,onClick:()->Unit) {
    Column(Modifier.size(48.dp).clip(RoundedCornerShape(10.dp)).clickable(role=Role.Button,onClick=onClick)
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
    val mark=if(r.isNow) (if(gallery) Color(0xFF087C77) else Color(0xFF45DAE3)) else c.accent
    val fill=when {r.isNow->if(gallery)Color(0xFFEAF4EE) else Color(0xFF07303B).copy(alpha=.92f)
        r.isNext->if(gallery)Color(0xFFFFEBDD) else Color(0xFF30291D).copy(alpha=.90f)
        else->c.panel}
    Row(modifier.heightIn(min=48.dp).clip(RoundedCornerShape(9.dp)).background(fill)
        .border(.7.dp,if(r.isNow||r.isNext)mark.copy(alpha=.75f)else c.accent.copy(alpha=.22f),RoundedCornerShape(9.dp))
        .testTag("compact-prayer-${r.prayer.name}").combinedClickable(onClick=a.onToggleRelative,onLongClick=onWhy,role=Role.Button)
        .semantics(mergeDescendants=true){contentDescription=r.spoken},verticalAlignment=Alignment.CenterVertically) {
        if(gallery&&s.artTheme!=ArtTheme.MINIMAL) ApprovedGalleryArt(r.prayer.ordinal,Modifier.width(24.dp).fillMaxHeight())
        Column(Modifier.weight(1f).padding(horizontal=5.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Text(r.label,Modifier.weight(1f).testTag("landscape-row-name-${r.prayer.name}"),fontFamily=if(urdu)Amiri else ApprovedBody,
                    fontSize=if(urdu)14.sp else 11.sp,lineHeight=if(urdu)24.sp else 14.sp,color=c.ink)
                val raw = if(s.showRelative) r.relative else L10n.iso((r.clock+" "+r.suffix).trim())
                val clockLabel = buildAnnotatedString {
                    append(raw)
                    if(!s.showRelative&&r.suffix.isNotBlank()) {
                        val start=raw.indexOf(r.suffix)
                        if(start>=0)addStyle(SpanStyle(fontSize=9.sp),start,start+r.suffix.length)
                    }
                }
                Text(clockLabel,Modifier.weight(1.15f).testTag("landscape-row-time-${r.prayer.name}"),
                    fontFamily=if(s.showRelative) (if(urdu)Amiri else ApprovedBody) else ApprovedDisplay,fontSize=if(s.showRelative)11.sp else 18.sp,
                    lineHeight=if(urdu&&s.showRelative)22.sp else 22.sp,color=c.ink,textAlign=TextAlign.End)
            }
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                if(!urdu) Text(r.arabic,Modifier.testTag("landscape-row-arabic-${r.prayer.name}"),fontFamily=Amiri,fontSize=14.sp,lineHeight=20.sp,color=c.accent)
                if(r.small.isNotEmpty()) Text(r.small,Modifier.weight(1f).testTag("landscape-row-detail-${r.prayer.name}"),
                    fontFamily=if(urdu)Amiri else ApprovedBody,fontSize=9.sp,lineHeight=if(urdu)17.sp else 13.sp,color=c.secondary,
                    textAlign=if(urdu)TextAlign.Start else TextAlign.End)
            }
        }
        Column(Modifier.width(48.dp).fillMaxHeight().heightIn(min=48.dp).testTag("landscape-info-${r.prayer.name}")
            .clip(RoundedCornerShape(8.dp)).clickable(role=Role.Button,onClick=onWhy)
            .semantics(mergeDescendants=true){contentDescription=Str[R.string.s_why_this_time]},
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            if(r.isNow||r.isNext) {
                if(gallery) GalleryStatusBadge(r.isNow,s,compact=true)
                else Text(L10n.word(s,if(r.isNow)"NOW" else "NEXT"),Modifier.testTag(if(r.isNow)"home-status-now" else "home-status-next"),
                    fontFamily=if(urdu)Amiri else ApprovedBody,fontSize=9.sp,lineHeight=if(urdu)18.sp else 12.sp,color=mark,fontWeight=FontWeight.Bold)
            }
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                if(r.prayer.isPrayer) Icon(if(r.azaanOn)Icons.Outlined.NotificationsNone else Icons.Outlined.NotificationsOff,null,Modifier.size(13.dp)
                    .semantics{contentDescription=if(r.azaanOn)L10n.t("Azaan enabled","اذان فعال ہے")else L10n.t("Azaan off","اذان بند ہے")},tint=c.accent)
                Icon(Icons.Outlined.Info,null,Modifier.size(15.dp),tint=c.secondary)
            }
        }
    }
}
