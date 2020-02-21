package wangdaye.com.geometricweather.main;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import wangdaye.com.geometricweather.basic.GeoActivity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.main.model.Indicator;
import wangdaye.com.geometricweather.main.model.LocationResource;
import wangdaye.com.geometricweather.main.model.LockableLocationList;
import wangdaye.com.geometricweather.main.model.UpdatePackage;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;

public class MainActivityViewModel extends ViewModel
        implements MainActivityRepository.OnLocationCompletedListener {

    private MutableLiveData<LocationResource> currentLocation;
    private MutableLiveData<Indicator> indicator;
    private LockableLocationList lockableLocationList;
    private MainActivityRepository repository;

    private boolean newInstance;

    private static final int INVALID_LOCATION_INDEX = -1;

    public MainActivityViewModel() {
        currentLocation = new MutableLiveData<>();

        indicator = new MutableLiveData<>();
        indicator.setValue(new Indicator(1, 0));

        lockableLocationList = new LockableLocationList();

        newInstance = true;
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

        Observable.create((ObservableOnSubscribe<UpdatePackage>) emitter ->
            lockableLocationList.write((getter, setter) -> {
                setter.setCurrentPositionIndex(INVALID_LOCATION_INDEX);
                setter.setCurrentIndex(0);

                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(activity);
                List<Location> locationList = databaseHelper.readLocationList();
                setter.setLocationList(locationList);
                for (int i = 0; i < locationList.size(); i ++) {
                    locationList.get(i).setWeather(databaseHelper.readWeather(locationList.get(i)));
                    if (locationList.get(i).isCurrentPosition()) {
                        setter.setCurrentPositionIndex(i);
                    }
                    if (locationList.get(i).equals(formattedId)) {
                        setter.setCurrentIndex(i);
                    }
                }

                int currentPositionIndex = getter.getCurrentPositionIndex();
                int currentIndex = getter.getCurrentIndex();
                if (locationList.get(currentIndex).isResidentPosition()) {
                    Location current = locationList.get(currentIndex);
                    Location currentPosition = currentPositionIndex == INVALID_LOCATION_INDEX
                            ? null
                            : locationList.get(currentPositionIndex);
                    if (currentPosition != null && currentPosition.isCloseTo(activity, current)) {
                        currentIndex = currentPositionIndex;
                    }
                }

                emitter.onNext(
                        new UpdatePackage(
                                locationList.get(currentIndex),
                                getIndicatorInstance(activity, locationList, currentPositionIndex, currentIndex)
                        )
                );
            })
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(updatePackage -> setLocation(activity, updatePackage, false))
                .subscribe();
    }

    public void setLocation(GeoActivity activity, int offset) {
        AtomicReference<UpdatePackage> pkg = new AtomicReference<>();

        lockableLocationList.write((getter, setter) -> {
            setter.setCurrentIndex(getLocationIndexFromList(activity, getter, offset));
            pkg.set(new UpdatePackage(
                    getter.getLocationList().get(getter.getCurrentIndex()),
                    getIndicatorInstance(activity, getter)
            ));
        });

        setLocation(activity, pkg.get(), false);
    }

    private void setLocation(GeoActivity activity, UpdatePackage pkg, boolean updatedInBackground) {
        Location current = pkg.location;
        Indicator i = indicator.getValue();

        if (i == null || !i.equals(pkg.indicator)) {
            indicator.postValue(pkg.indicator);
        }

        float pollingIntervalInHour = SettingsOptionManager.getInstance(activity)
                .getUpdateInterval()
                .getIntervalInHour();
        boolean defaultLocation = pkg.indicator.index == 0;
        if (current.isUsable()
                && current.getWeather() != null
                && current.getWeather().isValid(pollingIntervalInHour)) {
            repository.cancel();
            currentLocation.setValue(LocationResource.success(current, defaultLocation, updatedInBackground));
        } else {
            currentLocation.setValue(LocationResource.loading(current, defaultLocation));
            updateWeather(activity);
        }
    }

    private Indicator getIndicatorInstance(Context context, LockableLocationList.Getter getter) {
        return getIndicatorInstance(context, getter.getLocationList(),
                getter.getCurrentPositionIndex(), getter.getCurrentIndex());
    }

    private Indicator getIndicatorInstance(Context context, List<Location> locationList,
                                           int currentPositionIndex, int currentIndex) {
        int index = 0;
        int total = 0;
        for (int i = 0; i < locationList.size(); i ++) {
            if (i == currentIndex) {
                index = total;
            }
            if (currentPositionIndex == INVALID_LOCATION_INDEX
                    || !locationList.get(i).isResidentPosition()
                    || !locationList.get(i).isCloseTo(context, locationList.get(currentPositionIndex))) {
                total ++;
            }
        }
        return new Indicator(total, index);
    }

    public void updateLocationFromBackground(GeoActivity activity, @Nullable String formattedId) {
        Observable.create((ObservableOnSubscribe<UpdatePackage>) emitter -> {
            AtomicReference<UpdatePackage> pkg = new AtomicReference<>(null);

            lockableLocationList.write((getter, setter) -> {
                List<Location> locationList = new ArrayList<>(getter.getLocationList());

                int index = indexLocation(locationList, formattedId);
                if (index == INVALID_LOCATION_INDEX) {
                    return;
                }

                Location location = DatabaseHelper.getInstance(activity).readLocation(locationList.get(index));
                if (location == null) {
                    return;
                }

                location.setWeather(DatabaseHelper.getInstance(activity).readWeather(location));
                locationList.set(index, location);
                setter.setLocationList(locationList);

                if (index == getter.getCurrentIndex()) {
                    pkg.set(new UpdatePackage(location, getIndicatorInstance(activity, getter)));
                }
            });

            if (pkg.get() != null) {
                emitter.onNext(pkg.get());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(updatePackage -> setLocation(activity, updatePackage, true))
                .subscribe();
    }

    private int indexLocation(List<Location> locationList, @Nullable String formattedId) {
        if (TextUtils.isEmpty(formattedId)) {
            return INVALID_LOCATION_INDEX;
        }

        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).equals(formattedId)) {
                return i;
            }
        }

        return INVALID_LOCATION_INDEX;
    }

    public int getLocationIndexFromList(GeoActivity activity, LockableLocationList.Getter getter, int offset) {
        return getLocationIndexFromList(activity, getter.getLocationList(),
                getter.getCurrentPositionIndex(), getter.getCurrentIndex(), offset);
    }

    public int getLocationIndexFromList(GeoActivity activity, List<Location> locationList,
                                        int currentPositionIndex, int currentIndex, int offset) {
        if (offset == 0) {
            return currentIndex;
        }

        int index = currentIndex;
        index += locationList.size() + offset;
        index %= locationList.size();
        while (currentPositionIndex != INVALID_LOCATION_INDEX
                && locationList.get(index).isResidentPosition()
                && locationList.get(index).isCloseTo(activity, locationList.get(currentPositionIndex))) {
            index += locationList.size() + (offset > 0 ? 1 : -1);
            index %= locationList.size();
        }
        return index;
    }

    public Location getLocationFromList(GeoActivity activity, int offset) {
        final Location[] location = new Location[1];
        lockableLocationList.read(getter ->
                location[0] = getter.getLocationList().get(
                        getLocationIndexFromList(activity, getter, offset)
                )
        );
        return location[0];
    }

    public void updateWeather(GeoActivity activity) {
        repository.cancel();

        assert currentLocation.getValue() != null;
        Location location = currentLocation.getValue().data;

        currentLocation.setValue(
                LocationResource.loading(location, currentLocation.getValue().isDefaultLocation())
        );

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
                                        repository.getWeather(activity,
                                                currentLocation, lockableLocationList,false, this);
                                    } else {
                                        currentLocation.setValue(
                                                LocationResource.error(location, true));
                                    }
                                    return;
                                }
                            }
                            repository.getWeather(activity, currentLocation, lockableLocationList, true, this);
                        });
                return;
            }
        }

        repository.getWeather(activity, currentLocation, lockableLocationList, location.isCurrentPosition(), this);
    }

    private boolean isPivotalPermission(String permission) {
        return permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                || permission.equals(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public MutableLiveData<LocationResource> getCurrentLocation() {
        return currentLocation;
    }

    public MutableLiveData<Indicator> getIndicator() {
        return indicator;
    }

    @Nullable
    public Location getCurrentLocationValue() {
        LocationResource resource = currentLocation.getValue();
        if (resource != null) {
            return resource.data;
        } else {
            return null;
        }
    }

    @Nullable
    public String getCurrentLocationFormattedId() {
        Location location = getCurrentLocationValue();
        if (location != null) {
            return location.getFormattedId();
        } else {
            return null;
        }
    }

    public int getIndicatorCountValue() {
        if (indicator.getValue() != null) {
            return indicator.getValue().total;
        } else {
            return 1;
        }
    }

    public int getIndicatorIndexValue() {
        if (indicator.getValue() != null) {
            return indicator.getValue().index;
        } else {
            return 0;
        }
    }

    public List<Location> getLocationList() {
        List<Location> locationList = new ArrayList<>();
        lockableLocationList.read(getter -> locationList.addAll(getter.getLocationList()));
        return locationList;
    }

    public boolean isNewInstance() {
        if (newInstance) {
            newInstance = false;
            return true;
        }
        return false;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (repository != null) {
            repository.cancel();
        }
    }

    // on location completed listener.

    @Override
    public void onCompleted(Context context) {
        Indicator old = indicator.getValue();
        final Indicator[] now = {null};

        lockableLocationList.read(getter -> now[0] = getIndicatorInstance(context, getter));

        if (old == null || !old.equals(now[0])) {
            indicator.setValue(now[0]);
        }
    }
}