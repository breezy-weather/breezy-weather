/**
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

package org.breezyweather.common.basic.models.weather

import android.content.Context
import androidx.annotation.ColorInt
import org.breezyweather.common.basic.models.options.index.PollutantIndex
import java.io.Serializable

/**
 * Air Quality.
 * AQI uses 2023 update of Plume AQI
 * https://plumelabs.files.wordpress.com/2023/06/plume_aqi_2023.pdf
 * For missing information, it uses WHO recommandations
 * https://www.who.int/news-room/fact-sheets/detail/ambient-(outdoor)-air-quality-and-health
 *
 * default unit : [AirQualityUnit.MUGPCUM], [AirQualityCOUnit.MGPCUM]
 */
class AirQuality(
    val pM25: Float? = null,
    val pM10: Float? = null,
    val sO2: Float? = null,
    val nO2: Float? = null,
    val o3: Float? = null,
    val cO: Float? = null
) : Serializable {
    fun getIndex(pollutant: PollutantIndex? = null): Int? {
        return if (pollutant == null) { // Air Quality
            val pollutantsAqi: List<Int> = listOfNotNull(
                getIndex(PollutantIndex.O3),
                getIndex(PollutantIndex.NO2),
                getIndex(PollutantIndex.PM10),
                getIndex(PollutantIndex.PM25)
            )
            if (pollutantsAqi.isNotEmpty()) pollutantsAqi.max() else null
        } else { // Specific pollutant
            pollutant.getIndex(getConcentration(pollutant)?.toDouble())
        }
    }

    fun getConcentration(pollutant: PollutantIndex) = when (pollutant) {
        PollutantIndex.O3 -> o3
        PollutantIndex.NO2 -> nO2
        PollutantIndex.PM10 -> pM10
        PollutantIndex.PM25 -> pM25
        PollutantIndex.SO2 -> sO2
        PollutantIndex.CO -> cO
    }

    fun getName(context: Context, pollutant: PollutantIndex? = null): String? {
        return if (pollutant == null) { // Air Quality
            PollutantIndex.getAqiToName(context, getIndex())
        } else { // Specific pollutant
            pollutant.getName(context, getConcentration(pollutant)?.toDouble())
        }
    }

    fun getDescription(context: Context, pollutant: PollutantIndex? = null): String? {
        return if (pollutant == null) { // Air Quality
            PollutantIndex.getAqiToDescription(context, getIndex())
        } else { // Specific pollutant
            pollutant.getDescription(context, getConcentration(pollutant)?.toDouble())
        }
    }

    @ColorInt
    fun getColor(context: Context, pollutant: PollutantIndex? = null): Int {
        return if (pollutant == null) {
            PollutantIndex.getAqiToColor(context, getIndex())
        } else { // Specific pollutant
            pollutant.getColor(context, getConcentration(pollutant)?.toDouble())
        }
    }

    val isValid: Boolean
        get() = getIndex() != null

}
