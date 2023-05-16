package wangdaye.com.geometricweather.weather.services;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.BuildConfig;
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
        String token = this.getToken(context);

        Observable<MfCurrentResult> current = mMfApi.getCurrent(
            getUserAgent(), location.getLatitude(), location.getLongitude(), languageCode, token);

        Observable<MfForecastV2Result> forecastV2 = mMfApi.getForecastV2(
            getUserAgent(), location.getLatitude(), location.getLongitude(),"timestamp","", token);

        Observable<MfEphemerisResult> ephemeris = mMfApi.getEphemeris(
            getUserAgent(), location.getLatitude(), location.getLongitude(), "en", "timestamp", token);
        // English required to convert moon phase

        Observable<MfRainResult> rain = mMfApi.getRain(
            getUserAgent(), location.getLatitude(), location.getLongitude(), languageCode, "timestamp", token);

        Observable<MfWarningsResult> warnings = mMfApi.getWarnings(
            getUserAgent(), location.getProvince(), "timestamp", token
        ).onErrorResumeNext(error ->
            // FIXME: Will not report warnings if current location was searched through AccuWeather search because "province" is not the department
            Observable.create(emitter -> emitter.onNext(new EmptyWarningsResult()))
        );

        Observable<AtmoAuraQAResult> aqiAtmoAura;
        if (location.getProvince().equals("Auvergne-Rhône-Alpes") || location.getProvince().equals("01")
                || location.getProvince().equals("03") || location.getProvince().equals("07")
                || location.getProvince().equals("15") || location.getProvince().equals("26")
                || location.getProvince().equals("38") || location.getProvince().equals("42")
                || location.getProvince().equals("43") || location.getProvince().equals("63")
                || location.getProvince().equals("69") || location.getProvince().equals("73")
                || location.getProvince().equals("74")) {

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date tomorrow = new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(tomorrow);
            c.add(Calendar.DATE, 1);
            tomorrow = c.getTime();
            aqiAtmoAura = mAtmoAuraApi.getPointDetails(
                    SettingsManager.getInstance(context).getProviderIqaAtmoAuraKey(),
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude()),
                    fmt.format(tomorrow), // Tomorrow because it gives access to D-1 and D+1
                    true
            ).onErrorResumeNext(error ->
                    Observable.create(emitter -> emitter.onNext(new EmptyAtmoAuraQAResult()))
            );
        } else {
            aqiAtmoAura = Observable.create(emitter -> emitter.onNext(new EmptyAtmoAuraQAResult()));
        }

        Observable.zip(current, forecastV2, ephemeris, rain, warnings, aqiAtmoAura,
                (mfCurrentResult, mfForecastV2Result, mfEphemerisResult, mfRainResult, mfWarningResults, aqiAtmoAuraResult) -> MfResultConverter.convert(
                        context,
                        location,
                        mfCurrentResult,
                        mfForecastV2Result,
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
        List<MfLocationResult> resultList = null;
        try {
            resultList = mMfApi.callWeatherLocation(getUserAgent(), query, 48.86d, 2.34d, this.getToken(context)).execute().body();
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
        mMfApi.getForecastV2(
            getUserAgent(),
            location.getLatitude(),
            location.getLongitude(),
            "timestamp",
            "",
            this.getToken(context)
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
        mMfApi.getWeatherLocation(getUserAgent(), query, 48.86d, 2.34d, this.getToken(context))
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

    protected String getToken(Context context) {
        if (!SettingsManager.getInstance(context).getProviderMfWsftKey().equals(BuildConfig.MF_WSFT_KEY)) {
            return SettingsManager.getInstance(context).getProviderMfWsftKey();
        } else {
            try {
                JwtBuilder jwtsBuilder = Jwts.builder();
                jwtsBuilder.setHeaderParam(Header.TYPE, Header.JWT_TYPE);

                HashMap<String, String> claims = new HashMap<>();
                claims.put("class", "mobile");
                claims.put(Claims.ISSUED_AT, String.valueOf(new Date().getTime() / 1000));
                claims.put(Claims.ID, UUID.randomUUID().toString());
                jwtsBuilder.setClaims(claims);

                byte[] keyBytes = BuildConfig.MF_WSFT_JWT_KEY.getBytes(StandardCharsets.UTF_8);
                jwtsBuilder.signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS256);
                return jwtsBuilder.compact();
            } catch(Exception ignored) {
                return BuildConfig.MF_WSFT_KEY;
            }
        }
    }

    protected String getUserAgent() {
        return "okhttp/4.9.2";
    }
}