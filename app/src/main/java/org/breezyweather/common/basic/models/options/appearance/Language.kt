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
import android.content.res.Resources
import android.os.Build
import org.breezyweather.R
import org.breezyweather.common.basic.models.options._basic.BaseEnum
import org.breezyweather.common.basic.models.options._basic.Utils
import java.util.Locale

enum class Language(
    override val id: String,
    val locale: Locale
): BaseEnum {

    FOLLOW_SYSTEM(
        "follow_system",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0]
        } else {
            Resources.getSystem().configuration.locale
        }
    ),
    BOSNIAN("bosnian", Locale("bs")),
    CATALAN("catalan", Locale("ca")),
    CZECH("czech", Locale("cs")),
    DANISH("danish", Locale("da")),
    GERMAN("german", Locale("de")),
    ENGLISH_AU("english_australia", Locale("en", "AU")),
    ENGLISH_CA("english_canada", Locale("en", "CA")),
    ENGLISH_UK("english_britain", Locale("en", "GB")),
    ENGLISH_US("english_america", Locale("en", "US")),
    SPANISH("spanish", Locale("es")),
    ESPERANTO("esperanto", Locale("eo")),
    BASQUE("basque", Locale("eu")),
    FRENCH("french", Locale("fr")),
    CROATIAN("croatian", Locale("hr")),
    INDONESIAN("indonesian", Locale("in")),
    INTERLINGUA("interlingua", Locale("ia")),
    ITALIAN("italian", Locale("it")),
    LATVIAN("latvian", Locale("lv")),
    LITHUANIAN("lithuanian", Locale("lt")),
    HUNGARIAN("hungarian", Locale("hu")),
    DUTCH("dutch", Locale("nl")),
    NORWEGIAN_BOKMAL("norwegian_bokmal", Locale("nb", "NO")),
    POLISH("polish", Locale("pl")),
    PORTUGUESE("portuguese", Locale("pt")),
    PORTUGUESE_BR("portuguese_brazilian", Locale("pt", "BR")),
    ROMANIAN("romanian", Locale("ro")),
    SLOVAK("slovak", Locale("sk")),
    SLOVENIAN("slovenian", Locale("sl", "SI")),
    FINNISH("finnish", Locale("fi")),
    SWEDISH("swedish", Locale("sv")),
    KABYLE("kabyle", Locale("kab")),
    VIETNAMESE("vietnamese", Locale("vi")),
    TURKISH("turkish", Locale("tr")),
    GREEK("greek", Locale("el")),
    BELARUSSIAN("belarussian", Locale("be")),
    BULGARIAN("bulgarian", Locale("bg")),
    MACEDONIAN("macedonian", Locale("mk")),
    RUSSIAN("russian", Locale("ru")),
    SERBIAN("serbian", Locale("sr")),
    UKRAINIAN("ukrainian", Locale("uk")),
    ARABIC("arabic", Locale("ar")),
    PERSIAN("persian", Locale("fa")),
    KURDISH_SORANI("kurdish_sorani", Locale("ckb")),
    HINDI("hindi", Locale("hi")),
    KOREAN("korean", Locale("ko")),
    JAPANESE("japanese", Locale("ja")),
    CHINESE("chinese", Locale("zh", "CN")),
    UNSIMPLIFIED_CHINESE("unsimplified_chinese", Locale("zh", "TW"));

    val code: String
        get() {
            val locale = locale
            val language = locale.language
            val country = locale.country
            return if (!country.isNullOrEmpty() &&
                (country.equals("tw", ignoreCase = true) ||
                    country.equals("hk", ignoreCase = true))
            ) {
                language.lowercase() + "-" + country.lowercase()
            } else language.lowercase()
        }

    val codeWithCountry: String
        get() {
            val locale = locale
            val language = locale.language
            val country = locale.country
            return if (!country.isNullOrEmpty()) {
                language.lowercase() + "-" + country.lowercase()
            } else {
                language.lowercase()
            }
        }

    // Accepts "Hant" for traditional chinese but no country code otherwise
    val codeForGeonames: String
        get() {
            val locale = locale
            val language = locale.language
            val country = locale.country
            return if (!country.isNullOrEmpty() &&
                (country.equals("tw", ignoreCase = true) ||
                    country.equals("hk", ignoreCase = true))
            ) {
                language.lowercase() + "-Hant"
            } else {
                language.lowercase()
            }
        }

    // Everything in uppercase + "ZHT" for traditional Chinese
    val codeForNaturalEarthService: String
        get() {
            val locale = locale
            val language = locale.language
            val country = locale.country
            return if (!country.isNullOrEmpty() &&
                (country.equals("tw", ignoreCase = true) ||
                    country.equals("hk", ignoreCase = true))
            ) {
                language.uppercase() + "T"
            } else language.uppercase()
        }

    val isChinese: Boolean
        get() = code.startsWith("zh")

    companion object {

        fun getInstance(
            value: String
        ) = Language.entries.firstOrNull {
            it.id == value
        } ?: FOLLOW_SYSTEM
    }

    override val valueArrayId = R.array.language_values
    override val nameArrayId = R.array.languages

    override fun getName(context: Context) = Utils.getName(context, this)
}
