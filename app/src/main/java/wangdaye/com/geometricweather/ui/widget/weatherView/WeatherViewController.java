package wangdaye.com.geometricweather.ui.widget.weatherView;

import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Weather view controller.
 * */

public class WeatherViewController {

    public static void setWeatherViewWeatherKind(WeatherView view, Weather weather, boolean dayTime) {
        view.setWeather(getWeatherViewWeatherKind(weather.realTime.weatherKind, dayTime));
    }

    public static String getEntityWeatherKind(@WeatherView.WeatherKindRule int weatherKind) {
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLEAR_DAY:
            case WeatherView.WEATHER_KIND_CLEAR_NIGHT:
                return WeatherHelper.KIND_CLEAR;

            case WeatherView.WEATHER_KIND_CLOUDY:
                return WeatherHelper.KIND_CLOUDY;

            case WeatherView.WEATHER_KIND_CLOUD_DAY:
            case WeatherView.WEATHER_KIND_CLOUD_NIGHT:
                return WeatherHelper.KIND_PARTLY_CLOUDY;

            case WeatherView.WEATHER_KIND_FOG:
                return WeatherHelper.KIND_FOG;

            case WeatherView.WEATHER_KIND_HAIL_DAY:
            case WeatherView.WEATHER_KIND_HAIL_NIGHT:
                return WeatherHelper.KIND_HAIL;

            case WeatherView.WEATHER_KIND_HAZE:
                return WeatherHelper.KIND_HAZE;

            case WeatherView.WEATHER_KIND_RAINY_DAY:
            case WeatherView.WEATHER_KIND_RAINY_NIGHT:
                return WeatherHelper.KIND_RAIN;

            case WeatherView.WEATHER_KIND_SLEET_DAY:
            case WeatherView.WEATHER_KIND_SLEET_NIGHT:
                return WeatherHelper.KIND_SLEET;

            case WeatherView.WEATHER_KIND_SNOW_DAY:
            case WeatherView.WEATHER_KIND_SNOW_NIGHT:
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
    public static int getWeatherViewWeatherKind(String weatherKind, boolean isDayTime) {
        switch (weatherKind) {
            case WeatherHelper.KIND_CLEAR:
                if (isDayTime) {
                    return WeatherView.WEATHER_KIND_CLEAR_DAY;
                } else {
                    return WeatherView.WEATHER_KIND_CLEAR_NIGHT;
                }

            case WeatherHelper.KIND_PARTLY_CLOUDY:
                if (isDayTime) {
                    return WeatherView.WEATHER_KIND_CLOUD_DAY;
                } else {
                    return WeatherView.WEATHER_KIND_CLOUD_NIGHT;
                }

            case WeatherHelper.KIND_CLOUDY:
                return WeatherView.WEATHER_KIND_CLOUDY;

            case WeatherHelper.KIND_RAIN:
                if (isDayTime) {
                    return WeatherView.WEATHER_KIND_RAINY_DAY;
                } else {
                    return WeatherView.WEATHER_KIND_RAINY_NIGHT;
                }

            case WeatherHelper.KIND_SNOW:
                if (isDayTime) {
                    return WeatherView.WEATHER_KIND_SNOW_DAY;
                } else {
                    return WeatherView.WEATHER_KIND_SNOW_NIGHT;
                }

            case WeatherHelper.KIND_WIND:
                return WeatherView.WEATHER_KIND_WIND;

            case WeatherHelper.KIND_FOG:
                return WeatherView.WEATHER_KIND_FOG;

            case WeatherHelper.KIND_HAZE:
                return WeatherView.WEATHER_KIND_HAZE;

            case WeatherHelper.KIND_SLEET:
                if (isDayTime) {
                    return WeatherView.WEATHER_KIND_RAINY_DAY;
                } else {
                    return WeatherView.WEATHER_KIND_RAINY_NIGHT;
                }

            case WeatherHelper.KIND_HAIL:
                if (isDayTime) {
                    return WeatherView.WEATHER_KIND_HAIL_DAY;
                } else {
                    return WeatherView.WEATHER_KIND_HAIL_NIGHT;
                }

            case WeatherHelper.KIND_THUNDER:
                return WeatherView.WEATHER_KIND_THUNDER;

            case WeatherHelper.KIND_THUNDERSTORM:
                return WeatherView.WEATHER_KIND_THUNDERSTORM;
        }
        return WeatherView.WEATHER_KIND_CLEAR_DAY;
    }
}
