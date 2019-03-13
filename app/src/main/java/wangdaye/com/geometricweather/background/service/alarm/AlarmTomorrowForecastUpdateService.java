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

public class AlarmTomorrowForecastUpdateService extends UpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (ForecastNotificationUtils.isEnable(this, false)) {
            ForecastNotificationUtils.buildForecastAndSendIt(context, weather, false);
        }
    }

    @Override
    public void setDelayTask(boolean notifyFailed) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean openTomorrowForecast = sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false);
        String tomorrowForecastTime = sharedPreferences.getString(
                getString(R.string.key_forecast_tomorrow_time),
                GeometricWeather.DEFAULT_TOMORROW_FORECAST_TIME);
        if (openTomorrowForecast) {
            PollingTaskHelper.startTomorrowForecastPollingTask(this, tomorrowForecastTime);
        }
    }
}
