package wangdaye.com.geometricweather.background;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.service.polling.AwakeForegroundUpdateService;
import wangdaye.com.geometricweather.background.service.polling.TimeObserverService;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Service helper.
 * */

public class ServiceHelper {

    public static void startPollingService(Context context, boolean updateSettings) {
        startPollingService(context, updateSettings, true);
    }

    public static void startPollingService(Context context,
                                           boolean updateSettings, boolean updateResult) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (!backgroundFree) {
            Intent intent = new Intent(context, TimeObserverService.class);
            intent.putExtra(TimeObserverService.KEY_UPDATE_SETTINGS, updateSettings);
            intent.putExtra(TimeObserverService.KEY_UPDATE_RESULT, updateResult);
            if (updateSettings) {
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
                        GeometricWeather.DEFAULT_TODAY_FORECAST_TIME
                );
                String tomorrowForecastTime = sharedPreferences.getString(
                        context.getString(R.string.key_forecast_tomorrow_time),
                        GeometricWeather.DEFAULT_TOMORROW_FORECAST_TIME
                );
                intent.putExtra(
                        TimeObserverService.KEY_POLLING_RATE,
                        ValueUtils.getRefreshRateScale(
                                GeometricWeather.getInstance().getUpdateInterval()
                        )
                );
                intent.putExtra(
                        TimeObserverService.KEY_TODAY_FORECAST_TIME,
                        openTodayForecast ? todayForecastTime : ""
                );
                intent.putExtra(
                        TimeObserverService.KEY_TOMORROW_FORECAST_TIME,
                        openTomorrowForecast ? tomorrowForecastTime : ""
                );
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    public static void stopPollingService(Context context) {
        Intent intent = new Intent(context, TimeObserverService.class);
        context.stopService(intent);
    }

    public static void startAwakePollingUpdateService(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(getAwakePollingUpdateServiceIntent(context));
        } else {
            context.startService(getAwakePollingUpdateServiceIntent(context));
        }
    }

    public static Intent getAwakePollingUpdateServiceIntent(Context context) {
        return new Intent(context, AwakeForegroundUpdateService.class);
    }
}
