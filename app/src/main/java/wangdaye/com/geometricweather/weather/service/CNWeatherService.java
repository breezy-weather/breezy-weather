package wangdaye.com.geometricweather.weather.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.utils.LanguageUtils;
import wangdaye.com.geometricweather.weather.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.api.CNWeatherApi;
import wangdaye.com.geometricweather.basic.model.location.ChineseCity;
import wangdaye.com.geometricweather.basic.model.location.Location;
import wangdaye.com.geometricweather.weather.converter.CNResultConverter;
import wangdaye.com.geometricweather.weather.json.cn.CNWeatherResult;
import wangdaye.com.geometricweather.utils.FileUtils;
import wangdaye.com.geometricweather.weather.interceptor.GzipInterceptor;
import wangdaye.com.geometricweather.db.DatabaseHelper;
import wangdaye.com.geometricweather.weather.observer.BaseObserver;
import wangdaye.com.geometricweather.weather.observer.ObserverContainer;

/**
 * CN weather service.
 * */

public class CNWeatherService extends WeatherService {

    private CNWeatherApi api;
    private CompositeDisposable compositeDisposable;

    public CNWeatherService() {
        api = new Retrofit.Builder()
                .baseUrl(BuildConfig.CN_WEATHER_BASE_URL)
                .client(
                        GeometricWeather.getInstance()
                                .getOkHttpClient()
                                .newBuilder()
                                .addInterceptor(new GzipInterceptor())
                                .build()
                ).addConverterFactory(GeometricWeather.getInstance().getGsonConverterFactory())
                .addCallAdapterFactory(GeometricWeather.getInstance().getRxJava2CallAdapterFactory())
                .build()
                .create((CNWeatherApi.class));
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        api.getWeather(location.getCityId())
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<CNWeatherResult>() {
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

        ensureChineseCityDatabase(context);

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
            ensureChineseCityDatabase(context);

            List<Location> locationList = new ArrayList<>();
            ChineseCity chineseCity = DatabaseHelper.getInstance(context).readChineseCity(
                    finalProvince, finalCity, finalDistrict);
            if (chineseCity != null) {
                locationList.add(chineseCity.toLocation(getSource()));
            }

            emitter.onNext(locationList);
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<List<Location>>() {
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
            ensureChineseCityDatabase(context);

            List<Location> locationList = new ArrayList<>();
            ChineseCity chineseCity = DatabaseHelper.getInstance(context).readChineseCity(latitude, longitude);
            if (chineseCity != null) {
                locationList.add(chineseCity.toLocation(getSource()));
            }

            emitter.onNext(locationList);
        }).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(compositeDisposable, new BaseObserver<List<Location>>() {
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

    @WorkerThread
    private static void ensureChineseCityDatabase(Context context) {
        if (DatabaseHelper.getInstance(context).countChineseCity() < 3216) {
            DatabaseHelper.getInstance(context).writeChineseCityList(FileUtils.readCityList(context));
        }
    }

    @Override
    public void cancel() {
        compositeDisposable.clear();
    }

    public ChineseCity.CNWeatherSource getSource() {
        return ChineseCity.CNWeatherSource.CN;
    }
}