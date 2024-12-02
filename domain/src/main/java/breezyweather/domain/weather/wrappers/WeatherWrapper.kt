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

import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Current
import breezyweather.domain.weather.model.Daily
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals

/**
 * Wrapper very similar to the object in database.
 * Helps the transition process and computing of missing data.
 */
data class WeatherWrapper(
    val current: Current? = null,
    val normals: Normals? = null,
    val dailyForecast: List<Daily>? = null,
    val hourlyForecast: List<HourlyWrapper>? = null,
    val minutelyForecast: List<Minutely>? = null,
    val alertList: List<Alert>? = null,
    val failedFeatures: List<SourceFeature>? = null,
)
