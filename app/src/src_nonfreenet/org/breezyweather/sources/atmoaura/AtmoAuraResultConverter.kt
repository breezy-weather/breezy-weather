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

package org.breezyweather.sources.atmoaura

import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.sources.atmoaura.json.AtmoAuraPointResult
import java.util.Date

fun convert(result: AtmoAuraPointResult): SecondaryWeatherWrapper {
    if (result.polluants == null) {
        throw SecondaryWeatherException()
    }

    val airQualityHourly = mutableMapOf<Date, AirQuality>()
    result.polluants.getOrNull(0)?.horaires?.forEach {
        airQualityHourly[it.datetimeEcheance] = getAirQuality(it.datetimeEcheance, result)
    }

    return SecondaryWeatherWrapper(
        airQuality = AirQualityWrapper(
            hourlyForecast = airQualityHourly
        )
    )
}

private fun getAirQuality(requestedDate: Date, aqiAtmoAuraResult: AtmoAuraPointResult): AirQuality {
    var pm25: Double? = null
    var pm10: Double? = null
    var so2: Double? = null
    var no2: Double? = null
    var o3: Double? = null

    aqiAtmoAuraResult.polluants
        ?.filter { p -> p.horaires?.firstOrNull { it.datetimeEcheance == requestedDate } != null }
        ?.forEach { p ->
            when (p.polluant) {
                "o3" -> o3 = p.horaires?.firstOrNull {
                    it.datetimeEcheance == requestedDate
                }?.concentration?.toDouble()
                "no2" -> no2 = p.horaires?.firstOrNull {
                    it.datetimeEcheance == requestedDate
                }?.concentration?.toDouble()
                "pm2.5" -> pm25 = p.horaires?.firstOrNull {
                    it.datetimeEcheance == requestedDate
                }?.concentration?.toDouble()
                "pm10" -> pm10 = p.horaires?.firstOrNull {
                    it.datetimeEcheance == requestedDate
                }?.concentration?.toDouble()
                "so2" -> so2 = p.horaires?.firstOrNull {
                    it.datetimeEcheance == requestedDate
                }?.concentration?.toDouble()
            }
        }

    return AirQuality(
        pM25 = pm25,
        pM10 = pm10,
        sO2 = so2,
        nO2 = no2,
        o3 = o3
    )
}
