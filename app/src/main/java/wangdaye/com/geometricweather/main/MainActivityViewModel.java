package wangdaye.com.geometricweather.main;

import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import wangdaye.com.geometricweather.common.basic.GeoViewModel;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.main.models.Indicator;
import wangdaye.com.geometricweather.main.models.LocationResource;
import wangdaye.com.geometricweather.main.models.PermissionsRequest;
import wangdaye.com.geometricweather.main.models.SelectableLocationListResource;
import wangdaye.com.geometricweather.main.utils.MainModuleUtils;
import wangdaye.com.geometricweather.main.utils.MainThemeManager;
import wangdaye.com.geometricweather.main.utils.StatementManager;

@HiltViewModel
public class MainActivityViewModel extends GeoViewModel
        implements MainActivityRepository.WeatherRequestCallback {

    private final MutableLiveData<LocationResource> mCurrentLocation;
    private final MutableLiveData<Indicator> mIndicator;
    private final MutableLiveData<PermissionsRequest> mPermissionsRequest;
    private final MutableLiveData<SelectableLocationListResource> mListResource;

    private final SavedStateHandle mSavedStateHandle;
    private final MainActivityRepository mRepository;

    // inner data.
    private @Nullable String mFormattedId; // current formatted id.
    private @Nullable List<Location> mTotalList; // all locations.
    private @Nullable List<Location> mValidList; // location list optimized for resident city.

    private final StatementManager mStatementManager;
    private final MainThemeManager mThemeManager;

    private static final String KEY_FORMATTED_ID = "formatted_id";

    @Inject
    public MainActivityViewModel(
            Application application,
            SavedStateHandle handle,
            MainActivityRepository repository,
            StatementManager statementManager,
            MainThemeManager themeManager
    ) {
        super(application);

        mCurrentLocation = new MutableLiveData<>();
        mCurrentLocation.setValue(null);

        mListResource = new MutableLiveData<>();
        mListResource.setValue(new SelectableLocationListResource(
                new ArrayList<>(), null, null));

        mIndicator = new MutableLiveData<>();
        mIndicator.setValue(new Indicator(1, 0));

        mPermissionsRequest = new MutableLiveData<>();
        mPermissionsRequest.setValue(
                new PermissionsRequest(new ArrayList<>(), null, false));

        mSavedStateHandle = handle;
        mRepository = repository;

        mFormattedId = mSavedStateHandle.get(KEY_FORMATTED_ID);
        mTotalList = null;
        mValidList = null;

        mStatementManager = statementManager;
        mThemeManager = themeManager;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRepository.destroy();
    }

    public void init() {
        init(mFormattedId);
    }

    public void init(@Nullable String formattedId) {
        setFormattedId(formattedId);

        List<Location> oldList = mTotalList == null
                ? new ArrayList<>()
                : Collections.unmodifiableList(mTotalList);

        if (oldList.isEmpty()) {
            oldList = Collections.unmodifiableList(
                    DatabaseHelper.getInstance(getApplication()).readLocationList()
            );

            List<Location> totalList = new ArrayList<>(oldList);
            List<Location> validList = Location.excludeInvalidResidentLocation(
                    getApplication(),
                    totalList
            );
            int validIndex = indexLocation(validList, mFormattedId);

            setInnerData(totalList, validList, validIndex);

            Location location = validList.get(validIndex);
            location.setWeather(DatabaseHelper.getInstance(getApplication()).readWeather(location));

            Indicator indicator = new Indicator(validList.size(), validIndex);
            boolean defaultLocation = validIndex == 0;
            LocationResource.Event event = LocationResource.Event.INITIALIZE;

            setLocationResourceWithVerification(location, defaultLocation, event,
                    indicator, null, new SelectableLocationListResource.DataSetChanged());
        }

        mRepository.getLocationList(getApplication(), oldList, (locationList, done) -> {
            if (locationList == null) {
                return;
            }

            List<Location> totalList = new ArrayList<>(locationList);
            List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
            int validIndex = indexLocation(validList, mFormattedId);

            setInnerData(totalList, validList, validIndex);

            Location current = validList.get(validIndex);
            Indicator indicator = new Indicator(validList.size(), validIndex);
            boolean defaultLocation = validIndex == 0;
            LocationResource.Event event = done
                    ? LocationResource.Event.UPDATE
                    : LocationResource.Event.INITIALIZE;

            setLocationResourceWithVerification(current, defaultLocation, event,
                    indicator, null, new SelectableLocationListResource.DataSetChanged());
        });
    }

    public void checkWhetherToChangeTheme() {
        Location location = getCurrentLocationValue();
        if (location == null) {
            return;
        }

        boolean lightTheme = mThemeManager.isLightTheme();
        mThemeManager.update(getApplication(), location);
        if (mThemeManager.isLightTheme() == lightTheme) {
            return;
        }

        LocationResource resource = mCurrentLocation.getValue();
        assert resource != null;
        mCurrentLocation.setValue(
                new LocationResource(
                        location,
                        resource.status,
                        resource.defaultLocation,
                        resource.locateFailed,
                        resource.event
                )
        );
    }

    public void updateLocationFromBackground(Location location) {
        if (mTotalList == null || mValidList == null) {
            return;
        }

        List<Location> totalList = new ArrayList<>(mTotalList);
        for (int i = 0; i < totalList.size(); i ++) {
            if (totalList.get(i).getFormattedId().equals(location.getFormattedId())) {
                totalList.set(i, location);
                break;
            }
        }

        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
        int validIndex = indexLocation(validList, mFormattedId);

        setInnerData(totalList, validList, validIndex);

        Location current = validList.get(validIndex);
        Indicator indicator = new Indicator(validList.size(), validIndex);
        boolean defaultLocation = validIndex == 0;

        LocationResource.Event event = location.getFormattedId().equals(current.getFormattedId())
                ? LocationResource.Event.BACKGROUND_UPDATE_CURRENT
                : LocationResource.Event.BACKGROUND_UPDATE_OTHERS;
        setLocationResourceWithVerification(current, defaultLocation, event,
                indicator, null, new SelectableLocationListResource.DataSetChanged());
    }

    public void setLocation(@NonNull String formattedId) {
        if (mTotalList == null || mValidList == null) {
            return;
        }

        List<Location> totalList = new ArrayList<>(mTotalList);
        List<Location> validList = new ArrayList<>(mValidList);
        int validIndex = indexLocation(validList, formattedId);

        setInnerData(totalList, validList, validIndex);

        Location current = validList.get(validIndex);
        Indicator indicator = new Indicator(validList.size(), validIndex);
        boolean defaultLocation = validIndex == 0;

        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, null, new SelectableLocationListResource.DataSetChanged());
    }

    public void setLocation(int offset) {
        if (mTotalList == null || mValidList == null) {
            return;
        }

        int validIndex = indexLocation(mValidList, mFormattedId);
        validIndex += offset + mValidList.size();
        validIndex %= mValidList.size();

        List<Location> totalList = new ArrayList<>(mTotalList);
        List<Location> validList = new ArrayList<>(mValidList);

        setInnerData(totalList, validList, validIndex);

        Location current = validList.get(validIndex);
        Indicator indicator = new Indicator(validList.size(), validIndex);
        boolean defaultLocation = validIndex == 0;

        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, null, new SelectableLocationListResource.DataSetChanged());
    }

    public void updateWeather(boolean triggeredByUser, boolean checkPermissions) {
        if (mCurrentLocation.getValue() == null) {
            return;
        }

        mRepository.cancelWeatherRequest();

        Location location = mCurrentLocation.getValue().data;
        mThemeManager.update(getApplication(), location);
        mCurrentLocation.setValue(
                LocationResource.loading(
                        location,
                        mCurrentLocation.getValue().defaultLocation,
                        LocationResource.Event.UPDATE
                )
        );

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || !location.isCurrentPosition()
                || !checkPermissions) {
            // don't need to request any permission -> request data directly.
            mRepository.getWeather(
                    getApplication(), location, location.isCurrentPosition(), this);
            return;
        }

        // check permissions.
        List<String> permissionList = getDeniedPermissionList();
        if (permissionList.size() == 0) {
            // already got all permissions -> request data directly.
            mRepository.getWeather(
                    getApplication(), location, true, this);
            return;
        }

        // request permissions.
        mPermissionsRequest.setValue(new PermissionsRequest(permissionList, location, triggeredByUser));
    }

    public void requestPermissionsFailed(Location location) {
        if (mTotalList == null || mValidList == null) {
            return;
        }

        mThemeManager.update(getApplication(), location);
        mCurrentLocation.setValue(
                LocationResource.error(
                        location,
                        location.getFormattedId().equals(mValidList.get(0).getFormattedId()),
                        LocationResource.Event.UPDATE
                )
        );
    }

    public void addLocation(Location location) {
        assert mTotalList != null;
        addLocation(location, mTotalList.size());
    }

    public void addLocation(Location location, int position) {
        assert mTotalList != null;

        List<Location> totalList = new ArrayList<>(mTotalList);
        totalList.add(position, location);

        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
        int validIndex = indexLocation(validList, mFormattedId);

        setInnerData(totalList, validList, validIndex);

        Location current = validList.get(validIndex);
        Indicator indicator = new Indicator(validList.size(), validIndex);
        boolean defaultLocation = validIndex == 0;

        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, null, new SelectableLocationListResource.DataSetChanged());

        if (position == mTotalList.size() - 1) {
            mRepository.writeLocation(getApplication(), location);
        } else {
            mRepository.writeLocationList(getApplication(), Collections.unmodifiableList(totalList), position);
        }
    }

    public void moveLocation(int from, int to) {
        assert mTotalList != null;

        List<Location> totalList = new ArrayList<>(mTotalList);
        Collections.swap(totalList, from, to);

        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
        int validIndex = indexLocation(validList, mFormattedId);

        setInnerData(totalList, validList, validIndex);

        mListResource.setValue(new SelectableLocationListResource(
                totalList, mFormattedId, null,
                new SelectableLocationListResource.ItemMoved(from, to)));
    }

    public void moveLocationFinish() {
        assert mTotalList != null;

        List<Location> totalList = new ArrayList<>(mTotalList);
        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
        int validIndex = indexLocation(validList, mFormattedId);

        setInnerData(totalList, validList, validIndex);

        Location current = validList.get(validIndex);
        Indicator indicator = new Indicator(validList.size(), validIndex);
        boolean defaultLocation = validIndex == 0;

        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, null, new SelectableLocationListResource.DataSetChanged());

        mRepository.writeLocationList(getApplication(), mTotalList);
    }

    public void forceUpdateLocation(Location location) {
        assert mTotalList != null;

        List<Location> totalList = new ArrayList<>(mTotalList);
        for (int i = 0; i < totalList.size(); i ++) {
            if (totalList.get(i).getFormattedId().equals(location.getFormattedId())) {
                totalList.set(i, location);
                break;
            }
        }

        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
        int validIndex = indexLocation(validList, mFormattedId);

        setInnerData(totalList, validList, validIndex);

        Location current = validList.get(validIndex);
        Indicator indicator = new Indicator(validList.size(), validIndex);
        boolean defaultLocation = validIndex == 0;

        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, location.getFormattedId(), new SelectableLocationListResource.DataSetChanged());

        mRepository.writeLocation(getApplication(), location);
    }

    public void forceUpdateLocation(Location location, int position) {
        assert mTotalList != null;

        List<Location> totalList = new ArrayList<>(mTotalList);
        totalList.set(position, location);

        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
        int validIndex = indexLocation(validList, mFormattedId);

        setInnerData(totalList, validList, validIndex);

        Location current = validList.get(validIndex);
        Indicator indicator = new Indicator(validList.size(), validIndex);
        boolean defaultLocation = validIndex == 0;

        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, location.getFormattedId(), new SelectableLocationListResource.DataSetChanged());

        mRepository.writeLocation(getApplication(), location);
    }

    @Nullable
    public Location deleteLocation(int position) {
        assert mTotalList != null;

        List<Location> totalList = new ArrayList<>(mTotalList);
        Location location = totalList.remove(position);
        if (location.getFormattedId().equals(mFormattedId)) {
            setFormattedId(totalList.get(0).getFormattedId());
        }

        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
        int validIndex = indexLocation(validList, mFormattedId);

        setInnerData(totalList, validList, validIndex);

        Location current = validList.get(validIndex);
        Indicator indicator = new Indicator(validList.size(), validIndex);
        boolean defaultLocation = validIndex == 0;

        setLocationResourceWithVerification(current, defaultLocation, LocationResource.Event.UPDATE,
                indicator, location.getFormattedId(), new SelectableLocationListResource.DataSetChanged());

        mRepository.deleteLocation(getApplication(), location);

        return location;
    }

    private void setFormattedId(@Nullable String formattedId) {
        mFormattedId = formattedId;
        mSavedStateHandle.set(KEY_FORMATTED_ID, formattedId);
    }

    private void setInnerData(@NonNull List<Location> totalList, @NonNull List<Location> validList,
                              int validIndex) {
        mTotalList = totalList;
        mValidList = validList;
        setFormattedId(validList.get(validIndex).getFormattedId());
    }

    private void setLocationResourceWithVerification(Location location, boolean defaultLocation,
                                                     LocationResource.Event event,
                                                     Indicator indicator,
                                                     @Nullable String forceUpdateId,
                                                     @NonNull SelectableLocationListResource.Source source) {
        mThemeManager.update(getApplication(), location);

        switch (event) {
            case INITIALIZE:
                mCurrentLocation.setValue(
                        LocationResource.loading(location, defaultLocation, event));
                break;

            case UPDATE:
                if (MainModuleUtils.needUpdate(getApplication(), location)) {
                    mCurrentLocation.setValue(
                            LocationResource.loading(location, defaultLocation, event));
                    updateWeather(false, true);
                } else {
                    mRepository.cancelWeatherRequest();
                    mCurrentLocation.setValue(
                            LocationResource.success(location, defaultLocation, event));
                }
                break;

            case BACKGROUND_UPDATE_CURRENT: {
                mRepository.cancelWeatherRequest();
                mCurrentLocation.setValue(
                        LocationResource.success(location, defaultLocation, event));
                break;
            }
            case BACKGROUND_UPDATE_OTHERS: {
                LocationResource old = mCurrentLocation.getValue();
                if (old == null) {
                    return;
                }
                mCurrentLocation.setValue(
                        new LocationResource(location, old.status, defaultLocation, old.locateFailed, event));
                break;
            }
        }

        mIndicator.setValue(indicator);

        assert mTotalList != null;
        mListResource.setValue(new SelectableLocationListResource(
                new ArrayList<>(mTotalList), location.getFormattedId(), forceUpdateId, source));
    }

    private static int indexLocation(List<Location> locationList, @Nullable String formattedId) {
        if (TextUtils.isEmpty(formattedId)) {
            return 0;
        }

        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).getFormattedId().equals(formattedId)) {
                return i;
            }
        }

        return 0;
    }

    private List<String> getDeniedPermissionList() {
        List<String> permissionList = mRepository.getLocatePermissionList(getApplication());
        for (int i = permissionList.size() - 1; i >= 0; i --) {
            if (ActivityCompat.checkSelfPermission(getApplication(), permissionList.get(i))
                    == PackageManager.PERMISSION_GRANTED) {
                permissionList.remove(i);
            }
        }
        return permissionList;
    }

    @Nullable
    public Location getLocationFromList(int offset) {
        if (mTotalList == null || mValidList == null) {
            return null;
        }

        int validIndex = indexLocation(mValidList, mFormattedId);
        return mValidList.get(
                (validIndex + offset + mValidList.size()) % mValidList.size()
        );
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

    public MutableLiveData<SelectableLocationListResource> getListResource() {
        return mListResource;
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
        } else if (mFormattedId != null) {
            return mFormattedId;
        } else if (mValidList != null) {
            return mValidList.get(0).getFormattedId();
        } else {
            return null;
        }
    }

    public @Nullable List<Location> getTotalLocationList() {
        if (mTotalList != null) {
            return Collections.unmodifiableList(mTotalList);
        } else {
            return null;
        }
    }

    public @Nullable List<Location> getValidLocationList() {
        if (mValidList != null) {
            return Collections.unmodifiableList(mValidList);
        } else {
            return null;
        }
    }

    public PermissionsRequest getPermissionsRequestValue() {
        return mPermissionsRequest.getValue();
    }

    public StatementManager getStatementManager() {
        return mStatementManager;
    }

    public MainThemeManager getThemeManager() {
        return mThemeManager;
    }

    // weather request callback.

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
        if (mTotalList == null || mValidList == null) {
            return;
        }

        List<Location> totalList = new ArrayList<>(mTotalList);
        for (int i = 0; i < totalList.size(); i ++) {
            if (totalList.get(i).getFormattedId().equals(location.getFormattedId())) {
                totalList.set(i, location);
                break;
            }
        }

        List<Location> validList = Location.excludeInvalidResidentLocation(getApplication(), totalList);
        int validIndex = indexLocation(validList, getCurrentFormattedId());
        boolean defaultLocation = location.getFormattedId().equals(validList.get(0).getFormattedId());

        setInnerData(totalList, validList, validIndex);

        LocationResource resource;
        if (!done) {
            resource = LocationResource.loading(
                    location, defaultLocation, locateFailed, LocationResource.Event.UPDATE);
        } else if (succeed) {
            resource = LocationResource.success(
                    location, defaultLocation, LocationResource.Event.UPDATE);
        } else {
            resource = LocationResource.error(
                    location, defaultLocation, locateFailed, LocationResource.Event.UPDATE);
        }

        Indicator indicator = new Indicator(validList.size(), validIndex);

        mThemeManager.update(getApplication(), location);
        mCurrentLocation.setValue(resource);
        mIndicator.setValue(indicator);
        mListResource.setValue(new SelectableLocationListResource(
                totalList, mFormattedId, null,
                new SelectableLocationListResource.DataSetChanged()));
    }
}