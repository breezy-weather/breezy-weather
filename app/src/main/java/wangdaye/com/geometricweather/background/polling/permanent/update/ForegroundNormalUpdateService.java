package wangdaye.com.geometricweather.background.polling.permanent.update;

import android.content.Context;
import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.background.polling.basic.ForegroundUpdateService;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.polling.permanent.PermanentServiceHelper;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;

/**
 * Foreground normal update service.
 * */
public class ForegroundNormalUpdateService extends ForegroundUpdateService {

    @Override
    public void updateView(Context context, Location location,
                           @Nullable Weather weather, @Nullable History history) {
        WidgetUtils.updateWidgetIfNecessary(context, location, weather, history);
        NotificationUtils.updateNotificationIfNecessary(context, weather);
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
