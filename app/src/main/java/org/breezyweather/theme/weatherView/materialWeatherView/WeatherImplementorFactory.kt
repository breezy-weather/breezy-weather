package org.breezyweather.theme.weatherView.materialWeatherView

import androidx.annotation.DrawableRes
import androidx.annotation.Size
import org.breezyweather.R
import org.breezyweather.theme.weatherView.WeatherView
import org.breezyweather.theme.weatherView.WeatherView.WeatherKindRule
import org.breezyweather.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor
import org.breezyweather.theme.weatherView.materialWeatherView.implementor.*

object WeatherImplementorFactory {

    @JvmStatic
    fun getWeatherImplementor(
        @WeatherKindRule weatherKind: Int,
        daytime: Boolean,
        @Size(2) sizes: IntArray?
    ): WeatherAnimationImplementor? = when (weatherKind) {
        WeatherView.WEATHER_KIND_CLEAR -> if (daytime) {
            SunImplementor(
                sizes
            )
        } else {
            MeteorShowerImplementor(
                sizes
            )
        }

        WeatherView.WEATHER_KIND_CLOUDY ->
            CloudImplementor(
                sizes,
                CloudImplementor.TYPE_CLOUDY,
                daytime
            )

        WeatherView.WEATHER_KIND_CLOUD ->
            CloudImplementor(
                sizes,
                CloudImplementor.TYPE_CLOUD,
                daytime
            )

        WeatherView.WEATHER_KIND_FOG ->
            CloudImplementor(
                sizes,
                CloudImplementor.TYPE_FOG,
                daytime
            )

        WeatherView.WEATHER_KIND_HAIL ->
            HailImplementor(
                sizes,
                daytime
            )

        WeatherView.WEATHER_KIND_HAZE ->
            CloudImplementor(
                sizes,
                CloudImplementor.TYPE_HAZE,
                daytime
            )

        WeatherView.WEATHER_KIND_RAINY ->
            RainImplementor(
                sizes,
                RainImplementor.TYPE_RAIN,
                daytime
            )

        WeatherView.WEATHER_KIND_SNOW ->
            SnowImplementor(
                sizes,
                daytime
            )

        WeatherView.WEATHER_KIND_THUNDERSTORM ->
            RainImplementor(
                sizes,
                RainImplementor.TYPE_THUNDERSTORM,
                daytime
            )

        WeatherView.WEATHER_KIND_THUNDER ->
            CloudImplementor(
                sizes,
                CloudImplementor.TYPE_THUNDER,
                daytime
            )

        WeatherView.WEATHER_KIND_WIND ->
            WindImplementor(
                sizes,
                daytime
            )

        WeatherView.WEATHER_KIND_SLEET ->
            RainImplementor(
                sizes,
                RainImplementor.TYPE_SLEET,
                daytime
            )

        else -> null
    }

    @JvmStatic
    @DrawableRes
    fun getBackgroundId(
        @WeatherKindRule weatherKind: Int,
        daylight: Boolean,
    ): Int = when (weatherKind) {
        WeatherView.WEATHER_KIND_CLEAR -> if (daylight) {
            R.drawable.weather_background_clear_day
        } else {
            R.drawable.weather_background_clear_night
        }

        WeatherView.WEATHER_KIND_CLOUD -> if (daylight) {
            R.drawable.weather_background_partly_cloudy_day
        } else {
            R.drawable.weather_background_partly_cloudy_night
        }

        WeatherView.WEATHER_KIND_CLOUDY -> if (daylight) {
            R.drawable.weather_background_cloudy_day
        } else {
            R.drawable.weather_background_cloudy_night
        }

        WeatherView.WEATHER_KIND_FOG -> if (daylight) {
            R.drawable.weather_background_fog_day
        } else {
            R.drawable.weather_background_fog_night
        }

        WeatherView.WEATHER_KIND_HAIL -> if (daylight) {
            R.drawable.weather_background_hail_day
        } else {
            R.drawable.weather_background_hail_night
        }

        WeatherView.WEATHER_KIND_HAZE -> if (daylight) {
            R.drawable.weather_background_haze_day
        } else {
            R.drawable.weather_background_haze_night
        }

        WeatherView.WEATHER_KIND_RAINY -> if (daylight) {
            R.drawable.weather_background_rain_day
        } else {
            R.drawable.weather_background_rain_night
        }

        WeatherView.WEATHER_KIND_SLEET -> if (daylight) {
            R.drawable.weather_background_sleet_day
        } else {
            R.drawable.weather_background_sleet_night
        }

        WeatherView.WEATHER_KIND_SNOW -> if (daylight) {
            R.drawable.weather_background_snow_day
        } else {
            R.drawable.weather_background_snow_night
        }

        WeatherView.WEATHER_KIND_THUNDER,
        WeatherView.WEATHER_KIND_THUNDERSTORM -> if (daylight) {
            R.drawable.weather_background_thunder_day
        } else {
            R.drawable.weather_background_thunder_night
        }

        WeatherView.WEATHER_KIND_WIND -> if (daylight) {
            R.drawable.weather_background_wind_day
        } else {
            R.drawable.weather_background_wind_night
        }

        else ->
            R.drawable.weather_background_default
    }
}