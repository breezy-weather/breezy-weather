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

package org.breezyweather.sources.accu

import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import com.google.maps.android.model.LatLng
import com.google.maps.android.model.LatLngBounds
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.NonFreeNetSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.sources.accu.preferences.AccuPortalPreference
import org.breezyweather.sources.openmeteo.OpenMeteoService.Companion.COPERNICUS_POLLEN_BBOX

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class AccuServiceStub :
    HttpSource(),
    WeatherSource,
    LocationSearchSource,
    ReverseGeocodingSource,
    ConfigurableSource,
    LocationParametersSource,
    NonFreeNetSource {

    override val id = "accu"
    override val name = "AccuWeather"
    override val continent = SourceContinent.WORLDWIDE

    protected val weatherAttribution = "AccuWeather"
    override val locationSearchAttribution = weatherAttribution
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.AIR_QUALITY to weatherAttribution,
        SourceFeature.POLLEN to weatherAttribution,
        SourceFeature.MINUTELY to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.NORMALS to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )

    // We have no way to distinguish the ones below. Others were deduced with other info in the code above
    override val knownAmbiguousCountryCodes = arrayOf(
        "MA", // Claims: EH
        "NO" // Territories: SJ
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return when (feature) {
            SourceFeature.POLLEN -> COPERNICUS_POLLEN_BBOX.contains(LatLng(location.latitude, location.longitude)) || (
                location.countryCode.equals("US", ignoreCase = true) &&
                    CONTIGUOUS_US_STATES_BBOX.contains(LatLng(location.latitude, location.longitude))
                ) || (location.countryCode.equals("CA", ignoreCase = true))
            else ->
                portal == AccuPortalPreference.ENTERPRISE ||
                    feature == SourceFeature.FORECAST ||
                    feature == SourceFeature.CURRENT ||
                    feature == SourceFeature.ALERT ||
                    feature == SourceFeature.REVERSE_GEOCODING
        }
    }

    protected abstract var portal: AccuPortalPreference

    override val isRestricted = false

    companion object {
        // Accuweather's pollen forecast is only available in the 48 contiguous U.S. states + D.C., Canada,
        // and European coverage area of Copernicus. We will limit Pollen Source to these areas.

        // 48 contiguous states boundary taken from Natural Earth Data, extended by 1° in each direction.
        // Source: https://www.naturalearthdata.com/
        private val CONTIGUOUS_US_STATES_BBOX = LatLngBounds(
            LatLng(23.542547919, -125.734607238),
            LatLng(50.369494121, -65.977324999)
        )
    }
}
