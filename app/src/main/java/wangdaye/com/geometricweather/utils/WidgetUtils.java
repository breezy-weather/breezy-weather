package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextPaint;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayCenterAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayWeekAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetDayAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetDayWeekAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetWeekAlarmService;

/**
 * Widget utils.
 * */

public class WidgetUtils {

    public static void refreshWidgetView(final Context c, final Location location) {
        if (location.weather == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences;
                String locationName;

                // day
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_day_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                if (location.name.equals(locationName)) {
                    WidgetDayAlarmService.refreshWidgetView(c, location.weather);
                }

                // week
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_week_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                if (location.name.equals(locationName)) {
                    WidgetWeekAlarmService.refreshWidgetView(c, location.weather);
                }

                // day week
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_day_week_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                if (location.name.equals(locationName)) {
                    WidgetDayWeekAlarmService.refreshWidgetView(c, location.weather);
                }

                // clock day
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_clock_day_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                if (location.name.equals(locationName)) {
                    WidgetDayAlarmService.refreshWidgetView(c, location.weather);
                }

                // clock day center
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_clock_day_center_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                if (location.name.equals(locationName)) {
                    WidgetClockDayCenterAlarmService.refreshWidgetView(c, location.weather);
                }

                // clock day week
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_clock_day_week_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                if (location.name.equals(locationName)) {
                    WidgetClockDayWeekAlarmService.refreshWidgetView(c, location.weather);
                }
            }
        }).start();
    }

    public static String[] buildWidgetDayStyleText(Weather weather) {
        String[] texts = new String[] {
                weather.live.weather,
                weather.live.temp + "℃",
                weather.dailyList.get(0).temps[0] + "°",
                weather.dailyList.get(0).temps[1] + "°"};

        TextPaint paint = new TextPaint();
        float[] widths = new float[4];
        for (int i = 0; i < widths.length; i ++) {
            widths[i] = paint.measureText(texts[i]);
        }

        float maxiWidth = widths[0];
        for (float w : widths) {
            if (w > maxiWidth) {
                maxiWidth = w;
            }
        }

        while (true) {
            int flag = 0;

            for (int i = 0; i < 2; i ++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = texts[i] + " ";
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flag ++;
                }
            }
            for (int i = 2; i < 4; i ++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = " " + texts[i];
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flag ++;
                }
            }

            if (flag == 4) {
                break;
            }
        }

        return new String[] {
                texts[0] + "\n" + texts[1],
                texts[2] + "\n" + texts[3]};
    }
}
