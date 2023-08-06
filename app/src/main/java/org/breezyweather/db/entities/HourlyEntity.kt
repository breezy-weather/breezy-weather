/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.db.entities

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import org.breezyweather.common.basic.models.weather.WeatherCode
import org.breezyweather.db.converters.WeatherCodeConverter
import java.util.Date

/**
 * Hourly entity.
 * [Hourly].
 */
@Entity
data class HourlyEntity(
    @field:Id var id: Long = 0,

    var formattedId: String,
    var date: Date,
    var daylight: Boolean = true,
    var weatherText: String? = null,
    @field:Convert(
        converter = WeatherCodeConverter::class,
        dbType = String::class
    ) var weatherCode: WeatherCode? = null,

    var temperature: Float? = null,
    var realFeelTemperature: Float? = null,
    var realFeelShaderTemperature: Float? = null,
    var apparentTemperature: Float? = null,
    var windChillTemperature: Float? = null,
    var wetBulbTemperature: Float? = null,

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

    var windDegree: Float? = null,
    var windSpeed: Float? = null,
    var windGusts: Float? = null,

    var pm25: Float? = null,
    var pm10: Float? = null,
    var so2: Float? = null,
    var no2: Float? = null,
    var o3: Float? = null,
    var co: Float? = null,

    // uv.
    var uvIndex: Float? = null,

    // details
    var relativeHumidity: Float? = null,
    var dewPoint: Float? = null,
    var pressure: Float? = null,
    var cloudCover: Int? = null,
    var visibility: Float? = null
)