package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.os.Build;

/**
 * Polling task helper.
 * */
public class PollingTaskHelper {

    public static void startNormalPollingTask(Context context, float pollingRate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlarmHelper.setAlarmForNormalView(context, pollingRate);
        } else {
            JobHelper.setJobForNormalView(context, pollingRate);
        }
    }

    public static void stopNormalPollingTask(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlarmHelper.cancelNormalViewAlarm(context);
        } else {
            JobHelper.cancelNormalViewJob(context);
        }
    }

    public static void startTodayForecastPollingTask(Context context, String todayForecastTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlarmHelper.setAlarmForTodayForecast(context, todayForecastTime);
        } else {
            JobHelper.setJobForTodayForecast(context, todayForecastTime);
        }
    }

    public static void stopTodayForecastPollingTask(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlarmHelper.cancelTodayForecastAlarm(context);
        } else {
            JobHelper.cancelTodayForecastJob(context);
        }
    }

    public static void startTomorrowForecastPollingTask(Context context, String tomorrowForecastTime) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlarmHelper.setAlarmForTomorrowForecast(context, tomorrowForecastTime);
        } else {
            JobHelper.setJobForTomorrowForecast(context, tomorrowForecastTime);
        }
    }

    public static void stopTomorrowForecastPollingTask(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AlarmHelper.cancelTomorrowForecastAlarm(context);
        } else {
            JobHelper.cancelTomorrowForecastJob(context);
        }
    }
}
