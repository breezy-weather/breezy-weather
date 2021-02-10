package wangdaye.com.geometricweather.manage;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.manage.model.SelectableLocationListResource;

public class ManageFragmentViewModel extends ViewModel {

    private final MutableLiveData<SelectableLocationListResource> mListResource;
    private final ManageFragmentRepository mRepository;

    private boolean mNewInstance;

    public ManageFragmentViewModel() {
        mListResource = new MutableLiveData<>();
        mListResource.setValue(new SelectableLocationListResource(
                new ArrayList<>(), null, null));

        mRepository = new ManageFragmentRepository();

        mNewInstance = true;
    }

    public void init(Context context, @Nullable String selectedId) {
        mRepository.readLocationList(context, locationList -> {
            if (locationList == null) {
                return;
            }

            List<Location> list = new ArrayList<>(locationList);
            mListResource.setValue(
                    new SelectableLocationListResource(list, selectedId, null));
        });
    }

    public void readAppendCache(Context context) {
        List<Location> oldList = getLocationList();
        mRepository.readAppendLocation(context, oldList, locationList -> {
            if (locationList == null) {
                return;
            }

            List<Location> list = new ArrayList<>(locationList);
            mListResource.setValue(
                    new SelectableLocationListResource(list, getSelectedId(), null));
        });
    }

    public void addLocation(Context context, Location location) {
        addLocation(context, location, getLocationCount());
    }

    public void addLocation(Context context, Location location, int position) {
        List<Location> list = innerGetLocationList();
        list.add(position, location);

        mListResource.setValue(
                new SelectableLocationListResource(list, getSelectedId(), null));

        if (position == getLocationCount() - 1) {
            mRepository.writeLocation(context, location);
        } else {
            mRepository.writeLocationList(context, Collections.unmodifiableList(list), position);
        }
    }

    public void updateLocation(Context context,
                               List<Location> locationList, @Nullable String selectedId) {
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
            mRepository.readLocationList(context, newList, list -> {
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

    public void moveLocationFinish(Context context) {
        mRepository.writeLocationList(context, getLocationList());
    }

    public void forceUpdateLocation(Context context, Location location, int position) {
        List<Location> list = innerGetLocationList();
        list.set(position, location);

        mListResource.setValue(
                new SelectableLocationListResource(
                        list,
                        getSelectedId(),
                        list.get(position).getFormattedId()
                )
        );

        mRepository.writeLocation(context, location);
    }

    public Location deleteLocation(Context context, int position) {
        List<Location> list = innerGetLocationList();
        Location location = list.remove(position);

        location.setWeather(DatabaseHelper.getInstance(context).readWeather(location));

        String selectedId = getSelectedId();
        if (selectedId != null && location.getFormattedId().equals(selectedId)) {
            selectedId = list.get(0).getFormattedId();
        }

        mListResource.setValue(
                new SelectableLocationListResource(list, selectedId, null));

        mRepository.deleteLocation(context, location);
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

    public boolean isNewInstance() {
        if (mNewInstance) {
            mNewInstance = false;
            return true;
        }
        return false;
    }
}
