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

package breezyweather.domain.weather.model

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
    val pM25: Double? = null,
    val pM10: Double? = null,
    val sO2: Double? = null,
    val nO2: Double? = null,
    val o3: Double? = null,
    val cO: Double? = null,
) : Serializable {

    val isValid: Boolean
        get() = pM25 != null || pM10 != null || sO2 != null || nO2 != null || o3 != null || cO != null
}
