package com.usman.miqaat.ui

import android.app.Application
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.usman.miqaat.MiqaatApp
import com.usman.miqaat.data.AppTheme
import com.usman.miqaat.data.Language
import org.junit.Rule
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/** Devices used by the UI and screenshot tests (dp). */
enum class Dev(val q: String, val label: String) {
    PHONE_PORTRAIT("w411dp-h891dp-port-xhdpi", "phone-portrait"),
    PHONE_LANDSCAPE("w891dp-h411dp-land-xhdpi", "phone-landscape"),
    TABLET_PORTRAIT("w800dp-h1280dp-port-xhdpi", "tablet-portrait"),
    TABLET_LANDSCAPE("w1280dp-h800dp-land-xhdpi", "tablet-landscape")
}

/** Shared plumbing: launches a bare ComponentActivity on the JVM (Robolectric) and composes real app UI inside the real theme. */
abstract class ComposeSupport {
    @get:Rule val rule = createEmptyComposeRule()
    protected lateinit var scenario: ActivityScenario<ComponentActivity>
    protected val app: MiqaatApp get() = ApplicationProvider.getApplicationContext<Application>() as MiqaatApp

    protected fun show(dev: Dev, theme: AppTheme = AppTheme.PRAYER_GALLERY, fontScale: Float = 1f, rtl: Boolean = false, language: Language? = null, qualifiers: String? = null, content: @Composable () -> Unit) {
        RuntimeEnvironment.setQualifiers("+" + (qualifiers ?: dev.q))
        val a = app
        shadowOf(a.packageManager).addActivityIfNotPresent(ComponentName(a.packageName, ComponentActivity::class.java.name))
        Str.apply(a, language ?: if (rtl) Language.UR else Language.EN)
        scenario = ActivityScenario.launch(ComponentActivity::class.java)
        scenario.onActivity { act ->
            act.setContent {
                val d = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(d.density, fontScale),
                    LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) { MiqaatTheme(theme) { content() } }
            }
        }
        rule.waitForIdle()
    }
}
