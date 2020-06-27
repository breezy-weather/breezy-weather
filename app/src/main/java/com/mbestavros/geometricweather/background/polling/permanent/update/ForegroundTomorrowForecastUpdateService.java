package com.mbestavros.geometricweather.background.polling.permanent.update;

import android.content.Context;

import androidx.core.app.NotificationCompat;

import java.util.List;

import com.mbestavros.geometricweather.GeometricWeather;
import com.mbestavros.geometricweather.R;
import com.mbestavros.geometricweather.background.polling.basic.ForegroundUpdateService;
import com.mbestavros.geometricweather.basic.model.location.Location;
import com.mbestavros.geometricweather.remoteviews.presenter.notification.ForecastNotificationIMP;

/**
 * Foreground Today forecast update service.
 * */

public class ForegroundTomorrowForecastUpdateService extends ForegroundUpdateService {

    @Override
    public void updateView(Context context, Location location) {
        if (ForecastNotificationIMP.isEnable(this, false)) {
            ForecastNotificationIMP.buildForecastAndSendIt(context, location, false);
        }
    }

    @Override
    public void updateView(Context context, List<Location> locationList) {
    }

    @Override
    public void handlePollingResult(boolean failed) {
        // do nothing.
    }

    @Override
    public NotificationCompat.Builder getForegroundNotification(int index, int total) {
        return super.getForegroundNotification(index, total)
                .setContentTitle(getString(R.string.geometric_weather) + " " + getString(R.string.forecast));
    }

    @Override
    public int getForegroundNotificationId() {
        return GeometricWeather.NOTIFICATION_ID_UPDATING_TOMORROW_FORECAST;
    }
}
