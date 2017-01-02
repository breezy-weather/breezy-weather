package wangdaye.com.geometricweather.service.job;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoJobService;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;

/**
 * Today forecast job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TodayForecastJobService extends GeoJobService {
    // data.
    public static final int SCHEDULE_CODE = 5;

    /** <br> life cycle. */

    @Override
    protected void doRefresh(Location location) {
        if (ForecastNotificationUtils.isEnable(this, true)) {

            ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(SCHEDULE_CODE);
            ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                    new JobInfo.Builder(SCHEDULE_CODE, new ComponentName(getPackageName(), getClass().getName()))
                            .setMinimumLatency(ForecastNotificationUtils.calcForecastDuration(this, true, false))
                            .build());

            if (ForecastNotificationUtils.isForecastTime(this, true)) {
                requestData(location);
            }
        }
    }

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        ForecastNotificationUtils.buildForecastAndSendIt(context, weather, true);
    }

    /** <br> interface. */

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
        jobFinished(getJobParameters(), false);
    }
}
