package com.usman.miqaat.data

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo

object Device {
    /** Tablets (smallest width >= 600dp) keep the wall layout; phones rotate freely. */
    fun isTablet(ctx: Context) = ctx.resources.configuration.smallestScreenWidthDp >= 600
    fun applyOrientation(a: Activity) {
        a.requestedOrientation = if (isTablet(a)) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_FULL_USER
    }
}
