package wangdaye.com.geometricweather.manage.search;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.manage.model.LoadableLocationList;

public class SearchActivityViewModel extends ViewModel {

    private final MutableLiveData<LoadableLocationList> mListResource;
    private final MutableLiveData<String> mQuery;
    private final MutableLiveData<Boolean> mMultiSourceEnabled;
    private final SearchActivityRepository mRepository;

    public SearchActivityViewModel() {
        mRepository = new SearchActivityRepository(GeometricWeather.getInstance());

        mListResource = new MutableLiveData<>();
        mListResource.setValue(
                new LoadableLocationList(new ArrayList<>(), LoadableLocationList.Status.SUCCESS));

        mQuery = new MutableLiveData<>();
        mQuery.setValue("");

        mMultiSourceEnabled = new MutableLiveData<>();
        mMultiSourceEnabled.setValue(mRepository.isMultiSourceEnabled());
    }

    public void requestLocationList(Context context, String query) {
        List<Location> oldList = innerGetLocationList();

        mRepository.cancel();
        mRepository.searchLocationList(context, query, isMultiSourceEnabled(), locationList -> {
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

    public void requestLocationList(Context context) {
        requestLocationList(context, getQueryValue());
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
