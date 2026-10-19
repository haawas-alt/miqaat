package com.usman.miqaat.ui

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.*

/** One destination per action, with live context in place of duplicated bordered chips. */
@Composable
internal fun ApprovedPhoneDock(state: PrayerState, s: AppSettings, a: HomeActions, links: List<Door>, tk: ThemeTokens) {
    val qibla = if (s.showQibla) links.firstOrNull() else null // doors() always adds Qibla first.
    val adhkar = links.firstOrNull { it.kind == DoorKind.ADHKAR }
    val mode = listOf(AdhkarMode.MORNING, AdhkarMode.EVENING, AdhkarMode.POST).firstOrNull { it in adhkarModes(state, s) }
    val period = when(mode) {
        AdhkarMode.MORNING -> L10n.t("Morning", "صبح")
        AdhkarMode.EVENING -> L10n.t("Evening", "شام")
        AdhkarMode.POST -> L10n.t("After prayer", "نماز کے بعد")
        else -> L10n.t("Library", "مجموعہ")
    }
    val extra = links.filter { it !== qibla && it !== adhkar }
    val notice = extra.firstOrNull { it.warn } ?: extra.firstOrNull()
    var more by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().testTag("gallery-phone-footer")) {
        // Reliability, Friday, travel and other contextual actions remain visible and actionable.
        // A single borderless notice replaces the old cluster of outlined capsules.
        if (notice != null) Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
            Row(Modifier.weight(1f).heightIn(min=48.dp).clip(RoundedCornerShape(8.dp))
                .then(if(notice.onClick != null) Modifier.clickable(role=Role.Button,onClick=notice.onClick) else Modifier)
                .testTag("phone-dock-notice").semantics(mergeDescendants=true) { contentDescription=notice.label }
                .padding(horizontal=8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                Icon(if(notice.warn) Icons.Outlined.WarningAmber else Icons.Outlined.Info,null,Modifier.size(17.dp),tint=if(notice.warn)tk.warning else tk.primary)
                Text(notice.label,Modifier.weight(1f),fontFamily=if(L10n.isUrdu(s))Amiri else ApprovedBody,
                    fontSize=11.sp,lineHeight=if(L10n.isUrdu(s))20.sp else 16.sp,color=if(notice.warn)tk.warning else tk.contentSecondary)
            }
            if(extra.size > 1) Box {
                IconButton({more=true},Modifier.size(48.dp).testTag("phone-dock-more")) { Icon(Icons.Outlined.MoreHoriz,L10n.word(s,"More actions"),tint=tk.contentPrimary) }
                DropdownMenu(more,{more=false}) { extra.filter{it !== notice}.forEach { d ->
                    DropdownMenuItem(text={Text(d.label)},onClick={more=false;d.onClick?.invoke()},enabled=d.onClick!=null)
                } }
            }
        }
        Box(Modifier.fillMaxWidth().height(.5.dp).background(tk.divider))
        Row(Modifier.fillMaxWidth().background(tk.background.copy(alpha=.88f)).padding(top=2.dp,bottom=2.dp).testTag("phone-action-dock")) {
            if(s.showQibla) {
                val q=PrayerEngine.qibla(s)
                PhoneDockItem(Icons.Outlined.Explore,L10n.word(s,"Qibla"),L10n.iso("${q.toInt()}° ${PrayerEngine.compass(q)}"),
                    qibla?.label ?: L10n.word(s,"Qibla"),"qibla",s,tk,Modifier.weight(1f),a.onOpenQibla)
            }
            PhoneDockItem(Icons.Outlined.AutoStories,L10n.t("Adhkar","اذکار"),period,adhkar?.label ?: L10n.t("Adhkar","اذکار"),
                "adhkar",s,tk,Modifier.weight(1f),{a.onOpenAdhkar(mode ?: AdhkarMode.MORNING)})
            if(s.kidsMode) PhoneDockItem(Icons.Outlined.MenuBook,L10n.t("Learn","سیکھیں"),L10n.t("Salah","نماز"),
                L10n.t("Learn Salah", "نماز سیکھیں") + ", " + L10n.t("Salah", "نماز"),"learn",s,tk,Modifier.weight(1f),a.onOpenLearn)
            PhoneDockItem(Icons.Outlined.CalendarMonth,L10n.t("Timetable","اوقات"),L10n.t("Monthly","ماہانہ"),
                L10n.t("Timetable", "اوقات") + ", " + L10n.t("Monthly", "ماہانہ"),"timetable",s,tk,Modifier.weight(1f),a.onOpenTimetable)
        }
    }
}

@Composable
private fun PhoneDockItem(icon:ImageVector,label:String,context:String,spoken:String,tag:String,s:AppSettings,tk:ThemeTokens,modifier:Modifier,onClick:()->Unit) {
    val urdu=L10n.isUrdu(s)
    Column(modifier.heightIn(min=68.dp).clip(RoundedCornerShape(8.dp)).clickable(role=Role.Button,onClick=onClick)
        .testTag("phone-dock-$tag").semantics(mergeDescendants=true) { contentDescription=spoken }
        .padding(horizontal=3.dp,vertical=4.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
        Icon(icon,null,Modifier.size(22.dp),tint=tk.primary)
        Text(label,Modifier.fillMaxWidth().testTag("phone-dock-$tag-label"),fontFamily=if(urdu)Amiri else ApprovedBody,
            fontSize=12.sp,lineHeight=if(urdu)23.sp else 17.sp,color=tk.contentPrimary,textAlign=TextAlign.Center)
        Text(context,Modifier.fillMaxWidth().testTag("phone-dock-$tag-context"),fontFamily=if(urdu)Amiri else ApprovedBody,
            fontSize=10.sp,lineHeight=if(urdu)20.sp else 15.sp,color=tk.contentSecondary,textAlign=TextAlign.Center)
    }
}
