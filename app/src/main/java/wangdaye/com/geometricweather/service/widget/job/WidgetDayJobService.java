package wangdaye.com.geometricweather.service.widget.job;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoJobService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.receiver.widget.WidgetDayProvider;
import wangdaye.com.geometricweather.service.widget.alarm.WidgetDayAlarmService;

/**
 * Widget day job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class WidgetDayJobService extends GeoJobService {
    // data.
    public static final int SCHEDULE_CODE = 31;

    /** <br> life cycle. */

    @Override
    public String readSettings() {
        return getSharedPreferences(getString(R.string.sp_widget_day_setting), MODE_PRIVATE)
                .getString(getString(R.string.key_location), getString(R.string.local));
    }

    @Override
    protected void doRefresh(Location location) {
        int[] widgetIds = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, WidgetDayProvider.class));
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
        WidgetDayAlarmService.refreshWidgetView(context, location, weather);
    }
}
