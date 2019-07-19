package wangdaye.com.geometricweather.background.polling.permanent.update;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
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
    public void setDelayTask(boolean failed) {
        PermanentServiceHelper.updatePollingService(this, failed);
    }

    @Override
    public Notification getForegroundNotification(int index, int total) {
        return new NotificationCompat.Builder(this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND)
                .setSmallIcon(R.drawable.ic_running_in_background)
                .setContentTitle(getString(R.string.geometric_weather))
                .setContentText(getString(R.string.feedback_updating_weather_data) + " (" + index + "/" + total + ")")
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(false)
                .build();
    }

    @Override
    public int getForegroundNotificationId() {
        return GeometricWeather.NOTIFICATION_ID_UPDATING_NORMALLY;
    }
}
