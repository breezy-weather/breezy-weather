package wangdaye.com.geometricweather.background.service.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.PollingTaskHelper;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.remote.utils.NormalNotificationUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayDetailsUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayVerticalUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayHorizontalUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetClockDayWeekUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetDayUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetDayWeekUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetTextUtils;
import wangdaye.com.geometricweather.remote.utils.WidgetWeekUtils;

/**
 * Alarm normal update service.
 * */

public class AlarmNormalUpdateService extends UpdateService {

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
        if (failed) {
            PollingTaskHelper.startNormalPollingTask(this, 0.25F);
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String refreshRate = sharedPreferences.getString(getString(R.string.key_refresh_rate), "1:30");
            PollingTaskHelper.startNormalPollingTask(this, ValueUtils.getRefreshRateScale(refreshRate));
        }
    }
}
