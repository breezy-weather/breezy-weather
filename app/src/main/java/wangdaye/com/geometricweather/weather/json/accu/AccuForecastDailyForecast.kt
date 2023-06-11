package wangdaye.com.geometricweather.weather.json.accu

import kotlinx.serialization.Serializable

@Serializable
data class AccuForecastDailyForecast(
    val EpochDate: Long,
    val Sun: AccuForecastSun?,
    val Moon: AccuForecastMoon?,
    val Temperature: AccuForecastTemperature?,
    val RealFeelTemperature: AccuForecastTemperature?,
    val RealFeelTemperatureShade: AccuForecastTemperature?,
    val HoursOfSun: Double?,
    val DegreeDaySummary: AccuForecastDegreeDaySummary?,
    val Day: AccuForecastHalfDay?,
    val Night: AccuForecastHalfDay?,
    val AirAndPollen: List<AccuForecastAirAndPollen>?,
    val Sources: List<String>?
)
