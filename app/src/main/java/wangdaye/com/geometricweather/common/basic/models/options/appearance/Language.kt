package wangdaye.com.geometricweather.common.basic.models.options.appearance

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import wangdaye.com.geometricweather.R
import wangdaye.com.geometricweather.common.basic.models.options._basic.BaseEnum
import wangdaye.com.geometricweather.common.basic.models.options._basic.Utils
import java.util.*

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
    CHINESE("chinese", Locale("zh", "CN")),
    UNSIMPLIFIED_CHINESE("unsimplified_chinese", Locale("zh", "TW")),
    ENGLISH_US("english_america", Locale("en", "US")),
    ENGLISH_UK("english_britain", Locale("en", "GB")),
    ENGLISH_AU("english_australia", Locale("en", "AU")),
    TURKISH("turkish", Locale("tr")),
    FRENCH("french", Locale("fr")),
    RUSSIAN("russian", Locale("ru")),
    GERMAN("german", Locale("de")),
    SERBIAN("serbian", Locale("sr")),
    SPANISH("spanish", Locale("es")),
    ITALIAN("italian", Locale("it")),
    DUTCH("dutch", Locale("nl")),
    HUNGARIAN("hungarian", Locale("hu")),
    PORTUGUESE("portuguese", Locale("pt")),
    PORTUGUESE_BR("portuguese_brazilian", Locale("pt", "BR")),
    SLOVENIAN("slovenian", Locale("sl", "SI")),
    ARABIC("arabic", Locale("ar")),
    CZECH("czech", Locale("cs")),
    POLISH("polish", Locale("pl")),
    KOREAN("korean", Locale("ko")),
    GREEK("greek", Locale("el")),
    JAPANESE("japanese", Locale("ja")),
    ROMANIAN("romanian", Locale("ro"));

    val code: String
        get() {
            val locale = locale
            val language = locale.language
            val country = locale.country
            return if (!TextUtils.isEmpty(country)
                && (country.lowercase() == "tw" || country.lowercase() == "hk")
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
            "chinese" -> CHINESE
            "unsimplified_chinese" -> UNSIMPLIFIED_CHINESE
            "english_america" -> ENGLISH_US
            "english_britain" -> ENGLISH_UK
            "english_australia" -> ENGLISH_AU
            "turkish" -> TURKISH
            "french" -> FRENCH
            "russian" -> RUSSIAN
            "german" -> GERMAN
            "serbian" -> SERBIAN
            "spanish" -> SPANISH
            "italian" -> ITALIAN
            "dutch" -> DUTCH
            "hungarian" -> HUNGARIAN
            "portuguese" -> PORTUGUESE
            "portuguese_brazilian" -> PORTUGUESE_BR
            "slovenian" -> SLOVENIAN
            "arabic" -> ARABIC
            "czech" -> CZECH
            "polish" -> POLISH
            "korean" -> KOREAN
            "greek" -> GREEK
            "japanese" -> JAPANESE
            "romanian" -> ROMANIAN
            else -> FOLLOW_SYSTEM
        }
    }

    override val valueArrayId = R.array.language_values
    override val nameArrayId = R.array.languages

    override fun getName(context: Context) = Utils.getName(context, this)
}