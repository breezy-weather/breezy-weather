package wangdaye.com.geometricweather.background.polling.basic;

import android.content.Context;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.polling.PollingManager;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;

/**
 * Awake foreground update service.
 * */
public class AwakeForegroundUpdateService extends ForegroundUpdateService {

    @Override
    public void updateView(Context context, Location location,
                           @Nullable Weather weather, @Nullable History history) {
        WidgetUtils.updateWidgetIfNecessary(context, location, weather, history);
        NotificationUtils.updateNotificationIfNecessary(context, weather);
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
