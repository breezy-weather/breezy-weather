package org.breezyweather.weather.accu;

import android.content.Context;

import android.text.TextUtils;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.main.utils.RequestErrorType;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.weather.WeatherService;
import org.breezyweather.weather.accu.AccuResultConverterKt;
import org.breezyweather.weather.accu.AccuWeatherApi;
import org.breezyweather.common.rxjava.BaseObserver;
import org.breezyweather.common.rxjava.ObserverContainer;
import org.breezyweather.common.rxjava.SchedulerTransformer;
import org.breezyweather.weather.accu.json.*;

/**
 * Accu weather service.
 * */

public class AccuWeatherService extends WeatherService {

    private final AccuWeatherApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public AccuWeatherService(AccuWeatherApi api, CompositeDisposable disposable) {
        mApi = api;
        mCompositeDisposable = disposable;
    }

    protected String getApiKey(Context context) {
        return SettingsManager.getInstance(context).getProviderAccuWeatherKey();
    }

    @Override
    public Boolean isConfigured(Context context) {
        return !TextUtils.isEmpty(getApiKey(context));
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        if (!this.isConfigured(context)) {
            callback.requestWeatherFailed(location, RequestErrorType.API_KEY_REQUIRED_MISSING);
            return;
        }

        String apiKey = getApiKey(context);

        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        Observable<List<AccuCurrentResult>> realtime = mApi.getCurrent(
                location.getCityId(), apiKey, languageCode, true);

        Observable<AccuForecastDailyResult> daily = mApi.getDaily(
                location.getCityId(), apiKey, languageCode, true, true);

        Observable<List<AccuForecastHourlyResult>> hourly = mApi.getHourly(
                location.getCityId(), apiKey, languageCode, true, true);

        Observable<AccuMinutelyResult> minute = mApi.getMinutely(
                apiKey,
                location.getLatitude() + "," + location.getLongitude(),
                languageCode,
                true
        ).onErrorResumeNext(error ->
                Observable.create(emitter -> emitter.onNext(new AccuMinutelyResult(null, null)))
        );

        Observable<List<AccuAlertResult>> alert = mApi.getAlert(
                apiKey,
                location.getLatitude() + "," + location.getLongitude(),
                languageCode,
                true
        );

        Observable<AccuAirQualityResult> airQuality = mApi.getAirQuality(
                location.getCityId(), apiKey, true, languageCode
        ).onErrorResumeNext(error ->
                Observable.create(emitter -> emitter.onNext(new AccuAirQualityResult(null)))
        );

        Observable.zip(realtime, daily, hourly, minute, alert, airQuality,
                (accuRealtimeResults,
                 accuDailyResult, accuHourlyResults, accuMinutelyResult,
                 accuAlertResults, accuAirQualityResult) -> AccuResultConverterKt.convert(
                         context,
                         location,
                         accuRealtimeResults.get(0),
                         accuDailyResult,
                         accuHourlyResults,
                         accuMinutelyResult,
                         accuAlertResults,
                         accuAirQualityResult
                 )
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<WeatherResultWrapper>() {
                    @Override
                    public void onSucceed(WeatherResultWrapper wrapper) {
                        if (wrapper.getResult() != null) {
                            callback.requestWeatherSuccess(
                                    Location.copy(location, wrapper.getResult())
                            );
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        if (this.isApiLimitReached()) {
                            callback.requestWeatherFailed(location, RequestErrorType.API_LIMIT_REACHED);
                        } else if (this.isApiUnauthorized()) {
                            callback.requestWeatherFailed(location, RequestErrorType.API_UNAUTHORIZED);
                        } else {
                            callback.requestWeatherFailed(location, RequestErrorType.WEATHER_REQ_FAILED);
                        }
                    }
                }));
    }

    @Override
    @NonNull
    public List<Location> requestLocation(Context context, String query) {
        if (!this.isConfigured(context)) {
            return new ArrayList<>();
        }

        String apiKey = getApiKey(context);
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();
        List<AccuLocationResult> resultList = null;
        try {
            resultList = mApi.callWeatherLocation(
                    apiKey,
                    query,
                    languageCode,
                    false,
                    "Always"
            ).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String zipCode = query.matches("[a-zA-Z0-9]*") ? query : null;

        List<Location> locationList = new ArrayList<>();
        if (resultList != null && resultList.size() != 0) {
            for (AccuLocationResult r : resultList) {
                locationList.add(AccuResultConverterKt.convert(null, r, zipCode));
            }
        }
        return locationList;
    }

    @Override
    public void requestLocation(Context context, Location location,
                                @NonNull RequestLocationCallback callback) {
        if (!this.isConfigured(context)) {
            callback.requestLocationFailed(
                    location.getLatitude() + "," + location.getLongitude(), RequestErrorType.API_KEY_REQUIRED_MISSING
            );
            return;
        }

        String apiKey = getApiKey(context);
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        mApi.getWeatherLocationByGeoPosition(
                        apiKey,
                        languageCode,
                        false,
                        location.getLatitude() + "," + location.getLongitude()
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<AccuLocationResult>() {
                    @Override
                    public void onSucceed(AccuLocationResult accuLocationResult) {
                        if (accuLocationResult != null) {
                            List<Location> locationList = new ArrayList<>();
                            locationList.add(AccuResultConverterKt.convert(location, accuLocationResult, null));
                            callback.requestLocationSuccess(
                                    location.getLatitude() + "," + location.getLongitude(),
                                    locationList
                            );
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        if (this.isApiLimitReached()) {
                            callback.requestLocationFailed(location.getLatitude() + "," + location.getLongitude(), RequestErrorType.API_LIMIT_REACHED);
                        } else if (this.isApiUnauthorized()) {
                            callback.requestLocationFailed(location.getLatitude() + "," + location.getLongitude(), RequestErrorType.API_UNAUTHORIZED);
                        } else {
                            callback.requestLocationFailed(location.getLatitude() + "," + location.getLongitude(), RequestErrorType.LOCATION_FAILED);
                        }
                    }
                }));
    }

    public void requestLocation(Context context, String query,
                                @NonNull RequestLocationCallback callback) {
        if (!this.isConfigured(context)) {
            callback.requestLocationFailed(query, RequestErrorType.API_KEY_REQUIRED_MISSING);
            return;
        }
        String apiKey = getApiKey(context);
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();
        String zipCode = query.matches("[a-zA-Z0-9]") ? query : null;

        mApi.getWeatherLocation(apiKey, query, languageCode, false, "Always")
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<AccuLocationResult>>() {
                    @Override
                    public void onSucceed(List<AccuLocationResult> accuLocationResults) {
                        if (accuLocationResults != null && accuLocationResults.size() != 0) {
                            List<Location> locationList = new ArrayList<>();
                            for (AccuLocationResult r : accuLocationResults) {
                                locationList.add(AccuResultConverterKt.convert(null, r, zipCode));
                            }
                            callback.requestLocationSuccess(query, locationList);
                        } else {
                            callback.requestLocationFailed(query, RequestErrorType.LOCATION_FAILED);
                        }
                    }

                    @Override
                    public void onFailed() {
                        if (this.isApiLimitReached()) {
                            callback.requestLocationFailed(query, RequestErrorType.API_LIMIT_REACHED);
                        } else if (this.isApiUnauthorized()) {
                            callback.requestLocationFailed(query, RequestErrorType.API_UNAUTHORIZED);
                        } else {
                            callback.requestLocationFailed(query, RequestErrorType.LOCATION_FAILED);
                        }
                    }
                }));
    }

    @Override
    public void cancel() {
        mCompositeDisposable.clear();
    }
}