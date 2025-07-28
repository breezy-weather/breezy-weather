package org.breezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsLatestObservation(
    val temperature: Double?,
    val windSpeed: Double?,
    val windDirection: Double?,
    val precipitation: Double?,
    val humidity: Double?,
    val maxWindGust: Double?,
    val pressure: Double?,
    val icon: String?,
    val dewPoint: Double?,
    val cloudCover: Double?,
)
