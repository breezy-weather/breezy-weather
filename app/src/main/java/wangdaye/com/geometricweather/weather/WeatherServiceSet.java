package wangdaye.com.geometricweather.weather;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.weather.services.AccuWeatherService;
import wangdaye.com.geometricweather.weather.services.CaiYunWeatherService;
import wangdaye.com.geometricweather.weather.services.MetNoWeatherService;
import wangdaye.com.geometricweather.weather.services.MfWeatherService;
import wangdaye.com.geometricweather.weather.services.OwmWeatherService;
import wangdaye.com.geometricweather.weather.services.WeatherService;

public class WeatherServiceSet {

    private final WeatherService[] mWeatherServices;

    @Inject
    public WeatherServiceSet(AccuWeatherService accuWeatherService,
                             CaiYunWeatherService caiYunWeatherService,
                             MfWeatherService mfWeatherService,
                             OwmWeatherService owmWeatherService,
                             MetNoWeatherService metNoWeatherService) {
        mWeatherServices = new WeatherService[] {
                accuWeatherService,
                caiYunWeatherService,
                mfWeatherService,
                owmWeatherService,
                metNoWeatherService
        };
    }

    @NonNull
    public WeatherService get(WeatherSource source) {
        switch (source) {
            case METNO:
                return mWeatherServices[4];

            case OWM:
                return mWeatherServices[3];

            case MF:
                return mWeatherServices[2];

            case CAIYUN:
                return mWeatherServices[1];

            default: // ACCU.
                return mWeatherServices[0];
        }
    }

    @NonNull
    public WeatherService[] getAll() {
        return mWeatherServices;
    }
}
