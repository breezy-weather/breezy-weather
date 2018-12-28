package wangdaye.com.geometricweather.ui.widget.weatherView;

import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Size;

/**
 * Weather view.
 *
 * This view is used to draw the weather phenomenon.
 *
 * */

public interface WeatherView {

    int WEATHER_KING_NULL = 0;
    int WEATHER_KIND_CLEAR_DAY = 1;
    int WEATHER_KIND_CLEAR_NIGHT = 2;
    int WEATHER_KIND_CLOUD_DAY = 3;
    int WEATHER_KIND_CLOUD_NIGHT = 4;
    int WEATHER_KIND_CLOUDY = 5;
    int WEATHER_KIND_RAINY_DAY = 6;
    int WEATHER_KIND_RAINY_NIGHT = 7;
    int WEATHER_KIND_SNOW_DAY = 8;
    int WEATHER_KIND_SNOW_NIGHT = 9;
    int WEATHER_KIND_SLEET_DAY = 10;
    int WEATHER_KIND_SLEET_NIGHT = 11;
    int WEATHER_KIND_HAIL_DAY = 12;
    int WEATHER_KIND_HAIL_NIGHT = 13;
    int WEATHER_KIND_FOG = 14;
    int WEATHER_KIND_HAZE = 15;
    int WEATHER_KIND_THUNDER = 16;
    int WEATHER_KIND_THUNDERSTORM = 17;
    int WEATHER_KIND_WIND = 18;

    @IntDef({
            WEATHER_KING_NULL,
            WEATHER_KIND_CLEAR_DAY, WEATHER_KIND_CLEAR_NIGHT,
            WEATHER_KIND_CLOUD_DAY, WEATHER_KIND_CLOUD_NIGHT,
            WEATHER_KIND_CLOUDY,
            WEATHER_KIND_RAINY_DAY, WEATHER_KIND_RAINY_NIGHT,
            WEATHER_KIND_SNOW_DAY, WEATHER_KIND_SNOW_NIGHT,
            WEATHER_KIND_SLEET_DAY, WEATHER_KIND_SLEET_NIGHT,
            WEATHER_KIND_HAIL_DAY, WEATHER_KIND_HAIL_NIGHT,
            WEATHER_KIND_FOG, WEATHER_KIND_HAZE,
            WEATHER_KIND_THUNDER, WEATHER_KIND_THUNDERSTORM,
            WEATHER_KIND_WIND})
    @interface WeatherKindRule {}

    void setWeather(@WeatherView.WeatherKindRule int weatherKind);

    void onClick();

    void onScroll(int scrollY);

    @WeatherView.WeatherKindRule
    int getWeatherKind();

    // primary color * 1, chart colors * 2.
    @ColorInt
    @Size(3)
    int[] getThemeColors();

    @ColorInt
    int getBackgroundColor();

    int getFirstCardMarginTop();
}
