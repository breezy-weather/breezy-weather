package wangdaye.com.geometricweather.management;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

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
    private final SavedStateHandle mSavedStateHandle;

    private @Nullable String mSelectedId;
    private boolean mSelectable;

    private Status mStatus;
    private enum Status {
        INITIALIZING, IDLE
    }

    private static final String KEY_SELECTED_ID = "selected_id";

    public ManagementFragmentViewModel(Application application, SavedStateHandle handle) {
        this(application, handle, new ManagementFragmentRepository());
    }

    public ManagementFragmentViewModel(Application application, SavedStateHandle handle,
                                       ManagementFragmentRepository repository) {
        super(application);
        mListResource = new MutableLiveData<>();
        mListResource.setValue(new SelectableLocationListResource(
                new ArrayList<>(), null, null, false));

        mRepository = repository;
        mSavedStateHandle = handle;

        mSelectedId = handle.get(KEY_SELECTED_ID);
        mSelectable = false;

        mStatus = Status.IDLE;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRepository.destroy();
    }

    public void init(@Nullable String selectedId) {
        setSelectedId(selectedId);

        mStatus = Status.INITIALIZING;
        mRepository.readLocationList(getApplication(), (locationList, done) -> {
            if (mStatus == Status.IDLE) {
                // Initialize in advance by another method.
                return;
            }

            if (done) {
                mStatus = Status.IDLE;
            }

            if (locationList == null) {
                return;
            }

            List<Location> list = new ArrayList<>(locationList);
            mListResource.setValue(new SelectableLocationListResource(
                    list, getValidSelectedId(list), null, false));
        });
    }

    public void updateFromOutside(List<Location> locationList, @Nullable String selectedId) {
        setSelectedId(selectedId);

        mStatus = Status.IDLE;
        mListResource.setValue(
                new SelectableLocationListResource(
                        new ArrayList<>(locationList),
                        getValidSelectedId(locationList),
                        null,
                        false
                )
        );
    }

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;

        List<Location> list = copyLocationList();
        mListResource.setValue(new SelectableLocationListResource(
                list, getValidSelectedId(list), null, false));
    }

    public void addLocation(Location location) {
        addLocation(location, getLocationCount());
    }

    public void addLocation(Location location, int position) {
        if (mStatus == Status.INITIALIZING) {
            return;
        }

        List<Location> list = copyLocationList();
        list.add(position, location);

        mListResource.setValue(new SelectableLocationListResource(
                list, getValidSelectedId(list), null, true));

        if (position == getLocationCount() - 1) {
            mRepository.writeLocation(getApplication(), location);
        } else {
            mRepository.writeLocationList(getApplication(), Collections.unmodifiableList(list), position);
        }
    }

    public void moveLocation(int from, int to) {
        if (mStatus == Status.INITIALIZING) {
            return;
        }

        List<Location> list = copyLocationList();
        Collections.swap(list, from, to);
        mListResource.setValue(
                new SelectableLocationListResource(
                        list,
                        getValidSelectedId(list),
                        null,
                        true,
                        new SelectableLocationListResource.ItemMoved(from, to)
                )
        );
    }

    public void moveLocationFinish() {
        if (mStatus == Status.INITIALIZING) {
            return;
        }

        mRepository.writeLocationList(getApplication(), getLocationList());
    }

    public void forceUpdateLocation(Location location) {
        if (mStatus == Status.INITIALIZING) {
            return;
        }

        List<Location> list = copyLocationList();
        for (int i = 0; i < list.size(); i ++) {
            if (list.get(i).equals(location)) {
                list.set(i, location);
                break;
            }
        }

        mListResource.setValue(
                new SelectableLocationListResource(
                        list,
                        getValidSelectedId(list),
                        location.getFormattedId(),
                        true
                )
        );

        mRepository.writeLocation(getApplication(), location);
    }

    public void forceUpdateLocation(Location location, int position) {
        if (mStatus == Status.INITIALIZING) {
            return;
        }

        List<Location> list = copyLocationList();
        list.set(position, location);

        mListResource.setValue(
                new SelectableLocationListResource(
                        list,
                        getValidSelectedId(list),
                        list.get(position).getFormattedId(),
                        true
                )
        );

        mRepository.writeLocation(getApplication(), location);
    }

    @Nullable
    public Location deleteLocation(int position) {
        if (mStatus == Status.INITIALIZING) {
            return null;
        }

        List<Location> list = copyLocationList();
        String selectedId = getSelectedIdIgnoreSelectable(list);
        Location location = list.remove(position);

        location.setWeather(DatabaseHelper.getInstance(getApplication()).readWeather(location));

        if (location.getFormattedId().equals(selectedId)) {
            setSelectedId(list.get(0).getFormattedId());
        }

        mListResource.setValue(new SelectableLocationListResource(
                list, getValidSelectedId(list), null, true));

        mRepository.deleteLocation(getApplication(), location);

        return location;
    }

    private void setSelectedId(@Nullable String selectedId) {
        mSelectedId = selectedId;
        mSavedStateHandle.set(KEY_SELECTED_ID, selectedId);
    }

    private List<Location> copyLocationList() {
        if (mListResource.getValue() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(mListResource.getValue().dataList);
    }

    @Nullable
    private String getValidSelectedId(@NonNull List<Location> list) {
        if (!mSelectable) {
            return null;
        } else if (mSelectedId != null) {
            return mSelectedId;
        } else if (list.size() > 0) {
            return list.get(0).getFormattedId();
        } else {
            return null;
        }
    }

    @Nullable
    private String getSelectedIdIgnoreSelectable(@NonNull List<Location> list) {
        if (mSelectedId != null) {
            return mSelectedId;
        } else if (list.size() > 0) {
            return list.get(0).getFormattedId();
        } else {
            return null;
        }
    }

    public MutableLiveData<SelectableLocationListResource> getListResource() {
        return mListResource;
    }

    public List<Location> getLocationList() {
        return Collections.unmodifiableList(copyLocationList());
    }

    public Location getLocation(int position) {
        return copyLocationList().get(position);
    }

    public int getLocationCount() {
        return copyLocationList().size();
    }

    @Nullable
    public String getSelectedId() {
        if (mListResource.getValue() == null) {
            return null;
        } else {
            return mListResource.getValue().selectedId;
        }
    }

    public boolean isInitializeDone() {
        return mStatus == Status.IDLE;
    }
}
