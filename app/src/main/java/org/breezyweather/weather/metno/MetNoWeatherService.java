package org.breezyweather.weather.metno;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.main.utils.RequestErrorType;
import org.breezyweather.weather.WeatherService;
import org.breezyweather.weather.openmeteo.OpenMeteoGeocodingApi;
import org.breezyweather.weather.metno.json.MetNoEphemerisResult;
import org.breezyweather.BuildConfig;
import org.breezyweather.common.basic.models.options.provider.WeatherSource;
import org.breezyweather.common.rxjava.BaseObserver;
import org.breezyweather.common.rxjava.ObserverContainer;
import org.breezyweather.common.rxjava.SchedulerTransformer;
import org.breezyweather.common.utils.DisplayUtils;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.weather.metno.json.MetNoForecastResult;
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResult;
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResults;
import org.breezyweather.weather.openmeteo.OpenMeteoResultConverterKt;

/**
 * MET Norway weather service.
 **/
public class MetNoWeatherService extends WeatherService {
    private final MetNoApi mApi;
    private final OpenMeteoGeocodingApi mGeocodingApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public MetNoWeatherService(MetNoApi api, OpenMeteoGeocodingApi geocodingApi, CompositeDisposable disposable) {
        mApi = api;
        mGeocodingApi = geocodingApi;
        mCompositeDisposable = disposable;
    }

    @Override
    public Boolean isConfigured(Context context) {
        return true;
    }

    protected String getUserAgent() {
        return "BreezyWeather/"+ BuildConfig.VERSION_NAME + " github.com/breezy-weather/breezy-weather/issues";
    }

    protected static String getTimezoneOffset(TimeZone tz) {
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

        return offset;
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        Observable<MetNoForecastResult> forecast = mApi.getForecast(
                getUserAgent(),
                location.getLatitude(),
                location.getLongitude()
        );

        String formattedDate = DisplayUtils.getFormattedDate(new Date(), location.getTimeZone(), "yyyy-MM-dd");
        Observable<MetNoEphemerisResult> ephemeris = mApi.getEphemeris(
                getUserAgent(),
                formattedDate,
                15,
                location.getLatitude(),
                location.getLongitude(),
                getTimezoneOffset(location.getTimeZone())
        );

        Observable.zip(forecast, ephemeris,
                (metNoForecast, metNoEphemeris) -> MetNoResultConverterKt.convert(
                         context,
                         location,
                         metNoForecast,
                         metNoEphemeris
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
                        } else {
                            callback.requestWeatherFailed(location, RequestErrorType.WEATHER_REQ_FAILED);
                        }
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
                locationList.add(OpenMeteoResultConverterKt.convert(null, r, WeatherSource.METNO));
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
                                locationList.add(OpenMeteoResultConverterKt.convert(null, r, WeatherSource.METNO));
                            }
                            callback.requestLocationSuccess(query, locationList);
                        } else {
                            callback.requestLocationFailed(query, RequestErrorType.LOCATION_FAILED);
                        }

                    }

                    @Override
                    public void onFailed() {
                        callback.requestLocationFailed(query, RequestErrorType.LOCATION_FAILED);
                    }
                }));
    }

    @Override
    public void cancel() {
        mCompositeDisposable.clear();
    }
}