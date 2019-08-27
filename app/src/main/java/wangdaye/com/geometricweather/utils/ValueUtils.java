package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.ArrayRes;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Value utils.
 * */

public class ValueUtils {

    @Nullable
    public static String getNameByValue(Context context, String value,
                                        @ArrayRes int nameArrayId, @ArrayRes int valueArrayId) {
        String[] names = context.getResources().getStringArray(nameArrayId);
        String[] values = context.getResources().getStringArray(valueArrayId);

        for (int i = 0; i < values.length; i ++) {
            if (values[i].equals(value)) {
                return names[i];
            }
        }

        return null;
    }

    @Nullable
    public static String getWeatherSourceName(Context context, String value) {
        switch (value) {
            case SettingsOptionManager.WEATHER_SOURCE_CN:
                return context.getResources().getStringArray(R.array.chinese_sources)[0];

            case SettingsOptionManager.WEATHER_SOURCE_CAIYUN:
                return context.getResources().getStringArray(R.array.chinese_sources)[1];

            case SettingsOptionManager.WEATHER_SOURCE_ACCU:
                return context.getResources().getStringArray(R.array.chinese_sources)[2];
        }
        return null;
    }

    public static String getCardDisplay(Context c, String[] values) {
        String[] options = c.getResources().getStringArray(R.array.card_display_options);

        StringBuilder builder = new StringBuilder();
        for (String v : values) {
            if (TextUtils.isEmpty(v)) {
                continue;
            }
            switch (v) {
                case SettingsOptionManager.CARD_DAILY_OVERVIEW:
                    builder.append(options[0]);
                    break;

                case SettingsOptionManager.CARD_HOURLY_OVERVIEW:
                    builder.append(options[1]);
                    break;

                case SettingsOptionManager.CARD_AIR_QUALITY:
                    builder.append(options[2]);
                    break;

                case SettingsOptionManager.CARD_LIFE_DETAILS:
                    builder.append(options[3]);
                    break;

                case SettingsOptionManager.CARD_SUNRISE_SUNSET:
                    builder.append(options[4]);
                    break;
            }
            builder.append(" ");
        }
        return builder.toString();
    }

    public static float getUpdateIntervalInHour(String value) {
        switch (value) {
            case SettingsOptionManager.UPDATE_INTERVAL_0_30:
                return 0.5f;

            case SettingsOptionManager.UPDATE_INTERVAL_1_00:
                return 1.0f;

            case SettingsOptionManager.UPDATE_INTERVAL_2_00:
                return 2.0f;

            case SettingsOptionManager.UPDATE_INTERVAL_2_30:
                return 2.5f;

            case SettingsOptionManager.UPDATE_INTERVAL_3_00:
                return 3.0f;

            case SettingsOptionManager.UPDATE_INTERVAL_3_30:
                return 3.5f;

            case SettingsOptionManager.UPDATE_INTERVAL_4_00:
                return 4.0f;

            default:
                return 1.5f;
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

    public static String buildAbsCurrentTemp(int temp, boolean f) {
        return String.valueOf(
                Math.abs(f ? calcFahrenheit(temp) : temp)
        );
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

    public static <T> T nonNull(@Nullable T obj) throws NullException {
        if (obj == null) {
            throw new NullException();
        }
        return obj;
    }

    public static int nonNull(int resId) throws NullResourceIdException {
        if (resId == 0) {
            throw new NullResourceIdException();
        }
        return resId;
    }

    public static class NullException extends Exception {

        public NullException() {
            super("Null Object.");
        }
    }

    public static class NullResourceIdException extends Exception {

        public NullResourceIdException() {
            super("Null Resource.");
        }
    }
}
