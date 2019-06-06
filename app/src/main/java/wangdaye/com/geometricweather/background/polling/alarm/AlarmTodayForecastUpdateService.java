package wangdaye.com.geometricweather.background.polling.alarm;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.background.service.UpdateService;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.polling.PollingTaskHelper;
import wangdaye.com.geometricweather.remoteviews.presenter.notification.ForecastNotificationIMP;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Alarm Today forecast update service.
 * */

public class AlarmTodayForecastUpdateService extends UpdateService {

    @Override
    public void updateView(Context context, Location location,
                           @Nullable Weather weather, @Nullable History history) {
        if (ForecastNotificationIMP.isEnable(this, true)) {
            ForecastNotificationIMP.buildForecastAndSendIt(context, weather, true);
        }
    }

    @Override
    public void setDelayTask(boolean notifyFailed) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean openTodayForecast = sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false);
        String todayForecastTime = sharedPreferences.getString(
                getString(R.string.key_forecast_today_time),
                SettingsOptionManager.DEFAULT_TODAY_FORECAST_TIME);
        if (openTodayForecast) {
            PollingTaskHelper.startTodayForecastPollingTask(this, todayForecastTime);
        }
    }
}
