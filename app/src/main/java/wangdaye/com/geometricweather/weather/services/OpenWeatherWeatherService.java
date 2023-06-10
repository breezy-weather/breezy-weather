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
import wangdaye.com.geometricweather.weather.apis.OpenWeatherApi;
import wangdaye.com.geometricweather.weather.converters.OpenWeatherResultConverterKt;
import wangdaye.com.geometricweather.weather.json.openweather.OpenWeatherAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.openweather.OpenWeatherLocationResult;
import wangdaye.com.geometricweather.weather.json.openweather.OpenWeatherOneCallResult;

/**
 * OpenWeather weather service.
 */
public class OpenWeatherWeatherService extends WeatherService {

    private final OpenWeatherApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public OpenWeatherWeatherService(OpenWeatherApi api, CompositeDisposable disposable) {
        mApi = api;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        Observable<OpenWeatherOneCallResult> oneCall = mApi.getOneCall(
                SettingsManager.getInstance(context).getProviderOpenWeatherOneCallVersion(),
                SettingsManager.getInstance(context).getProviderOpenWeatherKey(),
                location.getLatitude(),
                location.getLongitude(),
                "metric",
                languageCode
        );

        Observable<OpenWeatherAirPollutionResult> airPollution = mApi.getAirPollution(
                SettingsManager.getInstance(context).getProviderOpenWeatherKey(), location.getLatitude(), location.getLongitude()
        ).onErrorResumeNext(error ->
                Observable.create(emitter -> emitter.onNext(new OpenWeatherAirPollutionResult(null)))
        );

        Observable.zip(oneCall, airPollution,
                (openWeatherOneCallResult, openWeatherAirPollutionResult) -> OpenWeatherResultConverterKt.convert(
                        context,
                        location,
                        openWeatherOneCallResult,
                        openWeatherAirPollutionResult
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
        List<OpenWeatherLocationResult> resultList = null;
        try {
            resultList = mApi.callWeatherLocation(SettingsManager.getInstance(context).getProviderOpenWeatherKey(), query).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return OpenWeatherResultConverterKt.convert(resultList);
    }

    @Override
    public void requestLocation(Context context, Location location,
                                @NonNull RequestLocationCallback callback) {

        mApi.getWeatherLocationByGeoPosition(
                SettingsManager.getInstance(context).getProviderOpenWeatherKey(), location.getLatitude(), location.getLongitude()
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<OpenWeatherLocationResult>>() {
                    @Override
                    public void onSucceed(List<OpenWeatherLocationResult> openWeatherLocationResultList) {
                        if (openWeatherLocationResultList != null && !openWeatherLocationResultList.isEmpty()) {
                            List<Location> locationList = new ArrayList<>();
                            locationList.add(OpenWeatherResultConverterKt.convert(location, openWeatherLocationResultList.get(0)));
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
        mApi.getWeatherLocation(SettingsManager.getInstance(context).getProviderOpenWeatherKey(), query)
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<OpenWeatherLocationResult>>() {
                    @Override
                    public void onSucceed(List<OpenWeatherLocationResult> openWeatherLocationResults) {
                        if (openWeatherLocationResults != null && openWeatherLocationResults.size() != 0) {
                            List<Location> locationList = OpenWeatherResultConverterKt.convert(openWeatherLocationResults);
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