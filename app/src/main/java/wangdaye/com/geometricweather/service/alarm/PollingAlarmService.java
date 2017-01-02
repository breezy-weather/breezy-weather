package wangdaye.com.geometricweather.service.alarm;

import android.content.Context;

import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.remoteView.NormalNotificationUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayCenterUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetClockDayWeekUtils;
import wangdaye.com.geometricweather.utils.remoteView.WidgetDayPixelUtils;
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

    public PollingAlarmService() {
        super("PollingAlarmService");
    }

    public PollingAlarmService(String name) {
        super(name);
    }

    @Override
    protected void doRefresh(Location location) {
        if (WidgetDayUtils.isEnable(this)
                || WidgetDayPixelUtils.isEnable(this)
                || WidgetWeekUtils.isEnable(this)
                || WidgetDayWeekUtils.isEnable(this)
                || WidgetClockDayUtils.isEnable(this)
                || WidgetClockDayCenterUtils.isEnable(this)
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
        if (WidgetDayPixelUtils.isEnable(context)) {
            WidgetDayPixelUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetWeekUtils.isEnable(context)) {
            WidgetWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetDayWeekUtils.isEnable(context)) {
            WidgetDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayUtils.isEnable(context)) {
            WidgetClockDayUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayCenterUtils.isEnable(context)) {
            WidgetClockDayCenterUtils.refreshWidgetView(context, location, weather);
        }
        if (WidgetClockDayWeekUtils.isEnable(context)) {
            WidgetClockDayWeekUtils.refreshWidgetView(context, location, weather);
        }
        if (NormalNotificationUtils.isEnable(context)) {
            NormalNotificationUtils.buildNotificationAndSendIt(context, weather);
        }
    }
}
