package org.breezyweather.theme.weatherView

import org.breezyweather.common.basic.models.weather.Weather
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.theme.weatherView.WeatherView.WeatherKindRule

object WeatherViewController {

    fun getWeatherCode(
        @WeatherKindRule weatherKind: Int
    ): WeatherCode = when (weatherKind) {
        WeatherView.WEATHER_KIND_CLOUDY -> WeatherCode.CLOUDY
        WeatherView.WEATHER_KIND_CLOUD -> WeatherCode.PARTLY_CLOUDY
        WeatherView.WEATHER_KIND_FOG -> WeatherCode.FOG
        WeatherView.WEATHER_KIND_HAIL -> WeatherCode.HAIL
        WeatherView.WEATHER_KIND_HAZE -> WeatherCode.HAZE
        WeatherView.WEATHER_KIND_RAINY -> WeatherCode.RAIN
        WeatherView.WEATHER_KIND_SLEET -> WeatherCode.SLEET
        WeatherView.WEATHER_KIND_SNOW -> WeatherCode.SNOW
        WeatherView.WEATHER_KIND_THUNDERSTORM -> WeatherCode.THUNDERSTORM
        WeatherView.WEATHER_KIND_THUNDER -> WeatherCode.THUNDER
        WeatherView.WEATHER_KIND_WIND -> WeatherCode.WIND
        else -> WeatherCode.CLEAR
    }

    @WeatherKindRule
    fun getWeatherKind(weather: Weather?): Int = getWeatherKind(weather?.current?.weatherCode)

    @WeatherKindRule
    fun getWeatherKind(weatherCode: WeatherCode?): Int = when (weatherCode) {
        WeatherCode.CLEAR -> WeatherView.WEATHER_KIND_CLEAR
        WeatherCode.PARTLY_CLOUDY -> WeatherView.WEATHER_KIND_CLOUD
        WeatherCode.CLOUDY -> WeatherView.WEATHER_KIND_CLOUDY
        WeatherCode.RAIN -> WeatherView.WEATHER_KIND_RAINY
        WeatherCode.SNOW -> WeatherView.WEATHER_KIND_SNOW
        WeatherCode.WIND -> WeatherView.WEATHER_KIND_WIND
        WeatherCode.FOG -> WeatherView.WEATHER_KIND_FOG
        WeatherCode.HAZE -> WeatherView.WEATHER_KIND_HAZE
        WeatherCode.SLEET -> WeatherView.WEATHER_KIND_SLEET
        WeatherCode.HAIL -> WeatherView.WEATHER_KIND_HAIL
        WeatherCode.THUNDER -> WeatherView.WEATHER_KIND_THUNDER
        WeatherCode.THUNDERSTORM -> WeatherView.WEATHER_KIND_THUNDERSTORM
        else -> WeatherView.WEATHER_KIND_CLEAR
    }
}
