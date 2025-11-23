/*
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

package org.breezyweather.sources.nws

/**
 * Transition class for NwsGridPointProperties data
 */
class NwsForecast(
    val temperature: Double?,
    val dewpoint: Double?,
    val relativeHumidity: Int?,
    val apparentTemperature: Double?,
    val wetBulbGlobeTemperature: Double?,
    val heatIndex: Double?,
    val windChill: Double?,
    val skyCover: Int?,
    val windDirection: Int?,
    val windSpeed: Double?,
    val windGust: Double?,
    val weather: String?,
    val probabilityOfPrecipitation: Int?,
    val quantitativePrecipitation: Double?,
    val iceAccumulation: Double?,
    val snowfallAmount: Double?,
    val ceilingHeight: Double?,
    val visibility: Double?,
    val pressure: Double?,
    val probabilityOfThunder: Int?,
)
