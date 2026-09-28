package com.usman.miqaat.ui

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.usman.miqaat.data.*

/**
 * Resource-backed UI strings that follow the app's own Language setting (not the system locale),
 * usable from any code path — composables, click handlers and dialogs alike.
 * MainActivity calls [apply] whenever the language changes; the whole screen tree is re-keyed on it.
 */
object Str {
    @Volatile lateinit var res: Resources
    fun init(ctx: Context) { if (!::res.isInitialized) res = ctx.resources }
    fun apply(ctx: Context, language: Language) {
        val cfg = Configuration(ctx.resources.configuration)
        cfg.setLocale(java.util.Locale(language.tag))
        res = ctx.createConfigurationContext(cfg).resources
    }
    operator fun get(id: Int): String = res.getString(id)
    fun get(id: Int, vararg args: Any): String = res.getString(id, *args)
}

/** Localised display names for the settings enums (the enum's own `label` stays English for logs). */
val Method.text: String get() = Str[labelRes]
val Method.info: String get() = Str[detailRes]
val AsrMethod.text: String get() = Str[labelRes]
val LatitudeRule.text: String get() = Str[labelRes]
val Narration.text: String get() = Str[labelRes]
val RamadanMode.text: String get() = Str[labelRes]
val IqamahSound.text: String get() = Str[labelRes]
val AppTheme.text: String get() = Str[labelRes]
val ArtTheme.text: String get() = Str[labelRes]
