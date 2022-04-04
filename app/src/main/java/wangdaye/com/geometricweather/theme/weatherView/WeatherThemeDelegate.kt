package wangdaye.com.geometricweather.theme.weatherView

import android.content.Context
import android.view.Window
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.Size

interface WeatherThemeDelegate {

    fun getWeatherView(context: Context): WeatherView

    /**
     * @return colors[] {
     * theme color,
     * color of daytime chart line,
     * color of nighttime chart line
     * }
     *
     */
    @ColorInt
    @Size(3)
    fun getThemeColors(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): IntArray

    @ColorInt
    fun getBackgroundColor(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): Int

    @Px
    fun getHeaderHeight(context: Context): Int

    @ColorInt
    fun getHeaderTextColor(context: Context): Int

    fun setSystemBarStyle(
        context: Context,
        window: Window,
        statusShader: Boolean,
        lightStatus: Boolean,
        navigationShader: Boolean,
        lightNavigation: Boolean
    )

    @Px
    fun getHomeCardRadius(context: Context): Float

    @Px
    fun getHomeCardElevation(context: Context): Float

    @Px
    fun getHomeCardMargins(context: Context): Int
}