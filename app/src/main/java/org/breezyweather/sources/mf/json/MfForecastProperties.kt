package org.breezyweather.sources.mf.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MfForecastProperties(
    val country: String,
    @SerialName("daily_forecast") val dailyForecast: List<MfForecastDaily>?,
    val forecast: List<MfForecastHourly>?,
    @SerialName("french_department") val frenchDepartment: String?,
    val name: String,
    @SerialName("probability_forecast") val probabilityForecast: List<MfForecastProbability>?,
    val timezone: String?
)
