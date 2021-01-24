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
            List<Location> list = new ArrayList<>(locationList);
            listResource.setValue(
                    new SelectableLocationListResource(list, selectedId, null));
        });
    }

    public void readAppendCache(Context context) {
        List<Location> oldList = Collections.unmodifiableList(listResource.getValue().dataList);
        repository.readAppendLocation(context, oldList, locationList -> {
            List<Location> list = new ArrayList<>(locationList);
            String selectedId = listResource.getValue().selectedId;
            listResource.setValue(
                    new SelectableLocationListResource(list, selectedId, null));
        });
    }

    public void addLocation(Context context, Location location) {
        addLocation(context, location, listResource.getValue().dataList.size());
    }

    public void addLocation(Context context, Location location, int position) {
        List<Location> list = listResource.getValue().dataList;
        list.add(position, location);

        String selectedId = listResource.getValue().selectedId;

        listResource.setValue(
                new SelectableLocationListResource(list, selectedId, null));

        if (position == listResource.getValue().dataList.size() - 1) {
            repository.writeLocation(context, location);
        } else {
            repository.writeLocationList(context, list, position);
        }
    }

    public void updateLocation(Context context,
                               List<Location> locationList, @Nullable String selectedId) {
        List<Location> oldList = listResource.getValue().dataList;
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
            repository.readLocationList(context, newList, list -> listResource.setValue(
                    new SelectableLocationListResource(
                            new ArrayList<>(list), selectedId, null)
            ));
        } else {
            listResource.setValue(
                    new SelectableLocationListResource(
                            new ArrayList<>(newList), selectedId, null)
            );
        }
    }

    public void moveLocation(Context context, int from, int to) {
        List<Location> list = listResource.getValue().dataList;
        list.add(to, list.remove(from));

        String selectedId = listResource.getValue().selectedId;

        listResource.setValue(
                new SelectableLocationListResource(list, selectedId, null));

        repository.writeLocationList(context, list);
    }

    public void forceUpdateLocation(Context context, Location location, int position) {
        List<Location> list = listResource.getValue().dataList;
        list.set(position, location);

        String selectedId = listResource.getValue().selectedId;

        listResource.setValue(
                new SelectableLocationListResource(
                        list,
                        selectedId,
                        list.get(position).getFormattedId()
                )
        );

        repository.writeLocation(context, location);
    }

    public Location deleteLocation(Context context, int position) {
        List<Location> list = listResource.getValue().dataList;
        Location location = list.remove(position);

        location.setWeather(DatabaseHelper.getInstance(context).readWeather(location));

        String selectedId = listResource.getValue().selectedId;
        if (selectedId != null && location.getFormattedId().equals(selectedId)) {
            selectedId = list.get(0).getFormattedId();
        }

        listResource.setValue(
                new SelectableLocationListResource(list, selectedId, null));

        repository.deleteLocation(context, location);
        return location;
    }

    public MutableLiveData<SelectableLocationListResource> getListResource() {
        return listResource;
    }

    public boolean isNewInstance() {
        if (newInstance) {
            newInstance = false;
            return true;
        }
        return false;
    }
}
