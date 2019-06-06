package wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.ui.widget.weatherView.WeatherView;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.CloudImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.HailImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.MeteorShowerImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.RainImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SnowImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.SunImplementor;
import wangdaye.com.geometricweather.ui.widget.weatherView.materialWeatherView.implementor.WindImplementor;

public class WeatherImplementorFactory {

    public static MaterialWeatherView.WeatherAnimationImplementor getWeatherImplementor(
            @WeatherView.WeatherKindRule int weatherKind, boolean daytime, @Size(2) int[] sizes) {
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLEAR:
                if (daytime) {
                    return new SunImplementor(sizes);
                } else {
                    return new MeteorShowerImplementor(sizes);
                }

            case WeatherView.WEATHER_KIND_CLOUDY:
                if (daytime) {
                    return new CloudImplementor(sizes, CloudImplementor.TYPE_CLOUDY_DAY);
                } else {
                    return new CloudImplementor(sizes, CloudImplementor.TYPE_CLOUDY_NIGHT);
                }

            case WeatherView.WEATHER_KIND_CLOUD:
                if (daytime) {
                    return new CloudImplementor(sizes, CloudImplementor.TYPE_CLOUD_DAY);
                } else {
                    return new CloudImplementor(sizes, CloudImplementor.TYPE_CLOUD_NIGHT);
                }

            case WeatherView.WEATHER_KIND_FOG:
                return new CloudImplementor(sizes, CloudImplementor.TYPE_FOG);

            case WeatherView.WEATHER_KIND_HAIL:
                if (daytime) {
                    return new HailImplementor(sizes, HailImplementor.TYPE_HAIL_DAY);
                } else {
                    return new HailImplementor(sizes, HailImplementor.TYPE_HAIL_NIGHT);
                }

            case WeatherView.WEATHER_KIND_HAZE:
                return new CloudImplementor(sizes, CloudImplementor.TYPE_HAZE);

            case WeatherView.WEATHER_KIND_RAINY:
                if (daytime) {
                    return new RainImplementor(sizes, RainImplementor.TYPE_RAIN_DAY);
                } else {
                    return new RainImplementor(sizes, RainImplementor.TYPE_RAIN_NIGHT);
                }

            case WeatherView.WEATHER_KIND_SNOW:
                if (daytime) {
                    return new SnowImplementor(sizes, SnowImplementor.TYPE_SNOW_DAY);
                } else {
                    return new SnowImplementor(sizes, SnowImplementor.TYPE_SNOW_NIGHT);
                }

            case WeatherView.WEATHER_KIND_THUNDERSTORM:
                return new RainImplementor(sizes, RainImplementor.TYPE_THUNDERSTORM);

            case WeatherView.WEATHER_KIND_THUNDER:
                return new CloudImplementor(sizes, CloudImplementor.TYPE_THUNDER);

            case WeatherView.WEATHER_KIND_WIND:
                return new WindImplementor(sizes);

            case WeatherView.WEATHER_KIND_SLEET:
                if (daytime) {
                    return new RainImplementor(sizes, RainImplementor.TYPE_SLEET_DAY);
                } else {
                    return new RainImplementor(sizes, RainImplementor.TYPE_SLEET_NIGHT);
                }

            default:
            case WeatherView.WEATHER_KING_NULL:
                return null;
        }
    }
    
    @ColorInt
    public static int getWeatherThemeColor(Context context,
                                    @WeatherView.WeatherKindRule int weatherKind, 
                                    boolean daytime) {
        int color = Color.TRANSPARENT;
        switch (weatherKind) {
            case WeatherView.WEATHER_KIND_CLEAR:
                if (daytime) {
                    color = SunImplementor.getThemeColor();
                } else {
                    color = MeteorShowerImplementor.getThemeColor();
                }
                break;

            case WeatherView.WEATHER_KIND_CLOUDY:
                if (daytime) {
                    color = CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUDY_DAY);
                } else {
                    color = CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUDY_NIGHT);
                }
                break;

            case WeatherView.WEATHER_KIND_CLOUD:
                if (daytime) {
                    color = CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUD_DAY);
                } else {
                    color = CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUD_NIGHT);
                }
                break;

            case WeatherView.WEATHER_KIND_FOG:
                color = CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_FOG);
                break;

            case WeatherView.WEATHER_KIND_HAIL:
                if (daytime) {
                    color = HailImplementor.getThemeColor(context, HailImplementor.TYPE_HAIL_DAY);
                } else {
                    color = HailImplementor.getThemeColor(context, HailImplementor.TYPE_HAIL_NIGHT);
                }
                break;

            case WeatherView.WEATHER_KIND_HAZE:
                color = CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_HAZE);
                break;

            case WeatherView.WEATHER_KIND_RAINY:
                if (daytime) {
                    color = RainImplementor.getThemeColor(context, RainImplementor.TYPE_RAIN_DAY);
                } else {
                    color = RainImplementor.getThemeColor(context, RainImplementor.TYPE_RAIN_NIGHT);
                }
                break;

            case WeatherView.WEATHER_KIND_SLEET:
                if (daytime) {
                    color = RainImplementor.getThemeColor(context, RainImplementor.TYPE_SLEET_DAY);
                } else {
                    color = RainImplementor.getThemeColor(context, RainImplementor.TYPE_SLEET_NIGHT);
                }
                break;

            case WeatherView.WEATHER_KIND_SNOW:
                if (daytime) {
                    color = SnowImplementor.getThemeColor(context, SnowImplementor.TYPE_SNOW_DAY);
                } else {
                    color = SnowImplementor.getThemeColor(context, SnowImplementor.TYPE_SNOW_NIGHT);
                }
                break;

            case WeatherView.WEATHER_KIND_THUNDERSTORM:
                color = RainImplementor.getThemeColor(context, RainImplementor.TYPE_THUNDERSTORM);
                break;

            case WeatherView.WEATHER_KIND_THUNDER:
                color = CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_THUNDER);
                break;

            case WeatherView.WEATHER_KIND_WIND:
                color = WindImplementor.getThemeColor();
                break;

            default:
            case WeatherView.WEATHER_KING_NULL:
                break;
        }
        return color;
    }
}
