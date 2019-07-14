package wangdaye.com.geometricweather.background.polling.permanent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.permanent.observer.TimeObserverService;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Service helper.
 * */

public class PermanentServiceHelper {

    public static void startPollingService(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (!backgroundFree) {
            Intent intent = new Intent(context, TimeObserverService.class);
            intent.putExtra(TimeObserverService.KEY_CONFIG_CHANGED, true);
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
            intent.putExtra(
                    TimeObserverService.KEY_POLLING_RATE,
                    ValueUtils.getPollingRateScale(
                            SettingsOptionManager.getInstance(context).getUpdateInterval()
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    public static void updatePollingService(Context context, boolean pollingFailed) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean backgroundFree = sharedPreferences.getBoolean(context.getString(R.string.key_background_free), true);
        if (!backgroundFree) {
            Intent intent = new Intent(context, TimeObserverService.class);
            intent.putExtra(TimeObserverService.KEY_POLLING_FAILED, pollingFailed);

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
}
