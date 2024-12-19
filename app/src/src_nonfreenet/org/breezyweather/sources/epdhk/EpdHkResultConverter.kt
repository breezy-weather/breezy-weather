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

package org.breezyweather.sources.epdhk

import android.util.Log
import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.model.LatLng
import org.breezyweather.sources.epdhk.xml.EpdHkConcentrationsResult
import org.breezyweather.sources.getNearestLocation
import java.text.SimpleDateFormat
import java.util.Locale

fun convert(
    location: Location,
    concentrationsResult: EpdHkConcentrationsResult?,
): WeatherWrapper {
    val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
    val nearestStation = getNearestLocation(location, EPD_HK_STATIONS)
    var airQuality = AirQuality()
    concentrationsResult?.pollutantConcentration?.filter {
        it.stationName.value.equals(nearestStation)
    }?.sortedByDescending { formatter.parse(it.dateTime.value) }?.firstOrNull()?.let {
        Log.d("epd.station", it.stationName.value)
        Log.d("epd.time", it.dateTime.value)
        airQuality = AirQuality(
            pM25 = it.pM25?.value?.toDoubleOrNull(),
            pM10 = it.pM10?.value?.toDoubleOrNull(),
            sO2 = it.sO2?.value?.toDoubleOrNull(),
            nO2 = it.nO2?.value?.toDoubleOrNull(),
            o3 = it.o3?.value?.toDoubleOrNull(),
            cO = it.cO?.value?.toDoubleOrNull()?.div(1000.0) // convert µg/m³ to mg/m³
        )
    }
    return WeatherWrapper(
        airQuality = AirQualityWrapper(
            current = airQuality
        )
    )
}

// Source: https://data.gov.hk/en-data/dataset/hk-epd-aqmnteam-air-quality-monitoring-network-of-hong-kong
private val EPD_HK_STATIONS = mapOf(
    "Central/Western" to LatLng(22.28489089, 114.14442071),
    "Southern" to LatLng(22.24746092, 114.1601401),
    "Eastern" to LatLng(22.28288555, 114.21937156),
    "Kwun Tong" to LatLng(22.3096251, 114.23117417),
    "Sham Shui Po" to LatLng(22.3302259, 114.15910913),
    "Kwai Chung" to LatLng(22.35710398, 114.12960136),
    "Tsuen Wan" to LatLng(22.37174191, 114.11453491),
    "Tseung Kwan O" to LatLng(22.31764241, 114.25956137),
    "Yuen Long" to LatLng(22.44515508, 114.02264888),
    "Tuen Mun" to LatLng(22.39114326, 113.97672832),
    "Tung Chung" to LatLng(22.28888887, 113.94365902),
    "Tai Po" to LatLng(22.45095988, 114.16457022),
    "Sha Tin" to LatLng(22.37628072, 114.18453161),
    "North" to LatLng(22.49669723, 114.12824408),
    "Tap Mun" to LatLng(22.47131669, 114.3607185),
    "Causeway Bay" to LatLng(22.28013296, 114.18509009),
    "Central" to LatLng(22.2818145, 114.15812743),
    "Mong Kok" to LatLng(22.32261115, 114.16827176)
)
