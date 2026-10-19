package com.usman.miqaat.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.usman.miqaat.R
import com.usman.miqaat.azaan.AzaanService.Phase
import com.usman.miqaat.data.*
import java.time.Instant
import java.time.ZonedDateTime
import java.util.Locale

/** A shallow full-width band reserves space for the complete original text on a short phone. */
@Composable
internal fun PhoneLandscapeBand() {
    // Regular landscape keeps 12dp around the native Amiri glyphs. The shortest phones
    // retain 6dp clearance and all six 48dp prayer cards rather than clipping either.
    val clearance = if (LocalConfiguration.current.screenHeightDp < 390) 6.dp else 12.dp
    Box(Modifier.fillMaxWidth().padding(vertical=2.dp).testTag("kiswah-calligraphy-frame").border(.8.dp,Kiswah.thread).background(Color.Black.copy(alpha=.70f)),contentAlignment=Alignment.Center) {
        Text(Kiswah.BAND,Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=clearance).testTag("kiswah-calligraphy-band")
            .semantics{contentDescription=Kiswah.BAND},fontFamily=Amiri,fontSize=14.sp,lineHeight=22.sp,color=Kiswah.threadSoft,textAlign=TextAlign.Center)
    }
}

@Composable
private fun PhoneFlowHeader(s:AppSettings,now:ZonedDateTime,prefix:String,stages:Boolean=false) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val font=if(urdu)Amiri else QuietBody
    Row(Modifier.fillMaxWidth().height(56.dp),verticalAlignment=Alignment.CenterVertically) {
        Text("Miqaat",Modifier.weight(.21f).testTag("$prefix-brand"),fontFamily=QuietDisplay,fontSize=25.sp,lineHeight=33.sp,color=c.ink)
        Column(Modifier.weight(.55f),horizontalAlignment=Alignment.CenterHorizontally) {
            QuietLabel(Str[if(stages)R.string.s_iqamah else R.string.s_quiet_mode_title],urdu,c.accent,
                Modifier.testTag("$prefix-mode"),if(stages)9f else 11f)
            if(stages) {
                val labels=listOf(Str[R.string.s_azaan],Str[R.string.s_iqamah_countdown],Str[R.string.s_iqamah],Str[R.string.s_step_prayer])
                Row(Modifier.fillMaxWidth().testTag("iqamah-stages")) {
                    labels.forEachIndexed { index,label ->
                        Column(Modifier.weight(1f).semantics{if(index==1)selected=true},horizontalAlignment=Alignment.CenterHorizontally) {
                            Box(Modifier.fillMaxWidth().height(13.dp),contentAlignment=Alignment.Center) {
                                Box(Modifier.fillMaxWidth().height(1.dp).background(c.accent.copy(alpha=.35f)))
                                Box(Modifier.size(12.dp).clip(CircleShape).background(if(index<2)c.accent else c.background)
                                    .border(.8.dp,c.secondary.copy(alpha=.6f),CircleShape),contentAlignment=Alignment.Center) {
                                    if(index==0)Text("✓",fontSize=8.sp,lineHeight=10.sp,color=c.background)
                                }
                            }
                            Text(if(urdu)label else label.uppercase(Locale.ROOT),Modifier.fillMaxWidth().testTag("iqamah-stage-$index"),
                                fontFamily=font,fontSize=8.sp,lineHeight=if(urdu)17.sp else 12.sp,
                                color=if(index==1)c.accent else c.secondary,textAlign=TextAlign.Center)
                        }
                    }
                }
            }
        }
        Column(Modifier.weight(.24f),horizontalAlignment=Alignment.End) {
            Text(L10n.word(s,"NOW"),fontFamily=font,fontSize=8.sp,lineHeight=if(urdu)14.sp else 11.sp,color=c.secondary)
            Text(L10n.iso(L10n.at(now,s.use24h)),Modifier.testTag("$prefix-live-clock"),fontFamily=if(urdu)Amiri else QuietDisplay,
                fontSize=if(urdu)11.sp else 14.sp,lineHeight=if(urdu)22.sp else 19.sp,color=c.ink,textAlign=TextAlign.End)
            if(s.locationName.isNotBlank()) Text(L10n.iso(L10n.place(s.locationName)),Modifier.testTag("$prefix-location"),fontFamily=font,
                fontSize=8.sp,lineHeight=if(urdu)16.sp else 12.sp,color=c.secondary,textAlign=TextAlign.End)
        }
    }
    if(s.theme==AppTheme.KISWAH)PhoneLandscapeBand()
}

@Composable
internal fun PhoneLandscapeIqamahContent(p:Phase.IqamahCountdown,s:AppSettings,now:ZonedDateTime,onStop:()->Unit,onStart:()->Unit) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val target=Instant.ofEpochMilli(p.endsAt).atZone(s.zone())
    Box(Modifier.fillMaxSize().background(c.background).testTag("iqamah-board")) {
        PrayerFlowBackdrop(s,true,Modifier.matchParentSize())
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=20.dp,vertical=4.dp).testTag("phone-landscape-iqamah")) {
            PhoneFlowHeader(s,now,"iqamah",stages=true)
            Row(Modifier.weight(1f).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                IqamahDial(iqamahCounter(p,now.toInstant().toEpochMilli()),s,Modifier.weight(.44f).fillMaxHeight().testTag("iqamah-counter-panel"))
                Box(Modifier.width(1.dp).fillMaxHeight(.75f).background(c.accent.copy(alpha=.3f)))
                BoxWithConstraints(Modifier.weight(.56f).fillMaxHeight().padding(start=12.dp)) {
                    val paired=urdu||maxHeight<255.dp
                    val prayer:@Composable (Modifier)->Unit={m ->Column(m,horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(p.prayer.arabic,Modifier.fillMaxWidth().testTag("iqamah-prayer-arabic").semantics{heading()},fontFamily=if(s.theme==AppTheme.KISWAH)ReemKufi else Amiri,
                            fontSize=32.sp,lineHeight=46.sp,color=c.accent,textAlign=TextAlign.Center)
                        Text(if(urdu)L10n.prayer(s,p.prayer)else L10n.prayer(s,p.prayer).uppercase(Locale.ROOT),Modifier.fillMaxWidth().testTag("iqamah-prayer-name"),
                            fontFamily=if(urdu)Amiri else Cinzel,fontSize=11.sp,lineHeight=if(urdu)22.sp else 17.sp,color=c.ink,textAlign=TextAlign.Center)
                    }}
                    val schedule:@Composable (Modifier)->Unit={m ->Column(m,horizontalAlignment=Alignment.CenterHorizontally) {
                        QuietLabel(Str[R.string.s_iqamah_at],urdu,c.accent,Modifier.testTag("iqamah-at-label"),9f)
                        QuietTime(target,s,if(urdu)22f else 30f,c.ink,Modifier.fillMaxWidth().testTag("iqamah-at-time"))
                        if(target.toLocalDate().isAfter(now.withZoneSameInstant(s.zone()).toLocalDate()))
                            Text(Str[R.string.s_quiet_tomorrow],Modifier.testTag("iqamah-tomorrow"),fontFamily=if(urdu)Amiri else QuietBody,
                                fontSize=9.sp,lineHeight=if(urdu)18.sp else 13.sp,color=c.secondary)
                    }}
                    Column(Modifier.fillMaxSize().testTag("iqamah-details-panel"),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceEvenly) {
                        if(paired)Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {prayer(Modifier.weight(1f));schedule(Modifier.weight(1f))}
                        else {prayer(Modifier.fillMaxWidth());schedule(Modifier.fillMaxWidth())}
                        Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(Str[R.string.s_straighten_your_rows],Modifier.fillMaxWidth().testTag("iqamah-reminder"),fontFamily=Amiri,
                                fontSize=if(urdu)11.sp else 13.sp,lineHeight=if(urdu)20.sp else 23.sp,color=c.ink,textAlign=TextAlign.Center)
                            Text(Str[R.string.s_a_al_bukh_r_723],Modifier.fillMaxWidth().testTag("iqamah-source"),fontFamily=if(urdu)Amiri else QuietBody,
                                fontSize=9.sp,lineHeight=if(urdu)16.sp else 13.sp,color=c.secondary,textAlign=TextAlign.Center)
                        }
                        Text(Str[R.string.s_tap_skip_if_the_imam_is],Modifier.fillMaxWidth().testTag("iqamah-hint"),fontFamily=if(urdu)Amiri else QuietBody,
                            fontSize=10.sp,lineHeight=if(urdu)20.sp else 15.sp,color=c.secondary,textAlign=TextAlign.Center)
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            IqamahAction(true,s,Modifier.weight(.66f),onStart)
                            IqamahAction(false,s,Modifier.weight(.34f),onStop)
                        }
                    }
                }
            }
        }
        if(s.nightDim&&p.prayer==Prayer.ISHA)Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.22f)))
    }
}

@Composable
internal fun PhoneLandscapeQuietContent(p:Phase.Quiet,s:AppSettings,now:ZonedDateTime,onStop:()->Unit) {
    val c=quietPalette(s.theme);val urdu=L10n.isUrdu(s);val font=if(urdu)Amiri else QuietBody
    val times=quietPrayerTimes(s,p.prayer,now)
    val remaining=((p.endsAt-now.toInstant().toEpochMilli()).coerceAtLeast(0)+59999)/60000
    Box(Modifier.fillMaxSize().background(c.background).testTag("quiet-board").clickable(role=Role.Button,onClickLabel=Str[R.string.s_quiet_exit],onClick=onStop)) {
        PrayerFlowBackdrop(s,true,Modifier.matchParentSize())
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=20.dp,vertical=4.dp).testTag("phone-landscape-quiet")) {
            PhoneFlowHeader(s,now,"quiet")
            Row(Modifier.weight(1f).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Row(Modifier.weight(.70f).fillMaxHeight().testTag("quiet-current-panel"),verticalAlignment=Alignment.CenterVertically) {
                    Column(Modifier.weight(.50f),horizontalAlignment=Alignment.CenterHorizontally) {
                        QuietLabel(Str[R.string.s_quiet_prayer_progress],urdu,c.accent,Modifier.testTag("quiet-progress"),9f)
                        Text(p.prayer.arabic,Modifier.fillMaxWidth().testTag("quiet-current-arabic").semantics{heading()},
                            fontFamily=if(s.theme==AppTheme.KISWAH)ReemKufi else Amiri,fontSize=38.sp,lineHeight=58.sp,color=c.accent,textAlign=TextAlign.Center)
                        Text(if(urdu)L10n.prayer(s,p.prayer)else L10n.prayer(s,p.prayer).uppercase(Locale.ROOT),Modifier.fillMaxWidth().testTag("quiet-current-name"),
                            fontFamily=if(urdu)Amiri else Cinzel,fontSize=12.sp,lineHeight=if(urdu)24.sp else 19.sp,color=c.ink,textAlign=TextAlign.Center)
                    }
                    Column(Modifier.weight(.50f),horizontalAlignment=Alignment.CenterHorizontally) {
                        QuietLabel(Str[R.string.s_quiet_current_azaan],urdu,c.accent,Modifier.testTag("quiet-current-label"),10f)
                        QuietTime(times.current,s,if(urdu)26f else 48f,if(s.theme==AppTheme.PRAYER_GALLERY)c.accent else c.ink,Modifier.fillMaxWidth().testTag("quiet-current-time"))
                        Text(Str[R.string.s_quiet_iqamah_complete],Modifier.fillMaxWidth().testTag("quiet-iqamah-complete"),fontFamily=font,fontSize=12.sp,
                            lineHeight=if(urdu)24.sp else 18.sp,color=c.secondary,textAlign=TextAlign.Center)
                    }
                }
                Box(Modifier.width(1.dp).fillMaxHeight(.65f).background(c.accent.copy(alpha=.35f)))
                Column(Modifier.weight(.30f).testTag("quiet-next-panel"),horizontalAlignment=Alignment.CenterHorizontally) {
                    QuietLabel(Str[R.string.s_quiet_next_azaan],urdu,c.secondary,Modifier.testTag("quiet-next-label"),9f)
                    Text(if(urdu)L10n.prayer(s,times.next)else L10n.prayer(s,times.next).uppercase(Locale.ROOT),Modifier.fillMaxWidth().testTag("quiet-next-name"),
                        fontFamily=if(urdu)Amiri else Cinzel,fontSize=15.sp,lineHeight=if(urdu)30.sp else 23.sp,color=c.ink,textAlign=TextAlign.Center)
                    QuietTime(times.nextTime,s,if(urdu)21f else 33f,c.ink,Modifier.fillMaxWidth().testTag("quiet-next-time"))
                    if(times.nextTime.toLocalDate().isAfter(now.withZoneSameInstant(s.zone()).toLocalDate()))Text(Str[R.string.s_quiet_tomorrow],
                        Modifier.testTag("quiet-tomorrow"),fontFamily=font,fontSize=10.sp,lineHeight=if(urdu)20.sp else 15.sp,color=c.secondary)
                }
            }
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp),horizontalArrangement=Arrangement.spacedBy(16.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(Str.get(R.string.s_quiet_ends_hint,remaining.toInt()),Modifier.weight(.62f).testTag("quiet-expiry-hint"),fontFamily=font,
                    fontSize=11.sp,lineHeight=if(urdu)22.sp else 17.sp,color=c.secondary,textAlign=TextAlign.Center)
                PhoneHomeButton(Str[R.string.s_quiet_exit],s,Modifier.weight(.38f).testTag("quiet-return"),labelTag="quiet-exit-label",onClick=onStop)
            }
        }
        if(s.nightDim&&p.prayer==Prayer.ISHA)Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.22f)))
    }
}
