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

package org.breezyweather.sources.recosante

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.exceptions.SourceNotInstalledException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_MEDIUM
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import javax.inject.Inject

/**
 * Recosanté pollen service.
 */
class RecosanteService @Inject constructor(
    @ApplicationContext context: Context,
) : HttpSource(), WeatherSource {

    override val id = "recosante"
    override val name = "Recosanté (${context.currentLocale.getCountryName("FR")})"
    override val continent = SourceContinent.EUROPE
    override val privacyPolicyUrl = "https://recosante.beta.gouv.fr/donnees-personnelles/"

    override val supportedFeatures = mapOf(
        SourceFeature.POLLEN to "Recosanté • Atmo France"
    )
    override val attributionLinks = mapOf(
        "Recosanté" to "https://recosante.beta.gouv.fr/",
        "Atmo France" to "https://www.atmo-france.org/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return !location.countryCode.isNullOrEmpty() && location.countryCode.equals("FR", ignoreCase = true)
    }

    /**
     * Medium priority because it only has index, not concentrations
     */
    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_MEDIUM
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        throw SourceNotInstalledException()
    }
}
