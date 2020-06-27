package com.mbestavros.geometricweather.background.polling;

import android.content.Context;

import com.mbestavros.geometricweather.background.polling.permanent.PermanentServiceHelper;
import com.mbestavros.geometricweather.background.polling.work.WorkerHelper;
import com.mbestavros.geometricweather.settings.SettingsOptionManager;
import com.mbestavros.geometricweather.utils.helpter.IntentHelper;

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
                    context,
                    SettingsOptionManager.getInstance(context).getUpdateInterval().getIntervalInHour());

            if (settings.isTodayForecastEnabled()) {
                WorkerHelper.setTodayForecastUpdateWork(context, settings.getTodayForecastTime(), false);
            } else {
                WorkerHelper.cancelTodayForecastUpdateWork(context);
            }

            if (settings.isTomorrowForecastEnabled()) {
                WorkerHelper.setTomorrowForecastUpdateWork(context, settings.getTomorrowForecastTime(), false);
            } else {
                WorkerHelper.cancelTomorrowForecastUpdateWork(context);
            }
        } else {
            WorkerHelper.cancelNormalPollingWork(context);
            WorkerHelper.cancelTodayForecastUpdateWork(context);
            WorkerHelper.cancelTomorrowForecastUpdateWork(context);

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
                    context,
                    SettingsOptionManager.getInstance(context).getUpdateInterval().getIntervalInHour());
        } else {
            WorkerHelper.cancelNormalPollingWork(context);
            WorkerHelper.cancelTodayForecastUpdateWork(context);
            WorkerHelper.cancelTomorrowForecastUpdateWork(context);

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
                WorkerHelper.setTodayForecastUpdateWork(context, settings.getTodayForecastTime(), nextDay);
            } else {
                WorkerHelper.cancelTodayForecastUpdateWork(context);
            }
        } else {
            WorkerHelper.cancelNormalPollingWork(context);
            WorkerHelper.cancelTodayForecastUpdateWork(context);
            WorkerHelper.cancelTomorrowForecastUpdateWork(context);

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
                WorkerHelper.setTomorrowForecastUpdateWork(context, settings.getTomorrowForecastTime(), nextDay);
            } else {
                WorkerHelper.cancelTomorrowForecastUpdateWork(context);
            }
        } else {
            WorkerHelper.cancelNormalPollingWork(context);
            WorkerHelper.cancelTodayForecastUpdateWork(context);
            WorkerHelper.cancelTomorrowForecastUpdateWork(context);

            PermanentServiceHelper.startPollingService(context);
        }
    }
}
