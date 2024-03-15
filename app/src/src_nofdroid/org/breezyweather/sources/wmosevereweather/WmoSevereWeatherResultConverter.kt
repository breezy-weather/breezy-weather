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
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.Alert
import com.google.maps.android.EncodedPolylineUtil
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import org.breezyweather.common.exceptions.ParsingException
import org.breezyweather.common.extensions.toCalendarWithTimeZone
import org.breezyweather.common.extensions.toDateNoHour
import org.breezyweather.sources.wmosevereweather.json.WmoSevereWeatherAlert
import org.breezyweather.sources.wmosevereweather.json.WmoSevereWeatherAlertCoord
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun convert(location: Location, alertsResult: List<WmoSevereWeatherAlert>): List<Alert> {
    return alertsResult
        .filter {
            isAlertForLocation(location, it.coord)
        }.map {
            WmoSevereWeatherAlertWrapper(
                alert = Alert(
                    alertId = (it.identifier ?: it.capURL ?: it.url)!!,
                    startDate = parseDate(
                        if (it.onset.isNullOrEmpty()) {
                            if (it.effective.isNullOrEmpty()) {
                                it.sent
                            } else it.effective
                        } else it.onset
                    ),
                    endDate = parseDate(it.expires),
                    description = it.event?.replaceFirstChar { firstChar ->
                        if (firstChar.isLowerCase()) {
                            firstChar.titlecase(Locale.getDefault())
                        } else firstChar.toString()
                    } ?: "",
                    content = it.description,
                    priority = it.s ?: 5,
                    color = when (it.s) {
                        4 -> Color.rgb(215, 46, 41)
                        3 -> Color.rgb(254, 153, 0)
                        2 -> Color.rgb(255, 255, 1)
                        1 -> Color.rgb(0, 255, 255)
                        0 -> Color.rgb(51, 102, 255)
                        else -> null
                    }
                ),
                url = it.url?.let { url ->
                    WmoSevereWeatherService.WMO_ALERTS_URL_BASE_URL + url
                } ?: it.capURL?.let { capURL ->
                    WmoSevereWeatherService.WMO_ALERTS_CAP_URL_BASE_URL + capURL
                }
            )
        }.map {
            // TODO: Load URLs to allow translation and instructions section.
            // If fails, fallback to already generated alert
            it.alert
        }
}

fun isAlertForLocation(location: Location, coords: List<WmoSevereWeatherAlertCoord>): Boolean {
    coords.forEach { coord ->
        if (coord.polygon != null) {
            coord.polygon.forEach {
                if (PolyUtil.containsLocation(
                        location.latitude,
                        location.longitude,
                        it.map { latLng ->
                            parseCoordinate(latLng)
                        },
                        true
                    )) {
                    return true
                }
            }
        } else if (coord.geocode != null) {
            coord.geocode.forEach { geocodeList ->
                geocodeList.forEach {
                    it.coordinates?.forEach { multipolygon ->
                        multipolygon.forEach { encodedPolygon ->
                            if (PolyUtil.containsLocation(
                                    location.latitude,
                                    location.longitude,
                                    EncodedPolylineUtil.decode(encodedPolygon),
                                    true
                                )) {
                                return true
                            }
                        }
                    }
                }
            }
        } /*else if (coord.geojson != null) {
            // TODO
        } else if (coord.circle != null) {
            // TODO
        }*/ else if (coord.marker != null) {
            if (
                SphericalUtil.computeDistanceBetween(
                    LatLng(location.latitude, location.longitude),
                    parseCoordinate(coord.marker)
                ) < WmoSevereWeatherService.WMO_MARKER_RADIUS
            ) {
                return true
            }
        } else {
            throw ParsingException()
        }
    }

    return false
}

private fun parseCoordinate(coordinates: String): LatLng {
    val coordArr = coordinates.split(",")

    if (coordArr.size != 2) {
        throw ParsingException()
    }

    val lon = coordArr[0].trim().toDoubleOrNull()
    val lat = coordArr[1].trim().toDoubleOrNull()

    if (lon == null || lat == null || (lon == 0.0 && lat == 0.0)) {
        throw ParsingException()
    }
    return LatLng(lon, lat)
}

private fun parseDate(jsonValue: String?): Date? {
    return if (jsonValue.isNullOrEmpty() || jsonValue.length < 16
        || !jsonValue.matches(
            // Supports dates from 2020 to 2099
            Regex("20[2-9][0-9]-(0[1-9]|1[0-2])-([0-2][0-9]|3[0-1])[ T]([0-1][0-9]|2[0-3]):[0-5][0-9](.*)")
        )) {
        null
    } else {
        val timeZone = TimeZone.getTimeZone("UTC")
        jsonValue.toDateNoHour(timeZone)?.toCalendarWithTimeZone(timeZone)?.apply {
            set(Calendar.HOUR_OF_DAY, jsonValue.substring(11, 13).toInt())
            set(Calendar.MINUTE, jsonValue.substring(14, 16).toInt())
        }?.time
    }
}
