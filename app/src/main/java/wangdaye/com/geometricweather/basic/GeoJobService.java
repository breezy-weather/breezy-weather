package wangdaye.com.geometricweather.basic;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.remoteView.ForecastNotificationUtils;

/**
 * Widget job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class GeoJobService extends JobService
        implements WeatherHelper.OnRequestWeatherListener {
    // widget.
    private WeatherHelper weatherHelper;

    // data.
    private JobParameters jobParameters;

    /** <br> life cycle. */

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParameters = params;
        List<Location> locationList = DatabaseHelper.getInstance(this).readLocationList();
        doRefresh(locationList.get(0));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (weatherHelper != null) {
            weatherHelper.cancel();
        }
        return false;
    }

    protected abstract void doRefresh(Location location);

    public void requestData(Location location) {
        Weather weather = DatabaseHelper.getInstance(this).readWeather(location);
        if (weather != null) {

            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            String[] weatherDates = weather.base.date.split("-");
            String[] weatherTimes = weather.base.time.split(":");

            if (weatherDates[0].equals(String.valueOf(year))
                    && weatherDates[1].equals(String.valueOf(month))
                    && weatherDates[2].equals(String.valueOf(day))) {

                if (Math.abs((hour * 60 + minute)
                        - (Integer.parseInt(weatherTimes[0]) * 60 + Integer.parseInt(weatherTimes[1]))) <= 60) {
                    requestWeatherSuccess(weather, location);
                    return;
                }
            }
        }

        initWeatherHelper();
        if(location.isLocal()) {
            if (location.isUsable()) {
                weatherHelper.requestWeather(this, location, this);
            } else {
                weatherHelper.requestWeather(this, Location.buildDefaultLocation(), this);
                Toast.makeText(
                        this,
                        getString(R.string.feedback_not_yet_location),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            weatherHelper.requestWeather(this, location, this);
        }
    }

    protected abstract void updateView(Context context, Location location, Weather weather);

    public static void scheduleCycleJob(Context context, Class cls, int scheduleCode) {
        ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                new JobInfo.Builder(scheduleCode, new ComponentName(context.getPackageName(), cls.getName()))
                        .setPeriodic((long) (1000 * 60 * 60 * 1.5))
                        .build());
    }

    public static void scheduleDelayJob(Context context, Class cls, int scheduleCode, boolean today) {
        ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(
                new JobInfo.Builder(scheduleCode, new ComponentName(context.getPackageName(), cls.getName()))
                        .setMinimumLatency(ForecastNotificationUtils.calcForecastDuration(context, today, true))
                        .build());
    }

    public static void cancel(Context context, int scheduleCode) {
        ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(scheduleCode);
    }

    /** <br> widget. */

    private void initWeatherHelper() {
        if (weatherHelper == null) {
            weatherHelper = new WeatherHelper();
        } else {
            weatherHelper.cancel();
        }
    }

    /** <br> data. */

    public JobParameters getJobParameters() {
        return jobParameters;
    }

    /** <br> interface. */

    // request weather.

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        Weather oldResult = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        DatabaseHelper.getInstance(this).writeWeather(requestLocation, weather);
        DatabaseHelper.getInstance(this).writeHistory(weather);
        NotificationUtils.checkAndSendAlert(this, weather, oldResult);
        updateView(this, requestLocation, weather);
        jobFinished(jobParameters, false);
    }

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        Weather weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        updateView(this, requestLocation, weather);
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
        jobFinished(jobParameters, false);
    }
}
