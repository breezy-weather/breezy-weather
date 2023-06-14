package org.breezyweather.theme.weatherView.materialWeatherView

import androidx.annotation.DrawableRes
import androidx.annotation.Size
import org.breezyweather.R
import org.breezyweather.theme.weatherView.WeatherView.WeatherKindRule
import org.breezyweather.theme.weatherView.materialWeatherView.MaterialWeatherView.WeatherAnimationImplementor

object WeatherImplementorFactory {

    @JvmStatic
    fun getWeatherImplementor(
        @WeatherKindRule weatherKind: Int,
        daytime: Boolean,
        @Size(2) sizes: IntArray?
    ): WeatherAnimationImplementor? = when (weatherKind) {
        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLEAR -> if (daytime) {
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.SunImplementor(
                sizes
            )
        } else {
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.MeteorShowerImplementor(
                sizes
            )
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLOUDY ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor(
                sizes,
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_CLOUDY,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLOUD ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor(
                sizes,
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_CLOUD,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_FOG ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor(
                sizes,
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_FOG,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_HAIL ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.HailImplementor(
                sizes,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_HAZE ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor(
                sizes,
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_HAZE,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_RAINY ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor(
                sizes,
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.TYPE_RAIN,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_SNOW ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.SnowImplementor(
                sizes,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_THUNDERSTORM ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor(
                sizes,
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.TYPE_THUNDERSTORM,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_THUNDER ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor(
                sizes,
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.CloudImplementor.TYPE_THUNDER,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_WIND ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.WindImplementor(
                sizes,
                daytime
            )

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_SLEET ->
            org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor(
                sizes,
                org.breezyweather.theme.weatherView.materialWeatherView.implementor.RainImplementor.TYPE_SLEET,
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
        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLEAR -> if (daylight) {
            R.drawable.weather_background_clear_day
        } else {
            R.drawable.weather_background_clear_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLOUD -> if (daylight) {
            R.drawable.weather_background_partly_cloudy_day
        } else {
            R.drawable.weather_background_partly_cloudy_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_CLOUDY -> if (daylight) {
            R.drawable.weather_background_cloudy_day
        } else {
            R.drawable.weather_background_cloudy_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_FOG -> if (daylight) {
            R.drawable.weather_background_fog_day
        } else {
            R.drawable.weather_background_fog_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_HAIL -> if (daylight) {
            R.drawable.weather_background_hail_day
        } else {
            R.drawable.weather_background_hail_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_HAZE -> if (daylight) {
            R.drawable.weather_background_haze_day
        } else {
            R.drawable.weather_background_haze_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_RAINY -> if (daylight) {
            R.drawable.weather_background_rain_day
        } else {
            R.drawable.weather_background_rain_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_SLEET -> if (daylight) {
            R.drawable.weather_background_sleet_day
        } else {
            R.drawable.weather_background_sleet_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_SNOW -> if (daylight) {
            R.drawable.weather_background_snow_day
        } else {
            R.drawable.weather_background_snow_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_THUNDER,
        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_THUNDERSTORM -> if (daylight) {
            R.drawable.weather_background_thunder_day
        } else {
            R.drawable.weather_background_thunder_night
        }

        org.breezyweather.theme.weatherView.WeatherView.WEATHER_KIND_WIND -> if (daylight) {
            R.drawable.weather_background_wind_day
        } else {
            R.drawable.weather_background_wind_night
        }

        else ->
            R.drawable.weather_background_default
    }
}