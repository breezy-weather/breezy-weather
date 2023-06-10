package wangdaye.com.geometricweather.weather.json.nominatim

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimLocationAddress(
    val city: String?,
    @SerialName("state_district") val stateDistrict: String?,
    val state: String?,
    val country: String,
    @SerialName("country_code") val countryCode: String
)
