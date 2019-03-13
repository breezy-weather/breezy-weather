package wangdaye.com.geometricweather.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.Calendar;

import wangdaye.com.geometricweather.background.service.alarm.AlarmNormalUpdateService;
import wangdaye.com.geometricweather.background.service.alarm.AlarmTodayForecastUpdateService;
import wangdaye.com.geometricweather.background.service.alarm.AlarmTomorrowForecastUpdateService;

/**
 * Alarm helper.
 * */

class AlarmHelper {

    private static final int HOUR = 1000 * 60 * 60;
    private static final int MINUTE = 1000 * 60;

    private static final int REQUEST_CODE_NORMAL_VIEW = 1;
    private static final int REQUEST_CODE_FORECAST_TODAY = 2;
    private static final int REQUEST_CODE_FORECAST_TOMORROW = 3;

    static void setAlarmForNormalView(Context context, float pollingRate) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_NORMAL_VIEW,
                new Intent(context, AlarmNormalUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.set(
                    AlarmManager.ELAPSED_REALTIME,
                    (long) (SystemClock.elapsedRealtime() + HOUR * pollingRate),
                    pendingIntent);
        }
    }

    static void cancelNormalViewAlarm(Context context) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_NORMAL_VIEW,
                new Intent(context, AlarmNormalUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }

    static void setAlarmForTodayForecast(Context context, String todayForecastTime) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_FORECAST_TODAY,
                new Intent(context, AlarmTodayForecastUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + getForecastAlarmDelay(todayForecastTime),
                    pendingIntent);
        }
    }

    static void cancelTodayForecastAlarm(Context context) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_FORECAST_TODAY,
                new Intent(context, AlarmTodayForecastUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }

    static void setAlarmForTomorrowForecast(Context context, String tomorrowForecastTime) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_FORECAST_TOMORROW,
                new Intent(context, AlarmTomorrowForecastUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + getForecastAlarmDelay(tomorrowForecastTime),
                    pendingIntent);
        }
    }

    static void cancelTomorrowForecastAlarm(Context context) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_FORECAST_TOMORROW,
                new Intent(context, AlarmTomorrowForecastUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(pendingIntent);
        }
    }

    private static long getForecastAlarmDelay(String time) {
        int realTimes[] = new int[] {
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE)};
        int setTimes[] = new int[]{
                Integer.parseInt(time.split(":")[0]),
                Integer.parseInt(time.split(":")[1])};
        if (setTimes[0] == realTimes[0] && setTimes[1] == realTimes[1]) {
            return MINUTE;
        } else {
            int duration = (setTimes[0] - realTimes[0]) * HOUR + (setTimes[1] - realTimes[1]) * MINUTE;
            if (duration <= 0) {
                duration += 24 * HOUR;
            }
            return duration;
        }
    }
}
