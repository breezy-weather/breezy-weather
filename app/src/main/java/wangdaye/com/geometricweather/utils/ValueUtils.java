package wangdaye.com.geometricweather.utils;

import android.content.Context;

import wangdaye.com.geometricweather.R;

/**
 * Value utils.
 * */

public class ValueUtils {

    public static String getLanguage(Context c, String value) {
        switch (value) {
            case "follow_system":
                return c.getResources().getStringArray(R.array.languages)[0];

            case "chinese":
                return c.getResources().getStringArray(R.array.languages)[1];

            case "english":
                return c.getResources().getStringArray(R.array.languages)[2];

            default:
                return null;
        }
    }

    public static String getNotificationTextColor(Context c, String value) {
        switch (value) {
            case "dark":
                return c.getResources().getStringArray(R.array.notification_text_colors)[0];

            case "grey":
                return c.getResources().getStringArray(R.array.notification_text_colors)[1];

            default:
                return c.getResources().getStringArray(R.array.notification_text_colors)[2];
        }
    }
}
