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
    FRENCH("french", Locale("fr")),
    INDONESIAN("indonesian", Locale("in")),
    ITALIAN("italian", Locale("it")),
    LITHUANIAN("lithuanian", Locale("lt")),
    HUNGARIAN("hungarian", Locale("hu")),
    DUTCH("dutch", Locale("nl")),
    POLISH("polish", Locale("pl")),
    PORTUGUESE("portuguese", Locale("pt")),
    PORTUGUESE_BR("portuguese_brazilian", Locale("pt", "BR")),
    ROMANIAN("romanian", Locale("ro")),
    SLOVENIAN("slovenian", Locale("sl", "SI")),
    FINNISH("finnish", Locale("fi")),
    VIETNAMESE("vietnamese", Locale("vi")),
    TURKISH("turkish", Locale("tr")),
    GREEK("greek", Locale("el")),
    BULGARIAN("bulgarian", Locale("bg")),
    RUSSIAN("russian", Locale("ru")),
    SERBIAN("serbian", Locale("sr")),
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
            "french" -> FRENCH
            "indonesian" -> INDONESIAN
            "italian" -> ITALIAN
            "lithuanian" -> LITHUANIAN
            "hungarian" -> HUNGARIAN
            "dutch" -> DUTCH
            "polish" -> POLISH
            "portuguese" -> PORTUGUESE
            "portuguese_brazilian" -> PORTUGUESE_BR
            "romanian" -> ROMANIAN
            "slovenian" -> SLOVENIAN
            "finnish" -> FINNISH
            "vietnamese" -> VIETNAMESE
            "turkish" -> TURKISH
            "greek" -> GREEK
            "bulgarian" -> BULGARIAN
            "russian" -> RUSSIAN
            "serbian" -> SERBIAN
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