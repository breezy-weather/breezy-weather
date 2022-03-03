package wangdaye.com.geometricweather.search;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import wangdaye.com.geometricweather.common.basic.GeoViewModel;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;

@HiltViewModel
public class SearchActivityViewModel extends GeoViewModel {

    private final MutableLiveData<LoadableLocationList> mListResource;
    private final MutableLiveData<String> mQuery;
    private final MutableLiveData<List<WeatherSource>> mEnabledSources;
    private final SearchActivityRepository mRepository;

    @Inject
    public SearchActivityViewModel(
            Application application,
            SearchActivityRepository repository
    ) {
        super(application);

        mListResource = new MutableLiveData<>();
        mListResource.setValue(
                new LoadableLocationList(new ArrayList<>(), LoadableLocationList.Status.SUCCESS));

        mQuery = new MutableLiveData<>();
        mQuery.setValue("");

        mEnabledSources = new MutableLiveData<>();
        mEnabledSources.setValue(repository.getValidWeatherSources(getApplication()));

        mRepository = repository;
    }

    public void requestLocationList(String query) {
        List<Location> oldList = innerGetLocationList();

        mRepository.cancel();
        mRepository.searchLocationList(getApplication(), query, getEnabledSourcesValue(), (locationList, done) -> {
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

    public void setEnabledSources(List<WeatherSource> enabledSources) {
        mRepository.setValidWeatherSources(enabledSources);
        mEnabledSources.setValue(enabledSources);
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

    public MutableLiveData<List<WeatherSource>> getEnabledSources() {
        return mEnabledSources;
    }

    public List<WeatherSource> getEnabledSourcesValue() {
        if (mEnabledSources.getValue() == null) {
            return new ArrayList<>();
        } else {
            return mEnabledSources.getValue();
        }
    }
}
