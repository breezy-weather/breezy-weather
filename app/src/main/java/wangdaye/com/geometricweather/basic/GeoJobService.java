package wangdaye.com.geometricweather.basic;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Widget job service.
 * */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class GeoJobService extends JobService
        implements LocationHelper.OnRequestLocationListener, WeatherHelper.OnRequestWeatherListener {
    // widget.
    private JobParameters jobParameters;

    /** <br> life cycle. */

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParameters = params;
        Location location = readSettings();
        if (location == null) {
            location = new Location(getString(R.string.local), null);
        }
        doRefresh(location);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    protected abstract Location readSettings();

    protected abstract void doRefresh(Location location);

    public void requestData(Location location) {
        if(location.name.equals(getString(R.string.local))) {
            new LocationHelper(this).requestLocation(this, this);
        } else {
            location.realName = location.name;
            DatabaseHelper.getInstance(this).insertLocation(location);
            new WeatherHelper().requestWeather(this, location, this);
        }
    }

    protected abstract void updateView(Context context, Weather weather);

    /** <br> data. */

    public JobParameters getJobParameters() {
        return jobParameters;
    }

    /** <br> interface. */

    // request name.

    @Override
    public void requestLocationSuccess(String locationName) {
        Location location = new Location(getString(R.string.local), locationName);
        DatabaseHelper.getInstance(this).insertLocation(location);
        new WeatherHelper().requestWeather(this, location, this);
    }

    @Override
    public void requestLocationFailed() {
        Location location = DatabaseHelper.getInstance(this).searchLocation(getString(R.string.local));
        new WeatherHelper().requestWeather(this, location, this);
        Toast.makeText(
                this,
                getString(R.string.feedback__location_failed),
                Toast.LENGTH_SHORT).show();
    }

    // request weather.

    @Override
    public void requestWeatherSuccess(Weather weather, String locationName) {
        DatabaseHelper.getInstance(this).insertWeather(weather);
        DatabaseHelper.getInstance(this).insertHistory(weather);
        updateView(this, weather);
        jobFinished(jobParameters, false);
    }

    @Override
    public void requestWeatherFailed(String locationName) {
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
        Weather weather = DatabaseHelper.getInstance(this).searchWeather(locationName);
        updateView(this, weather);
        jobFinished(jobParameters, true);
    }
}
