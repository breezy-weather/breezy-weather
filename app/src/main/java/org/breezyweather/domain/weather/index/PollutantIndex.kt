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

package org.breezyweather.domain.weather.index

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import org.breezyweather.R
import org.breezyweather.common.basic.models.options.basic.UnitEnum
import org.breezyweather.common.basic.models.options.basic.Utils.formatDouble
import org.breezyweather.common.basic.models.options.unit.AirQualityCOUnit
import org.breezyweather.common.basic.models.options.unit.AirQualityUnit
import kotlin.math.roundToInt

enum class PollutantIndex(
    val id: String,
    val thresholds: List<Int>,
    val molecularMass: Double?,
    @StringRes val shortName: Int,
    @StringRes val voicedName: Int,
    @StringRes val fullName: Int,
    @StringRes val sources: Int,
) {
    O3(
        "o3",
        listOf(0, 50, 100, 160, 240, 480), // Plume 2023
        48.0,
        R.string.air_quality_o3,
        R.string.air_quality_o3_voice,
        R.string.air_quality_o3_full,
        R.string.air_quality_o3_sources
    ),
    NO2(
        "no2",
        listOf(0, 10, 25, 200, 400, 1000), // Plume 2023
        46.0055,
        R.string.air_quality_no2,
        R.string.air_quality_no2_voice,
        R.string.air_quality_no2_full,
        R.string.air_quality_no2_sources
    ),
    PM10(
        "pm10",
        listOf(0, 15, 45, 80, 160, 400), // Plume 2023
        null,
        R.string.air_quality_pm10,
        R.string.air_quality_pm10_voice,
        R.string.air_quality_pm10_full,
        R.string.air_quality_pm_sources
    ),
    PM25(
        "pm25",
        listOf(0, 5, 15, 30, 60, 150), // Plume 2023
        null,
        R.string.air_quality_pm25,
        R.string.air_quality_pm25_voice,
        R.string.air_quality_pm25_full,
        R.string.air_quality_pm_sources
    ),
    SO2(
        "so2",
        listOf(
            0,
            20,
            40, // daily
            270,
            500, // 10 min
            960 // linear prolongation
        ), // WHO 2021
        64.066,
        R.string.air_quality_so2,
        R.string.air_quality_so2_voice,
        R.string.air_quality_so2_full,
        R.string.air_quality_so2_sources
    ),
    CO(
        "co",
        listOf(
            0,
            2,
            4, // daily
            35, // hourly
            100, // 15 min
            230 // linear prolongation
        ), // WHO 2021
        28.01,
        R.string.air_quality_co,
        R.string.air_quality_co_voice,
        R.string.air_quality_co_full,
        R.string.air_quality_co_sources
    ),
    ;

    companion object {
        // Plume 2023
        val aqiThresholds = listOf(0, 20, 50, 100, 150, 250)
        val namesArrayId = R.array.air_quality_levels
        val descriptionsArrayId = R.array.air_quality_level_descriptions
        val harmlessExposuresArrayId = R.array.air_quality_level_harmless_exposures
        val colorsArrayId = R.array.air_quality_level_colors

        val indexFreshAir = aqiThresholds[1]
        val indexHighPollution = aqiThresholds[3]
        val indexExcessivePollution = aqiThresholds.last()

        fun getAqiToLevel(aqi: Int?): Int? {
            if (aqi == null) return null
            val level = aqiThresholds.indexOfLast { aqi >= it }
            return if (level >= 0) level else null
        }

        @ColorInt
        fun getAqiToColor(context: Context, aqi: Int?): Int {
            if (aqi == null) return Color.TRANSPARENT
            val level = getAqiToLevel(aqi)
            return if (level != null) {
                context.resources.getIntArray(colorsArrayId).getOrNull(level) ?: Color.TRANSPARENT
            } else {
                Color.TRANSPARENT
            }
        }

        fun getAqiToName(context: Context, aqi: Int?): String? {
            if (aqi == null) return null
            val level = getAqiToLevel(aqi)
            return if (level != null) context.resources.getStringArray(namesArrayId).getOrNull(level) else null
        }

        fun getAqiToDescription(context: Context, aqi: Int?): String? {
            if (aqi == null) return null
            val level = getAqiToLevel(aqi)
            return if (level != null) context.resources.getStringArray(descriptionsArrayId).getOrNull(level) else null
        }

        fun getAqiToHarmlessExposure(context: Context, aqi: Int?): String? {
            if (aqi == null) return null
            val level = getAqiToLevel(aqi)
            return if (level != null) {
                context.resources.getStringArray(harmlessExposuresArrayId).getOrNull(level)
            } else {
                null
            }
        }

        fun getUnit(pollutantIndex: PollutantIndex): UnitEnum<Double> {
            return if (pollutantIndex == CO) {
                AirQualityCOUnit.MGPCUM
            } else {
                AirQualityUnit.MUGPCUM
            }
        }
    }

    private fun getIndex(cp: Double, bpLo: Int, bpHi: Int, inLo: Int, inHi: Int): Int {
        // Result will be incorrect if we don’t cast to double
        return (
            (inHi.toDouble() - inLo.toDouble()) / (bpHi.toDouble() - bpLo.toDouble()) * (cp - bpLo.toDouble()) +
                inLo.toDouble()
            ).roundToInt()
    }

    private fun getIndex(cp: Double, level: Int): Int {
        return if (level < thresholds.lastIndex) {
            getIndex(
                cp,
                thresholds[level],
                thresholds[level + 1],
                aqiThresholds[level],
                aqiThresholds[level + 1]
            )
        } else {
            // Continue producing a linear index above lastIndex
            ((cp * aqiThresholds.last()) / thresholds.last()).roundToInt()
        }
    }

    fun getFullName(context: Context): String {
        return if (this == PM10 || this == PM25) {
            context.getString(
                fullName,
                formatDouble(context, if (this == PM10) 10.0 else 2.5, 1) +
                    "\u202f" +
                    context.getString(R.string.unit_mum)
            )
        } else {
            context.getString(fullName)
        }
    }

    fun getIndex(cp: Double?): Int? {
        if (cp == null) return null
        val level = thresholds.indexOfLast { cp >= it }
        return if (level >= 0) getIndex(cp, level) else null
    }

    fun getLevel(cp: Double?): Int? {
        if (cp == null) return null
        val level = thresholds.indexOfLast { cp >= it }
        return if (level >= 0) level else null
    }

    val excessivePollution = thresholds.last()

    fun getName(context: Context, cp: Double?): String? = getAqiToName(context, getIndex(cp))
    fun getDescription(context: Context, cp: Double?): String? = getAqiToDescription(context, getIndex(cp))

    @ColorInt
    fun getColor(context: Context, cp: Double?): Int = getAqiToColor(context, getIndex(cp))
}
