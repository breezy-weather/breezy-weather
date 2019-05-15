package wangdaye.com.geometricweather.ui.widget.weatherView;

import androidx.annotation.NonNull;

import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

/**
 * Weather view controller.
 * */

public class WeatherViewController {

    public static void setWeatherViewWeatherKind(@NonNull WeatherView view, @NonNull Weather weather,
                                                 boolean dayTime, @NonNull ResourceProvider provider) {
        view.setWeather(getWeatherViewWeatherKind(weather.realTime.weatherKind), dayTime, provider);
    }

    public static String getEntityWeatherKind(@WeatherView.WeatherKindRule int weatherKind) {
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLEAR:
                return Weather.KIND_CLEAR;

            case WeatherView.WEATHER_KIND_CLOUDY:
                return Weather.KIND_CLOUDY;

            case WeatherView.WEATHER_KIND_CLOUD:
                return Weather.KIND_PARTLY_CLOUDY;

            case WeatherView.WEATHER_KIND_FOG:
                return Weather.KIND_FOG;

            case WeatherView.WEATHER_KIND_HAIL:
                return Weather.KIND_HAIL;

            case WeatherView.WEATHER_KIND_HAZE:
                return Weather.KIND_HAZE;

            case WeatherView.WEATHER_KIND_RAINY:
                return Weather.KIND_RAIN;

            case WeatherView.WEATHER_KIND_SLEET:
                return Weather.KIND_SLEET;

            case WeatherView.WEATHER_KIND_SNOW:
                return Weather.KIND_SNOW;

            case WeatherView.WEATHER_KIND_THUNDERSTORM:
                return Weather.KIND_THUNDERSTORM;

            case WeatherView.WEATHER_KIND_THUNDER:
                return Weather.KIND_THUNDER;

            case WeatherView.WEATHER_KIND_WIND:
                return Weather.KIND_WIND;

            case WeatherView.WEATHER_KING_NULL:
            default:
                return Weather.KIND_CLEAR;
        }
    }

    @WeatherView.WeatherKindRule
    public static int getWeatherViewWeatherKind(String weatherKind) {
        switch (weatherKind) {
            case Weather.KIND_CLEAR:
                return WeatherView.WEATHER_KIND_CLEAR;

            case Weather.KIND_PARTLY_CLOUDY:
                return WeatherView.WEATHER_KIND_CLOUD;

            case Weather.KIND_CLOUDY:
                return WeatherView.WEATHER_KIND_CLOUDY;

            case Weather.KIND_RAIN:
                return WeatherView.WEATHER_KIND_RAINY;

            case Weather.KIND_SNOW:
                return WeatherView.WEATHER_KIND_SNOW;

            case Weather.KIND_WIND:
                return WeatherView.WEATHER_KIND_WIND;

            case Weather.KIND_FOG:
                return WeatherView.WEATHER_KIND_FOG;

            case Weather.KIND_HAZE:
                return WeatherView.WEATHER_KIND_HAZE;

            case Weather.KIND_SLEET:
                return WeatherView.WEATHER_KIND_RAINY;

            case Weather.KIND_HAIL:
                return WeatherView.WEATHER_KIND_HAIL;

            case Weather.KIND_THUNDER:
                return WeatherView.WEATHER_KIND_THUNDER;

            case Weather.KIND_THUNDERSTORM:
                return WeatherView.WEATHER_KIND_THUNDERSTORM;
        }
        return WeatherView.WEATHER_KIND_CLEAR;
    }
}
