package org.breezyweather.db.entities

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.db.converters.WeatherCodeConverter
import java.util.Date

/**
 * Daily entity.
 *
 * [Daily].
 */
@Entity
data class DailyEntity(
    @field:Id var id: Long = 0,

    var formattedId: String,
    var date: Date,

    // daytime.
    var daytimeWeatherText: String? = null,
    var daytimeWeatherPhase: String? = null,
    @field:Convert(
        converter = WeatherCodeConverter::class,
        dbType = String::class
    ) var daytimeWeatherCode: WeatherCode? = null,

    var daytimeTemperature: Float? = null,
    var daytimeRealFeelTemperature: Float? = null,
    var daytimeRealFeelShaderTemperature: Float? = null,
    var daytimeApparentTemperature: Float? = null,
    var daytimeWindChillTemperature: Float? = null,
    var daytimeWetBulbTemperature: Float? = null,

    var daytimeTotalPrecipitation: Float? = null,
    var daytimeThunderstormPrecipitation: Float? = null,
    var daytimeRainPrecipitation: Float? = null,
    var daytimeSnowPrecipitation: Float? = null,
    var daytimeIcePrecipitation: Float? = null,

    var daytimeTotalPrecipitationProbability: Float? = null,
    var daytimeThunderstormPrecipitationProbability: Float? = null,
    var daytimeRainPrecipitationProbability: Float? = null,
    var daytimeSnowPrecipitationProbability: Float? = null,
    var daytimeIcePrecipitationProbability: Float? = null,

    var daytimeTotalPrecipitationDuration: Float? = null,
    var daytimeThunderstormPrecipitationDuration: Float? = null,
    var daytimeRainPrecipitationDuration: Float? = null,
    var daytimeSnowPrecipitationDuration: Float? = null,
    var daytimeIcePrecipitationDuration: Float? = null,

    var daytimeWindDegree: Float? = null,
    var daytimeWindSpeed: Float? = null,

    var daytimeCloudCover: Int? = null,

    // nighttime.
    var nighttimeWeatherText: String? = null,
    var nighttimeWeatherPhase: String? = null,
    @field:Convert(
        converter = WeatherCodeConverter::class,
        dbType = String::class
    ) var nighttimeWeatherCode: WeatherCode? = null,

    var nighttimeTemperature: Float? = null,
    var nighttimeRealFeelTemperature: Float? = null,
    var nighttimeRealFeelShaderTemperature: Float? = null,
    var nighttimeApparentTemperature: Float? = null,
    var nighttimeWindChillTemperature: Float? = null,
    var nighttimeWetBulbTemperature: Float? = null,

    var nighttimeTotalPrecipitation: Float? = null,
    var nighttimeThunderstormPrecipitation: Float? = null,
    var nighttimeRainPrecipitation: Float? = null,
    var nighttimeSnowPrecipitation: Float? = null,
    var nighttimeIcePrecipitation: Float? = null,

    var nighttimeTotalPrecipitationProbability: Float? = null,
    var nighttimeThunderstormPrecipitationProbability: Float? = null,
    var nighttimeRainPrecipitationProbability: Float? = null,
    var nighttimeSnowPrecipitationProbability: Float? = null,
    var nighttimeIcePrecipitationProbability: Float? = null,
    var nighttimeTotalPrecipitationDuration: Float? = null,

    var nighttimeThunderstormPrecipitationDuration: Float? = null,
    var nighttimeRainPrecipitationDuration: Float? = null,
    var nighttimeSnowPrecipitationDuration: Float? = null,
    var nighttimeIcePrecipitationDuration: Float? = null,

    var nighttimeWindDegree: Float? = null,
    var nighttimeWindSpeed: Float? = null,

    var nighttimeCloudCover: Int? = null,

    var degreeDayHeating: Float? = null,
    var degreeDayCooling: Float? = null,

    // sun.
    var sunRiseDate: Date? = null,
    var sunSetDate: Date? = null,

    // moon.
    var moonRiseDate: Date? = null,
    var moonSetDate: Date? = null,

    // moon phase.
    var moonPhaseAngle: Int? = null,

    // aqi.
    var pm25: Float? = null,
    var pm10: Float? = null,
    var so2: Float? = null,
    var no2: Float? = null,
    var o3: Float? = null,
    var co: Float? = null,

    // pollen.
    var tree: Int? = null,
    var alder: Int? = null,
    var birch: Int? = null,
    var grass: Int? = null,
    var olive: Int? = null,
    var ragweed: Int? = null,
    var mugwort: Int? = null,
    var mold: Int? = null,

    // uv.
    var uvIndex: Float? = null,

    var hoursOfSun: Float? = null
)
