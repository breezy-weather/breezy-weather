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

    public static void startupAllService(Context context, boolean onlyRefreshNormalView) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), true)) {
            startPermanentService(context, sharedPreferences, onlyRefreshNormalView);
        } else {
            if (WidgetDayUtils.isEnable(context)
                    || WidgetWeekUtils.isEnable(context)
                    || WidgetDayWeekUtils.isEnable(context)
                    || WidgetClockDayHorizontalUtils.isEnable(context)
                    || WidgetClockDayVerticalUtils.isEnable(context)
                    || WidgetClockDayWeekUtils.isEnable(context)
                    || NormalNotificationUtils.isEnable(context)
                    || TileHelper.isEnable(context)) {
                startPollingService(context, onlyRefreshNormalView);
            }

            if (ForecastNotificationUtils.isEnable(context, true)) {
                startForecastService(context, true, onlyRefreshNormalView);
            }
            if (ForecastNotificationUtils.isEnable(context, false)) {
                startForecastService(context, false, onlyRefreshNormalView);
            }
        }
    }

    /** <br> permanent. */

    public static void startPermanentService(Context context, boolean onlyRefreshNormalView) {
        startPermanentService(
                context,
                PreferenceManager.getDefaultSharedPreferences(context),
                onlyRefreshNormalView);
    }

    private static void startPermanentService(Context context, SharedPreferences sharedPreferences, boolean onlyRefreshNormalView) {
        boolean working = sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), true)
                && isBackgroundWorking(context);
        boolean openTodayForecast = sharedPreferences.getBoolean(context.getString(R.string.key_forecast_today), false);
        String todayForecastTime = sharedPreferences.getString(
                context.getString(R.string.key_forecast_today_time),
                GeometricWeather.DEFAULT_TODAY_FORECAST_TIME);
        boolean openTomorrowForecast = sharedPreferences.getBoolean(context.getString(R.string.key_forecast_tomorrow), false);
        String tomorrowForecastTime = sharedPreferences.getString(
                context.getString(R.string.key_forecast_tomorrow_time),
                GeometricWeather.DEFAULT_TOMORROW_FORECAST_TIME);

        // protect service.
        Intent protect = new Intent(context, ProtectService.class);
        protect.putExtra("is_refresh", true);
        protect.putExtra("working", working);

        // polling service.
        Intent polling = new Intent(context, PollingService.class);
        polling.putExtra("is_refresh", true);
        polling.putExtra("working", working);
        polling.putExtra("force_refresh", true);
        polling.putExtra("only_refresh_normal_view", onlyRefreshNormalView);
        polling.putExtra("today_forecast", openTodayForecast);
        polling.putExtra("today_forecast_time", todayForecastTime);
        polling.putExtra("tomorrow_forecast", openTomorrowForecast);
        polling.putExtra("tomorrow_forecast_time", tomorrowForecastTime);

        context.startService(polling);
        context.startService(protect);
    }

    private static boolean isBackgroundWorking(Context context) {
        return WidgetDayUtils.isEnable(context)
                || WidgetWeekUtils.isEnable(context)
                || WidgetDayWeekUtils.isEnable(context)
                || WidgetClockDayHorizontalUtils.isEnable(context)
                || WidgetClockDayVerticalUtils.isEnable(context)
                || WidgetClockDayWeekUtils.isEnable(context)
                || NormalNotificationUtils.isEnable(context)
                || ForecastNotificationUtils.isEnable(context, true)
                || ForecastNotificationUtils.isEnable(context, false)
                || TileHelper.isEnable(context);
    }

    /** <br> polling. */

    public static void startPollingService(Context context, boolean onlyRefreshNormalView) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), true)) {
            startPermanentService(context, sharedPreferences, onlyRefreshNormalView);
        } else {
            stopPollingService(context, onlyRefreshNormalView);
            Intent intent = new Intent(context, PollingAlarmService.class);
            context.startService(intent);
        }
    }

    public static void stopPollingService(Context context, boolean onlyRefreshNormalView) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), true)) {
            startPermanentService(context, sharedPreferences, onlyRefreshNormalView);
        } else {
            context.stopService(new Intent(context, PollingAlarmService.class));
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
                && !NormalNotificationUtils.isEnable(context)
                && !TileHelper.isEnable(context);
    }

    /** <br> forecast. */

    public static void startForecastService(Context context, boolean today, boolean onlyRefreshNormalView) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), true)) {
            startPermanentService(context, sharedPreferences, onlyRefreshNormalView);
        } else {
            stopForecastService(context, today, onlyRefreshNormalView);
            Intent intent = new Intent(
                    context,
                    today ? TodayForecastAlarmService.class : TomorrowForecastAlarmService.class);
            context.startService(intent);
        }
    }

    public static void stopForecastService(Context context, boolean today, boolean onlyRefreshNormalView) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(context.getString(R.string.key_permanent_service), true)) {
            startPermanentService(context, sharedPreferences, onlyRefreshNormalView);
        } else {
            if (today) {
                context.stopService(new Intent(context, TodayForecastAlarmService.class));
            } else {
                context.stopService(new Intent(context, TomorrowForecastAlarmService.class));
            }
            GeoAlarmService.cancelAlarmIntent(
                    context,
                    today ? TodayForecastAlarmService.class : TomorrowForecastAlarmService.class,
                    today ? TodayForecastAlarmService.ALARM_CODE : TomorrowForecastAlarmService.ALARM_CODE);
        }
    }
}
