package wangdaye.com.geometricweather.weather.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import wangdaye.com.geometricweather.BuildConfig;
import wangdaye.com.geometricweather.common.basic.models.Location;
import wangdaye.com.geometricweather.common.rxjava.BaseObserver;
import wangdaye.com.geometricweather.common.rxjava.ObserverContainer;
import wangdaye.com.geometricweather.common.rxjava.SchedulerTransformer;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
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

    private static final String PREFERENCE_LOCAL = "LOCAL_PREFERENCE";
    private static final String KEY_OLD_DISTRICT = "OLD_DISTRICT";
    private static final String KEY_OLD_CITY = "OLD_CITY";
    private static final String KEY_OLD_PROVINCE = "OLD_PROVINCE";
    private static final String KEY_OLD_KEY = "OLD_KEY";

    private static class CacheLocationRequestCallback implements RequestLocationCallback {

        private final Context mContext;
        private @NonNull
        final RequestLocationCallback mCallback;

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

    private static class EmptyAqiResult extends OwmAirPollutionResult {
    }

    @Inject
    public OwmWeatherService(OwmApi api, CompositeDisposable disposable) {
        mApi = api;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String languageCode = SettingsOptionManager.getInstance(context).getLanguage().getCode();

        Observable<OwmOneCallResult> oneCall = mApi.getOneCall(
                BuildConfig.OWM_KEY, location.getLatitude(), location.getLongitude(), "metric", languageCode);

        Observable<OwmAirPollutionResult> airPollutionCurrent = mApi.getAirPollutionCurrent(
                BuildConfig.OWM_KEY, location.getLatitude(), location.getLongitude()
        ).onExceptionResumeNext(
                Observable.create(emitter -> emitter.onNext(new EmptyAqiResult()))
        );

        Observable<OwmAirPollutionResult> airPollutionForecast = mApi.getAirPollutionForecast(
                BuildConfig.OWM_KEY, location.getLatitude(), location.getLongitude()
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
        List<OwmLocationResult> resultList = null;
        try {
            resultList = mApi.callWeatherLocation(BuildConfig.OWM_KEY, query).execute().body();
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

        final CacheLocationRequestCallback finalCallback = new CacheLocationRequestCallback(context, callback);

        mApi.getWeatherLocationByGeoPosition(
                BuildConfig.OWM_KEY, location.getLatitude(), location.getLongitude()
        ).compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<List<OwmLocationResult>>() {
                    @Override
                    public void onSucceed(List<OwmLocationResult> owmLocationResultList) {
                        if (owmLocationResultList != null && !owmLocationResultList.isEmpty()) {
                            List<Location> locationList = new ArrayList<>();
                            locationList.add(OwmResultConverter.convert(location, owmLocationResultList.get(0), null));
                            finalCallback.requestLocationSuccess(
                                    location.getLatitude() + "," + location.getLongitude(), locationList);
                        } else {
                            onFailed();
                        }
                    }

                    @Override
                    public void onFailed() {
                        finalCallback.requestLocationFailed(
                                location.getLatitude() + "," + location.getLongitude());
                    }
                }));
    }

    public void requestLocation(Context context, String query,
                                @NonNull RequestLocationCallback callback) {
        String zipCode = query.matches("[a-zA-Z0-9]") ? query : null;

        mApi.getWeatherLocation(BuildConfig.OWM_KEY, query)
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