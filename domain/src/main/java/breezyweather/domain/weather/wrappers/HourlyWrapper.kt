/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package breezyweather.domain.weather.wrappers

import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Hourly
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.model.UV
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind
import java.util.Date

/**
 * Hourly wrapper that allows isDaylight to be null and completed later
 */
data class HourlyWrapper(
    val date: Date,
    val isDaylight: Boolean? = null,
    val weatherText: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: Temperature? = null,
    val precipitation: Precipitation? = null,
    val precipitationProbability: PrecipitationProbability? = null,
    val wind: Wind? = null,
    val uV: UV? = null,
    val relativeHumidity: Double? = null,
    val dewPoint: Double? = null,
    val pressure: Double? = null,
    val cloudCover: Int? = null,
    val visibility: Double? = null,
    /**
     * Sunshine duration in hours (ex: 0.5 means 30 min)
     */
    val sunshineDuration: Double? = null,
) {
    fun toHourly(
        airQuality: AirQuality? = null,
        isDaylight: Boolean? = null,
        uV: UV? = null,
    ) = Hourly(
        date = this.date,
        isDaylight = isDaylight ?: this.isDaylight ?: true,
        weatherText = this.weatherText,
        weatherCode = this.weatherCode,
        temperature = this.temperature,
        precipitation = this.precipitation,
        precipitationProbability = this.precipitationProbability,
        wind = this.wind,
        airQuality = airQuality,
        uV = uV ?: this.uV,
        relativeHumidity = this.relativeHumidity,
        dewPoint = this.dewPoint,
        pressure = this.pressure,
        cloudCover = this.cloudCover,
        visibility = this.visibility
    )
}
