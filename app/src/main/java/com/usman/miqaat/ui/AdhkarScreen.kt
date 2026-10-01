package com.usman.miqaat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.usman.miqaat.R
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.Adhkar
import com.usman.miqaat.data.L10n
import com.usman.miqaat.data.UrduContent
import com.usman.miqaat.data.Dhikr

/**
 * Left: the list with progress ticks. Right: the selected dhikr, large, with a tap counter.
 * Tapping the big card counts one; when the count is reached it moves to the next.
 */
enum class AdhkarMode(val titleRes: Int, val arabic: String) {
    MORNING(R.string.s_morning_adhk_r, "أذكار الصباح"), EVENING(R.string.s_evening_adhk_r, "أذكار المساء"), POST(R.string.s_after_the_prayer, "أذكار بعد الصلاة");
    val title: String get() = Str[titleRes]
}

@Composable
fun AdhkarScreen(mode: AdhkarMode, onBack: () -> Unit) {
    val tk = screenTokens()
    val morning = mode == AdhkarMode.MORNING
    val list = remember(mode) { when (mode) { AdhkarMode.MORNING -> Adhkar.morning(); AdhkarMode.EVENING -> Adhkar.evening(); AdhkarMode.POST -> Adhkar.postPrayer } }
    val counts = remember(mode) { mutableStateMapOf<String, Int>() }
    var index by remember(mode) { mutableIntStateOf(0) }
    val cur = list[index]
    val done = (counts[cur.id] ?: 0) >= cur.count
    val listState = rememberLazyListState()

    BoxWithConstraints(Modifier.fillMaxSize().background(tk.backgroundBrush)) {
        val u0 = minOf(maxWidth / 100, maxHeight / 56)
        val u = if (maxWidth < 600.dp) u0 * 2.2f else u0
        if (tk.art == ArtStyle.CELESTIAL || tk.art == ArtStyle.GALLERY) ThemedBackdrop(maxHeight > maxWidth, Modifier.fillMaxSize(), scrim = 0.7f)
        else GirihLattice(Modifier.fillMaxSize(), tile = u.value * 11f, alpha = 0.07f)
        val compact = maxWidth < 600.dp
        Row(Modifier.fillMaxSize().statusBarsPadding()) {
            // ---- list
            if (!compact) Column(Modifier.width(u * 30).fillMaxHeight().background(tk.scrim).padding(vertical = u * 1.6f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = u * 1)) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = tk.contentPrimary) }
                    Column {
                        Text(mode.title, fontFamily = Cormorant, fontSize = (u.value * 3f).sp, color = tk.contentPrimary, lineHeight = (u.value * 3.2f).sp)
                        Text(mode.arabic, fontFamily = Amiri, fontSize = (u.value * 2.2f).sp, color = tk.accent)
                    }
                }
                val finished = list.count { (counts[it.id] ?: 0) >= it.count }
                Text(Str.get(R.string.s_count_complete, finished, list.size), fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, color = tk.contentSecondary, modifier = Modifier.padding(start = u * 2.6f, bottom = u * 1))
                LazyColumn(state = listState) {
                    items(list.size) { i ->
                        val d = list[i]
                        val c = counts[d.id] ?: 0
                        val isCur = i == index
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 48.dp).background(if (isCur) tk.primary.copy(alpha = 0.14f) else Color.Transparent)
                                .selectable(selected = isCur, role = androidx.compose.ui.semantics.Role.Tab) { index = i }.padding(horizontal = u * 2.2f, vertical = u * 1.1f)
                                .semantics(mergeDescendants = true) { stateDescription = if (c >= d.count) Str[R.string.s_complete] else if (d.count > 1) Str.get(R.string.s_count_of, c, d.count) else Str[R.string.s_not_yet_read] },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(u * 2.4f).clip(CircleShape).background(if (c >= d.count) tk.success else tk.softFill).border(1.dp, if (isCur) tk.primary else tk.neutralStroke, CircleShape), contentAlignment = Alignment.Center) {
                                if (c >= d.count) Icon(Icons.Outlined.Check, null, Modifier.size(u * 1.5f), tint = tk.onPrimary)
                                else Text("${i + 1}", fontFamily = Nunito, fontSize = (u.value * 1.1f).sp, color = tk.contentPrimary)
                            }
                            Spacer(Modifier.width(u * 1.2f))
                            Column(Modifier.weight(1f)) {
                                Text(if (L10n.uiUrdu) UrduContent.dhikrTitles.getValue(d.id) else d.title, fontFamily = Nunito, fontSize = (u.value * 1.55f).sp, fontWeight = FontWeight.SemiBold, color = tk.contentPrimary.copy(alpha = if (isCur) 1f else 0.8f))
                                Text(if (d.count > 1) "$c / ${d.count}" else if (c > 0) Str[R.string.s_complete] else Str[R.string.s_not_yet_read], fontFamily = Nunito, fontSize = (u.value * 1.15f).sp, color = tk.contentMuted)
                            }
                        }
                    }
                }
            }

            // ---- reader
            Column(
                Modifier.weight(1f).fillMaxHeight()
                    .clickable(onClickLabel = if (cur.count > 1) Str[R.string.s_count_one_recitation] else Str[R.string.s_mark_as_read]) { if (!done) counts[cur.id] = (counts[cur.id] ?: 0) + 1; if ((counts[cur.id] ?: 0) >= cur.count && index < list.size - 1 && cur.count == 1) index++ }
                    .padding(horizontal = u * 4, vertical = u * 2.4f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (compact) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, Str[R.string.s_back], tint = tk.contentPrimary) }
                    Text(mode.title + "  ·  " + Str.get(R.string.s_count_of, index + 1, list.size), fontFamily = Nunito, fontSize = 14.sp, color = tk.contentPrimary.copy(alpha = 0.8f))
                }
                Text(if (L10n.uiUrdu) UrduContent.dhikrTitles.getValue(cur.id) else cur.title.uppercase(), fontFamily = Nunito, fontSize = (u.value * (if (compact) 3f else 1.3f)).sp, letterSpacing = (u.value * 0.3f).sp, fontWeight = FontWeight.Bold, color = tk.accent)
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    val longText = cur.arabic.length > 220
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(cur.arabic, fontFamily = Amiri, fontSize = (u.value * (if (longText) 2.7f else 3.6f)).sp, lineHeight = (u.value * (if (longText) 4.6f else 6.2f)).sp, color = tk.arabicText, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = u * 1))
                    }
                    Text(if (L10n.uiUrdu) UrduContent.dhikrMeanings.getValue(cur.id) else cur.english, fontFamily = Cormorant, fontSize = (u.value * (if (longText) 1.8f else 2.1f)).sp, lineHeight = (u.value * 2.9f).sp, color = tk.contentPrimary, textAlign = TextAlign.Center)
                    Text(cur.source, fontFamily = Nunito, fontSize = (u.value * 1.25f).sp, color = tk.contentSecondary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = u * 1.2f))
                }
                // counter
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(u * 1.6f), modifier = Modifier.padding(top = u * 1)) {
                    val c = counts[cur.id] ?: 0
                    Box(
                        Modifier.size(maxOf(u * 7.5f, 56.dp)).clip(CircleShape).background(if (done) tk.success else tk.primary)
                            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClickLabel = if (cur.count > 1) Str[R.string.s_count_one_recitation] else Str[R.string.s_mark_as_read]) { if (!done) counts[cur.id] = c + 1 }
                            .semantics { contentDescription = if (done) Str[R.string.s_complete] else if (cur.count > 1) Str.get(R.string.s_count_remaining, cur.count - c, cur.count) else Str[R.string.s_tap_when_read]; stateDescription = if (done) Str[R.string.s_complete] else Str.get(R.string.s_count_of, c, cur.count); liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite },
                        contentAlignment = Alignment.Center
                    ) {
                        if (done) Icon(Icons.Outlined.Check, null, Modifier.size(u * 3.4f), tint = tk.onPrimary)
                        else Text("${cur.count - c}", fontFamily = Cormorant, fontSize = (u.value * 3.4f).sp, color = tk.onPrimary)
                    }
                    Column {
                        Text(if (done) Str[R.string.s_complete] else if (cur.count == 1) Str[R.string.s_tap_when_read] else Str[R.string.s_tap_for_each_recitation], fontFamily = Nunito, fontSize = (u.value * 1.5f).sp, fontWeight = FontWeight.SemiBold, color = tk.contentPrimary)
                        Text(if (cur.count > 1) Str.get(R.string.s_count_of, c, cur.count) else "", fontFamily = Nunito, fontSize = (u.value * 1.3f).sp, color = tk.contentSecondary)
                    }
                    Spacer(Modifier.weight(1f))
                    if (!compact) {
                        if (index > 0) Nav(Str[R.string.s_previous], u.value) { index-- }
                        if (index < list.size - 1) Nav(Str[R.string.s_next_2], u.value) { index++ } else Nav(Str[R.string.s_finish], u.value, primary = true, onClick = onBack)
                    }
                }
                if (compact) Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                    if (index > 0) Nav(Str[R.string.s_previous], u.value) { index-- }
                    if (index < list.size - 1) Nav(Str[R.string.s_next_2], u.value) { index++ } else Nav(Str[R.string.s_finish], u.value, primary = true, onClick = onBack)
                }
            }
        }
    }
}

@Composable
private fun Nav(label: String, u: Float, primary: Boolean = false, onClick: () -> Unit) {
    val tk = screenTokens()
    val shape = RoundedCornerShape(50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).clip(shape).background(if (primary) tk.primary else Color.Transparent).border(1.dp, if (primary) tk.primary else tk.neutralStroke, shape)
            .clickable(onClick = onClick).padding(horizontal = (u * 2).dp, vertical = (u * 1).dp)
    ) { Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = (u * 1.4f).sp, color = if (primary) tk.onPrimary else tk.contentPrimary) }
}
