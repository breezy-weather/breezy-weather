package wangdaye.com.geometricweather.common.basic.models.options.appearance;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Locale;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.common.basic.models.options._utils.Utils;

public enum Language {

    FOLLOW_SYSTEM(
            "follow_system",
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    ? Resources.getSystem().getConfiguration().getLocales().get(0)
                    : Resources.getSystem().getConfiguration().locale
    ),
    CHINESE("chinese", new Locale("zh", "CN")),
    UNSIMPLIFIED_CHINESE("unsimplified_chinese", new Locale("zh", "TW")),
    ENGLISH_US("english_america", new Locale("en", "US")),
    ENGLISH_UK("english_britain", new Locale("en", "GB")),
    ENGLISH_AU("english_australia", new Locale("en", "AU")),
    TURKISH("turkish", new Locale("tr")),
    FRENCH("french", new Locale("fr")),
    RUSSIAN("russian", new Locale("ru")),
    GERMAN("german", new Locale("de")),
    SERBIAN("serbian", new Locale("sr")),
    SPANISH("spanish", new Locale("es")),
    ITALIAN("italian", new Locale("it")),
    DUTCH("dutch", new Locale("nl")),
    HUNGARIAN("hungarian", new Locale("hu")),
    PORTUGUESE("portuguese", new Locale("pt")),
    PORTUGUESE_BR("portuguese_brazilian", new Locale("pt", "BR")),
    SLOVENIAN("slovenian", new Locale("sl", "SI")),
    ARABIC("arabic", new Locale("ar")),
    CZECH("czech", new Locale("cs")),
    POLISH("polish", new Locale("pl")),
    KOREAN("korean", new Locale("ko")),
    GREEK("greek", new Locale("el")),
    JAPANESE("japanese", new Locale("ja")),
    ROMANIAN("romanian", new Locale("ro"));

    private final String languageId;
    private final Locale locale;

    Language(String languageId, Locale locale) {
        this.languageId = languageId;
        this.locale = locale;
    }

    public String getCode() {
        Locale locale = getLocale();
        String language = locale.getLanguage();
        String country = locale.getCountry();

        if (!TextUtils.isEmpty(country)
                && (country.toLowerCase().equals("tw") || country.toLowerCase().equals("hk"))) {
            return language.toLowerCase() + "-" + country.toLowerCase();
        } else {
            return language.toLowerCase();
        }
    }

    public Locale getLocale() {
        return locale;
    }

    @Nullable
    public String getLanguageName(Context context) {
        return Utils.getNameByValue(
                context.getResources(),
                languageId,
                R.array.languages,
                R.array.language_values
        );
    }

    public boolean isChinese() {
        return getCode().startsWith("zh");
    }

    public static Language getInstance(String value) {
        switch (value) {
            case "chinese":
                return CHINESE;

            case "unsimplified_chinese":
                return UNSIMPLIFIED_CHINESE;

            case "english_america":
                return ENGLISH_US;

            case "english_britain":
                return ENGLISH_UK;

            case "english_australia":
                return ENGLISH_AU;

            case "turkish":
                return TURKISH;

            case "french":
                return FRENCH;

            case "russian":
                return RUSSIAN;

            case "german":
                return GERMAN;

            case "serbian":
                return SERBIAN;

            case "spanish":
                return SPANISH;

            case "italian":
                return ITALIAN;

            case "dutch":
                return DUTCH;

            case "hungarian":
                return HUNGARIAN;

            case "portuguese":
                return PORTUGUESE;

            case "portuguese_brazilian":
                return PORTUGUESE_BR;

            case "slovenian":
                return SLOVENIAN;

            case "arabic":
                return ARABIC;

            case "czech":
                return CZECH;

            case "polish":
                return POLISH;

            case "korean":
                return KOREAN;

            case "greek":
                return GREEK;

            case "japanese":
                return JAPANESE;

            case "romanian":
                return ROMANIAN;

            default:
                return FOLLOW_SYSTEM;
        }
    }
}
