package com.mbestavros.geometricweather.background.polling.permanent.update;

import android.content.Context;

import java.util.List;

import com.mbestavros.geometricweather.GeometricWeather;
import com.mbestavros.geometricweather.background.polling.basic.ForegroundUpdateService;
import com.mbestavros.geometricweather.basic.model.location.Location;
import com.mbestavros.geometricweather.background.polling.permanent.PermanentServiceHelper;
import com.mbestavros.geometricweather.remoteviews.NotificationUtils;
import com.mbestavros.geometricweather.remoteviews.WidgetUtils;

/**
 * Foreground normal update service.
 * */
public class ForegroundNormalUpdateService extends ForegroundUpdateService {

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
        PermanentServiceHelper.updatePollingService(this, failed);
    }

    @Override
    public int getForegroundNotificationId() {
        return GeometricWeather.NOTIFICATION_ID_UPDATING_NORMALLY;
    }
}
