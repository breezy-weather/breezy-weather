package wangdaye.com.geometricweather.main;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.basic.models.weather.Weather;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.utils.helpters.AsyncHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class MainActivityRepository {

    private final LocationHelper mLocationHelper;
    private final WeatherHelper mWeatherHelper;

    private long mReadCacheTimeStampFlag = -1;

    public MainActivityRepository(Context context) {
        mLocationHelper = new LocationHelper(context);
        mWeatherHelper = new WeatherHelper();
    }

    public AsyncHelper.Controller getLocationList(Context context, List<Location> oldList,
                                                  AsyncHelper.Callback<List<Location>> callback) {
        return AsyncHelper.runOnIO(emitter -> {
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
            emitter.send(list);

            // read weather cache and callback.
            for (Location location : list) {
                location.setWeather(DatabaseHelper.getInstance(context).readWeather(location));
            }
            emitter.send(list);
        }, callback);
    }

    public AsyncHelper.Controller getLocationAndWeatherCache(Context context,
                                                             @NonNull String formattedId,
                                                             AsyncHelper.Callback<Location> callback) {
        return AsyncHelper.runOnIO(emitter -> {
            Location location = DatabaseHelper.getInstance(context).readLocation(formattedId);
            if (location != null) {
                location.setWeather(DatabaseHelper.getInstance(context).readWeather(location));
                emitter.send(location);
            }
        }, callback);
    }

    public void getWeather(Context context, Location location, boolean locate, boolean swipeToRefresh,
                           WeatherRequestCallback callback) {

        if (location.getWeather() != null) {
            getNewWeatherInformation(context, location, locate, callback);
            return;
        }

        // if cache is null, we need to read cache from database first.
        final long timeStampFlag = System.currentTimeMillis();
        mReadCacheTimeStampFlag = timeStampFlag;
        AsyncHelper.runOnIO(
                emitter -> emitter.send(DatabaseHelper.getInstance(context).readWeather(location)),
                (AsyncHelper.Callback<Weather>) weather -> {
                    location.setWeather(weather);
                    if (timeStampFlag != mReadCacheTimeStampFlag) {
                        return;
                    }

                    // if update was triggered by swipe refreshing or weather data is invalid (too old),
                    // we need keep the requesting.
                    if (swipeToRefresh || MainModuleUtils.needUpdate(context, location)) {
                        callback.onReadCacheCompleted(location, true, false);
                        getNewWeatherInformation(context, location, locate, callback);
                    } else {
                        callback.onReadCacheCompleted(location, true, true);
                    }
                });
    }

    private void getNewWeatherInformation(Context context, Location location, boolean locate,
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
                        if (!requestLocation.equals(location)) {
                            return;
                        }
                        callback.onLocationCompleted(
                                requestLocation, true, false);
                        getWeatherWithValidLocationInformation(context, requestLocation, callback);
                    }

                    @Override
                    public void requestLocationFailed(Location requestLocation) {
                        if (!requestLocation.equals(location)) {
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
                if (!requestLocation.equals(location)) {
                    return;
                }
                callback.onGetWeatherCompleted(requestLocation, true, true);
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation) {
                if (!requestLocation.equals(location)) {
                    return;
                }
                callback.onGetWeatherCompleted(requestLocation, false, true);
            }
        });
    }

    public List<String> getLocatePermissionList() {
        return new ArrayList<>(
                Arrays.asList(mLocationHelper.getPermissions())
        );
    }

    public void cancelWeatherRequest() {
        mLocationHelper.cancel();
        mWeatherHelper.cancel();
        mReadCacheTimeStampFlag = -1;
    }

    public interface WeatherRequestCallback {
        void onReadCacheCompleted(Location location, boolean succeed, boolean done);
        void onLocationCompleted(Location location, boolean succeed, boolean done);
        void onGetWeatherCompleted(Location location, boolean succeed, boolean done);
    }
}
