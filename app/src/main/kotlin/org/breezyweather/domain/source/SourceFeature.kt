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

package org.breezyweather.domain.source

import breezyweather.domain.source.SourceFeature
import org.breezyweather.R

val SourceFeature.resourceName: Int
    get() = when (this) {
        SourceFeature.FORECAST -> R.string.forecast
        SourceFeature.CURRENT -> R.string.current_weather
        SourceFeature.AIR_QUALITY -> R.string.air_quality
        SourceFeature.POLLEN -> R.string.pollen
        SourceFeature.MINUTELY -> R.string.precipitation_nowcasting
        SourceFeature.ALERT -> R.string.alerts
        SourceFeature.NORMALS -> R.string.temperature_normals
        SourceFeature.REVERSE_GEOCODING -> R.string.location_reverse_geocoding
    }
