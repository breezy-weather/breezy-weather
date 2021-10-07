package wangdaye.com.geometricweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.common.basic.models.ChineseCity;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;
import wangdaye.com.geometricweather.common.utils.LanguageUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.weather.apis.CNWeatherApi;
import wangdaye.com.geometricweather.weather.converters.CNResultConverter;
import wangdaye.com.geometricweather.weather.json.cn.CNWeatherResult;

/**
 * CN weather service.
 * */

public class CNWeatherService extends WeatherService {

    private final CNWeatherApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public CNWeatherService(CNWeatherApi api, CompositeDisposable disposable) {
        mApi = api;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        mApi.getWeather(location.getCityId())
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<CNWeatherResult>() {
                    @Override
                    public void onSucceed(CNWeatherResult cnWeatherResult) {
                        WeatherResultWrapper wrapper = CNResultConverter.convert(context, location, cnWeatherResult);
                        if (wrapper.result != null) {
                            location.setWeather(wrapper.result);
                            callback.requestWeatherSuccess(location);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestWeatherFailed(location);
                    }
                }));
    }

    @NonNull
    @Override
    public List<Location> requestLocation(Context context, String query) {
        if (!LanguageUtils.isChinese(query)) {
            return new ArrayList<>();
        }

        DatabaseHelper.getInstance(context).ensureChineseCityList(context);

        List<Location> locationList = new ArrayList<>();
        List<ChineseCity> cityList = DatabaseHelper.getInstance(context).readChineseCityList(query);
        for (ChineseCity c : cityList) {
            locationList.add(c.toLocation(getSource()));
        }

        return locationList;
    }

    @Override
    public void requestLocation(Context context, Location location, @NonNull RequestLocationCallback callback) {

        final boolean hasGeocodeInformation = location.hasGeocodeInformation();

        Observable.create((ObservableOnSubscribe<List<Location>>) emitter -> {
            DatabaseHelper.getInstance(context).ensureChineseCityList(context);
            List<Location> locationList = new ArrayList<>();

            if (hasGeocodeInformation) {
                ChineseCity chineseCity = DatabaseHelper.getInstance(context).readChineseCity(
                        formatLocationString(convertChinese(location.getProvince())),
                        formatLocationString(convertChinese(location.getCity())),
                        formatLocationString(convertChinese(location.getDistrict()))
                );
                if (chineseCity != null) {
                    locationList.add(chineseCity.toLocation(getSource()));
                }
            }
            if (locationList.size() > 0) {
                emitter.onNext(locationList);
                return;
            }

            ChineseCity chineseCity = DatabaseHelper.getInstance(context).readChineseCity(
                    location.getLatitude(), location.getLongitude());
            if (chineseCity != null) {
                locationList.add(chineseCity.toLocation(getSource()));
            }

            emitter.onNext(locationList);

        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<Location>>() {
                    @Override
                    public void onSucceed(List<Location> locations) {
                        if (locations.size() > 0) {
                            callback.requestLocationSuccess(location.getFormattedId(), locations);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(location.getFormattedId());
                    }
                }));
    }

    @Override
    public void cancel() {
        mCompositeDisposable.clear();
    }

    public ChineseCity.CNWeatherSource getSource() {
        return ChineseCity.CNWeatherSource.CN;
    }
}