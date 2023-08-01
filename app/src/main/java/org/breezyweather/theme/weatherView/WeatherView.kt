package org.breezyweather.theme.weatherView

import androidx.annotation.IntDef
import org.breezyweather.theme.resource.providers.ResourceProvider

/**
 * Weather view.
 *
 * This view is used to draw the weather phenomenon.
 */
interface WeatherView {
    @IntDef(
        WEATHER_KIND_NULL,
        WEATHER_KIND_CLEAR,
        WEATHER_KIND_CLOUD,
        WEATHER_KIND_CLOUDY,
        WEATHER_KIND_RAINY,
        WEATHER_KIND_SNOW,
        WEATHER_KIND_SLEET,
        WEATHER_KIND_HAIL,
        WEATHER_KIND_FOG,
        WEATHER_KIND_HAZE,
        WEATHER_KIND_THUNDER,
        WEATHER_KIND_THUNDERSTORM,
        WEATHER_KIND_WIND
    )
    annotation class WeatherKindRule

    fun setWeather(@WeatherKindRule weatherKind: Int, daytime: Boolean, provider: ResourceProvider?)

    fun onClick()
    fun onScroll(scrollY: Int)

    @get:WeatherKindRule
    val weatherKind: Int
    fun setDrawable(drawable: Boolean)
    fun setAnimatable(animatable: Boolean)
    fun setGravitySensorEnabled(enabled: Boolean)

    companion object {
        const val WEATHER_KIND_NULL = 0
        const val WEATHER_KIND_CLEAR = 1
        const val WEATHER_KIND_CLOUD = 2
        const val WEATHER_KIND_CLOUDY = 3
        const val WEATHER_KIND_RAINY = 4
        const val WEATHER_KIND_SNOW = 5
        const val WEATHER_KIND_SLEET = 6
        const val WEATHER_KIND_HAIL = 7
        const val WEATHER_KIND_FOG = 8
        const val WEATHER_KIND_HAZE = 9
        const val WEATHER_KIND_THUNDER = 10
        const val WEATHER_KIND_THUNDERSTORM = 11
        const val WEATHER_KIND_WIND = 12
    }
}
