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
 * ATMO GrandEst air quality service.
 */
class AtmoGrandEstService @Inject constructor(
    @ApplicationContext injectedContext: Context,
    @Named("JsonClient") injectedJsonClient: Retrofit.Builder,
) : AtmoService() {

    override val id = "atmograndest"
    override val name = "ATMO GrandEst (${Locale(injectedContext.currentLocale.code, "FR").displayCountry})"
    override val attribution = "ATMO GrandEst (licence ODbL https://opendatacommons.org/licenses/odbl )"
    override val privacyPolicyUrl = "https://www.atmo-grandest.eu/article/politique-de-confidentialite"

    override val context = injectedContext
    override val jsonClient = injectedJsonClient
    override val baseUrl = "https://api.atmo-grandest.eu/airtogo/v1/"

    override val apiKeyPreference = R.string.settings_weather_source_atmo_grand_est_api_key
    override val builtInApiKey = BuildConfig.ATMO_GRAND_EST_KEY

    override fun isLocationInRegion(location: Location): Boolean {
        return location.admin1 in arrayOf(
            "Grand Est",
            "GrandEst",
            "Grand-Est"
        ) ||
            location.admin1Code == "44" ||
            location.admin2 in arrayOf(
                "Ardennes", // 08
                "Aube", // 08
                "Marne", // 51
                "Haute-Marne", // 52
                "Haute Marne", // 52
                "Meurthe-et-Moselle", // 54
                "Meurthe et Moselle", // 54
                "Meuse", // 55
                "Moselle", // 57
                "Bas-Rhin", // 67
                "Bas Rhin", // 67
                "Haut-Rhin", // 68
                "Haut Rhin", // 68
                "Vosges" // 88
            ) ||
            location.admin2Code in arrayOf(
                "08", // Ardennes
                "10", // Aube
                "51", // Marne
                "52", // Haute-Marne
                "54", // Meurthe-et-Moselle
                "55", // Meuse
                "57", // Moselle
                "67", // Bas-Rhin
                "68", // Haut-Rhin
                "88" // Vosges
            )
    }
}
