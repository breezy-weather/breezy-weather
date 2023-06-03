package wangdaye.com.geometricweather.common.basic.models.weather

import java.io.Serializable

/**
 * Current.
 *
 * default unit
 * [.relativeHumidity] : [RelativeHumidityUnit.PERCENT]
 * [.dewPoint] : [TemperatureUnit.C]
 * [.visibility] : [DistanceUnit.KM]
 * [.ceiling] : [DistanceUnit.KM]
 */
class Current(
    val weatherText: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: Temperature? = null,
    val precipitation: Precipitation? = null,
    val wind: Wind? = null,
    val uV: UV? = null,
    val airQuality: AirQuality? = null,
    val relativeHumidity: Float? = null,
    val pressure: Float? = null,
    val visibility: Float? = null,
    val dewPoint: Int? = null,
    val cloudCover: Int? = null,
    val ceiling: Float? = null,
    val dailyForecast: String? = null,
    val hourlyForecast: String? = null
) : Serializable
