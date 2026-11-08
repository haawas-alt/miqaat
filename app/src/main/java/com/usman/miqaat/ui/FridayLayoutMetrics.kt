package com.usman.miqaat.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.usman.miqaat.data.AppTheme

internal fun fridayBandHeight(theme:AppTheme,tablet:Boolean,wide:Boolean,fontScale:Float):Dp = when {
    wide && !tablet && theme==AppTheme.KISWAH -> 35.dp*fontScale
    wide && !tablet -> 24.dp*fontScale
    tablet -> when(theme){AppTheme.MIQAAT->38.dp;AppTheme.KISWAH->48.dp;AppTheme.CELESTIAL_MERIDIAN->42.dp;AppTheme.PRAYER_GALLERY->38.dp}
    theme==AppTheme.MIQAAT -> 32.dp
    theme==AppTheme.PRAYER_GALLERY -> 30.dp
    else -> 38.dp
}
internal fun fridayDockHeight(tablet:Boolean,wide:Boolean):Dp = if(tablet)66.dp else if(wide)48.dp else 56.dp

