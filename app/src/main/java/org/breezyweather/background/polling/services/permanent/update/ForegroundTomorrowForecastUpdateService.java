package org.breezyweather.background.polling.services.permanent.update;

import android.content.Context;

import androidx.core.app.NotificationCompat;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import org.breezyweather.BreezyWeather;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.remoteviews.presenters.notification.ForecastNotificationIMP;
import org.breezyweather.R;
import org.breezyweather.background.polling.services.basic.ForegroundUpdateService;

/**
 * Foreground Today forecast update service.
 * */

@AndroidEntryPoint
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
    public NotificationCompat.Builder getForegroundNotification(int total) {
        return super.getForegroundNotification(total).setContentTitle(
                getString(R.string.breezy_weather) + " " + getString(R.string.forecast)
        );
    }

    @Override
    public int getForegroundNotificationId() {
        return BreezyWeather.NOTIFICATION_ID_UPDATING_TOMORROW_FORECAST;
    }
}