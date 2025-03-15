/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.common.extensions

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

val Context.currentLocale: Locale
    get() {
        return AppCompatDelegate.getApplicationLocales().get(0)
            ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                this.resources.configuration.locale
            }
    }

// TODO: Review this use vs toString()
val Locale.code: String
    get() {
        val language = language
        val country = country
        return if (!country.isNullOrEmpty() &&
            (
                country.equals("tw", ignoreCase = true) ||
                    country.equals("hk", ignoreCase = true)
                )
        ) {
            language.lowercase() + "-" + country.lowercase()
        } else {
            language.lowercase()
        }
    }

// TODO: Review this use vs toString().replace("_", "-")
val Locale.codeWithCountry: String
    get() {
        val language = language
        val country = country
        return if (!country.isNullOrEmpty()) {
            language.lowercase() + "-" + country.lowercase()
        } else {
            language.lowercase()
        }
    }

// Accepts "Hant" for traditional chinese but no country code otherwise
val Locale.codeForGeonames: String
    get() {
        val language = language
        val country = country
        return if (!country.isNullOrEmpty() &&
            (
                country.equals("tw", ignoreCase = true) ||
                    country.equals("hk", ignoreCase = true)
                )
        ) {
            language.lowercase() + "-Hant"
        } else {
            language.lowercase()
        }
    }

// Everything in uppercase + "ZHT" for traditional Chinese
val Locale.codeForNaturalEarthService: String
    get() {
        val language = language
        val country = country
        return if (!country.isNullOrEmpty() &&
            (
                country.equals("tw", ignoreCase = true) ||
                    country.equals("hk", ignoreCase = true) ||
                    country.equals("mo", ignoreCase = true)
                )
        ) {
            language.uppercase() + "T"
        } else {
            language.uppercase()
        }
    }

val Locale.isChinese: Boolean
    get() = code.startsWith("zh")

val Locale.isIndian: Boolean
    get() = code.startsWith("hi") || code.startsWith("mr")

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

fun String.capitalize(locale: Locale = Locale("en", "001")): String {
    return this.replaceFirstChar { firstChar ->
        if (firstChar.isLowerCase()) {
            firstChar.titlecase(locale)
        } else {
            firstChar.toString()
        }
    }
}

fun String.uncapitalize(locale: Locale = Locale("en", "001")): String {
    return this.replaceFirstChar { firstChar ->
        if (firstChar.isUpperCase()) {
            firstChar.lowercase(locale)
        } else {
            firstChar.toString()
        }
    }
}

fun Context.getStringByLocale(id: Int, locale: Locale = Locale("en", "001")): String {
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration).resources.getString(id)
}
