package wangdaye.com.geometricweather.utils.manager;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

import wangdaye.com.geometricweather.data.entity.model.weather.Weather;

/**
 * Time manager.
 * */

public class TimeManager {

    private static TimeManager instance;

    public static synchronized TimeManager getInstance(Context context) {
        synchronized (TimeManager.class) {
            if (instance == null) {
                instance = new TimeManager(context);
            }
        }
        return instance;
    }

    private boolean dayTime;
    private static final String PREFERENCE_NAME = "time_preference";
    private static final String KEY_DAY_TIME = "day_time";

    private TimeManager(Context context) {
        getLastDayTime(context);
    }

    public TimeManager getDayTime(Context context, Weather weather, boolean writeToPreference) {
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

    private TimeManager getLastDayTime(Context context) {
        dayTime = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DAY_TIME, true);
        return this;
    }

    public boolean isDayTime() {
        return dayTime;
    }
}
