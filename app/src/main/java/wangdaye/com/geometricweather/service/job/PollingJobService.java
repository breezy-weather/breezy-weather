package wangdaye.com.geometricweather.service.job;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import wangdaye.com.geometricweather.basic.GeoJobService;
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
 * Polling job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollingJobService extends GeoJobService {
    // data
    public static final int SCHEDULE_CODE = 4;

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

            requestData(location);
        }
    }

    @Override
    protected void updateView(Context context, Location location, Weather weather) {
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
}
