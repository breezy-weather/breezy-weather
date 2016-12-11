package wangdaye.com.geometricweather.service.notification.job;

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
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;

/**
 * Today forecast job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TodayForecastJobService extends GeoJobService {
    // data.
    public static final int SCHEDULE_CODE = 38;

    /** <br> life cycle. */

    @Override
    public Location readSettings() {
        return DatabaseHelper.getInstance(this).readLocationList().get(0);
    }

    @Override
    protected void doRefresh(Location location) {
        requestData(location);
    }

    @Override
    protected void updateView(Context context, Weather weather) {
        NotificationUtils.buildForecastAndSendIt(context, weather, true);
    }

    /** <br> interface. */

    @Override
    public void requestWeatherSuccess(Weather weather, String locationName) {
        super.requestWeatherSuccess(weather, locationName);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(SCHEDULE_CODE);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                new JobInfo.Builder(SCHEDULE_CODE, new ComponentName(getPackageName(), getClass().getName()))
                        .setMinimumLatency(NotificationUtils.calcForecastDuration(this, true, false))
                        .build());
    }

    @Override
    public void requestWeatherFailed(String locationName) {
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
        jobFinished(getJobParameters(), true);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(SCHEDULE_CODE);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                new JobInfo.Builder(SCHEDULE_CODE, new ComponentName(getPackageName(), getClass().getName()))
                        .setMinimumLatency(NotificationUtils.calcForecastDuration(this, true, false))
                        .build());
    }
}
