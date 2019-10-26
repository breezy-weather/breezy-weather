package wangdaye.com.geometricweather.basic.model.option.appearance;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Locale;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionUtils;

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
    SLOVENIAN("slovenian", new Locale("sl", "SI"));

    private String languageId;
    private Locale locale;

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
        return OptionUtils.getNameByValue(
                context,
                languageId,
                R.array.languages,
                R.array.language_values
        );
    }
}
