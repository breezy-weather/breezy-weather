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
            if (value.lowercase().contains("partly_cloudy")) {
                return PARTLY_CLOUDY
            }
            if (value.lowercase().contains("cloudy")) {
                return CLOUDY
            }
            if (value.lowercase().contains("rain")) {
                return RAIN
            }
            if (value.lowercase().contains("snow")) {
                return SNOW
            }
            if (value.lowercase().contains("wind")) {
                return WIND
            }
            if (value.lowercase().contains("haze")) {
                return HAZE
            }
            if (value.lowercase().contains("sleet")) {
                return SLEET
            }
            if (value.lowercase().contains("hail")) {
                return HAIL
            }
            if (value.lowercase().contains("thunderstorm")) {
                return THUNDERSTORM
            }
            if (value.lowercase().contains("thunder")) {
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