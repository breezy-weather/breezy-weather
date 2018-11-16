package wangdaye.com.geometricweather.service.job;

import android.app.job.JobParameters;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.JobUpdateService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.PollingTaskHelper;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;

/**
 * Job tomorrow forecast update service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobTomorrowForecastUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (ForecastNotificationUtils.isEnable(this, false)) {
            ForecastNotificationUtils.buildForecastAndSendIt(context, weather, false);
        }
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, false);

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
