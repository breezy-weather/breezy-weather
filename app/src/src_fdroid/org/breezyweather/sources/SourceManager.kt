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

package org.breezyweather.sources

import org.breezyweather.BuildConfig
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.Source
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.sources.android.AndroidLocationSource
import org.breezyweather.sources.naturalearth.NaturalEarthService
import org.breezyweather.sources.openmeteo.OpenMeteoService
import javax.inject.Inject

class SourceManager @Inject constructor(
    androidLocationSource: AndroidLocationSource,
    naturalEarthService: NaturalEarthService,
    openMeteoService: OpenMeteoService
) {
    // TODO: Initialize lazily
    // The order of this list is preserved in "source chooser" dialogs
    private val sourceList: List<Source> = listOf(
        // Location sources
        androidLocationSource,

        // Reverse geocoding sources
        naturalEarthService,

        // Weather sources
        openMeteoService
    )

    fun getSource(id: String): Source? = sourceList.firstOrNull { it.id == id }
    fun getHttpSources(): List<HttpSource> = sourceList.filterIsInstance<HttpSource>()

    // Location
    fun getLocationSources(): List<LocationSource> = sourceList.filterIsInstance<LocationSource>()
    fun getLocationSource(id: String): LocationSource? = getLocationSources().firstOrNull { it.id == id }
    fun getLocationSourceOrDefault(id: String): LocationSource = getLocationSource(id)
        ?: getLocationSource(BuildConfig.DEFAULT_LOCATION_SOURCE)!!

    // Weather
    fun getMainWeatherSources(): List<MainWeatherSource> = sourceList.filterIsInstance<MainWeatherSource>()
    fun getMainWeatherSource(id: String): MainWeatherSource? = getMainWeatherSources().firstOrNull { it.id == id }
    fun getConfiguredMainWeatherSources(): List<MainWeatherSource> = getMainWeatherSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }

    // Secondary weather
    fun getSecondaryWeatherSources(): List<SecondaryWeatherSource> = sourceList.filterIsInstance<SecondaryWeatherSource>()
    fun getSecondaryWeatherSource(id: String): SecondaryWeatherSource? = getSecondaryWeatherSources().firstOrNull { it.id == id }

    // Location search
    fun getLocationSearchSources(): List<LocationSearchSource> = sourceList.filterIsInstance<LocationSearchSource>()
    fun getLocationSearchSource(id: String): LocationSearchSource? = getLocationSearchSources().firstOrNull { it.id == id }
    fun getLocationSearchSourceOrDefault(id: String): LocationSearchSource = getLocationSearchSource(id)
        ?: getLocationSearchSource(BuildConfig.DEFAULT_LOCATION_SEARCH_SOURCE)!!
    fun getConfiguredLocationSearchSources(): List<LocationSearchSource> = getLocationSearchSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }

    // Reverse geocoding
    fun getReverseGeocodingSources(): List<ReverseGeocodingSource> = sourceList.filterIsInstance<ReverseGeocodingSource>()
    fun getReverseGeocodingSource(id: String): ReverseGeocodingSource? = getReverseGeocodingSources().firstOrNull { it.id == id }
    fun getReverseGeocodingSourceOrDefault(id: String): ReverseGeocodingSource = getReverseGeocodingSource(id)
        ?: getReverseGeocodingSource(BuildConfig.DEFAULT_GEOCODING_SOURCE)!!

    // Configurables sources
    fun getConfigurableSources(): List<ConfigurableSource> = sourceList.filterIsInstance<ConfigurableSource>()

}
