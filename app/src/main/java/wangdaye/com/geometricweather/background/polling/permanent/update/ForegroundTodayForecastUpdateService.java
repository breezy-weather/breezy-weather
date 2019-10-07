package wangdaye.com.geometricweather.background.polling.permanent.update;

import android.content.Context;

import androidx.core.app.NotificationCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.polling.basic.ForegroundUpdateService;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.remoteviews.presenter.notification.ForecastNotificationIMP;

/**
 * Foreground Today forecast update service.
 * */

public class ForegroundTodayForecastUpdateService extends ForegroundUpdateService {

    @Override
    public void updateView(Context context, Location location) {
        if (ForecastNotificationIMP.isEnable(this, true)) {
            ForecastNotificationIMP.buildForecastAndSendIt(context, location, true);
        }
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
        return GeometricWeather.NOTIFICATION_ID_UPDATING_TODAY_FORECAST;
    }
}
