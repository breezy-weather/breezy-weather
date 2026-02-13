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

import breezyweather.domain.source.SourceContinent
import org.breezyweather.R

val SourceContinent.resourceName: Int
    get() = when (this) {
        SourceContinent.WORLDWIDE -> R.string.weather_source_continent_worldwide
        SourceContinent.AFRICA -> R.string.weather_source_continent_africa
        SourceContinent.ASIA -> R.string.weather_source_continent_asia
        SourceContinent.EUROPE -> R.string.weather_source_continent_europe
        SourceContinent.NORTH_AMERICA -> R.string.weather_source_continent_north_america
        SourceContinent.OCEANIA -> R.string.weather_source_continent_oceania
        SourceContinent.SOUTH_AMERICA -> R.string.weather_source_continent_south_america
    }
