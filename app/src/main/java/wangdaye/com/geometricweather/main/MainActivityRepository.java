package wangdaye.com.geometricweather.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.basic.model.resource.ListResource;
import wangdaye.com.geometricweather.basic.model.resource.LocationResource;
import wangdaye.com.geometricweather.location.LocationHelper;
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
                           @NonNull MutableLiveData<ListResource<Location>> locationList,
                           boolean locate) {
        assert currentLocation.getValue() != null && locationList.getValue() != null;
        Location data = currentLocation.getValue().data;
        List<Location> dataList = locationList.getValue().dataList;

        if (locate) {
            locationHelper.requestLocation(context, currentLocation.getValue().data,
                    new LocationHelper.OnRequestLocationListener() {
                        @Override
                        public void requestLocationSuccess(Location requestLocation) {
                            if (!requestLocation.equals(data)) {
                                return;
                            }
                            currentLocation.setValue(LocationResource.loading(requestLocation));
                            updateLocationList(locationList, dataList, requestLocation);

                            getWeatherWithValidLocationInformation(
                                    context, currentLocation, locationList);
                        }

                        @Override
                        public void requestLocationFailed(Location requestLocation) {
                            if (!requestLocation.equals(data)) {
                                return;
                            }

                            if (requestLocation.isUsable()) {
                                currentLocation.setValue(
                                        LocationResource.loading(requestLocation, true));
                                updateLocationList(locationList, dataList, requestLocation);

                                getWeatherWithValidLocationInformation(
                                        context, currentLocation, locationList);
                            } else {
                                currentLocation.setValue(
                                        LocationResource.error(requestLocation, true));
                                updateLocationList(locationList, dataList, requestLocation);
                            }
                        }
                    });
        } else {
            getWeatherWithValidLocationInformation(context, currentLocation, locationList);
        }
    }

    private void getWeatherWithValidLocationInformation(Context context,
                                                        @NonNull MutableLiveData<LocationResource> currentLocation,
                                                        @NonNull MutableLiveData<ListResource<Location>> locationList) {
        assert currentLocation.getValue() != null && locationList.getValue() != null;
        Location data = currentLocation.getValue().data;
        List<Location> dataList = locationList.getValue().dataList;

        weatherHelper.requestWeather(context, data, new WeatherHelper.OnRequestWeatherListener() {
            @Override
            public void requestWeatherSuccess(@NonNull Location requestLocation) {
                if (!requestLocation.equals(data)) {
                    return;
                }

                currentLocation.setValue(LocationResource.success(requestLocation));
                updateLocationList(locationList, dataList, requestLocation);
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation) {
                if (!requestLocation.equals(data)) {
                    return;
                }

                currentLocation.setValue(LocationResource.error(requestLocation));
                updateLocationList(locationList, dataList, requestLocation);
            }
        });
    }

    private void updateLocationList(@NonNull MutableLiveData<ListResource<Location>> locationList,
                                    List<Location> dataList,
                                    Location data) {
        assert locationList.getValue() != null;

        int index = indexLocation(dataList, data);
        if (index != INVALID_LOCATION_INDEX) {
            locationList.setValue(
                    ListResource.changeItem(locationList.getValue(), data, index));
        }
    }

    private int indexLocation(List<Location> locationList, Location location) {
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
}
