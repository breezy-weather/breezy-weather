/*
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

package org.breezyweather.sources.windy

import android.content.Context
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.ui.radar.INITIAL_ZOOM_LEVEL
import org.breezyweather.unit.precipitation.PrecipitationUnit
import org.breezyweather.unit.speed.SpeedUnit
import org.breezyweather.unit.temperature.TemperatureUnit
import javax.inject.Inject

class WindyService @Inject constructor() : WindyServiceStub() {

    override val allowedDomains = arrayOf(
        "https://embed.windy.com",
        "https://node.windy.com",
        "https://tiles-s.windy.com",
        "https://rdr.windy.com",
        "https://sat.windy.com",
        "https://ims.windy.com"
    )

    override fun getWebViewUrl(
        context: Context,
        longitude: Double,
        latitude: Double,
    ): String {
        val metricRain = when (SettingsManager.getInstance(context).precipitationUnit) {
            PrecipitationUnit.MILLIMETER, PrecipitationUnit.CENTIMETER -> "mm"
            PrecipitationUnit.INCH -> "in"
            else -> "default"
        }
        val metricTemp = when (SettingsManager.getInstance(context).temperatureUnit) {
            TemperatureUnit.CELSIUS, TemperatureUnit.KELVIN -> "°C"
            TemperatureUnit.FAHRENHEIT -> "°F"
            else -> "default"
        }
        val metricWind = when (SettingsManager.getInstance(context).speedUnit) {
            SpeedUnit.KNOT -> "kt"
            SpeedUnit.METER_PER_SECOND -> "m/s"
            SpeedUnit.KILOMETER_PER_HOUR -> "km/h"
            SpeedUnit.MILE_PER_HOUR -> "mph"
            SpeedUnit.BEAUFORT_SCALE -> "bft"
            else -> "default"
        }

        return "https://embed.windy.com/embed.html" +
            "?type=map" +
            "&location=coordinates" +
            "&metricRain=$metricRain" +
            "&metricTemp=$metricTemp" +
            "&metricWind=$metricWind" +
            "&zoom=$INITIAL_ZOOM_LEVEL" +
            "&overlay=radar" +
            "&product=radar" +
            "&level=surface" +
            "&lat=$latitude" +
            "&lon=$longitude" +
            "&message=true"
    }
}
