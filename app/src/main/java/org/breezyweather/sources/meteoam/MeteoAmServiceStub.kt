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

package org.breezyweather.sources.meteoam

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.NonFreeNetSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class MeteoAmServiceStub(context: Context) :
    HttpSource(),
    WeatherSource,
    ReverseGeocodingSource,
    NonFreeNetSource {

    override val id = "meteoam"
    override val name = "Servizio Meteo AM (${context.currentLocale.getCountryName("IT")})"
    override val continent = SourceContinent.EUROPE

    // Required wording for third-party use taken from https://www.meteoam.it/it/condizioni-utilizzo
    private val weatherAttribution = "Servizio Meteorologico dell’Aeronautica Militare. Informazioni elaborate " +
        "utilizzando, tra l’altro, dati e prodotti del Servizio Meteorologico dell’Aeronautica Militare pubblicati " +
        "sul sito www.meteoam.it"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            arrayOf("IT", "SM", "VA").any { location.countryCode.equals(it, ignoreCase = true) } -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    // This source is really inconsistent. Many missing, half correct, half “incorrect”
    override val knownAmbiguousCountryCodes: Array<String> = arrayOf(
        "AU", // Territories: NF
        "CN", // Territories: HK, MO
        "ES", // Nearest location for GI
        "FI", // Territories: AX
        "FR", // Territories: GF, PF, TF (uninhabited), GP, MQ, YT, NC, RE, BL, MF, PM, WF, CP. Claims: AQ
        "IL", // Claims: PS
        "MA", // Claims: EH
        "NL", // Territories: AW, BQ, CW, SX
        "NO", // Territories: SJ
        "US" // Territories: MP, VI
    )
}
