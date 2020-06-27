package com.mbestavros.geometricweather.background.polling.basic;

import android.content.Context;

import java.util.List;

import com.mbestavros.geometricweather.GeometricWeather;
import com.mbestavros.geometricweather.basic.model.location.Location;
import com.mbestavros.geometricweather.background.polling.PollingManager;
import com.mbestavros.geometricweather.remoteviews.NotificationUtils;
import com.mbestavros.geometricweather.remoteviews.WidgetUtils;

/**
 * Awake foreground update service.
 * */
public class AwakeForegroundUpdateService extends ForegroundUpdateService {

    @Override
    public void updateView(Context context, Location location) {
        WidgetUtils.updateWidgetIfNecessary(context, location);
        NotificationUtils.updateNotificationIfNecessary(context, location);
    }

    @Override
    public void updateView(Context context, List<Location> locationList) {
        WidgetUtils.updateWidgetIfNecessary(context, locationList);
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
