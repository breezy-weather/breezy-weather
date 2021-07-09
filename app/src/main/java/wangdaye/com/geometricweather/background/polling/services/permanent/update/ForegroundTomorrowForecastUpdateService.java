package wangdaye.com.geometricweather.background.polling.services.permanent.update;

import android.content.Context;

import androidx.core.app.NotificationCompat;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.services.basic.ForegroundUpdateService;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.remoteviews.presenters.notification.ForecastNotificationIMP;

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
        return super.getForegroundNotification(total)
                .setContentTitle(getString(R.string.geometric_weather) + " " + getString(R.string.forecast));
    }

    @Override
    public int getForegroundNotificationId() {
        return GeometricWeather.NOTIFICATION_ID_UPDATING_TOMORROW_FORECAST;
    }
}
