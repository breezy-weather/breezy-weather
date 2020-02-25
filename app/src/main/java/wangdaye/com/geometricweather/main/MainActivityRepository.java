package wangdaye.com.geometricweather.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.location.LocationHelper;
import wangdaye.com.geometricweather.main.model.LocationResource;
import wangdaye.com.geometricweather.main.model.LockableLocationList;
import wangdaye.com.geometricweather.weather.WeatherHelper;

public class MainActivityRepository {

    private LocationHelper locationHelper;
    private WeatherHelper weatherHelper;

    private static final int INVALID_LOCATION_INDEX = -1;

    public MainActivityRepository(Context context) {
        locationHelper = new LocationHelper(context);
        weatherHelper = new WeatherHelper();
    }

    public void getWeather(Context context,
                           @NonNull MutableLiveData<LocationResource> currentLocation,
                           @NonNull LockableLocationList lockableLocationList,
                           boolean locate,
                           @Nullable OnLocationCompletedListener l) {
        assert currentLocation.getValue() != null;
        Location data = currentLocation.getValue().data;
        boolean defaultLocation = currentLocation.getValue().isDefaultLocation();

        if (locate) {
            locationHelper.requestLocation(context, currentLocation.getValue().data, false,
                    new LocationHelper.OnRequestLocationListener() {
                        @Override
                        public void requestLocationSuccess(Location requestLocation) {
                            if (!requestLocation.equals(data)) {
                                return;
                            }

                            currentLocation.setValue(LocationResource.loading(requestLocation, defaultLocation));
                            lockableLocationList.write((getter, setter) -> {
                                updateLocationList(context, requestLocation, getter, setter);
                                if (l != null) {
                                    l.onCompleted(context);
                                }
                            });

                            getWeatherWithValidLocationInformation(
                                    context, currentLocation, lockableLocationList);
                        }

                        @Override
                        public void requestLocationFailed(Location requestLocation) {
                            if (!requestLocation.equals(data)) {
                                return;
                            }

                            if (requestLocation.isUsable()) {
                                currentLocation.setValue(
                                        LocationResource.loading(requestLocation, defaultLocation, true));
                            } else {
                                currentLocation.setValue(
                                        LocationResource.error(requestLocation, defaultLocation, true));
                            }

                            lockableLocationList.write((getter, setter) ->
                                    updateLocationList(context, requestLocation, getter, setter));

                            if (requestLocation.isUsable()) {
                                getWeatherWithValidLocationInformation(
                                        context, currentLocation, lockableLocationList);
                            }
                        }
                    });
        } else {
            getWeatherWithValidLocationInformation(context, currentLocation, lockableLocationList);
        }
    }

    private void getWeatherWithValidLocationInformation(Context context,
                                                        @NonNull MutableLiveData<LocationResource> currentLocation,
                                                        @NonNull LockableLocationList lockableLocationList) {
        assert currentLocation.getValue() != null;
        Location data = currentLocation.getValue().data;
        boolean defaultLocation = currentLocation.getValue().isDefaultLocation();

        weatherHelper.requestWeather(context, data, new WeatherHelper.OnRequestWeatherListener() {
            @Override
            public void requestWeatherSuccess(@NonNull Location requestLocation) {
                if (!requestLocation.equals(data)) {
                    return;
                }

                currentLocation.setValue(LocationResource.success(requestLocation, defaultLocation));
                lockableLocationList.write((getter, setter) ->
                        updateLocationList(context, requestLocation, getter, setter));
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation) {
                if (!requestLocation.equals(data)) {
                    return;
                }

                currentLocation.setValue(LocationResource.error(requestLocation, defaultLocation));
                lockableLocationList.write((getter, setter) ->
                        updateLocationList(context, requestLocation, getter, setter));
            }
        });
    }

    private void updateLocationList(Context context, @NonNull Location data,
                                    @NonNull LockableLocationList.Getter getter,
                                    @NonNull LockableLocationList.Setter setter) {
        List<Location> totalList = new ArrayList<>(getter.getTotalList());
        int index = indexLocation(totalList, data);
        if (index != INVALID_LOCATION_INDEX) {
            totalList.set(index, data);
        }
        setter.setLocationList(context, totalList);
    }

    private int indexLocation(@NonNull List<Location> locationList, @NonNull Location location) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(location)) {
                return i;
            }
        }
        return INVALID_LOCATION_INDEX;
    }

    public List<String> getLocatePermissionList() {
        return new ArrayList<>(
                Arrays.asList(locationHelper.getPermissions())
        );
    }

    public void cancel() {
        locationHelper.cancel();
        weatherHelper.cancel();
    }

    public interface OnLocationCompletedListener {
        /**
         * Work in write lock.
         * */
        void onCompleted(Context context);
    }
}
