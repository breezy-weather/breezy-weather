package org.breezyweather.theme.weatherView.materialWeatherView

import android.content.Context
import android.graphics.Color
import android.view.Window
import androidx.core.graphics.ColorUtils
import org.breezyweather.R
import org.breezyweather.theme.weatherView.WeatherThemeDelegate
import org.breezyweather.theme.weatherView.WeatherView.WeatherKindRule

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
            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLEAR -> if (daytime) {
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.SunImplementor.getThemeColor()
            } else {
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.MeteorShowerImplementor.getThemeColor()
            }

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLOUDY ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.getThemeColor(context, org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_CLOUDY, daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLOUD ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.getThemeColor(context, org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_CLOUD, daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_FOG ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.getThemeColor(context, org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_FOG, daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_HAIL ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.HailImplementor.getThemeColor(daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_HAZE ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.getThemeColor(context, org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_HAZE, daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_RAINY ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.getThemeColor(context, org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.TYPE_RAIN, daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_SLEET ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.getThemeColor(context, org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.TYPE_SLEET, daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_SNOW ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.SnowImplementor.getThemeColor(daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_THUNDERSTORM ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.getThemeColor(context, org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.TYPE_THUNDERSTORM, daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_THUNDER ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.getThemeColor(context, org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_THUNDER, daytime)

            org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_WIND ->
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.WindImplementor.getThemeColor(daytime)

            else -> Color.TRANSPARENT
        }
    }

    override fun getWeatherView(context: Context): org.breezyweather.theme.weatherView.WeatherView =
        MaterialWeatherView(context)

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
            context.resources.displayMetrics.heightPixels * 0.66
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
        org.breezyweather.common.utils.DisplayUtils.setSystemBarStyle(
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
        org.breezyweather.common.utils.DisplayUtils.dpToPx(context, 2f)

    override fun getHomeCardMargins(context: Context): Int = context
        .resources
        .getDimensionPixelSize(R.dimen.little_margin)
}