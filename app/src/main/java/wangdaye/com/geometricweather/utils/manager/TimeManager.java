package wangdaye.com.geometricweather.utils.manager;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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

    @SuppressLint("SimpleDateFormat")
    public static int compareDate(String d1, String d2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = format.parse(d1);
            date2 = format.parse(d2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date1 == null && date2 == null) {
            return 0;
        } else if (date1 == null) {
            return -1;
        } else if (date2 == null) {
            return 1;
        } else if (date1.getTime() > date2.getTime()) {
            return 1;
        } else if (date1.getTime() < date2.getTime()) {
            return -1;
        } else {
            return 0;
        }
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

    public boolean isDayTime() {
        return dayTime;
    }

    public static boolean is12Hour(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String strTimeFormat = android.provider.Settings.System.getString(
                resolver, android.provider.Settings.System.TIME_12_24);
        return strTimeFormat != null && strTimeFormat.equals("12");
    }
}
