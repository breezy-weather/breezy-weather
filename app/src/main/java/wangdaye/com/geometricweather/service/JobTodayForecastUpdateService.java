package wangdaye.com.geometricweather.service;

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
import wangdaye.com.geometricweather.utils.helpter.JobHelper;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;

/**
 * Job today forecast updateRotation service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobTodayForecastUpdateService extends JobUpdateService {

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        if (ForecastNotificationUtils.isEnable(this, true)) {
            ForecastNotificationUtils.buildForecastAndSendIt(context, weather, true);
        }
    }

    @Override
    public void setDelayTask(JobParameters jobParameters, boolean failed) {
        jobFinished(jobParameters, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean openTodayForecast = sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false);
        String todayForecastTime = sharedPreferences.getString(
                getString(R.string.key_forecast_today_time),
                GeometricWeather.DEFAULT_TODAY_FORECAST_TIME);
        if (openTodayForecast) {
            JobHelper.setJobForTodayForecast(this, todayForecastTime);
        }
    }
}
