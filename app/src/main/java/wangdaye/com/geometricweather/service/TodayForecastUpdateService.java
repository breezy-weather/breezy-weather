package wangdaye.com.geometricweather.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import wangdaye.com.geometricweather.basic.UpdateService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;

/**
 * Today forecast update service.
 * */

public class TodayForecastUpdateService extends UpdateService {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void doRefresh(Location location) {
        if (ForecastNotificationUtils.isEnable(this, true)
                && ForecastNotificationUtils.isForecastTime(this, true)) {
            requestData(location);
        } else {
            stopSelf();
        }
    }

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        ForecastNotificationUtils.buildForecastAndSendIt(context, weather, true);
    }
}
