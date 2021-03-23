package wangdaye.com.geometricweather.background.polling.services.basic;

import android.content.Context;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.remoteviews.NotificationHelper;
import wangdaye.com.geometricweather.remoteviews.WidgetHelper;

/**
 * Awake foreground update service.
 * */

@AndroidEntryPoint
public class AwakeForegroundUpdateService extends ForegroundUpdateService {

    @Override
    public void updateView(Context context, Location location) {
        WidgetHelper.updateWidgetIfNecessary(context, location);
    }

    @Override
    public void updateView(Context context, List<Location> locationList) {
        WidgetHelper.updateWidgetIfNecessary(context, locationList);
        NotificationHelper.updateNotificationIfNecessary(context, locationList);
    }

    @Override
    public void handlePollingResult(boolean failed) {
        PollingManager.resetAllBackgroundTask(this, false);
    }

    @Override
    public int getForegroundNotificationId() {
        return GeometricWeather.NOTIFICATION_ID_UPDATING_AWAKE;
    }
}
