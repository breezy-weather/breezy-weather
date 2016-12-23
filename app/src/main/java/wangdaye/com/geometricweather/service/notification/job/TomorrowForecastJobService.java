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
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;

/**
 * Today forecast job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class TomorrowForecastJobService extends GeoJobService {
    // data.
    public static final int SCHEDULE_CODE = 39;

    /** <br> life cycle. */

    @Override
    protected String readSettings() {
        return null;
    }

    @Override
    protected Location readLocation(String locationName) {
        return DatabaseHelper.getInstance(this).readLocationList().get(0);
    }

    @Override
    protected void doRefresh(Location location) {
        requestData(location);
    }

    @Override
    public void updateView(Context context, Location location, Weather weather) {
        NotificationUtils.buildForecastAndSendIt(context, weather, false);
    }

    /** <br> interface. */

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        super.requestWeatherSuccess(weather, requestLocation);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(SCHEDULE_CODE);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                new JobInfo.Builder(SCHEDULE_CODE, new ComponentName(getPackageName(), getClass().getName()))
                        .setMinimumLatency(NotificationUtils.calcForecastDuration(this, false, false))
                        .build());
    }

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
        jobFinished(getJobParameters(), false);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(SCHEDULE_CODE);
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                new JobInfo.Builder(SCHEDULE_CODE, new ComponentName(getPackageName(), getClass().getName()))
                        .setMinimumLatency(NotificationUtils.calcForecastDuration(this, false, false))
                        .build());
    }
}
