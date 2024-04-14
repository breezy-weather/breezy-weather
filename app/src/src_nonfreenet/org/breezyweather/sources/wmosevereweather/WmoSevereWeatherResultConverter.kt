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

package org.breezyweather.sources.wmosevereweather

import android.graphics.Color
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import org.breezyweather.common.extensions.capitalize
import org.breezyweather.sources.wmosevereweather.json.WmoSevereWeatherAlertResult
import java.util.Date

fun convert(alertResult: WmoSevereWeatherAlertResult): SecondaryWeatherWrapper {
    return SecondaryWeatherWrapper(
        alertList = alertResult.features
            ?.filter {
                it.properties != null &&
                    (it.properties.expires == null || it.properties.expires > Date())
            }?.map {
                val severity = AlertSeverity.getInstance(it.properties!!.s)
                Alert(
                    alertId = (it.id?.ifEmpty { null }
                        ?: it.properties.identifier?.ifEmpty { null }
                        ?: it.properties.capurl?.ifEmpty { null }
                        ?: it.properties.url)!!,
                    startDate = it.properties.onset ?: it.properties.effective ?: it.properties.sent,
                    endDate = it.properties.expires,
                    headline = it.properties.event?.capitalize(),
                    description = it.properties.description,
                    severity = AlertSeverity.getInstance(it.properties.s),
                    color = when (severity) {
                        AlertSeverity.EXTREME -> Color.rgb(212, 45, 65)
                        AlertSeverity.SEVERE -> Color.rgb(240, 140, 17)
                        AlertSeverity.MODERATE -> Color.rgb(244, 207, 0)
                        AlertSeverity.MINOR -> Color.rgb(57, 156, 199)
                        else -> Color.rgb(130, 168, 223)
                    }
                )
                // TODO: Use URL to get more info (description, instruction, translations)
                /*val url = it.url?.let { url ->
                    WmoSevereWeatherService.WMO_ALERTS_URL_BASE_URL + url
                } ?: it.capURL?.let { capURL ->
                    WmoSevereWeatherService.WMO_ALERTS_CAP_URL_BASE_URL + capURL
                }*/
            }
    )
}
