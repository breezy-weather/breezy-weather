package wangdaye.com.geometricweather.basic;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.Weather;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

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
        doRefresh(
                readLocation(
                        readSettings()));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (weatherHelper != null) {
            weatherHelper.cancel();
        }
        return false;
    }

    protected abstract String readSettings();

    protected Location readLocation(String locationName) {
        List<Location> locationList = DatabaseHelper.getInstance(this).readLocationList();
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationName.equals(getString(R.string.local)) && locationList.get(i).isLocal()) {
                return locationList.get(i);
            } else if (!locationName.equals(getString(R.string.local))
                    && locationList.get(i).city.equals(locationName)) {
                return locationList.get(i);
            }
        }
        return locationList.get(0);
    }

    protected abstract void doRefresh(Location location);

    public void requestData(Location location) {
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
        DatabaseHelper.getInstance(this).writeWeather(requestLocation, weather);
        DatabaseHelper.getInstance(this).writeHistory(weather);
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
