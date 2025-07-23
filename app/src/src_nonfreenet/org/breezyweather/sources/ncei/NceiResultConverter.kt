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

package org.breezyweather.sources.ncei

import breezyweather.domain.weather.model.Normals
import org.breezyweather.sources.ncei.json.NceiDataResult
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

internal fun getNormals(
    month: Int,
    normalsList: List<NceiDataResult>? = null,
    stationMap: Map<String, Double>,
): Normals {
    var tMaxWeightedSum = 0.0
    var tMaxWeightTotal = 0.0
    var tMinWeightedSum = 0.0
    var tMinWeightTotal = 0.0
    val monthEnding: String = if (month >= 1 && month <= 9) {
        "-0$month"
    } else {
        "-$month"
    }

    // Assign a weight to each station as a function of its distance from the weather location.
    // We calculate weights here so that we won't have to force reload location parameters
    // even if the weight function changes in the future.
    val stationWeights = stationMap.mapValues {
        getWeight(it.value)
    }

    // Add each relevant monthly record to the weighted sum of tMax and tMin,
    // using the weight of the reporting station,
    // so that we can calculate the weighted average later.
    normalsList?.forEach {
        if (it.date.endsWith(monthEnding)) {
            if (it.station in stationWeights.keys) {
                if (it.tMax != null) {
                    tMaxWeightedSum += it.tMax.toDouble().times(stationWeights[it.station]!!)
                    tMaxWeightTotal += stationWeights[it.station]!!
                }
                if (it.tMin != null) {
                    tMinWeightedSum += it.tMin.toDouble().times(stationWeights[it.station]!!)
                    tMinWeightTotal += stationWeights[it.station]!!
                }
            }
        }
    }
    return Normals(
        month = month,
        daytimeTemperature = if (tMaxWeightTotal > 0) {
            tMaxWeightedSum / tMaxWeightTotal
        } else {
            null
        },
        nighttimeTemperature = if (tMinWeightTotal > 0) {
            tMinWeightedSum / tMinWeightTotal
        } else {
            null
        }
    )
}

// Models the weight for each nearby station after the normal distribution.
// Let μ = 0
//     σ = 1 representing an arbitrary distance of 20km
//     x = distance between station and location in multiples of 20km
// Illustrative weights at various distances:
//  - Station at location: 0.399 (100% weight)
//  - Station 20km away:   0.242 (60% weight)
//  - Station 40km away:   0.054 (14% weight)
//  - Station 60km away:   0.004 (1% weight)
// Source: https://en.wikipedia.org/wiki/Normal_distribution
private fun getWeight(distance: Double): Double {
    val sigmaDistance = NceiService.DISTANCE_LIMIT / 3.0
    val x = distance / sigmaDistance
    val weight = 1.0 / sqrt(2.0 * PI) * exp(-x.pow(2.0) / 2.0)
    return weight
}
