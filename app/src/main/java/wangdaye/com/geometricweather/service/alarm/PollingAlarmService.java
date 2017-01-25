package wangdaye.com.geometricweather.service.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.remoteView.NormalNotificationUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayVerticalUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayHorizontalUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayWeekUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayWeekUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetWeekUtils;

/**
 * Polling alarm service.
 * */

public class PollingAlarmService extends GeoAlarmService {
    // data
    public static final int ALARM_CODE = 1;

    /** <br> life cycle. */

    @Override
    protected void doRefresh(Location location) {
        if (WidgetDayUtils.isEnable(this)
                || WidgetWeekUtils.isEnable(this)
                || WidgetDayWeekUtils.isEnable(this)
                || WidgetClockDayHorizontalUtils.isEnable(this)
                || WidgetClockDayVerticalUtils.isEnable(this)
                || WidgetClockDayWeekUtils.isEnable(this)
                || NormalNotificationUtils.isEnable(this)) {
            setAlarmIntent(this, PollingAlarmService.class, ALARM_CODE);
            requestData(location);
        }
    }

    @Override
    public void updateView(Context context, Location location, Weather weather) {
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
        if (WidgetClockDayVerticalUtils.isEnable(context)) {
            WidgetClockDayVerticalUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayWeekUtils.isEnable(context)) {
            WidgetClockDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (NormalNotificationUtils.isEnable(context)) {
            NormalNotificationUtils.buildNotificationAndSendIt(context, weather);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
