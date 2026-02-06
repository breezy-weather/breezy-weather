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

package org.breezyweather.sources.mf

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.ConfigurableSource
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
abstract class MfServiceStub(context: Context) :
    HttpSource(),
    WeatherSource,
    ReverseGeocodingSource,
    LocationParametersSource,
    ConfigurableSource,
    NonFreeNetSource {

    override val id = "mf"
    private val countryName = context.currentLocale.getCountryName("FR")
    override val name = "Météo-France".let {
        if (it.contains(countryName)) {
            it
        } else {
            "$it ($countryName)"
        }
    }
    override val continent = SourceContinent.EUROPE

    private val weatherAttribution = "Météo-France (Etalab)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            SourceFeature.CURRENT -> !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("FR", ignoreCase = true)
            SourceFeature.MINUTELY -> !location.countryCode.isNullOrEmpty() &&
                location.countryCode.equals("FR", ignoreCase = true)
            SourceFeature.ALERT -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("FR", "AD", "BL", "GF", "GP", "MF", "MQ", "NC", "PF", "PM", "RE", "WF", "YT")
                    .any { location.countryCode.equals(it, ignoreCase = true) }
            SourceFeature.NORMALS -> !location.countryCode.isNullOrEmpty() &&
                arrayOf("FR", "AD", "MC", "BL", "GF", "GP", "MF", "MQ", "NC", "PF", "PM", "RE", "WF", "YT")
                    .any { location.countryCode.equals(it, ignoreCase = true) }
            SourceFeature.FORECAST, SourceFeature.REVERSE_GEOCODING -> true // Main source available worldwide
            else -> false
        }
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(
                location,
                // Since forecast and reverse geocoding are available worldwide, use the same criteria as normals
                if (feature in arrayOf(SourceFeature.FORECAST, SourceFeature.REVERSE_GEOCODING)) {
                    SourceFeature.NORMALS
                } else {
                    feature
                }
            ) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    /**
     * The main issue on this source is that there are not many nearest locations recognized, so the resolutions are
     *  wrong on small regions like:
     * - Macau (nearest on China side, making "CN" ambiguous)
     * - For others, we were able to deduce from the timezone (no idea why it didn't work for Macau)
     *
     * When the nearest point is more than 50 km away, the app rejects the result, so we are safe for non-recognized
     *  uninhabited islands (if anyone actually need it)
     */
    override val knownAmbiguousCountryCodes: Array<String> = arrayOf("CN") // Territories: MO
}
