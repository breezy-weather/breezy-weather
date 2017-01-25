package wangdaye.com.geometricweather.utils.helpter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.service.PollingService;
import wangdaye.com.geometricweather.service.ProtectService;
import wangdaye.com.geometricweather.service.alarm.PollingAlarmService;
import wangdaye.com.geometricweather.service.alarm.TodayForecastAlarmService;
import wangdaye.com.geometricweather.service.alarm.TomorrowForecastAlarmService;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;
import wangdaye.com.geometricweather.utils.remoteView.NormalNotificationUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayVerticalUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayHorizontalUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayWeekUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayWeekUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetWeekUtils;

/**
 * Service helper.
 * */

public class ServiceHelper {

    /** <br> utils. */

    public static void startupAllService(Context context) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), false)) {
            startPermanentService(context, sharedPreferences);
        } else {
            if (WidgetDayUtils.isEnable(context)
                    || WidgetWeekUtils.isEnable(context)
                    || WidgetDayWeekUtils.isEnable(context)
                    || WidgetClockDayHorizontalUtils.isEnable(context)
                    || WidgetClockDayVerticalUtils.isEnable(context)
                    || WidgetClockDayWeekUtils.isEnable(context)
                    || NormalNotificationUtils.isEnable(context)) {
                startPollingService(context);
            }

            if (ForecastNotificationUtils.isEnable(context, true)) {
                startForecastService(context, true);
            }
            if (ForecastNotificationUtils.isEnable(context, false)) {
                startForecastService(context, false);
            }
        }
    }

    /** <br> permanent. */

    public static void startPermanentService(Context context) {
        startPermanentService(
                context,
                PreferenceManager.getDefaultSharedPreferences(context));
    }

    private static void startPermanentService(Context context, SharedPreferences sharedPreferences) {
        boolean working = sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), false)
                && isBackgroundWorking(context);
        boolean openTodayForecast = sharedPreferences.getBoolean(context.getString(R.string.key_forecast_today), false);
        String todayForecastTime = sharedPreferences.getString(
                context.getString(R.string.key_forecast_today_time),
                GeometricWeather.DEFAULT_TODAY_FORECAST_TIME);
        boolean openTomorrowForecast = sharedPreferences.getBoolean(context.getString(R.string.key_forecast_tomorrow), false);
        String tomorrowForecastTime = sharedPreferences.getString(
                context.getString(R.string.key_forecast_tomorrow_time),
                GeometricWeather.DEFAULT_TOMORROW_FORECAST_TIME);
        Intent intent;

        // protect service.
        intent = new Intent(context, ProtectService.class);
        intent.putExtra("from_main", true);
        intent.putExtra("working", working);
        context.startService(intent);

        // polling service.
        intent = new Intent(context, PollingService.class);
        intent.putExtra("from_main", true);
        intent.putExtra("working", working);
        intent.putExtra("force_refresh", true);
        intent.putExtra("today_forecast", openTodayForecast);
        intent.putExtra("today_forecast_time", todayForecastTime);
        intent.putExtra("tomorrow_forecast", openTomorrowForecast);
        intent.putExtra("tomorrow_forecast_time", tomorrowForecastTime);
        context.startService(intent);
    }

    private static boolean isBackgroundWorking(Context context) {
        return !(WidgetDayUtils.isEnable(context)
                || WidgetWeekUtils.isEnable(context)
                || WidgetDayWeekUtils.isEnable(context)
                || WidgetClockDayHorizontalUtils.isEnable(context)
                || WidgetClockDayVerticalUtils.isEnable(context)
                || WidgetClockDayWeekUtils.isEnable(context)
                || NormalNotificationUtils.isEnable(context)
                || ForecastNotificationUtils.isEnable(context, true)
                || ForecastNotificationUtils.isEnable(context, false));
    }

    /** <br> polling. */

    public static void startPollingService(Context context) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), false)) {
            startPermanentService(context, sharedPreferences);
        } else {
            stopPollingService(context);
            Intent intent = new Intent(context, PollingAlarmService.class);
            context.startService(intent);
        }
    }

    public static void stopPollingService(Context context) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), false)) {
            startPermanentService(context, sharedPreferences);
        } else {
            GeoAlarmService.cancelAlarmIntent(
                    context,
                    PollingAlarmService.class,
                    PollingAlarmService.ALARM_CODE);
        }
    }

    public static boolean isNeedShutdownPollingService(Context context) {
        return !WidgetDayUtils.isEnable(context)
                && !WidgetWeekUtils.isEnable(context)
                && !WidgetDayWeekUtils.isEnable(context)
                && !WidgetClockDayHorizontalUtils.isEnable(context)
                && !WidgetClockDayVerticalUtils.isEnable(context)
                && !WidgetClockDayWeekUtils.isEnable(context)
                && !NormalNotificationUtils.isEnable(context);
    }

    /** <br> forecast. */

    public static void startForecastService(Context context, boolean today) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), false)) {
            startPermanentService(context, sharedPreferences);
        } else {
            stopForecastService(context, today);
            Intent intent = new Intent(
                    context,
                    today ? TodayForecastAlarmService.class : TomorrowForecastAlarmService.class);
            context.startService(intent);
        }
    }

    public static void stopForecastService(Context context, boolean today) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), false)) {
            startPermanentService(context, sharedPreferences);
        } else {
            GeoAlarmService.cancelAlarmIntent(
                    context,
                    today ? TodayForecastAlarmService.class : TomorrowForecastAlarmService.class,
                    today ? TodayForecastAlarmService.ALARM_CODE : TomorrowForecastAlarmService.ALARM_CODE);
        }
    }
}
