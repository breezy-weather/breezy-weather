package org.breezyweather.weather.openmeteo;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import org.breezyweather.common.basic.models.Location;
import org.breezyweather.main.utils.RequestErrorType;
import org.breezyweather.weather.WeatherService;
import org.breezyweather.common.basic.models.options.provider.WeatherSource;
import org.breezyweather.common.rxjava.BaseObserver;
import org.breezyweather.common.rxjava.ObserverContainer;
import org.breezyweather.common.rxjava.SchedulerTransformer;
import org.breezyweather.settings.SettingsManager;
import org.breezyweather.weather.openmeteo.json.OpenMeteoAirQualityResult;
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResult;
import org.breezyweather.weather.openmeteo.json.OpenMeteoLocationResults;
import org.breezyweather.weather.openmeteo.json.OpenMeteoWeatherResult;

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
    public Boolean isConfigured(Context context) {
        return true;
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

        // TODO: pollen
        String[] airQualityHourly = {
                "pm10",
                "pm2_5",
                "carbon_monoxide",
                "nitrogen_dioxide",
                "sulphur_dioxide",
                "ozone"
        };
        Observable<OpenMeteoAirQualityResult> aqi = mAirQualityApi.getAirQuality(
                location.getLatitude(), location.getLongitude(), String.join(",", airQualityHourly));

        Observable.zip(weather, aqi,
                (openMeteoWeatherResult, openMeteoAirQualityResult) -> OpenMeteoResultConverterKt.convert(
                        context,
                        location,
                        openMeteoWeatherResult,
                        openMeteoAirQualityResult
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
                        callback.requestWeatherFailed(location, RequestErrorType.WEATHER_REQ_FAILED);
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