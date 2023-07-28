package org.breezyweather.sources.here.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HereNWSAlertRegion(
    @SerialName("country") val countryCode: String?,
    val countryName: String?,
    @SerialName("state") val stateCode: String?,
    val stateName: String?,
    @SerialName("province") val provinceCode: String?,
    val provinceName: String?,
    val name: String?,
    val location: HereWeatherLocation?
)
