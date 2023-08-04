package org.breezyweather.sources.msazure.json.geocoding

import kotlinx.serialization.Serializable

@Serializable
data class MsAzureAddress(
    val countryRegion: MsAzureCountry?,
    val adminDistricts: List<MsAzureAdminDistrict>?,
    val locality: String?,
    val postalCode: String?
)