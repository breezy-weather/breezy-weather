package wangdaye.com.geometricweather.theme.weatherView.materialWeatherView

import android.content.Context
import android.graphics.Color
import android.view.Window
import androidx.core.graphics.ColorUtils
import wangdaye.com.geometricweather.common.utils.DisplayUtils
import wangdaye.com.geometricweather.theme.weatherView.WeatherThemeDelegate
import wangdaye.com.geometricweather.theme.weatherView.WeatherView
import wangdaye.com.geometricweather.theme.weatherView.WeatherView.WeatherKindRule
import wangdaye.com.geometricweather.theme.weatherView.materialWeatherView.implementor.*

class MaterialWeatherThemeDelegate: WeatherThemeDelegate {

    companion object {

        private fun getBrighterColor(color: Int): Int {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[1] = hsv[1] - 0.25f
            hsv[2] = hsv[2] + 0.25f
            return Color.HSVToColor(hsv)
        }

        private fun innerGetBackgroundColor(
            context: Context,
            @WeatherKindRule weatherKind: Int,
            daytime: Boolean
        ): Int = when (weatherKind) {
            WeatherView.WEATHER_KIND_CLEAR -> if (daytime) {
                SunImplementor.getThemeColor()
            } else {
                MeteorShowerImplementor.getThemeColor()
            }

            WeatherView.WEATHER_KIND_CLOUDY -> if (daytime) {
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUDY_DAY)
            } else {
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUDY_NIGHT)
            }

            WeatherView.WEATHER_KIND_CLOUD -> if (daytime) {
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUD_DAY)
            } else {
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUD_NIGHT)
            }

            WeatherView.WEATHER_KIND_FOG ->
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_FOG)

            WeatherView.WEATHER_KIND_HAIL -> if (daytime) {
                HailImplementor.getThemeColor(context, HailImplementor.TYPE_HAIL_DAY)
            } else {
                HailImplementor.getThemeColor(context, HailImplementor.TYPE_HAIL_NIGHT)
            }

            WeatherView.WEATHER_KIND_HAZE ->
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_HAZE)

            WeatherView.WEATHER_KIND_RAINY -> if (daytime) {
                RainImplementor.getThemeColor(context, RainImplementor.TYPE_RAIN_DAY)
            } else {
                RainImplementor.getThemeColor(context, RainImplementor.TYPE_RAIN_NIGHT)
            }

            WeatherView.WEATHER_KIND_SLEET -> if (daytime) {
                RainImplementor.getThemeColor(context, RainImplementor.TYPE_SLEET_DAY)
            } else {
                RainImplementor.getThemeColor(context, RainImplementor.TYPE_SLEET_NIGHT)
            }

            WeatherView.WEATHER_KIND_SNOW -> if (daytime) {
                SnowImplementor.getThemeColor(context, SnowImplementor.TYPE_SNOW_DAY)
            } else {
                SnowImplementor.getThemeColor(context, SnowImplementor.TYPE_SNOW_NIGHT)
            }

            WeatherView.WEATHER_KIND_THUNDERSTORM ->
                RainImplementor.getThemeColor(context, RainImplementor.TYPE_THUNDERSTORM)

            WeatherView.WEATHER_KIND_THUNDER ->
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_THUNDER)

            WeatherView.WEATHER_KIND_WIND ->
                WindImplementor.getThemeColor()

            else -> Color.TRANSPARENT
        }
    }

    override fun getWeatherView(context: Context): WeatherView = MaterialWeatherView(context)

    override fun getThemeColors(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
        lightTheme: Boolean,
    ): IntArray {
        var color = innerGetBackgroundColor(context, weatherKind, daylight)
        if (!lightTheme) {
            color = getBrighterColor(color)
        }
        return intArrayOf(
            color,
            color,
            ColorUtils.setAlphaComponent(color, (0.5 * 255).toInt())
        )
    }

    override fun getBackgroundColor(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): Int {
        return innerGetBackgroundColor(context, weatherKind, daylight)
    }

    override fun getHeaderHeight(context: Context): Int = (
            context.resources.displayMetrics.heightPixels * 0.66
    ).toInt()

    override fun setSystemBarStyle(
        context: Context,
        window: Window,
        statusShader: Boolean,
        lightStatus: Boolean,
        navigationShader: Boolean,
        lightNavigation: Boolean
    ) {
        DisplayUtils.setSystemBarStyle(
            context,
            window,
            statusShader,
            lightNavigation,
            navigationShader,
            lightNavigation
        )
    }
}