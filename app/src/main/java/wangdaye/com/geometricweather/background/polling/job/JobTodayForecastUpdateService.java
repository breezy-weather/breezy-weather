package wangdaye.com.geometricweather.background.polling.job;

import android.app.job.JobParameters;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.History;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.background.polling.PollingTaskHelper;
import wangdaye.com.geometricweather.remoteviews.presenter.notification.ForecastNotificationIMP;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

/**
 * Job today forecast update service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobTodayForecastUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location,
                           @Nullable Weather weather, @Nullable History history) {
        if (ForecastNotificationIMP.isEnable(this, true)) {
            ForecastNotificationIMP.buildForecastAndSendIt(context, weather, true);
        }
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, false);

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
