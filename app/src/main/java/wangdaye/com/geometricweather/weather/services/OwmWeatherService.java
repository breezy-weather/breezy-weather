package wangdaye.com.geometricweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.weather.apis.OwmApi;
import wangdaye.com.geometricweather.weather.converters.OwmResultConverterKt;
import wangdaye.com.geometricweather.weather.json.owm.OwmAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmLocationResult;
import wangdaye.com.geometricweather.weather.json.owm.OwmOneCallResult;

/**
 * Owm weather service.
 */

public class OwmWeatherService extends WeatherService {

    private final OwmApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public OwmWeatherService(OwmApi api, CompositeDisposable disposable) {
        mApi = api;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        Observable<OwmOneCallResult> oneCall = mApi.getOneCall(
                SettingsManager.getInstance(context).getProviderOwmOneCallVersion(),
                SettingsManager.getInstance(context).getProviderOwmKey(),
                location.getLatitude(),
                location.getLongitude(),
                "metric",
                languageCode
        );

        Observable<OwmAirPollutionResult> airPollution = mApi.getAirPollution(
                SettingsManager.getInstance(context).getProviderOwmKey(), location.getLatitude(), location.getLongitude()
        ).onErrorResumeNext(error ->
                Observable.create(emitter -> emitter.onNext(new OwmAirPollutionResult(null)))
        );

        Observable.zip(oneCall, airPollution,
                (owmOneCallResult, owmAirPollutionResult) -> OwmResultConverterKt.convert(
                        context,
                        location,
                        owmOneCallResult,
                        owmAirPollutionResult
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
                        callback.requestWeatherFailed(location, this.isApiLimitReached(), this.isApiUnauthorized());
                    }
                }));
    }

    @Override
    @NonNull
    public List<Location> requestLocation(Context context, String query) {
        List<OwmLocationResult> resultList = null;
        try {
            resultList = mApi.callWeatherLocation(SettingsManager.getInstance(context).getProviderOwmKey(), query).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return OwmResultConverterKt.convert(resultList);
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
                            locationList.add(OwmResultConverterKt.convert(location, owmLocationResultList.get(0)));
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
        mApi.getWeatherLocation(SettingsManager.getInstance(context).getProviderOwmKey(), query)
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<OwmLocationResult>>() {
                    @Override
                    public void onSucceed(List<OwmLocationResult> owmLocationResults) {
                        if (owmLocationResults != null && owmLocationResults.size() != 0) {
                            List<Location> locationList = OwmResultConverterKt.convert(owmLocationResults);
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