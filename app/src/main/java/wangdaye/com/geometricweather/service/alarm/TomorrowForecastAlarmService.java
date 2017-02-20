package wangdaye.com.geometricweather.service.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoAlarmService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;

/**
 * Today forecast alarm service.
 * */

public class TomorrowForecastAlarmService extends GeoAlarmService {
    // data
    public static final int ALARM_CODE = 3;

    /** <br> life cycle. */

    @Override
    protected void doRefresh(Location location) {
        if (ForecastNotificationUtils.isEnable(this, false)) {
            setAlarmIntent(this, TomorrowForecastAlarmService.class);
            if (ForecastNotificationUtils.isForecastTime(this, false)) {
                requestData(location);
            } else {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        ForecastNotificationUtils.buildForecastAndSendIt(context, weather, false);
    }

    public void setAlarmIntent(Context context, Class<?> cls) {
        if (!PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.key_permanent_service), true)) {
            Intent target = new Intent(context, cls);
            PendingIntent pendingIntent = PendingIntent.getService(
                    context,
                    ALARM_CODE,
                    target,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                    .set(
                            AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            SystemClock.elapsedRealtime() + ForecastNotificationUtils.calcForecastDuration(context, false, false),
                            pendingIntent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
