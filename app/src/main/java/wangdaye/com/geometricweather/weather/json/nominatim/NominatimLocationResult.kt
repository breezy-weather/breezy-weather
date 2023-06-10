package wangdaye.com.geometricweather.weather.json.nominatim

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Nominatim location result.
 */
@Serializable
data class NominatimLocationResult(
    @SerialName("place_id") val placeId: Int,
    val lat: Double,
    val lon: Double,
    @SerialName("display_name") val displayName: String,
    val address: NominatimLocationAddress
)
