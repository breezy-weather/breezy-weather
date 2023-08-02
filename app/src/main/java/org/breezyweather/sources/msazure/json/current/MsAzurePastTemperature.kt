package org.breezyweather.sources.msazure.json.current

import kotlinx.serialization.Serializable
import org.breezyweather.sources.msazure.json.MsAzureTemperatureRange

@Serializable
data class MsAzurePastTemperature(
    val pastTwentyFourHours: MsAzureTemperatureRange?
)