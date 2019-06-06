package wangdaye.com.geometricweather.background;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.PollingTaskHelper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Background manager.
 * */
public class BackgroundManager {

    public static void resetAllBackgroundTask(Context context, boolean forceRefresh) {
        if (forceRefresh) {
            ServiceHelper.startAwakePollingUpdateService(context);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (backgroundFree) {
            ServiceHelper.stopPollingService(context);

            boolean openTodayForecast = sharedPreferences.getBoolean(
                    context.getString(R.string.key_forecast_today),
                    false
            );
            boolean openTomorrowForecast = sharedPreferences.getBoolean(
                    context.getString(R.string.key_forecast_tomorrow),
                    false
            );
            String todayForecastTime = sharedPreferences.getString(
                    context.getString(R.string.key_forecast_today_time),
                    SettingsOptionManager.DEFAULT_TODAY_FORECAST_TIME
            );
            String tomorrowForecastTime = sharedPreferences.getString(
                    context.getString(R.string.key_forecast_tomorrow_time),
                    SettingsOptionManager.DEFAULT_TOMORROW_FORECAST_TIME
            );

            PollingTaskHelper.stopNormalPollingTask(context);
            PollingTaskHelper.startNormalPollingTask(
                    context,
                    ValueUtils.getRefreshRateScale(
                            SettingsOptionManager.getInstance(context).getUpdateInterval()
                    )
            );

            PollingTaskHelper.stopTodayForecastPollingTask(context);
            if (openTodayForecast) {
                PollingTaskHelper.startTodayForecastPollingTask(context, todayForecastTime);
            }

            PollingTaskHelper.stopTomorrowForecastPollingTask(context);
            if (openTomorrowForecast) {
                PollingTaskHelper.startTomorrowForecastPollingTask(context, tomorrowForecastTime);
            }
        } else {
            PollingTaskHelper.stopNormalPollingTask(context);
            PollingTaskHelper.stopTodayForecastPollingTask(context);
            PollingTaskHelper.stopTomorrowForecastPollingTask(context);
            ServiceHelper.startPollingService(context, true);
        }
    }

    public static void resetNormalBackgroundTask(Context context, boolean forceRefresh) {
        if (forceRefresh) {
            ServiceHelper.startAwakePollingUpdateService(context);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (backgroundFree) {
            ServiceHelper.stopPollingService(context);

            PollingTaskHelper.stopNormalPollingTask(context);
            PollingTaskHelper.startNormalPollingTask(
                    context,
                    ValueUtils.getRefreshRateScale(
                            SettingsOptionManager.getInstance(context).getUpdateInterval()
                    )
            );
        } else {
            PollingTaskHelper.stopNormalPollingTask(context);
            PollingTaskHelper.stopTodayForecastPollingTask(context);
            PollingTaskHelper.stopTomorrowForecastPollingTask(context);
            ServiceHelper.startPollingService(context, true);
        }
    }

    public static void resetTodayForecastBackgroundTask(Context context, boolean forceRefresh) {
        if (forceRefresh) {
            ServiceHelper.startAwakePollingUpdateService(context);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (backgroundFree) {
            ServiceHelper.stopPollingService(context);

            boolean openTodayForecast = sharedPreferences.getBoolean(
                    context.getString(R.string.key_forecast_today),
                    false
            );
            String todayForecastTime = sharedPreferences.getString(
                    context.getString(R.string.key_forecast_today_time),
                    SettingsOptionManager.DEFAULT_TODAY_FORECAST_TIME
            );

            PollingTaskHelper.stopTodayForecastPollingTask(context);
            if (openTodayForecast) {
                PollingTaskHelper.startTodayForecastPollingTask(context, todayForecastTime);
            }
        } else {
            PollingTaskHelper.stopNormalPollingTask(context);
            PollingTaskHelper.stopTodayForecastPollingTask(context);
            PollingTaskHelper.stopTomorrowForecastPollingTask(context);
            ServiceHelper.startPollingService(context, true);
        }
    }

    public static void resetTomorrowForecastBackgroundTask(Context context, boolean forceRefresh) {
        if (forceRefresh) {
            ServiceHelper.startAwakePollingUpdateService(context);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (backgroundFree) {
            ServiceHelper.stopPollingService(context);

            boolean openTomorrowForecast = sharedPreferences.getBoolean(
                    context.getString(R.string.key_forecast_tomorrow),
                    false
            );
            String tomorrowForecastTime = sharedPreferences.getString(
                    context.getString(R.string.key_forecast_tomorrow_time),
                    SettingsOptionManager.DEFAULT_TOMORROW_FORECAST_TIME
            );

            PollingTaskHelper.stopTomorrowForecastPollingTask(context);
            if (openTomorrowForecast) {
                PollingTaskHelper.startTomorrowForecastPollingTask(context, tomorrowForecastTime);
            }
        } else {
            PollingTaskHelper.stopNormalPollingTask(context);
            PollingTaskHelper.stopTodayForecastPollingTask(context);
            PollingTaskHelper.stopTomorrowForecastPollingTask(context);
            ServiceHelper.startPollingService(context, true);
        }
    }
}
