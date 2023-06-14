package org.breezyweather.weather;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import org.breezyweather.common.basic.models.options.provider.WeatherSource;
import org.breezyweather.weather.services.*;

public class WeatherServiceSet {

    private final WeatherService[] mWeatherServices;

    @Inject
    public WeatherServiceSet(OpenMeteoWeatherService openMeteoWeatherService,
                             AccuWeatherService accuWeatherService,
                             MetNoWeatherService metNoWeatherService,
                             OpenWeatherWeatherService openWeatherWeatherService,
                             MfWeatherService mfWeatherService,
                             ChinaWeatherService chinaWeatherService) {
        mWeatherServices = new WeatherService[] {
                openMeteoWeatherService,
                accuWeatherService,
                metNoWeatherService,
                openWeatherWeatherService,
                mfWeatherService,
                chinaWeatherService
        };
    }

    @NonNull
    public WeatherService get(WeatherSource source) {
        switch (source) {
            case OPEN_METEO:
                return mWeatherServices[0];

            case ACCU:
                return mWeatherServices[1];

            case METNO:
                return mWeatherServices[2];

            case OPEN_WEATHER:
                return mWeatherServices[3];

            case MF:
                return mWeatherServices[4];

            case CHINA:
                return mWeatherServices[5];

            default:
                return mWeatherServices[1]; // ACCU
        }
    }

    @NonNull
    public WeatherService[] getAll() {
        return mWeatherServices;
    }
}
