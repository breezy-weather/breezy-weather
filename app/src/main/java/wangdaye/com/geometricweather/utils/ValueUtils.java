package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.text.TextUtils;

import wangdaye.com.geometricweather.R;

/**
 * Value utils.
 * */

public class ValueUtils {

    public static String getWeatherSource(Context c, String value) {
        switch (value) {
            case "cn":
                return c.getResources().getStringArray(R.array.chinese_sources)[0];

            case "caiyun":
                return c.getResources().getStringArray(R.array.chinese_sources)[1];

            default:
                return c.getResources().getStringArray(R.array.chinese_sources)[2];
        }
    }

    public static String getLocationService(Context c, String value) {
        switch (value) {
            case "baidu":
                return c.getResources().getStringArray(R.array.location_services)[0];

            case "baidu_ip":
                return c.getResources().getStringArray(R.array.location_services)[1];

            case "amap":
                return c.getResources().getStringArray(R.array.location_services)[2];

            default:
                return c.getResources().getStringArray(R.array.location_services)[3];
        }
    }

    public static String getUIStyle(Context c, String value) {
        switch (value) {
            case "circular":
                return c.getResources().getStringArray(R.array.ui_styles)[0];

            default:
                return c.getResources().getStringArray(R.array.ui_styles)[1];
        }
    }

    public static String getDarkMode(Context c, String value) {
        switch (value) {
            case "auto":
                return c.getResources().getStringArray(R.array.dark_modes)[0];

            case "light":
                return c.getResources().getStringArray(R.array.dark_modes)[1];

            default:
                return c.getResources().getStringArray(R.array.dark_modes)[2];
        }
    }

    public static String getCardDislay(Context c, String[] values) {
        String[] options = c.getResources().getStringArray(R.array.card_display_options);

        StringBuilder builder = new StringBuilder();
        for (String v : values) {
            if (TextUtils.isEmpty(v)) {
                continue;
            }
            switch (v) {
                case "daily_overview":
                    builder.append(options[0]);
                    break;

                case "hourly_overview":
                    builder.append(options[1]);
                    break;

                case "air_quality":
                    builder.append(options[2]);
                    break;

                case "life_details":
                    builder.append(options[3]);
                    break;

                case "sunrise_sunset":
                    builder.append(options[4]);
                    break;
            }
            builder.append(" ");
        }
        return builder.toString();
    }

    public static String getCardOrder(Context c, String value) {
        switch (value) {
            case "daily_first":
                return c.getResources().getStringArray(R.array.card_orders)[0];

            default:
                return c.getResources().getStringArray(R.array.card_orders)[1];
        }
    }

    public static float getRefreshRateScale(String value) {
        switch (value) {
            case "0:30":
                return 0.5f;

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

            case "english_america":
                return c.getResources().getStringArray(R.array.languages)[3];

            case "english_britain":
                return c.getResources().getStringArray(R.array.languages)[4];

            case "english_australia":
                return c.getResources().getStringArray(R.array.languages)[5];

            case "turkish":
                return c.getResources().getStringArray(R.array.languages)[6];

            case "french":
                return c.getResources().getStringArray(R.array.languages)[7];

            case "russian":
                return c.getResources().getStringArray(R.array.languages)[8];

            case "german":
                return c.getResources().getStringArray(R.array.languages)[9];

            case "serbian":
                return c.getResources().getStringArray(R.array.languages)[10];

            case "spanish":
                return c.getResources().getStringArray(R.array.languages)[11];

            case "italian":
                return c.getResources().getStringArray(R.array.languages)[12];

            case "dutch":
                return c.getResources().getStringArray(R.array.languages)[13];

            case "hungarian":
                return c.getResources().getStringArray(R.array.languages)[14];

            case "portuguese":
                return c.getResources().getStringArray(R.array.languages)[15];

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
}
