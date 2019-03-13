package wangdaye.com.geometricweather.background.service.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.PollingTaskHelper;
import wangdaye.com.geometricweather.remote.utils.ForecastNotificationUtils;

/**
 * Alarm Today forecast update service.
 * */

public class AlarmTodayForecastUpdateService extends UpdateService {

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
            PollingTaskHelper.startTodayForecastPollingTask(this, todayForecastTime);
        }
    }
}
