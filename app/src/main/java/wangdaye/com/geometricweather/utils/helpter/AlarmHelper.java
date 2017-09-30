package wangdaye.com.geometricweather.utils.helpter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.Calendar;

import wangdaye.com.geometricweather.service.NormalUpdateService;
import wangdaye.com.geometricweather.service.TodayForecastUpdateService;
import wangdaye.com.geometricweather.service.TomorrowForecastUpdateService;

/**
 * Alarm helper.
 * */

public class AlarmHelper {

    private static final int HOUR = 1000 * 60 * 60;
    private static final int MINUTE = 1000 * 60;

    private static final int REQUEST_CODE_NORMAL_VIEW = 1;
    private static final int REQUEST_CODE_FORECAST_TODAY = 2;
    private static final int REQUEST_CODE_FORECAST_TOMORROW = 3;

    public static void setAlarmForNormalView(Context context, float pollingRate) {
        cancelNormalViewAlarm(context);

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_NORMAL_VIEW,
                new Intent(context, NormalUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(
                AlarmManager.ELAPSED_REALTIME,
                (long) (SystemClock.elapsedRealtime() + HOUR * pollingRate),
                pendingIntent);
    }

    public static void cancelNormalViewAlarm(Context context) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_NORMAL_VIEW,
                new Intent(context, NormalUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
    }

    public static void setAlarmForTodayForecast(Context context, String todayForecastTime) {
        cancelTodayForecastAlarm(context);

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_FORECAST_TODAY,
                new Intent(context, TodayForecastUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + getForecastAlarmDelay(todayForecastTime),
                pendingIntent);
    }

    public static void cancelTodayForecastAlarm(Context context) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_FORECAST_TODAY,
                new Intent(context, TodayForecastUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
    }

    public static void setAlarmForTomorrowForecast(Context context, String tomorrowForecastTime) {
        cancelTomorrowForecastAlarm(context);

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_FORECAST_TOMORROW,
                new Intent(context, TomorrowForecastUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + getForecastAlarmDelay(tomorrowForecastTime),
                pendingIntent);
    }

    public static void cancelTomorrowForecastAlarm(Context context) {
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                REQUEST_CODE_FORECAST_TOMORROW,
                new Intent(context, TomorrowForecastUpdateService.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pendingIntent);
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
