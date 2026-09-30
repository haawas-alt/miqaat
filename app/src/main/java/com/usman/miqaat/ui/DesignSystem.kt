package com.usman.miqaat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R

/*
 * Shared responsive design-system layer. One implementation of each component, styled only through ThemeTokens
 * (via screenTokens()), so the four themes share structure, spacing, semantics and touch-target sizes and differ only in
 * colour, display face, surface treatment and artwork. Feature/domain logic never lives here.
 */

/** Spacing scale from the design spec: 4, 8, 12, 16, 24, 32, 48 dp. */
object Space {
    val xs = 4.dp; val s = 8.dp; val m = 12.dp; val l = 16.dp; val xl = 24.dp; val xxl = 32.dp; val huge = 48.dp
    /** Minimum interactive target. */
    val target = 48.dp
}

/** Width classes. The Settings shell switches at [Breakpoints.settingsRail] as specified. */
enum class WindowClass { Compact, Medium, Expanded }

object Breakpoints {
    val settingsRail = 720.dp
    val medium = 600.dp
    val expanded = 840.dp
    fun classFor(width: Dp) = when { width < medium -> WindowClass.Compact; width < expanded -> WindowClass.Medium; else -> WindowClass.Expanded }
    /** Content padding by class: compact / regular / large. */
    fun padding(width: Dp): Dp = when (classFor(width)) { WindowClass.Compact -> Space.l; WindowClass.Medium -> Space.xl; WindowClass.Expanded -> Space.xxl }
}

/** Kinds of status. Meaning is always carried by icon + words as well as colour. */
enum class NoticeKind { Success, Warning, Error }

@Composable
fun noticeColor(kind: NoticeKind, tk: ThemeTokens = screenTokens()): Color = when (kind) { NoticeKind.Success -> tk.success; NoticeKind.Warning -> tk.warning; NoticeKind.Error -> tk.error }

fun noticeIcon(kind: NoticeKind): ImageVector = when (kind) { NoticeKind.Success -> Icons.Outlined.CheckCircle; NoticeKind.Warning -> Icons.Outlined.WarningAmber; NoticeKind.Error -> Icons.Outlined.ErrorOutline }

/** Tinted container for a status colour, derived from the theme's own surface so it stays readable in light and dark. */
fun containerOf(c: Color, tk: ThemeTokens): Color = c.copy(alpha = if (tk.dark) 0.16f else 0.12f)

/** Section heading inside a screen. Announced as a heading. */
@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    val tk = screenTokens()
    Text(
        text.uppercase(), modifier.semantics { heading() }, fontFamily = Nunito, fontSize = 12.sp, letterSpacing = 1.6.sp,
        fontWeight = FontWeight.Bold, color = tk.contentSecondary
    )
}

/** A rounded, bordered group surface. */
@Composable
fun GroupCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val tk = screenTokens()
    val shape = RoundedCornerShape(tk.cornerMedium)
    Column(modifier.clip(shape).background(tk.surface).border(1.dp, tk.divider, shape), content = content)
}

/** A status notice: icon + title + optional body + optional action. Live-region so changes are announced. */
@Composable
fun StatusNotice(kind: NoticeKind, title: String, modifier: Modifier = Modifier, body: String? = null, actionLabel: String? = null, onAction: (() -> Unit)? = null) {
    val tk = screenTokens()
    val c = noticeColor(kind, tk)
    val shape = RoundedCornerShape(tk.cornerMedium)
    Row(
        modifier.fillMaxWidth().clip(shape).background(containerOf(c, tk)).border(1.dp, c.copy(alpha = 0.5f), shape).padding(Space.m).semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(noticeIcon(kind), null, Modifier.size(24.dp), tint = c)
        Spacer(Modifier.width(Space.m))
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = tk.contentPrimary)
            if (body != null) Text(body, fontFamily = Nunito, fontSize = 13.sp, color = tk.contentSecondary)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.width(Space.s))
            MiqTextButton(actionLabel, onAction)
        }
    }
}

enum class ButtonKind { Primary, Secondary }

@Composable
fun MiqButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, kind: ButtonKind = ButtonKind.Primary, enabled: Boolean = true) {
    val tk = screenTokens()
    val shape = RoundedCornerShape(50)
    val primary = kind == ButtonKind.Primary
    Box(
        modifier.heightIn(min = Space.target).clip(shape)
            .background(if (!enabled) tk.divider else if (primary) tk.primary else Color.Transparent)
            .then(if (primary) Modifier else Modifier.border(1.dp, tk.outline, shape))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = Space.xl, vertical = Space.s),
        contentAlignment = Alignment.Center
    ) { Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (!enabled) tk.contentMuted else if (primary) tk.onPrimary else tk.contentPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis) }
}

@Composable
fun MiqTextButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tk = screenTokens()
    Box(modifier.heightIn(min = Space.target).widthIn(min = Space.target).clip(RoundedCornerShape(50)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = Space.m), contentAlignment = Alignment.Center) {
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = tk.primary)
    }
}

/** Selectable chip: border + check-free text weight + semantic selected state (never colour alone). */
@Composable
fun MiqChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tk = screenTokens()
    val shape = RoundedCornerShape(50)
    Box(
        modifier.heightIn(min = Space.target).clip(shape).background(if (selected) tk.selectedSurface else Color.Transparent)
            .border(if (selected) 2.dp else 1.dp, if (selected) tk.primary else tk.outline, shape)
            .semantics { role = Role.RadioButton; this.selected = selected }
            .clickable(onClick = onClick).padding(horizontal = Space.l, vertical = Space.s),
        contentAlignment = Alignment.Center
    ) { Text(label, fontFamily = Nunito, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp, color = tk.contentPrimary) }
}

/** Search field with clear action. Uses a plain text field so it needs no Material text-field theming per theme. */
@Composable
fun MiqSearchField(query: String, onQuery: (String) -> Unit, hint: String, clearLabel: String, modifier: Modifier = Modifier, onSearch: () -> Unit = {}) {
    val tk = screenTokens()
    val shape = RoundedCornerShape(tk.cornerMedium)
    Row(
        modifier.fillMaxWidth().heightIn(min = 52.dp).clip(shape).background(tk.surfaceRaised).border(1.dp, tk.divider, shape).padding(horizontal = Space.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, null, Modifier.size(22.dp), tint = tk.contentSecondary)
        Spacer(Modifier.width(Space.m))
        Box(Modifier.weight(1f).padding(vertical = Space.s)) {
            if (query.isEmpty()) Text(hint, fontFamily = Nunito, fontSize = 16.sp, color = tk.contentSecondary, maxLines = 1, modifier = Modifier.clearAndSetSemantics { })
            BasicTextField(
                value = query, onValueChange = onQuery, singleLine = true, cursorBrush = SolidColor(tk.primary),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = Nunito, fontSize = 16.sp, color = tk.contentPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = hint }
            )
        }
        if (query.isNotEmpty()) Box(Modifier.size(Space.target).clip(CircleShape).clickable(role = Role.Button) { onQuery("") }.semantics { contentDescription = clearLabel }, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Close, null, Modifier.size(20.dp), tint = tk.contentPrimary)
        }
    }
}

/** A destination row: icon, title, subtitle, chevron. Used on the phone landing and in search results. */
@Composable
fun DestinationRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tk = screenTokens()
    Row(
        modifier.fillMaxWidth().heightIn(min = 64.dp).clickable(role = Role.Button, onClick = onClick).padding(horizontal = Space.l, vertical = Space.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(26.dp), tint = tk.primary)
        Spacer(Modifier.width(Space.l))
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = tk.fontDisplay, fontWeight = FontWeight.Medium, fontSize = 20.sp, color = tk.contentPrimary)
            if (!subtitle.isNullOrEmpty()) Text(subtitle, fontFamily = tk.fontUi, fontSize = 14.sp, color = tk.contentSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, Modifier.size(24.dp), tint = tk.contentSecondary)
    }
}

/** A navigation-rail item with three selection cues: leading accent bar, tinted background, stronger text/icon. */
@Composable
fun RailItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tk = screenTokens()
    val acc = selectionAccent(tk)
    Row(
        modifier.fillMaxWidth().heightIn(min = Space.target + 8.dp).background(if (selected) (if (tk.dark) tk.selectedSurface else Color(0xFFF1E4C6)) else Color.Transparent)
            .semantics { role = Role.Tab; this.selected = selected }
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(28.dp).background(if (selected) acc else Color.Transparent))
        Spacer(Modifier.width(Space.l))
        Icon(icon, null, Modifier.size(26.dp), tint = if (selected) acc else tk.contentPrimary.copy(alpha = 0.8f))
        Spacer(Modifier.width(Space.l))
        Text(label, Modifier.padding(vertical = Space.m).padding(end = Space.m), fontFamily = Nunito, fontSize = 18.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) acc else tk.contentPrimary)
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier, label: String = Str[R.string.s_loading]) {
    val tk = screenTokens()
    Column(modifier.fillMaxWidth().padding(Space.xl).semantics(mergeDescendants = true) { contentDescription = label; liveRegion = LiveRegionMode.Polite }, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = tk.primary, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(Space.m))
        Text(label, fontFamily = Nunito, fontSize = 14.sp, color = tk.contentSecondary)
    }
}

@Composable
fun EmptyState(title: String, modifier: Modifier = Modifier, body: String? = null) {
    val tk = screenTokens()
    Column(modifier.fillMaxWidth().padding(Space.xl), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontFamily = tk.fontDisplay, fontSize = 22.sp, color = tk.contentPrimary)
        if (body != null) Text(body, fontFamily = Nunito, fontSize = 14.sp, color = tk.contentSecondary, modifier = Modifier.padding(top = Space.s))
    }
}

/**
 * Themed artwork with a deterministic scrim. The image is decorative (not announced). Only the two new themes have
 * artwork; other themes draw nothing here so their look is unchanged. Focal alignment differs for portrait and landscape.
 */
@Composable
fun ThemedArtwork(portrait: Boolean, modifier: Modifier = Modifier, scrim: Float = 0.5f) {
    val tk = screenTokens()
    val res = when (tk.art) {
        ArtStyle.CELESTIAL -> if (portrait) R.drawable.art_celestial_portrait else R.drawable.art_celestial_landscape
        ArtStyle.GALLERY -> if (portrait) R.drawable.art_gallery_portrait else R.drawable.art_gallery_landscape
        else -> return
    }
    Box(modifier.clearAndSetSemantics { }) {
        Image(painterResource(res), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop, alignment = if (portrait) Alignment.TopCenter else Alignment.Center)
        // Scrim: the theme background colour fading in from the content side, so text contrast never depends on the crop.
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(tk.background.copy(alpha = scrim * 0.6f), tk.background.copy(alpha = scrim), tk.background.copy(alpha = 0.92f)))))
    }
}
