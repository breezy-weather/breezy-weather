package wangdaye.com.geometricweather.main;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.utils.helpers.AsyncHelper;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class MainActivityRepository {

    private final LocationHelper mLocationHelper;
    private final WeatherHelper mWeatherHelper;
    private final ExecutorService mSingleThreadExecutor;

    public interface WeatherRequestCallback {
        void onLocationCompleted(Location location, boolean succeed, boolean done);
        void onGetWeatherCompleted(Location location, boolean succeed, boolean done);
    }

    @Inject
    public MainActivityRepository(LocationHelper locationHelper, WeatherHelper weatherHelper) {
        mLocationHelper = locationHelper;
        mWeatherHelper = weatherHelper;
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    public void destroy() {
        cancelWeatherRequest();
    }

    public void getLocationList(Context context, List<Location> oldList,
                                AsyncHelper.Callback<List<Location>> callback) {
        AsyncHelper.runOnExecutor(emitter -> {
            // read location list and callback.
            List<Location> list = DatabaseHelper.getInstance(context).readLocationList();
            for (Location oldOne : oldList) {
                for (Location newOne : list) {
                    if (newOne.equals(oldOne)) {
                        newOne.setWeather(oldOne.getWeather());
                        break;
                    }
                }
            }
            emitter.send(list, false);

            // read weather cache and callback.
            for (Location location : list) {
                location.setWeather(DatabaseHelper.getInstance(context).readWeather(location));
            }
            emitter.send(list, true);
        }, callback, mSingleThreadExecutor);
    }

    public void writeLocation(Context context, Location location) {
        AsyncHelper.runOnExecutor(() -> {
            DatabaseHelper.getInstance(context).writeLocation(location);
            if (location.getWeather() != null) {
                DatabaseHelper.getInstance(context).writeWeather(location, location.getWeather());
            }
        }, mSingleThreadExecutor);
    }

    public void writeLocationList(Context context, List<Location> locationList) {
        AsyncHelper.runOnExecutor(() -> DatabaseHelper.getInstance(context).writeLocationList(locationList),
                mSingleThreadExecutor);
    }

    public void writeLocationList(Context context, List<Location> locationList, int newIndex) {
        AsyncHelper.runOnExecutor(() -> {
            DatabaseHelper.getInstance(context).writeLocationList(locationList);

            Location newItem = locationList.get(newIndex);
            if (newItem.getWeather() != null) {
                DatabaseHelper.getInstance(context).writeWeather(newItem, newItem.getWeather());
            }
        }, mSingleThreadExecutor);
    }

    public void deleteLocation(Context context, Location location) {
        AsyncHelper.runOnExecutor(() -> {
            DatabaseHelper.getInstance(context).deleteLocation(location);
            DatabaseHelper.getInstance(context).deleteWeather(location);
        }, mSingleThreadExecutor);
    }

    public void getWeather(Context context, Location location, boolean locate,
                           WeatherRequestCallback callback) {
        if (locate) {
            ensureValidLocationInformation(context, location, callback);
        } else {
            getWeatherWithValidLocationInformation(context, location, callback);
        }
    }

    private void ensureValidLocationInformation(Context context, Location location,
                                                WeatherRequestCallback callback) {
        mLocationHelper.requestLocation(context, location, false,
                new LocationHelper.OnRequestLocationListener() {
                    @Override
                    public void requestLocationSuccess(Location requestLocation) {
                        if (!requestLocation.getFormattedId().equals(location.getFormattedId())) {
                            return;
                        }
                        callback.onLocationCompleted(
                                requestLocation, true, false);
                        getWeatherWithValidLocationInformation(context, requestLocation, callback);
                    }

                    @Override
                    public void requestLocationFailed(Location requestLocation) {
                        if (!requestLocation.getFormattedId().equals(location.getFormattedId())) {
                            return;
                        }
                        callback.onLocationCompleted(
                                location, false, !requestLocation.isUsable());
                        if (requestLocation.isUsable()) {
                            getWeatherWithValidLocationInformation(context, requestLocation, callback);
                        }
                    }
                });
    }

    private void getWeatherWithValidLocationInformation(Context context, Location location,
                                                        WeatherRequestCallback callback) {
        mWeatherHelper.requestWeather(context, location, new WeatherHelper.OnRequestWeatherListener() {
            @Override
            public void requestWeatherSuccess(@NonNull Location requestLocation) {
                if (!requestLocation.getFormattedId().equals(location.getFormattedId())) {
                    return;
                }
                callback.onGetWeatherCompleted(requestLocation, true, true);
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation) {
                if (!requestLocation.getFormattedId().equals(location.getFormattedId())) {
                    return;
                }
                callback.onGetWeatherCompleted(requestLocation, false, true);
            }
        });
    }

    public List<String> getLocatePermissionList(Context context) {
        return new ArrayList<>(
                Arrays.asList(mLocationHelper.getPermissions(context))
        );
    }

    public void cancelWeatherRequest() {
        mLocationHelper.cancel();
        mWeatherHelper.cancel();
    }
}
