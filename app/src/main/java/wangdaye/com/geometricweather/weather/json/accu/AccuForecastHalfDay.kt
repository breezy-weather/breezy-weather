package wangdaye.com.geometricweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastHalfDay(
    val Icon: Int?,
    val IconPhrase: String?,
    val ShortPhrase: String?,
    val LongPhrase: String?,
    val PrecipitationProbability: Int?,
    val ThunderstormProbability: Int?,
    val RainProbability: Int?,
    val SnowProbability: Int?,
    val IceProbability: Int?,
    val Wind: AccuForecastWind?,
    val WindGust: AccuForecastWind?,
    val TotalLiquid: AccuValue?,
    val Rain: AccuValue?,
    val Snow: AccuValue?,
    val Ice: AccuValue?,
    val HoursOfPrecipitation: Double?,
    val HoursOfRain: Double?,
    val HoursOfSnow: Double?,
    val HoursOfIce: Double?,
    val CloudCover: Int?
)
