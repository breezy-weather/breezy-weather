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
import wangdaye.com.geometricweather.weather.apis.AccuWeatherApi;
import wangdaye.com.geometricweather.weather.converters.AccuResultConverter;
import wangdaye.com.geometricweather.weather.json.accu.AccuAlertResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuAqiResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuCurrentResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuDailyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuHourlyResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuLocationResult;
import wangdaye.com.geometricweather.weather.json.accu.AccuMinuteResult;

/**
 * Accu weather service.
 * */

public class AccuWeatherService extends WeatherService {

    private final AccuWeatherApi mApi;
    private final CompositeDisposable mCompositeDisposable;

    private static class EmptyMinuteResult extends AccuMinuteResult {
    }

    private static class EmptyAqiResult extends AccuAqiResult {
    }

    @Inject
    public AccuWeatherService(AccuWeatherApi api, CompositeDisposable disposable) {
        mApi = api;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        Observable<List<AccuCurrentResult>> realtime = mApi.getCurrent(
                location.getCityId(), SettingsManager.getInstance(context).getProviderAccuCurrentKey(true), languageCode, true);

        Observable<AccuDailyResult> daily = mApi.getDaily(
                location.getCityId(), SettingsManager.getInstance(context).getProviderAccuWeatherKey(true), languageCode, true, true);

        Observable<List<AccuHourlyResult>> hourly = mApi.getHourly(
                location.getCityId(), SettingsManager.getInstance(context).getProviderAccuWeatherKey(true), languageCode, true);

        Observable<AccuMinuteResult> minute = mApi.getMinutely(
                SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                languageCode,
                true,
                location.getLatitude() + "," + location.getLongitude()
        ).onExceptionResumeNext(
                Observable.create(emitter -> emitter.onNext(new EmptyMinuteResult()))
        );

        Observable<List<AccuAlertResult>> alert = mApi.getAlert(
                location.getCityId(), SettingsManager.getInstance(context).getProviderAccuWeatherKey(true), languageCode, true);

        Observable<AccuAqiResult> aqi = mApi.getAirQuality(
                location.getCityId(),
                SettingsManager.getInstance(context).getProviderAccuAqiKey(true)
        ).onExceptionResumeNext(
                Observable.create(emitter -> emitter.onNext(new EmptyAqiResult()))
        );

        Observable.zip(realtime, daily, hourly, minute, alert, aqi,
                (accuRealtimeResults,
                 accuDailyResult, accuHourlyResults, accuMinuteResult,
                 accuAlertResults,
                 accuAqiResult) -> AccuResultConverter.convert(
                         context,
                         location,
                         accuRealtimeResults.get(0),
                         accuDailyResult,
                         accuHourlyResults,
                         accuMinuteResult instanceof EmptyMinuteResult ? null : accuMinuteResult,
                         accuAqiResult instanceof EmptyAqiResult ? null : accuAqiResult,
                         accuAlertResults
                 )
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<WeatherResultWrapper>() {
                    @Override
                    public void onSucceed(WeatherResultWrapper wrapper) {
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

    @Override
    @NonNull
    public List<Location> requestLocation(Context context, String query) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();
        List<AccuLocationResult> resultList = null;
        try {
            resultList = mApi.callWeatherLocation(
                    "Always",
                    SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                    query,
                    languageCode
            ).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String zipCode = query.matches("[a-zA-Z0-9]*") ? query : null;

        List<Location> locationList = new ArrayList<>();
        if (resultList != null && resultList.size() != 0) {
            for (AccuLocationResult r : resultList) {
                locationList.add(AccuResultConverter.convert(null, r, zipCode));
            }
        }
        return locationList;
    }

    @Override
    public void requestLocation(Context context, Location location,
                                @NonNull RequestLocationCallback callback) {

        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        mApi.getWeatherLocationByGeoPosition(
                "Always",
                SettingsManager.getInstance(context).getProviderAccuWeatherKey(true),
                location.getLatitude() + "," + location.getLongitude(),
                languageCode
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<AccuLocationResult>() {
                    @Override
                    public void onSucceed(AccuLocationResult accuLocationResult) {
                        if (accuLocationResult != null) {
                            List<Location> locationList = new ArrayList<>();
                            locationList.add(AccuResultConverter.convert(location, accuLocationResult, null));
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

        mApi.getWeatherLocation("Always", SettingsManager.getInstance(context).getProviderAccuWeatherKey(true), query, languageCode)
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<AccuLocationResult>>() {
                    @Override
                    public void onSucceed(List<AccuLocationResult> accuLocationResults) {
                        if (accuLocationResults != null && accuLocationResults.size() != 0) {
                            List<Location> locationList = new ArrayList<>();
                            for (AccuLocationResult r : accuLocationResults) {
                                locationList.add(AccuResultConverter.convert(null, r, zipCode));
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