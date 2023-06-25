package org.breezyweather.theme.weatherView.materialWeatherView

import android.content.Context
import android.graphics.Color
import android.view.Window
import androidx.core.graphics.ColorUtils
import org.breezyweather.R
import org.breezyweather.common.utils.DisplayUtils
import org.breezyweather.theme.weatherView.WeatherThemeDelegate
import org.breezyweather.theme.weatherView.WeatherView
import org.breezyweather.theme.weatherView.WeatherView.WeatherKindRule
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.*

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

            WeatherView.WEATHER_KIND_CLOUDY ->
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUDY, daytime)

            WeatherView.WEATHER_KIND_CLOUD ->
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_CLOUD, daytime)

            WeatherView.WEATHER_KIND_FOG ->
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_FOG, daytime)

            WeatherView.WEATHER_KIND_HAIL ->
                HailImplementor.getThemeColor(daytime)

            WeatherView.WEATHER_KIND_HAZE ->
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_HAZE, daytime)

            WeatherView.WEATHER_KIND_RAINY ->
                RainImplementor.getThemeColor(context, RainImplementor.TYPE_RAIN, daytime)

            WeatherView.WEATHER_KIND_SLEET ->
                RainImplementor.getThemeColor(context, RainImplementor.TYPE_SLEET, daytime)

            WeatherView.WEATHER_KIND_SNOW ->
                SnowImplementor.getThemeColor(daytime)

            WeatherView.WEATHER_KIND_THUNDERSTORM ->
                RainImplementor.getThemeColor(context, RainImplementor.TYPE_THUNDERSTORM, daytime)

            WeatherView.WEATHER_KIND_THUNDER ->
                CloudImplementor.getThemeColor(context, CloudImplementor.TYPE_THUNDER, daytime)

            WeatherView.WEATHER_KIND_WIND ->
                WindImplementor.getThemeColor(daytime)

            else -> Color.TRANSPARENT
        }
    }

    override fun getWeatherView(context: Context): WeatherView = MaterialWeatherView(context)

    override fun getThemeColors(
        context: Context,
        weatherKind: Int,
        daylight: Boolean,
    ): IntArray {
        var color = innerGetBackgroundColor(context, weatherKind, daylight)
        if (!daylight) {
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
            context.resources.displayMetrics.heightPixels * 0.5 // 0.66
    ).toInt()

    override fun getHeaderTextColor(context: Context): Int {
        return Color.WHITE
    }

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

    override fun getHomeCardRadius(context: Context): Float = context
        .resources
        .getDimension(R.dimen.material3_card_list_item_corner_radius)

    override fun getHomeCardElevation(context: Context): Float =
        DisplayUtils.dpToPx(context, 2f)

    override fun getHomeCardMargins(context: Context): Int = context
        .resources
        .getDimensionPixelSize(R.dimen.little_margin)
}