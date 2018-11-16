package wangdaye.com.geometricweather.service.polling;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.PollingUpdateService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;

/**
 * Polling Today forecast update service.
 * */

public class PollingTomorrowForecastUpdateService extends PollingUpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (ForecastNotificationUtils.isEnable(this, false)) {
            ForecastNotificationUtils.buildForecastAndSendIt(context, weather, false);
        }
    }

    @Override
    public void setDelayTask(boolean notifyFailed) {
        // do nothing.
    }

    @Override
    public Notification getForegroundNotification() {
        return new NotificationCompat.Builder(this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND)
                .setSmallIcon(R.drawable.ic_running_in_background)
                .setContentTitle(getString(R.string.geometric_weather) + " " + getString(R.string.forecast))
                .setContentText(getString(R.string.feedback_updating_weather_data))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(false)
                .build();
    }

    @Override
    public int getForegroundNotificationId() {
        return GeometricWeather.NOTIFICATION_ID_UPDATING_TOMORROW_FORECAST;
    }
}
