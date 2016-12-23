package wangdaye.com.geometricweather.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextPaint;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayCenterProvider;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayProvider;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayWeekProvider;
import wangdaye.com.geometricweather.receiver.widget.WidgetDayProvider;
import wangdaye.com.geometricweather.receiver.widget.WidgetDayWeekProvider;
import wangdaye.com.geometricweather.receiver.widget.WidgetWeekProvider;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayCenterAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayWeekAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetDayAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetDayWeekAlarmService;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetWeekAlarmService;
import wangdaye.com.geometricweather.service.widget.job.WidgetClockDayCenterJobService;
import wangdaye.com.geometricweather.service.widget.job.WidgetClockDayJobService;
import wangdaye.com.geometricweather.service.widget.job.WidgetClockDayWeekJobService;
import wangdaye.com.geometricweather.service.widget.job.WidgetDayJobService;
import wangdaye.com.geometricweather.service.widget.job.WidgetDayWeekJobService;
import wangdaye.com.geometricweather.service.widget.job.WidgetWeekJobService;

/**
 * Widget utils.
 * */

public class WidgetUtils {

    /** <br> options. */

    public static void startupAllOfWidgetService(final Context c, final Location location) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPreferences;
                String locationName;
                int[] widgetIds;

                // day
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_day_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                widgetIds = AppWidgetManager.getInstance(c)
                        .getAppWidgetIds(new ComponentName(c, WidgetDayProvider.class));
                if (((location.isLocal() && locationName.equals(c.getString(R.string.local))) || (location.city.equals(locationName)))
                        && widgetIds != null && widgetIds.length > 0) {
                    startDayWidgetService(c);
                }

                // week
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_week_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                widgetIds = AppWidgetManager.getInstance(c)
                        .getAppWidgetIds(new ComponentName(c, WidgetWeekProvider.class));
                if (((location.isLocal() && locationName.equals(c.getString(R.string.local))) || (location.city.equals(locationName)))
                        && widgetIds != null && widgetIds.length > 0) {
                    startWeekWidgetService(c);
                }

                // day week
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_day_week_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                widgetIds = AppWidgetManager.getInstance(c)
                        .getAppWidgetIds(new ComponentName(c, WidgetDayWeekProvider.class));
                if (((location.isLocal() && locationName.equals(c.getString(R.string.local))) || (location.city.equals(locationName)))
                        && widgetIds != null && widgetIds.length > 0) {
                    startDayWeekWidgetService(c);
                }

                // clock day center
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_clock_day_center_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                widgetIds = AppWidgetManager.getInstance(c)
                        .getAppWidgetIds(new ComponentName(c, WidgetClockDayCenterProvider.class));
                if (((location.isLocal() && locationName.equals(c.getString(R.string.local))) || (location.city.equals(locationName)))
                        && widgetIds != null && widgetIds.length > 0) {
                    startClockDayCenterWidgetService(c);
                }

                // clock day
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_clock_day_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                widgetIds = AppWidgetManager.getInstance(c)
                        .getAppWidgetIds(new ComponentName(c, WidgetClockDayProvider.class));
                if (((location.isLocal() && locationName.equals(c.getString(R.string.local))) || (location.city.equals(locationName)))
                        && widgetIds != null && widgetIds.length > 0) {
                    startClockDayWidgetService(c);
                }

                // clock day week
                sharedPreferences = c.getSharedPreferences(
                        c.getString(R.string.sp_widget_clock_day_week_setting),
                        Context.MODE_PRIVATE);
                locationName = sharedPreferences.getString(
                        c.getString(R.string.key_location),
                        c.getString(R.string.local));
                widgetIds = AppWidgetManager.getInstance(c)
                        .getAppWidgetIds(new ComponentName(c, WidgetClockDayWeekProvider.class));
                if (((location.isLocal() && locationName.equals(c.getString(R.string.local))) || (location.city.equals(locationName)))
                        && widgetIds != null && widgetIds.length > 0) {
                    startClockDayWeekWidgetService(c);
                }
            }
        }).start();
    }

    public static void startDayWidgetService(Context context) {
        stopDayWidgetService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.schedule(
                    context,
                    WidgetDayJobService.class,
                    WidgetDayJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetDayAlarmService.class));
        }
    }

    public static void stopDayWidgetService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.cancel(context, WidgetDayJobService.SCHEDULE_CODE);
        } else {
            WidgetDayAlarmService.cancelAlarmIntent(
                    context,
                    WidgetDayAlarmService.class,
                    WidgetDayAlarmService.ALARM_CODE);
        }
    }

    public static void startWeekWidgetService(Context context) {
        stopWeekWidgetService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.schedule(
                    context,
                    WidgetWeekJobService.class,
                    WidgetWeekJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetWeekAlarmService.class));
        }
    }

    public static void stopWeekWidgetService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.cancel(context, WidgetWeekJobService.SCHEDULE_CODE);
        } else {
            WidgetWeekAlarmService.cancelAlarmIntent(
                    context,
                    WidgetWeekAlarmService.class,
                    WidgetWeekAlarmService.ALARM_CODE);
        }
    }

    public static void startDayWeekWidgetService(Context context) {
        stopDayWeekWidgetService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.schedule(
                    context,
                    WidgetDayWeekJobService.class,
                    WidgetDayWeekJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetDayWeekAlarmService.class));
        }
    }

    public static void stopDayWeekWidgetService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.cancel(context, WidgetDayWeekJobService.SCHEDULE_CODE);
        } else {
            WidgetDayWeekAlarmService.cancelAlarmIntent(
                    context,
                    WidgetDayWeekAlarmService.class,
                    WidgetDayWeekAlarmService.ALARM_CODE);
        }
    }

    public static void startClockDayCenterWidgetService(Context context) {
        stopClockDayCenterWidgetService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.schedule(
                    context,
                    WidgetClockDayCenterJobService.class,
                    WidgetClockDayCenterJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetClockDayCenterAlarmService.class));
        }
    }

    public static void stopClockDayCenterWidgetService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.cancel(context, WidgetClockDayCenterJobService.SCHEDULE_CODE);
        } else {
            WidgetClockDayCenterAlarmService.cancelAlarmIntent(
                    context,
                    WidgetClockDayCenterAlarmService.class,
                    WidgetClockDayCenterAlarmService.ALARM_CODE);
        }
    }

    public static void startClockDayWidgetService(Context context) {
        stopClockDayWidgetService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.schedule(
                    context,
                    WidgetClockDayJobService.class,
                    WidgetClockDayJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetClockDayAlarmService.class));
        }
    }

    public static void stopClockDayWidgetService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.cancel(context, WidgetClockDayJobService.SCHEDULE_CODE);
        } else {
            WidgetClockDayAlarmService.cancelAlarmIntent(
                    context,
                    WidgetClockDayAlarmService.class,
                    WidgetClockDayAlarmService.ALARM_CODE);
        }
    }

    public static void startClockDayWeekWidgetService(Context context) {
        stopClockDayWeekWidgetService(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.schedule(
                    context,
                    WidgetClockDayWeekJobService.class,
                    WidgetClockDayWeekJobService.SCHEDULE_CODE);
        } else {
            context.startService(new Intent(context, WidgetClockDayWeekAlarmService.class));
        }
    }

    public static void stopClockDayWeekWidgetService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduleUtils.cancel(context, WidgetClockDayWeekJobService.SCHEDULE_CODE);
        } else {
            WidgetClockDayWeekAlarmService.cancelAlarmIntent(
                    context,
                    WidgetClockDayWeekAlarmService.class,
                    WidgetClockDayWeekAlarmService.ALARM_CODE);
        }
    }

    /** <br> UI. */

    public static String[] buildWidgetDayStyleText(Weather weather) {
        String[] texts = new String[] {
                weather.realTime.weather,
                weather.realTime.temp + "℃",
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
