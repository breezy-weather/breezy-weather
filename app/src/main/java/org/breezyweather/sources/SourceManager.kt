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

package org.breezyweather.sources

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceFeature
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.breezyweather.BuildConfig
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.LocationSearchSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.source.PreferencesParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.domain.settings.SourceConfigStore

/**
 * Contains extensions to SourceManager
 * You can find the actual SourceManager class is in each flavor folder
 */
fun SourceManager.getLocationSource(id: String): LocationSource? = getLocationSources().firstOrNull { it.id == id }
fun SourceManager.getLocationSourceOrDefault(id: String): LocationSource = getLocationSource(id)
    ?: getLocationSource(BuildConfig.DEFAULT_LOCATION_SOURCE)!!

// Weather
fun SourceManager.getWeatherSource(id: String): WeatherSource? = getWeatherSources().firstOrNull { it.id == id }
fun SourceManager.getSupportedWeatherSources(
    feature: SourceFeature? = null,
    location: Location? = null,
    // Optional id of the source that will always be taken, even if not matching the criteria
    sourceException: String? = null,
): ImmutableList<WeatherSource> = getWeatherSources()
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
    }.toImmutableList()

fun SourceManager.getLocationSearchSource(id: String): LocationSearchSource? = getLocationSearchSources()
    .firstOrNull { it.id == id }
fun SourceManager.getLocationSearchSourceOrDefault(id: String): LocationSearchSource = getLocationSearchSource(id)
    ?: getLocationSearchSource(BuildConfig.DEFAULT_LOCATION_SEARCH_SOURCE)!!
fun SourceManager.getConfiguredLocationSearchSources(): ImmutableList<LocationSearchSource> = getLocationSearchSources()
    .filter { it !is ConfigurableSource || it.isConfigured }
    .toImmutableList()

fun SourceManager.getSupportedReverseGeocodingSources(
    location: Location? = null,
): ImmutableList<ReverseGeocodingSource> = getReverseGeocodingSources()
    .filter {
        it.id != "naturalearth" &&
            (
                location == null ||
                    (location.isCurrentPosition && !location.isUsable) ||
                    it.isReverseGeocodingSupportedForLocation(location)
                )
    }.toImmutableList()
fun SourceManager.getReverseGeocodingSource(id: String): ReverseGeocodingSource? = getReverseGeocodingSources()
    .firstOrNull { it.id == id }
fun SourceManager.getReverseGeocodingSourceOrDefault(id: String): ReverseGeocodingSource = getReverseGeocodingSource(id)
    ?: getReverseGeocodingSource(BuildConfig.DEFAULT_GEOCODING_SOURCE)!!

fun SourceManager.isBroadcastSourcesEnabled(context: Context): Boolean {
    return getBroadcastSources().any {
        (SourceConfigStore(context, it.id).getString("packages", null) ?: "").isNotEmpty()
    }
}

fun SourceManager.sourcesWithPreferencesScreen(
    location: Location,
): ImmutableList<PreferencesParametersSource> {
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
        .toImmutableList()
}

/**
 * Best source is determined using the priority given by sources, excluding unconfigured and restricted sources
 */
fun SourceManager.getBestSourceForFeature(
    location: Location,
    feature: SourceFeature,
): WeatherSource? {
    return getSupportedWeatherSources(feature, location)
        .filter {
            it.isFeatureSupportedForLocation(location, feature) &&
                it.getFeaturePriorityForLocation(location, feature) > PRIORITY_NONE &&
                (it !is ConfigurableSource || (it.isConfigured && !it.isRestricted))
        }
        .maxByOrNull { it.getFeaturePriorityForLocation(location, feature) }
}

/**
 * For pollen:
 * - AccuWeather in USA/Canada (unclear which other countries are supported)
 * - Open-Meteo in Europe
 * - None in other countries
 * For alerts, default source is AccuWeather (will be FPAS in the future)
 * For normals, default source is AccuWeather (may be NCEI in the future), unless:
 * - Current location: excluded until #1996 is implemented
 * - In China: no normals source, due to firewall
 * For other cases, default source is Open-Meteo
 */
fun SourceManager.getDefaultSourceForFeature(
    location: Location,
    feature: SourceFeature,
): WeatherSource? {
    return when (feature) {
        SourceFeature.POLLEN -> if (arrayOf("US", "CA").any { it.equals(location.countryCode, ignoreCase = true) }) {
            getWeatherSource("accu")
        } else {
            getWeatherSource("openmeteo")?.let {
                when {
                    !it.isFeatureSupportedForLocation(location, feature) -> null
                    else -> it
                }
            }
        }
        SourceFeature.ALERT -> getWeatherSource("accu")
        SourceFeature.NORMALS -> if (!location.isCurrentPosition &&
            !location.countryCode.equals("CN", ignoreCase = true)
        ) {
            getWeatherSource("accu")
        } else {
            null
        }
        else -> getWeatherSource("openmeteo")
    }
}
fun SourceManager.getBestSourceForFeatureOrDefault(
    location: Location,
    feature: SourceFeature,
): WeatherSource? {
    return getBestSourceForFeature(location, feature)
        ?: getDefaultSourceForFeature(location, feature)
}
