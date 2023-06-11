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
import wangdaye.com.geometricweather.weather.apis.AccuWeatherApi;
import wangdaye.com.geometricweather.weather.converters.AccuResultConverterKt;
import wangdaye.com.geometricweather.weather.json.accu.AccuAlertResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuCurrentResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuForecastDailyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuForecastHourlyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuLocationResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuMinutelyResult;

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

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        Observable<List<AccuCurrentResult>> realtime = mApi.getCurrent(
                location.getCityId(), SettingsManager.getInstance(context).getProviderAccuWeatherKey(), languageCode, true);

        Observable<AccuForecastDailyResult> daily = mApi.getDaily(
                location.getCityId(), SettingsManager.getInstance(context).getProviderAccuWeatherKey(), languageCode, true, true);

        Observable<List<AccuForecastHourlyResult>> hourly = mApi.getHourly(
                location.getCityId(), SettingsManager.getInstance(context).getProviderAccuWeatherKey(), languageCode, true, true);

        Observable<AccuMinutelyResult> minute = mApi.getMinutely(
                SettingsManager.getInstance(context).getProviderAccuWeatherKey(),
                location.getLatitude() + "," + location.getLongitude(),
                languageCode,
                true
        ).onErrorResumeNext(error ->
                Observable.create(emitter -> emitter.onNext(new AccuMinutelyResult(null, null)))
        );

        Observable<List<AccuAlertResult>> alert = mApi.getAlert(
                SettingsManager.getInstance(context).getProviderAccuWeatherKey(),
                location.getLatitude() + "," + location.getLongitude(),
                languageCode,
                true
        );

        Observable.zip(realtime, daily, hourly, minute, alert,
                (accuRealtimeResults,
                 accuDailyResult, accuHourlyResults, accuMinutelyResult,
                 accuAlertResults) -> AccuResultConverterKt.convert(
                         context,
                         location,
                         accuRealtimeResults.get(0),
                         accuDailyResult,
                         accuHourlyResults,
                         accuMinutelyResult,
                         accuAlertResults
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
        List<AccuLocationResult> resultList = null;
        try {
            resultList = mApi.callWeatherLocation(
                    SettingsManager.getInstance(context).getProviderAccuWeatherKey(),
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

        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        mApi.getWeatherLocationByGeoPosition(
                        SettingsManager.getInstance(context).getProviderAccuWeatherKey(),
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
                        callback.requestLocationFailed(
                                location.getLatitude() + "," + location.getLongitude()
                        );
                    }
                }));
    }

    public void requestLocation(Context context, String query,
                                @NonNull RequestLocationCallback callback) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();
        String zipCode = query.matches("[a-zA-Z0-9]") ? query : null;

        mApi.getWeatherLocation(SettingsManager.getInstance(context).getProviderAccuWeatherKey(), query, languageCode, false, "Always")
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