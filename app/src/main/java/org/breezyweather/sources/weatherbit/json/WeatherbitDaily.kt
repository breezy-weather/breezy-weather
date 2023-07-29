package org.breezyweather.sources.weatherbit.json

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class WeatherbitDaily(
    @SerialName("ts") val time: Long?,
    @SerialName("wind_gust_spd") val windGustSpeed: Float?,
    @SerialName("wind_spd") val windSpeed: Float?,
    @SerialName("wind_dir") val windDir: Int?,
    @SerialName("temp") val temperature: Float?,
    @SerialName("max_temp") val maxTemperature: Float?,
    @SerialName("min_temp") val minTemperature: Float?,
    @SerialName("high_temp") val highTemperature: Float?,
    @SerialName("low_temp") val lowTemperature: Float?,
    @SerialName("app_max_temp") val apparentMaxTemperature: Float?,
    @SerialName("app_min_temp") val apparentMinTemperature: Float?,
    @SerialName("pop") val precipitationProbability: Int?,
    @SerialName("precip") val precipitation: Float?,
    @SerialName("snow") val snow: Int?,
    @SerialName("snow_depth") val snowDepth: Int?,
    @SerialName("slp") val pressure: Float?,
    @SerialName("dewpt") val dewPoint: Float?,
    @SerialName("rh") val humidity: Float?,
    @SerialName("weather") val weather: WeatherbitWeather?,
    @SerialName("clouds_low") val cloudCoverageLow: Int?,
    @SerialName("clouds_mid") val cloudCoverageMid: Int?,
    @SerialName("clouds_hi") val cloudCoverageHigh: Int?,
    @SerialName("clouds") val cloudCoverage: Int?,
    @SerialName("vis") val visibility: Float?,
    @SerialName("uv") val uvIndex: Float?,
    @SerialName("moon_phase_lunation") val moonPhase: Float?,
    @SerialName("moonrise_ts") val moonrise: Long?,
    @SerialName("moonset_ts") val moonset: Long?,
    @SerialName("sunrise_ts") val sunrise: Long?,
    @SerialName("sunset_ts") val sunset: Long?
)