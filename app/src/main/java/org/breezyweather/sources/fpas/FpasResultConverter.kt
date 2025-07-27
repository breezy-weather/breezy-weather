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

package org.breezyweather.sources.fpas

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.AlertSeverity
import com.google.maps.android.model.LatLng
import org.breezyweather.sources.common.xml.CapAlert

fun convert(
    context: Context,
    location: Location,
    capAlerts: Map<String, CapAlert?>,
): List<Alert>? {
    if (capAlerts.isEmpty()) {
        return null
    }
    val alertList = mutableListOf<Alert>()
    capAlerts.keys.forEach { uuid ->

        val info = capAlerts[uuid]!!.getInfoForContext(context)
        info?.let {
            // Filter out non-meteorological alerts, past alerts,
            // and alert whose polygons do not cover the requested location
            val category = it.category?.value ?: ""
            val urgency = it.urgency?.value ?: ""
            val valid = it.containsPoint(LatLng(location.latitude, location.longitude))
            if (category.equals("Met", ignoreCase = true) && !urgency.equals("Past", ignoreCase = true) && valid) {
                val severity = when (it.severity?.value) {
                    "Extreme" -> AlertSeverity.EXTREME
                    "Severe" -> AlertSeverity.SEVERE
                    "Moderate" -> AlertSeverity.MODERATE
                    "Minor" -> AlertSeverity.MINOR
                    else -> AlertSeverity.UNKNOWN
                }
                val alert = Alert(
                    alertId = uuid,
                    startDate = it.onset?.value ?: it.effective?.value ?: capAlerts[uuid]!!.sent?.value,
                    endDate = it.expires?.value,
                    headline = it.event?.value ?: it.headline?.value,
                    description = formatAlertText(
                        it.senderName?.value,
                        it.description?.value
                    ),
                    instruction = it.instruction?.value,
                    source = it.senderName?.value,
                    severity = severity,
                    color = Alert.colorFromSeverity(severity)
                )
                alertList.add(alert)
            }
        }
    }
    return alertList
}

// apply formatting to alert text based on source
private fun formatAlertText(
    source: String?,
    text: String?,
): String {
    var result: String
    if (text.isNullOrEmpty()) {
        return ""
    }
    result = text
    if (!source.isNullOrEmpty()) {
        if (source.startsWith("NWS ", ignoreCase = true) ||
            source.equals("National Weather Service", ignoreCase = true)
        ) {
            // Look for SINGLE line breaks surrounded by letters, numbers, and punctuation.
            val regex = Regex("""([0-9A-Za-z.,]) *\n([0-9A-Za-z])""")
            result = regex.replace(result, "$1 $2")
        }
    }
    return result.trim()
}
