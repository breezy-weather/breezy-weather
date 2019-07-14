package wangdaye.com.geometricweather.background.polling;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.permanent.PermanentServiceHelper;
import wangdaye.com.geometricweather.background.polling.work.WorkerHelper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.utils.helpter.IntentHelper;

/**
 * Background manager.
 * */
public class PollingManager {

    public static void resetAllBackgroundTask(Context context, boolean forceRefresh) {
        if (forceRefresh) {
            IntentHelper.startAwakeForegroundUpdateService(context);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (backgroundFree) {
            PermanentServiceHelper.stopPollingService(context);

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

            WorkerHelper.setNormalPollingWork(
                    ValueUtils.getPollingRateScale(
                            SettingsOptionManager.getInstance(context).getUpdateInterval()
                    )
            );

            if (openTodayForecast) {
                WorkerHelper.setTodayForecastUpdateWork(todayForecastTime);
            } else {
                WorkerHelper.cancelTodayForecastUpdateWork();
            }

            if (openTomorrowForecast) {
                WorkerHelper.setTomorrowForecastUpdateWork(tomorrowForecastTime);
            } else {
                WorkerHelper.cancelTomorrowForecastUpdateWork();
            }
        } else {
            WorkerHelper.cancelNormalPollingWork();
            WorkerHelper.cancelTodayForecastUpdateWork();
            WorkerHelper.cancelTomorrowForecastUpdateWork();

            PermanentServiceHelper.startPollingService(context);
        }
    }

    public static void resetNormalBackgroundTask(Context context, boolean forceRefresh) {
        if (forceRefresh) {
            IntentHelper.startAwakeForegroundUpdateService(context);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (backgroundFree) {
            PermanentServiceHelper.stopPollingService(context);

            WorkerHelper.setNormalPollingWork(
                    ValueUtils.getPollingRateScale(
                            SettingsOptionManager.getInstance(context).getUpdateInterval()
                    )
            );
        } else {
            WorkerHelper.cancelNormalPollingWork();
            WorkerHelper.cancelTodayForecastUpdateWork();
            WorkerHelper.cancelTomorrowForecastUpdateWork();

            PermanentServiceHelper.startPollingService(context);
        }
    }

    public static void resetTodayForecastBackgroundTask(Context context, boolean forceRefresh) {
        if (forceRefresh) {
            IntentHelper.startAwakeForegroundUpdateService(context);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (backgroundFree) {
            PermanentServiceHelper.stopPollingService(context);

            boolean openTodayForecast = sharedPreferences.getBoolean(
                    context.getString(R.string.key_forecast_today),
                    false
            );
            String todayForecastTime = sharedPreferences.getString(
                    context.getString(R.string.key_forecast_today_time),
                    SettingsOptionManager.DEFAULT_TODAY_FORECAST_TIME
            );

            if (openTodayForecast) {
                WorkerHelper.setTodayForecastUpdateWork(todayForecastTime);
            } else {
                WorkerHelper.cancelTodayForecastUpdateWork();
            }
        } else {
            WorkerHelper.cancelNormalPollingWork();
            WorkerHelper.cancelTodayForecastUpdateWork();
            WorkerHelper.cancelTomorrowForecastUpdateWork();

            PermanentServiceHelper.startPollingService(context);
        }
    }

    public static void resetTomorrowForecastBackgroundTask(Context context, boolean forceRefresh) {
        if (forceRefresh) {
            IntentHelper.startAwakeForegroundUpdateService(context);
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (backgroundFree) {
            PermanentServiceHelper.stopPollingService(context);

            boolean openTomorrowForecast = sharedPreferences.getBoolean(
                    context.getString(R.string.key_forecast_tomorrow),
                    false
            );
            String tomorrowForecastTime = sharedPreferences.getString(
                    context.getString(R.string.key_forecast_tomorrow_time),
                    SettingsOptionManager.DEFAULT_TOMORROW_FORECAST_TIME
            );

            if (openTomorrowForecast) {
                WorkerHelper.setTomorrowForecastUpdateWork(tomorrowForecastTime);
            } else {
                WorkerHelper.cancelTomorrowForecastUpdateWork();
            }
        } else {
            WorkerHelper.cancelNormalPollingWork();
            WorkerHelper.cancelTodayForecastUpdateWork();
            WorkerHelper.cancelTomorrowForecastUpdateWork();

            PermanentServiceHelper.startPollingService(context);
        }
    }
}
