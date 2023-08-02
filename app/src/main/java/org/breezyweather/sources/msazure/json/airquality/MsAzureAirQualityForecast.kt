package org.breezyweather.sources.msazure.json.airquality

import kotlinx.serialization.Serializable
import org.breezyweather.common.serializer.DateSerializer
import java.util.Date


@Serializable
data class MsAzureAirQualityForecast(
    @Serializable(DateSerializer::class) val dateTime: Date,
    val pollutants: List<MsAzureAirPollutant>?
)