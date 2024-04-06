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

package org.breezyweather.sources.geosphereat

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Temperature
import breezyweather.domain.weather.wrappers.HourlyWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import org.breezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.breezyweather.sources.geosphereat.json.GeoSphereAtHourlyResult
import kotlin.math.roundToInt

/**
 * Converts DMI result into a forecast
 */
fun convert(
    hourlyResult: GeoSphereAtHourlyResult,
    location: Location
): WeatherWrapper {
    // If the API doesn’t return timeseries, consider data as garbage and keep cached data
    if (hourlyResult.timestamps.isNullOrEmpty() || hourlyResult.features?.getOrNull(0)?.properties?.parameters == null) {
        throw InvalidOrIncompleteDataException()
    }

    return WeatherWrapper(
        hourlyForecast = getHourlyForecast(hourlyResult)
    )
}

/**
 * Returns hourly forecast
 */
private fun getHourlyForecast(
    hourlyResult: GeoSphereAtHourlyResult
): List<HourlyWrapper> {
    return hourlyResult.timestamps!!.mapIndexed { i, date ->
        HourlyWrapper(
            date = date,
            //weatherCode = , // TODO
            temperature = Temperature(
                temperature = hourlyResult.features!![0].properties!!.parameters!!.t2m?.data?.getOrNull(i)
            ),
            //wind = ,// TODO
            relativeHumidity = hourlyResult.features[0].properties!!.parameters!!.rh2m?.data?.getOrNull(i),
            pressure = hourlyResult.features[0].properties!!.parameters!!.sp?.data?.getOrNull(i)?.div(100),
            cloudCover = hourlyResult.features[0].properties!!.parameters!!.tcc?.data?.getOrNull(i)?.times(100)?.roundToInt()
        )
    }
}
