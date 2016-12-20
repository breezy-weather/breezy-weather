package wangdaye.com.geometricweather.service.notification.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;

/**
 * Today forecast alarm service.
 * */

public class TodayForecastAlarmService extends GeoAlarmService {
    // data
    public static final int ALARM_CODE = 8;

    /** <br> life cycle. */

    public TodayForecastAlarmService() {
        super("TodayForecastAlarmService");
    }

    public TodayForecastAlarmService(String name) {
        super(name);
    }

    @Override
    protected String readSettings() {
        return null;
    }

    @Override
    protected Location readLocation(String locationName) {
        return DatabaseHelper.getInstance(this).readLocationList().get(0);
    }

    @Override
    protected void doRefresh(Location location) {
        if (NotificationUtils.isForecastTime(this, true)) {
            requestData(location);
        }
        setAlarmIntent(this, getClass(), ALARM_CODE);
    }

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        NotificationUtils.buildForecastAndSendIt(context, weather, true);
    }

    @Override
    public void setAlarmIntent(Context context, Class<?> cls, int requestCode) {
        Intent target = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                requestCode,
                target,
                PendingIntent.FLAG_UPDATE_CURRENT);

        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + NotificationUtils.calcForecastDuration(context, true, false),
                        pendingIntent);
    }

    /** <br> interface. */

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
    }
}
