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

package org.breezyweather.sources.climweb

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import breezyweather.domain.weather.model.Normals
import breezyweather.domain.weather.reference.AlertSeverity
import breezyweather.domain.weather.reference.Month
import com.google.maps.android.PolyUtil
import com.google.maps.android.data.geojson.GeoJsonFeature
import com.google.maps.android.data.geojson.GeoJsonMultiPolygon
import com.google.maps.android.data.geojson.GeoJsonParser
import com.google.maps.android.data.geojson.GeoJsonPolygon
import org.breezyweather.sources.climweb.json.ClimWebAlertsResult
import org.breezyweather.sources.climweb.json.ClimWebNormals
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun getAlertList(
    location: Location,
    source: String?,
    alertsResult: ClimWebAlertsResult,
): List<Alert> {
    val matchingAlerts = getMatchingAlerts(location, alertsResult.features)
    if (matchingAlerts.isEmpty()) return emptyList()

    val alertList = mutableListOf<Alert>()
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH)
    var alertId: String
    var startDate: Date?
    var endDate: Date?
    var severity: AlertSeverity

    matchingAlerts.forEach {
        alertId =
            it.getProperty("id")
                ?: "${it.getProperty("event")} ${it.getProperty("areaDesc")} ${it.getProperty("onset")}"
        startDate = if (it.getProperty("onset") != null) {
            formatter.parse(it.getProperty("onset")!!)
        } else {
            null
        }
        endDate = if (it.getProperty("expires") != null) {
            formatter.parse(it.getProperty("expires")!!)
        } else {
            null
        }
        severity = with(it.getProperty("severity")) {
            when {
                equals("Extreme", ignoreCase = true) -> AlertSeverity.EXTREME
                equals("Severe", ignoreCase = true) -> AlertSeverity.SEVERE
                equals("Moderate", ignoreCase = true) -> AlertSeverity.MODERATE
                equals("Minor", ignoreCase = true) -> AlertSeverity.MINOR
                else -> AlertSeverity.UNKNOWN
            }
        }

        alertList.add(
            Alert(
                alertId = alertId,
                startDate = startDate,
                endDate = endDate,
                headline = it.getProperty("headline")?.trim(),
                description = it.getProperty("description")?.trim(),
                instruction = it.getProperty("instruction")?.trim(),
                source = source,
                severity = severity,
                color = Alert.colorFromSeverity(severity)
            )
        )
    }
    return alertList
}

private fun getMatchingAlerts(
    location: Location,
    alertFeatures: Any?,
): List<GeoJsonFeature> {
    val json = """{"type":"FeatureCollection","features":$alertFeatures}"""
    val geoJsonParser = GeoJsonParser(JSONObject(json))
    return geoJsonParser.features.filter { feature ->
        when (feature.geometry) {
            is GeoJsonPolygon -> (feature.geometry as GeoJsonPolygon).coordinates.any { polygon ->
                PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
            }
            is GeoJsonMultiPolygon -> (feature.geometry as GeoJsonMultiPolygon).polygons.any {
                it.coordinates.any { polygon ->
                    PolyUtil.containsLocation(location.latitude, location.longitude, polygon, true)
                }
            }
            else -> false
        }
    }
}

internal fun getNormals(
    normalsResult: List<ClimWebNormals>,
): Map<Month, Normals>? {
    if (normalsResult.isEmpty()) return null

    return Month.entries.associateWith { month ->
        val regex = Regex("""^\d{4}-${month.value.toString().padStart(2, '0')}-\d{2}$""")
        // Some sources enter duplicate normals in their data. We use the latest date for a given month.
        normalsResult.filter { it.date != null && regex.matches(it.date) }.sortedBy { it.date }.lastOrNull()?.let {
            Normals(
                daytimeTemperature = it.maxTemp
                    ?: it.maximumTemperature
                    ?: it.meanMaximumTemperature
                    ?: it.temperatureMaximale,
                nighttimeTemperature = it.minTemp
                    ?: it.minimumTemperature
                    ?: it.meanMinimumTemperature
                    ?: it.temperatureMinimale
            )
        }
    }.filter { it.value != null } as Map<Month, Normals>
}
