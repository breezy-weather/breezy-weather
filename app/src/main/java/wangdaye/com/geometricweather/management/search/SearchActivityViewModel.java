package wangdaye.com.geometricweather.management.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.basic.GeoViewModel;
import wangdaye.com.geometricweather.basic.models.Location;
import wangdaye.com.geometricweather.management.models.LoadableLocationList;

public class SearchActivityViewModel extends GeoViewModel {

    private final MutableLiveData<LoadableLocationList> mListResource;
    private final MutableLiveData<String> mQuery;
    private final MutableLiveData<Boolean> mMultiSourceEnabled;
    private final SearchActivityRepository mRepository;

    public SearchActivityViewModel(Application application) {
        this(application, new SearchActivityRepository(application));
    }

    public SearchActivityViewModel(Application application, SearchActivityRepository repository) {
        super(application);

        mListResource = new MutableLiveData<>();
        mListResource.setValue(
                new LoadableLocationList(new ArrayList<>(), LoadableLocationList.Status.SUCCESS));

        mQuery = new MutableLiveData<>();
        mQuery.setValue("");

        mMultiSourceEnabled = new MutableLiveData<>();
        mMultiSourceEnabled.setValue(repository.isMultiSourceEnabled());

        mRepository = repository;
    }

    public void requestLocationList(String query) {
        List<Location> oldList = innerGetLocationList();

        mRepository.cancel();
        mRepository.searchLocationList(getApplication(), query, isMultiSourceEnabled(), (locationList, done) -> {
            if (locationList != null) {
                mListResource.setValue(
                        new LoadableLocationList(locationList, LoadableLocationList.Status.SUCCESS));
            } else {
                mListResource.setValue(
                        new LoadableLocationList(oldList, LoadableLocationList.Status.ERROR));
            }
        });

        mListResource.setValue(
                new LoadableLocationList(oldList, LoadableLocationList.Status.LOADING));
        mQuery.setValue(query);
    }

    public void requestLocationList() {
        requestLocationList(getQueryValue());
    }

    public void switchMultiSourceEnabled() {
        boolean enabled = !isMultiSourceEnabled();

        mRepository.setMultiSourceEnabled(enabled);
        mMultiSourceEnabled.setValue(enabled);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mRepository.cancel();
    }

    private List<Location> innerGetLocationList() {
        if (mListResource.getValue() == null) {
            return new ArrayList<>();
        }
        return mListResource.getValue().dataList;
    }

    public MutableLiveData<LoadableLocationList> getListResource() {
        return mListResource;
    }

    public List<Location> getLocationList() {
        return Collections.unmodifiableList(innerGetLocationList());
    }

    public int getLocationCount() {
        return innerGetLocationList().size();
    }

    public MutableLiveData<String> getQuery() {
        return mQuery;
    }

    @NonNull
    public String getQueryValue() {
        if (mQuery.getValue() == null) {
            return "";
        }
        return mQuery.getValue();
    }

    public MutableLiveData<Boolean> getMultiSourceEnabled() {
        return mMultiSourceEnabled;
    }

    public boolean isMultiSourceEnabled() {
        if (mMultiSourceEnabled.getValue() == null) {
            return false;
        } else {
            return mMultiSourceEnabled.getValue();
        }
    }
}
