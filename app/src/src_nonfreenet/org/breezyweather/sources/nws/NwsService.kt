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

package org.breezyweather.sources.nws

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.OutdatedServerDataException
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.extensions.getCountryName
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.nws.json.NwsAlertsResult
import org.breezyweather.sources.nws.json.NwsCurrentResult
import org.breezyweather.sources.nws.json.NwsDailyResult
import org.breezyweather.sources.nws.json.NwsGridPointResult
import retrofit2.Retrofit
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.hours

class NwsService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "nws"
    override val name = "NWS (${context.currentLocale.getCountryName("US")})"
    override val continent = SourceContinent.NORTH_AMERICA
    override val privacyPolicyUrl = "https://www.weather.gov/privacy"

    private val mApi by lazy {
        client
            .baseUrl(NWS_BASE_URL)
            .build()
            .create(NwsApi::class.java)
    }

    private val weatherAttribution = "National Weather Service (NWS)"
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.CURRENT to weatherAttribution,
        SourceFeature.ALERT to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )
    override val attributionLinks = mapOf(
        weatherAttribution to "https://www.weather.gov/"
    )

    private val supportedCountries = setOf(
        "US",
        "PR", // Puerto Rico
        "VI", // St Thomas Islands
        "MP", // Mariana Islands
        "GU" // Guam
        // The following codes no longer work as of 2024-11-21
        // "FM", // Palikir - no longer works as of 2024-11-21
        // "PW", // Melekeok - no longer works as of 2024-11-21
        // "AS", // Pago Pago - no longer works as of 2024-11-21
        // "UM", "XB", "XH", "XQ", "XU", "XM", "QM", "XV", "XL", "QW" // Minor Outlying Islands
        // Minor Outlying Islands are largely uninhabited, except for temporary U.S. military staff
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return supportedCountries.any {
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

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val gridId = location.parameters.getOrElse(id) { null }?.getOrElse("gridId") { null }
        val gridX = location.parameters.getOrElse(id) { null }?.getOrElse("gridX") { null }
        val gridY = location.parameters.getOrElse(id) { null }?.getOrElse("gridY") { null }
        val station = location.parameters.getOrElse(id) { null }?.getOrElse("station") { null }

        if (gridId.isNullOrEmpty() ||
            gridX.isNullOrEmpty() ||
            gridY.isNullOrEmpty() ||
            (SourceFeature.CURRENT in requestedFeatures && station.isNullOrEmpty())
        ) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        val nwsForecastResult = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getForecast(
                USER_AGENT,
                gridId,
                gridX.toInt(),
                gridY.toInt()
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(NwsGridPointResult())
            }
        } else {
            Observable.just(NwsGridPointResult())
        }

        val nwsDailyResult = if (SourceFeature.FORECAST in requestedFeatures) {
            mApi.getDaily(
                userAgent = USER_AGENT,
                gridId = gridId,
                gridX = gridX.toInt(),
                gridY = gridY.toInt()
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.FORECAST] = it
                Observable.just(NwsDailyResult())
            }
        } else {
            Observable.just(NwsDailyResult())
        }

        val nwsCurrentResult = if (SourceFeature.CURRENT in requestedFeatures) {
            mApi.getCurrent(
                USER_AGENT,
                station!!
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.CURRENT] = it
                Observable.just(NwsCurrentResult())
            }
        } else {
            Observable.just(NwsCurrentResult())
        }

        val nwsAlertsResult = if (SourceFeature.ALERT in requestedFeatures) {
            mApi.getActiveAlerts(
                USER_AGENT,
                "${location.latitude},${location.longitude}"
            ).onErrorResumeNext {
                failedFeatures[SourceFeature.ALERT] = it
                Observable.just(NwsAlertsResult())
            }
        } else {
            Observable.just(NwsAlertsResult())
        }

        return Observable.zip(
            nwsForecastResult,
            nwsDailyResult,
            nwsCurrentResult,
            nwsAlertsResult
        ) { forecastResult, dailyResult, currentResult, alertResult ->
            val current = if (SourceFeature.CURRENT in requestedFeatures) {
                // Unfortunately, some stations report their update time as UTC while it’s actually local time
                // So we need to subtract the timezone offset just to be safe
                if (currentResult.properties?.timestamp != null &&
                    currentResult.properties.timestamp.time < Date().time -
                    OUTDATED_HOURS.hours.inWholeMilliseconds + // Offset on next line is negative, don’t subtract here!
                    location.javaTimeZone.rawOffset // In milliseconds
                ) {
                    failedFeatures[SourceFeature.CURRENT] = OutdatedServerDataException()
                    null
                } else {
                    getCurrent(currentResult)
                }
            } else {
                null
            }

            WeatherWrapper(
                dailyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getDailyForecast(location, dailyResult)
                } else {
                    null
                },
                hourlyForecast = if (SourceFeature.FORECAST in requestedFeatures) {
                    getHourlyForecast(forecastResult.properties, location, context)
                } else {
                    null
                },
                current = current,
                alertList = if (SourceFeature.ALERT in requestedFeatures) {
                    getAlerts(alertResult.features)
                } else {
                    null
                },
                failedFeatures = failedFeatures
            )
        }
    }

    // Reverse geocoding
    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        return mApi.getPoints(
            USER_AGENT,
            location.latitude,
            location.longitude
        ).map {
            if (it.properties == null) {
                throw InvalidLocationException()
            }
            val locationList = mutableListOf<Location>()
            locationList.add(convert(location, it.properties))
            locationList
        }
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        // Not needed for alert endpoint
        // if (SourceFeature.FEATURE_ALERT in features) return false

        // Commented the line above for now, because location parameters are still needed,
        // if NWS is used as secondary source for both CURRENT and ALERT.

        if (coordinatesChanged) return true

        val currentGridId = location.parameters.getOrElse(id) { null }?.getOrElse("gridId") { null }
        val currentGridX = location.parameters.getOrElse(id) { null }?.getOrElse("gridX") { null }
        val currentGridY = location.parameters.getOrElse(id) { null }?.getOrElse("gridY") { null }
        val currentStation = if (SourceFeature.CURRENT in features) {
            location.parameters.getOrElse(id) { null }?.getOrElse("station") { null }
        } else {
            null
        }

        return currentGridId.isNullOrEmpty() ||
            currentGridX.isNullOrEmpty() ||
            currentGridY.isNullOrEmpty() ||
            (SourceFeature.CURRENT in features && currentStation.isNullOrEmpty())
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        return mApi.getPoints(
            USER_AGENT,
            location.latitude,
            location.longitude
        ).map {
            if (it.properties == null) {
                throw InvalidLocationException()
            }
            val stations = mApi.getStations(
                USER_AGENT,
                it.properties.gridId,
                it.properties.gridX,
                it.properties.gridY
            ).blockingFirst()

            buildMap {
                put("gridId", it.properties.gridId)
                put("gridX", it.properties.gridX.toString())
                put("gridY", it.properties.gridY.toString())
                stations.features?.firstOrNull()?.properties?.stationIdentifier?.let { stationId ->
                    // Only needed if requesting current, so can safely be avoided in some cases
                    put("station", stationId)
                }
            }
        }
    }

    override val testingLocations: List<Location> = emptyList()

    companion object {
        // Number of hours after which a station data is considered outdated
        // TODO: Move this to global for all sources
        private const val OUTDATED_HOURS = 2

        private const val NWS_BASE_URL = "https://api.weather.gov/"
        private const val USER_AGENT =
            "(BreezyWeather/${BuildConfig.VERSION_NAME}, github.com/breezy-weather/breezy-weather/issues)"
    }
}
