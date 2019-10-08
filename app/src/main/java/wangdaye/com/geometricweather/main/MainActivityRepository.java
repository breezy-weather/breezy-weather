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
                           @NonNull List<Location> totalLocationList,
                           boolean locate) {
        assert currentLocation.getValue() != null && locationList.getValue() != null;
        Location data = currentLocation.getValue().data;

        if (locate) {
            locationHelper.requestLocation(context, currentLocation.getValue().data,
                    new LocationHelper.OnRequestLocationListener() {
                        @Override
                        public void requestLocationSuccess(Location requestLocation) {
                            if (!requestLocation.equals(data)) {
                                return;
                            }
                            currentLocation.setValue(LocationResource.loading(requestLocation));
                            updateLocationList(context, requestLocation, locationList, totalLocationList);

                            getWeatherWithValidLocationInformation(
                                    context, currentLocation, locationList, totalLocationList);
                        }

                        @Override
                        public void requestLocationFailed(Location requestLocation) {
                            if (!requestLocation.equals(data)) {
                                return;
                            }

                            if (requestLocation.isUsable()) {
                                currentLocation.setValue(
                                        LocationResource.loading(requestLocation, true));
                                updateLocationList(context, requestLocation, locationList, totalLocationList);

                                getWeatherWithValidLocationInformation(
                                        context, currentLocation, locationList, totalLocationList);
                            } else {
                                currentLocation.setValue(
                                        LocationResource.error(requestLocation, true));
                                updateLocationList(context, requestLocation, locationList, totalLocationList);
                            }
                        }
                    });
        } else {
            getWeatherWithValidLocationInformation(context, currentLocation, locationList, totalLocationList);
        }
    }

    private void getWeatherWithValidLocationInformation(Context context,
                                                        @NonNull MutableLiveData<LocationResource> currentLocation,
                                                        @NonNull MutableLiveData<ListResource<Location>> locationList,
                                                        @NonNull List<Location> totalLocationList) {
        assert currentLocation.getValue() != null && locationList.getValue() != null;
        Location data = currentLocation.getValue().data;

        weatherHelper.requestWeather(context, data, new WeatherHelper.OnRequestWeatherListener() {
            @Override
            public void requestWeatherSuccess(@NonNull Location requestLocation) {
                if (!requestLocation.equals(data)) {
                    return;
                }

                currentLocation.setValue(LocationResource.success(requestLocation));
                updateLocationList(context, requestLocation, locationList, totalLocationList);
            }

            @Override
            public void requestWeatherFailed(@NonNull Location requestLocation) {
                if (!requestLocation.equals(data)) {
                    return;
                }

                currentLocation.setValue(LocationResource.error(requestLocation));
                updateLocationList(context, requestLocation, locationList, totalLocationList);
            }
        });
    }

    private void updateLocationList(Context context,
                                    @NonNull Location data,
                                    @NonNull MutableLiveData<ListResource<Location>> locationList,
                                    @NonNull List<Location> totalList) {
        int index = indexLocation(totalList, data);
        if (index != INVALID_LOCATION_INDEX) {
            totalList.set(index, data);
            locationList.setValue(new ListResource<>(copyValidLocations(context, totalList)));
        }
    }

    private int indexLocation(@NonNull List<Location> locationList, @NonNull Location location) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(location)) {
                return i;
            }
        }
        return INVALID_LOCATION_INDEX;
    }

    static List<Location> copyValidLocations(Context context, List<Location> locationList) {
        List<Location> validList = new ArrayList<>(locationList.size());
        Location currentLocation = null;

        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).isCurrentPosition()) {
                currentLocation = locationList.get(i);
                break;
            }
        }

        if (currentLocation == null) {
            validList.addAll(locationList);
        } else {
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).isCurrentPosition()
                        || !locationList.get(i).isResidentPosition()
                        || !locationList.get(i).isCloseTo(context, currentLocation)) {
                    validList.add(locationList.get(i));
                }
            }
        }

        return validList;
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
