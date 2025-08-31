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

package org.breezyweather.sources.atmo

import android.content.Context
import breezyweather.domain.location.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import javax.inject.Inject

/**
 * Atmo Hauts-de-France air quality service.
 *
 * Note: this region only has forecast starting from next hour
 */
class AtmoHdfService @Inject constructor(
    @ApplicationContext injectedContext: Context,
) : AtmoService() {

    override val id = "atmohdf"
    override val name = "Atmo Hauts-de-France (${injectedContext.currentLocale.getCountryName("FR")})"
    override val attribution = "Atmo Hauts-de-France"

    override fun isLocationInRegion(location: Location): Boolean {
        return location.admin1 in arrayOf(
            "Hauts-de-France",
            "Hauts de France"
        ) ||
            location.admin1Code in arrayOf("FR-HDF", "HDF", "32") ||
            location.admin2 in arrayOf(
                "Aisne", // 02
                "Nord", // 59
                "North", // 59
                "Oise", // 60
                "Pas-de-Calais", // 62
                "Pas de Calais", // 62
                "Somme" // 80
            ) ||
            arrayOf(
                "02", // Aisne
                "59", // Nord
                "60", // Oise
                "62", // Pas-de-Calais
                "80" // Somme
            ).any { location.admin2Code?.endsWith(it) == true }
    }
}
