package wangdaye.com.geometricweather.common.basic.models.weather

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
            if (value.equals("partly_cloudy", ignoreCase = true)) {
                return PARTLY_CLOUDY
            }
            if (value.equals("cloudy", ignoreCase = true)) {
                return CLOUDY
            }
            if (value.equals("rain", ignoreCase = true)) {
                return RAIN
            }
            if (value.equals("snow", ignoreCase = true)) {
                return SNOW
            }
            if (value.equals("wind", ignoreCase = true)) {
                return WIND
            }
            if (value.equals("haze", ignoreCase = true)) {
                return HAZE
            }
            if (value.equals("sleet", ignoreCase = true)) {
                return SLEET
            }
            if (value.equals("hail", ignoreCase = true)) {
                return HAIL
            }
            if (value.equals("thunderstorm", ignoreCase = true)) {
                return THUNDERSTORM
            }
            if (value.equals("thunder", ignoreCase = true)) {
                return THUNDER
            }
            return CLEAR
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