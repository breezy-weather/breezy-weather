package wangdaye.com.geometricweather.utils.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;

import androidx.annotation.Nullable;

import java.util.Calendar;

import wangdaye.com.geometricweather.basic.model.weather.Weather;

/**
 * Time manager.
 * */

public class TimeManager {

    private static volatile TimeManager instance;

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
        dayTime = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DAY_TIME, true);
    }

    public boolean isDayTime() {
        return dayTime;
    }

    public TimeManager update(Context context, @Nullable Weather weather) {
        dayTime = isDaylight(weather);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFERENCE_NAME, Context.MODE_PRIVATE
        ).edit();
        editor.putBoolean(KEY_DAY_TIME, dayTime);
        editor.apply();
        return this;
    }

    public static boolean isDaylight(String hour, String sunrise, String sunset) {
        try {
            int targetHour = Integer.parseInt(hour);
            int sunriseHour = Integer.parseInt(sunrise.split(":")[0]);
            int sunsetHour = Integer.parseInt(sunset.split(":")[0]);
            return sunriseHour < targetHour && targetHour <= sunsetHour;
        } catch (Exception ignore) {
            return true;
        }
    }

    public static boolean isDaylight(@Nullable Weather weather) {
        int time = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                + Calendar.getInstance().get(Calendar.MINUTE);

        if (weather != null) {
            try {
                int sunrise = 60 * Integer.parseInt(weather.dailyList.get(0).astros[0].split(":")[0])
                        + Integer.parseInt(weather.dailyList.get(0).astros[0].split(":")[1]);
                int sunset = 60 * Integer.parseInt(weather.dailyList.get(0).astros[1].split(":")[0])
                        + Integer.parseInt(weather.dailyList.get(0).astros[1].split(":")[1]);
                return sunrise < time && time <= sunset;
            } catch (Exception e) {
                int sr = 60 * 6;
                int ss = 60 * 18;
                return sr < time && time <= ss;
            }
        } else {
            int sr = 60 * 6;
            int ss = 60 * 18;
            return sr < time && time <= ss;
        }
    }

    public static boolean is12Hour(Context context) {
        return !DateFormat.is24HourFormat(context);
    }
}
