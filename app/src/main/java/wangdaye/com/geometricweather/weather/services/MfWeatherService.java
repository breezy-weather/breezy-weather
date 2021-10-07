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
import wangdaye.com.geometricweather.weather.apis.AtmoAuraIqaApi;
import wangdaye.com.geometricweather.weather.apis.MfWeatherApi;
import wangdaye.com.geometricweather.weather.converters.MfResultConverter;
import wangdaye.com.geometricweather.weather.json.atmoaura.AtmoAuraQAResult;
import wangdaye.com.geometricweather.weather.json.mf.MfCurrentResult;
import wangdaye.com.geometricweather.weather.json.mf.MfEphemerisResult;
import wangdaye.com.geometricweather.weather.json.mf.MfForecastResult;
import wangdaye.com.geometricweather.weather.json.mf.MfForecastV2Result;
import wangdaye.com.geometricweather.weather.json.mf.MfLocationResult;
import wangdaye.com.geometricweather.weather.json.mf.MfRainResult;
import wangdaye.com.geometricweather.weather.json.mf.MfWarningsResult;

/**
 * Mf weather service.
 */

public class MfWeatherService extends WeatherService {

    private final MfWeatherApi mMfApi;
    private final AtmoAuraIqaApi mAtmoAuraApi;
    private final CompositeDisposable mCompositeDisposable;

    private static class EmptyAtmoAuraQAResult extends AtmoAuraQAResult {
    }

    private static class EmptyWarningsResult extends MfWarningsResult {
    }

    @Inject
    public MfWeatherService(MfWeatherApi mfApi, AtmoAuraIqaApi atmoApi,
                            CompositeDisposable disposable) {
        mMfApi = mfApi;
        mAtmoAuraApi = atmoApi;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        Observable<MfCurrentResult> current = mMfApi.getCurrent(
                location.getLatitude(), location.getLongitude(), languageCode, SettingsManager.getInstance(context).getProviderMfWsftKey(true));

        Observable<MfForecastResult> forecast = mMfApi.getForecast(
                location.getLatitude(), location.getLongitude(), languageCode, SettingsManager.getInstance(context).getProviderMfWsftKey(true));

        // TODO: Will allow us to display forecast for day and night in daily
        //Observable<MfForecastResult> dayNightForecast = api.getForecastInstants(
        //        location.getLatitude(), location.getLongitude(), languageCode, "afternoon,night", SettingsManager.getInstance(context).getProviderMfWsftKey(true));

        Observable<MfEphemerisResult> ephemeris = mMfApi.getEphemeris(
                location.getLatitude(), location.getLongitude(), "en", SettingsManager.getInstance(context).getProviderMfWsftKey(true));
        // English required to convert moon phase

        Observable<MfRainResult> rain = mMfApi.getRain(
                location.getLatitude(), location.getLongitude(), languageCode, SettingsManager.getInstance(context).getProviderMfWsftKey(true));

        Observable<MfWarningsResult> warnings = mMfApi.getWarnings(
                location.getProvince(), null, SettingsManager.getInstance(context).getProviderMfWsftKey(true)
        ).onExceptionResumeNext(
                // FIXME: Will not report warnings if current location was searched through AccuWeather search because "province" is not the department
                Observable.create(emitter -> emitter.onNext(new EmptyWarningsResult()))
        );

        Observable<AtmoAuraQAResult> aqiAtmoAura;
        if (location.getProvince() == null) {
            aqiAtmoAura = Observable.create(emitter -> emitter.onNext(new EmptyAtmoAuraQAResult()));
        } else if (location.getProvince().equals("Auvergne-Rhône-Alpes") || location.getProvince().equals("01")
                || location.getProvince().equals("03") || location.getProvince().equals("07")
                || location.getProvince().equals("15") || location.getProvince().equals("26")
                || location.getProvince().equals("38") || location.getProvince().equals("42")
                || location.getProvince().equals("43") || location.getProvince().equals("63")
                || location.getProvince().equals("69") || location.getProvince().equals("73")
                || location.getProvince().equals("74")) {
            aqiAtmoAura = mAtmoAuraApi.getQAFull(
                    SettingsManager.getInstance(context).getProviderIqaAtmoAuraKey(true),
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude())
            ).onExceptionResumeNext(
                    Observable.create(emitter -> emitter.onNext(new EmptyAtmoAuraQAResult()))
            );
        } else {
            aqiAtmoAura = Observable.create(emitter -> emitter.onNext(new EmptyAtmoAuraQAResult()));
        }

        Observable.zip(current, forecast, ephemeris, rain, warnings, aqiAtmoAura,
                (mfCurrentResult, mfForecastResult, mfEphemerisResult, mfRainResult, mfWarningResults, aqiAtmoAuraResult) -> MfResultConverter.convert(
                        context,
                        location,
                        mfCurrentResult,
                        mfForecastResult,
                        mfEphemerisResult,
                        mfRainResult,
                        mfWarningResults,
                        aqiAtmoAuraResult instanceof EmptyAtmoAuraQAResult ? null : aqiAtmoAuraResult
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
        List<MfLocationResult> resultList = null;
        try {
            resultList = mMfApi.callWeatherLocation(query, 48.86d, 2.34d, SettingsManager.getInstance(context).getProviderMfWsftKey(true)).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Location> locationList = new ArrayList<>();
        if (resultList != null && resultList.size() != 0) {
            for (MfLocationResult r : resultList) {
                if (r.postCode != null) {
                    locationList.add(MfResultConverter.convert(null, r));
                }
            }
        }
        return locationList;
    }

    @Override
    public void requestLocation(Context context, Location location,
                                @NonNull RequestLocationCallback callback) {

        String languageCode = SettingsManager.getInstance(context).getLanguage().getCode();

        mMfApi.getForecastV2(
                location.getLatitude(),
                location.getLongitude(),
                languageCode,
                SettingsManager.getInstance(context).getProviderMfWsftKey(true)
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<MfForecastV2Result>() {
                    @Override
                    public void onSucceed(MfForecastV2Result mfForecastV2Result) {
                        if (mfForecastV2Result != null) {
                            List<Location> locationList = new ArrayList<>();
                            if (mfForecastV2Result.properties.insee != null) {
                                locationList.add(MfResultConverter.convert(null, mfForecastV2Result));
                            }
                            // FIXME: Caching geo position
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
                        // FIXME: Caching geo position
                        callback.requestLocationFailed(
                                location.getLatitude() + "," + location.getLongitude()
                        );
                    }
                }));
    }

    public void requestLocation(Context context, String query,
                                @NonNull RequestLocationCallback callback) {
        mMfApi.getWeatherLocation(query, 48.86d, 2.34d, SettingsManager.getInstance(context).getProviderMfWsftKey(true))
                .compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<MfLocationResult>>() {
                    @Override
                    public void onSucceed(List<MfLocationResult> mfLocationResults) {
                        if (mfLocationResults != null && mfLocationResults.size() != 0) {
                            List<Location> locationList = new ArrayList<>();
                            for (MfLocationResult r : mfLocationResults) {
                                if (r.postCode != null) {
                                    locationList.add(MfResultConverter.convert(null, r));
                                }
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