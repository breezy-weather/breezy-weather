package wangdaye.com.geometricweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.weather.apis.OwmApi;
import wangdaye.com.geometricweather.weather.converters.OwmResultConverter;
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult;

/**
 * Owm weather service.
 */

public class OwmWeatherService extends WeatherService {

    private final OwmApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    private static class EmptyAqiResult extends OwmAirPollutionResult {
    }

    @Inject
    public OwmWeatherService(OwmApi api, CompositeDisposable disposable) {
        mApi = api;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        Observable<OwmOneCallResult> oneCall = mApi.getOneCall(
                SettingsManager.getInstance(context).getProviderOwmKey(), location.getLatitude(), location.getLongitude(), "metric", languageCode);

        Observable<OwmAirPollutionResult> airPollutionCurrent = mApi.getAirPollutionCurrent(
                SettingsManager.getInstance(context).getProviderOwmKey(), location.getLatitude(), location.getLongitude()
        ).onExceptionResumeNext(
                Observable.create(emitter -> emitter.onNext(new EmptyAqiResult()))
        );

        Observable<OwmAirPollutionResult> airPollutionForecast = mApi.getAirPollutionForecast(
                SettingsManager.getInstance(context).getProviderOwmKey(), location.getLatitude(), location.getLongitude()
        ).onExceptionResumeNext(
                Observable.create(emitter -> emitter.onNext(new EmptyAqiResult()))
        );

        Observable.zip(oneCall, airPollutionCurrent, airPollutionForecast,
                (owmOneCallResult, owmAirPollutionCurrentResult, owmAirPollutionForecastResult) -> OwmResultConverter.convert(
                        context,
                        location,
                        owmOneCallResult,
                        owmAirPollutionCurrentResult instanceof EmptyAqiResult ? null : owmAirPollutionCurrentResult,
                        owmAirPollutionForecastResult instanceof EmptyAqiResult ? null : owmAirPollutionForecastResult
                )
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
                        callback.requestWeatherFailed(location, this.isApiLimitReached());
                    }
                }));
    }

    @Override
    @NonNull
    public List<Location> requestLocation(Context context, String query) {
        List<OwmLocationResult> resultList = null;
        try {
            resultList = mApi.callWeatherLocation(SettingsManager.getInstance(context).getProviderOwmKey(), query).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String zipCode = query.matches("[a-zA-Z0-9]*") ? query : null;

        List<Location> locationList = new ArrayList<>();
        if (resultList != null && resultList.size() != 0) {
            for (OwmLocationResult r : resultList) {
                locationList.add(OwmResultConverter.convert(null, r, zipCode));
            }
        }
        return locationList;
    }

    @Override
    public void requestLocation(Context context, Location location,
                                @NonNull RequestLocationCallback callback) {

        mApi.getWeatherLocationByGeoPosition(
                SettingsManager.getInstance(context).getProviderOwmKey(), location.getLatitude(), location.getLongitude()
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<OwmLocationResult>>() {
                    @Override
                    public void onSucceed(List<OwmLocationResult> owmLocationResultList) {
                        if (owmLocationResultList != null && !owmLocationResultList.isEmpty()) {
                            List<Location> locationList = new ArrayList<>();
                            locationList.add(OwmResultConverter.convert(location, owmLocationResultList.get(0), null));
                            callback.requestLocationSuccess(
                                    location.getLatitude() + "," + location.getLongitude(), locationList);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(
                                location.getLatitude() + "," + location.getLongitude());
                    }
                }));
    }

    public void requestLocation(Context context, String query,
                                @NonNull RequestLocationCallback callback) {
        String zipCode = query.matches("[a-zA-Z0-9]") ? query : null;

        mApi.getWeatherLocation(SettingsManager.getInstance(context).getProviderOwmKey(), query)
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<OwmLocationResult>>() {
                    @Override
                    public void onSucceed(List<OwmLocationResult> owmLocationResults) {
                        if (owmLocationResults != null && owmLocationResults.size() != 0) {
                            List<Location> locationList = new ArrayList<>();
                            for (OwmLocationResult r : owmLocationResults) {
                                locationList.add(OwmResultConverter.convert(null, r, zipCode));
                            }
                            callback.requestLocationSuccess(query, locationList);
                        } else {
                            callback.requestLocationFailed(query);
                        }
                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(query);
                    }
                }));
    }

    @Override
    public void cancel() {
        mCompositeDisposable.clear();
    }
}