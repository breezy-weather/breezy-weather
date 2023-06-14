package org.breezyweather.weather.json.mf

/**
 * Mf location result.
 */
import kotlinx.serialization.Serializable

@Serializable
data class MfLocationResult(
    /**
     * insee: "69123"
     * name: "Lyon"
     * lat: 45.75889
     * lon: 4.84139
     * country: "FR"
     * admin: "Rhône-Alpes"
     * admin2: "69"
     * postCode: "69000"
     */
    val insee: String?,
    val name: String?,
    val lat: Double,
    val lon: Double,
    val country: String,
    val admin: String?,
    val admin2: String?,
    val postCode: String?
)