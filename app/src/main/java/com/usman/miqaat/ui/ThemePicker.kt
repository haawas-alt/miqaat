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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Constraints
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.PrayerEngine
import java.time.ZonedDateTime
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
 * as well as colour, and announced to TalkBack as a radio button with its state. Previews use the actual native Home renderer and current art preference, so layout and artwork
 * are shared with the real theme.
 */
@Composable
fun ThemePicker(selected: AppTheme, settings: AppSettings = AppSettings(), onSelect: (AppTheme) -> Unit) {
    val host = screenTokens()
    androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cols = if (maxWidth >= 480.dp) 2 else 1
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTheme.entries.chunked(cols).forEach { rowThemes ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowThemes.forEach { theme -> ThemeCard(theme, theme == selected, host, settings, Modifier.weight(1f)) { onSelect(theme) } }
                    if (rowThemes.size < cols) Spacer(Modifier.weight((cols - rowThemes.size).toFloat()))
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(theme: AppTheme, cur: Boolean, host: ThemeTokens, settings: AppSettings, modifier: Modifier, onClick: () -> Unit) {
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
            Box(Modifier.fillMaxWidth().aspectRatio(891f/411f).testTag("theme-preview-${theme.name}").clearAndSetSemantics { }) {
                ThemePreview(tk, settings, Modifier.matchParentSize())
                // The miniature is decorative. Every tap, including its tiny controls, selects the card.
                Box(Modifier.matchParentSize().clickable(onClick=onClick).clearAndSetSemantics { })
            }
        }
    }
}

/** The warm-gold selection colour used by cards and the rail: readable on light themes, the theme's own gold on dark ones. */
@Composable
internal fun selectionAccent(tk: ThemeTokens): Color = if (tk.dark) tk.primary else Color(0xFF9A6B1E)

/** The same native short-landscape Home used by the app, reduced only as a decorative image. */
@Composable
private fun ThemePreview(tk: ThemeTokens, settings: AppSettings, modifier: Modifier) {
    val config = LocalConfiguration.current
    val previewConfig = remember(config) { android.content.res.Configuration(config).apply {
        orientation = android.content.res.Configuration.ORIENTATION_LANDSCAPE
        screenWidthDp = 891; screenHeightDp = 411
    } }
    val s = remember(tk.theme, settings.artTheme, settings.language) { AppSettings(theme=tk.theme,
        artTheme=settings.artTheme, language=settings.language, latitude=-33.93,longitude=151.19,
        locationName="Mascot, NSW", zoneId="Australia/Sydney",locationSet=true,setupDone=true) }
    val state=remember(s) { PrayerEngine.state(s,ZonedDateTime.of(2026,10,18,18,53,0,0,s.zone())) }
    val actions=remember { HomeActions({}, {}, {}, {}, {}, {}, {}, false, {}, {}) }
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(tk.background)) {
        CompositionLocalProvider(LocalConfiguration provides previewConfig) {
            Layout(content={ PhoneLandscapeHome(state,s,actions) }) { children,constraints ->
                val width=891.dp.roundToPx();val height=411.dp.roundToPx()
                val child=children.single().measure(Constraints.fixed(width,height))
                val scale=minOf(constraints.maxWidth.toFloat()/width,constraints.maxHeight.toFloat()/height)
                layout(constraints.maxWidth,constraints.maxHeight) {
                    child.placeWithLayer(((constraints.maxWidth-width*scale)/2).toInt(),((constraints.maxHeight-height*scale)/2).toInt()) {
                        scaleX=scale;scaleY=scale;transformOrigin=TransformOrigin(0f,0f)
                    }
                }
            }
        }
    }
}
