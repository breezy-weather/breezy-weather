package wangdaye.com.geometricweather.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.UpdateService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.AlarmHelper;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;

/**
 * Today forecast updateRotation service.
 * */

public class TodayForecastUpdateService extends UpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (ForecastNotificationUtils.isEnable(this, true)) {
            ForecastNotificationUtils.buildForecastAndSendIt(context, weather, true);
        }
    }

    @Override
    public void setDelayTask(boolean notifyFailed) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean openTodayForecast = sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false);
        String todayForecastTime = sharedPreferences.getString(
                getString(R.string.key_forecast_today_time),
                GeometricWeather.DEFAULT_TODAY_FORECAST_TIME);
        if (openTodayForecast) {
            AlarmHelper.setAlarmForTodayForecast(this, todayForecastTime);
        }
    }
}
