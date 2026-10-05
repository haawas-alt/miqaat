package com.usman.miqaat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.AppTheme

/**
 * Four theme choices as cards: a radio mark, name and one-line description, then a small live-drawn preview of that
 * theme (its own background, time and prayer strip). Selection is shown by a check, a thicker gold border and bold text
 * as well as colour, and announced to TalkBack as a radio button with its state. Previews are drawn from each theme's
 * own tokens, so they can never drift from the real theme.
 */
@Composable
fun ThemePicker(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    val host = screenTokens()
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cols = if (maxWidth >= 480.dp) 2 else 1
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTheme.entries.chunked(cols).forEach { rowThemes ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowThemes.forEach { theme -> ThemeCard(theme, theme == selected, host, Modifier.weight(1f)) { onSelect(theme) } }
                    if (rowThemes.size < cols) Spacer(Modifier.weight((cols - rowThemes.size).toFloat()))
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(theme: AppTheme, cur: Boolean, host: ThemeTokens, modifier: Modifier, onClick: () -> Unit) {
    val tk = ThemeTokenSets.of(theme)
    val shape = RoundedCornerShape(16.dp)
    val kind = Str[if (tk.dark) R.string.s_theme_dark_desc else R.string.s_theme_light_desc]
    val state = Str[if (cur) R.string.s_theme_state_selected else R.string.s_theme_state_not_selected]
    val desc = Str[when (theme) {
        AppTheme.MIQAAT -> R.string.s_theme_desc_miqaat
        AppTheme.KISWAH -> R.string.s_theme_desc_kiswah
        AppTheme.CELESTIAL_MERIDIAN -> R.string.s_theme_desc_celestial
        AppTheme.PRAYER_GALLERY -> R.string.s_theme_desc_gallery
    }]
    val accent = selectionAccent(host)
    Column(
        modifier.heightIn(min = 48.dp).clip(shape).background(host.surface)
            .border(if (cur) 2.dp else 1.dp, if (cur) accent else host.divider, shape)
            .selectable(selected = cur, role = Role.RadioButton, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = "${theme.text}, $desc, $kind"; stateDescription = state }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(if (cur) accent else Color.Transparent).border(2.dp, if (cur) accent else host.outline, CircleShape), contentAlignment = Alignment.Center) {
                if (cur) Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(theme.text, fontFamily = host.fontDisplay, fontSize = 20.sp, fontWeight = if (cur) FontWeight.Bold else FontWeight.Medium, color = host.contentPrimary, maxLines = 2)
                Text(desc, fontFamily = Nunito, fontSize = 14.sp, color = host.contentSecondary, maxLines = 2)
            }
        }
        // Decorative illustration only. Its external title/description remain fully font-scaled.
        val density = androidx.compose.ui.platform.LocalDensity.current
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density, 1f)) {
            ThemePreview(tk, Modifier.fillMaxWidth().height(112.dp).clearAndSetSemantics { })
        }
    }
}

/** The warm-gold selection colour used by cards and the rail: readable on light themes, the theme's own gold on dark ones. */
@Composable
internal fun selectionAccent(tk: ThemeTokens): Color = if (tk.dark) tk.primary else Color(0xFF9A6B1E)

/** A miniature home: the theme background, "Dhuhr 12:24 PM", and a five-prayer strip with the current one underlined. */
@Composable
private fun ThemePreview(tk: ThemeTokens, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(tk.backgroundBrush).border(1.dp, tk.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 8.dp)) {
        // The two artwork themes preview with their real (approved v2) artwork, so the miniature is the Home's own look.
        val art = when (tk.art) { ArtStyle.CELESTIAL -> R.drawable.art_celestial_landscape_v2; ArtStyle.GALLERY -> R.drawable.art_gallery_landscape_v2; else -> null }
        if (art != null) {
            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(art), null, Modifier.matchParentSize().clip(RoundedCornerShape(8.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
            Box(Modifier.matchParentSize().clip(RoundedCornerShape(8.dp)).background(tk.background.copy(alpha = 0.28f)))
        }
        Column(Modifier.align(Alignment.TopStart)) {
            Text("Dhuhr", fontFamily = tk.fontDisplay, fontSize = 13.sp, color = tk.contentPrimary)
            Text("12:24 PM", fontFamily = tk.fontDisplay, fontSize = 22.sp, fontWeight = FontWeight.Medium, color = tk.contentPrimary)
        }
        Box(Modifier.align(Alignment.TopEnd).size(14.dp).clip(CircleShape).background(tk.sun))
        Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").forEachIndexed { i, n ->
                Text(n, fontFamily = Nunito, fontSize = 9.sp, fontWeight = if (i == 1) FontWeight.Bold else FontWeight.Normal, color = if (i == 1) tk.primary else tk.contentSecondary, maxLines = 1)
            }
        }
    }
}
