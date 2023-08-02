package org.breezyweather.sources.msazure.json.airquality

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date


@Serializable
data class MsAzureAirQualityForecast(
    @Serializable(DateSerializer::class) @SerialName("dateTime") val time: Date,
    val pollutants: List<MsAzureAirPollutant>?
)