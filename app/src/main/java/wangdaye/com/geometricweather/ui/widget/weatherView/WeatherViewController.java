package wangdaye.com.geometricweather.ui.widget.weatherView;

import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.weather.WeatherHelper;

/**
 * Weather view controller.
 * */

public class WeatherViewController {

    public static void setWeatherViewWeatherKind(WeatherView view, Weather weather, boolean dayTime) {
        view.setWeather(getWeatherViewWeatherKind(weather.realTime.weatherKind), dayTime);
    }

    public static String getEntityWeatherKind(@WeatherView.WeatherKindRule int weatherKind) {
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLEAR:
                return WeatherHelper.KIND_CLEAR;

            case WeatherView.WEATHER_KIND_CLOUDY:
                return WeatherHelper.KIND_CLOUDY;

            case WeatherView.WEATHER_KIND_CLOUD:
                return WeatherHelper.KIND_PARTLY_CLOUDY;

            case WeatherView.WEATHER_KIND_FOG:
                return WeatherHelper.KIND_FOG;

            case WeatherView.WEATHER_KIND_HAIL:
                return WeatherHelper.KIND_HAIL;

            case WeatherView.WEATHER_KIND_HAZE:
                return WeatherHelper.KIND_HAZE;

            case WeatherView.WEATHER_KIND_RAINY:
                return WeatherHelper.KIND_RAIN;

            case WeatherView.WEATHER_KIND_SLEET:
                return WeatherHelper.KIND_SLEET;

            case WeatherView.WEATHER_KIND_SNOW:
                return WeatherHelper.KIND_SNOW;

            case WeatherView.WEATHER_KIND_THUNDERSTORM:
                return WeatherHelper.KIND_THUNDERSTORM;

            case WeatherView.WEATHER_KIND_THUNDER:
                return WeatherHelper.KIND_THUNDER;

            case WeatherView.WEATHER_KIND_WIND:
                return WeatherHelper.KIND_WIND;

            case WeatherView.WEATHER_KING_NULL:
            default:
                return WeatherHelper.KIND_CLEAR;
        }
    }

    @WeatherView.WeatherKindRule
    public static int getWeatherViewWeatherKind(String weatherKind) {
        switch (weatherKind) {
            case WeatherHelper.KIND_CLEAR:
                return WeatherView.WEATHER_KIND_CLEAR;

            case WeatherHelper.KIND_PARTLY_CLOUDY:
                return WeatherView.WEATHER_KIND_CLOUD;

            case WeatherHelper.KIND_CLOUDY:
                return WeatherView.WEATHER_KIND_CLOUDY;

            case WeatherHelper.KIND_RAIN:
                return WeatherView.WEATHER_KIND_RAINY;

            case WeatherHelper.KIND_SNOW:
                return WeatherView.WEATHER_KIND_SNOW;

            case WeatherHelper.KIND_WIND:
                return WeatherView.WEATHER_KIND_WIND;

            case WeatherHelper.KIND_FOG:
                return WeatherView.WEATHER_KIND_FOG;

            case WeatherHelper.KIND_HAZE:
                return WeatherView.WEATHER_KIND_HAZE;

            case WeatherHelper.KIND_SLEET:
                return WeatherView.WEATHER_KIND_RAINY;

            case WeatherHelper.KIND_HAIL:
                return WeatherView.WEATHER_KIND_HAIL;

            case WeatherHelper.KIND_THUNDER:
                return WeatherView.WEATHER_KIND_THUNDER;

            case WeatherHelper.KIND_THUNDERSTORM:
                return WeatherView.WEATHER_KIND_THUNDERSTORM;
        }
        return WeatherView.WEATHER_KIND_CLEAR;
    }
}
