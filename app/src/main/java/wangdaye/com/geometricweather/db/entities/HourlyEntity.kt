package wangdaye.com.geometricweather.db.entities

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import wangdaye.com.geometricweather.common.basic.models.weather.WeatherCode
import wangdaye.com.geometricweather.common.basic.models.weather.WindDegree
import wangdaye.com.geometricweather.db.converters.WeatherCodeConverter
import wangdaye.com.geometricweather.db.converters.WindDegreeConverter
import java.util.Date

/**
 * Hourly entity.
 * [Hourly].
 */
@Entity
data class HourlyEntity(
    @field:Id var id: Long = 0,

    var cityId: String,
    var weatherSource: String,
    var date: Date,
    var daylight: Boolean = true,
    var weatherText: String? = null,
    @field:Convert(
        converter = WeatherCodeConverter::class,
        dbType = String::class
    ) var weatherCode: WeatherCode? = null,

    var temperature: Int? = null,
    var realFeelTemperature: Int? = null,
    var realFeelShaderTemperature: Int? = null,
    var apparentTemperature: Int? = null,
    var windChillTemperature: Int? = null,
    var wetBulbTemperature: Int? = null,
    var degreeDayTemperature: Int? = null,

    var totalPrecipitation: Float? = null,
    var thunderstormPrecipitation: Float? = null,
    var rainPrecipitation: Float? = null,
    var snowPrecipitation: Float? = null,
    var icePrecipitation: Float? = null,

    var totalPrecipitationProbability: Float? = null,
    var thunderstormPrecipitationProbability: Float? = null,
    var rainPrecipitationProbability: Float? = null,
    var snowPrecipitationProbability: Float? = null,
    var icePrecipitationProbability: Float? = null,

    var windDirection: String? = null,
    @field:Convert(
        converter = WindDegreeConverter::class,
        dbType = Float::class
    ) var windDegree: WindDegree? = null,
    var windSpeed: Float? = null,
    var windLevel: String? = null,

    var aqiIndex: Int? = null,
    var pm25: Float? = null,
    var pm10: Float? = null,
    var so2: Float? = null,
    var no2: Float? = null,
    var o3: Float? = null,
    var co: Float? = null,

    // pollen.
    var grassIndex: Int? = null,
    var grassLevel: Int? = null,
    var grassDescription: String? = null,
    var moldIndex: Int? = null,
    var moldLevel: Int? = null,
    var moldDescription: String? = null,
    var ragweedIndex: Int? = null,
    var ragweedLevel: Int? = null,
    var ragweedDescription: String? = null,
    var treeIndex: Int? = null,
    var treeLevel: Int? = null,
    var treeDescription: String? = null,

    // uv.
    var uvIndex: Int? = null,
    var uvLevel: String? = null,
    var uvDescription: String? = null
)