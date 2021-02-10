package wangdaye.com.geometricweather.weather.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import retrofit2.Retrofit;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.basic.model.Location;
import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.weather.SchedulerTransformer;
import wangdaye.com.geometricweather.weather.api.AtmoAuraIqaApi;
import wangdaye.com.geometricweather.weather.api.MfWeatherApi;
import wangdaye.com.geometricweather.weather.converter.MfResultConverter;
import wangdaye.com.geometricweather.weather.interceptor.GzipInterceptor;
import wangdaye.com.geometricweather.weather.json.atmoaura.AtmoAuraQAResult;
import wangdaye.com.geometricweather.weather.json.mf.*;
import wangdaye.com.geometricweather.weather.observer.BaseObserver;
import wangdaye.com.geometricweather.weather.observer.ObserverContainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mf weather service.
 */

public class MfWeatherService extends WeatherService {

    private final MfWeatherApi mApi;
    private final AtmoAuraIqaApi mAtmoAuraApi;
    private final CompositeDisposable mCompositeDisposable;

    private static final String PREFERENCE_LOCAL = "LOCAL_PREFERENCE";
    private static final String KEY_OLD_DISTRICT = "OLD_DISTRICT";
    private static final String KEY_OLD_CITY = "OLD_CITY";
    private static final String KEY_OLD_PROVINCE = "OLD_PROVINCE";
    private static final String KEY_OLD_KEY = "OLD_KEY";

    private static class CacheLocationRequestCallback implements RequestLocationCallback {

        private final Context mContext;
        private final @NonNull RequestLocationCallback mCallback;

        CacheLocationRequestCallback(Context context, @NonNull RequestLocationCallback callback) {
            mContext = context;
            mCallback = callback;
        }

        @Override
        public void requestLocationSuccess(String query, List<Location> locationList) {
            if (!TextUtils.isEmpty(locationList.get(0).getCityId())) {
                mContext.getSharedPreferences(PREFERENCE_LOCAL, Context.MODE_PRIVATE)
                        .edit()
                        .putString(KEY_OLD_KEY, locationList.get(0).getCityId())
                        .apply();
            }
            mCallback.requestLocationSuccess(query, locationList);
        }

        @Override
        public void requestLocationFailed(String query) {
            mContext.getSharedPreferences(PREFERENCE_LOCAL, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_OLD_DISTRICT, "")
                    .putString(KEY_OLD_CITY, "")
                    .putString(KEY_OLD_PROVINCE, "")
                    .putString(KEY_OLD_KEY, "")
                    .apply();
            mCallback.requestLocationFailed(query);
        }
    }

    private static class EmptyAtmoAuraQAResult extends AtmoAuraQAResult {
    }

    private static class EmptyWarningsResult extends MfWarningsResult {
    }

    public MfWeatherService() {
        mApi = new Retrofit.Builder()
                .baseUrl(BuildConfig.MF_WSFT_BASE_URL)
                .client(
                        GeometricWeather.getInstance()
                                .getOkHttpClient()
                                .newBuilder()
                                .addInterceptor(new GzipInterceptor())
                                .build()
                ).addConverterFactory(GeometricWeather.getInstance().getGsonConverterFactory())
                .addCallAdapterFactory(GeometricWeather.getInstance().getRxJava2CallAdapterFactory())
                .build()
                .create((MfWeatherApi.class));
        mAtmoAuraApi = new Retrofit.Builder()
                .baseUrl(BuildConfig.IQA_ATMO_AURA_URL)
                .client(
                        GeometricWeather.getInstance()
                                .getOkHttpClient()
                                .newBuilder()
                                .addInterceptor(new GzipInterceptor())
                                .build()
                ).addConverterFactory(GeometricWeather.getInstance().getGsonConverterFactory())
                .addCallAdapterFactory(GeometricWeather.getInstance().getRxJava2CallAdapterFactory())
                .build()
                .create((AtmoAuraIqaApi.class));
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = SettingsOptionManager.getInstance(context).getLanguage().getCode();

        Observable<MfCurrentResult> current = mApi.getCurrent(
                location.getLatitude(), location.getLongitude(), languageCode, BuildConfig.MF_WSFT_KEY);

        Observable<MfForecastResult> forecast = mApi.getForecast(
                location.getLatitude(), location.getLongitude(), languageCode, BuildConfig.MF_WSFT_KEY);

        // TODO: Will allow us to display forecast for day and night in daily
        //Observable<MfForecastResult> dayNightForecast = api.getForecastInstants(
        //        location.getLatitude(), location.getLongitude(), languageCode, "afternoon,night", BuildConfig.MF_WSFT_KEY);

        Observable<MfEphemerisResult> ephemeris = mApi.getEphemeris(
                location.getLatitude(), location.getLongitude(), "en", BuildConfig.MF_WSFT_KEY);
        // English required to convert moon phase

        Observable<MfRainResult> rain = mApi.getRain(
                location.getLatitude(), location.getLongitude(), languageCode, BuildConfig.MF_WSFT_KEY);

        Observable<MfWarningsResult> warnings = mApi.getWarnings(
                location.getProvince(), null, BuildConfig.MF_WSFT_KEY
        ).onExceptionResumeNext(
                // FIXME: Will not report warnings if current location was searched through AccuWeather search because "province" is not the department
                Observable.create(emitter -> emitter.onNext(new EmptyWarningsResult()))
        );

        Observable<AtmoAuraQAResult> aqiAtmoAura = null;
        if (location.getProvince().equals("Auvergne-Rhône-Alpes") || location.getProvince().equals("01")
                || location.getProvince().equals("03") || location.getProvince().equals("07")
                || location.getProvince().equals("15") || location.getProvince().equals("26")
                || location.getProvince().equals("38") || location.getProvince().equals("42")
                || location.getProvince().equals("43") || location.getProvince().equals("63")
                || location.getProvince().equals("69") || location.getProvince().equals("73")
                || location.getProvince().equals("74")) {
            aqiAtmoAura = mAtmoAuraApi.getQAFull(
                    BuildConfig.IQA_ATMO_AURA_KEY,
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
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<Weather>() {
                    @Override
                    public void onSucceed(Weather weather) {
                        if (weather != null) {
                            location.setWeather(weather);
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
            resultList = mApi.callWeatherLocation(query, 48.86d, 2.34d, BuildConfig.MF_WSFT_KEY).execute().body();
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
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCE_LOCAL,
                Context.MODE_PRIVATE
        );
        String oldDistrict = sharedPreferences.getString(KEY_OLD_DISTRICT, "");
        String oldCity = sharedPreferences.getString(KEY_OLD_CITY, "");
        String oldProvince = sharedPreferences.getString(KEY_OLD_PROVINCE, "");
        String oldKey = sharedPreferences.getString(KEY_OLD_KEY, "");

        if (location.hasGeocodeInformation()
                && queryEqualsIgnoreEmpty(location.getDistrict(), oldDistrict)
                && queryEquals(location.getCity(), oldCity)
                && queryEquals(location.getProvince(), oldProvince)
                && queryEquals(location.getCityId(), oldKey)) {
            List<Location> locationList = new ArrayList<>();
            locationList.add(location);
            callback.requestLocationSuccess(
                    location.getLatitude() + "," + location.getLongitude(),
                    locationList
            );
            return;
        }

        sharedPreferences.edit()
                .putString(KEY_OLD_DISTRICT, location.getDistrict())
                .putString(KEY_OLD_CITY, location.getCity())
                .putString(KEY_OLD_PROVINCE, location.getProvince())
                .apply();

        String languageCode = SettingsOptionManager.getInstance(context).getLanguage().getCode();
        final CacheLocationRequestCallback finalCallback = new CacheLocationRequestCallback(context, callback);

        mApi.getForecastV2(
                location.getLatitude(),
                location.getLongitude(),
                languageCode,
                BuildConfig.MF_WSFT_KEY
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
                            finalCallback.requestLocationSuccess(
                                    location.getLatitude() + "," + location.getLongitude(), locationList);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        // FIXME: Caching geo position
                        finalCallback.requestLocationFailed(
                                location.getLatitude() + "," + location.getLongitude());
                    }
                }));
    }

    public void requestLocation(Context context, String query,
                                @NonNull RequestLocationCallback callback) {
        mApi.getWeatherLocation(query, 48.86d, 2.34d, BuildConfig.MF_WSFT_KEY)
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

    private boolean queryEquals(String a, String b) {
        if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b)) {
            return a.equals(b);
        }
        return false;
    }

    private boolean queryEqualsIgnoreEmpty(String a, String b) {
        if (TextUtils.isEmpty(a) && TextUtils.isEmpty(b)) {
            return true;
        }
        if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b)) {
            return a.equals(b);
        }
        return false;
    }
}