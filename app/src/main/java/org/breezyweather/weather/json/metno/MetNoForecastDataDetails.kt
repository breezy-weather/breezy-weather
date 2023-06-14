package org.breezyweather.weather.json.metno

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetNoForecastDataDetails(
    @SerialName("air_pressure_at_sea_level") val airPressureAtSeaLevel: Float?,
    @SerialName("air_temperature") val airTemperature: Float?,
    @SerialName("dew_point_temperature") val dewPointTemperature: Float?,
    @SerialName("precipitation_amount") val precipitationAmount: Float?,
    @SerialName("probability_of_precipitation") val probabilityOfPrecipitation: Float?,
    @SerialName("probability_of_thunder") val probabilityOfThunder: Float?,
    @SerialName("relative_humidity") val relativeHumidity: Float?,
    @SerialName("ultraviolet_index_clear_sky") val ultravioletIndexClearSky: Float?,
    @SerialName("wind_from_direction") val windFromDirection: Float?,
    @SerialName("wind_speed") val windSpeed: Float?
)
