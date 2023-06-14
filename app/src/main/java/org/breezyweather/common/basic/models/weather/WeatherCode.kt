package org.breezyweather.common.basic.models.weather

enum class WeatherCode(val id: String) {

    CLEAR("clear"),
    PARTLY_CLOUDY("partly_cloudy"),
    CLOUDY("cloudy"),
    RAIN("rain"),
    SNOW("snow"),
    WIND("wind"),
    FOG("fog"),
    HAZE("haze"),
    SLEET("sleet"),
    HAIL("hail"),
    THUNDER("thunder"),
    THUNDERSTORM("thunderstorm");

    companion object {

        @JvmStatic
        fun getInstance(
            value: String
        ): WeatherCode {
            return with (value) {
                when {
                    equals("partly_cloudy", ignoreCase = true) -> PARTLY_CLOUDY
                    equals("cloudy", ignoreCase = true) -> CLOUDY
                    equals("rain", ignoreCase = true) -> RAIN
                    equals("snow", ignoreCase = true) -> SNOW
                    equals("wind", ignoreCase = true) -> WIND
                    equals("haze", ignoreCase = true) -> HAZE
                    equals("sleet", ignoreCase = true) -> SLEET
                    equals("hail", ignoreCase = true) -> HAIL
                    equals("thunderstorm", ignoreCase = true) -> THUNDERSTORM
                    equals("thunder", ignoreCase = true) -> THUNDER
                    else -> CLEAR
                }
            }
        }
    }

    val isPrecipitation: Boolean
        get() = this == RAIN || this == SNOW || this == SLEET || this == HAIL || this == THUNDERSTORM

    val isRain: Boolean
        get() = this == RAIN || this == SLEET || this == THUNDERSTORM

    val isSnow: Boolean
        get() = this == SNOW || this == SLEET

    val isIce: Boolean
        get() = this == HAIL
}