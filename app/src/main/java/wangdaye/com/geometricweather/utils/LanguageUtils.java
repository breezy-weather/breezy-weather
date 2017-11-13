package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Language utils.
 * */

public class LanguageUtils {

    public static void setLanguage(Context c, String language) {
        if (!language.equals("follow_system")) {
            Resources resources = c.getResources();
            Configuration configuration = resources.getConfiguration();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            Locale locale;
            switch (language) {
                case "chinese":
                    locale = new Locale("zh", "CN");
                    break;

                case "unsimplified_chinese":
                    locale = new Locale("zh", "TW");
                    break;

                case "turkish":
                    locale = new Locale("tr");
                    break;

                case "french":
                    locale = new Locale("fr");
                    break;

                case "russian":
                    locale = new Locale("ru");
                    break;

                case "german":
                    locale = new Locale("de");
                    break;

                case "serbian":
                    locale = new Locale("sr");
                    break;

                case "spanish":
                    locale = new Locale("es");
                    break;

                default:
                    locale = new Locale("en");
                    break;
            }
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, metrics);
        }
    }

    public static String getLanguageCode(Context c) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = c.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = c.getResources().getConfiguration().locale;
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        return language.toLowerCase()
                + (TextUtils.isEmpty(country) ? "" : ("-" + country.toLowerCase()));
    }
}
