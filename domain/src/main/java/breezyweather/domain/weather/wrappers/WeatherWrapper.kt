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
import breezyweather.domain.weather.model.Minutely
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.reference.Month

/**
 * Wrapper very similar to the object in database.
 * Helps the transition process and computing of missing data.
 */
data class WeatherWrapper(
    val dailyForecast: List<DailyWrapper>? = null,
    val hourlyForecast: List<HourlyWrapper>? = null,
    val current: CurrentWrapper? = null,
    val airQuality: AirQualityWrapper? = null,
    val pollen: PollenWrapper? = null,
    val minutelyForecast: List<Minutely>? = null,
    val alertList: List<Alert>? = null,
    /**
     * You can get the month with Month.of(month)
     * where month is a value between 1 and 12
     */
    val normals: Map<Month, Normals>? = null,
    val failedFeatures: Map<SourceFeature, Throwable>? = null,
)
