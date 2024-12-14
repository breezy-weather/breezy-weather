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

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import org.breezyweather.BuildConfig
import org.breezyweather.common.source.BroadcastSource
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.PollenIndexSource
import org.breezyweather.common.source.PreferencesParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.Source
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.settings.SourceConfigStore
import org.breezyweather.sources.android.AndroidLocationService
import org.breezyweather.sources.brightsky.BrightSkyService
import org.breezyweather.sources.gadgetbridge.GadgetbridgeService
import org.breezyweather.sources.naturalearth.NaturalEarthService
import org.breezyweather.sources.openmeteo.OpenMeteoService
import org.breezyweather.sources.recosante.RecosanteService
import javax.inject.Inject

class SourceManager @Inject constructor(
    androidLocationService: AndroidLocationService,
    brightSkyService: BrightSkyService,
    gadgetbridgeService: GadgetbridgeService,
    naturalEarthService: NaturalEarthService,
    openMeteoService: OpenMeteoService,
    recosanteService: RecosanteService,
) {
    // TODO: Initialize lazily

    // Location sources
    private val locationSourceList = listOf(
        androidLocationService
    )

    // Reverse geocoding sources
    private val reverseGeocodingSourceList = listOf(
        naturalEarthService
    )

    // Worldwide weather sources, excluding national sources with worldwide support
    private val worldwideWeatherSourceList = listOf(
        openMeteoService
    )

    // Region-specific or national weather sources
    private val nationalWeatherSourceList = listOf(
        brightSkyService,
        recosanteService
    )

    // Broadcast sources
    private val broadcastSourceList = listOf(
        gadgetbridgeService
    )

    // The order of this list is preserved in "source chooser" dialogs
    private val sourceList: List<Source> = buildList {
        addAll(locationSourceList)
        addAll(reverseGeocodingSourceList)
        addAll(worldwideWeatherSourceList)
        addAll(
            nationalWeatherSourceList
            // Only one source in the freenet flavor, so no need to do that atm
            /*.sortedWith { ws1, ws2 ->
                // Sort by name because there are now a lot of sources
                Collator.getInstance(context.currentLocale).compare(ws1.name, ws2.name)
            }*/
        )
        addAll(broadcastSourceList)
    }

    fun getSource(id: String): Source? = sourceList.firstOrNull { it.id == id }
    fun getHttpSources(): List<HttpSource> = sourceList.filterIsInstance<HttpSource>()

    // Location
    fun getLocationSources(): List<LocationSource> = sourceList.filterIsInstance<LocationSource>()
    fun getLocationSource(id: String): LocationSource? = getLocationSources().firstOrNull { it.id == id }
    fun getLocationSourceOrDefault(id: String): LocationSource = getLocationSource(id)
        ?: getLocationSource(BuildConfig.DEFAULT_LOCATION_SOURCE)!!

    // Weather
    fun getWeatherSources(): List<WeatherSource> = sourceList.filterIsInstance<WeatherSource>()
    fun getWeatherSource(id: String): WeatherSource? = getWeatherSources().firstOrNull { it.id == id }
    fun getSupportedWeatherSources(
        feature: SourceFeature? = null,
        location: Location? = null,
        // Optional id of the source that will always be taken, even if not matching the criteria
        sourceException: String? = null,
    ): List<WeatherSource> = getWeatherSources()
        .filter {
            it.id == sourceException ||
                (
                    feature == null ||
                        (
                            it.supportedFeatures.containsKey(feature) &&
                                (
                                    location == null ||
                                        (location.isCurrentPosition && !location.isUsable) ||
                                        it.isFeatureSupportedForLocation(location, feature)
                                    )
                            )
                    )
        }

    // Secondary weather
    fun getPollenIndexSource(id: String): PollenIndexSource? = sourceList.filterIsInstance<PollenIndexSource>()
        .firstOrNull { it.id == id }

    // Location search
    fun getLocationSearchSources(): List<LocationSearchSource> = sourceList.filterIsInstance<LocationSearchSource>()
    fun getLocationSearchSource(id: String): LocationSearchSource? = getLocationSearchSources()
        .firstOrNull { it.id == id }
    fun getLocationSearchSourceOrDefault(id: String): LocationSearchSource = getLocationSearchSource(id)
        ?: getLocationSearchSource(BuildConfig.DEFAULT_LOCATION_SEARCH_SOURCE)!!
    fun getConfiguredLocationSearchSources(): List<LocationSearchSource> = getLocationSearchSources().filter {
        it !is ConfigurableSource || it.isConfigured
    }

    // Reverse geocoding
    fun getReverseGeocodingSources(): List<ReverseGeocodingSource> =
        sourceList.filterIsInstance<ReverseGeocodingSource>()
    fun getReverseGeocodingSource(id: String): ReverseGeocodingSource? = getReverseGeocodingSources()
        .firstOrNull { it.id == id }
    fun getReverseGeocodingSourceOrDefault(id: String): ReverseGeocodingSource = getReverseGeocodingSource(id)
        ?: getReverseGeocodingSource(BuildConfig.DEFAULT_GEOCODING_SOURCE)!!

    // Broadcast
    fun getBroadcastSources(): List<BroadcastSource> = sourceList.filterIsInstance<BroadcastSource>()
    fun isBroadcastSourcesEnabled(context: Context): Boolean {
        return getBroadcastSources().any {
            (SourceConfigStore(context, it.id).getString("packages", null) ?: "").isNotEmpty()
        }
    }

    // Configurables sources
    fun getConfigurableSources(): List<ConfigurableSource> = sourceList.filterIsInstance<ConfigurableSource>()

    fun sourcesWithPreferencesScreen(
        location: Location,
    ): List<PreferencesParametersSource> {
        val preferencesScreenSources = mutableListOf<PreferencesParametersSource>()

        with(location) {
            listOf(
                Pair(forecastSource, SourceFeature.FORECAST),
                Pair(currentSource, SourceFeature.CURRENT),
                Pair(airQualitySource, SourceFeature.AIR_QUALITY),
                Pair(pollenSource, SourceFeature.POLLEN),
                Pair(minutelySource, SourceFeature.MINUTELY),
                Pair(alertSource, SourceFeature.ALERT),
                Pair(normalsSource, SourceFeature.NORMALS)
            ).forEach {
                val source = getWeatherSource(it.first ?: location.forecastSource)
                if (source is PreferencesParametersSource &&
                    source.hasPreferencesScreen(location, listOf(it.second)) &&
                    !preferencesScreenSources.contains(source)
                ) {
                    preferencesScreenSources.add(source)
                }
            }
        }

        return preferencesScreenSources
        /*.sortedWith { s1, s2 ->
            // Sort by name because there are now a lot of sources
            Collator.getInstance(
                SettingsManager.getInstance(context).language.locale
            ).compare(s1.name, s2.name)
        })*/
    }
}
