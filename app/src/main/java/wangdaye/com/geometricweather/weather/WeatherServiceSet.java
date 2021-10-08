package wangdaye.com.geometricweather.weather;

import androidx.annotation.NonNull;

import javax.inject.Inject;

import wangdaye.com.geometricweather.common.basic.models.options.provider.WeatherSource;
import wangdaye.com.geometricweather.weather.services.AccuWeatherService;
import wangdaye.com.geometricweather.weather.services.CNWeatherService;
import wangdaye.com.geometricweather.weather.services.CaiYunWeatherService;
import wangdaye.com.geometricweather.weather.services.MfWeatherService;
import wangdaye.com.geometricweather.weather.services.OwmWeatherService;
import wangdaye.com.geometricweather.weather.services.WeatherService;

public class WeatherServiceSet {

    private final WeatherService[] mWeatherServices;

    @Inject
    public WeatherServiceSet(AccuWeatherService accuWeatherService,
                             CNWeatherService cnWeatherService,
                             CaiYunWeatherService caiYunWeatherService,
                             MfWeatherService mfWeatherService,
                             OwmWeatherService owmWeatherService) {
        mWeatherServices = new WeatherService[] {
                accuWeatherService,
                cnWeatherService,
                caiYunWeatherService,
                mfWeatherService,
                owmWeatherService
        };
    }

    @NonNull
    public WeatherService get(WeatherSource source) {
        switch (source) {
            case OWM:
                return mWeatherServices[4];

            case MF:
                return mWeatherServices[3];

            case CAIYUN:
                return mWeatherServices[2];

            case CN:
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
