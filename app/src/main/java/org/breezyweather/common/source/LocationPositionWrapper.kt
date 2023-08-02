package org.breezyweather.common.source

import java.util.TimeZone

// location.
data class LocationPositionWrapper(
    val latitude: Float,
    val longitude: Float,
    val timeZone: TimeZone? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val province: String? = null,
    val provinceCode: String? = null,
    val city: String? = null,
    val district: String? = null,
)
