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

package org.breezyweather.sources.ekuk

import breezyweather.domain.location.model.Location
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.model.Pollen
import com.google.maps.android.model.LatLng
import org.breezyweather.sources.ekuk.json.EkukObservationsResult
import org.breezyweather.sources.ekuk.json.EkukStationsResult
import kotlin.math.roundToInt

internal fun convert(
    location: Location,
    stationsResult: EkukStationsResult,
): Map<String, String> {
    val airQualityStations = mutableMapOf<String, LatLng>()
    val pollenStations = mutableMapOf<String, LatLng>()
    stationsResult.features?.filter { it.properties.type != "POLLEN" && it.properties.type != "RADIATION" }?.forEach {
        airQualityStations[it.id.toString()] = LatLng(it.geometry.coordinates[1], it.geometry.coordinates[0])
    }
    stationsResult.features?.filter { it.properties.type == "POLLEN" }?.forEach {
        pollenStations[it.id.toString()] = LatLng(it.geometry.coordinates[1], it.geometry.coordinates[0])
    }

    // limit a valid station within 50km of selected location; return empty string otherwise
    return mapOf(
        "airQualityStation" to
            (LatLng(location.latitude, location.longitude).getNearestLocation(airQualityStations, 50000.0) ?: ""),
        "pollenStation" to
            (LatLng(location.latitude, location.longitude).getNearestLocation(pollenStations, 50000.0) ?: "")
    )
}

internal fun getAirQuality(
    airQualityResult: List<EkukObservationsResult>,
): AirQuality {
    val pollutantConcentrations = mutableMapOf<String, Double?>()
    EKUK_POLLUTANT_IDS.keys.forEach { pollutant ->
        pollutantConcentrations[pollutant] = airQualityResult
            .filter { it.indicator == EKUK_POLLUTANT_IDS[pollutant] }
            .sortedByDescending { it.measured }
            .firstOrNull()?.value?.toDoubleOrNull()
    }
    return AirQuality(
        pM25 = pollutantConcentrations.getOrElse("PM25") { null },
        pM10 = pollutantConcentrations.getOrElse("PM10") { null },
        sO2 = pollutantConcentrations.getOrElse("SO2") { null },
        nO2 = pollutantConcentrations.getOrElse("NO2") { null },
        o3 = pollutantConcentrations.getOrElse("O3") { null },
        cO = pollutantConcentrations.getOrElse("CO") { null }
    )
}

internal fun getPollen(
    pollenResult: List<EkukObservationsResult>,
): Pollen {
    val pollenConcentrations = mutableMapOf<String, Double?>()
    EKUK_POLLEN_IDS.keys.forEach { pollen ->
        pollenConcentrations[pollen] = pollenResult
            .filter { it.indicator == EKUK_POLLEN_IDS[pollen] }
            .sortedByDescending { it.measured }
            .firstOrNull()?.value?.toDoubleOrNull()
    }
    // TODO: Alternaria, Elm, Juniper, Spruce, Pine, Maple, Saltbush
    return Pollen(
        alder = pollenConcentrations.getOrElse("ALDER") { null }?.roundToInt(),
        ash = pollenConcentrations.getOrElse("ASH") { null }?.roundToInt(),
        birch = pollenConcentrations.getOrElse("BIRCH") { null }?.roundToInt(),
        grass = pollenConcentrations.getOrElse("GRASSES") { null }?.roundToInt(),
        hazel = pollenConcentrations.getOrElse("HAZEL") { null }?.roundToInt(),
        mold = pollenConcentrations.getOrElse("CLADOSPORIUM") { null }?.roundToInt(),
        mugwort = pollenConcentrations.getOrElse("WORMWOOD") { null }?.roundToInt(),
        oak = pollenConcentrations.getOrElse("OAK") { null }?.roundToInt(),
        poplar = pollenConcentrations.getOrElse("POPLAR") { null }?.roundToInt(),
        ragweed = pollenConcentrations.getOrElse("AMBROSIA") { null }?.roundToInt(),
        sorrel = pollenConcentrations.getOrElse("DOCK") { null }?.roundToInt(),
        tree = pollenConcentrations.getOrElse("UNIDENTIFIED") { null }?.roundToInt(),
        urticaceae = pollenConcentrations.getOrElse("NETTLE") { null }?.roundToInt(),
        willow = pollenConcentrations.getOrElse("WILLOW") { null }?.roundToInt()
    )
}

// Source: https://www.ohuseire.ee/api/indicator/en?type=INDICATOR
private val EKUK_POLLUTANT_IDS = mapOf(
    "PM25" to 23,
    "PM10" to 21,
    "SO2" to 1,
    "NO2" to 3,
    "O3" to 6,
    "CO" to 4
)

// Source: https://www.ohuseire.ee/api/indicator/en?type=POLLEN
private val EKUK_POLLEN_IDS = mapOf(
    "ALDER" to 51,
    "ALTERNARIA" to 44,
    "AMBROSIA" to 63,
    "ASH" to 58,
    "BIRCH" to 48,
    "CLADOSPORIUM" to 45,
    "DOCK" to 54,
    "ELM" to 46,
    "GRASSES" to 49,
    "HAZEL" to 59,
    "JUNIPER" to 47,
    "MAPLE" to 61,
    "NETTLE" to 53,
    "OAK" to 60,
    "PINE" to 52,
    "POPLAR" to 56,
    "SALTBUSH" to 62,
    "SPRUCE" to 50,
    "UNIDENTIFIED" to 43,
    "WILLOW" to 55,
    "WORMWOOD" to 57
)
