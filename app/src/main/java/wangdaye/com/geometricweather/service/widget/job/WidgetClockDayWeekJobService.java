package wangdaye.com.geometricweather.service.widget.job;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoJobService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayWeekProvider;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetClockDayWeekAlarmService;

/**
 * Widget clock day week job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class WidgetClockDayWeekJobService extends GeoJobService {
    // data.
    public static final int SCHEDULE_CODE = 36;

    /** <br> life cycle. */

    @Override
    public String readSettings() {
        return getSharedPreferences(getString(R.string.sp_widget_clock_day_week_setting), MODE_PRIVATE)
                .getString(getString(R.string.key_location), getString(R.string.local));
    }

    @Override
    protected void doRefresh(Location location) {
        int[] widgetIds = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, WidgetClockDayWeekProvider.class));
        if (widgetIds != null && widgetIds.length != 0) {
            requestData(location);
        }
    }

    @Override
    protected void updateView(Context context, Location location, Weather weather) {
        refreshWidgetView(context, location, weather);
    }

    /** <br> widget. */

    public static void refreshWidgetView(Context context, Location location, Weather weather) {
        WidgetClockDayWeekAlarmService.refreshWidgetView(context, location, weather);
    }
}
