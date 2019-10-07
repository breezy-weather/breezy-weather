package wangdaye.com.geometricweather.ui.widget.weatherView;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.basic.model.weather.Weather;
import wangdaye.com.geometricweather.basic.model.weather.WeatherCode;
import wangdaye.com.geometricweather.resource.provider.ResourceProvider;
import wangdaye.com.geometricweather.settings.SettingsOptionManager;
import wangdaye.com.geometricweather.ui.widget.weatherView.circularSkyView.CircularSkyWeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.MaterialWeatherView;

/**
 * Weather view controller.
 * */

public class WeatherViewController {

    public static void setWeatherCode(@NonNull WeatherView view, @Nullable Weather weather,
                                      boolean dayTime, @NonNull ResourceProvider provider) {
        view.setWeather(getWeatherKind(weather), dayTime, provider);
    }

    public static WeatherCode getWeatherCode(@WeatherView.WeatherKindRule int weatherKind) {
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
        if (weather == null) {
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
    }/**
     * @return colors[] {
     *     theme color,
     *     color of daytime chart line,
     *     color of nighttime chart line
     * }
     *
     * */
    @ColorInt
    @Size(3)
    public static int[] getThemeColors(Context context, @NonNull Weather weather, boolean lightTheme) {
        switch (SettingsOptionManager.getInstance(context).getUiStyle()) {
            case MATERIAL:
                return MaterialWeatherView.getThemeColors(
                        context, getWeatherKind(weather), lightTheme);

            default: // circular
                return CircularSkyWeatherView.getThemeColors(context, lightTheme);
        }
    }
}
