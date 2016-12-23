package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

/**
 * Language utils.
 * */

public class LanguageUtils {

    public static void setLanguage(Context c, String language) {
        Resources resources = c.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        if (language.equals("follow_system")) {
            switch (Locale.getDefault().getCountry()) {
                case "CN":
                case "cn":
                    configuration.setLocale(Locale.SIMPLIFIED_CHINESE);
                    break;

                default:
                    configuration.setLocale(Locale.US);
                    break;
            }
        } else {
            switch (language) {
                case "chinese":
                    configuration.setLocale(Locale.SIMPLIFIED_CHINESE);
                    break;

                case "english":
                    configuration.setLocale(Locale.US);
                    break;

                default:
                    configuration.setLocale(Locale.US);
                    break;
            }
        }
        resources.updateConfiguration(configuration, metrics);
    }

    public static String getLanguageCode(Context c) {
        return c.getResources().getConfiguration().locale.getLanguage();
    }
}
