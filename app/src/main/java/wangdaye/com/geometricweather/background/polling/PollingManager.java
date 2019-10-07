package wangdaye.com.geometricweather.background.polling;

import android.content.Context;

import wangdaye.com.geometricweather.background.polling.permanent.PermanentServiceHelper;
import wangdaye.com.geometricweather.background.polling.work.WorkerHelper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
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

        SettingsOptionManager settings = SettingsOptionManager.getInstance(context);
        if (settings.isBackgroundFree()) {
            PermanentServiceHelper.stopPollingService(context);

            WorkerHelper.setNormalPollingWork(
                    SettingsOptionManager.getInstance(context).getUpdateInterval().getIntervalInHour());

            if (settings.isTodayForecastEnabled()) {
                WorkerHelper.setTodayForecastUpdateWork(settings.getTodayForecastTime(), false);
            } else {
                WorkerHelper.cancelTodayForecastUpdateWork();
            }

            if (settings.isTomorrowForecastEnabled()) {
                WorkerHelper.setTomorrowForecastUpdateWork(settings.getTomorrowForecastTime(), false);
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

        if (SettingsOptionManager.getInstance(context).isBackgroundFree()) {
            PermanentServiceHelper.stopPollingService(context);

            WorkerHelper.setNormalPollingWork(
                    SettingsOptionManager.getInstance(context).getUpdateInterval().getIntervalInHour());
        } else {
            WorkerHelper.cancelNormalPollingWork();
            WorkerHelper.cancelTodayForecastUpdateWork();
            WorkerHelper.cancelTomorrowForecastUpdateWork();

            PermanentServiceHelper.startPollingService(context);
        }
    }

    public static void resetTodayForecastBackgroundTask(Context context, boolean forceRefresh,
                                                        boolean nextDay) {
        if (forceRefresh) {
            IntentHelper.startAwakeForegroundUpdateService(context);
            return;
        }

        SettingsOptionManager settings = SettingsOptionManager.getInstance(context);
        if (settings.isBackgroundFree()) {
            PermanentServiceHelper.stopPollingService(context);

            if (settings.isTodayForecastEnabled()) {
                WorkerHelper.setTodayForecastUpdateWork(settings.getTodayForecastTime(), nextDay);
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

    public static void resetTomorrowForecastBackgroundTask(Context context, boolean forceRefresh,
                                                           boolean nextDay) {
        if (forceRefresh) {
            IntentHelper.startAwakeForegroundUpdateService(context);
            return;
        }

        SettingsOptionManager settings = SettingsOptionManager.getInstance(context);
        if (settings.isBackgroundFree()) {
            PermanentServiceHelper.stopPollingService(context);

            if (settings.isTomorrowForecastEnabled()) {
                WorkerHelper.setTomorrowForecastUpdateWork(settings.getTomorrowForecastTime(), nextDay);
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
