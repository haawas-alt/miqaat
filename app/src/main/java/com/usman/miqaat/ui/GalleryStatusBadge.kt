package com.usman.miqaat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.usman.miqaat.data.AppSettings
import com.usman.miqaat.data.L10n

/** Option 1: a textual, rounded status, distinct from the editorial prayer name. */
@Composable
internal fun GalleryStatusBadge(isNow: Boolean, settings: AppSettings, modifier: Modifier = Modifier, compact: Boolean = false) {
    val tk = ThemeTokenSets.gallery
    val urdu = L10n.isUrdu(settings)
    Text(L10n.word(settings, if (isNow) "NOW" else "NEXT"),
        modifier = modifier.testTag(if (isNow) "gallery-status-now" else "gallery-status-next")
            .clip(RoundedCornerShape(7.dp)).background(if (isNow) tk.contentPrimary else tk.accent)
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 2.dp),
        fontFamily = if (urdu) uiFont(settings) else Nunito,
        fontWeight = FontWeight.Bold, fontSize = if (urdu) 12.sp else if (compact) 10.sp else 11.sp,
        lineHeight = if (urdu) 23.sp else 16.sp, maxLines = 1,
        letterSpacing = if (urdu) 0.sp else 0.3.sp, color = tk.background)
}
