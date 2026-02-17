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

package org.breezyweather.sources.nws

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import org.breezyweather.BuildConfig
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.NonFreeNetSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class NwsServiceStub(context: Context) :
    HttpSource(),
    WeatherSource,
    ReverseGeocodingSource,
    LocationParametersSource,
    NonFreeNetSource {

    override val id = "nws"
    override val name = "NWS (${context.currentLocale.getCountryName("US")})"
    override val continent = SourceContinent.NORTH_AMERICA

    protected val weatherAttribution = "National Weather Service (NWS)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        // This source needs to know how to contact the app maintainers
        // Make sure the app was compiled with the matching property in gradle.properties if failing here
        if (BuildConfig.REPORT_ISSUE.isEmpty()) return false

        return setOf(
            "US",
            "GU", // Guam
            "MP", // Mariana Islands
            "PR", // Puerto Rico
            "VI" // St Thomas Islands
            // The following codes no longer work as of 2025-08-16
            // "FM", // Palikir - no longer works as of 2025-08-16
            // "PW", // Melekeok - no longer works as of 2025-08-16
            // "AS", // Pago Pago - no longer works as of 2025-08-16 - returns a result but no grid data
            // "UM", "XB", "XH", "XQ", "XU", "XM", "QM", "XV", "XL", "QW" // Minor Outlying Islands
            // Minor Outlying Islands are largely uninhabited, except for temporary U.S. military staff
        ).any {
            location.countryCode.equals(it, ignoreCase = true)
        }
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override val knownAmbiguousCountryCodes: Array<String> = emptyArray()
}
