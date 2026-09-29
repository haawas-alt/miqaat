package com.usman.miqaat.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.R
import com.usman.miqaat.data.AppTheme

/**
 * Four theme choices as small live-drawn previews. Selection is shown by a check mark, a thicker border and bold text
 * as well as colour, and announced to TalkBack as a radio button with its state. Previews are drawn from each theme's
 * own tokens, so they can never drift from the real theme.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemePicker(selected: AppTheme, onSelect: (AppTheme) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.selectableGroup()
    ) {
        AppTheme.entries.forEach { theme ->
            val tk = ThemeTokenSets.of(theme)
            val cur = theme == selected
            val shape = RoundedCornerShape(16.dp)
            val kind = Str[if (tk.dark) R.string.s_theme_dark_desc else R.string.s_theme_light_desc]
            val state = Str[if (cur) R.string.s_theme_state_selected else R.string.s_theme_state_not_selected]
            Column(
                Modifier.width(168.dp).heightIn(min = 48.dp).clip(shape)
                    .border(if (cur) 3.dp else 1.dp, if (cur) Palette.gold else Palette.lineStrong, shape)
                    .selectable(selected = cur, role = Role.RadioButton) { onSelect(theme) }
                    .semantics(mergeDescendants = true) { contentDescription = "${theme.text}, $kind"; stateDescription = state }
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeSwatch(tk, Modifier.fillMaxWidth().height(72.dp).clearAndSetSemantics { })
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        theme.text, fontFamily = Nunito, fontSize = 14.sp, fontWeight = if (cur) FontWeight.Bold else FontWeight.Normal,
                        color = Palette.ivory, modifier = Modifier.weight(1f), maxLines = 2
                    )
                    if (cur) Icon(Icons.Outlined.Check, contentDescription = null, tint = Palette.gold, modifier = Modifier.width(20.dp).height(20.dp))
                }
            }
        }
    }
}

/** A tiny preview of a theme: its background, a raised panel, a primary bar and a sun/moon mark. Decorative. */
@Composable
private fun ThemeSwatch(tk: ThemeTokens, modifier: Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(10.dp))) {
        drawRect(tk.backgroundBrush, size = size)
        // raised panel on the right, three "rows"
        val pw = size.width * 0.38f; val px = size.width * 0.58f
        drawRect(tk.surface, Offset(px, size.height * 0.12f), Size(pw, size.height * 0.76f))
        val rowH = size.height * 0.16f
        for (i in 0 until 3) {
            val y = size.height * 0.20f + i * (rowH + size.height * 0.06f)
            drawRect(if (i == 1) tk.primary else tk.divider, Offset(px + pw * 0.10f, y), Size(pw * 0.80f, rowH * 0.5f))
        }
        // sun / moon disc and a horizon
        drawCircle(tk.sun, radius = size.height * 0.14f, center = Offset(size.width * 0.24f, size.height * 0.36f))
        drawRect(tk.surfaceRaised, Offset(0f, size.height * 0.72f), Size(size.width * 0.52f, size.height * 0.28f))
        drawRect(tk.primary, Offset(size.width * 0.06f, size.height * 0.72f), Size(size.width * 0.40f, size.height * 0.04f))
    }
}
