package com.usman.miqaat.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.usman.miqaat.data.AppTheme

internal fun fridayBandHeight(theme:AppTheme,tablet:Boolean,wide:Boolean,fontScale:Float):Dp = when {
    // 33dp matches the approved inset band while preserving six native 48dp rows
    // at 115% text size on the 812x375 phone landscape viewport.
    // Keep the KA2 phone-landscape band fixed: scaling this container as well as
    // its sp text stole just enough height to make the six 48dp prayer targets scroll.
    wide && !tablet && theme==AppTheme.KISWAH -> 37.dp
    wide && !tablet -> 24.dp*fontScale
    tablet -> when(theme){
        AppTheme.MIQAAT->38.dp
        AppTheme.KISWAH->if(wide)56.dp else 72.dp
        AppTheme.CELESTIAL_MERIDIAN->if(wide)42.dp else 52.dp
        AppTheme.PRAYER_GALLERY->38.dp
    }
    theme==AppTheme.CELESTIAL_MERIDIAN -> 40.dp*fontScale
    theme==AppTheme.PRAYER_GALLERY -> 32.dp*fontScale
    theme==AppTheme.MIQAAT -> 32.dp
    theme==AppTheme.PRAYER_GALLERY -> 30.dp
    else -> if(theme==AppTheme.KISWAH)44.dp else 38.dp
}
internal fun fridayDockHeight(tablet:Boolean,wide:Boolean):Dp = if(tablet)66.dp else if(wide)48.dp else 56.dp

/** Option 2 portrait boards give the navigation a stronger visual base.
 * KA2 retains its separately approved exact heights. */
internal fun fridayApprovedDockHeight(theme:AppTheme,tablet:Boolean,wide:Boolean):Dp = when {
    theme == AppTheme.KISWAH -> fridayDockHeight(tablet,wide)
    tablet && !wide -> 100.dp
    theme == AppTheme.MIQAAT && !tablet && !wide -> 64.dp
    theme == AppTheme.PRAYER_GALLERY && !tablet && !wide -> 80.dp
    !tablet && !wide -> 76.dp
    else -> fridayDockHeight(tablet,wide)
}


