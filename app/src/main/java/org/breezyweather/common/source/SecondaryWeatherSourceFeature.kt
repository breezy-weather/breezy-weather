package org.breezyweather.common.source

enum class SecondaryWeatherSourceFeature(
    val id: String
) {
    FEATURE_AIR_QUALITY("airQuality"),
    FEATURE_ALLERGEN("allergen"),
    FEATURE_MINUTELY("minutely"),
    FEATURE_ALERT("alert");

    companion object {

        fun getInstance(
            value: String
        ) = when (value) {
            "airQuality" -> FEATURE_AIR_QUALITY
            "allergen" -> FEATURE_ALLERGEN
            "minutely" -> FEATURE_MINUTELY
            "alert" -> FEATURE_ALERT
            else -> null
        }
    }
}