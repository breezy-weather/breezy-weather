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
import android.graphics.Color
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.wrappers.SecondaryWeatherWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.BuildConfig
import org.breezyweather.common.exceptions.InvalidLocationException
import org.breezyweather.common.exceptions.SecondaryWeatherException
import org.breezyweather.common.extensions.code
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.LocationParametersSource
import org.breezyweather.common.source.MainWeatherSource
import org.breezyweather.common.source.ReverseGeocodingSource
import org.breezyweather.common.source.SecondaryWeatherSource
import org.breezyweather.sources.nws.json.NwsAlertsResult
import org.breezyweather.sources.nws.json.NwsCurrentResult
import retrofit2.Retrofit
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class NwsService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), MainWeatherSource, SecondaryWeatherSource, ReverseGeocodingSource, LocationParametersSource {

    override val id = "nws"
    override val name = "NWS (${Locale(context.currentLocale.code, "US").displayCountry})"
    override val continent = SourceContinent.NORTH_AMERICA
    override val privacyPolicyUrl = "https://www.weather.gov/privacy"

    override val color = Color.rgb(51, 176, 225)
    override val weatherAttribution = "National Weather Service (NWS)"

    private val mApi by lazy {
        client
            .baseUrl(NWS_BASE_URL)
            .build()
            .create(NwsApi::class.java)
    }

    override val supportedFeaturesInMain = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_ALERT
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

    override fun isFeatureSupportedInMainForLocation(
        location: Location,
        feature: SourceFeature?,
    ): Boolean {
        return supportedCountries.any {
            location.countryCode.equals(it, ignoreCase = true)
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        ignoreFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        val gridId = location.parameters.getOrElse(id) { null }?.getOrElse("gridId") { null }
        val gridX = location.parameters.getOrElse(id) { null }?.getOrElse("gridX") { null }
        val gridY = location.parameters.getOrElse(id) { null }?.getOrElse("gridY") { null }
        val station = location.parameters.getOrElse(id) { null }?.getOrElse("station") { null }

        if (gridId.isNullOrEmpty() ||
            gridX.isNullOrEmpty() ||
            gridY.isNullOrEmpty() ||
            (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT) && station.isNullOrEmpty())
        ) {
            return Observable.error(InvalidLocationException())
        }

        val nwsForecastResult = mApi.getForecast(
            USER_AGENT,
            gridId,
            gridX.toInt(),
            gridY.toInt()
        )

        val nwsDailyResult = mApi.getDaily(
            userAgent = USER_AGENT,
            gridId = gridId,
            gridX = gridX.toInt(),
            gridY = gridY.toInt()
        )

        val failedFeatures = mutableListOf<SourceFeature>()

        val nwsCurrentResult = if (!ignoreFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            mApi.getCurrent(
                USER_AGENT,
                station!!
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(NwsCurrentResult())
            }
        } else {
            Observable.just(NwsCurrentResult())
        }

        val nwsAlertsResult = if (!ignoreFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getActiveAlerts(
                USER_AGENT,
                "${location.latitude},${location.longitude}"
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_ALERT)
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
            convert(
                context = context,
                currentResult = currentResult,
                dailyResult = dailyResult,
                forecastResult = forecastResult,
                alertResult = alertResult,
                location = location,
                failedFeatures = failedFeatures
            )
        }
    }

    // SECONDARY WEATHER SOURCE
    override val supportedFeaturesInSecondary = listOf(
        SourceFeature.FEATURE_CURRENT,
        SourceFeature.FEATURE_ALERT
    )
    override fun isFeatureSupportedInSecondaryForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return isFeatureSupportedInMainForLocation(location, feature)
    }
    override val currentAttribution = weatherAttribution
    override val airQualityAttribution = null
    override val pollenAttribution = null
    override val minutelyAttribution = null
    override val alertAttribution = weatherAttribution
    override val normalsAttribution = null

    override fun requestSecondaryWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<SecondaryWeatherWrapper> {
        if (!isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_CURRENT) ||
            !isFeatureSupportedInSecondaryForLocation(location, SourceFeature.FEATURE_ALERT)
        ) {
            // TODO: return Observable.error(UnsupportedFeatureForLocationException())
            return Observable.error(SecondaryWeatherException())
        }
        val station = location.parameters.getOrElse(id) { null }?.getOrElse("station") { null }
        if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT) && station.isNullOrEmpty()) {
            return Observable.error(InvalidLocationException())
        }

        val failedFeatures = mutableListOf<SourceFeature>()
        val current = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
            if (!station.isNullOrEmpty()) {
                mApi.getCurrent(
                    USER_AGENT,
                    station
                ).onErrorResumeNext {
                    failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                    Observable.just(NwsCurrentResult())
                }
            } else {
                failedFeatures.add(SourceFeature.FEATURE_CURRENT)
                Observable.just(NwsCurrentResult())
            }
        } else {
            Observable.just(NwsCurrentResult())
        }

        val alerts = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
            mApi.getActiveAlerts(
                USER_AGENT,
                "${location.latitude},${location.longitude}"
            ).onErrorResumeNext {
                failedFeatures.add(SourceFeature.FEATURE_ALERT)
                Observable.just(NwsAlertsResult())
            }
        } else {
            Observable.just(NwsAlertsResult())
        }

        return Observable.zip(current, alerts) {
                currentResult: NwsCurrentResult,
                alertsResult: NwsAlertsResult,
            ->
            convertSecondary(
                currentResult = if (requestedFeatures.contains(SourceFeature.FEATURE_CURRENT)) {
                    currentResult
                } else {
                    null
                },
                alertsResult = if (requestedFeatures.contains(SourceFeature.FEATURE_ALERT)) {
                    alertsResult
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
        // if (features.contains(SourceFeature.FEATURE_ALERT)) return false

        // Commented the line above for now, because location parameters are still needed,
        // if NWS is used as secondary source for both CURRENT and ALERT.

        if (coordinatesChanged) return true

        val currentGridId = location.parameters.getOrElse(id) { null }?.getOrElse("gridId") { null }
        val currentGridX = location.parameters.getOrElse(id) { null }?.getOrElse("gridX") { null }
        val currentGridY = location.parameters.getOrElse(id) { null }?.getOrElse("gridY") { null }
        val currentStation = if (features.contains(SourceFeature.FEATURE_CURRENT)) {
            location.parameters.getOrElse(id) { null }?.getOrElse("station") { null }
        } else {
            null
        }

        return currentGridId.isNullOrEmpty() ||
            currentGridX.isNullOrEmpty() ||
            currentGridY.isNullOrEmpty() ||
            (features.contains(SourceFeature.FEATURE_CURRENT) && currentStation.isNullOrEmpty())
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
                it.properties.gridX.toInt(),
                it.properties.gridY.toInt()
            ).blockingFirst()
            if (stations.features?.firstOrNull()?.properties?.stationIdentifier.isNullOrEmpty()) {
                throw InvalidLocationException()
            }
            mapOf(
                "gridId" to it.properties.gridId,
                "gridX" to it.properties.gridX.toString(),
                "gridY" to it.properties.gridY.toString(),
                "station" to stations.features.first().properties?.stationIdentifier!!
            )
        }
    }

    companion object {
        private const val NWS_BASE_URL = "https://api.weather.gov/"
        private const val USER_AGENT =
            "(BreezyWeather/${BuildConfig.VERSION_NAME}, github.com/breezy-weather/breezy-weather/issues)"
    }
}
