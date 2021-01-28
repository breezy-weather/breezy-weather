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

    private final MutableLiveData<SelectableLocationListResource> listResource;
    private final ManageFragmentRepository repository;

    private boolean newInstance;

    public ManageFragmentViewModel() {
        listResource = new MutableLiveData<>();
        listResource.setValue(new SelectableLocationListResource(
                new ArrayList<>(), null, null));

        repository = new ManageFragmentRepository();

        newInstance = true;
    }

    public void init(Context context, @Nullable String selectedId) {
        repository.readLocationList(context, locationList -> {
            if (locationList == null) {
                return;
            }

            List<Location> list = new ArrayList<>(locationList);
            listResource.setValue(
                    new SelectableLocationListResource(list, selectedId, null));
        });
    }

    public void readAppendCache(Context context) {
        List<Location> oldList = getLocationList();
        repository.readAppendLocation(context, oldList, locationList -> {
            if (locationList == null) {
                return;
            }

            List<Location> list = new ArrayList<>(locationList);
            listResource.setValue(
                    new SelectableLocationListResource(list, getSelectedId(), null));
        });
    }

    public void addLocation(Context context, Location location) {
        addLocation(context, location, getLocationCount());
    }

    public void addLocation(Context context, Location location, int position) {
        List<Location> list = innerGetLocationList();
        list.add(position, location);

        listResource.setValue(
                new SelectableLocationListResource(list, getSelectedId(), null));

        if (position == getLocationCount() - 1) {
            repository.writeLocation(context, location);
        } else {
            repository.writeLocationList(context, Collections.unmodifiableList(list), position);
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
            repository.readLocationList(context, newList, list -> {
                if (list == null) {
                    return;
                }
                listResource.setValue(
                        new SelectableLocationListResource(
                                new ArrayList<>(list), selectedId, null
                        )
                );
            });
        } else {
            listResource.setValue(
                    new SelectableLocationListResource(
                            new ArrayList<>(newList), selectedId, null)
            );
        }
    }

    public void moveLocation(int from, int to) {
        List<Location> list = innerGetLocationList();
        Collections.swap(list, from, to);
        listResource.setValue(
                new SelectableLocationListResource(
                        list,
                        getSelectedId(),
                        null,
                        new SelectableLocationListResource.ItemMoved(from, to)
                )
        );
    }

    public void moveLocationFinish(Context context) {
        repository.writeLocationList(context, getLocationList());
    }

    public void forceUpdateLocation(Context context, Location location, int position) {
        List<Location> list = innerGetLocationList();
        list.set(position, location);

        listResource.setValue(
                new SelectableLocationListResource(
                        list,
                        getSelectedId(),
                        list.get(position).getFormattedId()
                )
        );

        repository.writeLocation(context, location);
    }

    public Location deleteLocation(Context context, int position) {
        List<Location> list = innerGetLocationList();
        Location location = list.remove(position);

        location.setWeather(DatabaseHelper.getInstance(context).readWeather(location));

        String selectedId = getSelectedId();
        if (selectedId != null && location.getFormattedId().equals(selectedId)) {
            selectedId = list.get(0).getFormattedId();
        }

        listResource.setValue(
                new SelectableLocationListResource(list, selectedId, null));

        repository.deleteLocation(context, location);
        return location;
    }

    private List<Location> innerGetLocationList() {
        if (listResource.getValue() == null) {
            return new ArrayList<>();
        }
        return listResource.getValue().dataList;
    }

    public MutableLiveData<SelectableLocationListResource> getListResource() {
        return listResource;
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
        if (listResource.getValue() == null) {
            return null;
        } else {
            return listResource.getValue().selectedId;
        }
    }

    public boolean isNewInstance() {
        if (newInstance) {
            newInstance = false;
            return true;
        }
        return false;
    }
}
