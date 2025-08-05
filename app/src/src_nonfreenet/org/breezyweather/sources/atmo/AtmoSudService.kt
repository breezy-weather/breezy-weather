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
import org.breezyweather.BuildConfig
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * AtmoSud air quality service.
 */
class AtmoSudService @Inject constructor(
    @ApplicationContext injectedContext: Context,
    @Named("JsonClient") injectedJsonClient: Retrofit.Builder,
) : AtmoService() {

    override val id = "atmosud"
    override val name = "AtmoSud (${injectedContext.currentLocale.getCountryName("FR")})"
    override val attribution = "AtmoSud"
    override val attributionLinks = mapOf(
        attribution to "https://www.atmosud.org/"
    )
    override val privacyPolicyUrl = "https://www.atmosud.org/article/politique-de-confidentialite"

    override val context = injectedContext
    override val jsonClient = injectedJsonClient
    override val baseUrl = "https://api.atmosud.org/air2go/v1/cartes/"

    override val apiKeyPreference = R.string.settings_weather_source_atmo_sud_api_key
    override val builtInApiKey = BuildConfig.ATMO_SUD_KEY

    override fun isLocationInRegion(location: Location): Boolean {
        return location.admin1 in arrayOf(
            "Provence-Alpes-Côte d'Azur",
            "Provence-Alpes-Côte d’Azur",
            "Provence-Alpes-Cote d'Azur",
            "Provence-Alpes-Cote d’Azur",
            "Provence Alpes Côte d'Azur",
            "Provence Alpes Côte d’Azur",
            "Provence Alpes Cote d'Azur",
            "Provence Alpes Cote d’Azur",
            "Sud Provence-Alpes-Côte d'Azur",
            "Sud Provence-Alpes-Côte d’Azur",
            "Sud Provence-Alpes-Cote d'Azur",
            "Sud Provence-Alpes-Cote d’Azur",
            "Sud Provence Alpes Côte d'Azur",
            "Sud Provence Alpes Côte d’Azur",
            "Sud Provence Alpes Cote d'Azur",
            "Sud Provence Alpes Cote d’Azur"
        ) ||
            location.admin1Code == "93" ||
            location.admin2 in arrayOf(
                "Alpes-de-Haute Provence", // 04
                "Alpes de Haute Provence", // 04
                "Hautes-Alpes", // 05
                "Hautes Alpes", // 05
                "Alpes-Maritimes", // 06
                "Alpes Maritimes", // 06
                "Bouches-du-Rhône", // 13
                "Bouches-du-Rhone", // 13
                "Bouches du Rhône", // 13
                "Bouches du Rhone", // 13
                "Var", // 83
                "Vaucluse" // 84
            ) ||
            location.admin2Code in arrayOf(
                "04", // Alpes-de-Haute Provence
                "05", // Hautes-Alpes
                "06", // Alpes-Maritimes
                "13", // Bouches du Rhône
                "83", // Var
                "84" // Vaucluse
            )
    }

    override val testingLocations: List<Location> = emptyList()
}
