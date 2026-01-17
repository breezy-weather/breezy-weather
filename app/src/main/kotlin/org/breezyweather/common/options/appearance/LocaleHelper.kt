/*
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

package org.breezyweather.common.options.appearance

import android.content.Context
import androidx.core.os.LocaleListCompat
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.R
import org.breezyweather.common.extensions.capitalize
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

object LocaleHelper {

    fun getLangs(context: Context): ImmutableList<Language> {
        val langs = mutableListOf<Language>()
        val parser = context.resources.getXml(R.xml.locales_config)
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "locale") {
                for (i in 0..<parser.attributeCount) {
                    if (parser.getAttributeName(i) == "name") {
                        val langTag = parser.getAttributeValue(i)
                        val displayName = getLocalizedDisplayName(langTag)
                        if (displayName.isNotEmpty()) {
                            langs.add(
                                Language(
                                    langTag,
                                    displayName
                                    // getDisplayName(langTag)
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        langs.sortBy { it.displayName }
        langs.add(
            0,
            Language(
                "",
                context.getString(R.string.settings_follow_system)
                // null
            )
        )

        return langs.toImmutableList()
    }

    fun getDisplayName(lang: String): String {
        return Locale.forLanguageTag(lang).displayName
    }

    /**
     * Returns display name of a string language code.
     *
     * @param lang empty for system language
     */
    fun getLocalizedDisplayName(lang: String?): String {
        if (lang == null) {
            return ""
        }

        val locale = when (lang) {
            "" -> LocaleListCompat.getAdjustedDefault()[0]
            else -> Locale.forLanguageTag(lang)
        }
        return locale!!.getDisplayName(locale).capitalize(locale)
    }

    data class Language(
        val langTag: String,
        val displayName: String,
        // val localizedDisplayName: String?,
    )
}
