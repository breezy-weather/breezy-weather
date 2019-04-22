package wangdaye.com.geometricweather.background.polling.alarm;

import android.content.Context;

import androidx.annotation.Nullable;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.background.service.UpdateService;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.polling.PollingTaskHelper;
import wangdaye.com.geometricweather.remoteviews.NotificationUtils;
import wangdaye.com.geometricweather.remoteviews.WidgetUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

/**
 * Alarm normal update service.
 * */

public class AlarmNormalUpdateService extends UpdateService {

    @Override
    public void updateView(Context context, Location location,
                           @Nullable Weather weather, @Nullable History history) {
        WidgetUtils.refreshWidgetIfNecessary(context, location, weather, history);
        NotificationUtils.refreshNotificationIfNecessary(context, weather);
    }

    @Override
    public void setDelayTask(boolean failed) {
        if (failed) {
            PollingTaskHelper.startNormalPollingTask(this, 0.25F);
        } else {
            PollingTaskHelper.startNormalPollingTask(
                    this,
                    ValueUtils.getRefreshRateScale(
                            GeometricWeather.getInstance().getUpdateInterval()
                    )
            );
        }
    }
}
