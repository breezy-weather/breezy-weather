package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

import wangdaye.com.geometricweather.data.entity.model.Weather;

/**
 * Time utils.
 * */

public class TimeUtils {
    // data
    private boolean dayTime;
    private static final String PREFERENCE_NAME = "time_preference";
    private static final String KEY_DAY_TIME = "day_time";

    /** <br> data. */

    private TimeUtils(Context context) {
        getLastDayTime(context);
    }

    public TimeUtils getDayTime(Context context, Weather weather, boolean writeToPreference) {
        int time = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                + Calendar.getInstance().get(Calendar.MINUTE);

        if (weather != null) {
            int sr = 60 * Integer.parseInt(weather.dailyList.get(0).astros[0].split(":")[0])
                    + Integer.parseInt(weather.dailyList.get(0).astros[0].split(":")[1]);
            int ss = 60 * Integer.parseInt(weather.dailyList.get(0).astros[1].split(":")[0])
                    + Integer.parseInt(weather.dailyList.get(0).astros[1].split(":")[1]);

            dayTime = sr < time && time <= ss;
        } else {
            int sr = 60 * 6;
            int ss = 60 * 18;

            dayTime = sr < time && time <= ss;
        }

        if (writeToPreference) {
            SharedPreferences.Editor editor = context.getSharedPreferences(
                    PREFERENCE_NAME, Context.MODE_PRIVATE).edit();
            editor.putBoolean(KEY_DAY_TIME, dayTime);
            editor.apply();
        }

        return this;
    }

    private TimeUtils getLastDayTime(Context context) {
        dayTime = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DAY_TIME, true);
        return this;
    }

    public boolean isDayTime() {
        return dayTime;
    }

    /** <br> singleton. */

    private static TimeUtils instance;

    public static synchronized TimeUtils getInstance(Context context) {
        synchronized (TimeUtils.class) {
            if (instance == null) {
                instance = new TimeUtils(context);
            }
        }
        return instance;
    }
}
