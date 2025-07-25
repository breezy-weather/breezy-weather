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

import breezyweather.domain.weather.model.HalfDay
import breezyweather.domain.weather.model.Precipitation
import breezyweather.domain.weather.model.PrecipitationDuration
import breezyweather.domain.weather.model.PrecipitationProbability
import breezyweather.domain.weather.model.WeatherCode
import breezyweather.domain.weather.model.Wind

/**
 * Half day.
 */
data class HalfDayWrapper(
    /**
     * A short description of the weather condition
     */
    val weatherText: String? = null,

    /**
     * A long description of the weather condition. Used as a half-day summary
     */
    val weatherPhase: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: TemperatureWrapper? = null,
    val precipitation: Precipitation? = null,
    val precipitationProbability: PrecipitationProbability? = null,
    val precipitationDuration: PrecipitationDuration? = null,
    val wind: Wind? = null,
    val cloudCover: Int? = null,
) {
    fun toHalfDay() = HalfDay(
        weatherText = this.weatherText,
        weatherPhase = this.weatherPhase,
        weatherCode = this.weatherCode,
        temperature = this.temperature?.toTemperature(),
        precipitation = this.precipitation,
        precipitationProbability = this.precipitationProbability,
        precipitationDuration = this.precipitationDuration,
        wind = this.wind,
        cloudCover = this.cloudCover
    )
}
