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

package org.breezyweather.sources.climweb

import android.content.Context
import breezyweather.domain.location.model.Location
import dagger.hilt.android.qualifiers.ApplicationContext
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * Seychelles
 */
class SmaScService @Inject constructor(
    @ApplicationContext injectedContext: Context,
    @Named("JsonClient") val injectedJsonClient: Retrofit.Builder,
) : ClimWebService() {

    override val id = "smasc"
    override val countryCode = "SC"
    override val name = "SMA (${injectedContext.currentLocale.getCountryName(countryCode)})"
    override val privacyPolicyUrl = ""
    override val weatherAttribution = "Seychelles Meteorological Authority"

    override val context = injectedContext
    override val jsonClient = injectedJsonClient
    override val baseUrl = "https://www.meteo.sc/"
    override val instancePreference = R.string.settings_weather_source_sma_sc_instance

    override val alertAttribution = weatherAttribution
    override val normalsAttribution = weatherAttribution

    override val cityClimatePageId = "305"
}
