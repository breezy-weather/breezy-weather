package wangdaye.com.geometricweather.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.NotificationUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;
import wangdaye.com.geometricweather.utils.manager.ShortcutsManager;

/**
 * Update service.
 *
 * process of request weather data:
 *
 * 1. location.
 *
 * 2.1. location successfully && location never changed && the latest refresh is in half an hour.
 *      --> update view with cache data.
 *
 * 2.2. location successfully || location changed || the latest refresh is in half an hour ago.
 *      --> refresh new data and update view.
 *
 * 2.3. failed to location.
 *      --> over and feedback.
 *
 * */

public abstract class UpdateService extends Service
        implements LocationHelper.OnRequestLocationListener, WeatherHelper.OnRequestWeatherListener {

    private LocationHelper locationHelper;
    private WeatherHelper weatherHelper;

    private List<Location> locationList;
    private boolean refreshing;

    @Override
    public void onCreate() {
        refreshing = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!refreshing) {
            this.refreshing = true;
            this.locationList = DatabaseHelper.getInstance(this).readLocationList();
            this.doRefresh(locationList.get(0));
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        refreshing = false;
        if (locationHelper != null) {
            locationHelper.cancel();
        }
        if (weatherHelper != null) {
            weatherHelper.cancel();
        }
    }

    // control.

    protected abstract void doRefresh(Location location);

    public void requestData(Location location) {
        initLocationHelper();
        locationHelper.requestLocation(this, location, this);
    }

    private void initLocationHelper() {
        if (locationHelper == null) {
            locationHelper = new LocationHelper(this);
        } else {
            locationHelper.cancel();
        }
    }

    private void initWeatherHelper() {
        if (weatherHelper == null) {
            weatherHelper = new WeatherHelper();
        } else {
            weatherHelper.cancel();
        }
    }

    public abstract void updateView(Context context, Location location, Weather weather);

    // interface.

    // on request location listener.

    @Override
    public void requestLocationSuccess(Location requestLocation, boolean locationChanged) {
        Weather weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        if (weather != null) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            String[] weatherDates = weather.base.date.split("-");
            String[] weatherTimes = weather.base.time.split(":");

            if (!locationChanged
                    && year == Integer.parseInt(weatherDates[0])
                    && month == Integer.parseInt(weatherDates[1])
                    && day == Integer.parseInt(weatherDates[2])) {

                if (Math.abs((hour * 60 + minute) - (Integer.parseInt(weatherTimes[0]) * 60
                        + Integer.parseInt(weatherTimes[1]))) <= 30) {
                    requestWeatherSuccess(weather, requestLocation);
                    return;
                }
            }
        }

        initWeatherHelper();
        if(requestLocation.isLocal()) {
            if (requestLocation.isUsable()) {
                weatherHelper.requestWeather(this, requestLocation, this);
            } else {
                weatherHelper.requestWeather(this, Location.buildDefaultLocation(), this);
                Toast.makeText(
                        this,
                        getString(R.string.feedback_not_yet_location),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            weatherHelper.requestWeather(this, requestLocation, this);
        }
    }

    @Override
    public void requestLocationFailed(Location requestLocation) {
        requestWeatherFailed(requestLocation);
    }

    // on request weather listener.

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        Weather oldResult = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        DatabaseHelper.getInstance(this).writeWeather(requestLocation, weather);
        DatabaseHelper.getInstance(this).writeHistory(weather);
        NotificationUtils.checkAndSendAlert(this, weather, oldResult);
        updateView(this, requestLocation, weather);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutsManager.refreshShortcuts(this, locationList);
        }
        stopSelf();
    }

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        Weather weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        updateView(this, requestLocation, weather);
        Toast.makeText(
                this,
                getString(R.string.feedback_get_weather_failed),
                Toast.LENGTH_SHORT).show();
        stopSelf();
    }
}
