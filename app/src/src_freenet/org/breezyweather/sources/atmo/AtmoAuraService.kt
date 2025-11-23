/*
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
 * ATMO Auvergne-Rhône-Alpes air quality service.
 */
class AtmoAuraService @Inject constructor(
    @ApplicationContext injectedContext: Context,
) : AtmoService() {

    override val id = "atmoaura"
    override val name = "Atmo Auvergne-Rhône-Alpes (${injectedContext.currentLocale.getCountryName("FR")})"
    override val attribution = "Atmo Auvergne-Rhône-Alpes"

    override fun isLocationInRegion(location: Location): Boolean {
        return location.admin1 in arrayOf(
            "Auvergne-Rhône-Alpes",
            "Auvergne-Rhone-Alpes",
            "Auvergne Rhône Alpes",
            "Auvergne Rhone Alpes"
        ) ||
            location.admin1Code in arrayOf("FR-ARA", "ARA", "84") ||
            location.admin2 in arrayOf(
                "Ain", // 01
                "Allier", // 03
                "Ardèche", // 07
                "Ardeche", // 07
                "Cantal", // 15
                "Drôme", // 26
                "Drome", // 26
                "Isère", // 38
                "Isere", // 38
                "Loire", // 42
                "Haute Loire", // 43
                "Haute-Loire", // 43
                "Métropole de Lyon", // 69M
                "Puy-de-Dôme", // 63
                "Puy-de-Dome", // 63
                "Puy de Dôme", // 63
                "Puy de Dome", // 63
                "Rhône", // 69
                "Rhone", // 69
                "Savoie", // 73
                "Haute-Savoie", // 74
                "Haute Savoie" // 74
            ) ||
            arrayOf(
                "01", // Ain
                "03", // Allier
                "07", // Ardèche
                "15", // Cantal
                "26", // Drôme
                "38", // Isère
                "42", // Loire
                "43", // Haute-Loire
                "63", // Puy-de-Dôme
                "69", // Rhône
                "69M", // Métropole de Lyon
                "73", // Savoie
                "74" // Haute-Savoie
            ).any { location.admin2Code?.endsWith(it, ignoreCase = true) == true }
    }
}
