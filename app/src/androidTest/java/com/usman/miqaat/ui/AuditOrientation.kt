package com.usman.miqaat.ui

import android.graphics.Point
import android.hardware.display.DisplayManager
import android.content.Context
import android.view.Display
import androidx.test.platform.app.InstrumentationRegistry

/** Gate tests must run in the requested orientation too, not only screenshot fixtures. */
fun enforceAuditOrientation() {
    val desired = InstrumentationRegistry.getArguments().getString("orientation") ?: return
    val inst = InstrumentationRegistry.getInstrumentation()
    val display = inst.targetContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    fun matches(): Boolean {
        val point = Point()
        @Suppress("DEPRECATION")
        display.getDisplay(Display.DEFAULT_DISPLAY).getRealSize(point)
        return (point.x > point.y) == (desired == "landscape")
    }
    if (!matches()) for (rotation in listOf(android.app.UiAutomation.ROTATION_FREEZE_90, android.app.UiAutomation.ROTATION_FREEZE_270, android.app.UiAutomation.ROTATION_FREEZE_0)) {
        inst.uiAutomation.setRotation(rotation)
        Thread.sleep(900)
        if (matches()) break
    }
    inst.runOnMainSync {
        val activity = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
            .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).firstOrNull()
        activity?.requestedOrientation = if (desired == "landscape")
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    Thread.sleep(1000)
    inst.waitForIdleSync()
    check(matches()) { "Could not enforce $desired for gate tests" }
}
