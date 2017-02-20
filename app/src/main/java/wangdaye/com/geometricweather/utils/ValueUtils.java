package wangdaye.com.geometricweather.utils;

import android.content.Context;

import wangdaye.com.geometricweather.R;

/**
 * Value utils.
 * */

public class ValueUtils {

    public static float getRefreshRateScale(String value) {
        switch (value) {
            case "1:00":
                return 1.0f;

            case "2:00":
                return 2.0f;

            case "2:30":
                return 2.5f;

            case "3:00":
                return 3.0f;

            case "3:30":
                return 3.0f;

            case "4:00":
                return 4.0f;

            default:
                return 1.5f;
        }
    }

    public static String getLanguage(Context c, String value) {
        switch (value) {
            case "follow_system":
                return c.getResources().getStringArray(R.array.languages)[0];

            case "chinese":
                return c.getResources().getStringArray(R.array.languages)[1];

            case "unsimplified_chinese":
                return c.getResources().getStringArray(R.array.languages)[2];

            case "english":
                return c.getResources().getStringArray(R.array.languages)[3];

            case "turkish":
                return c.getResources().getStringArray(R.array.languages)[4];

            case "french":
                return c.getResources().getStringArray(R.array.languages)[5];

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

    public static String buildCurrentTemp(int temp, boolean space, boolean f) {
        if (f) {
            return calcFahrenheit(temp) + (space ? " ℉" : "℉");
        } else {
            return temp + (space ? " ℃" : "℃");
        }
    }

    public static String buildAbbreviatedCurrentTemp(int temp, boolean f) {
        if (f) {
            return calcFahrenheit(temp) + "°";
        } else {
            return temp + "°";
        }
    }

    public static String buildDailyTemp(int[] temps, boolean space, boolean f) {
        if (f) {
            return calcFahrenheit(temps[1]) + (space ? "° / " : "/") + calcFahrenheit(temps[0]) + "°";
        } else {
            return temps[1] + (space ? "° / " : "/") + temps[0] + "°";
        }
    }

    public static int calcFahrenheit(int temp) {
        return (int) (9.0 / 5.0 * temp + 32);
    }

    public static float calcFahrenheit(float temp) {
        return (float) (9.0 / 5.0 * temp + 32);
    }
}
