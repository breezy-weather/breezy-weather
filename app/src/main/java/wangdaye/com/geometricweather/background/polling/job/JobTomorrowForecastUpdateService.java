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
 * Job tomorrow forecast update service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobTomorrowForecastUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location, @Nullable Weather weather, @Nullable History history) {
        if (ForecastNotificationIMP.isEnable(this, false)) {
            ForecastNotificationIMP.buildForecastAndSendIt(context, weather, false);
        }
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean openTomorrowForecast = sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false);
        String tomorrowForecastTime = sharedPreferences.getString(
                getString(R.string.key_forecast_tomorrow_time),
                SettingsOptionManager.DEFAULT_TOMORROW_FORECAST_TIME);
        if (openTomorrowForecast) {
            PollingTaskHelper.startTomorrowForecastPollingTask(this, tomorrowForecastTime);
        }
    }
}
