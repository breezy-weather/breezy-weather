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
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

/**
 * ATMO Auvergne-Rhône-Alpes air quality service.
 */
class AtmoAuraService @Inject constructor(
    @ApplicationContext injectedContext: Context,
    @Named("JsonClient") injectedJsonClient: Retrofit.Builder,
) : AtmoService() {

    override val id = "atmoaura"
    override val name = "Atmo Auvergne-Rhône-Alpes (${Locale(injectedContext.currentLocale.code, "FR").displayCountry})"
    override val attribution = "Atmo Auvergne-Rhône-Alpes"
    override val privacyPolicyUrl = "https://www.atmo-auvergnerhonealpes.fr/article/politique-de-confidentialite"

    override val context = injectedContext
    override val jsonClient = injectedJsonClient
    override val baseUrl = "https://api.atmo-aura.fr/air2go/v3/"

    override val apiKeyPreference = R.string.settings_weather_source_atmo_aura_api_key
    override val builtInApiKey = BuildConfig.ATMO_AURA_KEY

    override fun isLocationInRegion(location: Location): Boolean {
        return location.admin1 in arrayOf(
            "Auvergne-Rhône-Alpes",
            "Auvergne-Rhone-Alpes",
            "Auvergne Rhône Alpes",
            "Auvergne Rhone Alpes"
        ) ||
            location.admin1Code == "84" ||
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
            location.admin2Code in arrayOf(
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
                "73", // Savoie
                "74" // Haute-Savoie
            )
    }

    override val testingLocations: List<Location> = emptyList()
}
