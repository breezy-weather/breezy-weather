package wangdaye.com.geometricweather.background.polling;

import android.content.Context;
import android.os.Build;

import java.util.List;

import wangdaye.com.geometricweather.background.polling.services.permanent.PermanentServiceHelper;
import wangdaye.com.geometricweather.background.polling.work.WorkerHelper;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;
import wangdaye.com.geometricweather.common.utils.helpers.IntentHelper;
import wangdaye.com.geometricweather.db.repositories.LocationEntityRepository;
import wangdaye.com.geometricweather.db.repositories.WeatherEntityRepository;
import wangdaye.com.geometricweather.remoteviews.NotificationHelper;
import wangdaye.com.geometricweather.remoteviews.WidgetHelper;
import wangdaye.com.geometricweather.settings.SettingsManager;

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
                List<Location> locationList = LocationEntityRepository.INSTANCE.readLocationList();
                for (int i = 0; i < locationList.size(); i ++) {
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
