package wangdaye.com.geometricweather.main;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.utils.helpter.AsyncHelper;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class MainActivityRepository {

    private final LocationHelper mLocationHelper;
    private final WeatherHelper mWeatherHelper;

    private long mReadCacheTimeStampFlag = -1;

    public MainActivityRepository(Context context) {
        mLocationHelper = new LocationHelper(context);
        mWeatherHelper = new WeatherHelper();
    }

    public void getLocationList(Context context, List<Location> oldList,
                                AsyncHelper.Callback<List<Location>> callback) {
        AsyncHelper.runOnIO(emitter -> {
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

    public void getLocationAndWeatherCache(Context context, @NonNull String formattedId,
                                           AsyncHelper.Callback<Location> callback) {
        AsyncHelper.runOnIO(emitter -> {
            Location location = DatabaseHelper.getInstance(context).readLocation(formattedId);
            if (location != null) {
                location.setWeather(DatabaseHelper.getInstance(context).readWeather(location));
                emitter.send(location);
            }
        }, callback);
    }

    public void getWeather(GeoActivity activity, Location location, boolean locate, boolean swipeToRefresh,
                           WeatherRequestCallback callback) {

        if (location.getWeather() != null) {
            getNewWeatherInformation(activity, location, locate, callback);
            return;
        }

        // if cache is null, we need to read cache from database first.
        final long timeStampFlag = System.currentTimeMillis();
        mReadCacheTimeStampFlag = timeStampFlag;
        AsyncHelper.runOnIO(
                emitter -> emitter.send(DatabaseHelper.getInstance(activity).readWeather(location)),
                (AsyncHelper.Callback<Weather>) weather -> {
                    location.setWeather(weather);
                    if (timeStampFlag != mReadCacheTimeStampFlag) {
                        return;
                    }

                    // if update was triggered by swipe refreshing or weather data is invalid (too old),
                    // we need keep the requesting.
                    if (swipeToRefresh || MainModuleUtils.needUpdate(activity, location)) {
                        callback.onReadCacheCompleted(activity, location, true, false);
                        getNewWeatherInformation(activity, location, locate, callback);
                    } else {
                        callback.onReadCacheCompleted(activity, location, true, true);
                    }
                });
    }

    private void getNewWeatherInformation(GeoActivity activity, Location location, boolean locate,
                                          WeatherRequestCallback callback) {
        if (locate) {
            ensureValidLocationInformation(activity, location, callback);
        } else {
            getWeatherWithValidLocationInformation(activity, location, callback);
        }
    }

    private void ensureValidLocationInformation(GeoActivity activity, Location location,
                                                WeatherRequestCallback callback) {
        mLocationHelper.requestLocation(activity, location, false,
                new LocationHelper.OnRequestLocationListener() {
                    @Override
                    public void requestLocationSuccess(Location requestLocation) {
                        if (!requestLocation.equals(location)) {
                            return;
                        }
                        callback.onLocationCompleted(
                                activity, requestLocation, true, false);
                        getWeatherWithValidLocationInformation(activity, requestLocation, callback);
                    }

                    @Override
                    public void requestLocationFailed(Location requestLocation) {
                        if (!requestLocation.equals(location)) {
                            return;
                        }
                        callback.onLocationCompleted(
                                activity, location, false, !requestLocation.isUsable());
                        if (requestLocation.isUsable()) {
                            getWeatherWithValidLocationInformation(activity, requestLocation, callback);
                        }
                    }
                });
    }

    private void getWeatherWithValidLocationInformation(GeoActivity activity, Location location,
                                                        WeatherRequestCallback callback) {
        mWeatherHelper.requestWeather(activity, location, new WeatherHelper.OnRequestWeatherListener() {
            @Override
            public void requestWeatherSuccess(@NonNull Location requestLocation) {
                if (!requestLocation.equals(location)) {
                    return;
                }
                callback.onGetWeatherCompleted(activity, requestLocation, true, true);
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation) {
                if (!requestLocation.equals(location)) {
                    return;
                }
                callback.onGetWeatherCompleted(activity, requestLocation, false, true);
            }
        });
    }

    public List<String> getLocatePermissionList(boolean background) {
        return new ArrayList<>(
                Arrays.asList(mLocationHelper.getPermissions(background))
        );
    }

    public void cancel() {
        mLocationHelper.cancel();
        mWeatherHelper.cancel();
        mReadCacheTimeStampFlag = -1;
    }

    public interface WeatherRequestCallback {
        void onReadCacheCompleted(GeoActivity activity, Location location, boolean succeed, boolean done);
        void onLocationCompleted(GeoActivity activity, Location location, boolean succeed, boolean done);
        void onGetWeatherCompleted(GeoActivity activity, Location location, boolean succeed, boolean done);
    }
}
