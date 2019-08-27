package wangdaye.com.geometricweather.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.resource.ListResource;
import wangdaye.com.geometricweather.basic.model.resource.LocationResource;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<ListResource<Location>> locationList;
    private MutableLiveData<LocationResource> currentLocation;
    private int locationCount;
    private int currentIndex;

    private MainActivityRepository repository;

    private static final int INVALID_LOCATION_INDEX = -1;

    public MainActivityViewModel() {
        locationList = new MutableLiveData<>();
        currentLocation = new MutableLiveData<>();
        currentIndex = INVALID_LOCATION_INDEX;
        locationCount = 0;
    }

    public void reset(GeoActivity activity) {
        LocationResource resource = currentLocation.getValue();
        if (resource != null) {
            init(activity, resource.data);
        }
    }

    public void init(GeoActivity activity, @NonNull Location location) {
        init(activity, location.getFormattedId());
    }

    public void init(GeoActivity activity, @Nullable String formattedId) {
        if (repository == null) {
            repository = new MainActivityRepository(activity);
        }

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(activity);

        List<Location> dataList = databaseHelper.readLocationList();
        for (int i = 0; i < dataList.size(); i ++) {
            dataList.get(i).weather = databaseHelper.readWeather(dataList.get(i));
            dataList.get(i).history = databaseHelper.readHistory(dataList.get(i).weather);
        }
        locationList.setValue(new ListResource<>(dataList));
        locationCount = dataList.size();

        setLocation(activity, formattedId, false);
    }

    public void setLocation(GeoActivity activity, int offset) {
        setLocation(activity, getLocationFromList(offset).getFormattedId(), false);
    }

    private void setLocation(GeoActivity activity, @Nullable String formattedId,
                            boolean updatedInBackground) {
        assert locationList.getValue() != null;
        List<Location> dataList = locationList.getValue().dataList;

        int index = indexLocation(dataList, formattedId);
        if (index == INVALID_LOCATION_INDEX) {
            index = currentIndex != INVALID_LOCATION_INDEX ? currentIndex : 0;
        }
        if (index < 0 || index >= dataList.size()) {
            index = 0;
        }
        currentIndex = index;

        Location current = dataList.get(index);

        float pollingRateScale = ValueUtils.getUpdateIntervalInHour(
                SettingsOptionManager.getInstance(activity).getUpdateInterval());
        if (current.isUsable()
                && current.weather != null
                && current.weather.isValid(pollingRateScale)) {
            repository.cancel();
            currentLocation.setValue(LocationResource.success(current, updatedInBackground));
        } else {
            currentLocation.setValue(LocationResource.loading(current));
            updateWeather(activity);
        }
    }

    private int indexLocation(List<Location> locationList, @Nullable String formattedId) {
        if (TextUtils.isEmpty(formattedId)) {
            return INVALID_LOCATION_INDEX;
        }

        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).isCurrentPosition() && Location.isLocal(formattedId)) {
                return i;
            } else if (!locationList.get(i).isCurrentPosition() && formattedId.equals(locationList.get(i).cityId)) {
                return i;
            }
        }

        return INVALID_LOCATION_INDEX;
    }

    public void updateLocationFromBackground(GeoActivity activity, @Nullable String formattedId) {
        ListResource<Location> resource = locationList.getValue();
        if (resource == null) {
            return;
        }

        int index = indexLocation(resource.dataList, formattedId);
        if (index == INVALID_LOCATION_INDEX) {
            return;
        }

        Location location = resource.dataList.get(index);
        location = DatabaseHelper.getInstance(activity).readLocation(location);
        if (location == null) {
            return;
        }

        location.weather = DatabaseHelper.getInstance(activity).readWeather(location);
        location.history = DatabaseHelper.getInstance(activity).readHistory(location.weather);

        locationList.setValue(ListResource.changeItem(resource, location, index));
        if (index == currentIndex) {
            if (activity.isForeground()) {
                SnackbarUtils.showSnackbar(
                        activity, activity.getString(R.string.feedback_updated_in_background));
            }
            setLocation(activity, location.getFormattedId(), true);
        }
    }

    public Location getDefaultLocation() {
        assert locationList.getValue() != null;
        return locationList.getValue().dataList.get(0);
    }

    public Location getLocationFromList(int offset) {
        assert locationList.getValue() != null;

        int index = currentIndex + offset;
        int size = locationList.getValue().dataList.size();

        while (index < 0) {
            index += size;
        }
        index %= size;

        return locationList.getValue().dataList.get(index);
    }

    public void updateWeather(GeoActivity activity) {
        repository.cancel();

        assert currentLocation.getValue() != null;
        Location location = currentLocation.getValue().data;

        currentLocation.setValue(LocationResource.loading(location));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && location.isCurrentPosition()) {
            // check permissions.
            List<String> permissionList = repository.getLocatePermissionList();
            for (int i = permissionList.size() - 1; i >= 0; i --) {
                if (ActivityCompat.checkSelfPermission(activity, permissionList.get(i))
                        == PackageManager.PERMISSION_GRANTED) {
                    permissionList.remove(i);
                }
            }

            if (permissionList.size() != 0) {
                // request permission.
                activity.requestPermissions(permissionList.toArray(new String[0]), 0,
                        (requestCode, permission, grantResult) -> {
                    for (int i = 0; i < permission.length && i < grantResult.length; i++) {
                        if (isPivotalPermission(permission[i])
                                && grantResult[i] != PackageManager.PERMISSION_GRANTED) {
                            if (location.isUsable()) {
                                repository.getWeather(activity, currentLocation, locationList, false);
                            } else {
                                currentLocation.setValue(
                                        LocationResource.error(location, true));
                            }
                            return;
                        }
                    }
                    repository.getWeather(activity, currentLocation, locationList, true);
                });
                return;
            }
        }

        repository.getWeather(activity, currentLocation, locationList, location.isCurrentPosition());
    }

    private boolean isPivotalPermission(String permission) {
        return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public MutableLiveData<ListResource<Location>> getLocationList() {
        return locationList;
    }

    public MutableLiveData<LocationResource> getCurrentLocation() {
        return currentLocation;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getLocationCount() {
        return locationCount;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (repository != null) {
            repository.cancel();
        }
    }
}
