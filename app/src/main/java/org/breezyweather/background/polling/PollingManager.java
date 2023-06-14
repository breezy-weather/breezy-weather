package org.breezyweather.background.polling;

import android.content.Context;
import android.os.Build;

import java.util.List;

import org.breezyweather.background.polling.services.permanent.PermanentServiceHelper;
import org.breezyweather.background.polling.work.WorkerHelper;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.common.utils.helpers.IntentHelper;
import org.breezyweather.db.repositories.LocationEntityRepository;
import org.breezyweather.db.repositories.WeatherEntityRepository;
import org.breezyweather.remoteviews.NotificationHelper;
import org.breezyweather.remoteviews.WidgetHelper;
import org.breezyweather.common.utils.helpers.AsyncHelper;
import org.breezyweather.settings.SettingsManager;

public class PollingManager {

    public static void resetAllBackgroundTask(Context context, boolean forceRefresh) {
        SettingsManager settings = SettingsManager.getInstance(context);

        if (forceRefresh) {
            forceRefresh(context);
            return;
        }

        if (settings.isBackgroundFree()) {
            PermanentServiceHelper.stopPollingService(context);

            if (SettingsManager.getInstance(context).getUpdateInterval().getIntervalInHour() != null) {
                WorkerHelper.setNormalPollingWork(
                        context,
                        SettingsManager.getInstance(context).getUpdateInterval().getIntervalInHour());
            } else {
                WorkerHelper.cancelNormalPollingWork(context);
            }

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

            // Polling service is started only if there is a need for one of the background services
            if (SettingsManager.getInstance(context).getUpdateInterval().getIntervalInHour() != null
                || settings.isTodayForecastEnabled()
                || settings.isTomorrowForecastEnabled()) {
                PermanentServiceHelper.startPollingService(context);
            } else {
                PermanentServiceHelper.stopPollingService(context);
            }
        }
    }

    public static void resetNormalBackgroundTask(Context context, boolean forceRefresh) {
        SettingsManager settings = SettingsManager.getInstance(context);

        if (forceRefresh) {
            forceRefresh(context);
            return;
        }

        if (settings.isBackgroundFree()) {
            PermanentServiceHelper.stopPollingService(context);

            if (SettingsManager.getInstance(context).getUpdateInterval().getIntervalInHour() != null) {
                WorkerHelper.setNormalPollingWork(
                        context,
                        SettingsManager.getInstance(context).getUpdateInterval().getIntervalInHour());
            } else {
                WorkerHelper.cancelNormalPollingWork(context);
            }
        } else {
            WorkerHelper.cancelNormalPollingWork(context);
            WorkerHelper.cancelTodayForecastUpdateWork(context);
            WorkerHelper.cancelTomorrowForecastUpdateWork(context);

            // Polling service is started only if there is a need for one of the background services
            if (SettingsManager.getInstance(context).getUpdateInterval().getIntervalInHour() != null
                    || settings.isTodayForecastEnabled()
                    || settings.isTomorrowForecastEnabled()) {
                PermanentServiceHelper.startPollingService(context);
            } else {
                PermanentServiceHelper.stopPollingService(context);
            }
        }
    }

    public static void resetTodayForecastBackgroundTask(Context context, boolean forceRefresh,
                                                        boolean nextDay) {
        SettingsManager settings = SettingsManager.getInstance(context);

        if (forceRefresh) {
            forceRefresh(context);
            return;
        }

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

            // Polling service is started only if there is a need for one of the background services
            if (SettingsManager.getInstance(context).getUpdateInterval().getIntervalInHour() != null
                    || settings.isTodayForecastEnabled()
                    || settings.isTomorrowForecastEnabled()) {
                PermanentServiceHelper.startPollingService(context);
            } else {
                PermanentServiceHelper.stopPollingService(context);
            }
        }
    }

    public static void resetTomorrowForecastBackgroundTask(Context context, boolean forceRefresh,
                                                           boolean nextDay) {
        SettingsManager settings = SettingsManager.getInstance(context);

        if (forceRefresh) {
            forceRefresh(context);
            return;
        }

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

            // Polling service is started only if there is a need for one of the background services
            if (SettingsManager.getInstance(context).getUpdateInterval().getIntervalInHour() != null
                    || settings.isTodayForecastEnabled()
                    || settings.isTomorrowForecastEnabled()) {
                PermanentServiceHelper.startPollingService(context);
            } else {
                PermanentServiceHelper.stopPollingService(context);
            }
        }
    }

    private static void forceRefresh(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AsyncHelper.runOnIO(() -> {
                List<Location> locationList = LocationEntityRepository.INSTANCE.readLocationList(context);
                for (int i = 0; i < locationList.size(); i++) {
                    locationList.set(
                            i, Location.copy(
                                    locationList.get(i),
                                    WeatherEntityRepository.INSTANCE.readWeather(locationList.get(i))
                            )
                    );
                }

                WidgetHelper.updateWidgetIfNecessary(context, locationList.get(0));
                WidgetHelper.updateWidgetIfNecessary(context, locationList);
                NotificationHelper.updateNotificationIfNecessary(context, locationList);
            });

            WorkerHelper.setExpeditedPollingWork(context);
        } else {
            IntentHelper.startAwakeForegroundUpdateService(context);
        }
    }
}
