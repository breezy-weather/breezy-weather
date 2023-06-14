package org.breezyweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.breezyweather.common.basic.models.ChineseCity;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.db.repositories.ChineseCityEntityRepository;
import org.breezyweather.weather.apis.ChinaApi;
import org.breezyweather.common.rxjava.BaseObserver;
import org.breezyweather.common.rxjava.ObserverContainer;
import org.breezyweather.common.rxjava.SchedulerTransformer;
import org.breezyweather.common.utils.LanguageUtils;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.weather.converters.ChinaResultConverterKt;
import org.breezyweather.weather.json.china.ChinaMinutelyResult;
import org.breezyweather.weather.json.china.ChinaForecastResult;

public class ChinaWeatherService extends WeatherService {

    private final ChinaApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public ChinaWeatherService(ChinaApi cyApi, CompositeDisposable disposable) {
        mApi = cyApi;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context,
                               Location location, @NonNull RequestWeatherCallback callback) {
        Observable<ChinaForecastResult> mainly = mApi.getForecastWeather(
                location.getLatitude(),
                location.getLongitude(),
                location.isCurrentPosition(),
                "weathercn%3A" + location.getCityId(),
                15,
                "weather20151024",
                "zUFJoAR2ZVrDy1vF3D07",
                false,
                SettingsManager.getInstance(context).getLanguage().getCode()
        );
        Observable<ChinaMinutelyResult> forecast = mApi.getMinutelyWeather(
                location.getLatitude(),
                location.getLongitude(),
                SettingsManager.getInstance(context).getLanguage().getCode(),
                false,
                "weather20151024",
                "weathercn%3A" + location.getCityId(),
                "zUFJoAR2ZVrDy1vF3D07"
        );

        Observable.zip(mainly, forecast, (mainlyResult, forecastResult) ->
                ChinaResultConverterKt.convert(context, location, mainlyResult, forecastResult)
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<WeatherResultWrapper>() {
                    @Override
                    public void onSucceed(WeatherResultWrapper wrapper) {
                        if (wrapper.result != null) {
                            callback.requestWeatherSuccess(
                                    Location.copy(location, wrapper.result)
                            );
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestWeatherFailed(location, this.isApiLimitReached(), this.isApiUnauthorized());
                    }
                }));
    }

    @NonNull
    @Override
    public List<Location> requestLocation(Context context, String query) {
        if (!LanguageUtils.isChinese(query)) {
            return new ArrayList<>();
        }

        ChineseCityEntityRepository.INSTANCE.ensureChineseCityList(context);

        List<Location> locationList = new ArrayList<>();
        List<ChineseCity> cityList = ChineseCityEntityRepository.INSTANCE.readChineseCityList(query);
        for (ChineseCity c : cityList) {
            locationList.add(c.toLocation());
        }

        return locationList;
    }

    @Override
    public void requestLocation(Context context, Location location, @NonNull RequestLocationCallback callback) {

        final boolean hasGeocodeInformation = location.hasGeocodeInformation();

        Observable.create((ObservableOnSubscribe<List<Location>>) emitter -> {
            ChineseCityEntityRepository.INSTANCE.ensureChineseCityList(context);
            List<Location> locationList = new ArrayList<>();

            if (hasGeocodeInformation) {
                ChineseCity chineseCity = ChineseCityEntityRepository.INSTANCE.readChineseCity(
                        formatLocationString(convertChinese(location.getProvince())),
                        formatLocationString(convertChinese(location.getCity())),
                        formatLocationString(convertChinese(location.getDistrict()))
                );
                if (chineseCity != null) {
                    locationList.add(chineseCity.toLocation());
                }
            }
            if (locationList.size() > 0) {
                emitter.onNext(locationList);
                return;
            }

            ChineseCity chineseCity = ChineseCityEntityRepository.INSTANCE.readChineseCity(
                    location.getLatitude(), location.getLongitude());
            if (chineseCity != null) {
                locationList.add(chineseCity.toLocation());
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
}