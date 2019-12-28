package wangdaye.com.geometricweather.ui.widget.weatherView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.Size;

import wangdaye.com.geometricweather.resource.provider.ResourceProvider;

/**
 * Weather view.
 *
 * This view is used to draw the weather phenomenon.
 *
 * */

public interface WeatherView {

    int WEATHER_KING_NULL = 0;
    int WEATHER_KIND_CLEAR = 1;
    int WEATHER_KIND_CLOUD = 2;
    int WEATHER_KIND_CLOUDY = 3;
    int WEATHER_KIND_RAINY = 4;
    int WEATHER_KIND_SNOW = 5;
    int WEATHER_KIND_SLEET = 6;
    int WEATHER_KIND_HAIL = 7;
    int WEATHER_KIND_FOG = 8;
    int WEATHER_KIND_HAZE = 9;
    int WEATHER_KIND_THUNDER = 10;
    int WEATHER_KIND_THUNDERSTORM = 11;
    int WEATHER_KIND_WIND = 12;

    @IntDef({
            WEATHER_KING_NULL, WEATHER_KIND_CLEAR, WEATHER_KIND_CLOUD, WEATHER_KIND_CLOUDY,
            WEATHER_KIND_RAINY, WEATHER_KIND_SNOW, WEATHER_KIND_SLEET, WEATHER_KIND_HAIL,
            WEATHER_KIND_FOG, WEATHER_KIND_HAZE, WEATHER_KIND_THUNDER, WEATHER_KIND_THUNDERSTORM,
            WEATHER_KIND_WIND
    }) @interface WeatherKindRule {}

    void setWeather(@WeatherView.WeatherKindRule int weatherKind, boolean daytime,
                    @Nullable ResourceProvider provider);

    void onClick();

    void onScroll(int scrollY);

    @WeatherView.WeatherKindRule
    int getWeatherKind();

    /**
     * @return colors[] {
     *     theme color,
     *     color of daytime chart line,
     *     color of nighttime chart line
     * }
     *
     * */
    @ColorInt @Size(3)
    int[] getThemeColors(boolean lightTheme);

    @ColorInt
    int getBackgroundColor();

    @Px
    int getHeaderHeight();

    void setDrawable(boolean drawable);

    void setGravitySensorEnabled(boolean enabled);
}
