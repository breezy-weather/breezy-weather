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
import wangdaye.com.geometricweather.main.dialog.BackgroundLocationDialog;
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
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(activity);
                List<Location> totalList = databaseHelper.readLocationList();
                for (Location l : totalList) {
                    l.setWeather(databaseHelper.readWeather(l));
                }
                setter.setLocationList(activity, totalList);
                setter.setCurrentIndex(0);

                List<Location> validList = getter.getValidList();
                for (int i = 0; i < validList.size(); i ++) {
                    if (validList.get(i).equals(formattedId)) {
                        setter.setCurrentIndex(i);
                        break;
                    }
                }

                emitter.onNext(
                        new UpdatePackage(
                                validList.get(getter.getValidCurrentIndex()),
                                getIndicatorInstance(getter)
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
            setter.setCurrentIndex(getLocationIndexFromList(getter, offset));
            pkg.set(new UpdatePackage(
                    getter.getValidList().get(getter.getValidCurrentIndex()),
                    getIndicatorInstance(getter)
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

    private Indicator getIndicatorInstance(LockableLocationList.Getter getter) {
        return new Indicator(getter.getValidList().size(), getter.getValidCurrentIndex());
    }

    public void updateLocationFromBackground(GeoActivity activity, @Nullable String formattedId) {
        Observable.create((ObservableOnSubscribe<UpdatePackage>) emitter -> {
            AtomicReference<UpdatePackage> pkg = new AtomicReference<>(null);

            lockableLocationList.write((getter, setter) -> {
                List<Location> totalList = new ArrayList<>(getter.getTotalList());
                String currentId = getter.getValidFormattedId();

                int index = indexLocation(totalList, formattedId);
                if (index == INVALID_LOCATION_INDEX) {
                    return;
                }

                Location location = DatabaseHelper.getInstance(activity).readLocation(totalList.get(index));
                if (location == null) {
                    return;
                }

                location.setWeather(DatabaseHelper.getInstance(activity).readWeather(location));
                totalList.set(index, location);
                setter.setLocationList(activity, totalList);

                if (currentId.equals(formattedId)) {
                    pkg.set(new UpdatePackage(location, getIndicatorInstance(getter)));
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

    private int getLocationIndexFromList(LockableLocationList.Getter getter, int offset) {
        if (offset == 0) {
            return getter.getValidCurrentIndex();
        }
        int validSize = getter.getValidList().size();
        return (getter.getValidCurrentIndex() + validSize + offset) % validSize;
    }

    public Location getLocationFromList(int offset) {
        final Location[] location = new Location[1];
        lockableLocationList.read(getter ->
                location[0] = getter.getValidList().get(
                        getLocationIndexFromList(getter, offset)
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
            // check basic location permissions.
            List<String> permissionList = getDeniedPermissionList(activity, false);
            if (permissionList.size() != 0) {
                // request basic location permissions.
                activity.requestPermissions(permissionList.toArray(new String[0]), 0,
                        (requestCode, permission, grantResult) -> {
                            for (int i = 0; i < permission.length && i < grantResult.length; i++) {
                                if (isPivotalPermission(permission[i])
                                        && grantResult[i] != PackageManager.PERMISSION_GRANTED) {
                                    // denied basic location permissions.
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

                            repository.getWeather(activity, currentLocation, lockableLocationList, true, this);
                        });
                return;
            }
        }

        repository.getWeather(activity, currentLocation, lockableLocationList, location.isCurrentPosition(), this);
    }

    private List<String> getDeniedPermissionList(Context context, boolean background) {
        List<String> permissionList = repository.getLocatePermissionList(background);
        for (int i = permissionList.size() - 1; i >= 0; i --) {
            if (ActivityCompat.checkSelfPermission(context, permissionList.get(i))
                    == PackageManager.PERMISSION_GRANTED) {
                permissionList.remove(i);
            }
        }
        return permissionList;
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
        lockableLocationList.read(getter -> locationList.addAll(getter.getValidList()));
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

        lockableLocationList.read(getter -> now[0] = getIndicatorInstance(getter));

        if (old == null || !old.equals(now[0])) {
            indicator.setValue(now[0]);
        }
    }
}