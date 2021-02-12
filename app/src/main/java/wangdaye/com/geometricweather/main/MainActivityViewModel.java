package wangdaye.com.geometricweather.main;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoViewModel;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.main.models.Indicator;
import wangdaye.com.geometricweather.main.models.LocationResource;
import wangdaye.com.geometricweather.main.models.PermissionsRequest;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.utils.helpters.AsyncHelper;

public class MainActivityViewModel extends GeoViewModel
        implements MainActivityRepository.WeatherRequestCallback {

    private final MutableLiveData<LocationResource> mCurrentLocation;
    private final MutableLiveData<Indicator> mIndicator;
    private final MutableLiveData<PermissionsRequest> mPermissionsRequest;

    private final MainActivityRepository mRepository;

    private @Nullable AsyncHelper.Controller mIOController;

    private @Nullable List<Location> mTotalList; // all locations.
    private @Nullable List<Location> mValidList; // location list optimized for resident city.
    private @Nullable Integer mValidIndex; // current index for validList.

    public MainActivityViewModel(@NonNull Application application) {
        super(application);

        mCurrentLocation = new MutableLiveData<>();
        mCurrentLocation.setValue(null);

        mIndicator = new MutableLiveData<>();
        mIndicator.setValue(new Indicator(1, 0));

        mPermissionsRequest = new MutableLiveData<>();
        mPermissionsRequest.setValue(
                new PermissionsRequest(new ArrayList<>(), null, false));

        mRepository = new MainActivityRepository(getApplication());

        mIOController = null;

        mTotalList = null;
        mValidList = null;
        mValidIndex = null;
    }

    public void reset() {
        LocationResource resource = mCurrentLocation.getValue();
        if (resource != null) {
            init(resource.data);
        }
    }

    public void init(@NonNull Location location) {
        init(location.getFormattedId());
    }

    public void init(@Nullable String formattedId) {
        if (mIOController != null) {
            mIOController.cancel();
            mIOController = null;
        }

        final boolean[] firstInit = {true};

        List<Location> oldList = mTotalList == null
                ? new ArrayList<>()
                : Collections.unmodifiableList(mTotalList);
        mIOController = mRepository.getLocationList(getApplication(), oldList, locationList -> {
            if (locationList == null) {
                return;
            }

            List<Location> totalList = new ArrayList<>(locationList);
            List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);

            int validIndex = firstInit[0]
                    ? indexLocation(validList, formattedId)
                    : indexLocation(mValidList, getCurrentFormattedId());
            firstInit[0] = false;

            setLocation(totalList, validList, validIndex, LocationResource.Source.REFRESH, null);
        });
    }

    public void init(List<Location> locationList, @Nullable String formattedId) {
        if (mIOController != null) {
            mIOController.cancel();
            mIOController = null;
        }

        List<Location> totalList = new ArrayList<>(locationList);
        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);

        int validIndex = indexLocation(validList, formattedId);

        setLocation(totalList, validList, validIndex, LocationResource.Source.REFRESH, null);
    }

    public void setLocation(int offset) {
        if (mTotalList == null || mValidList == null || mValidIndex == null) {
            return;
        }

        int index = mValidIndex;
        index = Math.max(index, 0);
        index = Math.min(index, mValidList.size() - 1);
        index += offset + mValidList.size();
        index %= mValidList.size();

        setLocation(
                new ArrayList<>(mTotalList),
                new ArrayList<>(mValidList),
                index,
                LocationResource.Source.SWITCH,
                null
        );
    }

    private void setLocation(@NonNull List<Location> newTotalList,
                             @NonNull List<Location> newValidList,
                             int newValidIndex,
                             LocationResource.Source source,
                             @Nullable LocationResource resource) {
        mTotalList = newTotalList;
        mValidList = newValidList;

        mValidIndex = newValidIndex;
        mValidIndex = Math.max(mValidIndex, 0);
        mValidIndex = Math.min(mValidIndex, newValidList.size() - 1);

        Location current = mValidList.get(mValidIndex);
        Indicator i = new Indicator(mValidList.size(), mValidIndex);

        boolean defaultLocation = mValidIndex == 0;
        if (resource == null && MainModuleUtils.needUpdate(getApplication(), current)) {
            mCurrentLocation.setValue(LocationResource.loading(current, defaultLocation, source));
            updateWeather(false, true);
        } else if (resource == null) {
            mRepository.cancelWeatherRequest();
            mCurrentLocation.setValue(LocationResource.success(current, defaultLocation, source));
        } else {
            mCurrentLocation.setValue(resource);
        }
        mIndicator.setValue(i);
    }

    public void updateLocationFromBackground(@Nullable String formattedId) {
        if (TextUtils.isEmpty(formattedId)
                || mTotalList == null || mValidList == null || mValidIndex == null) {
            return;
        }

        if (mIOController != null) {
            mIOController.cancel();
            mIOController = null;
        }

        assert formattedId != null;
        mIOController = mRepository.getLocationAndWeatherCache(getApplication(), formattedId, location -> {

            List<Location> totalList = new ArrayList<>(mTotalList);
            for (int i = 0; i < totalList.size(); i ++) {
                if (totalList.get(i).equals(location)) {
                    totalList.set(i, location);
                    break;
                }
            }

            List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);

            int validIndex = indexLocation(validList, getCurrentFormattedId());

            setLocation(totalList, validList, validIndex,
                    LocationResource.Source.BACKGROUND, null);
        });
    }

    private static int indexLocation(List<Location> locationList, @Nullable String formattedId) {
        if (TextUtils.isEmpty(formattedId)) {
            return -1;
        }

        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(formattedId)) {
                return i;
            }
        }

        return -1;
    }

    @Nullable
    public Location getLocationFromList(int offset) {
        if (mTotalList == null || mValidList == null || mValidIndex == null) {
            return null;
        }
        return mValidList.get((mValidIndex + offset + mValidList.size()) % mValidList.size());
    }

    public void updateWeather(boolean triggeredByUser, boolean checkPermissions) {
        if (mCurrentLocation.getValue() == null) {
            return;
        }

        mRepository.cancelWeatherRequest();

        Location location = mCurrentLocation.getValue().data;

        mCurrentLocation.setValue(
                LocationResource.loading(
                        location,
                        mCurrentLocation.getValue().defaultLocation,
                        LocationResource.Source.REFRESH
                )
        );

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || !location.isCurrentPosition()
                || !checkPermissions) {
            // don't need to request any permission -> request data directly.
            mRepository.getWeather(
                    getApplication(), location, location.isCurrentPosition(), triggeredByUser, this);
            return;
        }

        // check permissions.
        List<String> permissionList = getDeniedPermissionList();
        if (permissionList.size() == 0) {
            // already got all permissions -> request data directly.
            mRepository.getWeather(
                    getApplication(), location, true, triggeredByUser, this);
            return;
        }

        // request permissions.
        mPermissionsRequest.setValue(new PermissionsRequest(permissionList, location, triggeredByUser));
    }

    public void requestPermissionsFailed(Location location) {
        mCurrentLocation.setValue(
                LocationResource.error(
                        location,
                        true,
                        LocationResource.Source.REFRESH
                )
        );
    }

    private List<String> getDeniedPermissionList() {
        List<String> permissionList = mRepository.getLocatePermissionList();
        for (int i = permissionList.size() - 1; i >= 0; i --) {
            if (ActivityCompat.checkSelfPermission(getApplication(), permissionList.get(i))
                    == PackageManager.PERMISSION_GRANTED) {
                permissionList.remove(i);
            }
        }
        return permissionList;
    }

    public MutableLiveData<LocationResource> getCurrentLocation() {
        return mCurrentLocation;
    }

    public MutableLiveData<Indicator> getIndicator() {
        return mIndicator;
    }

    public MutableLiveData<PermissionsRequest> getPermissionsRequest() {
        return mPermissionsRequest;
    }

    @Nullable
    public Location getCurrentLocationValue() {
        LocationResource resource = mCurrentLocation.getValue();
        if (resource != null) {
            return resource.data;
        } else {
            return null;
        }
    }

    @Nullable
    public String getCurrentFormattedId() {
        Location location = getCurrentLocationValue();
        if (location != null) {
            return location.getFormattedId();
        } else if (mValidList != null && mValidIndex != null) {
            return mValidList.get(mValidIndex).getFormattedId();
        } else {
            return null;
        }
    }

    public @NonNull List<Location> getTotalLocationList() {
        if (mTotalList != null) {
            return Collections.unmodifiableList(mTotalList);
        } else {
            return new ArrayList<>();
        }
    }

    public @NonNull List<Location> getValidLocationList() {
        if (mValidList != null) {
            return Collections.unmodifiableList(mValidList);
        } else {
            return new ArrayList<>();
        }
    }

    public PermissionsRequest getPermissionsRequestValue() {
        return mPermissionsRequest.getValue();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mRepository != null) {
            mRepository.cancelWeatherRequest();
        }
    }

    // weather request callback.

    @Override
    public void onReadCacheCompleted(Location location, boolean succeed, boolean done) {
        callback(location, false, succeed, done);
    }

    @Override
    public void onLocationCompleted(Location location, boolean succeed, boolean done) {
        callback(location, !succeed, succeed, done);
    }

    @Override
    public void onGetWeatherCompleted(Location location, boolean succeed, boolean done) {
        callback(location, false, succeed, done);
    }

    private void callback(Location location,
                          boolean locateFailed, boolean succeed, boolean done) {
        if (mTotalList == null || mValidList == null || mValidIndex == null) {
            return;
        }

        List<Location> totalList = new ArrayList<>(mTotalList);
        for (int i = 0; i < totalList.size(); i ++) {
            if (totalList.get(i).equals(location)) {
                totalList.set(i, location);
                break;
            }
        }

        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);

        int validIndex = indexLocation(validList, getCurrentFormattedId());

        boolean defaultLocation = location.equals(validList.get(0));
        LocationResource resource;
        if (!done) {
            resource = LocationResource.loading(
                    location, defaultLocation, locateFailed, LocationResource.Source.REFRESH);
        } else if (succeed) {
            resource = LocationResource.success(
                    location, defaultLocation, LocationResource.Source.REFRESH);
        } else {
            resource = LocationResource.error(
                    location, defaultLocation, locateFailed, LocationResource.Source.REFRESH);
        }

        setLocation(totalList, validList, validIndex, LocationResource.Source.BACKGROUND, resource);
    }
}