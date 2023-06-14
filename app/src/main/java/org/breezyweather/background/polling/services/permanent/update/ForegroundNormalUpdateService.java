package org.breezyweather.background.polling.services.permanent.update;

import android.content.Context;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import org.breezyweather.BreezyWeather;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.remoteviews.NotificationHelper;
import org.breezyweather.remoteviews.WidgetHelper;
import org.breezyweather.background.polling.services.basic.ForegroundUpdateService;
import org.breezyweather.background.polling.services.permanent.PermanentServiceHelper;

/**
 * Foreground normal update service.
 * */

@AndroidEntryPoint
public class ForegroundNormalUpdateService extends ForegroundUpdateService {

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
        PermanentServiceHelper.updatePollingService(this, failed);
    }

    @Override
    public int getForegroundNotificationId() {
        return BreezyWeather.NOTIFICATION_ID_UPDATING_NORMALLY;
    }
}
