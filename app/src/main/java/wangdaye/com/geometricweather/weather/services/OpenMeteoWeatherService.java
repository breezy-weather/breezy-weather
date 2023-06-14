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
import wangdaye.com.geometricweather.weather.apis.OpenMeteoAirQualityApi;
import wangdaye.com.geometricweather.weather.apis.OpenMeteoGeocodingApi;
import wangdaye.com.geometricweather.weather.apis.OpenMeteoWeatherApi;
import wangdaye.com.geometricweather.weather.converters.OpenMeteoResultConverterKt;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResult;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoLocationResults;
import wangdaye.com.geometricweather.weather.json.openmeteo.OpenMeteoWeatherResult;

/**
 * Open-Meteo weather service.
 **/
public class OpenMeteoWeatherService extends WeatherService {

    private final OpenMeteoWeatherApi mWeatherApi;
    private final OpenMeteoGeocodingApi mGeocodingApi;
    private final OpenMeteoAirQualityApi mAirQualityApi;
    private final CompositeDisposable mCompositeDisposable;

    @Inject
    public OpenMeteoWeatherService(OpenMeteoWeatherApi weatherApi, OpenMeteoGeocodingApi geocodingApi, OpenMeteoAirQualityApi airQualityApi, CompositeDisposable disposable) {
        mWeatherApi = weatherApi;
        mGeocodingApi = geocodingApi;
        mAirQualityApi = airQualityApi;
        mCompositeDisposable = disposable;
    }

    @Override
    public void requestWeather(Context context, Location location, @NonNull RequestWeatherCallback callback) {
        String[] daily = {
                "temperature_2m_max",
                "temperature_2m_min",
                "apparent_temperature_max",
                "apparent_temperature_min",
                "sunrise",
                "sunset",
                "uv_index_max"
        };
        String[] hourly = {
                "temperature_2m",
                "apparent_temperature",
                "precipitation_probability",
                "precipitation",
                "rain",
                "showers",
                "snowfall",
                "weathercode",
                "windspeed_10m",
                "winddirection_10m",
                "uv_index",
                "is_day",

                // Used by current only
                "relativehumidity_2m",
                "dewpoint_2m",
                "surface_pressure",
                "cloudcover",
                "visibility"
        };

        Observable<OpenMeteoWeatherResult> weather = mWeatherApi.getWeather(
                location.getLatitude(), location.getLongitude(), String.join(",", daily), String.join(",", hourly), 16, 1, true);

        // TODO: air quality and pollen
        /*Observable<OpenMeteoAirQualityResult> aqi = mAirQualityApi.getAirQuality(
                location.getLatitude(), location.getLongitude());

        Observable.zip(weather, Observable.from(new String[] {"test"}), aqi,
                        (openMeteoWeatherResult, openMeteoAirQualityResult) -> OpenMeteoResultConverter.convert(
                                context,
                                location,
                                openMeteoWeatherResult,
                                openMeteoAirQualityResult
                        )
                )*/weather.compose(SchedulerTransformer.create())
                .subscribe(new ObserverContainer<>(mCompositeDisposable, new BaseObserver<OpenMeteoWeatherResult>() {
                    @Override
                    public void onSucceed(OpenMeteoWeatherResult openMeteoWeatherResult) {
                        WeatherResultWrapper wrapper = OpenMeteoResultConverterKt.convert(
                                context,
                                location,
                                openMeteoWeatherResult
                        );
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
                locationList.add(OpenMeteoResultConverterKt.convert(null, r, WeatherSource.OPEN_METEO));
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
                                locationList.add(OpenMeteoResultConverterKt.convert(null, r, WeatherSource.OPEN_METEO));
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