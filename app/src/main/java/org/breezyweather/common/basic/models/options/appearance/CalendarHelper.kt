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

package org.breezyweather.common.basic.models.options.appearance

import android.content.Context
import android.icu.util.ULocale
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.text.util.LocalePreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.R
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.isChinese
import org.breezyweather.common.extensions.isIndian
import org.breezyweather.settings.SettingsManager
import java.util.Locale

object CalendarHelper {

    const val CALENDAR_EXTENSION_TYPE = "ca"
    private const val NUMBERS_EXTENSION_TYPE = "nu"
    private const val DISPLAY_KEYWORD_OF_CALENDAR = "calendar"

    private val supportedCalendars = listOf(
        LocalePreferences.CalendarType.CHINESE,
        LocalePreferences.CalendarType.DANGI,
        LocalePreferences.CalendarType.INDIAN,
        LocalePreferences.CalendarType.ISLAMIC,
        LocalePreferences.CalendarType.ISLAMIC_CIVIL,
        LocalePreferences.CalendarType.ISLAMIC_RGSA,
        LocalePreferences.CalendarType.ISLAMIC_TBLA,
        LocalePreferences.CalendarType.ISLAMIC_UMALQURA,
        LocalePreferences.CalendarType.PERSIAN
    )

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getDisplayName(
        calendar: String,
        locale: Locale = Locale("en", "001"),
    ): String {
        val localeWithCalendar = Locale.Builder()
            .setUnicodeLocaleKeyword(CALENDAR_EXTENSION_TYPE, calendar)
            .build()

        return ULocale.getDisplayKeywordValue(
            localeWithCalendar.toLanguageTag(),
            DISPLAY_KEYWORD_OF_CALENDAR,
            ULocale.forLocale(locale)
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getCalendars(context: Context): ImmutableList<AlternateCalendar> {
        return supportedCalendars.map {
            val displayName = try {
                getDisplayName(it, context.currentLocale).let { result ->
                    if (result.equals(it, ignoreCase = false)) {
                        // Fallback to English if there is no translation
                        getDisplayName(it).capitalize(context.currentLocale)
                    } else {
                        result.capitalize()
                    }
                }
            } catch (ignored: Exception) {
                try {
                    getDisplayName(it).capitalize()
                } catch (ignored2: Exception) {
                    it.capitalize()
                }
            }
            AlternateCalendar(
                id = it,
                displayName = displayName,
                additionalParams = when (it) {
                    "chinese" -> mapOf(NUMBERS_EXTENSION_TYPE to "hanidays")
                    else -> null
                },
                specificPattern = when (it) {
                    "chinese" -> "MMMd"
                    else -> null
                }
            )
        }.sortedBy {
            it.displayName
        }.toMutableList().apply {
            add(0, AlternateCalendar("none", context.getString(R.string.settings_none)))
            add(1, AlternateCalendar("", context.getString(R.string.settings_follow_system)))
        }.toImmutableList()
    }

    fun getAlternateCalendarSetting(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return null
        }

        val alternateCalendarSetting = SettingsManager.getInstance(context).alternateCalendar
        if (alternateCalendarSetting == "none") {
            return null
        }
        val alternateCalendar = alternateCalendarSetting.ifEmpty {
            with(context.currentLocale) {
                when {
                    isChinese -> LocalePreferences.CalendarType.CHINESE
                    isIndian -> LocalePreferences.CalendarType.INDIAN
                    // Looks like all locales defaults to Gregorian calendar:
                    // https://unicode-org.github.io/icu/userguide/datetime/calendar/#calendar-locale-and-keyword-handling
                    else -> LocalePreferences.getCalendarType(context.currentLocale)
                }
            }
        }
        return if (supportedCalendars.contains(alternateCalendar)) {
            alternateCalendar
        } else {
            null
        }
    }

    data class AlternateCalendar(
        val id: String,
        val displayName: String,
        val additionalParams: Map<String, String>? = null,
        val specificPattern: String? = null,
    )
}
