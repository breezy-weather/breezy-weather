package wangdaye.com.geometricweather.background.polling.permanent;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import wangdaye.com.geometricweather.background.polling.permanent.observer.TimeObserverService;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Service helper.
 * */

public class PermanentServiceHelper {

    public static void startPollingService(Context context) {
        SettingsOptionManager settings = SettingsOptionManager.getInstance(context);
        if (!settings.isBackgroundFree()) {
            Intent intent = new Intent(context, TimeObserverService.class)
                    .putExtra(TimeObserverService.KEY_CONFIG_CHANGED, true)
                    .putExtra(
                            TimeObserverService.KEY_POLLING_RATE,
                            settings.getUpdateInterval().getIntervalInHour()
                    ).putExtra(
                            TimeObserverService.KEY_TODAY_FORECAST_TIME,
                            settings.isTodayForecastEnabled()
                                    ? settings.getTodayForecastTime()
                                    : ""
                    ).putExtra(
                            TimeObserverService.KEY_TOMORROW_FORECAST_TIME,
                            settings.isTomorrowForecastEnabled()
                                    ? settings.getTomorrowForecastTime()
                                    : ""
                    );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    public static void updatePollingService(Context context, boolean pollingFailed) {
        if (!SettingsOptionManager.getInstance(context).isBackgroundFree()) {
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
