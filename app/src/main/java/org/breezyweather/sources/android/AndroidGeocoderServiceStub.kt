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

package org.breezyweather.sources.android

import android.location.Geocoder
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import org.breezyweather.common.source.NonFreeNetSource
import org.breezyweather.common.source.ReverseGeocodingSource

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class AndroidGeocoderServiceStub() :
    ReverseGeocodingSource,
    NonFreeNetSource {

    override val id = "nativegeocoder"
    override val name = "Android"

    override val supportedFeatures = mapOf(
        SourceFeature.REVERSE_GEOCODING to name
    )

    override fun isFeatureSupportedForLocation(location: Location, feature: SourceFeature): Boolean {
        return Geocoder.isPresent()
    }

    // Each device can implement their own geocoder, so better be safe
    override val knownAmbiguousCountryCodes: Array<String>? = null
}
