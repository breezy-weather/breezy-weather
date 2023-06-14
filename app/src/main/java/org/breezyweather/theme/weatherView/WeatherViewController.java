package org.breezyweather.theme.weatherView;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.breezyweather.common.basic.models.weather.Weather;
import org.breezyweather.common.basic.models.weather.WeatherCode;
import org.breezyweather.theme.resource.providers.ResourceProvider;

public class WeatherViewController {

    public static void setWeatherCode(
            @NonNull WeatherView view,
            @Nullable Weather weather,
            boolean dayTime,
            @NonNull ResourceProvider provider
    ) {
        view.setWeather(getWeatherKind(weather), dayTime, provider);
    }

    @SuppressLint("SwitchIntDef")
    public static WeatherCode getWeatherCode(
            @WeatherView.WeatherKindRule int weatherKind
    ) {
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLOUDY:
                return WeatherCode.CLOUDY;

            case WeatherView.WEATHER_KIND_CLOUD:
                return WeatherCode.PARTLY_CLOUDY;

            case WeatherView.WEATHER_KIND_FOG:
                return WeatherCode.FOG;

            case WeatherView.WEATHER_KIND_HAIL:
                return WeatherCode.HAIL;

            case WeatherView.WEATHER_KIND_HAZE:
                return WeatherCode.HAZE;

            case WeatherView.WEATHER_KIND_RAINY:
                return WeatherCode.RAIN;

            case WeatherView.WEATHER_KIND_SLEET:
                return WeatherCode.SLEET;

            case WeatherView.WEATHER_KIND_SNOW:
                return WeatherCode.SNOW;

            case WeatherView.WEATHER_KIND_THUNDERSTORM:
                return WeatherCode.THUNDERSTORM;

            case WeatherView.WEATHER_KIND_THUNDER:
                return WeatherCode.THUNDER;

            case WeatherView.WEATHER_KIND_WIND:
                return WeatherCode.WIND;

            default:
                return WeatherCode.CLEAR;
        }
    }

    @WeatherView.WeatherKindRule
    public static int getWeatherKind(@Nullable Weather weather) {
        if (weather == null || weather.getCurrent() == null || weather.getCurrent().getWeatherCode() == null) {
            return WeatherView.WEATHER_KIND_CLEAR;
        }
        return getWeatherKind(weather.getCurrent().getWeatherCode());
    }

    @WeatherView.WeatherKindRule
    public static int getWeatherKind(WeatherCode weatherCode) {
        switch (weatherCode) {
            case CLEAR:
                return WeatherView.WEATHER_KIND_CLEAR;

            case PARTLY_CLOUDY:
                return WeatherView.WEATHER_KIND_CLOUD;

            case CLOUDY:
                return WeatherView.WEATHER_KIND_CLOUDY;

            case RAIN:
                return WeatherView.WEATHER_KIND_RAINY;

            case SNOW:
                return WeatherView.WEATHER_KIND_SNOW;

            case WIND:
                return WeatherView.WEATHER_KIND_WIND;

            case FOG:
                return WeatherView.WEATHER_KIND_FOG;

            case HAZE:
                return WeatherView.WEATHER_KIND_HAZE;

            case SLEET:
                return WeatherView.WEATHER_KIND_SLEET;

            case HAIL:
                return WeatherView.WEATHER_KIND_HAIL;

            case THUNDER:
                return WeatherView.WEATHER_KIND_THUNDER;

            case THUNDERSTORM:
                return WeatherView.WEATHER_KIND_THUNDERSTORM;
        }
        return WeatherView.WEATHER_KIND_CLEAR;
    }
}
