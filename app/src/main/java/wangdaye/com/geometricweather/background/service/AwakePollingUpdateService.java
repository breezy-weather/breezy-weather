package wangdaye.com.geometricweather.background.service;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.service.polling.PollingUpdateService;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.BackgroundManager;
import wangdaye.com.geometricweather.remote.utils.NormalNotificationUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayDetailsUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayHorizontalUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayVerticalUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayWeekUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetDayUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetDayWeekUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetTextUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetWeekUtils;

/**
 * Awake polling update service.
 * */
public class AwakePollingUpdateService extends PollingUpdateService {

    @Override
    public void updateView(Context context, Location location, @Nullable Weather weather) {
        if (WidgetDayUtils.isEnable(context)) {
            WidgetDayUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetWeekUtils.isEnable(context)) {
            WidgetWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetDayWeekUtils.isEnable(context)) {
            WidgetDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayHorizontalUtils.isEnable(context)) {
            WidgetClockDayHorizontalUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayDetailsUtils.isEnable(context)) {
            WidgetClockDayDetailsUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayVerticalUtils.isEnable(context)) {
            WidgetClockDayVerticalUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayWeekUtils.isEnable(context)) {
            WidgetClockDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetTextUtils.isEnable(context)) {
            WidgetTextUtils.refreshWidgetView(context, location, weather);
        }
        if (NormalNotificationUtils.isEnable(context)) {
            NormalNotificationUtils.buildNotificationAndSendIt(context, weather);
        }
    }

    @Override
    public void setDelayTask(boolean failed) {
        BackgroundManager.resetAllBackgroundTask(this, false);
    }

    @Override
    public Notification getForegroundNotification() {
        return new NotificationCompat.Builder(this, GeometricWeather.NOTIFICATION_CHANNEL_ID_BACKGROUND)
                .setSmallIcon(R.drawable.ic_running_in_background)
                .setContentTitle(getString(R.string.geometric_weather))
                .setContentText(getString(R.string.feedback_updating_weather_data))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setProgress(0, 0, true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(false)
                .build();
    }

    @Override
    public int getForegroundNotificationId() {
        return GeometricWeather.NOTIFICATION_ID_UPDATING_NORMALLY;
    }
}
