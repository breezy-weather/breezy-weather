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
import wangdaye.com.geometricweather.common.basic.models.weather.Weather;
import wangdaye.com.geometricweather.common.utils.LanguageUtils;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.apis.CNWeatherApi;
import wangdaye.com.geometricweather.weather.converters.CNResultConverter;
import wangdaye.com.geometricweather.weather.json.cn.CNWeatherResult;
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;

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
                        Weather weather = CNResultConverter.convert(context, location, cnWeatherResult);
                        if (weather != null) {
                            location.setWeather(weather);
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
        if (location.hasGeocodeInformation()) {
            searchLocationsInThread(
                    context,
                    location.getProvince(),
                    location.getCity(),
                    location.getDistrict(),
                    callback
            );
        } else {
            searchLocationsInThread(
                    context,
                    location.getLatitude(),
                    location.getLongitude(),
                    callback
            );
        }
    }

    private void searchLocationsInThread(Context context,
                                         String province, String city, String district,
                                         RequestLocationCallback callback) {
        if (callback == null) {
            return;
        }

        final String finalProvince = formatLocationString(convertChinese(province));
        final String finalCity = formatLocationString(convertChinese(city));
        final String finalDistrict = formatLocationString(convertChinese(district));

        Observable.create((ObservableOnSubscribe<List<Location>>) emitter -> {
            DatabaseHelper.getInstance(context).ensureChineseCityList(context);

            List<Location> locationList = new ArrayList<>();
            ChineseCity chineseCity = DatabaseHelper.getInstance(context).readChineseCity(
                    finalProvince, finalCity, finalDistrict);
            if (chineseCity != null) {
                locationList.add(chineseCity.toLocation(getSource()));
            }

            emitter.onNext(locationList);
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<Location>>() {
                    @Override
                    public void onSucceed(List<Location> locations) {
                        if (locations.size() > 0) {
                            callback.requestLocationSuccess(finalDistrict, locations);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(finalDistrict);
                    }
                }));
    }

    private void searchLocationsInThread(Context context, float latitude, float longitude,
                                         RequestLocationCallback callback) {
        if (callback == null) {
            return;
        }

        Observable.create((ObservableOnSubscribe<List<Location>>) emitter -> {
            DatabaseHelper.getInstance(context).ensureChineseCityList(context);

            List<Location> locationList = new ArrayList<>();
            ChineseCity chineseCity = DatabaseHelper.getInstance(context).readChineseCity(latitude, longitude);
            if (chineseCity != null) {
                locationList.add(chineseCity.toLocation(getSource()));
            }

            emitter.onNext(locationList);
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<Location>>() {
                    @Override
                    public void onSucceed(List<Location> locations) {
                        if (locations.size() > 0) {
                            callback.requestLocationSuccess(latitude + "," + longitude, locations);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(latitude + "," + longitude);
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