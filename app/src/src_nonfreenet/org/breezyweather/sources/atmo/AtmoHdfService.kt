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
 * Atmo Hauts-de-France air quality service.
 *
 * Note: this region only has forecast starting from next hour
 */
class AtmoHdfService @Inject constructor(
    @ApplicationContext injectedContext: Context,
    @Named("JsonClient") injectedJsonClient: Retrofit.Builder,
) : AtmoService() {

    override val id = "atmohdf"
    override val name = "Atmo Hauts-de-France (${injectedContext.currentLocale.getCountryName("FR")})"
    override val attribution = "Atmo Hauts-de-France"
    override val attributionLinks = mapOf(
        attribution to "https://www.atmo-hdf.fr/"
    )
    override val privacyPolicyUrl = "https://www.atmo-hdf.fr/article/donnees-personnelles"

    override val context = injectedContext
    override val jsonClient = injectedJsonClient
    override val baseUrl = "https://api.atmo-hdf.fr/airtogo/"
    override val isTokenInHeaders = true

    override val apiKeyPreference = R.string.settings_weather_source_atmo_hdf_api_key
    override val builtInApiKey = BuildConfig.ATMO_HDF_KEY

    override fun isLocationInRegion(location: Location): Boolean {
        return location.admin1 in arrayOf(
            "Hauts-de-France",
            "Hauts de France"
        ) ||
            location.admin1Code == "32" ||
            location.admin2 in arrayOf(
                "Aisne", // 02
                "Nord", // 59
                "North", // 59
                "Oise", // 60
                "Pas-de-Calais", // 62
                "Pas de Calais", // 62
                "Somme" // 80
            ) ||
            location.admin2Code in arrayOf(
                "02", // Aisne
                "59", // Nord
                "60", // Oise
                "62", // Pas-de-Calais
                "80" // Somme
            )
    }

    override val testingLocations: List<Location> = emptyList()
}
