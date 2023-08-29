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
    CATALAN("catalan", Locale("ca")),
    CZECH("czech", Locale("cs")),
    GERMAN("german", Locale("de")),
    ENGLISH_AU("english_australia", Locale("en", "AU")),
    ENGLISH_UK("english_britain", Locale("en", "GB")),
    ENGLISH_US("english_america", Locale("en", "US")),
    SPANISH("spanish", Locale("es")),
    BASQUE("basque", Locale("eu")),
    FRENCH("french", Locale("fr")),
    INDONESIAN("indonesian", Locale("in")),
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
    VIETNAMESE("vietnamese", Locale("vi")),
    TURKISH("turkish", Locale("tr")),
    GREEK("greek", Locale("el")),
    BULGARIAN("bulgarian", Locale("bg")),
    RUSSIAN("russian", Locale("ru")),
    SERBIAN("serbian", Locale("sr")),
    UKRAINIAN("ukrainian", Locale("uk")),
    ARABIC("arabic", Locale("ar")),
    KURDISH_SORANI("kurdish_sorani", Locale("ckb")),
    KOREAN("korean", Locale("ko")),
    JAPANESE("japanese", Locale("ja")),
    CHINESE("chinese", Locale("zh", "CN")),
    UNSIMPLIFIED_CHINESE("unsimplified_chinese", Locale("zh", "TW"));

    val code: String
        get() {
            val locale = locale
            val language = locale.language
            val country = locale.country
            return if (!country.isNullOrEmpty()
                && (country.equals("tw", ignoreCase = true) || country.equals("hk", ignoreCase = true))
            ) {
                language.lowercase() + "-" + country.lowercase()
            } else {
                language.lowercase()
            }
        }

    // Used by GeoNames, accepts "Hant" for traditional chinese but no country code otherwise
    val codeAlt: String
        get() {
            val locale = locale
            val language = locale.language
            val country = locale.country
            return if (!country.isNullOrEmpty()
                && (country.equals("tw", ignoreCase = true) || country.equals("hk", ignoreCase = true))
            ) {
                language.lowercase() + "-Hant"
            } else {
                language.lowercase()
            }
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

    val isChinese: Boolean
        get() = code.startsWith("zh")

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "catalan" -> CATALAN
            "czech" -> CZECH
            "german" -> GERMAN
            "english_australia" -> ENGLISH_AU
            "english_britain" -> ENGLISH_UK
            "english_america" -> ENGLISH_US
            "spanish" -> SPANISH
            "basque" -> BASQUE
            "french" -> FRENCH
            "indonesian" -> INDONESIAN
            "italian" -> ITALIAN
            "latvian" -> LATVIAN
            "lithuanian" -> LITHUANIAN
            "hungarian" -> HUNGARIAN
            "dutch" -> DUTCH
            "norwegian_bokmal" -> NORWEGIAN_BOKMAL
            "polish" -> POLISH
            "portuguese" -> PORTUGUESE
            "portuguese_brazilian" -> PORTUGUESE_BR
            "romanian" -> ROMANIAN
            "slovak" -> SLOVAK
            "slovenian" -> SLOVENIAN
            "finnish" -> FINNISH
            "vietnamese" -> VIETNAMESE
            "turkish" -> TURKISH
            "greek" -> GREEK
            "bulgarian" -> BULGARIAN
            "russian" -> RUSSIAN
            "serbian" -> SERBIAN
            "ukrainian" -> UKRAINIAN
            "arabic" -> ARABIC
            "kurdish_sorani" -> KURDISH_SORANI
            "korean" -> KOREAN
            "japanese" -> JAPANESE
            "chinese" -> CHINESE
            "unsimplified_chinese" -> UNSIMPLIFIED_CHINESE
            else -> FOLLOW_SYSTEM
        }
    }

    override val valueArrayId = R.array.language_values
    override val nameArrayId = R.array.languages

    override fun getName(context: Context) = Utils.getName(context, this)
}