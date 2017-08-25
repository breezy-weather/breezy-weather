package wangdaye.com.geometricweather.basic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.service.PollingService;
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

public abstract class UpdateService extends Service {

    private LocationHelper locationHelper;
    private WeatherHelper weatherHelper;

    private List<Location> locationList;
    private boolean refreshing;
    private boolean needFailedCallback;

    public static final String KEY_NEED_FAILED_CALLBACK = "need_failed_callback";

    @Override
    public void onCreate() {
        super.onCreate();
        locationHelper = new LocationHelper(this);
        weatherHelper = new WeatherHelper();
        refreshing = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (!refreshing) {
            this.refreshing = true;
            this.locationList = DatabaseHelper.getInstance(this).readLocationList();
            requestData(0, false);
        }
        if (!needFailedCallback) {
            needFailedCallback = intent.getBooleanExtra(KEY_NEED_FAILED_CALLBACK, false);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        refreshing = false;
        locationHelper.cancel();
        weatherHelper.cancel();
    }

    // control.

    private void requestData(int position, boolean located) {
        if (locationList.get(position).isLocal() && !located) {
            locationHelper.requestLocation(
                    this, locationList.get(position), new RequestLocationCallback(position));
        } else {
            Weather old = DatabaseHelper.getInstance(UpdateService.this).readWeather(locationList.get(position));
            if (old != null && old.isValid(0.5F)) {
                new RequestWeatherCallback(old, position)
                        .requestWeatherSuccess(old, locationList.get(position));
                return;
            }
            weatherHelper.requestWeather(
                    this, locationList.get(position), new RequestWeatherCallback(old, position));
        }
    }

    public abstract void updateView(Context context, Location location, @Nullable Weather weather);

    // interface.

    // on request location listener.

    private class RequestLocationCallback implements LocationHelper.OnRequestLocationListener {

        private int position;

        RequestLocationCallback(int position) {
            this.position = position;
        }

        @Override
        public void requestLocationSuccess(Location requestLocation, boolean locationChanged) {
            Weather old = DatabaseHelper.getInstance(UpdateService.this).readWeather(locationList.get(position));
            if (old != null && old.isValid(0.5F) && !locationChanged) {
                new RequestWeatherCallback(old, position)
                        .requestWeatherSuccess(old, locationList.get(position));
                return;
            }

            if (requestLocation.isUsable()) {
                requestData(position, true);
            } else {
                requestLocationFailed(requestLocation);
                Toast.makeText(
                        UpdateService.this,
                        getString(R.string.feedback_not_yet_location),
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void requestLocationFailed(Location requestLocation) {
            if (locationList.get(position).isUsable()) {
                requestData(position, true);
            } else {
                new RequestWeatherCallback(null, position)
                        .requestWeatherFailed(locationList.get(position));
            }
        }
    }

    // on request weather listener.

    private class RequestWeatherCallback implements WeatherHelper.OnRequestWeatherListener {

        @Nullable
        private Weather old;
        private int position;

        RequestWeatherCallback(@Nullable Weather old, int position) {
            this.old = old;
            this.position = position;
        }

        @Override
        public void requestWeatherSuccess(Weather weather, Location requestLocation) {
            DatabaseHelper.getInstance(UpdateService.this).writeWeather(requestLocation, weather);
            DatabaseHelper.getInstance(UpdateService.this).writeHistory(weather);
            if (position == 0) {
                NotificationUtils.checkAndSendAlert(UpdateService.this, weather, old);
                updateView(UpdateService.this, requestLocation, weather);

                if (needFailedCallback && weather == null) {
                    Intent intent = new Intent(UpdateService.this, PollingService.class);
                    intent.putExtra(PollingService.KEY_POLLING_UPDATE_FAILED, true);
                    startService(intent);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutsManager.refreshShortcuts(UpdateService.this, locationList);
            }
            if (position + 1 < locationList.size()) {
                requestData(position + 1, false);
            } else {
                stopSelf();
            }
        }

        @Override
        public void requestWeatherFailed(Location requestLocation) {
            if (position == 0) {
                updateView(UpdateService.this, requestLocation, old);
                Toast.makeText(
                        UpdateService.this,
                        getString(R.string.feedback_get_weather_failed),
                        Toast.LENGTH_SHORT).show();

                if (needFailedCallback) {
                    Intent intent = new Intent(UpdateService.this, PollingService.class);
                    intent.putExtra(PollingService.KEY_POLLING_UPDATE_FAILED, true);
                    startService(intent);
                }
            }
            if (position + 1 < locationList.size()) {
                requestData(position + 1, false);
            } else {
                stopSelf();
            }
        }
    }
}
