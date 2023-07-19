package org.breezyweather.common.extensions

import android.content.Context
import android.os.Build
import java.util.Locale

val Context.currentLocale: Locale
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.resources.configuration.locales[0]
        } else {
            this.resources.configuration.locale
        }
    }

fun Context.setLanguage(locale: Locale) {
    if (this.currentLocale != locale) {
        val resources = this.resources
        val configuration = resources.configuration
        val metrics = resources.displayMetrics
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, metrics)
    }
}

/**
 * Replaces the given string to have at most [count] characters using [replacement] at its end.
 * If [replacement] is longer than [count] an exception will be thrown when `length > count`.
 */
fun String.chop(count: Int, replacement: String = "…"): String {
    return if (length > count) {
        take(count - replacement.length) + replacement
    } else {
        this
    }
}