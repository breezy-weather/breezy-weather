package wangdaye.com.geometricweather.management;

import android.app.Application;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoViewModel;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.management.models.SelectableLocationListResource;

public class ManagementFragmentViewModel extends GeoViewModel {

    private final MutableLiveData<SelectableLocationListResource> mListResource;
    private final ManagementFragmentRepository mRepository;

    public ManagementFragmentViewModel(Application application) {
        this(application, new ManagementFragmentRepository());
    }

    public ManagementFragmentViewModel(Application application,
                                       ManagementFragmentRepository repository) {
        super(application);
        mListResource = new MutableLiveData<>();
        mListResource.setValue(new SelectableLocationListResource(
                new ArrayList<>(), null, null));

        mRepository = repository;
    }

    public void resetLocationList(@Nullable String selectedId) {
        mRepository.readLocationList(getApplication(), (locationList, done) -> {
            if (locationList == null) {
                return;
            }

            List<Location> list = new ArrayList<>(locationList);
            mListResource.setValue(
                    new SelectableLocationListResource(list, selectedId, null));
        });
    }

    public void readAppendCache() {
        List<Location> oldList = getLocationList();
        mRepository.readAppendLocation(getApplication(), oldList, (locationList, done) -> {
            if (locationList == null) {
                return;
            }

            List<Location> list = new ArrayList<>(locationList);
            mListResource.setValue(
                    new SelectableLocationListResource(list, getSelectedId(), null));
        });
    }

    public void addLocation(Location location) {
        addLocation(location, getLocationCount());
    }

    public void addLocation(Location location, int position) {
        List<Location> list = innerGetLocationList();
        list.add(position, location);

        mListResource.setValue(
                new SelectableLocationListResource(list, getSelectedId(), null));

        if (position == getLocationCount() - 1) {
            mRepository.writeLocation(getApplication(), location);
        } else {
            mRepository.writeLocationList(getApplication(), Collections.unmodifiableList(list), position);
        }
    }

    public void updateLocation(List<Location> locationList, @Nullable String selectedId) {
        List<Location> oldList = getLocationList();
        List<Location> newList = new ArrayList<>(locationList);

        boolean needReadList = false;
        for (Location newOne : newList) {
            if (newOne.getWeather() != null) {
                continue;
            }

            boolean hasWeather = false;
            for (Location oldOne : oldList) {
                if (newOne.equals(oldOne)) {
                    newOne.setWeather(oldOne.getWeather());
                    hasWeather = true;
                    break;
                }
            }
            if (!hasWeather) {
                needReadList = true;
            }
        }

        if (needReadList) {
            mRepository.readLocationList(getApplication(), newList, (list, done) -> {
                if (list == null) {
                    return;
                }
                mListResource.setValue(
                        new SelectableLocationListResource(
                                new ArrayList<>(list), selectedId, null
                        )
                );
            });
        } else {
            mListResource.setValue(
                    new SelectableLocationListResource(
                            new ArrayList<>(newList), selectedId, null)
            );
        }
    }

    public void moveLocation(int from, int to) {
        List<Location> list = innerGetLocationList();
        Collections.swap(list, from, to);
        mListResource.setValue(
                new SelectableLocationListResource(
                        list,
                        getSelectedId(),
                        null,
                        new SelectableLocationListResource.ItemMoved(from, to)
                )
        );
    }

    public void moveLocationFinish() {
        mRepository.writeLocationList(getApplication(), getLocationList());
    }

    public void forceUpdateLocation(Location location, int position) {
        List<Location> list = innerGetLocationList();
        list.set(position, location);

        mListResource.setValue(
                new SelectableLocationListResource(
                        list,
                        getSelectedId(),
                        list.get(position).getFormattedId()
                )
        );

        mRepository.writeLocation(getApplication(), location);
    }

    public Location deleteLocation(int position) {
        List<Location> list = innerGetLocationList();
        Location location = list.remove(position);

        location.setWeather(DatabaseHelper.getInstance(getApplication()).readWeather(location));

        String selectedId = getSelectedId();
        if (selectedId != null && location.getFormattedId().equals(selectedId)) {
            selectedId = list.get(0).getFormattedId();
        }

        mListResource.setValue(
                new SelectableLocationListResource(list, selectedId, null));

        mRepository.deleteLocation(getApplication(), location);
        return location;
    }

    private List<Location> innerGetLocationList() {
        if (mListResource.getValue() == null) {
            return new ArrayList<>();
        }
        return mListResource.getValue().dataList;
    }

    public MutableLiveData<SelectableLocationListResource> getListResource() {
        return mListResource;
    }

    public List<Location> getLocationList() {
        return Collections.unmodifiableList(innerGetLocationList());
    }

    public Location getLocation(int position) {
        return innerGetLocationList().get(position);
    }

    public int getLocationCount() {
        return innerGetLocationList().size();
    }

    public String getSelectedId() {
        if (mListResource.getValue() == null) {
            return null;
        } else {
            return mListResource.getValue().selectedId;
        }
    }
}
