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
        if (!language.equals("follow_system")) {
            Resources resources = c.getResources();
            Configuration configuration = resources.getConfiguration();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            switch (language) {
                case "chinese":
                    configuration.setLocale(new Locale("zh"));
                    break;

                case "unsimplified_chinese":
                    configuration.setLocale(new Locale("zh", "TW"));
                    break;

                case "turkish":
                    configuration.setLocale(new Locale("tr"));
                    break;

                default:
                    configuration.setLocale(new Locale("en"));
                    break;
            }
            resources.updateConfiguration(configuration, metrics);
        }
    }

    public static String getLanguageCode(Context c) {
        return c.getResources().getConfiguration().locale.getLanguage();
    }
}
