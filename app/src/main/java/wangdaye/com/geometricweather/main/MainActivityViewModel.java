package wangdaye.com.geometricweather.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.main.dialog.BackgroundLocationDialog;
import wangdaye.com.geometricweather.main.dialog.LocationPermissionStatementDialog;
import wangdaye.com.geometricweather.main.model.Indicator;
import wangdaye.com.geometricweather.main.model.LocationResource;

public class MainActivityViewModel extends ViewModel
        implements MainActivityRepository.WeatherRequestCallback {

    private final MutableLiveData<LocationResource> mCurrentLocation;
    private final MutableLiveData<Indicator> mIndicator;

    private MainActivityRepository mRepository;

    private @Nullable List<Location> mTotalList; // all locations.
    private @Nullable List<Location> mValidList; // location list optimized for resident city.
    private @Nullable Integer mValidIndex; // current index for validList.

    private boolean mNewInstance;
    private boolean mRequestingPermissions;

    public MainActivityViewModel() {
        mCurrentLocation = new MutableLiveData<>();
        mCurrentLocation.setValue(null);

        mIndicator = new MutableLiveData<>();
        mIndicator.setValue(new Indicator(1, 0));

        mTotalList = null;
        mValidList = null;
        mValidIndex = null;

        mNewInstance = true;
        mRequestingPermissions = false;
    }

    public void reset(GeoActivity activity) {
        LocationResource resource = mCurrentLocation.getValue();
        if (resource != null) {
            init(activity, resource.data);
        }
    }

    public void init(GeoActivity activity, @NonNull Location location) {
        init(activity, location.getFormattedId());
    }

    public void init(GeoActivity activity, @Nullable String formattedId) {
        if (mRepository == null) {
            mRepository = new MainActivityRepository(activity);
        }

        final boolean[] firstInit = {true};

        List<Location> oldList = mTotalList == null
                ? new ArrayList<>()
                : Collections.unmodifiableList(mTotalList);
        mRepository.getLocationList(activity, oldList, locationList -> {
            if (locationList == null) {
                return;
            }

            List<Location> totalList = new ArrayList<>(locationList);
            List<Location> validList = Location.excludeInvalidResidentLocation(activity, totalList);

            int validIndex = firstInit[0]
                    ? indexLocation(validList, formattedId)
                    : indexLocation(mValidList, getCurrentFormattedId());
            firstInit[0] = false;

            setLocation(activity, totalList, validList, validIndex,
                    LocationResource.Source.REFRESH, null);
        });
    }

    public void setLocation(GeoActivity activity, int offset) {
        if (mTotalList == null || mValidList == null || mValidIndex == null) {
            return;
        }

        int index = mValidIndex;
        index = Math.max(index, 0);
        index = Math.min(index, mValidList.size() - 1);
        index += offset + mValidList.size();
        index %= mValidList.size();

        setLocation(
                activity,
                new ArrayList<>(mTotalList),
                new ArrayList<>(mValidList),
                index,
                LocationResource.Source.SWITCH,
                null
        );
    }

    private void setLocation(GeoActivity activity,
                             @NonNull List<Location> newTotalList,
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
        if (resource == null && MainModuleUtils.needUpdate(activity, current)) {
            mCurrentLocation.setValue(LocationResource.loading(current, defaultLocation, source));
            updateWeather(activity, false);
        } else if (resource == null) {
            mRepository.cancel();
            mCurrentLocation.setValue(LocationResource.success(current, defaultLocation, source));
        } else {
            mCurrentLocation.setValue(resource);
        }
        mIndicator.setValue(i);
    }

    public void updateLocationFromBackground(GeoActivity activity, @Nullable String formattedId) {
        if (TextUtils.isEmpty(formattedId)
                || mTotalList == null || mValidList == null || mValidIndex == null) {
            return;
        }

        assert formattedId != null;
        mRepository.getLocationAndWeatherCache(activity, formattedId, location -> {

            List<Location> totalList = new ArrayList<>(mTotalList);
            for (int i = 0; i < totalList.size(); i ++) {
                if (totalList.get(i).equals(location)) {
                    totalList.set(i, location);
                    break;
                }
            }

            List<Location> validList = Location.excludeInvalidResidentLocation(activity, totalList);

            int validIndex = indexLocation(validList, getCurrentFormattedId());

            setLocation(activity, totalList, validList, validIndex,
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

    public void updateWeather(GeoActivity activity, boolean triggeredByUser) {
        if (mCurrentLocation.getValue() == null) {
            return;
        }

        mRepository.cancel();

        Location location = mCurrentLocation.getValue().data;

        mCurrentLocation.setValue(
                LocationResource.loading(
                        location,
                        mCurrentLocation.getValue().defaultLocation,
                        LocationResource.Source.REFRESH
                )
        );

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !location.isCurrentPosition()) {
            // don't need to request any permission -> request data directly.
            mRepository.getWeather(
                    activity, location, location.isCurrentPosition(), triggeredByUser, this);
            return;
        }

        // check basic location permissions.
        List<String> permissionList = getDeniedPermissionList(activity, false);
        if (permissionList.size() == 0) {
            // already got all permissions -> request data directly.
            mRepository.getWeather(
                    activity, location, true, triggeredByUser, this);
            return;
        }

        // only show dialog if we need request location permissions.
        boolean needShowDialog = false;
        for (String permission : permissionList) {
            if (isNecessaryPermission(permission)) {
                needShowDialog = true;
                break;
            }
        }
        if (needShowDialog) {
            // only show dialog once.
            if (!mRequestingPermissions) {
                mRequestingPermissions = true;
                // need request permissions -> request basic location permissions.
                LocationPermissionStatementDialog dialog = new LocationPermissionStatementDialog();
                dialog.setOnSetButtonClickListener(() -> {
                    mRequestingPermissions = false;
                    requireLocationPermissionsAndUpdate(activity, permissionList, location, triggeredByUser);
                });
                dialog.setCancelable(false);
                dialog.show(activity.getSupportFragmentManager(), null);
            }
        } else {
            requireLocationPermissionsAndUpdate(activity, permissionList, location, triggeredByUser);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requireLocationPermissionsAndUpdate(GeoActivity activity,
                                                     List<String> permissionList,
                                                     Location location,
                                                     boolean triggeredByUser) {
        String[] permissions = permissionList.toArray(new String[0]);

        activity.requestPermissions(permissions, 0, (requestCode, permission, grantResult) -> {

            for (int i = 0; i < permission.length && i < grantResult.length; i++) {
                if (isNecessaryPermission(permission[i])
                        && grantResult[i] != PackageManager.PERMISSION_GRANTED) {
                    // denied basic location permissions.
                    if (location.isUsable()) {
                        mRepository.getWeather(
                                activity, location, false, triggeredByUser, this);
                    } else {
                        mCurrentLocation.setValue(
                                LocationResource.error(
                                        location,
                                        true,
                                        LocationResource.Source.REFRESH
                                )
                        );
                    }
                    return;
                }
            }

            // check background location permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                List<String> backgroundPermissionList = getDeniedPermissionList(activity, true);
                if (backgroundPermissionList.size() != 0) {
                    BackgroundLocationDialog dialog = new BackgroundLocationDialog();
                    dialog.setOnSetButtonClickListener(() ->
                            activity.requestPermissions(
                                    backgroundPermissionList.toArray(new String[0]),
                                    0,
                                    null
                            )
                    );
                    dialog.show(activity.getSupportFragmentManager(), null);
                }
            }

            mRepository.getWeather(
                    activity, location, true, triggeredByUser, this);
        });
    }

    private List<String> getDeniedPermissionList(Context context, boolean background) {
        List<String> permissionList = mRepository.getLocatePermissionList(background);
        for (int i = permissionList.size() - 1; i >= 0; i --) {
            if (ActivityCompat.checkSelfPermission(context, permissionList.get(i))
                    == PackageManager.PERMISSION_GRANTED) {
                permissionList.remove(i);
            }
        }
        return permissionList;
    }

    private boolean isNecessaryPermission(String permission) {
        return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public MutableLiveData<LocationResource> getCurrentLocation() {
        return mCurrentLocation;
    }

    public MutableLiveData<Indicator> getIndicator() {
        return mIndicator;
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

    public @NonNull List<Location> getLocationList() {
        if (mValidList != null) {
            return Collections.unmodifiableList(mValidList);
        } else {
            return new ArrayList<>();
        }
    }

    public boolean isNewInstance() {
        if (mNewInstance) {
            mNewInstance = false;
            return true;
        }
        return false;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (mRepository != null) {
            mRepository.cancel();
        }
    }

    // weather request callback.

    @Override
    public void onReadCacheCompleted(GeoActivity activity, Location location, boolean succeed, boolean done) {
        callback(activity, location, false, succeed, done);
    }

    @Override
    public void onLocationCompleted(GeoActivity activity, Location location, boolean succeed, boolean done) {
        callback(activity, location, !succeed, succeed, done);
    }

    @Override
    public void onGetWeatherCompleted(GeoActivity activity, Location location, boolean succeed, boolean done) {
        callback(activity, location, false, succeed, done);
    }

    private void callback(GeoActivity activity, Location location,
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

        List<Location> validList = Location.excludeInvalidResidentLocation(activity, totalList);

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

        setLocation(activity, totalList, validList, validIndex,
                LocationResource.Source.BACKGROUND, resource);
    }
}