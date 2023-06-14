package wangdaye.com.geometricweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;
import wangdaye.com.geometricweather.settings.SettingsManager;
import wangdaye.com.geometricweather.weather.apis.OpenMeteoGeocodingApi;
import wangdaye.com.geometricweather.weather.apis.OpenWeatherApi;
import wangdaye.com.geometricweather.weather.converters.OpenMeteoResultConverterKt;
import wangdaye.com.geometricweather.weather.converters.OpenWeatherResultConverterKt;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResults;
import wangdaye.com.geometricweather.weather.json.openweather.OpenWeatherAirPollutionResult;
import wangdaye.com.geometricweather.weather.json.openweather.OpenWeatherOneCallResult;

/**
 * OpenWeather weather service.
 */
public class OpenWeatherWeatherService extends WeatherService {

    private final OpenWeatherApi mApi;
    private final OpenMeteoGeocodingApi mGeocodingApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public OpenWeatherWeatherService(OpenWeatherApi api, OpenMeteoGeocodingApi geocodingApi, CompositeDisposable disposable) {
        mApi = api;
        mGeocodingApi = geocodingApi;
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
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();
        OpenMeteoLocationResults results = null;
        try {
            results = mGeocodingApi.callWeatherLocation(
                    query, 20, languageCode).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Location> locationList = new ArrayList<>();
        if (results != null && results.getResults() != null && results.getResults().size() != 0) {
            for (OpenMeteoLocationResult r : results.getResults()) {
                locationList.add(OpenMeteoResultConverterKt.convert(null, r, WeatherSource.OPEN_WEATHER));
            }
        }
        return locationList;
    }

    // Reverse geocoding
    @Override
    public void requestLocation(Context context, Location location,
                                @NonNull RequestLocationCallback callback) {
        // Currently there is no reverse geocoding, so we just return the same location
        // TimeZone is initialized with the TimeZone from the phone (which is probably the same as the current position)
        // Hopefully, one day we will have a reverse geocoding API
        List<Location> locationList = new ArrayList<>();
        locationList.add(location);
        callback.requestLocationSuccess(
                location.getLatitude() + "," + location.getLongitude(),
                locationList
        );
    }

    public void requestLocation(Context context, String query,
                                @NonNull RequestLocationCallback callback) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();
        mGeocodingApi.getWeatherLocation(query, 20, languageCode)
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<OpenMeteoLocationResults>() {
                    @Override
                    public void onSucceed(OpenMeteoLocationResults openMeteoLocationResults) {
                        if (openMeteoLocationResults.getResults() != null && openMeteoLocationResults.getResults().size() != 0) {
                            List<Location> locationList = new ArrayList<>();
                            for (OpenMeteoLocationResult r : openMeteoLocationResults.getResults()) {
                                locationList.add(OpenMeteoResultConverterKt.convert(null, r, WeatherSource.OPEN_WEATHER));
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